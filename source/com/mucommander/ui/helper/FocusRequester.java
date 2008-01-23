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


package com.mucommander.ui.helper;

import javax.swing.*;
import java.awt.*;


/**
 * The sole purpose of this class is to provide a way to request focus on a component after all other
 * UI events have been processed. This is useful for components that are not eligible to receive focus
 * at the time they request it, for instance when they are not visible yet.
 *
 * @author Maxence Bernard
 */
public class FocusRequester implements Runnable {
    private Component c;
	
    private FocusRequester(Component c) {
        this.c = c;
    }
	
    /**
     * Requests focus on the given component when after all other current queued Swing jobs
     * are finished.
     * <p>This method can typically be used when a component has been added to the screen but may 
     * not yet be visible and thus calling requestFocus() directly on the component would not work.</p>
     */
    public static synchronized void requestFocus(Component c) {
        if(c==null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>>>>>>>>>>> Component is null, returning!", -1); 
            
            return;
        }
        
        SwingUtilities.invokeLater(new FocusRequester(c));
    }
	
    public void run() {
        // Request focus on the component
        c.requestFocus();

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("focus requested on "+(c.getClass().getName()));

        this.c = null;
    }
}
