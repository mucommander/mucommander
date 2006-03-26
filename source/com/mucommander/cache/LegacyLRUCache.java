
package com.mucommander.cache;

import java.util.Vector;
import java.util.Hashtable;


/**
 * Simple LRU (Least Recently Used) cache implementation.
 *
 * <p>Implementation note: it would have been more efficient to use LinkedHashMap but it is only available
 * in Java 1.4 and above.</p>
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


	/**
	 * Creates an initially empty LegacyLRUCache with the specified maximum capacity.
	 */
	public LegacyLRUCache(int capacity) {
		super(capacity);
		keys = new Vector(capacity+1);
		values = new Hashtable();
		expirationDates = new Vector(capacity+1);
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

	
	/**
	 * Adds a new key/value pair to the cache, which become the most recently used element.
	 * <p>If capacity has been reached:
	 * <ul>
	 * <li>any object with a past expiration date will be removed<li>
	 * <li>if no expired item could be removed, the least recently used item will be removed
	 * <ul> 
	 *
	 * @param key the key for the object to store
	 * @param value the value to cache
	 * @param timeToLive time to live for the object in the cache, in milliseconds
	 */
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
		this.capacity = capacity;
		keys = new Vector();
		values = new Hashtable();
		expirationDates = new Vector();	
		eldestExpirationDate = Long.MAX_VALUE;
	}


	//////////////////
	// Test methods //
	//////////////////

	/**
	 * Tests this LRUCache for corruption and throws a RuntimeException if something is wrong.
	 */
	private void testCorruption() throws RuntimeException {
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