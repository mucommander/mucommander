/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.hadoop;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.file.ProtocolProvider;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

/**
 * A file protocol provider for the Amazon S3 protocol, provided by the Hadoop virtual filesystem.
 *
 * <p>Even though it is working for the most part, it is flawed in several ways and should not be used.
 * See the {@link com.mucommander.file.impl.s3} package for a better implementation of the Amazon S3 protocol.</p>
 *
 * @deprecated  
 * @author Maxence Bernard
 */
public class S3ProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return instantiationParams.length==0
            ?new S3File(url)
            :new S3File(url, (FileSystem)instantiationParams[0], (FileStatus)instantiationParams[1]);
    }
}
