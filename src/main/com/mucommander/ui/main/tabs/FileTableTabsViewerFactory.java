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

import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.tabs.TabWithoutHeaderViewer;
import com.mucommander.ui.tabs.TabsCollection;
import com.mucommander.ui.tabs.TabsViewerFactory;
import com.mucommander.ui.tabs.TabsWithHeaderViewer;

/**
* Factory that creates displays for file table tabs
* 
* @author Arik Hadas
*/
public class FileTableTabsViewerFactory implements TabsViewerFactory<FileTableTab> {

	private FolderPanel folderPanel;
	private MainFrame mainFrame;
	
	public FileTableTabsViewerFactory(MainFrame mainFrame, FolderPanel folderPanel) {
		this.folderPanel = folderPanel;
		this.mainFrame = mainFrame;
	}

	/************************************
	 * TabsDisplayFactory Implementation
	 ************************************/
	
	public TabsWithHeaderViewer<FileTableTab> createTabsWithHeadersDisplay(TabsCollection<FileTableTab> tabs) {
		return new TabsWithHeaderViewer<FileTableTab>(tabs, new FileTableTabbedPane(mainFrame, folderPanel, folderPanel.getFileTable().getAsUIComponent()));
	}

	public TabWithoutHeaderViewer<FileTableTab> createTabWithoutHeaderDisplay(TabsCollection<FileTableTab> tabs) {
		return new TabWithoutHeaderViewer<FileTableTab>(tabs, folderPanel.getFileTable().getAsUIComponent());
	}
}
