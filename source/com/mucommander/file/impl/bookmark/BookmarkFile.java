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

package com.mucommander.file.impl.bookmark;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkBuilder;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.process.AbstractProcess;

import java.io.*;

/**
 * Represents a file in the <code>bookmark://</code> file system.
 * @author Nicolas Rinaudo
 */
public class BookmarkFile extends AbstractFile {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Bookmark wrapped by this abstract file. */
    private Bookmark     bookmark;
    /** Underlying abstract file. */
    private AbstractFile file;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new bookmark file wrapping the specified bookmark.
     * @param  bookmark    bookmark to wrap.
     * @throws IOException if the specified bookmark's URL cannot be resolved.
     */
    public BookmarkFile(Bookmark bookmark) throws IOException {
        super(new FileURL(FileProtocols.BOOKMARKS + "://" + java.net.URLEncoder.encode(bookmark.getName(), "UTF-8")));
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

    public Bookmark getBookmark() {return bookmark;}



    // - AbstractFile methods --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the underlying bookmark's name.
     * @return the underlying bookmark's name.
     */
    public String getName() {return bookmark.getName();}

    /**
     * Returns the wrapped file's descendants.
     * @return             the wrapped file's descendants.
     * @throws IOException if an I/O error occurs.
     */
    public AbstractFile[] ls() throws IOException {return getUnderlyingFile().ls();}

    /**
     * Returns the wrapped file's parent.
     * @return the wrapped file's parent.
     */
    public AbstractFile getParent() throws IOException {
        return new BookmarkRoot();
    }

    /**
     * Returns <code>true</code> if the wrapped file knows how to create processes.
     * @return <code>true</code> if the wrapped file knows how to create processes.
     */
    public boolean canRunProcess() {return getUnderlyingFile().canRunProcess();}

    /**
     * Runs the specified command on the wrapped file.
     * @param  tokens command to run.
     * @return        a process running the specified command.
     */
    public AbstractProcess runProcess(String[] tokens) throws IOException {return getUnderlyingFile().runProcess(tokens);}

    /**
     * Returns the result of the wrapped file's <code>getFreeSpace()</code> methods.
     * @return the result of the wrapped file's <code>getFreeSpace()</code> methods.
     */
    public long getFreeSpace() {return getUnderlyingFile().getFreeSpace();}

    /**
     * Returns the result of the wrapped file's <code>getTotalSpace()</code> methods.
     * @return the result of the wrapped file's <code>getTotalSpace()</code> methods.
     */
    public long getTotalSpace() {return getUnderlyingFile().getTotalSpace();}

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>.
     */
    public boolean isDirectory() {return false;}

    /**
     * Returns <code>true</code>.
     * @return <code>true</code>.
     */
    public boolean isBrowsable() {return true;}

    /**
     * Sets the wrapped file's parent.
     * @param parent object to use as the wrapped file's parent.
     */
    public void setParent(AbstractFile parent) {
        getUnderlyingFile().setParent(parent);}

    /**
     * Returns <code>true</code> if the specified bookmark exists.
     * <p>
     * A bookmark is said to exist if and only if it is known to the {@link com.mucommander.bookmark.BookmarkManager}.
     * </p>
     * @return <code>true</code> if the specified bookmark exists, <code>false</code> otherwise.
     */
    public boolean exists() {return BookmarkManager.getBookmark(bookmark.getName()) != null;}

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

    public String getCanonicalPath() {return bookmark.getLocation();}



    // - Bookmark renaming -----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns {@link AbstractFile#MUST_HINT}.
     * <p>
     * If the specified file is a <code>BookmarkFile</code>, then we must use the custom
     * {@link #moveTo(AbstractFile) moveTo} method. Otherwise, the point is moot as any
     * other move operation will fail.
     * </p>
     * @param  destination where the file will be moved to.
     * @return             {@link AbstractFile#MUST_HINT}.
     */
    public int getMoveToHint(AbstractFile destination) {
        if(destination.getAncestor() instanceof BookmarkFile)
            return MUST_HINT;
        return MUST_NOT_HINT;
    }

    /**
     * Tries to move the bookmark to the specified destination.
     * <p>
     * If the specified destination is an instance of <code>BookmarkFile</code>,
     * this will rename the bookmark. Otherwise, this method will fail.
     * </p>
     * @param  destination           where to move the bookmark to.
     * @return                       <code>true</code>.
     * @throws FileTransferException if the specified destination is not an instance of <code>BookmarkFile</code>.
     */
    public boolean moveTo(AbstractFile destination) throws FileTransferException {
        Bookmark oldBookmark;
        Bookmark newBookmark;

        destination = destination.getAncestor();

        // Makes sure we're working with a bookmark.
        if(!(destination instanceof BookmarkFile))
            throw new FileTransferException(FileTransferException.OPENING_DESTINATION);

        // Creates the new bookmark and checks for conflicts.
        newBookmark = new Bookmark(((BookmarkFile)destination).getName(), bookmark.getLocation());
        if((oldBookmark = BookmarkManager.getBookmark(newBookmark.getName())) != null)
            BookmarkManager.removeBookmark(oldBookmark);

        // Adds the new bookmark and deletes its 'old' version.
        BookmarkManager.addBookmark(newBookmark);
        BookmarkManager.removeBookmark(bookmark);

        return true;
    }

    /**
     * Deletes the bookmark.
     * <p>
     * Deleting a bookmark means unregistering it from the {@link com.mucommander.bookmark.BookmarkManager}.
     * </p>
     */
    public void delete() {BookmarkManager.removeBookmark(bookmark);}



    // - Bookmark duplication --------------------------------------------------
    // -------------------------------------------------------------------------
    public int getCopyToHint(AbstractFile destination) {
        destination = destination.getAncestor();
        if(destination instanceof BookmarkFile)
            return MUST_HINT;
        return MUST_NOT_HINT;
    }

    /**
     * Tries to copy the bookmark to the specified destination.
     * <p>
     * If the specified destination is an instance of <code>BookmarkFile</code>,
     * this will duplicate the bookmark. Otherwise, this method will fail.
     * </p>
     * @param  destination           where to copy the bookmark to.
     * @return                       <code>true</code>.
     * @throws FileTransferException if the specified destination is not an instance of <code>BookmarkFile</code>.
     */
    public boolean copyTo(AbstractFile destination) throws FileTransferException {
        // Makes sure we're working with a bookmark.
        destination = destination.getAncestor();
        if(!(destination instanceof BookmarkFile))
            throw new FileTransferException(FileTransferException.OPENING_DESTINATION);

        // Copies this bookmark to the specified destination.
        BookmarkManager.addBookmark(new Bookmark(((BookmarkFile)destination).getName(), bookmark.getLocation()));

        return true;
    }



    // - Permissions -----------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns <code>true</code>.
     * <p>
     * Since bookmarks are always readable, writable and 'openable' by the current
     * user, all permissions are always granted.
     * </p>
     * @param  access     ignored.
     * @param  permission ignored.
     * @return            <code>true</code>.
     */
    public boolean getPermission(int access, int permission) {return true;}

    /**
     * Returns <code>true</code>.
     * <p>
     * Bookmarks' permissions are fully retrievable.
     * </p>
     * @param  access     ignored.
     * @param  permission ignored.
     * @return            <code>true</code>.
     */
    public boolean canGetPermission(int access, int permission) {return true;}

    /**
     * Returns <code>false</code>.
     * <p>
     * Bookmarks always have all permissions, this is not changeable. Calls
     * to this method will always be ignored.
     * </p>
     * @param  access     ignored.
     * @param  permission ignored.
     * @param  enabled    ignored.
     * @return            <code>false</code>.
     */
    public boolean setPermission(int access, int permission, boolean enabled) {return false;}

    /**
     * Returns <code>false</code>.
     * <p>
     * Bookmarks always have all permissions, this is not changeable.
     * </p>
     * @param  access     ignored.
     * @param  permission ignored.
     * @return            <code>false</code>.
     */
    public boolean canSetPermission(int access, int permission) {return false;}



    // - Import / export -------------------------------------------------------
    // -------------------------------------------------------------------------
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
            if(e2 instanceof IOException)
                throw (IOException)e2;

            // Otherwise, throw the exception as an IOException with a the underlying cause's message.
            throw new IOException(e.getMessage());
        }

        return new ByteArrayInputStream(stream.toByteArray());
    }

    public OutputStream getOutputStream(boolean append) throws IOException {return new BookmarkOutputStream();}



    // - Unused methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    // The following methods are not used by BookmarkFile. They will throw an exception or
    // return an 'operation non supported' / default value.

    public void mkdir() throws IOException {throw new IOException();}
    public long getDate() {return 0;}
    public boolean canChangeDate() {return false;}
    public boolean changeDate(long lastModified) {return false;}
    public long getSize() {return -1;}
    public boolean hasRandomAccessInputStream() {return false;}
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {throw new IOException();}
    public boolean hasRandomAccessOutputStream() {return false;}
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {throw new IOException();}
    public Object getUnderlyingFileObject() {return null;}
    public boolean isSymlink() {return false;}

}
