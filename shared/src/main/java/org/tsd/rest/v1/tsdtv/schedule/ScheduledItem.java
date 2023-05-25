package org.tsd.rest.v1.tsdtv.schedule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ScheduledItem {
    private String series;
    private String season;
    private int commercialBreakMinutes;

    public int getCommercialBreakMinutes() {
        return commercialBreakMinutes;
    }

    public void setCommercialBreakMinutes(int commercialBreakMinutes) {
        this.commercialBreakMinutes = commercialBreakMinutes;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("series", series)
                .append("season", season)
                .append("commercialBreakMinutes", commercialBreakMinutes)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ScheduledItem that = (ScheduledItem) o;

        return new EqualsBuilder()
                .append(series, that.series)
                .append(season, that.season)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(series)
                .append(season)
                .toHashCode();
    }
}
