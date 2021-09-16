/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file.protocol.sftp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;

/**
 * Handles connections to SFTP servers.
 *
 * @author Arik Hadas, Maxence Bernard, Vassil Dichev
 */
class SFTPConnectionHandler extends ConnectionHandler implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SFTPConnectionHandler.class);

    Session session;
    ChannelSftp channelSftp;

    /** 'Public key' SSH authentication method, not supported at the moment */
    private final static String PUBLIC_KEY_AUTH_METHOD = "publickey";


    SFTPConnectionHandler(FileURL location) {
        super(location);
    }


    //////////////////////////////////////
    // ConnectionHandler implementation //
    //////////////////////////////////////

    @Override
    public void startConnection() throws IOException {
        LOGGER.info("starting connection to {}", realm);
        try {
            FileURL realm = getRealm();

            // Retrieve credentials to be used to authenticate
            final Credentials credentials = getCredentials();

            // Throw an AuthException if no auth information, required for SSH
            if(credentials ==null)
                throwAuthException("Login and password required");  // Todo: localize this entry

            LOGGER.trace("creating SshClient");

            JSch jsch = new JSch();

            // Override default port (22) if a custom port was specified in the URL
            int port = realm.getPort();
            if(port==-1)
                port = 22;

            String privateKeyPath = realm.getProperty(SFTPFile.PRIVATE_KEY_PATH_PROPERTY_NAME);
            if (privateKeyPath != null) {
                LOGGER.info("Using {} authentication method", PUBLIC_KEY_AUTH_METHOD);
                jsch.addIdentity(privateKeyPath);
            }

            session = jsch.getSession(credentials.getLogin(), realm.getHost(), port);
            session.setUserInfo(new PasswordAuthentication());

            session.connect(5*1000);
            // Init SFTP connections
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect(5*1000);
            LOGGER.info("authentication complete");
        }
        catch(IOException e) {
            LOGGER.info("IOException thrown while starting connection", e);

            // Disconnect if something went wrong
            if(session!=null && session.isConnected())
            	session.disconnect();;

            session = null;
            channelSftp = null;

            // Re-throw exception
            throw e;
		} catch (JSchException e) {
			LOGGER.info("Caught exception while authenticating: {}", e.getMessage());
			LOGGER.debug("Exception:", e);
            throwAuthException(e.getMessage());
		}
    }


    @Override
    public synchronized boolean isConnected() {
        return session!=null && session.isConnected()
            && channelSftp!=null && !channelSftp.isClosed();
    }


    @Override
    public synchronized void closeConnection() {
        if(channelSftp!=null) {
            channelSftp.quit();
        }

        if(session!=null)
        	session.disconnect();
    }


    @Override
    public void keepAlive() {
        // No-op, keep alive is not available and shouldn't really be necessary, SSH servers such as OpenSSH usually
        // maintain connections open without limit.
    }

    private class PasswordAuthentication implements UserInfo, UIKeyboardInteractive {

    	@Override
		public void showMessage(String message) {
		}
		
		@Override
		public boolean promptYesNo(String message) {
			return true;
		}
		
		@Override
		public boolean promptPassword(String message) {
			return true;
		}
		
		@Override
		public boolean promptPassphrase(String message) {
			return true;
		}
		
		@Override
		public String getPassword() {
			return credentials.getPassword();
		}
		
		@Override
		public String getPassphrase() {
			return credentials.getPassword();
		}

        @Override
        public String[] promptKeyboardInteractive(String destination,
                String name,
                String instruction,
                String[] prompt,
                boolean[] echo) {
            String[] result = new String[prompt.length];
            for (int i=0; i<echo.length; i++)
                result[i] = echo[i] ? credentials.getLogin() : credentials.getPassword();
            return result;
        }
    }

    @Override
    public void close() throws Exception {
        releaseLock();
    }
}
