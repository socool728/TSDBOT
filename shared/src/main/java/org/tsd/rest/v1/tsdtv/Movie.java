package org.tsd.rest.v1.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Movie extends Media {

    private String name;

    public Movie() {
    }

    public Movie(String agentId, MediaInfo mediaInfo) {
        super(agentId, mediaInfo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("id", id)
                .append("agentId", agentId)
                .append("mediaInfo", mediaInfo)
                .toString();
    }
}
