package org.tsd.tsdbot.meme;

import java.io.IOException;
import java.util.List;

public interface MemeRepository {
    boolean doesMemeExist(String id);
    List<String> searchMemes(String partialId);
    byte[] getMeme(String id) throws IOException;
    String storeMeme(String memeUrl) throws IOException;
    String saveMeme(String id) throws IOException, MemeAlreadySavedException, MemeNotFoundException;
    String getRandomSavedMeme() throws IOException;
}
