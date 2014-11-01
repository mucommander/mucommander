/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.icon.FileIconProvider;
import com.mucommander.commons.file.icon.impl.SwingFileIconProvider;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.impl.local.LocalProtocolProvider;
import com.mucommander.commons.file.util.FilePool;
import com.mucommander.commons.file.util.PathTokenizer;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.runtime.JavaVersion;
import com.mucommander.commons.runtime.OsFamily;

/**
 * FileFactory is an abstract class that provides static methods to get a {@link AbstractFile} instance for
 * a specified path or {@link FileURL} location.
 * <h3>Protocols</h3>
 * <p>
 * In order to allow the <code>com.mucommander.commons.file</code> API to access new file protocols, developers must create
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
 * </ul>
 * </p>
 * <h3>Archive formats</h3>
 * <p>
 * In order to allow the <code>com.mucommander.commons.file</code> API to access new archive formats, developers must create
 * an implementation of {@link AbstractArchiveFile} that handles that format and register it to <code>FileFactory</code>.
 * This registration requires an implementation of {@link ArchiveFormatProvider}, an instance of which will be passed to
 * {@link #registerArchiveFormat(ArchiveFormatProvider)}.
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
 *   <li><code>RAR</code>, registered to rar files.</li>
 *   <li><code>SEVENZIP</code>, registered to 7z files.</li>
 * </ul>
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class FileFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileFactory.class);

    /** All registered protocol providers. */
    private static Hashtable<String, ProtocolProvider> protocolProviders = new Hashtable<String, ProtocolProvider>();

    /** Local file provider to avoid hashtable lookups (faster). */
    private static ProtocolProvider localFileProvider;

    /** Vector of registered ArchiveFormatMapping instances */
    private static Vector<ArchiveFormatProvider> archiveFormatProvidersV = new Vector<ArchiveFormatProvider>();

    /** Array of registered FileProtocolMapping instances, for quicker access */
    private static ArchiveFormatProvider[] archiveFormatProviders;

    /** Contains a FilePool instance for each registered scheme */
    private static final HashMap<String, FilePool> FILE_POOL_MAP = new HashMap<String, FilePool>();

    /** System temp directory */
    private static final AbstractFile TEMP_DIRECTORY;

    /** Default file icon provider, initialized in static block */
    private static FileIconProvider defaultFileIconProvider;

    /** Default authenticator, used when none is specified */
    private static Authenticator defaultAuthenticator;

    static {
        // Register built-in file protocols.
        ProtocolProvider protocolProvider;
        registerProtocol(FileProtocols.FILE, new LocalProtocolProvider());
        registerProtocol(FileProtocols.SMB, new com.mucommander.commons.file.impl.smb.SMBProtocolProvider());
        registerProtocol(FileProtocols.HTTP, protocolProvider = new com.mucommander.commons.file.impl.http.HTTPProtocolProvider());
        registerProtocol(FileProtocols.HTTPS, protocolProvider);
        registerProtocol(FileProtocols.FTP, new com.mucommander.commons.file.impl.ftp.FTPProtocolProvider());
        registerProtocol(FileProtocols.NFS, new com.mucommander.commons.file.impl.nfs.NFSProtocolProvider());
        registerProtocol(FileProtocols.SFTP, new com.mucommander.commons.file.impl.sftp.SFTPProtocolProvider());
        if(JavaVersion.JAVA_1_6.isCurrentOrHigher()) {
            // Hadoop requires Java 1.6
            registerProtocol(FileProtocols.HDFS, new com.mucommander.commons.file.impl.hadoop.HDFSProtocolProvider());
//            registerProtocol(FileProtocols.S3, new com.mucommander.commons.file.impl.hadoop.S3ProtocolProvider());
        }
        registerProtocol(FileProtocols.S3, new com.mucommander.commons.file.impl.s3.S3ProtocolProvider());
        registerProtocol(FileProtocols.VSPHERE, new com.mucommander.commons.file.impl.vsphere.VSphereProtocolProvider());

        // Register built-in archive file formats, order for TarArchiveFile and GzipArchiveFile/Bzip2ArchiveFile is important:
        // TarArchiveFile must match 'tar.gz'/'tar.bz2' files before GzipArchiveFile/Bzip2ArchiveFile does.
        registerArchiveFormat(new com.mucommander.commons.file.impl.zip.ZipFormatProvider());
        registerArchiveFormat(new com.mucommander.commons.file.impl.tar.TarFormatProvider());
        registerArchiveFormat(new com.mucommander.commons.file.impl.gzip.GzipFormatProvider());
        registerArchiveFormat(new com.mucommander.commons.file.impl.bzip2.Bzip2FormatProvider());
        registerArchiveFormat(new com.mucommander.commons.file.impl.iso.IsoFormatProvider());
        registerArchiveFormat(new com.mucommander.commons.file.impl.ar.ArFormatProvider());
        registerArchiveFormat(new com.mucommander.commons.file.impl.lst.LstFormatProvider());
        registerArchiveFormat(new com.mucommander.commons.file.impl.rar.RarFormatProvider());
        registerArchiveFormat(new com.mucommander.commons.file.impl.sevenzip.SevenZipFormatProvider());

        // Set the default FileIconProvider instance
        defaultFileIconProvider = new SwingFileIconProvider();

        // Create the temp directory folder
        TEMP_DIRECTORY = getFile(System.getProperty("java.io.tmpdir"));
    }


    /**
     * Makes sure no instance of <code>FileFactory</code> is created.
     */
    private FileFactory() {
    }


    /**
     * Registers a new file protocol.
     * <p>
     * If a {@link ProtocolProvider} was already registered to the specified protocol, it will automatically be
     * unregistered.
     * </p>
     * <p>
     * The <code>protocol</code> argument is expected to be the protocol identifier, without the trailing <code>://</code>.
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
        protocol = protocol.toLowerCase();

        // Create raw and archive file pools
        FILE_POOL_MAP.put(protocol, new FilePool());

        // Special case for local file provider.
        // Note that the local file provider is also added to the provider hashtable.
        if(protocol.equals(FileProtocols.FILE))
            localFileProvider = provider;

        return protocolProviders.put(protocol, provider);
    }

    /**
     * Unregisters the provider associated with the specified protocol.
     *
     * @param  protocol identifier of the protocol whose provider should be unregistered.
     * @return          the provider that has been unregistered, or <code>null</code> if none.
     */
    public static ProtocolProvider unregisterProtocol(String protocol) {
        protocol = protocol.toLowerCase();

        // Remove raw and archive file pools
        FILE_POOL_MAP.remove(protocol);

        // Special case for local file provider
        if(protocol.equals(FileProtocols.FILE))
            localFileProvider = null;

        return protocolProviders.remove(protocol);
    }

    /**
     * Returns the protocol provider associated with the specified protocol identifier, or <code>null</code> if there
     * is none.
     *
     * @param  protocol identifier of the protocol whose provider should be retrieved.
     * @return the protocol provider registered to the specified protocol identifier, or <code>null</code> if none.
     */
    public static ProtocolProvider getProtocolProvider(String protocol) {
        return protocolProviders.get(protocol.toLowerCase());
    }

    /**
     * Returns <code>true</code> if the given protocol has a registered {@link ProtocolProvider}.
     *
     * @param protocol identifier of the protocol to test
     * @return <code>true</code> if the given protocol has a registered {@link ProtocolProvider}.
     */
    public static boolean isRegisteredProtocol(String protocol) {
        return getProtocolProvider(protocol)!=null;
    }

    /**
     * Returns an iterator on all known protocol names.
     *
     * <p>All objects returned by the iterator's <code>nextElement()</code> method will be string instances. These can
     * then be passed to {@link #getProtocolProvider(String) getProtocolProvider} to retrieve the associated
     * {@link ProtocolProvider}.</p>
     *
     * @return an iterator on all known protocol names.
     */
    public static Iterator<String> protocols() {
        return protocolProviders.keySet().iterator();
    }

    /**
     * Registers a new <code>ArchiveFormatProvider</code>.
     *
     * @param provider the <code>ArchiveFormatProvider</code> to register.
     */
    public static void registerArchiveFormat(ArchiveFormatProvider provider) {
        archiveFormatProvidersV.add(provider);
        updateArchiveFormatProviderArray();
    }

    /**
     * Removes a previously-registered <code>ArchiveFormatProvider</code>.
     * <p>
     * To unregister the provider of a particular archive format without knowing the associated provider instance, use
     * {@link #getArchiveFormatProvider(String)} with a known archive filename to retrieve the provider instance.
     * For example, <code>FileFactory.unregisterArchiveFormat(FileFactory.getArchiveFormatProvider("file.zip"))</code>
     * will unregister the (first, if any) Zip provider.
     * </p>
     *
     * @param provider the <code>ArchiveFormatProvider</code> to unregister.
     * @see #getArchiveFormatProvider(String)
     */
    public static void unregisterArchiveFormat(ArchiveFormatProvider provider) {
        int index = archiveFormatProvidersV.indexOf(provider);

        if(index!=-1) {
            archiveFormatProvidersV.removeElementAt(index);
            updateArchiveFormatProviderArray();
        }
    }

    /**
     * Updates the <code>ArchiveFormatProvider</code> array to reflect the contents of the Vector.
     */
    private static void updateArchiveFormatProviderArray() {
        archiveFormatProviders = new ArchiveFormatProvider[archiveFormatProvidersV.size()];
        archiveFormatProvidersV.toArray(archiveFormatProviders);
    }

    /**
     * Returns the first <code>ArchiveFormatProvider</code> that matches the specified filename, <code>null</code>
     * if there is none. Note that if a filename matches the {@link java.io.FilenameFilter} of several registered
     * providers, the first provider matching the filename will be returned.
     *
     * @param filename an archive filename that potentially matches one of the registered <code>ArchiveFormatProvider</code>
     * @return the first <code>ArchiveFormatProvider</code> that matches the specified filename, <code>null</code> if there is none
     */
    public static ArchiveFormatProvider getArchiveFormatProvider(String filename) {
        if(filename == null)
            return null;

        for (ArchiveFormatProvider provider : archiveFormatProviders) {
            if (provider.getFilenameFilter().accept(filename))
                return provider;
        }
        return null;
    }

    /**
     * Returns an iterator on all known archive formats.
     *
     * @return an iterator on all known archive formats.
     */
    public static Iterator<ArchiveFormatProvider> archiveFormats() {
        return archiveFormatProvidersV.iterator();
    }


    /**
     * Returns an instance of AbstractFile for the given absolute path.
     *
     * <p>This method does not throw any IOException but returns <code>null</code> if the file could not be created.</p>
     *
     * @param absPath the absolute path to the file
     * @return <code>null</code> if the given path is not absolute or incorrect (doesn't correspond to any file) or
     * if something went wrong during file creation.
     */
    public static AbstractFile getFile(String absPath) {
        try {return getFile(absPath, null);}
        catch(IOException e) {
            LOGGER.info("Caught an exception", e);
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
     * @return <code>null</code> if the given path is not absolute or incorrect (doesn't correspond to any file)
     * @throws java.io.IOException  and throwException param was set to <code>true</code>.
     * @throws AuthException if additional authentication information is required to create the file
     */
    public static AbstractFile getFile(String absPath, boolean throwException) throws AuthException, IOException {
        try {return getFile(absPath, null);}
        catch(IOException e) {
            LOGGER.info("Caught an exception", e);

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
     * @return an instance of <code>AbstractFile</code> for the specified absolute path.
     * @throws java.io.IOException if something went wrong during file or file url creation.
     * @throws AuthException if additionnal authentication information is required to create the file
     */
    public static AbstractFile getFile(String absPath, AbstractFile parent) throws AuthException, IOException {
        return getFile(FileURL.getFileURL(absPath), parent);
    }

    /**
     * Returns an instance of AbstractFile for the given FileURL instance.
     *
     * @param fileURL the file URL
     * @return the created file or null if something went wrong during file creation
     */
    public static AbstractFile getFile(FileURL fileURL) {
        try {return getFile(fileURL, null);}
        catch(IOException e) {
            LOGGER.info("Caught an exception", e);
            return null;
        }
    }

    /**
     * Returns an instance of AbstractFile for the given FileURL instance.
     *
     * @param fileURL the file URL
     * @param throwException if set to <code>true</code>, an IOException will be thrown if something went wrong during file creation
     * @return the created file
     * @throws java.io.IOException if something went wrong during file creation
     */
    public static AbstractFile getFile(FileURL fileURL, boolean throwException) throws IOException {
        try {return getFile(fileURL, null);}
        catch(IOException e) {
            LOGGER.info("Caught an exception", e);

            if(throwException)
                throw e;
            return null;
        }
    }

    /**
     * Shorthand for {@link #getFile(FileURL, AbstractFile, Authenticator, Object...)} called with the
     * {@link #getDefaultAuthenticator() default authenticator}.
     *
     * @param fileURL the file URL representing the file to be created
     * @param parent the parent AbstractFile to use as the created file's parent, can be <code>null</code>
     * @return an instance of {@link AbstractFile} for the given {@link FileURL}.
     * @throws java.io.IOException if something went wrong during file creation.
     */
    public static AbstractFile getFile(FileURL fileURL, AbstractFile parent, Object... instantiationParams) throws IOException {
        return getFile(fileURL, parent, defaultAuthenticator, instantiationParams);
    }

    /**
     * Creates and returns an instance of AbstractFile for the given FileURL and uses the specified parent file (if any)
     * as the created file's parent.
     *
     * <p>Specifying the file parent if an instance already exists allows to recycle the AbstractFile instance
     * instead of creating a new one when the parent file is requested.
     *
     * @param fileURL the file URL representing the file to be created
     * @param authenticator used to authenticate the specified location if its protocol
     * {@link FileURL#getAuthenticationType() is authenticated} and the location contains no credentials already.
     * If the value is <code>null</code>, no {@link Authenticator} will be used, not even the default one.
     * @param parent the parent AbstractFile to use as the created file's parent, can be <code>null</code>
     * @return an instance of {@link AbstractFile} for the given {@link FileURL}.
     * @throws java.io.IOException if something went wrong during file creation.
     */
    public static AbstractFile getFile(FileURL fileURL, AbstractFile parent, Authenticator authenticator, Object... instantiationParams) throws IOException {
        String protocol = fileURL.getScheme();
        if(!isRegisteredProtocol(protocol))
            throw new IOException("Unsupported file protocol: "+protocol);

        // Lookup the pool for an existing AbstractFile instance, only if there are no instantiationParams.
        // If there are instantiationParams (the file was created by the AbstractFile implementation directly, that is
        // by ls()), any existing file in the pool must be replaced with a new, more up-to-date one.
        FilePool filePool = FILE_POOL_MAP.get(fileURL.getScheme().toLowerCase());
        if(instantiationParams.length==0) {
            // Note: FileURL#equals(Object) and #hashCode() take into account credentials and properties and are
            // trailing slash insensitive (e.g. '/root' and '/root/' URLS are one and the same)
            AbstractFile file = filePool.get(fileURL);
            if(file!=null)
                return file;
        }

        String filePath = fileURL.getPath();
        // For local paths under Windows (e.g. "/C:\temp"), remove the leading '/' character
        if(OsFamily.WINDOWS.isCurrent() && FileProtocols.FILE.equals(protocol))
            filePath = PathUtils.removeLeadingSeparator(filePath, "/");

        String pathSeparator = fileURL.getPathSeparator();

        PathTokenizer pt = new PathTokenizer(filePath,
                pathSeparator,
                false);

        AbstractFile currentFile = null;
        boolean lastFileResolved = false;

        // Extract every filename from the path from left to right and for each of them, see if it looks like an archive.
        // If it does, create the appropriate protocol file and wrap it with an archive file.
        while(pt.hasMoreFilenames()) {
            // Test if the filename's extension looks like a supported archive format...
            // Note that the archive can also be a directory with an archive extension.
            if(isArchiveFilename(pt.nextFilename())) {
                // Remove trailing separator of file, some file protocols such as SFTP don't like trailing separators.
                // On the contrary, directories without a trailing slash are fine.
                String currentPath = PathUtils.removeTrailingSeparator(pt.getCurrentPath(), pathSeparator);

                // Test if current file is an archive and if it is, create an archive entry file instead of a raw
                // protocol file
                if(currentFile==null || !currentFile.isArchive()) {
                    // Create a fresh FileURL with the current path
                    FileURL clonedURL = (FileURL)fileURL.clone();
                    clonedURL.setPath(currentPath);

                    // Look for a cached file instance before creating a new one
                    currentFile = filePool.get(clonedURL);
                    if(currentFile==null) {
                        currentFile = wrapArchive(createRawFile(clonedURL, authenticator, instantiationParams));
                        // Add the intermediate file instance to the cache
                        filePool.put(clonedURL, currentFile);
                    }

                    lastFileResolved = true;
                }
                else {          // currentFile is an AbstractArchiveFile
                    // Note: wrapArchive() is already called by AbstractArchiveFile#createArchiveEntryFile()
                    AbstractFile tempEntryFile = ((AbstractArchiveFile)currentFile).getArchiveEntryFile(PathUtils.removeLeadingSeparator(currentPath.substring(currentFile.getURL().getPath().length(), currentPath.length()), pathSeparator));
                    if(tempEntryFile.isArchive()) {
                        currentFile = tempEntryFile;
                        lastFileResolved = true;
                    }
                    else {
                        lastFileResolved = false;
                    }
                    // Note: don't cache the entry file
                }
            }
            else {
                lastFileResolved = false;
            }
        }

        // Create last file if it hasn't been already (if the last filename was not an archive), same routine as above
        // except that it doesn't wrap the file with an archive file
        if(!lastFileResolved) {
            // Note: DON'T strip out the trailing separator, as this would cause problems with root resources
            String currentPath = pt.getCurrentPath();

            if(currentFile==null || !currentFile.isArchive()) {
                FileURL clonedURL = (FileURL)fileURL.clone();
                clonedURL.setPath(currentPath);

                // Note: no need to look a cached file instance, we have already looked for it at the very beginning.
                currentFile = createRawFile(clonedURL, authenticator, instantiationParams);
                // Add the final file instance to the cache
                filePool.put(currentFile.getURL(), currentFile);
            }
            else {          // currentFile is an AbstractArchiveFile
                currentFile = ((AbstractArchiveFile)currentFile).getArchiveEntryFile(PathUtils.removeLeadingSeparator(currentPath.substring(currentFile.getURL().getPath().length(), currentPath.length()), pathSeparator));
                // Note: don't cache the entry file
            }
        }

        // Reuse existing parent file instance if one was specified
        if(parent!=null)
            currentFile.setParent(parent);

        return currentFile;
    }

    private static AbstractFile createRawFile(FileURL fileURL, Authenticator authenticator, Object... instantiationParams) throws IOException {
        String scheme = fileURL.getScheme().toLowerCase();

        // Special case for local files to avoid provider hashtable lookup and other unnecessary checks
        // (for performance reasons)
        if(scheme.equals(FileProtocols.FILE)) {
            if(localFileProvider == null)
                throw new IOException("Unknown file protocol: " + scheme);

            return localFileProvider.getFile(fileURL, instantiationParams);

            // Uncomment this line and comment the previous one to simulate a slow filesystem
            //file = new DebugFile(file, 0, 50);
        }
        // Use the protocol hashtable for any other file protocol
        else {
            // If an Authenticator has been specified and the specified FileURL's protocol is authenticated and the
            // FileURL doesn't contain any credentials, use it to authenticate the FileURL.
            if(authenticator!=null && fileURL.getAuthenticationType()!=AuthenticationType.NO_AUTHENTICATION && !fileURL.containsCredentials())
                authenticator.authenticate(fileURL);

            // Finds the right file protocol provider
            ProtocolProvider provider = getProtocolProvider(scheme);
            if(provider == null)
                throw new IOException("Unknown file protocol: " + scheme);

            return provider.getFile(fileURL, instantiationParams);
        }
    }

    /**
     * Returns a variation of the given filename, appending a pseudo-unique ID to the filename's prefix while keeping
     * the same filename extension.
     *
     * @param filename base filename
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
     * @throws IOException if an error occurred while instantiating the temporary file. This should not happen under
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
     * Creates a temporary file with a default filename. This method is a shorthand for
     * {@link #getTemporaryFile(String, boolean)} called with a <code>null</code> name.
     *
     * @param deleteOnExit if <code>true</code>, the temporary file will be deleted upon normal termination of the JVM
     * @return the temporary file, may be a LocalFile or an AbstractArchiveFile if the filename's extension corresponds
     * to a registered archive format.
     * @throws IOException if an error occurred while instantiating the temporary file. This should not happen under
     * normal circumstances.
     */
    public static AbstractFile getTemporaryFile(boolean deleteOnExit) throws IOException {
        return getTemporaryFile(null, deleteOnExit);
    }

    /**
     * Returns the temporary folder, i.e. the parent folder of temporary files returned by
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
     * @return <code>true</code> if the specified filename is a known archive file name, <code>false</code> otherwise.
     */
    public static boolean isArchiveFilename(String filename) {
        return getArchiveFormatProvider(filename) != null;
    }

    /**
     * Tests if the given file's extension matches that of one of the registered archive formats.
     * If it does, a corresponding {@link AbstractArchiveFile} instance is created on top of the provided file
     * and returned. If it doesn't, the provided <code>AbstractFile</code> instance is simply returned.
     * <p>
     * Note that return {@link AbstractArchiveFile} instances may not actually be archives according to
     * {@link AbstractFile#isArchive()}. An {@link AbstractArchiveFile} instance that is not currently an archive 
     * (either non-existent or a directory) will behave as a regular (non-archive) file. This allows file instances to
     * go from being an archive to not being an archive (and vice-versa), without having to re-resolve the file.
     * </p>
     */
    public static AbstractFile wrapArchive(AbstractFile file) throws IOException {
        String filename = file.getName();

        // Looks for an archive FilenameFilter that matches the given filename.
        // Comparing the filename against each and every archive extension has a cost, so we only perform the test if
        // the filename contains a dot '.' character, since most of the time this method is called with a filename that
        // doesn't match any of the filters.
        if(filename.indexOf('.')!=-1) {
            ArchiveFormatProvider provider;
            if((provider = getArchiveFormatProvider(filename)) != null) {
                return provider.getFile(file);
            }
        }

        return file;
    }


    /**
     * Returns the default {@link com.mucommander.commons.file.icon.FileIconProvider} instance. The default provider class
     * (before {@link #setDefaultFileIconProvider(com.mucommander.commons.file.icon.FileIconProvider)} is called) is
     * platform-dependent and as such may vary across platforms.
     *
     * <p>It is noteworthy that the provider returned by this method is used by {@link com.mucommander.commons.file.AbstractFile#getIcon()}
     * to create and return the icon.</p>
     *
     * @return the default FileIconProvider implementation
     */
    public static FileIconProvider getDefaultFileIconProvider() {
        return defaultFileIconProvider;
    }

    /**
     * Sets the default {@link com.mucommander.commons.file.icon.FileIconProvider} implementation.
     *
     * <p>It is noteworthy that the provider returned by this method is used by {@link com.mucommander.commons.file.AbstractFile#getIcon()}
      * to create and return the icon.</p>
      *
     * @param fip the new value for the default FileIconProvider
     */
    public static void setDefaultFileIconProvider(FileIconProvider fip) {
        defaultFileIconProvider = fip;
    }

    /**
     * Returns the default {@link Authenticator} that is used for authenticating {@link FileURL} instances prior
     * to resolving the corresponding file.
     *
     * @return the default {@link Authenticator} that is used for authenticating {@link FileURL} instances prior
     * to resolving the corresponding file
     * @see Authenticator
     */
    public static Authenticator getDefaultAuthenticator() {
        return defaultAuthenticator;
    }

    /**
     * Sets the default {@link Authenticator} that is used for authenticating {@link FileURL} instances prior
     * to resolving the corresponding file.
     *
     * @param authenticator the new default {@link Authenticator}
     * @see Authenticator
     */
    public static void setDefaultAuthenticator(Authenticator authenticator) {
        defaultAuthenticator = authenticator;
    }
}
