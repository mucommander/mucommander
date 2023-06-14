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

import com.google.cloud.storage.Storage;
import com.mucommander.commons.file.*;
import com.mucommander.commons.file.connection.ConnectionPool;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Dummy abstract implementation of CloudStorage file. Refuses almost all standard operations later overridden
 * by children who can support them.
 *
 * @author miroslav.spak
 */
public abstract class GoogleCloudStorageAbstractFile extends ProtocolFile {

    private static final String CLOUD_STORAGE_DIRECTORY_SEPARATOR = "/";

    private Storage storageService;

    protected GoogleCloudStorageAbstractFile parent;

    protected GoogleCloudStorageAbstractFile(FileURL url) {
        this(url, null);
    }

    protected GoogleCloudStorageAbstractFile(FileURL url, Storage storageService) {
        super(url);
        this.storageService = storageService;
    }

    protected Storage getStorageService() throws IOException {
        if (storageService == null) {
            storageService = getCloudStorageClient().getConnection();
        }

        return storageService;
    }

    private GoogleCloudStorageClient getCloudStorageClient() throws IOException {
        GoogleCloudStorageConnectionHandler connectionHandler =
                // Get connection handler for the given GCS url
                (GoogleCloudStorageConnectionHandler) ConnectionPool.getConnectionHandler(
                        GoogleCloudStorageConnectionHandlerFactory.getInstance(), fileURL, false);

        connectionHandler.checkConnection();
        return connectionHandler.getClient();
    }

    @Override
    public String getSeparator() {
        return CLOUD_STORAGE_DIRECTORY_SEPARATOR;
    }

    @Override
    public long getDate() {
        // No date supplied
        return 0;
    }

    @Override
    public void changeDate(long lastModified) throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
    }

    @Override
    public long getSize() {
        // No size for the GoogleCloudStorageAbstractFile
        return 0;
    }

    @Override
    public AbstractFile getParent() {
        if (parent == null) {
            FileURL parentFileURL = this.fileURL.getParent();
            if (parentFileURL != null) {
                setParent(FileFactory.getFile(parentFileURL));
            }
            // Note: parent may be null if it can't be resolved
        }
        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {
        if (parent instanceof GoogleCloudStorageMonitoredFile) {
            parent = ((GoogleCloudStorageMonitoredFile) parent).getUnderlyingFile();
        }
        this.parent = (GoogleCloudStorageAbstractFile) parent;
    }

    @Override
    public boolean exists() {
        // GoogleCloudStorageAbstractFile has no representation
        return false;
    }

    @Override
    public FilePermissions getPermissions() {
        return isDirectory() ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
                : new SimpleFilePermissions(FilePermissions.FULL_PERMISSION_INT);
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return PermissionBits.EMPTY_PERMISSION_BITS;
    }

    @Override
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled)
            throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
    }

    @Override
    public String getOwner() {
        // No owner info
        return null;
    }

    @Override
    public boolean canGetOwner() {
        return false;
    }

    @Override
    public String getGroup() {
        // No group info
        return null;
    }

    @Override
    public boolean canGetGroup() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
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
    public boolean isRoot() {
        return false;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        try {
            // Try to list this as directory
            var childrenList = listDir().collect(Collectors.toList());
            // Remap childrenList to array
            var childrenArray = new AbstractFile[childrenList.size()];
            childrenList.toArray(childrenArray);
            return childrenArray;
        } catch (Exception ex) {
            throw new IOException("Unable to list directory: " + fileURL, ex);
        }
    }

    /**
     * @return lists the children of the current {@link GoogleCloudStorageAbstractFile}.
     */
    protected abstract Stream<GoogleCloudStorageAbstractFile> listDir() throws IOException;

    /**
     * Unifies the creation of a {@link GoogleCloudStorageAbstractFile} in the module.
     *
     * @param filePathFun function that gets parent path as parameter and returns a new target file path
     * @param createFun   function to create a new concrete instance of the abstract file
     *                    (e.g., {@link GoogleCloudStorageFile})
     */
    protected <FileT extends GoogleCloudStorageAbstractFile> FileT toFile(
            Function<String, String> filePathFun,
            Function<FileURL, FileT> createFun
    ) {
        Storage storageService;
        try {
            storageService = getStorageService();
        } catch (IOException e) {
            // Storage service is not mandatory here
            storageService = null;
        }

        var url = (FileURL) getURL().clone();
        var parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + getSeparator();
        url.setPath(filePathFun.apply(parentPath));
        var result = createFun.apply(url);
        // Initialize known properties
        ((GoogleCloudStorageAbstractFile) result).storageService = storageService;
        result.setParent(this);

        return result;
    }

    @Override
    public void mkdir() throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.READ_FILE);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
    }

    @Override
    public void delete() throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.DELETE);
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.RENAME);
    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    @Override
    public long getFreeSpace() {
        // No space info for the GoogleCloudStorageAbstractFile
        return 0;
    }

    @Override
    public long getTotalSpace() {
        // No space info for the GoogleCloudStorageAbstractFile
        return 0;
    }

    @Override
    public MonitoredFile toMonitoredFile() {
        return new GoogleCloudStorageMonitoredFile(this);
    }

    @Override
    public Object getUnderlyingFileObject() {
        return null;
    }
}
