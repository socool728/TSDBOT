package org.tsd.tsdbot.tsdtv.job;

import org.tsd.rest.v1.tsdtv.job.TSDTVPlayJob;
import org.tsd.rest.v1.tsdtv.job.TSDTVPlayJobResult;
import org.tsd.rest.v1.tsdtv.job.TSDTVStopJob;
import org.tsd.rest.v1.tsdtv.job.TSDTVStopJobResult;

public interface JobFactory {
    SubmittedJob<TSDTVPlayJob, TSDTVPlayJobResult> createSubmittedTsdtvPlayJob(TSDTVPlayJob job);
    SubmittedJob<TSDTVStopJob, TSDTVStopJobResult> createSubmittedTsdtvStopJob(TSDTVStopJob job);
}
