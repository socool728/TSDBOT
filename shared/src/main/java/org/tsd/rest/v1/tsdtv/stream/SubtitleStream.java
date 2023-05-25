package org.tsd.rest.v1.tsdtv.stream;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class SubtitleStream extends Stream {
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("language", language)
                .append("index", index)
                .append("codecName", codecName)
                .append("tags", tags)
                .toString();
    }
}
