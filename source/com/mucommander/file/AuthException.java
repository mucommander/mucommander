
package com.mucommander.file;

import java.io.IOException;

public class AuthException extends IOException {

	private FileURL fileURL;
	
	public AuthException(FileURL fileURL) {
		this(fileURL, null);
	}
	
	public AuthException(FileURL fileURL, String msg) {
		this.fileURL = fileURL;
		this.msg = msg;
	}
	
	public FileURL getFileURL() {
		return fileURL;
	}

	public String getMessage() {
		return msg;
	}

}
