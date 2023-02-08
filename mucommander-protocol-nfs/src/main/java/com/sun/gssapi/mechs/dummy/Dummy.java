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

package com.sun.gssapi.mechs.dummy;

import java.security.Provider;

import com.sun.gssapi.Oid;
import com.sun.gssapi.GSSException;


/**
 * Dummy Mechanism plug in for JGSS
 * This is the properties object required by the JGSS framework.
 * All mechanism specific information is defined here.
 */

public final class Dummy extends Provider {

	private static String info = "JGSS Dummy Mechanism Provider";
	
	public Dummy() {

		super("JGSS Dummy Provider 1", 1.0, info);


		//list mechs supported
		put("JGSS.Mechs", "1.3.6.1.4.1.42.2.26.1.2");

		//configure 1.3.6.1.4.1.42.2.26.1.2
		put("JGSS.Mech.1.3.6.1.4.1.42.2.26.1.2.Alias", "dummy");
		put("JGSS.Mech.dummy.NAMES", "1.3.6.1.5.6.2:1.2.840.113554.1.2.1.1");
		put("JGSS.Mech.dummy.CRED", "com.sun.gssapi.dummy.DummyCred");
		put("JGSS.Mech.dummy.CONTEXT", "com.sun.gssapi.dummy.DummyCtxt");
		put("JGSS.Mech.dummy.NAME", "com.sun.gssapi.dummy.DummyName");


	}


	/**
	 * Package private method to return the oid of this mech.
	 */
	static Oid getMyOid() {

		return (M_myOid);
	}


	/**
	 * Package private method to return the number of tokens
	 * to be used in the context creation exchange.
	 */
	static int getNumOfTokExchanges() {

		return (M_tokNum);
	}

	
	//private variables
	private static Oid M_myOid;
	private static final int M_tokNum = 2;


	static {
		try {
                        M_myOid = new Oid("1.3.6.1.4.1.42.2.26.1.2");
		} catch (GSSException e) {
                        throw new NumberFormatException();
		}
	}
}
