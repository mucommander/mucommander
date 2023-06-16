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

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;

import java.io.IOException;

import static com.mucommander.commons.file.protocol.gcs.GoogleCloudStorageClient.ConnectionProperties;

/**
 * TODO
 *
 * @author Arik Hadas
 */
public class GoogleCloudStorageConnectionHandler extends ConnectionHandler implements AutoCloseable {

    private final ConnectionProperties connectionProperties;
    private GoogleCloudStorageClient client;

    public GoogleCloudStorageConnectionHandler(FileURL serverURL) {
        super(serverURL);
        // Read all  properties from url
        this.connectionProperties = new ConnectionProperties(serverURL);
    }

    public GoogleCloudStorageClient getClient() throws IOException {
        checkConnection();
        return client;
    }

    @Override
    public void startConnection() throws IOException {
        if (client == null) {
            client = new GoogleCloudStorageClient(connectionProperties);
            client.connect();
        }
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
