package org.tsd.tsdbot.history.filter;

import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordMessage;

public class NoUrlsFilter implements MessageHistoryFilter {
    @Override
    public boolean test(DiscordMessage discordMessage) {
        return !discordMessage.getContent().matches(Constants.URL_REGEX);
    }
}
