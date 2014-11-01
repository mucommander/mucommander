/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main.tabs;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.tabs.HideableTabbedPane;
import com.mucommander.ui.tabs.TabFactory;
import com.mucommander.ui.tabs.TabUpdater;
import com.mucommander.utils.Callback;

/**
* HideableTabbedPane of {@link com.mucommander.ui.main.tabs.FileTableTab} instances.
* 
* @author Arik Hadas
*/
public class FileTableTabs extends HideableTabbedPane<FileTableTab> implements LocationListener {

	/** FolderPanel containing those tabs */
	private FolderPanel folderPanel;

	/** Factory of instances of FileTableTab */
	private TabFactory<FileTableTab, FileURL> defaultTabsFactory;

	/** Factory of instances of FileTableTab */
	private TabFactory<FileTableTab, FileTableTab> clonedTabsFactory;

	public FileTableTabs(MainFrame mainFrame, FolderPanel folderPanel, ConfFileTableTab[] initialTabs) {
		super(new FileTableTabsWithoutHeadersViewerFactory(folderPanel), new FileTableTabsWithHeadersViewerFactory(mainFrame, folderPanel));

		this.folderPanel = folderPanel;

		defaultTabsFactory = new DefaultFileTableTabFactory(folderPanel);
		clonedTabsFactory = new ClonedFileTableTabFactory(folderPanel);

		// Register to location change events
		folderPanel.getLocationManager().addLocationListener(this);

		// Add the initial folders
		for (FileTableTab tab : initialTabs)
			addTab(clonedTabsFactory.createTab(tab));
	}

	@Override
	public void selectTab(int index) {
		super.selectTab(index);

		show(index);
	}

	@Override
	protected void show(final int tabIndex) {
		folderPanel.tryChangeCurrentFolderInternal(getTab(tabIndex).getLocation(), new Callback() {
			public void call() {
				fireActiveTabChanged();
			}
		});
	};

	/**
	 * Return the currently selected tab
	 * 
	 * @return currently selected tab
	 */
	public FileTableTab getCurrentTab() {
		return getTab(getSelectedIndex());
	}

	private void updateTabLocation(final FileURL location) {
		updateCurrentTab(new TabUpdater<FileTableTab>() {

			public void update(FileTableTab tab) {
				tab.setLocation(location);
			}
		});
	}
	
	private void updateTabLocking(final boolean lock) {
		updateCurrentTab(new TabUpdater<FileTableTab>() {
			
			public void update(FileTableTab tab) {
				tab.setLocked(lock);
			}
		});
	}

	private void updateTabTitle(final String title) {
		updateCurrentTab(new TabUpdater<FileTableTab>() {

			public void update(FileTableTab tab) {
				tab.setTitle(title);
			}
		});
	}

	@Override
	protected boolean showSingleTabHeader() {
		int nbTabs = getTabs().count();
		
		if (nbTabs == 1) {
			FileTableTab tab = getTab(0);
			
			// If there's just single tab that is locked don't remove his header
			if (tab.isLocked())
				return true;
		}
		
		return super.showSingleTabHeader();
	}
	
	@Override
	protected FileTableTab removeTab() {
		return !getCurrentTab().isLocked() ? super.removeTab() : null;
	}
	
	/********************
	 * MuActions support
	 ********************/
	
	public void add(AbstractFile file) {
		addTab(defaultTabsFactory.createTab(file.getURL()));
	}
	
	public void add(FileTableTab tab) {
		addAndSelectTab(tab);
	}
	
	public FileTableTab closeCurrentTab() {
		return removeTab();
	}
	
	public void closeDuplicateTabs() {
		removeDuplicateTabs();
	}
	
	public void closeOtherTabs() {
		removeOtherTabs();
	}
	
	public void duplicate() {
		add(clonedTabsFactory.createTab(getCurrentTab()));
	}
	
	public void lock() {
		updateTabLocking(true);
	}
	
	public void unlock() {
		updateTabLocking(false);
	}

	public void setTitle(String title) {
		updateTabTitle(title);
	}

	/****************
	 * Other Actions
	 ****************/
	
	public void close(FileTableTabHeader fileTableTabHeader) {
		removeTab(fileTableTabHeader);
	}
	
	/**********************************
	 * LocationListener Implementation
	 **********************************/
	
	public void locationChanged(LocationEvent locationEvent) {
		updateTabLocation(folderPanel.getCurrentFolder().getURL());
	}

	public void locationCancelled(LocationEvent locationEvent) {
		updateTabLocation(folderPanel.getCurrentFolder().getURL());
	}

	public void locationFailed(LocationEvent locationEvent) {
		updateTabLocation(folderPanel.getCurrentFolder().getURL());
	}
	
	public void locationChanging(LocationEvent locationEvent) { }
}
