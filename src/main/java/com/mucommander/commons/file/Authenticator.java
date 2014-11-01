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
 * This interface is used by{@link FileFactory} to authenticate {@link FileURL} instances prior to resolving
 * corresponding {@link AbstractFile} instances. This interface provides the necessary hooks for interacting with an
 * application or system keystore.
 * <p>
 * A typical implementation of {@link #authenticate(FileURL)} will look for {@link Credentials} matching the
 * specified URL and, if one set (or more) is found, call {@link FileURL#setCredentials(Credentials)} to set them.
 * Likewise, this method may also look for and set {@link FileURL#setProperty(String, String) URL properties},
 * that will be used by the corresponding {@link AbstractFile} during or after resolution.
 * </p>
 * <p>
 * {@link #authenticate(FileURL)} should normally be called only for {@link FileURL} schemes that
 * {@link FileURL#getAuthenticationType() support authentication}. Implementations should however not rely on that and
 * handle non-authenticated URLs as a no-op.
 * </p>
 * <p>
 * A default authenticator can be registered at {@link FileFactory#setDefaultAuthenticator(Authenticator)}.
 * </p>
 *
 * @see FileURL#getAuthenticationType()
 * @see FileFactory#setDefaultAuthenticator(Authenticator)
 * @see FileFactory#getFile(FileURL, AbstractFile, Authenticator, Object...)
 * @author Maxence Bernard
 */
public interface Authenticator {

    /**
     * Authenticates the specified {@link FileURL} instance.
     *
     * @param fileURL the file URL to authenticate
     */
    public void authenticate(FileURL fileURL);
}
