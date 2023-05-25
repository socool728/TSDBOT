package org.tsd.tsdbot.history.filter;

import org.tsd.tsdbot.discord.DiscordMessage;

public class NoBotsFilter implements MessageHistoryFilter {

    @Override
    public boolean test(DiscordMessage discordMessage) {
        return !discordMessage.getAuthor().getUser().isBot();
    }
}
