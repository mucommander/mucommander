/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.conf;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A test case for the {@link FileConfigurationSource} class.
 * @author Nicolas Rinaudo
 */
public class FileConfigurationSourceTest extends TestCase {
    // - Test constants ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** String used for tests. */
    private static final String TEST_VALUE = "Hello, World!";



    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** File used to run the tests. */
    private File file;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new temporary file with which to work.
     */
    public void setUp() throws IOException {
        file = File.createTempFile("conf", "test");
        file.deleteOnExit();
    }



    // - Tests ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void testFiles() {
        FileConfigurationSource source;

        // Makes sure the 'file' constructor works properly.
        source = new FileConfigurationSource(file);
        assertEquals(file, source.getFile());

        // Makes sure the 'string' constructor works properly.
        source = new FileConfigurationSource(file.getAbsolutePath());
        assertEquals(file, source.getFile());
    }

    /**
     * Reads and returns the content of <code>in</code>.
     */
    private String read(InputStream in) throws IOException {
        StringBuffer content;
        byte[]       buffer;
        int          count;

        buffer = new byte[TEST_VALUE.length()];
        content = new StringBuffer();

        while((count = in.read(buffer)) != -1)
            content.append(new String(buffer, 0, count));

        return content.toString();
    }

    /**
     * Tests the source's streams.
     */
    public void testStreams() throws IOException {
        FileConfigurationSource source;
        OutputStream            out;
        InputStream             in;

        // Initialisation.
        out    = null;
        in     = null;
        source = new FileConfigurationSource(file);

        // Writes the test string to the source's output stream.
        try {(out = source.getOutputStream()).write(TEST_VALUE.getBytes());}
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }

        // Reads the content of the source's input stream and makes sure it
        // matches with what we wrote.
        try {assertEquals(TEST_VALUE, read(in = source.getInputStream()));}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }
}
