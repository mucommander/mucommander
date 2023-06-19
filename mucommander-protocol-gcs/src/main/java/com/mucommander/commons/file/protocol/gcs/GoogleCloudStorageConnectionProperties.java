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

import com.google.gson.Gson;

/**
 * Properties for the GSC connection. The properties are read from {@link GoogleCloudStoragePanel} or from credentials
 * json file.
 *
 * @author miroslav.spak
 */
final class GoogleCloudStorageConnectionProperties {
    private static final Gson jsonMapper = new Gson();
    private String projectId;
    private String credentialsJsonPath;
    private String impersonatedPrincipal;
    private String location;
    private boolean defaultProjectId;
    private boolean defaultCredentials;
    private boolean impersonation;
    private boolean defaultLocation;

    @SuppressWarnings("unused")
    public GoogleCloudStorageConnectionProperties() {
        // No args constructor for Gson library
    }

    GoogleCloudStorageConnectionProperties(
            String projectId,
            String credentialsJsonPath,
            String impersonatedPrincipal,
            String location,
            boolean defaultProjectId,
            boolean defaultCredentials,
            boolean impersonation,
            boolean defaultLocation
    ) {
        this.projectId = projectId;
        this.credentialsJsonPath = credentialsJsonPath;
        this.impersonatedPrincipal = impersonatedPrincipal;
        this.location = location;
        this.defaultProjectId = defaultProjectId;
        this.defaultCredentials = defaultCredentials;
        this.impersonation = impersonation;
        this.defaultLocation = defaultLocation;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getCredentialsJsonPath() {
        return credentialsJsonPath;
    }

    public String getImpersonatedPrincipal() {
        return impersonatedPrincipal;
    }

    public String getLocation() {
        return location;
    }

    public boolean isDefaultProjectId() {
        return defaultProjectId;
    }

    public boolean isDefaultCredentials() {
        return defaultCredentials;
    }

    public boolean isImpersonation() {
        return impersonation;
    }

    public boolean isDefaultLocation() {
        return defaultLocation;
    }

    /**
     * Transform the instance of {@link GoogleCloudStorageConnectionProperties} to the JSON representation.
     */
    public String toJson() {
        return jsonMapper.toJson(this);
    }

    /**
     * Reads instance of {@link GoogleCloudStorageConnectionProperties} from the JSON string.
     */
    public static GoogleCloudStorageConnectionProperties from(String json) {
        return jsonMapper.fromJson(json, GoogleCloudStorageConnectionProperties.class);
    }

    @Override
    public String toString() {
        return toJson();
    }
}