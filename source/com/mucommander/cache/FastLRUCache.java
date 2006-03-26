
package com.mucommander.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;


/**
 * Simple LRU (Least Recently Used) cache implementation.
 *
 * <p>Implementation note: it would have been more efficient to use LinkedHashMap but it is only available
 * in Java 1.4 and above.</p>
 *
 * @author Maxence Bernard
 */
public class FastLRUCache extends LRUCache {

	private LinkedHashMap cacheMap;

	/** Timestamp of last expired items purge */
	private long lastExpiredPurge;

	/** Number of millisecond to wait between 2 expired items purges, if cache is not full */
	private final static int PURGE_EXPIRED_DELAY = 1000;
		

	/**
	 * Creates an initially empty FastLRUCache with the specified maximum capacity.
	 */
	public FastLRUCache(int capacity) {
		super(capacity);
		this.cacheMap = new LinkedHashMap(capacity+1, 0.75f, true) {
//		this.cacheMap = new LinkedHashMap(16, 0.75f, true) {
			protected final boolean removeEldestEntry(Map.Entry eldest) {
				return cacheMap.size() > FastLRUCache.this.capacity;
			}
		};
	}


	/**
	 * Returns a String representation of this cache.
	 */
	public String toString() {
		int size = cacheMap.size();
		String s = super.toString()+" size="+cacheMap.size()+" capacity="+capacity+" eldestExpirationDate="+eldestExpirationDate+"\n";

		Iterator iterator = cacheMap.entrySet().iterator();
		Map.Entry mapEntry;
		Object key;
		Object value[];
		int i=0;
		while(iterator.hasNext()) {
			mapEntry = (Map.Entry)iterator.next();
			key = mapEntry.getKey();
			value = (Object[])mapEntry.getValue();
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
// if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("now="+System.currentTimeMillis()+" eldestExpirationDate="+eldestExpirationDate);
		// No need to go any further if eldestExpirationDate is in the future.
		// Also, since iterating on the values is an expensive operation (especially for LinkedHashMap),
		// wait PURGE_EXPIRED_DELAY between two purges, unless cache is full
		if(this.eldestExpirationDate>now || (cacheMap.size()<capacity && now-lastExpiredPurge<PURGE_EXPIRED_DELAY))
			return;

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("iterating...");

		// Look for expired items and remove them and recalculate eldestExpirationDate for next time
		this.eldestExpirationDate = Long.MAX_VALUE;
		Long expirationDateL;
		long expirationDate;
		Object value[];

		// Iterate on all cached values
		Iterator iterator = cacheMap.values().iterator();
		while(iterator.hasNext()) {
			expirationDateL = (Long)((Object[])iterator.next())[1];
			
			// No expiration date for this value
			if(expirationDateL==null)
				continue;

			expirationDate = expirationDateL.longValue();
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
		
		lastExpiredPurge = now;
	}


	/////////////////////////////////////
	// LRUCache methods implementation //
	/////////////////////////////////////	

	public synchronized Object get(Object key) {
		// Look for expired items and purge them (if any)
		purgeExpiredItems();	

		// Look for a value correponding to the specified key in the cache map
		Object[] value = (Object[])cacheMap.get(key);

		if(value==null) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("key="+key+" has no matching value");
			// No value matching key, better luck next time!
			if(UPDATE_CACHE_COUNTERS)
				nbMisses++;	// Increase cache miss counter
			return null;
		}

		// Since expired items purge is not performed on every call to this method for
		// performance reason, we can end with an expired cached value so we need
		// to check this
		Long expirationDateL = (Long)value[1];
		if(expirationDateL!=null && System.currentTimeMillis()>expirationDateL.longValue()) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("key="+key+" : value is expired ("+expirationDateL.longValue()+", now="+System.currentTimeMillis());
			// Value has expired, let's remove it
			if(UPDATE_CACHE_COUNTERS)
				nbMisses++;	// Increase cache miss counter
			cacheMap.remove(key);
			return null;
		}
			

		if(UPDATE_CACHE_COUNTERS)
			nbHits++;	// Increase cache hit counter

		return value[0];
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
			expirationDateL = new Long(expirationDate);
		}

		cacheMap.put(key, new Object[]{value, expirationDateL});
	}


	public synchronized int size() {
		return cacheMap.size();
	}

	
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
	private void testCorruption() throws RuntimeException {
		Object key;
		Object value[];
		long expirationDate;
		Long expirationDateL;
		Iterator iterator = cacheMap.keySet().iterator();
		while(iterator.hasNext()) {
			key = iterator.next();
			value = (Object[])cacheMap.get(key);
			if(value==null)
				throw new RuntimeException("cache corrupted: value could not be found for key="+key);

			expirationDateL = (Long)value[1];
			if(expirationDateL==null)
				continue;
			
			expirationDate = expirationDateL.longValue();
			if(expirationDate<eldestExpirationDate)
				throw new RuntimeException("cache corrupted: expiration date for key="+key+" older than eldestExpirationDate");
		}
	}
	
}