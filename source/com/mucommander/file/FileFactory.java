package com.mucommander.file;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.cache.LRUCache;
import com.mucommander.file.filter.ExtensionFilenameFilter;
import com.mucommander.file.filter.FileFilter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 * FileFactory is an abstract class that provides static methods to create {@link AbstractFile} instances
 * and cache the most frequently accessed ones.
 *
 * @see AbstractFile
 * @author Maxence Bernard
 */
public abstract class FileFactory {

    /** Protocol/Constructor map of registered protocols */
    private static Hashtable registeredProtocolConstructors = new Hashtable();

    /** List of registered archive filters */
    private static Vector registeredArchiveFiltersV = new Vector();
    /** List of registered archive constructors */
    private static Vector registeredArchiveConstructorsV = new Vector();

    /** Array of registered archive filters, for quicker access */
    private static FileFilter registeredArchiveFilters[];
    /** Array of registered archive constructors, for quicker access */
    private static Constructor registeredArchiveConstructors[];

    /** Static LRUCache instance that caches frequently accessed AbstractFile instances */
    private static LRUCache fileCache = LRUCache.createInstance(ConfigurationManager.getVariableInt(ConfigurationVariables.FILE_CACHE_CAPACITY, ConfigurationVariables.DEFAULT_FILE_CACHE_CAPACITY));

    /** System temp directory */
    private final static File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));


    static {
        // Register built-in file protocols
        // Local file protocol is hard-wired for performance reasons, no need to add it
        // registerFileProtocol(FSFile.class, FileProtocols.FILE);
        registerFileProtocol(SMBFile.class, FileProtocols.SMB);
        registerFileProtocol(HTTPFile.class, FileProtocols.HTTP);
        registerFileProtocol(HTTPFile.class, FileProtocols.HTTPS);
        registerFileProtocol(FTPFile.class, FileProtocols.FTP);
        registerFileProtocol(SFTPFile.class, FileProtocols.SFTP);
//        registerFileProtocol(WebDAVFile.class, FileProtocols.WEBDAV);
//        registerFileProtocol(WebDAVFile.class, FileProtocols.WEBDAVS);
        
        // Register built-in archive file formats, order for TarArchiveFile and GzipArchiveFile/Bzip2ArchiveFile is important:
        // TarArchiveFile must match 'tar.gz'/'tar.bz2' files before GzipArchiveFile/Bzip2ArchiveFile does.
        registerArchiveFileFormat(ZipArchiveFile.class, new ExtensionFilenameFilter(new String[]{".zip", ".jar", ".pk3", ".pk4", ".war", ".wal", ".wmz", ".xpi", ".ear", ".sar"}));
        registerArchiveFileFormat(TarArchiveFile.class, new ExtensionFilenameFilter(new String[]{".tar", ".tar.gz", ".tgz", ".tar.bz2", ".tbz2"}));
        registerArchiveFileFormat(GzipArchiveFile.class, new ExtensionFilenameFilter(".gz"));
        registerArchiveFileFormat(Bzip2ArchiveFile.class, new ExtensionFilenameFilter(".bz2"));
        registerArchiveFileFormat(IsoArchiveFile.class, new ExtensionFilenameFilter(new String[]{".iso", ".nrg"}));
        registerArchiveFileFormat(ArArchiveFile.class, new ExtensionFilenameFilter(new String[]{".ar", ".a", ".deb"}));
    }


    /**
     * Registers an {@link AbstractFile} Class to be used by getFile() methods to create files for the given file protocol.
     *
     * @param abstractFileClass a Class denoting an AbstractFile class
     * @param protocol the protocol to register the AbstractFile Class for (e.g. "smb")
     * @return <code>true</code> if the protocol was registered without any error, <code>false</code> otherwise
     */
    public static boolean registerFileProtocol(Class abstractFileClass, String protocol) {
        try {
            registeredProtocolConstructors.put(protocol.toLowerCase(), abstractFileClass.getConstructor(new Class[]{FileURL.class}));
            return true;
        }
        catch(NoSuchMethodException e) {
            System.out.println("Error: unable to register protocol "+protocol+" with "+abstractFileClass);
            return false;
        }
    }


    /**
     * Registers an {@link AbstractArchiveFile} Class to be used by the {@link #wrapArchive(AbstractFile)} method
     * (and <code>getFile()</code> methods) to create archive files for files that match the specified file filter.
     *
     * @param abstractArchiveFileClass a Class denoting an AbstractArchiveFile class
     * @param filter a FileFilter that will be used to determine if the file is an archive of the registered format
     * @return <code>true</code> if the protocol was registered without any error, <code>false</code> otherwise
     */
    public static boolean registerArchiveFileFormat(Class abstractArchiveFileClass, FileFilter filter) {
        try {
            Constructor constructor = abstractArchiveFileClass.getConstructor(new Class[]{AbstractFile.class});

            // Constructor could be created, it means the class looks ok so far, register the filter and associated constructor
            registeredArchiveConstructorsV.add(constructor);
            registeredArchiveFiltersV.add(filter);

            int nbArchiveFormats = registeredArchiveConstructorsV.size();

            // Convert vectors to arrays to speed up access a bit as these are very frequently accessed 
            registeredArchiveConstructors = new Constructor[nbArchiveFormats];
            registeredArchiveConstructorsV.toArray(registeredArchiveConstructors);
            
            registeredArchiveFilters = new FileFilter[nbArchiveFormats];
            registeredArchiveFiltersV.toArray(registeredArchiveFilters);

            return true;
        }
        catch(NoSuchMethodException e) {
            System.out.println("Error: unable to register archive file format with class "+abstractArchiveFileClass);
            return false;
        }

    }


    public static Set getRegisteredProtocols() {
        return registeredProtocolConstructors.keySet();
    }


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
     * @throws AuthException if additionnal authentication information is required to create the file
     */
    public static AbstractFile getFile(String absPath, boolean throwException) throws AuthException, IOException {
        try {
            return getFile(absPath, null);
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) {
                com.mucommander.Debug.trace("Caught exception: "+e);
                e.printStackTrace();
            }
            if(throwException)
                throw e;
            return null;
        }
    }

    /**
     * Returns an instance of AbstractFile for the given absolute path and sets the giving parent if not null. AbstractFile subclasses should
     * call this method rather than {@link #getFile(String)} because it is more efficient.
     *
     * @param absPath the absolute path to the file
     * @param parent the returned file's parent
     *
     * @throws java.io.IOException if something went wrong during file or file url creation.
     * @throws AuthException if additionnal authentication information is required to create the file
     */
    protected static AbstractFile getFile(String absPath, AbstractFile parent) throws AuthException, IOException {
        return getFile(URLFactory.getFileURL(absPath, parent==null?null:parent.getURL(), true), parent);
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
     * Creates and returns an instance of AbstractFile for the given FileURL and uses the specified parent file (if any)
     * as the created file's parent.
     *
     * <p>Specifying the file parent if an instance already exists allows to recycle the AbstractFile instance
     * instead of creating a new one when the parent file is requested.
     *
     * @param fileURL the file URL representing the file to be created
     * @param parent the parent AbstractFile to use as the created file's parent, can be <code>null</code>
     *
     * @throws java.io.IOException if something went wrong during file creation.
     */
    public static AbstractFile getFile(FileURL fileURL, AbstractFile parent) throws IOException {
        try {
            String protocol = fileURL.getProtocol().toLowerCase();

            AbstractFile file;

            // Special case for local files, do not use protocol registration mechanism to speed things up a bit
            if(protocol.equals(FileProtocols.FILE)) {
                // Use an LRU file cache to recycle frequently used local file instances.
                String urlRep = fileURL.getStringRep(true);
                file = (AbstractFile)fileCache.get(urlRep);

//                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("file cache hits/misses: "+fileCache.getHitCount()+"/"+fileCache.getMissCount());

                if(file!=null) {
//                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("File cache hit for "+file);

                    // Create an archive file on top of this file if the file matches one of the archive filters
                    return wrapArchive(file);
                }

                // Create local file instance
                file = new FSFile(fileURL);

                // Reuse existing parent file instance if one was specified
                if(parent!=null)
                    file.setParent(parent);

                fileCache.add(urlRep, file);
//                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Added to file cache: "+file);

                // Create an archive file on top of this file if the file matches one of the archive filters
                // This must be done after adding the file to the LRU cache, this could otherwise lead to weird
                // behaviors, for example if a directory with the same filename of a former archive was created,
                // the directory would be considered as an archive
                return wrapArchive(file);
            }
            // For any other file protocol, use registered protocols map
            else {
                // If the specified FileURL doesn't contain any credentials, use CredentialsManager to find
                // any credentials matching the url and use them.
if(Debug.ON) Debug.trace("fileURL.containsCredentials() "+fileURL.containsCredentials());
                if(!fileURL.containsCredentials())
                    CredentialsManager.authenticateImplicit(fileURL);
if(Debug.ON) Debug.trace("credentials="+fileURL.getCredentials());

                // Get a registered Constructor instance for the file protocol
                Constructor constructor = (Constructor)registeredProtocolConstructors.get(protocol);

                // If constructor is null, it means the protocol hasn't been registered properly
                if(constructor==null) {
                    // Todo: localize this string as it can be displayed to the end user
                    throw new IOException("Unknown protocol: "+protocol);
                }

                // May throw InstantiationException, IllegalAccessException, IllegalAccessException, ExceptionInInitializerError, InvocationTargetException
                file = (AbstractFile)constructor.newInstance(new Object[]{fileURL});

                // Reuse existing parent file instance if one was specified
                if(parent!=null)
                    file.setParent(parent);

                // Create an archive file on top of this file if the file matches one of the archive filters
                return wrapArchive(file);
            }
        }
        catch(InvocationTargetException e) {
            // This exception is thrown by Constructor.newInstance() when the target constructor throws an Exception.
            // If the exception was an IOException, throw it instead of a new IOException, as it may contain
            // additional information about the error cause
            Throwable cause = e.getTargetException();
            if(cause instanceof IOException)
                throw (IOException)cause;

            throw new IOException();
        }
        catch(IOException e2) {
            throw e2;
        }
        catch(Exception e3) {
            // InstantiationException, IllegalAccessException, IllegalAccessException
            throw new IOException();
        }
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

            if(lastDotPos==-1)
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
     * Tests based on the given file's extension, if the file corresponds to a registered archive format.
     * If it does, creates the appropriate {@link com.mucommander.file.AbstractArchiveFile} instance on top of the
     * provided file and returns it.
     */
    protected static AbstractFile wrapArchive(AbstractFile file) {
        // Look for an archive format filter that matches the file
        if(!file.isDirectory()) {
            int nbArchiveFormats = registeredArchiveFilters.length;
            for(int i=0; i<nbArchiveFormats; i++) {
                if(registeredArchiveFilters[i].accept(file)) {
                    try {
                        // Found one, create the AbstractArchiveFile instance and return it
                        file = (AbstractFile)registeredArchiveConstructors[i].newInstance(new Object[]{file});
                        break;
                    }
                    catch(Exception e) {
                        if(Debug.ON) Debug.trace("Caught exception while trying to instanciate registered AbstractArchiveFile constructor: "+registeredArchiveConstructors[i]);
                    }
                }
            }
        }

//if(Debug.ON) Debug.trace("file="+file+" class="+file.getClass());
        return file;
    }
}
