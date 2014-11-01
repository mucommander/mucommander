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
package com.sun.zip;

import java.net.MalformedURLException;

/**
 * @author Brent Callaghan
 */

public class ZipURL {

    private String url;
    private String protocol;
    private String location;
    private String path;

    /*
     * Undocumented testing options
     */
    private int version;
    private String proto;
    private boolean pub = true;

    public ZipURL(String url) throws MalformedURLException {
        int p, q, r;
        int end = url.length();

        url = url.trim();	// remove leading & trailing spaces
        this.url = url;

        p = url.indexOf(':');
        if (p < 0)
            throw new MalformedURLException("colon expected");
        protocol = url.substring(0, p);
        p++;	// skip colon
        if (url.regionMatches(p, "//", 0, 2)) {	// have location
            p += 2;
            q = url.indexOf('/', p);
            if (q < 0)
                q = end;
            location = url.substring(0, q);
            r = q;	// no port
            if (p < r)
                location = url.substring(p, r);
        } else {
             q = p;
        }

        if (q < end)
            path = url.substring(q + 1, end);

    }

    public String getProtocol() {
        return (protocol);
    }

    public String getLocation() {
        return (location);
    }

    public String getPath() {
        return (path);
    }

    public String toString() {
        String s = getProtocol() + ":";

        if (location != null)
            s += "//" + location;

        if (path != null)
            s += "/" + path;

        return (s);
    }
}
