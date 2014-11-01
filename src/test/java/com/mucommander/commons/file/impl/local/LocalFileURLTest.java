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

package com.mucommander.commons.file.impl.local;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.FileURLTestCase;
import com.mucommander.commons.runtime.OsFamily;
import org.testng.annotations.Test;

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
    @Test
    public void testLocalPathParsing() throws MalformedURLException {
        FileURL url;
        // For OSes that use backslash as a path separator and have a notion of 'root drives' like Windows (C:\ D:\ ...).
        if("\\".equals(getPathSeparator())) {
            assert "\\".equals(getPathSeparator());

            url = FileURL.getFileURL("C:\\");
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/C:\\".equals(url.getPath());

            url = FileURL.getFileURL("C:\\dir\\file");
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/C:\\dir\\file".equals(url.getPath());
            assert "file".equals(url.getFilename());

            url = url.getParent();
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/C:\\dir\\".equals(url.getPath());
            assert "dir".equals(url.getFilename());

            url = FileURL.getFileURL("C:\\direc/tory");
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/C:\\direc/tory".equals(url.getPath());
            assert "direc/tory".equals(url.getFilename());

            // Test forward-separated paths which are also supported

            url = FileURL.getFileURL("C:/");
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/C:\\".equals(url.getPath());

            url = FileURL.getFileURL("C:/dir/file");
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/C:\\dir\\file".equals(url.getPath());
            assert "file".equals(url.getFilename());

            url = url.getParent();
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/C:\\dir\\".equals(url.getPath());
            assert "dir".equals(url.getFilename());

            url = FileURL.getFileURL("C:/direc\\tory");
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/C:\\direc\\tory".equals(url.getPath());
            assert "tory".equals(url.getFilename());
        }
        // For OSes that use forward slash as a path separator
        else {
            url = FileURL.getFileURL("/path");
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/path".equals(url.getPath());
            assert "path".equals(url.getFilename());

            url = FileURL.getFileURL("/path/to");
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/path/to".equals(url.getPath());
            assert "to".equals(url.getFilename());

            url = url.getParent();
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/path/".equals(url.getPath());
            assert "path".equals(url.getFilename());

            url = FileURL.getFileURL("/direc\\tory");
            assert "file".equals(url.getScheme());
            assert "localhost".equals(url.getHost());
            assert "/direc\\tory".equals(url.getPath());
            assert "direc\\tory".equals(url.getFilename());
        }
    }

    /**
     * Tests the resolution of Windows UNC paths (e.g. \\host\\share). This test is system-dependant.
     *
     * @throws MalformedURLException should not happen
     */
    @Test
    public void testUNCParsing() throws MalformedURLException {
        FileURL url = FileURL.getFileURL("\\\\host\\share");

        // UNC path will be transformed into either a 'file' or a 'smb' URL, depending on the current OS
        assert (OsFamily.WINDOWS.isCurrent()?"file":"smb").equals(url.getScheme());
        assert "host".equals(url.getHost());
        assert "/share".equals(url.getPath());
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
    protected AuthenticationType getAuthenticationType() {
        return AuthenticationType.NO_AUTHENTICATION;
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
