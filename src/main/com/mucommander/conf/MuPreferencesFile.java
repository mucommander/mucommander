package com.mucommander.conf;

import java.io.FileNotFoundException;

class MuPreferencesFile extends MuConfigurationFile {
	
	private static final String       DEFAULT_PREFERENCES_FILE_NAME = "preferences.xml";
	
	public static MuPreferencesFile getPreferencesFile(String path) throws FileNotFoundException {
		return new MuPreferencesFile(path);
	}
	
	public static MuPreferencesFile getPreferencesFile() {
		try {
			return new MuPreferencesFile(null);
		} catch (FileNotFoundException e) {
			// Not possible exception
			return null;
		}
	}
	
	private MuPreferencesFile(String path) throws FileNotFoundException {
		super(path, DEFAULT_PREFERENCES_FILE_NAME);
	}
}
