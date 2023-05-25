package org.tsd.tsdbot.filename;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.util.MiscUtils;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.tsd.Constants.Filenames.MAX_RANDOM_FILENAME_HISTORY;
import static org.tsd.Constants.Filenames.VALID_FILE_TYPES;

@Singleton
public class S3FilenameLibrary implements FilenameLibrary {

    private static final Logger log = LoggerFactory.getLogger(S3FilenameLibrary.class);

    private final AmazonS3 s3Client;
    private final HttpClient httpClient;
    private final String filenamesBucket;
    private final String randomFilenamesBucket;

    private LinkedHashMap<String, Filename> randomFilenames = new LinkedHashMap<>();

    @Inject
    public S3FilenameLibrary(AmazonS3 s3Client,
                             HttpClient httpClient,
                             @Named(Constants.Annotations.S3_FILENAMES_BUCKET) String filenamesBucket,
                             @Named(Constants.Annotations.S3_RANDOM_FILENAMES_BUCKET) String randomFilenamesBucket) {
        this.s3Client = s3Client;
        this.httpClient = httpClient;
        this.filenamesBucket = filenamesBucket;
        this.randomFilenamesBucket = randomFilenamesBucket;
    }

    @Override
    public Filename getRandomRealFilename() throws IOException {
        ListObjectsV2Result result = s3Client.listObjectsV2(filenamesBucket);
        S3ObjectSummary random = MiscUtils.getRandomItemInList(result.getObjectSummaries());
        if (random != null) {
            return getFilename(random.getKey());
        }
        return null;
    }

    @Override
    public Filename getFilename(String name) throws IOException {
        S3Object object = s3Client.getObject(filenamesBucket, name);
        byte[] data = IOUtils.toByteArray(object.getObjectContent());
        return new Filename(data, object.getKey());
    }

    @Override
    public List<String> listAllFilenames() throws IOException {
        ListObjectsV2Result result = s3Client.listObjectsV2(filenamesBucket);
        return result.getObjectSummaries().stream()
                .sorted(Comparator.comparing(S3ObjectSummary::getKey))
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Filename createRandomFilename() throws IOException {
        ListObjectsV2Result allPossibleFiles = s3Client.listObjectsV2(randomFilenamesBucket);
        S3ObjectSummary imageToUse = MiscUtils.getRandomItemInList(allPossibleFiles.getObjectSummaries());
        if (imageToUse != null) {
            S3Object image = s3Client.getObject(randomFilenamesBucket, imageToUse.getKey());
            String extension = StringUtils.substringAfterLast(image.getKey(), ".");
            byte[] data = IOUtils.toByteArray(image.getObjectContent());
            String filename = pickRandomFilenameString() + "." + extension;
            Filename newRandomFilename = new Filename(data, filename);

            if (randomFilenames.size() >= MAX_RANDOM_FILENAME_HISTORY) {
                String toRemove = randomFilenames.keySet().iterator().next();
                randomFilenames.remove(toRemove);
            }

            randomFilenames.put(newRandomFilename.getName(), newRandomFilename);
            return newRandomFilename;
        }
        return null;
    }

    @Override
    public void addFileToRandomFilenameBucket(String urlString) throws FilenameValidationException {
        String extension = parseExtensionFromName(urlString);

        if (extension == null || !ArrayUtils.contains(VALID_FILE_TYPES, extension)) {
            throw new FilenameValidationException("Invalid file type");
        }

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new FilenameValidationException("Invalid image URL");
        }

        byte[] imageData;
        try {
            HttpGet get = new HttpGet(url.toURI());
            HttpResponse response = httpClient.execute(get);
            imageData = IOUtils.toByteArray(response.getEntity().getContent());
        } catch (Exception e) {
            log.error("Error fetching new image for random filename database: " + urlString, e);
            throw new FilenameValidationException("Error retrieving image");
        }

        if (ArrayUtils.isNotEmpty(imageData)) {
            String randomFilename = RandomStringUtils.randomAlphabetic(20)+"."+extension;
            s3Client.putObject(randomFilenamesBucket, randomFilename, new ByteArrayInputStream(imageData), new ObjectMetadata());
        } else {
            throw new FilenameValidationException("Failed to download image");
        }
    }

    private String parseExtensionFromName(String name) {
        if(name.contains(".")) {
            String[] parts = name.split("\\.");
            return parts[parts.length-1];
        } else {
            return null;
        }
    }

    @Override
    public Filename getRandomFilename(String name) {
        return randomFilenames.get(name);
    }
}
