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


package dev.barebones.commander.commons.file.archive.zip;

import java.io.IOException;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dev.barebones.commander.commons.file.archive.ArchiveEntry;
import dev.barebones.commander.commons.file.archive.ArchiveEntryIterator;
import dev.barebones.commander.commons.file.archive.SafePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An <code>ArchiveEntryIterator</code> that iterates through a {@link ZipInputStream}.
 *
 * @author Maxence Bernard
 */
public class JavaUtilZipEntryIterator implements ArchiveEntryIterator  {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaUtilZipEntryIterator.class);

    /** InputStream to the archive file */
    private ZipInputStream zin;

    /** The current entry, where the ZipInputStream is currently positionned */
    private ArchiveEntry currentEntry;

    /** A function that enables creating {@link dev.barebones.commander.commons.file.archive.zip.provider.ZipEntry} */
    private Function<dev.barebones.commander.commons.file.archive.zip.provider.ZipEntry, ArchiveEntry> createArchiveEntryFunc;

    /**
     * Creates a new TarEntryIterator that iterates through the entries of the given {@link ZipInputStream}.
     *
     * @param zin the TarInputStream to iterate through
     * @throws IOException if an error occurred while fetching the first entry
     */
    JavaUtilZipEntryIterator(ZipInputStream zin, Function<dev.barebones.commander.commons.file.archive.zip.provider.ZipEntry, ArchiveEntry> createArchiveEntryFunc) {
        this.zin = zin;
        this.createArchiveEntryFunc = createArchiveEntryFunc;
    }

    /**
     * Returns the {@link ZipInputStream} instance that was used to create this object.
     *
     * @return the {@link ZipInputStream} instance that was used to create this object.
     */
    ZipInputStream getZipInputStream() {
        return zin;
    }

    /**
     * Returns the current entry, where the <code>ZipInputStream</code> is currently positionned.
     *
     * @return the current entry, where the <code>ZipInputStream</code> is currently positionned.
     */
    ArchiveEntry getCurrentEntry() {
        return currentEntry;
    }

    /**
     * Advances the {@link ZipInputStream} to the next entry and returns the corresponding {@link ArchiveEntry}.
     *
     * @return the next ArchiveEntry
     * @throws java.io.IOException if an I/O error occurred
     */
    private ArchiveEntry getNextEntry() throws IOException {
        // Loop so a single unsafe entry doesn't terminate iteration —
        // we skip + log it and try the next.
        while (true) {
            ZipEntry entry;
            try {
                entry = zin.getNextEntry();
            }
            catch(Exception e) {
                // java.util.zip.ZipInputStream can throw an
                // IllegalArgumentException when the filename / comment
                // encoding is not UTF-8 (ZipInputStream always expects
                // UTF-8). Catch the broader Exception just to be safe.
                throw new IOException("ZipInputStream.getNextEntry failed", e);
            }
            catch(Error e) {
                // ZipInputStream#getNextEntry() throws InternalError
                // ("invalid compression method") on non-DEFLATED /
                // STORED methods (e.g. IMPLODED).
                throw new IOException("ZipInputStream rejected compression method", e);
            }

            if (entry == null) {
                return null;
            }

            // Reject entries whose path would escape the extraction
            // root or otherwise can't safely round-trip through the
            // filesystem.
            try {
                SafePath.validate(entry.getName());
            } catch (SafePath.UnsafeEntryNameException e) {
                LOGGER.warn("Skipping unsafe zip entry: {}", e.getMessage());
                continue;
            }

            return createArchiveEntryFunc.apply(new dev.barebones.commander.commons.file.archive.zip.provider.ZipEntry(entry));
        }
    }


    /////////////////////////////////////////
    // ArchiveEntryIterator implementation //
    /////////////////////////////////////////

    @Override
    public ArchiveEntry nextEntry() throws IOException {
        // Get the next entry, if any
        this.currentEntry = getNextEntry();

        return currentEntry;
    }

    @Override
    public void close() throws IOException {
        zin.close();
    }
}
