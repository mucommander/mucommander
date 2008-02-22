/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.file.icon.impl;

import com.mucommander.Debug;
import com.mucommander.cache.LRUCache;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.icon.CacheableFileIconProvider;
import com.mucommander.file.icon.CachedFileIconProvider;
import com.mucommander.file.icon.LocalFileIconProvider;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersion;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;

/**
 * Package-protected class which provides the {@link com.mucommander.file.icon.LocalFileIconProvider} and
 * {@link com.mucommander.file.icon.CacheableFileIconProvider} implementations to {@link SwingFileIconProvider}.
 *
 * @see SwingFileIconProvider
 * @author Maxence Bernard
 */
class SwingFileIconProviderImpl extends LocalFileIconProvider implements CacheableFileIconProvider {

    /** Swing object used to retrieve file icons, used on all platforms but Mac OS X */
    private static FileSystemView fileSystemView;

    /** Swing object used to retrieve file icons, used under Mac OS X only */
    private static JFileChooser fileChooser;

    /** Caches icons for directories, used only for non-local files */
    protected static LRUCache directoryIconCache = CachedFileIconProvider.createCache();

    /** Caches icons for regular files, used only for non-local files */
    protected static LRUCache fileIconCache = CachedFileIconProvider.createCache();

    /** True if init has been called */
    protected static boolean initialized;

    /** Transparent icon symbolizing symlinks, painted over an existing icon */
    public final static String SYMLINK_ICON_NAME = "link.png";


    /**
     * Initializes the Swing object used to retrieve the icon.
     * Note: instanciating this object is expensive (I/O bound) so we want to do that only if needed, and only once.
     */
    private static void init() {
        if(OsFamilies.MAC_OS_X.isCurrent())
            fileChooser = new JFileChooser();
        else
            fileSystemView = FileSystemView.getFileSystemView();
    }


    /**
     * Returns an icon for the given <code>java.io.File</code> using the underlying Swing provider component,
     * <code>null</code> in case of an error.
     *
     * @param javaIoFile the file for which to return an icon
     * @return an icon for the specified file, null in case of an unexpected error
     */
    private static Icon getSwingIcon(java.io.File javaIoFile) {
        try {
            if(fileSystemView!=null) {
                // FileSystemView.getSystemIcon() will behave in the following way if the specified file doesn't exist
                // when the icon is requested:
                //  - throw a NullPointerException (caused by a java.io.FileNotFoundException) => OK why not
                //  - dump the stack trace to System.err => bad! bad! bad!
                //
                // A way to workaround this odd behavior would be to test if the file exists when it is requested,
                // but a/ this is an expensive operation (especially under Windows) and b/ it wouldn't guarantee that
                // the file effectively exists when the icon is requested.
                // So the workaround here is to catch exceptions and disable System.err output during the call.

                Debug.setSystemErrEnabled(false);

                return fileSystemView.getSystemIcon(javaIoFile);
            }
            else {
                return fileChooser.getIcon(javaIoFile);
            }
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Caught exception while retrieving system icon for file "+ javaIoFile.getAbsolutePath()+" :"+e);
            return null;
        }
        finally {
            if(fileSystemView!=null)
                Debug.setSystemErrEnabled(true);
        }
    }


    /**
     * Returns an icon symbolizing a symlink to the given target icon.
     *
     * @param targetIcon the icon representing the symlink's target
     * @return an icon symbolizing a symlink to the given target
     */
    private static ImageIcon getSymlinkIcon(Icon targetIcon) {
        return IconManager.getCompositeIcon(targetIcon, IconManager.getIcon(IconManager.FILE_ICON_SET, SYMLINK_ICON_NAME));
    }


    //////////////////////////////////////////
    // LocalFileIconProvider implementation //
    //////////////////////////////////////////

    /**
     * <b>Implementation notes:</b> returns <code>false</code> (no caching) for:
     * <ul>
     *  <li>local files: their icons are cached by the Swing component that provides icons.</li>
     *  <li>symlinks: their icon cannot be cached using the file's extension as a key.</li>
     * </ul>
     * <code>true</code> is returned for non-local files that are not symlinks to avoid excessive temporary file
     * creation.
     */
    public boolean isCacheable(AbstractFile file, Dimension preferredResolution) {
        return !((file.getTopAncestor() instanceof LocalFile) || file.isSymlink());
    }

    public Icon lookupCache(AbstractFile file, Dimension preferredResolution) {
        // Under Mac OS X, return the icon of /Network for the root of remote (non-local) locations. 
        if(OsFamilies.MAC_OS_X.isCurrent() && !FileProtocols.FILE.equals(file.getURL().getProtocol()) && file.isRoot())
            return getSwingIcon(new java.io.File("/Network"));

        // Look for an existing icon instance for the file's extension
        return (Icon)(file.isDirectory()? directoryIconCache : fileIconCache).get(file.getExtension());
    }

    public void addToCache(AbstractFile file, Icon icon, Dimension preferredResolution) {
        // Map the extension onto the given icon
        (file.isDirectory()? directoryIconCache : fileIconCache).add(file.getExtension(), icon);
    }

    /**
     * <i>Implementation note:</i> only one resolution is available (usually 16x16) and blindly returned, the
     * <code>preferredResolution</code> argument is simply ignored.
     */
    public Icon getLocalFileIcon(LocalFile localFile, AbstractFile originalFile, Dimension preferredResolution) {
        if(!initialized) {
            // init() will be called once at most
            init();
            initialized = true;
        }

        // Retrieve the icon using the Swing provider component
        Icon icon = getSwingIcon((java.io.File)localFile.getUnderlyingFileObject());

        // Add a symlink indication to the icon if:
        // - the original file is a symlink AND
        //   - the original file is not a local file OR
        //   - the original file is a local file but the Swing component generates icons which do not have a symlink
        // indication. That is the case on Mac OS X 10.5 (regression, 10.4 did this just fine).
        //
        // Note that the symlink test is performed last because it is the most expensive.
        //
        if((!(originalFile.getTopAncestor() instanceof LocalFile) || (OsFamilies.MAC_OS_X.isCurrent() && OsVersion.MAC_OS_X_10_5.isCurrent()))
                && originalFile.isSymlink()) {
            icon = getSymlinkIcon(icon);
        }

        return icon;
    }
}
