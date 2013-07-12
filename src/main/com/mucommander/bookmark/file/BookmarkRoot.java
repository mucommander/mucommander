/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.bookmark.file;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.commons.file.*;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the root of the <code>bookmarks://</code> file system.
 * @author Nicolas Rinaudo
 */
class BookmarkRoot extends ProtocolFile implements BookmarkListener {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Time at which the bookmarks were last modified. */
    private long lastModified;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    public BookmarkRoot() throws IOException {this(FileURL.getFileURL(BookmarkProtocolProvider.BOOKMARK + "://"));}
    public BookmarkRoot(FileURL url) {
        super(url);
        lastModified = System.currentTimeMillis();
        BookmarkManager.addBookmarkListener(this);
    }



    // - AbstractFile methods --------------------------------------------------
    // -------------------------------------------------------------------------
    @Override
    public AbstractFile[] ls() throws IOException {
        AbstractFile[] files;
        Object[]       buffer;

        // Retrieves all available bookmarks.
        buffer = BookmarkManager.getBookmarks().toArray();
        files  = new AbstractFile[buffer.length];

        // Creates the associated instances of BookmarkFile
        for(int i = 0; i < files.length; i++)
            files[i] = new BookmarkFile((Bookmark)buffer[i]);
        return files;
    }

    @Override
    public String getName() {return "";}

    @Override
    public boolean isDirectory() {return true;}



    // - Bookmarks synchronisation ---------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Stores the current date as the date of last modification.
     */
    public void bookmarksChanged() {lastModified = System.currentTimeMillis();}

    /**
     * Returns the date at which the bookmark list was last modified.
     * @return the date at which the bookmark list was last modified.
     */
    @Override
    public long getDate() {return lastModified;}



    // - Unused methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    // The following methods are not used by BookmarkFile. They will throw an exception,
    // return an 'operation non supported' value or return a default value.

    @Override
    public AbstractFile getParent() {return null;}
    @Override
    @UnsupportedFileOperation
    public void delete() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.DELETE);}
    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);}
    @Override
    @UnsupportedFileOperation
    public void renameTo(AbstractFile destFile) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.RENAME);}
    @Override
    @UnsupportedFileOperation
    public void changeDate(long lastModified) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);}
    @Override
    public long getSize() {return -1;}
    @Override
    public void setParent(AbstractFile parent) {}
    @Override
    public boolean exists() {return true;}
    @Override
    public FilePermissions getPermissions() {return BookmarkFile.PERMISSIONS;}
    @Override
    @UnsupportedFileOperation
    public void changePermission(int access, int permission, boolean enabled) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);}
    @Override
    public PermissionBits getChangeablePermissions() {return PermissionBits.EMPTY_PERMISSION_BITS;}
    @Override
    public boolean isSymlink() {return false;}
    @Override
    public boolean isSystem() {return false;}
    @Override
    @UnsupportedFileOperation
    public void mkdir() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);}
    @Override
    @UnsupportedFileOperation
    public InputStream getInputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.READ_FILE);}
    @Override
    @UnsupportedFileOperation
    public OutputStream getOutputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);}
    @Override
    @UnsupportedFileOperation
    public OutputStream getAppendOutputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);}
    @Override
    @UnsupportedFileOperation
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);}
    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);}
    @Override
    @UnsupportedFileOperation
    public long getFreeSpace() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);}
    @Override
    @UnsupportedFileOperation
    public long getTotalSpace() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);}
    @Override
    public Object getUnderlyingFileObject() {return null;}
    @Override
    public String getOwner() {return null;}
    @Override
    public boolean canGetOwner() {return false;}
    @Override
    public String getGroup() {return null;}
    @Override
    public boolean canGetGroup() {return false;}
}
