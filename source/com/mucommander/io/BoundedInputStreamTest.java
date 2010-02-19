/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * A test case for {@link com.mucommander.io.BoundedInputStream}.
 *
 * @see com.mucommander.io.BoundedInputStream 
 * @author Maxence Bernard
 */
public class BoundedInputStreamTest extends TestCase {

    private final static byte[] TEST_BYTES = new byte[]{0x6d, 0x75, 0x63, 0x6f, 0x6d, 0x6d, 0x61, 0x6e, 0x64, 0x65, 0x72};


    /**
     * Performs some tests that are common to {@link #testBoundedStreamWithException()} and
     * {@link #testBoundedStreamWithoutException()}.
     *
     * @param bin the BoundedInputStream to prepare
     * @throws IOException should not happen
     */
    private void prepareBoundedStream(BoundedInputStream bin) throws IOException {
        assertEquals(0, bin.getProcessedBytes());
        assertEquals(4, bin.getRemainingBytes());
        assertEquals(4, bin.getAllowedBytes());

        assertTrue(bin.read()!=-1);
        assertEquals(1, bin.getProcessedBytes());
        assertEquals(3, bin.getRemainingBytes());
        assertEquals(4, bin.getAllowedBytes());

        assertTrue(bin.read(new byte[1])!=-1);
        assertEquals(2, bin.getProcessedBytes());
        assertEquals(2, bin.getRemainingBytes());
        assertEquals(4, bin.getAllowedBytes());

        assertTrue(bin.read(new byte[1], 0, 1)!=-1);
        assertEquals(3, bin.getProcessedBytes());
        assertEquals(1, bin.getRemainingBytes());
        assertEquals(4, bin.getAllowedBytes());

        assertTrue(bin.skip(1)!=-1);
        assertEquals(4, bin.getProcessedBytes());
        assertEquals(0, bin.getRemainingBytes());
        assertEquals(4, bin.getAllowedBytes());
    }

    /**
     * Tests a <code>BoundedInputStream</code> operating in bounded mode and throwing a {@link StreamOutOfBoundException}.
     *
     * @throws IOException should not happen
     */
    public void testBoundedStreamWithException() throws IOException {
        BoundedInputStream bin = new BoundedInputStream(new ByteArrayInputStream(TEST_BYTES), 4, true);
        prepareBoundedStream(bin);

        boolean exceptionThrown = false;
        try { bin.read(); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try { bin.read(new byte[1]); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try { bin.read(new byte[1], 0, 1); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try { bin.skip(1); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);
        
        assertEquals(4, bin.getProcessedBytes());
        assertEquals(0, bin.getRemainingBytes());
        assertEquals(4, bin.getAllowedBytes());

        // Attempt to read a chunk larger than the remaining bytes and assert that it does not throw a StreamOutOfBoundException
        bin = new BoundedInputStream(new ByteArrayInputStream(TEST_BYTES), 4, true);
        assertTrue(bin.read(new byte[6])!=-1);

        assertEquals(4, bin.getProcessedBytes());
        assertEquals(0, bin.getRemainingBytes());
        assertEquals(4, bin.getAllowedBytes());
    }

    /**
     * Tests a <code>BoundedInputStream</code> operating in bounded mode and returning <code>-1/</code>.
     *
     * @throws IOException should not happen
     */
    public void testBoundedStreamWithoutException() throws IOException {
        BoundedInputStream bin = new BoundedInputStream(new ByteArrayInputStream(TEST_BYTES), 4, false);
        prepareBoundedStream(bin);

        assertEquals(-1, bin.read());
        assertEquals(-1, bin.read(new byte[1]));
        assertEquals(-1, bin.read(new byte[1], 0, 1));
        assertEquals(-1, bin.skip(1));

        assertEquals(4, bin.getProcessedBytes());
        assertEquals(0, bin.getRemainingBytes());
        assertEquals(4, bin.getAllowedBytes());

        // Attempt to read a chunk larger than the remaining bytes and assert that it does not return -1
        bin = new BoundedInputStream(new ByteArrayInputStream(TEST_BYTES), 4, false);
        assertTrue(bin.read(new byte[6])!=-1);
    }

    /**
     * Tests a <code>BoundedInputStream</code> operating in unbounded mode.
     *
     * @throws IOException should not happen
     */
    public void testUnboundedStream() throws IOException {
        BoundedInputStream bin = new BoundedInputStream(new ByteArrayInputStream(TEST_BYTES), -1, false);
        assertEquals(0, bin.getProcessedBytes());
        assertEquals(Long.MAX_VALUE, bin.getRemainingBytes());
        assertEquals(-1, bin.getAllowedBytes());

        assertTrue(bin.read()!=-1);
        assertEquals(1, bin.getProcessedBytes());
        assertEquals(Long.MAX_VALUE, bin.getRemainingBytes());
        assertEquals(-1, bin.getAllowedBytes());

        assertTrue(bin.read(new byte[1])!=-1);
        assertEquals(2, bin.getProcessedBytes());
        assertEquals(Long.MAX_VALUE, bin.getRemainingBytes());
        assertEquals(-1, bin.getAllowedBytes());

        assertTrue(bin.read(new byte[1], 0, 1)!=-1);
        assertEquals(3, bin.getProcessedBytes());
        assertEquals(Long.MAX_VALUE, bin.getRemainingBytes());
        assertEquals(-1, bin.getAllowedBytes());

        long totalRead = 0;
        while(bin.read()!=-1)
            totalRead ++;

        assertEquals(8, totalRead);
    }
}
