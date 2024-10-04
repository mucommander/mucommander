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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.conf.PlatformManager;
import com.mucommander.core.desktop.DesktopManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Arik Hadas
 */
public class GoogleDriveClient implements Closeable {

    private static Logger log = LoggerFactory.getLogger(GoogleDriveClient.class);

    private static final String APPLICATION_NAME = "muCommander";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_FILE);
//            DriveScopes.DRIVE, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_METADATA);

    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";

    private Drive drive;
    private FileURL fileUrl;
    private Credential credential;

    public GoogleDriveClient(FileURL fileUrl) {
        this.fileUrl = fileUrl;
    }

    public GoogleDriveClient(Credential credential) {
        this.credential = credential;
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
        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = this.credential != null ? this.credential : getCredentials(HTTP_TRANSPORT, fileUrl.getHost(), null);
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        } catch (GeneralSecurityException e) {
            throw new AuthException(fileUrl);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public static Credential getCredentials(LocalServerReceiver receiver) throws IOException, GeneralSecurityException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return getCredentials(HTTP_TRANSPORT, null, receiver);
    }

    /**
     * Creates an authorized Credential object.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String host, LocalServerReceiver receiver) throws IOException {
        // Load client secrets.
        Details details = new Details();
        details.setClientId(CLIENT_ID);
        details.setClientSecret(CLIENT_SECRET);
        details.setAuthUri("https://accounts.google.com/o/oauth2/auth");
        details.setTokenUri("https://oauth2.googleapis.com/token");
        details.setRedirectUris(Arrays.asList("urn:ietf:wg:oauth:2.0:oob","http://localhost"));
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setInstalled(details);

        GoogleAuthorizationCodeFlow.Builder builder = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES);
        if (host != null) {
            String tokensDir = getCredentialsFolder().getAbsolutePath();
            builder.setDataStoreFactory(tokensDir != null ? new FileDataStoreFactory(new java.io.File(tokensDir)) : null);
        }
        builder.setAccessType("offline");
        GoogleAuthorizationCodeFlow flow = builder.build();
        if (receiver == null)
            receiver = new LocalServerReceiver();
        return new AuthorizationCodeInstalledApp(flow, receiver, DesktopManager::browse).authorize(host);
    }

    @Override
    public void close() throws IOException {
        // no-op
    }

    public boolean isConnected() {
        return drive != null;
    }
}
