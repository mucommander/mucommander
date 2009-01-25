/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

import java.awt.*;
import java.awt.image.BufferedImage;

import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

/**
 * HeaderItem is a custom menu item that shown as the first item in every GenericTablePopup.
 * 
 * @author Arik Hadas
 */
public class HeaderMenuItem extends MenuItem implements ThemeListener {
	private Font HEADER_FONT;
	protected Color TEXT_COLOR;
	protected Color BACKGROUND_COLOR;
	protected Color SECONDARY_BACKGROUND_COLOR;
	protected Dimension dimension;
	
	public HeaderMenuItem(String text) {
	   super(text);
	   TEXT_COLOR = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_HEADER_FOREGROUND_COLOR);
	   BACKGROUND_COLOR = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_HEADER_BACKGROUND_COLOR);
	   SECONDARY_BACKGROUND_COLOR = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR);
	   setFont(ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_HEADER_FONT));
	   ThemeManager.addCurrentThemeListener(this);
	}
	
	public Dimension getPreferredSize() { return dimension; }
	
	protected final void paintComponent(Graphics g) {		
		Graphics old = g.create();
		Graphics2D graphics = (Graphics2D) g;
	
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// paint background image	
		graphics.drawImage(getBackgroundImage(getWidth(), getHeight(),
				graphics, BACKGROUND_COLOR, SECONDARY_BACKGROUND_COLOR), 				
				0, 0, null);
		
		// draw text:
		graphics.setFont(HEADER_FONT);
	    // place the text slightly to the left of the center
		int x = (getWidth() - graphics.getFontMetrics().stringWidth(getText())) / 4;
		int y = (int)(graphics.getFontMetrics().getLineMetrics(
				getText(), graphics).getHeight());
		graphics.setColor(Color.BLACK);
		graphics.drawString(getText(), x+1, y+1);
		graphics.setColor(TEXT_COLOR);
		graphics.drawString(getText(), x, y);
		
		g = old;
	}
	
	public BufferedImage getBackgroundImage(int width, int height, 
			Graphics2D graphics, Color leftColor, Color rightColor) {
		
		//clear previous painting:
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, getWidth(), getHeight());
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				   RenderingHints.VALUE_ANTIALIAS_ON);
		
		// start transition right after the text end. 
		int transitionStart = (getWidth() - graphics.getFontMetrics().stringWidth(
				getText())) / 4 + graphics.getFontMetrics().stringWidth(this.getText());
		
		// end transition at 90% of the item's width.
		int transitionEnd = (int) (0.9 * getWidth());

		// paint transition color
		GradientPaint gradient = new GradientPaint(transitionStart, 0,
		   leftColor, transitionEnd, 0, rightColor);
		graphics.setPaint(gradient);
		graphics.fillRect(transitionStart, 0, transitionEnd - transitionStart, height);
		// paint left color
		graphics.setColor(leftColor);
		graphics.fillRect(0, 0, transitionStart, height);
		// paint right color
		graphics.setColor(rightColor);
		graphics.fillRect(transitionEnd, 0, width - transitionEnd, height);
				
		return image;
	}
	
	public void setForegroundColor(Color foreground) {
		TEXT_COLOR = foreground;
		repaint();
	}

	public void setBackgroundColors(Color background, Color secondaryBackground) {		
		BACKGROUND_COLOR = background;
		SECONDARY_BACKGROUND_COLOR = secondaryBackground;
		repaint();
	}
	
	public void colorChanged(ColorChangedEvent event) {
		if (event.getColorId() == ThemeData.QUICK_LIST_HEADER_BACKGROUND_COLOR)
			BACKGROUND_COLOR = event.getColor();

		else if (event.getColorId() == ThemeData.QUICK_LIST_HEADER_FOREGROUND_COLOR)
			TEXT_COLOR = event.getColor();
		
		else if (event.getColorId() == ThemeData.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR)
			SECONDARY_BACKGROUND_COLOR = event.getColor();
	}
	
	public void setFont(Font font) {
		HEADER_FONT = font;
		dimension = new Dimension((int) Math.ceil(getFontMetrics(HEADER_FONT).stringWidth(getText()) * 1.1), (int) (HEADER_FONT.getSize() * 1.5));
		setPreferredSize(dimension);
		setSize(dimension);
	}

	public void fontChanged(FontChangedEvent event) {
		setFont(ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_HEADER_FONT));
	}
}
