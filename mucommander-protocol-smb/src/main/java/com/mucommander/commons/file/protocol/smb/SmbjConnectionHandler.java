package com.mucommander.commons.file.protocol.smb;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SmbjConnectionHandler extends ConnectionHandler  {

    private final FileURL serverURL;

    private SMBClient client = null;
    private Connection connection = null;
    private Session session = null;
    private DiskShare diskShare = null;

    public SmbjConnectionHandler(FileURL serverURL) {
        super(serverURL);
        this.serverURL = serverURL;
    }

    @Override
    public void startConnection() throws IOException, AuthException {
        System.out.println("startConnection"); // TODO - remove

        // TODO - this should be PER SHARE, not PER FILE!

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
        System.out.println(hostname); // TODO

        // SmbjFile smbjFile = new SmbjFile(url);
        AuthenticationContext authenticationContext = new AuthenticationContext(login, password.toCharArray(), domain);

        SmbConfig smbConfig = SmbConfig.builder()
                .withEncryptData(true)
                .build();

        client = new SMBClient(smbConfig);
        connection = client.connect(serverURL.getHost());
        session = connection.authenticate(authenticationContext);
        Share share = session.connectShare(serverURL.getPath().substring(1));

        if (!(share instanceof DiskShare)) {
            share.close();
            throw new IOException(String.format("Error connecting to SMB: %s", hostname));
        }

        diskShare = (DiskShare) share;
    }

    @Override
    public boolean isConnected() {
        System.out.println("isConnected");

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
                e.printStackTrace(); // TODO - log?
            }

        }
    }

    @Override
    public void keepAlive() {
        System.out.println("keepAlive");
        if (diskShare != null) {
            FileAllInformation fileInformation = diskShare.getFileInformation("");
            System.out.println(fileInformation); // TODO - remove
        }
    }

    public DiskShare getDiskShare() {
        return this.diskShare;
    }

}
