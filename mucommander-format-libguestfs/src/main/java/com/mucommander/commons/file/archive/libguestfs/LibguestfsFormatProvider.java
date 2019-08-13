package com.mucommander.commons.file.archive.libguestfs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.archive.AbstractArchiveFile;
import com.mucommander.commons.file.archive.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;

public class LibguestfsFormatProvider implements ArchiveFormatProvider {

	/** extensions of archive filenames */
	public static final String[] EXTENSIONS = new String[] {".qcow", ".qcow2", ".vmdk"};

	@Override
	public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
		return new LibguestfsArchiveFile(file);
	}

	@Override
	public FilenameFilter getFilenameFilter() {
		return new ExtensionFilenameFilter(EXTENSIONS);
	}

	@Override
	public List<String> getExtensions() {
		return Arrays.asList(EXTENSIONS);
	}
}
