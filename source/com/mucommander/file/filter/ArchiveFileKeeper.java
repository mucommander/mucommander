package com.mucommander.file.filter;

import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;

/**
 * FileFilter that only accepts archive files.
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
