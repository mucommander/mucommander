
package com.mucommander.file;

import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Vector;

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
    private FileURL parentURL;
    private boolean parentURLSet;
    private String filename;
    private String query;
	
    private Hashtable properties;


    /**
     * Creates a new FileURL from the given URL string.
     */
    public FileURL(String url) throws MalformedURLException {
        this(url, null);
    }


    /**
     * Creates a new FileURL object from the given string and using the given FileURL as the parent URL.
     * 
     * <p>If the parent URL contains authentication information (login/password), it will used in the child URL.</p>
     */
    public FileURL(String url, FileURL parentURL) throws MalformedURLException {
        try {
            int pos;
            int urlLen = url.length();
			
            // Parse protocol
            pos = url.indexOf("://");
            if(pos==-1)
                throw new MalformedURLException("Protocol not specified");
//            protocol =  url.substring(0, pos).trim();
            protocol =  url.substring(0, pos);
            // Advance string index
            pos += 3;
			
            // Parse login and password if they have been specified in the URL
            int atPos = url.indexOf('@');		// passwords containing an @ character will not work, but they're not normally allowed in URLs
            //com.mucommander.Debug.trace("url="+url+" pos="+pos+" atPos="+atPos);			
            int colonPos;
            int separatorPos = url.indexOf('/', pos);
            // Filenames may contain @ chars, so atPos must be lower than next separator's position (if any)
            if(atPos!=-1 && (separatorPos==-1 || atPos<separatorPos)) {
                colonPos = url.indexOf(':', pos);
//                login = url.substring(pos, colonPos==-1?atPos:colonPos).trim();
                login = url.substring(pos, colonPos==-1?atPos:colonPos);
                if(colonPos!=-1)
                    password = url.substring(colonPos+1, atPos);
//                    password = url.substring(colonPos+1, atPos).trim();
                // Advance string index
                pos = atPos+1;
            }

            // Parse host and port (if specified)
            colonPos = url.indexOf(':', pos);
            //com.mucommander.Debug.trace("pos="+pos+" colonPos="+colonPos+" atPos="+atPos);			
            // The question mark character (if any) marks the beginning of the query part, only for the http/https protocol.
            // No other supported protocols have a use for the query part, and some protocols such as 'file' allow the '?'
            // character in filenames which thus would be ambiguous.
            int questionMarkPos = "http".equals(protocol)||"https".equals(protocol)?url.indexOf('?', pos):-1;
            separatorPos = url.indexOf('/', pos);
            int hostEndPos;
            // Separator is necessarily before question mark
            if(separatorPos!=-1)
                hostEndPos = separatorPos;
            else if(questionMarkPos !=-1)
                hostEndPos = questionMarkPos;
            else
                hostEndPos = urlLen;

            if(colonPos!=-1 && colonPos<hostEndPos) {
//                host = url.substring(pos, colonPos).trim();
                host = url.substring(pos, colonPos);
//                port = Integer.parseInt(url.substring(colonPos+1, hostEndPos).trim());
                port = Integer.parseInt(url.substring(colonPos+1, hostEndPos));
            }
            else {
//                host = url.substring(pos, hostEndPos).trim();
                host = url.substring(pos, hostEndPos);
            }
			
            if(host.equals(""))
                host = null;
				
            // Parse path part excluding query part
            pos = hostEndPos;
//            path = url.substring(pos, questionMarkPos==-1?urlLen:questionMarkPos).trim();
            path = url.substring(pos, questionMarkPos ==-1?urlLen:questionMarkPos);
            // Empty path means '/'
            if(path.equals(""))
                path = "/";

            //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Raw path = "+path);
			
            // Canonize path: factor out '.' and '..' and replace '~' by home folder for 'file' protocol
            if(!path.equals("/")) {
                pos = 0;	// position of current '/' or '\' character
                int pos2 = 0;	// position of next '/' or '\' character
                int posb;		// temporary position of next '\\' character
                String dir;		// Current directory
                String dirWS;	// Current directory without trailing slash
                Vector pathV = new Vector();	// Will contain directory hierachy
                while((pos=pos2)!=-1) {
                    // Find next '/' or '\' character, whichever comes first
                    pos2 = path.indexOf('/', pos);
                    posb = path.indexOf('\\', pos);
                    if(posb!=-1 && posb<pos2)
                        pos2 = posb;

                    if(pos2==-1) {	// Last dir (or empty string)
                        dir = path.substring(pos, path.length());
                        dirWS = dir;
                    }
                    else {
                        dir = path.substring(pos, ++pos2);		// Dir name includes trailing slash
                        dirWS = dir.substring(0, dir.length()-1);
                    }
					
                    //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Raw dir name = "+dir);
					
                    // Discard '.' and empty directories
                    if((dirWS.equals("") && pathV.size()>0) || dirWS.equals(".")) {
                        //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found . or empty dir");
                        continue;
                    }
                    // Remove last directory
                    else if(dirWS.equals("..")) {
                        //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found .. dir");
                        if(pathV.size()==0)
                            throw new MalformedURLException();
                        pathV.removeElementAt(pathV.size()-1);
                        continue;
                    }
                    // Replace '~' by actual home directory if protocol is 'file' and '~' appears in the path
                    else if(dirWS.equals("~") && protocol.equalsIgnoreCase("file")) {
                        //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found ~ dir");
                        path = path.substring(0, pos) + System.getProperty("user.home") + path.substring(pos+1, path.length());
                        // Will perform another pass at the same position
                        pos2 = pos;
                        continue;
                    }

                    // Add directory to the end of the list
                    pathV.add(dir);
                }
			
                // Reconstruct path from directory list
                path = "";
                int nbDirs = pathV.size();
                for(int i=0; i<nbDirs; i++)
                    path += pathV.elementAt(i);
                // We now have a path free of '.' and '..'

                //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Reconstructed path = "+path+" "+pathV);
            }
			
            // Parse query part (if any)
            if(questionMarkPos !=-1)
                query = url.substring(questionMarkPos, urlLen);
//                query = url.substring(questionMarkPos, urlLen).trim();
		
            // Extract filename and parent from path
            if(path.equals("") || path.equals("/")) {
                filename = null;
                // No parent (parentURL will be null)
            }
            else {	
                String pathCopy = new String(path).replace('\\', '/');
                // Extract filename from path
                int len = pathCopy.length();
                while(pathCopy.charAt(len-1)=='/')
                    --len;
				 
                filename = pathCopy.substring(0, len);
                separatorPos = filename.lastIndexOf('/');
                filename = path.substring(separatorPos+1, len);
                if(filename.equals(""))
                    filename = null;
				
                // If parent URL is not null, keep it for getParent()
                if(parentURL!=null) {
                    this.parentURL = parentURL;
                    this.parentURLSet = true;
                    // Use parent's login and password if login and password not specified in the url
                    if((login==null||login.equals("")) && (password==null||password.equals(""))) {
                        this.login = parentURL.getLogin();
                        this.password = parentURL.getPassword();
                    }
                }
            }
        }
        catch(MalformedURLException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Exception in FileURL(), malformed FileURL "+url+" : "+e);
            throw e;
        }
        catch(Exception e2) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Unexpected exception in FileURL() with "+url+" : "+e2);
            throw new MalformedURLException();
        }
    }


    /**
     * Returns a FileURL constructed from the specified absolute path that must point to a local file. 
     * If the path is not absolute, a <code>MalformedURLException</code> will be thrown.<b>
     *
     * <p>The returned URL will start with <code>file://localhost</code> by default, unless a Windows-style UNC path
     * (i.e. \\hostname\path) containing a hostname is given.
     *
     * @param absPath an absolute path to a local file, the path may start with user home '~'.
     * @param parentURL optional parent's URL, may be <code>null</code>
     * @throws java.net.MalformedURLException if the path is not absolute
     */
    public static FileURL getLocalFileURL(String absPath, FileURL parentURL) throws MalformedURLException {
        if(!absPath.equals("")) {
            char firstChar = absPath.charAt(0);
            int len;
            // Unix-style path
            if(firstChar=='/')
                return new FileURL("file://localhost"+absPath, parentURL);
            // Path starts with a reference to the user home folder, or is a Windows-style path
            else if(firstChar=='~' || absPath.indexOf(":\\")!=-1)
                return new FileURL("file://localhost/"+absPath, parentURL);
            // Windows-style UNC network path ( \\hostname\path ), transform it into:
            // file://hostname/\path
            else if(absPath.startsWith("\\\\") && (len=absPath.length())>2) {
                int pos = absPath.indexOf('\\', 2);
                if(pos==-1)
                    return new FileURL("file://"+absPath.substring(2, len));
                else
                    return new FileURL("file://"+absPath.substring(2, pos)+"/"+absPath.substring(pos, len));
            }
        }

        // Todo: localize that message as it can be displayed to the user
        throw new MalformedURLException("Path not absolute or malformed: "+absPath);
    }

	
    /**
     * Returns the protocol part of this URL (e.g. smb). The returned protocol may never be <code>null</code>.
     */
    public String getProtocol() {
        return protocol;
    }
	

    /**
     * Returns the host part of this URL (e.g. google.com), <code>null</code> if this URL doesn't contain
     * any host.
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
//            login = login.trim();
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
//            password = password.trim();
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
        // ParentURL not set yet
        if(!this.parentURLSet && parentURL==null) {
            // Creates parent URL from reconstructed URL with canonized path
            String url = reconstructURL(path, true, true);
            int len = url.length();
            String urlCopy = new String(url).replace('\\', '/');
            int separatorPos = (urlCopy.endsWith("/")?urlCopy.substring(0, --len):urlCopy).lastIndexOf('/');
            if(separatorPos>7) {
                try { 
                    // Leave trailing separator
                    this.parentURL = new FileURL(url.substring(0, separatorPos+1)); 
                }
                catch(MalformedURLException e) {
                    // No parent (parentURL will be null)
                }
            }
            this.parentURLSet = true;
        }
		
        if(parentURL!=null) {
            // Return a cloned instance of parentURL since it is mutable and changes made in the returned
            // FileURL instance should not impact this instance 
            try {
                return (FileURL)parentURL.clone();
            }
            catch(CloneNotSupportedException e) {
                return null;
            }
        }
	
        return parentURL;
    }
	

    /**
     * Returns the filename part of this URL (e.g. mucommander.jnlp for http://mucommander.com/webstart/mucommander.jnlp)
     * <code>null</code> if this URL doesn't contain any URL (e.g. http://google.com)
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
     * Sets the given properties (name/value pair) to this URL.
     * Properties can be used as a way to pass parameters to AbstractFile constructors.
     */
    public void setProperty(String name, String value) {
        if(properties==null)
            properties = new Hashtable();
		
        properties.put(name, value);
    }
	
    /**
     * Returns the value corresponding to the given property's name, null if the property doesn't exist (has no value).
     */
    public String getProperty(String name) {
        return properties==null?null:(String)properties.get(name);
    }
	
	
    /**
     * Reconstructs the URL and returns its String representation.
     *
     * @param includeAuthInfo if <code>true</code>, login and password (if any) will be included in the returned URL.
     * Login and password in URLs should never be visible to the end user.
     * @param maskPassword if <code>true</code> (and includeAuthInfo param too), password will be replaced by '*' characters. This
     * can be used to display a full URL to the end user without displaying the actual password.
     */
    public String getStringRep(boolean includeAuthInfo, boolean maskPassword) {
        return reconstructURL(this.path, includeAuthInfo, maskPassword);
    }

    /**
     * Reconstructs the URL and returns its String representation.
     *
     * @param includeAuthInfo if <code>true</code>, login and password (if any) will be included in the returned URL and not masked.
     * Login and password in URLs should never be visible to the end user.
     */
    public String getStringRep(boolean includeAuthInfo) {
        return getStringRep(includeAuthInfo, false);
    }


    /**
     * Reconstructs the URL with the given path and returns its String representation.
     *
     * @param path the file's path
     * @param includeAuthInfo if <code>true</code>, login and password (if any) will be included in the returned URL.
     * Login and password in URLs should never be visible to the end user.
     * @param maskPassword if <code>true</code> (and includeAuthInfo param too), password will be replaced by '*' characters. This
     * can be used to display a full URL to the end user without displaying the actual password.
     */
    private String reconstructURL(String path, boolean includeAuthInfo, boolean maskPassword) {
        String s = protocol + "://";
		
        if(includeAuthInfo && login!=null) {
            s += login;
            if(password!=null) {
                s += ":";
                if(maskPassword) {
                    int passwordLength = password.length();
                    for(int i=0; i<passwordLength; i++)
                        s += "*";
                }
                else {
                    s += password;
                }
            }
            s += "@";
        }

        if(host!=null)
            s += host;
		
        if(port!=-1)
            s += ":"+port;

        if(host!=null || !path.equals("/"))	// Test to avoid URLs like 'smb:///'
            s += path;
		
        if(query!=null)
            s += query;
		
        return s;
    }


    /**
     * Returns a clone of this FileURL, useful because FileURL is mutable.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	
    public String toString() {
        return getStringRep(true);
    }
	
	
    /**
     * Tests FileURL instances for equality :<br>
     *  - authentication info (login and password) are not taken into account when testing equality
     *  - there can be a trailing slash or backslash difference between 2 identical URLs, true will be returned
     *
     * @return true if both FileURL are equal.
     */
    public boolean equals(Object o) {
        if(!(o instanceof FileURL))
            return false;
		
        //		return ((FileURL)o).getStringRep(true).equals(getStringRep(true));

        // Do not take into account authentication info (login and password) to test equality
        String rep1 = getStringRep(false);
        String rep2 = ((FileURL)o).getStringRep(false);
		
        // If strings are equal, return true
        if(rep1.equals(rep2))
            return true;
		
        // If difference between the 2 strings is just a trailing slash or backslash, then we consider them equal and return true  
        int len1 = rep1.length();
        int len2 = rep2.length();
        if(Math.abs(len1-len2)==1 && (len1>len2 ? rep1.startsWith(rep2) : rep2.startsWith(rep1))) {
            char cdiff = len1>len2 ? rep1.charAt(len1-1) : rep2.charAt(len2-1);
            if(cdiff=='/' || cdiff=='\\')
                return true;
        }
	
        return false;
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
            "smb://maxence:yep@garfield",
            "smb://maxence:yep@garfield/shared/music",
            "ftp://mucommander.com",
            "ftp://mucommander.com:21",
            "ftp://mucommander.com:21/",
            "ftp://mucommander.com:21/pub/incoming",
            "ftp://mucommander.com:21/pub/incoming/",
            "ftp://mucommander.com:21/pub/incoming/0day-warez.zip",
            "ftp://anonymous:john.doe@somewhere.net@mucommander.com:21/pub/incoming/0day-warez.zip",
            "sftp://maxence:yep@192.168.1.2",
            "file://relative_path",
            "file:///absolute_path",
            "file://localhost/absolute_path",
            "file://localhost/~/Projects/",
            "file://localhost/C:",
            "file://localhost/C:\\",
            "file://localhost/C:\\Projects",
            "file://localhost/C:\\Projects\\",
            "file://localhost/C:\\Documents and Settings",
            "file://localhost/~/../..",
            "file://localhost/~/Projects/mucommander/../mucommander/./source/..",
            "file://localhost/Users/maxence/Desktop/en@boldquot.header"
        };
		
        FileURL f;
        for(int i=0; i<urls.length; i++) {
            try {
                System.out.println("Creating "+urls[i]);
                f = new FileURL(urls[i]); 
                System.out.println("FileURL.toString()= "+f.toString());
                System.out.println(" - path= "+f.getPath());
                System.out.println(" - host= "+f.getHost());
                if(f.getLogin()!=null)
                    System.out.println(" - login/pass= "+f.getLogin()+"/"+f.getPassword());
                System.out.println(" - filename= "+f.getFilename());
                System.out.println(" - parent= "+f.getParent());
                if(f.getParent()!=null)
                    System.out.println(" - parent path= "+f.getParent().getPath()+"\n");
                else
                    System.out.println();
				
                if(f.getProtocol().equals("file"))
                    System.out.println(" FSFile's path="+FileFactory.getFile(f.getPath(), true).getAbsolutePath());
            }
            catch(java.io.IOException e) {
                if(com.mucommander.Debug.ON) {
                    System.out.println("Unexcepted exception in FileURL() with "+urls[i]);
                    e.printStackTrace();
                }
            }
        }
    }
}
