package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.async.ChannelThreadFactory;
import org.tsd.tsdbot.async.ThreadManager;
import org.tsd.tsdbot.async.dorj.DorjThread;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.listener.MessageHandler;

import javax.inject.Inject;

public class DorjHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(DorjHandler.class);

    private final ThreadManager threadManager;
    private final ChannelThreadFactory channelThreadFactory;

    @Inject
    public DorjHandler(DiscordAPI api, ThreadManager threadManager, ChannelThreadFactory channelThreadFactory) {
        super(api);
        this.threadManager = threadManager;
        this.channelThreadFactory = channelThreadFactory;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return StringUtils.startsWith(message.getContent().trim(), Constants.Dorj.COMMAND);
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling dorj: channel={}, message={}", message.getRecipient(), message.getContent());

        DorjThread existingThread = threadManager.getChannelThread(DorjThread.class, message.getRecipient());
        if (existingThread == null) {
            DorjThread newThread = channelThreadFactory.createDorjThread(channel, message.getAuthor());
            threadManager.addThread(newThread);
        } else {
            existingThread.addSummoner(message.getAuthor());
        }
    }
}
