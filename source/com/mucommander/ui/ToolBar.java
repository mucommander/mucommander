
package com.mucommander.ui;

import com.mucommander.Launcher;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.comp.button.RolloverButton;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class ToolBar extends JToolBar implements ActionListener, LocationListener {
	private MainFrame mainFrame;

	private JButton newWindowButton;
	private JButton backButton;
	private JButton forwardButton;
	private JButton configureButton;
	private JButton markButton;
	private JButton unmarkButton;
	private JButton swapFoldersButton;
	private JButton setSameFolderButton;
	private JButton serverConnectButton;

	private final static String NEW_WINDOW_BUTTON_ICON_PATH = "/newwindow.gif";
	private final static String NEW_WINDOW_BUTTON_TOOLTIP_TEXT = "New window";

	private final static String BACK_BUTTON_ICON_PATH = "/back.gif";
	private final static String BACK_BUTTON_DISABLED_ICON_PATH = "/backg.gif";
	private final static String BACK_BUTTON_TOOLTIP_TEXT = "Go back";
	
	private final static String FORWARD_BUTTON_ICON_PATH = "/forward.gif";
	private final static String FORWARD_BUTTON_DISABLED_ICON_PATH = "/forwardg.gif";
	private final static String FORWARD_BUTTON_TOOLTIP_TEXT = "Go forward";

	private final static String MARK_BUTTON_ICON_PATH = "/mark.gif";
	private final static String MARK_BUTTON_DISABLED_ICON_PATH = "/markg.gif";
	private final static String MARK_BUTTON_TOOLTIP_TEXT = "Mark...";

	private final static String UNMARK_BUTTON_ICON_PATH = "/unmark.gif";
	private final static String UNMARK_BUTTON_DISABLED_ICON_PATH = "/unmarkg.gif";
	private final static String UNMARK_BUTTON_TOOLTIP_TEXT = "Unmark...";

	private final static String SWAP_FOLDERS_BUTTON_ICON_PATH = "/switch.gif";
	private final static String SWAP_FOLDERS_BUTTON_TOOLTIP_TEXT = "Swap folders";

	private final static String SET_SAME_FOLDER_BUTTON_ICON_PATH = "/same.gif";
	private final static String SET_SAME_FOLDER_BUTTON_TOOLTIP_TEXT = "Set same folder";

	private final static String CONFIGURE_BUTTON_ICON_PATH = "/configure.gif";
    // private final static String CONFIGURE_BUTTON_DISABLED_ICON_PATH = "/configureg.gif";
	private final static String CONFIGURE_BUTTON_TOOLTIP_TEXT = "Preferences";

	private final static String SERVER_CONNECT_BUTTON_ICON_PATH = "/sconnect.gif";
	private final static String SERVER_CONNECT_BUTTON_TOOLTIP_TEXT = "Connect to Server...";

	public ToolBar(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		
		setBorderPainted(false);

		setFloatable(false);
		putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		
		newWindowButton = addButton(NEW_WINDOW_BUTTON_ICON_PATH, null, NEW_WINDOW_BUTTON_TOOLTIP_TEXT);
		newWindowButton.setEnabled(true);

		addSeparator();

		backButton = addButton(BACK_BUTTON_ICON_PATH, BACK_BUTTON_DISABLED_ICON_PATH, BACK_BUTTON_TOOLTIP_TEXT);
		backButton.setEnabled(false);

		forwardButton = addButton(FORWARD_BUTTON_ICON_PATH, FORWARD_BUTTON_DISABLED_ICON_PATH, FORWARD_BUTTON_TOOLTIP_TEXT);
		forwardButton.setEnabled(false);

		addSeparator();

		markButton = addButton(MARK_BUTTON_ICON_PATH, MARK_BUTTON_DISABLED_ICON_PATH, MARK_BUTTON_TOOLTIP_TEXT);
		markButton.setEnabled(true);

		unmarkButton = addButton(UNMARK_BUTTON_ICON_PATH, UNMARK_BUTTON_DISABLED_ICON_PATH, UNMARK_BUTTON_TOOLTIP_TEXT);
		unmarkButton.setEnabled(true);

		addSeparator();

		swapFoldersButton = addButton(SWAP_FOLDERS_BUTTON_ICON_PATH, null, SWAP_FOLDERS_BUTTON_TOOLTIP_TEXT);
		unmarkButton.setEnabled(true);

		setSameFolderButton = addButton(SET_SAME_FOLDER_BUTTON_ICON_PATH, null, SET_SAME_FOLDER_BUTTON_TOOLTIP_TEXT);
		setSameFolderButton.setEnabled(true);

		addSeparator();

		serverConnectButton = addButton(SERVER_CONNECT_BUTTON_ICON_PATH, null, SERVER_CONNECT_BUTTON_TOOLTIP_TEXT);
		serverConnectButton.setEnabled(true);

		addSeparator();

		configureButton = addButton(CONFIGURE_BUTTON_ICON_PATH, null, CONFIGURE_BUTTON_TOOLTIP_TEXT);
		configureButton.setEnabled(true);
	}

	private JButton addButton(String iconPath, String disabledIconPath, String toolTipText) {
		JButton button = new RolloverButton(
			new ImageIcon(getClass().getResource(iconPath)),
			disabledIconPath==null?null:new ImageIcon(getClass().getResource(disabledIconPath)),
			toolTipText);
		
//		button.setMargin(new Insets(1,1,1,1));
		button.addActionListener(this);
		add(button);
		return button;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
	
		if (source == newWindowButton) {
			Launcher.getLauncher().createNewMainFrame();
		}
		else if (source == backButton) {
			mainFrame.getLastActiveTable().getBrowser().goBack();
		}
		else if(source == forwardButton) {
			mainFrame.getLastActiveTable().getBrowser().goForward();
		}
		else if(source == markButton) {
			mainFrame.showSelectionDialog(true);
		}
		else if(source == unmarkButton) {
			mainFrame.showSelectionDialog(false);
		}
		else if(source == swapFoldersButton) {
			mainFrame.swapFolders();
		}
		else if(source == setSameFolderButton) {
			mainFrame.setSameFolder();
		}
		else if(source == serverConnectButton) {
			mainFrame.showServerConnectDialog();
		}
		else if (source == configureButton) {
			mainFrame.showPreferencesDialog();
		}
		
		mainFrame.getLastActiveTable().requestFocus();
	}
	
	public void locationChanged(FolderPanel folderPanel) {
		backButton.setEnabled(folderPanel.hasBackFolder());
		forwardButton.setEnabled(folderPanel.hasForwardFolder());
	}
	
	// For JDK 1.3 (deprecated in 1.4)
	public boolean isFocusTraversable() {
		return false;
	}

	// For JDK 1.4 and up
	public boolean isFocusable() {
		return false;
	}
}
