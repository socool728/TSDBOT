package org.tsd.tsdbot.history;

import com.google.inject.Singleton;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageHistory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.DiscordUser;
import org.tsd.tsdbot.discord.MessageType;
import org.tsd.tsdbot.listener.MessageFilter;
import org.tsd.tsdbot.listener.MessageFilterException;
import org.tsd.tsdbot.listener.MessageHandler;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class HistoryCache extends MessageFilter {

    private static final Logger log = LoggerFactory.getLogger(HistoryCache.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd HH:mm:ss z");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private final Map<DiscordChannel, History<DiscordChannel>> channelMessages = new ConcurrentHashMap<>();
    private final Map<DiscordUser, History<DiscordUser>> userMessages = new ConcurrentHashMap<>();

    private final List<MessageFilter> messageFilters = new LinkedList<>();

    private final List<MessageHandler<DiscordChannel>> channelMessageHandlers = new LinkedList<>();
    private final List<MessageHandler<DiscordUser>> userMessageHandlers = new LinkedList<>();

    private final boolean initializeUsers;
    private final RemoteConfigurationRepository remoteConfigurationRepository;

    @Inject
    public HistoryCache(DiscordAPI api, RemoteConfigurationRepository remoteConfigurationRepository) {
        super(api);

        String initializeUsersEnv = System.getProperty("initializeUsers");
        initializeUsers = StringUtils.isBlank(initializeUsersEnv) || Boolean.parseBoolean(initializeUsersEnv);

        this.remoteConfigurationRepository = remoteConfigurationRepository;
    }

    @Override
    public boolean isHistorical() {
        return false;
    }

    public void addMessageFilter(MessageFilter messageFilter) {
        messageFilters.add(messageFilter);
    }

    public void addChannelMessageHandler(MessageHandler<DiscordChannel> channelMessageHandler) {
        channelMessageHandlers.add(channelMessageHandler);
    }

    public void addUserMessageHandler(MessageHandler<DiscordUser> userMessageHandler) {
        userMessageHandlers.add(userMessageHandler);
    }

    public void initialize() {
        for (Channel channel : api.getChannels()) {
            try {
                initializeChannelHistory(new DiscordChannel(channel));
            } catch (Exception e) {
                log.error("Error initializing channel " + channel.getName(), e);
            }
        }

        if (initializeUsers) {
            for (User user : api.getUsers()) {
                try {
                    initializeUserHistory(new DiscordUser(user));
                } catch (Exception e) {
                    log.error("Error initializing user " + user.getName(), e);
                }
            }
        }
    }

    public List<DiscordMessage<DiscordChannel>> getChannelHistory(HistoryRequest<DiscordChannel> request) {
        return request.apply(channelMessages.get(request.getRecipient()));
    }

    public List<DiscordMessage<DiscordUser>> getUserHistory(HistoryRequest<DiscordUser> request) {
        return request.apply(userMessages.get(request.getRecipient()));
    }

    public DiscordMessage<DiscordChannel> getRandomChannelMessage(HistoryRequest<DiscordChannel> request) {
        List<DiscordMessage<DiscordChannel>> possibilities = getChannelHistory(request);
        if (CollectionUtils.isNotEmpty(possibilities)) {
            DiscordMessage<DiscordChannel> random = possibilities.get(RandomUtils.nextInt(0, possibilities.size()));
            log.info("Retrieved random message: {}", random);
            return random;
        }
        return null;
    }

    public DiscordMessage<DiscordUser> getRandomUserMessage(HistoryRequest<DiscordUser> request) {
        List<DiscordMessage<DiscordUser>> possibilities = getUserHistory(request);
        if (CollectionUtils.isNotEmpty(possibilities)) {
            DiscordMessage<DiscordUser> random = possibilities.get(RandomUtils.nextInt(0, possibilities.size()));
            log.info("Retrieved random message: {}", random);
            return random;
        }
        return null;
    }

    private void initializeChannelHistory(DiscordChannel channel) throws Exception {
        log.debug("Initializing channel history: channel={}", channel);

        MessageHistory messageHistory = channel.getChannel()
                .getMessageHistory(Constants.History.DEFAULT_HISTORY_LENGTH)
                .get(Constants.History.HISTORY_FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        log.debug("Retrieved {} messages (no lastMessageId)",
                messageHistory.getMessages().size());

        History<DiscordChannel> history = new History<>();
        messageHistory.getMessagesSorted().stream()
                .peek(message -> {
                    log.debug("Adding message to history: channel={}, time={}, message=\"{}\"",
                            channel.getChannel().getName(),
                            DATE_FORMAT.format(message.getCreationDate().getTime()), message.getContent());
                })
                .map(this::wrapChannelMessage)
                .forEach(history::addMessage);

        channelMessages.put(channel, history);
    }

    private DiscordMessage<DiscordChannel> wrapChannelMessage(Message message) {
        DiscordMessage<DiscordChannel> discordMessage = new DiscordMessage<>(message);

        if (remoteConfigurationRepository.isMessageFromBlacklistedUser(discordMessage)) {
            discordMessage.setType(MessageType.BLACKLISTED);
            return discordMessage;
        }

        messageFilters.stream()
                .filter(MessageFilter::isHistorical)
                .forEach(filter -> {
                    try {
                        filter.filter(discordMessage);
                    } catch (MessageFilterException e) {
                        log.error("Error filtering channel message during wrapping, filter="+filter.getClass()+": " + message, e);
                    }
                });

        boolean isFunction = channelMessageHandlers.stream()
                .anyMatch(handler -> handler.isValid(discordMessage));

        if (isFunction) {
            discordMessage.setType(MessageType.FUNCTION);
        }

        return discordMessage;
    }

    private void initializeUserHistory(DiscordUser user) throws Exception {
        log.debug("Initializing user history: user={}", user.getName());

        MessageHistory messageHistory = user.getUser()
                .getMessageHistory(Constants.History.DEFAULT_HISTORY_LENGTH)
                .get(Constants.History.HISTORY_FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        log.debug("Retrieved {} messages (no lastMessageId)",
                messageHistory.getMessages().size());

        History<DiscordUser> history = new History<>();
        messageHistory.getMessagesSorted().stream()
                .peek(message -> {
                    log.debug("Adding message to history: user={}, time={}, message=\"{}\"",
                            user.getUser().getName(),
                            DATE_FORMAT.format(message.getCreationDate().getTime()), message.getContent());
                })
                .map(this::wrapUserMessage)
                .forEach(history::addMessage);

        userMessages.put(user, history);
    }

    private DiscordMessage<DiscordUser> wrapUserMessage(Message message) {
        DiscordMessage<DiscordUser> discordMessage = new DiscordMessage<>(message);

        if (remoteConfigurationRepository.isMessageFromBlacklistedUser(discordMessage)) {
            markMessage(discordMessage, MessageType.BLACKLISTED);
            return discordMessage;
        }

        messageFilters.stream()
                .filter(MessageFilter::isHistorical)
                .forEach(filter -> {
                    try {
                        filter.filter(discordMessage);
                    } catch (MessageFilterException e) {
                        log.error("Error filtering user message during wrapping, filter="+filter.getClass()+": " + message, e);
                    }
                });

        boolean isFunction = userMessageHandlers.stream()
                .anyMatch(handler -> handler.isValid(discordMessage));

        if (isFunction) {
            discordMessage.setType(MessageType.FUNCTION);
        }

        return discordMessage;
    }

    public void markMessage(DiscordMessage<?> message, MessageType type) {
        if (message.isChannelMessage()) {
            channelMessages.get((DiscordChannel)message.getRecipient()).markMessage(message.getId(), type);
        } else {
            userMessages.get((DiscordUser)message.getRecipient()).markMessage(message.getId(), type);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void filter(DiscordMessage message) throws MessageFilterException {
        try {
            log.debug("Received message: {}", message);
            if (message.isChannelMessage()) {
                DiscordChannel channel = ((DiscordMessage<DiscordChannel>) message).getRecipient();
                if (!channelMessages.containsKey(channel)) {
                    initializeChannelHistory(channel);
                } else {
                    channelMessages.get(channel).addMessage(message);
                }
            } else {
                DiscordUser user = ((DiscordMessage<DiscordUser>) message).getRecipient();
                if (!userMessages.containsKey(user)) {
                    initializeUserHistory(user);
                } else {
                    userMessages.get(user).addMessage(message);
                }
            }
        } catch (Exception e) {
            log.error("Error filtering message: "+message, e);
            throw new MessageFilterException();
        }
    }

}
