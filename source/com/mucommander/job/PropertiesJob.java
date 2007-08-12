/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;


/**
 * This FileJob calculates the number of files contained in a list of file and folders and
 * computes their size.
 *
 * @author Maxence Bernard
 */
public class PropertiesJob extends FileJob {
    
    /** Number of folders encountered so far */
    private int nbFolders;

    /** Number of regular files (not folders) encountered so far */
    private int nbFilesRecurse;
	
    /** Combined size of all files encountered so far */
    private long totalBytes;
	
	
    public PropertiesJob(FileSet files, MainFrame mainFrame) {
        super(mainFrame, files);
        setAutoUnmark(false);
    }

    /**
     * Returns the size in bytes of all the files seen so far.
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * Returns the number of folders counted so far.
     */
    public int getNbFolders() {
        return nbFolders;
    }
 
    /**
     * Returns the number of files (folders excluded) counted so far.
     */
    public int getNbFilesRecurse() {
        return nbFilesRecurse;
    }
 

    ////////////////////////////
    // FileJob implementation //
    ////////////////////////////

    /**
     * Adds the given file to the total of files or folders and the total size,
     * and recurses if it is a folder.
     */
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted
        if(getState()==INTERRUPTED)
            return false;

        // If file is a directory, increase folder counter and recurse
        if (file.isDirectory() && !file.isSymlink()) {
            nbFolders++;

            try {
                AbstractFile subFiles[] = file.ls();
                for(int i=0; i<subFiles.length && getState()!=INTERRUPTED; i++) {
                    // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                    nextFile(subFiles[i]);
                    processFile(subFiles[i], null);
                }
            }
            catch(IOException e) {
                // Should we tell the user?
            }
        }
        // If not, increase file counter and bytes total
        else {
            nbFilesRecurse++;
            long fileSize = file.getSize();
            if(fileSize>0)		// Can be equal to -1 if size not available
                totalBytes += fileSize;
        }
	
        return true;
    }


    /**
     * Not used.
     */
    public String getStatusString() {
        return null;
    }

    // This job does not modify anything
	
    protected boolean hasFolderChanged(AbstractFile folder) {
        return false;
    }
}
