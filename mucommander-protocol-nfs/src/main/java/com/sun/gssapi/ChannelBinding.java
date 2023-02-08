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

import java.net.InetAddress;

/**
 * The JGSS accommodates the concept of caller-provided channel
 * binding information. Channel bindings are used to strengthen
 * the quality with which peer entity authentication is provided
 * during context establishment. They enable the JGSS callers to
 * bind the establishment of the a security context to relevant
 * characteristics like addresses or to application specific data.
 * <p>
 * The caller initiating the security context must determine the
 * appropriate channel binding values to set in the GSSContext
 * object. The acceptor must provide identical binding in order
 * to validate that received tokens possess correct
 * channel-related characteristics.
 * <p>
 * Use of channel bindings is optional in JGSS. Since channel-
 * binding information may be transmitted in context establishment
 * tokens, applications should therefore not use confidential data
 * as channel-binding components.
 * @see GSSContext#setChannelBinding
 * @see java.net.InetAddress
 */

public class ChannelBinding {

    private InetAddress m_initiator;
    private InetAddress m_acceptor;
  
    private  byte[] m_appData;

    /**
     * Construct a channel bindings object that contains all the user
     * specified tags.
     *
     * @param initAddr the address of the context initiator
     * @param acceptAddr address of the context acceptor
     * @param appData a byte array of application data to be used as
     *	part of the channel-binding
     */
    public ChannelBinding(InetAddress initAddr, InetAddress acceptAddr,
			byte[] appData) {

	m_initiator = initAddr;
	m_acceptor = acceptAddr;

	if (appData != null) {
	    m_appData = new byte[appData.length];
	    java.lang.System.arraycopy(appData, 0, m_appData, 0,
				m_appData.length);
	}
    }

    
    /**
     * Construct a channel bindings object without any addressing 
     * information.
     *
     * @param appData a byte array of application data to be used as
     *	part of the channel-binding
     */
    public ChannelBinding(byte[] appData) {
    
	m_initiator = null;
	m_acceptor = null;
	m_appData = new byte[appData.length];
	java.lang.System.arraycopy(appData, 0, m_appData, 0,
					m_appData.length);
    }

    /**
     * Get the initiator's address for this channel binding.
     *
     * @return the initiator's address. null if no address
     *	information is contained
     */
    public InetAddress getInitiatorAddress() {
    
	return m_initiator;
    }

    /**
     * Get the acceptor's address for this channel binding.
     *
     * @return the acceptor's address. null if no address
     *	information is contained
     */
    public InetAddress getAcceptorAddress() {
		
	return m_acceptor;
    }

  
    /**
     * Get the application specified data for this channel binding.
     * The byte array is not copied.
     *
     * @return byte[] the application data that comprises this
     *			channel-binding
     */
    public byte[] getApplicationData() {
    
	return m_appData;
    }
	

    /**
     * Compares two instances of ChannelBinding
     *
     * @return true if objects are the same
     * @overrides java.lang.Object#equals
     */
    public boolean equals(Object obj) {
	
	if (! (obj instanceof ChannelBinding))
	    return false;
    
	ChannelBinding cb = (ChannelBinding)obj;
    
	//check for application data being null in one but not the other
	if ((getApplicationData() == null &&
			cb.getApplicationData() != null) ||
			(getApplicationData() != null &&
			cb.getApplicationData() == null))
		return (false);

	return (this.m_initiator.equals(cb.getInitiatorAddress()) &&
		this.m_acceptor.equals(cb.getAcceptorAddress()) &&
		(this.getApplicationData() == null ||
		this.m_appData.equals(cb.getApplicationData())));
    }
}
