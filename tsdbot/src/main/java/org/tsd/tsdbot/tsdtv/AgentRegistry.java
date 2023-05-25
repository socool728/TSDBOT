package org.tsd.tsdbot.tsdtv;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.rest.v1.tsdtv.Heartbeat;
import org.tsd.rest.v1.tsdtv.HeartbeatResponse;
import org.tsd.tsdbot.tsdtv.job.JobQueue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class AgentRegistry {

    private static final Logger log = LoggerFactory.getLogger(AgentRegistry.class);

    private final Map<String, OnlineAgent> onlineAgents = new ConcurrentHashMap<>();
    private final TSDTVAgentDao tsdtvAgentDao;
    private final JobQueue jobQueue;
    private final Clock clock;
    private final String tsdtvStreamUrl;

    @Inject
    public AgentRegistry(TSDTVAgentDao tsdtvAgentDao,
                         JobQueue jobQueue,
                         Clock clock,
                         @Named(Constants.Annotations.TSDTV_STREAM_URL) String tsdtvStreamUrl) {
        this.tsdtvAgentDao = tsdtvAgentDao;
        this.jobQueue = jobQueue;
        this.tsdtvStreamUrl = tsdtvStreamUrl;
        this.clock = clock;

        new Thread(new ConnectedAgentReaper()).start();
    }

    public HeartbeatResponse handleHeartbeat(Heartbeat heartbeat, String ipAddress) throws BlacklistedAgentException {
        log.debug("Handling heartbeat, ipAddress={}, agentId={}", heartbeat.getAgentId());
        TSDTVAgent agent = tsdtvAgentDao.getAgentByAgentId(heartbeat.getAgentId());
        if (agent == null) {
            // this is an agent we've never seen before
            agent = new TSDTVAgent();
            agent.setAgentId(heartbeat.getAgentId());
            agent.setStatus(AgentStatus.unregistered);
            agent.setLastHeartbeatFrom(ipAddress);
            log.debug("Creating new TSDTVAgent in database: {}", agent);
        } else if (AgentStatus.blacklisted.equals(agent.getStatus())) {
            throw new BlacklistedAgentException(heartbeat.getAgentId());
        } else {
            agent.setLastHeartbeatFrom(ipAddress);
            log.debug("Updating TSDTVAgent in database: {}", agent);
        }

        tsdtvAgentDao.saveAgent(agent);

        OnlineAgent onlineAgent;
        if (!onlineAgents.containsKey(agent.getAgentId())) {
            log.debug("Agent {} is newly online, adding...", agent.getAgentId());
            onlineAgent = new OnlineAgent();
            onlineAgents.put(agent.getAgentId(), onlineAgent);
        } else {
            onlineAgent = onlineAgents.get(agent.getAgentId());
        }

        updateOnlineAgent(onlineAgent, agent, heartbeat);
        log.debug("Updated online agent info: {}", onlineAgent);

        Instant inventoryExpiration
                = Instant.now(clock).minus(Constants.TSDTV.INVENTORY_REFRESH_PERIOD_MINUTES, ChronoUnit.MINUTES);
        LocalDateTime now = LocalDateTime.now(clock);
        log.debug("Inventory for {} last reported on: {}", onlineAgent.getInventoryLastUpdated());
        log.debug("Inventory for {} expires on: {} (now = {})", inventoryExpiration, now);
        boolean agentShouldReportInventory = onlineAgent
                .getInventoryLastUpdated()
                .isBefore(inventoryExpiration);

        HeartbeatResponse response = new HeartbeatResponse();
        response.setSleepSeconds(20);
        response.setSendInventory(agentShouldReportInventory);
        log.debug("Returning heartbeat response for {}: {}", agent.getAgentId(), response);

        return response;
    }

    private void updateOnlineAgent(OnlineAgent onlineAgent, TSDTVAgent agent, Heartbeat heartbeat) {
        onlineAgent.setAgent(agent);
        onlineAgent.setLastHeartbeat(Instant.now(clock));
        onlineAgent.setBitrate(heartbeat.getUploadBitrate());
        if (heartbeat.getInventory() != null) {
            log.debug("Updating inventory for agent {}: {}", agent.getAgentId(), heartbeat.getInventory());
            onlineAgent.setInventory(heartbeat.getInventory());
            onlineAgent.setInventoryLastUpdated(Instant.now(clock));
        }
    }

    public void registerAgent(String agentId) {
        setAgentStatus(agentId, AgentStatus.registered);
    }

    public void blacklistAgent(String agentId) {
        setAgentStatus(agentId, AgentStatus.blacklisted);
    }

    public Set<OnlineAgent> getOnlineAgents() {
        return new HashSet<>(onlineAgents.values());
    }

    public OnlineAgent getFastestAgent() {
        if (onlineAgents.isEmpty()) {
            return null;
        }
        List<OnlineAgent> ordered = onlineAgents.values()
                .stream()
                .sorted(Comparator.comparing(OnlineAgent::getBitrate))
                .collect(Collectors.toList());
        return ordered.get(0);
    }

    private void setAgentStatus(String agentId, AgentStatus status) {
        TSDTVAgent agent = tsdtvAgentDao.getAgentByAgentId(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("No known agent matching ID " + agentId);
        }
        agent.setStatus(status);
        tsdtvAgentDao.saveAgent(agent);
        if (status.equals(AgentStatus.blacklisted)) {
            onlineAgents.remove(agentId);
        }
    }

    private class ConnectedAgentReaper implements Runnable {

        private boolean shutdown = false;

        void shutdown() {
            this.shutdown = true;
        }

        @Override
        public void run() {
            while (!shutdown) {
                Instant cutoff = Instant
                        .now(clock)
                        .minus(3*Constants.TSDTV.AGENT_HEARTBEAT_PERIOD_MILLIS, ChronoUnit.MILLIS);
                log.debug("Checking for agents with last heartbeat before {}", cutoff);
                List<OnlineAgent> expiredAgents = onlineAgents.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().getLastHeartbeat().isBefore(cutoff))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
                for (OnlineAgent agent : expiredAgents) {
                    String agentId = agent.getAgent().getAgentId();
                    log.warn("Agent offline: {} (last heartbeat = {})", agentId, agent.getLastHeartbeat());
                    onlineAgents.remove(agentId);
                    jobQueue.handleOfflineAgent(agentId);
                }
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                } catch (InterruptedException e) {
                    log.error("Interrupted");
                    shutdown();
                }
            }
        }
    }
}
