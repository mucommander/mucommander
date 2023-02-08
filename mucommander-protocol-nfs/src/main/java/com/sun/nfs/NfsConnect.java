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
import java.util.Hashtable;

/**
 *
 * Connect to an NFS server and return an
 * Nfs file object.
 *
 * @see Nfs
 * @see Nfs2
 * @see Nfs3
 * @author Brent Callaghan
 */
public class NfsConnect {

    private static byte[] pubfh2 = new byte[32];  // v2 public filehandle
    private static byte[] pubfh3 = new byte[0];   // v3 public filehandle
    private static Hashtable cacheNfsConnect = new Hashtable();

    static final int NFS_PORT = 2049;
    static final int NFS_PROG = 100003;
    static final int MAXBUF = 32768 + 512; // Max size of NFS reply

    String server;
    int port;
    int version;
    String proto;
    boolean pub;
    static CredUnix cred = new CredUnix();
    static RpcHandler rhandler;
    static String sec_flavor;

    NfsConnect(String server, int port, int version, String proto, boolean pub)
    {
        this.server = server;
        this.port = port;
        this.version = version;
        this.proto = proto;
        this.pub = pub;
    }

    /**
     * Return an Nfs object given an NFS URL
     *
     * This code will also obtain testing options
     * if present in the URL and use them to set specific
     * configurations.
     *
     * @param url	NFS URL
     * @returns		The Nfs object
     */
    static Nfs connect(String urlstr) throws IOException {

        NfsURL url = new NfsURL(urlstr);

        String server = url.getHost();
        int port      = url.getPort();
        String path   = url.getFile();

        int version   = url.getVersion();
        String proto  = url.getProto();
        boolean pub   = url.getPub();

        NfsConnect n = null;

        if (server == null)
                throw new java.net.UnknownHostException();

        if (version == 0 && proto == null && pub) // no testing options
            n = NfsConnect.cache_get(server);

        if (n == null)
            return (connect(server, path, port, version, proto, pub));
        else
            return (connect(server, path, n.port, n.version, n.proto, n.pub));
    }

    /**
     * Return an Nfs object given a server and pathname
     *
     * We're not fussy about NFS version or whether TCP
     * or UDP is used.
     *
     * @param server	The server that hosts the object
     * @param name	The pathname of the object
     * @returns		The object
     */
    static Nfs connect(String server, int port, String path)
        throws IOException {

        NfsConnect n = NfsConnect.cache_get(server);
        if (n == null)
            return (connect(server, path, port, 0, null, true));
        else
            return (connect(server, path, n.port, n.version, n.proto, n.pub));
    }
    
    /**
     * Return an Nfs object given a server, pathname, version, and protocol
     *
     * Try to obtain the filehandle for the object via a public filehandle
     * otherwise fall back to the mount protocol.
     *
     * @param server	The server that hosts the object
     * @param name	The pathname of the object
     * @param vers	The version of NFS to be used.  If zero then
     * 			prefer v3 over v2.
     * @param proto	The transport protocol to be used: "tcp" or "udp."
     *			If this is null then prefer TCP over UDP.
     * @param pub	Boolean Public filehandle support.
     */
    static Nfs connect(String server, String path, int port, int vers,
                                    String proto, boolean pub)
        throws IOException {

        if (port == 0)
            port = NFS_PORT;

        if (path == null || path.length() == 0)
            path = ".";

        /*
         * Check first if we already have the file/dir cached
         */
        Nfs nfs = Nfs.cache_get(server, path);
        if (nfs != null) {
            nfs.getattr();	// for close-to-open consistency

	    if (nfs.isSymlink())
		return followLink(nfs);

            return (nfs);
        }

        /*
         * First set up a connection using the specified proto.
         * If neither "tcp" or "udp" is specified, then try
         * "tcp" and fall back to "udp" if that fails.
         *
         * Note that we check to see if there's an existing
         * connection to the server and use that if available.
         */

        Connection conn;

        if (proto == null) {
            conn = Connection.getCache(server, port, "tcp");
            if (conn == null)
                conn = Connection.getCache(server, port, "udp");

            if (conn == null)  {	// no cached connections
                try {
                    conn = new ConnectSocket(server, port, MAXBUF);
                    Connection.putCache(conn);
                    proto = "tcp";

                } catch (java.net.UnknownHostException e) {
                    throw e;	// don't catch as an IOException

                } catch (IOException e) {
                    conn = new ConnectDatagram(server, port, MAXBUF);
                    Connection.putCache(conn);
                    proto = "udp";
                }
            }
        } else if (proto.equals("tcp")) {
            conn = Connection.getCache(server, port, "tcp");
            if (conn == null) {
                conn = new ConnectSocket(server, port, MAXBUF);
                Connection.putCache(conn);
            }

        } else if (proto.equals("udp")) {
            conn = Connection.getCache(server, port, "udp");
            if (conn == null) {
                conn = new ConnectDatagram(server, port, MAXBUF);
                Connection.putCache(conn);
            }

        } else {
            throw new IOException("Unknown protocol: " + proto);
        }


        /*
         * Try using the public filehandle
         */

        if (pub) {
            try {
                switch (vers) {
                case 0:
                    try {
                        nfs = tryNfs(conn, pubfh3, path, 3, false);
                        vers = 3;
                    } catch (MsgAcceptedException e) {
                        if (e.error != e.PROG_MISMATCH)
                            throw e;
    
                        vers = 2;
                        nfs = tryNfs(conn, pubfh2, path, 2, false);
                    }
                    break;
                case 2:
                    nfs = tryNfs(conn, pubfh2, path, 2, false);
                    break;
                case 3:
                    nfs = tryNfs(conn, pubfh3, path, 3, false);
                    break;
                }
    
            } catch (MsgAcceptedException e) {
                if (e.error != e.GARBAGE_ARGS)
                    throw e;

                // Not WebNFS but which version of NFS ?  Assume 3

                if (vers == 0)
                    vers = 3;

            } catch (NfsException e) {
                if (e.error != e.NFSERR_STALE && e.error != e.NFSERR_BADHANDLE)
                    throw e;
    
                if (vers == 0)
                    vers = 3;	// if we get here then server must be v3
            }
    
            if (nfs != null) {	// public fh worked
                NfsConnect.cache_put(new NfsConnect(server, port, vers, proto, true));
                return (nfs);
            }
        }
	
        /*
         * Server doesn't accept public filehandles
         * Resort to using the MOUNT protocol
         */
        if (path.equals("."))
            path = "/";

	Mount m = new Mount();
        byte[] fh = m.getFH(server, path, vers);
	sec_flavor = m.getSec();

        NfsConnect.cache_put(new NfsConnect(server, port, vers, proto, false));

        return (tryNfs(conn, fh, path, vers, true));
    }

    private static Nfs tryNfs(Connection conn, byte[] pubfh, String path,
            int vers, boolean mount)
        throws IOException {

        Nfs pubnfs;

        Rpc rpc = new Rpc(conn, NFS_PROG, vers);
        /*
         * Use the default security flavor.
         */
        String defaultSec = NfsSecurity.getDefault();
        if (defaultSec.equals("1")){
	    // AUTH_SYS
            rpc.setCred(cred);
        } else {
	    if (NfsSecurity.getMech(defaultSec) != null) {
	    // if there is a mechOid, use RPCSEC_GSS
		rpc.setCred(new CredGss("nfs",
			NfsSecurity.getMech(defaultSec),
			NfsSecurity.getService(defaultSec),
			NfsSecurity.getQop(defaultSec)));
	    } else {
	    // not RPCSEC_GSS; use AUTH_SYS for now
		rpc.setCred(cred);
	    }
        }
        rpc.setRpcHandler(rhandler);

        if (vers == 2)
            pubnfs = new Nfs2(rpc, pubfh, path, null);
        else
            pubnfs = new Nfs3(rpc, pubfh, path, null);

        if (path.equals("/."))  // special path for testing
            return pubnfs;

        if (mount) {
            if (NfsSecurity.getMech(sec_flavor) != null) {
                rpc.setCred(new CredGss("nfs",
                        NfsSecurity.getMech(sec_flavor),
                        NfsSecurity.getService(sec_flavor),
                        NfsSecurity.getQop(sec_flavor)));
            }

            pubnfs.getattr();
            Nfs.cache_put(pubnfs);

            return (pubnfs);
        }
	/* The path passed to lookup will be null since it
	 * has been filled in when object has been created.
	 */
        return (pubnfs.lookup(null));
    }


    /*
     * Given a symbolic link - read its text
     * find the NFS object it refers to.
     *
     * XXX needs to contain a loop (max 30x) to follow
     * chains of symlinks properly.
     */
    static Nfs followLink(Nfs link) throws IOException {

        String base = link.name;
        String text = link.readlink();
        
        String server = link.rpc.conn.server;
        int port = link.rpc.conn.port;
        String newpath;

        if (text.startsWith("nfs://")) {	// NFS URL
            NfsURL url = new NfsURL(text);
            server =  url.getHost();
            port = url.getPort();
            newpath = url.getFile();

        } else if (text.startsWith("/")) {	// absolute path
            newpath = text;

        } else {

            /*
             * Combine base path with relative path
             * according to rules in RFCs 1808 & 2054
             *
             * e.g.  /a/b/c + x    = /a/b/x
             *       /a/b/c + /y   = /z
             *       /a/b/c + ../z = /a/z
             */
            int head = 0;
            int tail = base.lastIndexOf('/');
            int len = text.length();
    
            while (text.regionMatches(head, "..", 0, len - head) ||
                    text.startsWith("../", head)) {
                head += 3;
                if (head >= len)
                    break;
                tail = base.lastIndexOf('/', tail - 1);
            }
            if (head > len)
                head = len;
            if (tail < 0)
                tail = 0;
            else
                tail++;
    
            newpath = base.substring(0, tail) + text.substring(head);
        }

        try {
            return (NfsConnect.connect(server, port, newpath));
        } catch (IOException e) {
            System.err.println(e + ": symbolic link: " +
                base + " -> " + text);
            return (link);
        }
    }

    /**
     * Cache an NfsConnect object
     *
     * @param n	the object to be cached
     */
    private static void cache_put(NfsConnect n) {
        cacheNfsConnect.put(n.server, n);
    }

    /**
     * Retrieve a cached NfsConnect object
     *
     * @param server	The server that hosts the object
     * @returns		The object - or null if not cached
     */
    private static NfsConnect cache_get(String server) {
        return ((NfsConnect)cacheNfsConnect.get(server));
    }

    /**
     * Get the credential as a Unix cred
     *
     * @returns		The credential stored for Nfs operations
     */
    public static CredUnix getCred() {
	return (cred);
    }

    /**
     * Set the timeout handler
     */
    public static void setRpcHandler(RpcHandler r) {
        rhandler = r;
    }
}
