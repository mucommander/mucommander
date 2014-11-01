/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



package com.mucommander.commons.file;

import com.mucommander.commons.file.compat.CompatURLStreamHandler;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

/**
 * This class represents a Uniform Resource Locator (URL). The general format of a URL is as follows:
 * <pre>
 * 	scheme://[login[:password]@]host[:port][/path][?query]
 * </pre>
 *
 * <h3>Instanciation</h3>
 * <p>
 * FileURL cannot be instantiated directly, instances can be created using {@link #getFileURL(String)}.
 * Unlike the <code>java.net.URL</code> and <code>java.net.URI</code> classes, FileURL instances are mutable --
 * all URL parts can be freely modified. FileURL instances can also be cloned using the standard {@link #clone()} method.
 * </p>
 *
 * <h3>Handlers and Scheme-specific attributes</h3>
 * <p>
 * In addition to standard URL features, FileURL gives access to scheme-specific attributes:
 * <dl>
 *  <dt>{@link #getStandardPort() standard port}</dt><dd>the standard port implied when no port is defined in the URL,
 * e.g. 21 for FTP</dd>
 *  <dt>{@link #getPathSeparator() path separator}</dt><dd>the character(s) that separates path fragments, e.g. '/' for
 * most schemes, '\' for local paths under certain OSes like Windows.</dd>
 *  <dt>{@link #getGuestCredentials() guest credentials}</dt><dd>credentials to authenticate as a guest, e.g. 'GUEST'
 * for SMB, 'anonymous' for FTP.</dd>
 *  <dt>{@link #getRealm() authentication realm}</dt><dd>the base URL throughout which a set of credentials can be used.
 * </dd>
 * </dl>
 * These attribute values are provided by the {@link SchemeHandler} registered with the scheme, if any.
 * </p>
 * <p>
 * In addition to providing those attributes, a SchemeHandler provides a {@link SchemeParser}
 * instance which takes care of the actual parsing of URLs of a particular scheme when {@link #getFileURL(String)} is
 * invoked. This allows for scheme-specific parsing, like for example for the query part which should only be parsed
 * and considered as a separate part for certain schemes such as HTTP.
 * </p>
 * <p>
 * This class registers a number of handlers for the schemes/protocols supported by the muCommander file API.
 * Additional handlers can be registered dynamically using {@link #registerHandler(String, SchemeHandler)}. Likewise,
 * existing handlers can be unregistered or replaced at runtime using <code>registerHandler</code> and
 * <code>unregisterHandler</code>.
 * </p>
 * <p>
 * A {@link #getDefaultHandler() default handler} is used for schemes that do not have a specific handler registered.
 * It provides default values for the above-mentioned attributes and provides a parser that parses those scheme URLs.
 * The default handler's parser is also used for parsing locations passed to {@link #getFileURL(String)} that do not
 * contain a scheme (i.e. without the leading <code>scheme://</code>). Those locations can be system-dependent,
 * local and absolute paths, or UNC paths. These paths are turned by the parser into an equivalent, fully-qualified URL. 
 * </p>
 *
 * <h3>Properties</h3>
 * <p>
 * This class provides methods to attach properties to a FileURL instance. These properties are not part of the URL
 * itself and are absent from its string representation. They allow protocol-specific properties like connection
 * settings to be passed along, to {@link AbstractFile} instances in particular.
 * </p>
 *
 * <h3>Limitations</h3>
 * <p>
 * This class has the several limitations that are worth noting:
 * <ul>
 *  <li>URL syntax is not strictly enforced: some invalid URLs (as per RFC) will be parsed without throwing an exception</li>
 *  <li>relative URLs are not supported</li>
 *  <li>no proper percent encoding/decoding </li>
 *  <li>no support for the fragment part</li>
 * </ul>
 * Some of these limitations will be addressed in upcoming revisions of this class.
 * </p>
 *
 * @see SchemeHandler
 * @see SchemeParser
 * @author Maxence Bernard
 */
public class FileURL implements Cloneable {

    // Todo: add support for the fragment part
    // Todo: add percent encoding/decoding

    /** Handler instance that provides the scheme-specific features of this FileURL */
    private SchemeHandler handler;

    /** Scheme part */
    private String scheme;
    /** Port part, -1 if this URL has none */
    private int port = -1;
    /** Host part, null if this URL has none */
    private String host;
    /** Path part */
    private String path;
    /** Filename, extracted from the path, null if the path has none */
    private String filename;
    /** Query part, null if this URL has none */
    private String query;

    /** Properties, null if none have been set thus far */
    private Hashtable<String, String> properties;
    /** Credentials (login and password parts), null if this URL has none */
    private Credentials credentials;

    /** Caches the value returned by #hashCode() for as long as this instance is not modified */
    private int hashCode;

    /** Default handler for schemes that do not have a specific handler */
    private final static SchemeHandler DEFAULT_HANDLER = new DefaultSchemeHandler();

    /** Maps schemes (String) onto SchemeHandler instances */
    private final static Hashtable<String, SchemeHandler> handlers = new Hashtable<String, SchemeHandler>();

    /** String designating the localhost */
    public final static String LOCALHOST = "localhost";


    static {
        // Register custom handlers for known schemes

        registerHandler(FileProtocols.FILE, new DefaultSchemeHandler(new DefaultSchemeParser(new DefaultPathCanonizer(LocalFile.SEPARATOR, System.getProperty("user.home")), false), -1, System.getProperty("file.separator"), AuthenticationType.NO_AUTHENTICATION, null));
        registerHandler(FileProtocols.FTP, new DefaultSchemeHandler(new DefaultSchemeParser(), 21, "/", AuthenticationType.AUTHENTICATION_REQUIRED, new Credentials("anonymous", "anonymous_coward@mucommander.com")));
        registerHandler(FileProtocols.SFTP, new DefaultSchemeHandler(new DefaultSchemeParser(), 22, "/", AuthenticationType.AUTHENTICATION_REQUIRED, null));
        registerHandler(FileProtocols.HDFS, new DefaultSchemeHandler(new DefaultSchemeParser(true), 8020, "/", AuthenticationType.AUTHENTICATION_OPTIONAL, null));
        registerHandler(FileProtocols.HTTP, new DefaultSchemeHandler(new DefaultSchemeParser(true), 80, "/", AuthenticationType.AUTHENTICATION_OPTIONAL, null));
        registerHandler(FileProtocols.S3, new DefaultSchemeHandler(new DefaultSchemeParser(true), 443, "/", AuthenticationType.AUTHENTICATION_REQUIRED, null));
        registerHandler(FileProtocols.WEBDAV, new DefaultSchemeHandler(new DefaultSchemeParser(true), 80, "/", AuthenticationType.AUTHENTICATION_REQUIRED, null));
        registerHandler(FileProtocols.HTTPS, new DefaultSchemeHandler(new DefaultSchemeParser(true), 443, "/", AuthenticationType.AUTHENTICATION_OPTIONAL, null));
        registerHandler(FileProtocols.WEBDAVS, new DefaultSchemeHandler(new DefaultSchemeParser(true), 443, "/", AuthenticationType.AUTHENTICATION_REQUIRED, null));
        registerHandler(FileProtocols.NFS, new DefaultSchemeHandler(new DefaultSchemeParser(), 2049, "/", AuthenticationType.NO_AUTHENTICATION, null));
        registerHandler(FileProtocols.VSPHERE, new DefaultSchemeHandler(new DefaultSchemeParser(true), 443, "/", AuthenticationType.AUTHENTICATION_REQUIRED, null));

        registerHandler(FileProtocols.SMB, new DefaultSchemeHandler(new DefaultSchemeParser(), -1, "/", AuthenticationType.AUTHENTICATION_REQUIRED, new Credentials("GUEST", "")) {
            @Override
            public FileURL getRealm(FileURL location) {
                FileURL realm = new FileURL(this);

                String newPath = location.getPath();
                // Find first path token (share)
                int pos = newPath.indexOf('/', 1);
                newPath = newPath.substring(0, pos==-1?newPath.length():pos+1);

                realm.setPath(newPath);
                realm.setScheme(location.getScheme());
                realm.setHost(location.getHost());
                realm.setPort(location.getPort());

                // Copy properties (if any)
                realm.importProperties(location);

                return realm;
            }
        });
    }


    /**
     * Private constructor. Creates an empty FileURL that uses the given handler, all parts have to be manually set.
     *
     * @param handler the handler to have this FileURL use
     */
    private FileURL(SchemeHandler handler) {
        this.handler = handler;
    }

    /**
     * This method is called whenever this instance is modified to invalidate caches.
     */
    private void urlModified() {
        hashCode = 0;
    }

    /**
     * Creates and returns a new FileURL instance from the given location, throws a <code>MalformedURLException</code>
     * if the specified location is not a valid URL or path and cannot be resolved. The {@link SchemeParser parser}
     * of the {@link SchemeHandler handler} registered for the location's scheme is used to parse the given location.
     * If the scheme specified in the location does not have a specific handler, or if the location does not contain a
     * scheme (i.e. is local or UNC path, not a URL) then the default handler's parser is used.
     *
     * @param location the URL or path for which to get a <code>FileURL</code> instance
     * @throws MalformedURLException if the specified string isn't a valid URL, according to the scheme's parser used
     * @return a FileURL corresponding to the given location
     */
    public static FileURL getFileURL(String location) throws MalformedURLException {
        int schemeDelimPos = location.indexOf("://");
        SchemeHandler handler;

        if(schemeDelimPos==-1) {
            // No scheme: the location is a local or UNC path, not a URL
            handler = getDefaultHandler();
        }
        else {
            handler = getSchemeHandler(location.substring(0, schemeDelimPos));
        }

        FileURL fileURL = new FileURL(handler);
        try {
            handler.getParser().parse(location, fileURL);
        }
        catch(Exception e) {
            // Catch any unexpected exception thrown by the SchemeParser and turn it into a MalformedURLException
            // with a specific error message.
            if(e instanceof MalformedURLException)
                throw (MalformedURLException)e;

            throw new MalformedURLException("URL parser error");
        }

        return fileURL;
    }

    /**
     * Returns the handler registered the specified scheme if there is one, the default handler otherwise.
     *
     * @param scheme the scheme for which to return a handler
     * @return a handler for the specified scheme
     */
    private static SchemeHandler getSchemeHandler(String scheme) {
        SchemeHandler handler = getRegisteredHandler(scheme);
        if(handler==null)
            return getDefaultHandler();

        return handler;
    }

    /**
     * Returns the <code>SchemeHandler</code> instance that provides the scheme-specific features of this FileURL.
     *
     * @return the <code>SchemeHandler</code> instance that provides the scheme-specific features of this FileURL
     */
    public SchemeHandler getHandler() {
        return handler;
    }

    /**
     * Sets the <code>SchemeHandler</code> that provides the scheme-specific features of this FileURL.
     * <p>
     * <b>Important:</b> after calling this method, the scheme should also be changed to match the new handler --
     * changing the handler without changing the scheme to an appropriate one will result in inconsistent
     * scheme-specific attributes to be returned.
     * </p>
     *
     * @param handler the <code>SchemeHandler</code> instance that provides the scheme-specific features of this FileURL
     */
    public void setHandler(SchemeHandler handler) {
        this.handler = handler;
    }

    /**
     * Registers a handler for the specified scheme, replacing any handler previously registered
     * for the same scheme.
     *
     * @param scheme the scheme to associate the handler with (case-insensitive)
     * @param handler the new handler in charge of the scheme
     */
    public static void registerHandler(String scheme, SchemeHandler handler) {
        handlers.put(scheme.toLowerCase(), handler);
    }

    /**
     * Removes any handler associated with the specified scheme, leaving the default handler in charge of the scheme.
     * This method has no effect if there is no handler registered for the scheme.
     *
     * @param scheme the scheme to remove the handler for
     */
    public static void unregisterHandler(String scheme) {
        handlers.remove(scheme.toLowerCase());
    }

    /**
     * Returns the handler registered for the specified scheme, <code>null</code> if there isn't any.
     *
     * @param scheme the scheme for which to return the handler
     * @return the handler registered for the specified scheme
     */
    public static SchemeHandler getRegisteredHandler(String scheme) {
        return handlers.get(scheme.toLowerCase());
    }

    /**
     * Returns the default handler, which handles schemes which do not have a specific handler.
     * The returned instance is a {@link DefaultSchemeHandler} created with the no-arg constructor. 
     *
     * @return the default handler
     */
    public static SchemeHandler getDefaultHandler() {
        return DEFAULT_HANDLER;
    }

    /**
     * Extracts the filename from the given path and returns it, or <code>null</null> if the path does not contain
     * a filename.
     *
     * @param path the path from which to extract a filename
     * @param separator the path separator
     * @return the filename extracted from the given path, <code>null</code> if the path doesn't contain any
     */
    public static String getFilenameFromPath(String path, String separator) {
        if(path.equals("") || path.equals("/"))
            return null;

        // Remove any trailing separator
        path = PathUtils.removeTrailingSeparator(path, separator);

        if(!separator.equals("/"))
            path = PathUtils.removeLeadingSeparator(path, "/");

        // Extract filename
        int pos = path.lastIndexOf(separator);
        if(pos==-1)
            return null;

        return path.substring(pos+1);
    }


    /**
     * Returns the scheme part of this URL. The returned scheme may never be <code>null</code>.
     *
     * @return the scheme part of this <code>FileURL</code>.
     * @see #setScheme(String)
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the scheme part of this URL. An <code>IllegalArgumentException</code> will be thrown if the specified scheme
     * is <code>null</code> or an empty string.
     * <p>
     * <b>Important:</b> after calling this method, the handler should also be changed to match the new scheme --
     * changing the scheme without changing the handler to an appropriate one will result in inconsistent
     * scheme-specific attributes to be returned.
     * </p>
     *
     * @param scheme new scheme part of this URL.
     * @throws IllegalArgumentException if the specified is null or an empty string
     * @see #getScheme()
     */
    public void setScheme(String scheme) {
        if(scheme==null)
            throw new IllegalArgumentException();

        this.scheme = scheme;

        urlModified();
    }

    /**
     * Returns the host part of this URL, <code>null</code> if it doesn't contain any.
     *
     * @return the host part of this URL.
     * @see #setHost(String)
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host part of this URL, <code>null</code> for no host.
     *
     * @param host new host part of this URL.
     * @see #getHost()
     */
    public void setHost(String host) {
        this.host = host;

        urlModified();
    }

    /**
     * Returns the port part of this URL, <code>-1</code> if none was specified in the URL.
     *
     * @return the port part of this URL, -1 if there isn't any.
     * @see #setPort(int)
     * @see #getDefaultHandler()
     */
    public int getPort() {
        return port;
    }
	
    /**
     * Sets the port part of this URL, <code>-1</code> for no specific port.
     *
     * @param port new port part of this URL.
     * @see #getPort()
     * @see #getDefaultHandler()
     */
    public void setPort(int port) {
        this.port = port;

        urlModified();
    }

    /**
     * Returns this scheme's standard port, <code>-1</code> if the scheme doesn't have any.
     * If this URL doesn't have a specific port part, the return value should be considered as being this URL's port.
     *
     * <p>Some file protocols may not have a notion of standard port or even no use for the port part at all, for
     * example those that are not TCP or UDP based such as the local 'file' scheme.</p>
     *
     * <p>This method is just a shorthand for <code>getHandler().getStandardPort()</code>.</p>
     *
     * @return the scheme's standard port
     * @see #getPort()
     */
    public int getStandardPort() {
        return handler.getStandardPort();
    }
    

    /**
     * Returns the login part of this URL, <code>null</code> if there isn't any.
     *
     * @return the login part of this URL, <code>null</code> if there isn't any
     * @see #getCredentials()
     */
    public String getLogin() {
        return credentials==null?null:credentials.getLogin();
    }

    /**
     * Returns the password part of this URL, <code>null</code> if there isn't any.
     *
     * @return the password part of this URL, <code>null</code> if there isn't any
     * @see #getCredentials()
     */
    public String getPassword() {
        return credentials==null?null:credentials.getPassword();
    }

    /**
     * Returns the type of authentication used by the scheme's file protocol. The returned value is one of the constants
     * defined in the {@link AuthenticationType} enum.
     *
     * <p>This method is just a shorthand for <code>getHandler().getAuthenticationType()</code>.</p>
     *
     * @return the type of authentication used by the scheme's file protocol
     */
    public AuthenticationType getAuthenticationType() {
        return handler.getAuthenticationType();
    }

    /**
     * Returns true if this URL contains credentials, i.e. a login and/or password part. If <code>true</code> is
     * returned, {@link #getCredentials()} will return a non-null value.
     *
     * @return <code>true</code> if this URL contains credentials, <code>false</code> otherwise.
     */
    public boolean containsCredentials() {
        return credentials!=null;
    }

    /**
     * Returns the credentials (login and password) contained by this URL, wrapped in an {@link Credentials} object.
     * Returns <code>null</code> if this URL doesn't have a login or password part.
     *
     * <p>The returned credentials may or may be of any use for the scheme's file protocol depending on the value
     * returned by {@link #getAuthenticationType()}.</p>
     *
     * @return the credentials contained by this URL, <code>null</code> if this URL doesn't have a login or password part.
     * @see #setCredentials(Credentials)
     * @see #getAuthenticationType()
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the login and password parts of this URL. Any credentials contained by this FileURL will be replaced.
     *  <code>null</code> can be passed to discard existing credentials.
     *
     * <p>Credentials may or may not be of any use for the scheme's file protocol depending on the value
     * returned by {@link #getAuthenticationType()}.</p>
     *
     * @param credentials the new login and password parts, replacing any existing credentials. If null is passed,
     * existing credentials will be discarded.
     * @see #getCredentials()
     */
    public void setCredentials(Credentials credentials) {
        if(credentials==null || credentials.isEmpty())  // Empty credentials are equivalent to null credentials
            this.credentials = null;
        else
            this.credentials = credentials;

        urlModified();
    }

    /**
     * Returns this scheme's guest credentials, <code>null</code> if the scheme doesn't have any.
     * <p>
     * Guest credentials offer a way to authenticate a URL as a 'guest' on file protocols that require a set of
     * credentials to establish a connection. The returned credentials are provided with no guarantee that the fileystem
     * will actually accept them and allow the request/connection. The notion of 'guest' credentials may or may not
     * have a meaning depending on the underlying file protocol.
     * </p>
     *
     * <p>This method is just a shorthand for <code>getHandler().getGuestCredentials()</code>.</p>
     *
     * @return the scheme's guest credentials, <code>null</code> if the scheme doesn't have any
     */
    public Credentials getGuestCredentials() {
        return handler.getGuestCredentials();
    }


    /**
     * Returns the path part of this URL. The returned value will never be <code>null</code> and always start with a
     * leading '/' character.
     *
     * @return the path part of this URL.
     * @see    #setPath(String)
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path part of this URL. The specified path cannot be <code>null</code> and must start with a leading 
     * '/' character. If the specified path value is <code>null</code>, then the path will be set to "/".
     * If the path does not start with a leading separator, one will be added.
     *
     * @param path new path part of this URL
     * @see #getPath()
     */
    public void setPath(String path) {
        if(path==null || path.equals(""))
            path = "/";

        if(!path.startsWith("/"))
            path = "/"+path;

        this.path = path;
        // Extract new filename from path
        this.filename = getFilenameFromPath(path, getPathSeparator());

        urlModified();
    }

    /**
     * Returns this scheme's path separator, which serves as a delimiter for path fragments. For most schemes, this is
     * the forward slash character.
     *
     * <p>This method is just a shorthand for <code>getHandler().getPathSeparator()</code>.</p>
     *
     * @return this scheme's path separator
     */
    public String getPathSeparator() {
        return handler.getPathSeparator();
    }


    /**
     * Returns the parent of this URL according to its path, <code>null</code> if this URL has no parent (its path is "/").
     * <p>
     * The returned FileURL will have the same handler, scheme, host, port, credentials and properties as this one.
     * The query part of the returned parent URL will always be <code>null</code>, even if this URL had one.
     * </p>
     * <p>Note: this method returns a new FileURL instance every time it is called, and all mutable fields of this FileURL
     * are cloned. Therefore, the returned URL can be safely modified without any risk of side effects.</p>
     *
     * @return this URL's parent, <code>null</code> if it doesn't have one.
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
                FileURL parentURL = new FileURL(handler);

                parentURL.scheme = scheme;
                parentURL.host = host;
                parentURL.port = port;
                parentURL.path = parentPath.substring(0, lastSeparatorPos+1);  // Keep trailing slash
                parentURL.filename = getFilenameFromPath(parentURL.path, separator);

                // Set same credentials for parent, (if any)
                // Note: Credentials are immutable.
                parentURL.credentials = credentials;

                // Copy properties to parent (if any)
                if(properties!=null)
                    parentURL.properties = new Hashtable<String, String>(properties);

                return parentURL;
            }
        }

        return null;    // URL has no parent
    }


    /**
     * Returns the authentication realm corresponding to this URL, i.e. the base location throughout which credentials
     * can be used. Any property contained by the specified FileURL will be carried over in the returned FileURL.
     * On the contrary, credentials will not be copied, the returned URL always has no credentials.
     *
     * <p>Note: this method returns a new FileURL instance every time it is called. Therefore the returned FileURL can
     * safely be modified without any risk of side effects.</p>

     * <p>This method is just a shorthand for <code>getHandler().getRealm(this)</code>.</p>
     *
     * @return this url's authentication realm
     */
    public FileURL getRealm() {
        return handler.getRealm(this);
    }


    /**
     * Returns the filename of this URL , <code>null</code> if doesn't have one (e.g. if the path is "/").
     * <p>
     * There is no <code>setFilename</code> as the filename is simply extrapolated from the path.
     * Use {@link #setPath(String)} to change the path and its filename.
     * </p>
     *
     * @return the filename of this URL, <code>null</code> if it doesn't have one.
     * @see    #setPath(String)
     */
    public String getFilename() {
        return filename;
    }


    /**
     * Returns the query part of this URL if it has one, <code>null</code> otherwise.
     *
     * @return the query part of this URL if it has one, <code>null</code> otherwise
     * @see    #setQuery(String)
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query part of this URL, <code>null</code> for no query part.
     *
     * @param query new query part of this URL, <code>null</code> for no query part
     * @see #getQuery()
     */
    public void setQuery(String query) {
        this.query = query;

        urlModified();
    }

	
    /**
     * Returns the value corresponding to the given property name, <code>null</code> if the property has no value.
     *
     * @param name name of the property whose value is to be retrieved
     * @return the value associated with the specified property name, <code>null</code> if it has no value
     * @see #setProperty(String,String)
     */
    public String getProperty(String name) {
        return properties==null?null:properties.get(name);
    }
	
    /**
     * Sets the given property (name/value pair) in the FileURL instance. A <code>null</code> property value has the
     * effect of removing the property.
     *
     * @param name name of the property to set
     * @param value value of the property
     * @see #getProperty(String)
     */
    public void setProperty(String name, String value) {
        // Create the property hashtable only when a property is set for the first time
        if(properties==null)
            properties = new Hashtable<String, String>();

        if(value==null)
            properties.remove(name);
        else
            properties.put(name, value);

        urlModified();
    }


    /**
     * Returns an <code>Enumeration</code> of all property names this FileURL contains.
     *
     * @return an <code>Enumeration</code> of all property names this FileURL contains
     */
    public Enumeration<String> getPropertyNames() {
        // Return an empty enumeration if the property hashtable is null
        if(properties==null) {
            return new Enumeration<String>() {
                public boolean hasMoreElements() {
                    return false;
                }

                public String nextElement() {
                    throw new NoSuchElementException();
                }
            };
        }

        return properties.keys();
    }

    /**
     * Copy the properties of the given FileURL into this FileURL.
     *
     * @param url FileURL instance whose properties should be imported into this one.
     */
    public void importProperties(FileURL url) {
        // Slight optimization to avoid creating an enumeration if the FileURL doesn't have any property
        if(url.properties==null)
            return;

        Enumeration<String> propertyKeys = url.getPropertyNames();
        String key;
        while(propertyKeys.hasMoreElements()) {
            key = propertyKeys.nextElement();
            setProperty(key, url.getProperty(key));
        }
    }

    /**
     * Returns a String representation of this FileURL, including the login and password parts (credentials) only if
     * specified, and masking the password as requested. 
     *
     * @param includeCredentials if <code>true</code>, the login and password parts (if any) will be included in the
     * returned URL.
     * @param maskPassword if <code>true</code> and the includeCredentials parameter is also true, the password's
     * characters (if any) will be replaced by '*' characters. This allows a URL containing credentials to be displayed
     * to the end user without revealing the actual password.
     * @return a string representation of this <code>FileURL</code>
     */
    public String toString(boolean includeCredentials, boolean maskPassword) {
        StringBuffer sb = new StringBuffer(scheme);
        sb.append("://");

        if(includeCredentials && credentials!=null) {
            try {
                sb.append(URLEncoder.encode(credentials.getLogin(), "UTF-8"));
            }
            catch(UnsupportedEncodingException e) {
                // This can't happen in practice, UTF-8 is necessarily supported
            }

            String password = credentials.getPassword();
            if(!"".equals(password)) {
                sb.append(':');
                if(maskPassword)
                    sb.append(credentials.getMaskedPassword());
                else {
                    try {
                        sb.append(URLEncoder.encode(password, "UTF-8"));
                    }
                    catch(UnsupportedEncodingException e) {
                        // This can't happen in practice, UTF-8 is necessarily supported
                    }
                }
            }
            sb.append('@');
        }

        if(host!=null)
            sb.append(host);

        // Set the port only if it has a value that is different from the standard port
        if(port!=-1 && port!=handler.getStandardPort()) {
            sb.append(':');
            sb.append(port);
        }

        if(host!=null || !path.equals("/"))	{ // Test to avoid URLs like 'smb:///'
            if(path.startsWith("/")) {
                sb.append(path);
            }
            else {
                // Add a leading '/' if path doesn't already start with one, needed for scheme paths that are not
                // forward slash-separated
                sb.append('/');
                sb.append(path);
            }
        }

        if(query!=null) {
            sb.append('?');
            sb.append(query);
        }

        return sb.toString();
    }

    /**
     * Returns a String representation of this FileURL, including the login and password parts (credentials) only if
     * requested.
     *
     * @param includeCredentials if <code>true</code>, the login and password parts (if any) will be included in the
     * returned URL.
     * @return a string representation of this <code>FileURL</code>.
     */
    public String toString(boolean includeCredentials) {
        return toString(includeCredentials, false);
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
     * Returns <code>true</code> if the scheme part of this URL and the given URL are equal.
     * The comparison is case-sensitive.
     *
     * @param url the URL to test for scheme equality
     * @return <code>true</code> if the scheme part of this URL and the given URL are equal
     */
    public boolean schemeEquals(FileURL url) {
        return this.scheme.equalsIgnoreCase(url.scheme);
    }

    /**
     * Returns <code>true</code> if the host part of this URL and the given URL are equal.
     * The comparison is case-insensitive.
     *
     * @param url the URL to test for host equality
     * @return <code>true</code> if the host part of this URL and the given URL are equal
     */
    public boolean hostEquals(FileURL url) {
        // Note: StringUtils#equals is null-safe 
        return StringUtils.equals(this.host, url.host, false);
    }

    /**
     * Returns <code>true</code> if the port of this URL and the given URL's are equal. Ports are said to be equal if
     * the values returned by {@link #getPort()} are equal, or if both URLs have the same standard port
     * (as returned by {@link #getStandardPort()} and one of the port value is <code>-1</code> (undefined) and the other
     * is the standard port.
     *
     * @param url the URL to test for port equality
     * @return <code>true</code> if the port of this URL and the given one are equal
     */
    public boolean portEquals(FileURL url) {
        int port1 = this.port;
        int port2 = url.port;
        int standardPort = getStandardPort();

        return port1==port2 ||
            (standardPort==url.getStandardPort() && ((port1==-1 && port2==standardPort || (port2==-1 && port1==standardPort))));
    }

    /**
     * Returns <code>true</code> if the path of this URL and the given URL are equal. The comparison is case-sensitive.
     * If the sole difference between two paths is a trailing path separator (and both URLs have the same path separator),
     * they will be considered as equal.
     * For example, <code>/path</code> and <code>/path/</code> are considered equal, assuming the path separator is '/'.
     *
     * <p>It is noteworthy that this method uses <code>java.lang.String#equals(Object)</code> to compare URL paths,
     * which in some rare cases may return <code>false</code> for non-ascii/Unicode paths that have the same written
     * representation but are not equal according to <code>java.lang.String#equals(Object)</code>. Handling such cases
     * would require a locale-aware String comparison which is not an option here.</p>
     *
     * @param url the URL to test for path equality
     * @return <code>true</code> if the path of this URL and the given URL are equal
     */
    public boolean pathEquals(FileURL url) {
    	boolean isCaseSensitiveOS = !(OsFamily.getCurrent().equals(OsFamily.WINDOWS) || OsFamily.getCurrent().equals(OsFamily.OS_2));
    	
        String path1 = isCaseSensitiveOS ? this.getPath() : this.getPath().toLowerCase();
        String path2 = isCaseSensitiveOS ? url.getPath() : url.getPath().toLowerCase();

        if(path1.equals(path2))
            return true;

        String separator = getPathSeparator();

        if(separator.equals(url.getPathSeparator())) {
            int separatorLen = separator.length();
            int len1 = path1.length();
            int len2 = path2.length();

            // If the difference between the 2 strings is just a trailing path separator, we consider the paths as equal
            if(Math.abs(len1-len2)==separatorLen && (len1>len2 ? path1.startsWith(path2) : path2.startsWith(path1))) {
                String diff = len1>len2 ? path1.substring(len1-separatorLen) : path2.substring(len2-separatorLen);
                return separator.equals(diff);
            }
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the query part of this URL and the given URL are equal.
     * The comparison is case-sensitive.
     *
     * @param url the URL to test for query equality
     * @return <code>true</code> if the query part of this URL and the given URL are equal
     */
    public boolean queryEquals(FileURL url) {
        return StringUtils.equals(this.query, url.query, true);
    }

    /**
     * Returns <code>true</code> if the credentials (login and password) of this URL and the given URL are equal.
     * The comparison is case-sensitive.
     *
     * @param url the URL to test for credentials equality
     * @return <code>true</code> if the credentials of this URL and the given URL are equal
     */
    public boolean credentialsEquals(FileURL url) {
        Credentials creds1 = this.credentials;
        Credentials creds2 = url.credentials;

        return (creds1==null && creds2==null)
            || (creds1!=null && creds1.equals(creds2, true))
            || (creds2!=null && creds2.equals(creds1, true));
    }

    /**
     * Returns <code>true</code> if the properties contained by this URL and the given URL are equal.
     * The comparison of each property is case-sensitive.
     *
     * @param url the URL to test for properties equality
     * @return <code>true</code> if the properties contained by this URL and the given URL are equal
     */
    public boolean propertiesEquals(FileURL url) {
        return (this.properties==null && url.properties==null)
           ||  (this.properties!=null && this.properties.equals(url.properties))
           ||  (url.properties!=null && url.properties.equals(this.properties));
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns a String representation of this FileURL, without including the login and password parts it may have.
     */
    public String toString() {
        return toString(false);
    }


    /**
     * Returns a clone of this FileURL. The returned instance can safely be modified without any impact on this FileURL
     * or any previously cloned URL.
     */
    @Override
    public Object clone() {
        // Create a new FileURL return it, instead of using Object.clone() which is probably way slower;
        // most FileURL fields are immutable and as such reused in cloned instance
        FileURL clonedURL = new FileURL(handler);

        // Immutable fields
        clonedURL.scheme = scheme;
        clonedURL.host = host;
        clonedURL.port = port;
        clonedURL.path = path;
        clonedURL.filename = filename;
        clonedURL.query = query;
        clonedURL.credentials = credentials;  // Note: Credentials are immutable.

        // Mutable fields
        if(properties!=null)    // Copy properties (if any)
            clonedURL.properties = new Hashtable<String, String>(properties);

        // Caches
        clonedURL.hashCode = hashCode;

        return clonedURL;
    }

    /**
     * This method is equivalent to calling {@link #equals(Object, boolean, boolean)} with credentials and properties
     * comparisons enabled.
     *
     * @param o object to compare against this FileURL instance.
     * @return true if both FileURL instances are equal.
     */
    public boolean equals(Object o) {
        return equals(o, true, true);
    }

    /**
     * Tests the specified FileURL for equality with this FileURL. <code>false</code> is systematically returned if the
     * specified object is not a FileURL instance or is <code>null</code>.
     * <p>
     * Two <code>FileURL</code> instances are said to be equal if:
     * <ul>
     *  <li>schemes are equal (case-insensitive)</li>
     *  <li>hosts are equal (case-insensitive)</li>
     *  <li>ports are equal. The default port is taken into account when comparing ports: a non specified port part (-1)
     * is equivalent to the scheme's standard port. For instance, <code>http://mucommander.com:80/</code>
     * and <code>http://mucommander.com/</code> are considered equal.</li>
     *  <li>paths are equal (case-sensitive). There can be a trailing separator difference in the two paths, they will
     * still be considered as equal. For example, <code>/path</code> and <code>/path/</code> are considered equal
     * (assuming the path separator is '/').</li>
     *  <li>queries are equal (case-sensitive)</li>
     * </ul>
     * </p>
     * <p>
     * Credentials (login and password parts) are compared only if requested. The comparison for both the login and
     * password is case-sensitive.</br>
     * Likewise, properties are compared only if requested: the comparison of all properties is case-sensitive.
     * </p>
     *
     * @param o object to compare against this FileURL instance
     * @param compareCredentials if <code>true</code>, the login and password parts of both FileURL need to be
     * equal (case-sensitive) for the FileURL instances to be equal
     * @param compareProperties if <code>true</code>, all properties need to be equal (case-sensitive) in both
     * FileURL for them to be equal
     * @return true if both FileURL instances are equal
     */
    public boolean equals(Object o, boolean compareCredentials, boolean compareProperties) {
        if(o==null || !(o instanceof FileURL))
            return false;

        FileURL url = (FileURL)o;

        return pathEquals(url)      // Compare the path first as it is the most likely to be different
            && schemeEquals(url)
            && hostEquals(url)
            && portEquals(url)
            && queryEquals(url)
            && (!compareCredentials || credentialsEquals(url))
            && (!compareProperties || propertiesEquals(url));
    }

    /**
     * This method is overridden to return a hash code that takes into account the behavior of {@link FileURL#equals(Object)},
     * so that <code>url1.equals(url2)</code> implies <code>url1.hashCode()==url2.hashCode()</code>.
     */
    public int hashCode() {
        if(hashCode==0) {
            String separator = handler.getPathSeparator();

            // #equals(Object) is trailing separator insensitive, so the hashCode must be trailing separator invariant
            int h = PathUtils.getPathHashCode(path, separator);

            h = 31* h + scheme.toLowerCase().hashCode();
            h = 31* h + (port==-1?handler.getStandardPort():port);

            if(host!=null)
                h = 31* h + host.toLowerCase().hashCode();

            if(query!=null)
                h = 31* h + query.hashCode();

            if(credentials!=null)
                h = 31* h + credentials.hashCode();

            if(properties!=null)
                h = 31* h + properties.hashCode();

            // Cache the value until for as long as this instance is not modified
            hashCode = h;
        }

        return hashCode;
    }
}
