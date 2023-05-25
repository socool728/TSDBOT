package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.news.NewsArticle;
import org.tsd.tsdbot.news.NewsClient;
import org.tsd.tsdbot.news.NewsQueryResult;
import org.tsd.tsdbot.news.NewsTopicDao;
import org.tsd.tsdbot.util.BitlyUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.tsd.Constants.Morning.PREFIX;

public class MorningHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(MorningHandler.class);

    private static final int NEWS_STORIES_PER_DIGEST = 3;
    private static final int MAX_DEFAULT_NEWS_TRIES = 100;

    private static final List<String> DEFAULT_TOPICS = Arrays.asList(
            "red pandas",
            "halo",
            "destiny 2"
    );

    private final NewsTopicDao newsTopicDao;
    private final NewsClient newsClient;
    private final BitlyUtil bitlyUtil;

    private final Map<String, Date> briefingsLastFetched = new HashMap<>();

    @Inject
    public MorningHandler(DiscordAPI api,
                          NewsClient newsClient,
                          NewsTopicDao newsTopicDao,
                          BitlyUtil bitlyUtil) {
        super(api);
        this.bitlyUtil = bitlyUtil;
        this.newsTopicDao = newsTopicDao;
        this.newsClient = newsClient;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return startsWith(message.getContent(), PREFIX);
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling morning: channel={}, message={}",
                channel.getName(), message.getContent());

        String[] parts = message.getContent().split("\\s+");

        if (parts.length == 1) {

            int hourOfDay = Calendar.getInstance(TimeZone.getTimeZone("America/New_York")).get(Calendar.HOUR_OF_DAY);
            if (hourOfDay < 5 || hourOfDay > 12) {
                channel.sendMessage("It's not morning. Try again between 5AM and 12PM Eastern.");
                return;
            }

            if (briefingsLastFetched.containsKey(message.getAuthor().getId())) {
                Date lastFetchedForUser = briefingsLastFetched.get(message.getAuthor().getId());
                if (System.currentTimeMillis() - lastFetchedForUser.getTime() < TimeUnit.HOURS.toMillis(12)) {
                    channel.sendMessage("You already got your briefing, "+message.getAuthor().getName());
                    return;
                }
            }

            String messageAuthor = message.getAuthor().getName();
            log.info("Getting morning briefing for user {}", messageAuthor);

            List<String> newsTopics = newsTopicDao.getTopicsForUser(message.getAuthor().getId());
            Collections.shuffle(newsTopics);
            log.info("Shuffled news topics for user {}: {}", messageAuthor, newsTopics);

            List<String> topicsToFetch;
            if (CollectionUtils.isNotEmpty(newsTopics)) {
                topicsToFetch = new LinkedList<>();
                int i = 0;
                while (topicsToFetch.size() < NEWS_STORIES_PER_DIGEST) {
                    if (i == newsTopics.size()) {
                        i = 0;
                    }
                    topicsToFetch.add(newsTopics.get(i));
                    i++;
                }
            } else {
                log.info("Using default topics for user {}", messageAuthor);
                topicsToFetch = DEFAULT_TOPICS;
            }

            log.info("News topics to fetch for user {}: {}", messageAuthor, topicsToFetch);

            StringBuilder response = new StringBuilder("Good morning, ").append(messageAuthor).append(". ");
            if (CollectionUtils.isEmpty(newsTopics)) {
                response.append("I don't have any subscribed topics for you, but I'll see what I can do.");
            } else {
                response.append("Here is your news briefing for this morning:");
            }

            List<NewsArticleWithTopic> articles = getArticlesForTopics(topicsToFetch);

            if (CollectionUtils.isEmpty(articles)) {
                channel.sendMessage("You're out of luck, "+messageAuthor+": I couldn't find any articles for you");
            } else {
                for (NewsArticleWithTopic article : articles) {
                    response.append("\n");
                    String msg = String.format("(%s) \"%s\": <%s>",
                            StringUtils.capitalize(article.topic),
                            article.article.getTitle(),
                            bitlyUtil.shortenUrl(article.article.getUrl()));
                    response.append(msg);
                }

                channel.sendMessage(response.toString());
            }

            briefingsLastFetched.put(message.getAuthor().getId(), new Date());

        } else if (parts.length > 1) {
            String subCmd = parts[1].toLowerCase();
            switch (subCmd) {
                case "list": {
                    List<String> topicsForUser = newsTopicDao.getTopicsForUser(message.getAuthor().getId());
                    if (CollectionUtils.isEmpty(topicsForUser)) {
                        channel.sendMessage("I do not have any news topics for you, "+message.getAuthor().getName());
                    } else {
                        String topics = topicsForUser.stream().collect(Collectors.joining("\n"));
                        message.getAuthor().sendMessage("Here are your subscribed topics:\n"+topics);
                        channel.sendMessage("I have PM'd you a list of your subscribed news topics, "+message.getAuthor().getName());
                    }
                    break;
                }

                case "add": {
                    if (parts.length < 3) {
                        channel.sendMessage("Usage: .morning add a topic I care about");
                    } else {
                        String[] topicParts = ArrayUtils.subarray(parts, 2, parts.length);
                        String topic = StringUtils.join(topicParts, " ");
                        if (StringUtils.isBlank(topic) || StringUtils.length(topic) > 200) {
                            channel.sendMessage("News topic must be between 1 and 200 characters");
                        } else {
                            newsTopicDao.addTopicForUser(message.getAuthor().getId(), topic.toLowerCase());
                            channel.sendMessage("Added news topic for "+message.getAuthor().getName()+": "+topic);
                        }
                    }
                    break;
                }

                case "del":
                case "delete":
                case "rm":
                case "remove": {
                    if (parts.length < 3) {
                        channel.sendMessage("Usage: .morning remove a topic I don't care about");
                    } else {
                        String[] topicParts = ArrayUtils.subarray(parts, 2, parts.length);
                        String topic = StringUtils.join(topicParts, " ");
                        newsTopicDao.removeTopicForUser(message.getAuthor().getId(), topic.toLowerCase());
                        channel.sendMessage("Removed news topic for "+message.getAuthor().getName()+": "+topic);
                    }
                    break;
                }

                default: {
                    channel.sendMessage("Unknown command");
                }
            }
        }
    }

    private List<NewsArticleWithTopic> getArticlesForTopics(List<String> topics) throws URISyntaxException, IOException {
        log.info("Getting news articles for topics: {}", topics);

        List<NewsArticleWithTopic> articlesToReturn = new LinkedList<>();
        Map<String, List<NewsArticle>> allArticles = new HashMap<>();

        for (String topic : topics) {
            log.info("Getting news articles for topic: {}", topic);
            NewsQueryResult result = newsClient.queryForNews(topic);
            allArticles.putIfAbsent(topic, new LinkedList<>());
            allArticles.get(topic).addAll(result.getArticles());
        }

        log.info("Initial news fetch completed, allArticles ({}) = {}", allArticles.size(), allArticles);

        while (articlesToReturn.size() < NEWS_STORIES_PER_DIGEST && !allArticles.isEmpty()) {
            Iterator<Map.Entry<String, List<NewsArticle>>> iterator = allArticles.entrySet().iterator();
            Map.Entry<String, List<NewsArticle>> entry;
            while (iterator.hasNext() && articlesToReturn.size() < NEWS_STORIES_PER_DIGEST) {
                entry = iterator.next();
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    log.info("News topic {} has no more articles, removing...", entry.getKey());
                    iterator.remove();
                } else {
                    NewsArticle article = entry.getValue().remove(0);
                    log.info("Returning news article for topic \"{}\": {}", entry.getKey(), article);
                    articlesToReturn.add(new NewsArticleWithTopic(entry.getKey(), article));
                }
            }
        }

        log.info("Initial news compilation completed, articlesToReturn ({}) = {}",
                articlesToReturn.size(), articlesToReturn);

        int tryCount = 0;

        while (articlesToReturn.size() < NEWS_STORIES_PER_DIGEST && ++tryCount < MAX_DEFAULT_NEWS_TRIES) {
            for (String defaultTopic : DEFAULT_TOPICS) {
                if (articlesToReturn.size() >= NEWS_STORIES_PER_DIGEST) {
                    break;
                }
                log.info("Fetching articles for default topic: {}", defaultTopic);
                allArticles.putIfAbsent(defaultTopic, new LinkedList<>());
                NewsQueryResult result = newsClient.queryForNews(defaultTopic);
                if (CollectionUtils.isNotEmpty(result.getArticles())) {
                    NewsArticle article = result.getArticles().get(0);
                    log.info("Returning news article for default topic \"{}\": {}", defaultTopic, article);
                    articlesToReturn.add(new NewsArticleWithTopic(defaultTopic, article));
                } else {
                    log.warn("Found no news articles for default topic: {}", defaultTopic);
                }
            }
        }

        log.info("Compiled news articles: {}", articlesToReturn);
        return articlesToReturn;
    }

    private static class NewsArticleWithTopic {
        private final String topic;
        private final NewsArticle article;

        public NewsArticleWithTopic(String topic, NewsArticle article) {
            this.topic = topic;
            this.article = article;
        }

        public String getTopic() {
            return topic;
        }

        public NewsArticle getArticle() {
            return article;
        }
    }
}
