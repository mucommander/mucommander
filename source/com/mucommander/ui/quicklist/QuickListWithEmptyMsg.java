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

import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.quicklist.item.EmptyMessageMenuItem;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * FileTablePopupWithEmptyMsg is a FileTablePopup which contains EmptyMessageItem.
 * 
 * @author Arik Hadas
 */

class QuickListWithEmptyMsg extends QuickList implements QuickListFocusableComponent {
	protected EmptyMessageMenuItem emptyMenuItem;
	
	public QuickListWithEmptyMsg(String header, String emptyPopupHeader) {
		super(header);
		
		add(emptyMenuItem = new EmptyMessageMenuItem(emptyPopupHeader));
		
		addKeyListenerToList();
		addFocusListener(this);
	}	
	
	public void show(Component invoker, int x, int y) {
		emptyMenuItem.resize(invoker);
		super.show(invoker,	x, y);
		requestFocus();
	}
	
	private void addKeyListenerToList() {		
		addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {				
				default:
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

	public FileTable getInvokerFileTable() {
		return getPanel().getFileTable();
	}
}
