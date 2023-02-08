/*
 * Copyright (c) 1999, 2007 Sun Microsystems, Inc. 
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

package com.sun.rpc;

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;

/**
 * Sets up a TCP connection to the server.
 *
 * This class lets us transmit and receive buffers
 * of data to a port on a remote server.
 * It also handles reconnection of broken TCP links.
 *
 * @see Connection
 * @author Brent Callaghan
 */
public class ConnectSocket extends Connection {

    static final int  LAST_FRAG = 0x80000000;
    static final int  SIZE_MASK = 0x7fffffff;
    static final int  MTUSZ = 1460 - 4; // good for Ethernet

    private OutputStream outs;
    private InputStream ins;
    private Socket sock;
    Xdr rcv_mark = new Xdr(4);

    /**
     * Construct a new connection to a specified server and port.
     *
     * @param server	The hostname of the server
     * @param port	The port number on the server
     * @param maxSize	The maximum size of the received reply
     * @exception IOException if the connection cannot be made
     */
    public ConnectSocket (String server, int port, int maxSize)
        throws IOException {

	super(server, port, "tcp", maxSize);
        doConnect();
        start();	// the listener
    }

    private void doConnect()
        throws IOException {

        if (server == null)
            throw new java.net.UnknownHostException("null host");

        sock = new Socket(server, port);
        sock.setTcpNoDelay(true);
    	ins = sock.getInputStream();
    	outs = sock.getOutputStream();
    }

    private void doClose() throws IOException {
        if (ins != null) {
            ins.close();
            ins = null;
        }

        if (outs != null) {
            outs.close();
            outs = null;
        }

        if (sock != null) {
            sock.close();
            sock = null;
        }
    }

    void sendOne(Xdr x) throws IOException {
        int recsize;
        int lastbit = 0;
	int bufsiz = x.xdr_offset();
        int save;

        /*
         * Use the connection only if unlocked.
         * Otherwise we risk attempting a sendOne()
         * while it's being reconnected. We also
         * need to protect threads from a concurrent
         * sendOne that may interleave record data.
         */
        synchronized (this) {
            /*
             * The XDR buffer needs to be transmitted on the
             * socket outputstream in MTUSZ records.  In RPC
             * over TCP each of these records begins with a
             * 32 bit record mark which comprises a byte
             * count for the record and a LAST_FRAG bit which
             * is set on the last record.
             *
             * You'll notice that the code goes through some
             * hoops to prepend this record mark on each of
             * the records transmitted from the XDR buffer
             * WITHOUT COPYING THE DATA.
             * Notice that there's a 4 octet space inserted at
             * the front of the buffer for the first record
             * mark by the code that builds the RPC header.
             * Space is taken from the buffer for subsequent
             * record marks and the data in this space is
             * saved and restored after the record is sent,
             * so hopefully we leave things as they were
             * when we're done.
             *
             * BTW: this code originally wrote the record
             * mark to the OutputStream separately, but the
             * JVM doesn't seem to be doing Nagle and the
             * record mark sailed off in its own tiny TCP
             * segment separate from the rest of the record,
             * which wasn't what I had in mind.
             */
            for (int off = 4; off < bufsiz; off += recsize) {
                /*
                 * Insert the record mark
                 */
                recsize = bufsiz - off;
                if (recsize > MTUSZ)
                    recsize = MTUSZ;
                if ((off + recsize) >= bufsiz)
                    lastbit = LAST_FRAG;
                x.xdr_offset(off-4);
                save = x.xdr_int();
                x.xdr_offset(off-4);
                x.xdr_int(lastbit | recsize);
    
                /*
                 * then send the record data
                 */
                outs.write(x.xdr_buf(), off-4, recsize+4);
                outs.flush();
                x.xdr_offset(off-4);
                x.xdr_int(save);
            }
            x.xdr_offset(bufsiz);	// restore XDR offset
        }
    }

    void receiveOne(Xdr x, int timeout) throws IOException {
        int off;
        int rcount;
        boolean lastfrag = false;
        long recsize;

        sock.setSoTimeout(timeout);

        try {
            for (off = 0; !lastfrag; off += recsize) {
                /*
                 * Read the record mark
                 */
                if (ins.read(rcv_mark.xdr_buf()) != 4)
                    throw new IOException("TCP record mark: lost connection");
                rcv_mark.xdr_offset(0);
                recsize = rcv_mark.xdr_u_int();
                lastfrag = (recsize & LAST_FRAG) != 0;
                recsize &= SIZE_MASK;
        
                /*
                 * then read the record data
                 */
                for (int i = 0; i < recsize; i += rcount) {
        	        rcount = ins.read(x.xdr_buf(), off + i, (int) recsize - i);
                        if (rcount < 0)
                            throw new IOException("TCP data: lost connection");
                }
            }
            x.xdr_size(off);
        } catch (java.io.InterruptedIOException e) {
            throw e;

        } catch (IOException e) {
            /*
             * Assume something bad happened to the connection.
             * Close the connection and attempt to reconnect.
             */
             reconnect();
             throw e;
        }
    }

    /*
     * Get the address of the caller.
     * This is needed when we get a reply from
     * and RPC to a broadcast address.
     */
    InetAddress getPeer() {
        return sock.getInetAddress();
    }

    /*
     * This method is called when it is suspected that
     * the connection has been broken.  It keeps retrying
     * connection attempts until it is successful.
     */
    void reconnect() {

        System.err.println("Lost connection to " + server +
            " - attempting to reconnect");

        /*
         * Lock the connection while we're messing
         * with it so that another thread can't
         * attempt a sendOne() on it.
         */
        synchronized (this) {
            while (true) {
                try {
                    doClose();	// make sure we're at a known state
                    doConnect();
                    break;		// success
    
                } catch (IOException e) {
    
                    /*
                     * Wait here for 5 sec so's we don't
                     * overwhelm the server with connection requests.
                     */
                    try {
                        java.lang.Thread.sleep(5000);
                    } catch (java.lang.InterruptedException i) {
                    }
                }
            }
        }

        System.err.println("Reconnected to " + server);
    }

    /*
     * The listener calls this after an idle timeout.
     * Be kind to the server and drop the connection.
     */
    void dropConnection() {
        try {
            doClose();
        } catch (IOException e) {};
    }

    /*
     * Check to make sure that the connection is up.
     * If not, then reconnect and resume the listener.
     */
    void checkConnection() {
        if (sock != null)
            return;
       
        reconnect();
    }

    protected void finalize() throws Throwable {
        doClose();
        super.finalize();
    }
}
