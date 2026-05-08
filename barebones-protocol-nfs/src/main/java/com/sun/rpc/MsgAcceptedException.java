/*
 * Copyright (c) 1997, 2007 Sun Microsystems, Inc. 
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
 * Handle the RPC "message accepted" class of errors.
 *
 * Note that some of the errors also convey low and high
 * version information.
 *
 * @see RpcException
 * @author Brent Callaghan
 */
public class MsgAcceptedException extends RpcException {
    int lo, hi;

    public static final int  PROG_UNAVAIL   = 1;
    public static final int  PROG_MISMATCH  = 2;
    public static final int  PROC_UNAVAIL   = 3;
    public static final int  GARBAGE_ARGS   = 4;
    public static final int  SYSTEM_ERR     = 5;

    /*
     * Construct a new Exception for the specified RPC accepted error
     * @param error	The RPC error number
     */
    public MsgAcceptedException(int error) {
        super(error);
    }

    /*
     * Construct a new RPC error with the given low and high parameters
     * @param error	The RPC error number
     * @param lo	The low version number
     * @param hi	The high version number
     */
    public MsgAcceptedException(int error, int lo, int hi) {
        super(error);
        this.lo = lo;
        this.hi = hi;
    }

    public String toString() {
        switch (error) {

        case PROG_UNAVAIL:
            return "Program unavailable";

        case PROG_MISMATCH:
            return "Program number mismatch: " +
                    "low=" + lo + ",high=" + hi;

        case PROC_UNAVAIL:
            return "Procedure Unavailable: " +
                    "low=" + lo + ",high=" + hi;

        case GARBAGE_ARGS:
            return "Garbage Arguments";

        case SYSTEM_ERR:
            return "System error";

        default:
            return "Unknown RPC Error = " + error;
        }
    }
}
