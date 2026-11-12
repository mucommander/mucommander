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

package com.mucommander.commons.file.archiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.archive.zip.provider.ZipConstants;
import com.mucommander.commons.file.archive.zip.provider.ZipEntry;
import com.mucommander.commons.file.archive.zip.provider.ZipLong;

import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

/**
 * Archiver implementation using the Zip archive format, with password-protected entries encrypted using AES-256
 * (WinZip AE-2 format, supported by most Zip tools such as 7-Zip, WinZip or libarchive's bsdtar - but not by Info-ZIP's
 * unzip 6.0).
 *
 * <p>Since muCommander's own Zip implementation does not support encryption, archives are written by the
 * <a href="https://github.com/srikanth-lingala/zip4j">Zip4j</a> library. Directory entries are not encrypted as
 * they have no contents.</p>
 *
 * @see ZipArchiver
 */
class EncryptedZipArchiver extends Archiver {

    private final ZipOutputStream zos;

    /** Optional archive comment, written when the archive is closed */
    private String comment;

    /** Path of the entry currently being written, <code>null</code> if no entry has been created yet */
    private String currentEntryPath;

    /** Unix mode of the entry currently being written */
    private int currentEntryUnixMode;

    /**
     * Creates a new archiver that writes an encrypted Zip archive to the given stream.
     *
     * @param outputStream the stream to write the archive to
     * @param password the password used to encrypt the entries, must not be empty
     * @throws IOException if the archive could not be initialized
     */
    EncryptedZipArchiver(OutputStream outputStream, String password) throws IOException {
        super(outputStream);

        this.zos = new ZipOutputStream(outputStream, password.toCharArray());
    }

    /**
     * Overrides Archiver's no-op setComment method as Zip supports archive comment.
     */
    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }


    /////////////////////////////
    // Archiver implementation //
    /////////////////////////////

    @Override
    public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
        // Start by closing current entry
        closeCurrentEntry();

        boolean isDirectory = ZipArchiver.isDirectoryEntry(file);
        String normalizedPath = normalizePath(entryPath, isDirectory);

        ZipParameters parameters = new ZipParameters();
        parameters.setFileNameInZip(normalizedPath);
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        // Note: Zip4j disables encryption for directory entries
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(EncryptionMethod.AES);
        parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        parameters.setLastModifiedFileTime(file.getDate());

        // The size is used by Zip4j to decide whether the entry needs Zip64 extensions. It is not set for directories
        // and symlinks, whose contents (the link's target) is small and differs from the file's size.
        if (!isDirectory && !ZipArchiver.isLocalSymlink(file))
            parameters.setEntrySize(getEntrySizeHint(file.getSize()));

        zos.putNextEntry(parameters);

        currentEntryPath = normalizedPath;
        currentEntryUnixMode = ZipArchiver.getUnixMode(file);

        // Return the OutputStream that allows to write to the entry, only if it isn't a directory
        return isDirectory ? null : zos;
    }

    @Override
    public void close() throws IOException {
        // The underlying stream must be closed even if the archive could not be finalized
        try {
            closeCurrentEntry();

            if (comment != null && !comment.isEmpty())
                zos.setComment(comment);
        }
        finally {
            zos.close();
        }
    }

    @Override
    public InputStream getContentStream(AbstractFile file) throws UnsupportedFileOperationException, IOException {
        if (ZipArchiver.isLocalSymlink(file))
            return ZipArchiver.getSymlinkTargetStream(file);
        return super.getContentStream(file);
    }


    /////////////////////
    // Private methods //
    /////////////////////

    /**
     * Returns the entry size to give Zip4j for a file of the given size. Zip4j decides whether to write the entry's
     * sizes in Zip64 format (in the local header and data descriptor) from that size alone, before the data is
     * compressed and encrypted. Incompressible data grows slightly when deflated and encrypted, so a file slightly
     * smaller than the Zip32 limit can end up larger than it: in that case, as well as when the file size is unknown,
     * a size that forces Zip64 is returned. The actual sizes are written in the data descriptor and central directory.
     *
     * @param size the size of the file, <code>-1</code> if unknown
     * @return the entry size to give Zip4j
     */
    private static long getEntrySizeHint(long size) {
        if (size < 0)
            return ZipConstants.MAX_ZIP32_SIZE;

        // Worst-case size of deflated data (as computed by zlib's deflateBound()), plus the AES salt (16 bytes for
        // AES-256), password verifier (2 bytes) and authentication code (10 bytes)
        long maxCompressedSize = size + (size >>> 12) + (size >>> 14) + (size >>> 25) + 13 + 16 + 2 + 10;
        return maxCompressedSize >= ZipConstants.MAX_ZIP32_SIZE ? Math.max(size, ZipConstants.MAX_ZIP32_SIZE) : size;
    }

    /**
     * Closes the entry currently being written, if any, and stores the entry's Unix mode in its central directory
     * header. Zip4j doesn't allow to specify file attributes when writing to a stream, but the returned header is the
     * one that is written to the central directory when the archive is closed.
     *
     * @throws IOException if the entry could not be closed
     */
    private void closeCurrentEntry() throws IOException {
        if (currentEntryPath == null)
            return;

        FileHeader fileHeader = zos.closeEntry();

        // Let muCommander's own ZipEntry compute the external attributes (Unix mode and MS-DOS attributes) the same way
        // as for non-encrypted archives
        ZipEntry zipEntry = new ZipEntry(currentEntryPath);
        zipEntry.setUnixMode(currentEntryUnixMode);
        fileHeader.setExternalFileAttributes(ZipLong.getBytes(zipEntry.getExternalAttributes()));
        // Unix mode is only taken into account by unzip tools if the host system (upper byte of 'version made by')
        // is Unix
        fileHeader.setVersionMadeBy((zipEntry.getPlatform() << 8) | (fileHeader.getVersionMadeBy() & 0xFF));

        currentEntryPath = null;
    }
}
