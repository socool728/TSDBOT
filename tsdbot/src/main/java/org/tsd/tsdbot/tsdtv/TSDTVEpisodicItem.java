package org.tsd.tsdbot.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tsd.tsdbot.db.BaseEntity;

import javax.persistence.Entity;

@Entity
public class TSDTVEpisodicItem extends BaseEntity {

    private String seriesName;
    private String seasonName;

    private int currentEpisode;

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getSeasonName() {
        return seasonName;
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }

    public int getCurrentEpisode() {
        return currentEpisode;
    }

    public void setCurrentEpisode(int currentEpisode) {
        this.currentEpisode = currentEpisode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("seriesName", seriesName)
                .append("seasonName", seasonName)
                .append("currentEpisode", currentEpisode)
                .append("id", id)
                .toString();
    }
}
