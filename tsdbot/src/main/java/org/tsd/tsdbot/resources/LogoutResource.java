package org.tsd.tsdbot.resources;

import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.auth.User;

import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

@Path("/logout")
public class LogoutResource {

    private static final Logger log = LoggerFactory.getLogger(LogoutResource.class);

    @POST
    public Response logout(@Auth Optional<User> userOptional,
                           @CookieParam(Constants.Auth.TOKEN_KEY) Cookie cookie) {
        userOptional.ifPresent(user -> log.info("Logging out user: {}", user.getUsername()));
        NewCookie newCookie = new NewCookie(cookie, "delete cookie", 0, false);
        return Response.seeOther(UriBuilder.fromUri("/").build()).cookie(newCookie).build();
    }
}
