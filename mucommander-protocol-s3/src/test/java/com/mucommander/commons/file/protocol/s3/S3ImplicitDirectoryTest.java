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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.StorageObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.FileProtocols;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests directories that exist only as a common prefix of other objects, i.e. without a "dir/" placeholder object.
 * Most S3 tools create directories this way. Runs against an in-memory service, no S3 server is needed.
 */
public class S3ImplicitDirectoryTest {

    /** A service that keeps object keys in memory and implements the requests used by S3Object and S3File. */
    private static class InMemoryS3Service extends RestS3Service {
        private final SortedSet<String> keys = new TreeSet<>();

        InMemoryS3Service(String... keys) {
            super(null);
            this.keys.addAll(Arrays.asList(keys));
        }

        final List<String> deleted = new ArrayList<>();

        @Override
        public void deleteObject(String bucketName, String objectKey) {
            deleted.add(objectKey);
            keys.remove(objectKey);
        }

        private static StorageObject newObject(String key) {
            org.jets3t.service.model.S3Object object = new org.jets3t.service.model.S3Object(key);
            object.setContentLength(key.endsWith("/") ? 0 : 1);
            object.setLastModifiedDate(new Date(0));
            return object;
        }

        @Override
        public StorageObject getObjectDetails(String bucketName, String objectKey) throws ServiceException {
            if (!keys.contains(objectKey)) {
                ServiceException e = new ServiceException("Not Found");
                e.setResponseCode(404);
                throw e;
            }
            return newObject(objectKey);
        }

        @Override
        public StorageObjectsChunk listObjectsChunked(String bucketName, String prefix, String delimiter,
                long maxListingLength, String priorLastKey, boolean completeListing) throws ServiceException {
            List<StorageObject> objects = new ArrayList<>();
            SortedSet<String> commonPrefixes = new TreeSet<>();
            for (String key : keys) {
                if (!key.startsWith(prefix))
                    continue;
                // Like S3: keys with a delimiter after the prefix are rolled up into a common prefix
                int index = key.indexOf(delimiter, prefix.length());
                if (index >= 0)
                    commonPrefixes.add(key.substring(0, index + 1));
                else
                    objects.add(newObject(key));
            }
            return new StorageObjectsChunk(prefix, delimiter, objects.toArray(new StorageObject[0]),
                    commonPrefixes.toArray(new String[0]), null);
        }
    }

    /** A service whose listings are refused, as with a key that may not list the bucket. */
    private static class ListForbiddenS3Service extends InMemoryS3Service {
        @Override
        public StorageObjectsChunk listObjectsChunked(String bucketName, String prefix, String delimiter,
                long maxListingLength, String priorLastKey, boolean completeListing) throws ServiceException {
            ServiceException e = new ServiceException("Forbidden");
            e.setResponseCode(403);
            throw e;
        }
    }

    @BeforeClass
    public void registerProtocol() {
        FileFactory.registerProtocol(FileProtocols.S3, new S3ProtocolProvider());
    }

    private static AbstractFile getFile(InMemoryS3Service service, String path) throws IOException {
        FileURL url = FileURL.getFileURL("s3://s3.example.com" + path);
        url.setCredentials(new Credentials("access", "secret"));
        return FileFactory.getFile(url, null, Collections.singletonMap("service", service));
    }

    private static List<String> names(AbstractFile[] files) {
        List<String> names = new ArrayList<>();
        for (AbstractFile file : files)
            names.add(file.getName() + (file.isDirectory() ? "/" : ""));
        Collections.sort(names);
        return names;
    }

    @Test
    public void testDirectoryWithoutPlaceholder() throws IOException {
        InMemoryS3Service service = new InMemoryS3Service("photos/2024/beach.jpg", "photos/readme.txt");

        AbstractFile photos = getFile(service, "/bucket/photos/");
        assertTrue(photos.exists());
        assertTrue(photos.isDirectory());
        assertEquals(names(photos.ls()), Arrays.asList("2024/", "readme.txt"));

        // A directory holding only subdirectories
        service = new InMemoryS3Service("photos/2024/beach.jpg");
        photos = getFile(service, "/bucket/photos/");
        assertTrue(photos.exists());
        assertTrue(photos.isDirectory());
        assertEquals(names(photos.ls()), Collections.singletonList("2024/"));
        assertEquals(names(getFile(service, "/bucket/photos/2024/").ls()), Collections.singletonList("beach.jpg"));
    }

    @Test
    public void testDeleteDirectoryWithoutPlaceholderThatHasAFile() throws IOException {
        InMemoryS3Service service = new InMemoryS3Service("photos/readme.txt");
        AbstractFile photos = getFile(service, "/bucket/photos/");
        try {
            photos.delete();
            fail("deleting a non-empty directory must fail");
        }
        catch(IOException expected) {
            assertEquals(expected.getMessage(), "Directory not empty");
        }
        assertTrue(service.deleted.isEmpty());
        assertTrue(getFile(service, "/bucket/photos/").exists());
    }

    @Test
    public void testDeleteEmptyDirectoryWithPlaceholder() throws IOException {
        InMemoryS3Service service = new InMemoryS3Service("photos/");
        getFile(service, "/bucket/photos/").delete();
        assertEquals(service.deleted, Collections.singletonList("photos/"));
    }

    @Test
    public void testForbiddenListingIsAnAuthError() {
        try {
            getFile(new ListForbiddenS3Service(), "/bucket/photos/");
            fail("a 403 from the listing must not be reported as a missing directory");
        }
        catch(IOException e) {
            assertTrue(e instanceof AuthException, e.getClass().getName());
        }
    }

    @Test
    public void testDirectoryWithPlaceholder() throws IOException {
        InMemoryS3Service service = new InMemoryS3Service("docs/", "docs/a.txt");

        AbstractFile docs = getFile(service, "/bucket/docs/");
        assertTrue(docs.exists());
        assertTrue(docs.isDirectory());
        assertEquals(names(docs.ls()), Collections.singletonList("a.txt"));

        // An empty directory exists only through its placeholder
        service = new InMemoryS3Service("empty/");
        AbstractFile empty = getFile(service, "/bucket/empty/");
        assertTrue(empty.exists());
        assertTrue(empty.isDirectory());
        assertEquals(empty.ls().length, 0);
    }

    @Test
    public void testMissing() throws IOException {
        InMemoryS3Service service = new InMemoryS3Service("photos/2024/beach.jpg");

        AbstractFile missingDirectory = getFile(service, "/bucket/nothing/");
        assertFalse(missingDirectory.exists());
        try {
            missingDirectory.ls();
            throw new AssertionError("ls() of a missing directory must fail");
        } catch (IOException e) {
            // expected
        }

        assertFalse(getFile(service, "/bucket/photos/missing.jpg").exists());
        // A key that is only the start of another key's name is not a directory
        assertFalse(getFile(service, "/bucket/photos/20").exists());
    }
}
