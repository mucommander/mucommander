/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.file.compat;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Maxence Bernard
*/
class CompatURLConnection extends URLConnection {

    protected AbstractFile file;

    public CompatURLConnection(URL url) throws IOException {
        super(url);

        // Not connected yet
    }

    public CompatURLConnection(URL url, AbstractFile file) throws IOException {
        super(url);

        if(file!=null) {
            this.file = file;
            connected = true;
        }
    }

    /**
     * Checks if this <code>URLConnection</code> is connected and if it isn't, calls {@link #connect()} to connect it.
     *
     * @throws IOException if an error occurred while connecting this URLConnection
     */
    private void checkConnected() throws IOException {
        if(!connected)
            connect();
    }


    //////////////////////////////////
    // URLConnection implementation //
    //////////////////////////////////

    /**
     * Creates the {@link AbstractFile} instance corresponding to the URL location, only if no <code>AbstractFile</code>
     * has been specified when this <code>CompatURLConnection</code> was created.
     *
     * @throws IOException if an error occurred while instanciating the AbstractFile
     */
    public void connect() throws IOException {
        if(!connected) {
            file = FileFactory.getFile(url.toString(), true);
            connected = true;
        }
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public InputStream getInputStream() throws IOException {
        checkConnected();

        return file.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        checkConnected();

        return file.getOutputStream(false);
    }

    public long getLastModified() {
        try {
            checkConnected();

            return file.getDate();
        }
        catch(IOException e) {
            return 0;
        }
    }

    public long getDate() {
        return getLastModified();
    }

    public int getContentLength() {
        try {
            checkConnected();

            return (int)file.getSize();
        }
        catch(IOException e) {
            return -1;
        }
    }
}
