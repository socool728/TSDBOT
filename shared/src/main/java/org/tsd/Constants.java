package org.tsd;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Constants {

    public static final String URL_REGEX = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    public static final String USER_BEEP_REGEX = "<@([\\d]+)>";

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static class Annotations {
        public static final String SELF = "self";
        public static final String OWNER = "owner";
        public static final String ENCRYPTION_KEY = "encryptionKey";
        public static final String SERVICE_AUTH_PASSWORD = "serviceAuthPassword";
        public static final String NEWS_API_KEY = "newsApiKey";

        public static final String BITLY_USER = "bitlyUser";
        public static final String BITLY_API_KEY = "bitlyApiKey";

        public static final String S3_TSDBOT_CONFIG_BUCKET = "tsdbotConfigBucket";
        public static final String S3_FILENAMES_BUCKET = "filenamesBucket";
        public static final String S3_MEMES_BUCKET = "memesBucket";
        public static final String S3_RANDOM_FILENAMES_BUCKET = "randomFilenamesBucket";
        public static final String S3_TSDTV_BUCKET = "tsdtvBucket";
        public static final String S3_TSDTV_IMAGES_BUCKET = "tsdtvImagesBucket";
        public static final String S3_TSDTV_COMMERCIALS_BUCKET = "tsdtvCommercialsBucket";

        public static final String MASHAPE_API_KEY = "mashapeApiKey";
        public static final String GOOGLE_GIS_CX = "gisCx";
        public static final String GOOGLE_API_KEY = "googleApiKey";
        public static final String TSDTV_STREAM_URL = "tsdtvStreamUrl";
        public static final String TSDTV_CHANNEL = "tsdtvChannel";
        public static final String TSDTV_SCHEDULE = "tsdtvSchedule";
    }

    public static class Auth {
        public static final String BASIC_AUTH = "Authorization";
        public static final String SERVICE_AUTH_TOKEN_HEADER = "TSDHQ-Service-Auth-Token";
        public static final String SERVICE_AUTH_NAME_HEADER = "TSDHQ-Service-Auth-Name";
        public static final String BEARER = "Bearer";
        public static final String USER_REALM = "TSDHQ_User";
        public static final String SERVICE_REALM = "TSDHQ_Service";
        public static final String TOKEN_KEY = "tsdhq_token";
    }

    public static class History {
        public static final long HISTORY_FETCH_TIMEOUT_SECONDS = 10;
        public static final int DEFAULT_HISTORY_LENGTH = 500;
    }

    public static class Choose {
        public static final String PREFIX = ".choose";
        public static final List<String> CHOICE_DELIMITERS = Arrays.asList(",", " or ", "|");

        public static final List<String> OUTPUT_FORMATS = Arrays.asList(
                "Go with %s",
                "%s",
                "%s. Do it now",
                "It's a tough one but I'm gonna go with %s",
                "I choose %s but you're going to do what you want anyway",
                "I choo-choo-choose %s",
                "Hmm. %s");
    }

    public static class Deej {
        public static final String PREFIX = ".deej";

        public static final List<String> DECORATORS = Arrays.asList(
                "Fear not, Guardians: %s",
                "But be wary, Guardians of our city: %s",
                "The scribes of our city, stewards of the lost knowledge from our Golden Age, have uncovered a " +
                        "mysterious tome whose pages are all empty but for one mysterious line: \"%s\"",
                "Rejoice, Guardians! %s",
                "The spirits and specters from our bygone era of prosperity remind us: %s",
                "A message appears written in the amber skies above Earth's last city, at once a harbinger of caution " +
                        "and hope for all whose light shines bright against the darkness: \"%s\"");
    }

    public static class Dorj {
        public static final String COMMAND = ".dorj";

        public static final int DURATION_MINUTES = 3;
        public static final String DEEJ_HANDLE = "@DeeJ_BNG";

        public static final List<String> SUMMONING_FORMATS = Arrays.asList(
                "(lights go dim as the %s Dorj hums to life)",
                "%s Dorj ONLINE...",
                "Bringing %s Dorj online ... [ OK ]",
                "Initiating %s Dorj subsystems... [ ONLINE ]",
                "%s Dorj primed and ready!");

        public static final List<String> STARTING_VIDEOS = Arrays.asList(
                "https://www.youtube.com/watch?v=7OlcJzin3LE",  // big o - sure promise
                "https://www.youtube.com/watch?v=52BcC9Qzl-Q",  // big o - stand a chance
                "https://www.youtube.com/watch?v=MvsHNN0inzs"); // big o - stoning

        public static final List<String> FAILURE_VIDEOS = Arrays.asList(
                "https://www.youtube.com/watch?v=o5brIeXY1U0",  // big o - evolution
                "https://www.youtube.com/watch?v=2VPwmFPp9Kk",  // big o - sleep my dear
                "https://www.youtube.com/watch?v=CilZSMpxboQ",  // big o - apologize
                "https://www.youtube.com/watch?v=XH2KdaPpeZg"); // big o - name of god (choral)
    }

    public static class Emoji {
        public static final String EMOJI_URL_FORMAT = "https://cdn.discordapp.com/emojis/%s.png";

        public static final String STANDARD_EMOJI_REGEX = ":[\\w]+?:";

        public static final String CUSTOM_EMOJI_MENTION_REGEX = "<:(\\w+):(\\d+)>";
        public static final Pattern CUSTOM_EMOJI_MENTION_PATTERN = Pattern.compile(CUSTOM_EMOJI_MENTION_REGEX);
    }

    public static class Filenames {
        public static final String COMMAND_STRING = "^\\.(filename|fname).*?";
        public static final Pattern COMMAND_PATTERN = Pattern.compile(COMMAND_STRING);

        public static final int MAX_RANDOM_FILENAME_HISTORY = 30;
        public static final String[] VALID_FILE_TYPES = {"jpg", "jpeg", "bmp", "webm", "png", "gif", "gifv"};
    }

    public static class GV {
        public static final String PREFIX = ".gv";

        public static final List<String> RESPONSES = Arrays.asList(
                "I don't think you understand, but whatever",
                "Why?",
                "I don't think that's correct",
                "Why would you think that?",
                "I'm pretty sure I heard about that, but maybe not",
                "That doesn't really sound like something I'd like",
                "I'm not interested, sorry",
                "I don't really care, sorry",
                "Where did you see that?",
                "Where did you read that?",
                "Where did you hear that?",
                "I'm pretty sure science says no, but you can disagree I guess, maybe",
                "Why do you care?",
                "Why would you care?",
                "That's something I've always found interesting",
                "That's something I never found interesting, sorry",
                "I don't understand. Why?",
                "This isn't really something I want to discuss, sorry",
                "This isn't something I want to discuss, sorry",
                "I thought we agreed that was wrong?",
                "Why would you bring this up now?",
                "That's not very interesting",
                "Why am I supposed to care about that?",
                "I'd ask you to elaborate but I don't really care, sorry",
                "I'd ask you to elaborate but I'm not interested, sorry",
                "I know I'm late but I read this and I don't care",
                "I'm trying to imagine how this could be lamer and I'm having trouble, maybe if someone's mom was involved?",
                "Oh wow, let me get my heart medication",
                "Yawn");
    }

    public static class Hustle {
        public static final String COMMAND = ".hustle";
    }

    public static class Meme {
        public static final long MAX_AGE_DAYS = 30;
        public static final String HALL_OF_FAME_PREFIX = "HOF_";
    }

    public static class Morning {
        public static final String PREFIX = ".morning";
    }

    public static class News {
        public static final String PREFIX = ".news";
    }

    public static class OmniDatabase {
        public static final String COMMAND_PREFIX = ".odb";
    }

    public static class Printout {
        public static final String GIS_API_TARGET = "https://www.googleapis.com/customsearch/v1";

        public static final String QUERY_REGEX = "^TSDBot.*?printout of (.*)";
        public static final Pattern QUERY_PATTERN = Pattern.compile(QUERY_REGEX, Pattern.DOTALL);
        public static final String ACCEPTABLE_FORMATS = ".*?(JPG|jpg|PNG|png|JPEG|jpeg)$";
        public static final String OUTPUT_FILE_TYPE = "jpg";
    }

    public static class Role {
        public static final String TSD = "tsd";
        public static final String NOT_AUTHORIZED_MESSAGE = "You don't have permission to do that";
    }

    public static class Scheduler {
        public static final TimeZone TSDTV_DEFAULT_TIME_ZONE = TimeZone.getTimeZone("America/New_York");
        public static final String TSDTV_GROUP_ID = "tsdtv";
        public static final String TSDTV_BLOCK_DATA_KEY = "blockInfo";
    }

    public static class TSDTV {
        public static final String COMMAND_PREFIX = ".tsdtv";

        public static final long AGENT_HEARTBEAT_PERIOD_MILLIS
                = TimeUnit.MINUTES.toMillis(5);

        public static final long INVENTORY_REFRESH_PERIOD_MINUTES = 30;

        // When calculating the start times of items in the queue, assume they won't start immediately
        // after the previous show
        public static final long SCHEDULING_FUDGE_FACTOR_SECONDS = 5;
        public static final long SCHEDULING_FUDGE_FACTOR_MILLIS = TimeUnit.SECONDS.toMillis(SCHEDULING_FUDGE_FACTOR_SECONDS);
    }

    public static class View {
        public static final String FILENAMES_VIEW = "filenames.ftl";
        public static final String HUSTLE_VIEW = "hustle.ftl";
        public static final String TSDTV_VIEW = "tsdtv.ftl";
        public static final String LOGIN_VIEW = "login.ftl";
        public static final String DASHBOARD_VIEW = "dashboard.ftl";
        public static final String TSDTV_AGENTS_VIEW = "tsdtvAgents.ftl";
        public static final String SPLASH_VIEW = "splash.ftl";
    }
}
