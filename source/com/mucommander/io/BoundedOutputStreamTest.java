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

import java.io.IOException;

/**
 * A test case for {@link com.mucommander.io.BoundedOutputStream}.
 *
 * @see com.mucommander.io.BoundedOutputStream
 * @author Maxence Bernard
 */
public class BoundedOutputStreamTest extends TestCase {

    /**
     * Tests a <code>BoundedOutputStream</code> operating in bounded mode.
     *
     * @throws IOException should not happen
     */
    public void testBoundedStreamWithException() throws IOException {
        BoundedOutputStream bout = new BoundedOutputStream(new SinkOutputStream(), 4);

        assertEquals(0, bout.getProcessedBytes());
        assertEquals(4, bout.getRemainingBytes());
        assertEquals(4, bout.getAllowedBytes());

        bout.write(0);
        assertEquals(1, bout.getProcessedBytes());
        assertEquals(3, bout.getRemainingBytes());
        assertEquals(4, bout.getAllowedBytes());

        bout.write(new byte[1]);
        assertEquals(2, bout.getProcessedBytes());
        assertEquals(2, bout.getRemainingBytes());
        assertEquals(4, bout.getAllowedBytes());

        bout.write(new byte[2], 0, 2);
        assertEquals(4, bout.getProcessedBytes());
        assertEquals(0, bout.getRemainingBytes());
        assertEquals(4, bout.getAllowedBytes());

        boolean exceptionThrown = false;
        try { bout.write(0); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try { bout.write(new byte[1]); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try { bout.write(new byte[1], 0, 1); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assertTrue(exceptionThrown);

        assertEquals(4, bout.getProcessedBytes());
        assertEquals(0, bout.getRemainingBytes());
        assertEquals(4, bout.getAllowedBytes());

        // Attempt to write a chunk larger than the remaining bytes and assert that it does not throw a StreamOutOfBoundException
        bout = new BoundedOutputStream(new SinkOutputStream(), 4);
        bout.write(new byte[6]);

        assertEquals(4, bout.getProcessedBytes());
        assertEquals(0, bout.getRemainingBytes());
        assertEquals(4, bout.getAllowedBytes());
    }

    /**
     * Tests a <code>BoundedOutputStream</code> operating in unbounded mode.
     *
     * @throws IOException should not happen
     */
    public void testUnboundedStream() throws IOException {
        BoundedOutputStream bout = new BoundedOutputStream(new SinkOutputStream(), -1);

        assertEquals(0, bout.getProcessedBytes());
        assertEquals(Long.MAX_VALUE, bout.getRemainingBytes());
        assertEquals(-1, bout.getAllowedBytes());

        bout.write(0);
        assertEquals(1, bout.getProcessedBytes());
        assertEquals(Long.MAX_VALUE, bout.getRemainingBytes());
        assertEquals(-1, bout.getAllowedBytes());

        bout.write(new byte[1]);
        assertEquals(2, bout.getProcessedBytes());
        assertEquals(Long.MAX_VALUE, bout.getRemainingBytes());
        assertEquals(-1, bout.getAllowedBytes());

        bout.write(new byte[2], 0, 2);
        assertEquals(4, bout.getProcessedBytes());
        assertEquals(Long.MAX_VALUE, bout.getRemainingBytes());
        assertEquals(-1, bout.getAllowedBytes());
    }
}
