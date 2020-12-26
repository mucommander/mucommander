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

package com.mucommander.commons.file.protocol.gdrive;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.drive.Drive;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;

/**
 *
 * @author Arik Hadas
 */
public class GoogleDriveConnHandler extends ConnectionHandler implements AutoCloseable {

    private static Logger log = LoggerFactory.getLogger(GoogleDriveConnHandler.class);


    private FileURL location;
    private GoogleDriveClient client;

    public String getCertificate() {
        return location.getProperty("certificate");
    }

    public GoogleDriveConnHandler(FileURL serverURL) {
        super(serverURL);
        this.location = serverURL;
    }

    public Drive getConnection() throws IOException {
        return client.getConnection();
    }

    @Override
    public void startConnection() throws IOException, AuthException {
        if (client == null) {
            client = new GoogleDriveClient(location);
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
