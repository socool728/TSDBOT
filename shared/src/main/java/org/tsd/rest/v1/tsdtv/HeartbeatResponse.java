package org.tsd.rest.v1.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class HeartbeatResponse {
    private int sleepSeconds;
    private boolean sendInventory;

    public boolean isSendInventory() {
        return sendInventory;
    }

    public void setSendInventory(boolean sendInventory) {
        this.sendInventory = sendInventory;
    }

    public int getSleepSeconds() {
        return sleepSeconds;
    }

    public void setSleepSeconds(int sleepSeconds) {
        this.sleepSeconds = sleepSeconds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("sleepSeconds", sleepSeconds)
                .append("sendInventory", sendInventory)
                .toString();
    }
}
