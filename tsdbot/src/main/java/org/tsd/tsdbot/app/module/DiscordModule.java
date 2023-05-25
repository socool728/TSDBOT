package org.tsd.tsdbot.app.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import org.apache.commons.lang3.StringUtils;
import org.tsd.Constants;
import org.tsd.tsdbot.app.DiscordServer;
import org.tsd.tsdbot.app.config.TSDBotConfiguration;
import org.tsd.tsdbot.discord.DiscordUser;

import java.util.Optional;

public class DiscordModule extends AbstractModule {

    private final DiscordAPI api;
    private final TSDBotConfiguration configuration;

    public DiscordModule(DiscordAPI api, TSDBotConfiguration configuration) {
        this.api = api;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(DiscordAPI.class).toInstance(api);

        Server borgu = api.getServerById(configuration.getServerId());
        bind(Server.class)
                .annotatedWith(DiscordServer.class)
                .toInstance(borgu);

        Optional<DiscordUser> owner = api.getUsers()
                .stream()
                .filter(user -> StringUtils.equals(configuration.getOwner(), user.getName()))
                .map(DiscordUser::new)
                .findAny();

        if (!owner.isPresent()) {
            throw new RuntimeException("Could not find owner in chat: " + configuration.getOwner());
        }

        bind(DiscordUser.class)
                .annotatedWith(Names.named(Constants.Annotations.OWNER))
                .toInstance(owner.get());

        bind(DiscordUser.class)
                .annotatedWith(Names.named(Constants.Annotations.SELF))
                .toInstance(new DiscordUser(api.getYourself()));
    }
}
