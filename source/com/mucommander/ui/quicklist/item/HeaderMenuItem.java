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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * HeaderItem is a custom menu item that shown as the first item in every GenericTablePopup.
 * 
 * @author Arik Hadas
 */
public class HeaderMenuItem extends MenuItem {
	private static Font HEADER_FONT = new Font("Arial", Font.BOLD, 13);
	public static final Color mainMidColor = new Color(0, 64, 196);
	
	public HeaderMenuItem(String text) {
	   super(text);
	}
	
	public void resize(Component invoker) {
		Dimension invokerSize = invoker.getSize();
		int itemWidth = Math.max(getFontMetrics(HEADER_FONT).stringWidth(getText()) + ((int)(invokerSize.width * 0.1)) , invokerSize.width/2);
		setMinimumSize(new Dimension(itemWidth, 0));
		setPreferredSize(new Dimension(itemWidth, (int) (HEADER_FONT.getSize() * 1.5)));
	}
	
	protected final void paintComponent(Graphics g) {		
		Graphics old = g.create();
		Graphics2D graphics = (Graphics2D) g;
	
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// paint background image	
		graphics.drawImage(getBackgroundImage(getWidth(), getHeight(),
				graphics, mainMidColor, Color.black), 				
				0, 0, null);
		
		// draw text:
		graphics.setFont(HEADER_FONT);
	    // place the text slightly to the left of the center
		int x = (getWidth() - graphics.getFontMetrics().stringWidth(getText())) / 4;
		int y = (int)(graphics.getFontMetrics().getLineMetrics(
				getText(), graphics).getHeight());
		graphics.setColor(Color.black);
		graphics.drawString(getText(), x+1, y+1);
		graphics.setColor(Color.white);
		graphics.drawString(getText(), x, y);
		
		g = old;
	}
	
	public BufferedImage getBackgroundImage(int width, int height, 
			Graphics2D graphics, Color leftColor, Color rightColor) {
		
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
}
