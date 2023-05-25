package org.tsd.tsdtv.release;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdtv.release.horriblesubs.HorribleSubsReleaseFetcher;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class ReleaseFetcherThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ReleaseFetcherThread.class);

    private static final long SLEEP_PERIOD = TimeUnit.MINUTES.toMillis(30);

    private final List<? extends ReleaseFetcher> fetchers;
    private boolean shutdown = false;

    @Inject
    public ReleaseFetcherThread(HorribleSubsReleaseFetcher horribleSubsReleaseFetcher) {
        this.fetchers = Arrays.asList(horribleSubsReleaseFetcher);
    }

    @Override
    public void run() {
        while (!shutdown) {
            log.info("Executing release fetcher thread");

            for (ReleaseFetcher fetcher : fetchers) {
                try {
                    log.info("Beginning release fetch: {}", fetcher);
                    fetcher.fetchAndProcess();
                    log.info("Release fetch complete: {}", fetcher);
                } catch (Exception e) {
                    log.error("Error executing release fetcher "+fetcher, e);
                }
            }

            try {
                log.debug("Release fetcher complete, sleeping for {} seconds", SLEEP_PERIOD/1000);
                Thread.sleep(SLEEP_PERIOD);
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
                shutdown();
            }
        }
    }

    private void shutdown() {
        this.shutdown = true;
    }
}
