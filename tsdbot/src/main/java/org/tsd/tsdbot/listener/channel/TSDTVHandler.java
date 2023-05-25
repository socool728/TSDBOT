package org.tsd.tsdbot.listener.channel;

import com.google.inject.Inject;
import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.rest.v1.tsdtv.Episode;
import org.tsd.rest.v1.tsdtv.Movie;
import org.tsd.rest.v1.tsdtv.Season;
import org.tsd.rest.v1.tsdtv.Series;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.DiscordUser;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.tsdtv.TSDTV;
import org.tsd.tsdbot.tsdtv.TSDTVScheduler;
import org.tsd.tsdbot.tsdtv.library.AgentMedia;
import org.tsd.tsdbot.tsdtv.library.TSDTVLibrary;
import org.tsd.tsdbot.tsdtv.library.TSDTVListing;
import org.tsd.tsdbot.util.AuthUtil;
import org.tsd.tsdbot.util.MiscUtils;
import org.tsd.tsdbot.util.TSDTVUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TSDTVHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(TSDTVHandler.class);

    private final AuthUtil authUtil;
    private final TSDTV tsdtv;
    private final TSDTVScheduler scheduler;
    private final TSDTVLibrary library;

    @Inject
    public TSDTVHandler(DiscordAPI api,
                        AuthUtil authUtil,
                        TSDTV tsdtv,
                        TSDTVLibrary tsdtvLibrary,
                        TSDTVScheduler tsdtvScheduler) {
        super(api);
        this.authUtil = authUtil;
        this.tsdtv = tsdtv;
        this.library = tsdtvLibrary;
        this.scheduler = tsdtvScheduler;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return StringUtils.startsWith(message.getContent().trim(), Constants.TSDTV.COMMAND_PREFIX);
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling tsdtv: channel={}, message={}", message.getRecipient().getName(), message.getContent());

        String[] parts = message.getContent().split("\\s+");

        switch (parts[1].toLowerCase()) {
            case "reload": {
                if (authUtil.userIsAdmin(message.getAuthor())) {
                    scheduler.loadSchedule();
                    channel.sendMessage("The schedule has been reloaded");
                } else {
                    channel.sendMessage("You don't have permission to do that");
                }
                break;
            }
            case "play": {
                handlePlay(channel, parts);
                break;
            }
            case "kill": {
                handleKill(channel, message.getAuthor(),false);
                break;
            }
            case "nuke": {
                handleKill(channel, message.getAuthor(), true);
                break;
            }
            default: {
                channel.sendMessage("Unknown command");
            }
        }
    }

    private void handleKill(DiscordChannel channel, DiscordUser author, boolean nuke) {
        if (authUtil.userIsAdmin(author)) {
            if (nuke) {
                log.warn("Nuke instruction received");
                tsdtv.stopAll();
                channel.sendMessage("The stream and queue have been nuked (with no survivors)");
            } else {
                log.warn("Kill instruction received");
                tsdtv.stopNowPlaying();
                channel.sendMessage("The current stream has been killed");
            }
        } else {
            channel.sendMessage("You don't have permission to do that");
        }
    }

    private void handlePlay(DiscordChannel channel, String[] messageParts) {
        QueryCaptor captor = new QueryCaptor();

        for (String word : messageParts) {
            if (!captor.flipMode(word)) {
                captor.addQuery(word);
            }
        }

        TSDTVListing listing = library.getListings();

        if (captor.hasQueries(QueryCaptorMode.movie)) {
            // user searched for a movie
            List<Movie> matches = search(
                    listing.getAllMovies().stream().map(AgentMedia::getMedia),
                    Movie::getName,
                    captor.getQueries().get(QueryCaptorMode.movie));

            if (CollectionUtils.isEmpty(matches)) {
                channel.sendMessage("Found no movies matching that search");
            } else if (matches.size() > 1) {
                channel.sendMessage("Found multiple movies matching that search: " + matches.stream().map(Movie::getName).collect(Collectors.joining(", ")));
            } else {
                Movie movie = matches.get(0);
                try {
                    if (tsdtv.playOrEnqueue(movie.getAgentId(), movie.getId())) {
                        channel.sendMessage("Now playing: " + movie.getName());
                    } else {
                        channel.sendMessage("Your movie \""+movie.getName()+"\" has been enqueued");
                    }
                } catch (Exception e) {
                    log.error("Error playing movie " + movie.getName(), e);
                    channel.sendMessage("An error occurred when playing your movie");
                }
            }
        } else if (captor.hasQueries(QueryCaptorMode.series)) {
            // user searched for a series
            List<Series> matches = search(
                    listing.getAllSeries().stream().map(AgentMedia::getMedia),
                    Series::getName,
                    captor.getQueries().get(QueryCaptorMode.series));

            if (CollectionUtils.isEmpty(matches)) {
                channel.sendMessage("Found no series matching that search");
            } else if (matches.size() > 1) {
                channel.sendMessage("Found multiple series matching that search: "
                        + matches.stream().map(Series::getName).collect(Collectors.joining(", ")));
            } else {
                Series series = matches.get(0);

                Episode episode = null;
                String seriesSeasonString = series.getName();
                List<Episode> possibleEpisodes = null;

                if (captor.hasQueries(QueryCaptorMode.season)) {
                    // user specified a season
                    List<Season> matchingSeasons = search(
                            series.getSeasons().stream(),
                            Season::getName,
                            captor.getQueries().get(QueryCaptorMode.season));

                    if (CollectionUtils.isEmpty(matchingSeasons)) {
                        channel.sendMessage("Found no seasons for " + series.getName() + " matching that search");
                    } else if (matchingSeasons.size() > 1) {
                        channel.sendMessage("Found multiple seasons for " + series.getName()
                                + " matching that search: " + matchingSeasons.stream().map(Season::getName).collect(Collectors.joining(", ")));
                    } else {
                        Season season = matchingSeasons.get(0);
                        possibleEpisodes = season.getEpisodes();
                        seriesSeasonString += (" / "+season.getName());
                    }

                } else {
                    // user did not specify a season
                    possibleEpisodes = TSDTVUtils.getEffectiveEpisodes(series);
                }

                if (CollectionUtils.isNotEmpty(possibleEpisodes)) {
                    if (captor.hasQueries(QueryCaptorMode.episode)) {
                        List<Episode> matchingEpisodes = search(
                                possibleEpisodes.stream(),
                                Episode::getName,
                                captor.getQueries().get(QueryCaptorMode.episode));

                        if (CollectionUtils.isEmpty(matchingEpisodes)) {
                            channel.sendMessage(String.format("Found no episodes for %s matching that search",
                                    seriesSeasonString));
                        } else if (matchingEpisodes.size() > 1) {
                            channel.sendMessage(String.format("Found multiple episodes for %s matching that search: %s",
                                    seriesSeasonString,
                                    matchingEpisodes.stream().map(Episode::getName).collect(Collectors.joining(", "))));
                        } else {
                            episode = matchingEpisodes.get(0);
                        }
                    } else {
                        Integer episodeNumber = captor.getQueriedEpisodeNumber();
                        if (episodeNumber == null) {
                            // use a random episode for this season
                            episode = MiscUtils.getRandomItemInList(possibleEpisodes);
                        } else {
                            episode = getEpisodeForNumber(possibleEpisodes, episodeNumber);
                            if (episode == null) {
                                channel.sendMessage(String.format("Could not find episode number %d for %s",
                                        episodeNumber, seriesSeasonString));
                            }
                        }
                    }
                }

                // play an episode if we found one
                if (episode != null) {
                    try {
                        if (tsdtv.playOrEnqueue(episode.getAgentId(), episode.getId())) {
                            channel.sendMessage(String.format("Now playing: %s / %s",
                                    seriesSeasonString, episode.getName()));
                        } else {
                            channel.sendMessage(String.format("Your show has been enqueued: %s / %s",
                                    seriesSeasonString, episode.getName()));
                        }
                    } catch (Exception e) {
                        log.error("Error playing episode " + episode.getName(), e);
                        channel.sendMessage(String.format("An error occurred when playing your show: %s / %s",
                                seriesSeasonString, episode.getName()));
                    }
                }

            }
        } else {
            channel.sendMessage("Must search for either a movie or series, " +
                    "e.g. \".tsdtv play -series my awesome anime\" or \".tsdtv play -movie my shitty movie\"");
        }
    }

    private static Episode getEpisodeForNumber(List<Episode> episodes, int number) {
        return episodes.stream()
                .filter(episode -> {
                    if (episode.getOverriddenEpisodeNumber() != null) {
                        return episode.getOverriddenEpisodeNumber() == number;
                    } else {
                        return episode.getEpisodeNumber() == number;
                    }
                })
                .findAny().orElse(null);
    }

    private static <T> List<T> search(Stream<T> items,
                                      Function<T, String> toString,
                                      Collection<String> queryWords) {
        for (String word : queryWords) {
            items = items.filter(item -> StringUtils.containsIgnoreCase(toString.apply(item), word));
        }
        return items.collect(Collectors.toList());
    }

    class QueryCaptor {
        private QueryCaptorMode mode;
        private Map<QueryCaptorMode, List<String>> queries = new HashMap<>();

        /**
         * @return true if this input flipped the mode
         */
        boolean flipMode(String input) {
            log.debug("Evaluating query word: {}", input);
            QueryCaptorMode matchingMode = Arrays.stream(QueryCaptorMode.values())
                    .filter(mode -> mode.getFlag().equalsIgnoreCase(input))
                    .findAny().orElse(null);
            if (matchingMode != null) {
                log.debug("Detected query mode flag: {}", matchingMode);
                this.mode = matchingMode;
                return true;
            } else {
                log.debug("Detected query word: {}", input);
                return false;
            }
        }

        void addQuery(String input) {
            if (mode != null) {
                log.debug("Adding query word: {} -> {}", mode, input);
                if (!queries.containsKey(mode)) {
                    queries.put(mode, new LinkedList<>());
                }
                queries.get(mode).add(input);
            }
        }

        public Map<QueryCaptorMode, List<String>> getQueries() {
            return queries;
        }

        public boolean hasQueries(QueryCaptorMode type) {
            return CollectionUtils.isNotEmpty(queries.get(type));
        }

        public Integer getQueriedEpisodeNumber() {
            if (CollectionUtils.isEmpty(queries.get(QueryCaptorMode.episode_num))) {
                return null;
            }

            Integer episodeNumber = null;
            for (String numberString : queries.get(QueryCaptorMode.episode_num)) {
                try {
                    episodeNumber = Integer.parseInt(numberString);
                    if (episodeNumber < 1) {
                        log.warn("Parsed negative episode number: {}", episodeNumber);
                        episodeNumber = null;
                    }
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse episode number from query string: {}", numberString);
                }
            }

            return episodeNumber;
        }
    }

    enum QueryCaptorMode {
        movie       ("-movie"),
        series      ("-series"),
        season      ("-season"),
        episode     ("-episode"),
        episode_num ("-episodeNumber");

        private final String flag;

        QueryCaptorMode(String flag) {
            this.flag = flag;
        }

        public String getFlag() {
            return flag;
        }
    }
}
