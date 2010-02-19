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

package com.mucommander.ui.list;

import com.mucommander.util.AlteredVector;

import javax.swing.JList;
import java.awt.Container;
import java.awt.Rectangle;

/**
 * This class represent a DynamicList with a following horizontal-wrap layout:
 * <pre>
 *     1   2   3
 *     4   5   6
 *     7   8   9
 *     10..
 * </pre>
 *   
 * In which the number of items per-row is dynamically determined by the 
 * width of the list and the given item-width parameter.
 *  
 * @author Arik Hadas
 */
public class DynamicHorizontalWrapList<E> extends DynamicList<E> {

	// The width of each item in the list
	private int itemWidth;
	// Saves the last width of the parent container to detect if there
	// should be a change in the number of items per-row.
	private int lastParentWidth;
	
	public DynamicHorizontalWrapList(AlteredVector<E> items, int itemWidth) {
		this(items, itemWidth, 0);
	}
	
	public DynamicHorizontalWrapList(AlteredVector<E> items, int itemWidth, int horizontalPadding) {
		super(items);
		
		this.itemWidth = itemWidth + horizontalPadding;
		setLayoutOrientation(JList.HORIZONTAL_WRAP);
	}

	@Override
    public void repaint() {
		Container parent = getParent();
		if (parent != null) {
			Rectangle parentBounds = parent.getBounds();
			int parentWidth = parentBounds.width;

			if (lastParentWidth != parentWidth) {
				lastParentWidth = parentWidth;

				int itemsPerRow = parentWidth / itemWidth;
				setFixedCellWidth(itemWidth + ((parentWidth - itemWidth * itemsPerRow)/ itemsPerRow));
				setVisibleRowCount(getComponentCount() / itemsPerRow);
			}
		}
		
		super.repaint();
	}
}
