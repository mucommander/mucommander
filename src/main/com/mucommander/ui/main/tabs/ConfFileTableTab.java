package com.mucommander.ui.main.tabs;

import com.mucommander.commons.file.FileURL;
import com.mucommander.core.LocalLocationHistory;

public class ConfFileTableTab extends FileTableTab {

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

	@Override
	public boolean isLocked() {
		return lock;
	}

	@Override
	public FileURL getLocation() {
		return location;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setLocation(FileURL location) {
		throw new UnsupportedOperationException("cannot change location of configuration tab");
	}

	@Override
	public void setLocked(boolean locked) {
		throw new UnsupportedOperationException("cannot lock configuration tab");
	}

	@Override
	public void setTitle(String title) {
		throw new UnsupportedOperationException("cannot change title of configuration tab");
	}

	@Override
	public LocalLocationHistory getLocationHistory() {
		return null;
	}
}
