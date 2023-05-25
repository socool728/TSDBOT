package org.tsd.tsdtv.module;

import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdtv.TSDTVAgentConfiguration;
import org.tsd.tsdtv.release.model.ReleaseRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;

public class TorrentModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(TorrentModule.class);

    private final TSDTVAgentConfiguration configuration;

    public TorrentModule(TSDTVAgentConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        DHTModule dhtModule = new DHTModule(new DHTConfig() {
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });

        bind(DHTModule.class).toInstance(dhtModule);

        try {
            /*
            Release parent directory
             */
            File releasesDirectory = new File(configuration.getReleasesDirectory());
            if (releasesDirectory.mkdir()) {
                log.info("Created release directory: {}", releasesDirectory);
            } else {
                log.info("Using existing release directory: {}", releasesDirectory);
            }

            if (!releasesDirectory.canRead() || !releasesDirectory.canWrite()) {
                log.error("Invalid read/write permissions to release directory: {}", releasesDirectory);
                throw new RuntimeException("Invalid read/write permissions to release directory: "+releasesDirectory.getCanonicalPath());
            }

            /*
            Torrent file directory
             */
            File torrentDirectory = new File(releasesDirectory, "torrents");

            if (torrentDirectory.mkdir()) {
                log.info("Created torrents directory: {}", torrentDirectory);
            } else {
                log.info("Using existing torrents directory: {}", torrentDirectory);
            }
            bind(File.class)
                    .annotatedWith(TorrentDirectory.class)
                    .toInstance(torrentDirectory);

            /*
            Release repository JSON file
             */
            File releaseRepositoryFile = new File(releasesDirectory, "releaseRepository.json");
            if (releaseRepositoryFile.createNewFile()) {
                log.info("Created release repository JSON file: {}", releaseRepositoryFile);
                ReleaseRepository releaseRepository = new ReleaseRepository();
                releaseRepository.setReleaseGroups(new LinkedList<>());
                new ObjectMapper().writeValue(new FileOutputStream(releaseRepositoryFile), releaseRepository);
            } else {
                log.info("Using existing release repository JSON file: {}", releaseRepositoryFile);
            }
            bind(File.class)
                    .annotatedWith(ReleasesFile.class)
                    .toInstance(releaseRepositoryFile);

            /*
            BitTorrent storage
             */
            Storage storage = new FileSystemStorage(torrentDirectory.toPath());
            bind(Storage.class).toInstance(storage);

        } catch (Exception e) {
            log.error("Failed to create temp directory for torrents", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
