package org.tsd.tsdtv.release.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tsd.tsdtv.release.ReleaseSource;

import java.util.List;

public class ReleaseGroup {

    @JsonProperty("group")
    private ReleaseSource group;

    @JsonProperty("series")
    private List<ReleaseSeries> releaseSeries;

    public ReleaseSource getGroup() {
        return group;
    }

    public void setGroup(ReleaseSource group) {
        this.group = group;
    }

    public List<ReleaseSeries> getReleaseSeries() {
        return releaseSeries;
    }

    public void setReleaseSeries(List<ReleaseSeries> releaseSeries) {
        this.releaseSeries = releaseSeries;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("group", group)
                .append("releaseSeries", releaseSeries)
                .toString();
    }
}
