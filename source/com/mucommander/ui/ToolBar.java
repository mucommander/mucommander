
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.ui.comp.button.RolloverButton;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MucoAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * This class is the icon toolbar attached to a MainFrame, triggering events when buttons are clicked.
 *
 * @author Maxence Bernard
 */
public class ToolBar extends JToolBar implements ConfigurationListener, MouseListener {

    private MainFrame mainFrame;

    /** JButton instances */
    private JButton buttons[];
	
    /** Buttons descriptions: action classname, enabled icon, disabled icon (null for no disabled icon), separator ("true" or null for false)  */
    private final static Object BUTTONS_DESC[][] = {
        {com.mucommander.ui.action.NewWindowAction.class, "new_window.png", null, Boolean.TRUE},
        {com.mucommander.ui.action.GoBackAction.class, "back.png", "back_grayed.png", Boolean.FALSE},
        {com.mucommander.ui.action.GoForwardAction.class, "forward.png", "forward_grayed.png", Boolean.TRUE},
        {com.mucommander.ui.action.GoToParentAction.class, "parent.png", "parent_grayed.png", Boolean.TRUE},
        {com.mucommander.ui.action.StopAction.class, "stop.png", "stop_grayed.png", Boolean.TRUE},
        {com.mucommander.ui.action.AddBookmarkAction.class, "add_bookmark.png", null, Boolean.FALSE},
        {com.mucommander.ui.action.EditBookmarksAction.class, "edit_bookmarks.png", null, Boolean.TRUE},
        {com.mucommander.ui.action.MarkGroupAction.class, "mark.png", null, Boolean.FALSE},
        {com.mucommander.ui.action.UnmarkGroupAction.class, "unmark.png", null, Boolean.TRUE},
        {com.mucommander.ui.action.SwapFoldersAction.class, "swap_folders.png", null, Boolean.FALSE},
        {com.mucommander.ui.action.SetSameFolderAction.class, "set_same_folder.png", null, Boolean.TRUE},
        {com.mucommander.ui.action.PackAction.class, "pack.png", null, Boolean.FALSE},
        {com.mucommander.ui.action.UnpackAction.class, "unpack.png", null, Boolean.TRUE},
        {com.mucommander.ui.action.ConnectToServerAction.class, "server_connect.png", null, Boolean.FALSE},
        {com.mucommander.ui.action.RunCommandAction.class, "run_command.png", null, Boolean.FALSE},
        {com.mucommander.ui.action.EmailAction.class, "email.png", null, Boolean.TRUE},
        {com.mucommander.ui.action.RevealInDesktopAction.class, "reveal_in_desktop.png", "reveal_in_desktop_grayed.png", Boolean.FALSE},
        {com.mucommander.ui.action.PropertiesAction.class, "properties.png", null, Boolean.TRUE},
        {com.mucommander.ui.action.PreferencesAction.class, "preferences.png", null, Boolean.FALSE}
    };

	
    private final static int OPEN_IN_DESKTOP_INDEX = 16;

	
    /**
     * Preloads icons if toolbar is to become visible after launch. 
     * Icons will then be in IconManager's cache, ready for use when the first ToolBar is created.
     */
    public static void init() {
        if(com.mucommander.conf.ConfigurationManager.getVariableBoolean("prefs.toolbar.visible", true)) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Preloading toolbar icons");
			
            // For each icon
            int nbIcons = BUTTONS_DESC.length;
            for(int i=0; i<nbIcons; i++) {
                // Preload 'enabled' icon
                IconManager.getIcon(IconManager.TOOLBAR_ICON_SET, (String)BUTTONS_DESC[i][1]);
                // Preload 'disabled' icon if available
                if(BUTTONS_DESC[i][2]!=null)
                    IconManager.getIcon(IconManager.TOOLBAR_ICON_SET, (String)BUTTONS_DESC[i][2]);
            }
        }
    }
	
	
    /**
     * Creates a new toolbar and attaches it to the given frame.
     */
    public ToolBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
		
        setBorderPainted(false);
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        // Create buttons
        int nbButtons = BUTTONS_DESC.length;
        buttons = new JButton[nbButtons];
        Dimension separatorDimension = new Dimension(10, 16);
        for(int i=0; i<nbButtons; i++) {
            // Add 'reveal in desktop' button only if current platform is capable of doing this
            if(i==OPEN_IN_DESKTOP_INDEX && !PlatformManager.canOpenInDesktop())
                continue;
			
            buttons[i] = addButton((Class)BUTTONS_DESC[i][0]);
            if((BUTTONS_DESC[i][3]).equals(Boolean.TRUE))
                addSeparator(separatorDimension);
        }

        // Listen to mouse events in order to popup a menu when toolbar is right-clicked
        addMouseListener(this);

        // Listen to configuration changes to reload toolbar buttons when icon size has changed
        ConfigurationManager.addConfigurationListener(this);
    }

	

    /**
     * Sets icons in toolbar buttons, called when this toolbar is about to become visible.
     */
    private void setButtonIcons() {
        int nbIcons = BUTTONS_DESC.length;
		
        // For each button
        for(int i=0; i<nbIcons; i++) {
            JButton button = buttons[i];
            if(button==null)
                continue;

            Object buttonDesc[] = BUTTONS_DESC[i];
            // Set 'enabled' icon
            button.setIcon(IconManager.getIcon(IconManager.TOOLBAR_ICON_SET, (String)buttonDesc[1]));
            // Set 'disabled' icon if available
            if(buttonDesc[2]!=null)
                button.setDisabledIcon(IconManager.getIcon(IconManager.TOOLBAR_ICON_SET, (String)buttonDesc[2]));
        }
    }
	
    /**
     * Remove icons from toolbar buttons, called when this toolbar is about to become invisible in order to garbage-collect icon instances.
     */
    private void removeButtonIcons() {
        int nbIcons = BUTTONS_DESC.length;
		
        // For each button
        for(int i=0; i<nbIcons; i++) {
            JButton button = buttons[i];
            if(button==null)
                continue;

            // Remove 'enabled' icon
            button.setIcon(null);
            // Remove 'disabled' icon if available
            if(BUTTONS_DESC[i][2]!=null)
                button.setDisabledIcon(null);
        }
    }	

	
    private JButton addButton(Class actionClass) {
        MucoAction action = ActionManager.getActionInstance(actionClass, mainFrame);
        JButton button = new RolloverButton(action);
        // Remove label
        button.setText(null);
        // Add tooltip using the action's label and accelerator
        String toolTipText = action.getLabel();
        String acceleratorText = action.getAcceleratorText();
        if(acceleratorText!=null)
            toolTipText += " ("+acceleratorText+")";
        button.setToolTipText(toolTipText);
        add(button);
        return button;
    }

	
    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overridden method to set/remove toolbar icons depending on the specified new visible state.
     */
    public void setVisible(boolean visible) {
        if(visible) {
            // Set icons to buttons
            setButtonIcons();
        }
        else {
            // Remove icon from buttons
            removeButtonIcons();
        }
		
        super.setVisible(visible);
    }


    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////
	
    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();

        // Reload toolbar icons if their size has changed and toolbar is visible
        if (var.equals(IconManager.TOOLBAR_ICON_SCALE_CONF_VAR)) {
            if(isVisible())
                setButtonIcons();
        }
	
        return true;
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////
	
    public void mouseClicked(MouseEvent e) {
        // Discard mouse events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        Object source = e.getSource();
		
        // Right clicking on the toolbar brings up a popup menu
        if(source == this) {
            int modifiers = e.getModifiers();
            if ((modifiers & MouseEvent.BUTTON2_MASK)!=0 || (modifiers & MouseEvent.BUTTON3_MASK)!=0 || e.isControlDown()) {		
                //			if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
                JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.ToggleToolBarAction.class, mainFrame));
                popupMenu.show(this, e.getX(), e.getY());
                popupMenu.setVisible(true);
            }
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
}
