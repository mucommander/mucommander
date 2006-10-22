package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractArchiveFile;

/**
 * FileFilter that accepts only archive files.
 *
 * @author Maxence Bernard
 */
public class ArchiveFileKeeper extends FileFilter {

    public ArchiveFileKeeper() {
    }

    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean accept(AbstractFile file) {
        return file instanceof AbstractArchiveFile;
    }

}
