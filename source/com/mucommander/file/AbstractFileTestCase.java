/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file;

import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

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
public abstract class AbstractFileTestCase extends TestCase implements FilePermissions {

    /**
     * AbstractFile instances to be deleted if they exist when {@link #tearDown()} is called.
     */
    protected Vector filesToDelete;

    /**
     * A temporary file instance automatically instanciated by {@link #setUp()} when a test is started. The file
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
    protected void setUp() throws IOException {
        filesToDelete = new Vector();

        tempFile = getTemporaryFile();
        deleteWhenFinished(tempFile);   // this file will be automatically deleted when the test is over

        // Use a static seed so that the generated values are reproducible
        random = new Random(0);
    }

    /**
     * Cleans up test files after each test execution so as to leave the filesystem in the same state as it was
     * before the test. In particular, all files registered with {@link #deleteWhenFinished(AbstractFile)} are
     * deleted if they exist.
     *
     * @throws IOException if an error occurred while delete files registered with {@link #deleteWhenFinished(AbstractFile)}
     */
    protected void tearDown() throws IOException {
        Iterator iterator = filesToDelete.iterator();

        AbstractFile file;
        while(iterator.hasNext()) {
            file = (AbstractFile)iterator.next();
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
     */
    protected void deleteWhenFinished(AbstractFile fileToDelete) {
        if(!filesToDelete.contains(fileToDelete))
            filesToDelete.add(fileToDelete);
    }


    /**
     * Fills the given file with <code>length</code> bytes of random data, calling <code>OutputStream#write(byte b[])</code>
     * with a random array length of up to <code>maxChunkSize</code>. The <code>write</code> method will be called
     * <code>(length/(maxChunkSize/2)) times on average</code>. The <code>OutputStream</code> used for writing data is
     * retrieved from {@link AbstractFile#getOutputStream(boolean)}, passing the specified <code>append</code> argument.
     *
     * <p>The {@link #random} instance used by this method is initialized with a static seed, so the data generated
     * by this method will remain the same if the series of prior calls to the random instance haven't changed.
     * This makes it possible to reproduce and fix a failed test case.</p>
     *
     * @param file the file to fill with data
     * @param length the number of random bytes to fill the file with
     * @param maxChunkSize maximum size of a data chunk written to the file. Size of chunks is comprised between 1 and this value (inclusive).
     *        If -1 is passed,
     * @param append if true, data written to the OutputStream will be appended to the end of this file. If false,
     * any existing data this file contains will be discarded and overwritten.
     * @throws IOException if an error occurred while retrieved the OutputStream or while writing to it
     */
    protected void fillRandomData(AbstractFile file, int length, int maxChunkSize, boolean append) throws IOException {
        OutputStream out = file.getOutputStream(append);
        int remaining = length;
        byte bytes[];
        int chunkSize;
        try {
            while(remaining>0) {
                chunkSize = random.nextInt(1+Math.min(remaining, maxChunkSize));

                bytes = new byte[chunkSize];
                random.nextBytes(bytes);

                out.write(bytes);

                remaining -= chunkSize;
            }
        }
        finally {
            out.close();
        }
    }

    /**
     * Creates a regular file and fills it with <code>length</code> random bytes. The file will be overwritten if it
     * already exists. Before returning, this method asserts that the file exists and that its size by
     * {@link AbstractFile#getSize()} matches the specified length argument. 
     *
     * @param file the file to create or overwrite
     * @param length the number of random bytes to fill the file with
     * @throws IOException if the file already exists or if an error occurred while writing to it
     */
    protected void createFile(AbstractFile file, int length) throws IOException {
        OutputStream out = file.getOutputStream(false);

        try {
            byte b[] = new byte[length];
            random.nextBytes(b);
            out.write(b);

            assertTrue(file.exists());
            assertEquals(length, file.getSize());
        }
        finally {
            out.close();
        }
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


    //////////////////
    // Test methods //
    //////////////////

    /**
     * Tests {@link AbstractFile#digest(java.security.MessageDigest)} and {@link AbstractFile#getDigestHexString(byte[])}
     * by computing file digests using different algorithms (MD5, SHA-1, ...) and comparing them against known values.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    public void testDigest() throws IOException, NoSuchAlgorithmException {

        // Assert digests of an empty file

        tempFile.mkfile();

        assertEquals("8350e5a3e24c153df2275c9f80692773", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("MD2"))));
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("MD5"))));
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("SHA-1"))));
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("SHA-256"))));
        assertEquals("38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("SHA-384"))));
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("SHA-512"))));

        OutputStream tempOut = tempFile.getOutputStream(false);

        // Assert digests of a sample phrase

        tempOut.write("The quick brown fox jumps over the lazy dog".getBytes());
        tempOut.close();

        assertEquals("03d85a0d629d2c442e987525319fc471", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("MD2"))));
        assertEquals("9e107d9d372bb6826bd81d3542a419d6", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("MD5"))));
        assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("SHA-1"))));
        assertEquals("d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("SHA-256"))));
        assertEquals("ca737f1014a48f4c0b6dd43cb177b0afd9e5169367544c494011e3317dbf9a509cb1e5dc1e85a941bbee3d7f2afbc9b1", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("SHA-384"))));
        assertEquals("07e547d9586f6a73f73fbac0435ed76951218fb7d0c8d788a309d785436bbb642e93a252a954f23912547d1e8a3b5ed6e1bfd7097821233fa0538f3db854fee6", AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("SHA-512"))));
    }

    /**
     * Verifies that temporary files can be resolved again by {@link FileFactory#getFile(String)}.
     *
     * @throws IOException should not happen
     */
    public void testPathResolution() throws IOException {
        assertNotNull(FileFactory.getFile(getTemporaryFile().getURL().toString(true)));
    }

    /**
     * Tests {@link com.mucommander.file.AbstractFile#exists()} in various situations.
     *
     * @throws IOException should not happen
     */
    public void testExists() throws IOException {
        assertFalse(tempFile.exists());

        tempFile.mkfile();
        assertTrue(tempFile.exists());

        tempFile.delete();
        assertFalse(tempFile.exists());

        tempFile.mkdir();
        assertTrue(tempFile.exists());

        tempFile.delete();
        assertFalse(tempFile.exists());
    }

    /**
     * Tests the {@link AbstractFile#delete()} method in various situations.
     *
     * @throws IOException should not happen
     */
    public void testDelete() throws IOException {
        // Assert that an IOException is thrown for a file that does not exist
        boolean ioExceptionThrown = false;
        try {
            tempFile.delete();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assertTrue(ioExceptionThrown);

        // Assert that a regular file can be properly deleted and that the file does not exist anymore after
        tempFile.mkfile();
        tempFile.delete();
        assertFalse(tempFile.exists());

        // Assert that a regular directory can be properly deleted and that the file does not exist anymore after
        tempFile.mkdir();
        tempFile.delete();
        assertFalse(tempFile.exists());

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

        assertTrue(ioExceptionThrown);
    }

    /**
     * Tests the {@link AbstractFile#mkdir()} method in various situations.
     *
     * @throws IOException should not happen
     */
    public void testMkdir() throws IOException {
        // Assert that a directory can be created when the file doesn't already exist (without throwing an IOException)
        tempFile.mkdir();

        // Assert that the file exists after the directory has been created
        assertTrue(tempFile.exists());

        // Assert that an IOException is thrown when the directory already exists
        boolean ioExceptionThrown = false;
        try {
            tempFile.mkdir();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assertTrue(ioExceptionThrown);

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

        assertTrue(ioExceptionThrown);
    }

    /**
     * Tests the {@link AbstractFile#mkfile()} method in various situations.
     *
     * @throws IOException should not happen
     */
    public void testMkfile() throws IOException {
        // Assert that a file can be created when it doesn't already exist (without throwing an IOException)
        tempFile.mkfile();

        // Assert that the file exists after it has been created
        assertTrue(tempFile.exists());

        // Assert that an IOException is thrown when the file already exists
        boolean ioExceptionThrown = false;
        try {
            tempFile.mkfile();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assertTrue(ioExceptionThrown);

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

        assertTrue(ioExceptionThrown);
    }

    /**
     * Tests the {@link AbstractFile#isDirectory()} method in various situations.
     *
     * @throws IOException should not happen
     */
    public void testIsDirectory() throws IOException {
        // Assert that isDirectory() returns false when the file does not exist
        assertFalse(tempFile.isDirectory());

        // Assert that isDirectory() returns true for directories
        tempFile.mkdir();
        assertTrue(tempFile.isDirectory());

        // Assert that isDirectory() returns false for regular files
        tempFile.delete();
        tempFile.mkfile();
        assertFalse(tempFile.isDirectory());
    }

    /**
     * Tests all <code>AbstractFile</code> permissions methods and asserts the following things for each access
     * (user, group, other) and permission (read, write, execute) combination:
     * <ul>
     *  <li>that the information returned by {@link AbstractFile#getPermissionGetMask()}
     * and {@link AbstractFile#canGetPermission(int, int)} are consistent</li>
     *  <li>that the information returned by {@link AbstractFile#getPermissionSetMask()}
     * and {@link AbstractFile#canSetPermission(int, int)} are consistent</li>
     *  <li>that the values returned by {@link AbstractFile#getPermissions()}
     * and {@link AbstractFile#getPermission(int, int)} are consistent for supported permission flags</li>
     *  <li>{@link AbstractFile#setPermission(int, int, boolean)} and {@link AbstractFile#setPermissions(int)} work as
     * they should for supported permission flags</li>
     * </ul>
     *
     * @throws IOException should not happen
     */
    public void testPermissions() throws IOException {
        createFile(tempFile, 0);

        int getPermMask = tempFile.getPermissionGetMask();
        int setPermMask = tempFile.getPermissionSetMask();

        int bitShift = 0;
        int bitMask;
        boolean canGetPermission, canSetPermission;

        for(int a=OTHER_ACCESS; a<= USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                bitMask = 1<<bitShift;

                canGetPermission = (getPermMask & bitMask)!=0;
                assertTrue("getPermissionGetMask() doesn't match canGetPermission("+a+", "+p+")",
                        tempFile.canGetPermission(a, p)==canGetPermission);

                canSetPermission = (setPermMask & bitMask)!=0;
                assertTrue("getPermissionSetMask() doesn't match canSetPermission("+a+", "+p+")",
                        tempFile.canSetPermission(a, p)==canSetPermission);

                if(canGetPermission) {
                    assertTrue("getPermissions() doesn't match getPermission("+a+", "+p+")",
                            tempFile.getPermission(a, p)==((tempFile.getPermissions() & bitMask)!=0));
                }

                if(canSetPermission) {
                    for(boolean enabled=true; ;) {
                        assertTrue("setPermission("+a+", "+p+") failed", tempFile.setPermission(a, p, enabled));
                        assertTrue("setPermissions("+(enabled?bitMask:(0777&~bitMask))+") failed", tempFile.setPermissions(enabled?bitMask:(0777&~bitMask)));

                        if(canGetPermission) {
                            assertTrue("getPermission("+a+", "+p+") should be "+enabled, tempFile.getPermission(a, p)==enabled);
                            assertTrue("permission bit "+bitShift+" should be "+enabled, ((tempFile.getPermissions() & bitMask)!=0)==enabled);
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
     * Tests {@link AbstractFile#getDate()}, {@link AbstractFile#canChangeDate()} and {@link AbstractFile#changeDate(long)},
     * no matter if dates can be changed or not.
     *
     * @throws IOException should not happen
     */
    public void testDate() throws IOException {
        createFile(tempFile, 0);

        // Asserts that the date changes when the file is modified
        long date = tempFile.getDate();
        sleep(1000);    // Sleep a full second, some filesystems may only have a one-second granularity
        createFile(tempFile, 1);  // 1 byte should be enough

        assertTrue(tempFile.getDate()>date);

        if(tempFile.canChangeDate()) {
            // Assert that changeDate succeeds (returns true)
            assertTrue(tempFile.changeDate(date=(tempFile.getDate()-1000)));

            // Assert that the getDate returns the date that was set
            assertEquals(date, tempFile.getDate());
        }
        else {
            // Assert that changeDate returns false if date cannot be changed
            assertFalse(tempFile.changeDate(tempFile.getDate()-1000));
        }
    }


    /**
     * Tests {@link AbstractFile#hasRandomAccessInputStream()} and {@link AbstractFile#getRandomAccessInputStream()}.
     *
     * @throws IOException should not happen
     */
    public void testRandomAccessInputStream() throws IOException {
        boolean ioExceptionThrown;

        if(tempFile.hasRandomAccessInputStream()) {
            // Assert that getRandomAccessInputStream throws an IOException when the file does not exist
            ioExceptionThrown = false;
            try {
                tempFile.getRandomAccessInputStream();
            }
            catch(IOException e) {
                ioExceptionThrown = true;
            }

            assertTrue(ioExceptionThrown);

            createFile(tempFile, 1);

            // Assert that getRandomAccessInputStream does not throw an IOException and returns a non-null value
            // when the file exists
            RandomAccessInputStream rais = tempFile.getRandomAccessInputStream();

            assertNotNull(rais);
            // Ensure that the size returned by RandomAccessInputStream#getLength() matches the one returned by
            // AbstractFile#getSize()
            assertEquals(tempFile.getSize(), rais.getLength());

            rais.close();
        }
        else {
            // Assert that getRandomAccessInputStream throws an IOException when such a stream cannot be provided
            ioExceptionThrown = false;
            try {
                tempFile.getRandomAccessInputStream();
            }
            catch(IOException e) {
                ioExceptionThrown = true;
            }

            assertTrue(ioExceptionThrown);
        }
    }

    /**
     * Tests {@link AbstractFile#hasRandomAccessOutputStream()} and {@link AbstractFile#getRandomAccessOutputStream()}.
     *
     * @throws IOException should not happen
     */
    public void testRandomAccessOutputStream() throws IOException {
        if(tempFile.hasRandomAccessOutputStream()) {
            // Assert that:
            // - getRandomAccessOutputStream does not throw an IOException
            // - returns a non-null value
            // - the file exists after
            RandomAccessOutputStream raos = tempFile.getRandomAccessOutputStream();

            assertNotNull(raos);
            assertTrue(tempFile.exists());

            raos.close();

            // Assert that the size returned by RandomAccessOutputStream#getLength() matches the one returned by
            // AbstractFile#getSize()
            createFile(tempFile, 1);
            raos = tempFile.getRandomAccessOutputStream();

            assertEquals(tempFile.getSize(), raos.getLength());

            raos.close();
        }
        else {
            // Assert that getRandomAccessOutputStream throws an IOException when such a stream cannot be provided
            boolean ioExceptionThrown = false;
            try {
                tempFile.getRandomAccessOutputStream();
            }
            catch(IOException e) {
                ioExceptionThrown = true;
            }

            assertTrue(ioExceptionThrown);
        }
    }

    /**
     * Tests {@link AbstractFile#getInputStream()}.
     *
     * @throws IOException should not happen
     */
    public void testInputStream() throws IOException {
        boolean ioExceptionThrown;

        // Assert that getInputStream throws an IOException when the file does not exist
        ioExceptionThrown = false;
        try {
            tempFile.getInputStream();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assertTrue(ioExceptionThrown);

        createFile(tempFile, 0);

        // Assert that getInputStream does not throw an IOException and returns a non-null value when the file exists
        InputStream in = tempFile.getInputStream();

        assertNotNull(in);

        in.close();
    }

    /**
     * Tests {@link AbstractFile#getOutputStream(boolean)}.
     *
     * @throws IOException should not happen
     */
    public void testOutputStream() throws IOException {
        // Assert that:
        // - getOutputStream does not throw an IOException
        // - returns a non-null value
        // - the file exists after
        OutputStream out = tempFile.getOutputStream(false);

        assertNotNull(out);
        assertTrue(tempFile.exists());

        out.close();

        // Assert that getOutputStream(false) overwrites the existing file contents (resets the file size to 0)
        createFile(tempFile, 1);
        out = tempFile.getOutputStream(false);
        out.close();

        assertEquals(0, tempFile.getSize());

        // Assert that getOutputStream(true) does not overwrite the existing file contents.
        // Appending to the file may not be supported, catch IOException thrown by getOutputStream(true) and only those  
        try {
            createFile(tempFile, 1);

            out = null;
            out = tempFile.getOutputStream(true);

            out.write('a');
            out.close();

            assertEquals(2, tempFile.getSize());
        }
        catch(IOException e) {
            if(out!=null)
                throw e;    // Exception was not thrown by getOutputStream(true), re-throw it
            else
                System.out.println("testOutputStream(): looks like append is not supported, caught: "+e);
        }
    }

    /**
     * Tests {@link AbstractFile#ls()}.
     *
     * @throws IOException should not happen
     */
    public void testLs() throws IOException {
        // Assert that an IOException is thrown when the file does not exist
        boolean ioExceptionThrown = false;
        try {
            tempFile.ls();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assertTrue(ioExceptionThrown);

        // Assert that an IOException is thrown when the file is not browsable
        tempFile.mkfile();
        ioExceptionThrown = false;
        try {
            tempFile.ls();
        }
        catch(IOException e) {
            ioExceptionThrown = true;
        }

        assertTrue(ioExceptionThrown);

        // Create an empty directory and assert that ls() does not throw an IOException and returns a zero-length array
        tempFile.delete();
        tempFile.mkdir();

        AbstractFile children[] = tempFile.ls();
        assertNotNull(children);
        assertEquals(0, children.length);

        // Create a child file and assert that this child (and only this child) is returned by ls(), and that the file exists
        AbstractFile child = tempFile.getChild("child");
        child.mkfile();
        children = tempFile.ls();

        assertNotNull(children);
        assertEquals(1, children.length);
        assertEquals(child, children[0]);
        assertTrue(children[0].exists());
    }

    /**
     * Tests {@link AbstractFile#getFreeSpace()} by asserting that the returned value is either <code>-1</code>
     * (not available), or a positive (potentially null) value.
     */
    public void testFreeSpace() {
        long freeSpace = tempFile.getFreeSpace();

        assertTrue(freeSpace>=-1);

        // Note: it would be interesting to assert that allocating space to a file diminishes free space accordingly
        // but it is not possible to guarantee that free space is not altered by another process.
    }

    /**
     * Tests {@link AbstractFile#getTotalSpace()} by asserting that the returned value is either <code>-1</code>
     * (not available), or a positive (potentially null) value. 
     */
    public void testTotalSpace() {
        long totalSpace = tempFile.getFreeSpace();

        assertTrue(totalSpace>=-1);
    }


    /**
     * Tests {@link AbstractFile#getCopyToHint(AbstractFile)} and {@link AbstractFile#copyTo(AbstractFile)}.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happen
     */
    public void testCopyTo() throws IOException, NoSuchAlgorithmException {
        createFile(tempFile, 100000);
        AbstractFile destFile = getTemporaryFile();
        deleteWhenFinished(destFile);       // this file will automatically be deleted if it exists when the test is over

        // Assert that getCopyToHint(AbstractFile) returns an allowed value (one of the hint constants)
        int copyToHint = tempFile.getCopyToHint(destFile);
        assertTrue(copyToHint>=AbstractFile.SHOULD_HINT && copyToHint<=AbstractFile.MUST_NOT_HINT);

        // Abort test if copyTo must not be called
        if(copyToHint==AbstractFile.MUST_NOT_HINT) {
            System.out.println("#copyTo(AbstractFile) not supported, skipping test.");
            return;
        }

       // Try and copy the file, copyTo is allowed to fail gracefully and return false
        if(tempFile.copyTo(destFile)) {     // If copyTo succeeded
            // Assert that the checksum of source and destination match
            assertEquals(AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("md5"))), AbstractFile.getDigestHexString(destFile.digest(MessageDigest.getInstance("md5"))));

            // At this point, we know that copyTo works (doesn't return false), at least for this destination file

            // Assert that copyTo fails when the destination exists, and that the destination file still exists after
            boolean exceptionThrown = false;
            try { tempFile.copyTo(destFile); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assertTrue(exceptionThrown);
            assertTrue(destFile.exists());

            // Assert that copyTo fails when the source and destination files are the same
            destFile.delete();
            exceptionThrown = false;
            try { tempFile.copyTo(tempFile); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assertTrue(exceptionThrown);
            assertFalse(destFile.exists());

            // Assert that copyTo fails when the source file doesn't exist
            tempFile.delete();
            exceptionThrown = false;
            try { tempFile.copyTo(destFile); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assertTrue(exceptionThrown);
            assertFalse(destFile.exists());

            // Assert that copyTo succeeds copying a directory
            tempFile.mkdir();

            assertTrue(tempFile.copyTo(destFile));
            assertTrue(destFile.exists());
            assertTrue(destFile.isDirectory());

            // Assert that copyTo fails when the source is a directory, and when the destination is a subfolder of source
            AbstractFile subFolder = tempFile.getDirectChild("subfolder");
            exceptionThrown = false;
            try { tempFile.copyTo(subFolder); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assertTrue(exceptionThrown);
            assertFalse(subFolder.exists());

            // Todo: test copyTo on a large, randomly-generated file tree
        }
        else {                              // copyTo failed gracefully
            System.out.println("Warning: AbstractFile#copyTo(AbstractFile) did not succeed (returned false)");

            // Assert that the destination file does not exist
            assertFalse(destFile.exists());
        }
    }


    /**
     * Tests {@link AbstractFile#getMoveToHint(AbstractFile)} and {@link AbstractFile#moveTo(AbstractFile)}.
     *
     * @throws IOException should not happen
     * @throws NoSuchAlgorithmException should not happens
     */
    public void testMoveTo() throws IOException, NoSuchAlgorithmException {
        createFile(tempFile, 100000);
        AbstractFile destFile = getTemporaryFile();
        deleteWhenFinished(destFile);       // this file will automatically be deleted if it exists when the test is over

        // Assert that getMoveToHint(AbstractFile) returns an allowed value (one of the hint constants)
        int moveToHint = tempFile.getMoveToHint(destFile);
        assertTrue(moveToHint>=AbstractFile.SHOULD_HINT && moveToHint<=AbstractFile.MUST_NOT_HINT);

        // Abort test if moveTo must not be called
        if(moveToHint==AbstractFile.MUST_NOT_HINT) {
            System.out.println("#moveTo(AbstractFile) not supported, skipping test.");
            return;
        }

        String sourceChecksum = AbstractFile.getDigestHexString(tempFile.digest(MessageDigest.getInstance("md5")));

       // Try and move the file, moveTo is allowed to fail gracefully and return false
        if(tempFile.moveTo(destFile)) {     // If moveTo succeeded
            // Assert that the source file is gone and the destination file exists
            assertFalse(tempFile.exists());
            assertTrue(destFile.exists());

            // Assert that the checksum of source and destination match
            assertEquals(sourceChecksum, AbstractFile.getDigestHexString(destFile.digest(MessageDigest.getInstance("md5"))));

            // At this point, we know that moveTo works (doesn't return false), at least for this destination file

            // Assert that moveTo fails when the destination exists, and that the source and destination files still
            // exists after
            createFile(tempFile, 1);
            boolean exceptionThrown = false;
            try { tempFile.moveTo(destFile); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assertTrue(exceptionThrown);
            assertTrue(tempFile.exists());
            assertTrue(destFile.exists());

            // Assert that moveTo fails when the source and destination files are the same
            destFile.delete();
            exceptionThrown = false;
            try { tempFile.moveTo(tempFile); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assertTrue(exceptionThrown);
            assertTrue(tempFile.exists());
            assertFalse(destFile.exists());

            // Assert that moveTo fails when the source file doesn't exist
            tempFile.delete();
            exceptionThrown = false;
            try { tempFile.moveTo(destFile); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assertTrue(exceptionThrown);
            assertFalse(destFile.exists());

            // Assert that moveTo succeeds moving a directory
            tempFile.mkdir();

            assertTrue(tempFile.moveTo(destFile));
            assertFalse(tempFile.exists());
            assertTrue(destFile.exists());
            assertTrue(destFile.isDirectory());

            // Assert that moveTo fails when the source is a directory and a parent of the destination
            tempFile.mkdir();
            AbstractFile subFolder = tempFile.getDirectChild("subfolder");
            exceptionThrown = false;
            try { tempFile.moveTo(subFolder); }
            catch(FileTransferException e) { exceptionThrown = true; }

            assertTrue(exceptionThrown);
            assertTrue(tempFile.exists());
            assertFalse(subFolder.exists());

            // Todo: test moveTo on a large, randomly-generated file tree
        }
        else {                              // moveTo failed gracefully
            System.out.println("Warning: AbstractFile#moveTo(AbstractFile) did not succeed (returned false)");

            // Assert that the destination file does not exist
            assertFalse(destFile.exists());
        }
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns a temporary file that can be used for testing purposes.
     * The implementation should return a file that does not exist, i.e. for which {@link AbstractFile#exists()}
     * returns <code>false</code>.
     *
     * @return a temporary file that does not physically exist
     * @throws IOException if an error occurred while creating a temporary file
     */
    protected abstract AbstractFile getTemporaryFile() throws IOException;
}
