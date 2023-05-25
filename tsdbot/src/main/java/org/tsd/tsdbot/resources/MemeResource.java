package org.tsd.tsdbot.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdbot.meme.MemeRepository;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

@Path("/memes")
public class MemeResource {

    private static final Logger log = LoggerFactory.getLogger(MemeResource.class);

    private final MemeRepository memeRepository;

    @Inject
    public MemeResource(MemeRepository memeRepository) {
        this.memeRepository = memeRepository;
    }

    @GET
    @Path("{memeId}")
    public Response getFilename(@PathParam("memeId") String memeId) throws IOException {
        log.info("Getting meme with ID {}", memeId);

        byte[] data;
        try {
            data = memeRepository.getMeme(memeId);
        } catch (Exception e) {
            log.error("Error getting meme with ID "+memeId, e);
            return Response.serverError().build();
        }

        return Response.ok()
                .header("Content-Type", "image/jpeg")
                .entity((StreamingOutput) output -> {
                    output.write(data);
                    output.flush();
                })
                .build();
    }
}
