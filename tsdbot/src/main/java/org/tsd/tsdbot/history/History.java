package org.tsd.tsdbot.history;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.MessageRecipient;
import org.tsd.tsdbot.discord.MessageType;

import java.util.ArrayList;
import java.util.List;

class History<T extends MessageRecipient> {

    private static final Logger log = LoggerFactory.getLogger(History.class);

    private CircularFifoQueue<DiscordMessage<T>> buffer = new CircularFifoQueue<>(Constants.History.DEFAULT_HISTORY_LENGTH);

    void addMessage(DiscordMessage<T> message) {
        this.buffer.add(message);
    }

    List<DiscordMessage<T>> getMessages() {
        return new ArrayList<>(buffer);
    }

    void markMessage(String id, MessageType type) {
        buffer.stream()
                .filter(message -> StringUtils.equals(message.getId(), id))
                .peek(message -> log.debug("Marking message as \"{}\": {}", type, message))
                .forEach(message -> message.setType(type));
    }
}
