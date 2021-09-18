package com.mucommander.commons.file.protocol.registry;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.archive.tar.TarArchiveFile;
import com.mucommander.commons.file.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;

public class RegistryImage extends AbstractRegistryImage {

	private static Logger log = LoggerFactory.getLogger(RegistryImage.class);

	private RegistryConnHandler connHandler;
	private List<String> layers;
	private String tempFolder;

	protected RegistryImage(FileURL url) {
		super(url, null);
	}

	@Override
	public AbstractFile[] ls() throws IOException {
		if (!isCachedData()) {
			if (connHandler == null) {
				connHandler = getConnHandler();
			}
			layers = connHandler.getClient().getLayers();
			tempFolder = connHandler.getClient().getTempFolder();
		}
		fileURL.setPath(tempFolder);
		try {
			return layers.stream()
					.map(this::toFile)
					.filter(Objects::nonNull)
					.toArray(AbstractFile[]::new);
		} finally {
			if (connHandler != null) {
				connHandler.releaseLock();
				connHandler.closeConnection();
				connHandler = null;
			}
		}
	}

	private boolean isCachedData() {
		return layers != null && tempFolder != null;
	}

	private AbstractFile toFile(String layer) {
		String parentPath = PathUtils.removeTrailingSeparator(fileURL
				.getPath()) + AbstractFile.DEFAULT_SEPARATOR;
		FileURL childURL = (FileURL) fileURL.clone();
		childURL.setPath(parentPath + layer.split(":")[1]);
		childURL.setScheme("file");
		TarArchiveFile tar = new TarArchiveFile(FileFactory.getFile(childURL));
		tar.setCustomExtension("tar.gz");
		tar.setParent(this);
		return tar;
	}

	@Override
	public AbstractFile getParent() {
		return null;
	}

	@Override
	public void setParent(AbstractFile parent) {}
}
