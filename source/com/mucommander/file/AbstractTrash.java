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

package com.mucommander.file;

/**
 * AbstractTrash is an abstract representation of a file trash, i.e. a temporary place where deleted files are stored
 * before the trash is emptied. A trash implementation provides methods to access basic trash operations: move files
 * to the trash, empty the trash, ...
 *
 * <p>Since AbstractTrash implementations are system-dependent, they should not be instanciated directly.
 * Use {@link com.mucommander.file.FileFactory#getTrash()} to retrieve an instance of a trash implementation that can
 * be used on the current platform.<br>
 * Also, some AbstractTrash subclasses may not be able to provide working implementations for all trash operations;
 * some methods are provided to find out which operations are available.</p>
 *
 * @see com.mucommander.file.FileFactory#getTrash()
 * @author Maxence Bernard
 */
public abstract class AbstractTrash {

    /**
     * Returns <code>true</code> if the specified file is eligible for being moved to the trash. This doesn't mean that
     * a call to {@link #moveToTrash(AbstractFile)} will necessarily succeed, but it should at least ensure that basic
     * prerequisites are met. 
     *
     * @param file the file to test
     * @return true if the given file can be moved to the trash
     */
    public abstract boolean canMoveToTrash(AbstractFile file);

    /**
     * Attempts to move the given file to the trash and returns <code>true</code> if the file could be moved successfully.
     *
     * @param file the file to move to the trash
     * @return true if the file could successfully be moved to the trash
     */
    public abstract boolean moveToTrash(AbstractFile file);

    /**
     * Returns <code>true</code> if this trash can be emptied.
     *
     * @return true if the trash can be emptied.
     */
    public abstract boolean canEmptyTrash();

    /**
     * Attempts to empty this trash and returns <code>true</code> if it was successfully emptied.
     *
     * @return true if the trash was successfully emptied
     */
    public abstract boolean emptyTrash();

    /**
     * Returns <code>true</code> if the given file is a trash folder, or one of its children.
     * For example, if <code>/home/someuser/.Trash</code> is a trash folder, calling this method with:
     * <ul>
     *  <li><code>/home/someuser/.Trash</code> will return <code>true</code>
     *  <li><code>/home/someuser/.Trash/somefolder/somefile</code> will return <code>true</code>
     *  <li><code>/home/someuser/Desktop</code> will return <code>false</code>
     * </ul>
     *
     * <p>Note that this method does not check the existence of the given file, the test is solely based on the
     * file's path.
     *
     * @param file the file to test
     * @return true if the given file is a trash folder, or one of its children.
     */
    public abstract boolean isTrashFile(AbstractFile file);

    /**
     * Returns the number of items that currently are in this trash, <code>-1</code> if this information is not available.
     *
     * @return the number of items that currently are in this trash, <code>-1</code> if this information is not available
     */
    public abstract int getTrashItemCount();

    /**
     * Reveals this trash in the default file manager of the current OS/Desktop manager.
     */
    public abstract void revealTrash();

    /**
     * Returns <code>true</code> if this trash can be revealed in the default file manager of the current OS/Desktop manager
     * by calling {@link #revealTrash()}.
     *
     * @return true if this trash can be revealed in the default file manager of the current OS/Desktop manager.
     */
    public abstract boolean canRevealTrash();

    /**
     * Waits (locks the caller thread) until all pending trash operations are completed.
     * This method can be useful since some <code>AbstractTrash</code> implementations are asynchroneous, i.e. perform
     * operations in separate threads.   
     */
    public abstract void waitForPendingOperations();
}
