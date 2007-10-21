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

import junit.framework.TestCase;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

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
public abstract class AbstractFileTestCase extends TestCase {

    /**
     * A temporary file instance automatically instanciated by {@link #setUp()} when a test is started.
     * The file itself is not physically created.
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
     * @throws Exception if an error occurred while creating test variables
     */
    protected void setUp() throws Exception {
        tempFile = getTemporaryFile();

        // Use a static seed so that the generated values are reproducible
        random = new Random(0);
    }

    /**
     * Cleans up test files after each test execution so as to leave the filesystem in the same state as it was
     * before the test.
     *
     * @throws Exception if an error occurred while cleaning up test files
     */
    protected void tearDown() throws Exception {
        if(tempFile.exists())
            deleteRecursively(tempFile);
    }


    /////////////////////
    // Support methods //
    /////////////////////

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
     * Creates a regular file and fills it with <code>length</code> random bytes. The file must not already exist when
     * this method is called or an <code>IOException</code> will be thrown.
     *
     * @param file the file to create
     * @param length the number of random bytes to fill the file with
     * @throws IOException if the file already exists or if an error occurred while writing to it
     */
    protected void createFile(AbstractFile file, int length) throws IOException {
        OutputStream out = file.getOutputStream(false);

        try {
            byte b[] = new byte[length];
            random.nextBytes(b);
            out.write(b);
        }
        finally {
            out.close();
        }
    }


    /**
     * Deletes the given file. If the file is a directory, enclosing files are deleted recursively. If the file is
     * a regular file, if will simply be deleted. The bottom line is that the given file will always be deleted no
     * matter the kind of file.
     *
     * @param file the file to delete
     * @throws IOException if an error occurred while deleting a file or listing files in a directory
     */
    protected void deleteRecursively(AbstractFile file) throws IOException {
        if(file.isDirectory()) {
            AbstractFile children[] = file.ls();
            for(int i=0; i<children.length; i++)
                deleteRecursively(children[i]);
        }

        file.delete();
    }


    //////////////////
    // Test methods //
    //////////////////

    /**
     * Tests {@link AbstractFile#digest(java.security.MessageDigest)} and {@link AbstractFile#getDigestHexString(byte[])}
     * by computing file digests using different algorithms (MD5, SHA-1, ...) and comparing them against known values.
     *
     * @throws IOException should not normally happen
     * @throws NoSuchAlgorithmException should not normally happen
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
     * Tests {@link com.mucommander.file.AbstractFile#exists()} in various situations.
     *
     * @throws IOException should not happen
     */
    public void testExistence() throws IOException {
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


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns a temporary file that can be used for testing purposes. The returned file should must not exist.
     * Note: implementations do not need to worry about deleting the file when the test is finished, 
     * {@link com.mucommander.file.AbstractFileTestCase} already takes care of that.
     *
     * @return a temporary, non-existing file
     * @throws Exception if an error occurred
     */
    protected abstract AbstractFile getTemporaryFile() throws Exception;
}
