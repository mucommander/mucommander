package com.mucommander.io;

import com.mucommander.Debug;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author Maxence Bernard
 */
public class ThroughputLimitInputStream extends InputStream {

    /** Underlying InputStream */
    private InputStream in;

    /** Throughput limit in bytes per second, -1 for no limit, 0 to completely block reading */
    private long bpsLimit;

    private long currentSecond;
    private long bytesReadThisSecond;


    /**
     * Creates a new ThroughputLimitInputStream with no initial throughput limit.
     *
     * @param in underlying stream that is used to read data from
     */
    public ThroughputLimitInputStream(InputStream in) {
        this.in = in;
        this.bpsLimit = -1;
    }

    /**
     * Creates a new ThroughputLimitInputStream with an initial throughput limit.
     *
     * @param in underlying stream that is used to read data from
     * @param bytesPerSecond initial throughput limit in bytes per second
     * @see #setThroughputLimit(long)
     */
    public ThroughputLimitInputStream(InputStream in, long bytesPerSecond) {
        this.in = in;
        this.bpsLimit = bytesPerSecond;
    }


    /**
     * Specifies a new throughput limit expressed in bytes per second.
     * The new limit will be effective immediately the next time one of the read methods are called.
     *
     * <p>Specifying 0 will completely block reads, any calls to the read methods will not return,
     * unless the {@link #setThroughputLimit(long)} is called from another thread.
     *
     * <p>Specifying -1 or any other negative value will disable any current throughput limit.
     *
     * @param bytesPerSecond new throughput limit expressed in bytes, -1 to disable it, 0 to block reads.
     */
    public void setThroughputLimit(long bytesPerSecond) {
        this.bpsLimit = bytesPerSecond;

        synchronized(this) {
            notify();
        }
    }


    private int getNbAllowedBytes() {
//if(Debug.ON) Debug.trace("called by thread "+Thread.currentThread()+" activeCount="+Thread.activeCount());

        updateLimitCounter();

        long allowedBytes;

//if(Debug.ON) Debug.trace("obtaining lock");

        synchronized(this) {
            while((allowedBytes=bpsLimit-bytesReadThisSecond)<=0) {
//if(Debug.ON) Debug.trace("looping");

                // Throughput limit was removed, return max int value
                if(bpsLimit<0)
                    return Integer.MAX_VALUE;

//if(Debug.ON) Debug.trace("waiting");

                try {
                    if(bpsLimit==0)
                        wait();
                    else
                        wait(System.currentTimeMillis()-currentSecond*1000);
                }
                catch(InterruptedException e) {
//if(Debug.ON) Debug.trace("interrupted!");
                }

                updateLimitCounter();
            }
        }

//if(Debug.ON) Debug.trace("getting out");
        return (int)allowedBytes;
    }


    private void updateLimitCounter() {
        long nowSecond = System.currentTimeMillis()/1000;

        if(this.currentSecond!=nowSecond) {
            this.currentSecond = nowSecond;
            this.bytesReadThisSecond = 0;
        }
    }


    private void addToLimitCounter(long nbRead) {
        updateLimitCounter();

        this.bytesReadThisSecond += nbRead;
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    public int read() throws IOException {
        if(bpsLimit>=0)
            getNbAllowedBytes();

        int i = in.read();

        if(i>0)
            addToLimitCounter(1);

        return i;
    }

    public int read(byte[] bytes) throws IOException {
        return this.read(bytes, 0, bytes.length);
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        int nbRead = in.read(bytes, off, bpsLimit>=0?Math.min(getNbAllowedBytes(),len):len);

        if(nbRead>0)
            addToLimitCounter(nbRead);

        return nbRead;
    }

    public long skip(long l) throws IOException {
        long nbSkipped = in.skip(bpsLimit>=0?Math.min(getNbAllowedBytes(),l):l);

        if(nbSkipped>0)
            addToLimitCounter(nbSkipped);

        return nbSkipped;
    }

    public int available() throws IOException {
        return in.available();
    }

    public void close() throws IOException {
        in.close();
    }

    public synchronized void mark(int i) {
        in.mark(i);
    }

    public synchronized void reset() throws IOException {
        in.reset();
    }            

    public boolean markSupported() {
        return in.markSupported();
    }
}
