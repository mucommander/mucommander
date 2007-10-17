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

    /** A temporary file instance automatically instanciated by {@link #setUp()} when a test is started */
    protected AbstractFile tempFile;


    /////////////////////////
    // Init/Deinit methods // 
    /////////////////////////

    /**
     * Creates a temporary {@link AbstractFile} instance each time a test is started. The instance is created but the
     * file is not physically created.
     *
     * @throws Exception if an error occurred while creating the temporary file
     */
    protected void setUp() throws Exception {
        tempFile = getTemporaryFile();
    }

    /**
     * Deletes the temporary file if it exists when the test is over.
     *
     * @throws Exception if an error occurred while deleting the temporary file
     */
    protected void tearDown() throws Exception {
        if(tempFile.exists())
            tempFile.delete();
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
