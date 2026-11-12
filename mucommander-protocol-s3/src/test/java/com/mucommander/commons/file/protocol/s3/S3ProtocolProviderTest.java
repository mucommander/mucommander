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
package com.mucommander.commons.file.protocol.s3;

import java.util.Collections;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class S3ProtocolProviderTest {

    @Test
    public void testMissingStorageTypeDefaultsToS3() throws Exception {
        FileURL url = FileURL.getFileURL("s3://example.com/");
        url.setCredentials(new Credentials("test-access-key", "test-secret-key"));
        url.setProperty(S3File.DISABLE_DNS_BUCKETS, "false");
        url.setProperty(S3File.DEFAULT_BUCKET_LOCATION, "US");
        // Simulates a bookmarked URL with credentials re-entered and no storageType property.
        AbstractFile file = new S3ProtocolProvider().getFile(url, Collections.emptyMap());
        assertTrue(file instanceof S3Root);
    }
}
