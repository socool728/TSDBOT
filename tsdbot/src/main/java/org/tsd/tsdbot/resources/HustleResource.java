package org.tsd.tsdbot.resources;

import com.google.inject.Inject;
import org.tsd.tsdbot.hustle.Hustle;
import org.tsd.tsdbot.view.HustleView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hustle")
public class HustleResource {

    private final Hustle hustle;

    @Inject
    public HustleResource(Hustle hustle) {
        this.hustle = hustle;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public HustleView hustle() {
        return new HustleView(hustle.getDataPoints());
    }
}
