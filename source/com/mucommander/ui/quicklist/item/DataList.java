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

package com.mucommander.ui.quicklist.item;

import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.quicklist.QuickListFocusableComponent;
import com.mucommander.ui.quicklist.QuickListWithDataList;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * This class represent a data list for FileTablePopupWithDataList.
 * 
 * @author Arik Hadas
 */

public class DataList extends JList implements QuickListFocusableComponent {	
	private final static int VISIBLE_ROWS_COUNT = 10;
	
	public DataList(){
		setFont(ThemeManager.getCurrentFont(Theme.FILE_TABLE_FONT));
		setFocusTraversalKeysEnabled(false);
		
        addMouseListenerToList();
		addKeyListenerToList();
		setFixedCellHeight((int) (getFontMetrics(getFont()).getHeight() * 1.5));
	}
	
	/**
	 * This function is called before showing TablePopupWithDataList.
	 * It does the required steps before the popup is shown.	
	 * 
	 * @return true if this list's popup should be shown, false if an TablePopupWithEmptyMsg
	 * 	should be shown.
	 */
	public void setListData(Object[] data) {
		super.setListData(data);
	
		int numOfRowsInList;
		if ((numOfRowsInList = getModel().getSize()) > 0) {
			setVisibleRowCount(Math.min(numOfRowsInList, VISIBLE_ROWS_COUNT));
			setSelectedIndex(0);
			ensureIndexIsVisible(0);
		}
	}
	
	private void addKeyListenerToList() {
		addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {			
				switch(e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					((QuickListWithDataList)(getParent().getParent().getParent())).itemSelected((String) getSelectedValue());
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
	
	private void addMouseListenerToList() {
    	addMouseListener(new MouseListener() {
        		
			public void mouseClicked(MouseEvent e) {
				// If there was double click on item of the popup's list, 
				// select it, and update the text component.
				if (e.getClickCount() == 2) {
		             int index = locationToIndex(e.getPoint());
		             setSelectedIndex(index);
		             ((QuickListWithDataList)(getParent().getParent().getParent())).itemSelected((String) getSelectedValue());
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
}
