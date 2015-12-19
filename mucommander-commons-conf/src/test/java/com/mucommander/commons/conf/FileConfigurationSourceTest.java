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

package com.mucommander.commons.conf;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A test case for the {@link FileConfigurationSource} class.
 * @author Nicolas Rinaudo
 */
public class FileConfigurationSourceTest  {
    // - Test constants ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** String used for tests. */
    private static final String TEST_VALUE = "Hello, World!";



    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** File used to run the tests. */
    private File file;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new temporary file with which to work.
     */
    @BeforeMethod
    public void setUp() throws IOException {
        file = File.createTempFile("conf", "test");
        file.deleteOnExit();
    }



    // - Tests ---------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Tests file source initialisation.
     */
    @Test
    public void testInitialisation() {
        FileConfigurationSource source;

        // Makes sure the 'file' constructor works properly.
        source = new FileConfigurationSource(file, "utf-8");
        assert file.equals(source.getFile());

        // Makes sure the 'string' constructor works properly.
        source = new FileConfigurationSource(file.getAbsolutePath(), "utf-8");
        assert file.equals(source.getFile());
    }

    /**
     * Reads and returns the content of <code>in</code>.
     */
    private String read(Reader in) throws IOException {
        StringBuilder content;
        char[]        buffer;
        int           count;

        buffer = new char[TEST_VALUE.length()];
        content = new StringBuilder();

        while((count = in.read(buffer)) != -1)
            content.append(buffer, 0, count);

        return content.toString();
    }

    /**
     * Tests the source's streams.
     * @throws IOException if an IO related error occurs.
     */
    @Test
    public void testStreams() throws IOException {
        FileConfigurationSource source;
        Writer                  out;
        Reader                  in;

        // Initialisation.
        out    = null;
        in     = null;
        source = new FileConfigurationSource(file, "utf-8");

        // Writes the test string to the source's output stream.
        try {(out = source.getWriter()).write(TEST_VALUE.toCharArray());}
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }

        // Reads the content of the source's input stream and makes sure it
        // matches with what we wrote.
        try {assert TEST_VALUE.equals(read(in = source.getReader()));}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }
}
