/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.commons.io;

import org.testng.annotations.Test;

import java.io.IOException;

/**
 * A test case for {@link com.mucommander.commons.io.BoundedOutputStream}.
 *
 * @see com.mucommander.commons.io.BoundedOutputStream
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class BoundedOutputStreamTest {

    /**
     * Tests a <code>BoundedOutputStream</code> operating in bounded mode.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testBoundedStreamWithException() throws IOException {
        BoundedOutputStream bout = new BoundedOutputStream(new SinkOutputStream(), 4);

        assert 0 == bout.getProcessedBytes();
        assert 4 == bout.getRemainingBytes();
        assert 4 == bout.getAllowedBytes();

        bout.write(0);
        assert 1 == bout.getProcessedBytes();
        assert 3 == bout.getRemainingBytes();
        assert 4 == bout.getAllowedBytes();

        bout.write(new byte[1]);
        assert 2 == bout.getProcessedBytes();
        assert 2 == bout.getRemainingBytes();
        assert 4 == bout.getAllowedBytes();

        bout.write(new byte[2], 0, 2);
        assert 4 == bout.getProcessedBytes();
        assert 0 == bout.getRemainingBytes();
        assert 4 == bout.getAllowedBytes();

        boolean exceptionThrown = false;
        try { bout.write(0); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assert exceptionThrown;

        exceptionThrown = false;
        try { bout.write(new byte[1]); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assert exceptionThrown;

        exceptionThrown = false;
        try { bout.write(new byte[1], 0, 1); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assert exceptionThrown;

        assert 4 == bout.getProcessedBytes();
        assert 0 == bout.getRemainingBytes();
        assert 4 == bout.getAllowedBytes();

        // Attempt to write a chunk larger than the remaining bytes and assert that it does not throw a StreamOutOfBoundException
        bout = new BoundedOutputStream(new SinkOutputStream(), 4);
        bout.write(new byte[6]);

        assert 4 == bout.getProcessedBytes();
        assert 0 == bout.getRemainingBytes();
        assert 4 == bout.getAllowedBytes();
    }

    /**
     * Tests a <code>BoundedOutputStream</code> operating in unbounded mode.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testUnboundedStream() throws IOException {
        BoundedOutputStream bout = new BoundedOutputStream(new SinkOutputStream(), -1);

        assert 0 == bout.getProcessedBytes();
        assert Long.MAX_VALUE == bout.getRemainingBytes();
        assert -1 == bout.getAllowedBytes();

        bout.write(0);
        assert 1 == bout.getProcessedBytes();
        assert Long.MAX_VALUE == bout.getRemainingBytes();
        assert -1 == bout.getAllowedBytes();

        bout.write(new byte[1]);
        assert 2 == bout.getProcessedBytes();
        assert Long.MAX_VALUE == bout.getRemainingBytes();
        assert -1 == bout.getAllowedBytes();

        bout.write(new byte[2], 0, 2);
        assert 4 == bout.getProcessedBytes();
        assert Long.MAX_VALUE == bout.getRemainingBytes();
        assert -1 == bout.getAllowedBytes();
    }
}
