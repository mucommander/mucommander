/*
 * Copyright (c) 1998, 2007 Sun Microsystems, Inc. 
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
import com.sun.xfile.*;

public class XFileExtensionAccessor
    extends com.sun.xfile.XFileExtensionAccessor {
	
    XFile xf;

    public XFileExtensionAccessor(XFile xf) {

        super(xf);
        if (! xf.getFileSystemName().equals("nfs"))
            throw new IllegalArgumentException("Invalid argument");

        this.xf = xf;
    }
   
    /**
     * Sets the user's RPC credential from Login name and password.
     *
     * Every NFS request includes a "credential" that identifies the user.
     * An AUTH_SYS credential includes the user's UID and GID values.
     * These are determined from the user's login name (and password)
     * by the PCNFSD service that must be available on a local server.
     * Once the credential is set, it is assigned globally to all
     * future NFS XFile objects.
     * <p>
     * If this method is not called, a default credential is assigned
     * with a UID and GID of "nobody".
     *
     * @param  <code>host</code> The name of the host that runs the PCNFSD service.
     *   This does not have to be an NFS server.
     * @param <code>username</code> The user's login name.
     * @param <code>password</code> The user's password.
     *   This is obscured before transmission to the PCNFSD server.
     * @return true if the login succeeded, false otherwise.
     */
    public boolean loginPCNFSD(String host, String username, String password) {
        return NfsConnect.getCred().fetchCred(host, username, password);
    }

    /**
     * Sets the user's RPC credential to "nobody"
     */

    public void logoutPCNFSD()  {
        NfsConnect.getCred().setCred();
    }


    /**
     * Sets the user's RPC credential to a known uid/gid.
     *
     * Assumes that the calling application has already
     * authenticated the user and has obtained to uid/gid
     * itself.
     * <p>
     * <i>Note: This credential setting method exposes an
     * inherent security hole in RPC AUTH_SYS authentication.
     * The server trusts the client to authenticate the
     * user before setting the UID and GID values.  It is
     * possible for a malicious client to allow the UID and/or
     * group ids to be set to allow unauthorized access to 
     * other user's files on the server.
     * <p>
     * Servers can avoid this security hole by exporting NFS
     * filesystem securely - requiring clients to use secure
     * Diffie-Hellman or Kerberos credentials.
     * </i>
     * <p>
     * If this method is not called, a default credential is assigned
     * with a UID and GID of "nobody".
     *
     * @param <code>uid</code> The user-ID.
     * @param <code>gid</code> The group-ID.
     * @param <code>gids</code> The group-ID list.
     */
    public void loginUGID(int uid, int gid, int[] gids) {
        NfsConnect.getCred().setCred(uid, gid, gids);
    }

    /**
     * Sets the user's RPC credential to "nobody"
     */

    public void logoutUGID()  {
        NfsConnect.getCred().setCred();
    }

    /**
     * Assigns an NfsHandler class that allows
     * the application to receive RPC timeout
     * notifications. The <code>handler</code> is used for all
     * NFS files accessed by the application.
     * The default handler can be restored by
     * passing a null <code>handler</code> argument.
     *
     * @param <code>handler</code> An instance of the NfsHandler class.
     */
    public void setNfsHandler(NfsHandler handler) {
        NfsConnect.setRpcHandler(handler);
    }

    /**
     * Get server's export list
     */
    public String[] getExports()
        throws java.net.UnknownHostException, IOException {

        return new Mount().getExports(new NfsURL(xf.toString()).getHost());
    }
}
