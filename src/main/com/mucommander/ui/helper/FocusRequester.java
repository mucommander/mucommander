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

import java.awt.Component;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The sole purpose of this class is to provide a way to request focus on a component after all currently queued
 * Swing events have been processed. This is useful for components that are not eligible to receive focus at the time
 * they request it, for instance when they are not visible yet.
 *
 * @author Maxence Bernard
 */
public class FocusRequester implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(FocusRequester.class);
	
    /** The component on which to request focus */
    private Component c;

    /** If true, focus will be requested using Component#requestFocusInWindow() instead of Component#requestFocus() */
    private boolean requestFocusInWindow;
	
    private FocusRequester(Component c, boolean requestFocusInWindow) {
        this.c = c;
        this.requestFocusInWindow = requestFocusInWindow;
    }
	
    /**
     * Requests focus on the given componentusing {@link java.awt.Component#requestFocus()}, after all currently queued
     * Swing events have been processed.
     *
     * <p>This method can typically be used when a component has been added to the screen but is not yet visible.
     * In that case, calling {@link Component#requestFocus()} would have no effect.</p>
     *
     * @param c the component on which to request focus
     * @see java.awt.Component#requestFocus()
     */
    public static synchronized void requestFocus(Component c) {
        if(c==null) {
            LOGGER.debug(">>>>>>>>>>>>>>>>>> Component is null, returning!");
            
            return;
        }
        
        SwingUtilities.invokeLater(new FocusRequester(c, false));
    }

    /**
     * Requests focus on the given componentusing {@link java.awt.Component#requestFocusInWindow(boolean)}}, after all
     * currently queued Swing events have been processed.
     *
     * <p>This method can typically be used when a component has been added to the screen but is not yet visible.
     * In that case, calling {@link java.awt.Component#requestFocusInWindow()} would have no effect.</p>
     *
     * @param c the component on which to request focus
     * @see java.awt.Component#requestFocusInWindow()
     */
    public static synchronized void requestFocusInWindow(Component c) {
        if(c==null) {
            LOGGER.debug(">>>>>>>>>>>>>>>>>> Component is null, returning!");

            return;
        }

        SwingUtilities.invokeLater(new FocusRequester(c, true));
    }


    /////////////////////////////
    // Runnable implementation //
    /////////////////////////////
    
    public void run() {
        // Request focus on the component
        if(requestFocusInWindow)
            c.requestFocusInWindow();
        else
            c.requestFocus();

        LOGGER.debug("focus requested on "+(c.getClass().getName()));

        this.c = null;
    }
}
