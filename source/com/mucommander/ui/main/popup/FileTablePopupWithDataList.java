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

package com.mucommander.ui.main.popup;

import java.awt.Component;
import java.util.Collection;

import javax.swing.JScrollPane;

import com.mucommander.ui.main.FolderPanel;

/**
 * FileTablePopupWithDataList is a FileTablePopup which contains FileTablePopupDataList.
 * 
 * @author Arik Hadas
 */

abstract class FileTablePopupWithDataList extends FileTablePopup {	
	protected FileTablePopupDataList dataList;	
	private FileTablePopupWithEmptyMsg emptyPopup;
	
	public FileTablePopupWithDataList(String header, String emptyPopupHeader, FolderPanel panel) {
		super(header, panel);

		// get the TablePopupDataList.
		dataList = getList();

		// add JScrollPane that contains the TablePopupDataList to the popup.
		JScrollPane scroll = new JScrollPane(dataList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);				
		scroll.setBorder(null);		        
		scroll.getVerticalScrollBar().setFocusable( false ); 
        scroll.getHorizontalScrollBar().setFocusable( false );
        add(scroll);
        
        dataList.addFocusListener(this);
        
        // create TablePopupWithEmptyMsg that will be shown instead of this popup, if this
        // popup's data list won't have any elements.
        emptyPopup = new FileTablePopupWithEmptyMsg(header, emptyPopupHeader, folderPanel);
	}
	
	protected void setData(Collection data) {
		dataList.setListData(data.toArray());
	}
	
	protected void setData(Object[] data) {
		dataList.setListData(data);
	}
	
	/**
	 * This function will be called when an element from the data list will be selected.
	 * 
	 * @param item - The selected item from the data list.
	 */
	public void itemSelected(String item) {
		setVisible(false);
		acceptListItem(item);
	}		
	
	public void show(Component invoker, int x, int y) {
		// if this popup's data list contain at least 1 element, show the this popup.
		if (dataList.prepareToBeShown()) {
			headerMenuItem.resize(invoker);
			super.show(invoker,	x, y);
			// transfer the focus to the data list.
			dataList.getFocus();
		}
		// else, show popup with a "no elements message".
		else
			emptyPopup.show();
	}

	/**
	 * This function defines what should be done with a selected item from the data list.
	 * 
	 * @param item - The selected item from the data list.
	 */
	protected abstract void acceptListItem(String item);
	
	protected abstract FileTablePopupDataList getList();
}
