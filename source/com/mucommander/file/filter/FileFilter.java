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


package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;

import java.util.Vector;


/**
 * A <code>FileFilter</code> matches files that meet certain criteria. The {@link #accept(AbstractFile)}
 * method has to be implemented by subclasses in order to accept or reject a given <code>AbstractFile</code>.
 * By default, a <code>FileFilter</code> operates in non-inverted mode and {@link #match(AbstractFile)} returns the
 * value of {@link #accept(AbstractFile)}. When operating in inverted mode, {@link #match(AbstractFile)} returns the
 * value of {@link #reject(AbstractFile)}. It is important to understand that {@link #accept(AbstractFile)} and
 * {@link #reject(AbstractFile)} are not affected by the inverted order in which the filter operates.
 *
 * <p>Several convenience methods are provided to operate this filter on a set of files, and filter out files that
 * do not match this filter.</p>
 *
 * <p>A <code>FileFilter</code> instance can be passed to {@link AbstractFile#ls(FileFilter)} to filter out some of the
 * some of the files contained by a folder.</p>
 *
 * @see FilenameFilter
 * @see com.mucommander.file.AbstractFile#ls(FileFilter)
 */
public abstract class FileFilter {

    /** True if this filter should operate in inverted mode and invert matches */
    protected boolean inverted;

    
    /**
     * Creates a new <code>FileFilter</code> that operates in normal, non-inverted mode.
     */
    public FileFilter() {
    }

    /**
     * Creates a new <code>FileFilter</code> that operates in the specified mode.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public FileFilter(boolean inverted) {
        setInverted(inverted);
    }


    /**
     * Return <code>true</code> if this filter operates in normal mode, <code>false</code> if in inverted mode.
     *
     * @return true if this filter operates in normal mode, false if in inverted mode
     */
    public boolean isInverted() {
        return inverted;
    }

    /**
     * Sets the mode in which {@link #match(com.mucommander.file.AbstractFile)} operates. If <code>true</code>, this
     * filter will operate in inverted mode: files that would be accepted by {@link #match(com.mucommander.file.AbstractFile)}
     * in normal (non-inverted) mode will be rejected, and vice-versa.<br>
     * The inverted mode has no effect on the values returned by {@link #accept(com.mucommander.file.AbstractFile)} and
     * {@link #reject(com.mucommander.file.AbstractFile)}.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }


    /**
     * Returns <code>true</code> if this filter matched the given file, according to the current {@link #isInverted()}
     * mode:
     * <ul>
     *  <li>if this filter currently operates in normal (non-inverted) mode, this method will return the value of {@link #accept(com.mucommander.file.AbstractFile)}</li>
     *  <li>if this filter currently operates in inverted mode, this method will return the value of {@link #reject(com.mucommander.file.AbstractFile)}</li>
     * </ul>
     *
     * @param file the file to test
     * @return true if this filter matched the given file, according to the current inverted mode
     */
    public boolean match(AbstractFile file) {
        if(inverted)
            return reject(file);

        return accept(file);
    }

    /**
     * Returns <code>true</code> if the given file was rejected by this filter, <code>false</code> if it was accepted.
     * This method is implemented by negating the value returned by {@link #accept(com.mucommander.file.AbstractFile)}.
     *
     * <p>The {@link #isInverted() inverted} mode has no effect on the values returned by this method.</p>
     *
     * @param file the file to test
     * @return true if the given file was rejected by this FileFilter
     */
    public boolean reject(AbstractFile file) {
        return !accept(file);
    }


    /**
     * Convenience method that filters out files that do not {@link #match(AbstractFile) match} this filter and
     * returns a file array of matched <code>AbstractFile</code> instances.
     *
     * @param files files to be test against {@link #match(com.mucommander.file.AbstractFile)}
     * @return a file array of files that were matches by this filter
     */
    public AbstractFile[] filter(AbstractFile files[]) {
        Vector filteredFilesV = new Vector();
        int nbFiles = files.length;
        AbstractFile file;
        for(int i=0; i<nbFiles; i++) {
            file = files[i];
            if(match(file))
                filteredFilesV.add(file);
        }

        AbstractFile filteredFiles[] = new AbstractFile[filteredFilesV.size()];
        filteredFilesV.toArray(filteredFiles);
        return filteredFiles;
    }

    /**
     * Convenience method that returns <code>true</code> if all the files containted in the specified file
     * array were matched by {@link #match(AbstractFile)}, <code>false</code> if one of the files wasn't.
     *
     * @param files the files to test against this FileFilter
     * @return true if all the files containted in the specified file array were matched by this filter
     */
    public boolean match(AbstractFile files[]) {
        int nbFiles = files.length;
        for(int i=0; i<nbFiles; i++)
            if(!match(files[i]))
                return false;

        return true;
    }

    /**
     * Convenience method that returns <code>true</code> if all the files containted in the specified file
     * array were accepted by {@link #accept(AbstractFile)}, <code>false</code> if one of the files wasn't.
     *
     * @param files the files to test against this FileFilter
     * @return true if all the files containted in the specified file array were accepted by this filter
     */
    public boolean accept(AbstractFile files[]) {
        int nbFiles = files.length;
        for(int i=0; i<nbFiles; i++)
            if(!accept(files[i]))
                return false;

        return true;
    }

    /**
     * Convenience method that returns <code>true</code> if all the files containted in the specified file
     * array were rejected by {@link #reject(AbstractFile)}, <code>false</code> if one of the files wasn't.
     *
     * @param files the files to test against this FileFilter
     * @return true if all the files containted in the specified file array were rejected by this filter
     */
    public boolean reject(AbstractFile files[]) {
        int nbFiles = files.length;
        for(int i=0; i<nbFiles; i++)
            if(!reject(files[i]))
                return false;

        return true;
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns <code>true</code> if the given file was accepted by this filter, <code>false</code> if it was rejected.
     *
     * <p>The {@link #isInverted() inverted} mode has no effect on the values returned by this method.</p>
     *
     * @param file the file to test
     * @return true if the given file was accepted by this FileFilter
     */
    public abstract boolean accept(AbstractFile file);
}