package dev.barebones.commander.commons.file.archive.libguestfs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.archive.AbstractArchiveFile;
import dev.barebones.commander.commons.file.archive.ArchiveFormatProvider;
import dev.barebones.commander.commons.file.filter.ExtensionFilenameFilter;
import dev.barebones.commander.commons.file.filter.FilenameFilter;

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
