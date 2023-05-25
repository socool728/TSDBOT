package org.tsd.tsdbot.async.dorj;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.async.ChannelThread;
import org.tsd.tsdbot.async.ThreadManager;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.DiscordUser;
import org.tsd.tsdbot.history.HistoryCache;
import org.tsd.tsdbot.history.HistoryRequest;
import org.tsd.tsdbot.history.filter.FilterFactory;
import org.tsd.tsdbot.twitter.TwitterManager;
import org.tsd.tsdbot.util.MiscUtils;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DorjThread extends ChannelThread {

    private static final Logger log = LoggerFactory.getLogger(DorjThread.class);

    private final FilterFactory filterFactory;
    private final HistoryCache historyCache;
    private final TwitterManager twitterManager;
    private final List<String> dorjFormats;
    private final Set<DiscordUser> summoners = new HashSet<>();

    @Inject
    public DorjThread(ThreadManager threadManager,
                      TwitterManager twitterManager,
                      FilterFactory filterFactory,
                      HistoryCache historyCache,
                      @Assisted DiscordChannel channel,
                      @Assisted DiscordUser author) {
        super(threadManager, channel, TimeUnit.MINUTES.toMillis(Constants.Dorj.DURATION_MINUTES));
        this.filterFactory = filterFactory;
        this.twitterManager = twitterManager;
        this.historyCache = historyCache;
        this.summoners.add(author);

        this.dorjFormats = new LinkedList<>(Constants.Dorj.SUMMONING_FORMATS);
        Collections.shuffle(dorjFormats);
    }

    @Override
    public void handleStart() {
        String vid = MiscUtils.getRandomItemInList(Constants.Dorj.STARTING_VIDEOS);
        channel.sendMessage("D.O.R.J. system starting ... [ ONLINE ] " + vid);
    }

    public void addSummoner(DiscordUser user) {
        AddSummonerError error = validateAdd(user);
        if (error == null) {
            summoners.add(user);

            boolean adminIsInvolved = isAdminInvolved();

            String dorjPart = null;
            switch (summoners.size()) {
                case 2:
                    dorjPart = "Left";
                    break;
                case 3:
                    dorjPart = "Right";
                    break;
                case 4:
                    dorjPart = "Core";
                    break;
            }

            String format = dorjFormats.remove(0);
            StringBuilder builder = new StringBuilder(String.format(format, dorjPart));

            if (summoners.size() == 3) {
                builder.append(" [ WARNING! ] Dorj imminent!");
                if (!adminIsInvolved) {
                    builder.append(" Call in a TSD member to summon the final Dorj!");
                }
            } else if (summoners.size() == 4) {
                builder.append(" I can't believe it! It's happening!!!");
                synchronized (mutex) {
                    mutex.notify();
                }
            }

            channel.sendMessage(builder.toString());

        } else {
            switch (error) {
                case DUPLICATE_SUMMONER: {
                    channel.sendMessage("You can only assume one part of the Dorj, " + user.getName());
                    break;
                }
                case NEED_ADMIN: {
                    channel.sendMessage("At least one TSD member must be involved when summoning the Dorj");
                    break;
                }
            }
        }
    }

    private AddSummonerError validateAdd(DiscordUser summoner) {
        if (summoners.contains(summoner)) {
            return AddSummonerError.DUPLICATE_SUMMONER;
        }
        if ( summoners.size() == 3
                && (!isAdminInvolved())
                && (!summoner.hasRole(channel.getServer(), Constants.Role.TSD)) ) {
            return AddSummonerError.NEED_ADMIN;
        }
        return null;
    }

    private boolean isAdminInvolved() {
        return summoners.stream().anyMatch(s -> s.hasRole(channel.getServer(), Constants.Role.TSD));
    }

    @Override
    public void handleEnd() {
        if (summoners.size() == 4) {
            try {
                HistoryRequest<DiscordChannel> request = HistoryRequest.create(channel, null)
                        .withFilter(filterFactory.createLengthFilter(1, 140))
                        .withFilter(filterFactory.createNoFunctionsFilter())
                        .withFilter(filterFactory.createNoOwnMessagesFilter())
                        .withFilter(filterFactory.createNoBotsFilter())
                        .withFilter(filterFactory.createIgnorableFilter());
                DiscordMessage<DiscordChannel> randomMessage = historyCache.getRandomChannelMessage(request);
                if (randomMessage != null) {
                    Status tweet = sendDeejTweet(randomMessage.getContent());
                    channel.sendMessage("https://twitter.com/TSD_IRC/status/"+tweet.getId());
                }
            } catch (Exception e) {
                log.error("Error sending Dorj tweet", e);
                channel.sendMessage("Failed to sent the Dorj due to error :(");
            }
        } else {
            String vid = MiscUtils.getRandomItemInList(Constants.Dorj.FAILURE_VIDEOS);
            channel.sendMessage("(the air goes quiet and the ground still as the Dorj returns to slumber) " + vid);
        }
    }

    private Status sendDeejTweet(String text) throws TwitterException {
        return twitterManager.postTweet(Constants.Dorj.DEEJ_HANDLE + " " + text);
    }

    enum AddSummonerError {
        DUPLICATE_SUMMONER,
        NEED_ADMIN
    }

}
