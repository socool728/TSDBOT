package org.tsd.tsdbot.resources;

import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdbot.auth.User;
import org.tsd.tsdbot.view.DashboardView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/dashboard")
public class DashboardResource {

    private static final Logger log = LoggerFactory.getLogger(DashboardResource.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public DashboardView getLoginPage(@Auth User user) {
        log.debug("User accessed dashboard: {}", user.getUsername());
        return new DashboardView(user);
    }
}
