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

package com.mucommander.commons.file.impl.smb;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURLTestCase;

import java.net.MalformedURLException;

/**
 * A {@link FileURLTestCase} implementation for SMB URLs.
 *
 * @author Maxence Bernard
 */
public class SMBFileURLTest extends FileURLTestCase {

    ////////////////////////////////////
    // FileURLTestCase implementation //
    ////////////////////////////////////

    @Override
    protected String getScheme() {
        return "smb";
    }

    @Override
    protected int getDefaultPort() {
        return -1;
    }

    @Override
    protected AuthenticationType getAuthenticationType() {
        return AuthenticationType.AUTHENTICATION_REQUIRED;
    }

    @Override
    protected Credentials getGuestCredentials() {
        return new Credentials("GUEST", "");
    }

    @Override
    protected String getPathSeparator() {
        return "/";
    }

    @Override
    protected boolean isQueryParsed() {
        return false;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * This method is overridden to test SMB's specific notion of realm. 
     */
    @Override
    public void testRealm() throws MalformedURLException {
        assertEquals(getURL("host", "/share"), getURL("host", "/share/path/to/file").getRealm());
    }
}
