package com.mucommander.ui.main.tabs;

import com.mucommander.commons.file.FileURL;
import com.mucommander.core.LocalLocationHistory;

public class ConfFileTableTab implements FileTableTab {

	private boolean lock;
	private FileURL location;
	private String title;
	
	public ConfFileTableTab(FileURL location) {
		this(false, location, null);
	}

	public ConfFileTableTab(boolean lock, FileURL location, String title) {
		this.lock = lock;
		this.location = location;
		this.title = title;
	}

	public boolean isLocked() {
		return lock;
	}

	public FileURL getLocation() {
		return location;
	}

	public String getTitle() {
		return title;
	}
	
	public void setLocation(FileURL location) {
		throw new UnsupportedOperationException("cannot change location of configuration tab");
	}

	public void setLocked(boolean locked) {
		throw new UnsupportedOperationException("cannot lock configuration tab");
	}

	public void setTitle(String title) {
		throw new UnsupportedOperationException("cannot change title of configuration tab");
	}

	public LocalLocationHistory getLocationHistory() {
		return null;
	}
}
