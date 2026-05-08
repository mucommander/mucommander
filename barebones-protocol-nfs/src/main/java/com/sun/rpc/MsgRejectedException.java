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

/**
 *
 * Handle the RPC "message rejected" class of errors.
 *
 * Note that some of the errors also convey low and high
 * version information or an authentication sub-error.
 *
 * @see RpcException
 * @author Brent Callaghan
 */
public class MsgRejectedException extends RpcException {

    public static final int  RPC_MISMATCH = 0;
    public static final int  AUTH_ERROR   = 1;

    public static final int  AUTH_BADCRED      = 1;
    public static final int  AUTH_REJECTEDCRED = 2;
    public static final int  AUTH_BADVERF      = 3;
    public static final int  AUTH_REJECTEDVERF = 4;
    public static final int  AUTH_TOOWEAK      = 5;
    public static final int  AUTH_INVALIDRESP  = 6;
    public static final int  AUTH_FAILED       = 7;

    public static final int  RPCSEC_GSS_NOCRED = 13;
    public static final int  RPCSEC_GSS_FAILED = 14;

    /*
     * Construct a new Exception for the specified RPC accepted error
     * @param error	The RPC error number
     */
    public MsgRejectedException(int error) {
        super(error);
    }

    /*
     * Construct a new RPC error with the given auth sub-error
     * @param error	The RPC error number
     * @param why	The auth sub-error
     */
    public MsgRejectedException(int error, int why) {
        super(error);
        this.why = why;
    }

    /*
     * Construct a new RPC error with the given low and high parameters
     * @param error	The RPC error number
     * @param lo	The low version number
     * @param hi	The high version number
     */
    public MsgRejectedException(int error, int lo, int hi) {
        super(error);
        this.lo = lo;
        this.hi = hi;
    }

    public String toString() {
        switch (error) {

        case RPC_MISMATCH:
            return "Version mismatch: " +
                    "low=" + lo + ",high=" + hi;

        case AUTH_ERROR:
            String msg = "Authentication error: ";
	    switch (why) {
            case AUTH_BADCRED:
                msg += "bogus credentials (seal broken)";
                break;
            case AUTH_REJECTEDCRED:
                msg += "client should begin new session";
                break;
            case AUTH_BADVERF:
                msg += "bogus verifier (seal broken)";
                break;
            case AUTH_REJECTEDVERF:
                msg += "verifier expired or was replayed";
                break;
            case AUTH_TOOWEAK:
                msg += "too weak";
                break;
            case AUTH_INVALIDRESP:
                msg += "bogus response verifier";
                break;
            case RPCSEC_GSS_NOCRED:
                msg += "no credentials for user";
                break;
            case RPCSEC_GSS_FAILED:
                msg += "GSS failure, credentials deleted";
                break;
            case AUTH_FAILED:
            default:
            	msg += "unknown reason";
                break;
            }
            return msg;

        default:
            return "Unknown RPC Error = " + error;
        }
    }
}
