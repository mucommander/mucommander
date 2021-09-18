/**
 * This file is part of muCommander, http://www.mucommander.com
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


package com.mucommander.commons.file.archive.tar;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.archive.AbstractROArchiveFile;
import com.mucommander.commons.file.archive.ArchiveEntry;
import com.mucommander.commons.file.archive.ArchiveEntryIterator;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * TarArchiveFile provides read-only access to archives in the Tar/Tgz format.
 *
 * <p>The actual decompression work is performed by the <code>Apache Commons Compress</code> library under the terms of
 * the Apache Software License.</p>
 *
 * @see com.mucommander.commons.file.archive.tar.TarFormatProvider
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


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    @Override
    public ArchiveEntryIterator getEntryIterator() throws IOException, UnsupportedFileOperationException {
        return new TarEntryIterator(new TarArchiveInputStream(getInputStream()));
    }


    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
        if(entry.isDirectory())
            throw new IOException();

        // Optimization: first check if the specified iterator is positioned at the beginning of the entry.
        // This will typically be the case if an iterator is being used to read all the archive's entries
        // (unpack operation). In that case, we save the cost of looking for the entry in the archive, which is all
        // the more expensive if the TAR archive is GZipped.
        if(entryIterator!=null && (entryIterator instanceof TarEntryIterator)) {
            ArchiveEntry currentEntry = ((TarEntryIterator)entryIterator).getCurrentEntry();
            if(currentEntry.equals(entry)) {
                // The entry/tar stream is wrapped in a FilterInputStream where #close is implemented as a no-op:
                // we don't want the TarArchiveInputStream to be closed when the caller closes the entry's stream.
                return new FilterInputStream(((TarEntryIterator)entryIterator).getTarInputStream()) {
                    @Override
                    public void close() throws IOException {
                        // No-op
                    }
                };
            }

            // This is not the one, look for the entry from the beginning of the archive
        }

        TarArchiveInputStream tin = new TarArchiveInputStream(getInputStream());
        TarArchiveEntry tarEntry;
        String targetPath = entry.getPath();
        // Iterate through the archive until we've found the entry
         while ((tarEntry = tin.getNextTarEntry()) != null) {
             if (tarEntry.getName().equals(targetPath)) {
                 // That's the one, return it
                 return tin;
            }
        }

        throw new IOException("Unknown TAR entry: "+entry.getName());
    }
}
