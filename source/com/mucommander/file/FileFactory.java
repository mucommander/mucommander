package com.mucommander.file;

import com.mucommander.cache.LRUCache;

import java.io.IOException;
import java.io.File;
import java.util.Random;

/**
 * FileFactory is an abstract class that provides static methods to create {link AbstractFile AbstractFile} instances.
 *
 * @see AbstractFile
 * @author Maxence Bernard
 */
public abstract class FileFactory {

    /** Static LRUCache instance that caches frequently accessed AbstractFile instances */
    private static LRUCache fileCache = LRUCache.createInstance(1000);
    /** Static LRUCache instance that caches frequently accessed FileURL instances */
    private static LRUCache urlCache = LRUCache.createInstance(1000);

    private final static File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));


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
    public static AbstractFile getFile(String absPath) {
        try {
            return getFile(absPath, null);
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught exception: "+e);
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
    public static AbstractFile getFile(String absPath, boolean throwException) throws AuthException, IOException {
        try {
            return getFile(absPath, null);
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught exception: "+e);
            if(throwException)
                throw e;
            return null;
        }
    }

    /**
     * Returns an instance of AbstractFile for the given absolute path and sets the giving parent if not null. AbstractFile subclasses should
     * call this method rather than getFile(String) because it is more efficient.
     *
     * @param absPath the absolute path to the file
     * @param parent the returned file's parent
     *
     * @throws java.io.IOException if something went wrong during file or file url creation.
     */
    protected static AbstractFile getFile(String absPath, AbstractFile parent) throws AuthException, IOException {
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

        return getFile(fileURL, parent);
    }

    /**
     * Returns an instance of AbstractFile for the given FileURL instance.
     *
     * @param fileURL the file URL
     *
     * @return the created file or null if something went wrong during file creation
     */
    public static AbstractFile getFile(FileURL fileURL) {
        try {
            return getFile(fileURL, null);
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught exception: "+e);
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
    public static AbstractFile getFile(FileURL fileURL, boolean throwException) throws IOException {
        try {
            return getFile(fileURL, null);
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught exception: "+e);
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
    public static AbstractFile getFile(FileURL fileURL, AbstractFile parent) throws IOException {
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
            throw new IOException("Unknown protocol "+protocol);

        if(parent!=null)
            file.setParent(parent);

        return wrapArchive(file);
    }


    /**
     * Creates and returns a temporary local file using the desired name.
     *
     * @param desiredName the desired filename for the temporary file. If a file already exists with this name in the
     * temporary directory, the name will be appended of a prefix, but the filename extension will always be preserved.
     * @param deleteOnExit if <code>true</code>, the file will be deleted on normal terminal of the JVM
     * @return the temporary FSFile instance
     */
    public static AbstractFile getTemporaryFile(String desiredName, boolean deleteOnExit) {
        // Attempt to use the desired name
        File tempFile = new File(TEMP_DIRECTORY, desiredName);

        if(tempFile.exists()) {
            // If a file already exists with the same name, append the current time in millisecond and a 5-digit random number
            // to the name part of the filename which pretty much (but not completly) guarantees that a file
            // doesn't already exist with that name. Filename extension is preserved.
            int lastDotPos = desiredName.lastIndexOf('.');
            int len = desiredName.length();
            String nameSuffix = "_"+System.currentTimeMillis()+(new Random().nextInt(10000));

            if(len==-1)
                desiredName += nameSuffix;
            else
                desiredName = desiredName.substring(0, lastDotPos) + nameSuffix + desiredName.substring(lastDotPos, len);

            tempFile = new File(TEMP_DIRECTORY, desiredName);
        }

        if(deleteOnExit)
            tempFile.deleteOnExit();

        return getFile(tempFile.getAbsolutePath());
    }


    /**
     * Tests if based on its extension, the given file corresponds to a supported archive format. If it is, it creates
     * the appropriate {@link com.mucommander.file.AbstractArchiveFile} on top of the provided file and returns it.
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
            else if(ext.equals("ar") || ext.equals("ar") || ext.equals("deb"))
                return new ArArchiveFile(file);
        }

        return file;
    }
}
