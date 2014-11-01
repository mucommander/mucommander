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

import java.io.InputStream;
import java.io.OutputStream;


/**
 * An object of this class implements the functionality of a GSSContext
 * for a specific mechanism.
 * A GSSCtxtSpi object can be thought of having 3 states:
 *    -before initialization
 *    -during initialization with its peer
 *    -after it is established
 * <p>
 * The context options can only be requested in state 1. In state 3,
 * the per message operations are available to the callers. The get
 * methods for the context options will return the requested options
 * while in state 1 and 2, and the established values in state 3.
 * Some mechanisms may allow the access to the per-message operations
 * and the context flags before the context is fully established. The
 * isProtReady method is used to indicate that these services are
 * available.
 */

public interface GSSCtxtSpi {

    /**
     * Sets the mechanism options to be used during context
     * creation on the initiator's side. This is used to
     * initialize a new GSSCtxtSpi object.
     *
     * @param myCred the principal's credentials; may be null
     * @param targName the context peer
     * @param desLifetime the requested lifetime; 0 indicates use
     *    default
     * @param mechOptions ORed GSSContext options
     * @exception GSSException may be thrown
     */
    public void _setInitOptions (GSSCredSpi myCred, GSSNameSpi targName,
            int desLifetime, int ctxtOptions) throws GSSException;
    

    /**
     * Sets the mechanism options to be used during context
     * creation on the acceptor's side. This is used to initialize
     * a new GSSCtxtSpi object.
     *
     * @param myCred the principal's credentials; may be null
     * @exception GSSException may be thrown
     */
    public void _setAcceptOptions (GSSCredSpi myCred) throws GSSException;
    

    /**
     * Sets the channel bindings to be used during context
     * establishment. This method is only called if the application
     * wishes to use channel bindings with this context.
     *
     * @param chb channel bindings to be set
     * @exception GSSException may be thrown
     */
    public void _setChannelBinding (ChannelBinding chb) throws GSSException;

                
    /**
     * Retrieves the mechanism options.
     *
     * @return int GSSContext options ORed together
     */
    public int _getOptions ();
    
    
    /**
     * Inquire the remaining lifetime.
     *
     * @return the lifetime in seconds. May return reserved
     *    value GSSContext.INDEFINITE for an indefinite lifetime.
     */
    public int _getLifetime ();
        

    /**
     * Returns the mechanism oid.
     *
     * @return the Oid for this context
     */           
    public Oid _getMech ();


    /**
     * Returns the context initiator name.
     * 
     * @return initiator name
     * @exception GSSException may be thrown
     */
    public GSSNameSpi _getSrcName () throws GSSException;
    
    
    /**
     * Returns the context acceptor name.
     *
     * @return context acceptor(target) name
     * @exception GSSException may be thrown
     */
    public GSSNameSpi _getTargName () throws GSSException;
    
    
    /**
     * Returns the delegated credential for the context. This
     * is an optional feature of contexts which not all
     * mechanisms will support. A context can be requested to
     * support credential delegation by using the <b>CRED_DELEG</b>.
     * This is only valid on the acceptor side of the context.
     * @return GSSCredSpi object for the delegated credential
     * @exception GSSException may be thrown
     * @see GSSContext#getDelegCredState
     */
    public GSSCredSpi _getDelegCred () throws GSSException;
 
    
    /**
     * Tests if this is the initiator side of the context.
     *
     * @return boolean indicating if this is initiator (true)
     *    or target (false)
     */
    public boolean _isInitiator ();
    

    /**
     * Tests if the context can be used for per-message service.
     * Context may allow the calls to the per-message service
     * functions before being fully established.
     *
     * @return boolean indicating if per-message methods can
     *    be called.
     */
    public boolean _isProtReady ();
    
    
    /**
     * Initiator context establishment call. This method may be
     * required to be called several times. A CONTINUE_NEEDED return
     * call indicates that more calls are needed after the next token
     * is received from the peer.
     *
     * @param is contains the token received from the peer. On the
     *    first call it will be ignored.
     * @param os to which any tokens required to be sent to the peer
     *    will be written. It is responsibility of the caller
     *    to send the token to its peer for processing.
     * @return integer indicating if more calls are needed. Possible
     *    values are COMPLETE and CONTINUE_NEEDED.
     * @exception GSSException may be thrown
     */
    public int _initSecCtxt (InputStream is, OutputStream os)
			throws GSSException;


    /**
     * Acceptor's context establishment call. This method may be
     * required to be called several times. A CONTINUE_NEEDED return
     * call indicates that more calls are needed after the next token
     * is received from the peer.
     *
     * @param is contains the token received from the peer.
     * @param os to which any tokens required to be sent to the peer
     *    will be written. It is responsibility of the caller
     *    to send the token to its peer for processing.
     * @return integer indicating if more calls are needed. Possible
     *    values are COMPLETE and CONTINUE_NEEDED.
     * @exception GSSException may be thrown
     */
    public int _acceptSecCtxt (InputStream is, OutputStream os)
			throws GSSException;


    /**
     * Queries the context for largest data size to accomodate
     * the specified protection and for the token to remain less then
     * maxTokSize.
     *
     * @param qop the quality of protection that the context will be
     *    asked to provide. 
     * @param confReq a flag indicating whether confidentiality will be
     *    requested or not
     * @param outputSize the maximum size of the output token
     * @return the maximum size for the input message that can be
     *    provided to the wrap() method in order to guarantee that these
     *    requirements are met.
     * @exception GSSException may be thrown
     */
    public int _getWrapSizeLimit (int qop, boolean confReq, int maxTokSize)
			throws GSSException;


    /**
     * Provides per-message token encapsulation.
     *
     * @param is the user-provided message to be protected
     * @param os the token to be sent to the peer. It includes
     *    the message from <i>is</i> with the requested protection.
     * @param msgPro on input it contains the requested qop and
     *    confidentiality state, on output, the applied values
     * @exception GSSException may be thrown
     * @see MessageInfo
     * @see unwrap
     */
    public void _wrap (InputStream is, OutputStream os, MessageProp msgProp)
			throws GSSException;


    /**
     * Retrieves the message token previously encapsulated in the wrap
     * call.
     *
     * @param is the token from the peer
     * @param os unprotected message data
     * @param msgProp will contain the applied qop and confidentiality
     *    of the input token and any informatory status values
     * @exception GSSException may be thrown
     * @see MessageInfo
     * @see wrap
     */
    public void _unwrap (InputStream is, OutputStream os,
			MessageProp msgProp) throws GSSException;


    /**
     * Applies per-message integrity services.
     *
     * @param is the user-provided message
     * @param os the token to be sent to the peer along with the
     *    message token. The message token <b>is not</b> encapsulated.
     * @param msgProp on input the desired QOP and output the applied QOP
     * @exception GSSException
     */
    public void _getMIC (InputStream is, OutputStream os,
			MessageProp msgProp)
                throws GSSException;


    /**
     * Checks the integrity of the supplied tokens.
     * This token was previously generated by getMIC.
     *
     * @param is token generated by getMIC
     * @param msgStr the message to check integrity for
     * @param msgProp will contain the applied QOP and confidentiality
     *    states of the token as well as any informatory status codes
     * @exception GSSException may be thrown
     */
    public void _verifyMIC (InputStream is, InputStream msgStr,
			MessageProp mProp) throws GSSException;


    /**
     * Produces a token representing this context. After this call
     * the context will no longer be usable until an import is
     * performed on the returned token.
     *
     * @return exported context token
     * @exception GSSException may be thrown
     */
    public byte []_export () throws GSSException;

    
    /**
     * Imports a previously exported context. This will be called
     * for newly created objects.
     *
     * @param is the previously exported token
     * @exception GSSException may be thrown
     * @see export
     */
    public void _importSecCtxt (byte []token) throws GSSException;

    
    /** 
     * Releases context resources and terminates the
     * context between 2 peer.
     *
     * @exception GSSException may be thrown
     */
    public void _dispose () throws GSSException;
}


