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
 * Sample gss-server application.
 * This will inter-operate with the MIT samples.
 *
 * GSSServer [-port port] [-mech 1.2.3.4.] serviceName
 */
 
import java.io.*;
import java.net.*;

import com.sun.gssapi.*;

class GSSServer {

	/**
	 * Main method to start our sample application.
	 * This is the server end of the MIT like sample
	 * GSS-API applications.
	 */
	public static void main(String args[]) {

		//default MIT port
		int port = 4444;

		if (args.length < 1) {
			usage();
			exit(-1);
		}

		try {
	
			String serviceName = args[args.length-1];
			Oid mechOid = GSSManager.getDefaultMech();

			//parse the command line options
			for (int i = 0; i < args.length; i++) {

				if (args[i].equals("-port")) {
					if (i >= (args.length - 2)) {
						usage();
						exit(-1);
					}
					port = Integer.parseInt(args[++i]);
				} else if (args[i].equals("-mech")) {
					if (i >= (args.length-2)) {
						usage();
						exit(-1);
					}
					mechOid = new Oid(args[++i]);
				}
			}


		
			//acquire server credentials
			print("\nAcquiring credentials as " + serviceName + "...");
			GSSCredential server = new GSSCredential(new GSSName(serviceName,
				GSSName.NT_HOSTBASED_SERVICE), GSSCredential.INDEFINITE,
				mechOid, GSSCredential.ACCEPT_ONLY);
		  
			print("\nDumping credential info...\n" + server.toString() + "\n");

			s = new ServerSocket(port);

			while (true) {
			
				//wait for connections
				print("\n\nWaiting for connections on port " + port + "...");
                                Socket c = s.accept();

				print("Accepted connection from " + c.getInetAddress().getHostName());
				processClient(server, c);
			}
		
		  
		//catch all errors
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
	 * Processes a client connection.
	 * 	-creates a context
	 *	-displays context information
	 *	-unwraps a client token
	 *	-produces a MIC on client token
	 *	-destroy the context
	 */
	private static void processClient(GSSCredential server, Socket client)
				throws GSSException, IOException {

		//we have a client connected on the socket
		DataInputStream dis = new DataInputStream(new BufferedInputStream(client.getInputStream()));
		DataOutputStream dos = new DataOutputStream(client.getOutputStream());

		print("\n\nCreating context...");
		
		//MIT apps, first send token length
		int tokLen = dis.readInt();
		print("\tReceiving token from peer (" + tokLen + " bytes)...");

		
		//create acceptor GSS-API context
		GSSContext aCtxt = new GSSContext(server);
		
		//get first token from peer
		byte []inTok = new byte[tokLen];
		dis.readFully(inTok, 0, inTok.length);

		do {
			byte []outTok = aCtxt.accept(inTok, 0, inTok.length);

			//check if we need to send token to peer
			if (outTok != null) {
				
				print("\tSending token to peer...");

				//MIT samples write length first, then token
				dos.writeInt(outTok.length);
				dos.write(outTok);
			}

			//are we done ??
			if (aCtxt.isEstablished())
				break;

			//the mechanism expects more tokens
			inTok = new byte[dis.readInt()];
			print("\tReceiving token from peer (" + inTok.length + " bytes)...");
			dis.readFully(inTok, 0, inTok.length);

		} while (true);

		//context is ready
		print("\tContext is fully established");

		//display context information
		displayContext(aCtxt);

		//exchange messages with peer
		exchangeWithPeer(aCtxt, dis, dos);


		//no more need for this context
		aCtxt.dispose();
		client.close();
	}


	/**
	 * Displays context informations/characteristics.
	 */
	private static void displayContext(GSSContext aCtxt) throws GSSException {


		//display context information
		print("\n\nContext Information....");
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
			print("\tReplay detraction ON");
		else
			print("\tNO replay detection");

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
	 * Performs exchange with client on a fully established context.
	 */
	private static void exchangeWithPeer(GSSContext aCtxt, DataInputStream dis,
			DataOutputStream dos) throws GSSException, IOException {

		/*
		 * We have a simple exchange with the client
		 * First it sends us a wrapped message which we
		 * unwrap, and produce a MIC on. This is then sent
		 * back to the client.
		 */
		print("\n\nPeer message exchange...");

		int len = dis.readInt();
		print("\tReceiving message from peer (" + len + " bytes)....");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MessageProp mInfo = new MessageProp();

		aCtxt.unwrap(dis, bos, mInfo);
		print("\tMessage from peer:\t" + new String(bos.toByteArray()));

		mInfo.setQOP(0);
		mInfo.setPrivacy(false);

		print("\n\tSending MIC to peer.");

		//now perform a signature on the received data
		byte []peerTok = bos.toByteArray();
		byte []toPeer = aCtxt.getMIC(peerTok, 0, peerTok.length, mInfo);

		//send token to peer
		dos.writeInt(toPeer.length);
		dos.write(toPeer);
		dos.close();

	}


	/**
	 * Display usage message.
	 */
	private static void usage() {

		print("\nUsage:\tGSSServert [-mech 1.2.34] [-port port] serviceName");
	}


	/**
	 * Utility method to display a string on the console.
	 */
	private static void print(String msg) {

		System.out.println(msg);
	}


	/**
	 * Terminates the application, performing socket cleanup.
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
	private static ServerSocket s;
}
