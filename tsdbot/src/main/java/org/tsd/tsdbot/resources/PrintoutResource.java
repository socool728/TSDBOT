package org.tsd.tsdbot.resources;

import org.tsd.tsdbot.printout.PrintoutLibrary;
import org.tsd.tsdbot.util.FileUtils;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

@Path("/printout")
public class PrintoutResource {

    private final PrintoutLibrary printoutLibrary;
    private final FileUtils fileUtils;

    @Inject
    public PrintoutResource(PrintoutLibrary printoutLibrary, FileUtils fileUtils) {
        this.printoutLibrary = printoutLibrary;
        this.fileUtils = fileUtils;
    }

    @GET
    @Path("{printoutId}")
    public Response getFilename(@PathParam("printoutId") String printoutId) throws IOException {
        byte[] data = printoutLibrary.getPrintout(printoutId);
        String contentType = fileUtils.detectMimeType(data, printoutId);
        return Response.ok()
                .header("Content-Type", contentType)
                .entity((StreamingOutput) output -> {
                    output.write(data);
                    output.flush();
                })
                .build();
    }
}
