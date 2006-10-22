
package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;

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


    /**
     * Convenience method: returns <code>true</code> if all the files containted in the specified {@link com.mucommander.file.FileSet}
     * were accepted by {@link #accept(AbstractFile)}, <code>false</code> if one of the files was not accepted.
     *
     * @param fileSet the files to test against this filter
     */
    public boolean accept(FileSet fileSet) {
        int nbFiles = fileSet.size();
        for(int i=0; i<nbFiles; i++)
            if(!accept(fileSet.fileAt(i)))
                return false;

        return true;
    }

    //////////////////////
    // Abstract methods //
    //////////////////////
    
    public abstract boolean accept(AbstractFile file);
}