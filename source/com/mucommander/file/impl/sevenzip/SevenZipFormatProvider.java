package com.mucommander.file.impl.sevenzip;

import java.io.IOException;

import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveFormatProvider;
import com.mucommander.file.filter.ExtensionFilenameFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.impl.rar.RarArchiveFile;

/**
 * This class is the provider for the 'Rar' archive format implemented by {@link RarArchiveFile}.
 *
 * @see com.mucommander.file.impl.rar.RarArchiveFile
 * @author Arik Hadas
 */
public class SevenZipFormatProvider implements ArchiveFormatProvider {
	/** Static instance of the filename filter that matches archive filenames */
    private final static ExtensionFilenameFilter filenameFilter = new ExtensionFilenameFilter(new String[]
        {".7z"}
    );


    //////////////////////////////////////////
    // ArchiveFormatProvider implementation //
    //////////////////////////////////////////

    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipArchiveFile(file);
    }

    public FilenameFilter getFilenameFilter() {
        return filenameFilter;
    }
}