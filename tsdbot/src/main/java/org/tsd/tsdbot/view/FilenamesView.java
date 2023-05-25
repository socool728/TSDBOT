package org.tsd.tsdbot.view;

import org.tsd.Constants;
import org.tsd.tsdbot.filename.FilenameLibrary;

import java.io.IOException;
import java.util.List;

public class FilenamesView extends TSDHQView {

    private final FilenameLibrary filenameLibrary;

    public FilenamesView(FilenameLibrary library) {
        super(Constants.View.FILENAMES_VIEW);
        this.filenameLibrary = library;
    }

    public List<String> getAllFilenames() throws IOException {
        return filenameLibrary.listAllFilenames();
    }
}
