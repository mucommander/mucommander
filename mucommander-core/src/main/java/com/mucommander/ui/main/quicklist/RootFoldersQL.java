/*
 * This file is part of muCommander, http://www.mucommander.com
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

import javax.swing.Icon;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.quicklist.QuickListWithIcons;

/**
 * This quick list shows roots of partitions.
 * 
 * @author Arik Hadas
 */
public class RootFoldersQL extends QuickListWithIcons<AbstractFile> {
	
	private FolderPanel folderPanel;
	
	public RootFoldersQL(FolderPanel folderPanel) {
		super(folderPanel, ActionProperties.getActionLabel(ActionType.ShowRootFoldersQL), Translator.get("roots_quick_list.empty_message"));
		
		this.folderPanel = folderPanel;
	}
	
	@Override
	protected Icon itemToIcon(AbstractFile item) {
		return FileIcons.hasProperSystemIcons()?FileIcons.getSystemFileIcon(item):null;
	}

	@Override
	protected AbstractFile[] getData() {
		return LocalFile.getVolumes();
	}

	@Override
	protected void acceptListItem(AbstractFile item) {
		folderPanel.tryChangeCurrentFolder(item);
	}
}
