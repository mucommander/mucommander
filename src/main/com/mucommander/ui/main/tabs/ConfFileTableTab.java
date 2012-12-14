package com.mucommander.ui.main.tabs;

import com.mucommander.commons.file.FileURL;
import com.mucommander.core.LocalLocationHistory;

public class ConfFileTableTab implements FileTableTab {

	private boolean lock;
	private FileURL location;
	
	public ConfFileTableTab(FileURL location) {
		this(false, location);
	}
	
	public ConfFileTableTab(boolean lock, FileURL location) {
		this.lock = lock;
		this.location = location;
	}

	public boolean isLocked() {
		return lock;
	}

	public FileURL getLocation() {
		return location;
	}
	
	public void setLocation(FileURL location) {
		throw new UnsupportedOperationException("cannot change location of configuration tab");
	}

	public void setLocked(boolean locked) {
		throw new UnsupportedOperationException("cannot lock configuration tab");
	}

	public LocalLocationHistory getLocationHistory() {
		return null;
	}
}
