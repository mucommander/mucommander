/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;

/**
 * A <code>FileFilter</code> matches files that meet certain criteria. It can operate in two opposite modes: inverted
 * and non-inverted. By default, a <code>FileFilter</code> operates in non-inverted mode where
 * {@link #match(AbstractFile)} returns the value of {@link #accept(AbstractFile)}. On the contrary, when operating in
 * inverted mode, {@link #match(AbstractFile)} returns the value of {@link #reject(AbstractFile)}. It is important to
 * understand that {@link #accept(AbstractFile)} and {@link #reject(AbstractFile)} are not affected by the inverted
 * mode in which a filter operates.
 *
 * <p>Several convenience methods are provided to operate this filter on a set of files, and filter out files that
 * do not match this filter.</p>
 *
 * <p>A <code>FileFilter</code> instance can be passed to {@link AbstractFile#ls(FileFilter)} to filter out some of the
 * the files contained by a folder.</p>
 *
 * @see AbstractFileFilter
 * @see FilenameFilter
 * @see com.mucommander.commons.file.AbstractFile#ls(FileFilter)
 * @author Maxence Bernard
 */
public interface FileFilter {

    /**
     * Return <code>true</code> if this filter operates in normal mode, <code>false</code> if in inverted mode.
     *
     * @return true if this filter operates in normal mode, false if in inverted mode
     */
    public boolean isInverted();

    /**
     * Sets the mode in which {@link #match(com.mucommander.commons.file.AbstractFile)} operates. If <code>true</code>, this
     * filter will operate in inverted mode: files that would be accepted by {@link #match(com.mucommander.commons.file.AbstractFile)}
     * in normal (non-inverted) mode will be rejected, and vice-versa.<br>
     * The inverted mode has no effect on the values returned by {@link #accept(com.mucommander.commons.file.AbstractFile)} and
     * {@link #reject(com.mucommander.commons.file.AbstractFile)}.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public void setInverted(boolean inverted);

    /**
     * Returns <code>true</code> if this filter matched the given file, according to the current {@link #isInverted()}
     * mode:
     * <ul>
     *  <li>if this filter currently operates in normal (non-inverted) mode, this method will return the value of {@link #accept(com.mucommander.commons.file.AbstractFile)}</li>
     *  <li>if this filter currently operates in inverted mode, this method will return the value of {@link #reject(com.mucommander.commons.file.AbstractFile)}</li>
     * </ul>
     *
     * @param file the file to test
     * @return true if this filter matched the given file, according to the current inverted mode
     */
    public boolean match(AbstractFile file);

    /**
     * Returns <code>true</code> if the given file was rejected by this filter, <code>false</code> if it was accepted.
     *
     * <p>The {@link #isInverted() inverted} mode has no effect on the values returned by this method.</p>
     *
     * @param file the file to test
     * @return true if the given file was rejected by this FileFilter
     */
    public boolean reject(AbstractFile file);

    /**
     * Convenience method that filters out files that do not {@link #match(AbstractFile) match} this filter and
     * returns a file array of matched <code>AbstractFile</code> instances.
     *
     * @param files files to be tested against {@link #match(com.mucommander.commons.file.AbstractFile)}
     * @return a file array of files that were matched by this filter
     */
    public AbstractFile[] filter(AbstractFile files[]);

    /**
     * Convenience method that filters out files that do not {@link #match(AbstractFile) match} this filter
     * and removes them from the given {@link FileSet}.
     *
     * @param files files to be tested against {@link #match(com.mucommander.commons.file.AbstractFile)}
     */
    public void filter(FileSet files);

    /**
     * Convenience method that returns <code>true</code> if all the files contained in the specified file array
     * were matched by {@link #match(AbstractFile)}, <code>false</code> if one of the files wasn't.
     *
     * @param files the files to test against this FileFilter
     * @return true if all the files contained in the specified file array were matched by this filter
     */
    public boolean match(AbstractFile files[]);

    /**
     * Convenience method that returns <code>true</code> if all the files contained in the specified {@link FileSet}
     * were matched by {@link #match(AbstractFile)}, <code>false</code> if one of the files wasn't.
     *
     * @param files the files to test against this FileFilter
     * @return true if all the files contained in the specified {@link FileSet} were matched by this filter
     */
    public boolean match(FileSet files);

    /**
     * Convenience method that returns <code>true</code> if all the files contained in the specified file array
     * were accepted by {@link #accept(AbstractFile)}, <code>false</code> if one of the files wasn't.
     *
     * @param files the files to test against this FileFilter
     * @return true if all the files contained in the specified file array were accepted by this filter
     */
    public boolean accept(AbstractFile files[]);

    /**
     * Convenience method that returns <code>true</code> if all the files contained in the specified {@link FileSet}
     * were accepted by {@link #accept(AbstractFile)}, <code>false</code> if one of the files wasn't.
     *
     * @param files the files to test against this FileFilter
     * @return true if all the files contained in the specified {@link FileSet} were accepted by this filter
     */
    public boolean accept(FileSet files);

    /**
     * Convenience method that returns <code>true</code> if all the files contained in the specified file array
     * were rejected by {@link #reject(AbstractFile)}, <code>false</code> if one of the files wasn't.
     *
     * @param files the files to test against this FileFilter
     * @return true if all the files contained in the specified file array were rejected by this filter
     */
    public boolean reject(AbstractFile files[]);

    /**
     * Convenience method that returns <code>true</code> if all the files contained in the specified {@link FileSet}
     * were rejected by {@link #reject(AbstractFile)}, <code>false</code> if one of the files wasn't.
     *
     * @param files the files to test against this FileFilter
     * @return true if all the files contained in the specified {@link FileSet} were rejected by this filter
     */
    public boolean reject(FileSet files);

    /**
     * Returns <code>true</code> if the given file was accepted by this filter, <code>false</code> if it was rejected.
     *
     * <p>The {@link #isInverted() inverted} mode has no effect on the values returned by this method.</p>
     *
     * @param file the file to test
     * @return true if the given file was accepted by this FileFilter
     */
    public boolean accept(AbstractFile file);
}
