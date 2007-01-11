package com.mucommander.file;

import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.impl.local.FSFile;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.process.AbstractProcess;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The superclass of all files, all your files are belong to it.
 *
 * <p>AbstractFile instances cannot and should not be created directly, use {@link FileFactory FileFactory}
 * for that purposes.
 *
 * @see FileFactory, ProxyFile
 * @author Maxence Bernard
 */
public abstract class AbstractFile {

    /** URL representing this file */
    protected FileURL fileURL;

    /** Default path separator */
    public final static String DEFAULT_SEPARATOR = "/";

    /** Indicates {@link #copyTo(AbstractFile)}/{@link #moveTo(AbstractFile)} *should* be used to copy/move the file (e.g. more efficient) */
    public final static int SHOULD_HINT = 0;
    /** Indicates {@link #copyTo(AbstractFile)}/{@link #moveTo(AbstractFile)} *should not* be used to copy/move the file (default) */
    public final static int SHOULD_NOT_HINT = 1;
    /** Indicates {@link #copyTo(AbstractFile)}/{@link #moveTo(AbstractFile)} *must* be used to copy/move the file (e.g. no other way to do so) */
    public final static int MUST_HINT = 2;
    /** Indicates {@link #copyTo(AbstractFile)}/{@link #moveTo(AbstractFile)} *must not* be used to copy/move the file (e.g. not implemented) */
    public final static int MUST_NOT_HINT = 3;

    // Note: raising buffers size from 8192 to 65536 makes a huge difference in SFTP transfer rates

    /** Size allocated to read buffer */
    public final static int READ_BUFFER_SIZE = 65536;

    /** Default buffer size for BufferedOutputStream */
    public final static int WRITE_BUFFER_SIZE = 65536;


    /** Bit mask for 'execute' file permission */
    public final static int EXECUTE_MASK = 64;
    /** Bit mask for 'write' file permission */
    public final static int WRITE_MASK = 128;
    /** Bit mask for 'read' file permission */
    public final static int READ_MASK = 256;


    /**
     * Creates a new file instance with the given URL.
     */
    protected AbstractFile(FileURL url) {
        this.fileURL = url;
    }
	
    
    ////////////////////
    // Static methods //
    ////////////////////


    //////////////////////////////////////
    // Implemented AbstractFile methods //
    //////////////////////////////////////

    /**
     * Returns the URL representing this file.
     */
    public FileURL getURL() {
        return fileURL;
    }


    /**
     * Returns the name of this AbstractFile.
     *
     * <p>The returned name is the filename extracted from this file's {@link FileURL}
     * as returned by {@link FileURL#getFilename()}. If the filename is <code>null</code> (e.g. http://google.com), the
     * <code>FileURL</code>'s host will be returned instead. If the host is <code>null</code> (e.g. smb://), an empty
     * String will be returned. Thus, the returned name will never be <code>null</code>.
     *
     * <p>This method should be overridden if a special treatment (e.g. URL-decoding)
     * needs to be applied to the returned filename.
     */
    public String getName() {
        String name = fileURL.getFilename();
        // If filename is null, use host instead
        if(name==null) {
            name = fileURL.getHost();
            // If host is null, return an empty string
            if(name==null)
                return "";
        }

        return name;
    }


    /**
     * Computes the name of the file without its extension.
     * <p>
     * Within the context of this method, a file will have an
     * extension if and only if:<br/>
     * - it's not a directory.<br/>
     * - it contains at least one <code>.</code> character.<br/>
     * - the last <code>.</code> is not the last character in the file's name.<br/>
     * - the last <code>.</code> is not the first character in the file's name.<br/>
     * If a file is found not to have an extension, its full name is returned.
     * </p>
     * @return the file's name, without its extension.
     * @see    #getName()
     * @see    #getExtension()
     */
    public String getNameWithoutExtension() {
        String name;
        int    position;

        // Directories do not have extension.
        if(isDirectory())
            return getName();

        name     = getName();
        position = name.lastIndexOf('.');

        // If the extension 'dot' either:
        // - does not exist
        // - is the first character of the file's name
        // - is the last character of the file's name
        // then we don't have an extension.
        if((position == -1) || (position == name.length() - 1) ||
           (position == 0))
            return name;

        return name.substring(0, position);
    }


    /**
     * Returns the given file's extension, <code>null</code> if the file doesn't have an extension.
     */
    public String getExtension() {
        return getExtension(getName());
    }

    
    /**
     * Returns the given filename's extension, <code>null</code> if the name doesn't have an extension.
     *
     * @param filename a filename, not a full path
     */
    public static String getExtension(String filename) {
        int lastDotPos = filename.lastIndexOf('.');

        int len;
        if(lastDotPos==-1 || lastDotPos==(len=filename.length())-1)
            return null;

        return filename.substring(lastDotPos+1, len);
    }



    /**
     * Returns the absolute path of this AbstractFile:
     * <ul>
     * <li>For local files, the path is returned 'sans' the protocol and host parts (i.e. without file://localhost)
     * <li>For any other file protocol, the full URL including the protocol and host parts is returned (e.g. smb://192.168.1.1/root/blah)
     * </ul>
     *
     * <p>The returned path will always be free of any login and password and thus can be safely displayed or stored.
     */
    public String getAbsolutePath() {
        FileURL fileURL = getURL();

        // For local files: return file's path 'sans' the protocol and host parts
        if(fileURL.getProtocol().equals(FileProtocols.FILE))
            return fileURL.getPath();

        // For any other file protocols: return the full URL that includes the protocol and host parts
        return fileURL.getStringRep(false);
    }


    /**
     * Returns the absolute path of this AbstractFile with a trailing separator character if <code>true</code> is passed,
     * or without one if <code>false</code> is passed.
     */
    public String getAbsolutePath(boolean appendSeparator) {
        String path = getAbsolutePath();
        return appendSeparator?addTrailingSeparator(path):removeTrailingSlash(path);
    }

	
    /**
     * Returns the canonical path of this AbstractFile, resolving any symbolic links or '..' and '.' occurrences.
     *
     * <p>This implementation simply returns the value of {@link #getAbsolutePath()}, and thus should be overridden
     * if canonical path resolution is available.
     */
    public String getCanonicalPath() {
        return getAbsolutePath();
    }


    /**
     * Returns the canonical path of this AbstractFile, resolving any symbolic links or '..' and '.' occurrences,
     * with an appended separator character if <code>true</code> is passed or without one if <code>false</code> is passed.
     *
     * <p>This implementation simply returns the value of {@link #getAbsolutePath(boolean)}, and thus should be
     * overridden if canonical path resolution is available.
     */
    public String getCanonicalPath(boolean appendSeparator) {
        String path = getCanonicalPath();
        return appendSeparator?addTrailingSeparator(path):removeTrailingSlash(path);
    }
	

    /**
     * Returns the path separator of this AbstractFile.
     * <p>This implementation returns the default separator "/", this method should be overridden
     * if the path separator is different.
     */
    public String getSeparator() {
        return DEFAULT_SEPARATOR;
    }


    /**
     * Returns <code>true</code> if this file is a parent folder of the given file, or if the 2 files are equal.
     */
    public boolean isParentOf(AbstractFile file) {
        return isBrowsable() && file.getCanonicalPath(true).startsWith(getCanonicalPath(true));
    }

	
    /**
     * Returns true if this AbstractFile can be browsed (entered): true for directories and supported archive files.
     */
    public boolean isBrowsable() {
        return isDirectory() || (this instanceof AbstractArchiveFile);
    }


    /**
     * Returns true if this AbstractFile is hidden.
     *
     * <p>This default implementation is solely based on the filename and returns <code>true</code> if this
     * file's name starts with '.'. This method should be overriden if the underlying filesystem has a notion 
     * of hidden files.</p>
     */	
    public boolean isHidden() {
        return getName().startsWith(".");
    }


    /**
     * Returns <code>true</code> if this file is an archive entry. 
     */
    public boolean isArchiveEntry() {
        return this instanceof ArchiveEntryFile
           || (this instanceof AbstractArchiveFile && !(((AbstractArchiveFile)this).getProxiedFile() instanceof FSFile));
    }


    /**
     * Returns the root folder that contains this AbstractFile. If this file is already
     * a root folder (no parent), it will simply be returned.
     */
    public AbstractFile getRoot() {
        AbstractFile parent;
        AbstractFile child = this; 
        while((parent=child.getParent())!=null && !parent.equals(child)) {
            child = parent;
        }
		
        return child;
    }
	

    /**
     * Tests if the given path contains a trailing separator character, and if not, adds one and returns the path.
     */
    protected String addTrailingSeparator(String path) {
        // Even though getAbsolutePath() is not supposed to return a trailing separator, root folders ('/', 'c:\' ...)
        // are exceptions that's why we still have to test if path ends with a separator
        String separator = getSeparator();
        if(!path.endsWith(separator))
            return path+separator;
        return path;
    }
	
	
    /**
     * Tests if the given path contains a trailing separator character, and if it does, removes it and returns the new path.
     */
    protected String removeTrailingSlash(String path) {
        // Remove trailing slash if path is not '/' or trailing backslash if path does not end with ':\' 
        // (Reminder: C: is C's current folder, while C:\ is C's root)
        String separator = getSeparator();
        if(path.endsWith(separator)
           && !((separator.equals("/") && path.length()==1) || (separator.equals("\\") && path.charAt(path.length()-2)==':')))
            path = path.substring(0, path.length()-1);
        return path;
    }
	

    /**
     * Returns an InputStream to read from this AbstractFile, skipping the
     * specified number of bytes. This method should be overridden whenever
     * possible to provide a more efficient implementation, as this implementation
     * uses {@link java.io.InputStream#skip(long)}
     * which may *read* bytes and discards them, which is bad (think of an ISO file on a remote server).
     *
     * @throws IOException if this AbstractFile cannot be read or is a folder.
     */
    public InputStream getInputStream(long skipBytes) throws IOException {
        InputStream in = getInputStream();
		
        // Call InputStream.skip() until the specified number of bytes have been skipped
        long nbSkipped = 0;
        long n;
        while(nbSkipped<skipBytes) {
            n = in.skip(skipBytes-nbSkipped);
            if(n>0)
                nbSkipped += n;
        }

        return in;
    }
	

    /**
     * Copies the contents of the given <code>InputStream</code> to the specified </code>OutputStream</code>
     * and throws an IOException if something went wrong. The streams will *NOT* be closed by this method.
     *
     * <p>A read buffer is used of {@link #READ_BUFFER_SIZE} bytes is used to read from the InputStream.
     * For optimal performance, a <code>BufferedOutputStream</code> should be provided. It is useless though to
     * provide a BufferInputStream as read operations are already buffered.
     *
     * <p>Copy progress can optionally be monitored by supplying a {@link com.mucommander.io.CounterInputStream}
     * and/or {@link com.mucommander.io.CounterOutputStream}.
     *
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     */
    public static void copyStream(InputStream in, OutputStream out) throws FileTransferException {
        // Init read buffer
        byte buffer[] = new byte[READ_BUFFER_SIZE];

        // Copies the InputStream's content to the OutputStream chunks by chunks
        int nbRead;

        while(true) {
            try {
                nbRead = in.read(buffer, 0, buffer.length);
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferException.READING_SOURCE);
            }

            if(nbRead==-1)
                break;

            try {
                out.write(buffer, 0, nbRead);
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferException.WRITING_DESTINATION);
            }
        }
    }


    /**
     * Copies the contents of the given <code>InputStream</code> to this file. The provided <code>InputStream</code>
     * will *NOT* be closed by this method.
     * 
     * <p>Read and write operations are buffered, with a respective buffer of {@link #READ_BUFFER_SIZE} and
     * {@link #WRITE_BUFFER_SIZE} bytes.
     *
     * <p>This method should be overridden by file protocols that do not offer a {@link #getOutputStream(boolean)}
     * implementation, but that can take an <code>InputStream</code> and use it to write the file.
     *
     * <p>Copy progress can optionally be monitored by supplying a {@link com.mucommander.io.CounterInputStream}.
     *
     * @param in the InputStream to read from
     * @param append if true, data written to the OutputStream will be appended to the end of this file. If false, any existing data will be overwritten.
     * @throws FileTransferException if something went wrong while reading from the InputStream or writing to this file
     */
    public void copyStream(InputStream in, boolean append) throws FileTransferException {
        OutputStream out;

        try {
            // Create a BufferedOutputStream to speed up the output
            out = new BufferedOutputStream(getOutputStream(append), WRITE_BUFFER_SIZE);
        }
        catch(IOException e) {
            throw new FileTransferException(FileTransferException.OPENING_DESTINATION);
        }

        try {
            copyStream(in, out);
        }
        finally {
            // Close stream even if copyStream() threw an IOException
            try {
                out.close();
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferException.CLOSING_DESTINATION);
            }
        }
    }
    

    /**
     * Copies this AbstractFile to another specified one and throws an <code>IOException</code> if the operation
     * failed. The contents of the destination file will be overwritten.
     *
     * <p>This generic implementation should be overridden by file protocols which are able to perform
     * a server-to-server copy.
     *
     * @param destFile the destination file this file should be copied to
     * @throws FileTransferException if this AbstractFile could be read, or the destination could be written, or if
     * the operation failed for any other reason (use {@link FileTransferException#getReason()} to get the reason of the failure).
     */
    public void copyTo(AbstractFile destFile) throws FileTransferException {
        // Throw a specific FileTransferException if source and destination files are identical
        if(this.equals(destFile))
            throw new FileTransferException(FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL);
        
        InputStream in;

        try {
            in = getInputStream();
        }
        catch(IOException e) {
            throw new FileTransferException(FileTransferException.OPENING_SOURCE);
        }

        try {
            destFile.copyStream(in, false);
        }
        finally {
            // Close stream even if copyStream() threw an IOException
            try {
                in.close();
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferException.CLOSING_SOURCE);
            }
        }
    }

    
    /**
     * Returns a hint that indicates whether the {@link #copyTo(AbstractFile)} method should be used to
     * copy this file to the specified destination file, rather than copying the file using
     * {@link #copyStream(InputStream, boolean)} or {@link #getInputStream()} and {@link #getOutputStream(boolean)}.
     *
     * <p>Potential returned values are:
     * <ul>
     * <li>{@link #SHOULD_HINT} if copyTo() should be preferred (more efficient)
     * <li>{@link #SHOULD_NOT_HINT} if the file should rather be copied using copyStream()
     * <li>{@link #MUST_HINT} if the file can only be copied using copyTo(), that's the case when getOutputStream() or copyStream() is not implemented
     * <li>{@link #MUST_NOT_HINT} if the file can only be copied using copyStream()
     * </ul>
     *
     * <p>This default implementation returns {@link #SHOULD_NOT_HINT} as some granularity is lost when using
     *  <code>copyTo()</code> making it impossible to monitor progress when copying a file.
     * This method should be overridden when <code>copyTo()</code> should be favored over <code>copyStream()</code>.
     *
     * @param destFile the destination file that is considered being copied
     * @return the hint int indicating whether the {@link #copyTo(AbstractFile)} method should be used
     */
    public int getCopyToHint(AbstractFile destFile) {
        return SHOULD_NOT_HINT;
    }


    /**
     * Moves this AbstractFile to another specified one and throws an <code>IOException</code> if the operation
     * failed. This generic implementation copies this file to the destination one, overwriting any data it contains,
     * and if (and only if) the copy was successful, deletes the original file (this file).
     *
     * <p>This method should be overridden by file protocols which are able to perform a server-to-server move.
     *
     * @param destFile the destination file this file should be moved to
     * @throws FileTransferException if this AbstractFile or destination cannot be written or if the operation failed
     * for any other reason (use {@link FileTransferException#getReason()} to get the reason of the failure).
     */
    public void moveTo(AbstractFile destFile) throws FileTransferException {
        // Throw a specific FileTransferException if source and destination files are identical
        if(this.equals(destFile))
            throw new FileTransferException(FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL);

        copyTo(destFile);

        // The file won't be deleted if copyTo() failed (threw an IOException)
        try {
            delete();
        }
        catch(IOException e) {
            throw new FileTransferException(FileTransferException.DELETING_SOURCE);
        }
    }


    /**
     * Returns a hint that indicates whether the {@link #moveTo(AbstractFile)} method should be used to
     * move this file to the specified destination file, rather than moving the file using
     * {@link #copyStream(InputStream, boolean)} or {@link #getInputStream()} and {@link #getOutputStream(boolean)}.
     *
     * <p>Potential returned values are:
     * <ul>
     * <li>{@link #SHOULD_HINT} if copyTo() should be preferred (more efficient)
     * <li>{@link #SHOULD_NOT_HINT} if the file should rather be copied using copyStream()
     * <li>{@link #MUST_HINT} if the file can only be copied using copyTo(), that's the case when getOutputStream() or copyStream() is not implemented
     * <li>{@link #MUST_NOT_HINT} if the file can only be copied using copyStream()
     * </ul>
     *
     * <p>This default implementation returns {@link #SHOULD_HINT} if both this file and the specified destination file
     * use the same and are located on the same host, {@link #SHOULD_NOT_HINT} otherwise. This method should be
     * overridden to return {@link #SHOULD_NOT_HINT} if the underlying file protocol doesn't not allow 
     * direct move/renaming without copying the contents of the source (this) file.
     *
     * @param destFile the destination file that is considered being copied
     * @return the hint int indicating whether the {@link #moveTo(AbstractFile)} method should be used
     */
    public int getMoveToHint(AbstractFile destFile) {
        // Return false if protocols differ
        if(!fileURL.getProtocol().equals(destFile.fileURL.getProtocol()))
          return SHOULD_NOT_HINT;

        // Are both fileURL's hosts equal ?
        // This test is a bit complicated because each of the hosts can potentially be null (e.g. smb://)
        String host = fileURL.getHost();
        String destHost = destFile.fileURL.getHost();
        boolean hostsEqual = host==null?(destHost==null?true:destHost.equals(host)):host.equals(destHost);

        return hostsEqual ? SHOULD_HINT : SHOULD_NOT_HINT;
    }


    /**
     *
     *
     * @param filter FileFilter which will be used to filter out files, may be <code>null</code>
     */
    public AbstractFile[] ls(FileFilter filter) throws IOException {
        return filter==null?ls():filter.filter(ls());
    }


    /**
     *
     *
     * <p>This default implementation filters out files *after* they have been created. This method
     * should be overridden if a more efficient implementation can be provided.
     *
     * @param filter FilenameFilter which will be used to filter out files, may be <code>null</code>
     */
    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        return filter==null?ls():filter.filter(ls());
    }


    /**
     * Returns read/write/execute permissions as an int, UNIX octal style.
     * The value can be compared against {@link #READ_MASK}, {@link #WRITE_MASK} and {@link #EXECUTE_MASK}
     * bit masks to determine if the file is readable/writable/executable.
     *
     * <p>Implementation note: the implementation of this method calls sequentially {@link #canRead()},
     * {@link #canWrite()} and {@link #canExecute()}. This may affect performance on filesystems which need to perform
     * a network request to retrieve each of these values. In that case, and if the fileystem allows to retrieve all
     * permissions with a single request, this method should be overridden.
     *
     * @return
     */
    public int getPermissions() {
        int perms = 0;

        if(canRead())
            perms |= READ_MASK; 

        if(canWrite())
            perms |= WRITE_MASK;

        if(canExecute())
            perms |= EXECUTE_MASK;    

        return perms;
    }


    /**
     * Returns a string representation of this file's permissions, a concatenation of the following characters:
     * <ul>
     * <li>'l' if this file is a symbolic link,'d' if it is a directory, '-' otherwise
     * <li>'r' if this file is readable, '-' otherwise
     * <li>'w' if this file is writable, '-' otherwise
     * <li>'x' if this file is executable, '-' otherwise
     * </ul>
     *
     * For example, if the file is a directory that is readable, writable and executable, "drwx" will be returned.
     */
    public String getPermissionsString() {
        String perms = "";
        perms += isSymlink()?'l':isDirectory()?'d':'-';
        perms += canRead()?'r':'-';
        perms += canWrite()?'w':'-';
        perms += canExecute()?'x':'-';

        return perms;
    }


    /**
     * Changes the read/write/execute permissions of this file, using the specified permissions int and returns true if
     * the operation was successful, false if at least one of the file permissions could not be changed.
     * The permissions int should be created using {@link #READ_MASK}, {@link #WRITE_MASK} and {@link #EXECUTE_MASK}
     * bit masks combined with logical OR.
     *
     * <p>Implementation note: the implementation of this method calls sequentially {@link #setReadable(boolean)},
     * {@link #setWritable(boolean)} and {@link #setExecutable(boolean)}. This may affect performance on filesystems
     * which need to perform a network request to retrieve each of these value. In that case, and if the fileystem allows
     * to change all permissions with a single request, this method should be overridden.
         *
     * @param permissions the new permissions this file should have
     * @return true if the operation was successful, false if at least one of the file permissions could not be changed 
     */
    public boolean setPermissions(int permissions) {
        boolean success;

        success = setReadable((permissions&READ_MASK)!=0);

        success &= setWritable((permissions&WRITE_MASK)!=0);

        success &= setExecutable((permissions&EXECUTE_MASK)!=0);

        return success;
    }


    /**
     * Convenience method that sets/unsets a bit in the given permissions int.
     */
    public static int setPermissionBit(int permissions, int bit, boolean enabled) {
        if(enabled)
            permissions |= bit;
        else
            permissions &= ~bit;

        return permissions;
    }


    /**
     * Tests a file for equality: returns <code>true</code> if the given file has the same canonical path,
     * as returned by {@link #getCanonicalPath()}.
     *
     * <p>This method should be overriden for network-based filesystems for which a host can have multiple
     * path representations (hostname and IP address).
     */
    public boolean equals(Object f) {
        if(f==null || !(f instanceof AbstractFile))
            return false;
		
        return getCanonicalPath(false).equals(((AbstractFile)f).getCanonicalPath(false));
    }

	
    /**
     * Returns a String representation of this AbstractFile which is the path as returned by getAbsolutePath().
     */
    public String toString() {
        return getAbsolutePath();
    }


    //////////////////////
    // Abstract Methods //
    //////////////////////

    /**
     * Returns the last modified date, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     */
    public abstract long getDate();
	
    /**
     * Changes last modified date and returns <code>true</code> if date was changed successfully, false if the
     * operation is not implemented or could not be successfully completed.
     *
     * @param lastModified last modified date, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     * @return <code>true</code> if date was changed successfully.
     */
    public abstract boolean changeDate(long lastModified);
	
    /**
     * Returns the size in bytes of this AbstractFile, -1 if not known.
     */
    public abstract long getSize();
	
    /**
     * Returns this AbstractFile's parent or null if it doesn't have any parent.
     */
    public abstract AbstractFile getParent();
	
    /**
     * Sets this file's parent or null if it doesn't have any parent.
     */
    public abstract void setParent(AbstractFile parent);

    /**
     * Returns <code>true</code> if this file exists.
     */
    public abstract boolean exists();
	
    /**
     * Returns true if this AbstractFile can be read.
     */	
    public abstract boolean canRead();
	
    /**
     * Returns true if this AbstractFile can be modified.
     */	
    public abstract boolean canWrite();

    /**
     * Returns true if this AbstractFile can be executed. If the underlying filesystem does not have a notion of
     * executable files, false must be returned. 
     */
    public abstract boolean canExecute();

    /**
     * Changes the 'execute' permission of this file and returns true if the operation succeeded, false if it failed or
     * if the operation is not available in the underlying filesystem.
     *
     * @param readable true to make this file readable
     * @return true if the operation succeeded, false if it failed or if the operation is not available
     * in the underlying filesystem
     */
    public abstract boolean setReadable(boolean readable);

    /**
     * Changes the 'write' permission of this file and returns true if the operation succeeded, false if it failed or
     * if the operation is not available in the underlying filesystem.
     *
     * @param writable true to make this file writable
     * @return true if the operation succeeded, false if it failed or if the operation is not available
     * in the underlying filesystem
     */
    public abstract boolean setWritable(boolean writable);

    /**
     * Changes the 'execute' permission of this file and returns true if the operation succeeded, false if it failed or
     * if the operation is not available in the underlying filesystem.
     *
     * @param executable true to make this file executable
     * @return true if the operation succeeded, false if it failed or if the operation is not available
     * in the underlying filesystem
     */
    public abstract boolean setExecutable(boolean executable);

    /**
     * Returns true if the underlying filesystem is capable of changing file permissions. This method does not have
     * read/write/execute granularity and will return true if at least of those permissions can be changed.
     * Filesystems that are read-only or lack the ability to change permissions will return false.
     *
     * @return true if at least one of the read/write/execute permissions can be changed.
     */
    public abstract boolean canSetPermissions();


    /**
     * Returns true if this AbstractFile is a 'regular' directory, not only a 'browsable' file (like an archive file).
     */
    public abstract boolean isDirectory();

    /**
     * Returns true if this file *may* be a symbolic link and thus handled with care.
     */
    public abstract boolean isSymlink();
	
    /**
     * Returns the files containted by this AbstractFile. For this operation to be successful, {@link #isBrowsable()}
     * must return <code>true</code>.
     *
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     */
    public abstract AbstractFile[] ls() throws IOException;

    /**
     * Creates a new directory if this AbstractFile is a folder.
     *
     * @throws IOException if this operation is not possible.
     */
    public abstract void mkdir(String name) throws IOException;

    /**
     * Returns an <code>InputStream</code> to read from this AbstractFile.
     *
     * @throws IOException if this AbstractFile could not be read or if an <code>InputStream</code> could not be
     * provided for any other reason (e.g. file is a directory).
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Returns an <code>InputStream</code> to read from this AbstractFile with random access.
     *
     * @throws IOException if this AbstractFile could not be read or if a <code>RandomAccessInputStream</code> could not
     * be provided because the underlying file protocol doesn't have random access support or for any other reason
     * (e.g. file is a directory).
     */
    public abstract RandomAccessInputStream getRandomAccessInputStream() throws IOException;

    /**
     * Returns an OuputStream to write to this AbstractFile.
     * @param append if true, data written to the OutputStream will be appended to the end of this file. If false, any existing data will be overwritten.
     * @throws IOException if this operation is not permitted or if this AbstractFile is a folder
     */
    public abstract OutputStream getOutputStream(boolean append) throws IOException;

    /**
     * Deletes this AbstractFile and this one only (does not recurse), throws an IOException
     * if it failed.
     * @throws IOException if this AbstractFile is not writable or could not be deleted.
     */	
    public abstract void delete() throws IOException;
	
    /**
     * Returns free space (in bytes) on the disk/volume where this file is, -1 if this information is not available.
     */
    public abstract long getFreeSpace();

    /**
     * Returns the total space (in bytes) of the disk/volume where this file is, -1 if this information is not available. 
     */
    public abstract long getTotalSpace();



    // - Process running -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if it's possible to run processes on the underlying file system.
     * @return <code>true</code> if it's possible to run processes on the underlying file system, <code>false</code> otherwise.
     */
    public abstract boolean canRunProcess();

    /**
     * Creates a process executing the specified command tokens using this AbstractFile as a working directory.
     * @param  tokens                        command and its arguments for the process to create.
     * @return                               a process executing the specified command tokens using this AbstractFile as a working directory.
     * @throws IOException                   thrown if an error occured while creating the process, if the current file is not a directory or if the operation is not supported.
     */
    public abstract AbstractProcess runProcess(String[] tokens) throws IOException;



    //////////////////
    // Test methods //
    //////////////////

    /**
     * Simple bench method.
     */
    public static void main(String args[]) throws IOException {
        AbstractFile folder = FileFactory.getFile("/usr/bin/", null);
        folder.ls();

        long totalTime = 0;
        long now;
        int nbIter = 100;
        for(int i=0; i<nbIter; i++) {
            now = System.currentTimeMillis();
            folder.ls();
            totalTime += System.currentTimeMillis()-now;
        }
	
        System.out.println("Average ls() time = "+totalTime/nbIter);
    }	
}
