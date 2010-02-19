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

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.icon.CustomFileIconProvider;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.quicklist.item.DataList;

import javax.swing.Icon;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.HashMap;

/**
 * FileTablePopupWithIcons is a FileTablePopupWithDataList in which the data list 
 * 	contains icons.
 * 
 * @author Arik Hadas
 */

public abstract class QuickListWithIcons extends QuickListWithDataList {
	// This HashMap's keys are items and its objects are the corresponding icon.
	private final HashMap<Object, Icon> itemToIconCacheMap = new HashMap<Object, Icon>();
	// This SpinningDial will appear until the icon fetching of an item is over.
	private static final SpinningDial waitingIcon = new SpinningDial();
	// If the icon fetching fails for some item, the following icon will appear for it. 
	private static final Icon notAvailableIcon = IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.NOT_ACCESSIBLE_FILE);
	// Saves the number of waiting-icons (SpinningDials) appearing in the list.
	private int numOfWaitingIconInList;
	
	public QuickListWithIcons(String header, String emptyPopupHeader) {
		super(header, emptyPopupHeader);
		numOfWaitingIconInList = 0;
		addPopupMenuListener(new PopupMenuListener() {

			public void popupMenuCanceled(PopupMenuEvent e) {}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				// Clear icon-caching before opening popup-list in order to let the icons be fetched again.
				itemToIconCacheMap.clear();
			}			
		});
	}
	
	/**
	 * Called when waitingIcon is added to the list.
	 */
	private synchronized void waitingIconAddedToList() {
		// If there was no other waitingIcon in the list before current addition - start the spinning dial.
		if (numOfWaitingIconInList++ == 0)
			waitingIcon.setAnimated(true);
	}
	
	/**
	 * Called when waitingIcon is removed from the list.
	 */
	private synchronized void waitingIconRemovedFromList() {
		// If after current remove operation, there will be no waitingIcon in the list - stop the spinning dial.
		if (--numOfWaitingIconInList == 0)
			waitingIcon.setAnimated(false);
	}
	
	@Override
    protected DataList getList() {
		return new GenericPopupDataListWithIcons() {
			@Override
            public Icon getImageIconOfItem(Object item) {
				return getImageIconOfItemImp(item);
			}
		};
	}
	
	/**
	 * This function gets an item from the data list and return its icon.
	 *  
	 * @param item a list item
     * @return an icon for the specified item
	 */
	protected abstract Icon itemToIcon(Object item);
	
	/**
	 * This function return an icon for the specified file.
	 * 
	 * @param file the file for which to return an icon
	 * @return the specified file's icon. null is returned if the file does not exist
	 */
	protected Icon getIconOfFile(AbstractFile file) {
		return (file != null && file.exists()) ?
			IconManager.getImageIcon(FileIcons.getFileIcon(file)) : null; 
	}
	
	private Icon getImageIconOfItemImp(final Object item) {
		boolean found;
		synchronized(itemToIconCacheMap) {
			if (!(found = itemToIconCacheMap.containsKey(item))) {
				itemToIconCacheMap.put(item, waitingIcon);
				waitingIconAddedToList();
			}
		}
		
		if (!found)
			new Thread() {
				@Override
                public void run() {
					Icon icon = itemToIcon(item);
					synchronized(itemToIconCacheMap) {
						// If the item does not exist or is not accessible, show notAvailableIcon for it.
						itemToIconCacheMap.put(item, icon != null ? icon : notAvailableIcon);
					}
					waitingIconRemovedFromList();
					repaint();
				}
			}.start();
		
		Icon result;
		synchronized(itemToIconCacheMap) {
			result = itemToIconCacheMap.get(item);
		}
		return result;
	}
}
