package org.tsd.tsdbot.history.filter;

import com.google.inject.name.Named;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.DiscordUser;

import javax.inject.Inject;
import java.util.Objects;

public class NoOwnMessagesFilter implements MessageHistoryFilter {

    private final DiscordUser bot;

    @Inject
    public NoOwnMessagesFilter(@Named(Constants.Annotations.SELF) DiscordUser bot) {
        this.bot = bot;
    }

    @Override
    public boolean test(DiscordMessage discordMessage) {
        return !Objects.equals(discordMessage.getAuthor(), bot);
    }
}
