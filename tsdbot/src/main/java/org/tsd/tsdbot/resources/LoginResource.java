package org.tsd.tsdbot.resources;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.auth.BCrypt;
import org.tsd.tsdbot.auth.LoginCredentials;
import org.tsd.tsdbot.auth.TokenStorage;
import org.tsd.tsdbot.auth.User;
import org.tsd.tsdbot.auth.UserDao;
import org.tsd.tsdbot.view.LoginView;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

@Path("/login")
public class LoginResource {

    private static final Logger log = LoggerFactory.getLogger(LoginResource.class);

    private final UserDao userDao;
    private final TokenStorage tokenStorage;

    @Inject
    public LoginResource(UserDao userDao, TokenStorage tokenStorage) {
        this.userDao = userDao;
        this.tokenStorage = tokenStorage;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getLoginPage(@Auth Optional<User> userOptional) {
        if (userOptional.isPresent()) {
            log.debug("User {} is already logged in, redirecting...", userOptional.get().getUsername());
            return Response
                    .seeOther(UriBuilder.fromUri("/dashboard").build())
                    .build();
        }
        return Response.ok(new LoginView(userOptional)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginCredentials credentials) {
        try {
            String username = StringUtils.trim(credentials.getUsername());
            String rawPassword = StringUtils.trim(credentials.getPassword());
            User user = userDao.findUserByUsername(username);
            if(user != null && BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
                String token = tokenStorage.putUser(user);
                return Response
                        .seeOther(UriBuilder.fromUri("/dashboard").build())
                        .cookie(new NewCookie(Constants.Auth.TOKEN_KEY, token))
                        .build();
            } else {
                log.warn("User failed to login: {}", credentials.getUsername());
            }
        } catch (Exception e) {
            log.error("Error authenticating user", e);
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Failed to authenticate")
                .build();
    }
}
