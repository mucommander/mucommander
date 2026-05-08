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

import com.sun.gssapi.*;

/**
 * Implements the dummy credential class.
 */
public class DummyCred implements GSSCredSpi {

	
	/**
	 * Standard constructor.
	 */
	public DummyCred() {
		m_freed = false;
	}


	/**
	 * Initialization entry point.
	 *
	 * @param name of the credential entity; can be null
	 *	meaning a system default
	 * @param Desired lifetime for the credential's ability
	 *	to initialize contexts; 0 means use default
	 * @param Desired lifetime for the credential's ability
	 *	to accept contexts; 0 means default
	 * @param Credential usage flag.
	 * @exception GSSException may be thrown
	 */
	public void init(GSSNameSpi aName, int initLifetime,
			int acceptLifetime, int usage) throws GSSException {

		//set the name
		if (aName == null)
			m_myName = DummyName.getDefault();
		else {
			//must be a dummy name
			if (!(aName instanceof DummyName))
				throw new GSSException(GSSException.BAD_NAME);

			m_myName = (DummyName)aName;
		}

		//get my lifetime
		if (initLifetime == 0)
			m_initLifetime = GSSCredential.INDEFINITE;
		else
			m_initLifetime = initLifetime;

		if (acceptLifetime == 0)
			m_acceptLifetime = GSSCredential.INDEFINITE;
		else
			m_acceptLifetime = acceptLifetime;
		

		m_usage = usage;
		m_freed = false;
	}
  

	/**
	 * Release this mechanism specific credential element.
	 *
	 * @exception GSSException with major codes NO_CRED and FAILURE.
	 */
	public void dispose() throws GSSException {

		m_freed = true;
		m_myName = null;
	}


	/**
	 * Returns the name for the credential.
	 *
	 * @exception GSSException may be thrown
	 */
	public GSSNameSpi getName() throws GSSException {

		//some debugging code
       		if (m_freed)
			throw new GSSException(GSSException.NO_CRED);

		return (m_myName);
	}


	/**
	 * Returns the remaining context initialization lifetime in
	 * seconds.
	 *
	 * @exception GSSException may be thrown
	 */
	public int getInitLifetime() throws GSSException {

     		//check if this is valid
       		if (m_freed)
			throw new GSSException(GSSException.NO_CRED);
		
		//return time based on usage
		if (getUsage() == GSSCredential.ACCEPT_ONLY)
			return (0);
		
		return (m_initLifetime);
	}

       	/**
	 * Returns the remaining context acceptance lifetime in
	 * seconds.
	 *
	 * @exception GSSException may be thrown
	 */

	public int getAcceptLifetime() throws GSSException {

		//sanity check
       		if (m_freed)
			throw new GSSException(GSSException.NO_CRED);

		//take usage into account
		if (getUsage() == GSSCredential.INITIATE_ONLY)
			return (0);
		
		return (m_acceptLifetime);
	}


        /**
	 * Returns the remaining context lifetime in
	 * seconds. This takes into account the usage property and
	 * returns the minimum remaining.
	 *
	 * @exception GSSException may be thrown
	 */

	public int getLifetime() throws GSSException {

		//sanity check
       		if (m_freed)
			throw new GSSException(GSSException.NO_CRED);

		//take usage into account, return minimum remaining time
		if (getUsage() == GSSCredential.ACCEPT_ONLY)
			return (m_acceptLifetime);
		else if (getUsage() == GSSCredential.INITIATE_ONLY)
			return (m_initLifetime);

		if (m_initLifetime < m_acceptLifetime)
			return (m_initLifetime);

		return (m_acceptLifetime);
	}


      	/**
	 * Returns the credential usage flag.
	 *
	 * @exception GSSException may be thrown
	 */

	public int getUsage() throws GSSException {

		//sanity check
       		if (m_freed)
			throw new GSSException(GSSException.NO_CRED);

		return (m_usage);
	}

	/**
	 * Returns the credential mechanism. Since this is a single
	 * credential element only a single oid can be returned.
	 */
	public Oid getMechanism() {

		return (Dummy.getMyOid());
	}


	//instance variables
	private DummyName m_myName;
	private int m_usage;
        private int m_initLifetime;
	private int m_acceptLifetime;
	private boolean m_freed;
	
}
