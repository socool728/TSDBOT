package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.MessageType;
import org.tsd.tsdbot.hustle.Hustle;
import org.tsd.tsdbot.listener.MessageFilter;
import org.tsd.tsdbot.listener.MessageFilterException;

import javax.inject.Inject;

public class HustleFilter extends MessageFilter {

    private final Hustle hustle;

    @Inject
    public HustleFilter(DiscordAPI api,
                        Hustle hustle) {
        super(api);
        this.hustle = hustle;
    }

    @Override
    public boolean isHistorical() {
        return false;
    }

    @Override
    public void filter(DiscordMessage<?> message) throws MessageFilterException {
        if (message.getType().equals(MessageType.NORMAL)) {
            hustle.process(message);
        }
    }
}
