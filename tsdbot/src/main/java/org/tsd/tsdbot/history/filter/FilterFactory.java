package org.tsd.tsdbot.history.filter;

import com.google.inject.assistedinject.Assisted;

public interface FilterFactory {
    LengthFilter createLengthFilter(@Assisted(value = "min") Integer min, @Assisted(value = "max") Integer max);
    NoFunctionsFilter createNoFunctionsFilter();
    NoOwnMessagesFilter createNoOwnMessagesFilter();
    NoUrlsFilter createNoUrlsFilter();
    NoBotsFilter createNoBotsFilter();
    IgnorableFilter createIgnorableFilter();
}
