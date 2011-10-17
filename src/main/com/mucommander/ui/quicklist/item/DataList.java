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

package com.mucommander.ui.quicklist.item;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import com.mucommander.ui.main.table.CellLabel;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.quicklist.QuickListFocusableComponent;
import com.mucommander.ui.quicklist.QuickListWithDataList;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

/**
 * This class represent a data list for FileTablePopupWithDataList.
 * 
 * @author Arik Hadas
 */

public class DataList<T> extends JList implements QuickListFocusableComponent {	

	private final static int VISIBLE_ROWS_COUNT = 10;


	public DataList(){
		setFocusTraversalKeysEnabled(false);

		addMouseListenerToList();
		addKeyListenerToList();

		DataListItemRenderer itemRenderer = getItemRenderer();
		ThemeManager.addCurrentThemeListener(itemRenderer);
		setCellRenderer(itemRenderer);
	}

	public DataList(T[] data) {
		this();
		setListData(data);
	}

	protected DataListItemRenderer getItemRenderer() {
		return new DataListItemRenderer();
	}
	
	/**
	 * This function is called before showing TablePopupWithDataList.
	 * It does the required steps before the popup is shown.	
	 */
	@Override
	public void setListData(Object[] data) {
		super.setListData(data);

		int numOfRowsInList = getModel().getSize();
		if (numOfRowsInList > 0) {
			setVisibleRowCount(Math.min(numOfRowsInList, VISIBLE_ROWS_COUNT));
			setSelectedIndex(0);
			ensureIndexIsVisible(0);
		}
	}

	protected T getListItem(int index) {
		if (index > getModel().getSize() || index < 0)
			return null;

		return (T) getModel().getElementAt(index);
	}

	protected void addKeyListenerToList() {
		addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {			
				switch(e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					((QuickListWithDataList)(getParent().getParent().getParent())).itemSelected(getSelectedValue());
					break;
				case KeyEvent.VK_UP:
				{
					int numOfItems = getModel().getSize();				
					if (numOfItems > 0 && getSelectedIndex() == 0) {
						setSelectedIndex(numOfItems - 1);
						ensureIndexIsVisible(numOfItems - 1);
						e.consume();
					}
				}
				break;
				case KeyEvent.VK_DOWN:
				{
					int numOfItems = getModel().getSize();
					if (numOfItems > 0 && getSelectedIndex() == numOfItems - 1) {				
						setSelectedIndex(0);
						ensureIndexIsVisible(0);
						e.consume();
					}						
				}
				break;
				case KeyEvent.VK_TAB:
					getInvokerFileTable().requestFocus();
					break;
				}				
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});
	}

	protected void addMouseListenerToList() {
		addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				// If there was double click on item of the popup's list, 
				// select it, and update the text component.
				if (e.getClickCount() == 2) {
					int index = locationToIndex(e.getPoint());
					setSelectedIndex(index);
					((QuickListWithDataList)(getParent().getParent().getParent())).itemSelected(getSelectedValue());
				}
			}
		});
	}

	public void getFocus(){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				requestFocus();
			}
		});
	}

	public FileTable getInvokerFileTable() {
		return ((QuickListWithDataList)(getParent().getParent().getParent())).getPanel().getFileTable(); 
	}

	public void setForegroundColors(Color foreground, Color selectedForeground) {
		DataListItemRenderer cellRenderer = (DataListItemRenderer) getCellRenderer();
		cellRenderer.setItemForeground(foreground);
		cellRenderer.setSelectedItemForeground(selectedForeground);
	}

	public void setBackgroundColors(Color background, Color selectedBackground) {
		DataListItemRenderer cellRenderer = (DataListItemRenderer) getCellRenderer();
		cellRenderer.setItemBackgound(background);
		cellRenderer.setSelectedItemBackgound(selectedBackground);
	}

	protected class DataListItemRenderer extends DefaultListCellRenderer implements ThemeListener {

		private Color selectedItemBackgound = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR);
		private Color selectedItemForeground = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR);
		private Color itemBackgound = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR);
		private Color itemForeground = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR);
		
		private Font itemFont = ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_ITEM_FONT);

		protected DataListItemRenderer() { }

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int rowIndex, boolean isSelected, boolean cellHasFocus) {
			// Let superclass deal with most of it...
			super.getListCellRendererComponent(list, value, rowIndex, isSelected, cellHasFocus);

			T item = getListItem(rowIndex);

			/*			
			boolean matches = false;

	        // Sanity check.
	        T file = (T) list.getModel().getElementAt(rowIndex);
	        if(file==null) {
	            AppLogger.fine("tableModel.getCachedFileAtRow("+ rowIndex +") RETURNED NULL !");
	            return null;
	        }
	        System.out.println("file: " + file + " at " + rowIndex);

	        DataList myList = (DataList) list;

	        FileTableQuickSearch search = myList.getQuickSearch();
	        if(!myList.hasFocus())
	            matches = true;
	        else {
	            if(search.isActive())
	                matches = search.matches(myList.name(file));
	            else
	                matches = true;
	        }
			 */	        
			CellLabel label = new CellLabel();
			label.setFont(itemFont);

			label.setText(""+item);
			//label.setToolTipText(""+item);

			// Set background color depending on whether the row is selected or not, and whether the table has focus or not
			label.setBackground(isSelected ? selectedItemBackgound : itemBackgound);
			label.setForeground(isSelected ? selectedItemForeground : itemForeground);

			return label;
		}
		
		public void setSelectedItemBackgound(Color selectedItemBackgound) {
			this.selectedItemBackgound = selectedItemBackgound;
		}

		public void setSelectedItemForeground(Color selectedItemForeground) {
			this.selectedItemForeground = selectedItemForeground;
		}

		public void setItemBackgound(Color itemBackgound) {
			this.itemBackgound = itemBackgound;
		}

		public void setItemForeground(Color itemForeground) {
			this.itemForeground = itemForeground;
		}

		//////////////////////////////////
		// ThemeListener implementation //
		//////////////////////////////////

		@Override
		public void colorChanged(ColorChangedEvent event) {		
			if (event.getColorId() == ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR)
				itemBackgound = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR);

			else if (event.getColorId() == ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR)
				itemForeground = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR);

			else if (event.getColorId() == ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR)
				selectedItemBackgound= ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR);

			else if (event.getColorId() == ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR)
				selectedItemForeground = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR);
		}

		@Override
		public void fontChanged(FontChangedEvent event) {
			itemFont = ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_ITEM_FONT);		
		}
	}
}
