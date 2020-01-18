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

package com.mucommander.commons.file.protocol.ovirt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import org.ovirt.engine.sdk4.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;

/**
 *
 * @author Arik Hadas
 */
public class OvirtConnHandler extends ConnectionHandler implements AutoCloseable {

    private static Logger log = LoggerFactory.getLogger(OvirtConnHandler.class);

    public final static int STANDARD_PORT = FileURL.getRegisteredHandler("ovirt").getStandardPort();

    private FileURL location;
    private OvirtClient client;

    public Connection getConnection() throws IOException {
        return client.getConnection();
    }

    public String getCertificate() {
        return location.getProperty("certificate");
    }

    public OvirtConnHandler(FileURL serverURL) {
        super(serverURL);
        this.location = serverURL;
        if (location.getPort() < 0)
            location.setPort(STANDARD_PORT);
    }

    @Override
    public void startConnection() throws IOException, AuthException {
        if (client == null && location.getCredentials() != null) {
            URL url = OvirtClient.getOvirtCrtUrl(location.getHost(), location.getPort());
            log.debug("getting certificate from " + url);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
                String certificate = br.lines().collect(Collectors.joining(System.lineSeparator()));
                location.setProperty("certificate", certificate);
            }
            client = new OvirtClient(location);
            log.debug("connecting to " + location);
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
