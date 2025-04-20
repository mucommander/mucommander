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


package com.mucommander.commons.file.protocol.smb;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.Share;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SmbjConnectionHandler extends ConnectionHandler  {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmbjConnectionHandler.class);

    private final FileURL serverURL;

    private SMBClient client;
    private Connection connection;
    private Session session;
    private DiskShare diskShare;

    public SmbjConnectionHandler(FileURL serverURL) {
        super(serverURL);
        this.serverURL = serverURL;
    }

    @Override
    public void startConnection() throws IOException, AuthException {
        Credentials credentials = serverURL.getCredentials();
        String login = credentials.getLogin();
        String domain;
        int domainStart = login.indexOf(";");
        if(domainStart!=-1) {
            domain = login.substring(0, domainStart);
            login = login.substring(domainStart + 1);
        }
        else {
            domain = null;
        }
        String password = credentials.getPassword();

        String hostname = serverURL.getHost();

        AuthenticationContext authenticationContext = new AuthenticationContext(login, password.toCharArray(), domain);

        SmbConfig smbConfig = SmbConfig.builder()
                .withEncryptData(true)
                .build();

        client = new SMBClient(smbConfig);
        connection = client.connect(serverURL.getHost());
        session = connection.authenticate(authenticationContext);

        // Strip first and last slash
        String shareName = serverURL.getRealm().getPath().substring(1);
        if (shareName.endsWith("/")) {
            shareName = shareName.substring(0, shareName.length() - 1);
        }

        Share share = session.connectShare(shareName);

        if (!(share instanceof DiskShare)) {
            share.close();
            throw new IOException(String.format("Error connecting to SMB: %s", hostname));
        }

        diskShare = (DiskShare) share;
    }

    @Override
    public boolean isConnected() {
        if (diskShare != null) {
            return diskShare.isConnected();
        } else {
            return false;
        }
    }

    @Override
    public void closeConnection() {
        List<AutoCloseable> resources = Arrays.asList(client, diskShare, connection, session);
        for (AutoCloseable closeable : resources) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception e) {
                LOGGER.error("Error closing smbj connection", e);
            }

        }
        releaseLock();
    }

    @Override
    public void keepAlive() {
        if (diskShare != null) {
            diskShare.getFileInformation("");
        }
    }

    public DiskShare getDiskShare() {
        return this.diskShare;
    }

}
