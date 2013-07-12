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
import com.mucommander.bookmark.BookmarkBuilder;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.commons.file.*;
import com.mucommander.commons.io.FileTransferException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.*;

/**
 * Represents a file in the <code>bookmark://</code> file system.
 * @author Nicolas Rinaudo
 */
public class BookmarkFile extends ProtocolFile {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Bookmark wrapped by this abstract file. */
    private Bookmark     bookmark;
    /** Underlying abstract file. */
    private AbstractFile file;

    /** Permissions for all bookmark files: rw- (600 octal). Only the 'user' permissions bits are supported. */
    final static FilePermissions PERMISSIONS = new SimpleFilePermissions(384, 448);


    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new bookmark file wrapping the specified bookmark.
     * @param  bookmark    bookmark to wrap.
     * @throws IOException if the specified bookmark's URL cannot be resolved.
     */
    protected BookmarkFile(Bookmark bookmark) throws IOException {
        super(FileURL.getFileURL(BookmarkProtocolProvider.BOOKMARK + "://" + java.net.URLEncoder.encode(bookmark.getName(), "UTF-8")));
        this.bookmark = bookmark;
    }



    // - Helper methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the <code>AbstractFile</code> this instance wraps.
     * <p>
     * Some methods need to have access to the underlying file. This, however, requires
     * resolving the path which can be time consuming. Using this method ensures that the
     * path is only resolved if necessary, and at most once.
     * </p>
     * @return the <code>AbstractFile</code> this instance wraps.
     */
    private synchronized AbstractFile getUnderlyingFile() {
        // Resolves the file if necessary.
        if(file == null)
            file = FileFactory.getFile(bookmark.getLocation());

        return file;
    }

    /**
     * Returns the underlying bookmark.
     * @return the underlying bookmark.
     */
    public Bookmark getBookmark() {return bookmark;}



    // - AbstractFile methods --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the underlying bookmark's name.
     * @return the underlying bookmark's name.
     */
    @Override
    public String getName() {return bookmark.getName();}

    /**
     * Returns the wrapped file's descendants.
     * @return             the wrapped file's descendants.
     * @throws IOException                       if an I/O error occurs.
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        return getUnderlyingFile().ls();
    }

    /**
     * Returns the wrapped file's parent.
     * @return             the wrapped file's parent.
     * @see                #setParent(AbstractFile)
     */
    @Override
    public AbstractFile getParent() {
        try {
            return new BookmarkRoot();
        }
        catch(IOException e) {
            return null;
        }
    }

    /**
     * Returns the result of the wrapped file's <code>getFreeSpace()</code> methods.
     * @return the result of the wrapped file's <code>getFreeSpace()</code> methods.
     * @throws IOException if an I/O error occurred
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {return getUnderlyingFile().getFreeSpace();}

    /**
     * Returns the result of the wrapped file's <code>getTotalSpace()</code> methods.
     * @return the result of the wrapped file's <code>getTotalSpace()</code> methods.
     * @throws IOException if an I/O error occurred
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {return getUnderlyingFile().getTotalSpace();}

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>.
     */
    @Override
    public boolean isDirectory() {return true;}

    /**
     * Sets the wrapped file's parent.
     * @param parent object to use as the wrapped file's parent.
     * @see          AbstractFile#getParent()
     */
    @Override
    public void setParent(AbstractFile parent) {
        getUnderlyingFile().setParent(parent);}

    /**
     * Returns <code>true</code> if the specified bookmark exists.
     * <p>
     * A bookmark is said to exist if and only if it is known to the {@link com.mucommander.bookmark.BookmarkManager}.
     * </p>
     * @return <code>true</code> if the specified bookmark exists, <code>false</code> otherwise.
     */
    @Override
    public boolean exists() {return BookmarkManager.getBookmark(bookmark.getName()) != null;}

    @Override
    public void mkfile() {BookmarkManager.addBookmark(bookmark);}

    public boolean equals(Object o) {
        // Makes sure we're working with an abstract file.
        if(!(o instanceof AbstractFile))
            return false;

        // Retrieves the actual file instance.
        // We might have received a Proxied or Cached file, so we need to make sure
        // we 'unwrap' that before comparing.
        AbstractFile file = ((AbstractFile)o).getAncestor();

        // We only know how to compare one bookmark file to the other.
        if(file instanceof BookmarkFile)
            return bookmark.equals(((BookmarkFile)file).getBookmark());
        return false;
    }

    @Override
    public String getCanonicalPath() {return bookmark.getLocation();}



    // - Bookmark renaming -----------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Attempts to rename the bookmark to the specified destination.
     * The operation will only be carried out if the specified destination is a <code>BookmarkFile</code> or has an
     * ancestor that is.
     *
     * @param  destination where to move the bookmark to.
     * @throws IOException if the operation could not be carried out.
     */
    @Override
    public void renameTo(AbstractFile destination) throws IOException {
        checkRenamePrerequisites(destination, true, true);

        Bookmark oldBookmark;
        Bookmark newBookmark;

        destination = destination.getTopAncestor();

        // Makes sure we're working with a bookmark.
        if(!(destination instanceof BookmarkFile))
            throw new IOException();

        // Creates the new bookmark and checks for conflicts.
        newBookmark = new Bookmark(destination.getName(), bookmark.getLocation());
        if((oldBookmark = BookmarkManager.getBookmark(newBookmark.getName())) != null)
            BookmarkManager.removeBookmark(oldBookmark);

        // Adds the new bookmark and deletes its 'old' version.
        BookmarkManager.addBookmark(newBookmark);
        BookmarkManager.removeBookmark(bookmark);
    }

    // TODO: bookmark deleting is currently disabled as a quick fix for #329    
//    /**
//     * Deletes the bookmark.
//     * <p>
//     * Deleting a bookmark means unregistering it from the {@link com.mucommander.bookmark.BookmarkManager}.
//     * </p>
//     */
//    @Override
//    public void delete() {
//        BookmarkManager.removeBookmark(bookmark);
//    }

    @Override
    @UnsupportedFileOperation
    public void delete() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.DELETE);
    }



    // - Bookmark duplication --------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Tries to copy the bookmark to the specified destination.
     * <p>
     * If the specified destination is an instance of <code>BookmarkFile</code>,
     * this will duplicate the bookmark. Otherwise, this method will fail.
     * </p>
     * @param  destination           where to copy the bookmark to.
     * @throws FileTransferException if the specified destination is not an instance of <code>BookmarkFile</code>.
     */
    @Override
    public void copyRemotelyTo(AbstractFile destination) throws IOException {
        // Makes sure we're working with a bookmark.
        destination = destination.getTopAncestor();
        if(!(destination instanceof BookmarkFile))
            throw new IOException();

        // Copies this bookmark to the specified destination.
        BookmarkManager.addBookmark(new Bookmark(destination.getName(), bookmark.getLocation()));
    }



    // - Permissions -----------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the same permissions for all boookmark files: rw- (600 octal).
     * Only the 'user' permissions bits are supported.

     * @return            this file's permissions.
     * @see               #changePermission(int,int,boolean)
     */
    @Override
    public FilePermissions getPermissions() {return PERMISSIONS;}

    /**
     * Always throws an {@link UnsupportedFileOperationException} when called: bookmarks always have all permissions,
     * this is not changeable.
     *
     * @param  access     ignored.
     * @param  permission ignored.
     * @param  enabled    ignored.
     * @see               #getPermissions()
     */
    @Override
    @UnsupportedFileOperation
    public void changePermission(int access, int permission, boolean enabled) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
    }


    // - Import / export -------------------------------------------------------
    // -------------------------------------------------------------------------
    @Override
    public InputStream getInputStream() throws IOException {
        BookmarkBuilder       builder;
        ByteArrayOutputStream stream;

        builder = BookmarkManager.getBookmarkWriter(stream = new ByteArrayOutputStream());
        try {
            builder.startBookmarks();
            builder.addBookmark(bookmark.getName(), bookmark.getLocation());
            builder.endBookmarks();
        }
        // If an exception occured, we have to look for its root cause.
        catch(Throwable e) {
            Throwable e2;

            // Looks for the cause.
            while((e2 = e.getCause()) != null)
                e = e2;

            // If the cause is an IOException, thow it.
            if(e instanceof IOException)
                throw (IOException)e;

            // Otherwise, throw the exception as an IOException with a the underlying cause's message.
            throw new IOException(e.getMessage());
        }

        return new ByteArrayInputStream(stream.toByteArray());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {return new BookmarkOutputStream();}


// - Unused methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    // The following methods are not used by BookmarkFile. They will throw an exception or
    // return an 'operation non supported' / default value.

    @Override
    @UnsupportedFileOperation
    public void mkdir() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);}
    @Override
    public long getDate() {return 0;}
    @Override
    public PermissionBits getChangeablePermissions() {return PermissionBits.EMPTY_PERMISSION_BITS;}
    @Override
    @UnsupportedFileOperation
    public void changeDate(long lastModified) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);}
    @Override
    public long getSize() {return -1;}
    @Override
    @UnsupportedFileOperation
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);}
    @Override
    @UnsupportedFileOperation
    public OutputStream getAppendOutputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);}
    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);}
    @Override
    public Object getUnderlyingFileObject() {return null;}
    @Override
    public boolean isSymlink() {return false;}
    @Override
    public boolean isSystem() {return false;}
    @Override
    public String getOwner() {return null;}
    @Override
    public boolean canGetOwner() {return false;}
    @Override
    public String getGroup() {return null;}
    @Override
    public boolean canGetGroup() {return false;}
}
