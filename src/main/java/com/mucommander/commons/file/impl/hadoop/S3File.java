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


package com.mucommander.commons.file.impl.hadoop;

import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;

/**
 * {@link HadoopFile} implementation for the Amazon S3 protocol.
 *
 * <p>Even though it is working for the most part, it is flawed in several ways and should not be used.
 * See the {@link com.mucommander.commons.file.impl.s3} package for a better implementation of the Amazon S3 protocol.</p>
 *
 * @deprecated
 * @author Maxence Bernard
 */
public class S3File extends HadoopFile {

    protected S3File(FileURL url) throws IOException {
        super(url);
    }

    protected S3File(FileURL url, FileSystem fs, FileStatus fileStatus) throws IOException {
        super(url, fs, fileStatus);
    }


    ///////////////////////////////
    // HadoopFile implementation //
    ///////////////////////////////

    @Override
    protected FileSystem getHadoopFileSystem(FileURL url) throws IOException {
        if(!url.containsCredentials())
            throw new AuthException(url);

        // Note: getRealm returns a fresh instance every time
        FileURL realm = url.getRealm();

        // Import credentials
        Credentials creds = url.getCredentials();
        if(creds!=null) {
            // URL-encode secret as it may contain non URL-safe characters ('+' and '/')
            realm.setCredentials(new Credentials(creds.getLogin(), URLEncoder.encode(creds.getPassword(), "UTF-8")));
        }

        // Change the scheme to the actual Hadoop fileystem (s3 -> s3n)
        realm.setScheme("s3n");

        return FileSystem.get(URI.create(realm.toString(true, false)), DEFAULT_CONFIGURATION);
    }

    @Override
    protected void setDefaultFileAttributes(FileURL url, HadoopFileAttributes atts) {
        // Implemented as a no-op (S3 has no user info)
    }
}
