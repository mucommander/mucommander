
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.event.LocationEvent;
import com.mucommander.event.LocationListener;
import com.mucommander.event.TableChangeListener;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.bookmark.AddBookmarkDialog;
import com.mucommander.ui.bookmark.EditBookmarksDialog;
import com.mucommander.ui.comp.button.RolloverButton;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.action.ActionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * This class is the icon toolbar attached to a MainFrame, triggering events when buttons are clicked.
 *
 * @author Maxence Bernard
 */
public class ToolBar extends JToolBar implements TableChangeListener, LocationListener, ConfigurationListener, MouseListener, ActionListener {

    private MainFrame mainFrame;

    /** Right-click popup menu */
    private JPopupMenu popupMenu;
    /** Popup menu item that hides the toolbar */
    private JMenuItem hideToolbarMenuItem;
	
    /** JButton instances */
    private JButton buttons[];
	
    /** Buttons descriptions: label, enabled icon, disabled icon (null for no disabled icon), separator ("true" or null for false)  */
    private final static String BUTTONS_DESC[][] = {
        {Translator.get("toolbar.new_window")+" (Ctrl+N)", "new_window.png", null, "true"},
        {Translator.get("toolbar.go_back")+" (Alt+Left)", "back.png", "back_grayed.png", null},
        {Translator.get("toolbar.go_forward")+" (Alt+Right)", "forward.png", "forward_grayed.png", "true"},
        {Translator.get("toolbar.go_to_parent")+" (Backspace)", "parent.png", "parent_grayed.png", "true"},
        {Translator.get("toolbar.stop")+" (Escape)", "stop.png", "stop_grayed.png", "true"},
        {Translator.get("toolbar.add_bookmark")+" (Ctrl+B)", "add_bookmark.png", null, null},
        {Translator.get("toolbar.edit_bookmarks"), "edit_bookmarks.png", null, "true"},
        {Translator.get("toolbar.mark")+" (NumPad +)", "mark.png", null, null},
        {Translator.get("toolbar.unmark")+" (NumPad -)", "unmark.png", null, "true"},
        {Translator.get("toolbar.swap_folders")+" (Ctrl+U)", "swap_folders.png", null, null},
        {Translator.get("toolbar.set_same_folder")+" (Ctrl+E)", "set_same_folder.png", null, "true"},
        {Translator.get("toolbar.pack")+" (Ctrl+I)", "pack.png", null, null},
        {Translator.get("toolbar.unpack")+" (Ctrl+P)", "unpack.png", null, "true"},
        {Translator.get("toolbar.server_connect")+" (Ctrl+K)", "server_connect.png", null, null},
        {Translator.get("toolbar.run_command")+" (Ctrl+R)", "run_command.png", null, null},
        {Translator.get("toolbar.email")+" (Ctrl+S)", "email.png", null, "true"},
        {Translator.get("toolbar.reveal_in_desktop", PlatformManager.getDefaultDesktopFMName())+" (Ctrl+L)", "reveal_in_desktop.png", "reveal_in_desktop_grayed.png", null},
        {Translator.get("toolbar.properties")+" (Alt+Enter)", "properties.png", null, "true"},
        {Translator.get("toolbar.preferences"), "preferences.png", null, null}
    };

	
    private final static int NEW_WINDOW_INDEX = 0;
    private final static int BACK_INDEX = 1;
    private final static int FORWARD_INDEX = 2;
    private final static int PARENT_INDEX = 3;
    private final static int STOP_INDEX = 4;
    private final static int ADD_BOOKMARK_INDEX = 5;
    private final static int EDIT_BOOKMARKS_INDEX = 6;
    private final static int MARK_INDEX = 7;
    private final static int UNMARK_INDEX = 8;
    private final static int SWAP_FOLDERS_INDEX = 9;
    private final static int SET_SAME_FOLDER_INDEX = 10;
    private final static int PACK_INDEX = 11;
    private final static int UNPACK_INDEX = 12;
    private final static int SERVER_CONNECT_INDEX = 13;
    private final static int RUNCMD_INDEX = 14;
    private final static int EMAIL_INDEX = 15;
    private final static int OPEN_IN_DESKTOP_INDEX = 16;
    private final static int PROPERTIES_INDEX = 17;
    private final static int PREFERENCES_INDEX = 18;
	
	
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
                IconManager.getIcon(IconManager.TOOLBAR_ICON_SET, BUTTONS_DESC[i][1]);
                // Preload 'disabled' icon if available
                if(BUTTONS_DESC[i][2]!=null)
                    IconManager.getIcon(IconManager.TOOLBAR_ICON_SET, BUTTONS_DESC[i][2]);
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
			
//if(i==BACK_INDEX) {
//    buttons[i] = (JButton)add(new JButton(ActionManager.getAction("com.mucommander.ui.action.GoBackAction", mainFrame)));
//}
//else
            buttons[i] = addButton(BUTTONS_DESC[i][0]);
            if(BUTTONS_DESC[i][3]!=null &&!BUTTONS_DESC[i][3].equals("false"))
                addSeparator(separatorDimension);
        }

        // Set inital enabled/disabled state for contextual buttons
        updateButtonsState(mainFrame.getLastActiveTable().getFolderPanel());
				
        // Listen to mouse events in order to catch Shift+clicks
        buttons[PACK_INDEX].addMouseListener(this);
        buttons[UNPACK_INDEX].addMouseListener(this);
	
        // Listen to table change events to update buttons state when current table has changed
        mainFrame.addTableChangeListener(this);
	
        // Listen to configuration changes to reload toolbar buttons when icon size has changed
        ConfigurationManager.addConfigurationListener(this);
		
        // Listen to mouse events to popup a menu on right-clicks on the toolbar
        this.addMouseListener(this);
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

            String buttonDesc[] = BUTTONS_DESC[i];
            // Set 'enabled' icon
            button.setIcon(IconManager.getIcon(IconManager.TOOLBAR_ICON_SET, buttonDesc[1]));
            // Set 'disabled' icon if available
            if(buttonDesc[2]!=null)
                button.setDisabledIcon(IconManager.getIcon(IconManager.TOOLBAR_ICON_SET, buttonDesc[2]));
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
	
	
	
    /**
     * Creates and return a new JButton and adds it to this toolbar.
     */
    private JButton addButton(String toolTipText) {
        JButton button = new RolloverButton(null, null, toolTipText);
        button.addActionListener(this);
        add(button);
        return button;
    }

	
    /**
     * Returns the index of the given button, -1 if it isn't part of this toolbar.
     */
    private int getButtonIndex(JButton button) {
        int nbButtons = buttons.length;
        for(int i=0; i<nbButtons; i++)
            if(buttons[i]==button)
                return i;
	
        return -1;
    }


    /**
     * Update buttons state (enabled/disabled) based on current FolderPanel's state.
     */
    private void updateButtonsState(FolderPanel folderPanel) {
        AbstractFile currentFolder = folderPanel.getCurrentFolder();
        buttons[BACK_INDEX].setEnabled(folderPanel.getFolderHistory().hasBackFolder());
        buttons[FORWARD_INDEX].setEnabled(folderPanel.getFolderHistory().hasForwardFolder());
        buttons[STOP_INDEX].setEnabled(false);
        buttons[PARENT_INDEX].setEnabled(currentFolder.getParent()!=null);
        if(buttons[OPEN_IN_DESKTOP_INDEX]!=null)
            buttons[OPEN_IN_DESKTOP_INDEX].setEnabled(currentFolder.getURL().getProtocol().equals("file"));
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

/*
    // For JDK 1.3 (deprecated in 1.4)
    public boolean isFocusTraversable() {
        return false;
    }

    // For JDK 1.4 and up
    public boolean isFocusable() {
        return false;
    }
*/

    /////////////////////////////////
    // TableChangeListener methods //
    /////////////////////////////////
	
    public void tableChanged(FolderPanel folderPanel) {
        updateButtonsState(folderPanel);
    }


    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////
	
    public void locationChanged(LocationEvent e) {
        updateButtonsState(e.getFolderPanel());
    }

    public void locationChanging(LocationEvent e) {
        buttons[STOP_INDEX].setEnabled(true);
    }
	
    public void locationCancelled(LocationEvent e) {
        buttons[STOP_INDEX].setEnabled(false);
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


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Hide toolbar menu item
        if(source == hideToolbarMenuItem) {
            mainFrame.setToolbarVisible(false);
            this.popupMenu.setVisible(false);
            this.popupMenu = null;
            this.hideToolbarMenuItem = null;
            return;
        }

        JButton button = (JButton)source;
        int buttonIndex = getButtonIndex(button);
		
        FolderPanel folderPanel = mainFrame.getLastActiveTable().getFolderPanel();
		
        if(buttonIndex==STOP_INDEX) {
            FolderPanel.ChangeFolderThread changeFolderThread = folderPanel.getChangeFolderThread();
            if(changeFolderThread!=null)
                changeFolderThread.tryKill();
        }

        // Discard action events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        if (buttonIndex==NEW_WINDOW_INDEX) {
            WindowManager.createNewMainFrame();
        }
        else if (buttonIndex==BACK_INDEX) {
            folderPanel.getFolderHistory().goBack();
        }
        else if(buttonIndex==FORWARD_INDEX) {
            folderPanel.getFolderHistory().goForward();
        }
        else if(buttonIndex==PARENT_INDEX) {
            folderPanel.trySetCurrentFolder(folderPanel.getCurrentFolder().getParent());
        }
        else if(buttonIndex==ADD_BOOKMARK_INDEX) {
            new AddBookmarkDialog(mainFrame);
        }
        else if(buttonIndex==EDIT_BOOKMARKS_INDEX) {
            new EditBookmarksDialog(mainFrame);
        }
        else if(buttonIndex==MARK_INDEX) {
            mainFrame.showSelectionDialog(true);
        }
        else if(buttonIndex==UNMARK_INDEX) {
            mainFrame.showSelectionDialog(false);
        }
        else if(buttonIndex==SWAP_FOLDERS_INDEX) {
            mainFrame.swapFolders();
        }
        else if(buttonIndex==SET_SAME_FOLDER_INDEX) {
            mainFrame.setSameFolder();
        }
        else if(buttonIndex==SERVER_CONNECT_INDEX) {
            mainFrame.showServerConnectDialog();
        }
        else if(buttonIndex==OPEN_IN_DESKTOP_INDEX) {
            PlatformManager.openInDesktop(folderPanel.getCurrentFolder());
        }
        else if (buttonIndex==PREFERENCES_INDEX) {
            mainFrame.showPreferencesDialog();
        }
        else if (buttonIndex==RUNCMD_INDEX) {
            new RunDialog(mainFrame);
        }
        else {
            // The following actions need to operate on selected files
            FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
            int nbSelectedFiles = files.size();
			
            // Return if no file is selected
            if(nbSelectedFiles==0)
                return;

            if (buttonIndex==EMAIL_INDEX) {
                new EmailFilesDialog(mainFrame, files);
            }
            else if (buttonIndex==PROPERTIES_INDEX) {
                mainFrame.showPropertiesDialog();
            }
        }
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
                if(this.popupMenu==null) {
                    popupMenu = new JPopupMenu();
                    this.hideToolbarMenuItem = new JMenuItem(Translator.get("toolbar.hide_toolbar"));
                    hideToolbarMenuItem.addActionListener(this);
                    popupMenu.add(hideToolbarMenuItem);
                }
                popupMenu.show(this, e.getX(), e.getY());
                popupMenu.setVisible(true);
            }
        }
        else if(source instanceof JButton) {
            JButton button = (JButton)source;
            int buttonIndex=getButtonIndex(button);
	
            // Don't display dialog is file selection is empty
            FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
            if(files.size()==0)
                return;

            if (buttonIndex==PACK_INDEX) {
                new PackDialog(mainFrame, files, e.isShiftDown());
            }
            else if (buttonIndex==UNPACK_INDEX) {
                new UnpackDialog(mainFrame, files, e.isShiftDown());
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
