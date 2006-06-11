
package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;


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