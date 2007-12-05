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

package com.mucommander.file;

import com.mucommander.Debug;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.cache.LRUCache;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.filter.ExtensionFilenameFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.icon.FileIconProvider;
import com.mucommander.file.icon.impl.SwingFileIconProvider;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.file.util.FileToolkit;
import com.mucommander.file.util.PathTokenizer;
import com.mucommander.util.Enumerator;

import java.io.IOException;
import java.util.*;

/**
 * FileFactory is an abstract class that provides static methods to get a {@link AbstractFile} instance for
 * a specified path or {@link FileURL} location.
 * <h3>Protocols</h3>
 * <p>
 * In order to allow the <code>com.mucommander.file</code> API to access new file protocols, developers must create
 * an implementation of {@link AbstractFile} that handles that protocol and register it to <code>FileFactory</code>.
 * This registration requires an implementation of {@link ProtocolProvider}, an instance of which will be passed to
 * {@link #registerProtocol(String,ProtocolProvider) registerProtocol}.
 * </p>
 * <p>
 * Built-in file protocols are:
 * <ul>
 *   <li>{@link FileProtocols#FILE Local} files.</li>
 *   <li>{@link FileProtocols#FTP FTP}.</li>
 *   <li>{@link FileProtocols#SFTP SFTP}.</li>
 *   <li>{@link FileProtocols#HTTP HTTP}.</li>
 *   <li>{@link FileProtocols#HTTPS HTTPS}.</li>
 *   <li>{@link FileProtocols#NFS NFS}.</li>
 *   <li>{@link FileProtocols#SMB SMB}.</li>
 *   <li>{@link FileProtocols#BOOKMARKS Bookmarks}.</li>
 * </ul>
 * </p>
 * <h3>Archive formats</h3>
 * <p>
 * In order to allow the <code>com.mucommander.file</code> API to access new archive formats, developers must create
 * an implementation of {@link AbstractArchiveFile} that handles that format and register it to <code>FileFactory</code>.
 * This registration requires an implementation of {@link ArchiveFormatProvider}, an instance of which will be passed to
 * {@link #registerArchiveFormat(ArchiveFormatProvider,com.mucommander.file.filter.FilenameFilter) registerArchiveFormat}.
 * </p>
 * <p>
 * Built-in file file formats are:
 * <ul>
 *   <li><code>ZIP</code>, registered to zip, jar, war, wal, wmz, xpi, ear, odt, ods and odp files.</li>
 *   <li><code>TAR</code>, registered to tar, tar.gz, tgz, tar.bz2 and tbz2 files.</li>
 *   <li><code>GZIP</code>, registered to gz files.</li>
 *   <li><code>BZip2</code>, registered to bz2 files.</li>
 *   <li><code>ISO</code>, registered to iso and nrg files.</li>
 *   <li><code>AR</code>, registered to ar, a and deb files.</li>
 *   <li><code>LST</code>, registered to lst files.</li>
 * </ul>
 * </p>
 * <h3>Trash</h3>
 * <p>
 * <code>FileFactory</code> also provides support for {@link AbstractTrash} registration.
 * Built-in implementations are:
 * <ul>
 *   <li>{@link com.mucommander.file.impl.trash.OSXTrashProvider OS X} trash.</li>
 *   <li>{@link com.mucommander.file.impl.trash.KDETrashProvider KDE} trash.</li>
 * </ul>
 * Note that <code<FileFactory</code> does not automatically register a trash provider, and application
 * writers must do so themselves depending on their own needs.
 * </p>
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class FileFactory {

    /** All registered protocol providers. */
    private static Hashtable protocolProviders = new Hashtable();

    /** Vector of registered ArchiveFormatMapping instances */
    private static Vector archiveFormatMappingsV = new Vector();

    /** Array of registered FileProtocolMapping instances, for quicker access */
    private static ArchiveFormatMapping[] archiveFormatMappings;

    /** Object used to create instances of {@link AbstractTrash}. */
    private static TrashProvider trashProvider;

    /** Used to synchronise access to the trash provider. */
    private final static Object trashLock = new Object();

    /** Static LRUCache instance that caches frequently accessed AbstractFile instances */
    private static LRUCache fileCache = LRUCache.createInstance(MuConfiguration.getVariable(MuConfiguration.FILE_CACHE_CAPACITY,
                                                                                            MuConfiguration.DEFAULT_FILE_CACHE_CAPACITY));

    private static WeakHashMap archiveFileCache = new WeakHashMap();

    /** System temp directory */
    private final static AbstractFile TEMP_DIRECTORY;

    /** Default file icon provider, initialized in static block */
    private static FileIconProvider defaultFileIconProvider;



    static {
        ProtocolProvider protocolProvider; // Buffer for protocols that use the same provider.

        // Register built-in file protocols.
        // Local file protocol is hard-wired for performance reasons, no need to add it.
        registerProtocol(FileProtocols.SMB,       new com.mucommander.file.impl.smb.SMBProtocolProvider());
        registerProtocol(FileProtocols.HTTP,      protocolProvider = new com.mucommander.file.impl.http.HTTPProtocolProvider());
        registerProtocol(FileProtocols.HTTPS,     protocolProvider);
        registerProtocol(FileProtocols.FTP,       new com.mucommander.file.impl.ftp.FTPProtocolProvider());
        registerProtocol(FileProtocols.SFTP,      new com.mucommander.file.impl.sftp.SFTPProtocolProvider());
        registerProtocol(FileProtocols.NFS,       new com.mucommander.file.impl.nfs.NFSProtocolProvider());
        registerProtocol(FileProtocols.BOOKMARKS, new com.mucommander.file.impl.bookmark.BookmarkProtocolProvider());
//        registerProtocol(FileProtocols.S3,        new com.mucommander.file.impl.s3.S3Provider());

        // Register built-in archive file formats, order for TarArchiveFile and GzipArchiveFile/Bzip2ArchiveFile is important:
        // TarArchiveFile must match 'tar.gz'/'tar.bz2' files before GzipArchiveFile/Bzip2ArchiveFile does.
        registerArchiveFormat(new com.mucommander.file.impl.zip.ZipFormatProvider(),     new ExtensionFilenameFilter(new String[] {".zip", ".jar", ".war", ".wal", ".wmz",
                                                                                                                                   ".xpi", ".ear", ".sar", ".odt", ".ods",
                                                                                                                                   ".odp", ".odg", ".odf"}));
        registerArchiveFormat(new com.mucommander.file.impl.tar.TarFormatProvider(),     new ExtensionFilenameFilter(new String[] {".tar", ".tar.gz", ".tgz",
                                                                                                                                   ".tar.bz2", ".tbz2"}));
        registerArchiveFormat(new com.mucommander.file.impl.gzip.GzipFormatProvider(),   new ExtensionFilenameFilter(".gz"));
        registerArchiveFormat(new com.mucommander.file.impl.bzip2.Bzip2FormatProvider(), new ExtensionFilenameFilter(".bz2"));
        registerArchiveFormat(new com.mucommander.file.impl.iso.IsoFormatProvider(),     new ExtensionFilenameFilter(new String[] {".iso", ".nrg"}));
        registerArchiveFormat(new com.mucommander.file.impl.ar.ArFormatProvider(),       new ExtensionFilenameFilter(new String[] {".ar", ".a", ".deb"}));
        registerArchiveFormat(new com.mucommander.file.impl.lst.LstFormatProvider(),     new ExtensionFilenameFilter(".lst"));

        // Set the default FileIconProvider instance
//        if(PlatformManager.getOsFamily()==PlatformManager.MAC_OS_X)
//            defaultFileIconProvider = new CocoaFileIconProvider();
//        else
        defaultFileIconProvider = new SwingFileIconProvider();

        // Create the temp directory folder
        TEMP_DIRECTORY = getFile(System.getProperty("java.io.tmpdir"));
    }


    /**
     * Makes sure no instance of <code>FileFactory</code> is created.
     */
    private FileFactory() {}


    /**
     * Returns an instance of the {@link AbstractTrash} implementation that can be used on the current platform,
     * or <code>null</code if none is available.
     *
     * @return an instance of the AbstractTrash implementation that can be used on the current platform, or null if
     * none is available. 
     */
    public static AbstractTrash getTrash() {
        TrashProvider provider;

        if((provider = getTrashProvider()) == null)
            return null;
        return provider.getTrash();
    }

    /**
     * Returns the object used to create instances of {@link AbstractTrash}.
     * @return the object used to create instances of {@link AbstractTrash} if any, <code>null</code> otherwise.
     */
    public static TrashProvider getTrashProvider() {
        synchronized(trashLock) {
            return trashProvider;
        }
    }

    /**
     * Sets the object that will be used to create instances of {@link AbstractTrash}.
     * @param  provider object that will be used to create instances of {@link AbstractTrash}.
     * @return          the previous trash provider if any, <code>null</code> otherwise.
     */
    public static TrashProvider setTrashProvider(TrashProvider provider) {
        TrashProvider buffer;

        synchronized(trashLock) {
            buffer = trashProvider;
            trashProvider = provider;
            return buffer;
        }
    }

    /**
     * Registers a new protocol.
     * <p>
     * If a {@link ProtocolProvider} was already registered to the specified protocol, it will automatically be
     * unregistered.
     * </p>
     * <p>
     * The <code>protocol</code> argument is expected to be the protocol identifier without trailing <code>://</code>.
     * For example, the identifier of the HTTP protocol would be <code>http</code>. This parameter's case is irrelevant,
     * as it will be stored in all lower-case.
     * </p>
     * <p>
     * After this call, the various {@link #getFile(String) getFile} methods will be able to resolve files using the
     * specified protocol.
     * </p>
     * <p>
     * Built-in file protocols are listed in {@link FileProtocols}.
     * </p>
     *
     * @param  protocol identifier of the protocol to register.
     * @param  provider object used to create instances of files using the specified protocol.
     * @return          the previously registered protocol provider if any, <code>null</code> otherwise.
     */
    public static ProtocolProvider registerProtocol(String protocol, ProtocolProvider provider) {
        return (ProtocolProvider)protocolProviders.put(protocol.toLowerCase(), provider);
    }

    /**
     * Unregisters the provider associated with the specified protocol.
     *
     * @param  protocol identifier of the protocol whose provider should be unregistered.
     * @return          the provider that has been unregistered, or <code>null</code> if none.
     */
    public static ProtocolProvider unregisterProtocol(String protocol) {
        return (ProtocolProvider)protocolProviders.remove(protocol);
    }

    /**
     * Returns the protocol provider registered to the specified protocol identifer.
     *
     * @param  protocol identifier of the protocol whose provider should be retrieved.
     * @return          the protocol provider registered to the specified protocol identifer, or <code>null</code> if none.
     */
    public static ProtocolProvider getProtocolProvider(String protocol) {
        return (ProtocolProvider)protocolProviders.get(protocol.toLowerCase());
    }

    /**
     * Returns an iterator on all known protocol names.
     * <code>
     * All objects returned by the iterator's <code>nextElement()</code> method will be instanced of string. These can then
     * be passed to {@link #getProtocolProvider(String) getProtocolProvider} to retrieve the associated {@link ProtocolProvider}.
     * </code>
     *
     * @return an iterator on all known protocol names.
     */
    public static Iterator protocols() {
        return new Enumerator(protocolProviders.keys());
    }


    /**
     * Registers a new archive format.
     */
    public static void registerArchiveFormat(ArchiveFormatMapping mapping) {
            archiveFormatMappingsV.add(mapping);
            updateArchiveFormatMappingsArray();
    }

    public static void registerArchiveFormat(ArchiveFormatProvider provider, FilenameFilter filter) {
        registerArchiveFormat(new ArchiveFormatMapping(provider, filter));
    }

    /**
     * Removes any archive format that might have been registered to the specified extension.
     */
    public static void unregisterArchiveFileFormat(ArchiveFormatMapping mapping) {
        int index = archiveFormatMappingsV.indexOf(mapping);

        if(index!=-1) {
            archiveFormatMappingsV.removeElementAt(index);
            updateArchiveFormatMappingsArray();
        }
    }

    /**
     * Updates the ArchiveFileFormat array to reflect the contents of the ArchiveFileFormat Vector.
     */
    private static void updateArchiveFormatMappingsArray() {
        archiveFormatMappings = new ArchiveFormatMapping[archiveFormatMappingsV.size()];
        archiveFormatMappingsV.toArray(archiveFormatMappings);
    }

    public static ArchiveFormatProvider getArchiveFormatProvider(String name) {
        if(name == null)
            return null;
        for(int i = 0; i < archiveFormatMappings.length; i++)
            if(archiveFormatMappings[i].filter.accept(name))
                return archiveFormatMappings[i].provider;
        return null;
    }

    /**
     * Returns an iterator on all known archive formats.
     *
     * @return an iterator on all known archive formats.
     */
    public static Iterator archiveFormats() {return archiveFormatMappingsV.iterator();}



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
        try {return getFile(absPath, null);}
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
        try {return getFile(absPath, null);}
        catch(IOException e) {
            if(com.mucommander.Debug.ON) {
                com.mucommander.Debug.trace("Caught exception: "+e);
            }
            if(throwException)
                throw e;
            return null;
        }
    }

    /**
     * Returns an instance of AbstractFile for the given absolute path and use the given parent for the new file if
     * not null. AbstractFile subclasses should as much as possible call this method rather than {@link #getFile(String)} 
     * because it is more efficient.
     *
     * @param absPath the absolute path to the file
     * @param parent the returned file's parent
     *
     * @throws java.io.IOException if something went wrong during file or file url creation.
     * @throws AuthException if additionnal authentication information is required to create the file
     */
    public static AbstractFile getFile(String absPath, AbstractFile parent) throws AuthException, IOException {return getFile(new FileURL(absPath), parent);}

    /**
     * Returns an instance of AbstractFile for the given FileURL instance.
     *
     * @param fileURL the file URL
     *
     * @return the created file or null if something went wrong during file creation
     */
    public static AbstractFile getFile(FileURL fileURL) {
        try {return getFile(fileURL, null);}
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
        try {return getFile(fileURL, null);}
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

        PathTokenizer pt = new PathTokenizer(fileURL.getPath(),
                fileURL.getPathSeparator(),
                false);

        AbstractFile currentFile = null;
        boolean lastFileResolved = false;

        // Extract every filename from the path from left to right and for each of them, see if it looks like an archive.
        // If it does, create the appropriate protocol file and wrap it into an archive file.
        while(pt.hasMoreFilenames()) {

            // Test if the filename's extension looks like a supported archive format...
            // Note that the archive can also be a directory with an archive extension
            if(isArchiveFilename(pt.nextFilename())) {
                // Remove trailing separator of file, some file protocols such as SFTP don't like trailing separators.
                // On the contrary, directories without a trailing slash are fine.
                String currentPath = FileToolkit.removeTrailingSeparator(pt.getCurrentPath());

                // Test if current file is an archive file and if it is, create an archive entry file instead of a raw
                // protocol file
                if(currentFile==null || !(currentFile instanceof AbstractArchiveFile)) {
                    // Create a fresh FileURL with the current path
                    FileURL clonedURL = (FileURL)fileURL.clone();
                    clonedURL.setPath(currentPath);
                    currentFile = wrapArchive(createRawFile(clonedURL));

                    lastFileResolved = true;
                }
                else {          // currentFile is an AbstractArchiveFile
                    // Note: wrapArchive() is already called by AbstractArchiveFile#createArchiveEntryFile()
                    AbstractFile tempEntryFile = ((AbstractArchiveFile)currentFile).getArchiveEntryFile(FileToolkit.removeLeadingSeparator(currentPath.substring(currentFile.getURL().getPath().length(), currentPath.length())));
                    if(tempEntryFile instanceof AbstractArchiveFile) {
                        currentFile = tempEntryFile;
                        lastFileResolved = true;
                    }
                    else {
                        lastFileResolved = false;
                    }
                }
            }
            else {
                lastFileResolved = false;
            }
        }

        // Create last file if it hasn't been already (if the last filename was not an archive), same routine as above
        // except that it doesn't wrap the file into an archive file
        if(!lastFileResolved) {
            String currentPath = pt.getCurrentPath();

            if(currentFile==null || !(currentFile instanceof AbstractArchiveFile)) {
                FileURL clonedURL = (FileURL)fileURL.clone();
                clonedURL.setPath(currentPath);
                currentFile = createRawFile(clonedURL);
            }
            else {          // currentFile is an AbstractArchiveFile
                currentFile = ((AbstractArchiveFile)currentFile).getArchiveEntryFile(FileToolkit.removeLeadingSeparator(currentPath.substring(currentFile.getURL().getPath().length(), currentPath.length())));
            }
        }

        // Reuse existing parent file instance if one was specified
        if(parent!=null)
            currentFile.setParent(parent);

        return currentFile;
    }


    private static AbstractFile createRawFile(FileURL fileURL) throws IOException {
        String protocol = fileURL.getProtocol().toLowerCase();

        // Cache file instances only for certain protocols
        boolean useFileCache = protocol.equals(FileProtocols.FILE)
                || protocol.equals(FileProtocols.SMB)
                || protocol.equals(FileProtocols.SFTP);

        // This value is used twice, only if file caching is used
        String urlRep = useFileCache?fileURL.toString(true):null;

        AbstractFile file;

        if(useFileCache) {
            // Lookup the cache for an existing AbstractFile instance
            file = (AbstractFile)fileCache.get(urlRep);
//            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("file cache hits/misses: "+fileCache.getHitCount()+"/"+fileCache.getMissCount());

            if(file!=null)
                return file;
        }

        // Special case for local files, do not use protocol registration mechanism to speed things up a bit
        // (saves a hashtable lookup)
        if(protocol.equals(FileProtocols.FILE)) {
            file = new LocalFile(fileURL);
            // Uncomment this line and comment the previous one to simulate a slow filesystem
            //file = new DebugFile(new LocalFile(fileURL), 0, 50);
        }
        // Use the protocol map for any other file protocol
        else {
            // If the specified FileURL doesn't contain any credentials, use CredentialsManager to find
            // any credentials matching the url and use them.
//            if(Debug.ON) Debug.trace("fileURL.containsCredentials() "+fileURL.containsCredentials());
            if(!fileURL.containsCredentials())
                CredentialsManager.authenticateImplicit(fileURL);
//            if(Debug.ON) Debug.trace("credentials="+fileURL.getCredentials());

            // Finds the right file protocol provider
            ProtocolProvider provider;
            if((provider = getProtocolProvider(protocol)) == null)
                throw new IOException("Unknown file protocol: " + protocol);
            file = provider.getFile(fileURL);
        }

        if(useFileCache) {
            // Note: Creating an archive file on top of the file must be done after adding the file to the LRU cache,
            // this could otherwise lead to weird behaviors, for example if a directory with the same filename
            // of a former archive was created, the directory would be considered as an archive
            fileCache.add(urlRep, file);
//                            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Added to file cache: "+file);
        }

        return file;
    }

    /**
     * Returns a variation of the given filename, appending a pseudo-unique ID to the filename's prefix while keeping
     * the same filename extension.
     */
    private static String getFilenameVariation(String filename) {
        int lastDotPos = filename.lastIndexOf('.');
        int len = filename.length();
        String nameSuffix = "_"+System.currentTimeMillis()+(new Random().nextInt(10000));

        if(lastDotPos==-1)
            filename += nameSuffix;
        else
            filename = filename.substring(0, lastDotPos) + nameSuffix + filename.substring(lastDotPos, len);

        return filename;
    }

    /**
     * Creates and returns a temporary local file using the desired filename. If a file with this name already exists
     * in the temp directory, the filename's prefix (name without extension) will be appended an ID. The filename's
     * extension will however always be preserved.
     *
     * <p>The returned file may be a {@link LocalFile} or a {@link AbstractArchiveFile} if the extension corresponds
     * to a registered archive format.</p>
     *
     * @param desiredFilename the desired filename for the temporary file. If a file with this name already exists
     * in the temp directory, the filename's prefix (name without extension) will be appended an ID, but the filename's
     * extension will always be preserved.
     * @param deleteOnExit if <code>true</code>, the temporary file will be deleted upon normal termination of the JVM
     * @return the temporary file, may be a LocalFile or an AbstractArchiveFile if the filename's extension corresponds
     * to a registered archive format.
     * @throws IOException if an error occurred while instanciating the temporary file. This should not happen under
     * normal circumstances.
     */
    public static AbstractFile getTemporaryFile(String desiredFilename, boolean deleteOnExit) throws IOException {
        if(desiredFilename==null || desiredFilename.equals(""))
            desiredFilename = "temp";
        
        // Attempt to use the desired name
        AbstractFile tempFile = TEMP_DIRECTORY.getDirectChild(desiredFilename);

        if(tempFile.exists())
            tempFile = TEMP_DIRECTORY.getDirectChild(getFilenameVariation(desiredFilename));

        if(deleteOnExit)
            ((java.io.File)tempFile.getUnderlyingFileObject()).deleteOnExit();

        return tempFile;
    }

    /**
     * Convenience method that creates a temporary file with a default 'desired name'. Yield the same result as calling
     * {@link #getTemporaryFile(String, boolean)} with <code>null</code>
     *
     * @param deleteOnExit if <code>true</code>, the temporary file will be deleted upon normal termination of the JVM
     * @return the temporary file, may be a LocalFile or an AbstractArchiveFile if the filename's extension corresponds
     * to a registered archive format.
     * @throws IOException if an error occurred while instanciating the temporary file. This should not happen under
     * normal circumstances.
     */
    public static AbstractFile getTemporaryFile(boolean deleteOnExit) throws IOException {
        return getTemporaryFile(null, deleteOnExit);
    }

    /**
     * Returns the temporary folder, i.e. the folder where the parent folder of temporary files returned by
     * {@link #getTemporaryFile(String, boolean)}.
     *
     * @return the temporary folder
     */
    public static AbstractFile getTemporaryFolder() {
        return TEMP_DIRECTORY;
    }


    /**
     * Returns true if the given filename's extension matches one of the registered archive formats.
     *
     * @param filename the filename to test
     */
    public static boolean isArchiveFilename(String filename) {return getArchiveFormatProvider(filename) != null;}

    /**
     * Tests based on the given file's extension, if the file corresponds to a registered archive format.
     * If it does, an appropriate {@link AbstractArchiveFile} instance is created on top of the provided file
     * and returned. If it doesn't (the file's extension doesn't correspond to a registered archive format or is a
     * directory), the provided <code>AbstractFile</code> instance is returned.
     */
    public static AbstractFile wrapArchive(AbstractFile file) throws IOException {
        String filename = file.getName();

        // Looks for an archive FilenameFilter that matches the given filename.
        // Comparing the filename against each and every archive extension has a cost, so we only perform the test if
        // the filename contains a dot '.' character, since most of the time this method is called with a filename that
        // doesn't match any of the filters.
        if(!file.isDirectory() && filename.indexOf('.')!=-1) {
            AbstractFile archiveFile;

            // Do not use cache for archive entries
            boolean useCache = !(file instanceof ArchiveEntryFile);

            if(useCache) {
                archiveFile = (AbstractFile)archiveFileCache.get(file.getAbsolutePath());
                if(archiveFile!=null) {
//                    if(Debug.ON) Debug.trace("Found cached archive file for: "+file.getAbsolutePath());
                    return archiveFile;
                }

//                if(Debug.ON) Debug.trace("No cached archive file found for: "+file.getAbsolutePath());
            }

            ArchiveFormatProvider provider;
            if((provider = getArchiveFormatProvider(filename)) != null) {
                archiveFile = provider.getFile(file);
                if(useCache) {
                    if(Debug.ON) Debug.trace("Adding archive file to cache: "+file.getAbsolutePath());
                    archiveFileCache.put(file.getAbsolutePath(), archiveFile);
                }
                return archiveFile;
            }
        }

        return file;
    }


    /**
     * Returns the default {@link com.mucommander.file.icon.FileIconProvider} instance. The default provider class
     * (before {@link #setDefaultFileIconProvider(com.mucommander.file.icon.FileIconProvider)} is called) is
     * platform-dependent and as such may vary across platforms.
     *
     * <p>It is noteworthy that the provider returned by this method is used by {@link com.mucommander.file.AbstractFile#getIcon()}
     * to create and return the icon.</p>
     *
     * @return the default FileIconProvider implementation
     */
    public static FileIconProvider getDefaultFileIconProvider() {
        return defaultFileIconProvider;
    }

    /**
     * Sets the default {@link com.mucommander.file.icon.FileIconProvider} implementation.
     *
     * <p>It is noteworthy that the provider returned by this method is used by {@link com.mucommander.file.AbstractFile#getIcon()}
      * to create and return the icon.</p>
      *
     * @param fip the new value for the default FileIconProvider
     */
    public static void setDefaultFileIconProvider(FileIconProvider fip) {
        defaultFileIconProvider = fip;
    }
}
