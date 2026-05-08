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
import com.sun.rpc.*;
import java.util.Date;

/**
 *
 * NFS version 2 file attributes
 *
 */
class Fattr2 extends Fattr {
    int		ftype;
    long 	mode;
    long	nlink;
    long	uid;
    long	gid;
    long	size;
    long	blocksz;
    long	rdev;
    long	blocks;
    long	fsid;
    long	fileid;
    long	atime;
    long	mtime;
    long	ctime;

    Fattr2() {
    }

    Fattr2 (Xdr x) {
        this.getFattr(x);
    }

    void putFattr(Xdr x) {
        x.xdr_int(ftype);
        x.xdr_u_int(mode);
        x.xdr_u_int(nlink);
        x.xdr_u_int(uid);
        x.xdr_u_int(gid);
        x.xdr_u_int(size);
        x.xdr_u_int(blocksz);
        x.xdr_u_int(rdev);
        x.xdr_u_int(blocks);
        x.xdr_u_int(fsid);
        x.xdr_u_int(fileid);
        x.xdr_u_int(atime / 1000);	// sec
        x.xdr_u_int(atime % 1000);	// msec
        x.xdr_u_int(mtime / 1000);	// sec
        x.xdr_u_int(mtime % 1000);	// msec
        x.xdr_u_int(ctime / 1000);	// sec
        x.xdr_u_int(ctime % 1000);	// msec
    }

    void getFattr(Xdr x) {
        long oldmtime = mtime;

        ftype  = x.xdr_int();
        mode    = x.xdr_u_int();
        nlink   = x.xdr_u_int();
        uid     = x.xdr_u_int(); if (uid == NFS_NOBODY) uid = NOBODY;
        gid     = x.xdr_u_int(); if (gid == NFS_NOBODY) gid = NOBODY;
        size    = x.xdr_u_int();
        blocksz = x.xdr_u_int();
        rdev    = x.xdr_u_int();
        blocks  = x.xdr_u_int();
        fsid    = x.xdr_u_int();
        fileid  = x.xdr_u_int();
        atime   = x.xdr_u_int() * 1000 + x.xdr_u_int();
        mtime   = x.xdr_u_int() * 1000 + x.xdr_u_int();
        ctime   = x.xdr_u_int() * 1000 + x.xdr_u_int();

        /*
         * We want the cache time to be short
         * for files/dirs that change frequently
         * and long for files/dirs that change
         * infrequently. So set the cache time to
         * the delta between file modifications
         * limited by ACMIN and ACMAX
         */
        long delta = mtime - oldmtime;
        if (delta > 0) {
            cachetime = delta;
            if (cachetime < ACMIN)
                cachetime = ACMIN;
            else if (cachetime > ACMAX)
                cachetime = ACMAX;
        }
        validtime = System.currentTimeMillis();
    }

    public String toString() {
         return (
             "  ftype = " + ftype + "\n" +
             "   mode = 0" + Long.toOctalString(mode) + "\n" +
             "  nlink = " + nlink + "\n" +
             "    uid = " + uid + "\n" +
             "    gid = " + gid + "\n" +
             "   size = " + size + "\n" +
             "blocksz = " + blocksz + "\n" +
             "   rdev = 0x" + Long.toHexString(rdev) + "\n" +
             " blocks = " + blocks + "\n" +
             "   fsid = " + fsid + "\n" +
             " fileid = " + fileid + "\n" +
             "  atime = " + new Date(atime) + "\n" +
             "  mtime = " + new Date(mtime) + "\n" +
             "  ctime = " + new Date(ctime)
         );
    }
}
