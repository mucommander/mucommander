/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.mucommander.io;


/**
 * Contains a number of bytes which have been read/written from/to a {@link CounterInputStream}/{@link CounterOutputStream}.
 *
 * <p>Provided methods allow to read the the byte count, add a number bytes to it or reset it (make it zero).
 *
 * <p>This class is thread safe, ensuring that the counter is always in a consistent state.</p>
 *
 * @see com.mucommander.io.CounterInputStream
 * @see com.mucommander.io.CounterOutputStream
 * @author Maxence Bernard
 */
public class ByteCounter {

    /** Byte count */
    private long count;

    /** Byte counter to add to the value returned by {@link #getByteCount()} */
    private ByteCounter addedCounter;

    
    /**
     * Creates a new ByteCounter with an initial byte count equal to zero.
     */
    public ByteCounter() {
    }

    /**
     * Creates a new ByteCounter with an initial byte count equal to zero and using the given ByteCounter.
     *
     * <p>The value returned by {@link #getByteCount()} will be the sum of the internal byte count and the one from
     * the specified ByteCounter, as returned by its {@link #getByteCount()} method. Resetting this ByteCounter's value
     * will only affect the internal byte count and not the one from the specified ByteCounter.
     */
    public ByteCounter(ByteCounter counter) {
        this.addedCounter = counter;
    }


    /**
     * Return the number of bytes which have been accounted for.
     */
    public synchronized long getByteCount() {
        if(addedCounter!=null)
            return count + addedCounter.getByteCount();

        return this.count;
    }


    /**
     * Increases the byte counter by the provided number of bytes. If the specified number is negative,
     * the byte counter will be left unchanged (won't be decreased).
     *
     * @param nbBytes number of bytes to add to the byte counter, will be ignored if negative
     */
    public synchronized void add(long nbBytes) {
        if(nbBytes>0)
            this.count += nbBytes;
    }


    /**
     * Increases the byte counter by the number of bytes contained in the specified counter (as returned by its
     * {@link #getByteCount()} method) and resets its byte counter after (if specified).
     *
     * @param counter the Bytecounter to add to this one, and reset after (if specified).
     * @param resetAfter if true, the specified counter will be reset after its byte count has been added to this ByteCounter
     */
    public synchronized void add(ByteCounter counter, boolean resetAfter) {
        // Hold a lock on the provided counter to make sure that it is not modified or accessed
        // while this operation is carried out
        synchronized(counter) {
            add(counter.getByteCount());
            if(resetAfter)
                counter.reset();
        }
    }


    /**
     * Resets the byte counter (make it zero).
     */
    public synchronized void reset() {
        this.count = 0;
    }
}