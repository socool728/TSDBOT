package org.tsd.rest.v1.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Heartbeat {

    private String agentId;
    private Inventory inventory;
    private Double uploadBitrate;
    private boolean healthy;
    private String unhealthyReason;

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Double getUploadBitrate() {
        return uploadBitrate;
    }

    public void setUploadBitrate(Double uploadBitrate) {
        this.uploadBitrate = uploadBitrate;
    }

    public String getUnhealthyReason() {
        return unhealthyReason;
    }

    public void setUnhealthyReason(String unhealthyReason) {
        this.unhealthyReason = unhealthyReason;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("agentId", agentId)
                .append("inventory", inventory)
                .append("uploadBitrate", uploadBitrate)
                .append("healthy", healthy)
                .append("unhealthyReason", unhealthyReason)
                .toString();
    }
}
