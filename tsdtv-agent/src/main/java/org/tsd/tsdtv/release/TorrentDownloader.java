package org.tsd.tsdtv.release;

import bt.Bt;
import bt.StandaloneClientBuilder;
import bt.data.Storage;
import bt.dht.DHTModule;
import bt.runtime.BtClient;
import com.google.common.io.BaseEncoding;
import com.google.inject.Inject;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdtv.module.TorrentDirectory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TorrentDownloader {

    private static final Logger log = LoggerFactory.getLogger(TorrentDownloader.class);

    private static final String MAGNET_URI_REGEX = "^magnet:\\?xt=urn:btih:(\\w+).*?";
    private static final Pattern MAGNET_URI_PATTERN = Pattern.compile(MAGNET_URI_REGEX, Pattern.DOTALL);

    private static final long DOWNLOAD_UPDATE_PERIOD = TimeUnit.SECONDS.toMillis(5);

    private final DHTModule dhtModule;
    private final Storage storage;
    private final File torrentDirectory;

    @Inject
    public TorrentDownloader(DHTModule dhtModule, Storage storage, @TorrentDirectory File torrentDirectory) {
        this.dhtModule = dhtModule;
        this.storage = storage;
        this.torrentDirectory = torrentDirectory;
    }

    public File downloadMagnet(String magnetUri) throws Exception {
        String downloadId = RandomStringUtils.randomAlphabetic(10);
        log.info("Initiating magnet download, id={}, uri={}", downloadId, magnetUri);

        String infoHash = parseInfoHash(magnetUri);

        if (infoHash != null && StringUtils.length(infoHash) == 32) {
            // this torrent library does not support Base32 hashes, convert to hex
            log.debug("Converting Base32 info-hash to hex: {}", infoHash);
            String hex = BaseEncoding.base16().encode(BaseEncoding.base32().decode(infoHash));
            log.debug("Base32 \"{}\" -> Base16 \"{}\"", infoHash, hex);
            magnetUri = StringUtils.replace(magnetUri, infoHash, hex);
            log.debug("New magnet URI: {}", magnetUri);
        }

        return downloadFile(magnetUri, true, downloadId);
    }

    public File downloadTorrent(String torrentUrlString) throws Exception {
        String downloadId = RandomStringUtils.randomAlphabetic(10);
        log.info("Initiating torrent download, id={}, url={}", downloadId, torrentUrlString);
        return downloadFile(torrentUrlString, false, downloadId);
    }

    private File downloadFile(String uri, boolean magnet, String downloadId) throws MalformedURLException {
        AtomicReference<File> downloadedFile = new AtomicReference<>();

        StandaloneClientBuilder clientBuilder = Bt.client();

        if (magnet) {
            clientBuilder.magnet(uri);
        } else {
            clientBuilder.torrent(new URL(uri));
        }

        BtClient client = clientBuilder
                .storage(storage)
                .autoLoadModules()
                .module(this.dhtModule)
                .stopWhenDownloaded()
                .afterTorrentFetched(torrent -> {
                    String fileName = torrent.getFiles().get(0).getPathElements().get(0);
                    log.info("Using filename for downloadId: {}", fileName);
                    downloadedFile.set(new File(torrentDirectory, fileName));
                })
                .build();

        client.startAsync(state -> {
            log.info("Downloading file, id={}, downloaded={}, piecesRemaining: {}",
                    downloadId, state.getDownloaded(), state.getPiecesRemaining());
            if (state.getPiecesRemaining() == 0) {
                log.warn("Stopping download {}...", downloadId);
                client.stop();
            }
        }, DOWNLOAD_UPDATE_PERIOD).join();

        log.info("Finished downloading: {}", downloadedFile.get());
        return downloadedFile.get();
    }

    private String parseInfoHash(String magnetUri) {
        Matcher m = MAGNET_URI_PATTERN.matcher(magnetUri);
        if (m.find()) {
            String hash = m.group(1);
            log.debug("Parsed info-hash \"{}\" from magnet URI \"{}\"", hash, magnetUri);
            return hash;
        }
        log.debug("Failed to parse info-hash from magnet URI: {}", magnetUri);
        return null;
    }
}
