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
 * <code>FilenameFilter</code> is a {@link FileFilter} that operates on filenames. It can be used to match filenames
 * without having to deal with {@link AbstractFile} instances.
 *
 * <p><code>FilenameFilter</code> implements {@link #accept(AbstractFile)} by delegating it to {@link #accept(String)}.
 * By extending <code>FileFilter</code>, this class can be used everywhere a <code>FileFilter</code> is accepted.</p>
 *
 * <p>Several convenience methods are provided to operate this filter on a set of filenames, and filter out filenames
 * that do not match this filter.</p>
 *
 * <p>A <code>FilenameFilter</code> can be passed to {@link AbstractFile#ls(FilenameFilter)} to filter out some of the
 * files contained by a folder without creating the associated <code>AbstractFile</code> instances.</p>
 */
public abstract class FilenameFilter extends FileFilter {

    /** True if this FilenameFilter is case-sensitive. */
    protected boolean caseSensitive;

    /**
     * Creates a new <code>FilenameFilter</code> that operates in normal, non-inverted mode, and that is case-insensitive.
     */
    public FilenameFilter() {
        this(false, false);
    }

    /**
     * Creates a new <code>FilenameFilter</code> that operates in normal, non-inverted mode.
     *
     * @param caseSensitive if true, this FilenameFilter will be case-sentive
     */
    public FilenameFilter(boolean caseSensitive) {
        this(caseSensitive, false);
    }

    /**
     * Creates a new <code>FilenameFilter</code> that operates in the specified mode.
     *
     * @param caseSensitive if true, this FilenameFilter will be case-sentive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public FilenameFilter(boolean caseSensitive, boolean inverted) {
        super(inverted);
        setCaseSensitive(caseSensitive);
    }


    /**
     * Returns <code>true</code> if this <code>FilenameFilter</code> is case-sensitive.
     *
     * @return true if this <code>FilenameFilter</code> is case-sensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Specifies whether this <code>FilenameFilter</code> should be case-sensitive or not when comparing filenames.
     *
     * @param caseSensitive if true, this FilenameFilter will be case-sentive
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }


    /**
     * Returns <code>true</code> if this filter matched the given filename, according to the current {@link #isInverted()}
     * mode:
     * <ul>
     *  <li>if this filter currently operates in normal (non-inverted) mode, this method will return the value of {@link #accept(String)}</li>
     *  <li>if this filter currently operates in inverted mode, this method will return the value of {@link #reject(String)}</li>
     * </ul>
     *
     * @param filename the filename to test
     * @return true if this filter matched the given filename, according to the current inverted mode
     */
    public boolean match(String filename) {
        if(inverted)
            return reject(filename);

        return accept(filename);
    }

    /**
     * Returns <code>true</code> if the given filename was rejected by this filter, <code>false</code> if it was accepted.
     * This method is implemented by negating the value returned by {@link #accept(String)}.
     *
     * <p>The {@link #isInverted() inverted} mode has no effect on the values returned by this method.</p>
     *
     * @param filename the filename to be tested
     * @return true if the given filefilename was rejected by this filter
     */
    public boolean reject(String filename) {
        return !accept(filename);
    }



    /**
     * Convenience method that filters out files that do not {@link #match(AbstractFile) match} this filter and
     * returns a file array of matched <code>AbstractFile</code> instances.
     *
     * @param filenames filenames to be tested
     * @return an array of accepted AbstractFile instances
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
     * Convenience method that returns <code>true</code> if all the filenames in the specified array were matched by
     * {@link #match(String)}, <code>false</code> if one of the filenames wasn't.
     *
     * @param filenames the filenames to be tested
     * @return true if all the filenames in the specified array were accepted
     */
    public boolean match(String filenames[]) {
        int nbFiles = filenames.length;
        for(int i=0; i<nbFiles; i++)
            if(!match(filenames[i]))
                return false;

        return true;
    }

    /**
     * Convenience method that returns <code>true</code> if all the filenames in the specified array were accepted by
     * {@link #accept(String)}, <code>false</code> if one of the filenames wasn't.
     *
     * @param filenames the filenames to be tested
     * @return true if all the filenames in the specified array were accepted
     */
    public boolean accept(String filenames[]) {
        int nbFiles = filenames.length;
        for(int i=0; i<nbFiles; i++)
            if(!accept(filenames[i]))
                return false;

        return true;
    }

    /**
     * Convenience method that returns <code>true</code> if all the filenames in the specified array were rejected by
     * {@link #reject(String)}, <code>false</code> if one of the filenames wasn't.
     *
     * @param filenames the filenames to be tested
     * @return true if all the filenames in the specified array were rejected
     */
    public boolean reject(String filenames[]) {
        int nbFiles = filenames.length;
        for(int i=0; i<nbFiles; i++)
            if(!reject(filenames[i]))
                return false;

        return true;
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    /**
     * Implements FileFilter by calling {@link #accept(String)} with the filename of the given file (as returned by
     * {@link AbstractFile#getName()}) and returning its value.
     *
     * @param file the file to be tested
     * @return true if the file was accepted
     */
    public boolean accept(AbstractFile file) {
        return accept(file.getName());
    }


    //////////////////////
    // Abstract methods //
    //////////////////////
    
    /**
     * Returns <code>true</code> if the given filename was accepted by this filter, <code>false</code> if it was rejected.
     *
     * @param filename the filename to test
     * @return true if the given filename was accepted by this filter, false if it was rejected
     */
    public abstract boolean accept(String filename);
}