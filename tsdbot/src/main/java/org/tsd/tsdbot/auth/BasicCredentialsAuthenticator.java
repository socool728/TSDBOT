package org.tsd.tsdbot.auth;

import com.google.inject.Inject;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.auth.BCrypt;

import java.util.Optional;

public class BasicCredentialsAuthenticator implements Authenticator<BasicCredentials, User> {

    private static final Logger log = LoggerFactory.getLogger(BasicCredentialsAuthenticator.class);

    private final UserDao userDao;

    @Inject
    public BasicCredentialsAuthenticator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User> authenticate(BasicCredentials basicCredentials) throws AuthenticationException {
        String username = basicCredentials.getUsername();
        if(StringUtils.isBlank(username)) {
            log.warn("Failed login due to missing username");
            return Optional.empty();
        }

        log.warn("Attempting login: user {}", username);

        String password = basicCredentials.getPassword();
        if(StringUtils.isBlank(password)) {
            log.warn("Failed login for user {} due to missing password", username);
            return Optional.empty();
        }

        User user = userDao.findUserByUsername(username);
        if (user == null) {
            log.warn("Found no user with username \"{}\"", username);
            return Optional.empty();
        }

        log.info("Found user {}, checking password...", user.getUsername());

        if(BCrypt.checkpw(password, user.getPasswordHash())) {
            log.warn("User {} logged in successfully", user.getUsername());
            return Optional.of(user);
        } else {
            log.warn("Login failed for {} due to incorrect password", user.getUsername());
            return Optional.empty();
        }
    }
}
