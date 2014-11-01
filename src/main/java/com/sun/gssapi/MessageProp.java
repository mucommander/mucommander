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
 * This class conveys information about the per-message security
 * services requested/provided through the GSSContext class in the
 * wrap and getMIC methods. It allows the caller to specify
 * the desired per-message Quality of Protection (QOP) and
 * privacy state. Upon return from these calls, this object indicates
 * the actual applied QOP and confidentiality state. 
 *
 * Instances of this class are also used by the unwrap and verifyMIC
 * methods to obtain the message protection applied by the peer. The
 * QOP indicates the algorithm used to protect the message. The
 * privacy flag indicates if message privacy has been applied. Methods
 * are also provided to query for supplementary status information
 * for the processed token.
 *
 * @see GSSContext#wrap
 * @see GSSContext#unwrap
 * @see GSSContext#getMIC
 * @see GSSContext#verifyMIC
 */
 
public class MessageProp {

    private boolean m_privacyState;
    private int m_qop;
    private boolean m_dupToken;
    private boolean m_oldToken;
    private boolean m_unseqToken;
    private boolean m_gapToken;

    /**
     * Default constructor for the class.  QOP is set to 0 and
     * confidentiality to false.
     */
    public MessageProp () {

        m_qop = 0;
        m_privacyState = false;
        resetStatusValues();
    }
    

    /**
     * Constructor allowing the setting of the qop and
     * the privacy state.
     *
     * @param qop the qop value for the message
     * @param privState indicates privacy request/applied state
     */
    public MessageProp(int qop, boolean privState) {
    
        m_qop = qop;
        m_privacyState = privState;
        resetStatusValues();
    }
 
 
    /**
     * Retrieves the QOP value.
     *
     * @return the QOP value
     */
    public int getQOP() {
    
        return (m_qop);
    }


    /**
     * Retrieves the privacy state.
     *
     * @return true indicates privacy has been applied
     */
    public boolean getPrivacy() {
    
        return (m_privacyState);
    }


    /**
     * Sets the QOP value.
     *
     * @param QOP value to store.
     */
    public void setQOP(int qopVal) {

        m_qop = qopVal;
    }


    /**
     * Sets the privacy state.
     *
     * @param privacy value to store.
     */
    public void setPrivacy(boolean privState) {
    
        m_privacyState = privState;
    }


    /**
     * Method to signal supplementary information.
     * Returns true if this is a duplicate of an earlier token.
     */
    public boolean isDuplicateToken() {

        return (m_dupToken);
    }


    /**
     * Method to signal supplementary information.
     * Returns true if the token's validity period has expired.
     */
    public boolean isOldToken() {

        return (m_oldToken);
    }


    /**
     * Method to signal supplementary information.
     * Returns true if a later token has already been processed.
     */
    public boolean isUnseqToken() {

        return (m_unseqToken);
    }


    /**
     * Method to signal supplementary information.
     * Returns true if an expected per-message token was not
     * received.
     */
    public boolean isGapToken() {

        return (m_gapToken);
    }


    /*
     * The following methods are to set the supplementary
     * status values. These should be JGSS private, but
     * the mechanism packages will need access to them.
     */

    /**
     * Used by mechanism packages to set supplementary status value.
     * Indicates that an expected per-message token was
     * not received.
     */
    public void setGapTokenStatus() {

        m_gapToken = true;
    }
    

    /**
     * Used by mechanism packages to set supplementary status value.
         * Indicates this is a duplicate of an earlier token.
     */
    public void setDuplicateTokenStatus() {
    
        m_dupToken = true;
    }
    
    
    /**
     * Used by mechanism packages to set supplementary status value.
     * Indicates that the token's validity period has expired.
     */
    public void setOldTokenState() {
    
        m_oldToken = true;
    }
    
    
    /**
     * Used by mechanism packages to set supplementary status value.
     * Indicates that a later token has already been processed.
         */
    public void setUnseqTokenStatus() {
    
        m_unseqToken = true;
    }


    /**
     * Resets the supplementary status values to false.
     */
    void resetStatusValues() {

        m_dupToken = false;
        m_oldToken = false;
        m_unseqToken = false;
        m_gapToken = false;
    }
}
