package org.tsd.tsdbot.news;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum Language {
    ar,
    de,
    en,
    es,
    fr,
    he,
    it,
    nl,
    no,
    pt,
    ru,
    se,
    ud,
    zh;

    public static Language fromString(String input) {
        return Arrays.stream(values())
                .filter(val -> StringUtils.equalsIgnoreCase(input, val.name()))
                .findAny().orElse(null);
    }
}
