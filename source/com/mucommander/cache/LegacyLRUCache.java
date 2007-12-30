/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

import java.util.Hashtable;
import java.util.Vector;


/**
 * LRU cache implementation which uses a mix of <code>Vector</code> and <code>Hashtable</code> for all operations, and
 * thus is slow because of Vector's inherent lookup complexity.
 *
 * <p>{@link com.mucommander.cache.LegacyLRUCache FastLRUCache} being faster by an order of magnitude, 
 * this implementation should only be used under Java 1.3.
 * Use the {@link #createInstance(int) createInstance()} method to retrieve an instance 
 * of the best implementation for the current Java runtime.</p>
 *
 * @author Maxence Bernard
 */
public class LegacyLRUCache extends LRUCache {

    /** All the keys this cache contains, ordered by last access (last key is the most recently used) */
    private Vector keys;
    /** Key<->value map, all keys have a corresponding value */
    private Hashtable values;	 
    /** Expiration dates for each element in the keys vector */
    private Vector expirationDates;


    public LegacyLRUCache(int capacity) {
        super(capacity);
        init();
    }

    private void init() {
        keys = new Vector(capacity+1);
        values = new Hashtable();
        expirationDates = new Vector(capacity+1);
    }


    /**
     * Returns a String representation of this cache.
     */
    public String toString() {
        int size = keys.size();
        String s = super.toString()+" size="+size+"/"+expirationDates.size()+"/"+values.size()+" capacity="+capacity+" eldestExpirationDate="+eldestExpirationDate+"\n";

        Object key;
        for(int i=0; i<size; i++) {
            key = keys.elementAt(i);
            s += i+"- key="+key+" value="+values.get(key)+" expirationDate="+expirationDates.elementAt(i)+"\n";
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
        // No need to go any further if eldestExpirationDate is in the future
        if(this.eldestExpirationDate>now)
            return;

        // Look for expired items and remove them and recalculate eldestExpirationDate for next time
        this.eldestExpirationDate = Long.MAX_VALUE;
        long expirationDate;
        Object expirationDateL;
        Object tempKey;
        int size = keys.size();
        int i = 0;
        while(i<size) {
            expirationDateL = expirationDates.elementAt(i);
            if(expirationDateL==null) {
                i++;
                continue;
            }

            expirationDate = ((Long)expirationDateL).longValue();
            // Test if the item has an expiration date and check if has passed
            if(expirationDate<now) {
                // Remove expired item
                tempKey = keys.elementAt(i);
                keys.removeElementAt(i);
                expirationDates.removeElementAt(i);
                values.remove(tempKey);
                size--;
            }
            else {
                if(expirationDate<this.eldestExpirationDate) {
                    // update eldestExpirationDate
                    this.eldestExpirationDate = expirationDate;
                }
                i++;
            }
        }
    }

	
    /////////////////////////////////////
    // LRUCache methods implementation //
    /////////////////////////////////////

    public synchronized Object get(Object key) {
        // Look for expired items and purge them (if any)
        purgeExpiredItems();	

        // Find key's index
        int keyIndex = keys.indexOf(key);

        // Return null if the key doesn't exist
        if(keyIndex==-1) {
            // Increase cache miss counter
            if(UPDATE_CACHE_COUNTERS) nbMisses++;
            return null;
        }
		
        // Move key to the end of the vector (most recently used)
        keys.removeElementAt(keyIndex);
        keys.add(key);
        expirationDates.add(expirationDates.elementAt(keyIndex));
        expirationDates.removeElementAt(keyIndex);

        // Increase cache hit counter
        if(UPDATE_CACHE_COUNTERS) nbHits++;

        // Return the cached object's value
        return values.get(key);
    }

	
    public synchronized void add(Object key, Object value, long timeToLive) {
        // Look for expired items and purge them (if any)
        purgeExpiredItems();	

        // Remove any existing key that is equal to the specified one
        int keyIndex = keys.indexOf(key);
        Object lruKey;
        if(keyIndex!=-1) {
            lruKey = keys.elementAt(keyIndex);
            keys.removeElementAt(keyIndex);
            expirationDates.removeElementAt(keyIndex);
            values.remove(lruKey);
        }

        // Test if capacity has been reached
        if(keys.size()>=capacity) {
            // Remove least recently used key and associated value
            lruKey = keys.elementAt(0);
            keys.removeElementAt(0);
            expirationDates.removeElementAt(0);
            values.remove(lruKey);
        }
		
        // Append key to the end of the keys vector (most recently used)
        keys.add(key);
        // Append expiration date to the end of the expiration dates vector
        if(timeToLive==-1) {
            expirationDates.add(null);
        }
        else {
            long expirationDate = System.currentTimeMillis()+timeToLive;
            expirationDates.add(new Long(expirationDate));
            // Update eledestExpirationDate if new element's expiration date is older
            if(expirationDate<this.eldestExpirationDate) {
                // update eldestExpirationDate
                this.eldestExpirationDate = expirationDate;
            }
        }

        // Add value to key<->value hashtable
        values.put(key, value);
    }


    public synchronized int size() {
        return keys.size();
    }

	
    public synchronized void clearAll() {
        init();
        eldestExpirationDate = Long.MAX_VALUE;
    }


    //////////////////
    // Test methods //
    //////////////////

    protected void testCorruption() throws RuntimeException {
        int keysSize = keys.size();
        if(keysSize!=expirationDates.size() || keysSize!=values.size())
            throw new RuntimeException("cache corrupted: internal sizes don't match");
	
        Object key;
        long expirationDate;
        Object expirationDateL;
        for(int i=0; i<keysSize; i++) {
            key = keys.elementAt(i);
            if(values.get(key)==null)
                throw new RuntimeException("cache corrupted: value could not be found for key="+key);

            expirationDateL = expirationDates.elementAt(i);
            if(expirationDateL==null)
                continue;
			
            expirationDate = ((Long)expirationDateL).longValue();
            if(expirationDate<eldestExpirationDate)
                throw new RuntimeException("cache corrupted: expiration date for key="+key+" older than eldestExpirationDate");
        }
    }
}
