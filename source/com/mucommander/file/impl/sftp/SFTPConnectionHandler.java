/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.sftp;

import com.mucommander.Debug;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.file.FileURL;
import com.mucommander.file.connection.ConnectionHandler;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.*;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import java.io.IOException;
import java.util.List;

/**
 * Handles connections to SFTP servers.
 *
 * @author Maxence Bernard
 */
class SFTPConnectionHandler extends ConnectionHandler {

    SshClient sshClient;
    SftpClient sftpClient;
    SftpSubsystemClient sftpSubsystem;

    /** 'Password' SSH authentication method */
    private final static String PASSWORD_AUTH_METHOD = "password";

    /** 'Keyboard interactive' SSH authentication method */
    private final static String KEYBOARD_INTERACTIVE_AUTH_METHOD = "keyboard-interactive";

    /** 'Public key' SSH authentication method, not supported at the moment */
    private final static String PUBLIC_KEY_AUTH_METHOD = "publickey";


    SFTPConnectionHandler(FileURL location) {
        super(location);
    }


    //////////////////////////////////////
    // ConnectionHandler implementation //
    //////////////////////////////////////

    public synchronized void startConnection() throws IOException {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starting connection to "+realm);
        try {
            FileURL realm = getRealm();

            // Retrieve credentials to be used to authenticate
            final Credentials credentials = getCredentials();

            // Throw an AuthException if no auth information, required for SSH
            if(credentials ==null)
                throw new AuthException(realm, "Login and password required");  // Todo: localize this entry

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating SshClient");

            // Init SSH client
            sshClient = new SshClient();

            // Override default port (22) if a custom port was specified in the URL
            int port = realm.getPort();
            if(port==-1)
                port = 22;

            // Connect to server, no host key verification
            sshClient.connect(realm.getHost(), port, new IgnoreHostKeyVerification());

            // Retrieve a list of available authentication methods on the server.
            // Some SSH servers support the 'password' auth method (e.g. OpenSSH on Debian unstable), some don't
            // and only support the 'keyboard-interactive' method.
            List authMethods = sshClient.getAvailableAuthMethods(credentials.getLogin());
            if(authMethods==null)   // this can happen
                throw new IOException();

            if(Debug.ON) Debug.trace("getAvailableAuthMethods()="+sshClient.getAvailableAuthMethods(credentials.getLogin()));

            SshAuthenticationClient authClient;

            // Use 'keyboard-interactive' method only if 'password' auth method is not available and
            // 'keyboard-interactive' is supported by the server
            if(!authMethods.contains(PASSWORD_AUTH_METHOD) && authMethods.contains(KEYBOARD_INTERACTIVE_AUTH_METHOD)) {
                if(Debug.ON) Debug.trace("Using "+KEYBOARD_INTERACTIVE_AUTH_METHOD+" authentication method");

                KBIAuthenticationClient kbi = new KBIAuthenticationClient();
                kbi.setUsername(credentials.getLogin());

                // Fake keyboard password input
                kbi.setKBIRequestHandler(new KBIRequestHandler() {
                    public void showPrompts(String name, String instruction, KBIPrompt[] prompts) {
                        // Workaround for what seems to be a bug in J2SSH: this method is called twice, first time
                        // with a valid KBIPrompt array, second time with null
                        if(prompts==null) {
                            if(Debug.ON) Debug.trace("prompts is null!");
                            return;
                        }

                        for(int i=0; i<prompts.length; i++) {
                            if(Debug.ON) Debug.trace("prompts["+i+"]="+prompts[i].getPrompt());
                            prompts[i].setResponse(credentials.getPassword());
                        }
                    }
                });

                authClient = kbi;
            }
            // Default to 'password' method, even if server didn't report as being supported
            else {
                if(Debug.ON) Debug.trace("Using "+PASSWORD_AUTH_METHOD+" authentication method");

                PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
                pwd.setUsername(credentials.getLogin());
                pwd.setPassword(credentials.getPassword());

                authClient = pwd;
            }

            try {
                int authResult = sshClient.authenticate(authClient);

                // Throw an AuthException if authentication failed
                if(authResult!=AuthenticationProtocolState.COMPLETE)
                    throw new AuthException(realm, "Login or password rejected");   // Todo: localize this entry

                if(Debug.ON) Debug.trace("authentication complete, authResult="+authResult);
            }
            catch(IOException e) {
                if(e instanceof AuthException)
                    throw e;

                if(Debug.ON) {
                    Debug.trace("Caught exception while authenticating: "+e);
                    e.printStackTrace();
                    throw new AuthException(realm, e.getMessage());
                }
            }


            // Init SFTP connections
            sftpClient = sshClient.openSftpClient();
            sftpSubsystem = sshClient.openSftpChannel();
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON)
                com.mucommander.Debug.trace("IOException thrown while starting connection: "+e);

            // Disconnect if something went wrong
            if(sshClient!=null && sshClient.isConnected())
                sshClient.disconnect();

            sshClient = null;
            sftpClient = null;
            sftpSubsystem = null;

            // Re-throw exception
            throw e;
        }
    }


    public synchronized boolean isConnected() {
        return sshClient!=null && sshClient.isConnected()
            && sftpClient!=null && !sftpClient.isClosed()
            && sftpSubsystem !=null && !sftpSubsystem.isClosed();
    }


    public synchronized void closeConnection() {
        if(sftpClient!=null) {
            try { sftpClient.quit(); }
            catch(IOException e) { if(Debug.ON) Debug.trace("IOException thrown while calling sftpClient.quit()"); }
        }

        if(sftpSubsystem !=null) {
            try { sftpSubsystem.close(); }
            catch(IOException e) { if(Debug.ON) Debug.trace("IOException thrown while calling sftpChannel.close ()"); }
        }

        if(sshClient!=null)
            sshClient.disconnect();
    }


    public void keepAlive() {
        // No-op, keep alive is not available and shouldn't really be necessary, SSH servers such as OpenSSH usually
        // maintain connections open without limit.
    }
}
