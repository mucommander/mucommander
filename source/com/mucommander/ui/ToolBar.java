
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
	private JButton zipButton;
	private JButton unzipButton;
	private JButton emailButton;
	private JButton runcmdButton;
	private JButton propertiesButton;
	
	private final static String NEW_WINDOW_BUTTON_ICON_PATH = "/newwindow.gif";
	private final static String NEW_WINDOW_BUTTON_TOOLTIP_TEXT = "New window";
	private static ImageIcon newWindowIcon;

	private final static String BACK_BUTTON_ICON_PATH = "/back.gif";
	private final static String BACK_BUTTON_DISABLED_ICON_PATH = "/backg.gif";
	private final static String BACK_BUTTON_TOOLTIP_TEXT = "Go back";
	private static ImageIcon backIcon;
	private static ImageIcon backDisabledIcon;

	private final static String FORWARD_BUTTON_ICON_PATH = "/forward.gif";
	private final static String FORWARD_BUTTON_DISABLED_ICON_PATH = "/forwardg.gif";
	private final static String FORWARD_BUTTON_TOOLTIP_TEXT = "Go forward";
	private static ImageIcon forwardIcon;
	private static ImageIcon forwardDisabledIcon;

	private final static String MARK_BUTTON_ICON_PATH = "/mark.gif";
	private final static String MARK_BUTTON_TOOLTIP_TEXT = "Mark...";
	private static ImageIcon markIcon;

	private final static String UNMARK_BUTTON_ICON_PATH = "/unmark.gif";
	private final static String UNMARK_BUTTON_TOOLTIP_TEXT = "Unmark...";
	private static ImageIcon unmarkIcon;

	private final static String SWAP_FOLDERS_BUTTON_ICON_PATH = "/switch.gif";
	private final static String SWAP_FOLDERS_BUTTON_TOOLTIP_TEXT = "Swap folders";
	private static ImageIcon swapFoldersIcon;
	
	private final static String SET_SAME_FOLDER_BUTTON_ICON_PATH = "/same.gif";
	private final static String SET_SAME_FOLDER_BUTTON_TOOLTIP_TEXT = "Set same folder";
	private static ImageIcon setSameFolderIcon;
	
	private final static String CONFIGURE_BUTTON_ICON_PATH = "/configure.gif";
	private final static String CONFIGURE_BUTTON_TOOLTIP_TEXT = "Preferences";
	private static ImageIcon configureIcon;

	private final static String SERVER_CONNECT_BUTTON_ICON_PATH = "/sconnect.gif";
	private final static String SERVER_CONNECT_BUTTON_TOOLTIP_TEXT = "Connect to Server...";
	private static ImageIcon serverConnectIcon;

	private final static String ZIP_BUTTON_ICON_PATH = "/zip.gif";
	private final static String ZIP_BUTTON_TOOLTIP_TEXT = "Zip...";
	private static ImageIcon zipIcon;
	
	private final static String UNZIP_BUTTON_ICON_PATH = "/unzip.gif";
	private final static String UNZIP_BUTTON_TOOLTIP_TEXT = "Unzip...";
	private static ImageIcon unzipIcon;
	
	private final static String EMAIL_BUTTON_ICON_PATH = "/mail.gif";
	private final static String EMAIL_BUTTON_TOOLTIP_TEXT = "Email files...";
	private static ImageIcon emailIcon;
	
	private final static String RUNCMD_BUTTON_ICON_PATH = "/runcmd.gif";
	private final static String RUNCMD_BUTTON_TOOLTIP_TEXT = "Run command...";
	private static ImageIcon runcmdIcon;
	
	private final static String PROPERTIES_BUTTON_ICON_PATH = "/properties.gif";
	private final static String PROPERTIES_BUTTON_TOOLTIP_TEXT = "Properties";
	private static ImageIcon propertiesIcon;
	
	private static boolean firstTime = true;
	
	
	/**
	 * Loads all the icons used by the toolbar buttons
	 */
	private void loadIcons() {
		newWindowIcon = new ImageIcon(getClass().getResource(NEW_WINDOW_BUTTON_ICON_PATH));
		backIcon = new ImageIcon(getClass().getResource(BACK_BUTTON_ICON_PATH));
		backDisabledIcon = new ImageIcon(getClass().getResource(BACK_BUTTON_DISABLED_ICON_PATH));
		forwardIcon = new ImageIcon(getClass().getResource(FORWARD_BUTTON_ICON_PATH));
		forwardDisabledIcon = new ImageIcon(getClass().getResource(FORWARD_BUTTON_DISABLED_ICON_PATH));
		markIcon = new ImageIcon(getClass().getResource(MARK_BUTTON_ICON_PATH));
		unmarkIcon = new ImageIcon(getClass().getResource(UNMARK_BUTTON_ICON_PATH));
		swapFoldersIcon = new ImageIcon(getClass().getResource(SWAP_FOLDERS_BUTTON_ICON_PATH));
		setSameFolderIcon = new ImageIcon(getClass().getResource(SET_SAME_FOLDER_BUTTON_ICON_PATH));
		serverConnectIcon = new ImageIcon(getClass().getResource(SERVER_CONNECT_BUTTON_ICON_PATH));
		configureIcon = new ImageIcon(getClass().getResource(CONFIGURE_BUTTON_ICON_PATH));
		zipIcon = new ImageIcon(getClass().getResource(ZIP_BUTTON_ICON_PATH));
		unzipIcon = new ImageIcon(getClass().getResource(UNZIP_BUTTON_ICON_PATH));
		emailIcon = new ImageIcon(getClass().getResource(EMAIL_BUTTON_ICON_PATH));
		runcmdIcon = new ImageIcon(getClass().getResource(RUNCMD_BUTTON_ICON_PATH));
		propertiesIcon = new ImageIcon(getClass().getResource(PROPERTIES_BUTTON_ICON_PATH));
	}
	
	
	public ToolBar(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		
		if(firstTime) {
			loadIcons();
			firstTime = false;
		}
		
		setBorderPainted(false);

		setFloatable(false);
		putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		
		newWindowButton = addButton(newWindowIcon, null, NEW_WINDOW_BUTTON_TOOLTIP_TEXT);
		newWindowButton.setEnabled(true);

		addSeparator();

		backButton = addButton(backIcon, backDisabledIcon, BACK_BUTTON_TOOLTIP_TEXT);
		backButton.setEnabled(false);

		forwardButton = addButton(forwardIcon, forwardDisabledIcon, FORWARD_BUTTON_TOOLTIP_TEXT);
		forwardButton.setEnabled(false);

		addSeparator();

		markButton = addButton(markIcon, null, MARK_BUTTON_TOOLTIP_TEXT);
		markButton.setEnabled(true);

		unmarkButton = addButton(unmarkIcon, null, UNMARK_BUTTON_TOOLTIP_TEXT);
		unmarkButton.setEnabled(true);

		addSeparator();

		swapFoldersButton = addButton(swapFoldersIcon, null, SWAP_FOLDERS_BUTTON_TOOLTIP_TEXT);
		swapFoldersButton.setEnabled(true);

		setSameFolderButton = addButton(setSameFolderIcon, null, SET_SAME_FOLDER_BUTTON_TOOLTIP_TEXT);
		setSameFolderButton.setEnabled(true);

		addSeparator();

		serverConnectButton = addButton(serverConnectIcon, null, SERVER_CONNECT_BUTTON_TOOLTIP_TEXT);
		serverConnectButton.setEnabled(true);

		addSeparator();

		configureButton = addButton(configureIcon, null, CONFIGURE_BUTTON_TOOLTIP_TEXT);
		configureButton.setEnabled(true);

		addSeparator();

		zipButton = addButton(zipIcon, null, ZIP_BUTTON_TOOLTIP_TEXT);
		zipButton.setEnabled(true);

		unzipButton = addButton(unzipIcon, null, UNZIP_BUTTON_TOOLTIP_TEXT);
		unzipButton.setEnabled(true);
		
		addSeparator();
		
		emailButton = addButton(emailIcon, null, EMAIL_BUTTON_TOOLTIP_TEXT);
		emailButton.setEnabled(true);		

		addSeparator();
		
		runcmdButton = addButton(runcmdIcon, null, RUNCMD_BUTTON_TOOLTIP_TEXT);
		runcmdButton.setEnabled(true);		

		addSeparator();
		
		propertiesButton = addButton(propertiesIcon, null, PROPERTIES_BUTTON_TOOLTIP_TEXT);
		propertiesButton.setEnabled(true);		
	}

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
