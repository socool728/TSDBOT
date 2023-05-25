package org.tsd.tsdbot.tsdtv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.tsd.rest.v1.tsdtv.schedule.Schedule;

@RunWith(MockitoJUnitRunner.class)
public class UT_Schedule {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testReadSample() throws Exception {
        Schedule schedule
                = objectMapper.readValue(getClass().getResourceAsStream("/tsdtvSchedule.json"), Schedule.class);
    }
}
