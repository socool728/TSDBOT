package org.tsd.tsdbot.auth;

import com.google.inject.Inject;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TokenAuthenticator implements Authenticator<String, User> {

    private static final Logger log = LoggerFactory.getLogger(TokenAuthenticator.class);

    private final UserDao userDao;
    private final TokenStorage tokenStorage;

    @Inject
    public TokenAuthenticator(UserDao userDao, TokenStorage tokenStorage) {
        this.userDao = userDao;
        this.tokenStorage = tokenStorage;
    }

    @Override
    public Optional<User> authenticate(String token) throws AuthenticationException {
        if(StringUtils.isBlank(token)) {
            return Optional.empty();
        }

        String userId = tokenStorage.getUser(token);
        User user = userDao.findUserById(userId);
        return Optional.ofNullable(user);
    }
}
