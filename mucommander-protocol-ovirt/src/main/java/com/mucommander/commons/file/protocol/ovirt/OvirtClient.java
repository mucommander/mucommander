/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

import static org.ovirt.engine.sdk4.ConnectionBuilder.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.ovirt.engine.sdk4.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.util.StringUtils;

/**
 * @author Arik Hadas
 */
public class OvirtClient implements Closeable {

    private static Logger log = LoggerFactory.getLogger(OvirtClient.class);

    private final String engine;
    private final String user;
    private final String password;
    private final Integer port;
    private final String cert;
    private String url;

    private Connection connection;

    public OvirtClient(FileURL fileUrl) {
        this.engine = fileUrl.getHost();
        this.user = fileUrl.getCredentials().getLogin();
        this.password = fileUrl.getCredentials().getPassword();
        this.port = fileUrl.getPort();
        this.cert = fileUrl.getProperty("certificate");
    }

    public Connection getConnection() {
        return connection;
    }

    public void connect() {
        String connectionUrl = getOvirtServiceUrl();
        log.info("connecting to: " + connectionUrl + ", cert: " + cert);

        connection = connection()
                .url(connectionUrl)
                .user(user)
                .password(password)
                .insecure(true)
                .build();
    }

    private String getOvirtServiceUrl() {
        if (url != null) {
            return url;
        }

        if (StringUtils.isNullOrEmpty(engine)) {
            log.warn("Can't construct ovirt service url, engine host name is empty");
            throw new IllegalArgumentException();
        }

        return url = String.format("https://%s:%s/ovirt-engine/api", engine, port);
    }

    public static URL getOvirtCrtUrl(String engine, Integer port) throws MalformedURLException {
        if (StringUtils.isNullOrEmpty(engine)) {
            log.warn("Can't construct ovirt service url, engine host name is empty");
            throw new IllegalArgumentException();
        }

        String url = String.format("https://%s:%s/ovirt-engine/services/pki-resource?resource=ca-certificate&format=X509-PEM-CA", engine, port);
        return new URL(url);
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (Exception e) {
            log.warn("failed to close connection to ovirt", e);
        } finally {
            connection = null;
        }
    }

    public boolean isConnected() {
        return connection != null;
    }
}
