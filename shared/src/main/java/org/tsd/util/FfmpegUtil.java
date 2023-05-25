package org.tsd.util;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.rest.v1.tsdtv.Media;
import org.tsd.rest.v1.tsdtv.MediaInfo;
import org.tsd.rest.v1.tsdtv.stream.AudioStream;
import org.tsd.rest.v1.tsdtv.stream.SubtitleStream;
import org.tsd.rest.v1.tsdtv.stream.VideoStream;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FfmpegUtil {

    private static final Logger log = LoggerFactory.getLogger(FfmpegUtil.class);

    private static final String LANGUAGE_TAG = "language";

    // amount of total bandwidth used for streaming, 8%
    private static final double STREAMING_PERCENTAGE = 0.08;

    // amount of streaming bandwidth used for video, 80%
    private static final double VIDEO_PERCENTAGE = 0.8;

    private static final long DEFAULT_AUDIO_BITRATE =  128_000;
    private static final long DEFAULT_VIDEO_BITRATE = 1200_000;


    public static MediaInfo getMediaInfo(FFprobe fFprobe, File file) throws IOException {
        log.debug("Getting media info for file: {}", file);
        FFmpegProbeResult probeResult = fFprobe.probe(file.getAbsolutePath());
        FFmpegFormat format = probeResult.getFormat();

        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.setFilePath(file.getAbsolutePath());
        mediaInfo.setFileSize(format.size);
        mediaInfo.setDurationSeconds((int)format.duration);
        mediaInfo.setBitRate(format.bit_rate);

        for (FFmpegStream fFmpegStream : probeResult.getStreams()) {
            if (fFmpegStream.codec_type != null) {
                switch (fFmpegStream.codec_type) {
                    case VIDEO: {
                        VideoStream videoStream = new VideoStream();
                        populateStreamInfo(videoStream, fFmpegStream);
                        videoStream.setWidth(fFmpegStream.width);
                        videoStream.setHeight(fFmpegStream.height);
                        videoStream.setSampleAspectRatio(fFmpegStream.sample_aspect_ratio);
                        videoStream.setDisplayAspectRatio(fFmpegStream.display_aspect_ratio);
                        videoStream.setPixFmt(fFmpegStream.pix_fmt);
                        videoStream.setAvc(Boolean.parseBoolean(fFmpegStream.is_avc));
                        videoStream.setrFrameRate(fFmpegStream.r_frame_rate.doubleValue());
                        videoStream.setAvgFrameRate(fFmpegStream.avg_frame_rate.doubleValue());
                        log.debug("Parsed video stream: {}", videoStream);
                        mediaInfo.getVideoStreams().add(videoStream);
                        break;
                    }
                    case AUDIO: {
                        AudioStream audioStream = new AudioStream();
                        populateStreamInfo(audioStream, fFmpegStream);
                        audioStream.setChannelLayout(fFmpegStream.channel_layout);
                        audioStream.setLanguage(detectLanguage(audioStream));
                        audioStream.setSampleRate(fFmpegStream.sample_rate);
                        log.debug("Parsed audio stream: {}", audioStream);
                        mediaInfo.getAudioStreams().add(audioStream);
                        break;
                    }
                    case SUBTITLE: {
                        SubtitleStream subtitleStream = new SubtitleStream();
                        populateStreamInfo(subtitleStream, fFmpegStream);
                        subtitleStream.setLanguage(detectLanguage(subtitleStream));
                        log.debug("Parsed subtitle stream: {}", subtitleStream);
                        mediaInfo.getSubtitleStreams().add(subtitleStream);
                        break;
                    }
                }
            }
        }

        log.debug("Parsed media info: {}", mediaInfo);
        return mediaInfo;
    }

    private static void populateStreamInfo(org.tsd.rest.v1.tsdtv.stream.Stream tsdtvStream,
                                           FFmpegStream fFmpegStream) {
        tsdtvStream.setIndex(fFmpegStream.index);
        tsdtvStream.setCodecName(fFmpegStream.codec_name);
        if (fFmpegStream.tags != null) {
            tsdtvStream.setTags(new HashMap<>(fFmpegStream.tags));
        }
    }

    private static String detectLanguage(org.tsd.rest.v1.tsdtv.stream.Stream tsdtvStream) {
        return tsdtvStream.getTags().entrySet().stream()
                .filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey(), LANGUAGE_TAG))
                .map(Map.Entry::getValue)
                .findAny().orElse(null);
    }

    public static FFmpegBuilder buildFfmpeg(Media media, String tsdtvUrl, Long availableBandwidthBits) {

        long videoBitrate = DEFAULT_VIDEO_BITRATE;
        long audioBitrate = DEFAULT_AUDIO_BITRATE;

        if (availableBandwidthBits != null && availableBandwidthBits > 0) {
            long availableBandwidthForStreaming = (long) (availableBandwidthBits * STREAMING_PERCENTAGE);
            videoBitrate = Math.min( (long) (availableBandwidthForStreaming*VIDEO_PERCENTAGE), DEFAULT_VIDEO_BITRATE );
            audioBitrate = Math.min( availableBandwidthForStreaming-videoBitrate, DEFAULT_AUDIO_BITRATE );
        }

        log.info("availableBandwidth={}, video={}, audio={}", availableBandwidthBits, videoBitrate, audioBitrate);

        FFmpegOutputBuilder outputBuilder = new FFmpegBuilder()
                .addExtraArgs("-re") // stream at native frame rate
                .setInput(media.getMediaInfo().getFilePath())
                .addOutput(tsdtvUrl)
                .setFormat("flv")

                .setAudioCodec("aac")
                .setAudioSampleRate(44_100)
                .setAudioBitRate(audioBitrate)

                .setVideoCodec("libx264")
                .setVideoFrameRate(24, 1)
                .setVideoBitRate(videoBitrate)
                .setVideoPixelFormat("yuv420p")

                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL);

        if (CollectionUtils.isNotEmpty(media.getMediaInfo().getSubtitleStreams())) {
            outputBuilder.setVideoFilter("subtitles='"+escapeSubtitlePath(media.getMediaInfo().getFilePath())+"'");
        }

        return outputBuilder.done();
    }

    private static String escapeSubtitlePath(String filePath) {
        log.debug("Escaping subtitle path: {}", filePath);
        filePath = filePath.replaceAll("\\\\", "/");
        filePath = filePath.replaceAll(":", "\\\\:");
        filePath = filePath.replaceAll("\\.", "\\\\.");
        log.debug("Escaped subtitle path: {}", filePath);
        return filePath;
    }
}
