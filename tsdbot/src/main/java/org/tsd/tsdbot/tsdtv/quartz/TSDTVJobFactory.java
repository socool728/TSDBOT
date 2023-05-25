package org.tsd.tsdbot.tsdtv.quartz;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.tsd.tsdbot.tsdtv.TSDTV;

public class TSDTVJobFactory implements JobFactory {

    private final TSDTV tsdtv;

    public TSDTVJobFactory(TSDTV tsdtv) {
        this.tsdtv = tsdtv;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        return new ScheduledBlockJob(tsdtv);
    }
}
