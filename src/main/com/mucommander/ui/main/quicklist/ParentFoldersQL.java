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
import java.util.Vector;

import javax.swing.Icon;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowParentFoldersQLAction;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.quicklist.QuickListWithIcons;

/**
 * This quick list shows the parent folders of the current location in the FileTable.
 * 
 * @author Arik Hadas
 */
public class ParentFoldersQL extends QuickListWithIcons<AbstractFile> implements LocationListener {
	
	private List<AbstractFile> parents = new Vector<AbstractFile>();
	private boolean updated = true;
	private FolderPanel folderPanel;
	
	public ParentFoldersQL(FolderPanel folderPanel) {
		super(folderPanel, ActionProperties.getActionLabel(ShowParentFoldersQLAction.Descriptor.ACTION_ID), Translator.get("parent_folders_quick_list.empty_message"));
		
		this.folderPanel = folderPanel;
		
		folderPanel.getLocationManager().addLocationListener(this);
	}
	
	@Override
    protected void acceptListItem(AbstractFile item) {
		folderPanel.tryChangeCurrentFolder(item);
	}
	
	protected void populateParentFolders(AbstractFile folder) {
		parents = new Vector<AbstractFile>();
				
		while((folder=folder.getParent())!=null)
            parents.add(folder);
    }
	
	@Override
    public AbstractFile[] getData() {
		if (!updated && (updated = true))
			populateParentFolders(folderPanel.getCurrentFolder());
		
		return parents.toArray(new AbstractFile[0]);
	}

	@Override
    protected Icon itemToIcon(AbstractFile item) {
		return getIconOfFile(item);
	}

	/**********************************
	 * LocationListener Implementation
	 **********************************/

	public void locationChanged(LocationEvent locationEvent) {
		updated = false;
	}
	
	public void locationChanging(LocationEvent locationEvent) { }

	public void locationCancelled(LocationEvent locationEvent) { }

	public void locationFailed(LocationEvent locationEvent) { }
}
