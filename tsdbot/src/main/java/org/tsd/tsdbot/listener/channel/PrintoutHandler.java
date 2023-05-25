package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.app.BotUrl;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.printout.PrintoutLibrary;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class PrintoutHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(PrintoutHandler.class);

    private final URL botUrl;
    private final PrintoutLibrary printoutLibrary;

    @Inject
    public PrintoutHandler(DiscordAPI api,
                           PrintoutLibrary printoutLibrary,
                           @BotUrl URL botUrl) {
        super(api);
        this.botUrl = botUrl;
        this.printoutLibrary = printoutLibrary;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return StringUtils.isNotBlank(message.getContent())
                && (message.getContent().matches(Constants.Printout.QUERY_REGEX)
                    || printoutLibrary.isUserPendingComputing(message.getAuthor()));
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling printout: channel={}, message={}",
                channel.getName(), message.getContent());

        if (printoutLibrary.isUserPendingComputing(message.getAuthor())) {
            String input = splitAndCombine(message.getContent());
            String capitalized = input.toUpperCase();
            if (StringUtils.equals(input, capitalized)) {
                String printoutId = printoutLibrary.generatePrintout(input);
                channel.sendMessage(buildUrlForPrintout(printoutId));
                printoutLibrary.removeUserNotComputing(message.getAuthor());
            }
        } else {
            Matcher matcher = Constants.Printout.QUERY_PATTERN.matcher(message.getContent());
            if (matcher.find()) {
                if (RandomUtils.nextDouble(0.0, 1.0) > 0.8) {
                    printoutLibrary.addUserNotComputing(message.getAuthor());
                    channel.sendMessage("Not computing. Please repeat.");
                } else {
                    String query = matcher.group(1);
                    String printoutId = printoutLibrary.generatePrintout(query);
                    channel.sendMessage(buildUrlForPrintout(printoutId));
                }
            }
        }
    }

    private String buildUrlForPrintout(String printoutId) throws URISyntaxException {
        return new URIBuilder(botUrl.toURI())
                .setPath("/printout/"+printoutId)
                .build().toString();
    }

    private static String splitAndCombine(String input) {
        String[] parts = input.split("\\.");
        return Arrays.stream(parts)
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

}
