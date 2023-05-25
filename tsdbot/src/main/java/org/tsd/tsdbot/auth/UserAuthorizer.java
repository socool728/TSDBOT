package org.tsd.tsdbot.auth;

import io.dropwizard.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAuthorizer implements Authorizer<User> {
    private static final Logger log = LoggerFactory.getLogger(UserAuthorizer.class);

    @Override
    public boolean authorize(User principal, String roleString) {
        log.debug("Checking if user {} has role \"{}\"", principal.getUsername(), roleString);
        Role role = Role.valueOf(roleString);
        return principal.getRole().getLevel() >= role.getLevel();
    }
}
