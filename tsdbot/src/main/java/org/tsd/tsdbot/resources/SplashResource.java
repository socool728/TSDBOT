package org.tsd.tsdbot.resources;

import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdbot.auth.User;
import org.tsd.tsdbot.view.SplashView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

@Path("/")
public class SplashResource {

    private static final Logger log = LoggerFactory.getLogger(SplashResource.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getSplashPage(@Auth Optional<User> user) {
        if (user.isPresent()) {
            return Response.seeOther(UriBuilder.fromUri("/dashboard").build()).build();
        }
        return Response.ok(new SplashView()).build();
    }
}
