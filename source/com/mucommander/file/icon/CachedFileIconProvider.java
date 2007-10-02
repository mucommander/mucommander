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

package com.mucommander.file.icon;

import com.mucommander.cache.LRUCache;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;

import javax.swing.*;
import java.awt.*;

/**
 * <code>CachedFileIconProvider</code> is a <code>FileIconProvider</code> with caching capabilities.
 *
 * <p>This class does not actually provide icons nor does it manage the contents of the cache ; it delegates these tasks
 * to a {@link CacheableFileIconProvider} instance. All this class does is use the cache implementation to harness its
 * befinits and take all the credit for it.</br>
 * When an icon is requested, a cache lookup is performed. If a cached value is found, it is returned. If not, the icon
 * is fetched from the underlying provider and added to the cache.</p>
 *
 * @author Maxence Bernard
 */
public class CachedFileIconProvider implements FileIconProvider {

    /** The underlying icon provider and cache manager */
    protected CacheableFileIconProvider cacheableFip;


    /**
     * Creates a new CachedFileIconProvider that uses the given {@link CacheableFileIconProvider} to access the cache
     * and retrieve the icons.
     *
     * @param cacheableFip the underlying icon provider and cache manager
     */
    public CachedFileIconProvider(CacheableFileIconProvider cacheableFip) {
        this.cacheableFip = cacheableFip;
    }

    /**
     * Creates and returns an {@link com.mucommander.cache.LRUCache} instance, with a cache capacity defined
     * by the {@link com.mucommander.conf.impl.MuConfiguration#SYSTEM_ICON_CACHE_CAPACITY} configuration variable which
     * value defaults to {@link com.mucommander.conf.impl.MuConfiguration#DEFAULT_SYSTEM_ICON_CACHE_CAPACITY}.
     *
     * @return Creates and returns an LRUCache instance
     */
    public static LRUCache createCacheInstance() {
        return LRUCache.createInstance(
                MuConfiguration.getVariable(MuConfiguration.SYSTEM_ICON_CACHE_CAPACITY,
                                            MuConfiguration.DEFAULT_SYSTEM_ICON_CACHE_CAPACITY)
        );
    }


    /////////////////////////////////////
    // FileIconProvider implementation //
    /////////////////////////////////////

    /**
     * <i>Implementation notes</i>: this method first calls {@link CacheableFileIconProvider#lookupCache(com.mucommander.file.AbstractFile, java.awt.Dimension)}
     * to look for a matching cached icon. If a value is found, it is returned. If not, <code>CacheableFileIconProvider</code>'s
     * {@link #getFileIcon(com.mucommander.file.AbstractFile, java.awt.Dimension)} is called to retrieve the icon.
     * Before being returned, this icon is added to the cache by calling {@link CacheableFileIconProvider#addToCache(com.mucommander.file.AbstractFile, javax.swing.Icon, java.awt.Dimension)}.
     */
    public Icon getFileIcon(AbstractFile file, Dimension preferredResolution) {
        // Look for the file icon in the provider's cache
        Icon icon = cacheableFip.lookupCache(file, preferredResolution);

        if(icon==null) {
            // Icon isn't present in cache, retrieve it from the provider
            icon = cacheableFip.getFileIcon(file, preferredResolution);

            // Cache the icon
            if(icon!=null)
                cacheableFip.addToCache(file, icon, preferredResolution);
        }

        return icon;
    }
}
