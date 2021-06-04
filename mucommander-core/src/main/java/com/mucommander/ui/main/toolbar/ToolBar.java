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

package com.mucommander.ui.main.toolbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.ui.button.RolloverButtonAdapter;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.GoBackAction;
import com.mucommander.ui.action.impl.GoForwardAction;
import com.mucommander.ui.action.impl.OpenLocationAction;
import com.mucommander.ui.action.impl.ToggleToolBarAction;
import com.mucommander.ui.button.NonFocusableButton;
import com.mucommander.ui.button.PopupButton;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

/**
 * This class is the icon toolbar attached to a MainFrame, triggering events when buttons are clicked.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ToolBar extends JToolBar implements ConfigurationListener, MouseListener, ToolBarAttributesListener {

    private MainFrame mainFrame;

    /** Holds a reference to the RolloverButtonAdapter instance so that it doesn't get garbage-collected */
    private RolloverButtonAdapter rolloverButtonAdapter;

    /** Dimension of button separators */
    private final static Dimension SEPARATOR_DIMENSION = new Dimension(10, 16);

    /** Current icon scale value */
    // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the configuration file.
    private static float scaleFactor = Math.max(1.0f, MuConfigurations.getPreferences().getVariable(MuPreference.TOOLBAR_ICON_SCALE,
                                                                        MuPreferences.DEFAULT_TOOLBAR_ICON_SCALE));


    /**
     * Creates a new toolbar and attaches it to the given frame.
     */
    public ToolBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // Decoration properties
        setBorderPainted(false);
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        // Listen to mouse events in order to popup a menu when toolbar is right-clicked
        addMouseListener(this);

        // Listen to configuration changes to reload toolbar buttons when icon size has changed
        MuConfigurations.addPreferencesListener(this);

        // Rollover-enable the button and hold a reference to the RolloverButtonAdapter instance so that it doesn't
        // get garbage-collected
        rolloverButtonAdapter = new RolloverButtonAdapter();

        // Create buttons for each actions and add them to the toolbar
        addButtons(ToolBarAttributes.getActions());
        
        ToolBarAttributes.addToolBarAttributesListener(this);
    }
    
    private void addButtons(String[] actionIds) {
        for (String actionId : actionIds) {
            if(actionId==null)
                addSeparator(SEPARATOR_DIMENSION);
            else {
                // Get a MuAction instance
                MuAction action = ActionManager.getActionInstance(actionId, mainFrame);
                // Do not add buttons for actions that do not have an icon
                if(action.getIcon()!=null)
                    addButton(action);
            }
        }

        if(OsFamily.MAC_OS.isCurrent()) {
            int nbComponents = getComponentCount();

            // Set the 'segment position' required for the 'segmented capsule' style  
            for(int i=0; i<nbComponents; i++) {
                Component comp = getComponent(i);
                if(!(comp instanceof JButton))
                    continue;

                boolean hasPrevious = i!=0 && (getComponent(i-1) instanceof JButton);
                boolean hasNext = i!=nbComponents-1 && (getComponent(i+1) instanceof JButton);

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

        if(OsFamily.MAC_OS.isCurrent()) {
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

        if(!OsFamily.MAC_OS.isCurrent())     // Add padding around the icon so the button feels less crowded
            icon = IconManager.getPaddedIcon(icon, new Insets(3, 4, 3, 4));

        button.setIcon(icon);
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
        if (var.equals(MuPreferences.TOOLBAR_ICON_SCALE)) {
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
                popupMenu.add(ActionManager.getActionInstance(ToggleToolBarAction.Descriptor.ACTION_ID, mainFrame));
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

    
    ///////////////////////////////////////
    // ToolBarAttributesListener methods //
    ///////////////////////////////////////
    
	public void toolBarActionsChanged() {
		removeAll();
		addButtons(ToolBarAttributes.getActions());
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

        @Override
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
                popupMenu.add(new OpenLocationAction(mainFrame, new Hashtable<String, Object>(), history[i]));

            return popupMenu;
        }
    }
}
