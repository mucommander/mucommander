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

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.quicklist.item.DataList;

/**
 * FileTablePopupWithIcons is a FileTablePopupWithDataList in which the data list 
 * 	contains icons.
 * 
 * @author Arik Hadas
 */

public abstract class QuickListWithIcons extends QuickListWithDataList {

	public QuickListWithIcons(String header, String emptyPopupHeader) {
		super(header, emptyPopupHeader);
	}
	
	protected DataList getList() { return new GenericPopupDataListWithIcons(); }
	
	/**
	 * This function gets an item from the data list and return a corresponding icon.
	 * This is the default implementation - if the given string is a valid path to existing
	 * file, then the file's icon is returned. else, null is returned.
	 * This function can be override to provide different implementation.
	 *  
	 * @param value - an item from the data list.
	 * @return icon.
	 */
	protected ImageIcon getImageIcon(String value) {
		AbstractFile file = FileFactory.getFile(value);
		if (file != null)
			return IconManager.getImageIcon(FileIcons.getFileIcon(file));
		return null;
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

				String val = (String) (getModel().getElementAt(index));
				
				final ImageIcon imageIcon = getImageIcon(val);
				if (imageIcon != null) {
					Image image = imageIcon.getImage();
					final Dimension dimension = this.getPreferredSize();
					final double height = dimension.getHeight();
					final double width = (height / imageIcon.getIconHeight()) * imageIcon.getIconWidth();
					image = image.getScaledInstance((int)width, (int)height, Image.SCALE_SMOOTH);
					final ImageIcon finalIcon = new ImageIcon(image);
					setIcon(finalIcon);
				}

				return this;
			}
		}	
	}
}
