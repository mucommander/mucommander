/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.tabs.HideableTabbedPane;

/**
* HideableTabbedPane of FileTableTabs.
* 
* @author Arik Hadas
*/
public class FileTableTabs extends HideableTabbedPane<FileTableTab> implements LocationListener {

	/** FolderPanel containing those tabs */
	private FolderPanel folderPanel;
	
	public FileTableTabs(MainFrame mainFrame, FolderPanel folderPanel) {
		super(new FileTableTabsDisplayFactory(mainFrame, folderPanel));
		
		this.folderPanel = folderPanel;
		folderPanel.getLocationManager().addLocationListener(this);
		
		add(folderPanel.getCurrentFolder());
	}
	
	/**
	 * This function returns a list of tabs which are clones of the current tabs presented in the FolderPanel
	 * 
	 * @return List of clones of the current tabs
	 */
	public List<FileTableTab> getClonedTabs() {
		List<FileTableTab> tabs = new ArrayList<FileTableTab>();
		Iterator<FileTableTab> tabsIterator = getTabsIterator();
		while(tabsIterator.hasNext())
			tabs.add(tabsIterator.next().clone());
		return tabs;
	}
	
	/********************
	 * MuActions support
	 ********************/
	
	public void add(AbstractFile file) {
		addAndSelectTab(FileTableTab.create(file));
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

	/*******************
	 * Other Actions
	 *******************/
	
	public void close(FileTableTabHeader fileTableTabHeader) {
		removeTab(fileTableTabHeader);
	}
	
	/*******************
	 * LocationListener
	 *******************/
	
	@Override
	public void locationChanged(LocationEvent locationEvent) {
		updateTab(FileTableTab.create(folderPanel.getCurrentFolder()));
	}

	@Override
	public void locationCancelled(LocationEvent locationEvent) { }

	@Override
	public void locationFailed(LocationEvent locationEvent) { }
	
	@Override
	public void locationChanging(LocationEvent locationEvent) { }
}
