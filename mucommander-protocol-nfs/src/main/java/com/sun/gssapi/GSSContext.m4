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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * This class represents the JGSS security context and its associated
 * operations.  JGSS security contexts are established between
 * peers using locally established credentials.  Multiple contexts
 * may exist simultaneously between a pair of peers, using the same
 * or different set of credentials.  The JGSS is independent of
 * the underlying transport protocols and depends on its callers to
 * transport the tokens between peers.
 * <p>
 * The context object can be thought of as having 3 implicit states:
 * before it is established, during its context establishment, and
 * after a fully established context exists.
 * <p>
 * Before the context establishment phase is initiated, the context
 * initiator may request specific characteristics desired of the
 * established context. These can be set using the set methods. After the
 * context is established, the caller can check the actual characteristic
 * and services offered by the context using the query methods.
 * <p>
 * The context establishment phase begins with the first call to the init
 * method by the context initiator. During this phase the init and accept
 * methods will produce GSS-API authentication tokens which the calling
 * application needs to send to its peer. The init and accept methods may
 * return a CONTINUE_NEEDED code which indicates that a token is needed
 * from its peer in order to continue the context establishment phase. A
 * return code of COMPLETE signals that the local end of the context is
 * established. This may still require that a token be sent to the peer,
 * depending if one is produced by GSS-API. The isEstablished method can
 * also be used to determine if the local end of the context has been
 * fully established. During the context establishment phase, the
 * isProtReady method may be called to determine if the context can be
 * used for the per-message operations. This allows implementation to
 * use per-message operations on contexts which aren't fully established.
 * <p>
 * After the context has been established or the isProtReady method
 * returns "true", the query routines can be invoked to determine the actual
 * characteristics and services of the established context. The
 * application can also start using the per-message methods of wrap and
 * getMIC to obtain cryptographic operations on application supplied data.
 * <p>
 * When the context is no longer needed, the application should call
 * dispose to release any system resources the context may be using.
 * <DL><DT><B>RFC 2078</b>
 *    <DD>This class corresponds to the context level calls together with
 * the per message calls of RFC 2078. The gss_init_sec_context and
 * gss_accept_sec_context calls have been made simpler by only taking
 * required parameters.  The context can have its properties set before
 * the first call to init. The supplementary status codes for the per-message
 * operations are returned in an instance of the MessageProp class, which is
 * used as an argument in these calls.</dl>
 */
public class GSSContext {

    /**
     * Indefinite lifetime value for a context. Set to the
     * largest value for an int in Java.
     * @see #getLifetime
     */
    public static final int INDEFINITE = Integer.MAX_VALUE;

    /**
     * Return value from either accept or init stating that
     * the context creation phase is complete for this peer.
     * @see #init
     * @see #accept
     */
    public static final int COMPLETE = 0;
    
    /**
     * Return value from either accept or init stating that
     * another token is required from the peer to continue context
     * creation. This may be returned several times indicating
     * multiple token exchanges.
     * @see #init
     * @see #accept
     */
    public static final int CONTINUE_NEEDED = 1;

    /**
     * Context option flag - credential delegation.
     */
    public static final int CRED_DELEG = 0x1;
        
    /**
     * Context option flag - mutual authentication.
     */
    public static final int MUTUAL_AUTH = 0x02;
        
    /**
     * Context option flag - replay detection.
     */
    public static final int REPLAY_DET = 0x04;
    
    /**
     * Context option flag - sequence detection.
     */
    public static final int SEQUENCE_DET = 0x08;
    
    /**
     * Context option flag - anonymity.
     */
    public static final int ANON = 0x10;
    
    /**
     * Context option flag - confidentiality.
     */
    public static final int CONF = 0x20;
    
    /**
     * Context option flag -  integrity.
     */
    public static final int INTG = 0x40;
    
    /**
     * Context option flag - transferability (output flag only).
     * Indicates if context may be transferred by using export().
     * @see #export
     */
    public static final int TRANS = 0x80;
    

    /**
     * Constructor for creating a context on the initiator's side.
     * Context flags may be modified through the set methods prior
     * to calling init().
     *
     * @param peer Name of the target peer.
     * @param mechOid Oid of the desired mechanism;
     *    may be null to indicate the default mechanism
     * @param myCred the credentials for the initiator; may be
     *    null to indicate desire to use the default credentials
     * @param lifetime the request lifetime, in seconds, for the context
     * @exception GSSException with possible major codes of BAD_NAME, 
     *    BAD_MECH, BAD_NAMETYPE.
     * @see #init
     */
    public GSSContext(GSSName peer, Oid mechOid, GSSCredential myCred, int
            lifetime) throws GSSException {
        
        initialize();
        
        m_myCred = myCred;
        m_reqLifetime = lifetime;
        m_targName = peer;
        
        if (mechOid == null)
            m_mechOid = GSSManager.getDefaultMech();
        else
            m_mechOid = mechOid;
    } 


    /**
     * Constructor for creating a context on the acceptor' side. The
     * context's properties will be determined from the input token
     * supplied to accept().
     *
     * @param myCred GSSCredential for the acceptor. Use null to
     *    request usage of default credentials.
     * @exception GSSException with possible major codes of BAD_NAME, 
     *    BAD_MECH, BAD_NAMETYPE.
     * @see #accept
     */
    public GSSContext(GSSCredential myCred) throws GSSException {

        initialize();
        m_myCred = myCred;
    }


    /**
     * Constructor for creating a GSSContext from a previously
     * exported context. The context properties will be determined
     * from the input token.
     * <p><DL><DT><B>RFC 2078</b>
     *    <DD>equivalent to gss_import_sec_context</DL>
     * @param interProcessToken the token emitted from export routine
     * @exception GSSException with possible major codes of CONTEXT_EXPIRED,
     *    NO_CONTEXT, DEFECTIVE_TOKEN, UNAVAILABLE, UNAUTHORIZED, FAILURE
     * @see #export
     */
    public GSSContext(byte [] interProcessToken) throws GSSException {
    
        initialize();
        
        /*
         * Strip the mechanism oid from the token
         *
         * the token encoding is implementation specific, and
         * we start by having 4 bytes of length, followed by the
         * mechanism oid of the specified length, followed by the
         * mechanism part of the token. This is the same as our C
         * version.
         */
        try {
            int length = (interProcessToken[0] & 0xff);
            for (int i = 1; i < 4; i++) {
                length <<= 8;
                length += (interProcessToken[i] & 0xff);
            }
            
            ByteArrayInputStream is = 
                new ByteArrayInputStream(interProcessToken);

            //ask mechanism to import this context
            m_mechCtxt = GSSManager.getCtxtInstance (
                    Oid.getFromDEROctets(is, length));
            m_mechCtxt._importSecCtxt (interProcessToken);
            m_curState = READY;
            
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new GSSException(GSSException.DEFECTIVE_TOKEN);
        }
    }


    /**
     * Called by the context initiator to start the context creation
     * process. This is equivalent to the stream based method except
     * that the token buffers are handled as byte arrays instead of
     * using stream objects. This method may return an output token
     * which the application will need to send to the peer for
     * processing by the accept call. "null" return value indicates
     * that no token needs to be sent to the peer. The application
     * can call isEstablished to determine if the context
     * establishment phase is complete for this peer. A return value
     * of "false" from isEstablished indicates that more tokens are
     * expected to be supplied to the init method. Please note that
     * the init method may return a token for the peer, and
     * isEstablished return "true" also. This indicates that the token
     * needs to be sent to the peer, but the local end of the context
     * is now fully established.
     * <p>
     * Upon completion of the context establishment, the available
     * context options may be queried through the get methods.
     * <p><DL><DT><B>RFC 2078</b>
     *    <DD>equivalent to gss_init_sec_context; The requested context
     *    options can be set before the first call, and the available
     *    options can be obtained after the context is fully established.
     *    </dl>
     * @param inputBuf token generated by the peer; this parameter is
     *    ignored on the first call
     * @param outputBuf token generated for the peer; this may be empty
     * @return establishment state of either COMPLETE or CONTINUE_NEEDED
     * @exception GSSException with possible major values of DEFECTIVE_TOKEN,
     *    DEFECTIVE_CREDENTIAL, BAD_SIG, NO_CRED, CREDENTIALS_EXPIRED,
     *    BAD_BINDINGS, OLD_TOKEN, DUPLICATE_ELEMENT, BAD_NAMETYPE, BAD_NAME,
     *    BAD_MECH, and FAILURE
     * @see #init(InputStream,OutputStream)
     * @see #setChannelBinding
     */
    public byte[] init(byte []inputBuf, int offset, int length)
                throws GSSException {
        
        ByteArrayInputStream is = null;
        
        if (inputBuf != null)
            is = new ByteArrayInputStream(inputBuf, offset, length);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        init(is, os);
        if (os.size() == 0)
            return (null);
            
        return (os.toByteArray());
    }


    /**
     * Called by the context initiator to start the context creation
     * process. This is equivalent to the byte array based method.
     * This method may write an output token to the outputBuf, which
     * the application will need to send to the peer for processing
     * by the accept call. 0 bytes written to the output stream
     * indicate that no token needs to be sent to the peer. The
     * method will return either COMPLETE or CONTINUE_NEEDED
     * indicating the status of the current context. A return value
     * of COMPLETE indicates that the context establishment phase is
     * complete for this peer, while CONTINUE_NEEDED means that
     * another token is expected from the peer. The isEstablished
     * method can also be used to determine this state. Note that
     * it is possible to have a token for the peer while this method
     * returns COMPLETE. This indicates that the local end of the
     * context is established, but the token needs to be sent to
     * the peer to complete the context establishment.
     * <p>
     * The GSS-API authentication tokens contain a definitive
     * start and end. This method will attempt to read one of these
     * tokens per invocation, and may block on the stream if only
     * part of the token is available.
     * <p>
     * Upon completion of the context establishment, the available
     * context options may be queried through the get methods.
     * <p><DL><DT><B>RFC 2078</b>
     *    <DD>equivalent to gss_init_sec_context; The requested context
     *    options can be set before the first call, and the available
     *    options can be obtained after the context is fully established.
     * </dl>
     * @param inputBuf token generated by the peer; this parameter is
     *    ignored on the first call
     * @param outputBuf token generated for the peer; this may be empty
     * @return establishment state of either COMPLETE or CONTINUE_NEEDED
     * @exception GSSException with possible major values of DEFECTIVE_TOKEN,
     *    DEFECTIVE_CREDENTIAL, BAD_SIG, NO_CRED, CREDENTIALS_EXPIRED,
     *    BAD_BINDINGS, OLD_TOKEN, DUPLICATE_ELEMENT, BAD_NAMETYPE, BAD_NAME,
     *    BAD_MECH, and FAILURE
     * @see #init(byte[],int,int)
     * @see #accept(byte[],int,int)
     * @see #setChannelBinding
     */
    public int init(InputStream inputBuf, OutputStream outputBuf)
                throws GSSException {
        
        if (m_mechCtxt == null) {
            m_mechCtxt = GSSManager.getCtxtInstance (m_mechOid);
            GSSCredSpi mechCred = null;
            if (m_myCred != null)
                mechCred = m_myCred.getMechCred(m_mechOid, true);
                
            m_mechCtxt._setInitOptions (mechCred,
                    m_targName.canonicalizeInPlace(m_mechOid),
                    m_reqLifetime, m_reqFlags);
                    
            if (m_chB != null)
                m_mechCtxt._setChannelBinding (m_chB);
    
            m_curState = IN_PROGRESS;
        }
        
        //debug code
        if (m_curState != IN_PROGRESS) {
            throw new GSSException(GSSException.FAILURE, -1,
                    "wrong status in init");
        }
        
        int retStatus = m_mechCtxt._initSecCtxt (inputBuf, outputBuf);
        if (retStatus == COMPLETE)
            m_curState = READY;
            
        return (retStatus);
    }


    /**
     * Called by the context acceptor upon receiving a token from
     * the peer. This call is equivalent to the stream based method
     * except that the token buffers are handled as byte arrays
     * instead of using stream objects.
     * <p>
     * This method may return an output token which the application
     * will need to send to the peer for further processing by the
     * init call. "null" return value indicates that no token needs
     * to be sent to the peer. The application can call isEstablished
     * to determine if the context establishment phase is complete
     * for this peer. A return value of "false" from isEstablished
     * indicates that more tokens are expected to be supplied to this
     * method.
     * <p>
     * Please note that the accept method may return a token for the
     * peer, and isEstablished return "true" also. This indicates that
     * the token needs to be sent to the peer, but the local end of the
     * context is now fully established.
     * <p>
     * Upon completion of the context establishment, the available
     * context options may be queried through the get methods.
     * Called by the context acceptor upon receiving a token from
     * the peer. May need to be called again if returns CONTINUE_NEEDED.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *    <DD>equivalent to gss_accept_sec_context; context options can
     *    obtained through the query methods
     * </dl>
     * @param inputToken token that was received from the initiator
     * @param outputBut token generated for the peer; may be empty
     * @return creation state of either COMPLETE or CONTINUE_NEEDED
     * @exception GSSException may be thrown with major status values of
     *    DEFECTIVE_TOKEN, DEFECTIVE_CREDENTIAL, BAD_SIG, NO_CRED,
     *    CREDENTIALS_EXPIRED, BAD_BINDINGS, OLD_TOKEN, DUPLICATE_ELEMENT,
     *    BAD_MECH, and FAILURE
     * @see #init(byte[],int,int)
     * @see #accept(InputStream,OutputStream)
     * @see #setChannelBinding
     */
    public byte[] accept(byte[] inTok, int offset, int length)
                throws GSSException {
    
        ByteArrayInputStream is = new ByteArrayInputStream(inTok,
						offset, length);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        accept(is, os);

        //check if token is available for the peer
        if (os.size() == 0)
            return (null);
            
        return (os.toByteArray());
    }


    /**
     * Called by the context acceptor upon receiving a token from
     * the peer. This call is equivalent to the byte array method.
     * It may write an output token to the outputBuf, which the
     * application will need to send to the peer for processing
     * by its init method. 0 bytes written to the output stream
     * indicate that no token needs to be sent to the peer. The
     * method will return either COMPLETE or CONTINUE_NEEDED
     * indicating the status of the current context. A return
     * value of COMPLETE indicates that the context establishment
     * phase is complete for this peer, while CONTINUE_NEEDED means
     * that another token is expected from the peer. The isEstablished
     * method can also be used to determine this state. Note that it
     * is possible to have a token for the peer while this method
     * returns COMPLETE. This indicates that the local end of the
     * context is established, but the token needs to be sent to
     * the peer to complete the context establishment.
     * <p>
     * The GSS-API authentication tokens contain a definitive start
     * and end. This method will attempt to read one of these tokens
     * per invocation, and may block on the stream if only part of the
     * token is available.
     * <p>
     * Upon completion of the context establishment, the available
     * context options may be queried through the get methods.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *    <DD>equivalent to gss_accept_sec_context; context options can
     *    obtained through the query methods</dl>
     * @param inputToken token that was received from the initiator
     * @param outputBut token generated for the peer; may be empty
     * @return creation state of either COMPLETE or CONTINUE_NEEDED
     * @exception GSSException may be thrown with major status values of
     *    DEFECTIVE_TOKEN, DEFECTIVE_CREDENTIAL, BAD_SIG, NO_CRED,
     *    CREDENTIALS_EXPIRED, BAD_BINDINGS, OLD_TOKEN, DUPLICATE_ELEMENT,
     *    BAD_MECH, and FAILURE
     * @see #accept(byte[],int,int)
     * @see #init(InputStream,OutputStream)
     * @see #setChannelBinding
     */
    public int accept(InputStream inputBuf, OutputStream outputBuf)
                throws GSSException {
    
        if (m_mechCtxt == null) {
        
            //get the mechanism oid from the inputBuf
            Oid mechOid = getTokenMech(inputBuf);
            m_mechCtxt = GSSManager.getCtxtInstance (mechOid);
            if (m_myCred != null)
                m_mechCtxt._setAcceptOptions (
                    m_myCred.getMechCred (mechOid, true));
            if (m_chB != null)
                m_mechCtxt._setChannelBinding (m_chB);
                
            m_curState = IN_PROGRESS;
        }
        
        //debug code
        if (m_curState != IN_PROGRESS) {
            throw new GSSException(GSSException.FAILURE, -1,
                    "wrong status in accept");
        }
        
        int retStatus = m_mechCtxt._acceptSecCtxt (inputBuf, outputBuf);
        if (retStatus == COMPLETE)
            m_curState = READY;
            
        return (retStatus);
    }


    /**
     * Returns true is this is a fully established context. Used after
     * the init and accept methods to check if more tokens are needed
     * from the peer.
     *
     * @return boolean indicating if this side of the context is
     *    fully established.
     */
    public boolean isEstablished() {
    
        return (m_curState == READY);
    }
    

    /** 
     * Release any system resources and cryptographic information
     * stored in the context object.  This will invalidate the
     * context.
     * <p><DL><DT><B>RFC 2078</b>
     *    <DD>equivalent to gss_delete_sec_context</dl>
     * @exception GSSException with major codes NO_CONTEXT or FAILURE
     */
    public void dispose() throws GSSException {
    
        checkState(IN_PROGRESS);
        m_mechCtxt._dispose ();
        m_curState = DELETED;
    }
   
             
    /**
     * Returns the maximum message size that, if presented to the
     * wrap method with the same confReq and qop parameters will
     * result in an output token containing no more then maxTokenSize
     * bytes.
     * <p><DL><DT><B>RFC 2078</b>
     *      <DD>equivalent to gss_wrap_size_limit</dl>
     * @param qop quality of protection to apply to the message
     * @param confReq boolean indicating if privacy should be applied
     * @param maxTokenSize the maximum size of the token to be emitted
     *    from wrap
     * @return maximum input buffer size for encapsulation by wrap
     *    using the specified QOP and confReq without exceeding
     *    the maxTokenSize
     * @exception GSSException with the possible major codes of BAD_QOP,
     *    CONTEXT_EXPIRED, and FAILURE.
     * @see #wrap
     */
    public int getWrapSizeLimit(int qop, boolean confReq, int maxTokenSize)
                    throws GSSException {
        
        checkState(READY);
        return (m_mechCtxt._getWrapSizeLimit (qop, confReq, maxTokenSize));
    }


    /**
     * Allows to apply per-message security services over the
     * established security context. The method will return a
     * token with a cryptographic MIC and may optionally encrypt
     * the specified inBuf.  This method is equivalent i
     * functionality to its stream counterpart. The returned
     * byte array will contain both the MIC and the message.
     * The msgProp object is used to specify a QOP value which
     * selects cryptographic algorithms, and a privacy service,
     * if supported by the chosen mechanism.
     * <p>
     * Supports the wrapping and unwrapping of zero-length messages.
     * <p>
     * The application will be responsible for sending the token
     * to the peer.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *    <DD>equivalent to gss_wrap; MessageProp object is used to
     *    select QOP and confidentiality</dl>
     * @param inBuf the application data to be protected
     * @param offset the offset in the inBuf where the data begins
     * @param length the length of the data starting at offset
      * @param msgPro indicates the desired QOP and confidentiality state,
     *    and upon return the actual QOP and message confidentiality state
     * @return buffer to be sent to the peer for processing by unwrap
     * @exception GSSException with possible major codes of CONTEXT_EXPIRED,
     *   CREDENTIALS_EXPIRED, BAD_QOP, FAILURE.
     * @see #wrap(InputStream,OutputStream, MessageProp)
     * @see #unwrap(byte[],int,int,MessageProp)
     * @see MessageProp
     */
    public byte[] wrap(byte[] inBuf, int offset, int length,
                MessageProp msgProp) throws GSSException {

/*
 * XXX EXPORT DELETE START
 *
 * Turn off the privacy.  Once an export control liscence
 * is issued, this routine can be turned back on.
 *
        ByteArrayInputStream is = new ByteArrayInputStream(inBuf,
                            offset, length);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wrap(is, os, msgProp);

        //return output token
        return (os.toByteArray());
 * XXX EXPORT DELETE END
 */

// XXX delete this line once this routine is turned back on.
	throw new GSSException(GSSException.FAILURE);
    }


    /**
     * Allows to apply per-message security services over the
     * established security context. The method will produce
     * a token with a cryptographic MIC and may optionally
     * encrypt the specified inBuf.  The outBuf will contain
     * both the MIC and the message.  The msgProp object is
     * used to specify a QOP value to select cryptographic
     * algorithms, and a privacy service, if supported by the
     * chosen mechanism.
     * <p>
     * Supports the wrapping and unwrapping of zero-length messages.
     * <p>
     * The application will be responsible for sending the token
     * to the peer.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *    <DD>equivalent to gss_wrap; MessageProp object is used to
     *    select QOP and confidentiality</dl>
     * @param inputBuf the application data to be protected
     * @param outputBuf the token to be sent to the peer
     * @param msgPro indicates the desired QOP and confidentiality state,
     *    and upon return the actual QOP and message confidentiality state
     * @exception GSSException with possible major codes of CONTEXT_EXPIRED,
     *   CREDENTIALS_EXPIRED, BAD_QOP, FAILURE.
     * @see #wrap(byte,int,int,MessageProp)
     * @see #unwrap(InputStream,OutputStream,MessageProp)
     * @see MessageProp
     */
    public void wrap(InputStream inBuf, OutputStream outBuf,
            MessageProp msgProp) throws GSSException {
        
/*
 * XXX EXPORT DELETE START
 *
 * Turn off the privacy.  Once an export control liscence
 * is issued, this routine can be turned back on.
 *
        //clear status values
        msgProp.resetStatusValues();

        checkState(READY);
        m_mechCtxt._wrap (inBuf, outBuf, msgProp);
 * XXX EXPORT DELETE END
 */

// XXX delete this line once this routine is turned back on.
	throw new GSSException(GSSException.FAILURE);
    }


    /**
     * Used by the peer application to process tokens generated
     * with the wrap call.  This call is equal in functionality
     * to its stream counterpart. The method will return the
     * message supplied in the peer application to the wrap call,
     * verifying the embedded MIC.  The msgProp instance will
     * indicate whether the message was encrypted and will contain
     * the QOP indicating the strength of protection that was used
     * to provide the confidentiality and integrity services.
     * <p>
     * Supports the wrapping and unwrapping of zero-length messages.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *      <DD>equivalent to the gss_unwrap</dl>
     *
     * @param inBuf token received from peer application which was
     *    generated by call to wrap
     * @param offset within the inBuf where the token begins.
     * @param length The length of the token in inBuf.
     * @param msgProp Upon return from the this method, will contain
     *      QOP and privacy state of the supplied message as well as 
     *    any supplementary status values.
     * @return the application message used in the wrap call
     * @exception GSSException with possible major codes of DEFECTIVE_TOKEN,
     *    BAD_SIG, CONTEXT_EXPIRED, CREDENTIALS_EXPIRED, and FAILURE.
     * @see #unwrap(InputStream,OutputStream,MessageProp)
     * @see #wrap(byte[],int,int,MessageProp)
     * @see MessageProp
     */
    public byte[] unwrap(byte[] inBuf, int offset, int length,
               MessageProp msgProp) throws GSSException {

/*
 * XXX EXPORT DELETE START
 *
 * Turn off the privacy.  Once an export control liscence
 * is issued, this routine can be turned back on.
 *
        ByteArrayInputStream is = new ByteArrayInputStream(inBuf,
                        offset, length);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        unwrap(is, os, msgProp);

        //return output token
        if (os.size() > 0)
            return (os.toByteArray());

        return (null);
 * XXX EXPORT DELETE END
 */

// XXX delete this line once this routine is turned back on.
	throw new GSSException(GSSException.FAILURE);
    }


    /**
     * Used by the peer application to process tokens generated with
     * the wrap call.  This call is equal in functionality to its byte
     * array counterpart. It will produce the message supplied in the
     * peer application to the wrap call, verifying the embedded MIC.
     * The msgProp parameter will indicate whether the message was
     * encrypted and will contain the QOP indicating the strength
     * of protection that was used to provide the confidentiality
     * and integrity services. The msgProp object will also contain
     * the supplementary status information for the token.
     * <p>
     * Supports the wrapping and unwrapping of zero-length messages.
     * 
     * <p><DL><DT><B>RFC 2078</b>
     *      <DD>equivalent to the gss_unwrap</dl>
     *
     * @param inBuf token received from peer application which was
     *    generated by call to wrap
     * @param outBuf original message passed into wrap
     * @param msgProp Upon return from the this method, will contain
     *      QOP and privacy state of the supplied message as well as 
     *    any supplementary status values.
     * @exception GSSException with possible major codes of DEFECTIVE_TOKEN,
     *    BAD_SIG, CONTEXT_EXPIRED, CREDENTIALS_EXPIRED, and FAILURE.
     * @see #unwrap(byte[],int,int,MessageProp)
     * @see #wrap(InputStream,OutputStream,MessageProp)
     * @see MessageProp
     */
    public void unwrap(InputStream inBuf, OutputStream outBuf,
            MessageProp msgProp) throws GSSException {
        
/*
 * XXX EXPORT DELETE START
 *
 * Turn off the privacy.  Once an export control liscence
 * is issued, this routine can be turned back on.
 *
        //clear status values
        msgProp.resetStatusValues();

        checkState(READY);
        m_mechCtxt._unwrap (inBuf, outBuf, msgProp);
 * XXX EXPORT DELETE END
 */

// XXX delete this line once this routine is turned back on.
	throw new GSSException(GSSException.FAILURE);
    }


    /**
     * Returns a token containing a cryptographic MIC for the
     * supplied message,  for transfer to the peer application.
     * Unlike wrap, which encapsulates the user message in the
     * returned token, only the message MIC is returned in the
     * output token. This method is identical in functionality
     * to its stream counterpart.
     * <p>
     * Note that privacy can only be applied through the wrap call.
     * <p>
     * Supports the derivation of MICs from zero-length messages.
     *      
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to gss_getMIC</dl>
     * @param inBuf message to apply security service to
     * @param offset The offset within the inMsg where the
     *    token begins.
     * @param length the length of the application message
     * @param msgProp Indicates the desired QOP to be used. Use QOP of 0
     *     to indicate default value.  The confidentiality flag
     *    is ignored. Upon return from this method, this object
     *    will contain the actual QOP applied (in case 0 was selected).
     * @return token containing cryptographic information for the
     *    requested security service over the passed in message
     * @exception GSSException with possible major codes of CONTEXT_EXPIRED,
     *    BAD_QOP, FAILURE.
     * @see #getMIC(InputStream,OutputStream,MessageProp)
     * @see #verifyMIC(byte[],int,int,MessageProp)
     * @see MessageProp
     */
    public byte[] getMIC(byte[] inMsg, int offset, int length,
                   MessageProp msgProp) throws GSSException {

        ByteArrayInputStream is = new ByteArrayInputStream(inMsg,
                        offset, length);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        getMIC(is, os, msgProp);

        //return output token
        return (os.toByteArray());
     }


    /**
     * Produces a token containing a cryptographic MIC for the
     * supplied message, for transfer to the peer application.
     * Unlike wrap, which encapsulates the user message in the
     * returned token, only the message MIC is produced in the
     * output token. This method is identical in functionality
     * to its byte array counterpart.
     * <p>
     * Note that privacy can only be applied through the wrap call.
     * <p>
     * Supports the derivation of MICs from zero-length messages.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to gss_getMIC</dl>
     * @param inBuf Buffer containing the message to generate MIC over.
     * @param outBuf The buffer to write the GSS-API output token into.
     * @param msgProp Indicates the desired QOP to be used. Use QOP of 0
     *     to indicate default value.  The confidentiality flag
     *    is ignored. Upon return from this method, this object
     *    will contain the actual QOP applied (in case 0 was selected).
     * @exception GSSException with possible major codes of CONTEXT_EXPIRED,
     *    BAD_QOP, FAILURE.
     * @see #getMIC(byte[],int,int,MessageProp)
     * @see #verifyMIC(byte[],int,int,byte[],int,int,MessageProp)
     * @see MessageProp
     */
    public void getMIC(InputStream inBuf, OutputStream outBuf,
            MessageProp msgProp) throws GSSException {
        
        //clear status values
        msgProp.resetStatusValues();

        checkState(READY);
        m_mechCtxt._getMIC (inBuf, outBuf, msgProp);
    }


    /**
     * Verifies the cryptographic MIC, contained in the token
     * parameter, over the supplied message.  The msgProp parameter
     * will contain the QOP indicating the strength of protection
     * that was applied to the message and any supplementary status
     * values for the token. This method is equivalent in
     * functionality to its stream counterpart.
     *
     * <p><DL><DT><B>RFC 2078</b> 
     *    <DD>equivalent to gss_verifyMIC</dl>
     * @param inTok token generated by peer's getMIC method
     * @param tokOffset the offset within the inTok where the token begins
     * @param tokLen the length of the token
     * @param inMsg Application message to verify the Cryptographic MIC
     *     over.
     * @param msgOffset the offset within the inMsg where the message
     *    begins
     * @param msgLen the length of the message
     * @param msgProp upon return from this method, this object
     *    will contain the applied QOP and supplementary status
     *    values for the supplied token.  The privacy state will
     *    always be set to false.
     * @exception GSSException with possible major codes DEFECTIVE_TOKEN,
     *    BAD_SIG, CONTEXT_EXPIRED
     * @see #verifyMIC(InputStream,InputStream,MessageProp)
     * @see #wrap(byte[],int,int,MessageProp)
     * @see MessageProp
     */
    public void verifyMIC(byte []inTok, int tokOffset, int tokLen,
                  byte[] inMsg, int msgOffset, int msgLen,
                  MessageProp msgProp) throws GSSException {

        ByteArrayInputStream sTok = new ByteArrayInputStream(inTok,
                        tokOffset, tokLen);
        ByteArrayInputStream sMsg = new ByteArrayInputStream(inMsg,
                        msgOffset, msgLen);
        verifyMIC(sTok, sMsg, msgProp);
    }
 
    
    /**
     * Verifies the cryptographic MIC, contained in the token
     * parameter, over the supplied message.  The msgProp parameter
     * will contain the QOP indicating the strength of protection
     * that was applied to the message. This method is equivalent
     * in functionality to its byte array counterpart.
     *
     * <p><DL><DT><B>RFC 2078</b> 
     *    <DD>equivalent to gss_verifyMIC</dl>
     * @param inputTok Contains the token generated by peer's getMIC
     *    method.
     * @param inputMsg Contains application message to verify the
     *    cryptographic MIC over.
     * @param msgProp upon return from this method, this object
     *    will contain the applied QOP and supplementary statustatus
     *    values for the supplied token.  The privacy state will
     *    always be set to false.
     * @exception GSSException with possible major codes DEFECTIVE_TOKEN,
     *    BAD_SIG, CONTEXT_EXPIRED
     * @see #verifyMIC(byte[],int,int,byte[],int,int,MessageProp)
     * @see #wrap(InputStream,OutputStream)
     * @see MessageProp
     */
    public void verifyMIC(InputStream inTok, InputStream inMsg,
        MessageProp msgProp) throws GSSException {
        
        //clear status values
        msgProp.resetStatusValues();

        checkState(READY);
        m_mechCtxt._verifyMIC (inTok, inMsg, msgProp);
    }


    /**
     * Provided to support the sharing of work between multiple
     * processes. This routine will typically be used by the
     * context-acceptor, in an application where a single process
     * receives incoming connection requests and accepts security
     * contexts over them, then passes the established context to
     * one or more other processes for message exchange.
     * <p>
     * This method deactivates the security context and creates an
     * interprocess token which, when passed to the byte array
     * constructor of the GSSContext class in another process,
     * will re-activate the context in the second process.
     * <p>
     * Only a single instantiation of a given context may be active
     * at any one time; a subsequent attempt by a context exporter
     * to access the exported security context will fail.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *      <DD>equivalent to gss_export_sec_context</dl>
     * @return inter-process token representing the context
     *    in export form
     * @exception GSSException with possible major codes of UNAVAILABLE,
     *    CONTEXT_EXPIRED, NO_CONTEXT, FAILURE.
     * @see GSSContext#GSSContext(byte[])
     * @see #isTransferable
     */
    public byte [] export() throws GSSException {
    
        checkState(READY);
        byte [] outTok = m_mechCtxt._export ();
        dispose();

        return (outTok);
    }


    /**
     * Sets the request state of the mutual authentication flag
     * for the context.  This method is only valid before the
     * context creation process begins and only for the initiator.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *      <DD>equivalent to the mutual_req_flag parameter in
     *    gss_init_sec_context</dl>
     * @param Boolean representing if mutual authentication
     *    should be requested during context establishment.
     * @exception GSSException may be thrown
     * @see #getMutualAuthState
     */
    public void requestMutualAuth(boolean state) throws GSSException {

        checkState(PRE_INIT);
        
        if (state)
            m_reqFlags |= MUTUAL_AUTH;
        else
            m_reqFlags ^= MUTUAL_AUTH;
    }

        
    /**
     * Sets the request state of the replay detection service
     * for the context.  This method is only valid before the
     * context creation process begins and only for the initiator.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the replay_det_req_flag parameter in
     *    gss_init_sec_context</dl>
     * @param Boolean representing if replay detection is desired
     *    over the established context.
     * @exception GSSException may be thrown
     * @see #getReplayDetState
     */
    public void requestReplayDet(boolean state) throws GSSException {

        checkState(PRE_INIT);
        
        if (state)
            m_reqFlags |= REPLAY_DET;
        else
            m_reqFlags ^= REPLAY_DET;
    }
    

    /**
     * Sets the request state of the sequence checking service
     * for the context.  This method is only valid before the
     * context creation process begins and only for the initiator.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the sequence_req_flag parameter in
     *    gss_init_sec_context</dl>
     * @param Boolean representing if sequence checking service
     *    is desired over the established context.
     * @exception GSSException may be thrown
     * @see #getSequenceDetState
     */
    public void requestSequenceDet(boolean state) throws GSSException {
    
        checkState(PRE_INIT);
        
        if (state)
            m_reqFlags |= SEQUENCE_DET;
        else
            m_reqFlags ^= SEQUENCE_DET;
    }
     
        
    /**
     * Sets the request state of the credential delegation flag
     * for the context.  This method is only valid before the
     * context creation process begins and only for the initiator.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the deleg_req_flag parameter in
     *    gss_init_sec_context</dl>
     * @param Boolean representing if credential delegation is desired.
     * @exception GSSException may be thrown
     * @see #getDelegCredState
     */
    public void requestCredDeleg(boolean state) throws GSSException {
    
        checkState(PRE_INIT);
        
        if (state)
            m_reqFlags |= CRED_DELEG;
        else
            m_reqFlags ^= CRED_DELEG;

    }
            
                
    /**
     * Requests anonymous support over the context. This method is
     * only valid before the context creation process begins and
     * only for the initiator.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to anon_req_flag parameter in
     *    gss_init_sec_context</dl>
     * @param Boolean representing if anonymity support is desired.
     * @exception GSSException may be thrown
     * @see #getAnonymityState
     */
    public void requestAnonymity(boolean state) throws GSSException {

        checkState(PRE_INIT);
        
        if (state)
            m_reqFlags |= ANON;
        else
            m_reqFlags ^= ANON;
    }


    /**
     * Requests that confidentiality service be available over
     * the context. This method is only valid before the context
     * creation process begins and only for the initiator.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the conf_req_flag parameter in
     *    gss_init_sec_context</dl>
     * @param Boolean indicating if confidentiality services are to
     *    be requested for the context.
     * @exception GSSException may be thrown
     * @see #getConfState
     */
    public void requestConf(boolean state) throws GSSException {

        checkState(PRE_INIT);
        
        if (state)
            m_reqFlags |= CONF;
        else
            m_reqFlags ^= CONF;
    }


    /**
     * Requests that integrity service be available    over
     * the context. This method is only valid before the context
     * creation process begins and only for the initiator.
     *     
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the integ_req_flag parameter in
     *    gss_init_sec_context</dl>
     * @param Boolean indicating if integrity services are to
     *    be requested for the context.
     * @exception GSSException may be thrown
     * @see #getIntegState
     */
    public void requestInteg(boolean state) throws GSSException {

        checkState(PRE_INIT);
        
        if (state)
            m_reqFlags |= INTG;
        else
            m_reqFlags ^= INTG;
    }


    /**
     * Sets the desired lifetime for the context in seconds.
     * This method is only valid before the context creation
     * process begins and only for the initiator.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the lifetime_req parameter in
     *    gss_init_sec_context</dl>
     * @param The desired context lifetime in seconds.
     * @exception GSSException may be thrown
     * @see #getLifetime
     */
    public void requestLifetime(int lifetime) throws GSSException {

        checkState(PRE_INIT);
        
        m_reqLifetime = lifetime;
    }
  

    /**
     * Sets the channel bindings to be used during context
     * establishment. This method is only valid before the
     * context creation process begins.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the chan_bindings parameter in
     *    gss_init_sec_context and gss_accept_sec_context</dl>
     * @param Channel binding to be used.
     * @exception GSSException may be thrown
     * @see ChannelBinding
     */
    public void setChannelBinding(ChannelBinding cb) throws GSSException {
    
        checkState(PRE_INIT);
        m_chB = cb;
    }


    /**
     * Returns the state of the delegated credentials for the context.
     * When issued before context establishment completes or
     * when the isProtReady method returns false, it returns the
     * desired state, otherwise it will indicate the actual state over
     * the established context.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the deleg_state flag output parameter in
     *    gss_init_sec_context, gss_accept_sec_context and
     *    gss_inquire_context</dl>
     * @return boolean indicating if delegated credentials are available
     * @see #requestCredDeleg
     * @see #isProtReady
     */
    public boolean getDelegCredState() {
    
        if (m_curState < READY)
            return ((m_reqFlags & CRED_DELEG) == CRED_DELEG);
            
        return ((m_mechCtxt._getOptions () & CRED_DELEG) == CRED_DELEG);
    }

  
    /**
     * Returns the state of the mutual authentication option for
     * the context.  When issued before context establishment completes
     * or when the isProtReady method returns false, it returns the
     * desired state, otherwise it will indicate the actual state over
     * the established context.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the mutual_state flag output parameter
     *    in gss_init_sec_context, gss_accept_sec_context and
     *    gss_inquire_context</dl>
     * @return boolean indicating state of mutual authentication option
     * @see #requestMutualAuth
     * @see #isProtReady
     */
    public boolean getMutualAuthState() {
    
        if (m_curState < READY)
            return ((m_reqFlags & MUTUAL_AUTH) == MUTUAL_AUTH);
            
        return ((m_mechCtxt._getOptions () & MUTUAL_AUTH) == MUTUAL_AUTH);
    }


    /**
     * Returns the state of the replay detection service for
     * the context.  When issued before context establishment completes
     * or when the isProtReady method returns false, it returns the
     * desired state, otherwise it will indicate the actual state over
     * the established context.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the replay_det_state flag output
     *    parameter in gss_init_sec_context, gss_accept_sec_context
     *    and gss_inquire_context</dl>
     * @return boolean indicating replay detection state
     * @see #requestReplayDet
     * @see #isProtReady
     */
    public boolean getReplayDetState() {
    
        if (m_curState < READY)
            return ((m_reqFlags & REPLAY_DET) == REPLAY_DET);
            
        return ((m_mechCtxt._getOptions () & REPLAY_DET) == REPLAY_DET);
    }


    /**
     * Returns the state of the sequence detection service for
     * the context.  When issued before context establishment completes
     * or when the isProtReady method returns false, it returns the
     * desired state, otherwise it will indicate the actual state over
     * the established context.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the sequence_state flag output parameter
     *    in gss_init_sec_context, gss_accept_sec_context and
     *    gss_inquire_context</dl>
     * @return boolean indicating sequence detection state
     * @see #requestSequenceDet
     * @see #isProtReady
     */
    public boolean getSequenceDetState() {
    
        if (m_curState < READY)
            return ((m_reqFlags & SEQUENCE_DET) == SEQUENCE_DET);
            
        return ((m_mechCtxt._getOptions () & SEQUENCE_DET) == SEQUENCE_DET);
    }


    /**
     * Returns true if this is an anonymous context. When issued
     * before context establishment completes or when the
     * isProtReady method returns false, it returns the desired
     * state, otherwise it will indicate the actual state over
     * the established context.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the anon_state flag output parameter in
     *    gss_init_sec_context, gss_accept_sec_context and
     *    gss_inquire_context</dl>
     * @return boolean indicating anonymity state
     * @see #requestAnonymity
     * @see #isProtReady
     */
    public boolean getAnonymityState() {
    
        if (m_curState < READY)
            return ((m_reqFlags & ANON) == ANON);
            
        return ((m_mechCtxt._getOptions () & ANON) == ANON);
    }


    /**
     * Indicates if the context is transferable to other processes
     * through the use of the export method. This call is only valid
     * on fully established contexts.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the trans_state flag output parameter in
     *    gss_init_sec_context, gss_accept_sec_context and
     *    gss_inquire_context</dl>
     * @return boolean indicating the transferability of the context
     * @exception GSSException may be thrown
     * @see #export
     */
    public boolean isTransferable() throws GSSException {
        
        checkState(READY);
        return ((m_mechCtxt._getOptions () & TRANS) == TRANS);
    }
    

    /**
     * Indicates if the per message operations can be applied over
     * the context.  Some mechanisms may allow to apply per-message
     * operations before the context is fully established. This will
     * also indicate that the get methods will return actual context
     * state characteristics instead of the desired ones.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the prot_ready_state flag output
     *    parameter in gss_init_sec_context and
     *    gss_accept_sec_context</dl>
     * @return boolean indicating if per message operations are available
     */
    public boolean isProtReady() {
        
        if (m_mechCtxt == null)
            return false;
            
        return (m_mechCtxt._isProtReady ());
    }


    /**
     * Returns the confidentiality service state over the context.
     * When issued before context establishment completes or when
     * the isProtReady method returns false, it returns the desired
     * state, otherwise it will indicate the actual state over
     * the established context.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the conf_avail flag output parameter
     *    in gss_init_sec_context, gss_accept_sec_context and
     *    gss_inquire_context</dl>
     * @return boolean indicating confidentiality state
     * @see #requestConf
     * @see #isProtReady
     */
    public boolean getConfState() {
    
        if (m_curState < READY)
            return ((m_reqFlags & CONF) == CONF);
            
        return ((m_mechCtxt._getOptions () & CONF) == CONF);
    }


    /**
     * Returns the integrity service state over the context.
     * When issued before context establishment completes or when
     * the isProtReady method returns false, it returns the desired
     * state, otherwise it will indicate the actual state over
     * the established context.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the integ_avail flag output parameter
     *    in gss_init_sec_context, gss_accept_sec_context and
     *    gss_inquire_context</dl>
     * @return boolean indicating integrity state
     * @see #requestInteg
     * @see #isProtReady
     */
    public boolean getIntegState() {
    
        if (m_curState < READY)
            return ((m_reqFlags & INTG) == INTG);
            
        return ((m_mechCtxt._getOptions () & INTG) == INTG);
    }


    /**
     * Returns the context lifetime in seconds.
     * When issued before context establishment completes or when
     * the isProtReady method returns false, it returns the desired
     * lifetime, otherwise it will indicate the actual lifetime over
     * the established context.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the lifetime_rec output parameter in
     *    gss_init_sec_context, gss_accept_sec_context, 
     *    gss_inquire_context and to gss_context_time call</dl>
     * @return lifetime in seconds
     * @see #requestLifetime
     * @see #isProtReady
     */
    public int getLifetime() {
    
        if (m_curState < READY)
            return (m_reqLifetime);
            
        return (m_mechCtxt._getLifetime ());
    }
    
    
    /**
     * Retrieves the name of the context initiator.
     * This call is valid only after context has been fully established
     * or when the isProtReady methods returns true.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the src_name parameter in
     *    gss_accept_sec_context and gss_inquire_context</dl>
     * @return name of the context initiator
     * @exception GSSException with possible major codes of CONTEXT_EXPIRED
     *    and FAILURE
     * @see #isProtReady
     */
    public GSSName getSrcName() throws GSSException {
    
        checkState(IN_PROGRESS);
        return (new GSSName(m_mechCtxt._getSrcName ()));
    }
    
    
    /**
     * Retrieves the name of the context target (acceptor).
     * This call is only valid on fully established contexts
     * or when the isProtReady methods returns true.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the targ_name parameter in
     *    gss_inquire_context</dl>
     * @return name of the context target (acceptor)
     * @exception GSSException with possible major codes of
     *    CONTEXT_EXPIRED and FAILURE
     * @see #isProtReady
     */
    public GSSName getTargName() throws GSSException {
    
        checkState(IN_PROGRESS);
        return (new GSSName(m_mechCtxt._getTargName ()));
    }
    
    
    /**
     * Returns the mechanism oid for the context.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to the mech_type parameter in
     *    gss_accept_sec_context and gss_inquire_context</dl>
     * @return Oid object for the context's mechanism
     * @exception GSSException may be thrown when the mechanism
     *     oid can't be determined
     * 
     */
    public Oid getMech() throws GSSException {

        checkState(IN_PROGRESS);
        return (m_mechCtxt._getMech ());
    }
    
    
    /**
     * Returns the delegated credential object on the acceptor's side.
     * To check for availability of delegated credentials call
     * getDelegCredState. This call is only valid on fully established
     * contexts.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to delegated_cred_handle parameter
     *    in gss_accept_sec_context</dl>
     * @return delegated credential object for the context
     * @exception GSSException with possible major codes of
     *    CONTEXT_EXPIRED and FAILURE
     * @see #getDelegCredState
     */
    public GSSCredential getDelegCred() throws GSSException {
    
        checkState(IN_PROGRESS);        
        return (new GSSCredential(m_mechCtxt._getDelegCred ()));
    }
    
    
    /**
     * Returns true if this is the initiator of the context.
     * This call is only valid after the context creation process
     * has started.
     *
     * <p><DL><DT><B>RFC 2078</b>
     *     <DD>equivalent to locally_initiated output parameter
     *    in gss_inquire_context</dl>
     * @return true if this is the context initiator
     * @exception GSSException with possible major codes of
     *    CONTEXT_EXPIRED and FAILURE
     */
    public boolean isInitiator() throws GSSException {

        checkState(IN_PROGRESS);
        return (m_mechCtxt._isInitiator ());
    }


    /**
     * Ensures the object is in the required state.
     * 
     * @param minimumState the minimum required state.
     * @exception GSSException is thrown when the state is incorrect.
     */
    private void checkState(int minimumState) throws GSSException {
    
        if (m_curState < minimumState)
            throw new GSSException(GSSException.NO_CONTEXT);
            
        //this is just temporary for debugging
        if (minimumState > PRE_INIT && m_mechCtxt == null)
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                    "error in checkState");
    }


    /**
     * Private initializer of context variables
     */
    private void initialize() {
    
        m_reqFlags = MUTUAL_AUTH | SEQUENCE_DET | REPLAY_DET;
        m_myCred = null;
        m_reqLifetime = INDEFINITE;
        m_curState = PRE_INIT;
        m_mechOid = null;
        m_targName = null;
        m_chB = null;
    }


    /**
     * Reads the token header as defined in RFC 2078 and
     * returns the mechanism oid. Currently this requires that
     * the stream support mark/reset operations. As an
     * alternative we can strip this header, and pass the information
     * into the mechanism.
     */
    private Oid getTokenMech(InputStream is) throws GSSException {
    
        try {
            is.mark(100);
        
            if (is.read() != 0x60)
                throw new GSSException(GSSException.DEFECTIVE_TOKEN);
        
            //we can skip the token length    
            DERParser.readLength(is);
        
            //next we have the full der encoding of the oid
            Oid res = new Oid(is);
            is.reset();
            
            return (res);
        } catch (IOException e) {
            throw new GSSException(GSSException.DEFECTIVE_TOKEN);
        }
        
    }


    //private flags for the context state
    private static final int PRE_INIT = 1;
    private static final int IN_PROGRESS = 2;
    private static final int READY = 3;
    private static final int DELETED = 4;
    
    
    //instance variables
    private GSSCtxtSpi m_mechCtxt;
    private int m_reqFlags;
    private GSSCredential m_myCred;
    private int m_reqLifetime;
    private int m_curState;
    private Oid m_mechOid;
    private GSSName m_targName;
    private ChannelBinding m_chB;
}
