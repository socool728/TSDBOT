package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.util.MiscUtils;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.*;
import static org.tsd.Constants.Choose.CHOICE_DELIMITERS;
import static org.tsd.Constants.Choose.PREFIX;

public class ChooseHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(ChooseHandler.class);

    @Inject
    public ChooseHandler(DiscordAPI api) {
        super(api);
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return startsWith(message.getContent(), PREFIX);
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling choose: channel={}, message={}",
                channel.getName(), message.getContent());

        String input = substringAfter(message.getContent(), PREFIX).trim();
        log.info("Parsed input: {}", input);

        for (String delimiter : CHOICE_DELIMITERS) {
            if (contains(input, delimiter)) {
                log.info("Using delimiter: {}", delimiter);
                String result = choose(input, delimiter);
                channel.sendMessage(result);
            }
        }
    }

    private String choose(String input, String delimiter) {
        String[] parts = splitByWholeSeparator(input, delimiter);
        log.info("Choices: {}", ArrayUtils.toString(parts));

        if (parts.length < 2) {
            return "Give me some choices";
        }

        String choice = parts[RandomUtils.nextInt(0, parts.length)].trim();
        log.info("Chosen: {}", choice);
        return MiscUtils.formatRandom(choice, Constants.Choose.OUTPUT_FORMATS);
    }
}
