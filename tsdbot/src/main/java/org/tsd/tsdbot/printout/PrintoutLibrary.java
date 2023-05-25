package org.tsd.tsdbot.printout;

import com.google.common.collect.EvictingQueue;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.imgscalr.Scalr;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordUser;
import org.tsd.tsdbot.util.MiscUtils;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.*;

import static org.tsd.Constants.Printout.GIS_API_TARGET;

@Singleton
public class PrintoutLibrary {

    private static final Logger log = LoggerFactory.getLogger(PrintoutLibrary.class);

    private final String gisCx;
    private final String apiKey;
    private final HttpClient httpClient;

    private final EvictingQueue<String> printoutIds = EvictingQueue.create(50);
    private final Map<String, byte[]> printouts = new HashMap<>();

    private final Set<DiscordUser> notComputing = new HashSet<>();

    @Inject
    public PrintoutLibrary(HttpClient httpClient,
                           @Named(Constants.Annotations.GOOGLE_GIS_CX) String gisCx,
                           @Named(Constants.Annotations.GOOGLE_API_KEY) String apiKey) {
        this.httpClient = httpClient;
        this.gisCx = gisCx;
        this.apiKey = apiKey;
    }

    public String generatePrintout(String query) throws Exception {
        BufferedImage image = searchAndDownload(query);
        if (image != null) {
            try {
                BufferedImage overlayedImage = transformImage(image);
                if (overlayedImage != null) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        String id = String.format("%s.%s",
                                RandomStringUtils.randomAlphabetic(15), Constants.Printout.OUTPUT_FILE_TYPE);
                        ImageIO.write(overlayedImage, Constants.Printout.OUTPUT_FILE_TYPE, baos);
                        addPrintoutToLibrary(id, baos.toByteArray());
                        return id;
                    }
                } else {
                    throw new Exception("Could not generate image for an unknown reason");
                }
            } catch (Exception e) {
                log.error("Error manipulating image(s)", e);
            }
        }
        return null;
    }

    private void addPrintoutToLibrary(String printoutId, byte[] data) {
        printoutIds.add(printoutId);
        printouts.put(printoutId, data);

        Set<String> validIds = new HashSet<>(printoutIds);
        for (String id : printouts.keySet()) {
            if (!validIds.contains(id)) {
                printouts.remove(id);
            }
        }
    }

    public byte[] getPrintout(String id) {
        return printouts.get(id);
    }

    public void addUserNotComputing(DiscordUser user) {
        notComputing.add(user);
    }

    public void removeUserNotComputing(DiscordUser user) {
        notComputing.remove(user);
    }

    public boolean isUserPendingComputing(DiscordUser user) {
        return notComputing.contains(user);
    }

    private BufferedImage searchAndDownload(String query) throws Exception {
        BufferedImage img = null;
        String response = search(query);
        JSONObject json = new JSONObject(response);
        LinkedList<String> urlResults = new LinkedList<>();
        JSONArray items = (JSONArray) json.get("items");
        JSONObject item;
        for(int i=0 ; i < items.length() ; i++) {
            item = items.getJSONObject(i);
            urlResults.add(item.getString("link"));
        }
        Collections.shuffle(urlResults);
        String url;
        while ( (url = urlResults.pollFirst()) != null && img == null ) {
            if (url.matches(Constants.Printout.ACCEPTABLE_FORMATS)) {
                try {
                    log.info("Trying to fetch image: {}", url);
                    img = ImageIO.read(new URL(url));
                } catch (Exception e) {
                    log.warn("Could not retrieve external image, skipping...", e);
                }
            }
        }
        return img;
    }

    private String search(String query) throws Exception {
        URIBuilder builder = new URIBuilder(GIS_API_TARGET);
        builder.addParameter(   "searchType",   "image" );
        builder.addParameter(   "q",            query   );
        builder.addParameter(   "cx",           gisCx   );
        builder.addParameter(   "key",          apiKey  );
        URL url = new URL(builder.toString());
        HttpGet get = new HttpGet(url.toURI());
        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(get)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    private BufferedImage transformImage(BufferedImage source) throws Exception {
        BufferedImage bg = ImageIO.read(getClass().getResourceAsStream("/printout.png"));

        BufferedImage resizedImage = Scalr.resize(source, Scalr.Mode.FIT_EXACT, 645, 345);

        AffineTransform translate = AffineTransform.getTranslateInstance(200, 125);
        AffineTransformOp translateOp = new AffineTransformOp(translate , AffineTransformOp.TYPE_BILINEAR);
        resizedImage = translateOp.filter(resizedImage, null);

        AffineTransform rotateTransform = AffineTransform.getRotateInstance(-0.022);
        AffineTransformOp rotateTransformOp = new AffineTransformOp(rotateTransform , AffineTransformOp.TYPE_BICUBIC);
        resizedImage = rotateTransformOp.filter(resizedImage, null);

        return MiscUtils.overlayImages(bg, resizedImage);
    }
}
