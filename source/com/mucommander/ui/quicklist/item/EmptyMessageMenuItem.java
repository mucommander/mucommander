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

package com.mucommander.ui.quicklist.item;

import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import java.awt.*;

/**
 * This class represent an item that will be shown in a FileTablePopup which doesn't
 * contain any elements, with a relevant message.
 * 
 * @author Arik Hadas
 */

public class EmptyMessageMenuItem extends MenuItem {
	private static Font FONT = new Font(ThemeManager.getCurrentFont(Theme.FILE_TABLE_FONT).getFontName(), Font.BOLD, 16);
	private Dimension dim;
	
	public EmptyMessageMenuItem(String text) {
		super(text);
		dim = new Dimension((int) Math.ceil(getFontMetrics(FONT).stringWidth(text) * 1.1), FONT.getSize() * 2);
	}
	
	public Dimension getPreferredSize() {
		return dim;
	}
	
	protected final void paintComponent(Graphics g) {
		Graphics old = g.create();
		Graphics2D graphics = (Graphics2D) g;
		
		// paint the background of this item with lightGray color
		graphics.setColor(Color.lightGray);
		graphics.fillRect(0, 0, getWidth(), getHeight());

		// draw message:		
		graphics.setFont(FONT);
		// locate the text in the middle of the item:
		int x = (this.getWidth() - graphics.getFontMetrics().stringWidth(
				this.getText())) / 2;
		int y = (int) ((this.getHeight() - graphics.getFontMetrics().getLineMetrics(
				this.getText(), graphics).getHeight()) * 1.5);			   
		graphics.setColor(Color.black);		
		graphics.drawString(getText(), x, y);
		
		g = old;
	}
}
