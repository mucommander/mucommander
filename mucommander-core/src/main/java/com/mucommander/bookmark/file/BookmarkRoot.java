/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionAccess;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.PermissionType;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

/**
 * Represents the root of the <code>bookmarks://</code> file system.
 * @author Nicolas Rinaudo
 */
class BookmarkRoot extends ProtocolFile implements BookmarkListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkRoot.class);

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
        // Retrieves all available bookmarks.
        Collection<Bookmark> bookmarks = BookmarkManager.getBookmarks();
        try {
            // Creates the associated instances of BookmarkFile
            return bookmarks.stream().map(this::toFile).toArray(AbstractFile[]::new);
        } catch(Exception e) {
            throw new IOException(e);
        }
    }

    private BookmarkFile toFile(Bookmark bookmark) {
        try {
            return new BookmarkFile(bookmark);
        } catch (IOException e) {
            LOGGER.error("failed to create BookmarkFile", e);
            throw new RuntimeException(e);
        }
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
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);}
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
