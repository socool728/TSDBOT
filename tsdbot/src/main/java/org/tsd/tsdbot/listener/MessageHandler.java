package org.tsd.tsdbot.listener;

import de.btobastian.javacord.DiscordAPI;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.MessageRecipient;

public abstract class MessageHandler<T extends MessageRecipient> {

    protected final DiscordAPI api;

    protected MessageHandler(DiscordAPI api) {
        this.api = api;
    }

    public final boolean handle(DiscordMessage<T> message) throws Exception {
        if (isValid(message)) {
            doHandle(message, message.getRecipient());
            return true;
        }
        return false;
    }

    public abstract boolean isValid(DiscordMessage<T> message);
    public abstract void doHandle(DiscordMessage<T> message, T recipient) throws Exception;
}
