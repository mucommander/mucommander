package com.mucommander.commons.file.protocol.dropbox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.DeletedMetadata;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.SpaceUsage;
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
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

public class DropboxFile extends ProtocolFile implements ConnectionHandlerFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(DropboxFile.class);

	private String id;
	private boolean dir;
	private long size;
	private long date;

	private boolean parentValSet;
	private AbstractFile parent;

	private boolean fileResolved;

	protected DropboxFile(FileURL url) {
		super(url);
	}

	protected DropboxFile(FileURL url, DropboxFile parent, Metadata metadata) {
		this(url);
		setParent(parent);
		updateAttributes(metadata);
		fileResolved = true;
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

	private void resolveFile() throws IOException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			LOGGER.info("Resolving {}", getURL());
			String path = PathUtils.removeTrailingSeparator(getURL().getPath());
			Metadata metadata = connHandler.getDbxClient().files().getMetadata(path);
			updateAttributes(metadata);
		} catch (DbxException e) {
			LOGGER.error("failed to resolve dropbox file", e);
		} finally {
			fileResolved = true;
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
		throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public AbstractFile getParent() {
		if(!parentValSet) {
            FileURL parentURL = fileURL.getParent();
            if (parentURL==null)
                this.parent = null;
            else {
                this.parent = FileFactory.getFile(parentURL);
            }
            this.parentValSet = true;
        }
        return this.parent;
	}

	@Override
	public void setParent(AbstractFile parent) {
		this.parent = parent;
		this.parentValSet = true;
	}

	@Override
	public boolean exists() {
		if (!fileResolved) {
			try { resolveFile(); }
			catch(IOException e) {}
		}
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
				LOGGER.error("failed to list folder", e);
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
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			CreateFolderResult result = connHandler.getDbxClient().files().createFolderV2(getURL().getPath());
			id = result.getMetadata().getId();
		} catch (DbxException e) {
			LOGGER.error("failed to make directory" , e);
			throw new IOException(e);
		}
	}

	@Override
	public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			InputStream in = connHandler.getDbxClient().files().download(id).getInputStream();
			return new BufferedInputStream(in, 4 << 20);
		} catch (DbxException e) {
			LOGGER.error("failed to get input stream", e);
			throw new IOException(e);
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			OutputStream out = new OutputStream() {
				UploadSessionCursor cursor;

				@Override
				public void write(int b) throws IOException {
					// noop
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					if (cursor == null) {
						try {
							String sessionId = connHandler.getDbxClient().files().uploadSessionStart()
							        .uploadAndFinish(new ByteArrayInputStream(b), len)
							        .getSessionId();
							cursor = new UploadSessionCursor(sessionId, len);
						} catch (DbxException | IOException e) {
							LOGGER.error("failed to initiate upload", e);
						}
					} else {
						try {
							connHandler.getDbxClient().files().uploadSessionAppendV2(cursor)
							.uploadAndFinish(new ByteArrayInputStream(b), len);
							cursor = new UploadSessionCursor(cursor.getSessionId(), cursor.getOffset() + len);
						} catch (DbxException | IOException e) {
							LOGGER.error("failed to append to file", e);
						}
					}
					connHandler.updateLastActivityTimestamp();
				}

				@Override
				public void close() throws IOException {
					CommitInfo commitInfo = CommitInfo.newBuilder(getURL().getPath())
							.withMode(WriteMode.ADD)
							.withClientModified(new Date())
							.build();
					FileMetadata metadata;
					try {
						metadata = connHandler.getDbxClient().files().uploadSessionFinish(cursor, commitInfo).finish();
					} catch (DbxException e) {
						LOGGER.error("failed to finish upload", e);
						throw new IOException(e);
					}
					id = metadata.getId();
				}
			};
			return new BufferedOutputStream(out, 4 << 20);
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
	public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException {
		return null;
	}

	@Override
	public void delete() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) { 
			connHandler.getDbxClient().files().deleteV2(getId());
		} catch (DbxException e) {
			LOGGER.error("failed to delete file", e);
			throw new IOException(e);
		}
	}

	@Override
	public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			connHandler.getDbxClient().files().moveV2(getId(), destFile.getURL().getPath());
		} catch (DbxException e) {
			LOGGER.error("failed to rename file", e);
			throw new IOException(e);
		}
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
			LOGGER.error("failed to get free space", e);
			throw new IOException(e);
		}
	}

	@Override
	public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
		try (DropboxConnectionHandler connHandler = getConnHandler()) {
			SpaceUsage storage = connHandler.getDbxClient().users().getSpaceUsage();
			return storage.getAllocation().getIndividualValue().getAllocated();
		} catch (DbxException e) {
			LOGGER.error("failed to get total space", e);
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
