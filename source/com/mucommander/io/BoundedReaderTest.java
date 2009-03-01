/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.io;

import junit.framework.TestCase;

import java.io.CharArrayReader;
import java.io.IOException;

/**
 * A test case for {@link com.mucommander.io.BoundedReader}.
 *
 * @see com.mucommander.io.BoundedReader
 * @author Maxence Bernard
 */
public class BoundedReaderTest extends TestCase {

    private final static char[] TEST_CHARACTERS = new char[]{'m', 'u', 'c', 'o', 'm', 'm', 'a', 'n', 'd', 'e', 'r'};


    /**
     * Performs some tests that are common to {@link #testBoundedReaderWithException()} and
     * {@link #testBoundedReaderWithoutException()}.
     *
     * @param br the BoundedReader to prepare
     * @throws IOException should not happen
     */
    private void prepareBoundedReader(BoundedReader br) throws IOException {
        assertEquals(0, br.getReadCounter());
        assertEquals(4, br.getRemainingCharacters());
        assertEquals(4, br.getAllowedCharacters());

        assertTrue(br.read()!=-1);
        assertEquals(1, br.getReadCounter());
        assertEquals(3, br.getRemainingCharacters());
        assertEquals(4, br.getAllowedCharacters());

        assertTrue(br.read(new char[1])!=-1);
        assertEquals(2, br.getReadCounter());
        assertEquals(2, br.getRemainingCharacters());
        assertEquals(4, br.getAllowedCharacters());

        assertTrue(br.read(new char[1], 0, 1)!=-1);
        assertEquals(3, br.getReadCounter());
        assertEquals(1, br.getRemainingCharacters());
        assertEquals(4, br.getAllowedCharacters());

        assertTrue(br.skip(1)!=-1);
        assertEquals(4, br.getReadCounter());
        assertEquals(0, br.getRemainingCharacters());
        assertEquals(4, br.getAllowedCharacters());
    }

    /**
     * Tests a <code>BoundedReader</code> operating in bounded mode.
     *
     * @throws IOException should not happen
     */
    public void testBoundedReaderWithException() throws IOException {
        BoundedReader br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), 4, new StreamOutOfBoundException(4));
        prepareBoundedReader(br);

        boolean exceptionThrown = false;
        try { br.read(); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try { br.read(new char[1]); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try { br.read(new char[1], 0, 1); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try { br.skip(1); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        assertEquals(4, br.getReadCounter());
        assertEquals(0, br.getRemainingCharacters());
        assertEquals(4, br.getAllowedCharacters());

        // Attempt to read a chunk larger than the remaining characters and assert that it does not throw a StreamOutOfBoundException
        br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), 4);
        assertTrue(br.read(new char[6])!=-1);
    }

    /**
     * Tests a <code>BoundedReader</code> operating in bounded mode.
     *
     * @throws IOException should not happen
     */
    public void testBoundedReaderWithoutException() throws IOException {
        BoundedReader br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), 4);
        prepareBoundedReader(br);

        assertEquals(-1, br.read());
        assertEquals(-1, br.read(new char[1]));
        assertEquals(-1, br.read(new char[1], 0, 1));
        assertEquals(-1, br.skip(1));

        assertEquals(4, br.getReadCounter());
        assertEquals(0, br.getRemainingCharacters());
        assertEquals(4, br.getAllowedCharacters());

        // Attempt to read a chunk larger than the remaining characters and assert that it does not return -1
        br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), 4);
        assertTrue(br.read(new char[6])!=-1);
    }

    /**
     * Tests a <code>BoundedReader</code> operating in unbounded mode.
     *
     * @throws IOException should not happen
     */
    public void testUnboundedReader() throws IOException {
        BoundedReader br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), -1);
        assertEquals(0, br.getReadCounter());
        assertEquals(Long.MAX_VALUE, br.getRemainingCharacters());
        assertEquals(-1, br.getAllowedCharacters());

        assertTrue(br.read()!=-1);
        assertEquals(1, br.getReadCounter());
        assertEquals(Long.MAX_VALUE, br.getRemainingCharacters());
        assertEquals(-1, br.getAllowedCharacters());

        assertTrue(br.read(new char[1])!=-1);
        assertEquals(2, br.getReadCounter());
        assertEquals(Long.MAX_VALUE, br.getRemainingCharacters());
        assertEquals(-1, br.getAllowedCharacters());

        assertTrue(br.read(new char[1], 0, 1)!=-1);
        assertEquals(3, br.getReadCounter());
        assertEquals(Long.MAX_VALUE, br.getRemainingCharacters());
        assertEquals(-1, br.getAllowedCharacters());

        long totalRead = 0;
        while(br.read()!=-1)
            totalRead ++;

        assertEquals(8, totalRead);
    }
}
