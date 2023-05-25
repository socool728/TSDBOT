package org.tsd.tsdbot.news;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum SortBy {
    relevancy,
    popularity,
    publishedAt;

    public static SortBy fromString(String input) {
        return Arrays.stream(values())
                .filter(val -> StringUtils.equalsIgnoreCase(input, val.name()))
                .findAny().orElse(null);
    }
}
