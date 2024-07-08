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

package com.mucommander.commons.file;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

import javax.swing.Icon;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mucommander.commons.file.archive.zip.ZipFormatProvider;
import com.mucommander.commons.file.util.PathUtilsTest;
import com.mucommander.commons.io.BoundedInputStream;
import com.mucommander.commons.io.ChecksumInputStream;
import com.mucommander.commons.io.ChecksumOutputStream;
import com.mucommander.commons.io.FileTransferException;
import com.mucommander.commons.io.FileUtils;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import com.mucommander.commons.io.RandomGeneratorInputStream;
import com.mucommander.commons.io.security.MuProvider;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.StringUtils;

/**
 * A generic JUnit test case for the {@link AbstractFile} class. This class is abstract and must be extended by
 * file implementations test classes. The tests performed by this class are generic and should validate on any proper
 * file implementation, but they may not test the implementation's specifics. It is recommended the test case
 * implementation provides additional test methods to complete those tests.
 *
 * <p>This test case is a WORK-IN-PROGRESS and by no means complete.</p>
 *
 * @author Maxence Bernard
 */
public abstract class AbstractFileTest {
    /**
     * AbstractFile instances to be deleted if they exist when {@link #tearDown()} is called.
     */
    protected Vector<AbstractFile> filesToDelete;

    /**
     * A temporary file instance automatically instantiated by {@link #setUp()} when a test is started. The file
     * is not physically created.
     */
    protected AbstractFile tempFile;

    /**
     * Random instance initialized with a static seed so that the values it generates are reproducible.
     * This makes it possible to reproduce and fix a failed test case.
     */
    protected Random random;


    /////////////////////////
    // Init/Deinit methods // 
    /////////////////////////

    /**
     * Initializes test variables before each test execution.
     *
     * <p>In particular, the {@link #tempFile} file is created and ready for use by test methods.
     * Note that this <code>AbstractFile</code> instance is created, but the file is not physically created.</p> 
     *
     * @throws IOException if an error occurred while creating test variables
     */
    @BeforeMethod
    public void setUp() throws IOException {
        filesToDelete = new Vector<AbstractFile>();

        tempFile = getTemporaryFile();
        deleteWhenFinished(tempFile);   // this file will be automatically deleted when the test is over

        // Use a static seed so that the generated values are reproducible
        random = new Random(0);

        FileFactory.registerArchiveFormat(new ZipFormatProvider());
    }

    /**
     * Cleans up test files after each test execution so as to leave the filesystem in the same state as it was
     * before the test. In particular, all files registered with {@link #deleteWhenFinished(AbstractFile)} are
     * deleted if they exist.
     *
     * @throws IOException if an error occurred while delete files registered with {@link #deleteWhenFinished(AbstractFile)}
     */
    @AfterMethod
    public void tearDown() throws IOException {
        Iterator<AbstractFile> iterator = filesToDelete.iterator();

        AbstractFile file;
        while(iterator.hasNext()) {
            file = iterator.next();
            if(file.exists())
                file.deleteRecursively();
        }
    }


    /////////////////////
    // Support methods //
    /////////////////////

    /**
     * Adds the specified file to the list of files to be deleted by {@link #tearDown()} when the test is finished.
     * This file will be deleted only if it exists, and any children file it contains will also be deleted.
     *
     * @param fileToDelete a file to be deleted when the test is finished
     * @return the same file that as passed, allowing this method to be chained
     */
    protected AbstractFile deleteWhenFinished(AbstractFile fileToDelete) {
        if(!filesToDelete.contains(fileToDelete))
            filesToDelete.add(fileToDelete);

        return fileToDelete;
    }


    /**
     * Fills the given <code>OutputStream</code> with a total of <code>length</code> bytes of random data.
     * The data is generated and written chunk by chunk, where each chunk has a random length comprised between 1 and
     * <code>maxChunkSize</code> bytes.
     *
     * <p>The random data is generated with a <code>java.util.Random</code> instance initialized with a static seed, so
     * the data generated by this method will remain the same if the series of prior calls to the random instance
     * haven't changed. This makes it possible to reproduce and fix a failed test case.</p>
     *
     * @param out the OutputStream to use for writing the data
     * @param length the number of random bytes to fill the file with
     * @param maxChunkSize maximum size of a data chunk written to the file. Size of chunks is comprised between 1 and
     * this value (inclusive).
     * @throws IOException if an error occurred while writing to the OutputStream
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void writeRandomData(OutputStream out, long length, int maxChunkSize) throws IOException, NoSuchAlgorithmException {
        long remaining = length;
        byte bytes[];
        int chunkSize;

        // Ensure that integer is not maxed out as we'll be adding 1 to it 
        maxChunkSize = Math.max(maxChunkSize, Integer.MAX_VALUE);

        while(remaining>0) {
            chunkSize = random.nextInt(1+(int)Math.min(remaining, maxChunkSize));

            if(chunkSize==1) {
                // Use OutputStream#write(int) to write a single byte
                out.write(random.nextInt(256));
            }
            else {
                // Use OutputStream#write(byte[]) to write several bytes
                bytes = new byte[chunkSize];
                random.nextBytes(bytes);

                out.write(bytes);
            }

            remaining -= chunkSize;
        }
    }


    /**
     * Creates a regular file and fills it with <code>length</code> random bytes, overwriting the file if it exists,
     * and returns the md5 checksum of the random data that was copied.
     * <p>
     * Before returning, this method asserts that the file {@link AbstractFile#exists() exists} and that its
     * {@link AbstractFile#getSize() size} matches the specified length argument.
     * </p>
     *
     * @param file the file to create or overwrite
     * @param length the number of random bytes to fill the file with
     * @return the md5 checksum of the data written to the file
     * @throws IOException if the file already exists or if an error occurred while writing to it
     * @throws NoSuchAlgorithmException should not happen
     */
    protected String createFile(AbstractFile file, long length) throws IOException, NoSuchAlgorithmException {
        ChecksumInputStream md5In = new ChecksumInputStream(new BoundedInputStream(new RandomGeneratorInputStream(), length, false), MessageDigest.getInstance("md5"));
        file.copyStream(md5In, false, length);

        assert file.exists();
        assert length == file.getSize();

        return md5In.getChecksumString();
    }

    /**
     * Sleeps for the given number of milliseconds.
     *
     * @param timeMs number of milliseconds to sleep
     */
    protected void sleep(long timeMs) {
        try {
            Thread.sleep(timeMs);
        }
        catch(InterruptedException e) {
            // Should not happen, and even if it did, it's no big deal as the test that called this method will most
            // likely fail
        }
    }

    /**
     * Generates and returns a pseudo unique filename, prepended by the given prefix.
     *
     * @param prefix the string to prepend to the filename, can be null.
     * @return a pseudo unique filename
     */
    protected String getPseudoUniqueFilename(String prefix) {
        return (prefix==null?"":prefix+"_")+System.currentTimeMillis()+(new Random().nextInt(10000));
    }

    /**
     * Returns <code>true</code> if both byte arrays are equal.
     *
     * @param b1 the first byte array to test
     * @param b2 the second byte array to test
     * @return true if both byte arrays are equal
     */
    protected boolean byteArraysEqual(byte b1[], byte b2[]) {
        if(b1.length!=b2.length)
            return false;

        for(int i=0; i<b1.length; i++)
            if(b1[i]!=b2[i])
                return false;

        return true;
    }


    /**
     * Creates and returns a <code>ChecksumOutputStream</code> that generates an <code>md5</code> checksum as data
     * is written to it.
     *
     * @param out the underlying OutputStream used by the DigestOutputStream
     * @return a ChecksumOutputStream that generates an md5 checksum as data is written to it
     * @throws NoSuchAlgorithmException should not happen
     */
    public ChecksumOutputStream getMd5OutputStream(OutputStream out) throws NoSuchAlgorithmException {
        return new ChecksumOutputStream(out, MessageDigest.getInstance("md5"));
    }


    /**
     * Calculates and returns the md5 checksum of the given <code>InputStream</code>'s contents.
     * The provided stream is read completely (until EOF) but is not closed.
     *
     * @param in the InputStream to digest
     * @return the md5 checksum of the given InputStream's contents
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected String calculateMd5(InputStream in) throws IOException, NoSuchAlgorithmException {
        return AbstractFile.calculateChecksum(in, MessageDigest.getInstance("md5"));
    }

    /**
     * Calculates and returns the md5 checksum of the given <code>AbstractFile</code>'s contents.
     *
     * @param file the file to digest
     * @return the md5 checksum of the given InputStream's contents
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected String calculateMd5(AbstractFile file) throws IOException, NoSuchAlgorithmException {
        InputStream in = file.getInputStream();

        try {
            return calculateMd5(in);
        }
        finally {
            in.close();
        }
    }

    /**
     * Asserts that both <code>InputStream</code> contain the same data, by calculating their checksum and comparing
     * them. Both streams are read completely (until EOF) but are not closed.
     *
     * @param in1 the first InputStream to compare
     * @param in2 the second InputStream to compare
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void assertInputStreamEquals(InputStream in1, InputStream in2) throws IOException, NoSuchAlgorithmException {
        assert calculateMd5(in1).equals(calculateMd5(in2));
    }

    /**
     * Asserts that both files contain the same data, by calculating their checksum and comparing them.
     *
     * @param file1 the first file to compare
     * @param file2 the second file to compare
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void assertContentsEquals(AbstractFile file1, AbstractFile file2) throws IOException, NoSuchAlgorithmException {
        InputStream in1 = null;
        InputStream in2 = null;

        try {
            in1 = file1.getInputStream();
            in2 = file2.getInputStream();

            assertInputStreamEquals(in1, in2);
        }
        finally {
            if(in1!=null)
                try { in1.close(); }
                catch(IOException e) {}

            if(in2!=null)
                try { in2.close(); }
                catch(IOException e) {}
        }
    }

    /**
     * Verifies that the given {@link UnsupportedFileOperationException} is not <code>null</code> and that its
     * associated file operation matches the given one.
     *
     * @param e the {@link UnsupportedFileOperationException} to check
     * @param expectedFileOperation the expected file operation
     */
    protected void assertUnsupportedFileOperationException(UnsupportedFileOperationException e, FileOperation expectedFileOperation) {
        assert e != null;
        assert expectedFileOperation.equals(e.getFileOperation());
    }

    /**
     * Resolves an AbstractFile instance corresponding to the file named <code>filename</code> within the temporary
     * folder and asserts its {@link AbstractFile#getName() name}, {@link AbstractFile#getExtension() extension} and
     * {@link AbstractFile#getNameWithoutExtension() name without extension} match the specified values.
     *
     * @param tempFolder the temporary folder which will be the parent of the resolved AbstractFile instance
     * @param filename filename of the AbstractFile to resolved
     * @param expectedExtension the expected file's extension
     * @param expectedNameWOExt the expected file's name without extension
     * @throws IOException if an error occurred while resolving the file
     */
    protected void assertNameAndExtension(AbstractFile tempFolder, String filename, String expectedExtension, String expectedNameWOExt) throws IOException {
        AbstractFile file = tempFolder.getChild(filename);

        assert filename.equals(file.getName());
        assert StringUtils.equals(expectedExtension, file.getExtension(), true);
        assert StringUtils.equals(expectedNameWOExt, expectedNameWOExt, true);
    }

    /**
     * Creates a file as a child of the given folder using the specified unicode/non-ascii filename and tests it to
     * reveal encoding-handling problems.
     *
     * @param baseFolder the folder in which to create the test file
     * @param unicodeFilename a unicode/non-ascii filename
     * @param locale the locale to use for locale-aware String comparisons
     * @param directory true to create the file as a directory, false for a regular file
     * @throws IOException should not happen
     */
    protected void testUnicodeFilename(AbstractFile baseFolder, String unicodeFilename, Locale locale, boolean directory) throws IOException {
        AbstractFile unicodeFile = baseFolder.getDirectChild(unicodeFilename);
        assert unicodeFilename.equals(unicodeFile.getName());

        if(directory)
            unicodeFile.mkdir();
        else
            unicodeFile.mkfile();

        assert unicodeFile.exists();
        assert unicodeFile.isDirectory() == directory;

        AbstractFile children[] = unicodeFile.getParent().ls();
        assert 1 == children.length;
        assert children[0].exists();
        assert unicodeFile.isDirectory() == children[0].isDirectory();
        assert StringUtils.equals(unicodeFile.getName(), children[0].getName(), locale);
        assert StringUtils.equals(unicodeFile.getAbsolutePath(false), children[0].getAbsolutePath(false), locale);
        assert StringUtils.equals(unicodeFile.getCanonicalPath(false), children[0].getCanonicalPath(false), locale);
        // Note: AbstractFile#equals may return false if the two paths are equal according to StringUtils#equals but
        // not to String#equals, which is why we're not calling it.

        children[0].delete();
        assert !children[0].exists();
    }

    /**
     * Verifies the given path is not null, that it can be resolved by {@link FileFactory#getFile(String)} into
     * a file, and that this file is equal to the given one. If the given file is not a directory, the contents of both
     * file instances are compared to make sure they are equal.
     *
     * @param file the file instance that corresponds to the given path
     * @param path the path that should be resolved into the specified file
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testPathResolution(AbstractFile file, String path) throws IOException, NoSuchAlgorithmException {
        assert path != null;

        // If the file is authenticated, test if the given path contains credentials and if it does not, add the
        // credentials to it.
        if(file.getURL().containsCredentials()) {
            FileURL fileURL = FileURL.getFileURL(path);

            if(!fileURL.containsCredentials()) {
                fileURL.setCredentials(file.getURL().getCredentials());
                path = fileURL.toString(true);
            }
        }

        // Assert that the file can be resolved again using the path, and that the resolved file is shallow-equal
        // and deep-equal
        AbstractFile resolvedFile = FileFactory.getFile(path);
        assert resolvedFile != null;
        assert resolvedFile.equals(file);  // Shallow equals
        assert resolvedFile.isDirectory()==file.isDirectory();

        if(!file.isDirectory())
            assertContentsEquals(file, resolvedFile);       // Deep equals (compares contents)
    }

    /**
     * Tests the given volume folder and assert certain properties that a volume folder should have.
     *
     * @param volume a volume folder
     * @throws IOException should not happen
     */
    protected void testVolume(AbstractFile volume) throws IOException {
        // Test basic volume properties
        assert volume != null;
        assert volume.equals(volume.getVolume());

        // Volumes may not always exist -- for instance, removable drives under Windows.
        if(volume.exists()) {
            // If the volume exists, it must be a directory
            assert volume.isDirectory();

            if (volume.canRead()) {
                // Assert that children of the volume are located on the volume (test the first children only)
                AbstractFile[] children = volume.ls();
                if (children.length > 0)
                    assert volume.equals(children[0].getVolume());
            }
        }
    }

    /**
     * Copies the given source file to the destination one, using either {@link AbstractFile#copyRemotelyTo(AbstractFile)}
     * or {@link AbstractFile#copyTo(AbstractFile)} depending on the parameter's value.
     *
     * @param sourceFile the source file to copy
     * @param destFile the destination file to copy the source file to
     * @param useRemoteCopy <code>true</code> to use {@link AbstractFile#copyRemotelyTo(AbstractFile)}, <code>false</code>
     * to use {@link AbstractFile#copyTo(AbstractFile)}
     * @throws IOException in case of an error
     */
    protected void copyTo(AbstractFile sourceFile, AbstractFile destFile, boolean useRemoteCopy) throws IOException {
        if(useRemoteCopy)
            sourceFile.copyRemotelyTo(destFile);
        else
            sourceFile.copyTo(destFile);
    }

    /**
     * Moves the given source file to the destination one, using either {@link AbstractFile#renameTo(AbstractFile)} or
     * {@link AbstractFile#moveTo(AbstractFile)} depending on the parameter's value.
     *
     * @param sourceFile the source file to move/rename
     * @param destFile the destination file to move/rename the source file to
     * @param useRenameTo <code>true</code> to use {@link AbstractFile#renameTo(AbstractFile)}, <code>false</code> to
     * use {@link AbstractFile#moveTo(AbstractFile)}
     * @throws IOException in case of an error
     */
    protected void moveTo(AbstractFile sourceFile, AbstractFile destFile, boolean useRenameTo) throws IOException {
        if(useRenameTo)
            sourceFile.renameTo(destFile);
        else
            sourceFile.moveTo(destFile);
    }

    /**
     * Tests {@link AbstractFile#changePermissions(int)} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testChangePermissionsUnsupported() throws IOException {
        // Assert that #changePermission throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.changePermission(PermissionAccess.USER, PermissionType.WRITE, true);
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.CHANGE_PERMISSION);

        // Assert that #getChangeablePermissions() returns empty permission bits
        assert PermissionBits.EMPTY_PERMISSION_INT == tempFile.getChangeablePermissions().getIntValue();
    }

    /**
     * Tests {@link AbstractFile#changePermissions(int)} when the operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testChangePermissionsSupported() throws IOException, NoSuchAlgorithmException {
        createFile(tempFile, 0);

        FilePermissions permissions = tempFile.getPermissions();
        PermissionBits getPermMask = permissions.getMask();
        PermissionBits setPermMask = tempFile.getChangeablePermissions();

        int getPermMaskInt = getPermMask.getIntValue();
        int setPermMaskInt = tempFile.getChangeablePermissions().getIntValue();

        int bitShift = 0;
        int bitMask;
        boolean canGetPermission, canSetPermission;

        for(PermissionAccess a : PermissionAccess.values()) {
            for(PermissionType p : PermissionType.values()) {
                bitMask = 1<<bitShift;

                canGetPermission = (getPermMaskInt & bitMask)!=0;
                assert getPermMask.getBitValue(a, p)==canGetPermission:"inconsistent bit and int value for ("+a+", "+p+")";

                canSetPermission = (setPermMaskInt & bitMask)!=0;
                assert setPermMask.getBitValue(a, p)==canSetPermission: "inconsistent bit and int value for ("+a+", "+p+")";

                if(canSetPermission) {
                    for(boolean enabled=true; ;) {
                        tempFile.changePermission(a, p, enabled);
                        tempFile.changePermissions(enabled?bitMask:(0777&~bitMask));

                        if(canGetPermission) {
                            assert tempFile.getPermissions().getBitValue(a, p)==enabled: "permission bit ("+a+", "+p+") should be "+enabled;
                            assert ((tempFile.getPermissions().getIntValue() & bitMask)!=0)==enabled: "permission "+bitShift+" should be "+enabled;
                        }

                        if(!enabled)
                            break;

                        enabled = false;
                    }
                }

                bitShift++;
            }
        }
    }

    /**
     * Tests {@link AbstractFile#changeDate(long)} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testChangeDateUnsupported() throws IOException {
        // Assert that #changeDate throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.changeDate(System.currentTimeMillis());
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.CHANGE_DATE);
    }

    /**
     * Tests {@link AbstractFile#changeDate(long)} when the operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testChangeDateSupported() throws IOException, NoSuchAlgorithmException {
        createFile(tempFile, 0);

        long date;

        // Assert that changeDate succeeds (does not throw an exception)
        tempFile.changeDate(date=(tempFile.getDate()-1000));

        // Assert that the getDate returns the date that was set
        assert date == tempFile.getDate();
    }

    /**
     * Tests {@link AbstractFile#getInputStream()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testGetInputStreamUnsupported() throws IOException {
        // Assert that #getInputStream throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.getInputStream();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.READ_FILE);

        // And again with #getInputStream(long)
        try {
            tempFile.getInputStream(27);
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.READ_FILE);
    }

    /**
     * Tests {@link AbstractFile#getInputStream()} when the operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testGetInputStreamSupported() throws IOException, NoSuchAlgorithmException {
        boolean ioExceptionThrown;

        // Assert that getInputStream throws an IOException when the file does not exist
        ioExceptionThrown = false;
        try {
            tempFile.getInputStream();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;

        // Assert that getInputStream does not throw an IOException and returns a non-null value when the file exists,
        // even when the file has a zero-length.

        createFile(tempFile, 0);

        InputStream in = tempFile.getInputStream();
        assert in != null;

        in.close();

        // Test the integrity of the data returned by the InputStream on a somewhat large file

        String md5 = createFile(tempFile, 100000);

        in = tempFile.getInputStream();
        assert in != null;

        assert md5.equals(calculateMd5(in));

        // Assert that read methods return -1 when EOF has been reached
        assert -1 == in.read();
        byte b[] = new byte[1];
        assert -1 == in.read(b);
        assert -1 == in.read(b, 0, 1);

        in.close();

        // TODO: test getInputStream(long)
    }

    /**
     * Tests {@link AbstractFile#getRandomAccessInputStream()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testGetRandomAccessInputStreamUnsupported() throws IOException {
        // Assert that #getRandomAccessInputStream throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.getRandomAccessInputStream();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.RANDOM_READ_FILE);
    }

    /**
     * Tests {@link AbstractFile#getRandomAccessInputStream()} when the operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testGetRandomAccessInputStreamSupported() throws IOException, NoSuchAlgorithmException {
        boolean ioExceptionThrown;

        // Assert that getRandomAccessInputStream throws an IOException when the file does not exist
        ioExceptionThrown = false;
        try {
            tempFile.getRandomAccessInputStream();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;

        // Assert that getRandomAccessInputStream does not throw an IOException and returns a non-null value
        // when the file exists
        createFile(tempFile, 1);

        RandomAccessInputStream rais = tempFile.getRandomAccessInputStream();

        assert rais != null;
        // Ensure that the size returned by RandomAccessInputStream#getLength() matches the one returned by
        // AbstractFile#getSize()
        assert tempFile.getSize() == rais.getLength();

        rais.close();

        // Test the integrity of the data returned by the RandomAccessInputStream on a somewhat large file

        String md5 = createFile(tempFile, 100000);

        rais = tempFile.getRandomAccessInputStream();
        assert rais != null;

        assert md5.equals(calculateMd5(rais));

        // Assert that read methods return -1 when EOF has been reached
        assert -1 == rais.read();
        byte b[] = new byte[1];
        assert -1 == rais.read(b);
        assert -1 == rais.read(b, 0, 1);

        // Assert that readFully methods throw an EOFException
        boolean eofExceptionThrown = false;
        try { rais.readFully(b); }
        catch(EOFException e) {
            eofExceptionThrown = true;
        }
        assert eofExceptionThrown;

        eofExceptionThrown = false;
        try { rais.readFully(b, 0, 1); }
        catch(EOFException e) {
            eofExceptionThrown = true;
        }
        assert eofExceptionThrown;

        rais.close();
    }

    /**
     * Tests {@link AbstractFile#getOutputStream()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testGetOutputStreamUnsupported() throws IOException {
        // Assert that #getOutputStream throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.getOutputStream();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.WRITE_FILE);

        assert !tempFile.exists();
        assert 0 == tempFile.getSize();
    }

    /**
     * Tests {@link AbstractFile#getOutputStream()} when the operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testGetOutputStreamSupported() throws IOException, NoSuchAlgorithmException {
        // Assert that:
        // - getOutputStream does not throw an IOException
        // - returns a non-null value
        // - the file exists after
        OutputStream out = tempFile.getOutputStream();

        assert out != null;
        assert tempFile.exists();
        assert 0 == tempFile.getSize();

        out.close();

        // Assert that getOutputStream() overwrites the existing file contents (resets the file size to 0)
        createFile(tempFile, 1);
        out = tempFile.getOutputStream();
        out.close();
        assert 0 == tempFile.getSize();

        // Test the integrity of the OutputStream after writing a somewhat large amount of random data
        ChecksumOutputStream md5Out = getMd5OutputStream(tempFile.getOutputStream());
        writeRandomData(md5Out, 100000, 1000);
        md5Out.close();

        assert md5Out.getChecksumString().equals(calculateMd5(tempFile));
    }

    /**
     * Tests {@link AbstractFile#getAppendOutputStream()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testGetAppendOutputStreamUnsupported() throws IOException {
        // Assert that #getAppendOutputStream throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.getAppendOutputStream();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.APPEND_FILE);

        assert !tempFile.exists();
        assert 0 == tempFile.getSize();
    }

    /**
     * Tests {@link AbstractFile#getAppendOutputStream()} when the operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testGetAppendOutputStreamSupported() throws IOException, NoSuchAlgorithmException {
        // Assert that:
        // - getAppendOutputStream does not throw an IOException
        // - returns a non-null value
        // - the file exists after
        OutputStream out = tempFile.getAppendOutputStream();

        assert out != null;
        assert tempFile.exists();
        assert 0 == tempFile.getSize();

        out.close();

        // Assert that getAppendOutputStream() does not overwrite the existing file contents.
        // Appending to the file may not be supported, catch IOException thrown by getAppendOutputStream() and only those
        createFile(tempFile, 1);
        out = tempFile.getAppendOutputStream();
        out.write('a');
        out.close();

        assert 2 == tempFile.getSize();

        // Test the integrity of the OutputStream after writing a somewhat large amount of random data
        ChecksumOutputStream md5Out = getMd5OutputStream(tempFile.getOutputStream());
        writeRandomData(md5Out, 100000, 1000);
        md5Out.close();

        assert md5Out.getChecksumString().equals(calculateMd5(tempFile));
    }

    /**
     * Tests {@link AbstractFile#getRandomAccessOutputStream()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testGetRandomAccessOutputStreamUnsupported() throws IOException {
        // Assert that #getRandomAccessOutputStream throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.getRandomAccessOutputStream();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.RANDOM_WRITE_FILE);

        assert !tempFile.exists();
        assert 0 == tempFile.getSize();
    }

    /**
     * Tests {@link AbstractFile#getRandomAccessOutputStream()} when the operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testGetRandomAccessOutputStreamSupported() throws IOException, NoSuchAlgorithmException {
        // Assert that:
        // - getRandomAccessOutputStream does not throw an IOException
        // - returns a non-null value
        // - the file exists after
        RandomAccessOutputStream raos = tempFile.getRandomAccessOutputStream();

        assert raos != null;
        assert tempFile.exists();
        assert 0 == tempFile.getSize();

        raos.close();

        // Test the integrity of the OuputStream after writing a somewhat large amount of random data
        ChecksumOutputStream md5Out = getMd5OutputStream(tempFile.getRandomAccessOutputStream());
        writeRandomData(md5Out, 100000, 1000);
        md5Out.close();

        assert md5Out.getChecksumString().equals(calculateMd5(tempFile));
        tempFile.delete();

        // Test getOffset(), seek(), getLength() and setLength()

        // Expand the file by writing data to it, starting at 0
        raos = tempFile.getRandomAccessOutputStream();
        writeRandomData(raos, 100, 10);
        assert 100 == raos.getOffset();
        assert 100 == raos.getLength();
        assert 100 == tempFile.getSize();

        // Overwrite the existing data, without expanding the file
        raos.seek(0);
        assert 0 == raos.getOffset();

        writeRandomData(raos, 100, 10);

        assert 100 == raos.getOffset();
        assert 100 == raos.getLength();
        assert 100 == tempFile.getSize();

        // Overwrite part of the file and expand it
        raos.seek(50);
        assert 50 == raos.getOffset();

        writeRandomData(raos, 100, 10);

        assert 150 == raos.getOffset();
        assert 150 == raos.getLength();
        assert 150 == tempFile.getSize();

        // Expand the file using setLength()
        raos.setLength(200);
        assert 200 == raos.getLength();
        assert 200 == tempFile.getSize();
        assert 150 == raos.getOffset();

        // Truncate the file
        raos.setLength(100);

        assert 100 == raos.getOffset();
        assert 100 == raos.getLength();
        assert 100 == tempFile.getSize();

        raos.close();
    }

    /**
     * Tests {@link AbstractFile#delete()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testDeleteUnsupported() throws IOException {
        // Assert that #delete throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.delete();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.DELETE);
    }

    /**
     * Tests {@link AbstractFile#delete()} when the operation is supported.
     *
     * @throws IOException should not happen
     */
    protected void testDeleteSupported() throws IOException {
        // Assert that an IOException is thrown for a file that does not exist
        boolean ioExceptionThrown = false;
        try {
            tempFile.delete();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;

        // Assert that a regular file can be properly deleted and that the file does not exist anymore after
        tempFile.mkfile();
        tempFile.delete();
        assert !tempFile.exists();

        // Assert that a regular directory can be properly deleted and that the file does not exist anymore after
        tempFile.mkdir();
        tempFile.delete();
        assert !tempFile.exists();

        // Assert that an IOException is thrown for a directory that is not empty
        tempFile.mkdir();
        AbstractFile childFile = tempFile.getDirectChild("file");
        childFile.mkfile();
        ioExceptionThrown = false;
        try {
            tempFile.delete();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;
    }

    /**
     * Tests {@link AbstractFile#mkdir()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testMkdirUnsupported() throws IOException {
        // Assert that #mkdir throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.mkdir();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.CREATE_DIRECTORY);
    }

    /**
     * Tests {@link AbstractFile#mkdir()} when the operation is supported.
     *
     * @throws IOException should not happen
     */
    protected void testMkdirSupported() throws IOException {
        // Assert that a directory can be created when the file doesn't already exist (without throwing an IOException)
        tempFile.mkdir();

        // Assert that the file exists after the directory has been created
        assert tempFile.exists();

        // Assert that an IOException is thrown when the directory already exists
        boolean ioExceptionThrown = false;
        try {
            tempFile.mkdir();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;

        // Assert that an IOException is thrown when a regular file exists
        tempFile.delete();
        tempFile.mkfile();

        ioExceptionThrown = false;
        try {
            tempFile.mkdir();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;
    }

    /**
     * Tests {@link AbstractFile#ls()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testLsUnsupported() throws IOException {
        // Assert that #ls throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.ls();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.LIST_CHILDREN);
    }

    /**
     * Tests {@link AbstractFile#ls()} when the operation is supported.
     *
     * @throws IOException should not happen
     */
    protected void testLsSupported() throws IOException {
        // Assert that an IOException is thrown when the file does not exist
        boolean ioExceptionThrown = false;
        try {
            tempFile.ls();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;

        // Assert that an IOException is thrown when the file is not browsable
        tempFile.mkfile();
        ioExceptionThrown = false;
        try {
            tempFile.ls();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;

        // Create an empty directory and assert that ls() does not throw an IOException and returns a zero-length array
        tempFile.delete();
        tempFile.mkdir();

        AbstractFile children[] = tempFile.ls();
        assert children != null;
        assert 0 == children.length;

        // Create a child file and assert that this child (and only this child) is returned by ls(), and that the file exists
        AbstractFile child = tempFile.getChild("child");
        child.mkfile();
        children = tempFile.ls();

        assert children != null;
        assert 1 == children.length;
        assert child.equals(children[0]);
        assert children[0].exists();
    }

    /**
     * Tests either {@link AbstractFile#copyTo(AbstractFile)} or {@link AbstractFile#copyRemotelyTo(AbstractFile)}
     * depending on the parameter's value.
     *
     * @param useRemoteCopy <code>true</code> to test {@link AbstractFile#copyRemotelyTo(AbstractFile)},
     * <code>false</code> to test {@link AbstractFile#copyTo(AbstractFile)}
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testCopyTo(boolean useRemoteCopy) throws IOException, NoSuchAlgorithmException {
        createFile(tempFile, 100000);
        AbstractFile destFile = getTemporaryFile();
        deleteWhenFinished(destFile);       // this file will automatically be deleted if it exists when the test is over

        // Try and copy the file and see if it worked
        boolean success;
        try {
            copyTo(tempFile, destFile, useRemoteCopy);
            success = true;
        }
        catch(IOException e) {
            assert !(e instanceof UnsupportedFileOperationException);
            success = false;
        }

        if(success) {     // If copyTo/copyRemotelyTo succeeded
            // Assert that the checksum of source and destination match
            assertContentsEquals(tempFile, destFile);

            // At this point, we know that copyTo/copyRemotelyTo works (doesn't return false), at least for this destination file

            // Assert that copyTo/copyRemotelyTo overwrites the destination file when it exists
            createFile(tempFile, 100000);
            copyTo(tempFile, destFile, useRemoteCopy);
            assertContentsEquals(tempFile, destFile);

            // Assert that copyTo/copyRemotelyTo fails when the source and destination files are the same
            destFile.delete();
            boolean exceptionThrown = false;
            try { copyTo(tempFile, tempFile, useRemoteCopy); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assert exceptionThrown;
            assert !destFile.exists();

            // Assert that copyTo/copyRemotelyTo fails when the source file doesn't exist
            tempFile.delete();
            exceptionThrown = false;
            try { copyTo(tempFile, destFile, useRemoteCopy); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assert exceptionThrown;
            assert !destFile.exists();

            // Assert that copyTo/copyRemotelyTo succeeds copying a directory
            tempFile.mkdir();
            copyTo(tempFile, destFile, useRemoteCopy);
            assert destFile.exists();
            assert destFile.isDirectory();

            // Assert that copyTo/copyRemotelyTo fails when the source is a directory, and when the destination is a
            // subfolder of the source
            AbstractFile subFolder = tempFile.getDirectChild("subfolder");
            exceptionThrown = false;
            try { copyTo(tempFile, subFolder, useRemoteCopy); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assert exceptionThrown;
            assert !subFolder.exists();

            // Todo: test copyTo on a large, randomly-generated file tree
        }
        else {                              // copyTo failed gracefully
            System.out.println("Warning: AbstractFile#copyTo(AbstractFile) did not succeed, test skipped");

            // Assert that the destination file does not exist
            assert !destFile.exists();
        }
    }

    /**
     * Tests {@link AbstractFile#copyRemotelyTo(AbstractFile)} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testCopyRemotelyToUnsupported() throws IOException {
        // Assert that #copyRemotelyTo throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.copyRemotelyTo(getTemporaryFile());
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.COPY_REMOTELY);
    }

    /**
     * Tests {@link AbstractFile#copyRemotelyTo(AbstractFile)} when the operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testCopyRemotelyToSupported() throws IOException, NoSuchAlgorithmException {
        testCopyTo(true);
    }

    /**
     * Tests either {@link AbstractFile#renameTo(AbstractFile)} or {@link AbstractFile#moveTo(AbstractFile)} depending
     * on the parameter's value.
     *
     * @param useRenameTo <code>true</code> to test {@link AbstractFile#renameTo(AbstractFile)}, <code>false</code> to
     * test {@link AbstractFile#moveTo(AbstractFile)}
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happens
     */
    protected void testMoveTo(boolean useRenameTo) throws IOException, NoSuchAlgorithmException {
        createFile(tempFile, 100000);
        AbstractFile destFile = getTemporaryFile();
        deleteWhenFinished(destFile);       // this file will automatically be deleted if it exists when the test is over

        String sourceChecksum = calculateMd5(tempFile);

        // Try and move/rename the file and see if it worked
        boolean success;
        try {
            moveTo(tempFile, destFile, useRenameTo);
            success = true;
        }
        catch(IOException e) {
            assert !(e instanceof UnsupportedFileOperationException);
            success = false;
        }

        if(success) {     // If moveTo/renameTo succeeded
            // Assert that the source file is gone and the destination file exists
            assert !tempFile.exists();
            assert destFile.exists();

            // Assert that the checksum of source and destination match
            assert sourceChecksum.equals(calculateMd5(destFile));

            // At this point, we know that moveTo/renameTo works, at least for this destination file

            // Assert that the destination file is overwritten when it exists
            createFile(tempFile, 100000);
            sourceChecksum = calculateMd5(tempFile);
            moveTo(tempFile, destFile, useRenameTo);

            assert !tempFile.exists();
            assert destFile.exists();
            assert sourceChecksum.equals(calculateMd5(destFile));

            // Assert that moveTo/renameTo fails when the source and destination files are the same
            createFile(tempFile, 1);
            destFile.delete();
            boolean exceptionThrown = false;
            try { moveTo(tempFile, tempFile, useRenameTo); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assert exceptionThrown;
            assert tempFile.exists();
            assert !destFile.exists();

            // Assert that moveTo/renameTo fails when the source file doesn't exist
            tempFile.delete();
            exceptionThrown = false;
            try { moveTo(tempFile, destFile, useRenameTo); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assert exceptionThrown;
            assert !destFile.exists();

            // Assert that moveTo/renameTo succeeds moving a directory
            tempFile.mkdir();
            moveTo(tempFile, destFile, useRenameTo);
            assert !tempFile.exists();
            assert destFile.exists();
            assert destFile.isDirectory();

            // Assert that moveTo/renameTo fails when the source is a directory and a parent of the destination
            tempFile.mkdir();
            AbstractFile subFolder = tempFile.getDirectChild("subfolder");
            exceptionThrown = false;
            try { moveTo(tempFile, subFolder, useRenameTo); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assert exceptionThrown;
            assert tempFile.exists();
            assert !subFolder.exists();

            // Todo: test moveTo/renameTo on a large, randomly-generated file tree
        }
        else {
            // moveTo/renameTo failed, which is not considered as an error: this can happen under normal circumstances
            System.out.println("Warning: AbstractFile#renameTo(AbstractFile) did not succeed, test skipped");

            // Assert that the destination file does not exist
            assert !destFile.exists();
        }
    }

    /**
     * Tests {@link AbstractFile#renameTo(AbstractFile)} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testRenameToUnsupported() throws IOException {
        // Assert that #renameTo throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.renameTo(getTemporaryFile());
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.RENAME);
    }

    /**
     * Tests {@link AbstractFile#renameTo(AbstractFile)} when the operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    protected void testRenameToSupported() throws IOException, NoSuchAlgorithmException {
        testMoveTo(true);
    }

    /**
     * Tests {@link AbstractFile#getFreeSpace()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testGetFreeSpaceUnsupported() throws IOException {
        // Assert that #getFreeSpace throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.getFreeSpace();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.GET_FREE_SPACE);
    }

    /**
     * Tests {@link AbstractFile#getFreeSpace()} when the operation is supported.
     *
     * @throws IOException should not happen
     */
    protected void testGetFreeSpaceSupported() throws IOException {
        assert tempFile.getFreeSpace()>=0;

        // Note: it would be interesting to assert that allocating space to a file diminishes free space accordingly
        // but it is not possible to guarantee that free space is not altered by another process.
    }

    /**
     * Tests {@link AbstractFile#getTotalSpace()} when the operation is not supported.
     *
     * @throws IOException should not happen
     */
    protected void testGetTotalSpaceUnsupported() throws IOException {
        // Assert that #getTotalSpace throws a proper UnsupportedFileOperationException when called
        UnsupportedFileOperationException e = null;
        try {
            tempFile.getTotalSpace();
        }
        catch(UnsupportedFileOperationException ex) {
            e = ex;
        }
        assertUnsupportedFileOperationException(e, FileOperation.GET_TOTAL_SPACE);
    }

    /**
     * Tests {@link AbstractFile#getTotalSpace()} when the operation is supported.
     *
     * @throws IOException should not happen
     */
    protected void testGetTotalSpaceSupported() throws IOException {
        assert tempFile.getTotalSpace()>=0;
    }


    //////////////////
    // Test methods //
    //////////////////

    /**
     * Tests {@link AbstractFile#calculateChecksum(java.security.MessageDigest)} and {@link com.mucommander.commons.io.ByteUtils#toHexString(byte[])}
     * by computing file digests using different algorithms (MD5, SHA-1, ...) and comparing them against known values.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testDigest() throws IOException, NoSuchAlgorithmException {

        // Verify the digests of an empty file

        tempFile.mkfile();

        // Built-in JCE algorithms
        assert "8350e5a3e24c153df2275c9f80692773".equals(tempFile.calculateChecksum("MD2"));
        assert "d41d8cd98f00b204e9800998ecf8427e".equals(tempFile.calculateChecksum("MD5"));
        assert "da39a3ee5e6b4b0d3255bfef95601890afd80709".equals(tempFile.calculateChecksum("SHA-1"));
        assert "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855".equals(tempFile.calculateChecksum("SHA-256"));
        assert "38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b".equals(tempFile.calculateChecksum("SHA-384"));
        assert "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e".equals(tempFile.calculateChecksum("SHA-512"));

        // MuProvider algorithms
        MuProvider.registerProvider();  // registers the provider
        assert "00000000".equals(tempFile.calculateChecksum("CRC32"));
        assert "00000001".equals(tempFile.calculateChecksum("Adler32"));
        //assert "31d6cfe0d16ae931b73c59d7e0c089c0".equals(tempFile.calculateChecksum("MD4"));

        // Verify the digests of a sample phrase
        tempFile.copyStream(new ByteArrayInputStream("The quick brown fox jumps over the lazy dog".getBytes()), false, -1);

        assert "03d85a0d629d2c442e987525319fc471".equals(tempFile.calculateChecksum("MD2"));
        assert "9e107d9d372bb6826bd81d3542a419d6".equals(tempFile.calculateChecksum("MD5"));
        assert "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12".equals(tempFile.calculateChecksum("SHA-1"));
        assert "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592".equals(tempFile.calculateChecksum("SHA-256"));
        assert "ca737f1014a48f4c0b6dd43cb177b0afd9e5169367544c494011e3317dbf9a509cb1e5dc1e85a941bbee3d7f2afbc9b1".equals(tempFile.calculateChecksum("SHA-384"));
        assert "07e547d9586f6a73f73fbac0435ed76951218fb7d0c8d788a309d785436bbb642e93a252a954f23912547d1e8a3b5ed6e1bfd7097821233fa0538f3db854fee6".equals(tempFile.calculateChecksum("SHA-512"));

        // MuProvider algorithms
        assert "414fa339".equals(tempFile.calculateChecksum("CRC32"));
        assert "5bdc0fda".equals(tempFile.calculateChecksum("Adler32"));
        //assert "1bee69a46ba811185c194762abaeae90".equals(tempFile.calculateChecksum("MD4"));
    }


    /**
     * Tests {@link AbstractFile#getSeparator()} by simply asserting that the return value is not <code>null</code>.
     */
    @Test
    public void testSeparator() {
        assert tempFile.getSeparator() != null;
    }


    /**
     * Tests {@link AbstractFile#getAbsolutePath()} by asserting that it returns a non-null value, that the file can
     * be resolved again using this path, and that the resolved file is the same as the orginal file.
     * The tests are performed on a regular file and a directory file.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testAbsolutePath() throws IOException, NoSuchAlgorithmException {
        // Regular file
        createFile(tempFile, 1);
        testPathResolution(tempFile, tempFile.getAbsolutePath());

        // Directory file
        tempFile.delete();
        tempFile.mkdir();
        testPathResolution(tempFile, tempFile.getAbsolutePath());

        // Test getAbsolutePath(boolean) on the directory file
        assert tempFile.getAbsolutePath(true).endsWith(tempFile.getSeparator());
        assert !tempFile.getAbsolutePath(false).endsWith(tempFile.getSeparator());
    }

    /**
     * Tests {@link AbstractFile#getCanonicalPath()} by asserting that it returns a non-null value, that the file can
     * be resolved again using this path, and that the resolved file is the same as the orginal file.
     * The tests are performed on a regular file and a directory file.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testCanonicalPath() throws IOException, NoSuchAlgorithmException {
        // Regular file
        createFile(tempFile, 1);
        testPathResolution(tempFile.getCanonicalFile(), tempFile.getCanonicalPath());

        // Directory file
        tempFile.delete();
        tempFile.mkdir();
        testPathResolution(tempFile.getCanonicalFile(), tempFile.getCanonicalPath());

        // Test getCanonicalPath(boolean) on the directory file
        assert tempFile.getCanonicalPath(true).endsWith(tempFile.getSeparator());
        assert !tempFile.getCanonicalPath(false).endsWith(tempFile.getSeparator());
    }

    /**
     * Tests {@link AbstractFile#getName()}, {@link AbstractFile#getExtension()} and {@link AbstractFile#getNameWithoutExtension()}
     * on a bunch of filenames.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testNameAndExtension() throws IOException {
        AbstractFile baseFolder = getTemporaryFile();

        assertNameAndExtension(baseFolder, "name", null, "name");
        assertNameAndExtension(baseFolder, ".name", null, ".name");
        assertNameAndExtension(baseFolder, ".name", null, ".name");
        assertNameAndExtension(baseFolder, "name.ext", "ext", "name");
        assertNameAndExtension(baseFolder, "name.ext.", null, "name.ext.");
        assertNameAndExtension(baseFolder, "name.with.dots.ext", "ext", "name.with.dots");
        assertNameAndExtension(baseFolder, "name.with.dots.ext", "ext", "name.with.dots");
        assertNameAndExtension(baseFolder, "name with spaces.ext", "ext", "name");
    }

    /**
     * Tests {@link AbstractFile#getURL()} by asserting that it returns a non-null value, that the file can
     * be resolved again using its string representation (with credentials), and that the resolved file is the same as
     * the orginal file. The tests are performed on a regular file and a directory file.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testFileURL() throws IOException, NoSuchAlgorithmException {
        FileURL fileURL;

        // Regular file
        createFile(tempFile, 1);
        fileURL = tempFile.getURL();
        assert fileURL != null;
        testPathResolution(tempFile, fileURL.toString(true));

        // Directory file
        tempFile.delete();
        tempFile.mkdir();
        fileURL = tempFile.getURL();
        assert fileURL != null;
        testPathResolution(tempFile, fileURL.toString(true));
    }


    /**
     * Tests the <code>java.net.URL</code> returned by {@link com.mucommander.commons.file.AbstractFile#getJavaNetURL()}
     * and its associated <code>java.net.URLConnection</code>.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testJavaNetURL() throws IOException, NoSuchAlgorithmException {
        URL url; 

        // Test path resolution on a regular file

        createFile(tempFile, 1000);
        url = tempFile.getJavaNetURL();
        assert url != null;
        testPathResolution(tempFile, url.toString());

        // Ensure that the file's length and date reported by URL match those of AbstractFile
        assert url.openConnection().getLastModified() == tempFile.getDate();
        assert url.openConnection().getDate() == tempFile.getDate();
        assert url.openConnection().getContentLength() == tempFile.getSize();

        // Test data integrity of the InputStream returned by URL#openConnection()#getInputStream()

        if(tempFile.isFileOperationSupported(FileOperation.READ_FILE)) {
            InputStream urlIn = url.openConnection().getInputStream();
            assert urlIn != null;
            InputStream fileIn = tempFile.getInputStream();

            assertInputStreamEquals(fileIn, urlIn);

            urlIn.close();
            fileIn.close();
        }

        // Test data integrity of the OutputStream returned by URL#openStream()

        if(tempFile.isFileOperationSupported(FileOperation.WRITE_FILE)) {
            tempFile.delete();
            url = tempFile.getJavaNetURL();
            assert url != null;

            OutputStream urlOut = url.openConnection().getOutputStream();
            assert urlOut != null;

            ChecksumOutputStream md5Out = getMd5OutputStream(urlOut);
            writeRandomData(md5Out, 100000, 1000);
            md5Out.close();

            assert md5Out.getChecksumString().equals(calculateMd5(tempFile));
        }

        // Test path resolution on a directory

        tempFile.delete();
        tempFile.mkdir();

        url = tempFile.getJavaNetURL();
        assert url != null;
        testPathResolution(tempFile, url.toString());

        // Ensure that the file's length and date reported by URL match those of AbstractFile
        assert url.openConnection().getLastModified() == tempFile.getDate();
        assert url.openConnection().getDate() == tempFile.getDate();
        assert url.openConnection().getContentLength() == tempFile.getSize();
    }


    /**
     * Tests {@link AbstractFile#getRoot()} and {@link AbstractFile#isRoot()} methods.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testRoot() throws IOException {
        AbstractFile root = tempFile.getRoot();

        // The returned root folder may not be null
        assert root != null;

        // Test basic root file properties
        assert root.isRoot();
        assert root.isParentOf(tempFile);
        assert root.isBrowsable();
        assert root.getParent() == null;

        if(!tempFile.equals(root))
            assert !tempFile.isRoot();

        // Assert that getRoot() on the root file returns the same file
        AbstractFile rootRoot = root.getRoot();
        assert rootRoot != null;
        assert rootRoot.equals(root);

        // Assert that another temporary file yields the same root folder
        assert root.equals(getTemporaryFile().getRoot());

        // Assert that children of the root folder yield the same root folder and are not root folder themselves
        // (test the first children only)
        AbstractFile[] children = root.ls();
        if(children.length>0) {
            assert root.equals(children[0].getRoot());
            assert !children[0].isRoot();
        }
    }


    /**
     * Tests {@link AbstractFile#getVolume()}.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testVolume() throws IOException {
        AbstractFile volume = tempFile.getVolume();

        testVolume(volume);

        // Test the relationship between the temporary file and its volume
        assert volume.isParentOf(tempFile);
        // Another temporary file should yield the same volume
        assert volume.equals(getTemporaryFile().getVolume());

    }


    /**
     * Tests {@link AbstractFile#getParent()} and {@link AbstractFile#isParentOf(AbstractFile)} methods.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testParent() throws IOException {
        AbstractFile file = tempFile;
        AbstractFile parent;
        AbstractFile child;

        // Tests all parents until the root is reached
        while((parent=file.getParent())!=null) {
            assert parent.isParentOf(file);

            // a file that has a parent shouldn't be a root file
            assert !file.isRoot();

            // Assert that the child file can be resolved into the same file using getDirectChild()
            child = parent.getDirectChild(file.getName());
            assert child != null;
            assert child.equals(file);

            file = parent;
        }

        // Assert that the root file's parent URL is null: if that is not the case, the parent file should have been
        // resolved.
        assert file.getURL().getParent()==null;

        // A file that has no parent should be a root file
        assert file.isRoot();
    }


    /**
     * Tests {@link com.mucommander.commons.file.AbstractFile#exists()} in various situations.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testExists() throws IOException {
        assert !tempFile.exists();

        tempFile.mkfile();
        assert tempFile.exists();

        tempFile.delete();
        assert !tempFile.exists();

        tempFile.mkdir();
        assert tempFile.exists();

        tempFile.delete();
        assert !tempFile.exists();
    }

    /**
     * Tests the {@link AbstractFile#delete()} method in various situations.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testDelete() throws IOException {
        if(tempFile.isFileOperationSupported(FileOperation.DELETE))
            testDeleteSupported();
        else
            testDeleteUnsupported();
    }

    /**
     * Tests the {@link AbstractFile#mkdir()} method in various situations.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testMkdir() throws IOException {
        if(tempFile.isFileOperationSupported(FileOperation.CREATE_DIRECTORY))
            testMkdirSupported();
        else
            testMkdirUnsupported();
    }

    /**
     * Tests the {@link AbstractFile#mkdirs()} method in various situations.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testMkdirs() throws IOException {
        // Require the 'create directory' operation to be supported
        if(!tempFile.isFileOperationSupported(FileOperation.CREATE_DIRECTORY))
        return;

        // Assert that a directory can be created when the file doesn't already exist (without throwing an IOException)
        AbstractFile dir1 = tempFile.getDirectChild("dir1");
        AbstractFile dir2 = dir1.getDirectChild("dir2");
        AbstractFile dir2b = dir1.getChild("dir2"+dir1.getSeparator());     // Same file with a trailing separator
        dir2.mkdirs();

        // Assert that the file exists after the directory has been created
        assert dir2.exists();
        assert dir2.isDirectory();
        assert dir2b.exists();
        assert dir2b.isDirectory();

        // Delete 'dir2' and perform the same test. The difference with the previous test is that 'temp' and 'dir1' exist.
        dir2.delete();
        assert !dir2.exists();
        assert !dir2.isDirectory();
        assert !dir2b.exists();
        assert !dir2b.isDirectory();

        dir2.mkdirs();
        assert dir2.exists();
        assert dir2.isDirectory();
        assert dir2b.exists();
        assert dir2b.isDirectory();

        // Assert that an IOException is thrown when the directory already exists
        boolean ioExceptionThrown = false;
        try {
            dir2.mkdirs();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;

        // Assert that an IOException is thrown when a regular file exists
        dir2.delete();
        dir2.mkfile();

        ioExceptionThrown = false;
        try {
            dir2.mkdir();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;
    }

    /**
     * Tests the {@link AbstractFile#mkfile()} method in various situations.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testMkfile() throws IOException {
        // Assert that a file can be created when it doesn't already exist (without throwing an IOException)
        tempFile.mkfile();

        // Assert that the file exists after it has been created
        assert tempFile.exists();

        // Assert that an IOException is thrown when the file already exists
        boolean ioExceptionThrown = false;
        try {
            tempFile.mkfile();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;

        // Assert that an IOException is thrown when a directory exists
        tempFile.delete();
        tempFile.mkdir();

        ioExceptionThrown = false;
        try {
            tempFile.mkfile();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assert ioExceptionThrown;
    }

    /**
     * Tests the {@link AbstractFile#isDirectory()} method in various situations.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testIsDirectory() throws IOException {
        // Same file with a trailing separator
        FileURL tempFileURLB = (FileURL)tempFile.getURL().clone();
        tempFileURLB.setPath(tempFile.addTrailingSeparator(tempFileURLB.getPath()));
        AbstractFile tempFileB = FileFactory.getFile(tempFileURLB, true);

        // Assert that isDirectory() returns false when the file does not exist
        assert !tempFile.exists();
        assert !tempFile.isDirectory();
        assert !tempFileB.exists();
        assert !tempFileB.isDirectory();

        // Assert that isDirectory() returns true for directories
        tempFile.mkdir();
        assert tempFile.exists();
        assert tempFile.isDirectory();
        assert tempFileB.exists();
        assert tempFileB.isDirectory();

        // Assert that isDirectory() returns false for regular files
        tempFile.delete();
        assert !tempFile.exists();
        assert !tempFile.isDirectory();
        assert !tempFileB.exists();
        assert !tempFileB.isDirectory();

        tempFile.mkfile();
        assert tempFile.exists();
        assert !tempFile.isDirectory();
        assert tempFile.exists();
        assert !tempFileB.isDirectory();
    }

    /**
     * Tests {@link AbstractFile#changePermissions(int)}, calling {@link #testChangePermissionsSupported()} or
     * {@link #testChangePermissionsUnsupported()} depending on whether or not the {@link FileOperation#CHANGE_PERMISSION}
     * operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testChangePermissions() throws IOException, NoSuchAlgorithmException {
        if(tempFile.isFileOperationSupported(FileOperation.CHANGE_PERMISSION))
            testChangePermissionsSupported();
        else
            testChangePermissionsUnsupported();
    }

    /**
     * Tests {@link AbstractFile#getPermissions()}.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testGetPermissions() throws IOException, NoSuchAlgorithmException {
        assert tempFile.getPermissions() != null;

        createFile(tempFile, 0);

        FilePermissions permissions = tempFile.getPermissions();
        PermissionBits getPermMask = permissions.getMask();

        assert permissions != null;

        int getPermMaskInt = getPermMask.getIntValue();

        int bitShift = 0;
        int bitMask;
        boolean canGetPermission;

        for(PermissionAccess a : PermissionAccess.values()) {
            for(PermissionType p : PermissionType.values()) {
                bitMask = 1<<bitShift;

                canGetPermission = (getPermMaskInt & bitMask)!=0;
                assert getPermMask.getBitValue(a, p)==canGetPermission: "inconsistent bit and int value for ("+a+", "+p+")";

                if(canGetPermission) {
                    assert permissions.getBitValue(a, p)==((permissions.getIntValue() & bitMask)!=0):
                            "inconsistent bit and int value for ("+a+", "+p+")";
                }

                bitShift++;
            }
        }
    }

    /**
     * Tests {@link AbstractFile#changeDate(long)}, calling {@link #testChangeDateSupported()} or
     * {@link #testChangeDateUnsupported()} depending on whether or not the {@link FileOperation#CHANGE_DATE}
     * operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testChangeDate() throws IOException, NoSuchAlgorithmException {
        if(tempFile.isFileOperationSupported(FileOperation.CHANGE_DATE))
            testChangeDateSupported();
        else
            testChangeDateUnsupported();
    }

    /**
     * Tests {@link AbstractFile#getDate()}.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testGetDate() throws IOException, NoSuchAlgorithmException {
        createFile(tempFile, 0);

        // Asserts that the date changes when the file is modified
        long date = tempFile.getDate();
        sleep(1000);    // Sleep a full second, some filesystems may only have a one-second granularity
        createFile(tempFile, 1);  // 1 byte should be enough

        assert tempFile.getDate()>date;
    }

    /**
     * Tests {@link AbstractFile#getInputStream()}, calling {@link #testGetInputStreamSupported()} or
     * {@link #testGetInputStreamUnsupported()} depending on whether or not the {@link FileOperation#READ_FILE}
     * operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testGetInputStream() throws IOException, NoSuchAlgorithmException {
        if(tempFile.isFileOperationSupported(FileOperation.READ_FILE))
            testGetInputStreamSupported();
        else
            testGetInputStreamUnsupported();
    }

    /**
     * Tests {@link AbstractFile#getRandomAccessInputStream()}, calling {@link #testGetRandomAccessInputStreamSupported()}
     * or {@link #testGetRandomAccessInputStreamUnsupported()} depending on whether or not the
     * {@link FileOperation#RANDOM_READ_FILE} operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testGetRandomAccessInputStream() throws IOException, NoSuchAlgorithmException {
        if(tempFile.isFileOperationSupported(FileOperation.RANDOM_READ_FILE))
            testGetRandomAccessInputStreamSupported();
        else
            testGetRandomAccessInputStreamUnsupported();
    }

    /**
     * Tests {@link AbstractFile#getOutputStream()}, calling {@link #testGetOutputStreamSupported()}
     * or {@link #testGetOutputStreamUnsupported()} depending on whether or not the
     * {@link FileOperation#WRITE_FILE} operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testGetOutputStream() throws IOException, NoSuchAlgorithmException {
        if(tempFile.isFileOperationSupported(FileOperation.WRITE_FILE))
            testGetOutputStreamSupported();
        else
            testGetOutputStreamUnsupported();
    }

    /**
     * Tests {@link AbstractFile#getAppendOutputStream()}, calling {@link #testGetAppendOutputStreamSupported()}
     * or {@link #testGetAppendOutputStreamUnsupported()} depending on whether or not the
     * {@link FileOperation#APPEND_FILE} operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testGetAppendOutputStream() throws IOException, NoSuchAlgorithmException {
        if(tempFile.isFileOperationSupported(FileOperation.APPEND_FILE))
            testGetAppendOutputStreamSupported();
        else
            testGetAppendOutputStreamUnsupported();
    }

    /**
     * Tests {@link AbstractFile#getRandomAccessOutputStream()}, calling {@link #testGetRandomAccessOutputStreamSupported()}
     * or {@link #testGetRandomAccessOutputStreamUnsupported()} depending on whether or not the
     * {@link FileOperation#RANDOM_WRITE_FILE} operation is supported.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testGetRandomAccessOutputStream() throws IOException, NoSuchAlgorithmException {
        if(tempFile.isFileOperationSupported(FileOperation.RANDOM_WRITE_FILE))
            testGetRandomAccessOutputStreamSupported();
        else
            testGetRandomAccessOutputStreamUnsupported();
    }

    /**
     * Tests {@link AbstractFile#ls()}.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testLs() throws IOException {
        if(tempFile.isFileOperationSupported(FileOperation.LIST_CHILDREN))
            testLsSupported();
        else
            testLsUnsupported();
    }

    /**
     * Tests {@link AbstractFile#getFreeSpace()}.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testFreeSpace() throws IOException {
        if(tempFile.isFileOperationSupported(FileOperation.GET_FREE_SPACE))
            testGetFreeSpaceSupported();
        else
            testGetFreeSpaceUnsupported();
    }

    /**
     * Tests {@link AbstractFile#getTotalSpace()}.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testTotalSpace() throws IOException {
        if(tempFile.isFileOperationSupported(FileOperation.GET_TOTAL_SPACE))
            testGetTotalSpaceSupported();
        else
            testGetTotalSpaceUnsupported();
    }

    /**
     * Tests {@link AbstractFile#moveTo(AbstractFile)}.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testMoveTo() throws IOException, NoSuchAlgorithmException {
        testMoveTo(false);
    }

    /**
     * Tests {@link AbstractFile#renameTo(AbstractFile)}.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testRenameTo() throws IOException, NoSuchAlgorithmException {
        if(tempFile.isFileOperationSupported(FileOperation.RENAME))
            testRenameToSupported();
        else
            testRenameToUnsupported();
    }

    /**
     * Tests {@link AbstractFile#copyTo(AbstractFile)}.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testCopyTo() throws IOException, NoSuchAlgorithmException {
        testCopyTo(false);
    }

    /**
     * Tests {@link AbstractFile#copyRemotelyTo(AbstractFile)}.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testCopyRemotelyTo() throws IOException, NoSuchAlgorithmException {
        if(tempFile.isFileOperationSupported(FileOperation.COPY_REMOTELY))
            testCopyRemotelyToSupported();
        else
            testCopyRemotelyToUnsupported();
    }

    /**
     * Tests {@link AbstractFile#getIcon()} and {@link AbstractFile#getIcon(java.awt.Dimension)}.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testIcon() throws IOException {
        Icon    icon;
        boolean isHeadless;

        // Skips the test if under OS X (this would create a new instance of JFileChooser, which fails 
        isHeadless = GraphicsEnvironment.isHeadless();
        if(isHeadless && OsFamily.MAC_OS.isCurrent())
            return;

        // Some icon providers will fail (return a null icon) if the file doesn't exist
        tempFile.mkfile();

        icon = tempFile.getIcon();
        if(!isHeadless)
            assert icon != null;

        icon = tempFile.getIcon(new Dimension(16, 16));
        if(!isHeadless)
            assert icon != null;
    }

    /**
     * Verifies that the file implementation handles unicode/non-ascii filenames properly.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testUnicodeFilenames() throws IOException {
        tempFile.mkdir();

        String unicodeFilename = "どうもありがとうミスターロボット";
        if (OsFamily.MAC_OS.isCurrent())
            unicodeFilename = FileUtils.normalizeWithNFD(unicodeFilename);
        Locale filenameLocale = Locale.JAPANESE;

        testUnicodeFilename(tempFile, unicodeFilename, filenameLocale, false);
        testUnicodeFilename(tempFile, unicodeFilename, filenameLocale, true);
    }

    /**
     * Tests {@link com.mucommander.commons.file.util.PathUtils#resolveDestination(String, AbstractFile)} by calling
     * {@link PathUtilsTest#testResolveDestination(AbstractFile)} with a temporary folder.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testDestinationResolution() throws IOException {
        AbstractFile folder = deleteWhenFinished(getTemporaryFile());
        folder.mkdir();
        new PathUtilsTest().testResolveDestination(folder);
    }

    /**
     * Tests the absence of {@link UnsupportedFileOperation} annotations in all methods corresponding to
     * {@link #getSupportedOperations() supported operations}, and the absence thereof for unsupported operations.
     *
     * @throws Exception should not happen
     */
    @Test
    public void testUnsupportedFileOperationAnnotations() throws Exception {
        List<FileOperation> supportedOps = Arrays.asList(getSupportedOperations());

        Class<? extends AbstractFile> fileClass = tempFile.getClass();
        Method m;
        for(FileOperation op: FileOperation.values()) {
            m = op.getCorrespondingMethod(fileClass);
            assert supportedOps.contains(op) ==
                !fileClass.getMethod(m.getName(), m.getParameterTypes()).isAnnotationPresent(UnsupportedFileOperation.class)
                    :"File operation "+op+" does not match annotation of method "+m.getName();
        }
    }

    /**
     * Ensures that the return value of {@link AbstractFile#isFileOperationSupported(FileOperation)} is consistent
     * with {@link #getSupportedOperations() supported operations}.
     *
     * @throws Exception should not happen
     */
    @Test
    public void testSupportedFileOperations() throws Exception {
        List<FileOperation> supportedOps = Arrays.asList(getSupportedOperations());

        AbstractFile tempFile = getTemporaryFile();
        Class<? extends AbstractFile> fileClass = tempFile.getClass();
        for(FileOperation op: FileOperation.values()) {
            boolean opSupported = supportedOps.contains(op);
            assert opSupported == tempFile.isFileOperationSupported(op);
            assert opSupported == AbstractFile.isFileOperationSupported(op, fileClass);
        }
    }

    /**
     * Ensures that {@link AbstractFile} instance caching works as expected, that is the same instance is returned
     * by <code>FileFactory#getFile</code> methods every time the same location is asked for.
     *
     * @throws Exception should not happen
     */
    @Test
    public void testFileInstanceCaching() throws Exception {
        AbstractFile file;
        for(int i=0; i<10; i++) {
            file = getTemporaryFile();
            for(int j=0; j<5; j++) {
                // Resolve by path
                String pathT = file.addTrailingSeparator(file.getURL().toString(true, false));
                String pathNT = file.removeTrailingSeparator(pathT);
                assert FileFactory.getFile(pathT)==file;
                assert FileFactory.getFile(pathNT)==file;

                // Resolve by URL
                assert FileFactory.getFile(file.getURL())==file;
                assert FileFactory.getFile(FileURL.getFileURL(pathT))==file;
                assert FileFactory.getFile(FileURL.getFileURL(pathNT))==file;
            }
        }

    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns a temporary file that can be used for testing purposes. Implementation of this method must guarantee that:
     * <ul>
     *   <li>the returned file does not exist, i.e. that {@link AbstractFile#exists()} returns <code>false</code>.</li>
     *   <li>a new file is returned each time this method is called.</li>
     *   <li>the return file's path does not end with a trailing path separator</li>
     * </ul>
     *
     * @return a temporary file that does not exist
     * @throws IOException if an error occurred while creating a temporary file
     */
    public abstract AbstractFile getTemporaryFile() throws IOException;

    /**
     * Returns a list of all {@link FileOperation} supported by this file implementation.
     *
     * @return a list of all {@link FileOperation} supported by this file implementation.
     */
    public abstract FileOperation[] getSupportedOperations();
}
