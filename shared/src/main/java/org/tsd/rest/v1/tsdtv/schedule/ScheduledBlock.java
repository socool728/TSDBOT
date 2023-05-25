package org.tsd.rest.v1.tsdtv.schedule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedList;
import java.util.List;

public class ScheduledBlock {
    private String id;
    private String cronString;
    private String name;
    private Long nextStartTime;
    private List<ScheduledItem> scheduledItems = new LinkedList<>();

    public Long getNextStartTime() {
        return nextStartTime;
    }

    public void setNextStartTime(Long nextStartTime) {
        this.nextStartTime = nextStartTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCronString() {
        return cronString;
    }

    public void setCronString(String cronString) {
        this.cronString = cronString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScheduledItem> getScheduledItems() {
        return scheduledItems;
    }

    public void setScheduledItems(List<ScheduledItem> scheduledItems) {
        this.scheduledItems = scheduledItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("cronString", cronString)
                .append("name", name)
                .append("scheduledItems", scheduledItems)
                .append("nextStartTime", nextStartTime)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ScheduledBlock that = (ScheduledBlock) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }
}
