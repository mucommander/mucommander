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

package com.mucommander.ui.main.quicklist;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowTabsQLAction;
import com.mucommander.ui.icon.EmptyIcon;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.tabs.FileTableTab;
import com.mucommander.ui.main.tabs.FileTableTabHeader;
import com.mucommander.ui.main.tabs.PrintableFileTableTabFactory;
import com.mucommander.ui.quicklist.QuickListWithIcons;
import com.mucommander.ui.tabs.TabFactory;

/**
 * This quick list shows the tabs contained in the FolderPanel.
 * 
 * @author Arik Hadas
 */
public class TabsQL extends QuickListWithIcons<FileTableTab> {

	/** The FolderPanel that contains the tabs */
	private FolderPanel folderPanel;
	
	private TabFactory<FileTableTab, FileTableTab> tabsFactory = new PrintableFileTableTabFactory();
	
	Icon lockedTabIcon = IconManager.getIcon(IconManager.COMMON_ICON_SET, FileTableTabHeader.LOCKED_ICON_NAME);
	Icon unlockedTabIcon = new EmptyIcon(8, 9);
	
	public TabsQL(FolderPanel folderPanel) {
		super(folderPanel, ActionProperties.getActionLabel(ShowTabsQLAction.Descriptor.ACTION_ID), Translator.get("tabs_quick_list.empty_message"));
		
		this.folderPanel = folderPanel;
	}

	@Override
	protected Icon getImageIconOfItemImp(final FileTableTab item,  final Dimension preferredSize) {
		return itemToIcon(item);
	}

	@Override
	protected Icon itemToIcon(FileTableTab item) {
		return item.isLocked() ? lockedTabIcon : unlockedTabIcon;
	}

	@Override
	protected FileTableTab[] getData() {
		List<FileTableTab> tabsList = new ArrayList<FileTableTab>();
		Iterator<FileTableTab> tabsIterator = folderPanel.getTabs().iterator();
		
		while(tabsIterator.hasNext())
			tabsList.add(tabsFactory.createTab(tabsIterator.next()));
		
		// Remove the selected tab from the list
		tabsList.remove(folderPanel.getTabs().getSelectedIndex());
		
		return tabsList.toArray(new FileTableTab[0]);
	}

	@Override
	protected void acceptListItem(FileTableTab item) {
		folderPanel.getTabs().selectTab(item);
	}
}
