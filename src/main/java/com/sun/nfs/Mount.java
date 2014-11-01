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

import java.io.*;
import com.sun.rpc.*;

/**
 * Handle the mount protocol for NFS versions 2 and 3
 *
 * Note that we transmit an unmount request immediately
 * after a successful mount request.  This avoids having
 * "mount" entries pile up in the server's /etc/rmtab log.
 *
 * @see Nfs
 * @author Brent Callaghan
 */
class Mount {
    private final static int MOUNTPROG = 100005;

    private final static int MOUNTPROC_MNT    =  1;
    private final static int MOUNTPROC_UMNT   = 3;
    private final static int MOUNTPROC_EXPORT = 5;

    private final static int FHSIZE  = 32;
    private final static int FHSIZE3 = 64;

    private final static int ENOENT =  2;
    private final static int EACCES = 13;

    String sec_flavor;

    /**
     * Get an NFS v2 or v3 file handle
     *
     * @param server	The NFS server
     * @param path	The file path on the server
     * @param vers	The NFS version
     * @returns 	The filehandle as a byte array
     */
    byte[] getFH(String server, String path, int vers)
        throws java.net.UnknownHostException, IOException {
        Rpc mnt;
        Xdr callmsg = new Xdr(1024);
        int status;
        byte[] fh;

        // Use Mount v1 for NFS v2, Mount v3 for NFS v3

        mnt = new Rpc(server, 0, MOUNTPROG, vers == 2 ? 1 : 3, "udp", 512);
        mnt.setCred(new CredUnix(0, 0));
        mnt.rpc_header(callmsg, MOUNTPROC_MNT);
        callmsg.xdr_string(path);

        Xdr replymsg = mnt.rpc_call(callmsg, 3 * 1000, 3);

        status = replymsg.xdr_int();
        if (status != 0) {
            /*
             * If ENOENT is returned and the path didn't
             * start with a slash then assume it's because
             * the URL didn't include it (a common mistake).
             * Add the slash and try again.
             */
            if ((status == ENOENT || status == EACCES) && ! path.startsWith("/"))
		return getFH(server, "/" + path, vers);

            throw new IOException("Mount status: " + status);
        }

        // Filehandle is different depending on version

        fh = vers == 2 ? replymsg.xdr_raw(FHSIZE) : replymsg.xdr_bytes();

	// Get security flavors if this is MOUNT V3.
        sec_flavor = null;
        if (vers == 3) {
            int numsec = replymsg.xdr_int();
            String prefer = NfsSecurity.getPrefer();
            while (numsec-- > 0) {
                String secmode = Integer.toString(replymsg.xdr_int());
 
                if ((prefer != null) && prefer.equals(secmode)) {
                    sec_flavor = prefer;
                }
 
                if ((sec_flavor == null) &&
                                NfsSecurity.hasValue(secmode)) {
                    sec_flavor = secmode;
                }
            } // while
        }
        if (sec_flavor == null) {
            sec_flavor = NfsSecurity.getDefault();
        }

        /*
         * Now send an unmount request
         */
        mnt.rpc_header(callmsg, MOUNTPROC_UMNT);
        callmsg.xdr_string(path);
        try {
            mnt.rpc_call(callmsg, 1000, 1);
        } catch (InterruptedIOException e) {
            // ignore
        }

	return (fh);
    }

    /*
     *  get the sec_flavor after a successful getMountInfo()
     */
    String getSec() {
        return (sec_flavor);
    }

    /*
     * Get the server's export list
     *
     */
    static String[] getExports(String server) 
        throws java.net.UnknownHostException, IOException {
        Rpc mnt;
        Xdr callmsg = new Xdr(255);
        Xdr replymsg;
        String[] elist = new String[32];
        int i = 0;

        try {
            mnt = new Rpc(server, 0, MOUNTPROG, 1, "tcp", 8192);
            mnt.setCred(new CredUnix(0, 0));
            mnt.rpc_header(callmsg, MOUNTPROC_EXPORT);
    
            // This RPC proc takes no arguments
    
            replymsg = mnt.rpc_call(callmsg, 3 * 1000, 3);

        } catch (java.net.UnknownHostException e) {
            throw e;

        } catch (IOException e) {
            return new String[0];	// an empty export list
        }

        /*
         * The exports come back as a linked list.
         * Walk along the list extracting the export names
         * into an array and ignore the associated groups list.
         */
        while (replymsg.xdr_bool()) {
            elist[i++] = replymsg.xdr_string();
            if (i >= elist.length) {		// last elem in array ?
                String[] tmp = elist;

                elist = new String[i*2];	// double its size
                System.arraycopy(tmp, 0, elist, 0, i);
            }

            /*
             * Skip the groups list
             */
            while (replymsg.xdr_bool()) {
                replymsg.xdr_string();
            }
        }

        /*
         * Trim export list to exact size
         */
        if (i < elist.length) {
            String[] tmp = elist;

            elist = new String[i];
            System.arraycopy(tmp, 0, elist, 0, i);
        }

        return elist;
    }
}
