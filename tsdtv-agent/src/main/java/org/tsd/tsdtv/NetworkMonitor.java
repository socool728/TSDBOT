package org.tsd.tsdtv;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Singleton
public class NetworkMonitor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(NetworkMonitor.class);

    private static final long TEST_PERIOD_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private static final String TARGET_URI = "http://2.testdebit.info/";
    private static final int UPLOAD_FILE_SIZE_BYTES = 1_000_000;

    private boolean shutdown = false;
    private Long uploadSpeedBitsPerSecond = null;
    private SpeedTestError error = null;

    @Inject
    public NetworkMonitor() {
        log.info("Created NetworkMonitor");
    }

    @Override
    public void run() {
        while (!shutdown) {
            SpeedTestSocket speedTestSocket = new SpeedTestSocket();
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                private Long bitsPerSecond = null;

                @Override
                public void onCompletion(SpeedTestReport speedTestReport) {
                    System.out.println("Speed test completed");
                    uploadSpeedBitsPerSecond = bitsPerSecond;
                    error = null;
                }

                @Override
                public void onProgress(float v, SpeedTestReport speedTestReport) {
                    log.debug("Progress {}: bitrate = {} kbit/s",
                            speedTestReport.getProgressPercent(),
                            speedTestReport.getTransferRateBit().longValue() / 1000);
                    if (speedTestReport.getProgressPercent() == 100f && bitsPerSecond == null) {
                        bitsPerSecond = speedTestReport.getTransferRateBit().longValue();
                        log.debug("Upload finished, uploaded bits = {}, speed = {} kbit/s",
                                UPLOAD_FILE_SIZE_BYTES*8, bitsPerSecond/1000);
                    }
                }

                @Override
                public void onError(SpeedTestError speedTestError, String s) {
                    System.err.println(String.format("Speed test ERROR (%s): %s", speedTestError, s));
                    uploadSpeedBitsPerSecond = null;
                    error = speedTestError;
                }
            });
            log.debug("Starting network monitor upload");
            speedTestSocket.startUpload(TARGET_URI,
                    UPLOAD_FILE_SIZE_BYTES,
                    100);
            try {
                log.debug("Network monitor upload initiated, sleeping for {} seconds", TEST_PERIOD_MILLIS/1000);
                Thread.sleep(TEST_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
                shutdown();
            }
        }
    }

    public Long getUploadSpeedBitsPerSecond() {
        return uploadSpeedBitsPerSecond;
    }

    public SpeedTestError getError() {
        return error;
    }

    public void shutdown() {
        this.shutdown = true;
    }
}
