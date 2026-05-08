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
 
package com.sun.gssapi.dummy;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import com.sun.gssapi.*;


/**
 * Implements the dummy Context class GSSCtxtSpi interface
 * for the dummy mechanism.
 */
public class DummyCtxt implements GSSCtxtSpi {


	/**
	 * Standard constructor.
	 */
	public DummyCtxt() {

		m_state = NEW;
		m_tokCount = Dummy.getNumOfTokExchanges();
		m_lifetime = 0;
	}


	/**
	 * Sets the context parameters for the initiator side of
	 * the context.
	 */
	public void _setInitOptions (GSSCredSpi myCred, GSSNameSpi targName,
			int desLifetime, int ctxtOptions) throws GSSException {

		//make sure we are in right state
		if (m_state != NEW) {
                        throw new GSSException(GSSException.NO_CONTEXT);
		}

		//set the target name
		if (targName == null || ! (targName instanceof DummyName))
			throw new GSSException(GSSException.BAD_NAME);
                
		m_targName = (DummyName)targName;


		//get the src name from credential - that's all we have in there
		if (myCred == null) {
			m_srcName = DummyName.getDefault();
		} else {
    			if (! (myCred instanceof DummyCred))
                                throw new GSSException(GSSException.DEFECTIVE_CREDENTIAL);
			
			//we can do the cast below because we know it is our credential
			m_srcName = (DummyName)myCred.getName();
		}


		//check for default lifetime request
		if (desLifetime == 0)
			m_lifetime = GSSContext.INDEFINITE;
		else
                        m_lifetime = desLifetime;


		m_flags = ctxtOptions;
		m_initiator = true;
	}
	
	
	/**
	 * Sets the context parameters for the acceptor side
	 * of the context.
	 */
	public void _setAcceptOptions (GSSCredSpi myCred) throws GSSException {

		if (myCred == null) {
			m_targName = DummyName.getDefault();
			m_lifetime = GSSContext.INDEFINITE;
		} else {
		
			if (!(myCred instanceof DummyCred))
				throw new GSSException(GSSException.NO_CRED);

			m_targName = (DummyName)myCred.getName();
			m_lifetime = myCred.getAcceptLifetime();
		}

		m_flags = 0;
                m_initiator = false;	
	}
	

	/**
	 * Sets the channel bindings to be used during context
	 * establishment.
	 */
	public void _setChannelBinding (ChannelBinding chb) throws GSSException {

		throw new GSSException(GSSException.UNAVAILABLE);
	}

				
	/**
	 * Retrieves the mechanism options.
	 *
	 * @return int GSSContext options ORed together
	 */
	public int _getOptions () {

		return (m_flags);
	}
	
	
	/**
	 * Inquire the remaining lifetime.
	 *
	 * @return the lifetime in seconds. May return reserved
	 *	value GSSContext.INDEFINITE for an indefinite lifetime.
	 */
	public int _getLifetime () {

		return (m_lifetime);
	}
		

	/**
	 * Returns the mechanism oid.
	 *
	 * @return the Oid of this context
	 */
	public Oid _getMech () {

		return (Dummy.getMyOid());
	}


	/**
	 * Returns the context initiator name.
	 * 
	 * @return initiator name
	 * @exception GSSException
	 */
	public GSSNameSpi _getSrcName () throws GSSException {

		if (m_state != DONE) {
			throw new GSSException(GSSException.NO_CONTEXT);

		}

		return (m_srcName);
	}
	
	
	/**
	 * Returns the context acceptor.
	 *
	 * @return context acceptor(target) name
	 * @exception GSSException
	 */
	public GSSNameSpi _getTargName () throws GSSException {

		if (m_state != DONE) {
			throw new GSSException(GSSException.NO_CONTEXT);
		}


		return (m_targName);
	}
	
	
	/**
	 * Returns the delegated credential for the context. This
	 * is an optional feature of contexts which not all
	 * mechanisms will support. A context can be requested to
	 * support credential delegation by using the <b>CRED_DELEG</b>.
	 * This is only valid on the acceptor side of the context.
	 * @return GSSCredSpi object for the delegated credential
	 * @exception GSSException
	 * @see GSSContext#getDelegCredState
	 */
	public GSSCredSpi _getDelegCred () throws GSSException {

		throw new GSSException(GSSException.UNAVAILABLE);
	}
 
	
	/**
	 * Tests if this is the initiator side of the context.
	 *
	 * @return boolean indicating if this is initiator (true)
	 *	or target (false)
	 */
	public boolean _isInitiator () {

		return (m_initiator);
	}
	

	/**
	 * Tests if the context can be used for per-message service.
	 * Context may allow the calls to the per-message service
	 * functions before being fully established.
	 *
	 * @return boolean indicating if per-message methods can
	 *	be called.
	 */
	public boolean _isProtReady () {

		return (m_state == DONE);
	}
	
	
	/**
	 * Initiator context establishment call. This method may be
	 * required to be called several times. A CONTINUE_NEEDED return
	 * call indicates that more calls are needed after the next token
	 * is received from the peer.
	 *
	 * @param is contains the token received from the peer. On the
	 *	first call it will be ignored.
	 * @param os to which any tokens required to be sent to the peer
	 *	will be written. It is responsibility of the caller
	 *	to send the token to its peer for processing.
	 * @return integer indicating if more calls are needed. Possible
	 *	values are COMPLETE and CONTINUE_NEEDED.
	 * @exception GSSException
	 */
	public int _initSecCtxt (InputStream is, OutputStream os) throws GSSException {


		boolean needTokenBack = true;
		boolean sendToken = true;

		m_tokCount--;
				
		if (m_state == NEW) {
                        m_initiator = true;
			m_state = IN_PROCESS;
		}
		else if (m_state == IN_PROCESS) {
			if (!_isInitiator ()) {
				throw new GSSException(GSSException.DEFECTIVE_TOKEN);
			}

			//read the token
			sendToken = processEstabToken(is);
		}

		//determine if we need token back
		if (!sendToken || m_tokCount < 1)
			needTokenBack = false;
			
		if (sendToken) {
			
			//create a token for our peer
			createEstabOutToken(os, needTokenBack);
		}
                
		if (needTokenBack) {
			return (GSSContext.CONTINUE_NEEDED);
		}

		m_state = DONE;
		return (GSSContext.COMPLETE);
	}


	/**
	 * Acceptor's context establishment call. This method may be
	 * required to be called several times. A CONTINUE_NEEDED return
	 * call indicates that more calls are needed after the next token
	 * is received from the peer.
	 *
	 * @param is contains the token received from the peer.
	 * @param os to which any tokens required to be sent to the peer
	 *	will be written. It is responsibility of the caller
	 *	to send the token to its peer for processing.
	 * @return integer indicating if more calls are needed. Possible
	 *	values are COMPLETE and CONTINUE_NEEDED.
	 * @exception GSSException
	 */
	public int _acceptSecCtxt (InputStream is, OutputStream os) throws GSSException {

		boolean needTokenBack = true;
		boolean sendToken = true;

		//we have this but it's not used since the status
		// code in tokens dictate if we need more
		m_tokCount--;
				
		if (m_state == NEW) {
                        m_initiator = false;
			m_state = IN_PROCESS;
		}
		else if (m_state == IN_PROCESS) {
			if (_isInitiator ()) {
				throw new GSSException(GSSException.DEFECTIVE_TOKEN);
			}

			//read the token
			sendToken = processEstabToken(is);
		}

		//determine if we need token back
		if (!sendToken || m_tokCount < 1)
			needTokenBack = false;
			
		if (sendToken) {
			
			//create a token for our peer
			createEstabOutToken(os, needTokenBack);
		}
                
		if (needTokenBack) {
			return (GSSContext.CONTINUE_NEEDED);
		}

		m_state = DONE;
		m_srcName = new DummyName("dummy src name");
		return (GSSContext.COMPLETE);
	}


	/**
	 * Queries the context for largest data size to accomodate
	 * the specified protection and be <= maxTokSize.
	 *
	 * @param qop the quality of protection that the context will be
	 *	asked to provide. 
	 * @param confReq a flag indicating whether confidentiality will be
	 *	requested or not
	 * @param outputSize the maximum size of the output token
	 * @return the maximum size for the input message that can be
	 *	provided to the wrap() method in order to guarantee that these
	 *	requirements are met.
	 * @throws GSSException
	 */
	public int _getWrapSizeLimit (int qop, boolean confReq, int maxTokSize) throws GSSException {

		return (maxTokSize);
	}


	/**
	 * Provides per-message token encapsulation.
	 *
	 * @param is the user-provided message to be protected
	 * @param os the token to be sent to the peer. It includes
	 *	the message from <i>is</i> with the requested protection.
	 * @param msgPro on input it contains the requested qop and
	 *	confidentiality state, on output, the applied values
	 * @exception GSSException
	 * @see MessageInfo
	 * @see unwrap
	 */
	public void _wrap (InputStream is, OutputStream os, MessageProp msgProp) throws GSSException {


		if (m_state != DONE)
			throw new GSSException(GSSException.NO_CONTEXT);

		try {

       			int length = is.available();
			createTokenHeader(os, length + 1);

			while (length-- > 0) {
                                os.write(is.read());
			}
			
			os.write(0);

		} catch (IOException e) {
			throw new GSSException(GSSException.FAILURE, -1, "io exception in wrap");
		}

	}


	/**
	 * Retrieves the message token previously encapsulated in the wrap
	 * call.
	 *
	 * @param is the token from the peer
	 * @param os unprotected message data
	 * @param msgProp will contain the applied qop and confidentiality
	 *	of the input token
	 * @return int representing the informatory status; this can be
	 *	COMPLETE, DUPLICATE_TOKEN, OLD_TOKEN, UNSEQ_TOKEN and GAP_TOKEN.
	 * @exception GSSException
	 * @see MessageInfo
	 * @see wrap
	 */
	public void _unwrap (InputStream is, OutputStream os, MessageProp msgProp) throws GSSException {
		
		if (m_state != DONE)
			throw new GSSException(GSSException.NO_CONTEXT);

		try {
       			int length = processTokenHeader(is);

			while (length-- > 0) {
				os.write(is.read());
			}

			msgProp.setPrivacy(true);
			msgProp.setQOP(0);
		} catch (IOException e) {
			throw new GSSException(GSSException.FAILURE, -1, "io exception in unwrap");
		}
	}


	/**
	 * Applies per-message integrity services.
	 *
	 * @param is the user-provided message
	 * @param os the token to be sent to the peer along with the
	 *	message token. The message token <b>is not</b> encapsulated.
	 * @param msgProp on input the desired QOP and output the applied QOP
	 * @exception GSSException
	 */
	public void _getMIC (InputStream is, OutputStream os, MessageProp msgProp)
				throws GSSException {
		
		if (m_state != DONE)
			throw new GSSException(GSSException.NO_CONTEXT);

		try {
			String tokenStr = new String("dummy_gss_sign");
			createTokenHeader(os, tokenStr.length() + 1);

			os.write(tokenStr.getBytes());
			os.write(0);

			msgProp.setPrivacy(false);
			msgProp.setQOP(0);

		} catch (IOException e) {
			throw new GSSException(GSSException.FAILURE, -1, "io exception in unwrap");
		}
	}


	/**
	 * Checks the integrity of the supplied tokens.
	 * This token was previously generated by getMIC.
	 *
	 * @param is token generated by getMIC
	 * @param msgStr the message to check integrity for
	 * @param msgProp will contain the applied QOP and confidentiality
	 *	states of the token
	 * @return int informatory status which can be one of COMPLETE,
	 *	DUPLICATE_TOKEN, OLD_TOKEN, UNSEQ_TOKEN and GAP_TOKEN.
	 * @exception GSSException
	 */
	public void _verifyMIC (InputStream is, InputStream msgStr, MessageProp mProp) throws GSSException {

		try {
			int msgLen = processTokenHeader(is);
			is.skip(msgLen);

		} catch (IOException e) {
			throw new GSSException(GSSException.DEFECTIVE_TOKEN);
		}
	}


	/**
	 * Produces a token representing this context. After this call
	 * the context will no longer be usable until an import is
	 * performed on the returned token.
	 *
	 * @param os the output token will be written to this stream
	 * @exception GSSException
	 */
	public byte [] _export () throws GSSException {


		throw new GSSException(GSSException.UNAVAILABLE);
	}

    
	/**
	 * Imports a previously exported context.
	 *
	 * @param Previously exported token
 	 * @exception GSSException
	 * @see export
	 */
	public void _importSecCtxt (byte []aCtxtToken) throws GSSException {


		throw new GSSException(GSSException.UNAVAILABLE);
	}

    
	/** 
	 * Releases context resources and terminates the
	 * context between 2 peer.
	 *
	 * @exception GSSException with major codes NO_CONTEXT, FAILURE.
	 */
	public void _dispose () throws GSSException {

		m_state = DELETED;
	}


	/**
	 * Create token used during context establishment.
	 */
	private void createEstabOutToken(OutputStream os, boolean moreTokens)
			throws GSSException {

		try {
			String msgBody;
		   	if (moreTokens)
				msgBody = Integer.toString(1);
			else
				msgBody = Integer.toString(0);
			
			/*
			 * the token is composed of:
			 * 	tag
			 *	DER tok length
			 *	DER oid
			 *	2 bytes for token type
			 *	msg body
			 */

			createTokenHeader(os, msgBody.length() + 1);

                        DataOutputStream dos = new DataOutputStream(os);
			
			//need to be written as bytes because that is what C version uses
			dos.writeBytes(msgBody);
			dos.write(0);	//add null char - this is what C dummy wants
			dos.flush();
		} catch (IOException e) {
			throw new GSSException(GSSException.FAILURE, -1, "createInitOutToken");
		}
	}


	/**
	 * Processes the peer's context establishment token.
	 */
	private boolean processEstabToken(InputStream is) throws GSSException {
	
		try {
		
			processTokenHeader(is);

			//get continue/complete code
			byte []b = new byte[1];
			b[0] = (byte)is.read();
			int status = Integer.parseInt(new String(b));
			is.read(); //null Byte
		
			if (status == 1)
				return (true);
				
			return(false);
			
		} catch (IOException e) {
			throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1, "processing init token from peer");
		}	
	
	}
	
	
	/**
	 * Checks the token header.
	 *
	 * @return the length of the message remaining
	 */
	private int processTokenHeader(InputStream is)
				throws GSSException, IOException {


		if (is.read() != 0x60)
			throw new GSSException(GSSException.DEFECTIVE_TOKEN);
			
		//read token length
		int length = DERParser.readLength(is);
		Oid tokOid = new Oid(is);
		if (!tokOid.equals(Dummy.getMyOid()))
			throw new GSSException(GSSException.BAD_MECH);
			
		//read token tag - not used
		is.read();
		is.read();

		//return the length of message remaining
		return (length - 1 - 1 - 10 - 2);
	}


	/**
	 * Constructs the token header
	 */
	private void createTokenHeader(OutputStream os, int msgBodyLen)
		throws GSSException {
 		
		try {
			/*
			 * the token header is composed of:
			 * 	tag
			 *	DER tok length
			 *	DER oid
			 *	0x0 0x0 (token tag)
			 */

			byte [] derOid = Dummy.getMyOid().getDER();

			int length = derOid.length + msgBodyLen + 2;

			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

			dos.write(0x60);
			DERParser.writeLength(dos, length);
			dos.write(derOid);

			//now write the token tag - 0x0 - doesn't seem to be used
			dos.write(0);
			dos.write(0);

			dos.flush();
		} catch (IOException e) {
			throw new GSSException(GSSException.FAILURE, -1, "createTokenHeader");
		}
	}


	//instance variables
	private DummyName m_srcName;
	private DummyName m_targName;
	private int m_tokCount;
	private int m_state;
	private int m_flags;
	private int m_lifetime;
	private boolean m_initiator;

	private static final int NEW = 1;
	private static final int IN_PROCESS = 2;
	private static final int DONE = 3;
	private static final int DELETED = 4;
}



/**
 * This is a package private class used to decode/encode ASN.1 DER
 * length.
 */
class DERParser {

	/**
	 * Returns the DER encoded length from the InputStream.
	 */
	static int readLength(InputStream is) throws GSSException {
	
		int length, tmp;
		//get the length of Oid - check if short form
		try {
			if (((tmp = is.read()) & 0x080) == 0)
				length = tmp;
			else {
				//must be long form
				tmp &= 0x7f;
				for (length = 0; tmp > 0; tmp--) {
					length <<= 8;
					length += (0xff & is.read());
				}
			}
		} catch (IOException e) {
			throw new GSSException(GSSException.DEFECTIVE_TOKEN);
		}
		
		return (length);
	}
	
	
	/**
	 * Encodes DER length.
	 */
	static void writeLength(OutputStream os, int length) throws IOException {
			
		//encode the length - for all practical purposes, the length
		//should always be less then 0x80 (128)
		if (length < 0x80)
			os.write(length);
		else if (length < 0x100) {
			os.write(0x081);
			os.write(length);
		} else if (length < 0x80000) {
			os.write(0x082);
			os.write(length >> 8);
			os.write(length & 0xff);
		} else if (length < 0x1000000) {
			os.write(0x083);
			os.write(length >> 16);
			os.write((length >> 8) & 0xff);
			os.write(length & 0xff);
		} else {
			os.write(0x084);
			os.write(length >>> 24);
			os.write((length >> 16) & 0xff);
			os.write((length >> 8) & 0xff);
			os.write(length & 0xff);
		}
	}
}
