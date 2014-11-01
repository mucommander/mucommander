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

/**
 *  This is the "NONE" credential, i.e. no credential
 *  It's the default credential for RPC unless set
 *  to something else.
 */

public class CredNone extends Cred {

    static final int AUTH_NONE = 0;

    /**
     * Put "no" creds into an XDR buffer
     */
    void putCred(Xdr x) {

        x.xdr_int(AUTH_NONE);
        x.xdr_int(0);		// no cred data
        x.xdr_int(0);		// no verifier
        x.xdr_int(0);		// no verifier
    }

    /**
     * Get "no" creds from an XDR buffer
     */
    void getCred(Xdr x) {

        x.xdr_int();	// assume it's AUTH_NONE
        x.xdr_int();	// cred length == 0
        x.xdr_int();	// no verifier
        x.xdr_int();	// no verifier
    }

    void init(Connection conn, int prog, int vers) {
        // No-op
    }


    boolean refresh(Connection conn, int prog, int vers) {
        // No-op
	return true;
    }

    void wrap(Xdr x, byte[] arg) {
        // No-op
    }

    int unwrap(Xdr x) {
        // No-op
	return 0;
    }

    void validate(byte[] verifier, int verifiee) {
        // No-op
    }

    void destroy(Rpc rpc) {
	// No-op
    }

}
