package org.tsd.tsdbot.listener.channel;

import com.google.inject.name.Named;
import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.DiscordUser;
import org.tsd.tsdbot.history.RemoteConfiguration;
import org.tsd.tsdbot.history.RemoteConfigurationRepository;
import org.tsd.tsdbot.listener.MessageHandler;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;

public class BlacklistHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(BlacklistHandler.class);

    private static final String PREFIX = ".blacklist";

    private final DiscordUser owner;
    private final RemoteConfigurationRepository remoteConfigurationRepository;

    @Inject
    public BlacklistHandler(DiscordAPI api,
                            @Named(Constants.Annotations.OWNER) DiscordUser owner,
                            RemoteConfigurationRepository remoteConfigurationRepository) {
        super(api);
        this.owner = owner;
        this.remoteConfigurationRepository = remoteConfigurationRepository;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return message.getAuthor().equals(owner) && startsWith(message.getContent(), PREFIX);
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling blacklist: channel={}, message={}",
                channel.getName(), message.getContent());

        String input = substringAfter(message.getContent(), PREFIX).trim();
        log.info("Parsed input: {}", input);

        String[] parts = input.split("\\s+");

        if (ArrayUtils.isEmpty(parts)) {
            channel.sendMessage("Invalid syntax");
            return;
        }

        RemoteConfiguration configuration = remoteConfigurationRepository.getRemoteConfiguration();

        boolean handled = false;

        switch (parts[0]) {
            case "add": {
                if (parts.length < 2) {
                    channel.sendMessage("Invalid syntax");
                } else {
                    String userToBlacklist = parts[1].toLowerCase();
                    if (!configuration.getBlacklistedUsers().contains(userToBlacklist)) {
                        configuration.getBlacklistedUsers().add(userToBlacklist);
                        channel.sendMessage(parts[1]+" has been banished to the shadow realm");
                    } else {
                        channel.sendMessage(parts[1]+" is already in the shadow realm");
                    }
                }
                handled = true;
                break;
            }

            case "remove":
            case "rm": {
                if (parts.length < 2) {
                    channel.sendMessage("Invalid syntax");
                } else {
                    String userToFree = parts[1].toLowerCase();
                    if (configuration.getBlacklistedUsers().remove(userToFree)) {
                        channel.sendMessage(parts[1]+" has been released from the shadow realm");
                    } else {
                        channel.sendMessage(parts[1]+" does not appear to be in the shadow realm");
                    }
                }
                handled = true;
                break;
            }

            default: {
                channel.sendMessage("Invalid syntax");
            }
        }

        if (handled) {
            remoteConfigurationRepository.upload();
        }
    }
}
