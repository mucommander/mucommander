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

package com.mucommander.ui.main.popup;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

/**
 * This class represent an item that will be shown in a FileTablePopup which doesn't
 * contain any elements, with a relevant message.
 * 
 * @author Arik Hadas
 */

class EmptyMessageItem extends FileTablePopupMenuItem {
	private static Font FONT = new Font(ThemeManager.getCurrentFont(Theme.FILE_TABLE_FONT).getFontName(), Font.BOLD, 16);
	
	public EmptyMessageItem(String text) {
		super(text);
	}
	
	public void resize(Component invoker) {
		Dimension invokerSize = invoker.getSize();
		setMinimumSize(new Dimension(invokerSize.width/2, 0));
		setPreferredSize(new Dimension(invokerSize.width/2, FONT.getSize() * 2));
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
