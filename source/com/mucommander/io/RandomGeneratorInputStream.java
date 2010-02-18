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

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * This stream allows random data generated with a {@link Random} instance to be read.
 * It can be instantiated in one of the three following ways:
 * <ul>
 *   <li>with a pseudo-unique seed, leading to different random data being generated from one instance of this class
 * to the other.</li>
 *   <li>with a specified seed, leading to the same random data being generated from one instance of this class to
 * the other.</li>
 *   <li>with a specified {@link Random}.</li>
 * </ul>
 *
 * @author Maxence Bernard
 */
public class RandomGeneratorInputStream extends InputStream {

    /** Random data generator. */
    protected Random random;


    public RandomGeneratorInputStream() {
        this(new Random());
    }

    public RandomGeneratorInputStream(long seed) {
        this(new Random(seed));
    }

    public RandomGeneratorInputStream(Random random) {
        this.random = random;
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    @Override
    public int read() throws IOException {
        return random.nextInt();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public int read(byte[] b) throws IOException {
        random.nextBytes(b);
        return b.length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int end = off+len;
        for(int i=off; i<end; i++)
            b[i] = (byte)random.nextInt();

        return len;
    }

    /**
     * Always returns {@link Integer#MAX_VALUE}: this stream is bottomless.
     */
    @Override
    public int available() throws IOException {
        return Integer.MAX_VALUE;
    }

    // Note: use the default skip implementation, which calls read
}
