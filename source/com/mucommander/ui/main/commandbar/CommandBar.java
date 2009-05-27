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

package com.mucommander.ui.main.commandbar;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.button.NonFocusableButton;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

/**
 * CommandBar is the button bar that sits at the bottom of the main window and provides access to
 * main commander actions (view, edit, copy, move...).
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class CommandBar extends JPanel implements ConfigurationListener, KeyListener, MouseListener, CommandBarAttributesListener {

    /** Parent MainFrame instance */
    private MainFrame mainFrame;

    /** True when modifier key is pressed */
    private boolean modifierDown;

    /** Command bar buttons */
    private JButton buttons[];

    /** Current icon scale factor */
    // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the configuration file.
    private static float scaleFactor = Math.max(1.0f, MuConfiguration.getVariable(MuConfiguration.COMMAND_BAR_ICON_SCALE,
                                                                        MuConfiguration.DEFAULT_COMMAND_BAR_ICON_SCALE));
    
    /** Command bar actions */
    private static Class actions[];
    
    /** Command bar alternate actions */
    private static Class alternateActions[];
    
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

        // Listen to configuration changes to reload command bar buttons when icon size has changed
        MuConfiguration.addConfigurationListener(this);
        
        actions = CommandBarAttributes.getActions();
		alternateActions = CommandBarAttributes.getAlternateActions();
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
    	setLayout(new GridLayout(0,actions.length));
    	
    	// Create buttons and add them to this command bar
        int nbButtons = actions.length;
        buttons = new JButton[nbButtons];
        for(int i=0; i<nbButtons; i++) {
            MuAction action = ActionManager.getActionInstance(actions[i], mainFrame);
            JButton button = createCommandBarButton(action);
            button.addMouseListener(this);
            add(button);
            buttons[i] = button;
        }
    }

    /**
     * A method to create command-bar button.
     */
    public static JButton createCommandBarButton(MuAction action) {
    	JButton button = new NonFocusableButton();

        // Use new JButton decorations introduced in Mac OS X 10.5 (Leopard) with Java 1.5 and up
        if(OsFamilies.MAC_OS_X.isCurrent() && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher() && JavaVersions.JAVA_1_5.isCurrentOrHigher()) {
            button.putClientProperty("JComponent.sizeVariant", "small");
            button.putClientProperty("JButton.buttonType", "textured");
        }
        else {
            button.setMargin(new Insets(3,4,3,4));
        }

        setButtonAction(button, action);
        
        // For Mac OS X whose default minimum width for buttons is enormous
        button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getHeight()));
        
    	return button;
    }

    /**
     * Sets the given button's action, custom label showing the accelerator and icon taking into account the scale factor.
     */
    private static void setButtonAction(JButton button, MuAction action) {
    	button.setAction(action);
    	
        // Append the action's shortcut to the button's label
        String label;
        label = action.getLabel();
        if(action.getAcceleratorText() != null)
            label += " [" + action.getAcceleratorText() + ']';
        button.setText(label);

        // Scale icon if scale factor is different from 1.0
        if(scaleFactor!=1.0f)
            button.setIcon(IconManager.getScaledIcon(action.getIcon(), scaleFactor));
    }


    /**
     * Displays/hides alternate actions: buttons that have an alternate action show it when the command bar's
     * modifier is pressed (Shift by default).
     */
    public void setAlternateActionsMode(boolean on) {
        // Do nothing if command bar is not currently visible
        if(!isVisible())
            return;

        if(this.modifierDown !=on) {
            this.modifierDown = on;

            int nbButtons = buttons.length;
            for(int i=0; i<nbButtons; i++)
                setButtonAction(buttons[i], 
                		ActionManager.getActionInstance(on && alternateActions[i]!=null?alternateActions[i]:actions[i], mainFrame));
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



    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Reload butons icon if the icon scale factor has changed
        if (var.equals(MuConfiguration.COMMAND_BAR_ICON_SCALE)) {
            scaleFactor = event.getFloatValue();

            int nbButtons = buttons.length;
            for(int i=0; i<nbButtons; i++) {
                JButton button = buttons[i];
                // Change the button's icon but NOT the action's icon which has to remain in its original non-scaled size
                button.setIcon(IconManager.getScaledIcon(((MuAction)button.getAction()).getIcon(), scaleFactor));
            }
        }
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent e) {
        // Right clicking on the toolbar brings up a popup menu
        if (DesktopManager.isRightMouseButton(e)) {
            //		if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.ToggleCommandBarAction.class, mainFrame));
            popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.ShowCommandBarCustomizationAction.class, mainFrame));
            popupMenu.show(this, e.getX(), e.getY());
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
    
	public void CommandBarActionsChanged() {
		actions = CommandBarAttributes.getActions();
		alternateActions = CommandBarAttributes.getAlternateActions();
		removeAll();
		addButtons();
		doLayout();
	}

	public void CommandBarModifierChanged() {
		modifier = CommandBarAttributes.getModifier();
	}
}
