package org.tsd.tsdtv;

import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.bramp.ffmpeg.FFprobe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.rest.v1.tsdtv.*;
import org.tsd.util.FfmpegUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class AgentInventory {

    private static final Logger log = LoggerFactory.getLogger(AgentInventory.class);

    private final File inventoryDirectory;
    private final FFprobe fFprobe;
    private final String agentId;
    private final Map<Integer, Media> filesById = new HashMap<>();

    private final Set<File> invalidFiles = new HashSet<>();

    private boolean forceOverride = false;

    @Inject
    public AgentInventory(@Named("inventory") File inventoryDirectory,
                          @Named("agentId") String agentId,
                          FFprobe fFprobe) {
        this.inventoryDirectory = inventoryDirectory;
        this.fFprobe = fFprobe;
        this.agentId = agentId;
    }

    public Media getFileByMediaId(int mediaId) {
        return filesById.get(mediaId);
    }

    public Inventory compileInventory() {
        Inventory inventory = new Inventory();
        filesById.clear();

        List<File> seriesToEvaluate = new LinkedList<>();
        List<File> moviesToEvaluate = new LinkedList<>();

        for (File file : listFilesAlphabetically(inventoryDirectory)) {
            if (file.isDirectory()) {
                seriesToEvaluate.add(file);
            } else {
                moviesToEvaluate.add(file);
            }
        }

        inventory.setSeries(
                seriesToEvaluate
                        .parallelStream()
                        .map(file -> {
                            try {
                                return compileSeries(file);
                            } catch (Exception e) {
                                log.error("Error building series from directory: "+file.getAbsolutePath(), e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));

        inventory.setMovies(
                moviesToEvaluate
                        .parallelStream()
                        .map(file -> {
                            try {
                                return buildMovie(file);
                            } catch (Exception e) {
                                log.error("Error building movie from file: "+file.getAbsolutePath(), e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));

        log.debug("Built TSDTV inventory: {}", inventory);
        log.debug("Files by ID: {}", filesById);
        return inventory;
    }

    private Series compileSeries(File seriesDirectory) {
        log.info("Compiling series: {}", seriesDirectory.getAbsolutePath());
        Series series = new Series();
        series.setName(seriesDirectory.getName());

        List<File> files = listFilesAlphabetically(seriesDirectory);
        int episodeNumber = 0;
        for (File file : files) {
            log.debug("Evaluating file: {}", file);
            if (file.isDirectory()) {
                Season season = compileSeason(series.getName(), file);
                season.setSeriesName(series.getName());
                series.getSeasons().add(season);
            } else {
                episodeNumber++;
                try {
                    Episode episode = buildEpisode(file, episodeNumber);
                    episode.setSeriesName(series.getName());
                    series.getEpisodes().add(episode);
                } catch (Exception e) {
                    log.error("Error building episode for series: "+file.getAbsolutePath(), e);
                    invalidFiles.add(file);
                }
            }
        }

        return series;
    }

    private Season compileSeason(String seriesName, File seasonDirectory) {
        log.info("Compiling season: {}", seasonDirectory.getAbsolutePath());
        Season season = new Season();
        season.setName(seasonDirectory.getName());

        List<File> files = listFilesAlphabetically(seasonDirectory);
        int episodeNumber = 0;
        for (File file : files) {
            if (!file.isDirectory()) {
                episodeNumber++;
                try {
                    Episode episode = buildEpisode(file, episodeNumber);
                    episode.setSeriesName(seriesName);
                    episode.setSeasonName(season.getName());
                    season.getEpisodes().add(episode);
                } catch (Exception e) {
                    log.error("Error building episode for season: "+file.getAbsolutePath(), e);
                    invalidFiles.add(file);
                }
            }
        }

        return season;
    }

    private Episode buildEpisode(File file, int episodeNumber) throws IOException {
        MediaInfo mediaInfo = FfmpegUtil.getMediaInfo(fFprobe, file);
        Episode episode = new Episode(agentId, mediaInfo);
        episode.setName(file.getName());
        episode.setEpisodeNumber(episodeNumber);
        log.debug("Built episode, file={}, episodeNumber={}: {}",
                file.getAbsolutePath(), episodeNumber, episode);
        filesById.put(episode.getId(), episode);
        return episode;
    }

    private Movie buildMovie(File file) throws IOException {
        MediaInfo mediaInfo = FfmpegUtil.getMediaInfo(fFprobe, file);
        Movie movie = new Movie(agentId, mediaInfo);
        movie.setName(file.getName());
        log.debug("Built movie, file={}: {}", file.getAbsolutePath(), movie);
        return movie;
    }

    private List<File> listFilesAlphabetically(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory.getAbsolutePath()+" is not a directory");
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return new LinkedList<>();
        }
        return Arrays.stream(files)
                .filter(file -> !invalidFiles.contains(file))
                .sorted(Comparator.comparing(file -> file.getName().toLowerCase()))
                .collect(Collectors.toList());
    }

    public boolean isForceOverride() {
        return forceOverride;
    }

    public void setForceOverride(boolean forceOverride) {
        this.forceOverride = forceOverride;
    }
}
