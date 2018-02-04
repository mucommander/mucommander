/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
 * <p>
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file.archive.gzip;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.archive.AbstractROArchiveFile;
import com.mucommander.commons.file.archive.ArchiveEntry;
import com.mucommander.commons.file.archive.ArchiveEntryIterator;
import com.mucommander.commons.file.archive.SingleArchiveEntryIterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * GzipArchiveFile provides read-only access to archives in the Gzip format.
 *
 * <p>The actual decompression work is performed by the {@link java.util.zip.GZIPInputStream} class.</p>
 *
 * @see com.mucommander.commons.file.archive.gzip.GzipFormatProvider
 * @author Maxence Bernard
 */
public class GzipArchiveFile extends AbstractROArchiveFile {

    /**
     * Creates a GzipArchiveFile on top of the given file.
     *
     * @param file the underlying file to wrap this archive file around
     */
    public GzipArchiveFile(AbstractFile file) {
        super(file);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    @Override
    public ArchiveEntryIterator getEntryIterator() throws IOException {
        String extension = getCustomExtension() != null ? getCustomExtension() : getExtension();
        String name = getName();

        if (extension != null) {
            // Remove the 'gz' or 'tgz' extension from the entry's name
            switch (extension.toLowerCase()) {
                case "tgz":
                    name = name.substring(0, name.length() - 3) + "tar";
                    break;
                case "gz":
                    name = name.substring(0, name.length() - 3);
                    break;
                default:
            }
        }

        return new SingleArchiveEntryIterator(new ArchiveEntry("/" + name, false, getDate(), -1, true));
    }


    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
        return new GZIPInputStream(getInputStream());
    }
}
