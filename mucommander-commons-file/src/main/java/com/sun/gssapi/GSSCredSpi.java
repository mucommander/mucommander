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
package com.sun.gssapi;


/**
 * This interface is implemented by each mechanism to provide the
 * functionality of a credential. Each GSSCredential uses provider
 * objects implementing this interface. A GSSCredential may have
 * several credential elements underneath it, but each GSSCredSpi
 * object can represent at most 1 credential element.
 */
public interface GSSCredSpi {

    /**
     * Initialized the credential object. Called after the
     * object is first instantiated.
     *
     * @param desiredName - desired name of the principal
     * @param initLifetime - desired lifetime for the init
     *	credential; 0 signals use mechanism default
     * @param acceptLifetime - desired lifetime for the accept
     *	credential; 0 signals use mechanism default
     * @param usage - the desired usage for this credential
     * @exception - GSSException may be thrown
     */
    public void init(GSSNameSpi desiredName, int initLifetime,
		int acceptLifetime, int usage) throws GSSException;
  

    /**
     * Called to invalidate this credential element and release
     * any system recourses and cryptographic information owned
     * by the credential.
     *
     * @exception GSSException with major codes NO_CRED and FAILURE
     */
    public void dispose() throws GSSException;


    /**
     * Returns the principal name for this credential. The name
     * is in mechanism specific format.
     *
     * @return GSSNameSpi representing principal name of this credential
     * @exception GSSException may be thrown
     */
    public GSSNameSpi getName() throws GSSException;


    /**
     * Returns the init lifetime remaining.
     *
     * @return the init lifetime remaining in seconds
     * @exception GSSException may be thrown
     */
    public int getInitLifetime() throws GSSException;

	
    /**
     * Returns the accept lifetime remaining.
     *
     * @return the accept lifetime remaining in seconds
     * @exception GSSException may be thrown
     */
    public int getAcceptLifetime() throws GSSException;


    /**
     * Returns the lifetime remaining. This should take
     * into account the credential usage, and return the
     * appropriate lifetime. See RFC 2078 for details.
     *
     * @return the lifetime remaining in seconds
     * @exception GSSException may be thrown
     */
    public int getLifetime() throws GSSException;

	
    /**
     * Returns the credential usage. This must be one
     * GSSCredential.ACCEPT_ONLY, GSSCredential.INITIATE_ONLY,
     * or GSSCredential.INITIATE_AND_ACCEPT.
     *
     * @return the credential usage
     * @exception GSSException may be thrown
     */
    public int getUsage() throws GSSException;


    /**
     * Returns the oid representing the underlying credential
     * mechanism oid.
     *
     * @return the Oid for this credential mechanism
     * @exception GSSException may be thrown
     */
    public Oid getMechanism();
}
