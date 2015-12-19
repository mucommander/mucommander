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
 * <code>SchemeHandler</code> is an interface that allows {@link FileURL} to be specialized for a particular scheme.
 * It provides a number of scheme-specific features:
 * <dl>
 *  <dt>{@link #getStandardPort() standard port}</dt><dd>the standard port implied when no port is defined in the URL,
 * e.g. 21 for FTP</dd>
 *  <dt>{@link #getPathSeparator() path separator}</dt><dd>the character(s) that separates path fragments, e.g. '/' for
 * most schemes, '\' for local paths under certain OSes like Windows.</dd>
 *  <dt>{@link #getGuestCredentials() guest credentials}</dt><dd>credentials to authenticate as a guest, e.g. 'GUEST'
 * for SMB, 'anonymous' for FTP.</dd>
 *  <dt>{@link #getRealm(FileURL) authentication realm}</dt><dd>the base URL throughout which a set of credentials can
 * be used.</dd>
 * </dl>
 * <p>
 * In addition to providing those attributes, a SchemeHandler provides a {@link SchemeParser} instance which takes care
 * of the actual parsing of URLs of a particular scheme when {@link FileURL#getFileURL(String)} is invoked. This allows
 * for scheme-specific parsing, like for example for the query part which should only be parsed and considered as a
 * separate part for certain schemes such as HTTP.
 * </p>
 *
 * <h3>Handler registration</h3>
 * <p>
 * <code>FileURL</code> registers a number of handlers for the schemes/protocols supported by the muCommander file API.
 * Additional handlers can be registered dynamically using {@link FileURL#registerHandler(String, SchemeHandler)}.
 * Likewise, existing handlers can be unregistered or replaced at runtime using
 * {@link FileURL#registerHandler(String, SchemeHandler)} and {@link FileURL#unregisterHandler(String)}.</br>
 * </br>
 * <code>FileURL</code> uses a default handler for schemes that do not have a specific handler registered.
 * </p>
 *
 * @see DefaultSchemeHandler
 * @see com.mucommander.commons.file.FileURL#registerHandler(String, SchemeHandler)
 * @see SchemeParser
 * @author Maxence Bernard
 */
public interface SchemeHandler {

    /**
     * Returns the <code>SchemeParser</code> that turns URL strings of a particular scheme into {@link FileURL} objects.
     *
     * @return the <code>SchemeParser</code> that turns URL strings of a particular scheme into {@link FileURL} objects
     */
    public SchemeParser getParser();

    /**
     * Returns the authentication realm of the given location, i.e. the base location throughout which a set of
     * credentials can be used. Any property contained by the specified FileURL will be carried over in the returned
     * FileURL. On the contrary, credentials will not be copied, the returned URL always has no credentials. 
     *
     * <p>This method returns a new FileURL instance every time it is called. Therefore, the returned URL can
     * safely be modified without any risk of side effects.</p>
     *
     * @param location the location for which to return the authentication realm
     * @return the authentication realm of the specified url
     */
    public FileURL getRealm(FileURL location);

    /**
     * Returns the scheme's guest credentials, <code>null</code> if the scheme doesn't have any.
     * <p>
     * Guest credentials offer a way to authenticate a URL as a 'guest' on file protocols that require a set of
     * credentials to establish a connection. The returned credentials are provided with no guarantee that the fileystem
     * will actually accept them and allow the request/connection. The notion of 'guest' credentials may or may not
     * have a meaning depending on the underlying file protocol.
     * </p>
     *
     * @return the scheme's guest credentials, <code>null</code> if the scheme doesn't have any
     */
    public Credentials getGuestCredentials();

    /**
     * Returns the type of authentication used by the scheme's file protocol. The returned value is one of the constants
     * defined in {@link AuthenticationType}.
     *
     * @return the type of authentication used by the scheme's file protocol
     */
    public AuthenticationType getAuthenticationType();

    /**
     * Returns the scheme's path separator, which serves as a delimiter for path fragments. For most schemes, this is
     * the forward slash character.
     *
     * @return this scheme's path separator
     */
    public String getPathSeparator();

    /**
     * Returns the scheme's standard port, <code>-1</code> if the scheme doesn't have any. Some file protocols may not
     * have a notion of standard port or even no use for the port part at all, for example those that are not TCP
     * or UDP based such as the local 'file' scheme. 
     *
     * @return the scheme's standard port
     */
    public int getStandardPort();
}
