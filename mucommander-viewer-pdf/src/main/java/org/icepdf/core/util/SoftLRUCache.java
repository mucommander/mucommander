/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

/**
 * Least recently used cache using SoftReferences.
 *
 * @since 5.0
 */
public class SoftLRUCache<K, V> {
    private LinkedHashMap<K, SoftReference<V>> lruCache;
    private ReferenceQueue<? super V> reqQueue;

    public SoftLRUCache(int aInitialSize) {
        lruCache = new LinkedHashMap<K, SoftReference<V>>(
                aInitialSize,
                0.75f,
                true
        );
        reqQueue = new ReferenceQueue<V>();
    }

    public V get(K aKey) {
        diposeStaleEntries();
        SoftReference<V> ref = lruCache.get(aKey);
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }

    public V put(K aKey, V aValue) {
        diposeStaleEntries();
        SoftReference<V> oldValue = lruCache.put(aKey, new KeyReference<K, V>(aKey, aValue, reqQueue));
        if (oldValue != null) {
            return oldValue.get();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void diposeStaleEntries() {
        KeyReference<K, V> ref;
        while ((ref = (KeyReference<K, V>) reqQueue.poll()) != null) {
            lruCache.remove(ref.getKey());
        }
    }

    public void clear() {
        lruCache.clear();
    }

    private static class KeyReference<K, V> extends SoftReference<V> {
        private K key;

        public KeyReference(K key, V value, ReferenceQueue<? super V> refQueue) {
            super(value, refQueue);
            this.key = key;
        }

        public K getKey() {
            return key;
        }
    }
}
