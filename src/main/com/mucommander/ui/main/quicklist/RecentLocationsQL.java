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

import java.util.List;

import javax.swing.Icon;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.core.GlobalLocationHistory;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowRecentLocationsQLAction;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.quicklist.QuickListWithIcons;

/**
 * This quick list shows recently accessed locations.
 * 
 * @author Arik Hadas
 */
public class RecentLocationsQL extends QuickListWithIcons<FileURL> {
	
	private static int MAX_ELEMENTS = 15;

	private FolderPanel folderPanel;
	
	public RecentLocationsQL(FolderPanel folderPanel) {
		super(folderPanel, ActionProperties.getActionLabel(ShowRecentLocationsQLAction.Descriptor.ACTION_ID), Translator.get("recent_locations_quick_list.empty_message"));
		
		this.folderPanel = folderPanel;
	}

	@Override
    protected void acceptListItem(FileURL item) {
		folderPanel.tryChangeCurrentFolder(item);
	}

	@Override
    public FileURL[] getData() {
		List<FileURL> list = GlobalLocationHistory.Instance().getHistory();
		
		// Remove currently presented location from the list
		list.remove(folderPanel.getCurrentFolder().getURL());

		return list.toArray(new FileURL[0]);
	}

	@Override
    protected Icon itemToIcon(FileURL item) {
		return getIconOfFile(FileFactory.getFile(item));
	}
}
