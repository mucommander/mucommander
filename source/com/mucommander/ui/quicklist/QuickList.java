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

package com.mucommander.ui.quicklist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.quicklist.item.HeaderMenuItem;

/**
 * This abstract class contains some common features to all file table's popups:
 * 1. add HeaderMenuItem as the first item.
 * 2. set custom line border.
 * 3. does the calculations needed in order to show the popup in the center of the
 * 	  invoker FolderPanel. 
 * 
 * @author Arik Hadas
 */

public abstract class QuickList extends JPopupMenu implements FocusListener {
	protected HeaderMenuItem headerMenuItem;
	protected FolderPanel folderPanel;
	
	protected QuickList(String header) {
		super();
				
		setBorder(new PopupsBorder());
		add(headerMenuItem = new HeaderMenuItem(header));		
		setFocusTraversalKeysEnabled(false);
	}
	
	public void show(FolderPanel folderPanel) {
		this.folderPanel = folderPanel;
		int y = folderPanel.getLocationTextField().getHeight();
		// the show function is called twice because I couln't find any other way to
		// make the popup return it's real dimensions - only after calling show method
		// the getWidth and getHeight return real values.
		show(folderPanel, folderPanel.getWidth() / 2, y);
		int x = (x  = ((folderPanel.getWidth() - getWidth()) / 2)) < 0 ? 0 : x;
		y += (y = (folderPanel.getHeight() - getHeight()) / 3) < 0 ? 0 : y;
		show(folderPanel, x, y);
	}
	
	public FolderPanel getPanel() {
		return folderPanel;
	}	
	
	public void focusGained(FocusEvent arg0) {}

	public void focusLost(FocusEvent arg0) {
		setVisible(false);		
	}
	
	private class PopupsBorder extends LineBorder {		
		public PopupsBorder() {
			super(Color.gray);		
		}
		
		public Insets getBorderInsets(Component c) {
			return new Insets(1,1,1,1);
		}
		
		public Insets getBorderInsets(Component c, Insets i) {
			return new Insets(1,1,1,1);
		}
	}	
}
