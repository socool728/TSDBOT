package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.history.HistoryCache;
import org.tsd.tsdbot.history.HistoryRequest;
import org.tsd.tsdbot.history.filter.FilterFactory;
import org.tsd.tsdbot.listener.MessageHandler;

import javax.inject.Inject;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(ReplaceHandler.class);

    private static final Pattern REPLACE_PATTERN = Pattern.compile("^r/([^/]+)/([^/]*)(.*)$");

    private final HistoryCache historyCache;
    private final FilterFactory filterFactory;

    @Inject
    public ReplaceHandler(DiscordAPI api, HistoryCache historyCache, FilterFactory filterFactory) {
        super(api);
        this.historyCache = historyCache;
        this.filterFactory = filterFactory;
    }

    private String tryStringReplace(List<DiscordMessage<DiscordChannel>> messages, String text) {
        Matcher matcher = REPLACE_PATTERN.matcher(text);
        if (matcher.find()) {

            String find = matcher.group(1);
            String replace = matcher.group(2);
            String theRest = matcher.group(3);
            log.debug("find=\"{}\", replace=\"{}\", theRest=\"{}\"", find, replace, theRest);

            // Trim off any leading "/g" looking stuff that comes before the username
            String user = theRest.replaceFirst("^(/g ?|/)\\s*", "");
            log.debug("user=\"{}\"", user);

            for (DiscordMessage<DiscordChannel> message : messages) {
                log.debug("Evaluating message: user={}, author={}, \"{}\"",
                        user, message.getAuthor().getName(), message.getContent());
                if (StringUtils.isBlank(user) || StringUtils.equalsIgnoreCase(user, message.getAuthor().getName())) {
                    String replaced = replace(message.getContent(), find, replace);
                    log.debug("Replace result: {}", replaced);
                    if (replaced != null) {
                        return message.getAuthor().getName() + " MEANT to say: " + replaced;
                    }
                }
            }
        }

        return null;
    }

    private String replace(String message, String find, String replace) {
        String modified = message.replace(find, replace);
        return modified.equals(message) ? null : modified;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return REPLACE_PATTERN.matcher(message.getContent()).matches();
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling replace: channel={}, message={}", message.getRecipient(), message.getContent());
        HistoryRequest<DiscordChannel> request = HistoryRequest.create(channel, message)
                .withFilter(filterFactory.createNoFunctionsFilter())
                .withFilter(filterFactory.createNoOwnMessagesFilter())
                .withFilter(filterFactory.createNoBotsFilter())
                .withFilter(filterFactory.createIgnorableFilter());
        List<DiscordMessage<DiscordChannel>> messages = historyCache.getChannelHistory(request);
        log.info("Retrieved {} messages in channel history", messages.size());
        String result = tryStringReplace(messages, message.getContent());
        if (StringUtils.isNotBlank(result)) {
            log.info("Replace result: \"{}\" -> \"{}\"", message.getContent(), result);
            channel.sendMessage(result);
        }
    }
}
