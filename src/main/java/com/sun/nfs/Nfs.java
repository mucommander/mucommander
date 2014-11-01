/*
 * Copyright (c) 1997-1999, 2007 Sun Microsystems, Inc. 
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
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * Container class for an NFS object: either a file
 * or a directory. Herein are common
 * methods that are not version specific.
 *
 * This class holds the file's filehandle, name,
 * and attributes. If a regular file then data may
 * be cached in an XDR buffer.  If a directory then
 * the string array for the entries will be cached.
 * There's also a static hash table that's used to cache
 * these Nfs objects.
 *
 * @see Nfs2
 * @see Nfs3
 * @see Buffer
 * @author Brent Callaghan
 * @author Ricardo Labiaga
 */
public abstract class Nfs {
    byte[] fh;
    Rpc rpc;
    String name;
    String[] dircache;
    String symlink;
    Buffer[] bufferList;
    long cacheTime;		// Time when object was cached
    int rsize, wsize;
    private Object wbLock = new Object(); // write-behind semaphore lock
    static Hashtable cacheNfs = new Hashtable();

    // Some of the filetypes we're dealing with.

    static final int NFREG = 1;
    static final int NFDIR = 2;
    static final int NFLNK = 5;

    // Flags for asynchronous or synchronous writes

    private final static int ASYNC = 0;
    private final static int SYNC  = 2;

    int NRA;	// max reads-ahead      (set in subclass constructor)
    int NWB;	// max writes-behind    (")
    int NWC;	// max writes committed (")
    int nwb;	// current writes-behind
    int prevReadIndex  = -1;	// Buffer index of previous read
    int prevWriteIndex = -1;	// Buffer index of previous write
    int maxIndexRead = 0;	// Max file offset read
    long maxLength = 0;		// Size of file

    // Some important permission bits

    static final int RBIT = 004;
    static final int WBIT = 002;

    /*
     * The following abstract classes are version-specific
     * and are implemented in the version subclasses.
     */

    abstract void checkAttr() throws IOException;

    abstract boolean cacheOK(long t) throws IOException;

    abstract void getattr() throws IOException;

    abstract long mtime() throws IOException;

    abstract long length() throws IOException;

    abstract boolean exists() throws IOException;

    abstract boolean canWrite() throws IOException;

    abstract boolean canRead() throws IOException;

    abstract boolean isFile() throws IOException;

    abstract boolean isDirectory() throws IOException;

    abstract boolean isSymlink() throws IOException;

    abstract Fattr getAttr() throws IOException;

    abstract Nfs lookup(String path) throws IOException;

    abstract String lookupSec() throws IOException;

    abstract void read_otw(Buffer b) throws IOException;

    abstract int write_otw(Buffer buf) throws IOException;

    abstract String[] readdir() throws IOException;

    abstract String readlink() throws IOException;

    abstract Nfs create(String name, long mode) throws IOException;

    abstract Nfs mkdir(String name, long mode) throws IOException;

    abstract boolean remove(String name) throws IOException;

    abstract boolean rename(Nfs dstP, String sName, String dName) throws IOException;

    abstract boolean rmdir(String name) throws IOException;

    abstract void fsinfo() throws IOException;

    abstract long commit(int foffset, int length) throws IOException;

    abstract void invalidate();
	    

    /*
     * The following methods are all NFS version independent.
     */

    /*
     * Get FileHandle for Nfs Object
     */
    byte[] getFH() {
	return (fh);
    }

    /*
     * Cache an Nfs object
     *
     * @param n	the object to be cached
     */
    static void cache_put(Nfs n) {
        cacheNfs.put(n.rpc.conn.server + ":" + n.name, n);
    }

    /*
     * Retrieve a cached Nfs object
     *
     * @param server	The server that hosts the object
     * @param name	The pathname of the object
     * @returns		The object - or null if not cached
     */
    static Nfs cache_get(String server, String name) {
        return ((Nfs)cacheNfs.get(server + ":" + name));
    }

    /*
     * Remove an Nfs object from the cache
     *
     * @param n	the object to be removed from cache
     */
    static void cache_remove(Nfs n, String name) {
	if (n.name.equals(".")) 
       	    cacheNfs.remove(n.rpc.conn.server + ":" + name);
	else
            cacheNfs.remove(n.rpc.conn.server + ":" + n.name + "/" + name);
    }

    /**
     * Read data from the specified file offset
     *
     * @param buf	The destination buffer
     * @param boff	Offset into the dest buffer
     * @param length	Amount of data to read
     * @param foffset	File offset to begin reading
     * @exception	java.io.IOException
     * @return		actual bytes read
     */
    synchronized int read(byte[] buf, int boff, int length, long foffset)
        throws IOException {

        Buffer b = null;
        int index;
	int readAhead = 0;
	int bytesRead = 0;

        /*
         * If the file modification time has changed since
         * the last read then invalidate all cached buffers.
         */
        if (!cacheOK(cacheTime) && bufferList != null) {
            for (int i = 0; i < bufferList.length; i++)
                if (i != prevWriteIndex)	// don't delete dirty buffers
                    bufferList[i] = null;

            prevReadIndex = -1;
        }

	/*
	 * Check whether we're at EOF
	 */
	if (foffset >= length())
	    return -1;

	/*
	 * Keep reading until the read request is satisfied.
 	 */
        while (length > 0) {

	    /*
	     * Check whether we're at EOF
	     */
	    if (foffset >= length())
		break;

            /*
             * Make sure an array of buffers exists that's big enough
             * to for the entire file.
             */
            if (bufferList == null)
                bufferList = new Buffer[(int) length() / rsize + 1];

	    /*
	     * Find the block that holds the data
	     */
            index = (int) foffset / rsize;
            if (index > maxIndexRead)
                maxIndexRead = index;

	    /*
	     * Make sure that previously read buffers are
	     * released.  If not, then reading a large file
	     * would quickly run the app out of memory, though
             * must be careful not to release in-use write buffers.
             * XXX We should find a way to make better use of
	     * available memory and keep file buffers cached.
	     */
            if (index != prevReadIndex) {
		if (prevReadIndex >= 0 && prevReadIndex != prevWriteIndex) {
                    b = bufferList[prevReadIndex];
                    if (b.status == b.LOADED) {
                        bufferList[prevReadIndex] = null;
                        b.exit();
                    }

		    /*
		     * Do read-ahead only for sequential I/O
		     */
		    if (index == (prevReadIndex + 1) && index >= maxIndexRead)
			readAhead = NRA;
		}
		prevReadIndex = index;
	    }

            /*
             * Make sure that the buffer is
             * are loaded or loading - as well as
             * any buffers that will likely be needed
	     * i.e. read-ahead buffers.
             */
            for (int n = index; n <= index + readAhead; n++) {

                if (n >= bufferList.length)
                    break;

                b = bufferList[n];
                if (b == null) {
                    b = new Buffer(this, n * rsize, rsize);
                    b.startLoad();
                    bufferList[n] = b;
                }
            }

            /*
             * Now select the buffer and wait until its not busy.
             */
            b = bufferList[index];
            try {
                b.waitLoaded();
            } catch (NfsException n) {
                /*
                 * Check if it's a bogus "EBADRPC"
		 * error from a Digital Unix server.
                 * It implies that the read was too
                 * long.  The server should just return
                 * a short read - but until they fix it
                 * we'll handle it here.
                 * Optimistically set the read
                 * size to 8k and try again.
                 */
                if (n.error == 72) { // DEC's EBADRPC
                    rsize = 8192;
                    bufferList = 
                        new Buffer[(int) length() / rsize + 1];
                    continue;
                }

                throw n;
            }

            /*
             * If the buffer contains less data than requested
             * and it's not EOF, then assume that we guessed
             * too big for the server's transfer size.
             */
            int bufflen = b.buflen;
            if (bufflen < rsize && !b.eof) {
		rsize = bufflen;
                bufferList = null;
                prevReadIndex  = -1;
                prevWriteIndex = -1;

                continue;	// Try again with new rsize
            }

	    /*
	     * Copy data from the file buffer into the application buffer.
	     */
            int cc = b.copyFrom(buf, boff, foffset, length);

            boff += cc;
            foffset += cc;
            length -= cc;
	    bytesRead += cc;
        }

        return (bytesRead);
    }

    /*
     * These two methods implement a semaphore to prevent the client from
     * generating an huge number of write-behind threads that could
     * overload the server.
     *
     * These methods synchronize on wbLock rather than the
     * class monitor otherwise there's a risk of deadlock
     * through Nfs.write() -> Buffer.startUnload() -> Nfs.beginWrite()
     */
    void beginWrite() {
        synchronized (wbLock) {
            while (nwb >= NWB) {
                try {
                    wbLock.wait();
                } catch (InterruptedException e) {}
            }
            nwb++;
        }
    }

    void endWrite() {
        synchronized (wbLock) {
            nwb--;
            wbLock.notify();
        }
    }

    /**
     * Write data to a file at a specified offset.
     *
     * @param buf	The data to write
     * @param boff	Offset into the data buffer
     * @param length	Amount of data to write
     * @param foffset	File offset to begin writing at
     * @exception	java.io.IOException
     */
    synchronized void write(byte buf[], int boff, int length, long foffset)
	throws IOException {

        /*
         * If the write size is not set then call FSINFO
         * to set it. We would prefer not to make this call
         * since it adds an extra turnaround, but the alternative
         * is to guess at the write size by trying a large write
         * and see how many bytes the server writes. If we're doing
         * async write-behind it'll take complex code to recover
         * from a series of partial writes that would wreak havoc with
         * any write gathering that the server might be doing. So
         * it's likely safer just to have the server tell us its
         * preferred write size as the protocol intended.
         */
        if (wsize == 0)
            fsinfo();

        /*
         * If we haven't read the file yet then there may
         * be no buffer list.  Allocate one that will hold
         * all of the existing file blocks, or if it's a newly
         * created file, assume an initial size of 50 blocks.
         */
        if (bufferList == null) {
            long fileSize = Math.max(length(), 50 * wsize);
            bufferList = new Buffer[(int) fileSize / wsize + 1];
        }

        /*
         * Keep writing data to the server in buffer-size chunks
         * until the write request is satisfied.  If the write
         * is a short one into an existing buffer then no data
         * will be written at all.  This is good, it's much more
	 * efficient to write larger amounts of data to the server.
         *
         * We get further improvement in write throughput by writing
	 * buffers asynchronously in a buffer thread.  This allows the
	 * application to continue filling a new buffer while previous
	 * buffers are written.
	 *
	 * This method takes advantage of the ability of NFS version 3
	 * to perform safe, asynchronous writes which significantly
	 * increase write throughput.
	 */
        while (length > 0) {

            int index = (int) foffset / wsize;

            /*
             * If writing into a new buffer
             * start writing out the previous one.
             */
            if (index != prevWriteIndex) {
                if (prevWriteIndex >= 0) {
                    bufferList[prevWriteIndex].startUnload(ASYNC);

                    checkCommit(false);
                }
                prevWriteIndex = index;
            }

            /*
             * If trying to write to a buffer off the end of
             * the current buffer list, then double the size
             * of the buffer list.
             */
            if (index >= bufferList.length) {
                Buffer[] tlist = new Buffer[bufferList.length * 2];
                for (int i = 0; i < bufferList.length; i++)
                    tlist[i] = bufferList[i];
                bufferList = tlist;
            }


            /*
             * Check if there's a buffer allocated
             */
            Buffer b = bufferList[index];
            if (b == null) {
                b = new Buffer(this, index * wsize, wsize);
                bufferList[index] = b;
            }

	    /*
	     * Copy data from the application buffer to the file buffer.
	     */
            int cc = b.copyTo(buf, boff, foffset, length);

            boff += cc;
            foffset += cc;
            length -= cc;

	    /*
	     * Need to record max file offset here in case
	     * the app calls length() before the data has
	     * been written out and recorded in the file attrs.
	     */
	    if (foffset > maxLength)
		maxLength = foffset;

        } // end while
    }

    /*
     * Check the buffer list for buffers that should be released.
     * Buffers must be released otherwise the entire file will
     * become cached and we risk running out of memory.
     *
     * The same scan also checks for buffers that are pending
     * commit.  If it's a v2 server then there will be none,
     * but if v3 and there are more than NWC of these then
     * send a COMMIT request.  Until these buffers are committed
     * they cannot be released.  The scan records the range of
     * buffers pending commit for the benefit of the COMMIT
     * request which requires an offset and range.
     *
     * This method is called with flushing set to true when
     * the file is being closed.  In this case the code must
     * write the current buffer and wait for all write operations
     * to complete.
     */ 
    void checkCommit(boolean flushing) throws IOException {

        int minIndex = Integer.MAX_VALUE;
        int maxIndex = 0;
        int nwc = 0;

        /*
         * Determine the first and last buffers in
         * the buffer list that are waiting commit.
         * Then we know the byte range to be committed.
         *
         * Also, release any LOADED buffers.
         */
        for (int i = 0; i < bufferList.length; i++) {
            Buffer b = bufferList[i];
            if (b != null) {
                if (flushing)
                    b.waitUnloaded();

                if (b.status == b.LOADED) {

                    /*
                     * Don't throw away the "current" buffer
                     */
                    if (i == prevReadIndex || i == prevWriteIndex)
                        continue;

                    bufferList[i] = null;
                    b.exit();
                } else if (b.status == b.COMMIT) {
                    nwc++;
                    if (i < minIndex)
                        minIndex = i;
                    if (i > maxIndex)
                        maxIndex = i;
                }
            }
        }

        /*
         * If flushing write the "current" buffer if it is dirty.
         * Here we catch writes to files no bigger than one
         * buffer.  It's better to do a single sync write than
         * do an async write followed by a commit for a single
         * buffer.
         */
        if (flushing) {
            Buffer b = bufferList[prevWriteIndex];
	    if (b != null) {
                if (b.status == b.DIRTY) {
                    if (nwc == 0) {		// just one - do it sync
                        b.startUnload(SYNC);
                        b.waitUnloaded();
                    } else {		// more than one - do it async
                        b.startUnload(ASYNC);
                        b.waitUnloaded();
    
    		        // Record the commit range
    
                        if (prevWriteIndex < minIndex)
                            minIndex = prevWriteIndex;
                        if (prevWriteIndex > maxIndex)
                            maxIndex = prevWriteIndex;
                    }
                }
	    }
        }

        /*
         * If writing to a v3 server then there may
         * be some buffers pending commit.
         * If the commit is successful the buffers can
         * be released.
         */
        if (nwc > 0 && (flushing || nwc >= NWC)) {
            int commitOffset = minIndex * rsize +
                bufferList[minIndex].minOffset;
            int commitLength = (maxIndex * rsize +
                bufferList[maxIndex].maxOffset) - commitOffset;
    
            long verf = commit(commitOffset, commitLength);

            /*
             * Check the write verifiers of the buffers
             * in the commit range.  If each verifier
             * matches then the buffer data are safe
             * and we can release the buffer.
             * If the verifier does not match its possible
             * that the server lost the data so rewrite
             * the buffer.
             */
            for (int i = minIndex; i <= maxIndex; i++) {
                Buffer b = bufferList[i];
                if (b == null)
                    continue;

                if (flushing)
                    b.waitUnloaded();

                if (b.status == b.COMMIT) {

                    /*
                     * Can now release committed buffers with
                     * matching verifiers iff they're not "current"
                     */
                    if (b.writeVerifier == verf) {

                        if (i == prevReadIndex || i == prevWriteIndex) {
                            b.status = b.LOADED;
                            continue;
                        }

                        bufferList[i] = null;		// release buffer
                        b.exit();
                    } else {

			/*
			 * Have to rewrite.
			 *
			 * If flushing then do sync-writes because
			 * we can't return until the data are safe.
			 * Otherwise, we just fire off another async
			 * write and have it committed later.
			 */
                        if (flushing) {
                            b.startUnload(SYNC);
                            b.waitUnloaded();
                        } else {
                            b.startUnload(ASYNC);
                        }
                    }
                }
            } // end for
        }
    }

    /**
     * Flush any buffered writes to the file.  This must be
     * called after any series of writes to guarantee that the
     * data reach the server.
     * @exception java.io.IOException if writes failed for some reason, e.g.
     * if server ran out of disk space.
     */
    synchronized public void flush() throws IOException {
        if (prevWriteIndex >= 0)	// if no writes then don't bother
            checkCommit(true);
    }

    /**
     * Close the file by flushing data and
     * deallocating buffers.
     * @exception java.io.IOException if failure during flushing.
     */
    synchronized public void close() throws IOException {
        int n = 0;

        if (bufferList == null)
            return;

        flush();	// unwritten data

        for (int i = 0; i < bufferList.length; i++) {
            if (bufferList[i] != null) {
                Buffer b = bufferList[i];
                bufferList[i] = null;
                b.exit();
            }
        }

        prevReadIndex  = -1;
        prevWriteIndex = -1;
    }

    /*
     * Make sure that pending writes are flushed if the app
     * neglected to call flush().
     */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public String toString() {

	try {
	    if (isSymlink()) {
	        if (symlink != null)
	            return "\"" + name + "\": symlink -> \"" + symlink + "\"";
		else
		    return "\"" + name + "\": symlink";

	    }

	    if (isDirectory()) {
	        String s = "\":" + name + "\" directory";

	        if (dircache != null)
		    return s + "(" + dircache.length + " entries)";
	        else
		    return s;
	    }

	    // Must be a regular file

	    return "\"" + name + "\": file (" + length() + " bytes)";

	} catch (IOException e) {
	    return e.getMessage();
	}
    }
}
