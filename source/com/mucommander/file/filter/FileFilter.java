
package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;

import java.util.Vector;


public abstract class FileFilter {

    public FileFilter() {
    }
    
    public AbstractFile[] filter(AbstractFile files[]) {
        Vector filteredFilesV = new Vector();
        int nbFiles = files.length;
        AbstractFile file;
        for(int i=0; i<nbFiles; i++) {
            file = files[i];
            if(accept(file))
                filteredFilesV.add(file);
        }

        AbstractFile filteredFiles[] = new AbstractFile[filteredFilesV.size()];
        filteredFilesV.toArray(filteredFiles);
        return filteredFiles;
    }
    
    //////////////////////
    // Abstract methods //
    //////////////////////
    
    public abstract boolean accept(AbstractFile file);
}