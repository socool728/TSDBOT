package org.tsd.tsdbot.history;

import org.tsd.tsdbot.discord.*;
import org.tsd.tsdbot.history.filter.MessageHistoryFilter;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HistoryRequest<T extends MessageRecipient> {

    private final T recipient;
    private final DiscordMessage<T> exclude;

    private int limit = Integer.MAX_VALUE;
    private Set<Predicate<DiscordMessage>> filters = new HashSet<>();

    private HistoryRequest(T recipient, DiscordMessage<T> exclude) {
        this.recipient = recipient;
        this.exclude = exclude;
    }

    public HistoryRequest<T> withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public HistoryRequest<T> withFilter(Predicate<DiscordMessage> filter) {
        this.filters.add(filter);
        return this;
    }

    public HistoryRequest<T> withFilters(List<MessageHistoryFilter> filters) {
        this.filters.addAll(filters);
        return this;
    }

    public T getRecipient() {
        return recipient;
    }

    public List<DiscordMessage<T>> apply(History<T> history) {
        Stream<DiscordMessage<T>> stream = history.getMessages()
                .stream()
                .filter(msg -> !Objects.equals(msg, exclude))
                .filter(msg -> !msg.getType().equals(MessageType.BLACKLISTED));

        for (Predicate<DiscordMessage> filter : filters) {
            stream = stream.filter(filter);
        }

        return stream
                .limit(limit)
                .sorted(Comparator.comparing((DiscordMessage msg) -> msg.getTimestamp()).reversed())
                .collect(Collectors.toList());
    }

    public static HistoryRequest<DiscordChannel> create(DiscordChannel channel, DiscordMessage<DiscordChannel> exclude) {
        return new HistoryRequest<>(channel, exclude);
    }

    public static HistoryRequest<DiscordUser> create(DiscordUser user, DiscordMessage<DiscordUser> exclude) {
        return new HistoryRequest<>(user, exclude);
    }
}
