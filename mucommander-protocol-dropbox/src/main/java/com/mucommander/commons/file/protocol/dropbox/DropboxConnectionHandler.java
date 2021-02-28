package com.mucommander.commons.file.protocol.dropbox;

import java.io.IOException;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;

public class DropboxConnectionHandler extends ConnectionHandler implements AutoCloseable {

	private DbxClientV2 dbxClient;
	
	public DbxClientV2 getDbxClient() {
		return dbxClient;
	}

	public DropboxConnectionHandler(FileURL serverURL) {
		super(serverURL);
	}

	@Override
	public void startConnection() throws IOException, AuthException {
		 // Use DbxCredential instead of DbxAuthInfo.
        DbxCredential credential;
        try {
            credential = DbxCredential.Reader.readFromFile("/tmp/arik.txt");
        }
        catch (JsonReader.FileLoadException ex) {
            System.exit(1); return;
        }

        // Create a DbxClientV2, which is what you use to make API calls.
        DbxRequestConfig requestConfig = new DbxRequestConfig("examples-account-info");
        // Use DbxCredential to create dbx client.
        dbxClient = new DbxClientV2(requestConfig, credential);
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
