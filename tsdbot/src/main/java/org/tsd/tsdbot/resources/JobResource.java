package org.tsd.tsdbot.resources;

import org.tsd.rest.v1.tsdtv.job.Job;
import org.tsd.rest.v1.tsdtv.job.JobResult;
import org.tsd.tsdbot.tsdtv.job.JobQueue;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/job")
public class JobResource {

    private final JobQueue jobQueue;

    @Inject
    public JobResource(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    @GET
    @Path("/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollForJob(@PathParam("agentId") String agentId) {
        Job job = jobQueue.pollForJob(agentId);
        if (job != null) {
            return Response.ok(job).build();
        } else {
            return Response.noContent().build();
        }
    }

    @PUT
    @Path("/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveJobResult(@PathParam("jobId") String jobId, JobResult result) {
        jobQueue.updateJobResult(result);
        return Response.ok().build();
    }
}
