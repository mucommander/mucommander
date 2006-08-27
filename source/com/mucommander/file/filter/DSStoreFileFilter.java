package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;

/**
 * Filter used to filter out Mac OS X .DS_Store files.
 *
 * @author Maxence Bernard
 */
public class DSStoreFileFilter extends FileFilter {

    public boolean accept(AbstractFile file) {
        return !".DS_Store".equals(file.getName());
    }
}
