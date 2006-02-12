
package com.mucommander.cache;

import java.util.Vector;
import java.util.Hashtable;


/**
 * Simple LRU (Least Recently Used) cache implementation.
 *
 * <p>Implementation note: it would have been more efficient to use LinkedHashMap but it is only available
 * in Java 1.4 and above.
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
	/** Copy of vlaues expiration dates to allow fast retrieval access (avoids access to hashtable) */
	private Vector expirationDates;

	/** Current eldest expiration date amongst all items */
	private long eldestExpirationDate = Long.MAX_VALUE;


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
	 * Returns the LRUObject value corresponding to the given key. This method will return null if:
	 * <ul>
	 * <li>the given key doesn't exist
	 * <li>the LRUObject value corresponding to the key has expired, in which case the key and value will be removed
	 * <ul>
	 *
	 * @param key the object key
	 */
	public synchronized LRUObject get(Object key) {
		// Find key's index
		int keyIndex = keys.indexOf(key);

		// If key is not registered, return null
		if(keyIndex==-1)
			return null;
		
		// Test if value has expired
		Long expirationDate = (Long)expirationDates.elementAt(keyIndex);
		if(System.currentTimeMillis()>expirationDate.longValue()) {
			// Value has expired, remove both key and associated value and return null
			keys.removeElementAt(keyIndex);
			expirationDates.removeElementAt(keyIndex);
			values.remove(key);
			return null;
		}

		// Move key to the end of the vector (most recently used)
		keys.removeElementAt(keyIndex);
		keys.add(key);
		expirationDates.removeElementAt(keyIndex);
		expirationDates.add(expirationDate);

		// Return LRUObject value
		return (LRUObject)values.get(key);
	}

	
	/**
	 * Adds a new key/value pair to the cache, which become the most recently used element.
	 * <p>If capacity has been reached:
	 * <ul>
	 * <li>any item with a past expiration date will be removed<li>
	 * <li>if no expired item could be removed, the least recently used item will be removed
	 * <ul> 
	 */
	public synchronized void add(Object key, LRUObject value) {
		// Test if capacity has been reached
		int size = keys.size();
		if(size>=capacity) {
			long now = System.currentTimeMillis();
			long expirationDate;
			Object tempKey;
			// Is there at least an item which expiration date has passed ?
			if(this.eldestExpirationDate<now) {
				// Look for expired items and remove them, while recalculating eldestExpirationDate for next time
				this.eldestExpirationDate = Long.MAX_VALUE;
				for(int i=0; i<size; i++) {
					expirationDate = ((Long)expirationDates.elementAt(i)).longValue();
					if(expirationDate<now) {
						// Remove expired item
						tempKey = keys.elementAt(i);
						keys.removeElementAt(i);
						expirationDates.removeElementAt(i);
						values.remove(tempKey);
					}
					else if(expirationDate!=-1 && expirationDate<this.eldestExpirationDate) {
						// update eldestExpirationDate
						this.eldestExpirationDate = expirationDate;
					}
				}
			}
			
			// If no expired items could be found
			if(keys.size()>=capacity) {
				// Remove least recently used key and associated value
				tempKey = keys.elementAt(0);
				keys.removeElementAt(0);
				expirationDates.removeElementAt(0);
				values.remove(tempKey);
			}
		}
		
		// Append key to the end of the keys vector (most recently used)
		keys.add(key);
		// Append expiration date to the end of the expiration dates vector
		expirationDates.add(new Long(value.getExpirationDate()));
		// Add value to key<->value hashtable
		values.put(key, value);
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
	 * Returns a String representation of this cache.
	 */
	public String toString() {
		int size = keys.size();
		String s = super.toString()+" size="+size+"/"+expirationDates.size()+"/"+values.size()+" capacity="+capacity+"\n";

		Object key;
		for(int i=0; i<size; i++) {
			key = keys.elementAt(i);
			s += i+"- key="+key+" value="+values.get(key)+"\n";
		}
		
		return s;
	}


	/**
	 * Tests this LRUCache for corruption and throws a RuntimeException if something is wrong.
	 */
	private void testCorruption() throws RuntimeException {
		int keysSize = keys.size();
		if(keysSize!=expirationDates.size() || keysSize!=values.size())
			throw new RuntimeException("cache corrupted, internal sizes don't match");
	
		Object key;
		for(int i=0; i<keysSize; i++) {
			key = keys.elementAt(i);
			if(((LRUObject)values.get(key)).getExpirationDate()!=((Long)expirationDates.elementAt(i)).longValue())
				throw new RuntimeException("cache corrupted, expirationDates don't match for item#"+i+" key="+key+" value="+values.get(key));
		}
	}
	

	/**
	 * Test class : simple test case + stress/sanity test
	 */
	public static void main(String args[]) {
		// Simple test case
		LRUCache cache = new LRUCache(3);

		cache.add("orange", new LRUObject("ORANGE"));
		System.out.println(cache.toString());

		cache.add("apple", new LRUObject("APPLE"));
		System.out.println(cache.toString());

		System.out.println("get(orange)= "+cache.get("orange"));
		System.out.println(cache.toString());
		
		cache.add("apricot", new LRUObject("APRICOT"));
		System.out.println(cache.toString());

		cache.add("banana", new LRUObject("BANANA", 1000));
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
				cache.add(new Integer(random.nextInt()), new LRUObject(new Integer(random.nextInt()), random.nextBoolean()?-1:random.nextInt(10)));
			// 50% chance to retrieve a random existing element
			else
				cache.get(cache.keys.get(random.nextInt(cache.size())));
			// Test the cache for corruption
			cache.testCorruption();
		}

		// Print the cache's state
		System.out.println(cache.toString());
	}
}