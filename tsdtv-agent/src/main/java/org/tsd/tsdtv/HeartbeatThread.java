package org.tsd.tsdtv;

import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.rest.v1.tsdtv.Heartbeat;
import org.tsd.rest.v1.tsdtv.HeartbeatResponse;
import org.tsd.rest.v1.tsdtv.Inventory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class HeartbeatThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatThread.class);

    private static final long PERIOD_SECONDS = 30;

    private final String agentId;
    private final NetworkMonitor networkMonitor;
    private final TSDBotClient tsdBotClient;
    private final AgentInventory agentInventory;
    private final Clock clock;
    private final TSDTVPlayer tsdtvPlayer;

    private LocalDateTime inventoryLastSent = LocalDateTime.MIN;
    private boolean shutdown = false;

    @Inject
    public HeartbeatThread(TSDBotClient tsdBotClient,
                           NetworkMonitor networkMonitor,
                           AgentInventory agentInventory,
                           Clock clock,
                           TSDTVPlayer tsdtvPlayer,
                           @Named("agentId") String agentId) {
        this.agentId = agentId;
        this.tsdBotClient = tsdBotClient;
        this.networkMonitor = networkMonitor;
        this.agentInventory = agentInventory;
        this.clock = clock;
        this.tsdtvPlayer = tsdtvPlayer;
    }

    public void run() {
        while (!shutdown) {
            log.debug("Building heartbeat...");

            Inventory inventory = null;
            if (shouldRefreshInventory()) {
                log.info("Inventory last sent: {}, sending...", inventoryLastSent);
                inventory = agentInventory.compileInventory();
                inventoryLastSent = LocalDateTime.now(clock);
                agentInventory.setForceOverride(false);
            }

            Heartbeat heartbeat = new Heartbeat();
            heartbeat.setAgentId(agentId);
            heartbeat.setHealthy(true);
            heartbeat.setInventory(inventory);

            Long uploadRateBitsPerSecond = networkMonitor.getUploadSpeedBitsPerSecond();
            if (uploadRateBitsPerSecond != null) {
                heartbeat.setUploadBitrate(uploadRateBitsPerSecond.doubleValue());
            } else if (networkMonitor.getError() != null) {
                heartbeat.setUploadBitrate(null);
                heartbeat.setHealthy(false);
                heartbeat.setUnhealthyReason("Network diagnostic in error: " + networkMonitor.getError());
            } else {
                heartbeat.setUploadBitrate(null);
            }

            long sleepSeconds;
            HeartbeatResponse response;
            try {
                response = tsdBotClient.sendTsdtvAgentHeartbeat(heartbeat);
                sleepSeconds = response.getSleepSeconds();
                if (response.isSendInventory()) {
                    log.info("Agent should send inventory in next heartbeat");
                    inventoryLastSent = LocalDateTime.MIN;
                }
            } catch (Exception e ) {
                log.error("Error sending heartbeat", e);
                sleepSeconds = PERIOD_SECONDS;
                inventoryLastSent = LocalDateTime.MIN;
            }

            try {
                log.debug("Sleeping for {} seconds", sleepSeconds);
                Thread.sleep(TimeUnit.SECONDS.toMillis(sleepSeconds));
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
                shutdown = true;
            }
        }
    }

    private boolean shouldRefreshInventory() {
        return !tsdtvPlayer.isPlaying()
                && (agentInventory.isForceOverride() || isInventoryStale());
    }

    private boolean isInventoryStale() {
        return inventoryLastSent.isBefore(LocalDateTime.now(clock).minus(Constants.TSDTV.INVENTORY_REFRESH_PERIOD_MINUTES, ChronoUnit.MINUTES));
    }

}
