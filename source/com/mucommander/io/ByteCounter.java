
package com.mucommander.io;


/**
 * Contains a number of bytes which have been read/written from/to a {@link CounterInputStream}/{@link CounterOutputStream}.
 *
 * <p>Provided methods allow to read the the byte count, add a number bytes to it or reset it (make it zero).
 *
 * @see CounterInputStream
 * @see CounterOutputStream
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
     * Creates a new ByteCounter with an initial byte count equal to zero.
     * The value returned by {@link #getByteCount()} will be the sum of the internal byte count and the one from
     * the specified ByteCounter, as returned by its {@link #getByteCount()} method.
     */
    public ByteCounter(ByteCounter counter) {
        this.addedCounter = counter;
    }


    /**
     * Return the number of bytes which have been accounted for.
     */
    public long getByteCount() {
        return this.count;
    }


    /**
     * Increases the byte counter to the provided number of bytes. If the specified number is negative,
     * the byte counter will be left unchanged (won't be decreased).
     *
     * @param l number of bytes to add to the byte counter, will simply be ignored if negative
     */
    public void add(long nbBytes) {
        if(nbBytes>0)
            this.count += nbBytes;
    }


    /**
     * Resets the byte counter (make it zero).
     */
    public void reset() {
        this.count = 0;
    }
}