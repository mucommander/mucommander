
package com.mucommander.ui;

import com.mucommander.Launcher;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.comp.button.RolloverButton;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


/**
 * This class is the icon toolbar attached to a MainFrame, triggering events when buttons are clicked.
 *
 * @author Maxence Bernard
 */
public class ToolBar extends JToolBar implements ActionListener, LocationListener, MouseListener {
	private MainFrame mainFrame;

	private JPopupMenu popupMenu;
	private JMenuItem hideToolbarMenuItem;
	
	/** Buttons icons, loaded only once */
	private static ImageIcon icons[][];
	
	/** JButton instances */
	private JButton buttons[];
	
	/** Buttons descriptions */
	private String BUTTONS_DESC[][] = {
		{"New window (Ctrl+N)", "/newwindow.gif", null, "true"},
		{"Go back (Alt+Left)", "/back.gif", "/backg.gif", null},
		{"Go forward (Alt+Right)", "/forward.gif", "/forwardg.gif", "true"},
		{"Mark (NumPad +)", "/mark.gif", null, null},
		{"Unmark (NumPad -)", "/unmark.gif", null, "true"},
		{"Swap folders (Ctrl+U)", "/switch.gif", null, null},
		{"Set same folder (Ctrl+E)", "/same.gif", null, "true"},
		{"Connect to Server (Ctrl+K)", "/sconnect.gif", null, null},
		{"Run command (Ctrl+R)", "/runcmd.gif", null, null},
		{"Zip (Ctrl+I)", "/zip.gif", null, null},
		{"Unzip (Ctrl+P)", "/unzip.gif", null, null},
		{"Email files (Ctrl+S)", "/mail.gif", null, null},
		{"Properties (Alt+Enter)", "/properties.gif", null, "true"},
		{"Preferences", "/configure.gif", null, null}
	};

	
	private final static int NEW_WINDOW_INDEX = 0;
	private final static int BACK_INDEX = 1;
	private final static int FORWARD_INDEX = 2;
	private final static int MARK_INDEX = 3;
	private final static int UNMARK_INDEX = 4;
	private final static int SWAP_FOLDERS_INDEX = 5;
	private final static int SET_SAME_FOLDER_INDEX = 6;
	private final static int SERVER_CONNECT_INDEX = 7;
	private final static int RUNCMD_INDEX = 8;
	private final static int ZIP_INDEX = 9;
	private final static int UNZIP_INDEX = 10;
	private final static int EMAIL_INDEX = 11;
	private final static int PROPERTIES_INDEX = 12;
	private final static int PREFERENCES_INDEX = 13;
		
	
	/**
	 * Loads all the icons used by the toolbar buttons
	 */
	private void loadIcons() {
		int nbIcons = BUTTONS_DESC.length;
		icons = new ImageIcon[nbIcons][2];
		
		for(int i=0; i<nbIcons; i++) {
			icons[i][0] = new ImageIcon(getClass().getResource(BUTTONS_DESC[i][1]));
			if(BUTTONS_DESC[i][2]!=null)
				icons[i][1] = new ImageIcon(getClass().getResource(BUTTONS_DESC[i][2]));
		}
	}
	
	
	/**
	 * Creates a new toolbar and attaches it to the given frame.
	 */
	public ToolBar(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		
		if(icons==null)
			loadIcons();
		
		setBorderPainted(false);
		setFloatable(false);
		putClientProperty("JToolBar.isRollover", Boolean.TRUE);

		// Create buttons
		int nbButtons = BUTTONS_DESC.length;
		buttons = new JButton[nbButtons];
		Dimension separatorDimension = new Dimension(10, 16);
		for(int i=0; i<nbButtons; i++) {
			buttons[i] = addButton(icons[i][0], icons[i][1], BUTTONS_DESC[i][0]);
			if(BUTTONS_DESC[i][3]!=null &&!BUTTONS_DESC[i][3].equals("false"))
				addSeparator(separatorDimension);
		}

		// In order to catch Shift+clicks
		buttons[ZIP_INDEX].addMouseListener(this);
		buttons[UNZIP_INDEX].addMouseListener(this);
	
		// Add a mouse listener to create popup menu when right-clicking
		// on the toolbar
		this.addMouseListener(this);
	}

	
	/**
	 * Creates and return a new JButton and adds it to this toolbar.
	 */
	private JButton addButton(ImageIcon enabledIcon, ImageIcon disabledIcon, String toolTipText) {
		JButton button = new RolloverButton(
			enabledIcon,
			disabledIcon,
			toolTipText);
		
//		button.setMargin(new Insets(1,1,1,1));
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

	
	
	public void locationChanged(FolderPanel folderPanel) {
		buttons[BACK_INDEX].setEnabled(folderPanel.hasBackFolder());
		buttons[FORWARD_INDEX].setEnabled(folderPanel.hasForwardFolder());
	}

	
	// For JDK 1.3 (deprecated in 1.4)
	public boolean isFocusTraversable() {
		return false;
	}

	// For JDK 1.4 and up
	public boolean isFocusable() {
		return false;
	}



	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
	
		// Hide toolbar
		if(source == hideToolbarMenuItem) {
			mainFrame.setToolbarVisible(false);
			this.popupMenu.setVisible(false);
			this.popupMenu = null;
			this.hideToolbarMenuItem = null;
		}
		
		if(!(source instanceof JButton))
			return;
		
		JButton button = (JButton)source;
		int buttonIndex=getButtonIndex(button);

		if(buttonIndex==-1)
			return;
		
		if (buttonIndex==NEW_WINDOW_INDEX) {
			Launcher.getLauncher().createNewMainFrame();
		}
		else if (buttonIndex==BACK_INDEX) {
			mainFrame.getLastActiveTable().getBrowser().goBack();
		}
		else if(buttonIndex==FORWARD_INDEX) {
			mainFrame.getLastActiveTable().getBrowser().goForward();
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
		else if (buttonIndex==PREFERENCES_INDEX) {
			mainFrame.showPreferencesDialog();
		}
		else if (buttonIndex==RUNCMD_INDEX) {
			new RunDialog(mainFrame);
		}
		else if (buttonIndex==EMAIL_INDEX) {
			new EmailFilesDialog(mainFrame);
		}
		else if (buttonIndex==PROPERTIES_INDEX) {
			mainFrame.showPropertiesDialog();
		}
	
		mainFrame.getLastActiveTable().requestFocus();
	}
	
	///////////////////////////////////////////////////////
	// MouseListener methods to catch shift-clicked buttons
	///////////////////////////////////////////////////////
	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		
		// Right clicking on the toolbar brings up a popup menu
		if(source == this) {
			int modifiers = e.getModifiers();
			if ((modifiers & MouseEvent.BUTTON2_MASK)!=0 || (modifiers & MouseEvent.BUTTON3_MASK)!=0 || e.isControlDown()) {		
				if(this.popupMenu==null) {
					popupMenu = new JPopupMenu();
					this.hideToolbarMenuItem = new JMenuItem("Hide Toolbar");
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
	
			if (buttonIndex==ZIP_INDEX) {
				new ZipDialog(mainFrame, e.isShiftDown());
			}
			else if (buttonIndex==UNZIP_INDEX) {
				new CopyDialog(mainFrame, true, false);
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
