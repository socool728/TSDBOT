package org.tsd.tsdbot.listener.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.lang3.StringUtils;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.DiscordUser;
import org.tsd.tsdbot.history.RemoteConfigurationRepository;
import org.tsd.tsdbot.listener.MessageHandler;

public class ConfigReloadHandler extends MessageHandler<DiscordUser> {

    private final DiscordUser owner;
    private final RemoteConfigurationRepository remoteConfigurationRepository;

    @Inject
    public ConfigReloadHandler(DiscordAPI api,
                               RemoteConfigurationRepository remoteConfigurationRepository,
                               @Named(Constants.Annotations.OWNER) DiscordUser owner) {
        super(api);
        this.owner = owner;
        this.remoteConfigurationRepository = remoteConfigurationRepository;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordUser> message) {
        return message.getAuthor().equals(owner)
                && StringUtils.startsWithIgnoreCase(message.getContent(), ".config");
    }

    @Override
    public void doHandle(DiscordMessage<DiscordUser> message, DiscordUser recipient) throws Exception {
        remoteConfigurationRepository.load();
        message.getAuthor().sendMessage("Successfully reloaded configuration settings");
    }
}
