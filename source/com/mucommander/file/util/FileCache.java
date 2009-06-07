/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.file.util;

import com.mucommander.file.AbstractFile;
import org.apache.commons.collections.map.ReferenceMap;

/**
 * This class provides a file cache, mapping <code>Object</code> keys onto {@link AbstractFile} instances.
 * Any kind of Object may be used as the key, but a sensible choice is to use the 
 * {@link AbstractFile#getURL() file's URL}.
 *
 * <p>Files are stored as {@link java.lang.ref.SoftReference soft references} so they can be garbage collected
 * when the VM runs low on memory.</p>
 *
 * <p>The implementation uses the {@link ReferenceMap} class part of the <code>Apache Commons Collection</code> library.
 * All accesses to the underlying map is synchronized, making this cache thread-safe.</p>
 *
 * @author Maxence Bernard
 */
public class FileCache {

    /** The actual hash map */
    protected final ReferenceMap hashMap = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT);

    /**
     * Creates a new file cache.
     */
    public FileCache() {
    }

    /**
     * Adds a new key/file mapping to the cache. If a mapping with the same key exists, it is replaced and the previous
     * value returned.
     *
     * @param key the key that will later allow to retrieve the cached file
     * @param value the file instance to cache
     * @return returns the file instance previously mapped onto the given key, <code>null</code> if no
     * such mapping existed
     */
    public synchronized AbstractFile put(Object key, AbstractFile value) {
        return (AbstractFile)hashMap.put(key, value);
    }

    /**
     * Returns the {@link AbstractFile} instance mapped onto the given key if there is one,
     * <code>null</code> otherwise
     *
     * @param key key of the file instance to retrieve
     * @return the {@link AbstractFile} instance mapped onto the given key if there is one,
     * <code>null</code> otherwise
     */
    public synchronized AbstractFile get(Object key) {
        return (AbstractFile)hashMap.get(key);
    }

    /**
     * Returns <code>true</code> if this cache currently contains a key/file mapping where the given key is used as
     * the mapping's key.
     *
     * @param key key to lookup
     * @return <code>true</code> if this cache currently contains a key/file mapping where the given key is used as
     * the mapping's key.
     */
    public synchronized boolean containsKey(Object key) {
        return hashMap.containsKey(key);
    }

    /**
     * Returns <code>true</code> if this cache currently contains a key/file mapping where the given file is used as
     * the mapping's value.
     *
     * @param file file to lookup
     * @return <code>true</code> if this cache currently contains a key/file mapping where the given file is used as
     * the mapping's key.
     */
    public synchronized boolean containsValue(AbstractFile file) {
        return hashMap.containsValue(file);
    }

    /**
     * Removes all existing key/file mapping from this cache, leaving the cache in the same state as it was right after
     * its creation.
     */
    public synchronized void clear() {
        hashMap.clear();
    }

    /**
     * Returns the number of key/file mapping this cache currently contains.
     *
     * @return the number of key/file mapping this cache currently contains.
     */
    public synchronized int size() {
        return hashMap.size();
    }
}
