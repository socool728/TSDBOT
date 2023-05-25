package org.tsd.tsdtv.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdtv.AgentInventory;
import org.tsd.tsdtv.TSDBotClient;
import org.tsd.tsdtv.release.model.ReleaseEpisode;
import org.tsd.tsdtv.release.model.ReleaseGroup;
import org.tsd.tsdtv.release.model.ReleaseRepository;
import org.tsd.tsdtv.release.model.ReleaseSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

public abstract class ReleaseFetcher<R extends Release> {

    private static final Logger log = LoggerFactory.getLogger(ReleaseFetcher.class);

    private final File releaseRepositoryJsonFile;
    private final ObjectMapper objectMapper;
    private final TorrentDownloader torrentDownloader;
    private final List<String> seriesToMonitor;
    private final File tsdtvInventory;
    private final AgentInventory agentInventory;
    private final TSDBotClient tsdBotClient;

    public ReleaseFetcher(File releaseRepositoryJsonFile,
                          ObjectMapper objectMapper,
                          TorrentDownloader torrentDownloader,
                          List<String> seriesToMonitor,
                          File tsdtvInventory,
                          AgentInventory agentInventory,
                          TSDBotClient tsdBotClient) {
        this.releaseRepositoryJsonFile = releaseRepositoryJsonFile;
        this.objectMapper = objectMapper;
        this.torrentDownloader = torrentDownloader;
        this.seriesToMonitor = seriesToMonitor == null ? new LinkedList<>() : seriesToMonitor;
        this.tsdtvInventory = tsdtvInventory;
        this.agentInventory = agentInventory;
        this.tsdBotClient = tsdBotClient;
    }

    protected abstract List<R> getReleases() throws Exception;

    public void fetchAndProcess() throws Exception {
        ReleaseRepository releaseRepository
                = objectMapper.readValue(releaseRepositoryJsonFile, ReleaseRepository.class);

        List<R> releasesListing = getReleases();

        for (R releaseListing: releasesListing) {
            if (isValidRelease(releaseListing)) {
                if (log.isDebugEnabled()) {
                    log.debug("Detected valid release: {}", releaseListing);
                } else {
                    log.info("Detected valid release: {}/{}/{}",
                            releaseListing.getReleaseSource(),
                            releaseListing.getSeriesName(),
                            releaseListing.getEpisodeNumber());
                }

                File downloadedFile = null;
                if (isNewRelease(releaseRepository, releaseListing)) {
                    log.warn("Detected new release: {}", releaseListing);
                    if (StringUtils.isNotBlank(releaseListing.getMagnetUri())) {
                        log.warn("Downloading magnet URI for release: {}", releaseListing.getMagnetUri());
                        downloadedFile = torrentDownloader.downloadMagnet(releaseListing.getMagnetUri());
                    } else if (StringUtils.isNotBlank(releaseListing.getTorrentUri())) {
                        log.warn("Downloading torrent URI for release: {}", releaseListing.getTorrentUri());
                        downloadedFile = torrentDownloader.downloadMagnet(releaseListing.getTorrentUri());
                    }

                    log.warn("Downloaded file: {}", downloadedFile);

                    if (downloadedFile == null) {
                        throw new IllegalStateException("No file downloaded for release: "+releaseListing);
                    }

                    File seriesDirectory = new File(tsdtvInventory, releaseListing.getSeriesName());
                    if (seriesDirectory.mkdir()) {
                        log.info("Created directory in TSDTV library for series: \"{}\" -> {}",
                                releaseListing.getSeriesName(), seriesDirectory);
                    } else {
                        log.info("Using existing directory in TSDTV library for series: \"{}\" -> {}",
                                releaseListing.getSeriesName(), seriesDirectory);
                    }

                    String newFilename = String.format("%03d_%s",
                            releaseListing.getEpisodeNumber(), downloadedFile.getName());
                    File newFile = new File(seriesDirectory, newFilename);

                    log.info("Copying file: {} to {}", downloadedFile, newFile);
                    FileUtils.copyFile(downloadedFile, newFile);
                    log.info("Successfully copied file: {}", newFile);
                    agentInventory.setForceOverride(true);
                    tsdBotClient.notifyNewRelease(releaseListing);
                }

                synchronizeReleaseRepository(releaseListing, downloadedFile, releaseRepository);
            }
        }

        log.debug("Saving updated release repository: {} -> {}", releaseRepositoryJsonFile, releaseRepository);
        objectMapper.writeValue(new FileOutputStream(releaseRepositoryJsonFile), releaseRepository);
        log.debug("Successfully wrote to release repository");
    }

    private void synchronizeReleaseRepository(R release, File downloadedFile, ReleaseRepository releaseRepository) {
        log.debug("Synchronizing release repository with release={}, file={}", release, downloadedFile);

        ReleaseGroup group = releaseRepository.getReleaseGroups()
                .stream()
                .filter(g -> g.getGroup().equals(release.getReleaseSource()))
                .findAny().orElse(null);

        if (group == null) {
            log.info("Could not find ReleaseGroup matching source {}, creating...", release.getReleaseSource());
            group = new ReleaseGroup();
            group.setGroup(release.getReleaseSource());
            group.setReleaseSeries(new LinkedList<>());
            releaseRepository.getReleaseGroups().add(group);
        }

        log.debug("Using ReleaseGroup: {}", group);

        ReleaseSeries series = group.getReleaseSeries()
                .stream()
                .filter(s -> StringUtils.equalsIgnoreCase(s.getSeriesName(), release.getSeriesName()))
                .findAny().orElse(null);

        if (series == null) {
            log.info("Could not find ReleaseSeries matching {}/{}, creating...", group.getGroup(), release.getSeriesName());
            series = new ReleaseSeries();
            series.setSeriesName(release.getSeriesName());
            series.setReleasedEpisodes(new LinkedList<>());
            group.getReleaseSeries().add(series);
        }

        log.debug("Using ReleaseSeries: {}", series);

        ReleaseEpisode episode = series.getReleasedEpisodes()
                .stream()
                .filter(e -> StringUtils.equalsIgnoreCase(e.getGuid(), release.getGuid()))
                .findAny().orElse(null);

        if (episode == null) {
            log.info("Could not find ReleaseEpisode matching {}/{}/{}, creating...",
                    group.getGroup(), series.getSeriesName(), release.getGuid());
            episode = new ReleaseEpisode(release, downloadedFile);
            series.getReleasedEpisodes().add(episode);
        } else {
            log.debug("ReleaseEpisode with guid={} already exists in store: {}", release.getGuid(), episode);
        }
    }

    private boolean isValidRelease(R release) {
        return seriesToMonitor
                .stream()
                .anyMatch(s -> StringUtils.containsIgnoreCase(release.getSeriesName(), s));
    }

    private boolean isNewRelease(ReleaseRepository releaseRepository, R release) {
        log.debug("Checking if new release: {}", release);
        return releaseRepository.getReleaseGroups()
                .stream()

                .peek(group -> log.debug("Evaluating group: {}", group.getGroup()))
                .filter(group -> group.getGroup().equals(release.getReleaseSource()))
                .flatMap(group -> group.getReleaseSeries().stream())

                .peek(series -> log.debug("\tEvaluating series: {}", series.getSeriesName()))
                .filter(series -> StringUtils.equalsIgnoreCase(series.getSeriesName(), release.getSeriesName()))
                .flatMap(series -> series.getReleasedEpisodes().stream())

                .peek(episode -> log.debug("\t\tEvaluating episode: {}", episode))
                .noneMatch(episode -> StringUtils.equalsIgnoreCase(episode.getGuid(), release.getGuid()));
    }
}
