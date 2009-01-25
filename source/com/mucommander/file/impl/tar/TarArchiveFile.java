/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.file.impl.tar;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractROArchiveFile;
import com.mucommander.file.ArchiveEntry;
import com.mucommander.file.ArchiveEntryIterator;
import com.mucommander.file.impl.tar.provider.TarEntry;
import com.mucommander.file.impl.tar.provider.TarInputStream;
import com.mucommander.io.StreamUtils;
import com.mucommander.util.StringUtils;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;


/**
 * TarArchiveFile provides read-only access to archives in the Tar/Tgz format.
 *
 * <p>The actual decompression work is performed by the <code>Apache Ant</code> library under the terms of the
 * Apache Software License.</p>
 *
 * @see com.mucommander.file.impl.tar.TarFormatProvider
 * @author Maxence Bernard
 */
public class TarArchiveFile extends AbstractROArchiveFile {

    protected ThreadLocal iterators = new ThreadLocal();

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
     */
    private TarInputStream createTarStream(long entryOffset) throws IOException {
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
                if(com.mucommander.Debug.ON)
                    com.mucommander.Debug.trace("Exception caught while creating CBZip2InputStream: "+e+", throwing IOException");

                throw new IOException();
            }
        }

        return new TarInputStream(in, entryOffset);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    public ArchiveEntryIterator getEntryIterator() throws IOException {
        TarArchiveEntryIterator iterator = new TarArchiveEntryIterator(createTarStream(0)) {
            public void close() throws IOException {
                if(iterators.get()==this)
                    iterators.set(null);
            }
        };

        iterators.set(iterator);

        return iterator;
    }

    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        // Optimization: first try to see if there isn't an open (i.e. not closed) iterator that is positionned
        // at the beginning of the entry. This will typically be the case if an iterator is being used to read all
        // the archive's entries (unpack operation). In that case, we save the cost of looking for the entry from the
        // beginning of the archive, which is especially expensive if the TAR archive is GZipped.
        TarArchiveEntryIterator iterator = (TarArchiveEntryIterator)iterators.get();
        if(iterator!=null) {
            ArchiveEntry currentEntry = iterator.peek();
            if(currentEntry.getPath().equals(entry.getPath()))
                return iterator.getTarInputStream();

            // OK, the iterator's next entry is not the one we're looking for. But it may be one of the subsequent
            // entries, in case the archive is being iterated and one of the entries was skipped.
            ArchiveEntry ae;
            while ((ae = iterator.nextEntry())!=null && ae!=currentEntry) {     // Avoid infinite loops
                if (ae.getName().equals(entry.getPath()))
                    return iterator.getTarInputStream();
            }

            // If it wasn't found, start from the beginning of the archive
        }

        TarEntry tarEntry = (TarEntry)entry.getEntryObject();
        if(tarEntry!=null) {
            TarInputStream tin = createTarStream(tarEntry.getOffset());
            tin.getNextEntry();

            return tin;
        }

        throw new IOException("Unknown TAR entry: "+entry.getName());
    }
}
