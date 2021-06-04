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
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jets3t.service.Constants;
import org.jets3t.service.S3Service;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.model.StorageObject;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileAttributes;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionAccess;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.PermissionType;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.io.RandomAccessOutputStream;

/**
 * Super class of {@link S3Root}, {@link S3Bucket} and {@link S3Object}.
 *
 * @author Maxence Bernard
 */
public abstract class S3File extends ProtocolFile {

    protected org.jets3t.service.S3Service service;

    protected AbstractFile parent;
    protected boolean parentSet;

    public static String STORAGE_TYPE = "storageType";
    public static String DISABLE_DNS_BUCKETS = "dnsBuckets";
    public static String SECUTRE_HTTP = "secureHttp";
    public static String DEFAULT_BUCKET_LOCATION = "defaultBucketLocation";

    protected S3File(FileURL url, S3Service service) {
        super(url);

        this.service = service;
    }
    
    protected IOException getIOException(ServiceException e) throws IOException {
        return getIOException(e, fileURL);
    }

    protected static IOException getIOException(ServiceException e, FileURL fileURL) throws IOException {
        handleAuthException(e, fileURL);

        Throwable cause = e.getCause();
        if(cause instanceof IOException)
            return (IOException)cause;

        return new IOException(e);
    }

    protected static void handleAuthException(ServiceException e, FileURL fileURL) throws AuthException {
        int code = e.getResponseCode();
        if(code==401 || code==403)
            throw new AuthException(fileURL);
    }
    
    protected AbstractFile[] listObjects(String bucketName, String prefix, S3File parent) throws IOException {
        try {
        	StorageObjectsChunk chunk = service.listObjectsChunked(bucketName, prefix, "/", Constants.DEFAULT_OBJECT_LIST_CHUNK_SIZE, null, true);
            StorageObject objects[] = chunk.getObjects();
            String[] commonPrefixes = chunk.getCommonPrefixes();

            if(objects.length==0 && !prefix.equals("")) {
                // This happens only when the directory does not exist
                throw new IOException();
            }

            AbstractFile[] children = new AbstractFile[objects.length+commonPrefixes.length];
            FileURL childURL;
            int i=0;
            String objectKey;

            for(StorageObject object : objects) {
                // Discard the object corresponding to the prefix itself
                objectKey = object.getKey();
                if(objectKey.equals(prefix))
                    continue;

                childURL = (FileURL)fileURL.clone();
                childURL.setPath(bucketName + "/" + objectKey);

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("service", service);
                parameters.put("object", object);
                children[i] = FileFactory.getFile(childURL, parent, parameters);
                i++;
            }

            org.jets3t.service.model.S3Object directoryObject;
            for(String commonPrefix : commonPrefixes) {
                childURL = (FileURL)fileURL.clone();
                childURL.setPath(bucketName + "/" + commonPrefix);

                directoryObject = new org.jets3t.service.model.S3Object(commonPrefix);
                // Common prefixes are not objects per se, and therefore do not have a date, content-length nor owner.
                directoryObject.setLastModifiedDate(new Date(System.currentTimeMillis()));
                directoryObject.setContentLength(0);
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("service", service);
                parameters.put("object", directoryObject);
                children[i] = FileFactory.getFile(childURL, parent, parameters);
                i++;
            }

            // Trim the array if an object was discarded.
            // Note: Having to recreate an array sucks (puts pressure on the GC), but I haven't found a reliable way
            // to know in advance whether the prefix will appear in the results or not.
            if(i<children.length) {
                AbstractFile[] childrenTrimmed = new AbstractFile[i];
                System.arraycopy(children, 0, childrenTrimmed, 0, i);

                return childrenTrimmed;
            }

            return children;
        }
        catch(ServiceException e) {
            throw getIOException(e);
        }
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    public abstract FileAttributes getFileAttributes();


    /////////////////////////////////
    // ProtocolFile implementation //
    /////////////////////////////////

    @Override
    public AbstractFile getParent() {
        if(!parentSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null) {
                try {
                    parent = FileFactory.getFile(parentFileURL, null, Collections.singletonMap("service", service));
                }
                catch(IOException e) {
                    // No parent
                }
            }

            parentSet = true;
        }

        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentSet = true;
    }


    // Delegates to FileAttributes

    @Override
    public long getDate() {
        return getFileAttributes().getDate();
    }

    @Override
    public long getSize() {
        return getFileAttributes().getSize();
    }

    @Override
    public boolean exists() {
        return getFileAttributes().exists();
    }

    @Override
    public boolean isDirectory() {
        return getFileAttributes().isDirectory();
    }

    @Override
    public FilePermissions getPermissions() {
        return getFileAttributes().getPermissions();
    }

    @Override
    public Object getUnderlyingFileObject() {
        return getFileAttributes();
    }
    

    // Unsupported operations, no matter the kind of resource (object, bucket, service)

    @Override
    public boolean isSymlink() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return PermissionBits.EMPTY_PERMISSION_BITS;
    }

    @Override
    @UnsupportedFileOperation
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public boolean canGetGroup() {
        return false;
    }

    @Override
    @UnsupportedFileOperation
    public OutputStream getAppendOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
    }

    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
    }

    @Override
    @UnsupportedFileOperation
    public long getFreeSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);
    }

    @Override
    @UnsupportedFileOperation
    public long getTotalSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);
    }

    @Override
    @UnsupportedFileOperation
    public void changeDate(long lastModified) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
    }
}
