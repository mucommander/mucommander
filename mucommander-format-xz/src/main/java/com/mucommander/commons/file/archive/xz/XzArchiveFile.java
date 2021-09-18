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


package com.mucommander.commons.file.archive.xz;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.archive.AbstractROArchiveFile;
import com.mucommander.commons.file.archive.ArchiveEntry;
import com.mucommander.commons.file.archive.ArchiveEntryIterator;
import com.mucommander.commons.file.archive.SingleArchiveEntryIterator;

import org.tukaani.xz.XZInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * XzArchiveFile provides read-only access to archives in the xz format.
 *
 * <p>The actual decompression work is performed by the {@link org.tukaani.xz.XZInputStream} class.</p>
 *
 * @see com.mucommander.commons.file.archive.xz.XzFormatProvider
 * @author Giorgos Retsinas
 */
public class XzArchiveFile extends AbstractROArchiveFile {

    /**
     * Creates a XzArchiveFile on top of the given file.
     *
     * @param file the underlying file to wrap this archive file around
     */
    public XzArchiveFile(AbstractFile file) {
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
            // Remove the 'xz' or 'txz' extension from the entry's name
            extension = extension.toLowerCase();
            int extensionIndex = name.toLowerCase().lastIndexOf("." + extension);

            if (extensionIndex > -1)
                name = name.substring(0, extensionIndex);

            if (extension.equals("txz") || extension.equals("tar.xz"))
                name += ".tar";
        }

        return new SingleArchiveEntryIterator(new ArchiveEntry("/"+name, false, getDate(), -1, true));
    }


    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
        return new XZInputStream(getInputStream());
    }
}
