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

package com.mucommander.file.icon;

import com.mucommander.cache.LRUCache;
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

    /** Default capacity of icon caches */
    public final static int DEFAULT_FILE_CACHE_CAPACITY = 100;

    /** Current capacity of icon caches */
    protected static int cacheCapacity = DEFAULT_FILE_CACHE_CAPACITY;


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
     * Creates and returns an {@link com.mucommander.cache.LRUCache} instance with a capacity equal to
     * {@link #getCacheCapacity()}.
     *
     * @return an LRUCache instance
     */
    public static LRUCache createCache() {
        return LRUCache.createInstance(cacheCapacity);
    }

    /**
     * Sets the capacity of the {@link LRUCache} that caches frequently accessed file instances. The more the capacity,
     * the more frequent the cache is hit but the higher the memory usage. By default, the capacity is
     * {@link #DEFAULT_FILE_CACHE_CAPACITY}.
     *
     * <p>Note that calling this method does not change the capacity of existing caches so to be effective, this method
     * must be called before {@link #createCache()} is called.</p>
     *
     * @param capacity the capacity of the LRU cache that caches frequently accessed icon instances
     * @see com.mucommander.cache.LRUCache
     */
    public static void setCacheCapacity(int capacity) {
        cacheCapacity = capacity;
    }

    /**
     * Returns the capacity of the {@link LRUCache} that caches frequently accessed icon instances. By default, the
     * capacity is {@link #DEFAULT_FILE_CACHE_CAPACITY}.
     *
     * @return the capacity of the LRU cache that caches frequently accessed icon instances
     */
    public static int getCacheCapacity() {
        return cacheCapacity;
    }


    /////////////////////////////////////
    // FileIconProvider implementation //
    /////////////////////////////////////

    /**
     * <i>Implementation notes</i>: this method first calls {@link CacheableFileIconProvider#isCacheable(com.mucommander.file.AbstractFile, java.awt.Dimension)}
     * to determine if the icon cache is used.
     *
     * <p><b>If the file icon is cacheable</b>, {@link CacheableFileIconProvider#lookupCache(com.mucommander.file.AbstractFile, java.awt.Dimension)}
     * is called to look for a previously cached icon. If a value is found, it is returned. If not,
     * {@link #getFileIcon(com.mucommander.file.AbstractFile, java.awt.Dimension)} is called on the <code>CacheableFileIconProvider</code>
     * to retrieve the icon. This icon is then added to the cache by calling
     * {@link CacheableFileIconProvider#addToCache(com.mucommander.file.AbstractFile, javax.swing.Icon, java.awt.Dimension)}.
     * </p>
     *
     * <p><b>If the file icon is not cacheable</b>, {@link #getFileIcon(com.mucommander.file.AbstractFile, java.awt.Dimension)}
     * is simply called on the <code>CacheableFileIconProvider</code> and its value returned.</p>
     */
    public Icon getFileIcon(AbstractFile file, Dimension preferredResolution) {
        Icon icon;
        boolean isCacheable = cacheableFip.isCacheable(file, preferredResolution);

        // Look for the file icon in the provider's cache
        if(isCacheable)
            icon = cacheableFip.lookupCache(file, preferredResolution);
        else
            icon = null;

        // Icon is not cacheable or isn't present in the cache, retrieve it from the provider
        if(icon==null) {
            icon = cacheableFip.getFileIcon(file, preferredResolution);

            // Cache the icon
            if(isCacheable && icon!=null)
                cacheableFip.addToCache(file, icon, preferredResolution);
        }

        return icon;
    }
}
