
package com.mucommander.file;

import java.net.MalformedURLException;
import java.net.URLDecoder;


/**
 * This class parses a file URL without any knowledge of the underlying protocol. URL are expected to respect the following format :<br>
 * 	<code>protocol://[login[:password]@]host[:port][path][query]</code>
 *
 * @author Maxence Bernard
 */
public class FileURL implements Cloneable {

	private String protocol;
	private String host;
	private int port = -1;
	private String login;
	private String password;
	private String path;
	private String parent;
	private String filename;
	private String query;
	
	
	public FileURL(FileURL parentURL, String childPath) {
		String parentURLString = parentURL.getURL(true);
		this(parentURLString+(parentURLString.endsWith("/")?"":"/")+childPath);
	}
	
	
	/**
	 * Creates a new FileURL from the given URL string.
	 */
	public FileURL(String url) throws MalformedURLException {
		try {
			int pos;
			int urlLen = url.length();
			
			// Parse protocol
			pos = url.indexOf("://");
			if(pos==-1)
				throw new MalformedURLException("Protocol not specified");
			protocol =  url.substring(0, pos).trim();
			// Advance string index
			pos += 3;
			
			// Parse login and password and they have been specified
//			int atPos = url.indexOf('@', pos);		
			int atPos = url.lastIndexOf('@');		// Last index because password can contain an '@' if it's an email address
			int colonPos;
			if(atPos!=-1) {
				colonPos = url.indexOf(':', pos);
				login = url.substring(pos, colonPos==-1?atPos:colonPos).trim();
				if(colonPos!=-1)
					password = url.substring(colonPos+1, atPos).trim();
				// Advance string index
				pos = atPos+1;
			}
			
			// Parse host and port (if specified)
			colonPos = url.indexOf(':', pos);
			int questionPos = url.indexOf('?', pos);
			int slashPos = url.indexOf('/', pos);
			int hostEndPos;
			// Slash is necessarely before question mark
			if(slashPos!=-1)
				hostEndPos = slashPos;
			else if(questionPos!=-1)
				hostEndPos = questionPos;
			else
				hostEndPos = urlLen;
			
			host = url.substring(pos, colonPos==-1?hostEndPos:colonPos).trim();
			if(colonPos!=-1)
				port = Integer.parseInt(url.substring(colonPos+1, hostEndPos).trim());
		
			// Parse path part excluding query part
			pos = hostEndPos;
			path = url.substring(pos, questionPos==-1?urlLen:questionPos).trim();
			// Add '/' to path is path is empty
			if(path.equals(""))
				path = "/";
			
			// Parse query part (if any)
			if(questionPos!=-1)
				query = url.substring(questionPos, urlLen).trim();
		
			// Extract filename and parent from path
			if(path.equals("") || path.equals("/")) {
				filename = host;
				// parent is null
			}
			else {	
				// Extract filename from path
				int len = path.length();
				slashPos = (path.endsWith("/")?path.substring(0, --len):path).lastIndexOf('/');
				filename = path.substring(slashPos+1, len).trim();
//				if(urlDecode)
//					try { filename = URLDecoder.decode(filename); }
//					catch(Exception e) {} // URLDecoder can throw an exception if name contains % character that are not followed by a numerical value
				
				len = url.length();
				slashPos = (url.endsWith("/")?url.substring(0, --len):url).lastIndexOf('/');
				if(slashPos>7) {
					parent = url.substring(0, slashPos);
				}
			}
		}
		catch(MalformedURLException e) {
			if(com.mucommander.Debug.ON) {
				System.out.println("Unexcepted exception in FileURL() with "+url);
//				e.printStackTrace();
			}
			throw e;
		}
		catch(Exception e2) {
			if(com.mucommander.Debug.ON) {
				System.out.println("Exception in FileURL() with "+url);
				e2.printStackTrace();
			}
			throw new MalformedURLException();
		}
	}

	
	/**
	 * Returns the protocol part of this URL (e.g. smb)
	 */
	public String getProtocol() {
		return protocol;
	}
	
	
	/**
	 * Returns the host part of this URL (e.g. google.com)
	 */
	public String getHost() {
		return host;
	}
	
	
	/**
	 * Returns the port specified in this URL (e.g. 8080) if there is one, -1 otherwise.
	 * (-1 means the protocol default port should be considered).
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Sets a custom port.
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	
	/**
	 * Returns the login specified in this URL (e.g. maxence for ftp://maxence@mucommander.com),
	 * <code>null</code> otherwise.
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * Sets the login part with the given one.
	 */
	public void setLogin(String login) {
		if(login==null)
			this.login = null;
		else {
			login = login.trim();
			if(login.equals(""))
				this.login = null;
			else
				this.login = login;
		}
	}


	/**
	 * Returns the password specified in this URL (e.g. blah for ftp://maxence:blah@mucommander.com),
	 * <code>null</code> otherwise.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password part with the given one.
	 */
	public void setPassword(String password) {
		if(password==null)
			this.password = null;
		else {
			password = password.trim();
			if(password.equals(""))
				this.password = null;
			else
				this.password = password;
		}
	}
	
	
	/**
	 * Returns the path part of this URL (e.g. /webstart/mucommander.jnlp for http://mucommander.com/webstart/mucommander.jnlp)
	 */
	public String getPath() {
		return path;
	}

	
	/**
	 * Returns the parent URL of this file based on the path, null if there is no parent file (path is '/').
	 */
	public FileURL getParent() {
		if(parent==null)
			return null;
		
		try { return new FileURL(parent); }
		catch(MalformedURLException e) {
			return null;
		}
	}
	

	/**
	 * Returns the filename part of this URL (e.g. mucommander.jnlp for http://mucommander.com/webstart/mucommander.jnlp)
	 * <code>null</code> if no filename can be extracted from this URL (e.g. http://google.com).
	 */
	public String getFilename() {
		return filename;
	}
	

	/**
	 * Returns the filename part of this URL, and if specified, decodes URL-encoded characters (e.g. %5D%35)
	 */
	public String getFilename(boolean urlDecode) {
		if(urlDecode && filename!=null)
			return URLDecoder.decode(filename);
			
		return filename;
	}
	

	/**
	 * Returns the query part of this URL if there is one (e.g. ?dummy=1&void=1 for http://mucommander.com/useless.php?dummy=1&void=1),
	 * <code>null</code> otherwise.
	 */
	public String getQuery() {
		return query;
	}

	
	/**
	 * Reconstructs the URL from all parsed fields and returns it.
	 */
	public String getURL(boolean includeAuthInfo) {
		String s = protocol + "://";
		
		if(includeAuthInfo && login!=null) {
			s += login;
			if(password!=null)
				s += ":"+password;
			s += "@";
		}

		if(host.equals(""))
			return s;
		
		s += host;
		
		if(port!=-1)
			s += ":"+port;

		s += path;
		if(query!=null)
			s += query;
		
		return s;
	}
	
	
	public String toString() {
		return getURL(true);
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof FileURL))
			return false;
		
		return ((FileURL)o).getURL(true).equals(getURL(true));
	}
	
	
	/**
	 * Test method.
	 */
	public static void main(String args[]) {
		String urls[] = new String[]{
			"http://google.com",
			"http://mucommander.com",
			"http://mucommander.com/",
			"http://mucommander.com/webstart/",
			"http://mucommander.com/webstart/index.html",
			"http://mucommander.com/webstart/index.php?dummy=1&useless=true",
			"smb://",
			"smb://maxence@garfield",
			"smb://maxence:blah@garfield",
			"smb://maxence:blah@garfield/shared/music",
			"ftp://mucommander.com",
			"ftp://mucommander.com:21",
			"ftp://mucommander.com:21/",
			"ftp://mucommander.com:21/pub/incoming",
			"ftp://mucommander.com:21/pub/incoming/",
			"ftp://mucommander.com:21/pub/incoming/0day-warez.zip",
			"ftp://anonymous:john.doe@somewhere.net@mucommander.com:21/pub/incoming/0day-warez.zip"
		};
		
		FileURL f;
		for(int i=0; i<urls.length; i++) {
			try {
				System.out.println("Creating "+urls[i]);
				f = new FileURL(urls[i]); 
				System.out.println("FileURL.toString()= "+f.toString());
				System.out.println(" - filename= "+f.getFilename());
				System.out.println(" - parent= "+f.getParent()+"\n");
			}
			catch(MalformedURLException e) {
				if(com.mucommander.Debug.ON) {
					System.out.println("Unexcepted exception in FileURL() with "+urls[i]);
					e.printStackTrace();
				}
			}
		}
	}
}
