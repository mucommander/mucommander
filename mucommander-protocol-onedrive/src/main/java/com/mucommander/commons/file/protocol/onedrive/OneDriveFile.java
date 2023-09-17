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

package com.mucommander.commons.file.protocol.onedrive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemCreateUploadSessionParameterSet;
import com.microsoft.graph.models.DriveItemUploadableProperties;
import com.microsoft.graph.models.Folder;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.DriveItemRequestBuilder;
import com.microsoft.graph.tasks.LargeFileUploadTask;
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
import com.mucommander.commons.io.FileTransferException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

public class OneDriveFile extends ProtocolFile implements ConnectionHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(OneDriveFile.class);
    private DriveItem driveItem;
    private OneDriveFile parent;
    /** This field is stores the length to stream in {@link #getOutputStream()} */
    private long lengthToStream;

    protected OneDriveFile(FileURL url, DriveItem driveItem) {
        super(url);
        this.driveItem = driveItem;
    }

    protected OneDriveFile(FileURL url) {
        super(url);
    }

    protected OneDriveConnHandler getConnHandler() throws IOException {
        OneDriveConnHandler connection = (OneDriveConnHandler) ConnectionPool.getConnectionHandler(this, fileURL, true);
        connection.checkConnection();
        return connection;
    }

    @Override
    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new OneDriveConnHandler(location);
    }

    @Override
    public void postCopyHook() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
    }

    @Override
    public MonitoredFile toMonitoredFile() {
        return new OneDriveMonitoredFile(this);
    }

    @Override
    public long getDate() {
        return driveItem != null ? driveItem.lastModifiedDateTime.toInstant().toEpochMilli() : 0;
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
    }

    @Override
    public long getSize() {
        return driveItem != null ? driveItem.size : 0;
    }

    @Override
    public boolean exists() {
        if (driveItem != null)
            return true;
        OneDriveFile parent = getParent();
        if (parent == null || !parent.exists())
            return false;

        try {
            Stream.of(parent.ls()).filter(this::equals).findFirst().ifPresent(other -> this.driveItem = other.driveItem);
        } catch (IOException e) {
            LOGGER.warn("failed to list {}", parent);
            return false;
        }

        return driveItem != null;
    }

    @Override
    public boolean isDirectory() {
        return driveItem != null && driveItem.folder != null;
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
    public OneDriveFile[] ls() throws IOException, UnsupportedFileOperationException {
        try (OneDriveConnHandler connHandler = getConnHandler()) {
            DriveItemRequestBuilder builder = connHandler.getClient().me().drive().root();
            String path = PathUtils.removeLeadingSeparator(getURL().getPath());
            if (path.length() > 0)
                builder = builder.itemWithPath(path);
            DriveItemCollectionPage items = builder.children().buildRequest().get();
            List<DriveItem> files = items.getCurrentPage();
            // TODO: support more than one page (https://docs.microsoft.com/en-us/graph/sdks/paging?view=graph-rest-beta&tabs=java)
            return files.stream()
                    .filter(file -> file.file != null || file.folder != null)
                    .map(this::toFile)
                    .toArray(OneDriveFile[]::new);
        }
    }

    protected AbstractFile toFile(DriveItem driveItem) {
        FileURL url = (FileURL) getURL().clone();
        String parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + AbstractFile.DEFAULT_SEPARATOR;
        url.setPath(parentPath + driveItem.name);
        OneDriveFile result = new OneDriveFile(url, driveItem);
        result.setParent(this);
        return result;
    }

    @Override
    public void mkdir() throws IOException, UnsupportedFileOperationException {
        try(OneDriveConnHandler connHandler = getConnHandler()) {
            DriveItem newItem = new DriveItem();
            newItem.name = getURL().getFilename();
            newItem.folder = new Folder();

            DriveItemRequestBuilder builder = connHandler.getClient().me().drive().root();
            String path = PathUtils.removeLeadingSeparator(getParent().getURL().getPath());
            if (path.length() > 0)
                builder = builder.itemWithPath(path);
            driveItem = builder.children().buildRequest().post(newItem);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        try(OneDriveConnHandler connHandler = getConnHandler()) {
            return connHandler.getClient()
                    .me()
                    .drive()
                    .root()
                    .itemWithPath(PathUtils.removeLeadingSeparator(getURL().getPath()))
                    .content()
                    .buildRequest()
                    .get();
        }
    }

    @Override
    public void copyStream(InputStream in, boolean append, long length) throws FileTransferException {
        this.lengthToStream = length;
        super.copyStream(in, append, length);
    }

    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
        DriveItemCreateUploadSessionParameterSet uploadParams =
                DriveItemCreateUploadSessionParameterSet.newBuilder()
                    .withItem(new DriveItemUploadableProperties()).build();
        try(OneDriveConnHandler connHandler = getConnHandler()) {
            UploadSession uploadSession = connHandler.getClient()
                    .me()
                    .drive()
                    .root()
                    .itemWithPath(PathUtils.removeLeadingSeparator(getURL().getPath()))
                    .createUploadSession(uploadParams)
                    .buildRequest()
                    .post();
            PipedOutputStream output = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(output);
            LargeFileUploadTask<DriveItem> largeFileUploadTask = new LargeFileUploadTask<>(
                    uploadSession,
                    connHandler.getClient(),
                    input,
                    lengthToStream,
                    DriveItem.class);
            new Thread(() -> {
                try {
                    largeFileUploadTask.upload(327680);
                } catch (IOException e) {
                    LOGGER.error("failed to upload to OneDrive", e);
                }
            }).start();
            return output;
        }
    }

    @Override
    @UnsupportedFileOperation
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
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
        try(OneDriveConnHandler connHandler = getConnHandler()) {
            String path = PathUtils.removeLeadingSeparator(getURL().getPath());
            connHandler.getClient().me().drive().root().itemWithPath(path).buildRequest().delete();
        }
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        try(OneDriveConnHandler connHandler = getConnHandler()) {
            DriveItem driveItem = new DriveItem();
            driveItem.name = destFile.getName();
            this.driveItem = connHandler.getClient()
                    .me()
                    .drive()
                    .root()
                    .itemWithPath(PathUtils.removeLeadingSeparator(getURL().getPath()))
                    .buildRequest()
                    .patch(driveItem);
        }
    }

    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        try(OneDriveConnHandler connHandler = getConnHandler()) {
            return connHandler.getClient()
                    .me()
                    .drive()
                    .buildRequest()
                    .get().quota.remaining;
        }
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        try(OneDriveConnHandler connHandler = getConnHandler()) {
            return connHandler.getClient()
                    .me()
                    .drive()
                    .buildRequest()
                    .get().quota.total;
        }
    }

    @Override
    public Object getUnderlyingFileObject() {
        return driveItem;
    }

    @Override
    public OneDriveFile getParent() {
        if (parent == null) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null) {
                setParent(FileFactory.getFile(parentFileURL));
                // Note: parent may be null if it can't be resolved
            }
        }
        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {
        if (parent instanceof OneDriveMonitoredFile)
            parent = ((OneDriveMonitoredFile) parent).getUnderlyingFileObject();
        this.parent = (OneDriveFile) parent;
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
    @UnsupportedFileOperation
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
}
