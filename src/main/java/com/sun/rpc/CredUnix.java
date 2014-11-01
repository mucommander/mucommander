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

/**
 *  The Unix credential.  Contains information specific
 *  to Unix users and NFS: uid/gid/grplist
 */

public class CredUnix extends Cred {

    /*
     * These are the data normally
     * found in a Unix credential.
     */
    private int uid;
    private int gid;
    private int[] gids;

    /*
     * These are additional info provided by
     * version 2 of PCNFSD.
     */
    private String home;
    private int def_umask;
    public  int status;

    /*
     * Default credential values
     */
    static final int AUTH_UNIX = 1;
    static final int UID_NOBODY = 60001;
    static final int GID_NOBODY = 60001;

    /*
     * Constants for PCNFSD protocol
     */
    private static final int PCNFSDPROG = 150001;
    private static final int PCNFSD_AUTH = 1;
    private static final int PCNFSD2_AUTH = 13;
    private static final int MAXREPLY = 512;

    static final int AUTH_RES_OK   = 0;
    static final int AUTH_RES_FAKE = 1;
    static final int AUTH_RES_FAIL = 2;

    private Xdr cr = new Xdr(64);

    /**
     * Constructor creates an instance of
     * Unix credential with given uid/gid
     */
    public CredUnix(int uid, int gid) {
        this.uid = uid;
        this.gid = gid;;
    }

    /**
     * Constructor creates an instance of
     * Unix credential and sets default uid/gid
     * to "nobody".
     */
    public CredUnix() {
	this(UID_NOBODY, GID_NOBODY);
    }

    /**
     * Put Unix creds into an XDR buffer
     *
     * @param xdr buffer
     */
    synchronized void putCred(Xdr x) {

        x.xdr_int(AUTH_UNIX);

	cr.xdr_offset(0);
        cr.xdr_int((int) (System.currentTimeMillis()/1000));
        cr.xdr_string("javaclient");
        cr.xdr_int(uid);
        cr.xdr_int(gid);
        if (gids == null) 
            cr.xdr_int(0);
        else {
            cr.xdr_int(gids.length);
            for (int i = 0; i < gids.length; i++)
                cr.xdr_int(gids[i]);
        }

        x.xdr_bytes(cr);

        x.xdr_int(0);		// no verifier
        x.xdr_int(0);		// no verifier
    }

    /**
     * Get Unix creds from an XDR buffer
     *
     * @param xdr buffer
     */
    void getCred(Xdr x) {

        x.xdr_int();	// assume it's AUTH_UNIX
        x.xdr_int();	// cred length
        x.xdr_int();	// timestamp
        x.xdr_string();	// hostname
        uid = x.xdr_int();
        gid = x.xdr_int();
        int count = x.xdr_int();
        if (count > 0) {
            gids = new int[count];
            for (int i = 0; i < count; i++)
                gids[i] = x.xdr_int();
        }
        x.xdr_int();	// no verifier
        x.xdr_int();	// no verifier
    }

    /**
     * Given a username and passwd, obtain Unix creds
     * from the named server.  This is not necessarily
     * an NFS server.
     *
     * If we fail then the creds are unaffected.
     *
     * @param server Name of the pcnfsd server that will return the creds.
     * @param  username the login name of the user.
     * @param  passwd of the user.
     *
     */
    public boolean fetchCred(String server, String username, String passwd) {

        username = disguise(username);
        passwd   = disguise(passwd);

        try {
            try {
               return (callV2(server, username, passwd));
            } catch (MsgAcceptedException e) {
                if (e.error != e.PROG_MISMATCH)
                    return false;
    
                    return (callV1(server, username, passwd));
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Set the cred back to the default: nobody/nobody
     */
    public void setCred() {
        uid = UID_NOBODY;
        gid = GID_NOBODY;
        gids = null;
    }

    /**
     * Set the uid, gid
     */
    public void setCred(int uid, int gid, int[] gids) {
        this.uid = uid;
        this.gid = gid;
        this.gids = gids;
    }

    /*
     * Disguise the string so that it's not
     * obvious to a casual snooper.
     */
    private String disguise(String s) {
        byte[] b = s.getBytes();

        for (int i = 0; i < b.length; i++)
            b[i] = (byte)((b[i] & 0x7f) ^ 0x5b);

        return (new String(b));
    }

    /**
     * Get the Unix user id for the user
     * @return uid
     */
    public int getUid() {
        return uid;
    }

    /**
     * Get the Unix group id for the user
     * @return gid
     */
    public int getGid() {
        return gid;
    }

    /**
     * Get the Unix group list for the user
     * @return gids
     */
    public int[] getGids() {
        return gids;
    }

    /**
     * Get the user's home directory path
     * @return pathname of home directory.
     */
    public String getHome() {
        return home;
    }

    /**
     * Get the user's home Unix umask
     * @return umask
     */
    public int getUmask() {
        return def_umask;
    }

    private boolean callV1(String server, String username, String passwd) 
        throws java.net.UnknownHostException, IOException {

        Rpc pc = new Rpc(server, 0, PCNFSDPROG, 1, "udp", MAXREPLY);
        Xdr call = new Xdr(MAXREPLY);
        pc.rpc_header(call, PCNFSD_AUTH);

        call.xdr_string(username);
        call.xdr_string(passwd);

        Xdr reply = pc.rpc_call(call, 10 * 1000, 2);

        status = reply.xdr_int();
        if (status == AUTH_RES_FAIL)
            return false;

        uid = reply.xdr_int();
        gid = reply.xdr_int();
        gids = null;
        home = null;
        def_umask = 0;

        return true;
    }

    private boolean callV2(String server, String username, String passwd) 
        throws java.net.UnknownHostException, IOException {

        Rpc pc = new Rpc(server, 0, PCNFSDPROG, 2, "udp", MAXREPLY);
        Xdr call = new Xdr(MAXREPLY);
        pc.rpc_header(call, PCNFSD2_AUTH);

        call.xdr_string("(anyhost)");	// XXX should be hostname
        call.xdr_string(username);
        call.xdr_string(passwd);
        call.xdr_string("Java client");	// comment

        Xdr reply = pc.rpc_call(call, 10 * 1000, 2);

        status = reply.xdr_int();
        if (status == AUTH_RES_FAIL)
            return false;

        uid = reply.xdr_int();
        gid = reply.xdr_int();
        gids = new int[reply.xdr_int()];
        for (int i = 0; i < gids.length; i++)
            gids[i] = reply.xdr_int();
        home = reply.xdr_string();
        def_umask = reply.xdr_int();

        return true;
    }

    public String toString() {
        String s = "AUTH_UNIX:\n   uid=" + uid + ",gid=" + gid + "\n";
	if (gids != null) {
            s += "   gids=";
            for (int i = 0; i < gids.length; i++)
                s += gids[i] + " ";
        }
        if (home != null)
            s += "\n   home=" + home;
        if (def_umask != 0)
            s += "\n   umask=0" + Long.toOctalString(def_umask);

        return s;
    }

    public void init(Connection conn, int prog, int vers) {
        // No-op
    }

    public boolean refresh(Connection conn, int prog, int vers) {
        // No-op
	return true;
    }

    public void wrap(Xdr x, byte[] arg) {
	// No-op
    }

    public int unwrap(Xdr x) {
	// No-op
	return 0;
    }

    public void validate(byte[] verifier, int verifiee) {
	// No-op
    }

    public void destroy(Rpc rpc) {
        // No-op
    }

}
