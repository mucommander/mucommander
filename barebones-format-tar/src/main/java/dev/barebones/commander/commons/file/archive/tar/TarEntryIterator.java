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

package dev.barebones.commander.commons.file.archive.tar;

import dev.barebones.commander.commons.file.PermissionBits;
import dev.barebones.commander.commons.file.SimpleFilePermissions;
import dev.barebones.commander.commons.file.archive.ArchiveEntry;
import dev.barebones.commander.commons.file.archive.ArchiveEntryIterator;
import dev.barebones.commander.commons.file.archive.SafePath;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An <code>ArchiveEntryIterator</code> that iterates through a {@link TarArchiveInputStream}.
 *
 * @author Maxence Bernard
 */
class TarEntryIterator implements ArchiveEntryIterator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TarEntryIterator.class);

    /** InputStream to the archive file */
    private TarArchiveInputStream tin;

    /** The current entry, where the TarArchiveInputStream is currently positioned */
    private ArchiveEntry currentEntry;


    /**
     * Creates a new TarEntryIterator that iterates through the entries of the given {@link TarArchiveInputStream}.
     *
     * @param tin the TarArchiveInputStream to iterate through
     * @throws IOException if an error occurred while fetching the first entry
     */
    TarEntryIterator(TarArchiveInputStream tin) throws IOException {
        this.tin = tin;
    }

    /**
     * Returns the {@link TarArchiveInputStream} instance that was used to create this object.
     *
     * @return the {@link TarArchiveInputStream} instance that was used to create this object.
     */
    TarArchiveInputStream getTarInputStream() {
        return tin;
    }

    /**
     * Returns the current entry where the {@link TarArchiveInputStream} is currently positioned.
     * The returned value is <code>null</code> until {@link #nextEntry()} is called for the first time.
     *
     * @return the current entry where the {@link TarArchiveInputStream} is currently positioned.
     */
    ArchiveEntry getCurrentEntry() {
        return currentEntry;
    }

    /**
     * Creates and returns an {@link ArchiveEntry()} whose attributes are fetched from the given
     * {@link TarArchiveEntry}.
     *
     * @param tarEntry the object that serves to initialize the attributes of the returned ArchiveEntry
     * @return an ArchiveEntry whose attributes are fetched from the given {@link TarArchiveEntry}
     */
    private ArchiveEntry createArchiveEntry(TarArchiveEntry tarEntry) {
        ArchiveEntry entry = new ArchiveEntry(tarEntry.getName(), tarEntry.isDirectory(), tarEntry.getModTime().getTime(), tarEntry.getSize(), true);
        entry.setPermissions(new SimpleFilePermissions(tarEntry.getMode() & PermissionBits.FULL_PERMISSION_INT));
        entry.setOwner(tarEntry.getUserName());
        entry.setGroup(tarEntry.getGroupName());
        entry.setEntryObject(tarEntry);
        entry.setSymlink(tarEntry.isSymbolicLink());
        entry.setLinkTarget(tarEntry.getLinkName());

        return entry;
    }

    /**
     * Advances the {@link TarArchiveInputStream} to the next entry and returns the corresponding {@link ArchiveEntry}.
     *
     * @return the next ArchiveEntry
     * @throws IOException if an I/O error occurred
     */
    private ArchiveEntry getNextEntry() throws IOException {
        // Loop so a single unsafe entry doesn't terminate iteration —
        // we skip + log it and try the next.
        while (true) {
            TarArchiveEntry entry = tin.getNextTarEntry();
            if (entry == null) {
                return null;
            }
            // Reject entries whose path would escape the extraction
            // root or otherwise can't safely round-trip through the
            // filesystem.
            try {
                SafePath.validate(entry.getName());
            } catch (SafePath.UnsafeEntryNameException e) {
                LOGGER.warn("Skipping unsafe tar entry: {}", e.getMessage());
                continue;
            }
            return createArchiveEntry(entry);
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
        tin.close();
    }
}
