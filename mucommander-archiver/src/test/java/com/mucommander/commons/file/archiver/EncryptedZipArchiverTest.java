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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.archive.ArchiveEntry;
import com.mucommander.commons.file.archive.ArchiveEntryIterator;
import com.mucommander.commons.file.archive.zip.ZipArchiveFile;
import com.mucommander.commons.io.StreamUtils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

/**
 * Tests creating password-protected Zip archives with {@link EncryptedZipArchiver}. The archives are read back both
 * with the Zip4j library and with muCommander's own {@link ZipArchiveFile}.
 */
@Test
public class EncryptedZipArchiverTest {

    private static final String PASSWORD = "s3cr3t-heslo-žluťoučký";

    private static final String COMMENT = "Encrypted archive";

    private static final byte[] TEXT_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
            .repeat(1000).getBytes(StandardCharsets.UTF_8);

    private static final byte[] BINARY_CONTENT = new byte[50_003];

    static {
        new Random(42).nextBytes(BINARY_CONTENT);
    }

    /** Whether the filesystem supports POSIX permissions and symlinks, which are then tested as well */
    private static final boolean POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

    /** Folder containing the files to pack and the resulting archive */
    private Path tempDir;

    @BeforeMethod
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory(getClass().getSimpleName());
    }

    @AfterMethod
    public void tearDown() throws IOException {
        try (Stream<Path> paths = Files.walk(tempDir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        }
    }

    public void testArchiveIsAes256Encrypted() throws IOException {
        Path archivePath = createArchive();

        try (ZipFile zipFile = new ZipFile(archivePath.toFile(), PASSWORD.toCharArray())) {
            assertTrue(zipFile.isEncrypted());
            assertEquals(zipFile.getComment(), COMMENT);

            Map<String, byte[]> contents = new HashMap<>();
            for (FileHeader fileHeader : zipFile.getFileHeaders()) {
                // Directory entries have no contents and are not encrypted
                if (fileHeader.isDirectory()) {
                    assertFalse(fileHeader.isEncrypted());
                    contents.put(fileHeader.getFileName(), null);
                    continue;
                }

                assertTrue(fileHeader.isEncrypted());
                assertEquals(fileHeader.getEncryptionMethod(), EncryptionMethod.AES);
                assertEquals(fileHeader.getAesExtraDataRecord().getAesKeyStrength(), AesKeyStrength.KEY_STRENGTH_256);
                try (InputStream in = zipFile.getInputStream(fileHeader)) {
                    contents.put(fileHeader.getFileName(), in.readAllBytes());
                }
            }

            assertEquals(contents.get("text.txt"), TEXT_CONTENT);
            assertEquals(contents.get("sub/binary.bin"), BINARY_CONTENT);
            assertTrue(contents.containsKey("sub/"));
        }
    }

    /**
     * Reads the archive back with muCommander's own Zip implementation, which also checks that Unix permissions and
     * symlinks are preserved.
     */
    public void testArchiveIsReadableByZipArchiveFile() throws IOException {
        Path archivePath = createArchive();

        ZipArchiveFile archive = new ZipArchiveFile(FileFactory.getFile(archivePath.toString()));
        archive.setPassword(PASSWORD);

        Map<String, ArchiveEntry> entries = new HashMap<>();
        Map<String, byte[]> contents = new HashMap<>();
        ArchiveEntryIterator iterator = archive.getEntryIterator();
        ArchiveEntry entry;
        while ((entry = iterator.nextEntry()) != null) {
            entries.put(entry.getPath(), entry);
            if (!entry.isDirectory() && !entry.isSymlink()) {
                try (InputStream in = archive.getEntryInputStream(entry, null)) {
                    contents.put(entry.getPath(), in.readAllBytes());
                }
            }
        }
        iterator.close();

        assertEquals(contents.get("text.txt"), TEXT_CONTENT);
        assertEquals(contents.get("sub/binary.bin"), BINARY_CONTENT);
        assertTrue(entries.get("sub/").isDirectory());

        if (POSIX) {
            // Only the owner's permissions are reported by local files, other permissions get default values
            assertEquals(entries.get("text.txt").getPermissions().getIntValue() & 0700, 0400);
            assertTrue(entries.get("link.txt").isSymlink());
            assertEquals(entries.get("link.txt").getLinkTarget(), "text.txt");
        }
    }

    public void testEmptyPasswordCreatesUnencryptedArchive() throws IOException {
        assertTrue(Archiver.getArchiver(new ByteArrayOutputStream(), Archiver.ZIP_FORMAT, "") instanceof ZipArchiver);
        assertTrue(Archiver.getArchiver(new ByteArrayOutputStream(), Archiver.ZIP_FORMAT, null) instanceof ZipArchiver);
    }

    public void testPasswordRejectedForUnsupportedFormat() {
        assertFalse(Archiver.formatSupportsPassword(Archiver.TAR_FORMAT));
        expectThrows(IllegalArgumentException.class,
                () -> Archiver.getArchiver(new ByteArrayOutputStream(), Archiver.TAR_FORMAT, PASSWORD));
    }


    ////////////////////
    // Helper methods //
    ////////////////////

    /**
     * Creates the files to pack and packs them into an encrypted Zip archive, the same way ArchiveJob does.
     *
     * @return the path to the created archive
     */
    private Path createArchive() throws IOException {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Path textFile = Files.write(sourceDir.resolve("text.txt"), TEXT_CONTENT);
        Path subDir = Files.createDirectory(sourceDir.resolve("sub"));
        Path binaryFile = Files.write(subDir.resolve("binary.bin"), BINARY_CONTENT);

        Map<String, Path> entryPaths = new LinkedHashMap<>();
        entryPaths.put("text.txt", textFile);
        entryPaths.put("sub", subDir);
        entryPaths.put("sub/binary.bin", binaryFile);

        if (POSIX) {
            Files.setPosixFilePermissions(textFile, PosixFilePermissions.fromString("r--------"));
            entryPaths.put("link.txt", Files.createSymbolicLink(sourceDir.resolve("link.txt"), Path.of("text.txt")));
        }

        Path archivePath = tempDir.resolve("archive.data");
        try (OutputStream out = Files.newOutputStream(archivePath)) {
            Archiver archiver = Archiver.getArchiver(out, Archiver.ZIP_FORMAT, PASSWORD);
            assertTrue(archiver instanceof EncryptedZipArchiver);
            archiver.setComment(COMMENT);

            for (Map.Entry<String, Path> entryPath : entryPaths.entrySet()) {
                AbstractFile file = FileFactory.getFile(entryPath.getValue().toString());
                if (file.isDirectory() && !file.isSymlink()) {
                    archiver.createEntry(entryPath.getKey(), file);
                    continue;
                }

                // The entry stream must not be closed, the archiver takes care of it
                try (InputStream in = archiver.getContentStream(file)) {
                    StreamUtils.copyStream(in, archiver.createEntry(entryPath.getKey(), file));
                }
            }

            archiver.close();
        }

        return archivePath;
    }
}
