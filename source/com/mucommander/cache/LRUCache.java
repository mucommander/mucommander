
package com.mucommander.cache;

import com.mucommander.PlatformManager;


/**
 * Simple LRU (Least Recently Used) cache implementation.
 *
 * <p>Implementation note: it would have been more efficient to use LinkedHashMap but it is only available
 * in Java 1.4 and above.</p>
 *
 * @author Maxence Bernard
 */
public abstract class LRUCache {

	/** Cache capacity: maximum number of items this cache can contain */
	protected int capacity;

	/** Current eldest expiration date amongst all items */
	protected long eldestExpirationDate = Long.MAX_VALUE;

	/** Specifies whether cache hit/miss counters should be updated (should be enabled for Debug purposes only) */ 
	protected final static boolean UPDATE_CACHE_COUNTERS = com.mucommander.Debug.ON;	
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

	public static LRUCache createInstance(int capacity) {
		if(PlatformManager.getJavaVersion()>=PlatformManager.JAVA_1_4)
			return new FastLRUCache(capacity);
		else
			return new LegacyLRUCache(capacity);
	}
	
	///////////////////////
	// Absctract methods //
	///////////////////////

	/**
	 * Returns the cached object value corresponding to the given key. This method will return null if:
	 * <ul>
	 * <li>the given key doesn't exist
	 * <li>the cached value corresponding to the key has expired, in which case the key and value will be removed
	 * <ul>
	 *
	 * @param key the object key
	 */
	public abstract Object get(Object key);
	
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
	public abstract void add(Object key, Object value, long timeToLive);


	/**
	 * Convenience method, equivalent to add(key, value, -1).
	 */
	public synchronized void add(Object key, Object value) {
		add(key, value, -1);
	}
	

	/**
	 * Clears all items from this cache, returning in the same state as after creation.
	 */
	public abstract void clearAll();
		

	/**
	 * Returns the current size of this cache.
	 */
	public abstract int size();


	//////////////////
	// Test methods //
	//////////////////

	/**
	 * Tests this LRUCache for corruption and throws a RuntimeException if something is wrong.
	 */
	private void testCorruption() throws RuntimeException {
	}
	

	/**
	 * Test method : simple test case + stress/sanity test
	 */
	public static void main(String args[]) {
		LRUCache cache;
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
		cache = new FastLRUCache(capacity);
		java.util.Random random = new java.util.Random();
		for(int i=0; i<100000; i++) {
			// 50% chance to add a new element with a random value and expiration date (50% chance for no expiration date)
			if(cache.size()==0 || random.nextBoolean()) {
//				System.out.println("cache.add()");				
				cache.add(new Integer(random.nextInt(capacity)), new Integer(random.nextInt()), random.nextBoolean()?-1:random.nextInt(10));
			}
			// 50% chance to retrieve a random existing element
			else {
//				System.out.println("cache.get()");
				cache.get(new Integer(random.nextInt(capacity)));
			}
		
			try {
				// Test the cache for corruption
				cache.testCorruption();
			}
			catch(RuntimeException e) {
				System.out.println("Cache corrupted after "+i+" iterations, cache state="+cache);
				return;
			}

//			// Print the cache's state
//			System.out.println(cache.toString());
		}

		System.out.println("Stress test took "+(System.currentTimeMillis()-timeStamp)+" ms.\n");

		// Print the cache's state
		System.out.println(cache.toString());
	}
}