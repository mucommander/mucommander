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

import com.google.api.services.storage.StorageScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ImpersonatedCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.mucommander.commons.file.FileURL;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.mucommander.commons.file.protocol.gcs.GoogleCloudStoragePanel.*;

/**
 * @author miroslav.spak
 */
public class GoogleCloudStorageClient implements Closeable {

    private static final List<String> SCOPES = List.of(StorageScopes.DEVSTORAGE_READ_WRITE);

    private final ConnectionProperties connectionProperties;
    private Storage storageService;

    public GoogleCloudStorageClient(ConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public Storage getConnection() throws IOException {
        if (storageService == null) {
            // Try to connect at first
            connect();
        }
        return storageService;
    }

    public void connect() throws IOException {
        try {
            var storageServiceBuilder = StorageOptions.newBuilder();

            // Prepare project id
            if (!connectionProperties.defaultProjectId) {
                // Set given project id
                storageServiceBuilder.setProjectId(connectionProperties.projectId);
            }

            // Prepare credentials
            GoogleCredentials credentials;
            if (connectionProperties.defaultCredentials) {
                credentials = GoogleCredentials.getApplicationDefault();
            } else {
                try (var credentialsStream = new FileInputStream(connectionProperties.credentialsJsonPath)) {
                    credentials = GoogleCredentials.fromStream(credentialsStream);
                }
            }

            // Prepare impersonation
            if (connectionProperties.impersonation) {
                var impersonatedCredentials = ImpersonatedCredentials.newBuilder()
                        .setSourceCredentials(credentials)
                        .setTargetPrincipal(connectionProperties.impersonatedPrincipal)
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

    /**
     * Properties for the GSC connection. All the properties are read from the {@link FileURL}.
     */
    static final class ConnectionProperties {
        private final String projectId;
        private final String credentialsJsonPath;
        private final String impersonatedPrincipal;
        private final boolean defaultProjectId;
        private final boolean defaultCredentials;
        private final boolean impersonation;

        ConnectionProperties(FileURL url) {
            this.projectId = url.getHost();
            this.credentialsJsonPath = url.getProperty(GCS_CREDENTIALS_JSON);
            this.impersonatedPrincipal = url.getProperty(GCS_IMPERSONATED_PRINCIPAL);
            this.defaultProjectId = Boolean.parseBoolean(url.getProperty(GCS_DEFAULT_PROJECT_ID));
            this.defaultCredentials = Boolean.parseBoolean(url.getProperty(GCS_DEFAULT_CREDENTIALS));
            this.impersonation = Boolean.parseBoolean(url.getProperty(GCS_IMPERSONATION));
        }
    }
}
