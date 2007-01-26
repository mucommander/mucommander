
package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;


/**
 * FileFilter that only accepts visible (non-hidden) files.
 *
 * @author Maxence Bernard
 */
public class HiddenFileFilter extends FileFilter {

    public HiddenFileFilter() {
    }
    
    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////
    
    public boolean accept(AbstractFile file) {
        return !file.isHidden();
    }
}