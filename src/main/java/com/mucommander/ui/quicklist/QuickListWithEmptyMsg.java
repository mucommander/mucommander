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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import com.mucommander.ui.quicklist.item.QuickListEmptyMessageItem;

/**
 * FileTablePopupWithEmptyMsg is a FileTablePopup which contains EmptyMessageItem.
 * 
 * @author Arik Hadas
 */

class QuickListWithEmptyMsg extends QuickList {
	protected QuickListEmptyMessageItem emptyMenuItem;
	
	public QuickListWithEmptyMsg(QuickListContainer container, String header, String emptyPopupHeader) {
		super(container, header);
		
		add(emptyMenuItem = new QuickListEmptyMessageItem(emptyPopupHeader));
		
		addKeyListenerToList();
		addFocusListener(this);
	}
	
	@Override
    protected boolean prepareForShowing(QuickListContainer container) {
		getFocus();
		return true;
	}
	
	@Override
	public void getFocus(){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				requestFocus();
			}
		});
	}
	
	private void addKeyListenerToList() {		
		addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {				
				default:
					nextFocusableComponent().requestFocus();
					break;
				}				
			}
		});
	}

}
