package org.tsd.tsdbot.util;

import com.google.inject.Inject;
import com.vdurmont.emoji.EmojiParser;
import de.btobastian.javacord.entities.Server;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.app.DiscordServer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageSanitizer {

    private static final Logger log = LoggerFactory.getLogger(MessageSanitizer.class);

    private final Server server;

    @Inject
    public MessageSanitizer(@DiscordServer Server server) {
        this.server = server;
    }

    @SuppressWarnings("unchecked")
    public String sanitize(String input) {
        return sanitizeChain(input,
                MessageSanitizer::stripEmojis,
                this::replaceUserBeepsWithUsernames,
                MessageSanitizer::stripUrls,
                MessageSanitizer::consolidateWhitespace,
                MessageSanitizer::stripBackticks,
                StringUtils::trim);
    }

    private static String stripUrls(String input) {
        return StringUtils.replaceAll(input, Constants.URL_REGEX, "");
    }

    private static String consolidateWhitespace(String input) {
        return StringUtils.replaceAll(input, "\\s+", " ");
    }

    private static String stripBackticks(String input) {
        return StringUtils.replaceAll(input, "`", "");
    }

    @SuppressWarnings("unchecked")
    private static String stripEmojis(String input) {
        return sanitizeChain(input,
                s -> StringUtils.replaceAll(s, Constants.Emoji.CUSTOM_EMOJI_MENTION_REGEX, ""),
                s -> StringUtils.replaceAll(s, Constants.Emoji.STANDARD_EMOJI_REGEX, ""),
                EmojiParser::removeAllEmojis);
    }

    private String replaceUserBeepsWithUsernames(String input) {
        Pattern pattern = Pattern.compile(Constants.USER_BEEP_REGEX);
        Matcher matcher = pattern.matcher(input);

        Map<String, String> userIds = new HashMap<>();

        while (matcher.find()) {
            String userId = matcher.group(1);
            log.debug("Found user ID in message: {} (\"{}\")", userId, input);
            userIds.put(userId, server.getMemberById(userId).getName());
        }

        for (String id : userIds.keySet()) {
            input = StringUtils.replaceAll(input, "<@"+id+">", userIds.get(id));
        }

        return input;
    }

    private static String sanitizeChain(String input, Function<String, String>... transforms) {
        for (Function<String, String> transform : transforms) {
            input = transform.apply(input);
        }
        return input;
    }
}
