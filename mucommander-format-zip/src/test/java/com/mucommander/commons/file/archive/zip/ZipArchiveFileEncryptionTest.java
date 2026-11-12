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

package com.mucommander.commons.file.archive.zip;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.archive.ArchiveEntry;
import com.mucommander.commons.file.archive.ArchiveEntryIterator;
import com.mucommander.commons.file.archive.zip.provider.ZipEntry;
import com.mucommander.commons.file.archive.zip.provider.ZipLong;
import com.mucommander.commons.file.archive.zip.provider.ZipShort;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

/**
 * Tests reading password-protected Zip archives with {@link ZipArchiveFile}. The archives are created with the Zip4j
 * library, using the WinZip AES and the traditional PKWARE (ZipCrypto) encryption methods.
 */
@Test
public class ZipArchiveFileEncryptionTest {

    /** Signature of a central directory file header, "PK\1\2" */
    private static final long CENTRAL_FILE_HEADER_SIGNATURE = 0x02014B50L;

    private static final String PASSWORD = "s3cr3t-heslo-žluťoučký";

    /** Highly compressible text, stored as a DEFLATED entry */
    private static final byte[] TEXT_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
            .repeat(1000).getBytes(StandardCharsets.UTF_8);

    /** Random data whose length is not a multiple of the AES block size, stored as a STORED entry */
    private static final byte[] BINARY_CONTENT = new byte[50_003];

    static {
        new Random(42).nextBytes(BINARY_CONTENT);
    }

    /** The Zip file created by each test, named so that it isn't recognized as a Zip archive by FileFactory */
    private AbstractFile tempFile;

    @BeforeMethod
    public void setUp() throws IOException {
        tempFile = FileFactory.getTemporaryFile(getClass().getName() + ".data", true);
    }

    @AfterMethod
    public void tearDown() throws IOException {
        if (tempFile.exists())
            tempFile.delete();
    }

    /**
     * Encryption methods to test: unencrypted archives must still be readable without a password.
     */
    @DataProvider
    public Object[][] encryptionMethods() {
        return new Object[][] {
            { EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256 },
            { EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_128 },
            { EncryptionMethod.ZIP_STANDARD, null },
            { EncryptionMethod.NONE, null },
        };
    }

    @Test(dataProvider = "encryptionMethods")
    public void testReadEntries(EncryptionMethod encryptionMethod, AesKeyStrength aesKeyStrength) throws IOException {
        createArchive(encryptionMethod, aesKeyStrength);
        assertEntries(openArchive(encryptionMethod == EncryptionMethod.NONE ? null : PASSWORD));
    }

    /**
     * Listing an encrypted archive without a password must fail, so that the password is asked for when the archive
     * is opened.
     */
    public void testMissingPassword() throws IOException {
        createArchive(EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256);
        ZipArchiveFile archive = openArchive(null);
        expectThrows(IOException.class, archive::getEntryIterator);
    }

    @Test(dataProvider = "encryptionMethods")
    public void testWrongPassword(EncryptionMethod encryptionMethod, AesKeyStrength aesKeyStrength) throws IOException {
        if (encryptionMethod == EncryptionMethod.NONE)
            return;

        createArchive(encryptionMethod, aesKeyStrength);
        ZipArchiveFile archive = openArchive("wrong password");
        expectThrows(IOException.class, archive::getEntryIterator);

        // The archive can be listed once the right password has been set
        archive.setPassword(PASSWORD);
        assertEntries(archive);
    }

    /**
     * Modified AES-encrypted data must be detected thanks to the authentication code.
     */
    public void testCorruptedAesEntry() throws IOException {
        createArchive(EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256);
        corruptEntryData("binary.bin");

        ZipArchiveFile archive = openArchive(PASSWORD);
        ArchiveEntry entry = listEntries(archive).get("binary.bin");
        expectThrows(IOException.class, () -> readEntry(archive, entry));
    }

    /**
     * The authentication code of a DEFLATED AES entry must be verified even if the inflater doesn't need all the
     * encrypted data, which happens when the compressed size recorded in the central directory is too large.
     */
    public void testAesEntryWithEnlargedCompressedSize() throws IOException {
        createArchive(EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256);
        enlargeCompressedSize("dir/text.txt", 20_000);

        ZipArchiveFile archive = openArchive(PASSWORD);
        ArchiveEntry entry = listEntries(archive).get("dir/text.txt");
        expectThrows(IOException.class, () -> readEntry(archive, entry));
    }

    /**
     * The Zip provider itself must refuse to modify a Zip file that contains encrypted entries.
     */
    public void testZipFileRefusesModifications() throws IOException {
        createArchive(EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256);
        com.mucommander.commons.file.archive.zip.provider.ZipFile zipFile = new com.mucommander.commons.file.archive.zip.provider.ZipFile(tempFile);

        expectThrows(IOException.class, () -> zipFile.addEntry(new ZipEntry("new.txt")));
        expectThrows(IOException.class, () -> zipFile.deleteEntry(zipFile.getEntry("binary.bin")));
        expectThrows(IOException.class, zipFile::defragment);
    }

    /**
     * Archives that contain encrypted entries must be read-only.
     */
    public void testEncryptedArchiveIsReadOnly() throws IOException {
        createArchive(EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256);
        ZipArchiveFile archive = openArchive(PASSWORD);
        archive.getEntryIterator();

        assertFalse(archive.isWritable());
        expectThrows(IOException.class, () -> archive.addEntry(new ArchiveEntry("new.txt", false, System.currentTimeMillis(), 0, false)));
    }


    /////////////////////
    // Helper methods //
    /////////////////////

    /**
     * Creates an archive containing a directory, a DEFLATED text entry and a STORED binary entry, encrypted with the
     * given method.
     */
    private void createArchive(EncryptionMethod encryptionMethod, AesKeyStrength aesKeyStrength) throws IOException {
        ZipFile zipFile = encryptionMethod == EncryptionMethod.NONE
                ? new ZipFile(tempFile.getAbsolutePath())
                : new ZipFile(tempFile.getAbsolutePath(), PASSWORD.toCharArray());

        try (zipFile) {
            zipFile.addStream(new ByteArrayInputStream(new byte[0]), createParameters("dir/", CompressionMethod.STORE, EncryptionMethod.NONE, null, 0));
            zipFile.addStream(new ByteArrayInputStream(TEXT_CONTENT), createParameters("dir/text.txt", CompressionMethod.DEFLATE, encryptionMethod, aesKeyStrength, TEXT_CONTENT.length));
            zipFile.addStream(new ByteArrayInputStream(BINARY_CONTENT), createParameters("binary.bin", CompressionMethod.STORE, encryptionMethod, aesKeyStrength, BINARY_CONTENT.length));
        }
    }

    private static ZipParameters createParameters(String name, CompressionMethod compressionMethod, EncryptionMethod encryptionMethod, AesKeyStrength aesKeyStrength, long size) {
        ZipParameters parameters = new ZipParameters();
        parameters.setFileNameInZip(name);
        parameters.setCompressionMethod(compressionMethod);
        parameters.setEntrySize(size);
        if (encryptionMethod != EncryptionMethod.NONE) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(encryptionMethod);
            if (aesKeyStrength != null)
                parameters.setAesKeyStrength(aesKeyStrength);
        }
        return parameters;
    }

    private ZipArchiveFile openArchive(String password) {
        ZipArchiveFile archive = new ZipArchiveFile(tempFile);
        archive.setPassword(password);
        return archive;
    }

    /**
     * Asserts that the given archive contains the entries created by {@link #createArchive}, with the right contents.
     */
    private static void assertEntries(ZipArchiveFile archive) throws IOException {
        Map<String, ArchiveEntry> entries = listEntries(archive);

        assertEquals(entries.keySet(), Set.of("dir/", "dir/text.txt", "binary.bin"));
        assertEquals(readEntry(archive, entries.get("dir/text.txt")), TEXT_CONTENT);
        assertEquals(readEntry(archive, entries.get("binary.bin")), BINARY_CONTENT);
    }

    /**
     * Returns the archive's entries, mapped by their path.
     */
    private static Map<String, ArchiveEntry> listEntries(ZipArchiveFile archive) throws IOException {
        Map<String, ArchiveEntry> entries = new HashMap<>();
        ArchiveEntryIterator iterator = archive.getEntryIterator();
        ArchiveEntry entry;
        while ((entry = iterator.nextEntry()) != null)
            entries.put(entry.getPath(), entry);
        iterator.close();
        return entries;
    }

    private static byte[] readEntry(ZipArchiveFile archive, ArchiveEntry entry) throws IOException {
        try (InputStream in = archive.getEntryInputStream(entry, null)) {
            return in.readAllBytes();
        }
    }

    /**
     * Adds the given number of bytes to the compressed size of the given entry, as recorded in its central directory
     * file header.
     */
    private void enlargeCompressedSize(String entryName, int delta) throws IOException {
        Path path = Path.of(tempFile.getAbsolutePath());
        byte[] zip = Files.readAllBytes(path);
        byte[] name = entryName.getBytes(StandardCharsets.UTF_8);

        // Central directory file header: signature at offset 0, compressed size at offset 20, filename length at
        // offset 28, filename at offset 46
        for (int i = 0; i + 46 + name.length <= zip.length; i++) {
            if (ZipLong.getValue(zip, i) != CENTRAL_FILE_HEADER_SIGNATURE || ZipShort.getValue(zip, i + 28) != name.length
                    || !Arrays.equals(zip, i + 46, i + 46 + name.length, name, 0, name.length))
                continue;

            System.arraycopy(ZipLong.getBytes(ZipLong.getValue(zip, i + 20) + delta), 0, zip, i + 20, 4);
            Files.write(path, zip);
            return;
        }
        throw new AssertionError("central directory header not found: " + entryName);
    }

    /**
     * Flips a byte in the middle of the encrypted data of the given AES-encrypted entry.
     */
    private void corruptEntryData(String entryName) throws IOException {
        long localHeaderOffset;
        try (ZipFile zipFile = new ZipFile(tempFile.getAbsolutePath())) {
            FileHeader fileHeader = zipFile.getFileHeader(entryName);
            assertTrue(fileHeader.isEncrypted());
            localHeaderOffset = fileHeader.getOffsetLocalHeader();
        }

        try (RandomAccessFile raf = new RandomAccessFile(new File(tempFile.getAbsolutePath()), "rw")) {
            // Local file header: 30 bytes of fixed-size fields, followed by the filename and the extra field, whose
            // lengths are stored as little-endian shorts at offsets 26 and 28
            raf.seek(localHeaderOffset + 26);
            int fileNameLength = raf.read() | (raf.read() << 8);
            int extraFieldLength = raf.read() | (raf.read() << 8);
            long dataOffset = localHeaderOffset + 30 + fileNameLength + extraFieldLength;

            // Skip the salt (16 bytes for AES-256) and password verification value (2 bytes)
            long corruptedOffset = dataOffset + 16 + 2 + BINARY_CONTENT.length / 2;
            raf.seek(corruptedOffset);
            int value = raf.read();
            raf.seek(corruptedOffset);
            raf.write(value ^ 0xFF);
        }
    }
}
