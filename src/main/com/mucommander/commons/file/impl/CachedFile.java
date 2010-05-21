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


package com.mucommander.commons.file.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.local.LocalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * CachedFile is a ProxyFile that caches the return values of most {@link AbstractFile} getter methods. This allows
 * to limit the number of calls to the underlying file methods which can have a cost since they often are I/O bound.
 * The methods that are cached are those overridden by this class, except for the <code>ls</code> methods, which are
 * overridden only to allow recursion (see {@link #CachedFile(com.mucommander.commons.file.AbstractFile, boolean)}).
 *
 * <p>The values are retrieved and cached only when the 'cached methods' are called for the first time; they are
 * not preemptively retrieved in the constructor, so using this class has no negative impact on performance,
 * except for the small extra CPU cost added by proxying the methods and the extra RAM used to store cached values.
 *
 * <p>Once the values are retrieved and cached, they never change: the same value will always be returned once a method
 * has been called for the first time. That means if the underlying file changes (e.g. its size or date has changed),
 * the changes will not be reflected by this CachedFile. Thus, this class should only be used when a 'real-time' view
 * of the file is not required, or when the file instance is used only for a small amount of time.
 *
 * @author Maxence Bernard
 */
public class CachedFile extends ProxyFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedFile.class);

    /** If true, AbstractFile instances returned by this class will be wrapped into CachedFile instances */
    private boolean recurseInstances;

    ///////////////////
    // Cached values //
    ///////////////////
    
    private long getSize;
    private boolean getSizeSet;

    private long getDate;
    private boolean getDateSet;

    private boolean isSymlink;
    private boolean isSymlinkSet;

    private boolean isDirectory;
    private boolean isDirectorySet;

    private boolean isArchive;
    private boolean isArchiveSet;

    private boolean isHidden;
    private boolean isHiddenSet;

    private String getAbsolutePath;
    private boolean getAbsolutePathSet;

    private String getCanonicalPath;
    private boolean getCanonicalPathSet;

    private String getExtension;
    private boolean getExtensionSet;

    private String getName;
    private boolean getNameSet;

    private long getFreeSpace;
    private boolean getFreeSpaceSet;

    private long getTotalSpace;
    private boolean getTotalSpaceSet;

    private boolean exists;
    private boolean existsSet;

    private FilePermissions getPermissions;
    private boolean getPermissionsSet;

    private String getPermissionsString;
    private boolean getPermissionsStringSet;

    private String getOwner;
    private boolean getOwnerSet;

    private String getGroup;
    private boolean getGroupSet;

    private boolean isRoot;
    private boolean isRootSet;

    private AbstractFile getParent;
    private boolean getParentSet;

    private AbstractFile getRoot;
    private boolean getRootSet;

    private AbstractFile getCanonicalFile;
    private boolean getCanonicalFileSet;

    // Used to access the java.io.FileSystem#getBooleanAttributes method
    private static boolean getFileAttributesAvailable;
    private static Method mGetBooleanAttributes;
    private static int BA_DIRECTORY, BA_EXISTS, BA_HIDDEN;
    private static Object fs;

    static {
        // Exposes the java.io.FileSystem class which by default has package access, in order to use its
        // 'getBooleanAttributes' method to speed up access to file attributes under Windows.
        // This method allows to retrieve the values of the 'exists', 'isDirectory' and 'isHidden' attributes in one
        // pass, resolving the underlying file only once instead of 3 times. Since resolving a file is a particularly
        // expensive operation under Windows due to improper use of the Win32 API, this helps speed things up a little.
        // References:
        //  - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5036988
        //  - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6240028
        //
        // This hack was made for Windows, but is now used for other platforms as well as it is necessarily faster than
        // retrieving file attributes individually.

        try {
            // Resolve FileSystem class, 'getBooleanAttributes' method and fields
            Class<?> cFile = File.class;
            Class<?> cFileSystem = Class.forName("java.io.FileSystem");
            mGetBooleanAttributes = cFileSystem.getDeclaredMethod("getBooleanAttributes", new Class [] {cFile});
            Field fBA_EXISTS = cFileSystem.getDeclaredField("BA_EXISTS");
            Field fBA_DIRECTORY = cFileSystem.getDeclaredField("BA_DIRECTORY");
            Field fBA_HIDDEN = cFileSystem.getDeclaredField("BA_HIDDEN");
            Field fFs = cFile.getDeclaredField("fs");

            // Allow access to the 'getBooleanAttributes' method and to the fields we're interested in
            mGetBooleanAttributes.setAccessible(true);
            fFs.setAccessible(true);
            fBA_EXISTS.setAccessible(true);
            fBA_DIRECTORY.setAccessible(true);
            fBA_HIDDEN.setAccessible(true);

            // Retrieve constant field values once for all
            BA_EXISTS = (Integer) fBA_EXISTS.get(null);
            BA_DIRECTORY = (Integer) fBA_DIRECTORY.get(null);
            BA_HIDDEN = (Integer) fBA_HIDDEN.get(null);
            fs = fFs.get(null);

            getFileAttributesAvailable = true;
            LOGGER.trace("Access to java.io.FileSystem granted");
        }
        catch(Exception e) {
            LOGGER.info("Error while allowing access to java.io.FileSystem", e);
        }
    }


    /**
     * Creates a new CachedFile instance around the specified AbstractFile, caching returned values of cached methods
     * as they are called. If recursion is enabled, the methods returning AbstractFile will return CachedFile instances,
     * allowing the cache files recursively.
     *
     * @param file the AbstractFile instance for which returned values of getter methods should be cached
     * @param recursiveInstances if true, AbstractFile instances returned by this class will be wrapped into CachedFile instances
     */
    public CachedFile(AbstractFile file, boolean recursiveInstances) {
        super(file);

        this.recurseInstances = recursiveInstances;
    }


    /**
     * Creates a CachedFile instance for each of the AbstractFile instances in the given array.
     */
    private AbstractFile[] createCachedFiles(AbstractFile files[]) {
        int nbFiles = files.length;
        for(int i=0; i<nbFiles; i++)
            files[i] = new CachedFile(files[i], true);

        return files;
    }


    /**
     * Pre-fetches values of {@link #isDirectory}, {@link #exists} and {@link #isHidden} for the given local file,
     * using the <code>java.io.FileSystem#getBooleanAttributes(java.io.File)</code> method.
     * The given {@link AbstractFile} must be a local file or a proxy to a local file ('file' protocol). This method
     * must only be called if the {@link #getFileAttributesAvailable} field is <code>true</code>.
     */
    private void getFileAttributes(AbstractFile file) {
        file = file.getTopAncestor();

        if(file instanceof LocalFile) {
            try {
                int ba = (Integer) mGetBooleanAttributes.invoke(fs, new Object[]{file.getUnderlyingFileObject()});

                isDirectory = (ba & BA_DIRECTORY)!=0;
                isDirectorySet = true;

                exists = (ba & BA_EXISTS)!=0;
                existsSet = true;

                isHidden = (ba & BA_HIDDEN)!=0;
                isHiddenSet = true;
            }
            catch(Exception e) {
                LOGGER.info("Could not retrieve file attributes for {}", file, e);
            }
        }
    }


    ////////////////////////////////////////////////////
    // Overridden methods to cache their return value //
    ////////////////////////////////////////////////////

    @Override
    public long getSize() {
        if(!getSizeSet) {
            getSize = file.getSize();
            getSizeSet = true;
        }

        return getSize;
    }

    @Override
    public long getDate() {
        if(!getDateSet) {
            getDate = file.getDate();
            getDateSet = true;
        }

        return getDate;
    }

    @Override
    public boolean isSymlink() {
        if(!isSymlinkSet) {
            isSymlink = file.isSymlink();
            isSymlinkSet = true;
        }

        return isSymlink;
    }

    @Override
    public boolean isDirectory() {
        if(!isDirectorySet && getFileAttributesAvailable && FileProtocols.FILE.equals(file.getURL().getScheme()))
            getFileAttributes(file);
        // Note: getFileAttributes() might fail to retrieve file attributes, so we need to test isDirectorySet again

        if(!isDirectorySet) {
            isDirectory = file.isDirectory();
            isDirectorySet = true;
        }

        return isDirectory;
    }

    @Override
    public boolean isArchive() {
        if(!isArchiveSet) {
            isArchive = file.isArchive();
            isArchiveSet = true;
        }

        return isArchive;
    }

    @Override
    public boolean isHidden() {
        if(!isHiddenSet && getFileAttributesAvailable && FileProtocols.FILE.equals(file.getURL().getScheme()))
            getFileAttributes(file);
        // Note: getFileAttributes() might fail to retrieve file attributes, so we need to test isDirectorySet again

        if(!isHiddenSet) {
            isHidden = file.isHidden();
            isHiddenSet = true;
        }

        return isHidden;
    }

    @Override
    public String getAbsolutePath() {
        if(!getAbsolutePathSet) {
            getAbsolutePath = file.getAbsolutePath();
            getAbsolutePathSet = true;
        }

        return getAbsolutePath;
    }

    @Override
    public String getCanonicalPath() {
        if(!getCanonicalPathSet) {
            getCanonicalPath = file.getCanonicalPath();
            getCanonicalPathSet = true;
        }

        return getCanonicalPath;
    }

    @Override
    public String getExtension() {
        if(!getExtensionSet) {
            getExtension = file.getExtension();
            getExtensionSet = true;
        }

        return getExtension;
    }

    @Override
    public String getName() {
        if(!getNameSet) {
            getName = file.getName();
            getNameSet = true;
        }

        return getName;
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        if(!getFreeSpaceSet) {
            getFreeSpace = file.getFreeSpace();
            getFreeSpaceSet = true;
        }

        return getFreeSpace;
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        if(!getTotalSpaceSet) {
            getTotalSpace = file.getTotalSpace();
            getTotalSpaceSet = true;
        }

        return getTotalSpace;
    }

    @Override
    public boolean exists() {
        if(!existsSet && getFileAttributesAvailable && FileProtocols.FILE.equals(file.getURL().getScheme()))
            getFileAttributes(file);
        // Note: getFileAttributes() might fail to retrieve file attributes, so we need to test isDirectorySet again

        if(!existsSet) {
            exists = file.exists();
            existsSet = true;
        }

        return exists;
    }

    @Override
    public FilePermissions getPermissions() {
        if(!getPermissionsSet) {
            getPermissions = file.getPermissions();
            getPermissionsSet = true;
        }

        return getPermissions;
    }

    @Override
    public String getPermissionsString() {
        if(!getPermissionsStringSet) {
            getPermissionsString = file.getPermissionsString();
            getPermissionsStringSet = true;
        }

        return getPermissionsString;
    }

    @Override
    public String getOwner() {
        if(!getOwnerSet) {
            getOwner = file.getOwner();
            getOwnerSet = true;
        }

        return getOwner;
    }

    @Override
    public String getGroup() {
        if(!getGroupSet) {
            getGroup = file.getGroup();
            getGroupSet = true;
        }

        return getGroup;
    }

    @Override
    public boolean isRoot() {
        if(!isRootSet) {
            isRoot = file.isRoot();
            isRootSet = true;
        }

        return isRoot;
    }


    @Override
    public AbstractFile getParent() {
        if(!getParentSet) {
            getParent = file.getParent();
            // Create a CachedFile instance around the file if recursion is enabled
            if(recurseInstances && getParent!=null)
                getParent = new CachedFile(getParent, true);
            getParentSet = true;
        }

        return getParent;
    }

    @Override
    public AbstractFile getRoot() {
        if(!getRootSet) {
            getRoot = file.getRoot();
            // Create a CachedFile instance around the file if recursion is enabled
            if(recurseInstances)
                getRoot = new CachedFile(getRoot, true);

            getRootSet = true;
        }

        return getRoot;
    }

    @Override
    public AbstractFile getCanonicalFile() {
        if(!getCanonicalFileSet) {
            getCanonicalFile = file.getCanonicalFile();
            // Create a CachedFile instance around the file if recursion is enabled
            if(recurseInstances) {
                // AbstractFile#getCanonicalFile() may return 'this' if the file is not a symlink. In that case,
                // no need to create a new CachedFile, simply use this one. 
                if(getCanonicalFile==file)
                    getCanonicalFile = this;
                else
                    getCanonicalFile = new CachedFile(getCanonicalFile, true);
            }

            getCanonicalFileSet = true;
        }

        return getCanonicalFile;
    }

    
    ////////////////////////////////////////////////
    // Overridden for recursion only (no caching) //
    ////////////////////////////////////////////////

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        // Don't cache ls() result but create a CachedFile instance around each of the files if recursion is enabled
        AbstractFile files[] = file.ls();

        if(recurseInstances)
            return createCachedFiles(files);

        return files;
    }

    @Override
    public AbstractFile[] ls(FileFilter filter) throws IOException, UnsupportedFileOperationException {
        // Don't cache ls() result but create a CachedFile instance around each of the files if recursion is enabled
        AbstractFile files[] = file.ls(filter);

        if(recurseInstances)
            return createCachedFiles(files);

        return files;
    }

    @Override
    public AbstractFile[] ls(FilenameFilter filter) throws IOException, UnsupportedFileOperationException {
        // Don't cache ls() result but create a CachedFile instance around each of the files if recursion is enabled
        AbstractFile files[] = file.ls(filter);

        if(recurseInstances)
            return createCachedFiles(files);

        return files;
    }
}
