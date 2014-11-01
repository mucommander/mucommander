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

import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * This abstract class represent menu item of QuickList.
 *
 * @author Arik Hadas
 */

abstract class QuickListItem extends JMenuItem implements ThemeListener {	
	
	protected static final int X_AXIS_OFFSET = 5;
	
	protected Font FONT;
	protected Dimension dimension;
	
	public QuickListItem(String text) {
		super(text);
		setEnabled(false);
		
		ThemeManager.addCurrentThemeListener(this);
	}
	
	@Override
    public void setFont(Font font) {
		FONT = font;
		dimension = new Dimension((int) Math.ceil(getFontMetrics(font).stringWidth(getText()) * 1.1), (int) (font.getSize() * 1.5));
		setPreferredSize(dimension);
		setSize(dimension);
	}
	
	/**
	 * This function returns the item's dimension which is based on the item's font.
	 */
	@Override
    public Dimension getPreferredSize() { return dimension; }
	
	/////////////////////////////
	/// ThemeListener methods ///
	/////////////////////////////
	abstract public void colorChanged(ColorChangedEvent event);
	
	abstract public void fontChanged(FontChangedEvent event);
}
