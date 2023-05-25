package org.tsd.tsdbot.tsdtv;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tsd.rest.v1.tsdtv.Inventory;

import java.time.Instant;

public class OnlineAgent {

    private TSDTVAgent agent;
    private Instant lastHeartbeat;
    private Instant inventoryLastUpdated = Instant.MIN;
    private Double bitrate;
    private Inventory inventory;

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Instant getInventoryLastUpdated() {
        return inventoryLastUpdated;
    }

    public void setInventoryLastUpdated(Instant inventoryLastUpdated) {
        this.inventoryLastUpdated = inventoryLastUpdated;
    }

    public TSDTVAgent getAgent() {
        return agent;
    }

    public void setAgent(TSDTVAgent agent) {
        this.agent = agent;
    }

    public Double getBitrate() {
        return bitrate;
    }

    public void setBitrate(Double bitrate) {
        this.bitrate = bitrate;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        OnlineAgent that = (OnlineAgent) o;

        return new EqualsBuilder()
                .append(agent, that.agent)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(agent)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("agent", agent)
                .append("lastHeartbeat", lastHeartbeat)
                .append("inventoryLastUpdated", inventoryLastUpdated)
                .append("bitrate", bitrate)
                .append("shows.size", inventory.getSeries().size())
                .append("movies.size", inventory.getMovies().size())
                .toString();
    }
}
