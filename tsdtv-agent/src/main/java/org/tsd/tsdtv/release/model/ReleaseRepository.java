package org.tsd.tsdtv.release.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class ReleaseRepository implements Serializable {

    @JsonProperty("groups")
    private List<ReleaseGroup> releaseGroups;

    public List<ReleaseGroup> getReleaseGroups() {
        return releaseGroups;
    }

    public void setReleaseGroups(List<ReleaseGroup> releaseGroups) {
        this.releaseGroups = releaseGroups;
    }
}
