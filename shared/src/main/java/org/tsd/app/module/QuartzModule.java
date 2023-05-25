package org.tsd.app.module;

import com.google.inject.AbstractModule;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(QuartzModule.class);

    @Override
    protected void configure() {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            bind(Scheduler.class).toInstance(scheduler);
            log.warn("Starting scheduler: {}", scheduler);
            scheduler.start();
        } catch (Exception e) {
            System.err.println("Error creating quartz scheduler: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
