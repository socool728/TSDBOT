package org.tsd.tsdbot.util;

import de.btobastian.javacord.entities.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.app.DiscordServer;
import org.tsd.tsdbot.discord.DiscordUser;

import javax.inject.Inject;

public class AuthUtil {

    private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);

    private final Server server;

    @Inject
    public AuthUtil(@DiscordServer Server server) {
        this.server = server;
    }

    public boolean userIsAdmin(DiscordUser user) {
        log.info("Checking if user is admin: {}", user);
        return userHasRole(user, Constants.Role.TSD);
    }

    public boolean userHasRole(DiscordUser user, String role) {
        log.info("Checking if user has role {}: {}", role, user);
        return user.hasRole(server, role);
    }
}
