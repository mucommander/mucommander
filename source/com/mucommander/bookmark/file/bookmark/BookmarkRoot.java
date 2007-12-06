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

package com.mucommander.bookmark.file.bookmark;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.process.AbstractProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the root of the <code>bookmarks://</code> file system.
 * @author Nicolas Rinaudo
 */
class BookmarkRoot extends AbstractFile implements BookmarkListener {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Time at which the bookmarks were last modified. */
    private long lastModified;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    public BookmarkRoot() throws IOException {this(new FileURL(FileProtocols.BOOKMARKS + "://"));}
    public BookmarkRoot(FileURL url) {
        super(url);
        lastModified = System.currentTimeMillis();
        BookmarkManager.addBookmarkListener(this);
    }



    // - AbstractFile methods --------------------------------------------------
    // -------------------------------------------------------------------------
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

    public String getName() {return "";}

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
    public long getDate() {return lastModified;}



    // - Unused methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    // The following methods are not used by BookmarkFile. They will throw an exception,
    // return an 'operation non supported' value or return a default value.

    public AbstractFile getParent() {return null;}
    public void delete() throws IOException {throw new IOException();}
    public boolean canChangeDate() {return false;}
    public boolean changeDate(long lastModified) {return false;}
    public long getSize() {return -1;}
    public void setParent(AbstractFile parent) {}
    public boolean exists() {return true;}
    public boolean getPermission(int access, int permission) {return false;}
    public boolean setPermission(int access, int permission, boolean enabled) {return false;}
    public boolean canGetPermission(int access, int permission) {return false;}
    public boolean canSetPermission(int access, int permission) {return false;}
    public boolean isSymlink() {return false;}
    public void mkdir() throws IOException {throw new IOException();}
    public InputStream getInputStream() throws IOException {throw new IOException();}
    public OutputStream getOutputStream(boolean append) throws IOException {throw new IOException();}
    public boolean hasRandomAccessInputStream() {return false;}
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {throw new IOException();}
    public boolean hasRandomAccessOutputStream() {return false;}
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {throw new IOException();}
    public long getFreeSpace() {return -1;}
    public long getTotalSpace() {return -1;}
    public Object getUnderlyingFileObject() {return null;}
    public boolean canRunProcess() {return false;}
    public AbstractProcess runProcess(String[] tokens) throws IOException {throw new IOException();}
    public String getOwner() {return null;}
    public boolean canGetOwner() {return false;}
    public String getGroup() {return null;}
    public boolean canGetGroup() {return false;}
}
