package org.tsd.tsdbot.resources;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.rest.v1.tsdtv.Heartbeat;
import org.tsd.rest.v1.tsdtv.HeartbeatResponse;
import org.tsd.tsdbot.auth.User;
import org.tsd.tsdbot.tsdtv.AgentRegistry;
import org.tsd.tsdbot.tsdtv.BlacklistedAgentException;
import org.tsd.tsdbot.tsdtv.OnlineAgent;
import org.tsd.tsdbot.tsdtv.TSDTVAgent;
import org.tsd.tsdbot.view.TSDTVAgentsView;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

@Path("/tsdtv/agent")
public class TSDTVAgentResource {

    private static final Logger log = LoggerFactory.getLogger(TSDTVAgentResource.class);

    private final AgentRegistry agentRegistry;

    @Inject
    public TSDTVAgentResource(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed({"staff"})
    public TSDTVAgentsView getLoginPage(@Auth User user) {
        log.debug("User accessed TSDTV agent page: {}", user.getUsername());
        return new TSDTVAgentsView(user);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list")
    public List<OnlineAgent> agentList() {
        return new LinkedList<>(agentRegistry.getOnlineAgents());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{agentId}")
    public Response agentHeartbeat(@Context HttpServletRequest request,
                                   @PathParam("agentId") String agentId,
                                   @Auth TSDTVAgent agent,
                                   Heartbeat heartbeat) {
        log.info("Received TSDTV agent heartbeat: {}", agentId);
        log.debug("Heartbeat detail: {}", heartbeat);
        try {
            HeartbeatResponse response = agentRegistry.handleHeartbeat(heartbeat, request.getRemoteAddr());
            return Response.accepted(response).build();
        } catch (BlacklistedAgentException e) {
            log.warn("Received heartbeat from blacklisted agent: {}", agentId);
            return Response.status(403).build();
        }
    }
}
