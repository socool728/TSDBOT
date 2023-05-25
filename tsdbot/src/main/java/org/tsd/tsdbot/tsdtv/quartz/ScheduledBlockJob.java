package org.tsd.tsdbot.tsdtv.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.rest.v1.tsdtv.schedule.ScheduledBlock;
import org.tsd.tsdbot.tsdtv.TSDTV;

public class ScheduledBlockJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(ScheduledBlockJob.class);

    private TSDTV tsdtv;

    public ScheduledBlockJob(TSDTV tsdtv) {
        this.tsdtv = tsdtv;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ScheduledBlock block = (ScheduledBlock) jobExecutionContext.getJobDetail().getJobDataMap()
                .get(Constants.Scheduler.TSDTV_BLOCK_DATA_KEY);
        log.warn("Executing scheduled block job: {}", block);
        tsdtv.startScheduledBlock(block);
    }
}
