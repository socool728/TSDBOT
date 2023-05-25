package org.tsd.rest.v1.tsdtv.job;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class TSDTVPlayJob extends Job {
    private int mediaId;
    private String targetUrl;

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
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
                .append("mediaId", mediaId)
                .append("targetUrl", targetUrl)
                .append("id", id)
                .append("agentId", agentId)
                .append("timeoutMillis", timeoutMillis)
                .toString();
    }
}
