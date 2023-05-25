package org.tsd.tsdbot.listener.channel;

import com.google.inject.Singleton;
import de.btobastian.javacord.DiscordAPI;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.odb.OdbItem;
import org.tsd.tsdbot.odb.OdbItemDao;
import org.tsd.tsdbot.odb.OmniDbException;
import org.tsd.tsdbot.util.OdbUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;

@Singleton
public class OmniDatabaseHandler extends MessageHandler<DiscordChannel> {

    private static final Logger log = LoggerFactory.getLogger(OmniDatabaseHandler.class);

    private static final int MAX_HISTORY = 100;
    private static final int MAX_HISTORY_TO_RETURN = 50;

    private final OdbItemDao odbItemDao;
    private final Map<DiscordChannel, CircularFifoQueue<OdbItem>> itemHistory = new HashMap<>();

    @Inject
    public OmniDatabaseHandler(DiscordAPI api, OdbItemDao odbItemDao) {
        super(api);
        this.odbItemDao = odbItemDao;
    }

    @Override
    public boolean isValid(DiscordMessage<DiscordChannel> message) {
        return startsWith(message.getContent(), Constants.OmniDatabase.COMMAND_PREFIX);
    }

    @Override
    public void doHandle(DiscordMessage<DiscordChannel> message, DiscordChannel channel) throws Exception {
        log.info("Handling odb: channel={}, message={}",
                channel.getName(), message.getContent());

        String input = substringAfter(message.getContent(), Constants.OmniDatabase.COMMAND_PREFIX).trim();
        String[] parts = input.split("\\s+");
        log.info("Parsed input: {} -> {}", input, Arrays.toString(parts));

        Mode mode = parseModeFromInput(input);
        log.info("ODB mode: {}", mode);

        try {
            switch (mode) {
                case add: {
                    handleAdd(message, parts);
                    break;
                }
                case mod: {
                    break;
                }
                case del: {
                    handleDelete(message, parts);
                    break;
                }
                case get_random: {
                    handleGetRandom(message);
                    break;
                }
                case get_search: {
                    handleGetSearch(message, parts);
                    break;
                }
                case runback: {
                    handleRunback(message, parts);
                    break;
                }
                default: {
                    channel.sendMessage("USAGE: .odb (add <#tag1> <#tag2> <item>) | ");
                }
            }
        } catch (OmniDbException e) {
            channel.sendMessage("Error: " + e.getMessage());
        }
    }

    private void handleRunback(DiscordMessage<DiscordChannel> message, String[] parts) {
        DiscordChannel channel = message.getRecipient();
        log.info("Handling .odb runback, channel={}, user={}, parts={}",
                channel.getName(), message.getAuthor().getName(), Arrays.toString(parts));

        int itemsToReturn = 1;

        if (parts.length > 1) {
            try {
                itemsToReturn = Integer.parseInt(parts[1]);
                log.info("Parsed itemsToReturn: {}", itemsToReturn);
                if (itemsToReturn < 1) {
                    throw new IllegalArgumentException();
                }
                itemsToReturn = Math.min(itemsToReturn, MAX_HISTORY_TO_RETURN);
            } catch (Exception e) {
                message.getRecipient().sendMessage("Item count must be an integer greater than zero");
                return;
            }
        }

        CircularFifoQueue<OdbItem> history = itemHistory.get(channel);

        if (CollectionUtils.isEmpty(history)) {
            message.getRecipient().sendMessage("No ODB search history for this channel");
            return;
        }

        itemsToReturn = Math.min(history.size(), itemsToReturn);
        log.info("Returning {} items from ODB history", itemsToReturn);

        if (itemsToReturn == 1) {
            OdbItem item = history.get(history.size()-1);
            channel.sendMessage("ODB: "+buildFullItem(item, true));
        } else {
            StringBuilder response = new StringBuilder("Here are the last ")
                    .append(itemsToReturn).append(" ODB pulls for ").append(channel.getName()).append(":");
            OdbItem item;
            for (int i = history.size()-1 ; i >= (history.size()-itemsToReturn) ; i--) {
                item = history.get(i);
                response.append("\n").append(buildFullItem(item, true));
            }
            channel.sendMessage("I'm PMing you the last "+itemsToReturn+" ODB pulls, "+message.getAuthor().getName());
            message.getAuthor().sendMessage(response.toString());
        }
    }

    private void handleAdd(DiscordMessage<DiscordChannel> message, String[] parts) throws OmniDbException {
        log.info("Handling .odb add: parts = {}", Arrays.toString(parts));
        List<String> tags = new LinkedList<>();
        StringBuilder itemData = new StringBuilder();

        for (int i=1 ; i < parts.length ; i++) {
            String word = parts[i];
            if (word.startsWith("#") && word.length() > 1) {
                log.info("Detected tag word: {}", word);
                tags.add(StringUtils.substring(word, 1));
            } else if (!word.startsWith("#")) {
                log.info("Detected non-tag word: {}", word);
                while (i < parts.length) {
                    itemData.append(word).append(" ");
                    i++;
                    if (i < parts.length) {
                        word = parts[i];
                        log.info("Next word: {}", word);
                    }
                }
            }
        }

        if (StringUtils.isBlank(itemData) && CollectionUtils.isNotEmpty(message.getAttachments())) {
            log.info("Item data is blank, adding attachment...");
            itemData.append(message.getAttachments().get(0).toString());
        }

        log.info("Result: tags = {}, item = \"{}\"", tags, itemData);

        if (CollectionUtils.isEmpty(tags) || StringUtils.isBlank(itemData.toString())) {
            message.getRecipient().sendMessage("USAGE: .odb add #tag1 #tag2 <item>");
        } else {
            String result = odbItemDao.addItem(itemData.toString(), tags.toArray(new String[tags.size()]));
            message.getRecipient().sendMessage("Item added to Omni Database, id = " + result);
        }
    }

    private void handleDelete(DiscordMessage<DiscordChannel> message, String[] parts) throws OmniDbException {
        log.info("Handling .odb del: parts = {}", Arrays.toString(parts));
        if (parts.length <= 1) {
            message.getRecipient().sendMessage("USAGE: .odb del <ItemID>");
        } else {
            if (!message.authorHasRole(Constants.Role.TSD)) {
                message.getRecipient().sendMessage(Constants.Role.NOT_AUTHORIZED_MESSAGE);
            } else {
                String itemId = parts[1];
                odbItemDao.deleteItem(itemId);
                message.getRecipient().sendMessage("Successfully deleted item "+itemId);
            }
        }
    }

    private void handleGetRandom(DiscordMessage<DiscordChannel> message) throws OmniDbException {
        log.info("Handling .odb get_random");
        OdbItem randomItem = odbItemDao.getRandomItem();
        message.getRecipient().sendMessage("ODB: "+buildFullItem(randomItem, false));
        addItemToHistory(randomItem, message.getRecipient());
    }

    private void handleGetSearch(DiscordMessage<DiscordChannel> message, String[] parts) throws OmniDbException {
        log.info("Handling .odb get_search: parts = {}", Arrays.toString(parts));

        List<String> tagsToSearch = Arrays.stream(parts)
                .filter(part -> !StringUtils.equalsIgnoreCase("get", part))
                .map(OdbUtils::sanitizeTag)
                .collect(Collectors.toList());
        log.info("Searching for tags: {}", tagsToSearch);

        if (CollectionUtils.isEmpty(tagsToSearch)) {
            handleGetRandom(message);
        } else {
            OdbItem item = odbItemDao.searchForItem(tagsToSearch);
            if (item == null) {
                message.getRecipient().sendMessage("Found no items in the Omni Database matching those tags");
            } else {
                message.getRecipient().sendMessage("ODB: " + item.getItem());
                addItemToHistory(item, message.getRecipient());
            }
        }
    }

    private void addItemToHistory(OdbItem item, DiscordChannel channel) {
        itemHistory.putIfAbsent(channel, new CircularFifoQueue<>(MAX_HISTORY));
        itemHistory.get(channel).add(item);
    }

    private static String buildFullItem(OdbItem item, boolean includeId) {
        StringBuilder builder = new StringBuilder(item.getItem());
        for (String tag : item.getTags()) {
            builder.append(" #").append(tag);
        }
        if (includeId) {
            builder.append(" (id = ").append(item.getId()).append(")");
        }
        return builder.toString().trim();
    }

    private static Mode parseModeFromInput(String input) {
        if (StringUtils.isBlank(input)) {
            return Mode.get_random;
        }
        String[] words = input.split("\\s+");
        switch (words[0]) {
            case "add":     return Mode.add;
            case "mod":     return Mode.mod;
            case "del":     return Mode.del;
            case "runback": return Mode.runback;
            case "get":
            default:        return Mode.get_search;
        }
    }

    enum Mode {
        add,
        get_search,
        get_random,
        mod,
        del,
        runback
    }

}
