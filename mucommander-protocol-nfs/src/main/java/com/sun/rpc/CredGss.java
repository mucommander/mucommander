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
import com.sun.gssapi.*;

/**
 *  The credential class for the RPCSEC_GSS security flavor.
 *
 *  @see Cred
 *  @author Lin Ling
 *
 */

public class CredGss extends Cred {

    /*
     * These are the data needed for setting up RPCSEC_GSS dialog.
     */
    public int serviceType;	// authenticate, integrity or privacy

    Oid mechOid;
    int qop;
    String serviceName; // e.g. "nfs" is a service name
    int seq_num_out; // sequence number in the out going request
    int seq_window;
    int control;	  // RPCSEC_GSS_INIT or RPCSEC_GSS_DATA ...etc
    GSSContext gssCtx;	  // context object for gss operations	
    byte[] ctx_handle;	  // context handle for the security context

    public static final int RPCSEC_GSS = 6;
    public static final int RPCSEC_GSS_DATA = 0;
    public static final int RPCSEC_GSS_INIT = 1;
    public static final int RPCSEC_GSS_CONTINUE_INIT = 2;
    public static final int RPCSEC_GSS_DESTROY = 3;

    public static final int RPCSEC_GSS_VERS_1 = 1;

    private static final int RPCGSS_MAXSZ = 1024;
    private static final int PROC_NULL = 0;


    /**
     * Constructor creates an instance of RPCSEC_GSS credential with
     * given service name, mechanism, service type and qop number.
     *
     * @param svcName	the target service name
     * @param mech	the string format of mech oid; e.g. "1.2.3.4.5"
     * @param svcType	none, integrity or privacy
     * @param qop_num	the number of quality protection
     */
    public CredGss(String svcName, String mech, int svcType, int qop_num)
	throws IOException {

	try {
	    mechOid = new Oid(mech);
	} catch (GSSException e) {
	    throw new IOException ("can not construct CredGss object");
	}
	serviceName = svcName;
	serviceType = svcType;
	qop = qop_num;
	seq_num_out = 0;
	ctx_handle = null;
	gssCtx = null;
	control = RPCSEC_GSS_INIT;
    }

    /**
     * Constructor creates an instance of RPCSEC_GSS credential with
     * given service name, mechanism, service type and qop number.
     *
     * @param svcName	the target service name
     * @param mech	the GSS Oid object of the mech
     * @param svcType	none, integrity or privacy
     * @param qop_num	the number of quality protection
     */
    public CredGss(String svcName, Oid mech, int svcType, int qop_num) {

	mechOid = mech;
	serviceName = svcName;
	serviceType = svcType;
	qop = qop_num;
	seq_num_out = 0;
	ctx_handle = null;
	gssCtx = null;
	control = RPCSEC_GSS_INIT;
    }

    /**
     * Put RPCSEC_GSS cred/verf into an XDR buffer
     *
     * @param xdr buffer
     */
    synchronized void putCred(Xdr x) throws RpcException {

	MessageProp mInfo = new MessageProp(qop, false);

	/*
	 * Marshalling the cred field
	 */
        x.xdr_int(RPCSEC_GSS);

	/*
	 * For every data request (including retransmit)
	 * use a different sequence number.
	 */
	if (control == RPCSEC_GSS_DATA || control == RPCSEC_GSS_DESTROY)
	    seq_num_out++;

	/*
	 * If a context is established, encode the context handle.
	 * otherwise, encode a 0 length field.
	 */
	if (ctx_handle != null) {
	    // length = 20 + ctx_handle.length 
	    x.xdr_int(20 + ctx_handle.length);
	    x.xdr_int(RPCSEC_GSS_VERS_1);
	    x.xdr_int(control);
	    x.xdr_int(seq_num_out);
	    x.xdr_int(serviceType);
	    x.xdr_bytes(ctx_handle);
	} else {
	    // length = 20
	    x.xdr_int(20);
	    x.xdr_int(RPCSEC_GSS_VERS_1);
	    x.xdr_int(control);
	    x.xdr_int(seq_num_out);
	    x.xdr_int(serviceType);
	    x.xdr_int(0);
	}

	/*
	 * Marshalling the verifier field
	 */
        if (gssCtx != null) {
	    // Checksum the header data upto cred field.
	    try {
		byte[] headerMIC = gssCtx.getMIC(x.xdr_buf(), 0,
					x.xdr_offset(), mInfo);
		x.xdr_int(RPCSEC_GSS);
		x.xdr_bytes(headerMIC); 
	    } catch (GSSException e) {
		throw new RpcException("can not checksum the header");
	    }
	} else {
	    // if context is not established yet, use null verifier
	    x.xdr_int(CredNone.AUTH_NONE);
	    x.xdr_int(0);
	}

	x.xdr_wrap_offset(x.xdr_offset());
	if (control == RPCSEC_GSS_DATA && serviceType != SVC_NONE) {
	    x.xdr_int(seq_num_out);
	}
    }

    public void getCred(Xdr x) {
	// No-Op
    }

    /*
     * Send rpcsec_gss init control requests.  Retry if time out.
     *
     * This routine is similar to rpc.rpc_call() but is customized for the
     * rpcsec_gss init sec context handshakes.
     *
     * For init control requests, when it needs to do a refresh, it does
     * not need to retry the original init call after the cred is refreshed.
     * (upon a successful refresh, a context is established)
     * Use this routine instead of rpc_call() to avoid confusion.
     */
    private Xdr rpc_send(Rpc rpc, Xdr call, int timeout, int retries)
			throws RpcException, IOException {

	Xdr reply = null;

	if (retries == 0) {
	    retries = Integer.MAX_VALUE; //retry forever
	}

	for (int c = 0; c < retries; c++) {

	    /*
	     * This is for init control requests, not a data request.
	     * No argument for wrapping.
	     */ 
	    try {
		reply = rpc.rpc_call_one(call, null, timeout);
		break;	// reply received OK

	    } catch (RpcException e) {
		throw e;

	    } catch (IOException e) {
		// probably a timeout. retry
		continue;
	    }
	}

	if (reply == null)	// reached retry limit
	    throw new InterruptedIOException();

	return reply;
    }

    /**
     * Init a security context using the given connection instance.
     *
     * @param conn	The connection to the server
     * @param prog	The program number of the rpc service
     * @param vers	The version number of the rpc service
     */
    synchronized void init(Connection conn, int prog, int vers)
	throws RpcException {

	byte[] inTok = new byte[0];
	Rpc secRpc;
	Xdr secCall, secReply;
	CredGss initCred;
        int major = 0, minor = 0;
	
	try {
	    GSSContext ctx = new GSSContext(new GSSName(serviceName,
				GSSName.NT_HOSTBASED_SERVICE),
				mechOid, null, 0);

	    // set context options
	    ctx.requestConf(true);
	    ctx.requestInteg(true);
	    ctx.requestMutualAuth(true);
	    ctx.requestReplayDet(true);
	    ctx.requestSequenceDet(true);

	    initCred = new CredGss(serviceName, mechOid, serviceType, qop);
            secRpc = new Rpc(conn, prog, vers, initCred);
            secCall = new Xdr(RPCGSS_MAXSZ);

	    /*
	     * gss token exchange semantics:
	     *
	     * (1 token)
	     * Client                               Server
	     * init stat = complete
	     * token length > 0     ---> token -->  accept stat = complete
	     *                                      token.len = 0
	     *
	     * (2 tokens)
	     * Client                               Server
	     * init stat = cont
	     * token.length > 0     ---> token ---> accept stat = complete
	     *                                      token.len > 0
	     *                      <--- token <---
	     * init stat = complete
	     * token.length = 0
	     *
	     * (3 tokens)
	     * Client                               Server
	     * init stat = cont
	     * token.length > 0     ---> token ---> accept stat = cont
	     *                                      token.length > 0
	     *                      <--- token <---
	     * init stat = complete
	     * token.length > 0     ---> token ---> accept stat = complete
	     *                                      token.length = 0
	     */
	    initCred.control = RPCSEC_GSS_INIT;
	    int num_refresh = 2;
            do {
                byte[] outTok = ctx.init(inTok, 0, inTok.length);

                if (outTok != null) {
		    secRpc.rpc_header(secCall, PROC_NULL);
		    secCall.xdr_bytes(outTok);

		    try {
			secReply = rpc_send(secRpc, secCall, 30 * 1000, 5);

		    } catch (MsgRejectedException e) {

			if (num_refresh > 0 &&
			    (e.why == MsgRejectedException.RPCSEC_GSS_NOCRED ||
			    e.why == MsgRejectedException.RPCSEC_GSS_FAILED)) {

			    // reset the parameters and retry (refresh)
			    inTok = new byte[0];
			    ctx = new GSSContext(new GSSName(serviceName,
				GSSName.NT_HOSTBASED_SERVICE),
				mechOid, null, 0);
			    ctx.requestConf(true);
			    ctx.requestInteg(true);
			    ctx.requestMutualAuth(true);
			    ctx.requestReplayDet(true);
			    ctx.requestSequenceDet(true);
			    initCred.gssCtx = null;
			    initCred.ctx_handle = null;
			    initCred.control = RPCSEC_GSS_INIT;
			    num_refresh--;
			    continue;
 
			} else {
			    throw e;
			}
		    } catch (RpcException e) {
			throw e;
		    }

		    // decode the result and get the context id
		    this.ctx_handle = secReply.xdr_bytes();
		    major = secReply.xdr_int();
		    minor = secReply.xdr_int();
		    if (major != GSSContext.COMPLETE &&
                        major != GSSContext.CONTINUE_NEEDED) {

			throw new RpcException("cred.init server failed");
		    }

		    this.seq_window = secReply.xdr_int();
		    inTok = secReply.xdr_bytes(); // token from the server

		    if (!ctx.isEstablished() && inTok == null) {
			throw new RpcException("cred.init:bad token");
		    }

                } else if (major == GSSContext.CONTINUE_NEEDED) {
		    // no more token, but server is waiting for one
		    throw new RpcException("cred.init:server needs token");
		}

	        initCred.control = RPCSEC_GSS_CONTINUE_INIT;
	        initCred.ctx_handle = ctx_handle;

            } while (!ctx.isEstablished());

            this.gssCtx = ctx;
	    this.control = RPCSEC_GSS_DATA;

	} catch (IOException e) {
	    throw new RpcException("cred.init: io errors ");
	} catch (GSSException e) {
	    throw new RpcException("cred.init: gss errors");
	}
    }

    /**
     * Refresh the RPCSEC_GSS credential.
     * Nulled context and ctx_handle and re-try init sec context.
     *
     * @param conn	The connection to the server
     * @param prog	The program number of the rpc service
     * @param vers	The version number of the rpc service
     * @return		true if success
     */
    synchronized boolean refresh(Connection conn, int prog, int vers) {

	// If no context has established, don't try to recreate it.
	if (ctx_handle == null) {
	    return false;
	}

    	gssCtx = null;
	ctx_handle = null;
	try {
	    init(conn, prog, vers);
	    return true;
	} catch (RpcException e) {
	    return false;
	}
    }

    /**
     * Use this cred object to encrypt the given xdr buffer. 
     *
     * @param call	xdr buffer
     * @param arg	the rpc argument to be encrypted
     * @return		the xdr buffer with the encrypted data
     */
    synchronized void wrap(Xdr call, byte[] arg) throws RpcException {
	byte[] argTok;
	MessageProp mInfo = new MessageProp(qop, false);

	if (control != RPCSEC_GSS_DATA) {
	    return;
	}

	try {
	    switch (serviceType) {
	    case SVC_NONE:
		break;

	    case SVC_INTEGRITY:
		argTok = gssCtx.getMIC(arg, 0, arg.length, mInfo);
		call.xdr_offset(call.xdr_wrap_offset());
		call.xdr_bytes(arg);
		call.xdr_bytes(argTok);
		break;

	    case SVC_PRIVACY:
		mInfo.setPrivacy(true);
		argTok = gssCtx.wrap(arg, 0, arg.length, mInfo);
		call.xdr_offset(call.xdr_wrap_offset());
		call.xdr_bytes(argTok);
		break;
	    }

	} catch (GSSException e) {
	    throw new RpcException("wrap: Can not wrap RPC arg");
	}
    }

    /**
     * Use this cred object to decrypt the given xdr buffer. 
     *
     * @param reply	xdr buffer
     * @return		the xdr buffer with the unencrypted data
     */
    synchronized int unwrap(Xdr reply) throws RpcException {
	int result_off, result_len, verify_off, csum_len, seq_num_in = 0;
	byte[] result;
	MessageProp mInfo = new MessageProp();

	if (control != RPCSEC_GSS_DATA) {
	    return 0;
	}

	result_off = reply.xdr_offset();

        switch (serviceType) {
        case SVC_NONE:
	    return 0;

        case SVC_INTEGRITY:
	    result_len = reply.xdr_int();
	    if (result_len <= 4) // length of (seq num + rpc arg) is 0 or 4
		return 0;

	    verify_off = reply.xdr_offset(); // offset of (seq num + rpc arg)
	    seq_num_in = reply.xdr_int();
	    /*
	     * When server is slow, it is possible that client will
	     * receive packets in different order.  Seqence window should be
	     * the maximum number of client requests that maybe outstanding
	     * for this context.  The this.seq_window is set to the sequence
	     * window length supported by the server for this context.
	     */
	    if ((seq_num_in < (seq_num_out - seq_window)) ||
		(seq_num_in > seq_num_out)) {
		throw new RpcException("unwrap: bad sequence number");
	    }

	    result = reply.xdr_raw(result_len - 4); // 4-length of seq num
	    csum_len = reply.xdr_int();
	    
            try {
                gssCtx.verifyMIC(reply.xdr_buf(), reply.xdr_offset(),
				csum_len, reply.xdr_buf(), verify_off,
				result_len, mInfo);
            } catch (GSSException e) {
		throw new RpcException("unwrap: gss_verifyMIC failed");
            }

	    if (mInfo.getQOP() != qop) {
		throw new RpcException("unwrap: unexpected qop");
	    }

	    reply.xdr_offset(result_off);
	    reply.xdr_raw(result);
	    reply.xdr_offset(result_off);
            break;

        case SVC_PRIVACY:
	    result_len = reply.xdr_int();
	    if (result_len == 0)
		return 0;

            try {
                result = gssCtx.unwrap(reply.xdr_buf(), reply.xdr_offset(),
					result_len, mInfo);
            } catch (GSSException e) {
		throw new RpcException("unwrap: gss_unwrap failed");
            }

	    if (mInfo.getQOP() != qop) {
		throw new RpcException("unwrap: unexpected qop");
	    }

	    reply.xdr_offset(result_off);
            reply.xdr_raw(result);
	    reply.xdr_offset(result_off);
	    seq_num_in = reply.xdr_int();
	    if ((seq_num_in < (seq_num_out - seq_window)) ||
		(seq_num_in > seq_num_out)) {
		throw new RpcException("unwrap: bad sequence number");
	    }
            break;
        }

	return seq_num_in;
    }   


    /**
     * Validate RPC response verifier from server. The response verifier
     * is the checksum of the request sequence number.
     *
     * @param snumber	the sequence number 
     * @param token	the verifier
     */
    synchronized void validate(byte[] token, int snumber)
				throws RpcException {

	if (control != RPCSEC_GSS_DATA)
		return;

	MessageProp mInfo = new MessageProp();

	// create a buffer for the sequence number in the net order
	byte[] msg = new byte[4];
	msg[0] = (byte)(snumber >>> 24);
	msg[1] = (byte)(snumber >> 16);
	msg[2] = (byte)(snumber >> 8);
	msg[3] = (byte)snumber;

        try {
            gssCtx.verifyMIC(token, 0, token.length,
			msg, 0, msg.length, mInfo);
        } catch (GSSException e) {
            throw new RpcException("CredGss: validate failed");
        }
    }

    /**
     * Delete the RPC credential data and destroy its security
     * context with the server.
     *
     * @param rpc	delete the security context of this Rpc object
     */
    synchronized void destroy(Rpc rpc) 
			throws RpcException {

	if (gssCtx != null) {
	    try {
		Xdr secCall = new Xdr(RPCGSS_MAXSZ);
		control = CredGss.RPCSEC_GSS_DESTROY;
		rpc.rpc_header(secCall, PROC_NULL);
		rpc.rpc_call(secCall, 30 * 1000, 5);
		gssCtx.dispose();
		mechOid = null;
		gssCtx = null;
		ctx_handle = null;
	    } catch (IOException e) {
	    } catch (GSSException e) {
		/*
		 * If the request to destroy the context fails for some
		 * reason, the client need not take any special action.
		 * The server must be prepared to deal with situations
		 * where clients never inform the server to maintain
		 * a context.
		 */
		return;
	    }
	}
    }
}
