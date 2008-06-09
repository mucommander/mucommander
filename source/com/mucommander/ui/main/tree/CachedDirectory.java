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


package com.mucommander.ui.main.tree;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.impl.ProxyFile;
import com.mucommander.ui.icon.FileIcons;

import javax.swing.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A class that holds cached children of a directory.
 * 
 * @author Mariusz Jakubowski
 * 
 */
public class CachedDirectory extends ProxyFile {
    /** an array of cached children */
    private AbstractFile[] cachedChildren = null;
    
    /** a flag indicating that a thread is running, caching children */
    private boolean readingChildren = false;
    
    /** a timestamp of last modification time of this directory */
    private long lsTimeStamp = -1;
    
    /** a cache in which this object is stored */
    private DirectoryCache cache;

    private Icon cachedIcon;
    

    /**
     * Creates a new instance.
     * 
     * @param directory a directory to cache
     */
    public CachedDirectory(AbstractFile directory, DirectoryCache cache) {
        super(directory);
        this.cache = cache;
    }

    /**
     * Checks if this directory is already cached. If it isn't cached then a new
     * cache thread is started.
     * @return true if directory is cached, false otherwise
     */
    public synchronized boolean isCached() {
        // check if caching thread is running
        if (isReadingChildren()) {
            return false;
        }
        // check if directory contents changed
        if (lsTimeStamp != file.getDate()) {
            // start new caching thread
            setReadingChildren(true);
            Thread lsThread = new Thread() {
                public void run() {
                    lsAsync();
                }
            };
            lsThread.start();
            return false;
        }
        return true;
    }

    /**
     * Gets children of current directory. Files are filtered and then sorted. This
     * method is executed by caching thread.
     */
    private void lsAsync() {
        try {
            final AbstractFile[] children = file.ls(cache.getFilter());
            Arrays.sort(children, cache.getSort());
            for (int i = 0; i < children.length; i++) {
                CachedDirectory cachedChild = new CachedDirectory(children[i], cache);
                cache.put(children[i], cachedChild);
                cachedChild.setCachedIcon(FileIcons.getFileIcon(children[i]));
            }
            try {
                /*
                 * Set cache to new value. This is invoked in swing thread
                 * so event listeners are called from right thread. 
                 */
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        setLsCache(children, file.getDate());
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sets cache information.
     * @param children array of children of this directory
     * @param lsTimeStamp timestamp of cache
     */
    private synchronized void setLsCache(AbstractFile[] children, long lsTimeStamp) {
        this.lsTimeStamp = lsTimeStamp;
        this.cachedChildren = children;
        setReadingChildren(false);
    }

    /**
     * Returns true if caching thread is running.
     */
    public synchronized boolean isReadingChildren() {
        return readingChildren;
    }

    /**
     * Sets a flag that indicates if caching thread is running. This method also
     * initializes spinning icon.
     * @param readingChildren
     */
    private synchronized void setReadingChildren(boolean readingChildren) {
        this.readingChildren = readingChildren;
        cache.fireChildrenCached(this, readingChildren);
    }

    /**
     * Gets cached children.
     * @return cached children.
     */
    public synchronized AbstractFile[] get() {
        return cachedChildren;
    }
    
    public Icon getCachedIcon() {
        return cachedIcon;
    }
    
    public void setCachedIcon(Icon cachedIcon) {
        this.cachedIcon = cachedIcon;
    }

}
