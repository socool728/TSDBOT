package org.tsd.tsdbot.util;

import org.apache.commons.collections4.CollectionUtils;
import org.tsd.rest.v1.tsdtv.Episode;
import org.tsd.rest.v1.tsdtv.Series;

import java.util.List;
import java.util.stream.Collectors;

public class TSDTVUtils {

    public static List<Episode> getEffectiveEpisodes(Series series) {
        if (CollectionUtils.isNotEmpty(series.getSeasons())) {
            List<Episode> allEpisodes = series.getSeasons()
                    .stream().flatMap(season -> season.getEpisodes().stream())
                    .collect(Collectors.toList());

            for (int i=0 ; i < allEpisodes.size() ; i++) {
                allEpisodes.get(0).setOverriddenEpisodeNumber(i+1);
            }

            return allEpisodes;
        } else {
            return series.getEpisodes();
        }
    }
}
