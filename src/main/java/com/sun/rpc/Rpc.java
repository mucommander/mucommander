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

package com.sun.rpc;

import java.io.*;
import java.net.InetAddress;

/**
 *
 * This class transmits and receives RPC calls to an RPC service
 * at a specific host and port.
 *
 * @see Connection
 * @see RpcException
 * @see MsgAcceptedException
 * @see MsgDeniedException
 * @author Brent Callaghan
 */
public class Rpc {
    public Connection conn;
    int prog;
    int vers;
    Cred cred;
    RpcHandler rhandler = new RpcHandler();

    private static int xid = (int) System.currentTimeMillis() & 0x0fffffff;

    private static final int PMAP_PROG = 100000;
    private static final int PMAP_PORT = 111;
    private static final int PMAP_VERS = 2;
    private static final int PMAP_GETPORT = 3;
    private static final int PMAP_MAXSZ = 128;

    private static final int MAX_TIMEOUT = 30 * 1000; // 30 sec
    private static final int MAX_REPLY = 8192 + 256;

    /**
     * Construct a new Rpc object - equivalent to a "client handle"
     * using an AUTH_NONE cred handle.
     *
     * @param conn	A connection to the server
     * @param prog	The program number of the service
     * @param vers	The version number of the service
     */
    public Rpc(Connection conn, int prog, int vers) {
        this.conn = conn;
        this.prog = prog;
        this.vers = vers;
        cred = new CredNone();
    }

    /**
     * Construct a new Rpc object - equivalent to a "client handle"
     * using a given cred handle "cr"
     *
     * @param conn	A connection to the server
     * @param prog	The program number of the service
     * @param vers	The version number of the service
     * @param cr	The cred to be used: CredUnix or CredGss
     */
    public Rpc(Connection conn, int prog, int vers, Cred cr) {
        this.conn = conn;
        this.prog = prog;
        this.vers = vers;
        cred = cr;
    }

    /**
     * Construct a new Rpc object - equivalent to a "client handle"
     *
     * @param server	The hostname of the server
     * @param port	The port number for the service
     * @param prog	The program number of the service
     * @param vers	The version number of the service
     * @param proto	The protocol to be used: "tcp" or "udp"
     * @param maxReply	The maximum size of the RPC reply
     * @exception	IOException if an I/O error occurs
     */
    public Rpc(String server, int port, int prog, int vers,
                String proto, int maxReply)
        throws IOException {
        this.conn = getConnection(server, port, prog, vers,
                        proto, maxReply);
        this.prog = prog;
        this.vers = vers;
        cred = new CredNone();
    }


    private Connection getConnection(String server, int port, int prog,
			int vers, String proto, int maxReply)
        throws IOException {

        if (port == 0) {
            Rpc pmap = new Rpc(server, PMAP_PORT, PMAP_PROG, PMAP_VERS,
                            "udp", PMAP_MAXSZ);
            Xdr call = new Xdr(PMAP_MAXSZ);

            pmap.rpc_header(call, PMAP_GETPORT);
    
            call.xdr_int(prog);
            call.xdr_int(vers);
            call.xdr_int(proto.equals("tcp") ? 6 : 17);
            call.xdr_int(0); // no port
    
            Xdr reply = pmap.rpc_call(call, 5 * 1000, 3);
    
            port = reply.xdr_int();
            if (port == 0)
                throw new MsgAcceptedException(PROG_UNAVAIL);
        }

        /*
         * Check the connection cache first to see
         * if there's a connection already set up
         * for this port, server and protocol.  This is
         * particularly important for TCP with its
         * high connection overhead.
         *
         * The code is synchronized to prevent concurrent
         * threads from missing the cache and setting
         * up multiple connections.
         */
        synchronized (Connection.connections) {
            conn = Connection.getCache(server, port, proto);
    
            if (conn == null) {
                if (proto.equals("tcp"))
                    conn = new ConnectSocket(server, port, maxReply);
                else
                    conn = new ConnectDatagram(server, port, maxReply);
    
                Connection.putCache(conn);
            }
        }

        return conn;
    }

    /**
     * Set the RPC credential
     *
     * @param c - cred to be used
     */
    public void setCred(Cred c) throws RpcException {

        cred = c;
	cred.init(conn, prog, vers);
    }

    /**
     * Delete the RPC credential data and destroy its security
     * context with the server.
     */
    public void delCred() throws RpcException {

	cred.destroy(this);
    }

    /**
     * Return the RPC credential
     *
     * @return		The credential
     */
    public Cred getCred() {
        return cred;
    }

    /**
     *
     */
    public void setRpcHandler(RpcHandler r) {
        rhandler = r == null ? new RpcHandler() : r;
    }

    /**
     * Construct an RPC header in the XDR buffer
     *
     * @param call	The XDR buffer for the header
     * @param proc	The service procedure to be called
     */
    public void rpc_header(Xdr call, int proc) throws RpcException {

        call.xid = next_xid();

        /*
         * Initialize XDR buffer
         * If using TCP then reserve space for record mark
         */
        call.xdr_offset(conn instanceof ConnectSocket ? 4 : 0);

        call.xdr_int(call.xid);
        call.xdr_int(0);	// direction=CALL
        call.xdr_int(2);	// RPC version
        call.xdr_int(prog);
        call.xdr_int(vers);
        call.xdr_int(proc);
        cred.putCred(call);
    }

    /*
     * A rare static method!  We need to
     * make sure that the xid is unique
     * for all instances of an RPC connection
     * on this client.
     */
    static synchronized int next_xid() {
        return xid++;
    }

    /*
     * Message type
     */
    static final int  CALL  = 0;
    static final int  REPLY = 1;

    /*
     * Reply Status
     */
    static final int  MSG_ACCEPTED = 0;
    static final int  MSG_DENIED   = 1;

    /*
     * Accept Status
     */
    static final int  SUCCESS        = 0;
    static final int  PROG_UNAVAIL   = 1;
    static final int  PROG_MISMATCH  = 2;
    static final int  PROC_UNAVAIL   = 3;
    static final int  GARBAGE_ARGS   = 4;
    static final int  SYSTEM_ERR     = 5;

    /*
     * Reject Status
     */
    static final int  RPC_MISMATCH   = 0;
    static final int  AUTH_ERROR     = 1;

    /*
     * Re-construct the call xdr buffer when retranmitting an
     * RPCSEC_GSS request.
     * (i.e. for a retransmitted RPCSEC_GSS requests, it uses a
     * different sequence number and it needs to use its un-encrypted
     * argument to do wrap() again.
     */
    private Xdr call_reconstruct(Xdr call, byte[] arg)
		throws IOException, RpcException {

	Xdr recall = new Xdr(call.xdr_size());
	recall.xid = call.xid;

	// the rpc_header
	recall.xdr_raw(call.xdr_raw(0,
			conn instanceof ConnectSocket ? 28 : 24));
	cred.putCred(recall);

	// the not-yet-encrypted rpc argument
	if (arg != null) {
	    if (recall.xdr_offset() == recall.xdr_wrap_offset()) {
		recall.xdr_raw(arg);
	    } else {
		recall.xdr_raw(arg, 4, arg.length - 4);
	    }
	}

        return recall;
    }

    /**
     * Transmit the XDR call buffer containing an RPC header
     * followed by a protocol header and receive the
     * reply.
     *
     * @param call	XDR buffer containing RPC call to transmit
     * @param arg	(seq_num + RPC argument) if wrap
     * @param timeout	after this number of milliseconds
     * @return Xdr	the XDR buffer for the reply
     * @throws		RpcException
     */
    public Xdr rpc_call_one(Xdr call, byte[] arg, int timeout)
	throws IOException, RpcException {

        int status, astat, rstat;
        int why;
        byte[] verifier;

	// encrypt the rpc argument if it's needed
	if (arg != null)
	    cred.wrap(call, arg);

        Xdr reply = conn.send(call, timeout);

        // XID already xdr'ed by the connection listener

        if (reply.xdr_int() != REPLY)		// direction
            throw new RpcException("Unknown RPC header");

        status = reply.xdr_int();
        switch (status) {
        case MSG_ACCEPTED:
            reply.xdr_skip(4);		// verifier flavor
	    verifier = reply.xdr_bytes(); // get the verifier
            astat = reply.xdr_int();

            switch (astat) {
            case SUCCESS:
		int seq_num_in = cred.unwrap(reply);

		// decrypt the result if it's needed
		if (seq_num_in > 0) {
		    cred.validate(verifier, seq_num_in);
		}
                break;

            case PROG_UNAVAIL:
            case PROG_MISMATCH:
            case PROC_UNAVAIL:
                throw new MsgAcceptedException(astat,
                            reply.xdr_int(), reply.xdr_int());

            case GARBAGE_ARGS:
            case SYSTEM_ERR:
            default:
                throw new MsgAcceptedException(astat);
            }
            break;

        case MSG_DENIED:
            rstat = reply.xdr_int();
            switch (rstat) {
            case RPC_MISMATCH:
                throw new MsgRejectedException(rstat,
                            reply.xdr_int(), reply.xdr_int());
            case AUTH_ERROR:
                why = reply.xdr_int();
		throw new MsgRejectedException(rstat, why);

            default:
                throw new MsgRejectedException(rstat);
            }
        }

        return reply;
    }

    /**
     * Make an RPC call but retry if necessary
     *
     * Retries use exponential backoff up to MAX_TIMEOUT ms.
     *
     * Note that we handle TCP connections differently: there is
     * no timeout, and retransmission is used only when reconnecting.
     *
     * @param call	XDR buffer containing RPC call to transmit
     * @param timeout	for the initial call
     * @param retries	the number of times to retry the call.
     *			A value of zero implies forever.
     * @return Xdr	the XDR buffer for the reply
     * @throws		IOException
     */
    public Xdr rpc_call(Xdr call, int timeout, int retries)
        throws IOException {

        boolean timedout = false;
        Xdr reply = null;
        long startTime = System.currentTimeMillis();

        if (retries == 0)
            retries = Integer.MAX_VALUE;	// retry forever

        /*
         * If it's a TCP connection, do retries only
         * to re-establish connection.
         */
        if (conn instanceof ConnectSocket)
            timeout = MAX_TIMEOUT;

	// refresh twice if needed
	int num_refresh = 2;
        for (int c = 0; c < retries; c++) {

	    byte[] arg = null;

	    /*
	     * Currently, only CredNone, CredUnix, CredGss is supported.
	     * For CredGss: save the (seq_num + rpc argument) before
	     * it's encrypted. This arg will be needed during retransmit.
	     *
	     * CredGss not checked to avoid loading un-used CredGss class. 
	     */
	    if (!(cred instanceof CredUnix) && !(cred instanceof CredNone) &&
		(call.xdr_offset() > call.xdr_wrap_offset())) {
		arg = call.xdr_raw(call.xdr_wrap_offset(),
                        call.xdr_offset() - call.xdr_wrap_offset());
	    }

            try {

                reply = rpc_call_one(call, arg, timeout);
                break;	// reply received OK

	    } catch (MsgRejectedException e) {

		/*
		 * Refresh the cred and try again
		 */
		if (num_refresh > 0 &&
			(e.why == MsgRejectedException.RPCSEC_GSS_NOCRED ||
			e.why == MsgRejectedException.RPCSEC_GSS_FAILED) &&
			cred.refresh(conn, prog, vers)) {

		    // re-construct the "call" Xdr buffer.
		    call = call_reconstruct(call, arg);
		    num_refresh--;
		    c--; // to do refresh

		} else {
		    throw e;
		}

            } catch (RpcException e) {

                /*
                 * An error that cannot be recovered by
                 * retrying - just give up.
                 */
                throw e;

            } catch (IOException e) {

                /*
                 * If it's a timeout then tell the RPC handler.
                 * It may request an abort by returning true.
                 */
                if (rhandler.timeout(conn.server, c,
                    (int) (System.currentTimeMillis() - startTime)))
                    throw new InterruptedIOException();

                /*
                 * Probably a timeout.
                 * Double the timeout and retry
                 */
                timedout = true;
                timeout *= 2;			// double the timeout
                if (timeout > MAX_TIMEOUT)
                    timeout = MAX_TIMEOUT;

		/*
		 * For CredGss: reconstruct the clear-text-argument
		 *		and use a new sequence number.
		 * Currently, only CredNone, CredUnix, CredGss is supported.
		 *
		 * CredGss not checked to avoid loading un-used CredGss class. 
		 */
		if (!(cred instanceof CredUnix) &&
			!(cred instanceof CredNone)) {
		    call = call_reconstruct(call, arg);
                }
            }
        }

        if (reply == null)			// reached retry limit
            throw new InterruptedIOException();

        /*
         * If recovered after a timeout then tell
         * the RPC Handler so it can display a
         * "server OK" message.
         */
        if (timedout && reply != null)
            rhandler.ok(conn.server);

        return reply;
    }

    /**
     * Since this returns the address of the server it may
     * seem redundant - but if you receive a reply to a
     * broadcast RPC you need to know who is replying.
     * @return address of the Peer
     */
    public InetAddress getPeer() {
        return conn.getPeer();
    }
}
