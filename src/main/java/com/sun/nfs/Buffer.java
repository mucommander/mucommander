/*
 * Copyright (c) 1998, 2007 Sun Microsystems, Inc. 
 * All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
 * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
 * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
 * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed,licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package com.sun.nfs;

import java.io.*;
import com.sun.rpc.*;

/**
 * Here we do all the NFS read and write buffering.
 *
 * @see Nfs
 * @see Nfs2
 * @see Nfs3
 * @author Brent Callaghan
 */

public class Buffer extends Thread {

    Nfs nfs;
    long foffset;

    byte[] buf;		// The buffer itself
    int bufoff;		// Offset into the buffer
    int buflen;		// Bytes in buffer
    int bufsize;	// Size of buffer

    int minOffset;	// First byte written
    int maxOffset;	// Last byte written

    int status;
    private int action;
    boolean eof;
    IOException e;
    Error err;
    long writeVerifier;
    int syncType;

    // Various kinds of action

    private final static int IDLE   = 0;
    private final static int LOAD   = 1;
    private final static int UNLOAD = 2;
    private final static int EXIT   = 3;

    /*
     * The initial state of a buffer is EMPTY.
     * When file data is read into a file it becomes LOADED.
     * If the buffer contains data that has not yet been written
     * to the file then it is DIRTY.
     * The COMMIT state indicates that the data is written but
     * not yet committed.  Once committed, the state returns
     * to LOADED.
     */
    final static int EMPTY  = 0;	// Has no data
    final static int LOADED = 1;	// Has file data
    final static int DIRTY  = 2;	// Has new data
    final static int COMMIT = 3;	// Not committed

    public Buffer(Nfs nfs, int foffset, int bufsize) {
        this.nfs = nfs;
        this.foffset = foffset;
        this.bufsize = bufsize;
        this.buflen = 0;

        minOffset = bufsize;
        maxOffset = 0;

        setDaemon(true);	// NFS threads die when app exits
        try {
            setName("Buffer-" + (foffset / bufsize));
        } catch (Exception e) {}; // non-essential, ignore
        action = IDLE;
        start();
    }

    /*
     * Copy data from a buffer.
     * We assume the buffer is loaded with data
     */
    synchronized int copyFrom(byte[] buff, int boff, long foffset, int length)
        throws IOException {

        /*
         * May have gotten an async exception
         * so throw it here.
         */
        if (e != null)
            throw e;
        if (err != null)
            throw err;

        if (status == EMPTY)
            throw new IOException("no data");

        /*
         * We may have a partial buffer if the file
         * has since been extended by a write into
         * another buffer that is not yet unloaded.
         * We must make sure that the buffer is complete.
         */
        if (buflen < bufsize) {
            byte[] nbuf = new byte[bufsize]; // bigger buffer
            if (buflen > 0)
                System.arraycopy(buf, bufoff, nbuf, 0, buflen);
            buflen = bufsize;
            bufoff = 0;
            buf = nbuf;
        }
     
        int off = (int) (foffset - this.foffset);
        int copylen = Math.min(length, buflen - off);
        copylen = Math.min(copylen, (int) (nfs.length() - foffset));

        System.arraycopy(buf, bufoff + off, buff, boff, copylen);

        return copylen;
    }

    /*
     * Copy data to a buffer.
     * If the buffer maps to a valid offset of a file then first
     * make sure data is loaded from the file into the buffer.
     * Record the range of data modified in the buffer so that
     * when the buffer is written only the modified range is
     * written back to the server.
     */
    synchronized int copyTo(byte[] buff, int boff, long foffset, int length)
        throws IOException {

        /*
         * May have gotten an async exception
         * so throw it here.
         */
        if (e != null)
            throw e;
        if (err != null)
            throw err;

        int off = (int) (foffset - this.foffset);
        int copylen = Math.min(length, bufsize - off);

        /*
         * If writing less than a full buffer and if
         * overwriting existing file data, then make
         * sure the buffer is loaded from the file.
         */
        if (status == EMPTY) {
            long bufEnd = Math.min(nfs.length(), this.foffset + nfs.wsize);

            if (this.foffset < nfs.length() &&
                (foffset > this.foffset || foffset + length < bufEnd)) {
                startLoad();
                waitLoaded();
            }
        }

        /*
         * May need to extend the size of the buffer
         */
        if (off + copylen > buflen) {
            byte[] nbuf = new byte[bufsize];
            if (buf != null)
                System.arraycopy(buf, bufoff, nbuf, 0, buflen);
            buf = nbuf;
            bufoff = 0;
            buflen = bufsize;
        }

        System.arraycopy(buff, boff, buf, bufoff + off, copylen);

        status = DIRTY;

        /*
         * Record the range of the buffer that's been
         * modified so that we can write less than
         * the full buffer if possible.
         */
        if (off < minOffset)
            minOffset = off;

        if (off + copylen > maxOffset)
            maxOffset = off + copylen;

        return copylen;
    }

    /*
     * Notify the buffer thread that it is to read data
     */
    synchronized void startLoad() {
        action = LOAD;
        notifyAll();
    }

    /*
     * Wait until the buffer thread has finished loading the buffer
     */
    synchronized void waitLoaded() throws IOException {

        /*
         * Check for an exception thrown by the async thread
         * in case the thread died and we block forever
         * waiting for the buffer state to change.
         */
        if (e != null)
            throw e;
        if (err != null)
            throw err;

        while (this.status == EMPTY) {
            try {
                wait();
            } catch (InterruptedException e) {}

            if (this.e != null)
                throw this.e;
            if (err != null)
                throw err;
        }
    }

    /*
     * Wait until the buffer thread is finished writing the buffer
     */
    synchronized void waitUnloaded() throws IOException {

        /*
         * Check in case async thread threw an
         * exception and died.
         */
        if (e != null)
            throw e;
        if (err != null)
            throw err;

        while (action == UNLOAD) {
            try {
                wait();
            } catch (InterruptedException e) {}

            if (this.e != null)
                throw this.e;
            if (err != null)
                throw err;
        }
    }


    /*
     * Notify the buffer thread that it is to write data
     */
    synchronized void startUnload(int sync) {
        nfs.beginWrite();

        action = UNLOAD;
        syncType = sync;
        notifyAll();
    }

    /*
     * Request the buffer thread to exit cleanly
     */
    synchronized void exit() {

        action = EXIT;
        notifyAll();
    }

    /*
     * This is the run method for the buffer thread.
     * It is started when the buffer is instantiated
     * and sleeps on the monitor waiting to be woken
     * up and perform one of two actions: LOAD data
     * (write) or UNLOAD data (read).
     */
    public void run() {

        synchronized (this) {
            try {
                while (true) {

                    while (action == IDLE) {
                        try {
                            wait();
                        } catch (InterruptedException e) {}
                    }
    
            	    /*
            	     * Thread has been notified - perform the action
            	     */
                    switch (action) {
    
                    case LOAD:
                        try {
                            nfs.read_otw(this);
    
                        } catch (IOException e) {
                            if (this.e == null)
                                this.e = e;
                        }
    
                        status = LOADED;
                        break;
    
                    case UNLOAD:
                        try {
    
                            /*
                             * Server may do a short write, so keep
                             * writing until all the bytes have been
                             * written.
                             */
                            int saveMin = minOffset;
                            while (minOffset < maxOffset)
                                minOffset += nfs.write_otw(this);

                            minOffset = bufsize;
                            maxOffset = 0;
    
                        } catch (IOException e) {
                            if (this.e == null)
                                this.e = e;
                        }
                        nfs.endWrite();
                        break;

                    case EXIT:
                        notifyAll();

                        /*
                         * XXX Dereferencing the buf here should not be
                         * necessary since the entire buffer is dereferenced
                         * from the bufferList in Nfs, however for some reason
                         * the GC is ignoring dereferenced buffers.
                         * Setting buf to null makes sure that the GC collects
                         * the bulk of the memory tied up in a buffer, even
                         * if the Buffer object itself is not reclaimed.
                         */
                        buf = null;

                        return;
                    }
    
                    action = IDLE;
                    notifyAll();
                }
            } catch (Error e) {
		/*
		 * Need to catch errors here, e.g. OutOfMemoryError
		 * and notify threads before this thread dies
		 * otherwise they'll wait forever.
		 */
            	err = e;
            	notifyAll();
            	throw e;
            }
        }
    }

    public String toString() {
        return (nfs.name + " @ " + foffset + " for " + buflen);
    }

//    protected void finalize() throws Throwable {
//        System.out.println("#### BUFFER FINALIZE " + toString());
//        super.finalize();
//    }
}
