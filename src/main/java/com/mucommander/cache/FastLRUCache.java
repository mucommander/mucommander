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


package com.mucommander.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * LRU cache implementation which uses <code>LinkedHashMap</code> which provides fast retrieval and insertion
 * operations.
 * 
 * <p>The only area this implemention is slow at, is checking for and removing expired elements which
 * requires traversing all values and <code>LinkedHashMap</code> is slow at that. 
 * To minimize the impact this could have on performance, this operation is not systematically performed
 * for each call to <code>get()</code> and <code>set()</code> methods, unless the cache is full. 
 * That means this implementation is not as aggressive as it could be in terms of releasing expired items' memory
 * but favors performance instead, which is what caches are for.</p>
 *
 * @author Maxence Bernard
 */
public class FastLRUCache<K, V> extends LRUCache<K,V> {

    /** Cache key->value/expirationDate map */
    private LinkedHashMap<K, Object[]> cacheMap;

    /** Timestamp of last expired items purge */
    private long lastExpiredPurge;

    /** Number of millisecond to wait between 2 expired items purges, if cache is not full */
    private final static int PURGE_EXPIRED_DELAY = 1000;
		

    public FastLRUCache(int capacity) {
        super(capacity);
        this.cacheMap = new LinkedHashMap<K, Object[]>(16, 0.75f, true) {
                // Override this method to automatically remove eldest entry before insertion when cache is full
                @Override
                protected final boolean removeEldestEntry(Map.Entry<K, Object[]> eldest) {
                    return cacheMap.size() > FastLRUCache.this.capacity;
                }
            };
    }


    /**
     * Returns a String representation of this cache.
     */
    public String toString() {
        String s = super.toString()+" size="+cacheMap.size()+" capacity="+capacity+" eldestExpirationDate="+eldestExpirationDate+"\n";

        Object key;
        Object value[];
        int i=0;
        for(Map.Entry<K, Object[]> mapEntry : cacheMap.entrySet()) {
            key = mapEntry.getKey();
            value = mapEntry.getValue();
            s += (i++)+"- key="+key+" value="+value[0]+" expirationDate="+value[1]+"\n";
        }
		
        if(UPDATE_CACHE_COUNTERS)
            s += "nbCacheHits="+nbHits+" nbCacheMisses="+nbMisses+"\n";
		
        return s;
    }


    /**
     * Looks for cached items that have a passed expiration date and purge them.
     */
    private void purgeExpiredItems() {
        long now = System.currentTimeMillis();
        // No need to go any further if eldestExpirationDate is in the future.
        // Also, since iterating on the values is an expensive operation (especially for LinkedHashMap),
        // wait PURGE_EXPIRED_DELAY between two purges, unless cache is full
        if(this.eldestExpirationDate>now || (cacheMap.size()<capacity && now-lastExpiredPurge<PURGE_EXPIRED_DELAY))
            return;

        // Look for expired items and remove them and recalculate eldestExpirationDate for next time
        this.eldestExpirationDate = Long.MAX_VALUE;
        Long expirationDateL;
        long expirationDate;
        Iterator<Object[]> iterator = cacheMap.values().iterator();
        // Iterate on all cached values
        while(iterator.hasNext()) {
            expirationDateL = (Long)iterator.next()[1];
			
            // No expiration date for this value
            if(expirationDateL==null)
                continue;

            expirationDate = expirationDateL;
            // Test if the item has an expiration date and check if has passed
            if(expirationDate<now) {
                // Remove expired item
                iterator.remove();
            }
            else if(expirationDate<this.eldestExpirationDate) {
                // update eldestExpirationDate
                this.eldestExpirationDate = expirationDate;
            }
        }
		
        // Set last purge timestamp to now
        lastExpiredPurge = now;
    }


    /////////////////////////////////////
    // LRUCache methods implementation //
    /////////////////////////////////////	

    @Override
    public synchronized V get(K key) {
        // Look for expired items and purge them (if any)
        purgeExpiredItems();	

        // Look for a value correponding to the specified key in the cache map
        Object[] value = cacheMap.get(key);

        if(value==null) {
            // No value matching key, better luck next time!
            if(UPDATE_CACHE_COUNTERS)
                nbMisses++;	// Increase cache miss counter
            return null;
        }

        // Since expired items purge is not performed on every call to this method for
        // performance reason, we can end with an expired cached value so we need
        // to check this
        Long expirationDateL = (Long)value[1];
        if(expirationDateL!=null && System.currentTimeMillis()> expirationDateL) {
            // Value has expired, let's remove it
            if(UPDATE_CACHE_COUNTERS)
                nbMisses++;	// Increase cache miss counter
            cacheMap.remove(key);
            return null;
        }
			

        if(UPDATE_CACHE_COUNTERS)
            nbHits++;	// Increase cache hit counter

        return (V)value[0];
    }

	
    @Override
    public synchronized void add(K key, V value, long timeToLive) {
        // Look for expired items and purge them (if any)
        purgeExpiredItems();	

        Long expirationDateL;
        if(timeToLive==-1) {
            expirationDateL = null;
        }
        else {
            long expirationDate = System.currentTimeMillis()+timeToLive;
            // Update eledestExpirationDate if new element's expiration date is older
            if(expirationDate<this.eldestExpirationDate) {
                // update eldestExpirationDate
                this.eldestExpirationDate = expirationDate;
            }
            expirationDateL = expirationDate;
        }

        cacheMap.put(key, new Object[]{value, expirationDateL});
    }


    @Override
    public synchronized int size() {
        return cacheMap.size();
    }

	
    @Override
    public synchronized void clearAll() {
        cacheMap.clear();
        eldestExpirationDate = Long.MAX_VALUE;
    }
	
	
    //////////////////
    // Test methods //
    //////////////////

    /**
     * Tests this LRUCache for corruption and throws a RuntimeException if something is wrong.
     */
    @Override
    protected void testCorruption() throws RuntimeException {
        Object value[];
        long expirationDate;
        Long expirationDateL;

        for(K key : cacheMap.keySet()) {
            value = cacheMap.get(key);
            if(value==null)
                throw new RuntimeException("cache corrupted: value could not be found for key="+key);

            expirationDateL = (Long)value[1];
            if(expirationDateL==null)
                continue;
			
            expirationDate = expirationDateL;
            if(expirationDate<eldestExpirationDate)
                throw new RuntimeException("cache corrupted: expiration date for key="+key+" older than eldestExpirationDate");
        }
    }
	
}
