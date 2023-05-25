package org.tsd.tsdtv.release.horriblesubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.tsd.tsdtv.AgentInventory;
import org.tsd.tsdtv.TSDBotClient;
import org.tsd.tsdtv.module.ReleasesFile;
import org.tsd.tsdtv.release.ReleaseFetcher;
import org.tsd.tsdtv.release.ReleaseSource;
import org.tsd.tsdtv.release.TorrentDownloader;

import java.io.File;
import java.util.List;
import java.util.Map;

public class HorribleSubsReleaseFetcher extends ReleaseFetcher<HorribleSubsRelease> {

    private final HorribleSubsClient horribleSubsClient;

    @Inject
    public HorribleSubsReleaseFetcher(HorribleSubsClient horribleSubsClient,
                                      @ReleasesFile File releasesFile,
                                      ObjectMapper objectMapper,
                                      TorrentDownloader torrentDownloader,
                                      @Named("monitoringReleases") Map<ReleaseSource, List<String>> monitoringReleases,
                                      @Named("inventory") File tsdtvInventory,
                                      AgentInventory agentInventory,
                                      TSDBotClient tsdBotClient) {
        super(releasesFile,
                objectMapper,
                torrentDownloader,
                monitoringReleases.get(ReleaseSource.horrible_subs),
                tsdtvInventory,
                agentInventory,
                tsdBotClient);
        this.horribleSubsClient = horribleSubsClient;
    }

    @Override
    public List<HorribleSubsRelease> getReleases() throws Exception {
        return horribleSubsClient.getReleases();
    }
}
