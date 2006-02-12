
package com.mucommander.cache;


/**
 * LRUObject wraps an object and an optional time to live after which the key/pair value will be automatically removed
 * from the LRUCache.
 *
 * @author Maxence Bernard
 */
public class LRUObject {

	/** Value to cache */
	private Object value;
	/** Expiration date, -1 if no time to live was specified */
	private long expirationDate;

	/**
	 * Creates a new LRUObject with the specified value and no time to live.
	 *
	 * @param value the value to cache
	 */
	public LRUObject(Object value) {
		this.value = value;
		this.expirationDate = -1;
	}
	
	/**
	 * Creates a new LRUObject with the specified value and time to live.
	 *
	 * @param value the value to cache
	 * @param timeToLive time to live for the object in the cache, in milliseconds
	 */
	public LRUObject(Object value, long timeToLive) {
		this.value = value;
		if(timeToLive<=0)
			this.expirationDate = -1;
		else
			this.expirationDate = System.currentTimeMillis()+timeToLive;
	}

	/**
	 * Returns the wrapped object value.
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Returns the expiration date for this LRUObject.
	 */
	public long getExpirationDate() {
		return expirationDate;
	}

	/**
	 * Returns true if this LRU Object has expired.
	 */
	public boolean hasExpired() {
		return expirationDate!=-1 && System.currentTimeMillis()>expirationDate;
	}

	/**
	 * Returns a String representation of this LRUObject.
	 */
	public String toString() {
		return value+" (expirationDate="+expirationDate+")";
	}
}