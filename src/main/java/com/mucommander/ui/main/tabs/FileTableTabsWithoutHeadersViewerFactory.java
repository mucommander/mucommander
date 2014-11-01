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
import com.mucommander.ui.tabs.TabWithoutHeaderViewer;
import com.mucommander.ui.tabs.TabsCollection;
import com.mucommander.ui.tabs.TabsViewer;
import com.mucommander.ui.tabs.TabsViewerFactory;

/**
* Factory that creates viewers presenting tabs with no header
* 
* @author Arik Hadas
*/
public class FileTableTabsWithoutHeadersViewerFactory implements TabsViewerFactory<FileTableTab> {

	private FolderPanel folderPanel;
	
	public FileTableTabsWithoutHeadersViewerFactory(FolderPanel folderPanel) {
		this.folderPanel = folderPanel;
	}

	/***********************************
	 * TabsViewerFactory Implementation
	 ***********************************/

	public TabsViewer<FileTableTab> create(TabsCollection<FileTableTab> tabs) {
		return new TabWithoutHeaderViewer<FileTableTab>(tabs, folderPanel.getFileTable().getAsUIComponent());
	}
}
