package com.mucommander.file;

import com.mucommander.PlatformManager;
import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.io.BufferPool;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.process.AbstractProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * The superclass of all files, all your files are belong to it.
 *
 * <p>AbstractFile instances cannot and should not be created directly, use {@link FileFactory FileFactory}
 * for that purposes.</p>
 *
 * @see com.mucommander.file.FileFactory
 * @see com.mucommander.file.impl.ProxyFile
 * @author Maxence Bernard
 */
public abstract class AbstractFile implements FilePermissions {

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

    /** Size of the read/write buffer */
    // Note: raising buffer size from 8192 to 65536 makes a huge difference in SFTP read transfer rates but beyond
    // 65536, no more gain (not sure why).
    public final static int IO_BUFFER_SIZE = 65536;


    /** Pattern matching Windows drive root folders, e.g. C:\ */
    protected final static Pattern windowsDriveRootPattern = Pattern.compile("^[a-zA-Z]{1}[:]{1}[\\\\]{1}$");


    /**
     * Creates a new file instance with the given URL.
     */
    protected AbstractFile(FileURL url) {
        this.fileURL = url;
    }
	
    
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
     * Returns the name of the file without its extension.
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
    public final String getNameWithoutExtension() {
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
        return fileURL.toString(false);
    }


    /**
     * Returns the absolute path of this AbstractFile with a trailing separator character if <code>true</code> is passed,
     * or without one if <code>false</code> is passed.
     */
    public final String getAbsolutePath(boolean appendSeparator) {
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
     */
    public final String getCanonicalPath(boolean appendSeparator) {
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
           || (this instanceof AbstractArchiveFile && !(((AbstractArchiveFile)this).getProxiedFile() instanceof LocalFile));
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
     * Returns true if this file is a root folder, that is:
     * <ul>
     *  <li>for all protocols other than 'file', if the URL's path part is '/'
     *  <li>for the 'file' protocol, '/' if the OS is not Windows, a drive root for Windows ('C:\' for instance)
     * </ul>
     */
    public boolean isRoot() {
        String path = fileURL.getPath();

        if(fileURL.getProtocol().equals(FileProtocols.FILE))
            return PlatformManager.isWindowsFamily()?windowsDriveRootPattern.matcher(path).matches():path.equals("/");
        else
            return path.equals("/");
    }
    

    /**
     * Tests if the given path contains a trailing separator character, and if not, adds one and returns the path.
     * The separator used is the one returned by {@link #getSeparator()}.
     */
    protected final String addTrailingSeparator(String path) {
        // Even though getAbsolutePath() is not supposed to return a trailing separator, root folders ('/', 'c:\' ...)
        // are exceptions that's why we still have to test if path ends with a separator
        String separator = getSeparator();
        if(!path.endsWith(separator))
            return path+separator;
        return path;
    }
	
	
    /**
     * Tests if the given path contains a trailing separator character, and if it does, removes it and returns the new path.
     * The separator used is the one returned by {@link #getSeparator()}.
     */
    protected final String removeTrailingSlash(String path) {
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
     * <p>Read and write operations are buffered, with a buffer of {@link #IO_BUFFER_SIZE} bytes. For performance
     * reasons, this buffer is provided by {@link BufferPool}. There is no need to provide a BufferedInputStream.
     * A BufferedOutputStream also isn't necessary, unless this method is called repeatedly with the same OutputStream
     * and with potentially small InputStream (smaller than {@link #IO_BUFFER_SIZE}: in this case, providing a
     * BufferedOutputStream will further improve performance by grouping calls to the underlying OutputStream write method.
     *
     * <p>Copy progress can optionally be monitored by supplying a {@link com.mucommander.io.CounterInputStream}
     * and/or {@link com.mucommander.io.CounterOutputStream}.
     *
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     */
    public static void copyStream(InputStream in, OutputStream out) throws FileTransferException {
        // Use BufferPool to reuse any available buffer of the same size
        byte buffer[] = BufferPool.getBuffer(IO_BUFFER_SIZE);
        try {
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
        finally {
            // Make the buffer available for further use
            BufferPool.releaseBuffer(buffer);
        }
    }


    /**
     * Copies the contents of the given <code>InputStream</code> to this file. The provided <code>InputStream</code>
     * will *NOT* be closed by this method.
     * 
     * <p>This method should be overridden by file protocols that do not offer a {@link #getOutputStream(boolean)}
     * implementation, but that can take an <code>InputStream</code> and use it to write the file.
     *
     * <p>Read and write operations are buffered, with a buffer of {@link #IO_BUFFER_SIZE} bytes. For performance
     * reasons, this buffer is provided by {@link BufferPool}. There is no need to provide a BufferedInputStream.
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
            out = getOutputStream(append);
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
     * Copies this AbstractFile to another specified one, overwriting the contents of the destination file (if any).
     * Returns true if the operation could be successfully be completed, false if the operation could not be performed
     * because of unsatisfied conditions (not an error), or throws an {@link FileTransferException} if the
     * operation was attempted but failed.
     *
     * <p>This generic implementation copies this file to the destination one, overwriting any data it contains.
     * The operation will always be attempted, thus will either return true or throw an exception, but will never return false.
     *
     * <p>This method should be overridden by file protocols which are able to perform a server-to-server copy.
     *
     * @param destFile the destination file this file should be copied to
     * @return true if the operation could be successfully be completed, false if the operation could not be performed
     * because of unsatisfied conditions (not an error)
     * @throws FileTransferException if this AbstractFile or destination cannot be written or if the operation failed
     * for any other reason (use {@link FileTransferException#getReason()} to get the reason of the failure).
     */
    public boolean copyTo(AbstractFile destFile) throws FileTransferException {
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
            return true;
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
     * Moves this AbstractFile to another specified one. Returns true if the operation could be successfully
     * be completed, false if the operation could not be performed because of unsatisfied conditions (not an error),
     * or throws an {@link FileTransferException} if the operation was attempted but failed.
     *
     * <p>This generic implementation copies this file to the destination one, overwriting any data it contains,
     * and if (and only if) the copy was successful, deletes the original file (this file). The operation will always
     * be attempted, thus will either return true or throw an exception, but will never return false.
     *
     * <p>This method should be overridden by file protocols which are able to rename files.
     *
     * @param destFile the destination file this file should be moved to
     * @return true if the operation could be successfully be completed, false if the operation could not be performed
     * because of unsatisfied conditions (not an error)
     * @throws FileTransferException if this AbstractFile or destination cannot be written or if the operation failed
     * for any other reason (use {@link FileTransferException#getReason()} to get the reason of the failure).
     */
    public boolean moveTo(AbstractFile destFile) throws FileTransferException {
        // Throw a specific FileTransferException if source and destination files are identical
        if(this.equals(destFile))
            throw new FileTransferException(FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL);

        copyTo(destFile);

        // The file won't be deleted if copyTo() failed (threw an IOException)
        try {
            delete();
            return true;
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
     * use the same protocol and are located on the same host, {@link #SHOULD_NOT_HINT} otherwise.
     * This method should be overridden to return {@link #SHOULD_NOT_HINT} if the underlying file protocol doesn't not
     * allow direct move/renaming without copying the contents of the source (this) file.
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
     * Returns the files contained by this AbstractFile, filtering out files that do not match the specified FileFilter.
     * For this operation to be successful, this file must be 'browsable', i.e. {@link #isBrowsable()} must return
     * <code>true</code>.
     *
     * @param filter FileFilter which will be used to filter out files, may be <code>null</code>
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     */
    public AbstractFile[] ls(FileFilter filter) throws IOException {
        return filter==null?ls():filter.filter(ls());
    }


    /**
     * Returns the files contained by this AbstractFile, filtering out files that do not match the specified FilenameFilter.
     * For this operation to be successful, this file must be 'browsable', i.e. {@link #isBrowsable()} must return 
     * <code>true</code>.
     *
     * <p>This default implementation filters out files *after* they have been created. This method
     * should be overridden if a more efficient implementation can be provided by subclasses.
     *
     * @param filter FilenameFilter which will be used to filter out files based on their filename, may be <code>null</code>
     */
    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        return filter==null?ls():filter.filter(ls());
    }


    /**
     * Convenience method that sets/unsets a bit in the given permissions int.
     */
    protected static int setPermissionBit(int permissions, int bit, boolean enabled) {
        if(enabled)
            permissions |= bit;
        else
            permissions &= ~bit;

        return permissions;
    }


    /**
     * Returns this file's permissions as an int, UNIX octal style.
     * The permissions can be compared against {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION}, {@link #EXECUTE_PERMISSION}
     * and {@link #USER_ACCESS}, {@link #GROUP_ACCESS} and {@link #OTHER_ACCESS} masks.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #getPermission(int, int)} for
     * each permission and access (that's a total of 9 calls). This may affect performance on filesystems which need to perform
     * an I/O request to retrieve each permission individually. In that case, and if the fileystem allows to retrieve all
     * permissions at once, this method should be overridden.
     *
     * @return permissions as an int, UNIX octal style.
     */
    public int getPermissions() {
        int bitShift = 0;
        int perms = 0;

        for(int a=OTHER_ACCESS; a<= USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                if(canGetPermission(a, p) && getPermission(a, p))
                    perms |= (1<<bitShift);

                bitShift++;
            }
        }

        return perms;
    }


    /**
     * Changes this file's permissions to the specified permissions int and returns true if
     * the operation was successful, false if at least one of the file permissions could not be changed.
     * The permissions int should be created using {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION}, {@link #EXECUTE_PERMISSION}
     * and {@link #USER_ACCESS}, {@link #GROUP_ACCESS} and {@link #OTHER_ACCESS} masks combined with logical OR.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #setPermission(int, int, boolean)},
     * for each permission and access (that's a total 9 calls). This may affect performance on filesystems which need
     * to perform an I/O request to change each permission individually. In that case, and if the fileystem allows
     * to change all permissions at once, this method should be overridden.
         *
     * @param permissions the new permissions this file should have
     * @return true if the operation was successful, false if at least one of the file permissions could not be changed
     */
    public boolean setPermissions(int permissions) {
        int bitShift = 0;
        boolean success = true;

        for(int a=OTHER_ACCESS; a<= USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                if(canSetPermission(a, p))
                    success = setPermission(a, p, (permissions & (1<<bitShift))!=0) && success;

                bitShift++;
            }
        }

        return success;
    }


    /**
     * Returns a mask describing the permission bits that the filesystem can read and which can be returned by
     * {@link #getPermission(int, int)} and {@link #getPermissions()}. This allows to determine which permissions are
     * meaningful. 0 is returned if no permission can be read or if the filesystem doesn't have a notion of permissions,
     * 777 if all permissions can be read.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #canGetPermission(int, int)},
     * for each permission and access (that's a total 9 calls). This method should be overridden if a more efficient
     * implementation can be provided. Usually, file permissions support is the same for all files on a filesystem. If
     * that is the case, this method should be overridden to return a static permission mask.
     *
     * @return a bit mask describing the permission bits that the filesystem can read
     */
    public int getPermissionGetMask() {
        int bitShift = 0;
        int permsMask = 0;

        for(int a=OTHER_ACCESS; a<= USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                if(canGetPermission(a, p))
                    permsMask |= (1<<bitShift);

                bitShift++;
            }
        }

        return permsMask;
    }


    /**
     * Returns a mask describing the permission bits that can be changed by the filesystem when calling
     * {@link #setPermission(int, int, boolean)} and {@link #setPermissions(int)}.
     * 0 is returned if no permission can be set or if the filesystem doesn't have a notion of permissions,
     * 777 if all permissions can be set.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #canGetPermission(int, int)},
     * for each permission and access (that's a total 9 calls). This method should be overridden if a more efficient
     * implementation can be provided. Usually, file permissions support is the same for all files on a filesystem. If
     * that is the case, this method should be overridden to return a static permission mask.
     *
     * @return a bit mask describing the permission bits that the filesystem can change
     */
    public int getPermissionSetMask() {
        int bitShift = 0;
        int permsMask = 0;

        for(int a=OTHER_ACCESS; a<= USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                if(canSetPermission(a, p))
                    permsMask |= (1<<bitShift);

                bitShift++;
            }
        }

        return permsMask;
    }



    /**
     * Returns a string representation of this file's permissions.
     *
     * <p>The first character is 'l' if this file is a symbolic link,'d' if it is a directory, '-' otherwise. Then
     * the string contains up to 3 character triplets, for each of the 'user', 'group' and 'other' access types, each
     * containing the following characters:
     * <ul>
     *  <li>'r' if this file has read permission, '-' otherwise
     *  <li>'w' if this file has write permission, '-' otherwise
     *  <li>'x' if this file has executable permission, '-' otherwise
     * </ul>
     *
     * <p>The first character triplet for 'user' access will always be added to the permissions. Then the 'group' and 'other'
     * triplets will only be added if at least one of the user permission bits can be retrieved, as tested with
     * {@link #getPermissionGetMask()}.
     * Here are a couple examples to illustrate:
     * <ul>
     *  <li>a directory for which {@link #getPermissionGetMask()} returns 0 will return the string "d----", no matter
     * what {@link #getPermissions()} returns.
     *  <li>a regular file for which {@link #getPermissionGetMask()} returns 777 (full permissions support) and which
     * has read/write/executable permissions for all three 'user', 'group' and 'other' access types will return "-rwxrwxrwx".
     * </ul>
     */
    public String getPermissionsString() {
        int availPerms = getPermissionGetMask();

        String s = "";
        s += isSymlink()?'l':isDirectory()?'d':'-';

        int perms = getPermissions();

        int bitShift = USER_ACCESS *3;

        // Permissions go by triplets (rwx), there are 3 of them for respectively 'owner', 'group' and 'other' accesses.
        // The first one ('owner') will always be displayed, regardless of the permission bit mask. 'Group' and 'other'
        // will be displayed only if the permission mask contains information about them (at least one permission bit).
        for(int a= USER_ACCESS; a>=OTHER_ACCESS; a--) {

            if(a== USER_ACCESS || (availPerms & (7<<bitShift))!=0) {
                for(int p=READ_PERMISSION; p>=EXECUTE_PERMISSION; p=p>>1) {
                    if((perms & (p<<bitShift))==0)
                        s += '-';
                    else
                        s += p==READ_PERMISSION?'r':p==WRITE_PERMISSION?'w':'x';
                }
            }

            bitShift -= 3;
        }

        return s;
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
     * Returns true if this file has the specified permission enabled for the given access type.
     * If the permission flag for the access type is not supported (use {@link #getPermissionGetMask()} or
     * {@link #canGetPermission(int, int)} to determine that), the return value will be meaningless and therefore
     * should not be taken into account.
     *
     * @param access {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} or {@link #EXECUTE_PERMISSION}
     * @param permission {@link #USER_ACCESS}, {@link #GROUP_ACCESS} or {@link #OTHER_ACCESS}
     * @return true if the file has the specified permission flag enabled for the access type
     */
    public abstract boolean getPermission(int access, int permission);

    /**
     * Changes the specified permission flag for the given access type. If the permission bit in the access type is not
     * supported (use {@link #getPermissionSetMask()} or {@link #canSetPermission(int, int)} to determine that),
     * calling this method will have no effect.
     *
     * @param access {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} or {@link #EXECUTE_PERMISSION}
     * @param permission {@link #USER_ACCESS}, {@link #GROUP_ACCESS} or {@link #OTHER_ACCESS}
     * @return true if the permission flag was successfully set for the access type
     */
    public abstract boolean setPermission(int access, int permission, boolean enabled);

    /**
     * Returns true if this file can retrieve the specified permission flag for the given access type.
     *
     * @param access {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} or {@link #EXECUTE_PERMISSION}
     * @param permission {@link #USER_ACCESS}, {@link #GROUP_ACCESS} or {@link #OTHER_ACCESS}
     * @return true if this file can retrieve the specified permission flag for the given access type
     */
    public abstract boolean canGetPermission(int access, int permission);

    /**
     * Returns true if this file can change the specified permission flag for the given access type.
     *
     * @param access {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} or {@link #EXECUTE_PERMISSION}
     * @param permission {@link #USER_ACCESS}, {@link #GROUP_ACCESS} or {@link #OTHER_ACCESS}
     * @return true if this file can change the specified permission flag for the given access type
     */
    public abstract boolean canSetPermission(int access, int permission);

    /**
     * Returns true if this AbstractFile is a 'regular' directory, not only a 'browsable' file (like an archive file).
     */
    public abstract boolean isDirectory();

    /**
     * Returns true if this file *may* be a symbolic link and thus handled with care.
     */
    public abstract boolean isSymlink();
	
    /**
     * Returns the files contained by this AbstractFile. For this operation to be successful, this file must be
     * 'browsable', i.e. {@link #isBrowsable()} must return <code>true</code>.
     *
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     */
    public abstract AbstractFile[] ls() throws IOException;

    /**
     * Creates a new directory. This method will fail if this AbstractFile is not a folder.
     *
     * @throws IOException if this operation is not possible.
     */
    public abstract void mkdir(String name) throws IOException;

    /**
     * Returns an <code>InputStream</code> to read the contents of this AbstractFile.
     *
     * @throws IOException if this AbstractFile could not be read or if an <code>InputStream</code> could not be
     * provided for any other reason (e.g. file is a directory).
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Returns an <code>InputStream</code> to read the contents of this AbstractFile with random access.
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
        AbstractFile folder = FileFactory.getFile(args[0]);
        folder.ls();

        int nbIter = 100;

        long totalTime = 0;
        long now;
        for(int i=0; i<nbIter; i++) {
            now = System.currentTimeMillis();
            folder.ls();
            totalTime += System.currentTimeMillis()-now;
        }
	
        System.out.println("Average AbstractFile#ls() time = "+totalTime/nbIter);

        totalTime = 0;
        java.io.File ioFolder = new java.io.File(args[0]);
        for(int i=0; i<nbIter; i++) {
            now = System.currentTimeMillis();
            ioFolder.listFiles();
            totalTime += System.currentTimeMillis()-now;
        }

        System.out.println("Average java.io.File#listFiles() time = "+totalTime/nbIter);

        totalTime = 0;
        ioFolder = new java.io.File(args[0]);
        for(int i=0; i<nbIter; i++) {
            now = System.currentTimeMillis();
            String names[] = ioFolder.list();
            for(int j=0; j<names.length; j++)
                new java.io.File(names[j]);
            totalTime += System.currentTimeMillis()-now;
        }

        System.out.println("Average java.io.File#list() time = "+totalTime/nbIter);
    }
}
