package dev.barebones.commander.commons.file.protocol.dropbox;

import dev.barebones.commander.commons.file.FileURL;

public class DropboxRoot extends DropboxFile {

	protected DropboxRoot(FileURL url) {
		super(url);
	}

	@Override
	protected String getId() {
		return "";
	}

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isRoot() {
        return true;
    }
}
