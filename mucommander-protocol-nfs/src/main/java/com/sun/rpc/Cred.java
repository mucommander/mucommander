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

/**
 * RPC Credentials
 *
 * Extended by each credential class
 */

public abstract class Cred {

    // service types: authentication, integrity and privacy
    public static final int SVC_NONE = 1;
    public static final int SVC_INTEGRITY = 2;
    public static final int SVC_PRIVACY = 3;

    /**
     * Put creds into an XDR buffer
     */
    abstract void putCred(Xdr x) throws RpcException;

    /**
     * Get creds from an XDR buffer
     */
    abstract void getCred(Xdr x);

    /**
     * Initiate a security context with peers
     */
    abstract void init(Connection conn, int prog, int vers)
		throws RpcException;

    /**
     * Refresh the cred
     */
    abstract boolean refresh(Connection conn, int prog, int vers);

    /**
     * Encrypt an XDR buffer
     */
    abstract void wrap(Xdr x, byte[] arg) throws RpcException;

    /**
     * Descrypt an XDR buffer
     */
    abstract int unwrap(Xdr x) throws RpcException;

    /**
     * Validate the response verifier from server
     */
    abstract void validate(byte[] verifier, int verifiee)
		throws RpcException;

    /**
     * Destroy the cred data and its security context with the server
     */
    abstract void destroy(Rpc rpc) throws RpcException;

}
