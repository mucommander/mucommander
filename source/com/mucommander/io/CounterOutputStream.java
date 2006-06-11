
package com.mucommander.io;

import java.io.*;


/**
 * An OutputStream that keeps track of the number of bytes that have been read and skipped from it.
 *
 * <p>The actual {@link ByteCounter} contains the number of bytes.
 * The {@link #CounterOutputStream(OutputStream, ByteCounter)} constructor can be used to specify an existing
 * <code>ByteCounter</code> instead of creating a new one. The ByteCounter will always remain accessible, even
 * after this stream has been closed.
 *
 * @see ByteCounter
 * @author Maxence Bernard
 */
public class CounterOutputStream extends OutputStream {

    /** Underlying OutputStream */
    private OutputStream out;
    /** Counter */
    private ByteCounter counter;

    
    /**
     * Creates a new CounterOutputStream using the specified OutputStream. A new {@link ByteCounter} will be created.
     *
     * @param out the underlying OutputStream the data will be written to
     */
    public CounterOutputStream(OutputStream out) {
        this.out = out;
        this.counter = new ByteCounter();
    }

    /**
     * Creates a new CounterOutputStream using the specified OutputStream and {@link ByteCounter}.
     * The provided <code>ByteCounter</code> will NOT be reset, whatever value it contains will be kept.
     *
     * @param out the underlying OutputStream the data will be written to
     */
    public CounterOutputStream(OutputStream out, ByteCounter counter) {
        this.out = out;
        this.counter = counter;
    }

    public ByteCounter getCounter() {
        return this.counter;
    }


    /////////////////////////////////
    // OutputStream implementation //
    /////////////////////////////////

    public void write(int b) throws IOException {
        out.write(b);
        counter.add(1);
    }

    public void write(byte b[]) throws IOException {
        out.write(b);
        counter.add(b.length);
    }
    
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        counter.add(len);
    }
    
    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }
}