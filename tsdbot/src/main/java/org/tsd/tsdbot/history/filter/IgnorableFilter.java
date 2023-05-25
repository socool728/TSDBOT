package org.tsd.tsdbot.history.filter;

import com.google.inject.Inject;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.history.RemoteConfigurationRepository;

public class IgnorableFilter implements MessageHistoryFilter {

    private final RemoteConfigurationRepository remoteConfigurationRepository;

    @Inject
    public IgnorableFilter(RemoteConfigurationRepository remoteConfigurationRepository) {
        this.remoteConfigurationRepository = remoteConfigurationRepository;
    }

    @Override
    public boolean test(DiscordMessage message) {
        return !remoteConfigurationRepository.isMessageFromIgnorableUser(message)
                && !remoteConfigurationRepository.isMessageInIgnorablePattern(message);
    }
}
