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
import java.util.Hashtable;
import java.net.InetAddress;

/**
 * Sets up a connection to the server using
 * either UDP or TCP as determined by the
 * subclass.
 *
 * This class also handles the connection caching.
 *
 * @see ConnectSocket
 * @see ConnectDatagram
 * @author Brent Callaghan
 */
public abstract class Connection extends Thread {

    static Hashtable connections = new Hashtable();
    public String server;
    public int port;
    String proto;
    Hashtable waiters = new Hashtable();
    static final int IDLETIME = 300 * 1000; // idle connection after 5 min
    int xid;		// transaction id
    Xdr reply;
    int maxSize;	// size of reply Xdr buffer
    Error err;		// might get thrown by the thread

    /**
     * Construct a new connection to a specified <i>server</i>
     * and <i>port</i> using protocol <i>proto</i> with a
     * reply buffer of size <i>maxsize</i>.
     *
     * @param server	The hostname of the server
     * @param port	The port number on the server
     */
    public Connection (String server, int port, String proto, int maxSize) {
        this.server = server;
        this.port = port;
        this.proto = proto;
        this.maxSize = maxSize;

        setName("Listener-" + server);
        setDaemon(true);
    }

    /**
     * Get a cached connection for the specified server, port and protocol
     *
     * @param server	The hostname of the server
     * @param port	The port number on the server
     * @param proto	The connection type: "tcp" or "udp"
     * @returns null	If there is no cached connection
     */
    public static Connection getCache(String server, int port, String proto) {
        Connection conn = (Connection) connections.get(
                server + ":" + port + ":" + proto);

        return conn;
    }

    /**
     * Stash a new connection in the cache
     *
     * @param	The connection to be cached
     */
    public static void putCache(Connection conn) {
        connections.put(conn.server + ":" + conn.port + ":" + conn.proto, conn);
    }

    abstract void sendOne(Xdr call) throws IOException;

    abstract void receiveOne(Xdr reply, int timeout) throws IOException;

    abstract InetAddress getPeer();

    abstract void dropConnection();

    abstract void checkConnection();

    /**
     * Return information about the connection
     *
     * @returns server, port number and protocol info.
     */
    public String toString() {
        return (server + ":" + port + ":" + proto);
    }

    private boolean running;

    synchronized void suspendListener() {
        running = false;

        while (!running) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    synchronized void resumeListener() {
        running = true;
        notifyAll();
    }

    synchronized Xdr send(Xdr call, int timeout)
        throws IOException {

        checkConnection();
        resumeListener();
        sendOne(call);

        waiters.put(new Integer(call.xid), new Integer(timeout));

        /*
         * Now sleep until the listener thread posts
         * my XID and notifies me - or I time out.
         */
        while (xid != call.xid) {
            long t = System.currentTimeMillis();

	    if (err != null)
		throw err;

            try {
                wait(timeout);
            } catch (InterruptedException e) {}
		
	    if (err != null)
		throw err;

            timeout -= (System.currentTimeMillis() - t);
            if (timeout <= 0) {
                waiters.remove(new Integer(call.xid));
                throw new InterruptedIOException(); // timed out
            }
        }

        /*
         * My reply has come in.
         */
        xid = 0;
        waiters.remove(new Integer(call.xid));
        notifyAll(); // wake the listener

        return reply;
    }

    /*
     * This is the code for the listener thread.
     * It blocks in a receive waiting for an RPC
     * reply to come in, then delivers it to the
     * appropriate thread.
     */
    public void run() {

	try {
            while (true) {
    
                synchronized (this) {
                    while (xid != 0) {
                        try {
                            wait();
                        } catch (InterruptedException e) {}
                    }
                }
    
                reply = new Xdr(maxSize);
        
                /*
                 * The listener thread now blocks reading
                 * from the socket until either a packet
                 * comes in - or it gets an idle timeout.
                 */
                try {
                    receiveOne(reply, IDLETIME);
                } catch (InterruptedIOException e) {
    
                    /*
                     * Got an idle timeout.  If there's
                     * no threads waiting then drop the
                     * connection and suspend.
                     */
                    if (waiters.isEmpty())
                        dropConnection();
                        suspendListener();
                } catch (IOException e) {
                        continue;
                }
    
                /*
                 * Have received an Xdr buffer.
                 * Extract the xid and check the hashtable
                 * to see if there's thread waiting for that reply.
                 * If there is, then notify the thread.  If not
                 * then ignore the reply (its thread may
                 * have timed out and gone away).
                 */
                synchronized (this) {
                    xid = reply.xdr_int();
                    if (waiters.containsKey(new Integer(xid)))
                        notifyAll();
                    else
                        xid = 0;   // ignore it
                }
            }
        } catch (Error e) {
            /*
             * Need to catch errors here, e.g. OutOfMemoryError
             * and notify threads before this listener thread dies
             * otherwise they'll wait forever.
             */
            this.err = e;
            synchronized (this) {
                notifyAll();
	    }
            throw e;
        }
    }
}
