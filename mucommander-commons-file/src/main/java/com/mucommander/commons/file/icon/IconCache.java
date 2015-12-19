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


package com.mucommander.commons.file.icon;

import org.apache.commons.collections.map.ReferenceMap;

import javax.swing.*;

/**
 * This class provides a icon cache, mapping <code>Object</code> keys onto {@link Icon} instances.
 * Any kind of Object may be used as the key: a file, a URL, an extension, ... allowing different of icon caching
 * strategies to be implemented.
 *
 * <p>Icons are stored as {@link java.lang.ref.SoftReference soft references} so they can be garbage collected 
 * when the VM runs low on memory.</p>
 *
 * <p>The implementation uses the {@link ReferenceMap} class part of the <code>Apache Commons Collection</code> library.
 * All accesses to the underlying map is synchronized, making this cache thread-safe.</p>
 *
 * @author Maxence Bernard
 */
public class IconCache {

   /** The actual hash map */
    protected final ReferenceMap hashMap = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT);

    /**
     * Creates a new icon cache.
     */
    public IconCache() {
    }

    /**
     * Adds a new key/icon mapping to the cache. If a mapping with the same key exists, it is replaced and the previous
     * value returned.
     *
     * @param key the key that will later allow to retrieve the cached icon
     * @param value the icon instance to cache
     * @return returns the icon instance previously mapped onto the given key, <code>null</code> if no
     * such mapping existed
     */
    public synchronized Icon put(Object key, Icon value) {
        return (Icon)hashMap.put(key, value);
    }

    /**
     * Returns the {@link Icon} instance mapped onto the given key if there is one,
     * <code>null</code> otherwise
     *
     * @param key key of the icon instance to retrieve
     * @return the {@link Icon} instance mapped onto the given key if there is one,
     * <code>null</code> otherwise
     */
    public synchronized Icon get(Object key) {
        return (Icon)hashMap.get(key);
    }

    /**
     * Returns <code>true</code> if this cache currently contains a key/icon mapping where the given key is used as
     * the mapping's key.
     *
     * @param key key to lookup
     * @return <code>true</code> if this cache currently contains a key/icon mapping where the given key is used as
     * the mapping's key.
     */
    public synchronized boolean containsKey(Object key) {
        return hashMap.containsKey(key);
    }

    /**
     * Returns <code>true</code> if this cache currently contains a key/icon mapping where the given icon is used as
     * the mapping's value.
     *
     * @param icon icon to lookup
     * @return <code>true</code> if this cache currently contains a key/icon mapping where the given icon is used as
     * the mapping's key.
     */
    public synchronized boolean containsValue(Icon icon) {
        return hashMap.containsValue(icon);
    }

    /**
     * Removes all existing key/icon mapping from this cache, leaving the cache in the same state as it was right after
     * its creation.
     */
    public synchronized void clear() {
        hashMap.clear();
    }

    /**
     * Returns the number of key/icon mapping this cache currently contains.
     *
     * @return the number of key/icon mapping this cache currently contains.
     */
    public synchronized int size() {
        return hashMap.size();
    }
}
