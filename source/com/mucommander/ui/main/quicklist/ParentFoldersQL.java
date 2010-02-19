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

package com.mucommander.ui.main.quicklist;

import com.mucommander.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowParentFoldersQLAction;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.quicklist.QuickListWithIcons;

import javax.swing.Icon;
import java.util.Vector;

/**
 * This quick list shows the parent folders of the current location in the FileTable.
 * 
 * @author Arik Hadas
 */
public class ParentFoldersQL extends QuickListWithIcons implements LocationListener {
	protected Vector<AbstractFile> parents = new Vector<AbstractFile>();
	protected boolean updated = true;
		
	public ParentFoldersQL(FolderPanel folderPanel) {
		super(ActionProperties.getActionLabel(ShowParentFoldersQLAction.Descriptor.ACTION_ID), Translator.get("parent_folders_quick_list.empty_message"));
		
		folderPanel.getLocationManager().addLocationListener(this);		
	}
	
	@Override
    protected void acceptListItem(Object item) {
		folderPanel.tryChangeCurrentFolder((AbstractFile)item);
	}
	
	public void locationChanged(LocationEvent locationEvent) {
		updated = false;
	}
	
	protected void populateParentFolders(AbstractFile folder) {
		parents = new Vector<AbstractFile>();
				
		while((folder=folder.getParent())!=null)
            parents.add(folder);
    }
	
	public void locationCancelled(LocationEvent locationEvent) {}

	public void locationChanging(LocationEvent locationEvent) {}

	public void locationFailed(LocationEvent locationEvent) {}	

	@Override
    public Object[] getData() {
		if (!updated && (updated = true))
			populateParentFolders(folderPanel.getCurrentFolder());
		
		return parents.toArray();
	}

	@Override
    protected Icon itemToIcon(Object item) {
		return getIconOfFile((AbstractFile)item);
	}
}
