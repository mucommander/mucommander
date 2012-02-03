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

/**
 * This class provides a default {@link SchemeHandler} implementation. The no-arg constructor creates an
 * instance with default values that suit most schemes. This is how the default URL handler returned by
 * {@link FileURL#getDefaultHandler()} is created.</br>
 * The multi-arg constructor allows to create a scheme handler with specific values.
 * <p>
 * The {@link #getRealm(FileURL)} implementation returns a URL with the same scheme and host (if any) as the specified
 * URL, and a path set to <code>"/"</code>. This behavior can be modified by overriding <code>getRealm</code>.
 * </p>
 *
 * @see com.mucommander.commons.file.FileURL#getDefaultHandler()
 * @see com.mucommander.commons.file.SchemeHandler
 * @author Maxence Bernard
 */
public class DefaultSchemeHandler implements SchemeHandler {

    protected SchemeParser parser;
    protected int standardPort;
    protected String pathSeparator;
    protected AuthenticationType authenticationType;
    protected Credentials guestCredentials;

    /**
     * Creates a DefaultSchemeHandler with default values that suit schemes in which the scheme name is not included
	 * in the URL (local and unc locations):
     * <ul>
     *  <li>the parser is a DefaultSchemeParser instance created with the no-arg constructor</li>
     *  <li>the scheme's standard port is <code>-1</code></li>
     *  <li>the scheme's path separator is operating system's path separator</li>
     *  <li>authentication type is {@link AuthenticationType#NO_AUTHENTICATION}</li>
     *  <li>guest credentials are <code>null</code></li>
     * </ul>
     */
    public DefaultSchemeHandler() {
        this(new DefaultSchemeParser(), -1, System.getProperty("file.separator"), AuthenticationType.NO_AUTHENTICATION, null);
    }

    /**
     * Creates a DefaultSchemeHandler with the specified values.
     *
     * @param parser the parser that takes care of parsing URL strings and turning them into FileURL
     * @param standardPort the scheme's standard port, <code>-1</code> for none
     * @param pathSeparator the scheme's path separator, cannot be <code>null</code>
     * @param authenticationType the type of authentication used by the scheme's file protocol
     * @param guestCredentials the scheme's guest credentials, <code>null</code> for none
     */
    public DefaultSchemeHandler(SchemeParser parser, int standardPort, String pathSeparator, AuthenticationType authenticationType, Credentials guestCredentials) {
        this.parser = parser;
        this.standardPort = standardPort;
        this.pathSeparator = pathSeparator;
        this.authenticationType = authenticationType;
        this.guestCredentials = guestCredentials;
    }


    //////////////////////////////////
    // SchemeHandler implementation //
    //////////////////////////////////

    /**
     * Returns the parser that was passed to the constructor.
     *
     * @return the parser that was passed to the constructor
     */
    public SchemeParser getParser() {
        return parser;
    }

    /**
     * Returns the authentication type that was passed to the constructor.
     *
     * @return the authentication type that was passed to the constructor
     */
    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    /**
     * Returns the set of guest credentials that was passed to the constructor.
     *
     * @return the set of guest credentials that was passed to the constructor
     */
    public Credentials getGuestCredentials() {
        return guestCredentials;
    }

    /**
     * Returns the path separator that was passed to the constructor.
     *
     * @return the path separator that was passed to the constructor
     */
    public String getPathSeparator() {
        return pathSeparator;
    }

    /**
     * Returns the standard port that was passed to the constructor.
     *
     * @return the standard port that was passed to the constructor
     */
    public int getStandardPort() {
        return standardPort;
    }

    /**
     * Returns a URL with the same scheme, host and port (if any) as the specified URL, and a path set to 
	 * <code>"/"</code> or <code>"\"</code> depending on the URL format.
     * The login, password, query and fragment parts of the returned URL are always <code>null</code>.
     * For example, when called with <code>http://www.mucommander.com:8080/path/to/file?query&param=value</code>,
     * this method returns <code>http://www.mucommander.com:8080/</code>.
     * 
     * @param location the location for which to return the authentication realm
     * @return the authentication realm of the specified location
     */
    public FileURL getRealm(FileURL location) {
        // Start by cloning the given URL and then modify the parts that need it
        FileURL realm = (FileURL)location.clone();

        realm.setPath(location.getPathSeparator());
        realm.setCredentials(null);
        realm.setQuery(null);
        // Todo
             // realm.setFragment(null)

        return realm;
    }
}
