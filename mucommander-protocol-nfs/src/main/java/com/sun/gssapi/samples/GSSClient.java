package com.sun.gssapi.samples;
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
/*
 * Sample gss-client application.
 * This will inter-operate with the MIT samples.
 *
 *
 * GSSClient [-port port] [-mech 1.2.3.] host service msg
 */
 
import java.io.*;
import java.net.*;

import com.sun.gssapi.*;

class GSSClient {

	/**
	 * Main method for the GSSClient sample application.
	 * This application in compatible with the MIT sample
	 * applications (gss-server, gss-client).
	 */
	public static void main(String args[]) {

		String serverHost, serverName, message;
		int port = 4444;

		if (args.length < 3) {
			usage();
			exit(1);
		}


		//set the required command line args
		serverHost = args[args.length - 3];
		serverName = args[args.length - 2];
		message = args[args.length - 1];
		
		try {

			Oid mechOid = GSSManager.getDefaultMech();

			//parse the command line options
			for (int i = 0; i < (args.length - 3); i++) {

				if (args[i].equals("-port")) {
					if (i >= (args.length - 4)) {
						usage();
						exit(-1);
					}
					port = Integer.parseInt(args[++i]);
				} else if (args[i].equals("-mech")) {
					if (i >= (args.length-4)) {
						usage();
						exit(-1);
					}
					mechOid = new Oid(args[++i]);
				}
			}

			//connect to the server
			s = new Socket(serverHost, port);
		

			//establish a GSSContext with server
			GSSContext aCtxt = createCtxt(serverName, mechOid);

    			print("\nContext established\n");

			//display context options
			displayContext(aCtxt);


			//wrap the message and send it to server
			sendMsgToPeer(aCtxt, message);                        
			
			//receiving message from server
			verifyMsgFromPeer(aCtxt, message);

			//context no longer needed
			aCtxt.dispose();
			exit(0);

		} catch (IOException e) {
			print("\n**Communication ERROR**:\t" + e.getMessage());
			e.printStackTrace();
			exit(-1);
		} catch (GSSException e) {
			print("\n**GSSAPI ERROR**:\t" + e.getMessage());
			e.printStackTrace();
			exit(-1);
		}
	}


	/**
	 * Creates a GSS-API context with the server over the specified
	 * security mechanism.
	 */
	private static GSSContext createCtxt(String serverName, Oid mechOid)
					throws GSSException, IOException {

		GSSContext aCtxt = new GSSContext(
			new GSSName(serverName, GSSName.NT_HOSTBASED_SERVICE),
      				mechOid, null, 0);

		//set context options
		aCtxt.requestConf(true);
		aCtxt.requestInteg(true);
		aCtxt.requestMutualAuth(true);
		aCtxt.requestReplayDet(true);
		aCtxt.requestSequenceDet(true);
		
		//start the context creation process
		DataInputStream dis = new DataInputStream(s.getInputStream());
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());

		byte []inTok = new byte[0];
		
		print("\nCalling init.....");
		do {
			byte[] outTok = aCtxt.init(inTok, 0, inTok.length);
			
			//send the token if present
			if (outTok != null) {
			
				print("\tSending token to peer (" + outTok.length + " bytes)...");
				
				//MIT apps write length first, then token
				dos.writeInt(outTok.length);
				dos.write(outTok, 0, outTok.length);
			}
			
			//check if we should expect more tokens
			if (aCtxt.isEstablished())
				break;
			
			//get input token from peer
			inTok = new byte[dis.readInt()];
			print("\tReceiving token from peer (" + inTok.length + " bytes)...");
			dis.readFully(inTok, 0, inTok.length);
		} while (true);
			
		return (aCtxt);
	}

	
	/**
	 * Display context information/flags.
	 */
	private static void displayContext(GSSContext aCtxt) throws GSSException {


		//display context information
		print("Context Information....");
		if (aCtxt.getLifetime() == GSSContext.INDEFINITE)
			print("\tOver mech:\t" + aCtxt.getMech().toString() + " for  " + " INDEFINITE seconds");
		else
			print("\tOver mech:\t" + aCtxt.getMech().toString() + " for  " + aCtxt.getLifetime() + " seconds");

		print("\tInitiator:\t" + aCtxt.getSrcName().toString());
		print("\tAcceptor:\t" + aCtxt.getTargName().toString());
		if (aCtxt.getDelegCredState())
			print("\tDelegated credentials available.");
		else
			print("\tNO delegated credentials");

		if (aCtxt.getMutualAuthState())
			print("\tMutaul Authentication ON");
		else
			print("\tNO mutual authentication performed.");

		if (aCtxt.getReplayDetState())
			print("\tReplay detection ON");
		else
			print("NO replay detection");

		if (aCtxt.getSequenceDetState())
			print("\tSequence detection ON");
		else
			print("\tNO sequence detection");

		if (aCtxt.getAnonymityState())
			print("\tAnonymous context");
		
		if (aCtxt.isTransferable())
			print("\tContext is transferable");
		else
			print("\tNO context transfer");

		if (aCtxt.isProtReady())
			print("\tContext protection is ready");
		else
			print("**ERROR wrong state - context established, but isProtReady = false");

		if (aCtxt.getConfState())
			print("\tConfidentiality available");
		else
			print("\tNO confidentiality services");

		if (aCtxt.getIntegState())
			print("\tIntegrity available");
		else
			print("\tNO integrity services");

	}


	/**
	 * Sends a wraped message to the server.
	 */
	private static void sendMsgToPeer(GSSContext aCtxt, String msg)
				throws GSSException, IOException {

		print("\nWrapping message for server...");

		MessageProp mInfo = new MessageProp(0, true);
		byte []tok = aCtxt.wrap(msg.getBytes(), 0, msg.length(), mInfo);
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		dos.writeInt(tok.length);
		dos.write(tok);
		dos.flush();
	}


	/**
	 * Checks the MIC on a message received from the server.
	 */
	private static void verifyMsgFromPeer(GSSContext aCtxt, String msg)
				throws GSSException, IOException {
	
	
		print("receiving MIC message from server...");

		DataInputStream dis = new DataInputStream(s.getInputStream());

		int len = dis.readInt();
		print("Receiving message from peer (" + len + " bytes)");

		MessageProp mInfo = new MessageProp();
		aCtxt.verifyMIC(dis, new StringBufferInputStream(msg), mInfo);

 		print("Verified server message protected with QOP = " + mInfo.getQOP());
	}


	/**
	 * Utility method to display application usage string.
	 */
	private static void usage() {

		print("GSSClient [-port port] [-mech 1.2.3.3] serverhost servername message");
	}


	/**
	 * Utility method to display information to the screen.
	 */
	private static void print(String msg) {

		System.out.println(msg);
	}


	/**
	 * Utility method to gracefully shut down the connection and
	 * terminate the application.
	 */
	private static void exit(int status) {
		
		if (s != null) {
			try {
                                s.close();
			} catch (IOException e) {};
		}

		System.exit(status);
	}

	//private class variables
	private static Socket s;
}
