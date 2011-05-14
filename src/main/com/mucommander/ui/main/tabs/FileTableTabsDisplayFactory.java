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

import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.tabs.MultipleTabsDisplay;
import com.mucommander.ui.tabs.SingleTabDisplay;
import com.mucommander.ui.tabs.TabsCollection;
import com.mucommander.ui.tabs.TabsDisplayFactory;

/**
* Factory that creates displays for file table tabs
* 
* @author Arik Hadas
*/
public class FileTableTabsDisplayFactory implements TabsDisplayFactory<FileTableTab> {

	private FileTable fileTable;
	
	public FileTableTabsDisplayFactory(FileTable fileTable) {
		this.fileTable = fileTable;
	}

	@Override
	public MultipleTabsDisplay<FileTableTab> createMultipleTabsDisplay(TabsCollection<FileTableTab> tabs) {
		return createMultipleTabsDisplay(tabs, 0);
	}
	
	@Override
	public MultipleTabsDisplay<FileTableTab> createMultipleTabsDisplay(TabsCollection<FileTableTab> tabs, int selectedTabIndex) {
		MultipleTabsDisplay<FileTableTab> multipleTabsDisplay = new MultipleTabsDisplay<FileTableTab>(tabs, new FileTableTabbedPane(fileTable.getAsUIComponent()));
		multipleTabsDisplay.setSelectedTabIndex(selectedTabIndex);
		return multipleTabsDisplay;
	}

	@Override
	public SingleTabDisplay<FileTableTab> createSingleTabsDisplay(TabsCollection<FileTableTab> tabs) {
		return new SingleTabDisplay<FileTableTab>(tabs, fileTable.getAsUIComponent());
	}
}
