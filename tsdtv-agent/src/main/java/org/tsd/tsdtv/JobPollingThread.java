package org.tsd.tsdtv;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.rest.v1.tsdtv.Media;
import org.tsd.rest.v1.tsdtv.job.*;
import org.tsd.util.RetryInstructionFailedException;
import org.tsd.util.RetryUtil;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Singleton
public class JobPollingThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(JobPollingThread.class);

    private static final long PERIOD_MILLIS = TimeUnit.SECONDS.toMillis(1);

    private final TSDBotClient tsdBotClient;
    private final TSDTVPlayer player;
    private final AgentInventory agentInventory;
    private final TSDBotClient client;
    private final NetworkMonitor networkMonitor;

    private boolean shutdown = false;

    @Inject
    public JobPollingThread(TSDBotClient tsdBotClient,
                            NetworkMonitor networkMonitor,
                            TSDTVPlayer player,
                            AgentInventory agentInventory,
                            TSDBotClient client) {
        this.tsdBotClient = tsdBotClient;
        this.player = player;
        this.agentInventory = agentInventory;
        this.client = client;
        this.networkMonitor = networkMonitor;
    }

    @Override
    public void run() {
        while (!shutdown) {
            log.debug("Polling for jobs...");

            try {
                Job job = tsdBotClient.pollForJob();
                if (job != null) {
                    handleJob(job);
                }
            } catch (Exception e) {
                log.error("Error polling for jobs", e);
            }

            try {
                log.debug("Sleeping for {} ms", PERIOD_MILLIS);
                Thread.sleep(PERIOD_MILLIS);
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
                shutdown = true;
            }
        }
    }

    private void handleJob(Job job) {
        log.warn("Received job: {}", job);
        if (job instanceof TSDTVPlayJob) {
            int mediaId = ((TSDTVPlayJob) job).getMediaId();
            String targetUrl = ((TSDTVPlayJob) job).getTargetUrl();
            TSDTVPlayJobResult result = new TSDTVPlayJobResult();
            result.setJobId(job.getId());
            try {
                Media media = agentInventory.getFileByMediaId(mediaId);
                if (media == null) {
                    throw new Exception("Could not find media in inventory with id "+mediaId);
                }
                log.info("Found media: {}", media);
                player.play(media, targetUrl, getAvailableBandwidth(), state -> {
                    boolean error = !FFmpegJob.State.FINISHED.equals(state);
                    try {
                        RetryUtil.executeWithRetry("SendMediaStoppedNotification",
                                5,
                                () -> client.sendMediaStoppedNotification(mediaId, error));
                    } catch (RetryInstructionFailedException e) {
                        log.error("Failed to report to TSDTV that the media has stopped");
                    }
                });
                result.setSuccess(true);
                result.setTimeStarted(Instant.now().toEpochMilli());
            } catch (Exception e) {
                log.error("Error playing media: " + mediaId, e);
                result.setSuccess(false);
            }
            tsdBotClient.sendJobResult(result);
        } else if (job instanceof TSDTVStopJob) {
            TSDTVStopJobResult result = new TSDTVStopJobResult();
            result.setJobId(job.getId());
            try {
                player.stop();
            } catch (Exception e) {
                log.error("Error stopping stream", e);
            }
            tsdBotClient.sendJobResult(result);
        }
    }

    private Long getAvailableBandwidth() {
        Long availableBandwidth = networkMonitor.getUploadSpeedBitsPerSecond();
        if (availableBandwidth != null) {
            log.info("Available bandwidth: {} kbit/s", availableBandwidth/1000);
        } else {
            log.info("No bandwidth information");
        }
        return availableBandwidth;
    }
}
