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

package com.mucommander.ui.quicklist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

import com.mucommander.ui.quicklist.item.QuickListHeaderItem;

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
	protected QuickListHeaderItem headerMenuItem;
	private java.util.List<Component> items = new Vector<Component>();
	private QuickListContainer container;
	
	protected QuickList(QuickListContainer container, String header) {
		super();
		
		this.container = container;
		
		setBorder(new PopupsBorder());
		add(headerMenuItem = new QuickListHeaderItem(header));		
		setFocusTraversalKeysEnabled(false);
	}
	
	protected Component nextFocusableComponent() {
		return container.nextFocusableComponent();
	}
	
	/**
	 * This function is called before showing quick-list.
	 * If the return value is true, the quick list will be shown. Otherwise, it won't be shown.
	 */
	protected abstract boolean prepareForShowing(QuickListContainer container);
	
	public void show() {
        
		if (prepareForShowing(container)) {
			// Note: the actual popup menu's size is not known at this stage so we use the component's preferred size
	        Dimension dim = getPreferredSize();

	        Point location = container.calcQuickListPosition(dim);
	        
	        show(container.containerComponent(), location.x, location.y);
	        getFocus();
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

		for (Component item : items) {
			width = Math.max(width, item.getPreferredSize().getWidth());
			height += item.getPreferredSize().getHeight();
		}
		
		return new Dimension((int) Math.ceil(
				Math.max(container == null ? 0 : container.getWidth() / 2, width * 1.05))
				, (int) Math.ceil(height));
	}
	
	public void focusGained(FocusEvent arg0) {}

	public void focusLost(FocusEvent arg0) {
		setVisible(false);		
	}
	
	/**
	 * Get focus for the desired subcomponent. Only subclasses know which
	 * component to focus, so they must implement it.
	 */
	protected abstract void getFocus();
	
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
