package com.mucommander.file;

import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.cache.LRUCache;

import java.io.*;

/**
 * The superclass of all files, all your files are belong to it.
 *
 * @see ProxyFile
 * @author Maxence Bernard
 */
public abstract class AbstractFile {

    /** URL representing this file */
    protected FileURL fileURL;

    /** Default path separator */
    public final static String DEFAULT_SEPARATOR = "/";

    /** Static LRUCache instance that caches frequently accessed AbstractFile instances */
    private static LRUCache fileCache = LRUCache.createInstance(1000);
    /** Static LRUCache instance that caches frequently accessed FileURL instances */
    private static LRUCache urlCache = LRUCache.createInstance(1000);

    /** Indicates copyTo()/moveTo() *should* be used to copy/move the file (e.g. more efficient) */
    public final static int SHOULD_HINT = 0;
    /** Indicates copyTo()/moveTo() *should not* be used to copy/move the file (default) */
    public final static int SHOULD_NOT_HINT = 1;
    /** Indicates copyTo()/moveTo() *must* be used to copy/move the file (e.g. no other way to do so) */
    public final static int MUST_HINT = 2;
    /** Indicates copyTo()/moveTo() *must not* be used to copy/move the file (e.g. not implemented) */
    public final static int MUST_NOT_HINT = 3;

    /** Size allocated to read buffer */
    public final static int READ_BUFFER_SIZE = 8192;

    /** Default buffer size for BufferedOutputStream */
    public final static int WRITE_BUFFER_SIZE = 8192;
	

    /**
     * Creates a new file instance with the given URL.
     */
    protected AbstractFile(FileURL url) {
        this.fileURL = url;
    }
	
    
    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Returns an instance of AbstractFile for the given absolute path.
     * 
     * <p>This method does not throw any IOException but returns <code>null</code> if the file could not be created.</p>
     *
     * @param absPath the absolute path to the file
     *
     * @return <code>null</code> if the given path is not absolute or incorrect (doesn't correspond to any file) or 
     * if something went wrong during file creation.
     */
    public static AbstractFile getAbstractFile(String absPath) {
        try {
            return getAbstractFile(absPath, null);
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns an instance of AbstractFile for the given absolute path.
     * 
     * <p>This method does not throw any IOException but returns <code>null</code> if the file could not be created.</p>
     *
     * @param absPath the absolute path to the file
     * @param throwException if set to <code>true</code>, an IOException will be thrown if something went wrong during file creation
     *
     * @return <code>null</code> if the given path is not absolute or incorrect (doesn't correspond to any file) 
     * @throws java.io.IOException  and throwException param was set to <code>true</code>.
     */
    public static AbstractFile getAbstractFile(String absPath, boolean throwException) throws AuthException, IOException {
        try {
            return getAbstractFile(absPath, null);
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) e.printStackTrace();
            if(throwException)
                throw e;
            return null;
        }
    }

	
    /**
     * Returns an instance of AbstractFile for the given absolute path and sets the giving parent if not null. AbstractFile subclasses should
     * call this method rather than getAbstractFile(String) because it is more efficient.
     *
     * @param absPath the absolute path to the file
     * @param parent the returned file's parent
     *
     * @throws java.io.IOException if something went wrong during file or file url creation.
     */
    protected static AbstractFile getAbstractFile(String absPath, AbstractFile parent) throws AuthException, IOException {
        // Create a FileURL instance using the given path
        FileURL fileURL;

        // If path contains no protocol, consider the file as a local file and add the 'file' protocol to the URL.
        // Frequently used local FileURL instances are cached for performance  
        if(absPath.indexOf("://")==-1) {
            // Try to find a cached FileURL instance
            fileURL = (FileURL)urlCache.get(absPath);
            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace((fileURL==null?"Adding to FileURL cache:":"FileURL cache hit: ")+absPath);
            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("url cache hits/misses: "+urlCache.getHitCount()+"/"+urlCache.getMissCount());

            // FileURL not in cache, let's create it and add it to the cache
            if(fileURL==null) {
                // A MalformedURLException will be thrown if the path is not absolute
                fileURL = FileURL.getLocalFileURL(absPath, parent==null?null:parent.getURL());	// Reuse parent file's FileURL (if any)
                urlCache.add(absPath, fileURL);
            }
        }
        else {
            // FileURL cache is not used for now as FileURL are mutable (setLogin, setPassword, setPort) and it
            // may cause some weird side effects
            fileURL = new FileURL(absPath, parent==null?null:parent.getURL());		// Reuse parent file's FileURL (if any)
        }
		
        return getAbstractFile(fileURL, parent);
    }
	

    /**
     * Returns an instance of AbstractFile for the given FileURL instance.
     *
     * @param fileURL the file URL
     *
     * @return the created file or null if something went wrong during file creation 
     */
    public static AbstractFile getAbstractFile(FileURL fileURL) {
        try {
            return getAbstractFile(fileURL, null);
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns an instance of AbstractFile for the given FileURL instance.
     *
     * @param fileURL the file URL
     * @param throwException if set to <code>true</code>, an IOException will be thrown if something went wrong during file creation
     *
     * @return the created file
     * @throws java.io.IOException if something went wrong during file creation
     */
    public static AbstractFile getAbstractFile(FileURL fileURL, boolean throwException) throws IOException {
        try {
            return getAbstractFile(fileURL, null);
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) e.printStackTrace();
            if(throwException)
                throw e;
            return null;
        }
    }


    /**
     * Returns an instance of AbstractFile for the given FileURL instance and sets the giving parent.
     *
     * @param fileURL the file URL
     * @param parent the returned file's parent
     *
     * @throws java.io.IOException if something went wrong during file creation.
     */
    public static AbstractFile getAbstractFile(FileURL fileURL, AbstractFile parent) throws IOException {
        String protocol = fileURL.getProtocol().toLowerCase();
		
        AbstractFile file;

        // FS file (local filesystem) : an LRU file cache is used to recycle frequently used file instances
        if (protocol.equals("file")) {
            String urlRep = fileURL.getStringRep(true);
            file = (AbstractFile)fileCache.get(urlRep);
            //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace((file==null?"Adding to file cache:":"File cache hit: ")+urlRep);
            //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("file cache hits/misses: "+fileCache.getHitCount()+"/"+fileCache.getMissCount());

            if(file==null) {
                file = new FSFile(fileURL);
                fileCache.add(urlRep, file);
            }
        }
        // SMB file
        else if (protocol.equals("smb"))
            file = new SMBFile(fileURL);
        // HTTP/HTTPS file
        else if (protocol.equals("http") || protocol.equals("https"))
            file = new HTTPFile(fileURL);
        // FTP file
        else if (protocol.equals("ftp"))
            file = new FTPFile(fileURL);
        // SFTP file
        else if (protocol.equals("sftp"))
            file = new SFTPFile(fileURL);
        // WebDAV file
//        else if (protocol.equals("webdav") || protocol.equals("webdavs"))
//            file = new WebDAVFile(fileURL);
        else
            throw new IOException("Unkown protocol "+protocol);

        if(parent!=null)
            file.setParent(parent);
	
        return wrapArchive(file);
    }

	
    /**
     * Tests if based on its extension, the given file corresponds to a supported archive format. If it is, it creates
     * the appropriate {@link AbstractArchiveFile} on top of the provided file and returns it.
     */
    protected static AbstractFile wrapArchive(AbstractFile file) {
        if(!file.isDirectory()) {
            String ext = file.getExtension();
            if(ext==null)
                return file;
				
            ext = ext.toLowerCase();
            String nameLC = file.getName().toLowerCase();

            if(ext.equals("zip") || ext.equals("jar"))
                return new ZipArchiveFile(file);
            else if(ext.equals("tar") || ext.equals("tgz") || nameLC.endsWith(".tar.gz") || ext.equals("tbz2") || nameLC.endsWith(".tar.bz2"))
                return new TarArchiveFile(file);
            else if(ext.equals("gz"))
                return new GzipArchiveFile(file);
            else if(ext.equals("bz2"))
                return new Bzip2ArchiveFile(file);
            else if(ext.equals("iso") || ext.equals("nrg"))
                return new IsoArchiveFile(file);
        }

        return file;		
    }


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
     * <p>This method should be overridden if a special treatment (e.g. URL-decoding)
     * needs to be applied to the returned filename.
     */
    public String getName() {
        return fileURL.getFilename();        
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

        // If the extension 'dot' eiter:
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
     * Returns the file's extension, <code>null</code> if the file doesn't have an extension.
     */
    public String getExtension() {
        String name = getName();
        int lastDotPos = name.lastIndexOf('.');

        int len;
        if(lastDotPos==-1 || lastDotPos==(len=name.length())-1)
            return null;
	
        return name.substring(lastDotPos+1, len);
    }


    /**
     * Returns the absolute path of this AbstractFile. The returned path will be free of any login and password and thus
     * can be safely displayed or stored.
     */
    public String getAbsolutePath() {
        return getURL().getStringRep(false);
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
     * AbstractFile's implementation simply returns the absolute path, this method should be overridden if canonical path resolution is available.
     */
    public String getCanonicalPath() {
        return getAbsolutePath();
    }


    /**
     * Returns the canonical path of this AbstractFile, resolving any symbolic links or '..' and '.' occurrences,
     * and with a separator character if <code>true</code> is passed or without one if <code>false</code> is passed.
     * <p>AbstractFile's implementation simply returns the absolute path, this method should be overridden if canonical path resolution is available.</p>
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
     * Returns <code>true</code> if this file is a parent of the given file, or if the 2 files
     * have the same path.
     */
    public boolean isParentOf(AbstractFile file) {
        return getCanonicalPath(false).startsWith(file.getCanonicalPath(false));
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
     * @throws IOException if something went wrong while reading from or writing to one of the provided streams
     */
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        // Init read buffer
        byte buffer[] = new byte[READ_BUFFER_SIZE];

        // Copies the InputStream's content to the OutputStream chunks by chunks
        int read;
        while ((read=in.read(buffer, 0, buffer.length))!=-1)
            out.write(buffer, 0, read);
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
     * @throws IOException if something went wrong while reading from the InputStream or writing to this file
     */
    public void copyStream(InputStream in, boolean append) throws IOException {
        // Create a BufferedOutputStream to speed up the output
        OutputStream out = new BufferedOutputStream(getOutputStream(append), WRITE_BUFFER_SIZE); 

        try {
            copyStream(in, out);
        }
        finally {
            // Close stream even if copyStream() threw an IOException
            if(out!=null)
                out.close();
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
     * @throws IOException if this AbstractFile could be read, or the destination could be written, or if 
     * the operation failed for any other reason.
     */
    public void copyTo(AbstractFile destFile) throws IOException {
        InputStream in = getInputStream();

        try {
            destFile.copyStream(in, false);
        }
        finally {
            // Close stream even if copyStream() threw an IOException
            if(in!=null)
                in.close();
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
     * @see constants
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
     * @throws IOException if this AbstractFile or destination cannot be written or if the operation failed
     *  for any other reason.
     */
    public void moveTo(AbstractFile destFile) throws IOException {
        try {
            copyTo(destFile);
            // The file won't be deleted if copyTo() failed (threw an IOException);
            delete();
        }
        catch(IOException e) {
            // Rethrow exception
            throw e;
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
     * @see constants
     */
    public int getMoveToHint(AbstractFile destFile) {
        return fileURL.getProtocol().equals(destFile.fileURL.getProtocol())
            && fileURL.getHost().equals(destFile.fileURL.getHost())
        ? SHOULD_HINT : SHOULD_NOT_HINT;
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
     * <p>Tests a file for equality: returns <code>true</code> if the given file denotes the same
     * file or directory. Note that two files can be equal and not have the exact same absolute
     * path.</p>
     *
     * <p>This method should be overriden as it only compares the absolute path.</p>
     */
    public boolean equals(Object f) {
        if(f==null || !(f instanceof AbstractFile))
            return false;
		
        return getAbsolutePath().equals(((AbstractFile)f).getAbsolutePath());
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
     * @throws IOException if this operation is not possible.
     */
    public abstract void mkdir(String name) throws IOException;

    /**
     * Returns an InputStream to read from this AbstractFile.
     * @throws IOException if this AbstractFile cannot be read or is a folder.
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Returns an OuputStream to write to this AbstractFile.
     * @param append if true, data written to the OutputStream will be appended to the end of this file. If false, any existing data will be overwritten.
     * @throws IOException if this operation is not permitted or if this AbstractFile 
     * is a folder.
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


    //////////////////
    // Test methods //
    //////////////////

    /**
     * Simple bench method.
     */
    public static void main(String args[]) throws IOException {
        AbstractFile folder = AbstractFile.getAbstractFile("/usr/bin/", null);
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
