package org.tsd.tsdtv;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.rest.v1.tsdtv.Media;
import org.tsd.util.FfmpegUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Singleton
public class TSDTVPlayer {

    private static final Logger log = LoggerFactory.getLogger(TSDTVPlayer.class);

    private static final long NANNY_SLEEP_PERIOD_SECONDS = 3;

    private FFmpegJob runningStream = null;

    private final FFmpeg fFmpeg;
    private final FFprobe fFprobe;
    private final ExecutorService executorService;

    @Inject
    public TSDTVPlayer(ExecutorService executorService,
                       FFmpeg fFmpeg,
                       FFprobe fFprobe) {
        this.fFmpeg = fFmpeg;
        this.fFprobe = fFprobe;
        this.executorService = executorService;
    }

    public void play(Media media,
                     String targetUrl,
                     Long availableBandwidth,
                     Consumer<FFmpegJob.State> handleEnd) throws Exception {
        log.info("Playing media, targetUrl={}, media={}", targetUrl, media);

        FFmpegBuilder builder = FfmpegUtil.buildFfmpeg(media, targetUrl, availableBandwidth);
        FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
        runningStream = executor.createJob(builder, progress -> {
            if (progress.isEnd()) {
                try {
                    FFmpegJob.State state = runningStream.getState();
                    log.warn("Stream ended, state = {}, media = {}", state, media);
                    stop();
                    handleEnd.accept(state);
                } catch (Exception e) {
                    log.error("Error sending stopped notification to TSDBot", e);
                }
            }
        });
        executorService.submit(runningStream);
        log.info("Media playing, waiting to confirm start...");
        Thread.sleep(TimeUnit.SECONDS.toMillis(NANNY_SLEEP_PERIOD_SECONDS));
        if (!FFmpegJob.State.RUNNING.equals(runningStream.getState())) {
            log.error("Stream failed to start, state = {}, media = {}", runningStream.getState(), media);
            log.warn("Stopping stream in error, state={}, media = {}", runningStream.getState(), media);
            stop();
            throw new Exception("Stream failed to start, media="+media);
        }
    }

    public void stop() {
        if (runningStream != null) {
            try {
                log.warn("Stopping stream...");
                runningStream.stop();
                log.warn("Stopped stream");
            } catch (Throwable t) {
                log.error("Error stopping stream", t);
            }
        }
    }

    public boolean isPlaying() {
        return runningStream != null
                && !runningStream.getState().isTerminal();
    }
}
