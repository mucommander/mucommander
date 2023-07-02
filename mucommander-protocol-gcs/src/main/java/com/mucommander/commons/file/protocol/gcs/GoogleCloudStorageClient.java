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

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.google.api.services.storage.StorageScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ImpersonatedCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.mucommander.commons.util.StringUtils;

/**
 * @author miroslav.spak
 */
public class GoogleCloudStorageClient implements Closeable {

    private static final List<String> SCOPES = List.of(StorageScopes.DEVSTORAGE_READ_WRITE);

    private final GoogleCloudStorageConnectionProperties connectionProperties;
    private Storage storageService;

    public GoogleCloudStorageClient(GoogleCloudStorageConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public Storage getConnection() throws IOException {
        if (storageService == null) {
            // Try to connect at first
            connect();
        }
        return storageService;
    }

    public GoogleCloudStorageConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    public void connect() throws IOException {
        try {
            var storageServiceBuilder = StorageOptions.newBuilder();

            // Prepare project id
            if (!connectionProperties.isDefaultProjectId()) {
                if (StringUtils.isNullOrEmpty(connectionProperties.getProjectId())) {
                    throw new IllegalStateException("Missing project id");
                }

                // Set given project id
                storageServiceBuilder.setProjectId(connectionProperties.getProjectId());
            }

            // Prepare credentials
            GoogleCredentials credentials;
            if (connectionProperties.isDefaultCredentials()) {
                credentials = GoogleCredentials.getApplicationDefault();
            } else {
                if (StringUtils.isNullOrEmpty(connectionProperties.getCredentialsJsonPath())) {
                    throw new IllegalStateException("Missing credentials JSON for the project");
                }

                try (var credentialsStream = new FileInputStream(connectionProperties.getCredentialsJsonPath())) {
                    credentials = GoogleCredentials.fromStream(credentialsStream);
                }
            }

            // Prepare impersonation
            if (connectionProperties.isImpersonation()) {
                var impersonatedCredentials = ImpersonatedCredentials.newBuilder()
                        .setSourceCredentials(credentials)
                        .setTargetPrincipal(connectionProperties.getImpersonatedPrincipal())
                        // With R/W permissions
                        .setScopes(SCOPES)
                        .build();

                // Verify impersonation
                impersonatedCredentials.refresh();

                // Use impersonated credentials
                credentials = impersonatedCredentials;
            }

            // Build service
            storageService = storageServiceBuilder.setCredentials(credentials).build().getService();

        } catch (Exception e) {
            throw new IOException("Unable to connect to the storage service with config " + connectionProperties, e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            storageService.close();
        } catch (Exception e) {
            // Let enclosing code to handle the close exception
            throw new IOException("Unable to close connection to project with config " + connectionProperties, e);
        }
    }
}
