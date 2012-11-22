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

package com.mucommander.ui.popup;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.main.MainFrame;

/**
 * Abstract class for popup menus that display MuActions.
 * 
 * @author Maxence Bernard, Nicolas Rinaudo, Arik Hadas
 */
public abstract class MuActionsPopupMenu extends JPopupMenu {

	/** Parent MainFrame instance */
    private MainFrame mainFrame;
    
    public MuActionsPopupMenu(MainFrame mainFrame) {
    	this.mainFrame = mainFrame;
    }
    
    /**
     * Adds the MuAction denoted by the given ID to this popup menu, as a <code>JMenuItem</code>.
     * <p>
     * No icon will be displayed, regardless of whether the action has one or not.
     * </p>
     * <p>
     * If the action has a keyboard shortcut that conflicts with the menu's internal ones (enter, space and escape),
     * they will not be used.
     * </p>
     * @param actionId action ID
     */
    protected JMenuItem addAction(String actionId) {
        JMenuItem item;
        KeyStroke stroke;

        item = add(ActionManager.getActionInstance(actionId, mainFrame));
        item.setIcon(null);

        stroke = item.getAccelerator();
        if(stroke != null)
            if(stroke.getModifiers() == 0 &&
               (stroke.getKeyCode() == KeyEvent.VK_ENTER || stroke.getKeyCode() == KeyEvent.VK_SPACE || stroke.getKeyCode() == KeyEvent.VK_ESCAPE))
                item.setAccelerator(null);
        
        return item;
    }
}
