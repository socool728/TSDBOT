package org.tsd.tsdbot.util;

import org.apache.commons.lang3.StringUtils;

public class OdbUtils {

    public static String sanitizeTag(String tag) {
        if (StringUtils.isBlank(tag)) {
            return tag;
        }

        if (StringUtils.startsWith(tag, "#")) {
            tag = StringUtils.substring(tag, 1);
        }

        return tag.trim().toLowerCase();
    }
}
