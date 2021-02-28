package com.mucommander.commons.file.protocol.dropbox;

import java.io.IOException;
import java.util.Map;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.ProtocolProvider;

public class DropboxProtocolProvider implements ProtocolProvider {

	@Override
	public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) throws IOException {
		String path = url.getPath();
	    switch(path) {
	    case "/":
	        return new DropboxRoot(url);
	    default:
	        return new DropboxFile(url);
	    }
	}

}
