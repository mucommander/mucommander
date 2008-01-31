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

import com.apple.cocoa.application.NSBitmapImageRep;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSImageRep;
import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSData;
import com.mucommander.cache.LRUCache;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.icon.CacheableFileIconProvider;
import com.mucommander.file.icon.CachedFileIconProvider;
import com.mucommander.file.icon.LocalFileIconProvider;
import com.mucommander.file.impl.local.LocalFile;

import javax.swing.*;
import java.awt.*;

/**
 * Package-protected class which provides the {@link com.mucommander.file.icon.LocalFileIconProvider} and
 * {@link com.mucommander.file.icon.CacheableFileIconProvider} implementations to {@link CocoaFileIconProvider}.
 *
 * @see CocoaFileIconProvider
 * @author Maxence Bernard
 */
class CocoaFileIconProviderImpl extends LocalFileIconProvider implements CacheableFileIconProvider {

    /** Caches icons for directories, used only for non-local files */
    protected static LRUCache directoryIconCache = CachedFileIconProvider.createCache();

    /** Caches icons for regular files, used only for non-local files */
    protected static LRUCache fileIconCache = CachedFileIconProvider.createCache();

    
    /**
     * Returns the nearest dimension (width/height) to the given preferred resolution, among available icon dimensions.
     *
     * @param preferredResolution the preferred resolution for the icon
     * @return the nearest dimension (width/height) to the given preferred resolution
     */
    private static int getNearestDimension(Dimension preferredResolution) {
        int dim = Math.max(preferredResolution.width, preferredResolution.width);
        if(dim<=16)
            return 16;
        else if(dim<=32)
            return 32;
        else if(dim<=48)
            return 48;
        else
            return 128;

    }

    /**
     * Returns the key to be used to cache the given file's icon. Local files will return the given file's absolute
     * path where non-local files will return the file's extension.
     *
     * @param file the file for which the icon is to be cached
     * @return the key to be used to cache the given file's icon
     */
    private String getCacheKey(AbstractFile file) {
        return file.getTopAncestor() instanceof LocalFile ?file.getAbsolutePath():file.getExtension();
    }


    //////////////////////////////////////////
    // LocalFileIconProvider implementation //
    //////////////////////////////////////////

    public Icon getLocalFileIcon(LocalFile file, Dimension preferredResolution) {
        NSImage nsimage = NSWorkspace.sharedWorkspace().iconForFile(file.getAbsolutePath());
        // Note: NSWorkspace looked promising but didn't turn out to give good results (tested under OS X 10.4).
        // NSImage nsimage = NSWorkspace.sharedWorkspace().iconForFileType("."+file.getExtension());

        NSBitmapImageRep bmRep = null;
        NSArray repr = nsimage.representations();

        int nearestDim = getNearestDimension(preferredResolution);
        int nearestAvailableDim = 0;
        int dim;

        // Iterate through all available image resolutions and look for the best dimension
        for(int i = 0; i<repr.count(); i++) {
            NSImageRep imRep = (NSImageRep)repr.objectAtIndex(i);
            if((imRep instanceof NSBitmapImageRep) && (dim=imRep.pixelsWide())<=nearestDim && dim>nearestAvailableDim) {
                bmRep = (NSBitmapImageRep)imRep;
                break;
            }
        }

//if(Debug.ON) Debug.trace("preferredResolution"+preferredResolution+" bestDim="+bestDim+" bestAvailableDim="+bestAvailableDim);
        if(bmRep == null)
            return null;

        NSData pngData = bmRep.representationUsingType(NSBitmapImageRep.PNGFileType,null);
        try{
            return new ImageIcon(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(pngData.bytes(0, pngData.length()))));
        }
        catch(Exception e) {
            return null;
        }
    }


    //////////////////////////////////////////////
    // CacheableFileIconProvider implementation //
    //////////////////////////////////////////////

    public Icon lookupCache(AbstractFile file, Dimension preferredResolution) {
//if(Debug.ON) Debug.trace("key="+getCacheKey(file)+"cache hit="+((isDirectory? directoryIconCache : fileIconCache).get(getCacheKey(file))!=null));
        CacheItem cacheItem = (CacheItem)(file.isDirectory()? directoryIconCache : fileIconCache).get(getCacheKey(file));

        // Return the cached icon (if any) only if the requested preferred resolution matches the one that was
        // originally requested when the icon was created. Not doing so would have this method return icons that have
        // a different resolution from what would normally be returned by getLocalFileIcon
        return cacheItem!=null && cacheItem.preferredDimension.equals(preferredResolution)?cacheItem.icon:null;
    }

    public void addToCache(AbstractFile file, Icon icon, Dimension preferredResolution) {
//if(Debug.ON) Debug.trace("key="+getCacheKey(file));

        // Wrap the icon and preferred dimension in a CacheItem. This allows to return a cached icon only if the
        // requested preferred resolution matches the one that was originally requested when the icon was created
        (file.isDirectory()? directoryIconCache : fileIconCache).add(getCacheKey(file), new CacheItem(icon, preferredResolution));
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * This class wraps the icon and preferred dimension in a CacheItem. This allows to return a cached icon only if
     * the requested preferred resolution matches the one that was originally requested when the icon was created.
     */
    private static class CacheItem {
        private Icon icon;
        private Dimension preferredDimension;

        private CacheItem(Icon icon, Dimension preferredDimension) {
            this.icon = icon;
            this.preferredDimension = preferredDimension;
        }
    }
}
