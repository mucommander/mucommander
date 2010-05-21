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

package com.mucommander.commons.file.impl.lst;

import com.mucommander.commons.file.ArchiveEntry;
import com.mucommander.commons.file.ArchiveEntryIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

/**
 * An <code>ArchiveEntryIterator</code> that iterates through an LST archive.
 *
 * @author Maxence Bernard
 */
class LstArchiveEntryIterator implements ArchiveEntryIterator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LstArchiveEntryIterator.class);

    /** Allows to read the LST archive line by line */
    private BufferedReader br;

    /** Parses LST-formatted dates */
    private SimpleDateFormat lstDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm.ss");

    /** The next entry to be returned by #nextEntry(), null if there is no more entry */
    private ArchiveEntry nextEntry;

    /** Base folder of all entries */
    private String baseFolder;

    /** Current directory, used for parsing the LST file */
    private String currentDir = "";


    /**
     * Creates a new <code>LstArchiveEntryIterator</code> that parses the given LST <code>InputStream</code>.
     * The <code>InputStream</code> will be closed by {@link #close()}.
     *
     * @param in an LST archive <code>InputStream</code>
     * @throws IOException if an I/O error occurred while initializing this iterator
     */
    LstArchiveEntryIterator(InputStream in) throws IOException {
        br = new BufferedReader(new InputStreamReader(in));

        // Read the base folder
        baseFolder = br.readLine();
        if(baseFolder==null)
            throw new IOException();
    }

    /**
     * Reads the next entry and returns an {@link ArchiveEntry} representing it.
     *
     * @return an ArchiveEntry representing the entry
     * @throws IOException if an error occurred
     */
    ArchiveEntry getNextEntry() throws IOException {
        String line = br.readLine();
        if(line==null)
            return null;

        try {
            StringTokenizer st = new StringTokenizer(line, "\t");

            String name = st.nextToken().replace('\\', '/');
            long size = Long.parseLong(st.nextToken());
            long date = lstDateFormat.parse((st.nextToken()+" "+st.nextToken())).getTime();

            String path;
            boolean isDirectory;

            if(name.endsWith("/")) {
                isDirectory = true;
                currentDir = name;
                path = currentDir;
            }
            else {
                isDirectory = false;
                path = currentDir+name;
            }

            return new LstArchiveEntry(path, isDirectory, date, size, baseFolder);
        }
        catch(Exception e) {    // Catches exceptions thrown by StringTokenizer and SimpleDateFormat
            LOGGER.info("Exception caught while parsing LST file", e);

            throw new IOException();
        }
    }


    /////////////////////////////////////////
    // ArchiveEntryIterator implementation //
    /////////////////////////////////////////

    public ArchiveEntry nextEntry() throws IOException {
        // Return the next entry, if any
        return getNextEntry();
    }

    public void close() throws IOException {
        br.close();
    }
}
