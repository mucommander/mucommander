
package com.mucommander.file;

import java.util.Hashtable;
import java.util.Enumeration;


/**
 * This class centralizes login/password combinations ({@link com.mucommander.file.AuthInfo AuthInfo} instances) used
 * to connect to remote file systems. It is used to manipulate file paths and show them to the end user
 * without the login and password information. Each supplied login/password is mapped to a path (or file URL) and
 * stored in a table and can later be retrieved.
 *
 * @author Maxence Bernard
 */
public class AuthManager {
	
	private static AuthManager authManager = new AuthManager();
	private static Hashtable entries;

	private AuthManager() {
		entries = new Hashtable();
	}
	
	/**
	 * Adds the given AuthInfo and maps it to the given path, overriding any previous AuthInfo
	 * instance mapped on the same path.
	 */
	public static void put(String path, AuthInfo authInfo) {
		// Remove trailing separator
		if((path.endsWith("/") && !path.equals("/")) || (path.endsWith("\\") && !path.equals("\\")))
			path = path.substring(0, path.length());
		entries.put(path, authInfo);
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("path="+path+" "+"authInfo="+authInfo+", entries = "+entries);
	}

	
	/**
	 * Looks for a AuthInfo mapping which path is at least
	 * partially equal to the given path (given path starts with the path associated with AuthInfo).
	 * <p>If there are several AuthInfo that match the given path, the AuthInfo instance returned
	 * will be the one corresponding to the path that best matches the supplied path</p>
	 */
	public static AuthInfo get(String path) {
		AuthInfo info = (AuthInfo)entries.get(path);
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("path="+path+" authInfo="+info+" entries="+entries);
		if(info!=null) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("returning= "+info);
			return info;
		}
	
		Enumeration keys = entries.keys();
		String key;
		AuthInfo bestInfo = null;
		int max = -1;
		int len;
		while (keys.hasMoreElements()) {
			key = (String)keys.nextElement();
			if (path.toLowerCase().startsWith(key.toLowerCase())) {
				len = key.length();
				if (len>max) {
					max = len;
					bestInfo = (AuthInfo)entries.get(key);
				}
			}
		}
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("returning= "+bestInfo);
		return bestInfo;
	}
	
	
	/**
	 * Same as {@link #get(String) get(String)} but using the given
	 * FileURL and ignoring the login/password info in it (if any).
	 */
	public static AuthInfo get(FileURL fileURL) {
		return get(fileURL.getStringRep(false));
	}
		
	
	/**
	 * Removes any AuthInfo mapped to the given path.
	 */
	public static void remove(String path) {
		entries.remove(path);
	}
	
	/**
	 * Same as {@link #remove(String) remove(String)} but using the given
	 * FileURL and ignoring the login/password info in it (if any).
	 */
	public static void remove(FileURL fileURL) {
		remove(fileURL.getStringRep(false));
	}

	
	/**
	 * Use the given FileURL instance and process it in the following way :<br>
	 *  - if it contains login/password info, adds this info ({@link #put(String, AuthInfo) put})
	 * and maps it to the file public url (removing login and password info)<br>
	 *  - if it doesn't, looks for an existing AuthInfo mapping ({@link #get(String) get})
     * and if one was found, add the login and password info to the FileURL<br>
	 *
	 * @param fileURL the file URL to add or retrieve from login and password.
	 * @param addAuthInfo if true, the auth info contained in the FileURL will be added and mapped to the URL.
	 *
	 */
	public static void authenticate(FileURL fileURL, boolean addAuthInfo) {
		// Retrieve login/password from URL (if any)
		AuthInfo urlAuthInfo = AuthInfo.getAuthInfo(fileURL);
		
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(" fileURL="+fileURL+" urlAuthInfo="+urlAuthInfo+" addAuthInfo="+addAuthInfo);
		
		// if the URL specifies a login and password (typed in by the user)
		// add it to AuthManager and use it
		if (urlAuthInfo!=null) {
			if(addAuthInfo)
				put(fileURL.getStringRep(false), urlAuthInfo);
		}
		// if not, check if AuthManager has a login/password matching this url
		else {
			AuthInfo authInfo = get(fileURL.getStringRep(false));
			// Add login and password to the URL	
			if (authInfo!=null) {
				fileURL.setLogin(authInfo.getLogin());
				fileURL.setPassword(authInfo.getPassword());
			}
		}
	}
}