/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

import com.mucommander.auth.AuthenticationTypes;
import com.mucommander.auth.Credentials;
import junit.framework.TestCase;

import java.net.MalformedURLException;

/**
 * A generic test case for {@link com.mucommander.file.FileURL}. This class is abstract and must be extended for
 * each URL scheme to be tested. Those tests should be completed by additional scheme-specific tests for
 * certain particularities that would not covered by this generic test case.  
 *
 * @see com.mucommander.file.FileURL
 * @author Maxence Bernard
 */
public abstract class FileURLTestCase extends TestCase {

    ////////////////////
    // Helper methods //
    ////////////////////

    /**
     * Creates and returns a FileURL instance using the specified partial URL, which is combined with the scheme
     * returned by {@link #getScheme()}. For instance, if <code>mucommander.com</code> is specified and
     * <code>http</code> is the scheme, a FileURL with <code>http://mucommander.com</code> as a string representation
     * will be returned.
     *
     * @param url a partial URL, minus the scheme and ://
     * @return a FileURL corresponding to the specified partial URL and the current scheme
     * @throws MalformedURLException if the FileURL could not be created because the specified URL is invalid
     */
    protected FileURL getSchemeURL(String url) throws MalformedURLException {
        return FileURL.getFileURL(getScheme()+"://"+url);
    }

    /**
     * Returns the simplest FileURL instance possible using the scheme returned by {@link #getScheme()}, containing
     * only the scheme and '/' as a path. For instance, if <code>http</code> is the current scheme, a FileURL with
     * <code>http:///</code> as a string representation will be returned.
     *
     * @return a 'root' FileURL instance with the scheme returned by {@link # getScheme ()}
     * @throws MalformedURLException should never happen
     */
    protected FileURL getRootURL() throws MalformedURLException {
        return getSchemeURL("");
    }

    /**
     * Attemps to parse the specified url using {@link FileURL#getFileURL(String)} and returns <code>true</code> if it
     * succeeded, <code>false</code> if it threw a <code>MalformedURLException</code>.
     *
     * @param url the URL to try and parse
     * @return <code>true</code> if the URL could be parsed, <code>false</code> if a <code>MalformedURLException</code> was thrown
     */
    protected boolean canParse(String url) {
        try {
            FileURL.getFileURL(url);
            return true;
        }
        catch(MalformedURLException e) {
            return false;
        }
    }


    //////////////////
    // Test methods //
    //////////////////

    /**
     * Ensures that the values returned by {@link FileURL#getStandardPort()} and {@link SchemeHandler#getStandardPort()}
     * match the expected one returned by {@link #getDefaultPort()}.
     *
     * @throws MalformedURLException should not happen
     */
    public void testDefaultPort() throws MalformedURLException {
        FileURL url = getRootURL();
        int expectedDefaultPort = getDefaultPort();

        // Assert that the default port value returned by the FileURL and its handler match the expected one
        // and are consistent
        assertEquals(expectedDefaultPort, url.getStandardPort());
        assertEquals(expectedDefaultPort, url.getHandler().getStandardPort());

        // Assert that the default port value is valid: either -1 or comprised between 1 and 65535
        assertTrue(expectedDefaultPort==-1||(expectedDefaultPort>0 && expectedDefaultPort<65536));
    }


    /**
     * Ensures that the values returned by {@link FileURL#getGuestCredentials()} and {@link SchemeHandler#getGuestCredentials()}
     * match the expected one returned by {@link #getGuestCredentials()}.
     *
     * @throws MalformedURLException should not happen
     */
    public void testGuestCredentials() throws MalformedURLException {
        FileURL url = getRootURL();
        Credentials expectedGuestCredentials = getGuestCredentials();

        // Assert that the guest credentials values returned by the FileURL and its handler match the expected one
        // and are consistent
        assertEquals(expectedGuestCredentials, url.getGuestCredentials());
        assertEquals(expectedGuestCredentials, url.getHandler().getGuestCredentials());
    }

    /**
     * Ensures that the values returned by {@link FileURL#getAuthenticationType()} ()} and {@link SchemeHandler#getAuthenticationType()}
     * match the expected value returned by {@link #getAuthenticationType()}, and that the value is one of the constants
     * defined in {@link AuthenticationTypes}.
     * If the authentication type is {@link AuthenticationTypes#NO_AUTHENTICATION}, verifies that
     * {@link #getGuestCredentials()} returns <code>null</code>.
     *
     * @throws MalformedURLException should not happen
     */
    public void testAuthenticationType() throws MalformedURLException {
        FileURL url = getRootURL();
        int expectedAuthenticationType = getAuthenticationType();

        assertEquals(expectedAuthenticationType, url.getAuthenticationType());
        assertEquals(expectedAuthenticationType, url.getHandler().getAuthenticationType());

        assertTrue(expectedAuthenticationType==AuthenticationTypes.NO_AUTHENTICATION
                || expectedAuthenticationType==AuthenticationTypes.AUTHENTICATION_REQUIRED
                || expectedAuthenticationType==AuthenticationTypes.AUTHENTICATION_OPTIONAL);

        if(expectedAuthenticationType==AuthenticationTypes.NO_AUTHENTICATION)
            assertNull(url.getGuestCredentials());
     }

    /**
     * Ensures that the values returned by {@link FileURL#getPathSeparator()} and {@link SchemeHandler#getPathSeparator()}
     * match the expected one returned by {@link #getPathSeparator()}.
     *
     * @throws MalformedURLException should not happen
     */
    public void testPathSeparator() throws MalformedURLException {
        FileURL url = getRootURL();
        String expectedPathSeparator = url.getPathSeparator();

        // Assert that the path separator values returned by the FileURL and its handler match the expected one
        // and are consistent
        assertEquals(expectedPathSeparator, url.getPathSeparator());
        assertEquals(expectedPathSeparator, url.getHandler().getPathSeparator());
    }


    /**
     * Tests {@link com.mucommander.file.FileURL#getRealm()} by ensuring that it returns the same URL only with the
     * path stripped out.
     * <p>
     * <b>Important:</b> this method must be overridden for protocols that have a specific realm notion (like SMB) or
     * else the test will fail.
     * </p>
     *
     *
     * @throws MalformedURLException should not happen
     */
    public void testRealm() throws MalformedURLException {
        assertEquals(getSchemeURL("host/"), getSchemeURL("host/path/to/file").getRealm());
    }


    /**
     * Ensures that the query part is parsed only if it should be, as specified by {@link #isQueryParsed()}.
     *
     * @throws MalformedURLException should not happen
     */
    public void testQueryParsing() throws MalformedURLException {
        FileURL url = getSchemeURL("host/path?query&param=value");
        String query = url.getQuery();

        if(isQueryParsed()) {
            assertEquals("?query&param=value", query);
        }
        else {
            assertNull(query);
        }
    }


    /**
     * Ensures that FileURL#getParent() works consistently according to the method's contract.
     * 
     * @throws MalformedURLException should not happen
     */
    public void testParent() throws MalformedURLException {
        FileURL url = getSchemeURL("login:password@host:10000/path/to?query&param=value");
        url.setProperty("key", "value");

        FileURL parentURL = url.getParent();

        // Test path and filename
        assertEquals("/path/", parentURL.getPath());
        assertEquals("path", parentURL.getFilename());

        // Assert that schemes, hosts and ports match
        assertEquals(url.getScheme(), parentURL.getScheme());
        assertEquals(url.getHost(), parentURL.getHost());
        assertEquals(url.getPort(), parentURL.getPort());

        // Assert that credentials match
        assertEquals(url.getCredentials(), parentURL.getCredentials());
        assertEquals(url.getLogin(), parentURL.getLogin());
        assertEquals(url.getPassword(), parentURL.getPassword());

        // Assert that the sample property is in the parent URL
        assertEquals("value", parentURL.getProperty("key"));

        // Assert that handlers match
        assertEquals(url.getHandler(), parentURL.getHandler());

        // Assert that the query part is null
        assertNull(parentURL.getQuery());

        // One more time, the parent path is now "/"

        url = parentURL;
        parentURL = url.getParent();

        // Test path and filename
        assertEquals("/", parentURL.getPath());
        assertNull(parentURL.getFilename());

        // The parent URL should now be null   

        // Test path and filename
        url = parentURL;
        assertNull(url.getParent());
    }


    /**
     * Tests the {@link SchemeParser}'s path canonization which is required to factoring out
     * '.' and '..' path fragments, and if the value returned by {@link #getTildeReplacement()} is not <code>null</code>,
     * to replace '~' path fragments by the said value.
     *
     * @throws MalformedURLException should not happen
     * @see #getTildeReplacement()
     */
    public void testCanonization() throws MalformedURLException {
        // Test '.' and '..' factorization
        assertEquals("/", getSchemeURL("host/1/.././1/./2/./.././../").getPath());

        // Test '~' canonization (or the lack thereof)
        String tildeReplacement = getTildeReplacement();
        if(tildeReplacement!=null) {
            assertEquals(tildeReplacement, getSchemeURL("host/~").getPath());
        }
        else {
            assertEquals("/~", getSchemeURL("host/~").getPath());
        }

        // The following URL should fail to parse
        boolean exceptionThrown = false;
        try {
            assertEquals("/", getSchemeURL("host/../..").getPath());
        }
        catch(MalformedURLException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }


    /**
     * Parses some borderline but valid URLs and ensures that they parse and the getters return the proper part values.
     *
     * @throws MalformedURLException should not happen
     */
    public void testParsing() throws MalformedURLException {
        String scheme = getScheme();

        // Test a sample URL with all parts used
        FileURL url = getSchemeURL("login:password@host:10000/path/to?query");
        assertEquals(scheme, url.getScheme());
        assertEquals("login", url.getLogin());
        assertEquals("password", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(10000, url.getPort());
        assertEquals(isQueryParsed()?"/path/to":"/path/to?query", url.getPath());
        assertEquals(isQueryParsed()?"to":"to?query", url.getFilename());
        assertEquals(isQueryParsed()?"?query":null, url.getQuery());

        // Ensure that login and password parts can contain '@' characters without disrupting the parsing
        url = getSchemeURL("login@domain.com:password@domain.com@host:10000/path/to?query");
        assertEquals(scheme, url.getScheme());
        assertEquals("login@domain.com", url.getLogin());
        assertEquals("password@domain.com", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(10000, url.getPort());
        assertEquals(isQueryParsed()?"/path/to":"/path/to?query", url.getPath());
        assertEquals(isQueryParsed()?"to":"to?query", url.getFilename());
        assertEquals(isQueryParsed()?"?query":null, url.getQuery());

        // Ensure that paths can contain '@' characters without disrupting the parsing
        url = getSchemeURL("login:password@host:10000/path@at/to@at?query");
        assertEquals(scheme, url.getScheme());
        assertEquals("login", url.getLogin());
        assertEquals("password", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(10000, url.getPort());
        assertEquals(isQueryParsed()?"/path@at/to@at":"/path@at/to@at?query", url.getPath());
        assertEquals(isQueryParsed()?"to@at":"to@at?query", url.getFilename());
        assertEquals(isQueryParsed()?"?query":null, url.getQuery());

        // Ensure that empty port parts are tolerated
        url = getSchemeURL("login:password@host:/path@at/to@at?query");
        assertEquals(scheme, url.getScheme());
        assertEquals("login", url.getLogin());
        assertEquals("password", url.getPassword());
        assertEquals("host", url.getHost());
        assertEquals(-1, url.getPort());
        assertEquals(isQueryParsed()?"/path@at/to@at":"/path@at/to@at?query", url.getPath());
        assertEquals(isQueryParsed()?"to@at":"to@at?query", url.getFilename());
        assertEquals(isQueryParsed()?"?query":null, url.getQuery());
    }


    /**
     * Tests FileURL's getters and setters.
     *
     * @throws MalformedURLException should not happen
     */
    public void testAccessors() throws MalformedURLException {
        FileURL url = FileURL.getFileURL("scheme://");

        String scheme = getScheme();
        Credentials credentials = new Credentials("login", "password");
        String host = "host";
        int port = 10000;
        String path = "/path/to";
        String query = "?query";

        url.setScheme(scheme);
        url.setCredentials(credentials);
        url.setHost(host);
        url.setPort(port);
        url.setPath(path);
        url.setQuery(query);
        url.setProperty("name", "value");

        assertEquals(scheme, url.getScheme());
        assertTrue(credentials.equals(url.getCredentials(), true));
        assertEquals(host, url.getHost());
        assertEquals(port, url.getPort());
        assertEquals(path, url.getPath());
        assertEquals(query, url.getQuery());
        assertEquals("to", url.getFilename());
        assertEquals("value", url.getProperty("name"));
        assertEquals(scheme+"://login:password@host:10000/path/to?query", url.toString(true, false));

        // Test null values

        url.setCredentials(null);
        url.setHost(null);
        url.setPort(-1);
        url.setPath("/");
        url.setQuery(null);
        url.setProperty("name", null);

        assertEquals(scheme, url.getScheme());
        assertNull(url.getCredentials());
        assertFalse(url.containsCredentials());
        assertNull(url.getHost());
        assertEquals(-1, url.getPort());
        assertEquals("/", url.getPath());
        assertNull(url.getQuery());
        assertNull(url.getFilename());
        assertNull(url.getProperty("name"));
        assertEquals(scheme+"://", url.toString(true, false));

        // Path cannot be null, the path is supposed to be "/" if a null or empty value is specified
        url.setPath(null);
        assertEquals("/", url.getPath());
        url.setPath("");
        assertEquals("/", url.getPath());

        // Path must always start with a leading '/', if the specified does not then a '/' is automatically added
        url.setPath("path/to");
        assertEquals("/path/to", url.getPath());
    }


    /**
     * Tests FileURL's <code>toString</code> methods.
     *
     * @throws MalformedURLException should not happen
     */
    public void testStringRepresentation() throws MalformedURLException {
        FileURL url = getSchemeURL("login:password@host:10000/path/to?query");
        String urlString = getScheme()+"://host:10000/path/to?query";

        assertEquals(urlString, url.toString());
        assertEquals(urlString, url.toString(false));
        assertEquals(urlString, url.toString(false, false));

        urlString = getScheme()+"://login:password@host:10000/path/to?query";
        assertEquals(urlString, url.toString(true));
        assertEquals(urlString, url.toString(true, false));

        urlString = getScheme()+"://login:********@host:10000/path/to?query";
        assertEquals(urlString, url.toString(true, true));
    }


    /**
     * Tests <code>equals</code> methods.
     *
     * @throws MalformedURLException should not happen
     */
    public void testEquals() throws MalformedURLException {
        FileURL url1 = getSchemeURL("login:password@host:10000/path/to?query&param=value");
        url1.setProperty("name", "value");
        FileURL url2 = getSchemeURL("login:password@host:10000/path/to?query&param=value");
        url2.setProperty("name", "value");

        // Assert that both URLs are equal
        assertTrue(url1.equals(url2));
        assertTrue(url2.equals(url1));
        assertTrue(url1.equals(url2, true, true));
        assertTrue(url2.equals(url1, true, true));

        // Add a trailing path separator to one of the URL's path and assert they are still equal
        url1.setPath(url1.getPath()+url1.getPathSeparator());
        assertTrue(url1.equals(url2));
        assertTrue(url1.equals(url2, true, true));

        // Assert that having the port part set to the standart port is equivalent to not having a port part (-1)
        url1.setPort(url1.getStandardPort());
        url2.setPort(-1);
        assertTrue(url1.equals(url2));
        assertTrue(url1.equals(url2, true, true));

        // Assert that the scheme comparison is case-insensitive
        url1.setScheme(url1.getScheme().toUpperCase());
        assertTrue(url1.equals(url2));
        assertTrue(url1.equals(url2, true, true));

        // Assert that the host comparison is case-insensitive
        url1.setHost(url1.getHost().toUpperCase());
        assertTrue(url1.equals(url2));
        assertTrue(url1.equals(url2, true, true));

        // Assert that the path comparison is case-sensitive
        url1.setPath(url1.getPath().toUpperCase());
        assertFalse(url1.equals(url2));
        assertFalse(url1.equals(url2, true, true));

        // Make both URLs equal again
        url1.setPath(url2.getPath());
        assertTrue(url1.equals(url2, true, true));

        // Assert that the query comparison is case-sensitive
        url1.setQuery("?query");
        url2.setQuery("?QUERY");
        assertFalse(url1.equals(url2));
        assertFalse(url1.equals(url2, true, true));

        // Make both URLs equal again
        url1.setQuery(url2.getQuery());
        assertTrue(url1.equals(url2, true, true));

        // Assert that the credentials comparison is case-sensitive
        url1.setCredentials(new Credentials("LOGIN", "password"));
        assertTrue(url1.equals(url2));
        assertFalse(url1.equals(url2, true, true));
        url1.setCredentials(new Credentials("login", "PASSWORD"));
        assertTrue(url1.equals(url2));
        assertFalse(url1.equals(url2, true, true));

        // Assert that URLs are equal if credentials comparison is disabled
        assertTrue(url1.equals(url2, false, true));

        // Make both URLs equal again
        url1.setCredentials(new Credentials("login", "password"));
        assertTrue(url1.equals(url2, true, true));

        // Assert that the properties comparison is case-sensitive
        url1.setProperty("name", null);
        url1.setProperty("NAME", "value");
        assertTrue(url1.equals(url2));
        assertFalse(url1.equals(url2, true, true));
        url1.setProperty("name", "VALUE");
        assertTrue(url1.equals(url2));
        assertFalse(url1.equals(url2, true, true));

        // Assert that URLs are equal if properties comparison is disabled
        assertTrue(url1.equals(url2, true, false));

        // Make both URLs equal again
        url1.setProperty("NAME", null);
        url1.setProperty("name", "value");
        assertTrue(url1.equals(url2, true, true));

        // Assert that the properties comparison fails if an extra property is added to one of the URLs
        url1.setProperty("name2", "value2");
        assertFalse(url1.equals(url2, true, true));

        // Assert that URLs are equal if properties comparison is disabled
        assertTrue(url1.equals(url2, true, false));

        // Make both URLs equal again
        url1.setProperty("name2", null);
        assertTrue(url1.equals(url2, true, true));
    }


    /**
     * Tests {@link FileURL#clone()}.
     *
     * @throws MalformedURLException should not happen
     */
    public void testClone() throws MalformedURLException {
        FileURL url = getSchemeURL("login:password@host:10000/path/to?query");
        url.setProperty("name", "value");

        FileURL clonedURL = (FileURL)url.clone();

        // Assert that both instances are equal according to FileURL#equals
        assertEquals(url, clonedURL);

        // Assert that both URL's string representations (with credentials) are equal
        assertEquals(url.toString(true), clonedURL.toString(true));

        // Assert that both instances are not one and the same
        assertFalse(url==clonedURL);

        // Assert that the property has survived the cloning
        assertEquals("value", clonedURL.getProperty("name"));
    }

    /**
     * Tests a few invalid URLs and makes sure {@link FileURL#getFileURL} throws a <code>MalformedURLException</code>.
     *
     * @throws MalformedURLException should not happen
     */
    public void testInvalidURLs() throws MalformedURLException {
        // relative URLs
        assertFalse(canParse("relative"));
        assertFalse(canParse("C:"));
        assertFalse(canParse("scheme:/"));

        // Invalid port (non-numeric)
        assertFalse(canParse(getScheme()+"://host:port/path"));
    }
    

    //////////////////////
    // Abstract methods //
    //////////////////////

    protected abstract String getScheme();

    protected abstract int getDefaultPort();

    protected abstract int getAuthenticationType();

    protected abstract Credentials getGuestCredentials();

    protected abstract String getPathSeparator();
    
    protected abstract String getTildeReplacement();

    protected abstract boolean isQueryParsed();
}
