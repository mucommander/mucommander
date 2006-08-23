package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;

/**
 * FileFilter that only accepts regular files that are not directories.
 *
 * @author Maxence Bernard
 */
public class DirectoryFileFilter extends FileFilter {

    public DirectoryFileFilter() {
    }

    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean accept(AbstractFile file) {
        return !file.isDirectory();
    }

}
