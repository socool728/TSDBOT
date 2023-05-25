package org.tsd.tsdbot.resources;

import org.tsd.tsdbot.filename.FilenameLibrary;
import org.tsd.tsdbot.util.FileUtils;
import org.tsd.tsdbot.view.FilenamesView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

@Path("/filenames")
public class FilenameResource {

    private final FilenameLibrary filenameLibrary;
    private final FileUtils fileUtils;

    @Inject
    public FilenameResource(FilenameLibrary filenameLibrary, FileUtils fileUtils) {
        this.filenameLibrary = filenameLibrary;
        this.fileUtils = fileUtils;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public FilenamesView listFilenames() {
        return new FilenamesView(filenameLibrary);
    }

    @GET
    @Path("{filename}")
    public Response getFilename(@PathParam("filename") String filename) throws IOException {
        byte[] data = filenameLibrary.getFilename(filename).getData();
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
