package org.tsd.rest.v1.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class StoppedPlayingNotification {
    private String agentId;
    private int mediaId;
    private boolean error;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("agentId", agentId)
                .append("mediaId", mediaId)
                .append("error", error)
                .toString();
    }
}
