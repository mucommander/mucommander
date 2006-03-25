
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
public class LRUCache {

	/** Cache capacity: maximum number of items this cache can contain */
	private int capacity;

	/** All the keys this cache contains, ordered by last use (last key is the most recently used) */
	private Vector keys;
	/** Key<->value map, all keys have a corresponding value */
	private Hashtable values;	 
	/** Expiration dates for each element in the keys vector */
	private Vector expirationDates;

	/** Current eldest expiration date amongst all items */
	private long eldestExpirationDate = Long.MAX_VALUE;

	/** Specifies whether cache hit/miss counters should be updated (should be enabled for Debug purposes only) */ 
	private final static boolean UPDATE_CACHE_COUNTERS = false;	
	private int nbHits;
	private int nbMisses;	


	/**
	 * Creates an initially empty LRUCache with the specified maximum capacity.
	 */
	public LRUCache(int capacity) {
		this.capacity = capacity;
		keys = new Vector();
		values = new Hashtable();
		expirationDates = new Vector();
	}


	/**
	 * Returns the cached object value corresponding to the given key. This method will return null if:
	 * <ul>
	 * <li>the given key doesn't exist
	 * <li>the cached value corresponding to the key has expired, in which case the key and value will be removed
	 * <ul>
	 *
	 * @param key the object key
	 */
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

		// Test if capacity has been reached
		if(keys.size()>=capacity) {
			// Remove least recently used key and associated value
			Object lruKey = keys.elementAt(0);
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


	/**
	 * Convenience method, equivalent to add(key, value, -1).
	 */
	public synchronized void add(Object key, Object value) {
		add(key, value, -1);
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
	 * Returns the maximum number of items this cache can contain.
	 */
	public int capacity() {
		return capacity;
	}

	/**
	 * Returns the current size of this cache.
	 */
	public int size() {
		return keys.size();
	}

	
	/**
	 * Clears all items from this cache, returning in the same state as after creation.
	 */
	public synchronized void clearAll() {
		this.capacity = capacity;
		keys = new Vector();
		values = new Hashtable();
		expirationDates = new Vector();	
		eldestExpirationDate = Long.MAX_VALUE;
	}
	
	
	/**
	 * Returns the number of cache hits, if cache counters were enabled.
	 */
	public int getNbHits() {
		return nbHits;
	}
	
	/**
	 * Returns the number of cache misses, if cache counters were enabled.
	 */
	public int getNbMisses() {
		return nbMisses;
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
		
		return s;
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
	

	/**
	 * Test method : simple test case + stress/sanity test
	 */
	public static void main(String args[]) {
		// Simple test case
		LRUCache cache = new LRUCache(3);

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


		// Stress test to see if everything looks OK after a few thousand iterations
		cache = new LRUCache(100);
		java.util.Random random = new java.util.Random();
		for(int i=0; i<5000; i++) {
			// 50% chance to add a new element with a random value and expiration date (50% chance for no expiration date)
			if(cache.size()==0 || random.nextBoolean())
				cache.add(new Integer(random.nextInt()), new Integer(random.nextInt()), random.nextBoolean()?-1:random.nextInt(10));
			// 50% chance to retrieve a random existing element
			else
				cache.get(cache.keys.get(random.nextInt(cache.size())));
			
			try {
				// Test the cache for corruption
				cache.testCorruption();
			}
			catch(RuntimeException e) {
				System.out.println("Cache corrupted after "+i+" iterations, cache state="+cache);
				return;
			}
		}

		// Print the cache's state
		System.out.println(cache.toString());
	}
}