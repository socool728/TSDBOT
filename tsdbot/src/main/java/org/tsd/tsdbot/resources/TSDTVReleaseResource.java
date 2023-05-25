package org.tsd.tsdbot.resources;

import com.google.inject.name.Named;
import de.btobastian.javacord.entities.Server;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.rest.v1.tsdtv.NewReleaseNotification;
import org.tsd.tsdbot.app.DiscordServer;
import org.tsd.tsdbot.discord.DiscordChannel;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/release")
public class TSDTVReleaseResource {

    private static final Logger log = LoggerFactory.getLogger(TSDTVReleaseResource.class);

    private final DiscordChannel tsdtvChannel;

    @Inject
    public TSDTVReleaseResource(@DiscordServer Server server,
                                @Named(Constants.Annotations.TSDTV_CHANNEL) String tsdtvChannel) {
        Optional<DiscordChannel> channel = server.getChannels()
                .stream()
                .filter(c -> StringUtils.equalsIgnoreCase(c.getName(), tsdtvChannel))
                .map(DiscordChannel::new)
                .findAny();
        if (!channel.isPresent()) {
            throw new RuntimeException("Could not find TSDTV channel: " + tsdtvChannel);
        }
        this.tsdtvChannel = channel.get();
        log.info("Initialized TSDTV, channel={}", this.tsdtvChannel);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveNewRelease(NewReleaseNotification newReleaseNotification) {
        String msg = String.format("A new episode of \"%s\" has been downloaded: %s Episode #%s",
                newReleaseNotification.getSeries(),
                newReleaseNotification.getEpisodeName(),
                newReleaseNotification.getEpisodeNumber());
        tsdtvChannel.sendMessage(msg);
        return Response.ok().build();
    }
}
