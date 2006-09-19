package com.mucommander.io;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author Maxence Bernard
 */
public class ByteLimitInputStream extends FilterInputStream {

    private long bytesRemaining;

    public ByteLimitInputStream(InputStream in, long size) {
        super(in);
        this.bytesRemaining = size;
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    public int read() throws IOException {
        if(bytesRemaining<=0)
            return -1;  // EOF reached

        int i = in.read();
        if(i>0)
            this.bytesRemaining--;

        return i;
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        if(bytesRemaining<=0)
            return -1;  // EOF reached

        int nbRead = in.read(b, off, Math.min(len, (int)this.bytesRemaining));
        if(nbRead>0)
            this.bytesRemaining -= nbRead;

        return nbRead;
    }


    public long skip(long n) throws IOException {
        if(bytesRemaining<=0)
            return -1;  // EOF reached

        long nbSkipped = in.skip(Math.min(n, (int)this.bytesRemaining));
        if(nbSkipped>0)
            this.bytesRemaining -= nbSkipped;

        return nbSkipped;
    }


    public int available() throws IOException {
        return (int)this.bytesRemaining;
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
