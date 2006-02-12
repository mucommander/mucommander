
package com.mucommander.cache;

public class LRUObject {

	private Object value;
	private long expirationDate;

	public LRUObject(Object value) {
		this.value = value;
		this.expirationDate = -1;
	}
	
	public LRUObject(Object value, long timeToLive) {
		this.value = value;
		if(timeToLive<=0)
			this.expirationDate = -1;
		else
			this.expirationDate = System.currentTimeMillis()+timeToLive;
	}

	public Object getValue() {
		return value;
	}
	
	public long getExpirationDate() {
		return expirationDate;
	}

	public boolean hasExpired() {
		return expirationDate!=-1 && System.currentTimeMillis()>expirationDate;
	}

	public String toString() {
		return value+" (expirationDate="+expirationDate+")";
	}
}