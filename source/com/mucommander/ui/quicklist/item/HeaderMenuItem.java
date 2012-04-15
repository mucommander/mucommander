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

package com.mucommander.ui.quicklist.item;

import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.theme.ThemeManager;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * HeaderMenuItem is a custom MenuItem that shown as the first item in every QuickList.
 * 
 * @author Arik Hadas
 */
public class HeaderMenuItem extends MenuItem {
	
	protected Color foreground;
	protected Color background;
	protected Color secondaryBackground;
	
	public HeaderMenuItem(String text) {
	   super(text);
	   foreground = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_HEADER_FOREGROUND_COLOR);
	   background = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_HEADER_BACKGROUND_COLOR);
	   secondaryBackground = ThemeManager.getCurrentColor(ThemeData.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR);
	   setFont(ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_HEADER_FONT));
	}
		
	@Override
    protected final void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;
	
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// paint background image	
		graphics.drawImage(getBackgroundImage(getWidth(), getHeight(),
				graphics, background, secondaryBackground), 				
				0, 0, null);

		// draw text:
		graphics.setFont(FONT);
		graphics.setColor(foreground);
		graphics.drawString(getText(), X_AXIS_OFFSET, (int) graphics.getFontMetrics().getLineMetrics(this.getText(), graphics).getHeight());
	}
	
	private BufferedImage getBackgroundImage(int width, int height, 
			Graphics2D graphics, Color leftColor, Color rightColor) {
		
		//clear previous painting:
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, width, height);
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// paint transition color
		GradientPaint gradient = new GradientPaint(0, 0, leftColor, width, 0, rightColor);
		graphics.setPaint(gradient);
		graphics.fillRect(0, 0, width, height);

		return image;
	}
	
	public void setForegroundColor(Color foreground) {
		this.foreground = foreground;
		repaint();
	}

	public void setBackgroundColors(Color background, Color secondaryBackground) {		
		this.background = background;
		this.secondaryBackground = secondaryBackground;
		repaint();
	}
	
	@Override
    public void colorChanged(ColorChangedEvent event) {
		if (event.getColorId() == ThemeData.QUICK_LIST_HEADER_BACKGROUND_COLOR)
			background = event.getColor();

		else if (event.getColorId() == ThemeData.QUICK_LIST_HEADER_FOREGROUND_COLOR)
			foreground = event.getColor();
		
		else if (event.getColorId() == ThemeData.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR)
			secondaryBackground = event.getColor();
	}
	
	@Override
    public void fontChanged(FontChangedEvent event) {
		setFont(ThemeManager.getCurrentFont(ThemeData.QUICK_LIST_HEADER_FONT));
	}
}
