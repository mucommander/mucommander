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

package com.mucommander.ui.main.toolbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.file.FileURL;
import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.AddBookmarkAction;
import com.mucommander.ui.action.ConnectToServerAction;
import com.mucommander.ui.action.EditBookmarksAction;
import com.mucommander.ui.action.EditCredentialsAction;
import com.mucommander.ui.action.EmailAction;
import com.mucommander.ui.action.GoBackAction;
import com.mucommander.ui.action.GoForwardAction;
import com.mucommander.ui.action.GoToHomeAction;
import com.mucommander.ui.action.GoToParentAction;
import com.mucommander.ui.action.MarkGroupAction;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.NewWindowAction;
import com.mucommander.ui.action.OpenLocationAction;
import com.mucommander.ui.action.PackAction;
import com.mucommander.ui.action.RevealInDesktopAction;
import com.mucommander.ui.action.RunCommandAction;
import com.mucommander.ui.action.SetSameFolderAction;
import com.mucommander.ui.action.ShowFilePropertiesAction;
import com.mucommander.ui.action.ShowPreferencesAction;
import com.mucommander.ui.action.ShowServerConnectionsAction;
import com.mucommander.ui.action.StopAction;
import com.mucommander.ui.action.SwapFoldersAction;
import com.mucommander.ui.action.ToggleToolBarAction;
import com.mucommander.ui.action.UnmarkGroupAction;
import com.mucommander.ui.action.UnpackAction;
import com.mucommander.ui.button.NonFocusableButton;
import com.mucommander.ui.button.PopupButton;
import com.mucommander.ui.button.RolloverButtonAdapter;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

/**
 * This class is the icon toolbar attached to a MainFrame, triggering events when buttons are clicked.
 *
 * @author Maxence Bernard
 */
public class ToolBar extends JToolBar implements ConfigurationListener, MouseListener {

    private MainFrame mainFrame;

    /** Holds a reference to the RolloverButtonAdapter instance so that it doesn't get garbage-collected */
    private RolloverButtonAdapter rolloverButtonAdapter;

    /** Dimension of button separators */
    private final static Dimension SEPARATOR_DIMENSION = new Dimension(10, 16);

    /** Whether to use the new JButton decorations introduced in Mac OS X 10.5 (Leopard) with Java 1.5 and up */
    private final static boolean USE_MAC_OS_X_CLIENT_PROPERTIES =
            OsFamilies.MAC_OS_X.isCurrent() &&
            OsVersions.MAC_OS_X_10_5.isCurrentOrHigher() &&
            JavaVersions.JAVA_1_5.isCurrentOrHigher();

    /** Current icon scale value */
    // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the configuration file.
    private static float scaleFactor = Math.max(1.0f, MuConfiguration.getVariable(MuConfiguration.TOOLBAR_ICON_SCALE,
                                                                        MuConfiguration.DEFAULT_TOOLBAR_ICON_SCALE));

    /** Command bar actions: Class instances or null to signify a separator */
    private static Class actions[];

    public static ToolBar createToolBar(MainFrame mainFrame) {
    	// If toolbar's action were not loaded from a file, create default toolbar
        if (actions == null)
        	createDefaultToolbar();
        return new ToolBar(mainFrame);
    }
    
    /**
     * Creates a new toolbar and attaches it to the given frame.
     */
    private ToolBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // Decoration properties
        setBorderPainted(false);
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        // Listen to mouse events in order to popup a menu when toolbar is right-clicked
        addMouseListener(this);

        // Listen to configuration changes to reload toolbar buttons when icon size has changed
        MuConfiguration.addConfigurationListener(this);

        // Rollover-enable the button and hold a reference to the RolloverButtonAdapter instance so that it doesn't
        // get garbage-collected
        rolloverButtonAdapter = new RolloverButtonAdapter();

        // Create buttons for each actions and add them to the toolbar
        int nbActions = actions.length;
        for(int i=0; i<nbActions; i++) {
            Class actionClass = actions[i];
            if(actionClass==null)
                addSeparator(SEPARATOR_DIMENSION);
            else {
                // Get a MuAction instance
                MuAction action = ActionManager.getActionInstance(actionClass, mainFrame);
                // Do not add buttons for actions that do not have an icon
                if(action.getIcon()!=null)
                    addButton(action);
            }
        }

        if(USE_MAC_OS_X_CLIENT_PROPERTIES) {
            int nbComponents = getComponentCount();
            Component comp;
            boolean hasPrevious, hasNext;

            // Set the 'segment position' required for the 'segmented capsule' style  
            for(int i=0; i<nbComponents; i++) {
                comp = getComponent(i);
                if(!(comp instanceof JButton))
                    continue;

                hasPrevious = i!=0 && (getComponent(i-1) instanceof JButton);
                hasNext = i!=nbComponents-1 && (getComponent(i+1) instanceof JButton);

                String segmentPosition;
                if(hasPrevious && hasNext)
                    segmentPosition = "middle";
                else if(hasPrevious)
                    segmentPosition = "last";
                else if(hasNext)
                    segmentPosition = "first";
                else
                    segmentPosition = "only";

                ((JButton)comp).putClientProperty("JButton.segmentPosition", segmentPosition);
             }
        }
    }

    /**
     * Sets the toolbar's actions to the given action classes.
     */
    static void setToolBarActions(Class[] newActions) {
    	actions = newActions;
    }

    /**
     * Adds a button to this toolbar using the given action.
     */
    private void addButton(MuAction action) {
        JButton button;

        if(action instanceof GoBackAction || action instanceof GoForwardAction)
            button = new HistoryPopupButton(action);
        else
            button = new NonFocusableButton(action);

        // Remove label
        button.setText(null);

        // Add tooltip using the action's label and accelerator
        String toolTipText = action.getLabel();
        String acceleratorText = action.getAcceleratorText();
        if(acceleratorText!=null)
            toolTipText += " ("+acceleratorText+")";
        button.setToolTipText(toolTipText);

        // Sets the button icon, taking into account the icon scale factor
        setButtonIcon(button);

        if(USE_MAC_OS_X_CLIENT_PROPERTIES) {
            button.putClientProperty("JButton.buttonType", "segmentedTextured");
            button.setRolloverEnabled(true);
        }
        // On other platforms, use a custom rollover effect
        else {
            // Init rollover
            RolloverButtonAdapter.setButtonDecoration(button);
            button.addMouseListener(rolloverButtonAdapter);
        }

        add(button);
    }

    /**
     * Sets the specified button's icon to the proper scale.
     *
     * @param button the button to update
     */
    private void setButtonIcon(JButton button) {
        // Note: the action's icon must not be changed and remain in its original, non-scaled size
        ImageIcon icon = IconManager.getScaledIcon((ImageIcon)button.getAction().getValue(Action.SMALL_ICON), scaleFactor);

        if(!USE_MAC_OS_X_CLIENT_PROPERTIES)     // Add padding around the icon so the button feels less crowded
            icon = IconManager.getPaddedIcon(icon, new Insets(3, 4, 3, 4));

        button.setIcon(icon);
    }

    /**
     * Sets the toolbar's actions to default values.
     * This function is called when the toolbar items could not be loaded from external file.
     */
    private static void createDefaultToolbar() {
    	LinkedList toolBarItems = new LinkedList();
    	toolBarItems.add(NewWindowAction.class);
    	toolBarItems.add(GoBackAction.class);
    	toolBarItems.add(GoForwardAction.class);
    	toolBarItems.add(null);
    	toolBarItems.add(GoToParentAction.class);
    	toolBarItems.add(GoToHomeAction.class);
    	toolBarItems.add(null);
    	toolBarItems.add(StopAction.class);
    	toolBarItems.add(null);
    	toolBarItems.add(MarkGroupAction.class);
    	toolBarItems.add(UnmarkGroupAction.class);
    	toolBarItems.add(null);
    	toolBarItems.add(SwapFoldersAction.class);
    	toolBarItems.add(SetSameFolderAction.class);
    	toolBarItems.add(null);
    	toolBarItems.add(PackAction.class);
    	toolBarItems.add(UnpackAction.class);
    	toolBarItems.add(null);
    	toolBarItems.add(AddBookmarkAction.class);
    	toolBarItems.add(EditBookmarksAction.class);
    	toolBarItems.add(EditCredentialsAction.class);
    	toolBarItems.add(null);
    	toolBarItems.add(ConnectToServerAction.class);
    	toolBarItems.add(ShowServerConnectionsAction.class);
    	toolBarItems.add(RunCommandAction.class);
    	toolBarItems.add(EmailAction.class);
    	toolBarItems.add(null);
    	toolBarItems.add(RevealInDesktopAction.class);
    	toolBarItems.add(ShowFilePropertiesAction.class);
    	toolBarItems.add(null);
    	toolBarItems.add(ShowPreferencesAction.class);
    	toolBarItems.add(null);
    	actions = new Class[toolBarItems.size()];
    	toolBarItems.toArray(actions);
    }

    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Rescale buttons icon
        if (var.equals(MuConfiguration.TOOLBAR_ICON_SCALE)) {
            scaleFactor = event.getFloatValue();
            Component components[] = getComponents();
            int nbComponents = components.length;

            for(int i=0; i<nbComponents; i++) {
                if(components[i] instanceof JButton) {
                    setButtonIcon((JButton)components[i]);
                }
            }
        }
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent e) {
        Object source = e.getSource();

        // Right clicking on the toolbar brings up a popup menu
        if(source == this) {
            if (DesktopManager.isRightMouseButton(e)) {
                //			if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
                JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.add(ActionManager.getActionInstance(ToggleToolBarAction.class, mainFrame));
                popupMenu.show(this, e.getX(), e.getY());
                popupMenu.setVisible(true);
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
        Object source = e.getSource();
        if(source instanceof JButton)
            ((JButton)source).setBorderPainted(true);
    }

    public void mouseExited(MouseEvent e) {
        Object source = e.getSource();
        if(source instanceof JButton)
            ((JButton)source).setBorderPainted(false);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }


    /**
     * PopupButton used for 'Go back' and 'Go forward' actions which displays the list of back/forward folders in the
     * popup menu and allows to recall them by clicking on them.
     */
    private class HistoryPopupButton extends PopupButton {

        private MuAction action;

        private HistoryPopupButton(MuAction action) {
            super(action);
            this.action = action;
        }

        public JPopupMenu getPopupMenu() {
            FileURL history[] = action instanceof GoBackAction?
                    mainFrame.getActivePanel().getFolderHistory().getBackFolders()
                    :mainFrame.getActivePanel().getFolderHistory().getForwardFolders();
            int historyLen = history.length;        

            // If no back/forward folder, do not display popup menu
            if(history.length==0)
                return null;

            JPopupMenu popupMenu = new JPopupMenu();
            for(int i=0; i<historyLen; i++)
                popupMenu.add(new OpenLocationAction(mainFrame, new Hashtable(), history[i]));

            return popupMenu;
        }
    }
}
