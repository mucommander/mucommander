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

package com.mucommander.ui.helper;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * This class offers screen related services
 * 
 * @author Arik Hadas
 */
public class ScreenServices {

	/**
     * Computes the screen's insets for the specified window and returns them.
     * <p>
     * While this might seem strange, screen insets can change from one window
     * to another. For example, on X11 windowing systems, there is no guarantee that
     * a window will be displayed on the same screen, let alone computer, as the one
     * the application is running on.
     * </p>
     * @param window the window for which screen insets should be computed.
     * @return the screen's insets for the specified window
     */
    public static Insets getScreenInsets(Window window) {
        return Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
    }
	
	
    /**
     * Checks whether the specified frame can be moved to the specified coordinates and still
     * be fully visible.
     * <p>
     * If <code>x</code> (resp. <code>y</code>) is <code>null</code>, this method won't test
     * whether the frame is within horizontal (resp. vertical) bounds.
     * </p>
     * @param frame frame who's visibility should be tested.
     * @param x     horizontal coordinate of the upper-leftmost corner of the area to check for.
     * @param y     vertical coordinate of the upper-leftmost corner of the area to check for.
     * @return      <code>true</code> if the frame can be moved at the specified location,
     *              <code>false</code> otherwise.
     */
    public static boolean isInsideUsableScreen(Frame frame, int x, int y) {
        Insets    screenInsets;
        Dimension screenSize;

        screenInsets = getScreenInsets(frame);
        screenSize   = Toolkit.getDefaultToolkit().getScreenSize();

        return (x < 0 || (x >= screenInsets.left && x < screenSize.width - screenInsets.right))
            && (y < 0 || (y >= screenInsets.top && y < screenSize.height - screenInsets.bottom));
    }
	
	
    /**
     * Returns the maximum dimensions for a full-screen window.
     *
     * @param window window who's full screen size should be computed.
     * @return the maximum dimensions for a full-screen window
     */
    public static Rectangle getFullScreenBounds(Window window) {
        Toolkit   toolkit;
        Dimension screenSize;

        toolkit    = Toolkit.getDefaultToolkit();
        screenSize = toolkit.getScreenSize();

        Insets screenInsets = toolkit.getScreenInsets(window.getGraphicsConfiguration());
        return new Rectangle(screenInsets.left, screenInsets.top, screenSize.width-screenInsets.left-screenInsets.right, screenSize.height-screenInsets.top-screenInsets.bottom);
    }
}
