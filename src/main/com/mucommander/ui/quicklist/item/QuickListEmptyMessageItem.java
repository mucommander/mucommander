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
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.theme.ThemeManager;

import java.awt.*;

/**
 * This class represent an item that will be shown in a QuickList which doesn't
 * contain any elements, with a relevant message.
 * 
 * @author Arik Hadas
 */

public class QuickListEmptyMessageItem extends QuickListItem {
	
	protected Color foreground;
	protected Color background;
	
	public QuickListEmptyMessageItem(String text) {
		super(text);
		foreground = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR);
		background = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR);
		setFont(ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_ITEM_FONT));
	}
	
	@Override
    protected final void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;
		
		// paint the background of this item with lightGray color
		graphics.setColor(background);
		graphics.fillRect(0, 0, getWidth(), getHeight());

		// draw message:		
		graphics.setFont(FONT);
		graphics.setColor(foreground);
		graphics.drawString(getText(), X_AXIS_OFFSET, (int) graphics.getFontMetrics().getLineMetrics(this.getText(), graphics).getHeight());
	}

	@Override
    public void colorChanged(ColorChangedEvent event) {
		if (event.getColorId() == ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR)
			background = event.getColor();
			
		else if (event.getColorId() == ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR)
			foreground = event.getColor();
	}
	
	@Override
    public void fontChanged(FontChangedEvent event) {
		setFont(ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_ITEM_FONT));
	}
}
