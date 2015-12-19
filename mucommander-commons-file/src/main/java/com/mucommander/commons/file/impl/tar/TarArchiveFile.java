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


package com.mucommander.commons.file.impl.tar;

import com.mucommander.commons.file.*;
import com.mucommander.commons.file.impl.tar.provider.TarEntry;
import com.mucommander.commons.file.impl.tar.provider.TarInputStream;
import com.mucommander.commons.io.StreamUtils;
import com.mucommander.commons.util.StringUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;


/**
 * TarArchiveFile provides read-only access to archives in the Tar/Tgz format.
 *
 * <p>The actual decompression work is performed by the <code>Apache Ant</code> library under the terms of the
 * Apache Software License.</p>
 *
 * @see com.mucommander.commons.file.impl.tar.TarFormatProvider
 * @author Maxence Bernard
 */
public class TarArchiveFile extends AbstractROArchiveFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(TarArchiveFile.class);

    /**
     * Creates a TarArchiveFile on of the given file.
     *
     * @param file the underlying archive file
     */
    public TarArchiveFile(AbstractFile file) {
        super(file);
    }


    /**
     * Returns a TarInputStream which can be used to read TAR entries.
     *
     * @param entryOffset offset from the start of the archive to an entry. Must be a multiple of recordSize, or
     * <code>0</code> to start at the first entry.
     * @return a TarInputStream which can be used to read TAR entries
     * @throws IOException if an error occurred while create the stream
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    private TarInputStream createTarStream(long entryOffset) throws IOException, UnsupportedFileOperationException {
        InputStream in = file.getInputStream();

        String name = getName();
            // Gzip-compressed file
        if(StringUtils.endsWithIgnoreCase(name, "tgz") || StringUtils.endsWithIgnoreCase(name, "tar.gz"))
                // Note: this will fail for gz/tgz entries inside a tar file (IOException: Not in GZIP format),
                // why is a complete mystery: the gz/tgz entry can be extracted and then properly browsed
            in = new GZIPInputStream(in);

        // Bzip2-compressed file
        else if(StringUtils.endsWithIgnoreCase(name, "tbz2") || StringUtils.endsWithIgnoreCase(name, "tar.bz2")) {
            try {
                // Skips the 2 magic bytes 'BZ', as required by CBZip2InputStream. Quoted from CBZip2InputStream's Javadoc:
                // "Although BZip2 headers are marked with the magic 'Bz'. this constructor expects the next byte in the
                // stream to be the first one after the magic.  Thus callers have to skip the first two bytes. Otherwise
                // this constructor will throw an exception."
                StreamUtils.skipFully(in, 2);

                // Quoted from CBZip2InputStream's Javadoc:
                // "CBZip2InputStream reads bytes from the compressed source stream via the single byte {@link java.io.InputStream#read()
                // read()} method exclusively. Thus you should consider to use a buffered source stream."
                in = new CBZip2InputStream(new BufferedInputStream(in));
            }
            catch(Exception e) {
                // CBZip2InputStream is known to throw NullPointerException if file is not properly Bzip2-encoded
                // so we need to catch those and throw them as IOException
                LOGGER.info("Exception caught while creating CBZip2InputStream, throwing IOException", e);

                throw new IOException();
            }
        }

        return new TarInputStream(in, entryOffset);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    @Override
    public ArchiveEntryIterator getEntryIterator() throws IOException, UnsupportedFileOperationException {
        return new TarEntryIterator(createTarStream(0));
    }


    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
        if(entry.isDirectory())
            throw new IOException();

        // Optimization: first check if the specified iterator is positionned at the beginning of the entry.
        // This will typically be the case if an iterator is being used to read all the archive's entries
        // (unpack operation). In that case, we save the cost of looking for the entry in the archive, which is all
        // the more expensive if the TAR archive is GZipped.
        if(entryIterator!=null && (entryIterator instanceof TarEntryIterator)) {
            ArchiveEntry currentEntry = ((TarEntryIterator)entryIterator).getCurrentEntry();
            if(currentEntry.getPath().equals(entry.getPath())) {
                // The entry/tar stream is wrapped in a FilterInputStream where #close is implemented as a no-op:
                // we don't want the TarInputStream to be closed when the caller closes the entry's stream.
                return new FilterInputStream(((TarEntryIterator)entryIterator).getTarInputStream()) {
                    @Override
                    public void close() throws IOException {
                        // No-op
                    }
                };
            }

            // This is not the one, look for the entry from the beginning of the archive
        }

        // Iterate through the archive until we've found the entry
        TarEntry tarEntry = (TarEntry)entry.getEntryObject();
        if(tarEntry!=null) {
            TarInputStream tin = createTarStream(tarEntry.getOffset());
            tin.getNextEntry();

            return tin;
        }

        throw new IOException("Unknown TAR entry: "+entry.getName());
    }
}
