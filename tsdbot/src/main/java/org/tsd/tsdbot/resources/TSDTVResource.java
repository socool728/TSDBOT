package org.tsd.tsdbot.resources;

import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.rest.v1.tsdtv.PlayMediaRequest;
import org.tsd.rest.v1.tsdtv.StoppedPlayingNotification;
import org.tsd.tsdbot.auth.User;
import org.tsd.tsdbot.tsdtv.MediaNotFoundException;
import org.tsd.tsdbot.tsdtv.TSDTV;
import org.tsd.tsdbot.tsdtv.TSDTVAgent;
import org.tsd.tsdbot.tsdtv.TSDTVException;
import org.tsd.tsdbot.tsdtv.library.TSDTVLibrary;
import org.tsd.tsdbot.util.FileUtils;
import org.tsd.tsdbot.view.TSDTVView;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.Optional;

@Path("/tsdtv")
public class TSDTVResource {

    private static final Logger log = LoggerFactory.getLogger(TSDTVResource.class);

    private final TSDTVLibrary tsdtvLibrary;
    private final TSDTV tsdtv;
    private final FileUtils fileUtils;

    @Inject
    public TSDTVResource(TSDTVLibrary tsdtvLibrary,
                         TSDTV tsdtv,
                         FileUtils fileUtils) {
        this.tsdtvLibrary = tsdtvLibrary;
        this.tsdtv = tsdtv;
        this.fileUtils = fileUtils;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TSDTVView getTsdtvPage(@Auth Optional<User> user, @QueryParam("playerType") String playerType) {
        TSDTVView.PlayerType playerTypeEnum = StringUtils.isBlank(playerType) ?
                TSDTVView.PlayerType.videojs : TSDTVView.PlayerType.valueOf(playerType);
        return new TSDTVView(tsdtvLibrary, playerTypeEnum, user.orElse(null));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/listings")
    public Response getListings() {
        return Response.ok(tsdtvLibrary.getListings()).build();
    }

    @GET
    @Path("/img/{mediaId}")
    public Response getQueueImage(@PathParam("mediaId") Integer mediaId) throws IOException, MediaNotFoundException {
        byte[] data = tsdtvLibrary.getQueueImage(mediaId);
        String contentType = fileUtils.detectMimeType(data);
        return Response.ok()
                .header("Content-Type", contentType)
                .entity((StreamingOutput) output -> {
                    output.write(data);
                    output.flush();
                })
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nowPlaying")
    public Response getNowPlaying(@Context HttpServletRequest request) {
        try {
            String requestIpAddress = request.getRemoteAddr();
            if (StringUtils.isNotBlank(requestIpAddress)) {
                log.debug("Received viewing notification: {}", requestIpAddress);
                tsdtv.reportViewer(requestIpAddress);
            }
            return Response.ok(tsdtv.getLineup()).build();
        } catch (Exception e) {
            log.error("Error building TSDTV lineup", e);
            return Response.serverError().build();
        }
    }

    /*
    When a user clicks a "Play" button to start a TSDTV video
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/play")
    public Response play(PlayMediaRequest playMediaRequest) {
        log.info("Received playMedia instruction: data={}", playMediaRequest);
        try {
            tsdtv.playOrEnqueue(playMediaRequest.getAgentId(), Integer.parseInt(playMediaRequest.getMediaId()));
            return Response.accepted("Accepted").build();
        } catch (TSDTVException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    /*
    When a user clicks the "Stop" button to halt a TSDTV video
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/stop")
    @RolesAllowed({"staff"})
    public Response stop(@Context HttpServletRequest request,
                         @Auth User user) {
        log.info("Received stop instruction, user={}", user.getUsername());
        tsdtv.stopNowPlaying();
        return Response.accepted("Accepted").build();
    }

    /*
    Sent by an agent when a TSDTV video has stopped playing
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/stopped")
    public Response stopped(@Context HttpServletRequest request,
                            @Auth TSDTVAgent agent,
                            StoppedPlayingNotification notification) {
        log.info("Received stopped notification, agent={}: {}", agent.getAgentId(), notification);
        tsdtv.reportStopped(notification.getMediaId());
        return Response.accepted("Accepted").build();
    }
}
