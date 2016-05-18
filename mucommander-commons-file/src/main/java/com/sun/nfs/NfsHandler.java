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

import com.sun.rpc.*;

/**
 *  This handler is implemented by the NFS application
 *  if it wishes to be notifed of retransmissions.
 *  A good example is an NFS client that displays
 *  "NFS Server not responding" and "NFS server OK"
 */
public abstract class NfsHandler extends RpcHandler {

    /**
     * Called when an NFS request has timed out
     *
     * The RPC code will retransmit NFS requests
     * until the server responds.  The initial
     * retransmission timeout is set by the NFS
     * code and increases exponentially with
     * each retransmission until an upper bound
     * of 30 seconds is reached, e.g. timeouts
     * will be 1, 2, 4, 8, 16, 30, 30, ... sec.
     * <p>
     * An instance of the NfsHandler class is
     * registered with the setHandler method of
     * the NFS XFileExtensionAccessor.
     *
     * @param server The name of the server to which the
     *        request was sent.
     * @param retries The number of times the request has
     *        been retransmitted.  After the first timeout
     *        retries will be zero.
     * @param wait Total time since first call in milliseconds
     *        (cumulative value of all retransmission timeouts).
     * @return false if retransmissions are to continue.
     *        If the method returns true, the RPC layer will
     *        abort the retransmissions and return an
     *        InterruptedIOException to the application.
     */
    public abstract boolean timeout(String server, int retries, int wait);

    /**
     * Called when a server reply is recieved after a timeout.
     *
     * @param server The name of the server that returned
     *        the reply.
     */
    public abstract void ok(String server);
}
