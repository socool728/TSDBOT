package org.tsd.tsdbot.listener.channel;

import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.app.BotUrl;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.filename.Filename;
import org.tsd.tsdbot.filename.FilenameLibrary;
import org.tsd.tsdbot.filename.FilenameValidationException;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.util.MiscUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilenameHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(FilenameHandler.class);

    private final FilenameLibrary filenameLibrary;
    private final URL botUrl;

    @Inject
    public FilenameHandler(DiscordAPI api,
                           FilenameLibrary filenameLibrary,
                           @BotUrl URL botUrl) {
        super(api);
        this.filenameLibrary = filenameLibrary;
        this.botUrl = botUrl;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return message.getContent().matches(Constants.Filenames.COMMAND_STRING);
    }


    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling filename: channel={}, message={}",
                channel.getName(), message.getContent());

        String[] words = message.getContent().toLowerCase().split("\\s+");

        if (words.length == 1) {
            Filename filename = filenameLibrary.getRandomRealFilename();
            channel.sendMessage(buildFilenameUrl(filename, false));
        } else if (words.length >= 2) {
            String subCmd = words[1];
            switch (subCmd) {
                case "rando": {
                    handleRando(message, ArrayUtils.subarray(words, 2, words.length));
                    break;
                }
                case "get":
                case "search":
                case "query": {
                    handleQuery(message, ArrayUtils.subarray(words, 2, words.length));
                    break;
                }
            }
        }
    }

    private void handleQuery(DiscordMessage<DiscordChannel> message, String[] arguments) throws IOException, URISyntaxException {
        if (arguments.length == 0) {
            message.getRecipient().sendMessage("USAGE: .filename get <query>");
            return;
        }

        List<String> matchingFilenames = filenameLibrary.listAllFilenames()
                .stream()
                .filter(filename -> Arrays.stream(arguments)
                        .allMatch(arg -> StringUtils.containsIgnoreCase(filename, arg)))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(matchingFilenames)) {
            message.getRecipient().sendMessage("Found no filenames matching that query");
        } else {
            String randomMatch = MiscUtils.getRandomItemInList(matchingFilenames);
            message.getRecipient().sendMessage(buildFilenameUrl(filenameLibrary.getFilename(randomMatch), false));
        }
    }

    private void handleRando(DiscordMessage<DiscordChannel> message, String[] arguments) throws IOException, URISyntaxException {
        if (arguments.length == 0) {
            Filename filename = filenameLibrary.createRandomFilename();
            message.getRecipient().sendMessage(buildFilenameUrl(filename, true));
        } else {
            switch (arguments[0]) {
                case "add": {
                    String url = null;

                    if (arguments.length  == 1 && CollectionUtils.isNotEmpty(message.getAttachments())) {
                        // check for attachments
                        url = message.getAttachments().get(0).toString();
                    } else if (arguments.length > 1) {
                        url = arguments[1];
                    }

                    if (url != null) {
                        try {
                            filenameLibrary.addFileToRandomFilenameBucket(url);
                            message.getRecipient().sendMessage("Image successfully added to rando repo");
                        } catch (FilenameValidationException e) {
                            message.getRecipient().sendMessage("Error: " + e.getMessage());
                        }
                    } else {
                        message.getRecipient().sendMessage("USAGE: " + ".fname rando add <image url>");
                    }
                }
            }
        }
    }

    private String buildFilenameUrl(Filename filename, boolean random) throws URISyntaxException {
        String path = random ? "/randomFilenames/" : "/filenames/";
        return new URIBuilder(botUrl.toURI())
                .setPath(path+filename.getName())
                .build().toString();
    }

}
