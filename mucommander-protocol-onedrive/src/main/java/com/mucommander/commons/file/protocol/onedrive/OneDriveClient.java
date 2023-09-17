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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.graph.requests.GraphServiceClient;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.conf.PlatformManager;

/**
 * @author Arik Hadas
 */
public class OneDriveClient implements Closeable {
    private static Logger LOGGER = LoggerFactory.getLogger(OneDriveClient.class);

    private static final String CLIENT_ID = "";
    private static final String TENANT_ID = "";
    private static final String REDIRECT_URL = "http://localhost";
    private static final String AUTHORITY_HOST = "https://login.microsoftonline.com/common/oauth2/v2.0";
    private static final Set<String> SCOPE = new HashSet<>(Arrays.asList("User.Read", "Files.ReadWrite"));

    private GraphServiceClient<?> graphClient;
    private String account;
    private String token;

    public OneDriveClient(String account) {
        this.account = account;
    }

    public GraphServiceClient<?> getClient() {
        return graphClient;
    }

    public static AbstractFile getCredentialsFolder() throws IOException {
        AbstractFile credentialsFolder = PlatformManager.getCredentialsFolder().getChild("/microsoft");
        if (!credentialsFolder.exists())
            credentialsFolder.mkdir();

        return credentialsFolder;
    }

    private void loadToken(String account) {
        if (account != null) {
            File credentialsFile = getCredentualsFile(account);
            try {
                token = Files.readString(credentialsFile.toPath());
            } catch (IOException e1) {
                LOGGER.error("failed to load token for: " + account);
            }
        }
    }

    void saveToken(String accountName, String token) {
        File f = getCredentualsFile(accountName);
        try {
            Files.writeString(f.toPath(), token);
        } catch (Exception e) {
            LOGGER.error("failed to save AuthenticationRecord");
        }
    }

    private File getCredentualsFile(String filename) {
        try {
            return new File(getCredentialsFolder().getAbsolutePath(true) + filename);
        } catch (IOException e) {
            LOGGER.info("failed to load AuthenticationRecord file");
            return null;
        }
    }

    public String connect() throws AuthException, MalformedURLException {
        loadToken(account);
        var pca = PublicClientApplication.builder(CLIENT_ID)
                .authority(AUTHORITY_HOST)
                .setTokenCacheAccessAspect(new ITokenCacheAccessAspect() {
                    @Override
                    public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
                            iTokenCacheAccessContext.tokenCache().deserialize(token);
                    }

                    @Override
                    public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
                            token = iTokenCacheAccessContext.tokenCache().serialize();
                    }
                })
                .build();

        IAuthenticationResult result;
        var accounts = pca.getAccounts().join();
        try {
            var account = accounts.iterator().next();
            SilentParameters silentParameters = SilentParameters
                    .builder(SCOPE, account)
                    .build();
            result = pca.acquireTokenSilently(silentParameters).join();
        } catch (Exception ex) {
                InteractiveRequestParameters parameters;
                try {
                    parameters = InteractiveRequestParameters
                            .builder(new URI(REDIRECT_URL))
                            .scopes(SCOPE)
                            .tenant(TENANT_ID)
                            .build();
                } catch (URISyntaxException e) {
                    LOGGER.error("failed to authenticate interactively", e);
                    throw new AuthException(FileURL.getFileURL("onedrive://"+account));
                }

                result = pca.acquireToken(parameters).join();
        }

        String accessToken = result.accessToken();
        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(requestURL -> {
                    CompletableFuture<String> alreadyThere = new CompletableFuture<>();
                    alreadyThere.complete(accessToken);
                    return alreadyThere;
                })
                .buildClient();

        return token;
    }

    @Override
    public void close() throws IOException {
        // no-op
    }

    public boolean isConnected() {
        return graphClient != null;
    }
}
