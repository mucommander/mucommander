
package com.mucommander.ui;

import com.mucommander.ui.comp.button.RolloverButton;
import com.mucommander.ui.bookmark.AddBookmarkDialog;
import com.mucommander.ui.bookmark.EditBookmarksDialog;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.icon.IconManager;

import com.mucommander.conf.*;
import com.mucommander.text.Translator;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.PlatformManager;
import com.mucommander.event.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


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
	
	/** Buttons icons, loaded only once */
	private static ImageIcon icons[][];
	
	/** True if icons (ImageIcon instances) have been loaded */
	private static boolean iconsLoaded;
	
	/** JButton instances */
	private JButton buttons[];
	
	/** Buttons descriptions: label, enabled icon, disabled icon (null for no disabled icon), separator ("true" or null for false)  */
	private final static String BUTTONS_DESC[][] = {
		{Translator.get("toolbar.new_window")+" (Ctrl+N)", "new_window.png", null, "true"},
		{Translator.get("toolbar.go_back")+" (Alt+Left)", "back.gif", "back_grayed.gif", null},
		{Translator.get("toolbar.go_forward")+" (Alt+Right)", "forward.gif", "forward_grayed.gif", "true"},
		{Translator.get("toolbar.go_to_parent")+" (Backspace)", "parent.gif", "parent_grayed.gif", "true"},
		{Translator.get("toolbar.stop")+" (Escape)", "stop.png", "stop_grayed.png", "true"},
		{Translator.get("toolbar.add_bookmark")+" (Ctrl+B)", "add_bookmark.png", null, null},
		{Translator.get("toolbar.edit_bookmarks"), "edit_bookmarks.png", null, "true"},
		{Translator.get("toolbar.mark")+" (NumPad +)", "mark.png", null, null},
		{Translator.get("toolbar.unmark")+" (NumPad -)", "unmark.png", null, "true"},
		{Translator.get("toolbar.swap_folders")+" (Ctrl+U)", "swap_folders.png", null, null},
		{Translator.get("toolbar.set_same_folder")+" (Ctrl+E)", "set_same_folder.png", null, "true"},
		{Translator.get("toolbar.zip")+" (Ctrl+I)", "zip.png", null, null},
		{Translator.get("toolbar.unzip")+" (Ctrl+P)", "unzip.png", null, "true"},
		{Translator.get("toolbar.server_connect")+" (Ctrl+K)", "server_connect.png", null, null},
		{Translator.get("toolbar.run_command")+" (Ctrl+R)", "run_command.png", null, null},
		{Translator.get("toolbar.email")+" (Ctrl+S)", "email.png", null, "true"},
		{Translator.get("toolbar.reveal_in_desktop", PlatformManager.getDefaultDesktopFMName()), "reveal_in_desktop.png", "reveal_in_desktop_grayed.png", null},
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
	private final static int ZIP_INDEX = 11;
	private final static int UNZIP_INDEX = 12;
	private final static int SERVER_CONNECT_INDEX = 13;
	private final static int RUNCMD_INDEX = 14;
	private final static int EMAIL_INDEX = 15;
	private final static int OPEN_IN_DESKTOP_INDEX = 16;
	private final static int PROPERTIES_INDEX = 17;
	private final static int PREFERENCES_INDEX = 18;
	
	
	static {
		if(com.mucommander.conf.ConfigurationManager.getVariableBoolean("prefs.toolbar.visible", true)) {
			// Preload icons if toolbar is to become visible
			loadIcons();
			iconsLoaded = true;
		}
	}
	
	/**
	 * Dummy method which does nothing but trigger static block execution.
	 * Calling this method early enough at launch time makes initialization predictable.
	 */
	public static void init() {
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
			
			buttons[i] = addButton(BUTTONS_DESC[i][0]);
			if(BUTTONS_DESC[i][3]!=null &&!BUTTONS_DESC[i][3].equals("false"))
				addSeparator(separatorDimension);
		}

		// Set inital enabled/disabled state for contextual buttons
		updateButtonsState(mainFrame.getLastActiveTable().getFolderPanel());
				
		// Listen to mouse events in order to catch Shift+clicks
		buttons[ZIP_INDEX].addMouseListener(this);
		buttons[UNZIP_INDEX].addMouseListener(this);
	
		// Listen to table change events to update buttons state when current table has changed
		mainFrame.addTableChangeListener(this);
	
		// Listen to configuration changes to reload toolbar buttons when icon size has changed
		ConfigurationManager.addConfigurationListener(this);
		
		// Listen to mouse events to popup a menu on right-clicks on the toolbar
		this.addMouseListener(this);
	}

	
	/**
	 * Loads all the icons used by the toolbar buttons.
	 */
	private static void loadIcons() {
		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Loading toolbar icons");

		int nbIcons = BUTTONS_DESC.length;
		icons = new ImageIcon[nbIcons][2];
		
		for(int i=0; i<nbIcons; i++) {
			// Load 'enabled' icon
			icons[i][0] = IconManager.getToolBarIcon(BUTTONS_DESC[i][1]);
			// Load 'disabled' icon if available
			if(BUTTONS_DESC[i][2]!=null)
				icons[i][1] = IconManager.getToolBarIcon(BUTTONS_DESC[i][2]);
		}
	}
	

	/**
	 * Sets icons in toolbar buttons.
	 */
	private void setIcons() {
		int nbIcons = BUTTONS_DESC.length;
		for(int i=0; i<nbIcons; i++) {
			if(buttons[i]==null)
				continue;
			
			// Set 'enabled' icon
			buttons[i].setIcon(icons[i][0]);
			// Set 'disabled' icon if available
			if(icons[i][1]!=null)
				buttons[i].setDisabledIcon(icons[i][1]);
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
		buttons[BACK_INDEX].setEnabled(folderPanel.hasBackFolder());
		buttons[FORWARD_INDEX].setEnabled(folderPanel.hasForwardFolder());
		buttons[STOP_INDEX].setEnabled(false);
		buttons[PARENT_INDEX].setEnabled(currentFolder.getParent()!=null);
		if(buttons[OPEN_IN_DESKTOP_INDEX]!=null)
			buttons[OPEN_IN_DESKTOP_INDEX].setEnabled(currentFolder.getURL().getProtocol().equals("file"));
	}
	
	////////////////////////
	// Overridden methods //
	////////////////////////

	/**
	 * Overridden method to load/unload toolbar icons depending on this Toolbar's visible state.
	 * In other words, icons are only loaded when Toolbar is visible.
	 */
	public void setVisible(boolean visible) {
		if(visible) {
			if(!iconsLoaded) {
				// Load icon images
				loadIcons();
				iconsLoaded = true;
			}
			// Set icons to buttons
			setIcons();
		}
		else {
			if(iconsLoaded) {
				// Unload icon images (set values to null)
				int nbButtons = buttons.length;
				for(int i=0; i<nbButtons; i++) {
					icons[i][0] = null;
					icons[i][1] = null;
				}
				iconsLoaded = false;
			}
		}
		
		super.setVisible(visible);
	}

	
	// For JDK 1.3 (deprecated in 1.4)
	public boolean isFocusTraversable() {
		return false;
	}

	// For JDK 1.4 and up
	public boolean isFocusable() {
		return false;
	}


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

		// Reload toolbar icons if their size has changed 
		if (var.equals(IconManager.TOOLBAR_ICON_SCALE_CONF_VAR)) {
			if(isVisible()) {
				loadIcons();
				setIcons();
			}
		}
	
		return true;
	}


	////////////////////////////
	// ActionListener methods //
	////////////////////////////

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		JButton button = (JButton)source;
		int buttonIndex = getButtonIndex(button);
		
		FolderPanel folderPanel = mainFrame.getLastActiveTable().getFolderPanel();
		
		if(buttonIndex==STOP_INDEX) {
			FolderPanel.ChangeFolderThread changeFolderThread = folderPanel.getChangeFolderThread();
			if(changeFolderThread!=null)
				changeFolderThread.tryKill();
			mainFrame.requestFocus();
		}

		// Discard action events while in 'no events mode'
		if(mainFrame.getNoEventsMode())
			return;
	
		// Hide toolbar
		if(source == hideToolbarMenuItem) {
			mainFrame.setToolbarVisible(false);
			this.popupMenu.setVisible(false);
			this.popupMenu = null;
			this.hideToolbarMenuItem = null;
			mainFrame.requestFocus();
			return;
		}
				
//		if(buttonIndex==-1)
//			return;

		// Actions that do not invoke a dialog must request focus on the main frame,
		// since ToolBar has the focus when a button is clicked.
		boolean requestFocus = false;
		
		if (buttonIndex==NEW_WINDOW_INDEX) {
			WindowManager.getInstance().createNewMainFrame();
		}
		else if (buttonIndex==BACK_INDEX) {
			folderPanel.goBack();
		}
		else if(buttonIndex==FORWARD_INDEX) {
			folderPanel.goForward();
		}
		else if(buttonIndex==PARENT_INDEX) {
			folderPanel.trySetCurrentFolder(folderPanel.getCurrentFolder().getParent(), true);
			requestFocus = true;
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
			requestFocus = true;
		}
		else if(buttonIndex==SET_SAME_FOLDER_INDEX) {
			mainFrame.setSameFolder();
			requestFocus = true;
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
			if(nbSelectedFiles<=0) {
				requestFocus = true;
			}
			else if (buttonIndex==EMAIL_INDEX) {
				new EmailFilesDialog(mainFrame, files);
			}
			else if (buttonIndex==PROPERTIES_INDEX) {
				mainFrame.showPropertiesDialog();
			}
		}
		
		// Request focus for actions that did not invoke a dialog
		if(requestFocus)
			mainFrame.requestFocus();
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
			if(files.size()==0) {
				mainFrame.requestFocus();
				return;
			}
				
			if (buttonIndex==ZIP_INDEX) {
				new ZipDialog(mainFrame, files, e.isShiftDown());
			}
			else if (buttonIndex==UNZIP_INDEX) {
				new UnzipDialog(mainFrame, files, e.isShiftDown());
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
