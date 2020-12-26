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

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.conf.PlatformManager;

/**
 * @author Arik Hadas
 */
public class GoogleDriveClient implements Closeable {

    private static Logger log = LoggerFactory.getLogger(GoogleDriveClient.class);

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
//            DriveScopes.DRIVE, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_METADATA);

    private Drive drive;
    private FileURL fileUrl;

    public GoogleDriveClient(FileURL fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Drive getConnection() {
        return drive;
    }

    public static AbstractFile getCredentialsFolder() throws IOException {
        AbstractFile credentialsFolder = PlatformManager.getCredentialsFolder().getChild("/google");
        if (!credentialsFolder.exists())
            credentialsFolder.mkdir();

        return credentialsFolder;
    }

    public void connect() throws AuthException {
        // Build a new authorized API client service.
        NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            throw new AuthException(fileUrl);
        } catch (IOException e) {
            throw new IOError(e);
        }
        try {
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, fileUrl.getHost()))
                    .build();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String host) throws IOException {
        // Load client secrets.
        Details details = new Details();
        details.setClientId(BuildConfig.CLIENT_ID);
        details.setClientSecret(BuildConfig.CLIENT_SECRET);
        details.setAuthUri("https://accounts.google.com/o/oauth2/auth");
        details.setTokenUri("https://oauth2.googleapis.com/token");
        details.setRedirectUris(Arrays.asList("urn:ietf:wg:oauth:2.0:oob","http://localhost"));
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setInstalled(details);

        String tokensDir = getCredentialsFolder().getAbsolutePath(true) + host;
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDir)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Override
    public void close() throws IOException {
        // no-op
    }

    public boolean isConnected() {
        return drive != null;
    }
}
