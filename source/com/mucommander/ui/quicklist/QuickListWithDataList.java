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

import com.mucommander.ui.quicklist.item.DataList;

import javax.swing.JScrollPane;

/**
 * FileTablePopupWithDataList is a FileTablePopup which contains FileTablePopupDataList.
 * 
 * @author Arik Hadas
 */

public abstract class QuickListWithDataList extends QuickList {	
	protected DataList dataList;	
	private QuickListWithEmptyMsg emptyPopup;
	
	public QuickListWithDataList(String header, String emptyPopupHeader) {
		super(header);

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
        emptyPopup = new QuickListWithEmptyMsg(header, emptyPopupHeader);
	}
	
	protected abstract Object[] getData();
	
	/**
	 * This function will be called when an element from the data list will be selected.
	 * 
	 * @param item - The selected item from the data list.
	 */
	public void itemSelected(Object item) {
		setVisible(false);
		acceptListItem(item);
	}		
	
	@Override
    protected boolean prepareForShowing() {
		boolean toShow = false;
		// if data list contains at least 1 element, show this popup.
		Object[] data;
		if ((data = getData()).length > 0) {
			dataList.setListData(data);
			// transfer the focus to the data list.
			dataList.getFocus();
			toShow = true;
		}
		// else, show popup with a "no elements" message.
		else
			emptyPopup.show(folderPanel);
		
		return toShow;
	}

	/**
	 * This function defines what should be done with a selected item from the data list.
	 * 
	 * @param item - The selected item from the data list.
	 */
	protected abstract void acceptListItem(Object item);
	
	protected abstract DataList getList();
}
