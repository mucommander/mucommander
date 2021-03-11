package com.mucommander.commons.file.protocol.dropbox;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.conf.PlatformManager;

public class DropboxConnectionHandler extends ConnectionHandler implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DropboxConnectionHandler.class);

	private DbxClientV2 dbxClient;
	private FileURL fileURL;

	public DbxClientV2 getDbxClient() {
		return dbxClient;
	}

	public DropboxConnectionHandler(FileURL serverURL) {
		super(serverURL);
		this.fileURL = serverURL;
	}

	@Override
	public void startConnection() throws IOException, AuthException {
		FileURL credentialFileURL = getCredentialFileURL(fileURL.getHost());
        DbxCredential credential;
        try {
            credential = DbxCredential.Reader.readFromFile(credentialFileURL.getPath());
        }
        catch (JsonReader.FileLoadException e) {
        	LOGGER.error("failed to load credentials to dropbox", e);
        	throw new AuthException(fileURL, e.getMessage());
        }

        // Create a DbxClientV2, which is what you use to make API calls.
        DbxRequestConfig requestConfig = new DbxRequestConfig("examples-account-info");
        // Use DbxCredential to create dbx client.
        dbxClient = new DbxClientV2(requestConfig, credential);
	}

	public static FileURL getCredentialFileURL(String account) throws IOException {
		AbstractFile credentialFolder = getCredentialsFolder();
		FileURL credentialFileURL = (FileURL)credentialFolder.getURL().clone();
		credentialFileURL.setPath(credentialFolder.addTrailingSeparator(credentialFileURL.getPath()) + account);
        return credentialFileURL;
	}

	public static AbstractFile getCredentialsFolder() throws IOException {
        AbstractFile credentialsFolder = PlatformManager.getCredentialsFolder().getChild("/dropbox");
        if (!credentialsFolder.exists())
            credentialsFolder.mkdir();

        return credentialsFolder;
    }

	@Override
	public boolean isConnected() {
		return dbxClient != null;
	}

	@Override
	public void closeConnection() {
		dbxClient = null;
	}

	@Override
	public void keepAlive() {
	}

	@Override
	public void close() {
		releaseLock();
	}

}
