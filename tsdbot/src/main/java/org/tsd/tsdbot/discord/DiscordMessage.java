package org.tsd.tsdbot.discord;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordMessage<T extends MessageRecipient> {

    private final String id;
    private final String content;
    private final Instant timestamp;
    private final DiscordUser author;
    private final T recipient;
    private final List<URL> attachments;
    private MessageType type;

    private final Message message;

    @SuppressWarnings("unchecked")
    public DiscordMessage(Message message) {
        this.message = message;
        this.id = message.getId();
        this.content = message.getContent();
        this.author = new DiscordUser(message.getAuthor());
        this.timestamp = message.getCreationDate().toInstant();
        this.attachments = message.getAttachments().stream().map(MessageAttachment::getUrl).collect(Collectors.toList());

        if (message.getChannelReceiver() != null) {
            this.recipient = (T) new DiscordChannel(message.getChannelReceiver());
        } else {
            this.recipient = (T) new DiscordUser(message.getUserReceiver());
        }

        this.type = StringUtils.equalsIgnoreCase(message.getAuthor().getName(), "tsdbot") ?
                MessageType.SELF : MessageType.NORMAL;
    }

    public List<URL> getAttachments() {
        return attachments;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public T getRecipient() {
        return recipient;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public DiscordUser getAuthor() {
        return author;
    }

    public boolean isChannelMessage() {
        return recipient instanceof DiscordChannel;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public DiscordMessage<T> reply(String text) {
        try {
            Message reply = message.reply(text).get(10, TimeUnit.SECONDS);
            return new DiscordMessage<>(reply);
        } catch (Exception e) {
            throw new RuntimeException("Error replying to message: " + this, e);
        }
    }

    public boolean authorHasRole(String role) {
        return author.hasRole(message.getChannelReceiver().getServer(), role);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("message", message)
                .append("timestamp", timestamp)
                .append("author", author == null ? "[NULL]" : author.getName())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DiscordMessage that = (DiscordMessage) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }
}
