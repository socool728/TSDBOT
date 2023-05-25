package org.tsd.tsdbot.tsdtv.library;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tsd.tsdbot.tsdtv.OnlineAgent;

public class AgentMedia<T> {
    private T media;
    private String agentId;
    private Double agentBitrate;

    public AgentMedia() {
    }

    public AgentMedia(T media, OnlineAgent agent) {
        this.media = media;
        this.agentId = agent.getAgent().getAgentId();
        this.agentBitrate = agent.getBitrate();
    }

    public T getMedia() {
        return media;
    }

    public void setMedia(T media) {
        this.media = media;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Double getAgentBitrate() {
        return agentBitrate;
    }

    public void setAgentBitrate(Double agentBitrate) {
        this.agentBitrate = agentBitrate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("media", media)
                .append("agentId", agentId)
                .append("agentBitrate", agentBitrate)
                .toString();
    }
}
