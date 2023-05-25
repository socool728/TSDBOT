package org.tsd.rest.v1.tsdtv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Episode extends Media {

    private String name;
    private String seasonName;
    private String seriesName;
    private Integer episodeNumber;

    @JsonIgnore
    private Integer overriddenEpisodeNumber;

    public Episode() {
    }

    public Integer getOverriddenEpisodeNumber() {
        return overriddenEpisodeNumber;
    }

    public void setOverriddenEpisodeNumber(Integer overriddenEpisodeNumber) {
        this.overriddenEpisodeNumber = overriddenEpisodeNumber;
    }

    public Episode(String agentId, MediaInfo mediaInfo) {
        super(agentId, mediaInfo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getSeasonName() {
        return seasonName;
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("seasonName", seasonName)
                .append("seriesName", seriesName)
                .append("episodeNumber", episodeNumber)
                .append("id", id)
                .append("agentId", agentId)
                .append("mediaInfo", mediaInfo)
                .append("overriddenEpisodeNumber", overriddenEpisodeNumber)
                .toString();
    }
}
