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

import com.mucommander.file.AbstractFile;

import javax.swing.*;
import java.awt.*;

/**
 * <code>CacheableFileIconProvider</code> is an interface to be implemented by file icon providers that wish to use
 * some icon caching to improve performance. This interface is to be used in conjunction with {@link CachedFileIconProvider}
 * to form a functional cached provider.
 *
 * @author Maxence Bernard
 */
public interface CacheableFileIconProvider extends FileIconProvider {

    /**
     * This method is called by {@link CachedFileIconProvider#getFileIcon(com.mucommander.file.AbstractFile, java.awt.Dimension)}
     * each time an icon is requested, to perform a cache lookup and give implementations a chance to re-use a cached icon.
     * If a non-null value is returned, the cached icon will be used.
     * On the other hand, if <code>null</code> is returned, the icon will be fetched by calling {@link #getFileIcon(com.mucommander.file.AbstractFile, java.awt.Dimension)}
     * followed by a call to {@link #addToCache(com.mucommander.file.AbstractFile, javax.swing.Icon,java.awt.Dimension)}
     * to add the freshly-retrieved icon to the cache.<br/>
     *
     * @param file the file for which to look for a cached icon
     * @param preferredResolution the preferred icon resolution
     * @return a cached icon to re-use, null if there is none
     */
    public abstract Icon lookupCache(AbstractFile file, Dimension preferredResolution);

    /**
     * This method is called by {@link FileIconProvider#getFileIcon(com.mucommander.file.AbstractFile, java.awt.Dimension)} each time
     * an icon has been fetched with {@link #getFileIcon(com.mucommander.file.AbstractFile, java.awt.Dimension)}
     * to give implementations a chance to store this icon in a cache and return it later when {@link #lookupCache(com.mucommander.file.AbstractFile, java.awt.Dimension)}
     * is called.<br/>
     * There is no obligation to cache the given icon, implementations may freely choose whether to cache a particular
     * icon or not.
     *
     * @param file the file that corresponds to the given icon
     * @param icon the icon to add to the cache
     * @param preferredResolution the preferred icon resolution that was originally requested
     */
    public abstract void addToCache(AbstractFile file, Icon icon, Dimension preferredResolution);

}
