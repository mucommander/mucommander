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

package com.sun.xfile;

import java.net.MalformedURLException;

/**
 * This is just a dumb URL parser class.
 * I wrote it because I got fed up with the
 * JDK URL class calling NFS URL's "invalid"
 * simply because the Handler wasn't installed.
 *
 * @author Brent Callaghan
 */
class XFurl {

    private String url;
    private String protocol;
    private String location;
    private String path;

    XFurl(String url) throws MalformedURLException {
        int p, q, r;

        url = url.trim();	// remove leading & trailing spaces
        this.url = url;
        int end = url.length();

        p = url.indexOf(':');
        if (p < 0)
            throw new MalformedURLException("colon expected");
        protocol = url.substring(0, p);
        q = p;
        p++;	// skip colon
        if (url.regionMatches(p, "//", 0, 2)) {	// have hostname
            p += 2;
            q = url.indexOf('/', p);
            if (q < 0)
                q = end;
            location = url.substring(p, q);
        }

        path = q < end ? url.substring(q + 1, end) : "";

        // Remove trailing slashes from path

        while (path.endsWith("/"))
            path = path.substring(0, path.length()-1);
    }

    XFurl(XFurl base, String rpath) throws MalformedURLException {

        protocol = base.getProtocol();
        location = base.getLocation();
        path = base.getPath();

        rpath = rpath.trim();

        if (rpath.indexOf("://") > 0) {	// URL - ignore base
            url = rpath;
            XFurl u = new XFurl(rpath);
            protocol = u.getProtocol();
            location =  u.getLocation();
            path = u.getPath();

        } else if (rpath.startsWith("/")) {	// absolute path
            path = rpath.substring(1);

        } else {

            /*
             * Escape any "%" characters in the name
             * e.g. "%markup" -> "%25markup"
             */
            String npath = "";
            int len = rpath.length();
            int p1 = 0, p2;

            while (true) {
                p2 = rpath.indexOf('%', p1);  // look for %
                if (p2 < 0)
                    p2 = len;

                npath += rpath.substring(p1, p2);
                if (p2 >= len)
                    break;

                npath += "%25";	// replace % with %25
                p1 = p2 + 1;
            }
            rpath = npath;
            len = rpath.length();
 
            /*
             * Combine base path with relative path
             * according to rules in RFCs 1808 & 2054
             *
             * e.g.  /a/b/c + x    = /a/b/c/x
             *       /a/b/c + /y   = /y
             *       /a/b/c + ../z = /a/b/z
             *       /a/b/c + d/.  = /a/b/c/d
             */
            String bpath = base.getPath();
            p1 = 0;
    
            while (p1 <= len) {
                p2 = rpath.indexOf("/", p1);
                if (p2 < 0)
                    p2 = len;
                String component = rpath.substring(p1, p2);

                if (component.equals(".") || component.equals("")) {
                    // ignore
                } else if (component.equals("..")) {
                    int q = bpath.lastIndexOf("/");
                    bpath = q < 0 ? "" : bpath.substring(0, q);
                } else {
                    if (bpath.equals(""))
                        bpath = component;
                    else
                        bpath += "/" + component;
                }
                p1 = p2 + 1;
            }
            path = bpath;
        }
    }

    String getProtocol() {
        return (protocol);
    }

    String getLocation() {
        return (location);
    }

    String getPath() {
        return (path);
    }

    String getParent() {

        if (path.equals(""))
            return null;	// no parent

        String s = protocol + ":";

        if (location != null)
            s += "//" + location;

        int index = path.lastIndexOf('/');
        if (index >= 0)
            s += "/" + path.substring(0, index);

        return s;
    }

    String getName() {
        int index = path.lastIndexOf('/');
        return index < 0 ? path : path.substring(index + 1);
    }

    public String toString() {
        String s = protocol + ":";

        if (location != null)
            s += "//" + location;

        if (path != null)
            s += "/" + path;

        return (s);
    }
}
