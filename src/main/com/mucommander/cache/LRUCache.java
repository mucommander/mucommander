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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract LRU cache.
 *
 * <p>An LRU (Least Recently Used) cache can contain a fixed number of items (the capacity). When capacity is reached,
 * the least recently used is removed. Each object retrieved with the {@link #get(Object) get()} method
 * makes the requested item the most recently used one. Similarly, each object inserted using the 
 * {@link #add(Object, Object) add()} method makes the added item the most recently used one.</p>
 *
 * <p>This LRUCache provides an optional feature : the ability to assign a time-to-live for each or part of the
 * items added. When the time-to-live of an item expires, the item is automatically removed from the cache and won't
 * be returned by the {@link #get(Object) get()} method anymore.</p>
 *
 * <p><b>Implementation note:</b> checking for expired items can be an expensive operation so it doesn't have
 * to be done as soon as the item has expired, the expired items can live a bit longer in the cache if necessary.
 * <br>The LRUCache implementation must however guarantee two things :
 * <ul>
 * <li>as soon as an item has expired, it cannot be returned by {@link #get(Object) get()}.
 * <li>when cache capacity is reached (cache is full) and a new item needs to be added, any expired item must be 
 * immediately removed. This prevents least recently used items from being removed unnecessarily.
 * </ul></p>
 *
 * @author Maxence Bernard
 */
public abstract class LRUCache<K, V> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LRUCache.class);
	
    /** Cache capacity: maximum number of items this cache can contain */
    protected int capacity;

    /** Current eldest expiration date amongst all items */
    protected long eldestExpirationDate = Long.MAX_VALUE;

    /** Specifies whether cache hit/miss counters should be updated (should be enabled for Debug purposes only) */ 
    protected final static boolean UPDATE_CACHE_COUNTERS = false;	
    /** Number of cache hits since this LRUCache was created */
    protected int nbHits;
    /** Number of cache misses since this LRUCache was created */
    protected int nbMisses;	


    /**
     * Creates an initially empty LRUCache with the specified maximum capacity.
     */
    public LRUCache(int capacity) {
        this.capacity = capacity;
    }


    /**
     * Returns the maximum number of items this cache can contain.
     */
    public int getCapacity() {
        return capacity;
    }


    /**
     * Returns the number of cache hits since this LRUCache was created.
     *
     * @return the number of cache hits since this LRUCache was created
     */
    public int getHitCount() {
        return nbHits;
    }


    /**
     * Returns the number of cache misses since this LRUCache was created.
     *
     * @return the number of cache misses since this LRUCache was created
     */
    public int getMissCount() {
        return nbMisses;
    }


    ///////////////////////
    // Absctract methods //
    ///////////////////////

    /**
     * Returns the cached object value corresponding to the given key and marks the cached item as the most
     * recently used one.
     *
     * <p>This method will return <code>null</code> if:
     * <ul>
     * <li>the given key doesn't exist
     * <li>the cached value corresponding to the key has expired
     * <ul></p>
     *
     * @param key the cached item's key
     * @return the cached value corresponding to the specified key, or <code>null</code> if a value could not
     * found or has expired
     */
    public abstract V get(K key);
	
    /**
     * Adds a new key/value pair to the cache and marks it as the most recently used.
     *
     * <p>If the cache's capacity has been reached (cache is full):
     * <ul>
     * <li>any object with a past expiration date will be removed<li>
     * <li>if no expired item could be removed, the least recently used item will be removed
     * <ul></p>
     *
     * @param key the key for the object to store
     * @param value the value to cache
     * @param timeToLive the time-to-live of the object in the cache in milliseconds, or -1 for no time-to-live,
     * the object will just be removed when it becomes the least recently used one.
     */
    public abstract void add(K key, V value, long timeToLive);


    /**
     * Convenience method, equivalent to add(key, value, -1).
     */
    public synchronized void add(K key, V value) {
        add(key, value, -1);
    }
	

    /**
     * Removes all items from this cache, leaving the cache in the same state as when it was just created.
     */
    public abstract void clearAll();
		

    /**
     * Returns the current size of this cache, i.e. the number of cached elements it contains.
     * <br><b>Note: </b>Some items that have expired and have not yet been removed might be accounted for
     * in the returned size.
     */
    public abstract int size();


    //////////////////
    // Test methods //
    //////////////////

    /**
     * Tests this LRUCache for corruption and throws a RuntimeException if something is wrong.
     */
    protected abstract void testCorruption() throws RuntimeException;
	

    /**
     * Test method : simple test case + stress/sanity test
     */
    public static void main(String args[]) {
        LRUCache<Integer, Integer> cache;
        /*
        // Simple test case
        cache = new FastLRUCache(3);

        cache.add("orange", "ORANGE");
        System.out.println(cache.toString());

        cache.add("apple", "APPLE");
        System.out.println(cache.toString());

        System.out.println("get(orange)= "+cache.get("orange"));
        System.out.println(cache.toString());
		
        cache.add("apricot", "APRICOT");
        System.out.println(cache.toString());

        cache.add("banana", "BANANA", 1000);
        System.out.println(cache.toString());

        System.out.println("waiting for banana expiration");
        try { Thread.sleep(1050); } catch(InterruptedException e) {}
        System.out.println(cache.toString());

        System.out.println("get(banana)= "+cache.get("banana"));

        System.out.println(cache.toString());
        */

        long timeStamp = System.currentTimeMillis();

        // Stress test to see if everything looks OK after a few thousand iterations
        int capacity = 1000;
        cache = new FastLRUCache<Integer, Integer>(capacity);
        java.util.Random random = new java.util.Random();
        for(int i=0; i<100000; i++) {
            // 50% chance to add a new element with a random value and expiration date (50% chance for no expiration date)
            if(cache.size()==0 || random.nextBoolean()) {
                //				System.out.println("cache.add()");				
                cache.add(random.nextInt(capacity), random.nextInt(), random.nextBoolean()?-1:random.nextInt(10));
            }
            // 50% chance to retrieve a random existing element
            else {
                //				System.out.println("cache.get()");
                cache.get(random.nextInt(capacity));
            }
		
            try {
                // Test the cache for corruption
                cache.testCorruption();
            }
            catch(RuntimeException e) {
                LOGGER.debug("Cache corrupted after "+i+" iterations, cache state="+cache);
                return;
            }

            //			// Print the cache's state
            //			System.out.println(cache.toString());
        }

        LOGGER.debug("Stress test took "+(System.currentTimeMillis()-timeStamp)+" ms.\n");

        // Print the cache's state
        System.out.println(cache.toString());
    }
}
