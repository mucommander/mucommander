/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.main.tree;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.util.FileComparator;

import javax.swing.event.EventListenerList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds cached directories. 
 * It maps AbstractFiles to DirectoryCache instances.
 * @author Mariusz Jakubowski
 *
 */
public class DirectoryCache {
    
    /** a map that holds cached folders */
    private Map<AbstractFile, CachedDirectory> cache;
    
    /** Comparator used to sort folders */
    private FileComparator sort;

    /** A file filter */
    private FileFilter filter;

    /** Listeners. */
    protected EventListenerList listenerList = new EventListenerList();


    /**
     * Creates a new directory cache.
     * @param filter filter used to filter children directories.
     * @param sort a comparator used to sort children
     */
    public DirectoryCache(FileFilter filter, FileComparator sort) {
        //this.cache = Collections.synchronizedMap(new HashMap());
        this.cache = new HashMap<AbstractFile, CachedDirectory>();
        this.filter = filter;
        this.sort = sort;
    }

    /**
     * Returns current sort order.
     */
    public FileComparator getSort() {
        return sort;
    }

    /**
     * Returns current filter.
     */
    public FileFilter getFilter() {
        return filter;
    }

    /**
     * Fires a cachingStarted or cachingEnded event on all listeners.
     * @param cachedDirectory a directory those children has been cached
     * @param readingChildren 
     */
    public void fireChildrenCached(CachedDirectory cachedDirectory, boolean readingChildren) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CachedDirectoryListener.class) {
                if (readingChildren) {
                    ((CachedDirectoryListener) listeners[i + 1]).cachingStarted(cachedDirectory);
                } else {
                    ((CachedDirectoryListener) listeners[i + 1]).cachingEnded(cachedDirectory);
                }
            }
        }
    }
    
    public void addCachedDirectoryListener(CachedDirectoryListener l) {
        listenerList.add(CachedDirectoryListener.class, l);
    }

    public void removeCachedDirectoryListener(CachedDirectoryListener l) {
        listenerList.remove(CachedDirectoryListener.class, l);
    }

    public synchronized void clear() {
        cache.clear();
    }

    public synchronized CachedDirectory get(AbstractFile key) {
        return cache.get(key);
    }

    public synchronized void put(AbstractFile key, CachedDirectory value) {
        cache.put(key, value);
    }
    
    /**
     * Deletes entry and all children from the cache.
     */
    public synchronized void removeWithChildren(AbstractFile key) {
        CachedDirectory cachedDir = cache.get(key);
        if (cachedDir != null) {
            cache.remove(key);
            AbstractFile[] children = cachedDir.get();
            if (children != null) {
                for (AbstractFile child : children) {
                    removeWithChildren(child);
                }
            }
        }
    }
    
    /**
     * Gets a cached instance of a file. If the cached instance
     * of the file doesn't exists it's added to the cache.
     * @param key an AbstractFile instance
     * @return a cached file instance
     */
    public synchronized CachedDirectory getOrAdd(AbstractFile key) {
        CachedDirectory cachedDir = cache.get(key);
        if (cachedDir == null) {
            cachedDir = new CachedDirectory(key, this);
            cache.put(key, cachedDir);
        }
        return cachedDir;
    }
   

}
