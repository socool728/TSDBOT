package org.tsd.rest.v1.tsdtv;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class Media {

    protected int id;
    protected String agentId;
    protected MediaInfo mediaInfo;

    public Media() {
    }

    public Media(String agentId, MediaInfo mediaInfo) {
        this.agentId = agentId;
        this.mediaInfo = mediaInfo;
        this.id = (agentId+mediaInfo.getFilePath()).hashCode();
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MediaInfo getMediaInfo() {
        return mediaInfo;
    }

    public void setMediaInfo(MediaInfo mediaInfo) {
        this.mediaInfo = mediaInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        return new EqualsBuilder()
                .append(id, media.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }
}
