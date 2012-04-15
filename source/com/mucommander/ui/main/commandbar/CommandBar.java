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

package com.mucommander.ui.main.commandbar;

import com.mucommander.desktop.DesktopManager;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.main.MainFrame;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * CommandBar is the button bar that sits at the bottom of the main window and provides access to
 * main commander actions (view, edit, copy, move...).
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class CommandBar extends JPanel implements KeyListener, MouseListener, CommandBarAttributesListener {

    /** Parent MainFrame instance */
    private MainFrame mainFrame;

    /** True when modifier key is pressed */
    private boolean modifierDown;

    /** Command bar buttons */
    private CommandBarButton buttons[];
    
    /** Command bar actions */
    private static String actionIds[];
    
    /** Command bar alternate actions */
    private static String alternateActionIds[];
    
    /** Modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke modifier;

    /**
     * Creates a new CommandBar instance associated with the given MainFrame.
     */
    public CommandBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // Listen to modifier key events to display alternate actions
        mainFrame.getLeftPanel().getFileTable().addKeyListener(this);
        mainFrame.getRightPanel().getFileTable().addKeyListener(this);

        // Listen to mouse events to popup a menu when command bar is right clicked
        addMouseListener(this);

        actionIds = CommandBarAttributes.getActions();
		alternateActionIds = CommandBarAttributes.getAlternateActions();
        modifier = CommandBarAttributes.getModifier();
        
        addButtons();
        
        CommandBarAttributes.addCommandBarAttributesListener(this);
    }
    
    /**
     * Add buttons and separators to the command-bar panel according to the actions array.
     * 
     * actions array must be initialized before this function is called.
     */
    private void addButtons() {
    	setLayout(new GridLayout(0,actionIds.length));
    	
    	// Create buttons and add them to this command bar
        int nbButtons = actionIds.length;
        buttons = new CommandBarButton[nbButtons];
        for(int i=0; i<nbButtons; i++) {
        	buttons[i] = CommandBarButton.create(actionIds[i], mainFrame);
        	buttons[i].addMouseListener(this);
            add(buttons[i]);
        }
    }

    /**
     * Displays/hides alternate actions: buttons that have an alternate action show it when the command bar's
     * modifier is pressed (Shift by default).
     */
    public void setAlternateActionsMode(boolean on) {
        // Do nothing if command bar is not currently visible
        if(!isVisible())
            return;

        if(this.modifierDown != on) {
            this.modifierDown = on;

            int nbButtons = buttons.length;
            for(int i=0; i<nbButtons; i++)
                buttons[i].setButtonAction(on && alternateActionIds[i]!=null?alternateActionIds[i]:actionIds[i], mainFrame);
        }
    }

    /////////////////////////
    // KeyListener methods //
    /////////////////////////

    public void keyPressed(KeyEvent e) {
        // Display alternate actions when the modifier key is pressed
        if(e.getKeyCode() == modifier.getKeyCode())
            setAlternateActionsMode(true);
    }

    public void keyReleased(KeyEvent e) {
        // Display regular actions when the modifier key is released
        if(e.getKeyCode() == modifier.getKeyCode())
            setAlternateActionsMode(false);
    }

    public void keyTyped(KeyEvent e) {
    }

    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent e) {
        // Right clicking on the toolbar brings up a popup menu
        if (DesktopManager.isRightMouseButton(e)) {
            //		if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.impl.ToggleCommandBarAction.Descriptor.ACTION_ID, mainFrame));
            popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.impl.CustomizeCommandBarAction.Descriptor.ACTION_ID, mainFrame));
			// Get the click location in  the CommandBar's coordinate system. 
			// The location returned by the MouseEvent is in the source component (button) coordinate system. it's converted using SwingUtilities to the CommandBar's coordinate system.
			Point clickLocation = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), this);
            popupMenu.show(this, clickLocation.x, clickLocation.y);
            popupMenu.setVisible(true);
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    
    //////////////////////////////////////////
    // CommandBarAttributesListener methods //
    //////////////////////////////////////////
    
    public void commandBarAttributeChanged() {
		actionIds = CommandBarAttributes.getActions();
		alternateActionIds = CommandBarAttributes.getAlternateActions();
		modifier = CommandBarAttributes.getModifier();
		removeAll();
		addButtons();
		doLayout();
	}
}
