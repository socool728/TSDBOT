package org.tsd.tsdtv.release.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

public class ReleaseSeries implements Serializable{

    @JsonProperty("seriesName")
    private String seriesName;

    @JsonProperty("episodes")
    private List<ReleaseEpisode> releasedEpisodes;

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public List<ReleaseEpisode> getReleasedEpisodes() {
        return releasedEpisodes;
    }

    public void setReleasedEpisodes(List<ReleaseEpisode> releasedEpisodes) {
        this.releasedEpisodes = releasedEpisodes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("seriesName", seriesName)
                .append("releasedEpisodes", releasedEpisodes)
                .toString();
    }
}
