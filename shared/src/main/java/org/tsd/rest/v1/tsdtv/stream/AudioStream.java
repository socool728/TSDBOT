package org.tsd.rest.v1.tsdtv.stream;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AudioStream extends Stream {
    private String channelLayout;
    private String language;
    private long sampleRate;

    public String getChannelLayout() {
        return channelLayout;
    }

    public void setChannelLayout(String channelLayout) {
        this.channelLayout = channelLayout;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(long sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("channelLayout", channelLayout)
                .append("language", language)
                .append("sampleRate", sampleRate)
                .append("index", index)
                .append("codecName", codecName)
                .append("tags", tags)
                .toString();
    }
}
