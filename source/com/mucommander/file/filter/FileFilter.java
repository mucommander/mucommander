/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;

import java.util.Vector;


/**
 * FileFilter allows to filter out files that do not match certain criteria. The {@link #accept(AbstractFile)}
 * method must be implemented by subclasses in order to accept or reject a given AbstractFile.
 *
 * <p>The {@link #accept(AbstractFile[])} method can be used to accept or reject a set of files
 * which must all satisfy the {@link #accept(com.mucommander.file.AbstractFile)}.
 * The {@link #filter(AbstractFile[])} method allows to filter out the files that do not satisfy the
 * {@link #accept(AbstractFile)} method.
 *
 * <p>A <code>FileFilter</code> can be passed to {@link AbstractFile#ls(FileFilter)}, in order to filter out files
 * contained by a folder.
 *
 * @see com.mucommander.file.AbstractFile#ls(FileFilter)
 */
public abstract class FileFilter {

    /**
     * Creates a new FileFilter.
     */
    public FileFilter() {
    }

    
    /**
     * Convenience method that filters out files that do not satisfy the {@link #accept(AbstractFile)} method and
     * returns an array of accepted <code>AbstractFile</code> instances.
     *
     * @param files files to be test against {@link #accept(com.mucommander.file.AbstractFile)}
     */
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
     * Convenience method that returns <code>true</code> if all the files containted in the specified <code>AbstractFile</code>
     * array were accepted by {@link #accept(AbstractFile)}, <code>false</code> if one of the files was not accepted.
     *
     * @param files the files to test against this FileFilter
     */
    public boolean accept(AbstractFile files[]) {
        int nbFiles = files.length;
        for(int i=0; i<nbFiles; i++)
            if(!accept(files[i]))
                return false;

        return true;
    }

    
    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns <code>true</code> if the given AbstractFile was accepted by this FileFilter, false it was rejected.
     *
     * @param file the file to test
     * @return <code>true</code> if the given AbstractFile was accepted by this FileFilter, false it was rejected
     */
    public abstract boolean accept(AbstractFile file);
}