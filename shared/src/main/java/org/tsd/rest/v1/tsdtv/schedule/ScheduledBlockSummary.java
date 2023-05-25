package org.tsd.rest.v1.tsdtv.schedule;

import java.util.LinkedList;
import java.util.List;

public class ScheduledBlockSummary {
    private long startTime;
    private String name;
    private List<String> shows = new LinkedList<>();

    public List<String> getShows() {
        return shows;
    }

    public void setShows(List<String> shows) {
        this.shows = shows;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
