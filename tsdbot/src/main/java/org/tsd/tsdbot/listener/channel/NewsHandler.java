package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.news.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static org.apache.commons.lang3.StringUtils.startsWith;

public class NewsHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(NewsHandler.class);

    private final NewsClient newsClient;

    @Inject
    public NewsHandler(DiscordAPI api,
                       NewsClient newsClient) {
        super(api);
        this.newsClient = newsClient;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return startsWith(message.getContent(), Constants.News.PREFIX);
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling news: channel={}, message={}",
                channel.getName(), message.getContent());

        String[] parts = message.getContent().split("\\s+");

        StringBuilder topicBuilder = new StringBuilder();
        Language language = null;
        SortBy sortBy = null;
        LocalDateTime fromDate = null;

        if (parts.length == 1) {
            channel.sendMessage("USAGE: .news <topic> " +
                    "[-language <2-letter language code>, default: english] " +
                    "[-fromDate <MM-dd-yyyy>, default: yesterday] " +
                    "[-sortBy <relevancy, popularity, publishedAt>, default: popularity]");
        } else {
            for (int i = 1; i < parts.length; i++) {
                String word = parts[i].toLowerCase();
                switch (word) {
                    case "-language": {
                        i++;
                        language = Language.fromString(parts[i]);
                        if (language == null) {
                            channel.sendMessage("Invalid language, supported: " + StringUtils.join(Language.values(), ", "));
                            return;
                        }
                        break;
                    }
                    case "-sortby": {
                        i++;
                        sortBy = SortBy.fromString(parts[i]);
                        if (sortBy == null) {
                            channel.sendMessage("Invalid sortBy, supported: " + StringUtils.join(SortBy.values(), ", "));
                            return;
                        }
                        break;
                    }
                    case "-fromdate": {
                        i++;
                        try {
                            fromDate = LocalDateTime.parse(parts[i], DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneId.of("America/Chicago")));
                        } catch (Exception e) {
                            channel.sendMessage("Invalid fromDate, supported format: MM-dd-yyyy (e.g. 10-20-1986)");
                            return;
                        }
                        break;
                    }
                    default: {
                        topicBuilder.append(word).append(" ");
                    }
                }
            }
        }

        String topic = topicBuilder.toString().trim();
        NewsQueryResult result = newsClient.queryForNews(topic, language, fromDate, sortBy);

        if (CollectionUtils.isEmpty(result.getArticles())) {
            channel.sendMessage("Found no news articles matching those criteria");
        } else {
            Random random = new Random();
            NewsArticle articleToPrint = result.getArticles().get(random.nextInt(result.getArticles().size()));
            String msg = String.format("(%s) \"%s\": <%s>",
                    topic,
                    articleToPrint.getTitle(),
                    articleToPrint.getUrl());
            channel.sendMessage(msg);
        }
    }
}
