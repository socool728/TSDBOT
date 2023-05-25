package org.tsd.tsdbot.tsdtv.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.rest.v1.tsdtv.job.*;

import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class JobQueue {

    private static final Logger log = LoggerFactory.getLogger(JobQueue.class);

    private final JobFactory jobFactory;
    private final Map<String, SubmittedJob> submittedJobs = new ConcurrentHashMap<>();

    @Inject
    public JobQueue(JobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }

    public TSDTVPlayJobResult submitTsdtvPlayJob(TSDTVPlayJob job)
            throws JobTimeoutException {
        SubmittedJob<TSDTVPlayJob, TSDTVPlayJobResult> submittedJob = jobFactory.createSubmittedTsdtvPlayJob(job);
        return submitAndWait(submittedJob);
    }

    public TSDTVStopJobResult submitTsdtvStopJob(TSDTVStopJob job)
            throws JobTimeoutException {
        SubmittedJob<TSDTVStopJob, TSDTVStopJobResult> submittedJob = jobFactory.createSubmittedTsdtvStopJob(job);
        return submitAndWait(submittedJob);
    }

    private <JOB extends Job, RESULT extends JobResult> RESULT submitAndWait(SubmittedJob<JOB, RESULT> submittedJob)
            throws JobTimeoutException {
        submittedJobs.put(submittedJob.getJob().getId(), submittedJob);
        return submittedJob.waitForResult();
    }

    @SuppressWarnings("unchecked")
    public <T extends JobResult> void updateJobResult(T result) {
        log.info("Updating job result: {}", result);
        SubmittedJob submittedJob = submittedJobs.remove(result.getJobId());
        if (submittedJob == null) {
            log.error("Job ID {} does not exist in submitted jobs map", result.getJobId());
        } else {
            submittedJob.updateResult(result);
        }
    }

    public Job pollForJob(String agentId) {
        synchronized (submittedJobs) {
            log.debug("Polling jobs for agent: {}", agentId);
            List<Map.Entry<String, SubmittedJob>> jobsForAgent = submittedJobs.entrySet()
                    .stream()
                    .filter(entry -> StringUtils.equals(entry.getValue().getAgentId(), agentId))
                    .filter(entry -> entry.getValue().getTaken() == null)
                    .sorted(Comparator.comparing(entry -> entry.getValue().getCreated().toEpochSecond(ZoneOffset.UTC)))
                    .collect(Collectors.toList());
            log.debug("Found jobs for agent {}: {}", agentId, jobsForAgent);
            if (CollectionUtils.isNotEmpty(jobsForAgent)) {
                SubmittedJob submittedJob = submittedJobs.get(jobsForAgent.get(0).getKey());
                submittedJob.take();
                return submittedJob.getJob();
            }
            return null;
        }
    }

    public void handleOfflineAgent(String agentId) {
        synchronized (submittedJobs) {
            log.warn("Removing jobs for offline agent: {}", agentId);
            submittedJobs.entrySet()
                    .removeIf(entry -> StringUtils.equals(entry.getValue().getAgentId(), agentId));
        }
    }
}
