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

import com.google.cloud.storage.Bucket;
import com.mucommander.commons.file.*;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;
import com.mucommander.commons.file.connection.ConnectionPool;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GoogleCloudStorageFile extends ProtocolFile implements ConnectionHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStorageFile.class);

    private File file;
    private GoogleCloudStorageFile parent;

//    public static String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    protected GoogleCloudStorageFile(FileURL url, File file) {
        super(url);
        this.file = file;
    }

    protected GoogleCloudStorageFile(FileURL url) {
        super(url);
    }

    /**
     * TODO CLIENT or connection?
     *
     * @return
     * @throws IOException
     */
    protected GoogleCloudStorageClient getCloudStorageClient() throws IOException {
        GoogleCloudStorageConnectionHandler connectionHandler =
                (GoogleCloudStorageConnectionHandler) ConnectionPool.getConnectionHandler(this, fileURL, true);

        // Return client only when connection was checked
        return connectionHandler.getClient();
    }

//    protected GoogleCloudStorageConnectionHandler getConnHandler() throws IOException {
//        GoogleCloudStorageConnectionHandler connection = (GoogleCloudStorageConnectionHandler) ConnectionPool.getConnectionHandler(this, fileURL, true);
//        connection.checkConnection();
//        return connection;
//    }

    @Override
    public MonitoredFile toMonitoredFile() {
        return new GoogleCloudStorageMonitoredFile(this);
    }

    @Override
    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new GoogleCloudStorageConnectionHandler(location);
    }

    @Override
    public long getDate() {
        return 0; //fixme
//        return file != null ? file.getModifiedTime().getValue() : 0;
    }

    @Override
    public void postCopyHook() {
        try {
            Thread.sleep(2000); //FIXME
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
    }

    @Override
    public long getSize() {
//        if (file == null)
//            return 0;
//        Long size = file.getSize();
//        return size != null ? size.longValue() : 0;
        return 0; // FIXME: 02.04.2023 
    }

    @Override
    public GoogleCloudStorageFile getParent() {
        if (parent == null) {
            FileURL parentFileURL = this.fileURL.getParent();
            if (parentFileURL != null)
                setParent(FileFactory.getFile(parentFileURL));
            // Note: parent may be null if it can't be resolved
        }
        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {
        if (parent instanceof GoogleCloudStorageMonitoredFile)
            parent = ((GoogleCloudStorageMonitoredFile) parent).getUnderlyingFile();
        this.parent = (GoogleCloudStorageFile) parent;
    }

    @Override
    public boolean exists() {
        if (file != null)
            return true;

        GoogleCloudStorageFile parent = getParent();
        if (parent == null || !parent.exists())
            return false;

        try {
            Stream.of(parent.ls()).filter(this::equals).findFirst().ifPresent(other -> this.file = other.file);
        } catch (IOException e) {
            LOGGER.warn("failed to list {}", parent);
            return false;
        }

        return file != null;
    }

    @Override
    public FilePermissions getPermissions() {
        return isDirectory() ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS : new SimpleFilePermissions(FilePermissions.FULL_PERMISSION_INT);
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return PermissionBits.EMPTY_PERMISSION_BITS;
    }

    @Override
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled)
            throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public boolean canGetOwner() {
        return false;
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
    public boolean isDirectory() {
        return false;
//        return file != null ? isFolder(file) : false;
    }

//    protected static boolean isFolder(File file) {
//        return FOLDER_MIME_TYPE.equals(file.getMimeType());
//    }

    @Override
    public boolean isSymlink() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public GoogleCloudStorageFile[] ls() throws IOException, UnsupportedFileOperationException {
//        try (GoogleCloudStorageConnectionHandler connHandler = getConnHandler()) {
//            FileList result = connHandler.getConnection().files().list()
//                    .setFields("files(id,name,parents,size,modifiedTime,mimeType,trashed)")
//                    .setQ(String.format("'%s' in parents", getId()))
//                    .execute();
//            List<File> files = result.getFiles();
//            if (files == null || files.isEmpty()) {
//                LOGGER.info("No files found.");
//                return new GoogleCloudStorageFile[0];
//            }
//
//            return files.stream()
//                    .filter(file -> file.getSize() != null || isFolder(file))
//                    .filter(file -> !file.getTrashed())
//                    .map(this::toFile)
//                    .toArray(GoogleCloudStorageFile[]::new);
//        }

        var buckets = getCloudStorageClient().getConnection().list();

        return StreamSupport.stream(buckets.iterateAll().spliterator(), false)
                .map(this::toFile)
                .toArray(GoogleCloudStorageFile[]::new);
    }

    private GoogleCloudStorageFile toFile(Bucket bucket) {
        FileURL url = (FileURL) getURL().clone();
        String parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + AbstractFile.DEFAULT_SEPARATOR;
        url.setPath(parentPath + file.getName());
        GoogleCloudStorageFile result = new GoogleCloudStorageFile(url, file);
        result.setParent(this);
        return result;
    }

//    private GoogleCloudStorageFile toFile(File file) {
//        FileURL url = (FileURL) getURL().clone();
//        String parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + AbstractFile.DEFAULT_SEPARATOR;
//        url.setPath(parentPath + file.getName());
//        GoogleCloudStorageFile result = new GoogleCloudStorageFile(url, file);
//        result.setParent(this);
//        return result;
//    }

    @Override
    public void mkdir() throws IOException, UnsupportedFileOperationException {
//        try (GoogleCloudStorageConnectionHandler connHandler = getConnHandler()) {
//            File fileMetadata = new File();
//            String filename = getURL().getFilename();
//            fileMetadata.setParents(Collections.singletonList(getParent().getId()));
//            fileMetadata.setName(filename);
//            fileMetadata.setMimeType(FOLDER_MIME_TYPE);
//            file = connHandler.getConnection().files().create(fileMetadata)
//                    .setFields("id,name,parents,size,modifiedTime,mimeType")
//                    .execute();
//        }
        throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
//        try (GoogleCloudStorageConnectionHandler connHandler = getConnHandler()) {
//            return connHandler.getConnection().files()
//                    .get(file.getId())
//                    .executeMediaAsInputStream();
//        }
        throw new UnsupportedFileOperationException(FileOperation.READ_FILE);
    }

    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
//        try (GoogleCloudStorageConnectionHandler connHandler = getConnHandler()) {
//            File fileMetadata = new File();
//            String filename = getURL().getFilename();
//            fileMetadata.setParents(Collections.singletonList(getParent().getId()));
//            fileMetadata.setName(filename);
//            PipedOutputStream output = new PipedOutputStream();
//            PipedInputStream input = new PipedInputStream(output);
//            new Thread(() -> {
//                InputStreamContent in = new InputStreamContent("application/octet-stream", input);
//                try {
//                    file = connHandler.getConnection().files()
//                            .create(fileMetadata, in)
//                            .setFields("id,name,parents,size,modifiedTime,mimeType")
//                            .execute();
//                } catch (IOException e) {
//                    LOGGER.error("failed to copy to Google Drive", e);
//                }
//            }).start();
//            return output;
//        }
        throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);
    }

    @Override
    public AbstractFile getChild(String filename, AbstractFile template) throws IOException {
//        if (template == null)
//            return super.getChild(filename, template);
//        FileURL url = (FileURL) getURL().clone();
//        String parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + AbstractFile.DEFAULT_SEPARATOR;
//        url.setPath(parentPath + filename);
//        return new GoogleCloudStorageFile(url);
        throw new UnsupportedFileOperationException(FileOperation.LIST_CHILDREN);
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
    }

    @Override
    @UnsupportedFileOperation
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);
    }

    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream()
            throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
//        try (GoogleCloudStorageConnectionHandler connHandler = getConnHandler()) {
//            connHandler.getConnection().files().delete(file.getId()).execute();
//        }
        throw new UnsupportedFileOperationException(FileOperation.DELETE);
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
//        try (GoogleCloudStorageConnectionHandler connHandler = getConnHandler()) {
//            connHandler.getConnection().files().update(file.getId(), new File().setName(destFile.getName())).execute();
//            file.setName(destFile.getName());
//        }
        throw new UnsupportedFileOperationException(FileOperation.RENAME);
    }

    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
//        try (GoogleCloudStorageConnectionHandler connHandler = getConnHandler()) {
//            About about = connHandler.getConnection().about().get().setFields("storageQuota").execute();
//            Map<String, Long> storageQuota = (Map<String, Long>) about.get("storageQuota");
//            Long limit = storageQuota.get("limit");
//            if (limit == null)
//                return -1;
//            Long usage = storageQuota.get("usage");
//            return limit - usage;
//        }
        throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
//        try (GoogleCloudStorageConnectionHandler connHandler = getConnHandler()) {
//            About about = connHandler.getConnection().about().get().setFields("storageQuota").execute();
//            Map<String, Long> storageQuota = (Map<String, Long>) about.get("storageQuota");
//            Long limit = storageQuota.get("limit");
//            return limit != null ? limit : -1;
//        }
        throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);
    }

    @Override
    public Object getUnderlyingFileObject() {
        return null;
    }

//    protected String getId() {
//        return file.getId();
//    }
}
