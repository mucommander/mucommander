
package com.mucommander.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * An InputStream that keeps track of the number of bytes that have been written to it.
 *
 * <p>The actual {@link ByteCounter} contains the number of bytes.
 * The {@link #CounterInputStream(InputStream, ByteCounter)} constructor can be used to specify an existing
 * <code>ByteCounter</code> instead of creating a new one. The ByteCounter will always remain accessible, even
 * after this stream has been closed.
 *
 * @see ByteCounter
 * @author Maxence Bernard
 */
public class CounterInputStream extends InputStream {

    /** Underlying InputStream */
    private InputStream in;
    /** Counter */
    private ByteCounter counter;

    
    /**
     * Creates a new CounterInputStream using the specified InputStream. A new {@link ByteCounter} will be created.
     *
     * @param in the underlying InputStream the data will be read from
     */
    public CounterInputStream(InputStream in) {
        this.in = in;
        this.counter = new ByteCounter();
    }

    /**
     * Creates a new CounterInputStream using the specified InputStream and {@link ByteCounter}.
     * The provided <code>ByteCounter</code> will NOT be reset, whatever value it contains will be kept.
     *
     * @param in the underlying InputStream the data will be read from
     */
    public CounterInputStream(InputStream in, ByteCounter counter) {
        this.in = in;
        this.counter = counter;
    }

    public ByteCounter getCounter() {
        return this.counter;
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    public int read() throws IOException {
        int nbRead = in.read();
        counter.add(nbRead);
        
        return nbRead;
    }

    
    public int read(byte b[]) throws IOException {
        int nbRead = in.read(b);
        counter.add(nbRead);
                
        return nbRead;
    }
    
    public int read(byte b[], int off, int len) throws IOException {
        int nbRead = in.read(b, off, len);
        counter.add(nbRead);
        
        return nbRead;
    }
    
    public long skip(long n) throws IOException {
        long nbSkipped = in.skip(n);
        counter.add(nbSkipped);
        
        return nbSkipped;
    }

    
    public int available() throws IOException {
        return in.available();
    }


    public void close() throws IOException {
        in.close();
    }


    public void mark(int readLimit) {
        in.mark(readLimit);
    }
    
    
    public boolean markSupported() {
        return in.markSupported();
    }


    public void reset() throws IOException  {
        in.reset();
    }
}