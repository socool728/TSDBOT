package org.tsd.tsdbot.listener.channel;

import com.google.inject.Inject;
import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdbot.app.BotUrl;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.filename.Filename;
import org.tsd.tsdbot.filename.FilenameLibrary;
import org.tsd.tsdbot.history.HistoryCache;
import org.tsd.tsdbot.history.HistoryRequest;
import org.tsd.tsdbot.history.filter.FilterFactory;
import org.tsd.tsdbot.history.filter.StandardMessageFilters;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.meme.MemeAlreadySavedException;
import org.tsd.tsdbot.meme.MemeNotFoundException;
import org.tsd.tsdbot.meme.MemeRepository;
import org.tsd.tsdbot.meme.MemegenClient;
import org.tsd.tsdbot.util.MessageSanitizer;
import org.tsd.tsdbot.util.MiscUtils;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MemeHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(MemeHandler.class);

    private final HistoryCache historyCache;
    private final FilterFactory filterFactory;
    private final StandardMessageFilters standardMessageFilters;
    private final MemegenClient memegenClient;
    private final FilenameLibrary filenameLibrary;
    private final Random random;
    private final URL botUrl;
    private final MemeRepository memeRepository;
    private final MessageSanitizer messageSanitizer;

    @Inject
    public MemeHandler(DiscordAPI api,
                       Random random,
                       MemegenClient memegenClient,
                       HistoryCache historyCache,
                       FilterFactory filterFactory,
                       StandardMessageFilters standardMessageFilters,
                       FilenameLibrary filenameLibrary,
                       MemeRepository memeRepository,
                       MessageSanitizer messageSanitizer,
                       @BotUrl URL botUrl) {
        super(api);
        this.messageSanitizer = messageSanitizer;
        this.random = random;
        this.memegenClient = memegenClient;
        this.historyCache = historyCache;
        this.filterFactory = filterFactory;
        this.standardMessageFilters = standardMessageFilters;
        this.filenameLibrary = filenameLibrary;
        this.botUrl = botUrl;
        this.memeRepository = memeRepository;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return StringUtils.startsWithIgnoreCase(message.getContent(), ".maymay");
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel recipient) throws Exception {
        String[] cmdParts = message.getContent().split("\\s+");

        log.info("MemeHandler, \"{}\" -> {}", message.getContent(), ArrayUtils.toString(cmdParts));

        if (cmdParts.length == 1) {
            // use a random template provided by memegen
            List<String> templates = new LinkedList<>(memegenClient.getTemplates());
            String template = templates.get(random.nextInt(templates.size()));
            log.info("Creating meme from template: {}", template);

            MemeData memeData = new MemeData(recipient, message);
            String memeUrl = memegenClient.generateMemeUrlFromTemplate(template, memeData.getText1(), memeData.getText2());
            log.info("Using meme template URL: {}", memeUrl);

            String memeId = memeRepository.storeMeme(memeUrl);
            String tsdbotUrl = buildMemeUrl(memeId);

            recipient.sendMessage(memeData.getFlavorText() + ": " + tsdbotUrl + " id = "+memeId);

        } else if (cmdParts.length > 1 && StringUtils.equalsIgnoreCase(cmdParts[1], "tsd")) {
            // use an image from the random filenames directory
            Filename filename = filenameLibrary.createRandomFilename();
            String filenameUrl = new URIBuilder(botUrl.toURI())
                    .setPath("/randomFilenames/"+filename.getName())
                    .build().toString();
            log.info("Creating meme from TSD filename: {}", filenameUrl);

            MemeData memeData = new MemeData(recipient, message);
            String memeUrl = memegenClient.generateMemeUrlFromAltImage(filenameUrl, memeData.getText1(), memeData.getText2());
            log.info("Using TSD URL: {}", memeUrl);

            String memeId = memeRepository.storeMeme(memeUrl);
            String tsdbotUrl = buildMemeUrl(memeId);

            recipient.sendMessage(memeData.getFlavorText() + ": " + tsdbotUrl + " id = "+memeId);

        } else if (cmdParts.length > 1 && StringUtils.equalsIgnoreCase(cmdParts[1], "hof")) {
            // return a random meme from the HOF
            String randomMemeId = memeRepository.getRandomSavedMeme();
            if (StringUtils.isBlank(randomMemeId)) {
                recipient.sendMessage("No saved memes available");
            } else {
                String url = buildMemeUrl(randomMemeId);
                recipient.sendMessage(MiscUtils.getRandomItemInList(HOF_FLAVOR_TEXT)+": "+url);
            }

        } else if (cmdParts.length > 2 && StringUtils.equalsIgnoreCase(cmdParts[1], "save")) {
            // save a meme
            String id = cmdParts[2];
            try {
                String newKey = memeRepository.saveMeme(id);
                String newUrl = buildMemeUrl(newKey);
                recipient.sendMessage("Meme saved to HOF: "+newUrl);
            } catch (MemeAlreadySavedException mase) {
                recipient.sendMessage("Meme has already been saved: "+id);
            } catch (MemeNotFoundException mnfe) {
                recipient.sendMessage("Unable to save meme: "+mnfe.getMessage());
            }

        }  else {
            recipient.sendMessage("USAGE: .maymay [tsd] [hof] [save {meme_id}]");
        }
    }

    private String buildMemeUrl(String memeId) throws URISyntaxException {
        return new URIBuilder(botUrl.toURI())
                .setPath("/memes/"+memeId)
                .build().toString();
    }

    private static final List<String> FLAVOR_TEXT = Arrays.asList(
            "LEL",
            "lel",
            "lol",
            "rofl",
            "kek",
            "*KFLEKL*",
            "Hit em with the good stuff",
            "A meme for the ages",
            "They said it couldn't be done"
    );

    private static final List<String> HOF_FLAVOR_TEXT = Arrays.asList(
            "Only the finest",
            "From the archives",
            "The best"
    );

    private class MemeData {
        private final String flavorText;

        private String text1;
        private String text2;

        MemeData(DiscordChannel recipient, DiscordMessage<DiscordChannel> message) {
            HistoryRequest<DiscordChannel> request = HistoryRequest.create(recipient, message)
                    .withFilters(standardMessageFilters.getStandardFilters())
                    .withFilter(filterFactory.createLengthFilter(2, 150));

            while (StringUtils.isBlank(text1)) {
                this.text1 = messageSanitizer.sanitize(historyCache.getRandomChannelMessage(request).getContent());
            }

            while (StringUtils.isBlank(text2) || text2.equals(text1)) {
                this.text2 = messageSanitizer.sanitize(historyCache.getRandomChannelMessage(request).getContent());
            }

            this.flavorText = FLAVOR_TEXT.get(random.nextInt(FLAVOR_TEXT.size()));
        }

        public String getFlavorText() {
            return flavorText;
        }

        public String getText1() {
            return text1;
        }

        public String getText2() {
            return text2;
        }
    }
}
