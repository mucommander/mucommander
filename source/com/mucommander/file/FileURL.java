/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.file;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.auth.Credentials;
import com.mucommander.file.compat.CompatURLStreamHandler;
import com.mucommander.file.impl.local.LocalFile;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class parses a file URL without any knowledge of the underlying protocol.
 * URL are expected to respect the following format :<br>
 * 	<code>protocol://[login[:password]@]host[:port][/path][?query]</code>
 *
 * @author Maxence Bernard
 */
public class FileURL implements Cloneable {

    private String protocol;
    private String host;
    private int port = -1;
    private String path;
    private String filename;
    private String query;

    private Credentials credentials;
    private Hashtable properties;

    /** String designating the localhost. */
    public final static String LOCALHOST = "localhost";

    /** Charset used to encode and decode special characters in URL. */
    private final static String URL_CHARSET = "UTF-8";


    /**
     * Protected constructor.
     */
    protected FileURL() {
    }


    /**
     * Creates a new FileURL from the given URL string.
     * @param url the string to parse as <code>FileURL</code>.
     * @throws MalformedURLException if the specified string isn't a valid URL.
     */
    public FileURL(String url) throws MalformedURLException {
        //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Parsing "+url);

        try {
            int pos;
            int protocolDelimPos = url.indexOf("://");
            int urlLen = url.length();

            // If path contains no protocol, consider the file as a local file
            if(protocolDelimPos==-1) {
                // Treat the URL as local file path if it starts with:
                // - '/' and OS doesn't use root drives (Unix-style path)
                // - a drive letter and OS uses root drives (Windows-style)
                // - a ~ character (refers to the user home folder)
                if((!LocalFile.USES_ROOT_DRIVES && url.startsWith("/")) || (LocalFile.USES_ROOT_DRIVES && url.indexOf(":\\")==1) || url.startsWith("~")) {
                    protocol = FileProtocols.FILE;
                    host = LOCALHOST;
                    String pathSeparator = getPathSeparator(FileProtocols.FILE);
                    path = canonizePath(url, pathSeparator, true);
                    filename = getFilenameFromPath(path, pathSeparator);

                    // All done, return
                    return;
                }

                // Handle Windows-style UNC network paths ( \\hostname\path ):
                // - under Windows, transform it into a URL in the file://hostname/path form,
                //   LocalFile constructor will translate it back into an UNC network path
                // - under other OS, conveniently transform it into smb://hostname/path to be nice with folks
                //   who've spent too much time using Windows
                else if(url.startsWith("\\\\") && urlLen>2) {
                    if(PlatformManager.WINDOWS.isCurrent()) {
                        pos = url.indexOf('\\', 2);
                        if(pos==-1)
                            url =  FileProtocols.FILE+"://"+url.substring(2);
                        else
                            url = FileProtocols.FILE+"://"+url.substring(2, pos)+"/"+(pos==urlLen-1?"":url.substring(pos+1));

                        // Update protocol delimiter position
                        protocolDelimPos = FileProtocols.FILE.length();
                    }
                    else {
                        url = FileProtocols.SMB+"://"+url.substring(2).replace('\\', '/');

                        // Update protocol delimiter position
                        protocolDelimPos = FileProtocols.SMB.length();
                    }

                    // Update URL's length
                    urlLen = url.length();
                }
                // This doesn't look like a valid path, throw an MalformedURLException
                else {
                    // Todo: localize that message as it can be displayed back to the user
                    throw new MalformedURLException("Path not absolute or malformed: "+url);
                }
            }

            // Start URL parsing

            protocol =  url.substring(0, protocolDelimPos);
            // Advance string index
            pos = protocolDelimPos+3;

            int separatorPos = url.indexOf('/', pos);

            // The question mark character (if any) marks the beginning of the query part, only for the http/https protocol.
            // No other supported protocols have a use for the query part, and some protocols such as 'file' allow the '?'
            // character in filenames which thus would be ambiguous.
            int questionMarkPos = FileProtocols.HTTP.equals(protocol)||FileProtocols.HTTPS.equals(protocol)?url.indexOf('?', pos):-1;
            int hostEndPos; // Contains the position of the beginning of the path/query part
            if(separatorPos!=-1)    // Separator is necessarily before question mark
                hostEndPos = separatorPos;
            else if(questionMarkPos !=-1)
                hostEndPos = questionMarkPos;
            else
                hostEndPos = urlLen;

            // URL part before path/query part and without protocol://
            String urlBP = url.substring(pos, hostEndPos);
            pos = 0;

            // Parse login and password if they have been specified in the URL
            // Login/password may @ characters, so consider the last '@' occurrence (if any) as the host delimiter
            // Note that filenames may contain @ characters, but that's OK here since path is not contained in the String
            int atPos = urlBP.lastIndexOf('@');
            int colonPos;
            // Filenames may contain @ chars, so atPos must be lower than next separator's position (if any)
            if(atPos!=-1 && (separatorPos==-1 || atPos<separatorPos)) {
                colonPos = urlBP.indexOf(':');
                String login = urlBP.substring(0, colonPos==-1?atPos:colonPos);
                String password;
                if(colonPos!=-1)
                    password = urlBP.substring(colonPos+1, atPos);
                else
                    password = null;

                if(!"".equals(login) || !(password==null || "".equals(password)))
                    this.credentials = new Credentials(login, password);

                // Advance string index
                pos = atPos+1;
            }

            // Parse host and port (if specified)
            colonPos = urlBP.indexOf(':', pos);

            if(colonPos!=-1) {
                host = urlBP.substring(pos, colonPos);
                port = Integer.parseInt(urlBP.substring(colonPos+1));
            }
            else {
                host = urlBP.substring(pos);
            }
			
            if(host.equals(""))
                host = null;
				
            // Parse path part excluding query part
            pos = hostEndPos;
            path = url.substring(pos, questionMarkPos==-1?urlLen:questionMarkPos);

            // Empty path means '/'
            if(path.equals(""))
                path = "/";

            String pathSeparator = getPathSeparator(protocol);

            // Canonize path: factor out '.' and '..' and replace '~' by home folder for 'file' protocol
            path = canonizePath(path, pathSeparator, protocol.equals(FileProtocols.FILE));

            if(Debug.ON && path.trim().equals("")) Debug.trace("Warning: path should not be empty, url="+url);

            // Parse query part (if any)
            if(questionMarkPos !=-1)
                query = url.substring(questionMarkPos);

            // Extract filename from path
            filename = getFilenameFromPath(path, pathSeparator);
        }
        catch(MalformedURLException e) {
            throw e;
        }
        catch(Exception e2) {
            if(com.mucommander.Debug.ON) {
                com.mucommander.Debug.trace("Unexpected exception in FileURL() with "+url+" : "+e2);
                e2.printStackTrace();
            }
            throw new MalformedURLException();
        }
    }


    /**
     * Canonize path: factor out '.' and '..' and replace '~' by home folder if the path corresponds to a local file.
     *
     * @param path the path to canonize
     * @param separator the path separator to use
     * @return the canonized path
     * @throws MalformedURLException if the path is invalid
     */
    private static String canonizePath(String path, String separator, boolean localFile) throws MalformedURLException {
        if(!path.equals("/")) {
            int pos;	    // position of current path separator
            int pos2 = 0;	// position of next path separator
            String dir;		// Current directory
            String dirWS;	// Current directory without trailing slash
            Vector pathV = new Vector();	// Will contain directory hierachy
            while((pos=pos2)!=-1) {
                // Get the index of the next path separator occurrence
                pos2 = path.indexOf(separator, pos);

                if(pos2==-1) {	// Last dir (or empty string)
                    dir = path.substring(pos);
                    dirWS = dir;
                }
                else {
                    dir = path.substring(pos, ++pos2);		// Dir name includes trailing slash
                    dirWS = dir.substring(0, dir.length()-1);
                }

                // Discard '.' and empty directories
                if((dirWS.equals("") && pathV.size()>0) || dirWS.equals(".")) {
                    continue;
                }
                // Remove last directory
                else if(dirWS.equals("..")) {
                    if(pathV.size()==0)
                        throw new MalformedURLException();
                    pathV.removeElementAt(pathV.size()-1);
                    continue;
                }
                // Replace '~' by actual home directory if protocol is 'file' and '~' appears in the path
                else if(dirWS.equals("~") && localFile) {
                    path = path.substring(0, pos) + System.getProperty("user.home") + path.substring(pos+1);
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
        }

        return path;
    }


    /**
     * Extracts a filename from the given path and returns it, or null if the path does not contain a filename.
     */
    private static String getFilenameFromPath(String path, String separator) {
        if(path.equals("") || path.equals("/"))
            return null;

        // Remove any trailing separator
        String filename = path.endsWith(separator)?path.substring(0, path.length()-separator.length()):path;

        // Extract filename
        return filename.substring(filename.lastIndexOf(separator)+1);
    }


    /**
     * Returns the protocol part of this FileURL (e.g. smb). The returned protocol may never be <code>null</code>.
     * @return the protocol part of this <code>FileURL</code>.
     * @see    #setProtocol(String)
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol part of this FileURL. The specified protocol must not be null.
     * @param protocol new protocol part for this <code>FileURL</code>.
     * @see            #getProtocol()
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    /**
     * Returns the path separator for the given protocol. If the {@link FileProtocols#FILE} protocol is specified,
     * the underlying local filesystem's separator as returned by {@link LocalFile#SEPARATOR} will be returned.
     *
     * <p>By default, if the given protocol is not known (not one of the protocols listed in {@link FileProtocols}),
     * "/" is returned.
     * 
     * @param protocol a protocol name
     * @return the path separator for the given protocol
     */
    public static String getPathSeparator(String protocol) {
        if(FileProtocols.FILE.equals(protocol))
            return LocalFile.SEPARATOR;

        return "/";
    }


    /**
     * Returns the path separator used in this FileURL. Has the same effect as calling {@link #getPathSeparator(String)}
     * with the value of {@link #getProtocol()}.
     *
     * @return the path separator used in this FileURL
     */
    public String getPathSeparator() {
        return getPathSeparator(protocol);
    }


    /**
     * Returns the host part of this FileURL (e.g. google.com), <code>null</code> if this FileURL doesn't contain
     * any host.
     * @return the host part of this <code>FileURL</code>.
     * @see    #setHost(String)
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host part of this FileURL, <code>null</code> for no host.
     * @param host new host part for this <code>FileURL</code>.
     * @see        #getHost()
     */
    public void setHost(String host) {
        this.host = host;
    }
    
	
    /**
     * Returns the port specified in this FileURL (e.g. 8080) if there is one, -1 otherwise.
     * (-1 means the protocol's default port).
     * @return the port specified in this <code>FileURL</code>.
     * @see    #setPort(int)
     */
    public int getPort() {
        return port;
    }
	
    /**
     * Sets a custom port, -1 for no custom port (use the protocol's defaut port).
     * @param port new port for this <code>FileURL</code>.
     * @see        #getPort()
     */
    public void setPort(int port) {
        this.port = port;
    }
	
	
    /**
     * Returns the login specified in this FileURL (e.g. maxence for ftp://maxence@mucommander.com),
     * <code>null</code> otherwise.
     * @return the login specified in this <code>FileURL</code>.
     */
    public String getLogin() {
        return credentials==null?null:credentials.getLogin();
    }


    /**
     * Returns the password specified in this FileURL (e.g. blah for ftp://maxence:blah@mucommander.com),
     * <code>null</code> otherwise.
     * @return the password specified in this <code>FileURL</code>.
     */
    public String getPassword() {
        return credentials==null?null:credentials.getPassword();
    }


    /**
     * Convenience method that discards any credentials (login and password) contained by this FileURL.
     * It has the same effect as calling {@link #setCredentials(com.mucommander.auth.Credentials)} with a null value.
     *
     */
    public void discardCredentials() {
        this.credentials = null;
    }


    /**
     * Returns true if this FileURL contains credentials. If true is returned, {@link #getCredentials()}
     * will return a non-null value.
     * @return <code>true</code> if this <code>FileURL</code> contains credentials, <code>false</code> otherwise.
     */
    public boolean containsCredentials() {
        return credentials!=null;
    }


    /**
     * Returns the credentials (login and password) contained in this FileURL, wrapped in an {@link Credentials} object.
     * Returns null if this FileURL doesn't contain any login or password.
     * @return the credentials contained by this <code>FileURL</code>, <code>null</code> if none.
     * @see    #setCredentials(Credentials)
     */
    public Credentials getCredentials() {
        return credentials;
    }


    /**
     * Sets the credentials (login and password) contained by this FileURL. Any credentials contained by this FileURL
     * will be discarded. Null can be passed to discard existing credentials.
     *
     * @param credentials the new credentials to use, replacing any existing credentials. If null is passed, existing
     * credentials will be discarded. 
     * @see #getCredentials()
     */
    public void setCredentials(Credentials credentials) {
        if(credentials==null || credentials.isEmpty())  // Empty credentials are equivalent to null credentials
            this.credentials = null;
        else
            this.credentials = credentials;
    }

	
    /**
     * Returns the path part of this FileURL (e.g. /webstart/mucommander.jnlp for http://mucommander.com/webstart/mucommander.jnlp).
     * @return the path part of this <code>FileURL</code>.
     * @see    #setPath(String)
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path part of this FileURL. The specified path must not be <code>null</code>.
     * @param path new path part for this <code>FileURL</code>.
     * @see        #getPath()
     */
    public void setPath(String path) {
        this.path = path;
        // Extract new filename from path
        this.filename = getFilenameFromPath(path, getPathSeparator());
    }

	
    /**
     * Returns this FileURL's parent, or null if this FileURL has no parent (path is "/").
     * The returned parent will have the same protocol, host, port, credentials and properties as this FileURL.
     * The filename and query parts of this FileURL (if any) will not be set in the returned parent, both will be null.
     *
     * <p>Note: this method returns a new FileURL instance everytime it is called, and all mutable fields of this FileURL
     * are cloned. Therefore the returned parent can be safely modified without risking to modify other FileURL instances.</p>
     * @return this <code>FileURL</code>'s parent, <code>null</code> if it doesn't have one.
     */
    public FileURL getParent() {
        // If path equals '/', url has no parent
        if(!(path.equals("/") || path.equals(""))) {
            String separator = getPathSeparator();

            // Remove any trailing separator
            String parentPath = path.endsWith(separator)?path.substring(0, path.length()-separator.length()):path;

            // Resolve parent folder's path and reconstruct parent URL
            int lastSeparatorPos = parentPath.lastIndexOf(separator);
            if(lastSeparatorPos!=-1) {
                FileURL parentURL = new FileURL();

                parentURL.protocol = protocol;
                parentURL.host = host;
                parentURL.port = port;
                parentURL.path = parentPath.substring(0, lastSeparatorPos+1);  // Keep trailing slash
                parentURL.filename = getFilenameFromPath(parentURL.path, separator);

                // Set same credentials for parent, (if any)
                // Note: Credentials are immutable.
                parentURL.credentials = credentials;

                // Copy properties to parent (if any)
                if(properties!=null)
                    parentURL.properties = new Hashtable(properties);

                return parentURL;
            }
        }

        return null;    // URL has no parent
    }


    /**
     * Returns the realm of a given location, that is the URL to the host (if this URL contains one), port
     * (if this URL contains one) and share path (if the location's protocol has a notion of share, e.g. SMB).
     * Properties contained by the give FileURL are copied, but Credentials aren't.
     *
     * <p>A few examples:
     * <ul>
     * <li>smb://somehost/someshare/somefolder/somefile -> smb://someserver/someshare/
     * <li>ftp://somehost/somefolder/somefile -> ftp://someserver/
     * <li>sftp://somehost:666/ -> sftp://someserver:666/
     * <li>smb:// -> smb://
     * </ul>
     *
     * <p>Note: this method returns a new FileURL instance everytime it is called.
     * Therefore the returned parent can be safely modified without risking to modify other FileURL instances.
     *
     * @param location the location to a resource on a remote server
     * @return the location's realm
     */
    public static FileURL resolveRealm(FileURL location) {
        String protocol = location.getProtocol();
        String newPath = "/";

        if(protocol.equals(FileProtocols.SMB)) {
            newPath = location.getPath();
            // Find first path token (share)
            int pos = newPath.indexOf(1, '/');
            newPath = newPath.substring(0, pos==-1?newPath.length():pos+1); 
        }

        FileURL realm = new FileURL();
        realm.protocol = location.protocol;
        realm.host = location.host;
        realm.port = location.port;
        realm.path = newPath;

        if(location.properties!=null)   // Clone properties if lcoation contains any
            realm.properties = (Hashtable)location.properties.clone();

        return realm;
    }


    /**
     * Returns the filename part of this FileURL (e.g. mucommander.jnlp for http://mucommander.com/webstart/mucommander.jnlp)
     * <code>null</code> if this FileURL doesn't contain any URL (e.g. http://google.com)
     * @return the filename part of this <code>FileURL</code>, <code>null</code> if none.
     * @see    #setPath(String)
     * @see    #getFilename(boolean)
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Returns the filename part of this FileURL, and if specified, decodes URL-encoded characters (e.g. %5D%35)
     * @param  urlDecode whether to URL-decode the filename.
     * @return the filename part of this <code>FileURL</code>, URL-decoded if necessary.
     * @see    #setPath(String)
     * @see    #getFilename()
     */
    public String getFilename(boolean urlDecode) {
        try {
            if(urlDecode && filename!=null)
                return URLDecoder.decode(filename, URL_CHARSET);
        }
        catch(UnsupportedEncodingException e) {
        }

        return filename;
    }

    // Note: no setFilename method, setPath should be used for that purpose   

    /**
     * Returns the query part of this FileURL if there is one (e.g. ?dummy=1&void=1 for http://mucommander.com/useless.php?dummy=1&void=1),
     * <code>null</code> otherwise.
     * @return the query part of this <code>FileURL</code> if present, <code>null</code> otherwise.
     * @see    #setQuery(String)
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query part of this FileURL, <code>null</code> for no query part.
     * @param query new query part for this <code>FileURL</code>.
     * @see         #getQuery()
     */
    public void setQuery(String query) {
        this.query = query;
    }

	
    /**
     * Returns the value corresponding to the given property's name, null if the property doesn't exist (has no value).
     * @param  name name of the property whose value should be retrieved.
     * @return      the value associated with the specified <code>name</code>, <code>null</code> if not found.
     * @see         #setProperty(String,String)
     */
    public String getProperty(String name) {
        return properties==null?null:(String)properties.get(name);
    }
	
    /**
     * Sets the given properties (name/value pair) to this FileURL.
     * Properties can be used as a way to pass parameters to AbstractFile constructors.
     * @param name  name of the property to set.
     * @param value value for the property.
     * @see         #getProperty(String)
     */
    public void setProperty(String name, String value) {
        if(properties==null)
            properties = new Hashtable();

        properties.put(name, value);
    }


    /**
     * Returns an Enumeration of property keys, or null if this FileURL contains no keys.
     * @return an <code>Enumaration</code> on all available property names, <code>null</code> if none.
     */
    public Enumeration getPropertyKeys() {
        // NOTE: might be cleaner to return an empty enumeration?
        return properties==null?null:properties.keys();
    }

    /**
     * Copies the properties of the given FileURL into this FileURL.
     * @param url url whose properties should be copied.
     */
    public void copyProperties(FileURL url) {
        Enumeration propertyKeys = url.getPropertyKeys();
        if(propertyKeys!=null) {
            String key;
            while(propertyKeys.hasMoreElements()) {
                key = (String)propertyKeys.nextElement();
                setProperty(key, url.getProperty(key));
            }
        }
    }


    /**
     * Returns a String representation of this FileURL.
     *
     * @param includeCredentials if <code>true</code>, login and password (if any) will be included in the returned URL.
     * Login and password in URLs should never be visible to the end user.
     * @param maskPassword if <code>true</code> (and includeCredentials param too), password will be replaced by '*' characters. This
     * can be used to display a full URL to the end user without displaying the actual password.
     * @return a string representation of this <code>FileURL</code>.
     */
    public String toString(boolean includeCredentials, boolean maskPassword) {
        return reconstructURL(this.path, includeCredentials, maskPassword);
    }

    /**
     * Returns a String representation of this FileURL.
     *
     * @param includeCredentials if <code>true</code>, login and password (if any) will be included in the returned URL and not masked.
     * Login and password in URLs should never be visible to the end user.
     * @return a string representation of this <code>FileURL</code>.
     */
    public String toString(boolean includeCredentials) {
        return toString(includeCredentials, false);
    }


    /**
     * Reconstructs the URL with the given path and returns its String representation.
     *
     * @param path the file's path
     * @param includeCredentials if <code>true</code>, login and password (if any) will be included in the returned URL.
     * Login and password in URLs should never be visible to the end user.
     * @param maskPassword if <code>true</code> (and includeCredentials param too), password will be replaced by '*' characters. This
     * can be used to display a full URL to the end user without displaying the actual password.
     */
    private String reconstructURL(String path, boolean includeCredentials, boolean maskPassword) {
        String s = protocol + "://";
		
        if(includeCredentials && credentials!=null) {
            s += credentials.getLogin();
            String password = credentials.getPassword();
            if(!"".equals(password)) {
                s += ":";
                if(maskPassword)
                    s += credentials.getMaskedPassword();
                else
                    s += password;
            }
            s += "@";
        }

        if(host!=null)
            s += host;
		
        if(port!=-1)
            s += ":"+port;

        if(host!=null || !path.equals("/"))	// Test to avoid URLs like 'smb:///'
            s += path.startsWith("/")?path:"/"+path;    // Add a leading '/' if path doesn't already start with one, needed in particular for Windows paths

        if(query!=null)
            s += query;
		
        return s;
    }


    /**
     * Creates and returns a <code>java.net.URL</code> referring to the same location as this <code>FileURL</code>.
     * The <code>java.net.URL</code> is created from the string representation of this <code>FileURL</code>.
     * Thus, any credentials this <code>FileURL</code> contains are preserved, but properties are lost.
     *
     * <p>The returned <code>URL</code> uses an {@link AbstractFile} to access the associated resource.
     * An {@link AbstractFile} instance is created by the underlying <code>URLConnection</code> when the URL is
     * connected.</p>  
     *
     * <p>It is important to note that this method is provided for interoperability purposes, for the sole purpose of
     * connecting to APIs that require a <code>java.net.URL</code>.</p>
     *
     * @return a <code>java.net.URL</code> referring to the same location as this <code>FileURL</code>
     * @throws MalformedURLException if the java.net.URL could not parse the location of this FileURL
     */
    public URL getJavaNetURL() throws MalformedURLException {
        return new URL(null, toString(true), new CompatURLStreamHandler());
    }


    /**
     * Test method.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        String urls[] = new String[]{
            "http://google.com",
            "http://mucommander.com",
            "http://mucommander.com/",
            "http://mucommander.com/webstart/",
            "http://mucommander.com/webstart/index.html",
            "http://mucommander.com/webstart/index.php?dummy=1&useless=true",
            "http://login:pass@mucommander.com:8080/webstart/index.php?dummy=1&useless=true",
            "smb://",
            "smb://a",
            "smb://a/b",
            "smb://maxence@garfield",
            "smb://maxence:yep@garfield",
            "smb://maxence:yep@garfield/shared/music",
            "ftp://mucommander.com",
            "ftp://mucommander.com:21",
            "ftp://mucommander.com:21/",
            "ftp://mucommander.com:21/pub/incoming",
            "ftp://mucommander.com:21/pub/incoming/",
            "ftp://mucommander.com:21/pub/incoming/0day-warez.zip",
            // @ characters in login or password are not valid
            "ftp://anonymous:john.doe@somewhere.net@mucommander.com:21/pub/incoming/0d@y-warez.zip",
            "sftp://maxence:yep@192.168.1.2",
            "file://relative_path",
            "file:///absolute_path",
            "file://localhost/absolute_path",
            "file://localhost/~/Projects/",
            // Not valid (not absolute)
            "file://localhost/C:",
            "file://localhost/C:\\",
            "file://localhost/C:\\Projects",
            "file://localhost/C:\\Projects\\",
            "file://localhost/C:\\Documents and Settings",
            "file://localhost/~/../..",
            "file://localhost/~/Projects/mucommander/../mucommander/./source/..",
            "file://localhost/Users/maxence/Desktop/en@boldquot.header",
            "\\\\somehost\\somepath",
            "\\\\somehost\\somepath\\",
            "\\\\somehost\\",
            "\\\\somehost"
        };

        try {
            FileURL f;
            for(int i=0; i<urls.length; i++) {
                System.out.println("Creating "+urls[i]);
                f = new FileURL(urls[i]);
                System.out.println(" - path= "+f.getPath());
                System.out.println(" - host= "+f.getHost());
                System.out.println(" - port= "+f.getPort());
                if(f.getLogin()!=null)
                    System.out.println(" - login/pass= "+f.getLogin()+"/"+f.getPassword());
                System.out.println(" - filename= "+f.getFilename());
                System.out.println(" - query= "+f.getQuery());
                String stringRep = f.toString(true);
                System.out.println("FileURL.toString(true)= "+stringRep+" "+(stringRep.equals(urls[i])?"EQUALS":"DIFFERS"));
                System.out.println(" - parent= "+f.getParent());
                if(f.getParent()!=null)
                    System.out.println(" - parent path= "+f.getParent().getPath());

                System.out.println("java.net.URL= "+f.getJavaNetURL().toString());

                System.out.println();
            }
        }
        catch(java.io.IOException e) {
            if(com.mucommander.Debug.ON) {
                System.out.println("Caught an unexcepted exception in FileURL()");
                e.printStackTrace();
            }
        }

    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns a String representation of this FileURL, without the credentials it may contain.
     */
    public String toString() {
        return toString(false);
    }


    /**
     * Returns a clone of this FileURL. The returned instance can safely be modified without impacting this FileURL.
     */
    public Object clone() {
        // Create a new FileURL return it, instead of using Object.clone() which is probably way slower;
        // most FileURL fields are immutable and as such reused in cloned instance
        FileURL clonedURL = new FileURL();

        // Immutable fields
        clonedURL.protocol = protocol;
        clonedURL.host = host;
        clonedURL.port = port;
        clonedURL.path = path;
        clonedURL.filename = filename;
        clonedURL.query = query;
        clonedURL.credentials = credentials;  // Note: Credentials are immutable.

        // Mutable fields
        if(properties!=null)    // Copy properties (if any)
            clonedURL.properties = new Hashtable(properties);

        return clonedURL;
    }


    /**
     * Tests FileURL instances for equality.
     * <p>
     * Two <code>FileURL</code> instances are said to be equal if:
     * <ul>
     * <li>credentials (login and password) are not taken into account when testing equality
     * <li>case is ignored
     * <li>there can be a trailing slash or backslash difference in the path of 2 otherwise identical URLs,
     * true will still be returned
     * </ul>
     * </p>
     * @param  o object against which to compare this <code>FileURL</code>.
     * @return   true if both FileURL instances are equal.
     */
    public boolean equals(Object o) {
        if(o==null || !(o instanceof FileURL))
            return false;

        // Do not take into account credentials (login and password) to test equality
        String url1 = toString(false).toLowerCase();
        String url2 = ((FileURL)o).toString(false).toLowerCase();

        // If strings are equal, return true
        if(url1.equals(url2))
            return true;

        // If difference between the 2 strings is just a trailing slash or backslash, then we consider them equal and return true
        int len1 = url1.length();
        int len2 = url2.length();
        if(Math.abs(len1-len2)==1 && (len1>len2 ? url1.startsWith(url2) : url2.startsWith(url1))) {
            char cdiff = len1>len2 ? url1.charAt(len1-1) : url2.charAt(len2-1);
            if(cdiff=='/' || cdiff=='\\')
                return true;
        }

        return false;
    }
}
