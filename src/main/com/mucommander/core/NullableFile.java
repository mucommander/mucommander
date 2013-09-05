package com.mucommander.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

/**
 * This class represents an {@link AbstractFile} that doesn't exists for UI purposes.
 * External libraries generally return null for path to non existing file, so in order
 * to be able to present such non existing file, we use this class.
 *
 * @author Arik Hadas
 */
class NullableFile extends AbstractFile {

	NullableFile(FileURL url) {
		super(url);
	}

	@Override
	public boolean canGetGroup() {
		return false;
	}

	@Override
	public boolean canGetOwner() {
		return false;
	}

	@Override
	public void changeDate(long arg0) throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
	}

	@Override
	public void changePermission(int arg0, int arg1, boolean arg2)
			throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
	}

	@Override
	public void copyRemotelyTo(AbstractFile arg0) throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
	}

	@Override
	public void delete() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.DELETE);
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public OutputStream getAppendOutputStream() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
	}

	@Override
	public PermissionBits getChangeablePermissions() {
		return null;
	}

	@Override
	public long getDate() {
		return 0;
	}

	@Override
	public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);
	}

	@Override
	public String getGroup() {
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);
	}

	@Override
	public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.READ_FILE);
	}

	@Override
	public String getOwner() {
		return null;
	}

	@Override
	public AbstractFile getParent() {
		return null;
	}

	@Override
	public FilePermissions getPermissions() {
		return null;
	}

	@Override
	public RandomAccessInputStream getRandomAccessInputStream()
			throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
	}

	@Override
	public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);
	}

	@Override
	public long getSize() {
		return -1;
	}

	@Override
	public long getTotalSpace() throws IOException,
			UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);
	}

	@Override
	public Object getUnderlyingFileObject() {
		return null;
	}

	@Override
	public boolean isArchive() {
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
	public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.LIST_CHILDREN);
	}

	@Override
	public void mkdir() throws IOException, UnsupportedFileOperationException {
	}

	@Override
	public void renameTo(AbstractFile arg0) throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(FileOperation.RENAME);
	}

	@Override
	public void setParent(AbstractFile arg0) {
	}
}