package org.tsd.tsdbot.app.config;

import javax.validation.constraints.NotNull;

public class TSDTVConfig {

    @NotNull
    private String streamUrl;

    @NotNull
    private String channel;

    @NotNull
    private String schedule;

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }
}
