/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2018 Maxence Bernard
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


package com.mucommander.commons.file.protocol.s3;

import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3Service;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.ProtocolProvider;

/**
 * A file protocol provider for the Amazon S3 protocol.
 *
 * @author Maxence Bernard
 */
public class S3ProtocolProvider implements ProtocolProvider {
    public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) throws IOException {
        Credentials credentials = url.getCredentials();
        if(credentials==null || credentials.getLogin().equals("") || credentials.getPassword().equals(""))
            throw new AuthException(url);

        S3Service service;
        String bucketName;

        if(instantiationParams.isEmpty()) {
            Jets3tProperties props = new Jets3tProperties();
            props.setProperty("s3service.s3-endpoint", url.getHost());
            service = new RestS3Service(new AWSCredentials(credentials.getLogin(), credentials.getPassword()), null, null, props);
        }
        else {
            service = (S3Service)instantiationParams.get("service");
        }

        String path = url.getPath();

        // Root resource
        if(("/").equals(path))
            return new S3Root(url, service);

        // Fetch the bucket name from the URL
        StringTokenizer st = new StringTokenizer(path, "/");
        bucketName = st.nextToken();

        // Object resource
        if (st.hasMoreTokens()) {
            org.jets3t.service.model.S3Object obj = (org.jets3t.service.model.S3Object) instantiationParams.get("object");
            if (obj != null)
                return new S3Object(url, service, bucketName, obj);

            return new S3Object(url, service, bucketName);
        }

        // Bucket resource
        org.jets3t.service.model.S3Bucket bucket = (org.jets3t.service.model.S3Bucket) instantiationParams.get("bucket");
        if (bucket != null)
            return new S3Bucket(url, service, bucket);

        return new S3Bucket(url, service, bucketName);
    }
}
