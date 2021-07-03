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

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;

/**
 * @author Arik Hadas
 */
public class OneDriveClient implements Closeable {

    private static Logger log = LoggerFactory.getLogger(OneDriveClient.class);

    private static final String CLIENT_ID = "";
    private static final String TENANT_ID = "";
    private static final String REDIRECT_URL = "http://localhost";
    private static final String AUTHORITY_HOST = "https://login.microsoftonline.com/common/oauth2/v2.0";

    private FileURL fileUrl;
    private GraphServiceClient<?> graphClient;

    public OneDriveClient(FileURL fileUrl) {
        this.fileUrl = fileUrl;
    }

    public GraphServiceClient<?> getConnection() {
        return graphClient;
    }

    public static AbstractFile getCredentialsFolder() throws IOException {
        return null;
    }

    public void connect() throws AuthException {
        InteractiveBrowserCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder()
                .clientId(CLIENT_ID)
                .tenantId(TENANT_ID)
                .redirectUrl(REDIRECT_URL)
                .authorityHost(AUTHORITY_HOST)
                .build();
        TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(interactiveBrowserCredential);
        graphClient =
                GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredentialAuthProvider)
                .buildClient();
    }

    @Override
    public void close() throws IOException {
        // no-op
    }

    public boolean isConnected() {
        return graphClient != null;
    }
}
