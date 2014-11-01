/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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


package com.mucommander.commons.file.impl.http;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * This class is the provider for the HTTP/HTTPS filesystem implemented by {@link com.mucommander.commons.file.impl.http.HTTPFile}.
 *
 * @author Nicolas Rinaudo
 * @see com.mucommander.commons.file.impl.http.HTTPFile
 */
public class HTTPProtocolProvider implements ProtocolProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPProtocolProvider.class);

    static {
        try {
            disableCertificateVerifications();
        }
        catch(Exception e) {
            LOGGER.info("Failed to install a custom TrustManager", e);
        }
    }

    /**
	 * Installs a custom <code>javax.net.ssl.X509TrustManager</code> and <code>javax.net.ssl.HostnameVerifier</code>
     * to bypass the default SSL certificate verifications and blindly trust all SSL certificates, even if they are
     * self-signed, expired, or do not match the requested hostname.
     * As a result in such cases, <code>HttpsURLConnection#openConnection()</code> will succeed instead of throwing a
     * <code>javax.net.ssl.SSLException</code>.
     *
     * <p>This method needs to be called only once in the JVM lifetime and will impact all HTTPS connections made,
     * i.e. not only the ones made by this class.</p>
     *
     * <p>This clearly is unsecure for the user, but arguably better from a feature standpoint than systematically
     * failing untrusted connections.</p>
     *
     * @throws Exception if an error occurred while installing the custom X509TrustManager.
	 */
	private static void disableCertificateVerifications() throws Exception {
        // Todo: find a way to warn the user when the server cannot be trusted

        // Create a custom X509 trust manager that does not validate certificate chains
        TrustManager permissiveTrustManager = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            }
        };

        // Install the permissive trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{permissiveTrustManager}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create and install a custom hostname verifier that allows hostname mismatches
        HostnameVerifier permissiveHostnameVerifier = new HostnameVerifier() {
           public boolean verify(String urlHostName, SSLSession session) {
               return true;
           }

        };
       HttpsURLConnection.setDefaultHostnameVerifier(permissiveHostnameVerifier);
    }
    
    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return instantiationParams.length==0
            ?new HTTPFile(url)
            :new HTTPFile(url, (URL)instantiationParams[0]);
    }
}
