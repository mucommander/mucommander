/**
 * This file is part of muCommander, http://www.mucommander.com
 * <p>
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.protocol.gcs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.conf.PlatformManager;

/**
 * @author miroslav.spak
 */
public class GoogleCloudStorageConnectionHandler extends ConnectionHandler implements AutoCloseable {

    private static final String GCS_CREDENTIALS_FOLDER = "/google_cloud_storage";
    private final FileURL fileURL;
    private GoogleCloudStorageClient client;

    public GoogleCloudStorageConnectionHandler(FileURL serverURL) {
        super(serverURL);
        this.fileURL = serverURL;
    }

    public GoogleCloudStorageClient getClient() throws IOException {
        try {
            // The connection is checked and started if needed
            checkConnection();
        } finally {
            // No need for a lock anymore
            releaseLock();
        }
        return client;
    }

    @Override
    public void startConnection() throws IOException {
        if (client == null) {
            GoogleCloudStorageConnectionProperties connectionProperties;
            try {
                // Read connection properties
                var inputPath = getCredentialFileUrl(fileURL.getHost());
                connectionProperties = GoogleCloudStorageConnectionProperties.from(Files.readString(inputPath));
            } catch (Exception ex) {
                throw new IOException("Cannot read connection properties", ex);
            }

            client = new GoogleCloudStorageClient(connectionProperties);
            client.connect();
        }
    }

    /**
     * Finds url path of the credentials file for the given project id.
     */
    public static Path getCredentialFileUrl(String projectId) throws IOException {
        var credentialsFolder = PlatformManager.getCredentialsFolder().getChild(GCS_CREDENTIALS_FOLDER);
        if (!credentialsFolder.exists()) {
            credentialsFolder.mkdir();
        }

        // Return path to the child with project id name
        return Paths.get(credentialsFolder.getChild(projectId).getPath());
    }

    @Override
    public boolean isConnected() {
        return client != null;
    }

    @Override
    public void closeConnection() {
        try {
            client.close();
        } catch (IOException e) {
            // noop
        } finally {
            client = null;
        }
    }

    @Override
    public void keepAlive() {
        // do nothing
    }

    @Override
    public void close() {
        releaseLock();
    }
}
