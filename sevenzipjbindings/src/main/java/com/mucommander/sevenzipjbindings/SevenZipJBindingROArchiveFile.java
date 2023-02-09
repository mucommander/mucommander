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

package com.mucommander.sevenzipjbindings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.archive.AbstractROArchiveFile;
import com.mucommander.commons.file.archive.ArchiveEntry;
import com.mucommander.commons.file.archive.ArchiveEntryIterator;
import com.mucommander.commons.file.archive.WrapperArchiveEntryIterator;
import com.mucommander.commons.util.CircularByteBuffer;
import com.mucommander.commons.util.StringUtils;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;

/**
 * @author Oleg Trifonov, Arik Hadas
 */
public class SevenZipJBindingROArchiveFile extends AbstractROArchiveFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(SevenZipJBindingROArchiveFile.class);

    protected IInArchive inArchive;
    private ArchiveFormat sevenZipJBindingFormat;

    private final byte[] formatSignature;

    /**
     * Creates an AbstractROArchiveFile on top of the given file.
     *
     * @param file the file on top of which to create the archive
     *
     * @see <a href="http://sevenzipjbind.sourceforge.net/javadoc/net/sf/sevenzipjbinding/ArchiveFormat.html">
     *      ArchiveFormat</a>
     */
    public SevenZipJBindingROArchiveFile(AbstractFile file, ArchiveFormat sevenZipJBindingFormat, byte[] formatSignature) {
        super(file);
        this.sevenZipJBindingFormat = sevenZipJBindingFormat;
        this.formatSignature = formatSignature;
    }

    /**
     * Open the file and check its signature compared to the one provided in {@link #SevenZipJBindingROArchiveFile(AbstractFile, ArchiveFormat, byte[])}
     * @return this {@code SevenZipJBindingROArchiveFile} instance when file signature matches the specified signature
     * @throws IOException in case the file cannot be opened or its signature differs from the specified signature
     */
    public SevenZipJBindingROArchiveFile check() throws IOException {
        openInArchive();
        return this;
    }

    private IInArchive openInArchive() throws IOException {
        if (inArchive == null) {
            SignatureCheckedRandomAccessFile in = new SignatureCheckedRandomAccessFile(file, formatSignature);
            inArchive = SevenZip.openInArchive(sevenZipJBindingFormat, in);
        }
        return inArchive;
    }

    @Override
    public ArchiveEntryIterator getEntryIterator() throws IOException {
        try {
            try (IInArchive sevenZipFile = openInArchive()) {
                int nbEntries = sevenZipFile.getNumberOfItems();
                List<ArchiveEntry> entries = new ArrayList<>();
                for (int i = 0; i < nbEntries; i++) {
                    entries.add(createArchiveEntry(i, sevenZipFile));
                }
                return new WrapperArchiveEntryIterator(entries.iterator());
            }
        } catch (SevenZipException e) {
            LOGGER.warn("failed to list archive: " + e.getMessage());
            LOGGER.debug("failed to list archive", e);
            throw new IOException(e);
        } finally {
            inArchive = null;
        }
    }

    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) {
        final CircularByteBuffer cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
        new Thread(() -> {
            synchronized (SevenZipJBindingROArchiveFile.this) {
                try {
                    try (IInArchive sevenZipFile = openInArchive()) {
                        ISequentialOutStream outStream = data -> {
                            try {
                                cbb.getOutputStream().write(data);
                            } catch (IOException e) {
                                throw new SevenZipException(e);
                            }
                            return data.length; // Return amount of proceed data
                        };
                        sevenZipFile.extractSlow((Integer) entry.getEntryObject(),outStream);
                    }
                } catch (IOException e) {
                    LOGGER.warn("failed to extract entry from archive " + e.getMessage());
                    LOGGER.debug("failed to extract entry from archive", e);
                } finally {
                    try {
                        cbb.getOutputStream().close();
                    } catch (IOException e) {
                        System.err.println("Error in closing outputstream: " + e.getMessage());
                    }
                    inArchive = null;
                }
            }
        }).start();

        return cbb.getInputStream();
    }

    /**
     * Creates and return an {@link ArchiveEntry()} whose attributes are fetched from the given {@link com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZipEntry}
     *
     * @param i the index of entry
     * @param sevenZipFile the archive file
     * @return an ArchiveEntry whose attributes are fetched from the given SevenZipEntry
     */
    private ArchiveEntry createArchiveEntry(int i, IInArchive sevenZipFile) throws IOException {
        String path = sevenZipFile.getStringProperty(i, PropID.PATH);
        boolean isDirectory = (Boolean)sevenZipFile.getProperty(i, PropID.IS_FOLDER);
        Date time = (Date) sevenZipFile.getProperty(i, PropID.LAST_MODIFICATION_TIME);
        Long size = (Long) sevenZipFile.getProperty(i, PropID.SIZE);
        if (StringUtils.isNullOrEmpty(path)) {
            path = file.getNameWithoutExtension();
        }
        path = path.replace(File.separatorChar, ArchiveEntry.SEPARATOR_CHAR);
        ArchiveEntry result = new ArchiveEntry(path, isDirectory,
                time == null ? -1 : time.getTime(),
                size == null ? -1 : size, true);
        result.setEntryObject(i);
        return result;
    }

}