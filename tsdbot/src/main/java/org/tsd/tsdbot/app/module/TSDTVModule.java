package org.tsd.tsdbot.app.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.app.config.FfmpegConfig;
import org.tsd.app.module.FfmpegModule;
import org.tsd.tsdbot.app.config.TSDTVConfig;

public class TSDTVModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(TSDTVModule.class);

    private final FfmpegConfig ffmpegConfig;
    private final TSDTVConfig tsdtvConfig;

    public TSDTVModule(FfmpegConfig ffmpegConfig, TSDTVConfig tsdtvConfig) {
        this.ffmpegConfig = ffmpegConfig;
        this.tsdtvConfig = tsdtvConfig;
    }

    @Override
    protected void configure() {
        install(new FfmpegModule(ffmpegConfig));

        log.info("Binding tsdtvStreamUrl to: {}", tsdtvConfig.getStreamUrl());
        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.TSDTV_STREAM_URL))
                .toInstance(tsdtvConfig.getStreamUrl());

        log.info("Binding channel to: {}", tsdtvConfig.getChannel());
        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.TSDTV_CHANNEL))
                .toInstance(tsdtvConfig.getChannel());

        log.info("Binding schedule to: {}", tsdtvConfig.getSchedule());
        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.TSDTV_SCHEDULE))
                .toInstance(tsdtvConfig.getSchedule());
    }
}
