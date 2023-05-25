package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.DiscordUser;
import org.tsd.tsdbot.history.HistoryCache;
import org.tsd.tsdbot.history.HistoryRequest;
import org.tsd.tsdbot.history.filter.FilterFactory;
import org.tsd.tsdbot.history.filter.StandardMessageFilters;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.util.MessageSanitizer;
import org.tsd.tsdbot.util.MiscUtils;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class RecapHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(RecapHandler.class);

    // pre-shuffling limit
    private static final int MESSAGE_HISTORY_COUNT = 5_000;

    // number of dramatic messages to replay in chat
    private static final int DRAMA_MESSAGE_COUNT = 4;

    private static final int COOLDOWN_MINUTES = 5;

    private final HistoryCache historyCache;
    private final FilterFactory filterFactory;
    private final Random random;
    private final Clock clock;
    private final StandardMessageFilters standardMessageFilters;
    private final MessageSanitizer messageSanitizer;

    private final Map<String, Instant> recapUsageMap = new ConcurrentHashMap<>();

    @Inject
    public RecapHandler(DiscordAPI api,
                        HistoryCache historyCache,
                        FilterFactory filterFactory,
                        StandardMessageFilters standardMessageFilters,
                        Random random,
                        MessageSanitizer messageSanitizer,
                        Clock clock) {
        super(api);
        this.historyCache = historyCache;
        this.filterFactory = filterFactory;
        this.random = random;
        this.clock = clock;
        this.messageSanitizer = messageSanitizer;
        this.standardMessageFilters = standardMessageFilters;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return StringUtils.startsWith(message.getContent(), ".recap");
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling recap: channel={}, message={}", message.getRecipient(), message.getContent());

        String recapUsageKey = getRecapUsageKey(message.getAuthor(), channel);
        if (recapUsageMap.containsKey(recapUsageKey)) {
            Instant recapLastUsed = recapUsageMap.get(recapUsageKey);
            Instant now = clock.instant();
            if (recapLastUsed.isAfter(now.minus(COOLDOWN_MINUTES, ChronoUnit.MINUTES))) {
                channel.sendMessage("You just used the .recap function "+message.getAuthor().getName()+", try again in five minutes");
                return;
            }
        }

        HistoryRequest<DiscordChannel> request = HistoryRequest.create(channel, message)
                .withFilter(filterFactory.createLengthFilter(2, 80))
                .withFilters(standardMessageFilters.getStandardFilters())
                .withLimit(MESSAGE_HISTORY_COUNT);

        List<DiscordMessage<DiscordChannel>> messages = historyCache.getChannelHistory(request);
        log.info("Retrieved {} messages in channel history", messages.size());

        if (CollectionUtils.isNotEmpty(messages)) {

            // randomize ordering among the most recent [MESSAGE_HISTORY_COUNT] messages
            Collections.shuffle(messages);

            StringBuilder output = new StringBuilder();

            String narration = MiscUtils.getRandomItemInArray(NARRATION_FORMATS, random);
            String showName = MiscUtils.getRandomItemInArray(SHOW_NAMES, random);
            String nextEpisode = MiscUtils.getRandomItemInArray(EPISODE_NAMES, random);

            assert narration != null;
            output.append(String.format(narration, showName));

            for (int i=0 ; i < Math.min(messages.size(), DRAMA_MESSAGE_COUNT) ; i++) {
                DiscordMessage<DiscordChannel> evaluatingMessage = messages.get(i);

                // if this is the last message, end with ellipses-style drama
                String originalText = messageSanitizer.sanitize(evaluatingMessage.getContent());
                String dramaticText = (i < messages.size()-1) ?
                        DramaStyle.getRandomDrama(originalText, random) : DramaStyle.ellipses.apply(originalText);

                output.append("\n")
                        .append(String.format("[%s]   %s", evaluatingMessage.getAuthor().getName(), dramaticText));
            }

            output.append("\n\n")
                    .append("Tonight's episode: \"").append(nextEpisode).append("\"");

            channel.sendMessage(output.toString());
            recapUsageMap.put(recapUsageKey, clock.instant());
        }
    }

    private static final String[] NARRATION_FORMATS = new String[] {
            "Last time, on a very special \"%s\":",
            "Last week, on a very special \"%s\":",
            "Last time on \"%s\":",
            "Last week on \"%s\":",
            "On last week's \"%s\":",
            "Previously on \"%s\":"
    };

    private static final String[] SHOW_NAMES = new String[] {
            "#tsd",                                 "Team Schooly D",
            "Team Schooly D IRC",                   "TSD: IRC",
            "Team Schooly D: Internet Relay Chat",  "Schooly and the Funky Bunch",
            "Fiasco & Blunder: Halo Cops",          "TSD High",
            "TSDU",                                 "TSD: Miami",
            "Survivor: TSDIRC",                     "TSD: The College Years",
            "Fast Times at TSD High",               "Slappy Days",
            "T.S.D.I.R.C.",                         "Hajime no Kanbo",
            "Tips & Tricks: Professional Rusemen",  "Real Housewives of Bellevue",
            "Schooly Drew Mysteries",               "Macross but Without Kaifun",
            "TSD: Brothers Under Fire",             "TSD: Australia",
            "TSD: Japan"
    };

    private static final String[] EPISODE_NAMES = new String[] {
            "GV's Wild Ride",
            "Crash! The Server's Down for Maintenance?!",
            "The Mystery of DeeJ",
            "Schooly Joins the Army",
            "Hickory Dickory... Dead",
            "Paddy's Big Secret",
            "Little kanbo, Big Adventure!",
            "KP DOA",
            "ZackDark in America",
            "The Red Menace",
            "The Downward Spiral",
            "The Argument",
            "tarehart Goes to College",
            "The Graduation",
            "Video Games",
            "Tex and the Five Magics",
            "The Bonkening",
            "Paddy's Big Goodbye",
            "The KP Caper",
            "Planes, Banes, and Batmobiles",
            "The Laird Problem",
            "Corgidome",
            "Nart Goes to Bed",
            "TDSpiral and the Intervention That Saved Christmas",
            "The Double DorJ",
            "The Splash Bash",
            "A Day without a ZackDark",
            "A Fistful of Clonkers",
            "For a Few Bonks More",
            "BaneKin's Ruse",
            "Paddy's Gambit",
            "Dr. DeeJ and Mr. DorJ",
            "Nart-kun, I'm Sorry",
            "The Swole Toll",
            "If Ever a Whiff There Was",
            "BoneKin Dies",
            "Everyone Dies",
            "The Case of the Missing tarehart",
            "A Good Ban Is Hard to Find",
            "Everyone Has Fun Playing the Master Chief Collection",
            "Star Macross'd Lovers",
            "Rumble in the Tumblr",
            "Minmay's Gambit",
            "Grillin' with Bernie",
            "Bar in the Pocket: Tex and the Hidden Flask Technique",
            "Take Flight, Gundam!",
            "Overmeme",
            "Justice Rains From Above",
            "High Noon",
            "Monkey Trouble",
            "Dr. Strangedeej, or: How I Learned to Stop Worrying and Love the Dorj",
            "Dr. GV, PhD, although I guess if he was a medical doctor he wouldn't have a PhD? Or maybe they can, " +
                    "I don't know. I know he'd be called \"Dr.\" though. I think they should make that clearer, like " +
                    "in the dictionary or wherever they spell things out like that. But I guess it wouldn't be an English " +
                    "thing it'd be a medical licensing and terminology thing? Uuuuuuugggggghhhh it's already so late " +
                    "and I was supposed to go to bed 23 minutes ago but then this came up and uuuggggghhhhh >_>"
    };

    private enum DramaStyle {
        exclamation {
            @Override
            public String apply(String s) {
                //hackish way to get rid of periods/ellipses, too lazy for regex
                s = stripPeriods(s);
                s = s + "!!";
                return s;
            }
        },
        question {
            @Override
            public String apply(String s) {
                //hackish way to get rid of periods/ellipses, too lazy for regex
                s = stripPeriods(s);
                s = s + "??";
                return s;
            }
        },
        bold {
            @Override
            public String apply(String s) {
                return MiscUtils.bold(s);
            }
        },
        caps {
            @Override
            public String apply(String s) {
                return s.toUpperCase();
            }
        },
        ellipses {
            @Override
            public String apply(String s) {
                if(!s.endsWith("..")) {
                    if(s.endsWith("?") || s.endsWith("!")) {
                        s = s.substring(0, s.length()-1);
                    }
                    return s + "...";
                }
                return s; // already ends with ellipses...
            }
        };

        public abstract String apply(String s);

        public static String getRandomDrama(String s, Random random) {
            DramaStyle[] dramas = new DramaStyle[]{exclamation, question, bold, caps}; // no ellipses
            return dramas[random.nextInt(dramas.length)].apply(s);
        }

        private static String stripPeriods(String s) {
            return s.replaceAll("\\.+$","");
        }
    }

    private static String getRecapUsageKey(DiscordUser user, DiscordChannel channel) {
        return String.format("%s/%s", user.getId(), channel.getId());
    }
}
