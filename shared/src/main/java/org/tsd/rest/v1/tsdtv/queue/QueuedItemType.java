package org.tsd.rest.v1.tsdtv.queue;

import org.tsd.rest.v1.tsdtv.Commercial;
import org.tsd.rest.v1.tsdtv.Episode;
import org.tsd.rest.v1.tsdtv.Media;
import org.tsd.rest.v1.tsdtv.Movie;

import java.util.Arrays;
import java.util.Optional;

public enum QueuedItemType {
    episode     (Episode.class),
    movie       (Movie.class),
    commercial  (Commercial.class);

    private final Class<? extends Media> clazz;

    QueuedItemType(Class<? extends Media> clazz) {
        this.clazz = clazz;
    }

    public static QueuedItemType fromClass(Class<? extends Media> clazz) {
        Optional<QueuedItemType> result = Arrays.stream(values())
                .filter(type -> type.clazz.equals(clazz))
                .findAny();
        if (!result.isPresent()) {
            throw new RuntimeException("Could not find QueuedItemType with class " + clazz);
        }
        return result.get();
    }
}
