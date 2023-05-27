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

package com.mucommander.commons.file.protocol.gdrive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.MonitoredFile;
import com.mucommander.commons.file.PermissionAccess;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.PermissionType;
import com.mucommander.commons.file.SimpleFilePermissions;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;
import com.mucommander.commons.file.connection.ConnectionPool;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

public class GoogleDriveFile extends ProtocolFile implements ConnectionHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveFile.class);

    private File file;
    private GoogleDriveFile parent;

    protected GoogleDriveFile(FileURL url, File file) {
        super(url);
        this.file = file;
    }

    protected GoogleDriveFile(FileURL url) {
        super(url);
    }

    protected GoogleDriveConnHandler getConnHandler() throws IOException {
        GoogleDriveConnHandler connection = (GoogleDriveConnHandler) ConnectionPool.getConnectionHandler(this, fileURL, true);
        connection.checkConnection();
        return connection;
    }

    @Override
    public MonitoredFile toMonitoredFile() {
        return new GoogleDriveMonitoredFile(this);
    }

    @Override
    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new GoogleDriveConnHandler(location);
    }

    @Override
    public long getDate() {
        return file != null ? file.getModifiedTime().getValue() : 0;
    }

    @Override
    public void postCopyHook() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
    }

    @Override
    public long getSize() {
        if (file == null)
            return 0;
        Long size = file.getSize();
        return size != null ? size.longValue() : 0;
    }

    @Override
    public GoogleDriveFile getParent() {
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
        if (parent instanceof GoogleDriveMonitoredFile)
            parent = ((GoogleDriveMonitoredFile) parent).getUnderlyingFile();
        this.parent = (GoogleDriveFile) parent;
    }

    @Override
    public boolean exists() {
        if (file != null)
            return true;

        GoogleDriveFile parent = getParent();
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
        return file != null ? Files.isFolder(file) : false;
    }

    @Override
    public boolean isSymlink() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public GoogleDriveFile[] ls() throws IOException, UnsupportedFileOperationException {
        try (GoogleDriveConnHandler connHandler = getConnHandler()) {
            FileList result = connHandler.getConnection().files().list()
                    .setFields("files(id,name,parents,size,modifiedTime,mimeType,trashed)")
                    .setQ(String.format("'%s' in parents", getId()))
                    .setPageSize(1000)
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                LOGGER.info("No files found.");
                return new GoogleDriveFile[0];
            }

            return files.stream()
                    .filter(Files::isNotTrashed)
                    .map(this::toFile)
                    .toArray(GoogleDriveFile[]::new);
        }
    }

    protected GoogleDriveFile toFile(File file) {
        FileURL url = (FileURL) getURL().clone();
        String parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + AbstractFile.DEFAULT_SEPARATOR;
        url.setPath(parentPath + file.getName());
        GoogleDriveFile result = new GoogleDriveFile(url, file);
        result.setParent(this);
        return result;
    }

    @Override
    public void mkdir() throws IOException, UnsupportedFileOperationException {
        try (GoogleDriveConnHandler connHandler = getConnHandler()) {
            File fileMetadata = new File();
            String filename = getURL().getFilename();
            fileMetadata.setParents(Collections.singletonList(getParent().getId()));
            fileMetadata.setName(filename);
            fileMetadata.setMimeType(Files.FOLDER_MIME_TYPE);
            file = connHandler.getConnection().files().create(fileMetadata)
                    .setFields("id,name,parents,size,modifiedTime,mimeType")
                    .execute();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        try (GoogleDriveConnHandler connHandler = getConnHandler()) {
            switch(file.getMimeType()) {
            case Files.DOCUMENT_MIME_TYPE:
                return connHandler.getConnection().files()
                        .export(file.getId(), "application/vnd.oasis.opendocument.text")
                        .executeMediaAsInputStream();
            case Files.SPREADSHEET_MIME_TYPE:
                return connHandler.getConnection().files()
                        .export(file.getId(), "application/vnd.oasis.opendocument.spreadsheet")
                        .executeMediaAsInputStream();
            case Files.PRESENTATION_MIME_TYPE:
                return connHandler.getConnection().files()
                        .export(file.getId(), "application/vnd.oasis.opendocument.presentation")
                        .executeMediaAsInputStream();
            case Files.DRAWING_MIME_TYPE:
                return connHandler.getConnection().files()
                        .export(file.getId(), "image/jpeg")
                        .executeMediaAsInputStream();
            default:
                return connHandler.getConnection().files()
                        .get(file.getId())
                        .executeMediaAsInputStream();
            }
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
        try (GoogleDriveConnHandler connHandler = getConnHandler()) {
            File fileMetadata = new File();
            String filename = getURL().getFilename();
            fileMetadata.setParents(Collections.singletonList(getParent().getId()));
            fileMetadata.setName(filename);
            PipedOutputStream output = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(output);
            new Thread(() -> {
                InputStreamContent in = new InputStreamContent("application/octet-stream", input);
                try {
                    file = connHandler.getConnection().files()
                            .create(fileMetadata, in)
                            .setFields("id,name,parents,size,modifiedTime,mimeType")
                            .execute();
                } catch (IOException e) {
                    LOGGER.error("failed to copy to Google Drive", e);
                }
            }).start();
            return output;
        }
    }

    @Override
    public AbstractFile getChild(String filename, AbstractFile template) throws IOException {
        if (template == null)
            return super.getChild(filename, template);
        FileURL url = (FileURL) getURL().clone();
        String parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + AbstractFile.DEFAULT_SEPARATOR;
        url.setPath(parentPath + filename);
        return new GoogleDriveFile(url);
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
        try (GoogleDriveConnHandler connHandler = getConnHandler()) {
            connHandler.getConnection().files().delete(file.getId()).execute();
        }
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        try (GoogleDriveConnHandler connHandler = getConnHandler()) {
            connHandler.getConnection().files().update(file.getId(), new File().setName(destFile.getName())).execute();
            file.setName(destFile.getName());
        }
    }

    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        try (GoogleDriveConnHandler connHandler = getConnHandler()) {
            About about = connHandler.getConnection().about().get().setFields("storageQuota").execute();
            Map<String, Long> storageQuota = (Map<String, Long>) about.get("storageQuota");
            Long limit = storageQuota.get("limit");
            if (limit == null)
                return -1;
            Long usage = storageQuota.get("usage");
            return limit - usage;
        }
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        try (GoogleDriveConnHandler connHandler = getConnHandler()) {
            About about = connHandler.getConnection().about().get().setFields("storageQuota").execute();
            Map<String, Long> storageQuota = (Map<String, Long>) about.get("storageQuota");
            Long limit = storageQuota.get("limit");
            return limit != null ? limit : -1;
        }
    }

    @Override
    public Object getUnderlyingFileObject() {
        return null;
    }

    protected String getId() {
        return file.getId();
    }
}
