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

package com.mucommander.ui.quicklist;

import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.quicklist.item.HeaderMenuItem;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

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
	private static final int PADDING = 2;
	protected HeaderMenuItem headerMenuItem;
	protected FolderPanel folderPanel;
	private Vector<Component> items = new Vector<Component>();
	
	protected QuickList(String header) {
		super();
				
		setBorder(new PopupsBorder());
		add(headerMenuItem = new HeaderMenuItem(header));		
		setFocusTraversalKeysEnabled(false);
	}
	
	/**
	 * This function is called before showing quick-list.
	 * If the return value is true, the quick list will be shown. Otherwise, it won't be shown.
	 * 
	 * @return
	 */
	protected abstract boolean prepareForShowing();
	
	public void show(FolderPanel folderPanel) {
		this.folderPanel = folderPanel;
        
		if (prepareForShowing()) {
	     // Note: the actual popup menu's size is not known at this stage so we use the component's preferred size
	        Dimension dim = getPreferredSize();
	
	        int x = Math.max((folderPanel.getWidth() - (int)dim.getWidth()) / 2, 0);
	        int y = folderPanel.getLocationTextField().getHeight() + Math.max((folderPanel.getHeight() - (int)dim.getHeight()) / 3, 0);
	        
	        show(folderPanel, x, y);
		}
	}
	
	@Override
    public Component add(Component comp) {
		items.add(comp);
		return super.add(comp);
	}
	
	@Override
    public JMenuItem add(JMenuItem comp) {
		items.add(comp);
		return super.add(comp);
	}
	
	@Override
    public Dimension getPreferredSize() {
		double width = PADDING, height = PADDING;
		int nbItems = items.size();
		for (int i=0; i<nbItems; i++) {
			width = Math.max(width, items.elementAt(i).getPreferredSize().getWidth());
			height += items.elementAt(i).getPreferredSize().getHeight();
		}
		return new Dimension((int) Math.ceil(
				Math.max(folderPanel == null ? 0 : folderPanel.getWidth() / 2, width * 1.05))
				, (int) Math.ceil(height));
	}
	
	public FolderPanel getPanel() {
		return folderPanel;
	}	
	
	public void focusGained(FocusEvent arg0) {}

	public void focusLost(FocusEvent arg0) {
		setVisible(false);		
	}
	
	public static class PopupsBorder extends LineBorder {
		public PopupsBorder() {
			super(Color.gray);		
		}
		
		@Override
        public Insets getBorderInsets(Component c) {
			return new Insets(1,1,1,1);
		}
		
		@Override
        public Insets getBorderInsets(Component c, Insets i) {
			return new Insets(1,1,1,1);
		}
	}	
}
