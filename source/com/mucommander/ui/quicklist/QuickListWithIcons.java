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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.StatusBar;
import com.mucommander.ui.quicklist.item.DataList;

/**
 * FileTablePopupWithIcons is a FileTablePopupWithDataList in which the data list 
 * 	contains icons.
 * 
 * @author Arik Hadas
 */

public abstract class QuickListWithIcons extends QuickListWithDataList {
	// This HashMap's keys are items and its objects are the corresponding icon.
	private HashMap itemToIconCacheMap = new HashMap();
	// Maximum number of cached items.
	private int MAX_ITEMS_NUM = 100;
	// This icon will appear until the real item's icon is fetched.
	static final ImageIcon waitingIcon = IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, StatusBar.WAITING_ICON);
	
	public QuickListWithIcons(String header, String emptyPopupHeader) {
		super(header, emptyPopupHeader);
	}
	
	protected DataList getList() { return new GenericPopupDataListWithIcons(); }
	
	/**
	 * This function gets an item from the data list and return its icon.
	 *  
	 * @param value - an item from the data list.
	 * @return icon.
	 */
	protected abstract ImageIcon itemToIcon(String value);
	
	/**
	 * This function gets a path, resolves the file it points to, and return the file's icon.
	 * 
	 * @param filepath - path.
	 * @return icon.
	 */
	protected ImageIcon getImageIconOfFile(String filepath) {
		AbstractFile file = FileFactory.getFile(filepath);
		return IconManager.getImageIcon(FileIcons.getFileIcon(file));
	}
	
	private ImageIcon getImageIconOfItem(final String item) {		
		boolean found;
		synchronized(itemToIconCacheMap) {
			if (!(found = itemToIconCacheMap.containsKey(item)))
				itemToIconCacheMap.put(item, waitingIcon);			
		}
		
		if (!found)
			new Thread() {
				public void run() {
					ImageIcon icon = itemToIcon(item);
					synchronized(itemToIconCacheMap) {
						if (itemToIconCacheMap.size() > MAX_ITEMS_NUM)
							itemToIconCacheMap.clear();
						itemToIconCacheMap.put(item, icon);
					}
					repaint();
				}
			}.start();
		
		ImageIcon result;
		synchronized(itemToIconCacheMap) {
			result = (ImageIcon) itemToIconCacheMap.get(item);			
		}
		return result;
	}
	
	private class GenericPopupDataListWithIcons extends DataList {		
		public GenericPopupDataListWithIcons() {
			super();
			setCellRenderer(new CellWithIconRenderer());
		}

		private class CellWithIconRenderer extends DefaultListCellRenderer {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				// Let superclass deal with most of it...
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				// Add its icon
				String item = (String) (getModel().getElementAt(index));				
				ImageIcon imageIcon = getImageIconOfItem(item);
				setIcon(resizeIcon(imageIcon));

				return this;
			}
			
			private ImageIcon resizeIcon(ImageIcon icon) {
				Image image = icon.getImage();
				final Dimension dimension = this.getPreferredSize();
				final double height = dimension.getHeight();
				final double width = (height / icon.getIconHeight()) * icon.getIconWidth();
				image = image.getScaledInstance((int)width, (int)height, Image.SCALE_SMOOTH);
				return new ImageIcon(image);
			}
		}
	}
}
