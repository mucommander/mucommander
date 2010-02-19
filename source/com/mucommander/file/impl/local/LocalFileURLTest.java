/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.file.impl.local;

import com.mucommander.auth.AuthenticationTypes;
import com.mucommander.auth.Credentials;
import com.mucommander.file.FileURL;
import com.mucommander.file.FileURLTestCase;
import com.mucommander.runtime.OsFamilies;

import java.net.MalformedURLException;

/**
 * A {@link FileURLTestCase} implementation for local file URLs.
 *
 * @author Maxence Bernard
 */
public class LocalFileURLTest extends FileURLTestCase {

    //////////////////
    // Extra tests //
    /////////////////

    /**
     * Tests the resolution of local paths (e.g. /path/to/file). This test is system-dependant.
     *
     * @throws MalformedURLException should not happen
     */
    public void testLocalPathParsing() throws MalformedURLException {
        FileURL url;
        // For OSes that use backslash as a path separator and have a notion of 'root drives' like Windows (C:\ D:\ ...).
        if("\\".equals(getPathSeparator())) {
            assertEquals("\\", getPathSeparator());

            url = FileURL.getFileURL("C:\\");
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/C:\\", url.getPath());

            url = FileURL.getFileURL("C:\\dir\\file");
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/C:\\dir\\file", url.getPath());
            assertEquals("file", url.getFilename());

            url = url.getParent();
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/C:\\dir\\", url.getPath());
            assertEquals("dir", url.getFilename());

            url = FileURL.getFileURL("C:\\direc/tory");
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/C:\\direc/tory", url.getPath());
            assertEquals("direc/tory", url.getFilename());

            // Test forward-separated paths which are also supported

            url = FileURL.getFileURL("C:/");
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/C:\\", url.getPath());

            url = FileURL.getFileURL("C:/dir/file");
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/C:\\dir\\file", url.getPath());
            assertEquals("file", url.getFilename());

            url = url.getParent();
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/C:\\dir\\", url.getPath());
            assertEquals("dir", url.getFilename());

            url = FileURL.getFileURL("C:/direc\\tory");
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/C:\\direc\\tory", url.getPath());
            assertEquals("tory", url.getFilename());
        }
        // For OSes that use forward slash as a path separator
        else {
            url = FileURL.getFileURL("/path");
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/path", url.getPath());
            assertEquals("path", url.getFilename());

            url = FileURL.getFileURL("/path/to");
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/path/to", url.getPath());
            assertEquals("to", url.getFilename());

            url = url.getParent();
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/path/", url.getPath());
            assertEquals("path", url.getFilename());

            url = FileURL.getFileURL("/direc\\tory");
            assertEquals("file", url.getScheme());
            assertEquals("localhost", url.getHost());
            assertEquals("/direc\\tory", url.getPath());
            assertEquals("direc\\tory", url.getFilename());
        }
    }

    /**
     * Tests the resolution of Windows UNC paths (e.g. \\host\\share). This test is system-dependant.
     *
     * @throws MalformedURLException should not happen
     */
    public void testUNCParsing() throws MalformedURLException {
        FileURL url = FileURL.getFileURL("\\\\host\\share");

        // UNC path will be transformed into either a 'file' or a 'smb' URL, depending on the current OS
        assertEquals(OsFamilies.WINDOWS.isCurrent()?"file":"smb", url.getScheme());
        assertEquals("host", url.getHost());
        assertEquals("/share", url.getPath());
    }

    ////////////////////////////////////
    // FileURLTestCase implementation //
    ////////////////////////////////////

    @Override
    protected String getScheme() {
        return "file";
    }

    @Override
    protected int getDefaultPort() {
        return -1;
    }

    @Override
    protected int getAuthenticationType() {
        return AuthenticationTypes.NO_AUTHENTICATION;
    }

    @Override
    protected Credentials getGuestCredentials() {
        return null;
    }

    @Override
    protected String getPathSeparator() {
        return System.getProperty("file.separator");
    }

    @Override
    protected boolean isQueryParsed() {
        return false;
    }
}
