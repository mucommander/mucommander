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

import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.ProxyFile;
import com.mucommander.ui.icon.CustomFileIconProvider;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;

/**
 * A class that holds cached children of a directory.
 * 
 * @author Mariusz Jakubowski
 * 
 */
public class CachedDirectory extends ProxyFile {
	private static final Logger LOGGER = LoggerFactory.getLogger(CachedDirectory.class);
	
    private static final ImageIcon NOT_ACCESSIBLE_ICON = IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.NOT_ACCESSIBLE_FILE);

    /** an array of cached children */
    private AbstractFile[] cachedChildren = null;
    
    /** a flag indicating that a thread is running, caching children */
    private boolean readingChildren = false;
    
    /** a timestamp of last modification time of this directory */
    private long lsTimeStamp = -1;
    
    /** a cache in which this object is stored */
    private DirectoryCache cache;

    /** a cached icon */
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
            setReadingChildren(true);
            // read children in caching thread
            TreeIOThreadManager.getInstance().addTask(new Runnable() {
                public void run() {
                    lsAsync();
                }
            });
            return false;
        }
        return true;
    }

    /**
     * Gets children of current directory. Files are filtered and then sorted. This
     * method is executed in caching thread.
     */
    private void lsAsync() {
        if (getCachedIcon() == null || getCachedIcon() == NOT_ACCESSIBLE_ICON) {
            setCachedIcon(FileIcons.getFileIcon(getProxiedFile()));
        }

        AbstractFile[] children;
        try {
            children = file.ls(cache.getFilter());
        } catch (Exception e) {
            LOGGER.debug("Caught exception", e);
            children = new AbstractFile[0];
            setCachedIcon(NOT_ACCESSIBLE_ICON);
        }

        Arrays.sort(children, cache.getSort());
        Icon icons[] = new Icon[children.length];
        for (int i = 0; i < children.length; i++) {
            icons[i] = FileIcons.getFileIcon(children[i]);
        }
        synchronized (cache) {
            for (int i = 0; i < children.length; i++) {
                CachedDirectory cachedChild = cache.getOrAdd(children[i]);
                cachedChild.setCachedIcon(icons[i]);
            }
        }
        
        final AbstractFile[] children2 = children;
        try {
            /*
             * Set cache to new value. This is invoked in swing thread
             * so event listeners are called from right thread. 
             */
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    setLsCache(children2, file.getDate());
                }
            });
        } catch (Exception e) {
            LOGGER.debug("Caught exception", e);
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
    
    /**
     * Gets a cached icon for this folder. 
     * @return a cached icon
     */
    public Icon getCachedIcon() {
        return cachedIcon;
    }
    
    /**
     * Sets a cached icon for this folder.
     * @param cachedIcon a cached icon
     */
    public void setCachedIcon(Icon cachedIcon) {
        this.cachedIcon = cachedIcon;
    }

}
