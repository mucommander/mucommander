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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Sets up a UDP connection to the server.
 * Since UDP is really connectionless, we
 * don't really have a connection, so perhaps
 * describing this as an <i>association</i>
 * is more accurate.
 *
 * This class lets us transmit and receive buffers
 * of data to a port on a remote server.
 *
 * @see Connection
 * @author Brent Callaghan
 */
public class ConnectDatagram extends Connection {

    DatagramSocket ds;
    DatagramPacket dp;
    InetAddress addr;

    /**
     * Construct a new connection to a specified server and port.
     * @param server	The hostname of the server
     * @param port	The port number on the server
     * @param maxSize	The maximum size in bytes of the received message
     * @exception IOException if the server does not exist
     */
    public ConnectDatagram (String server, int port, int maxSize)
        throws IOException {

        super(server, port, "udp", maxSize);

        ds = new DatagramSocket();
        addr = InetAddress.getByName(server);
        start();
    }

    void sendOne(Xdr x) throws IOException {

        /*
         * The interrupt call here is to break the listener
         * thread from its socket read.  For some unknown
         * reason a datagram socket read blocks threads
         * attempting to send.  The interrupt unblocks the
         * listener briefly so we can do the send.
         *
         * The blocking problem appears to be fixed as
         * of JDK 1.1.6, so the interrupt is skipped removed.
         */
	//interrupt();

        ds.send(new DatagramPacket(x.xdr_buf(), x.xdr_offset(), addr, port));
    }

    void receiveOne(Xdr x, int timeout) throws IOException {
        ds.setSoTimeout(timeout);
        dp = new DatagramPacket(x.xdr_buf(), x.xdr_buf().length);
        ds.receive(dp);
    }

    InetAddress getPeer() {
        return dp.getAddress();
    }

    /*
     * No connection to drop.
     */
    void dropConnection() {
    }

    /*
     * No connection to check
     */
    void checkConnection() {
    }

    protected void finalize() throws Throwable {
        if (ds != null) {
            ds.close();
            ds = null;
        }
        super.finalize();
    }
}
