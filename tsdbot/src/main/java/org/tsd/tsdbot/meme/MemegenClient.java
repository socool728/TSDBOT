package org.tsd.tsdbot.meme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MemegenClient implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(MemegenClient.class);

    private static final String[][] REPLACEMENT_MAP = {
            {"_",   "__"},
            {" ",   "_"},
            {"-",   "--"},
            {"\\?", "~q"},
            {"%",   "~p"},
            {"#",   "~h"},
            {"/",   "~s"},
            {"\"",  "''"}
    };

    private static final String SCHEME = HttpScheme.HTTPS.asString();
    private static final String HOST = "memegen.link";
    private static final String TEMPLATES_PATH = "api/templates";
    private static final String CUSTOM_IMAGE_PATH = "custom";

    private static final String IMG_PARAM_NAME = "alt";
    private static final String FONT_PARAM_NAME = "font";
    private static final String FONT_IMPACT = "impact";

    private static final long TEMPLATE_REFRESH_PERIOD = TimeUnit.HOURS.toMillis(1);

    private final Set<String> templateNames = new HashSet<>();
    private long templatesLastFetched = Long.MIN_VALUE;

    private final Clock clock;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Inject
    public MemegenClient(Clock clock, HttpClient httpClient) {
        this.clock = clock;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getTemplates() throws Exception {
        if (clock.millis() > templatesLastFetched+TEMPLATE_REFRESH_PERIOD) {
            URI uri = new URIBuilder()
                    .setScheme(SCHEME)
                    .setHost(HOST)
                    .setPath(TEMPLATES_PATH)
                    .build();

            HttpGet get = new HttpGet(uri);

            try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(get)) {
                String responseString = EntityUtils.toString(response.getEntity());
                Map<String, String> templates = objectMapper.readValue(responseString, Map.class);
                templateNames.clear();
                templateNames.addAll(
                        templates.values()
                        .stream()
                        .map(url -> StringUtils.substringAfterLast(url, "/"))
                        .collect(Collectors.toList()));
                templatesLastFetched = clock.millis();
            } catch (Exception e) {
                log.error("Error retrieving memegen templates", e);
            }
        }
        return templateNames;
    }

    public String generateMemeUrlFromTemplate(String templateName, String text1, String text2) throws URISyntaxException {
        try {
            String encoded1 = encodeString(text1);
            String encoded2 = encodeString(text2);

            log.info("Generating memegen URI, template={}\ntext1={}\nencoded1={}\ntext2={}\nencoded2={}",
                    templateName, text1, encoded1, text2, encoded2);

            URIBuilder uriBuilder = new URIBuilder()
                    .setScheme(SCHEME)
                    .setHost(HOST)
                    .setPath(templateName + "/" + encoded1 + "/" + encoded2 + ".jpg")
                    .setParameter(FONT_PARAM_NAME, FONT_IMPACT);

            return uriBuilder.build().toString();

        } catch (URISyntaxException e) {
            log.error("Failed to build memegen URI, template={}\ntext1={}\ntext2={}",
                    templateName, text1, text2);
            throw e;
        }
    }

    public String generateMemeUrlFromAltImage(String imageUrl, String text1, String text2) throws URISyntaxException {
        try {
            String encoded1 = encodeString(text1);
            String encoded2 = encodeString(text2);

            log.info("Generating memegen URI, imageUrl={}\ntext1={}\nencoded1={}\ntext2={}\nencoded2={}",
                    imageUrl, text1, encoded1, text2, encoded2);

            URIBuilder uriBuilder = new URIBuilder()
                    .setScheme(SCHEME)
                    .setHost(HOST)
                    .setPath(CUSTOM_IMAGE_PATH + "/" + encoded1 + "/" + encoded2 + ".jpg")
                    .setParameter(FONT_PARAM_NAME, FONT_IMPACT)
                    .setParameter(IMG_PARAM_NAME, imageUrl);

            return uriBuilder.build().toString();

        } catch (URISyntaxException e) {
            log.error("Failed to build memegen URI, imageUrl={}\ntext1={}\ntext2={}",
                    imageUrl, text1, text2);
            throw e;
        }
    }

    private static String encodeString(String input) {
        String original = input;
        for (String[] replacement : REPLACEMENT_MAP) {
            input = StringUtils.replaceAll(input, replacement[0], replacement[1]);
        }
        log.debug("Encoded string for memegen:\n{}\n{}", original, input);
        return input;
    }
}
