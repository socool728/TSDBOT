package org.tsd.tsdbot.resources;

import org.tsd.tsdbot.filename.FilenameLibrary;
import org.tsd.tsdbot.util.FileUtils;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

@Path("/randomFilenames")
public class RandomFilenameResource {

    private final FilenameLibrary filenameLibrary;
    private final FileUtils fileUtils;

    @Inject
    public RandomFilenameResource(FilenameLibrary filenameLibrary, FileUtils fileUtils) {
        this.filenameLibrary = filenameLibrary;
        this.fileUtils = fileUtils;
    }

    @GET
    @Path("{filename}")
    @Produces("image/*")
    public Response getRandomFilename(@PathParam("filename") String filename) throws IOException {
        byte[] data = filenameLibrary.getRandomFilename(filename).getData();
        String contentType = fileUtils.detectMimeType(data, filename);
        return Response.ok()
                .header("Content-Type", contentType)
                .entity((StreamingOutput) output -> {
                    output.write(data);
                    output.flush();
                })
                .build();
    }
}
