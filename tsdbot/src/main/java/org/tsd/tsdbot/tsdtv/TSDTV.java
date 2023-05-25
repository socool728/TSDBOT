package org.tsd.tsdbot.tsdtv;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.btobastian.javacord.entities.Server;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.utils.URIBuilder;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.rest.v1.tsdtv.*;
import org.tsd.rest.v1.tsdtv.job.TSDTVPlayJob;
import org.tsd.rest.v1.tsdtv.job.TSDTVPlayJobResult;
import org.tsd.rest.v1.tsdtv.job.TSDTVStopJob;
import org.tsd.rest.v1.tsdtv.queue.EpisodicInfo;
import org.tsd.rest.v1.tsdtv.queue.QueuedItem;
import org.tsd.rest.v1.tsdtv.queue.QueuedItemType;
import org.tsd.rest.v1.tsdtv.schedule.ScheduledBlock;
import org.tsd.rest.v1.tsdtv.schedule.ScheduledBlockSummary;
import org.tsd.rest.v1.tsdtv.schedule.ScheduledItem;
import org.tsd.tsdbot.app.BotUrl;
import org.tsd.tsdbot.app.DiscordServer;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.tsdtv.job.JobQueue;
import org.tsd.tsdbot.tsdtv.job.JobTimeoutException;
import org.tsd.tsdbot.tsdtv.library.AgentMedia;
import org.tsd.tsdbot.tsdtv.library.TSDTVLibrary;
import org.tsd.tsdbot.tsdtv.library.TSDTVListing;
import org.tsd.tsdbot.util.TSDTVUtils;
import org.tsd.tsdtv.TSDTVPlayer;
import org.tsd.util.RetryInstructionFailedException;
import org.tsd.util.RetryUtil;

import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class TSDTV {

    private static final Logger log = LoggerFactory.getLogger(TSDTV.class);

    private static final long STOP_NOW_PLAYING_WAIT_PERIOD_MILLIS
            = TimeUnit.SECONDS.toMillis(10);

    private static final long PLAY_MEDIA_WAIT_PERIOD_MILLIS
            = TimeUnit.SECONDS.toMillis(20);

    private QueuedItem nowPlaying;
    private final List<QueuedItem> queue = new LinkedList<>();

    private final TSDTVLibrary library;
    private final JobQueue jobQueue;
    private final TSDTVEpisodicItemDao episodicItemDao;
    private final TSDTVPlayer player;
    private final Scheduler scheduler;
    private final Clock clock;
    private final String tsdtvStreamUrl;
    private final DiscordChannel channel;
    private final URL botUrl;

    private final Map<String, TSDTVViewer> currentViewers = new HashMap<>();

    @Inject
    public TSDTV(TSDTVLibrary library,
                 TSDTVEpisodicItemDao episodicItemDao,
                 JobQueue jobQueue,
                 TSDTVPlayer player,
                 Clock clock,
                 Scheduler scheduler,
                 @BotUrl URL botUrl,
                 @DiscordServer Server server,
                 @Named(Constants.Annotations.TSDTV_STREAM_URL) String tsdtvStreamUrl,
                 @Named(Constants.Annotations.TSDTV_CHANNEL) String tsdtvChannel) {
        this.library = library;
        this.jobQueue = jobQueue;
        this.episodicItemDao = episodicItemDao;
        this.player = player;
        this.scheduler = scheduler;
        this.clock = clock;
        this.tsdtvStreamUrl = tsdtvStreamUrl;
        this.botUrl = botUrl;

        Optional<DiscordChannel> channel = server.getChannels()
                .stream()
                .filter(c -> StringUtils.equalsIgnoreCase(c.getName(), tsdtvChannel))
                .map(DiscordChannel::new)
                .findAny();
        if (!channel.isPresent()) {
            throw new RuntimeException("Could not find TSDTV channel: " + tsdtvChannel);
        }
        this.channel = channel.get();
        log.info("Initialized TSDTV, channel={}", this.channel);

        log.warn("Starting QueueManagerThread...");
        new Thread(new QueueManagerThread()).start();

        log.warn("Starting ViewerReaperThread...");
        new Thread(new ViewerReaperThread()).start();
    }

    public Lineup getLineup() throws SchedulerException {
        Lineup lineup = new Lineup();

        if (nowPlaying != null) {
            lineup.setNowPlaying(nowPlaying);
        }

        if (queue != null) {
            lineup.setQueue(
                    queue.stream()
                    .filter(queuedItem -> queuedItem.getType() != QueuedItemType.commercial)
                    .collect(Collectors.toList()));
        }

        lineup.setViewers(getViewerCount());
        lineup.getRemainingBlocks().addAll(getScheduledBlocks());

        log.debug("Built lineup: {}", lineup);
        return lineup;
    }

    @SuppressWarnings("unchecked")
    private List<ScheduledBlockSummary> getScheduledBlocks() throws SchedulerException {
        Date now = new Date(clock.millis());
        Date cutoff = DateUtils.addHours(now, 24);
        log.debug("Getting scheduled blocks, now={}, cutoff={}", now, cutoff);
        List<ScheduledBlockSummary> blocks = new LinkedList<>();

        Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.groupEquals(Constants.Scheduler.TSDTV_GROUP_ID));
        for(JobKey key : keys) {
            List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(key);
            if (CollectionUtils.isNotEmpty(triggers)) {
                Date nextFireTime = triggers.get(0).getNextFireTime();
                if (nextFireTime.before(cutoff)) {
                    JobDetail detail = scheduler.getJobDetail(key);
                    ScheduledBlock scheduledBlock = (ScheduledBlock) detail.getJobDataMap().get("blockInfo");
                    ScheduledBlockSummary summary = new ScheduledBlockSummary();
                    summary.setStartTime(nextFireTime.getTime());
                    summary.setName(scheduledBlock.getName());

                    Set<String> shows = new HashSet<>();
                    for (ScheduledItem item : scheduledBlock.getScheduledItems()) {
                        shows.add(item.getSeries());
                    }
                    summary.getShows().addAll(shows);

                    blocks.add(summary);
                }
            }
        }

        blocks.sort(Comparator.comparing(ScheduledBlockSummary::getStartTime));
        log.debug("Compiled scheduled blocks: {}", blocks);
        return blocks;
    }

    public synchronized boolean playOrEnqueue(String agentId, int mediaId) throws TSDTVException {
        log.info("Adding media to queue, agentId={}, mediaId={}", agentId, mediaId);
        Media media = library.findMediaById(agentId, mediaId);
        log.info("Found media: {}", media);
        return playOrEnqueue(new QueuedItem(media));
    }

    private synchronized boolean playOrEnqueue(QueuedItem addingItem) throws TSDTVException {
        if (nowPlaying == null && CollectionUtils.isEmpty(queue)) {
            // play immediately
            nowPlaying(addingItem);
            return true;
        } else {
            // try to enqueue
            if ( (nowPlaying != null && Objects.equals(addingItem.getMedia(), nowPlaying.getMedia())) ||
                    doesQueueContainMedia(addingItem.getMedia()) ) {
                log.error("Duplicate media: nowPlaying={}, queue={}", nowPlaying, queue);
                throw new DuplicateMediaQueuedException(addingItem.getMedia().getAgentId(), addingItem.getMedia().getId());
            }

            if (CollectionUtils.isNotEmpty(queue)) {
                QueuedItem lastItem = queue.get(queue.size() - 1);
                addingItem.setStartTime(lastItem.getEndTime() + Constants.TSDTV.SCHEDULING_FUDGE_FACTOR_MILLIS);
            } else {
                addingItem.setStartTime(nowPlaying.getEndTime() + Constants.TSDTV.SCHEDULING_FUDGE_FACTOR_MILLIS);
            }

            addingItem.updateEndTime();
            queue.add(addingItem);
            return false;
        }
    }

    private boolean doesQueueContainMedia(Media media) {
        return queue.stream().map(QueuedItem::getMedia).anyMatch(m -> m.equals(media));
    }

    public synchronized void stopAll() {
        log.warn("Stopping all media");
        queue.clear();
        log.warn("Queue cleared, stopping media...");
        stopNowPlaying();
        log.warn("Nuke complete");
    }

    /*
    Tell the agent to stop playing
     */
    public synchronized void stopNowPlaying() {
        if (nowPlaying != null) {
            log.info("Stopping nowPlaying: {}", nowPlaying);
            if (nowPlaying.getType().equals(QueuedItemType.commercial)) {
                player.stop();
            } else {
                TSDTVStopJob stopJob = new TSDTVStopJob();
                stopJob.setAgentId(nowPlaying.getMedia().getAgentId());
                stopJob.setTimeoutMillis(STOP_NOW_PLAYING_WAIT_PERIOD_MILLIS);
                try {
                    RetryUtil.executeWithRetry("TSDTVStop", 5, () -> {
                        jobQueue.submitTsdtvStopJob(stopJob);
                    });
                } catch (RetryInstructionFailedException e) {
                    log.error("Failed to stop media " + nowPlaying);
                }
            }
            this.nowPlaying = null;
        }
    }

    /*
    The agent is telling us that the media it was playing has stopped because:
    a. the agent was asked to stop via stopNowPlaying()
    b. the stream stopped normally
    c. the stream stopped in error
     */
    public synchronized void reportStopped(int mediaId) {
        log.info("Handling stopped notification, mediaId={}, nowPlaying={}", mediaId, nowPlaying);
        if (nowPlaying != null && nowPlaying.getMedia().getId() == mediaId) {
            this.nowPlaying = null;
        }
    }

    public synchronized void reportViewer(String ipAddress) {
        if (!currentViewers.containsKey(ipAddress)) {
            currentViewers.put(ipAddress, new TSDTVViewer(ipAddress, clock.instant()));
        } else {
            currentViewers.get(ipAddress).setLastRecorded(clock.instant());
        }
    }

    public synchronized int getViewerCount() {
        return currentViewers.size();
    }

    private void nowPlaying(QueuedItem queuedItem) throws TSDTVException {
        log.info("Setting nowPlaying: {}", queuedItem);

        switch (queuedItem.getType()) {
            case commercial: {
                Commercial commercial = (Commercial) queuedItem.getMedia();
                try {
                    log.info("Playing commercial: {}", commercial);
                    player.play(queuedItem.getMedia(), tsdtvStreamUrl, null, (state) -> {
                        if (state.equals(FFmpegJob.State.FINISHED)) {
                            log.info("Commercial stream ended normally");
                        } else {
                            log.error("Commercial stream ended in error: {}", state);
                        }
                    });
                } catch (Exception e) {
                    log.error("Error playing commercial: " + queuedItem.getMedia(), e);
                } finally {
                    commercial.getFile().delete();
                    log.info("Deleted commercial file: {}", commercial.getFile());
                }
                break;
            }

            case movie:
            case episode: {
                Media media = queuedItem.getMedia();
                log.info("Playing media: {}", media);

                // job request to send to agent
                TSDTVPlayJob playJob = new TSDTVPlayJob();
                playJob.setAgentId(media.getAgentId());
                playJob.setTimeoutMillis(PLAY_MEDIA_WAIT_PERIOD_MILLIS);
                playJob.setMediaId(media.getId());
                playJob.setTargetUrl(tsdtvStreamUrl);
                try {
                    log.info("Sending play request to agent: {}", media.getAgentId());
                    TSDTVPlayJobResult result = jobQueue.submitTsdtvPlayJob(playJob);

                    if (!result.isSuccess()) {
                        throw new TSDTVException("Error playing media: "+playJob);
                    }

                    long startedTimeUTC = result.getTimeStarted();
                    queuedItem.setStartTime(startedTimeUTC);
                    queuedItem.updateEndTime();
                    log.info("Set nowPlaying start/end times, {} -> {}", queuedItem.getStartTime(), queuedItem.getEndTime());
                    this.nowPlaying = queuedItem;

                    String nowPlayingMessage = "[TSDTV] NOW PLAYING: " +
                            getMediaString(this.nowPlaying.getMedia()) +
                            " -- " + getBrowserLink();

                    channel.sendMessage(nowPlayingMessage);

                    long lastItemEndTime = queuedItem.getEndTime();
                    for (QueuedItem inQueue : queue) {
                        inQueue.setStartTime(lastItemEndTime + Constants.TSDTV.SCHEDULING_FUDGE_FACTOR_MILLIS);
                        inQueue.updateEndTime();
                        log.info("Set queued media start/end times, mediaId={}, {} -> {}",
                                inQueue.getMedia().getId(), queuedItem.getStartTime(), queuedItem.getEndTime());
                        lastItemEndTime = inQueue.getEndTime();
                    }

                    // update episode info if available
                    if (queuedItem.getEpisodicInfo() != null) {
                        EpisodicInfo episodicInfo = queuedItem.getEpisodicInfo();
                        log.info("Detected scheduled episode with episodic info: {}", episodicInfo);
                        episodicItemDao.setCurrentEpisode(
                                episodicInfo.getEpisodicSeriesName(),
                                episodicInfo.getEpisodicSeasonName(),
                                episodicInfo.getEffectiveEpisodeNumber()+1);
                    }

                } catch (JobTimeoutException e) {
                    log.error("Timed out waiting for response to play job");
                }
                break;
            }
        }
    }

    public void startScheduledBlock(ScheduledBlock block) {
        log.warn("Starting scheduled block: {}", block);
        this.queue.clear();
        stopNowPlaying();

        TSDTVListing listing = library.getListings();
        List<QueuedItem> toPlay = new LinkedList<>();

        // Used to keep track of episode numbers, since blocks can contain duplicate series/seasons
        Map<ScheduledItem, Integer> episodeNumberMap = new HashMap<>();

        for (ScheduledItem scheduledItem : block.getScheduledItems()) {
            AgentMedia<Series> matchingSeries = listing.getAllSeries()
                    .stream()
                    .filter(series -> StringUtils.equalsIgnoreCase(series.getMedia().getName(), scheduledItem.getSeries()))
                    .findAny().orElse(null);

            if (matchingSeries == null) {
                log.error("Could not find series matching \"{}\" in listings", scheduledItem.getSeries());
            } else {
                Series series = matchingSeries.getMedia();
                if (StringUtils.isNotBlank(scheduledItem.getSeason())) {
                    // this scheduled item specifies a season, find it
                    Season season = series.getSeasons()
                            .stream()
                            .filter(s -> StringUtils.equalsIgnoreCase(s.getName(), scheduledItem.getSeason()))
                            .findAny().orElse(null);
                    if (season == null) {
                        log.error("Could not find season matching \"{}\" for series {}",
                                scheduledItem.getSeason(), series.getName());
                    } else {
                        Episode episodeToPlay = findEpisodeToPlay(scheduledItem,
                                episodeNumberMap, series.getName(), season.getName(), season.getEpisodes());
                        EpisodicInfo episodicInfo = new EpisodicInfo();
                        episodicInfo.setEpisodicSeriesName(series.getName());
                        episodicInfo.setEpisodicSeasonName(season.getName());
                        episodicInfo.setEffectiveEpisodeNumber(episodeToPlay.getEpisodeNumber());
                        toPlay.add(new QueuedItem(episodeToPlay, episodicInfo));
                    }
                } else {
                    // this scheduled item only specifies a series
                    Episode episodeToPlay = null;
                    EpisodicInfo episodicInfo = null;

                    if (CollectionUtils.isNotEmpty(series.getSeasons())) {
                        // this series has seasons -- bundle up all episodes, use their index as episode number
                        List<Episode> effectiveEpisodes = TSDTVUtils.getEffectiveEpisodes(series);

                        int maxEpisodeNumber = effectiveEpisodes.size();
                        int currentEpisode = getEffectiveCurrentEpisodeNumber(scheduledItem,
                                episodeNumberMap, series.getName(), null);

                        if (currentEpisode > maxEpisodeNumber) {
                            log.info("currentEpisode {} is larger than maxEpisode {}, using first...",
                                    currentEpisode, maxEpisodeNumber);
                            episodeToPlay = effectiveEpisodes.get(0);
                            currentEpisode = 1;
                        } else {
                            episodeToPlay = effectiveEpisodes.get(currentEpisode-1);
                        }

                        episodicInfo = new EpisodicInfo();
                        episodicInfo.setEpisodicSeriesName(series.getName());
                        episodicInfo.setEffectiveEpisodeNumber(currentEpisode);
                        episodeNumberMap.put(scheduledItem, currentEpisode+1);
                    } else {
                        // this series has no seasons -- use episode number from media
                        episodeToPlay = findEpisodeToPlay(scheduledItem,
                                episodeNumberMap, series.getName(), null, series.getEpisodes());
                        episodicInfo = new EpisodicInfo();
                        episodicInfo.setEpisodicSeriesName(series.getName());
                        episodicInfo.setEffectiveEpisodeNumber(episodeToPlay.getEpisodeNumber());
                    }

                    QueuedItem queuedItem = new QueuedItem(episodeToPlay, episodicInfo);
                    log.info("Adding episode: {}", queuedItem);
                    toPlay.add(queuedItem);
                }
            }

            if (scheduledItem.getCommercialBreakMinutes() > 0) {
                double secondsRemaining = scheduledItem.getCommercialBreakMinutes()*60;
                while (secondsRemaining > 0) {
                    Commercial commercial = library.getCommercial();
                    if (commercial != null) {
                        toPlay.add(new QueuedItem(commercial));
                        secondsRemaining -= commercial.getMediaInfo().getDurationSeconds();
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(toPlay)) {
            String notification = "[TSDTV] Scheduled block now starting: " +
                    block.getName() + ". " +
                    "Lined up: " + buildShowsPlayingInBlock(toPlay) +
                    " -- " + getBrowserLink();
            channel.sendMessage(notification);

            for (QueuedItem queuedItem : toPlay) {
                try {
                    log.info("Adding scheduled item to queue: {}", queuedItem);
                    playOrEnqueue(queuedItem);
                } catch (Exception e) {
                    log.error("Error adding scheduled item to queue: " + queuedItem, e);
                }
            }
        } else {
            log.error("Could not play any shows for block: {}", block);
        }
    }
    
    private static String buildShowsPlayingInBlock(List<QueuedItem> blockItems) {
        return blockItems
                .stream()
                .filter(queuedItem -> queuedItem.getType() != QueuedItemType.commercial)
                .map(queuedItem -> {
                    if (queuedItem.getMedia() instanceof Episode) {
                        return ((Episode) queuedItem.getMedia()).getSeriesName();
                    } else if (queuedItem.getMedia() instanceof Movie) {
                        return ((Movie) queuedItem.getMedia()).getName();
                    }
                    return "UNKNOWN";
                })
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private Episode findEpisodeToPlay(ScheduledItem scheduledItem,
                                      Map<ScheduledItem, Integer> episodeNumberMap,
                                      String seriesName, String seasonName,
                                      List<Episode> episodes) {
        int episodeNumber = getEffectiveCurrentEpisodeNumber(scheduledItem, episodeNumberMap, seriesName, seasonName);
        Episode episode = findEpisodeMatchingNumber(episodes, episodeNumber);
        episodeNumberMap.put(scheduledItem, episode.getEpisodeNumber()+1);
        return episode;
    }

    private int getEffectiveCurrentEpisodeNumber(ScheduledItem scheduledItem,
                                                 Map<ScheduledItem, Integer> episodeNumberMap,
                                                 String seriesName, String seasonName) {
        if (episodeNumberMap.containsKey(scheduledItem)) {
            return episodeNumberMap.get(scheduledItem);
        } else {
            TSDTVEpisodicItem episodicItem = episodicItemDao.getCurrentEpisode(seriesName, seasonName);
            return episodicItem.getCurrentEpisode();
        }
    }

    private Episode findEpisodeMatchingNumber(List<Episode> allEpisodes, int number) {
        if (CollectionUtils.isEmpty(allEpisodes)) {
            throw new IllegalArgumentException("Episode list is null");
        }

        TreeMap<Integer, Episode> episodesOrderedByNumber = new TreeMap<>();
        for (Episode episode : allEpisodes) {
            log.info("Ordering episode by number: {} -> {}", episode.getEpisodeNumber(), episode);
            episodesOrderedByNumber.put(episode.getEpisodeNumber(), episode);
        }

        Episode matchingEpisode = null;
        int maxEpisodeNumber = episodesOrderedByNumber.lastKey();
        int searchingNumber = number;
        log.info("maxEpisodeNumber: {}", maxEpisodeNumber);
        while (matchingEpisode == null && searchingNumber <= maxEpisodeNumber) {
            Episode episode = episodesOrderedByNumber.get(searchingNumber);
            if (episode != null) {
                log.info("Found episode for number: {} -> {}", searchingNumber, episode);
                matchingEpisode = episode;
            } else {
                log.warn("Could not find episode for number {}", searchingNumber);
            }
            searchingNumber++;
        }

        if (matchingEpisode == null) {
            log.warn("Could not find any episodes matching number {}, using lowest available...", number);
            matchingEpisode = episodesOrderedByNumber.firstEntry().getValue();
        }

        log.info("Matching episode: {}", matchingEpisode);
        return matchingEpisode;
    }

    private String getMediaString(Media media) {
        if (media instanceof Episode) {
            Episode episode = (Episode) media;
            StringBuilder builder = new StringBuilder(episode.getSeriesName());
            if (StringUtils.isNotBlank(episode.getSeasonName())) {
                builder.append(", ").append(episode.getSeasonName());
            }
            builder.append(": ").append(episode.getName());
            return builder.toString();
        } else if (media instanceof Movie) {
            Movie movie = (Movie) media;
            return movie.getName();
        } else {
            return "UNKNOWN";
        }
    }

    private String getBrowserLink() {
        try {
            return new URIBuilder(botUrl.toURI())
                    .setPath("/tsdtv")
                    .toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class QueueManagerThread implements Runnable {
        private boolean shutdown = false;

        @Override
        public void run() {
            while (!shutdown) {
                if (nowPlaying == null && CollectionUtils.isNotEmpty(queue)) {
                    synchronized (queue) {
                        QueuedItem queuedItem = queue.remove(0);
                        log.info("Moving queued item to nowPlaying: {}", queuedItem.getMedia());
                        try {
                            nowPlaying(queuedItem);
                        } catch (Exception e) {
                            log.error("Error playing media: " + queuedItem.getMedia(), e);
                        }
                    }
                }
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                } catch (Exception e) {
                    log.error("Interrupted");
                    shutdown = true;
                }
            }
        }
    }

    class ViewerReaperThread implements Runnable {
        private boolean shutdown = false;

        @Override
        public void run() {
            while (!shutdown) {
                synchronized (currentViewers) {
                    Instant cutoff = clock.instant().minus(30, ChronoUnit.SECONDS);
                    currentViewers.entrySet()
                            .removeIf(entry -> {
                                return entry.getValue().getLastRecorded().isBefore(cutoff);
                            });
                }
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(30));
                } catch (Exception e) {
                    log.error("Interrupted");
                    shutdown = true;
                }
            }
        }
    }
}
