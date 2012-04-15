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

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;

/**
 * 
 * @author Arik Hadas
 */
public abstract class GenericPopupDataListWithIcons extends DataList {
	
	public abstract Icon getImageIconOfItem(final Object item);
	
	public GenericPopupDataListWithIcons() {
		super();
		setCellRenderer(new CellWithIconRenderer());
	}
	
	public GenericPopupDataListWithIcons(Object[] data) {
		super(data);
		setCellRenderer(new CellWithIconRenderer());
	}

	private class CellWithIconRenderer extends DefaultListCellRenderer {
		
		@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			// Let superclass deal with most of it...
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			// Add its icon
			Object item = list.getModel().getElementAt(index);
			Icon icon = getImageIconOfItem(item);
			setIcon(resizeIcon(icon));

			return this;
		}
		
		private Icon resizeIcon(Icon icon) {
			if (icon instanceof ImageIcon) {
				Image image = ((ImageIcon) icon).getImage();
				final Dimension dimension = this.getPreferredSize();
				final double height = dimension.getHeight();
				final double width = (height / icon.getIconHeight()) * icon.getIconWidth();
				image = image.getScaledInstance((int)width, (int)height, Image.SCALE_SMOOTH);
				return new ImageIcon(image);
			}

			return icon;
		}
	}
}
