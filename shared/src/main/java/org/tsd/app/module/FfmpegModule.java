package org.tsd.app.module;

import com.google.inject.AbstractModule;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.app.config.FfmpegConfig;

public class FfmpegModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(FfmpegModule.class);

    private final FfmpegConfig ffmpegConfig;

    public FfmpegModule(FfmpegConfig ffmpegConfig) {
        this.ffmpegConfig = ffmpegConfig;
    }

    @Override
    protected void configure() {
        FFprobe ffProbe;
        try {
            log.info("Binding FFprobe to: {}", ffmpegConfig.getFfprobeExec());
            ffProbe = new FFprobe(ffmpegConfig.getFfprobeExec());
            bind(FFprobe.class)
                    .toInstance(ffProbe);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize FFprobe: " + ffmpegConfig.getFfprobeExec(), e);
        }

        FFmpeg ffMpeg;
        try {
            log.info("Binding FFmpeg to: {}", ffmpegConfig.getFfmpegExec());
            ffMpeg = new FFmpeg(ffmpegConfig.getFfmpegExec());
            bind(FFmpeg.class)
                    .toInstance(ffMpeg);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize FFmpeg: " + ffmpegConfig.getFfmpegExec(), e);
        }
    }
}
