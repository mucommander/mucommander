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
 * FilenameFilter is a {@link FileFilter} that operates on filenames. It can be used to filter out filenames without
 * having to create {@link AbstractFile} instances.
 *
 * <p>FilenameFilter implements the {@link #accept(AbstractFile)} using its filename, so it can be used anywhere
 * a <code>FileFilter</code> is required.
 *
 * <p>A <code>FilenameFilter</code> can be passed to {@link AbstractFile#ls(FilenameFilter)}, in order to filter out
 * files contained by a folder without creating the associated <code>AbstractFile</code> instances.
 */
public abstract class FilenameFilter extends FileFilter {

    public FilenameFilter() {
    }


    /**
     * Convenience method that filters out filenames that do not satisfy the {@link #accept(AbstractFile)} method and
     * returns an array of accepted <code>AbstractFile</code> instances.
     *
     * @param filenames files to be test against {@link #accept(AbstractFile)}
     */
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
    

    /**
     * Convenience method that returns <code>true</code> if all the filenames containted in the specified array were
     * accepted by {@link #accept(String)}, <code>false</code> if one of the filenames was not accepted.
     *
     * @param filenames the filenames to test against this FilenameFilter
     */
    public boolean accept(String filenames[]) {
        int nbFiles = filenames.length;
        for(int i=0; i<nbFiles; i++)
            if(!accept(filenames[i]))
                return false;

        return true;
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    /**
     * Implements FileFilter by calling {@link #accept(String)} with the filename of the given file, as returned by
     * {@link AbstractFile#getName()}, and returning its value.
     *
     * @param file the file's name to test
     * @return true if the file was accepted, false if it was rejected
     */
    public boolean accept(AbstractFile file) {
        return accept(file.getName());
    }


    //////////////////////
    // Abstract methods //
    //////////////////////
    
    /**
     * Returns <code>true</code> if the given filename was accepted by this FilenameFilter, false it was rejected.
     *
     * @param filename the filename to test
     * @return <code>true</code> if the given filename was accepted by this FilenameFilter, false it was rejected
     */
    public abstract boolean accept(String filename);
}