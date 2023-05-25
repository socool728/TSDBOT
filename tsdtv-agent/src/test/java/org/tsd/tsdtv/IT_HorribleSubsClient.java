package org.tsd.tsdtv;

import com.rometools.rome.io.FeedException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.tsd.tsdtv.release.horriblesubs.HorribleSubsClient;
import org.tsd.tsdtv.release.horriblesubs.HorribleSubsRelease;

import java.io.IOException;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class IT_HorribleSubsClient {

    private CloseableHttpClient httpClient;

    private HorribleSubsClient client;

    @Before
    public void setup() {
        httpClient = HttpClients.createMinimal();
        client = new HorribleSubsClient(httpClient);
    }

    @Test
    public void testFetch() throws IOException, FeedException {
        List<HorribleSubsRelease> releaseList = client.getReleases();
        int i=0;
    }

    @After
    public void cleanup() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
