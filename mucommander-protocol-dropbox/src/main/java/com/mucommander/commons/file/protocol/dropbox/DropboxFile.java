package com.mucommander.commons.file.protocol.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.DeletedMetadata;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadUploader;
import com.dropbox.core.v2.users.SpaceUsage;
import com.mucommander.commons.file.AbstractFile;
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
import com.mucommander.commons.io.FilteredOutputStream;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

public class DropboxFile extends ProtocolFile implements ConnectionHandlerFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(DropboxFile.class);

	private String id;
	private AbstractFile parent;
	private boolean dir;
	private long size;
	private long date;

	protected DropboxFile(FileURL url) {
		super(url);
	}

	protected DropboxFile(FileURL url, DropboxFile parent, Metadata metadata) {
		this(url);
		this.parent = parent;
		updateAttributes(metadata);
	}

	void updateAttributes(Metadata metadata) {
		if (metadata instanceof FileMetadata) {
			FileMetadata file = (FileMetadata) metadata;
			size = file.getSize();
			dir = false;
			date = file.getServerModified().getTime();
			id = file.getId();
			return;
		}
		if (metadata instanceof FolderMetadata) {
			FolderMetadata folder = (FolderMetadata) metadata;
			dir = true;
			id = folder.getId();
			return;
		}
	}

	protected String getId() {
		return id;
	}

	@Override
	public long getDate() {
		return date;
	}

	@Override
	public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public AbstractFile getParent() {
		return parent;
	}

	@Override
	public void setParent(AbstractFile parent) {
		this.parent = parent;
	}

	@Override
	public boolean exists() {
		return id != null;
	}

	@Override
	public FilePermissions getPermissions() {
		return dir ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
				: FilePermissions.DEFAULT_FILE_PERMISSIONS;
	}

	@Override
	public PermissionBits getChangeablePermissions() {
		return PermissionBits.EMPTY_PERMISSION_BITS;
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
		return dir;
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
	public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			DbxUserFilesRequests r = connHandler.getDbxClient().files();
			ListFolderResult result;
			try {
				result = r.listFolder(getId());
			} catch (DbxException e) {
				e.printStackTrace();
				return null;
			}
			return result.getEntries().stream()
					.filter(meta -> !(meta instanceof DeletedMetadata))
					.map(meta -> {
						FileURL url = (FileURL) fileURL.clone();
						url.setPath(meta.getPathDisplay());
						return new DropboxFile(url, this, meta);
					})
					.toArray(AbstractFile[]::new);
		}
	}

	    
	private DropboxConnectionHandler getConnHandler() throws IOException {
		DropboxConnectionHandler connHandler = (DropboxConnectionHandler) ConnectionPool
				.getConnectionHandler(this, fileURL, true);
		try {
			connHandler.checkConnection();
		} catch (RuntimeException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return connHandler;
	}

	@Override
	public void mkdir() throws IOException, UnsupportedFileOperationException {
		
	}

	@Override
	public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			return connHandler.getDbxClient().files().download(id).getInputStream();
		} catch (DbxException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			UploadUploader uploader = connHandler.getDbxClient().files().upload(fileURL.getPath());
			return new FilteredOutputStream(uploader.getOutputStream()) {
				public void close() throws IOException {
					super.close();
					try {
						id = uploader.finish().getId();
					} catch (DbxException e) {
						e.printStackTrace();
					}
				}
			};
		} catch (DbxException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public OutputStream getAppendOutputStream() throws IOException, UnsupportedFileOperationException {
		return null;
	}

	@Override
	public RandomAccessInputStream getRandomAccessInputStream() throws IOException, UnsupportedFileOperationException {
		return null;
	}

	@Override
	public RandomAccessOutputStream getRandomAccessOutputStream()
			throws IOException, UnsupportedFileOperationException {
		return null;
	}

	@Override
	public void delete() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) { 
			connHandler.getDbxClient().files().deleteV2(getId());
		} catch (DbxException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
	}

	@Override
	@UnsupportedFileOperation
	public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
	}

	@Override
	public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			SpaceUsage storage = connHandler.getDbxClient().users().getSpaceUsage();
			return storage.getAllocation().getIndividualValue().getAllocated() - storage.getUsed();
		} catch (DbxException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			SpaceUsage storage = connHandler.getDbxClient().users().getSpaceUsage();
			return storage.getAllocation().getIndividualValue().getAllocated();
		} catch (DbxException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public Object getUnderlyingFileObject() {
		return null;
	}

	@Override
	public ConnectionHandler createConnectionHandler(FileURL location) {
		return new DropboxConnectionHandler(location);
	}

	@Override
	public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled)
			throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
	}
}
