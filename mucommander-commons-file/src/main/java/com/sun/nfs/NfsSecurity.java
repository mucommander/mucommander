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

import java.util.*;
import com.sun.rpc.*;

/**
 * NfsSecurity is a static class.  It reads in the com.sun.properties.nfssec
 * properties file and provides the vehicle to retrieve properties values
 * which are the (mechanism, service, qop) mappings for the NFS security pseudo
 * flavor numbers.
 *
 * @author Lin Ling
 */
public final class NfsSecurity {

    static ResourceBundle props;
    static String secName, secMode, mech;
    static int service, qop;
    static {
	initialize();
    }

    private static void initialize() {

	try {
	    props = ResourceBundle.getBundle("com.sun.nfs.nfssec");
	} catch (MissingResourceException e) {
	    props = null;
	}
    }

    /*
     *  Parse the string value using ":" as the delimiter.
     *
     *	nfsSecName:mechanismOID:service:qualityProtection
     *
     *  (e.g. dummyp:1.3.6.1.4.1.42.2.26.1.2:privacy:0)
     *
     */
    private static void parseValue(String value) {
	
	StringTokenizer parser = new StringTokenizer(value, ":\n\r");

	secName = parser.nextToken();

	try {
	    mech = parser.nextToken();
	} catch (NoSuchElementException e) {
	    // non-RPCSEC_GSS flavors
	    mech = null;
	    service = 0;
	    qop = 0;
	    return;
	}

	String serviceString = parser.nextToken();
	if (serviceString.equals("none"))
		service = Cred.SVC_NONE;
	else if (serviceString.equals("integrity"))
		service = Cred.SVC_INTEGRITY;
	else if (serviceString.equals("privacy"))
		service = Cred.SVC_PRIVACY;
	else
		service = Cred.SVC_PRIVACY;	// just use privacy service

	qop = Integer.parseInt(parser.nextToken());

    }

    /**
     *  Does the key have a value defined in the nfssec.properties file?
     *  (i.e. is key=value defined in the properties list?)
     *
     *  @param key 	the key to be searched
     *  @returns	true or false
     */
    public static boolean hasValue(String key) {
	
	if (props == null)
	    return false;

	try {
	    props.getString(key);
	    return true;

	} catch (MissingResourceException e) {
	    return false;
	}
    }

    /**
     *  Get the default security flavor number if it is specified in the
     *  nfssec.properties file, otherwise, simply return "1" for AUTH_SYS.
     */
    public static String getDefault() {

	if (props == null)
	    return "1";

	try {	
	    return props.getString("default");
	} catch (MissingResourceException e) {
	    return "1";
	}
    }

    /**
     *  Get the preferred nfs security flavor number if it is specified
     *  in the nfssec.properties file, otherwise, return null.
     */
    public static String getPrefer() {

	if (props == null)
	    return null;

	try {
	    return props.getString("prefer");
	} catch (MissingResourceException e) {
	    return null;
	}
    }

    /**
     * getName will get the NFS security flavor name from the first token
     * in the value.
     *
     * 		key=nfsSecName:mechOid:service:qop
     *		    ^^^^^^^^^^
     * 
     * @param key	the key to be searched
     * @returns		NFS Security flavor name
     */
    public static String getName(String key) {

	if (key.equals(secMode)) {
	    return secName;
	}

	parseValue(props.getString(key));
	secMode = key;
	return secName;
    }

    /**
     * getMech will get the security mechanism OID string from the second token
     * in the value.
     *
     * 		key=nfsSecName:mechOid:service:qop
     *		    	       ^^^^^^^
     * 
     * @param key	the key to be searched
     * @returns 	security mechansim OID string
     */
    public static String getMech(String key) {

	if (key.equals(secMode)) {
	    return mech;
	}

	parseValue(props.getString(key));
	secMode = key;
	return mech;
    }

    /**
     * getService will get the security service type from the third token
     * in the value.
     *
     * 		key=nfsSecName:mechOid:service:qop
     *		    	               ^^^^^^^
     * 
     * @param key	the key to be searched
     * @returns		one of (none, integrity, privacy); if the third token
     *			in the value does not have the expected data, simply
     *			returns the privacy service number. 
     */
    public static int getService(String key) {

	if (key.equals(secMode)) {
	    return service;
	}

	parseValue(props.getString(key));
	secMode = key;
	return service;
    }

    /**
     * getQop will get the Quality of Protection number from the fourth token
     * in the value.
     *
     * 		key=nfsSecName:mechOid:service:qop
     *		    	                       ^^^
     * 
     * @param key	the key to be searched
     * @returns		qop number; 0 means the mechanism-specific default qop
     */
    public static int getQop(String key) {

	if (key.equals(secMode)) {
	    return qop;
	}

	parseValue(props.getString(key));
	secMode = key;
	return qop;
    }
}
