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

package com.mucommander.ui.quicklist.item;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.table.CellLabel;
import com.mucommander.ui.quicklist.QuickListWithDataList;
import com.mucommander.ui.quicksearch.QuickSearch;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.ThemeCache;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

/**
 * This class represent a data list for FileTablePopupWithDataList.
 * 
 * @author Arik Hadas
 */

public class QuickListDataList<T> extends JList {	
	private static final Logger LOGGER = LoggerFactory.getLogger(QuickListDataList.class);
	
	private final static int VISIBLE_ROWS_COUNT = 10;

	private QuickSearch<T> quickSearch = new QuickListQuickSearch();
	
	private Component nextFocusableComponent;

	public QuickListDataList(Component nextFocusableComponent){
		this.nextFocusableComponent = nextFocusableComponent;
		
		setFocusTraversalKeysEnabled(false);

		addMouseListenerToList();

		DataListItemRenderer itemRenderer = getItemRenderer();
		ThemeManager.addCurrentThemeListener(itemRenderer);
		setCellRenderer(itemRenderer);
	}

	public QuickListDataList(T[] data) {
		this(new Component() {});
		setListData(data);
	}

	protected DataListItemRenderer getItemRenderer() {
		return new DataListItemRenderer();
	}

	public QuickSearch<T> getQuickSearch() { 
		return quickSearch;
	}
	
	public String getItemAsString(T item) {
		return ""+item;
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

	/**
	 * 
	 */
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

			// Sanity check.
			if(item==null) {
				LOGGER.debug("tableModel.getCachedFileAtRow("+ rowIndex +") RETURNED NULL !");
				return null;
			}

			QuickSearch<T> search = QuickListDataList.this.getQuickSearch();
			boolean matches = search.isActive() ? search.matches(getItemAsString(item)) : true;

			CellLabel label = new CellLabel();
			label.setFont(itemFont);

			label.setText(getItemAsString(item));
			//label.setToolTipText(""+item);

			// Set background color depending on whether the row is selected or not, and whether the table has focus or not
			if (isSelected) {
				label.setBackground(selectedItemBackgound);
				label.setForeground(selectedItemForeground);
			}
			else {
				label.setBackground(matches ? itemBackgound : ThemeCache.unmatchedBackground);
				label.setForeground(matches ? itemForeground : ThemeCache.unmatchedForeground);
			}
			
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

		public void fontChanged(FontChangedEvent event) {
			itemFont = ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_ITEM_FONT);		
		}
	}

	/**
	 * 
	 */
	public class QuickListQuickSearch extends QuickSearch<T> {

		public QuickListQuickSearch() {
			super(QuickListDataList.this);
		}

		@Override
		protected void searchStarted() {
		}

		@Override
		protected void searchStopped() {
			WindowManager.getCurrentMainFrame().getStatusBar().updateSelectedFilesInfo();
			QuickListDataList.this.repaint();
		}

		@Override
		protected int getNumOfItems() {
			return QuickListDataList.this.getModel().getSize();
		}

		@Override
		protected String getItemString(int index) {
			return getItemAsString(getListItem(index));
		}

		@Override
		protected void searchStringBecameEmpty(String searchString) {
			WindowManager.getCurrentMainFrame().getStatusBar().setStatusInfo(searchString); // TODO: is needed?
		}

		@Override
		protected void matchFound(int row, String searchString) {
			if(row!=getSelectedIndex()) {
				setSelectedIndex(row);
				ensureIndexIsVisible(row);
			}
			
			// Display the new search string in the status bar
            // that indicates that the search has yielded a match
			WindowManager.getCurrentMainFrame().getStatusBar().setStatusInfo(searchString, IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, QUICK_SEARCH_OK_ICON), false);
		}

		@Override
		protected void matchNotFound(String searchString) {
			// No file matching the search string, display the new search string with an icon
            // that indicates that the search has failed
			WindowManager.getCurrentMainFrame().getStatusBar().setStatusInfo(searchString, IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, QUICK_SEARCH_KO_ICON), false);
		}

		@Override
		public synchronized void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();

			// If quick search is not active...
			if (!isActive()) {
				// Return (do not start quick search) if the key is not a valid quick search input
				if(!isValidQuickSearchInput(e)) {
					if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER)
						tryToTransferFocusToTheNextComponent();
					
					if (keyCode == KeyEvent.VK_ENTER)
						((QuickListWithDataList)(getParent().getParent().getParent())).itemSelected(getSelectedValue());
					
					return;
				}

				// Start the quick search and continue to process the current key event
				start();
			}

			// At this point, quick search is active
			boolean keyHasModifiers = (e.getModifiersEx()&(KeyEvent.SHIFT_DOWN_MASK|KeyEvent.ALT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK|KeyEvent.META_DOWN_MASK))!=0;

			// Backspace removes the last character of the search string
			if(keyCode==KeyEvent.VK_BACK_SPACE && !keyHasModifiers) {
				// Search string is empty already
				if(isSearchStringEmpty())
					return;

				removeLastCharacterFromSearchString();

				// Find the row that best matches the new search string and select it
				findMatch(0, true, true);
			}
			// Escape immediately cancels the quick search
			else if(keyCode==KeyEvent.VK_ESCAPE && !keyHasModifiers) {
				stop();
			}
			// Up/Down jumps to previous/next match
			// Shift+Up/Shift+Down marks currently selected file and jumps to previous/next match
			else if((keyCode==KeyEvent.VK_UP || keyCode==KeyEvent.VK_DOWN) && !keyHasModifiers) {
				// Find the first row before/after the current row that matches the search string
				boolean down = keyCode==KeyEvent.VK_DOWN;
				findMatch(getSelectedIndex() + (down ? 1 : -1), down, false);
			}
			// If no modifier other than Shift is pressed and the typed character is not a control character (space is ok)
			// and a valid Unicode character, add it to the current search string
			else if(isValidQuickSearchInput(e)) {
				appendCharacterToSearchString(e.getKeyChar());

				// Find the row that best matches the new search string and select it
				findMatch(0, true, true);
			}
			else {
				switch(e.getKeyCode()) {
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
				case KeyEvent.VK_ENTER:
					tryToTransferFocusToTheNextComponent();
					((QuickListWithDataList)(getParent().getParent().getParent())).itemSelected(getSelectedValue());
					stop();
					break;
				case KeyEvent.VK_TAB:
					tryToTransferFocusToTheNextComponent();
					stop();
					break;
				}

				// Do not update last search string's change timestamp
				return;
			}

			// Update last search string's change timestamp
			setLastSearchStringChange(System.currentTimeMillis());

			e.consume();
		}
		
		private void tryToTransferFocusToTheNextComponent() {
			if (nextFocusableComponent != null)
				nextFocusableComponent.requestFocus();
		}
	}
}
