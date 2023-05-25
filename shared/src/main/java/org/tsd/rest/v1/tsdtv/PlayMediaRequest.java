package org.tsd.rest.v1.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class PlayMediaRequest {
    private String agentId;
    private String mediaId;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("agentId", agentId)
                .append("mediaId", mediaId)
                .toString();
    }
}
