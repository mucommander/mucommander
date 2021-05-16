/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2019
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

package com.mucommander.commons.file.protocol.registry;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionAccess;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.PermissionType;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;
import com.mucommander.commons.file.connection.ConnectionPool;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractRegistryImage extends ProtocolFile implements ConnectionHandlerFactory {

	public final static String REGISTRY_PROTOCOL_DOCKER = "docker";
	public final static String REGISTRY_PROTOCOL_OCI = "oci";
	public final static String REGISTRY_PROTOCOL_DIR = "dir";

	private AbstractFile parent;

	protected AbstractRegistryImage(FileURL url, AbstractFile parent) {
		super(url);
		this.parent = parent;
	}

	protected RegistryConnHandler getConnHandler() throws IOException {
		RegistryConnHandler connection = (RegistryConnHandler) ConnectionPool
				.getConnectionHandler(this, fileURL, true);
		connection.checkConnection();
		return connection;
	}

	@Override
	public ConnectionHandler createConnectionHandler(FileURL location) {
		return new RegistryConnHandler(location);
	}

	@Override
	public long getDate() {
		return 0;
	}

	@Override
	@UnsupportedFileOperation
	public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public PermissionBits getChangeablePermissions() {
		// no permission can be changed
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

	@Override
	public boolean isSymlink() {
		return false;
	}

	@Override
	public boolean isSystem() {
		return false;
	}

	@Override
	public FilePermissions getPermissions() {
		return isDirectory() ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS : FilePermissions.DEFAULT_FILE_PERMISSIONS;
	}

	@Override
	@UnsupportedFileOperation
	public void mkdir() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);		
	}

	@Override
	@UnsupportedFileOperation
	public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.READ_FILE);
	}

	@Override
	@UnsupportedFileOperation
	public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);
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
	public RandomAccessOutputStream getRandomAccessOutputStream()
			throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
	}

	@Override
	@UnsupportedFileOperation
	public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.RENAME);		
	}

	@Override
	@UnsupportedFileOperation
	public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
	}

	@Override
	@UnsupportedFileOperation
	public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);
	}

	@Override
	@UnsupportedFileOperation
	public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);
	}

	@Override
	public Object getUnderlyingFileObject() {
		return null;
	}

	@Override
    public AbstractFile getParent() {
        if (parent == null) {
            FileURL parentFileURL = this.fileURL.getParent();
            if (parentFileURL != null) {
                parent = FileFactory.getFile(parentFileURL);
                // Note: parent may be null if it can't be resolved
            }
        }
        return parent;
    }

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	@UnsupportedFileOperation
	public void delete() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.DELETE);
	}
}
