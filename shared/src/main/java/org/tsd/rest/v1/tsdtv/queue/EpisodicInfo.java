package org.tsd.rest.v1.tsdtv.queue;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class EpisodicInfo {
    private String episodicSeriesName;
    private String episodicSeasonName;
    private Integer effectiveEpisodeNumber;

    public String getEpisodicSeriesName() {
        return episodicSeriesName;
    }

    public void setEpisodicSeriesName(String episodicSeriesName) {
        this.episodicSeriesName = episodicSeriesName;
    }

    public String getEpisodicSeasonName() {
        return episodicSeasonName;
    }

    public void setEpisodicSeasonName(String episodicSeasonName) {
        this.episodicSeasonName = episodicSeasonName;
    }

    public Integer getEffectiveEpisodeNumber() {
        return effectiveEpisodeNumber;
    }

    public void setEffectiveEpisodeNumber(Integer effectiveEpisodeNumber) {
        this.effectiveEpisodeNumber = effectiveEpisodeNumber;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("episodicSeriesName", episodicSeriesName)
                .append("episodicSeasonName", episodicSeasonName)
                .append("effectiveEpisodeNumber", effectiveEpisodeNumber)
                .toString();
    }
}
