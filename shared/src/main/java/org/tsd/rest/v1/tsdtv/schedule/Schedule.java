package org.tsd.rest.v1.tsdtv.schedule;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedList;
import java.util.List;

public class Schedule {

    private String timezone;
    private List<ScheduledBlock> scheduledBlocks = new LinkedList<>();

    public List<ScheduledBlock> getScheduledBlocks() {
        return scheduledBlocks;
    }

    public void setScheduledBlocks(List<ScheduledBlock> scheduledBlocks) {
        this.scheduledBlocks = scheduledBlocks;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timezone", timezone)
                .append("scheduledBlocks", scheduledBlocks)
                .toString();
    }
}
