package com.mucommander.commons.file.archive.libguestfs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.archive.AbstractRWArchiveFile;
import com.mucommander.commons.file.archive.ArchiveEntry;
import com.mucommander.commons.file.archive.ArchiveEntryIterator;
import com.mucommander.commons.file.archive.WrapperArchiveEntryIterator;
import com.redhat.et.libguestfs.LibGuestFSException;

public class LibguestfsArchiveFile extends AbstractRWArchiveFile {

	private LibguestfsFile libguestFile;

	protected LibguestfsArchiveFile(AbstractFile file) {
		super(file);
	}

	private void checkFile() throws IOException {
		if (libguestFile == null) {// || (currentDate = file.getDate()) != lastFileDate) {
			try {
				libguestFile = new LibguestfsFile(file);
			} catch (LibGuestFSException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public OutputStream addEntry(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
		checkFile();
		libguestFile.add(entry);
		addToEntriesTree(entry);
		return new BufferedOutputStream(new OutputStream() {
			int offset;
			File tmpFile = File.createTempFile(entry.getName()+"-", "-upload");

			@Override
			public void write(int arg0) throws IOException {
				throw new IOException("unsupported");
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				byte[] output = b;
				if (off > 0 || b.length != len) {
					output = new byte[len];
					System.arraycopy(b, off, output, 0, len);
				}
				Files.write(tmpFile.toPath(), output, StandardOpenOption.TRUNCATE_EXISTING);
				libguestFile.write(entry, tmpFile, offset);
				offset += len;
			}

			@Override
			public void close() throws IOException {
				super.close();
				tmpFile.delete();
				String path = "/"+entry.getPath();
				libguestFile.chmod(path);
			}
		});
	}

	@Override
	public void deleteEntry(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
		checkFile();
		libguestFile.delete(entry);
		removeFromEntriesTree(entry);
	}

	@Override
	public void updateEntry(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
		throw new UnsupportedFileOperationException(null);
	}

	@Override
	public void optimizeArchive() throws IOException, UnsupportedFileOperationException {
		// noop
	}

	@Override
	public ArchiveEntryIterator getEntryIterator() throws IOException, UnsupportedFileOperationException {
		checkFile();
		try {
			return new WrapperArchiveEntryIterator(libguestFile.ls().iterator());
		} catch (LibGuestFSException e) {
			throw new IOException(e);
		}
	}

	@Override
	public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator)
			throws IOException, UnsupportedFileOperationException {
		return new BufferedInputStream(new InputStream() {
			long remaining = entry.getSize();
			File tmpFile = File.createTempFile(entry.getName()+"-", "-download");

			@Override
			public int read() throws IOException {
				throw new IOException("unsupported");
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				if (len > remaining) {
					len = (int) remaining;
				}
				if (len == 0) {
					return -1;
				}
				libguestFile.read(tmpFile, entry, off, len);
				byte[] output = Files.readAllBytes(tmpFile.toPath());
				System.arraycopy(output, 0, b, 0, len);
				remaining = remaining - len;
				return len;
			}

			@Override
			public void close() throws IOException {
				super.close();
				tmpFile.delete();
			}
		});
	}
}
