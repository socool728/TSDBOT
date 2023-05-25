package org.tsd.tsdbot.history.filter;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang3.StringUtils;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.util.MessageSanitizer;

public class LengthFilter implements MessageHistoryFilter {

    private final Integer min;
    private final Integer max;
    private final MessageSanitizer messageSanitizer;

    @Inject
    public LengthFilter(@Assisted(value = "min") Integer min,
                        @Assisted(value = "max") Integer max,
                        MessageSanitizer messageSanitizer) {
        this.min = min == null ? Integer.MIN_VALUE : min;
        this.max = max == null ? Integer.MAX_VALUE : max;
        this.messageSanitizer = messageSanitizer;
    }

    @Override
    public boolean test(DiscordMessage discordMessage) {
        String sanitized = messageSanitizer.sanitize(discordMessage.getContent());
        int length = StringUtils.length(sanitized);
        return length >= min && length <= max;
    }
}
