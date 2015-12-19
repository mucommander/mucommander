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

import java.io.CharArrayReader;
import java.io.IOException;

/**
 * A test case for {@link com.mucommander.commons.io.BoundedReader}.
 *
 * @see com.mucommander.commons.io.BoundedReader
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class BoundedReaderTest {

    private final static char[] TEST_CHARACTERS = new char[]{'m', 'u', 'c', 'o', 'm', 'm', 'a', 'n', 'd', 'e', 'r'};


    /**
     * Performs some tests that are common to {@link #testBoundedReaderWithException()} and
     * {@link #testBoundedReaderWithoutException()}.
     *
     * @param br the BoundedReader to prepare
     * @throws IOException should not happen
     */
    private void prepareBoundedReader(BoundedReader br) throws IOException {
        assert 0 == br.getReadCounter();
        assert 4 == br.getRemainingCharacters();
        assert 4 == br.getAllowedCharacters();

        assert br.read()!=-1;
        assert 1 == br.getReadCounter();
        assert 3 == br.getRemainingCharacters();
        assert 4 == br.getAllowedCharacters();

        assert br.read(new char[1])!=-1;
        assert 2 == br.getReadCounter();
        assert 2 == br.getRemainingCharacters();
        assert 4 == br.getAllowedCharacters();

        assert br.read(new char[1], 0, 1)!=-1;
        assert 3 == br.getReadCounter();
        assert 1 == br.getRemainingCharacters();
        assert 4 == br.getAllowedCharacters();

        assert br.skip(1)!=-1;
        assert 4 == br.getReadCounter();
        assert 0 == br.getRemainingCharacters();
        assert 4 == br.getAllowedCharacters();
    }

    /**
     * Tests a <code>BoundedReader</code> operating in bounded mode.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testBoundedReaderWithException() throws IOException {
        BoundedReader br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), 4, new StreamOutOfBoundException(4));
        prepareBoundedReader(br);

        boolean exceptionThrown = false;
        try { br.read(); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assert exceptionThrown;

        exceptionThrown = false;
        try { br.read(new char[1]); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assert exceptionThrown;

        exceptionThrown = false;
        try { br.read(new char[1], 0, 1); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assert exceptionThrown;

        exceptionThrown = false;
        try { br.skip(1); }
        catch(StreamOutOfBoundException e) { exceptionThrown = true; }

        assert exceptionThrown;

        assert 4 == br.getReadCounter();
        assert 0 == br.getRemainingCharacters();
        assert 4 == br.getAllowedCharacters();

        // Attempt to read a chunk larger than the remaining characters and assert that it does not throw a StreamOutOfBoundException
        br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), 4);
        assert br.read(new char[6])!=-1;
    }

    /**
     * Tests a <code>BoundedReader</code> operating in bounded mode.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testBoundedReaderWithoutException() throws IOException {
        BoundedReader br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), 4);
        prepareBoundedReader(br);

        assert -1 == br.read();
        assert -1 == br.read(new char[1]);
        assert -1 == br.read(new char[1], 0, 1);
        assert -1 == br.skip(1);

        assert 4 == br.getReadCounter();
        assert 0 == br.getRemainingCharacters();
        assert 4 == br.getAllowedCharacters();

        // Attempt to read a chunk larger than the remaining characters and assert that it does not return -1
        br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), 4);
        assert br.read(new char[6])!=-1;
    }

    /**
     * Tests a <code>BoundedReader</code> operating in unbounded mode.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testUnboundedReader() throws IOException {
        BoundedReader br = new BoundedReader(new CharArrayReader(TEST_CHARACTERS), -1);
        assert 0 == br.getReadCounter();
        assert Long.MAX_VALUE == br.getRemainingCharacters();
        assert -1 == br.getAllowedCharacters();

        assert br.read()!=-1;
        assert 1 == br.getReadCounter();
        assert Long.MAX_VALUE == br.getRemainingCharacters();
        assert -1 == br.getAllowedCharacters();

        assert br.read(new char[1])!=-1;
        assert 2 == br.getReadCounter();
        assert Long.MAX_VALUE == br.getRemainingCharacters();
        assert -1 == br.getAllowedCharacters();

        assert br.read(new char[1], 0, 1)!=-1;
        assert 3 == br.getReadCounter();
        assert Long.MAX_VALUE == br.getRemainingCharacters();
        assert -1 == br.getAllowedCharacters();

        long totalRead = 0;
        while(br.read()!=-1)
            totalRead ++;

        assert 8 == totalRead;
    }
}
