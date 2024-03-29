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

import java.util.Map;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.ProtocolProvider;
import com.mucommander.commons.util.StringUtils;

/**
 * An implementation of protocol provider that differentiates among all three GCS file/folder types. The provider
 * instantiates the right type according to their path.
 *
 * @author miroslav.spak
 */
public class GoogleCloudStorageProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) {
        var parent = url.getParent();

        if (parent == null) {
            // Only root has no parent
            return new GoogleCloudStorageRoot(url);
        }

        if (StringUtils.isNullOrEmpty(parent.getFilename())) {
            // Parent of the bucket is only the schema and host i.e. has no filename
            return new GoogleCloudStorageBucket(url);
        }

        return new GoogleCloudStorageFile(url);
    }
}
