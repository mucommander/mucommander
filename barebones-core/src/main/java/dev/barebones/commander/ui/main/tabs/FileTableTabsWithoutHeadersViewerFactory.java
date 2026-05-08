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

package dev.barebones.commander.ui.main.tabs;

import dev.barebones.commander.ui.main.FolderPanel;
import dev.barebones.commander.ui.tabs.TabWithoutHeaderViewer;
import dev.barebones.commander.ui.tabs.TabsCollection;
import dev.barebones.commander.ui.tabs.TabsViewer;
import dev.barebones.commander.ui.tabs.TabsViewerFactory;

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
