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

package com.mucommander.file.icon.impl;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.cache.LRUCache;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.icon.CacheableFileIconProvider;
import com.mucommander.file.icon.CachedFileIconProvider;
import com.mucommander.file.icon.LocalFileIconProvider;
import com.mucommander.file.impl.local.LocalFile;

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
    protected static LRUCache directoryIconCache = CachedFileIconProvider.createCacheInstance();

    /** Caches icons for regular files, used only for non-local files */
    protected static LRUCache fileIconCache = CachedFileIconProvider.createCacheInstance();


    static {
        // Initialize the Swing object that is used to retrieve file icons.
        // Note that the constructor of those objects is very expensive so we really want to instanciate only once.
        if(PlatformManager.getOsFamily()==PlatformManager.MAC_OS_X)
            fileChooser = new JFileChooser();
        else
            fileSystemView = FileSystemView.getFileSystemView();
    }


    //////////////////////////////////////////
    // LocalFileIconProvider implementation //
    //////////////////////////////////////////

    /**
     * Implementation notes: only non-local icons are cached to avoid excessive temporary file creation.
     * Local icons are cached by the Swing component used to retrieve icons.
     */
    public Icon lookupCache(AbstractFile file, Dimension preferredResolution) {
        // Do not use cache for local files, the Swing object already caches icons
        if(file.getTopAncestor() instanceof LocalFile)
            return null;

        // Look for an existing icon instance for the file's extension
        return (Icon)(file.isDirectory()? directoryIconCache : fileIconCache).get(file.getExtension());
    }

    /**
     * Implementation notes: only non-local icons are cached to avoid excessive temporary file creation.
     * Local icons are cached by the Swing component used to retrieve icons.
     */
    public void addToCache(AbstractFile file, Icon icon, Dimension preferredResolution) {
        // Do not use cache for local files, the Swing object already caches icons
        if(!(file.getTopAncestor() instanceof LocalFile)) {
            // Map the extension onto the given icon
            (file.isDirectory()? directoryIconCache : fileIconCache).add(file.getExtension(), icon);
        }
    }

    /**
     * <i>Implementation note:</i> only one resolution is available (usually 16x16) and blindly returned, the
     * <code>preferredResolution</code> argument is simply ignored.
     */
    public Icon getLocalFileIcon(LocalFile file, Dimension preferredResolution) {
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

                return fileSystemView.getSystemIcon((java.io.File)file.getUnderlyingFileObject());
            }
            else {
                return fileChooser.getIcon((java.io.File)file.getUnderlyingFileObject());
            }
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Caught exception while retrieving system icon for file "+file.getAbsolutePath()+" :"+e);
            return null;
        }
        finally {
            Debug.setSystemErrEnabled(true);
        }

    }
}
