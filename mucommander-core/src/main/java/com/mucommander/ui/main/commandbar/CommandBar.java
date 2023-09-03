/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.ActionId;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.main.MainFrame;

/**
 * CommandBar is the button bar that sits at the bottom of the main window and provides access to
 * main commander actions (view, edit, copy, move...).
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class CommandBar extends JPanel {

    /** Parent MainFrame instance */
    private MainFrame mainFrame;

    /** True when modifier key is pressed */
    private boolean modifierDown;

    /** Command bar buttons */
    private CommandBarButton buttons[];
    
    /** Command bar actions */
    private static ActionId actionIds[];
    
    /** Command bar alternate actions */
    private static ActionId alternateActionIds[];
    
    /** Modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke modifier;

    /**
     * Creates a new CommandBar instance associated with the given MainFrame.
     */
    public CommandBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // Listen to modifier key events to display alternate actions
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Display alternate actions when the modifier key is pressed
                if (e.getKeyCode() == modifier.getKeyCode())
                    setAlternateActionsMode(true);
            }
            @Override
            public void keyReleased(KeyEvent e) {
                // Display regular actions when the modifier key is released
                if (e.getKeyCode() == modifier.getKeyCode())
                    setAlternateActionsMode(false);
            }
        };
        mainFrame.getLeftPanel().getFileTable().addKeyListener(keyAdapter);
        mainFrame.getRightPanel().getFileTable().addKeyListener(keyAdapter);

        // Listen to mouse events to popup a menu when command bar is right clicked
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Right clicking on the toolbar brings up a popup menu
                if (DesktopManager.isRightMouseButton(e)) {
                    //      if (e.isPopupTrigger()) {   // Doesn't work under Mac OS X (CTRL+click doesn't return true)
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(ActionManager.getActionInstance(ActionType.ToggleCommandBar, mainFrame));
                    popupMenu.add(ActionManager.getActionInstance(ActionType.CustomizeCommandBar, mainFrame));
                    // Get the click location in  the CommandBar's coordinate system. 
                    // The location returned by the MouseEvent is in the source component (button) coordinate system. it's converted using SwingUtilities to the CommandBar's coordinate system.
                    Point clickLocation = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), CommandBar.this);
                    popupMenu.show(CommandBar.this, clickLocation.x, clickLocation.y);
                    popupMenu.setVisible(true);
                }
            }
        };
        addMouseListener(mouseAdapter);

        actionIds = CommandBarAttributes.getActions();
        alternateActionIds = CommandBarAttributes.getAlternateActions();
        modifier = CommandBarAttributes.getModifier();
        
        addButtons(mouseAdapter);
        
        CommandBarAttributesListener commandBarAttributesListener = () -> {
                actionIds = CommandBarAttributes.getActions();
                alternateActionIds = CommandBarAttributes.getAlternateActions();
                modifier = CommandBarAttributes.getModifier();
                removeAll();
                addButtons(mouseAdapter);
                doLayout();
        };
        CommandBarAttributes.addCommandBarAttributesListener(commandBarAttributesListener);

        mainFrame.getJFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                CommandBarAttributes.removeCommandBarAttributesListener(commandBarAttributesListener);
            }
        });
    }
    
    /**
     * Add buttons and separators to the command-bar panel according to the actions array.
     * 
     * actions array must be initialized before this function is called.
     */
    private void addButtons(MouseListener mouseListener) {
        setLayout(new GridLayout(0,actionIds.length));

        // Create buttons
        buttons = Arrays.stream(actionIds)
                .map(action -> {
                    CommandBarButton button = CommandBarButton.create(action, mainFrame);
                    button.addMouseListener(mouseListener);
                    return button;
                })
                .toArray(CommandBarButton[]::new);
        // And add them to this command bar
        Arrays.stream(buttons).forEach(this::add);
    }

    /**
     * Displays/hides alternate actions: buttons that have an alternate action show it when the command bar's
     * modifier is pressed (Shift by default).
     */
    public void setAlternateActionsMode(boolean on) {
        // Do nothing if command bar is not currently visible
        if (!isVisible())
            return;

        if (this.modifierDown != on) {
            this.modifierDown = on;

            int nbButtons = buttons.length;
            for(int i=0; i<nbButtons; i++)
                buttons[i].setButtonAction(on && alternateActionIds[i]!=null?alternateActionIds[i]:actionIds[i], mainFrame);
        }
    }
}
