
package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;
import java.util.Vector;

public abstract class FilenameFilter extends FileFilter {

    public FilenameFilter() {
    }


    public String[] filter(String filenames[]) {
        Vector filteredFilenamesV = new Vector();
        int nbFilenames = filenames.length;
        String filename;
        for(int i=0; i<nbFilenames; i++) {
            filename = filenames[i];
            if(accept(filename))
                filteredFilenamesV.add(filename);
        }

        String filteredFilenames[] = new String[filteredFilenamesV.size()];
        filteredFilenamesV.toArray(filteredFilenames);
        return filteredFilenames;
    }
    

    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean accept(AbstractFile file) {
        return accept(file.getName());
    }
    
    //////////////////////
    // Abstract methods //
    //////////////////////
    
    public abstract boolean accept(String filename);
}