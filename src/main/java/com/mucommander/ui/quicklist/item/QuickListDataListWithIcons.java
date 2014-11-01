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

package com.mucommander.ui.quicklist.item;


import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JList;

import com.mucommander.ui.main.table.CellLabel;

/**
 * 
 * @author Arik Hadas
 */
public abstract class QuickListDataListWithIcons<T> extends QuickListDataList<T> {
	
	public QuickListDataListWithIcons(Component nextFocusableComponent) {
		super(nextFocusableComponent);
	}
	
	public QuickListDataListWithIcons(T[] data) {
		super(data);
	}
	
	@Override
	protected DataListItemRenderer getItemRenderer() {
		return new DataListItemWithIconRenderer();
	}
	
	//////////////////////
	// Abstract methods //
	//////////////////////

	public abstract Icon getImageIconOfItem(final T item, final Dimension preferredSize);

	protected class DataListItemWithIconRenderer extends DataListItemRenderer {
		
		@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			// Let superclass deal with most of it...
			CellLabel label = (CellLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			// Add its icon
			T item = getListItem(index);
			Icon icon = getImageIconOfItem(item, this.getPreferredSize());
			label.setIcon(icon);

			return label;
		}
	}
}
