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

import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.quicklist.QuickListFocusableComponent;
import com.mucommander.ui.quicklist.QuickListWithDataList;
import com.mucommander.ui.theme.*;

import javax.swing.JList;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * This class represent a data list for FileTablePopupWithDataList.
 * 
 * @author Arik Hadas
 */

public class DataList extends JList implements QuickListFocusableComponent, ThemeListener {	
	private final static int VISIBLE_ROWS_COUNT = 10;
	
	public DataList(){
		setFont(ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_ITEM_FONT));
		setFocusTraversalKeysEnabled(false);
		
        addMouseListenerToList();
		addKeyListenerToList();

		setBackground(ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR));
		setSelectionBackground(ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR));
		
		setForeground(ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR));
		setSelectionForeground(ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR));
		
		ThemeManager.addCurrentThemeListener(this);
	}
	
	public DataList(Object[] data) {
		this();
		setListData(data);
	}
	
	/**
	 * This function is called before showing TablePopupWithDataList.
	 * It does the required steps before the popup is shown.	
	 * 
	 * @return true if this list's popup should be shown, false if an TablePopupWithEmptyMsg
	 * 	should be shown.
	 */
	@Override
    public void setListData(Object[] data) {
		super.setListData(data);
	
		int numOfRowsInList;
		if ((numOfRowsInList = getModel().getSize()) > 0) {
			setVisibleRowCount(Math.min(numOfRowsInList, VISIBLE_ROWS_COUNT));
			setSelectedIndex(0);
			ensureIndexIsVisible(0);
		}
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
    	addMouseListener(new MouseListener() {
        		
			public void mouseClicked(MouseEvent e) {
				// If there was double click on item of the popup's list, 
				// select it, and update the text component.
				if (e.getClickCount() == 2) {
		             int index = locationToIndex(e.getPoint());
		             setSelectedIndex(index);
		             ((QuickListWithDataList)(getParent().getParent().getParent())).itemSelected(getSelectedValue());
				}
			}

			public void mouseReleased(MouseEvent e) {}
			
			public void mouseEntered(MouseEvent e) {}

			public void mouseExited(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {}
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
	
	public void colorChanged(ColorChangedEvent event) {		
		if (event.getColorId() == ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR)
			setBackground(ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR));
		
		else if (event.getColorId() == ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR)
			setForeground(ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR));
		
		else if (event.getColorId() == ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR)
			setSelectionBackground(ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR));
					
		else if (event.getColorId() == ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR)
			setSelectionForeground(ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR));
	}
	
	@Override
    public void setFont(Font font) {
		super.setFont(font);
		setFixedCellHeight((int) (getFontMetrics(getFont()).getHeight() * 1.5));
	}

	public void fontChanged(FontChangedEvent event) {
		setFont(ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_ITEM_FONT));		
	}
	
	public void setForegroundColors(Color foreground, Color selectedForeground) {
		setForeground(foreground);
		setSelectionForeground(selectedForeground);
	}

	public void setBackgroundColors(Color background, Color selectedBackground) {
		setBackground(background);
		setSelectionBackground(selectedBackground);
	}
}
