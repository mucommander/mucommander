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

package com.mucommander.commons.file.protocol.onedrive;

import java.io.IOException;

import com.microsoft.graph.requests.GraphServiceClient;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;

/**
 * @author Arik Hadas
 */
public class OneDriveConnHandler extends ConnectionHandler implements AutoCloseable {

    private String account;
    private OneDriveClient client;

    public OneDriveConnHandler(FileURL serverURL) {
        super(serverURL);
        account = serverURL.getHost();
    }

    public GraphServiceClient<?> getClient() {
        return client.getClient();
    }

    @Override
    public void startConnection() throws IOException, AuthException {
        if (client == null) {
            client = new OneDriveClient(account);
            var token = client.connect();
            client.saveToken(account, token);
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
            // nothing we can do... ignore..
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
