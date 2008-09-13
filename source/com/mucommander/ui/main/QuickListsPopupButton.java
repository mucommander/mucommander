/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.ShowBookmarksQLAction;
import com.mucommander.ui.action.ShowParentFoldersQLAction;
import com.mucommander.ui.action.ShowRecentLocationsQLAction;
import com.mucommander.ui.action.ShowRecentExecutedFilesQLAction;
import com.mucommander.ui.button.PopupButton;

/**
 * This button contains the existing quick lists for FileTable.
 * 
 * @author Arik Hadas
 */
public class QuickListsPopupButton extends PopupButton {
	private FolderPanel folderPanel;
	private JPopupMenu popupMenu;
	
	public QuickListsPopupButton(FolderPanel panel) {
		folderPanel = panel;
		setPopupMenuLocation(PopupButton.BUTTOM_LEFT_ORIENTED);
		
		popupMenu = new JPopupMenu();
		// add item for ShowParentFoldersQLAction.
		addShowQuickListAction(popupMenu, ShowParentFoldersQLAction.class);
		// add item for ShowRecentLocationsQLAction.
		addShowQuickListAction(popupMenu, ShowRecentLocationsQLAction.class);
		// add item for ShowRecentExecutedFilesQLAction.
		addShowQuickListAction(popupMenu, ShowRecentExecutedFilesQLAction.class);
		// add item for ShowBookmarksQLAction.
		addShowQuickListAction(popupMenu, ShowBookmarksQLAction.class);
	}
	
	private void addShowQuickListAction(JPopupMenu menu, final Class action) {
		JMenuItem item = new JMenuItem(MuAction.getStandardLabel(action));
		item.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				ActionManager.performAction(action, folderPanel.getMainFrame());
			}});
		menu.add(item);
	}
	
	public JPopupMenu getPopupMenu() {
		folderPanel.getFileTable().requestFocus();
		return popupMenu;
	}
	
	public Dimension getPreferredSize() {
        // Limit button's maximum width to something reasonable and leave enough space for location field, 
        // as bookmarks name can be as long as users want them to be.
        // Note: would be better to use JButton.setMaximumSize() but it doesn't seem to work
        Dimension d = super.getPreferredSize();
        if(d.width > 20)
            d.width = 20;
        d.height = d.width;
        return d;
    }
}
