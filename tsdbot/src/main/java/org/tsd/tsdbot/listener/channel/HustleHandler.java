package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.app.BotUrl;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.hustle.Hustle;
import org.tsd.tsdbot.listener.MessageHandler;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;

import static org.apache.commons.lang3.StringUtils.startsWith;

public class HustleHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(HustleHandler.class);

    private final Hustle hustle;
    private final String hustleUrl;

    @Inject
    public HustleHandler(DiscordAPI api,
                         Hustle hustle,
                         @BotUrl URL botUrl) throws URISyntaxException {
        super(api);
        this.hustle = hustle;
        this.hustleUrl = new URIBuilder(botUrl.toURI())
                .setPath("/hustle")
                .build().toString();
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return startsWith(message.getContent(), Constants.Hustle.COMMAND);
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        String hhr = new DecimalFormat("##0.00").format(hustle.getCurrentHhr());
        String text = String.format("Current hustle/hate ratio: %s -- %s", hhr, hustleUrl);
        channel.sendMessage(text);
    }
}
