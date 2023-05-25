package org.tsd.tsdtv;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class FfprobeTest {

    @Test
    public void testProbe() throws Exception{
        FFprobe fFprobe = new FFprobe("C:\\ffmpeg-3.3.3-win64-static\\bin\\ffprobe.exe");
        FFmpegProbeResult probeResult = fFprobe.probe("C:\\Users\\Joe\\Videos\\tsdtv\\01.mkv");
        int i=0;
    }

    @Test
    public void testStream() throws Exception {
        FFmpeg fFmpeg = new FFmpeg("C:\\ffmpeg-3.3.3-win64-static\\bin\\ffmpeg.exe");
        FFprobe fFprobe = new FFprobe("C:\\ffmpeg-3.3.3-win64-static\\bin\\ffprobe.exe");

        FFmpegBuilder builder = fFmpeg.builder()
                .setInput("C:\\Users\\Joe\\Videos\\tsdtv\\01.mkv")
                .addOutput("rtmp://tsdtv.teamschoolyd.net/tsdtvApp/tsdtv")
                .setFormat("flv")

                .setAudioCodec("aac")
                .setAudioSampleRate(44_100)
                .setAudioBitRate(128_000)

                .setVideoCodec("libx264")
                .setVideoFrameRate(24, 1)
                .setVideoBitRate(1200_000)
                .setVideoFilter("subtitles='C\\:/Users/Joe/Videos/tsdtv/01\\.mkv'")
                .setVideoPixelFormat("yuv420p")

                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);

        executor.createJob(builder).run();
    }
}
