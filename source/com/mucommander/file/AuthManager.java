
package com.mucommander.file;

import java.util.Hashtable;
import java.util.Enumeration;

public class AuthManager {
	
	private static AuthManager authManager = new AuthManager();
	private static Hashtable entries;

	private AuthManager() {
		entries = new Hashtable();
	}

	public static void put(String path, AuthInfo authInfo) {
		entries.put(path, authInfo);
//System.out.println("entries " + entries);
	}

	public static AuthInfo get(String path) {
		AuthInfo info = (AuthInfo)entries.get(path);
		if(info!=null)
			return info;
	
		Enumeration keys = entries.keys();
		String key;
		AuthInfo bestInfo = null;
		int max = -1;
		int length;
		while (keys.hasMoreElements()) {
			key = (String)keys.nextElement();
			if (path.startsWith(key)) {
				length = key.length();
				if (length>max) {
					max = length;
					bestInfo = (AuthInfo)entries.get(key);
				}
			}
		}
		return bestInfo;
	}

	public static void remove(String path) {
		entries.remove(path);
	}
}