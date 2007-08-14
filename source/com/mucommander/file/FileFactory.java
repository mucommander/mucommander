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
import com.mucommander.PlatformManager;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.cache.LRUCache;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.filter.ExtensionFilenameFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.impl.ar.ArArchiveFile;
import com.mucommander.file.impl.bzip2.Bzip2ArchiveFile;
import com.mucommander.file.impl.ftp.FTPFile;
import com.mucommander.file.impl.gzip.GzipArchiveFile;
import com.mucommander.file.impl.http.HTTPFile;
import com.mucommander.file.impl.iso.IsoArchiveFile;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.file.impl.lst.LstArchiveFile;
import com.mucommander.file.impl.nfs.NFSFile;
import com.mucommander.file.impl.sftp.SFTPFile;
import com.mucommander.file.impl.smb.SMBFile;
import com.mucommander.file.impl.tar.TarArchiveFile;
import com.mucommander.file.impl.trash.KDETrash;
import com.mucommander.file.impl.trash.OSXTrash;
import com.mucommander.file.impl.zip.ZipArchiveFile;
import com.mucommander.file.util.FileToolkit;
import com.mucommander.file.util.PathTokenizer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * FileFactory is an abstract class that provides static methods to get a {@link AbstractFile} instance for
 * a specified path or {@link FileURL} location.
 *
 * <p>The muCommander file API provides ready-to-use implementations for several file protocols and archive types.
 * Additional AbstractFile implementations can be register in at runtime using the {@link #registerFileProtocol(FileProtocolMapping)}
 * and {@link #registerArchiveFormat(ArchiveFormatMapping)} methods.
 * Similarily, the {@link #getRegisteredFileProtocols()} and {@link #getRegisteredArchiveFileFormats()} allow to list
 * all the registered file protocols and archive formats. 

 * @see AbstractFile
 * @author Maxence Bernard
 */
public abstract class FileFactory {

    /** Vector of registered FileProtocolMapping instances */
    private static Vector fileProtocolMappingsV = new Vector();
    /** Array of registered FileProtocolMapping instances, for quicker access */
    private static FileProtocolMapping fileProtocolMappings[];

    /** Vector of registered ArchiveFormatMapping instances */
    private static Vector archiveFormatMappingsV = new Vector();
    /** Array of registered FileProtocolMapping instances, for quicker access */
    private static ArchiveFormatMapping archiveFormatMappings[];

    /** Static LRUCache instance that caches frequently accessed AbstractFile instances */
    private static LRUCache fileCache = LRUCache.createInstance(MuConfiguration.getVariable(MuConfiguration.FILE_CACHE_CAPACITY,
                                                                                                 MuConfiguration.DEFAULT_FILE_CACHE_CAPACITY));

private static WeakHashMap archiveFileCache = new WeakHashMap();

    /** System temp directory */
    private final static File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));


    static {
        // Register built-in file protocols
        // Local file protocol is hard-wired for performance reasons, no need to add it
        registerFileProtocol(SMBFile.class, FileProtocols.SMB);
        registerFileProtocol(HTTPFile.class, FileProtocols.HTTP);
        registerFileProtocol(HTTPFile.class, FileProtocols.HTTPS);
        registerFileProtocol(FTPFile.class, FileProtocols.FTP);
        registerFileProtocol(SFTPFile.class, FileProtocols.SFTP);
        registerFileProtocol(NFSFile.class, FileProtocols.NFS);
//        registerFileProtocol(WebDAVFile.class, FileProtocols.WEBDAV);
//        registerFileProtocol(WebDAVFile.class, FileProtocols.WEBDAVS);
        
        // Register built-in archive file formats, order for TarArchiveFile and GzipArchiveFile/Bzip2ArchiveFile is important:
        // TarArchiveFile must match 'tar.gz'/'tar.bz2' files before GzipArchiveFile/Bzip2ArchiveFile does.
        registerArchiveFormat(ZipArchiveFile.class, new ExtensionFilenameFilter(new String[]{".zip", ".jar", ".war", ".wal", ".wmz", ".xpi", ".ear", ".sar", ".odt", ".ods", ".odp", ".odg", ".odf"}));
        registerArchiveFormat(TarArchiveFile.class, new ExtensionFilenameFilter(new String[]{".tar", ".tar.gz", ".tgz", ".tar.bz2", ".tbz2"}));
        registerArchiveFormat(GzipArchiveFile.class, new ExtensionFilenameFilter(".gz"));
        registerArchiveFormat(Bzip2ArchiveFile.class, new ExtensionFilenameFilter(".bz2"));
        registerArchiveFormat(IsoArchiveFile.class, new ExtensionFilenameFilter(new String[]{".iso", ".nrg"}));
        registerArchiveFormat(ArArchiveFile.class, new ExtensionFilenameFilter(new String[]{".ar", ".a", ".deb"}));
        registerArchiveFormat(LstArchiveFile.class, new ExtensionFilenameFilter(new String[]{".lst"}));
//        registerArchiveFormat(SevenZArchiveFile.class, new ExtensionFilenameFilter(new String[]{".7z"}));
    }


    /**
     * Registers a new file protocol and associated provider class, contained in the given {@link FileProtocolMapping}
     * instance. Any previously registered FileProtocolMapping with the same protocol will be removed and replaced
     * by the given one.
     *
     * <p>After this method has been called, <code>getFile()</code> methods of this class will be able resolve files
     * with the newly registered protocol, provided of course that the provider class can be properly instanciated.
     *
     * @param fpm a FileProtocolMapping instance that contains the protocol and provider class to register
     */
    public static synchronized void registerFileProtocol(FileProtocolMapping fpm) {
        // First remove any registered FileProtocolMapping with the same protocol
        String protocol = fpm.getProtocol();
        FileProtocolMapping fpmTemp;
        int nbMappings = fileProtocolMappingsV.size();
        for(int i=0; i<nbMappings; i++) {
            fpmTemp = (FileProtocolMapping)fileProtocolMappingsV.elementAt(i);
            if(fpmTemp.getProtocol().equals(protocol)) {
                fileProtocolMappingsV.removeElementAt(i);
                // No need to check any further, there can be only FileProtocolMapping for a given protocol
                break;
            }
        }

        // Add the new FileProtocolMapping to the Vector
        fileProtocolMappingsV.add(fpm);

        // Update FileProtocolMapping array
        updateFileProtocolMappingsArray();

        if(Debug.ON) Debug.trace("Registered "+fpm.getProtocol()+" protocol to provider class "+fpm.getProviderClass());
    }

    /**
     * Convenience method to register a new file protocol and associated provider class. The specified class must
     * extend {@link AbstractFile} and provide a constructor with the {@link AbstractFile#AbstractFile(FileURL)}
     * signature.<br>
     * This method returns <code>true</code> if the file protocol could be properly registered, <code>false</code>
     * otherwise. If more information is needed as to why a file protocol could not be registered, the
     * {@link #registerFileProtocol(FileProtocolMapping)} method should be used instead.
     *
     * <p>After this method has been called, <code>getFile()</code> methods of this class will be able resolve files
     * with the newly registered protocol, provided of course that the provider class can be properly instanciated.
     *
     * @param abstractFileClass a Class instance denoting a class, which extends {@link AbstractFile} and has a constructor with the {@link AbstractFile#AbstractFile(FileURL)} signature
     * @param protocol the protocol to associate with the specified AbstractFile class (e.g. "ftp")
     * @return <code>true</code> if the protocol was registered without any error, <code>false</code> otherwise
     */
    public static synchronized boolean registerFileProtocol(Class abstractFileClass, String protocol) {
        try {
            registerFileProtocol(new FileProtocolMapping(abstractFileClass, protocol));
            return true;
        }
        catch(Exception e) {    // Catches NoSuchMethodException, IntrospectionException, SecurityException
            if(Debug.ON) Debug.trace("Error: unable to register protocol "+protocol+" with class "+abstractFileClass+": "+e);
            return false;
        }
    }

    /**
     * Returns an <code>Enumeration</code> of all registered FileProtocolMapping instances.
     */
    public static synchronized Enumeration getRegisteredFileProtocols() {
        return fileProtocolMappingsV.elements();
    }

    /**
     * Removes the given <code>FileProtocolMapping</code> instance from the list of registered file protocols.
     */
    public static synchronized void unregisterFileProtocol(FileProtocolMapping fpm) {
        int index = fileProtocolMappingsV.indexOf(fpm);

        if(index!=-1) {
            fileProtocolMappingsV.removeElementAt(index);
            updateFileProtocolMappingsArray();
        }
    }

    /**
     * Updates the FileProtocolMapping array to reflect the contents of the FileProtocolMapping Vector.
     */
    private static void updateFileProtocolMappingsArray() {
        fileProtocolMappings = new FileProtocolMapping[fileProtocolMappingsV.size()];
        fileProtocolMappingsV.toArray(fileProtocolMappings);
    }


    /**
     * Registers a new archive format and associated provider class, contained in the given {@link ArchiveFormatMapping}
     * instance.
     *
     * <p>After this method has been called, <code>getFile()</code> methods of this class will be able resolve archive
     * files with the newly registered archive format, provided of course that the provider class can be properly
     * instanciated.
     *
     * @param afm an ArchiveFormatMapping instance that contains the archive FilenameFilter and provider class to register
     */
    public static synchronized void registerArchiveFormat(ArchiveFormatMapping afm) {
        archiveFormatMappingsV.add(afm);

        updateArchiveFormatMappingsArray();
    }

    /**
     * Convenience method to register a new archive format and associated provider class. The specified class must
     * extend {@link AbstractArchiveFile} and provide a constructor with the
     * {@link AbstractArchiveFile#AbstractArchiveFile(AbstractFile)} signature.<br>
     * This method returns <code>true</code> if the archive format could be properly registered, <code>false</code>
     * otherwise. If more information is needed as to why an archive format could not be registered, the
     * {@link #registerArchiveFormat(ArchiveFormatMapping)} method should be used instead.
     *
     * <p>After this method has been called, <code>getFile()</code> methods of this class will be able resolve files
     * with the newly registered archive format, provided of course that the provider class can be properly instanciated.
     *
     * @param abstractArchiveFileClass a Class instance denoting a class which extends {@link AbstractArchiveFile} and
     * which has a constructor with the {@link AbstractArchiveFile#AbstractArchiveFile(AbstractFile)} signature
     * @param filenameFilter a FilenameFilter that characterizes the archive format to associate with the specified
     * AbstractArchiveFile class
     * @return <code>true</code> if the archive format was registered without any error, <code>false</code> otherwise
     */
    public static synchronized boolean registerArchiveFormat(Class abstractArchiveFileClass, FilenameFilter filenameFilter) {
        try {
            registerArchiveFormat(new ArchiveFormatMapping(abstractArchiveFileClass, filenameFilter));
            return true;
        }
        catch(Exception e) {    // Catches NoSuchMethodException, IntrospectionException, SecurityException
            if(Debug.ON) Debug.trace("Error: unable to register filenameFilter "+ filenameFilter +" with class "+abstractArchiveFileClass+": "+e);
            return false;
        }
    }

    /**
     * Returns an <code>Enumeration</code> of all registered FileProtocolMapping instances.
     */
    public static synchronized Enumeration getRegisteredArchiveFileFormats() {
        return archiveFormatMappingsV.elements();
    }

    /**
     * Removes the given <code>ArchiveFormatMapping</code> instance from the list of registered archive formats.
     */
    public static synchronized void unregisterArchiveFileFormat(ArchiveFormatMapping afm) {
        int index = archiveFormatMappingsV.indexOf(afm);

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
    public static AbstractFile getFile(String absPath, AbstractFile parent) throws AuthException, IOException {
        return getFile(new FileURL(absPath), parent);
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
//                    currentFile = ((AbstractArchiveFile)currentFile).getEntryFile(FileToolkit.removeLeadingSeparator(currentPath.substring(currentFile.getURL().getPath().length(), currentPath.length())));
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
        try {
            String protocol = fileURL.getProtocol().toLowerCase();

            AbstractFile file;

            // Special case for local files, do not use protocol registration mechanism to speed things up a bit
            if(protocol.equals(FileProtocols.FILE)) {
                // Use an LRU file cache to recycle frequently used local file instances.
                String urlRep = fileURL.toString(true);
                file = (AbstractFile)fileCache.get(urlRep);

//                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("file cache hits/misses: "+fileCache.getHitCount()+"/"+fileCache.getMissCount());

                if(file!=null)
                    return file;

                // Create a local file instance
                file = new LocalFile(fileURL);
                // Uncomment this line and comment the previous one to simulate a slow filesystem 
                //file = new DebugFile(new LocalFile(fileURL), 0, 50);

                // Note: Creating an archive file on top of the file must be done after adding the file to the LRU cache,
                // this could otherwise lead to weird behaviors, for example if a directory with the same filename
                // of a former archive was created, the directory would be considered as an archive
                fileCache.add(urlRep, file);
//                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Added to file cache: "+file);

                return file;
            }
            // For any other file protocol, use registered protocols map
            else {
                // If the specified FileURL doesn't contain any credentials, use CredentialsManager to find
                // any credentials matching the url and use them.
if(Debug.ON) Debug.trace("fileURL.containsCredentials() "+fileURL.containsCredentials());
                if(!fileURL.containsCredentials())
                    CredentialsManager.authenticateImplicit(fileURL);
if(Debug.ON) Debug.trace("credentials="+fileURL.getCredentials());

                // Find a register FileProtocolMapping instance matching the protocol
                int nbMappings = fileProtocolMappings.length;
                FileProtocolMapping fpm = null;
                for(int i=0; i<nbMappings; i++) {
                    if(fileProtocolMappings[i].protocol.equals(protocol)) {
                        fpm = fileProtocolMappings[i];
                        break;
                    }
                }

                // Throw an IOException if no FileProtocolMapping instance could be found
                if(fpm==null) {
                    // Todo: localize this string as it can be displayed to the end user
                    throw new IOException("Unknown file protocol: "+protocol);
                }

                // May throw InstantiationException, IllegalAccessException, IllegalAccessException, ExceptionInInitializerError, InvocationTargetException
                return (AbstractFile)fpm.providerConstructor.newInstance(new Object[]{fileURL});
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
     * Returns a variation of the given filename, appending a pseudo-unique ID to the filename's prefix while keeping
     * the same filename extension.
     */
    private static String getFilenameVariation(String desiredName) {
        int lastDotPos = desiredName.lastIndexOf('.');
        int len = desiredName.length();
        String nameSuffix = "_"+System.currentTimeMillis()+(new Random().nextInt(10000));

        if(lastDotPos==-1)
            desiredName += nameSuffix;
        else
            desiredName = desiredName.substring(0, lastDotPos) + nameSuffix + desiredName.substring(lastDotPos, len);

        return desiredName;
    }

    /**
     * Creates and returns a temporary local file using the desired name.
     *
     * @param desiredName the desired filename for the temporary file. If a file already exists with this name
     * in the temp directory, the filename's prefix (name without extension) will be appended an ID,
     * but the filename's extension will always be preserved.
     * @param deleteOnExit if <code>true</code>, the temporary file will be deleted upon normal termination of the JVM
     * @return the temporary AbstractFile
     */
    public static AbstractFile getTemporaryFile(String desiredName, boolean deleteOnExit) {
        // Attempt to use the desired name
        File tempFile = new File(TEMP_DIRECTORY, desiredName);

        if(tempFile.exists()) {
            tempFile = new File(TEMP_DIRECTORY, getFilenameVariation(desiredName));
        }

        if(deleteOnExit)
            tempFile.deleteOnExit();

        return getFile(tempFile.getAbsolutePath());
    }

    /**
     * Returns true if the given filename's extension matches one of the registered archive formats.
     *
     * @param filename the filename to test
     */
    public static boolean isArchiveFilename(String filename) {
        // Looks for an archive FilenameFilter that matches the given filename.
        // Comparing the filename against each and every archive extension has a cost, so we only perform the test if
        // the filename contains a dot '.' character, since most of the time this method is called with a filename that
        // doesn't match any of the filters.
        if(filename.indexOf('.')==-1)
            return false;
                
        int nbMappings = archiveFormatMappings.length;
        for(int i=0; i<nbMappings; i++) {
            if(archiveFormatMappings[i].filenameFilter.accept(filename))
                return true;
        }

        return false;
    }

    /**
     * Tests based on the given file's extension, if the file corresponds to a registered archive format.
     * If it does, an appropriate {@link AbstractArchiveFile} instance is created on top of the provided file
     * and returned. If it doesn't (the file's extension doesn't correspond to a registered archive format or is a
     * directory), the provided AbstractFile instance is returned.
     */
    public static AbstractFile wrapArchive(AbstractFile file) {
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

            int nbMappings = archiveFormatMappings.length;
            for(int i=0; i<nbMappings; i++) {
                if(archiveFormatMappings[i].filenameFilter.accept(filename)) {
                    try {
                        // Found one, create the AbstractArchiveFile instance and return it
                        archiveFile = (AbstractFile)archiveFormatMappings[i].providerConstructor.newInstance(new Object[]{file});

                        if(useCache) {
                            if(Debug.ON) Debug.trace("Adding archive file to cache: "+file.getAbsolutePath());
                            archiveFileCache.put(file.getAbsolutePath(), archiveFile);
                        }

                        return archiveFile;
                    }
                    catch(Exception e) {
                        if(Debug.ON) Debug.trace("Caught exception while trying to instanciate registered AbstractArchiveFile constructor: "+archiveFormatMappings[i]);
                    }
                }
            }
        }

        return file;
    }

//    public static AbstractFile wrapArchive(AbstractFile file) {
//        String filename = file.getName();
//
//        // Looks for an archive FilenameFilter that matches the given filename.
//        // Comparing the filename against each and every archive extension has a cost, so we only perform the test if
//        // the filename contains a dot '.' character, since most of the time this method is called with a filename that
//        // doesn't match any of the filters.
//        if(!file.isDirectory() && filename.indexOf('.')!=-1) {
//            int nbMappings = archiveFormatMappings.length;
//            for(int i=0; i<nbMappings; i++) {
//                if(archiveFormatMappings[i].filenameFilter.accept(filename)) {
//                    try {
//                        // Found one, create the AbstractArchiveFile instance and return it
//                        file = (AbstractFile)archiveFormatMappings[i].providerConstructor.newInstance(new Object[]{file});
//                        break;
//                    }
//                    catch(Exception e) {
//                        if(Debug.ON) Debug.trace("Caught exception while trying to instanciate registered AbstractArchiveFile constructor: "+archiveFormatMappings[i]);
//                    }
//                }
//            }
//        }
//
//        return file;
//    }

    /**
     * Returns an instance of the {@link AbstractTrash} implementation that can be used on the current platform,
     * or <code>null</code if none is available.
     *
     * @return an instance of the AbstractTrash implementation that can be used on the current platform, or null if
     * none is available. 
     */
    public static AbstractTrash getTrash() {
//        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X && OSXTrash.isAvailable()) {
        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X) {
            return new OSXTrash();
        }
        else if(PlatformManager.OS_FAMILY==PlatformManager.LINUX && PlatformManager.UNIX_DESKTOP==PlatformManager.KDE_DESKTOP) {
            return new KDETrash();
        }
        return null;
    }
}
