package org.tsd.tsdbot.history.filter;

import com.google.inject.Inject;
import org.tsd.tsdbot.history.HistoryRequest;

import java.util.Arrays;
import java.util.List;

public class StandardMessageFilters {

    private final List<MessageHistoryFilter> standardFilters;

    @Inject
    public StandardMessageFilters(FilterFactory filterFactory) {
        this.standardFilters = Arrays.asList(
                filterFactory.createNoFunctionsFilter(),
                filterFactory.createNoOwnMessagesFilter(),
                filterFactory.createNoUrlsFilter(),
                filterFactory.createNoBotsFilter(),
                filterFactory.createIgnorableFilter());
    }

    public List<MessageHistoryFilter> getStandardFilters() {
        return standardFilters;
    }

    public HistoryRequest apply(HistoryRequest historyRequest) {
        return historyRequest.withFilters(getStandardFilters());
    }
}
