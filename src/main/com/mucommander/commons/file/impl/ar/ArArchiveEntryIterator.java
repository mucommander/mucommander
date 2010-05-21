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

package com.mucommander.commons.file.impl.ar;

import com.mucommander.commons.file.ArchiveEntry;
import com.mucommander.commons.file.ArchiveEntryIterator;
import com.mucommander.commons.io.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * An <code>ArchiveEntryIterator</code> that iterates through an AR archive. 
 *
 * @author Maxence Bernard
 */
class ArArchiveEntryIterator implements ArchiveEntryIterator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArArchiveEntryIterator.class);

    /** InputStream to the the archive file */
    private InputStream in;

    /** The current entry, where the stream is currently positionned */
    private ArchiveEntry currentEntry;

    /** GNU variant: extended filenames contained in the special // entry's data */
    private byte gnuExtendedNames[];


    /**
     * Creates a new <code>ArArchiveEntryIterator</code> that parses the given AR <code>InputStream</code>.
     * The <code>InputStream</code> will be closed by {@link #close()}.
     *
     * @param in an AR archive <code>InputStream</code>
     * @throws IOException if an I/O error occurred while initializing this iterator
     */
    ArArchiveEntryIterator(InputStream in) throws IOException {
        this.in = in;

        // Skip the global header: "!<arch>" string followed by LF char (8 characters in total).
        StreamUtils.skipFully(in, 8);
    }


    /**
     * Reads the next file header and returns an {@link ArchiveEntry} representing the entry.
     *
     * @return an ArchiveEntry representing the entry
     * @throws IOException if an error occurred
     */
    ArchiveEntry getNextEntry() throws IOException {
        byte fileHeader[] = new byte[60];

        try {
            // Fully read the 60 file header bytes. If it cannot be read, it most likely means we've reached
            // the end of the archive.
            StreamUtils.readFully(in, fileHeader);
        }
        catch(IOException e) {
            return null;
        }

        try {
            // Read the 16 filename characters and trim string to remove any trailing white space
            String name = new String(fileHeader, 0, 16).trim();

            // Read the 12 file date characters, trim string to remove any trailing white space
            // and parse date as a long.
            // If the entry is the special // GNU one (see below), date is null and thus should not be parsed
            // (would throw a NumberFormatException)
            long date = name.equals("//")?0:Long.parseLong(new String(fileHeader, 16, 12).trim()) * 1000;

            // No use for file's Owner ID, Group ID and mode at the moment, skip them

            // Read the 10 file size characters, trim string to remove any trailing white space
            // and parse size as a long
            long size = Long.parseLong(new String(fileHeader, 48, 10).trim());

            // BSD variant : BSD ar store extended filenames by placing the string "#1/" followed by the file name length
            // in the file name field, and appending the real filename to the file header.
            if(name.startsWith("#1/")) {
                // Read extended name
                int extendedNameLength = Integer.parseInt(name.substring(3, name.length()));
                name = new String(StreamUtils.readFully(in, new byte[extendedNameLength])).trim();
                // Decrease remaining file size
                size -= extendedNameLength;
            }
            // GNU variant: GNU ar stores multiple extended filenames in the data section of a file with the name "//",
            // this record is referred to by future headers. A header references an extended filename by storing a "/"
            // followed by a decimal offset to the start of the filename in the extended filename data section.
            // This entry appears first in the archive, i.e. before any other entries.
            else if(name.equals("//")) {
                this.gnuExtendedNames = StreamUtils.readFully(in, new byte[(int)size]);

                // Skip one padding byte if size is odd
                if(size%2!=0)
                    StreamUtils.skipFully(in, 1);

                // Don't return this entry which should not be visible, but recurse to return next entry instead
                return getNextEntry();
            }
            // GNU variant: entry with an extended name, look up extended name in // entry
            else if(this.gnuExtendedNames!=null && name.startsWith("/")) {
                int off = Integer.parseInt(name.substring(1, name.length()));
                name = "";
                byte b;
                while((b=this.gnuExtendedNames[off++])!='/')
                    name += (char)b;
            }

            return new ArchiveEntry(name, false, date, size, true);
        }
        // Re-throw IOException
        catch(IOException e) {
            LOGGER.info("Caught IOException", e);

            throw e;
        }
        // Catch any other exceptions (NumberFormatException for instance) and throw an IOException instead
        catch(Exception e2) {
            LOGGER.info("Caught Exception", e2);

            throw new IOException();
        }
    }


    /////////////////////////////////////////
    // ArchiveEntryIterator implementation //
    /////////////////////////////////////////

    public ArchiveEntry nextEntry() throws IOException {
        if(currentEntry!=null) {
            // Skip the current entry's data, plus 1 padding byte if size is odd
            long size = currentEntry.getSize();
            StreamUtils.skipFully(in, size + (size%2));
        }

        // Get the next entry, if any
        currentEntry = getNextEntry();

        return currentEntry;
    }

    public void close() throws IOException {
        in.close();
    }
}
