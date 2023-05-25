package org.tsd.tsdtv.release.horriblesubs;

import com.rometools.rome.feed.synd.SyndEntry;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tsd.tsdtv.release.Release;
import org.tsd.tsdtv.release.ReleaseSource;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HorribleSubsRelease extends Release {

    private static final String TITLE_REGEX = "^\\[HorribleSubs] (.*?) - ([\\d]+) \\[\\w+]\\.[\\w]+";
    private static final Pattern TITLE_PATTERN = Pattern.compile(TITLE_REGEX, Pattern.DOTALL);

    private final String title;
    private final Date publishDate;

    public HorribleSubsRelease(SyndEntry entry) {
        super(entry.getUri(), entry.getLink(), null);
        this.title = entry.getTitle();
        this.publishDate = entry.getPublishedDate();
    }

    @Override
    public String getSeriesName() {
        Matcher matcher = TITLE_PATTERN.matcher(title);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    @Override
    public String getEpisodeName() {
        Matcher matcher = TITLE_PATTERN.matcher(title);
        while (matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }

    @Override
    public int getEpisodeNumber() {
        return Integer.parseInt(getEpisodeName());
    }

    @Override
    public ReleaseSource getReleaseSource() {
        return ReleaseSource.horrible_subs;
    }

    public String getGuid() {
        return guid;
    }

    public String getTitle() {
        return title;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("title", title)
                .append("publishDate", publishDate)
                .append("guid", guid)
                .append("magnetUri", magnetUri)
                .append("torrentUri", torrentUri)
                .toString();
    }
}
