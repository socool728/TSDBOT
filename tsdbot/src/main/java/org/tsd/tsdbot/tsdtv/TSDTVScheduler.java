package org.tsd.tsdbot.tsdtv;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.rest.v1.tsdtv.schedule.Schedule;
import org.tsd.rest.v1.tsdtv.schedule.ScheduledBlock;
import org.tsd.tsdbot.tsdtv.quartz.ScheduledBlockJob;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Singleton
public class TSDTVScheduler {

    private static final Logger log = LoggerFactory.getLogger(TSDTVScheduler.class);

    private final Scheduler scheduler;
    private final AmazonS3 s3Client;
    private final String tsdtvBucket;
    private final String scheduleFilename;
    private final ObjectMapper objectMapper;

    @Inject
    public TSDTVScheduler(AmazonS3 s3Client,
                          ObjectMapper objectMapper,
                          Scheduler scheduler,
                          @Named(Constants.Annotations.S3_TSDTV_BUCKET) String tsdtvBucket,
                          @Named(Constants.Annotations.TSDTV_SCHEDULE) String scheduleFilename) {
        this.s3Client = s3Client;
        this.tsdtvBucket = tsdtvBucket;
        this.objectMapper = objectMapper;
        this.scheduler = scheduler;
        this.scheduleFilename = scheduleFilename;

        try {
            log.warn("Starting TSDTVScheduler...");
            scheduler.start();
            log.warn("Started quartz scheduler");
            loadSchedule();
            log.warn("Successfully started TSDTVScheduler");
        } catch (Exception e) {
            throw new RuntimeException("Error initializing TSDTV scheduler", e);
        }
    }

    public void loadSchedule() throws SchedulerException, IOException {
        log.warn("Loading TSDTV schedule...");
        scheduler.pauseAll();
        log.warn("Paused quartz scheduler");
        Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.groupEquals(Constants.Scheduler.TSDTV_GROUP_ID));
        log.warn("Deleting job keys for group: {} -> {}",
                Constants.Scheduler.TSDTV_GROUP_ID, keys.stream().map(Key::getName).collect(Collectors.joining(",")));
        scheduler.deleteJobs(new LinkedList<>(keys));

        log.info("Fetching schedule file from S3: {}/{}", tsdtvBucket, scheduleFilename);
        S3Object scheduleFile = s3Client.getObject(tsdtvBucket, scheduleFilename);
        if (scheduleFile == null) {
            throw new IOException("Could not find TSDTV schedule file in S3: " + tsdtvBucket + "/" + scheduleFilename);
        }
        
        Schedule schedule = objectMapper.readValue(scheduleFile.getObjectContent(), Schedule.class);
        log.info("Read schedule from S3: {}", schedule);

        TimeZone cronStringTimezone = Constants.Scheduler.TSDTV_DEFAULT_TIME_ZONE;

        if (StringUtils.isNotBlank(schedule.getTimezone())) {
            try {
                log.info("Parsing cron string timezone from TSDTV schedule: {}", schedule.getTimezone());
                cronStringTimezone = TimeZone.getTimeZone(schedule.getTimezone());
                log.info("Successfully parsed cron string timezone from TSDTV schedule: {}", cronStringTimezone);
            } catch (Exception e) {
                log.error("Failed to parse cron string timezone from TSDTV schedule: "+schedule.getTimezone(), e);
            }
        }

        log.info("Using cron string timezone: {}", cronStringTimezone);

        for (ScheduledBlock block : schedule.getScheduledBlocks()) {
            log.info("Evaluating scheduled block: {}", block);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(Constants.Scheduler.TSDTV_BLOCK_DATA_KEY, block);

            JobDetail job = newJob(ScheduledBlockJob.class)
                    .withIdentity(block.getId(), Constants.Scheduler.TSDTV_GROUP_ID)
                    .setJobData(jobDataMap)
                    .build();

            Trigger cronTrigger = newTrigger()
                    .withSchedule(cronSchedule(block.getCronString()).inTimeZone(cronStringTimezone))
                    .build();

            scheduler.scheduleJob(job, cronTrigger);
            log.warn("Successfully scheduled block {}/{}", block.getId(), block.getName());
        }
        scheduler.resumeAll();
        log.warn("Resumed quartz scheduler");
    }

}
