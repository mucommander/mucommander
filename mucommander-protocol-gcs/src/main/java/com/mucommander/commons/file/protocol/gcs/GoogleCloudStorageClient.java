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
import com.mucommander.commons.file.AuthException;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.util.List;

/**
 * TODO
 *
 * @author Arik Hadas
 */
public class GoogleCloudStorageClient implements Closeable {

    private static final List<String> SCOPES = List.of(StorageScopes.DEVSTORAGE_READ_WRITE);

    private final String projectId;
    private Storage storageService; //TODO force storageService not null!

    public GoogleCloudStorageClient(String projectId) {
        this.projectId = projectId;
    }


    public Storage getConnection() {
        return storageService;
    }

//    public static AbstractFile getCredentialsFolder() throws IOException {
//        AbstractFile credentialsFolder = PlatformManager.getCredentialsFolder().getChild("/google");
//        if (!credentialsFolder.exists())
//            credentialsFolder.mkdir();
//
//        return credentialsFolder;
//    }

    public void connect() throws AuthException {
        try {
//            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//            Credential credential = FIXME
//                    this.credential != null ? this.credential : getCredentials(HTTP_TRANSPORT, fileUrl.getHost(), null);

            storageService = StorageOptions.newBuilder()
//                    .setCredentials(getCredentials())
                    // With projectId
                    .setProjectId(projectId).build()
                    .getService();

//            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
//        } catch (GeneralSecurityException e) {
//            throw new AuthException(fileUrl);
        } catch (Exception e) {
            throw new IOError(e);
        }
    }

//    public static GoogleCredentials getCredentials(LocalServerReceiver receiver) throws IOException, GeneralSecurityException {
//        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        return getCredentials(HTTP_TRANSPORT, null, receiver);
//    }

    /**
     * Creates an authorized Credential object.
     */
    public static GoogleCredentials getCredentials() throws IOException {
        // Load client secrets. FIXME
//        Details details = new Details();
//        details.setClientId(CLIENT_ID);
//        details.setClientSecret(CLIENT_SECRET);
//        details.setAuthUri("https://accounts.google.com/o/oauth2/auth");
//        details.setTokenUri("https://oauth2.googleapis.com/token");
//        details.setRedirectUris(Arrays.asList("urn:ietf:wg:oauth:2.0:oob", "http://localhost"));
//        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setInstalled(details);

//        GoogleAuthorizationCodeFlow.Builder builder = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES);
//        if (host != null) {
//            String tokensDir = getCredentialsFolder().getAbsolutePath();
//            builder.setDataStoreFactory(
//                    tokensDir != null ? new FileDataStoreFactory(new java.io.File(tokensDir)) : null);
//        }
//        builder.setAccessType("offline");
//        GoogleAuthorizationCodeFlow flow = builder.build();
//        if (receiver == null)
//            receiver = new LocalServerReceiver();
//        return new AuthorizationCodeInstalledApp(flow, receiver, DesktopManager::browse).authorize(host);

        var impersonatedCredentials = ImpersonatedCredentials.newBuilder()
                .setSourceCredentials(GoogleCredentials.getApplicationDefault())
                .setTargetPrincipal("") //FIXME
                // With full access to permission management
                .setScopes(SCOPES)
                .build();

        // Verify impersonation
        impersonatedCredentials.refresh();

        return impersonatedCredentials;
    }

    @Override
    public void close() throws IOException {
        try {
            storageService.close();
        } catch (Exception ex) {
            // Let enclosing code to handle the close exception
            throw new IOException("Unable to close connection to project " + projectId, ex);
        }
    }
}
