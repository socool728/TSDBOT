package org.tsd.tsdtv;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FT_NetworkMonitor {

    @Test
    public void test() {
        NetworkMonitor networkMonitor = new NetworkMonitor();
        networkMonitor.run();
    }
}
