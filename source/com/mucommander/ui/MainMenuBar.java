
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.EmailFilesDialog;
import com.mucommander.ui.pref.PreferencesDialog;
import com.mucommander.ui.help.ShortcutsDialog;
import com.mucommander.ui.about.AboutDialog;
import com.mucommander.job.SendMailJob;
import com.mucommander.text.Translator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainMenuBar extends JMenuBar implements ActionListener, LocationListener, MenuListener {
	private MainFrame mainFrame;	
	
	// File menu
	private JMenu fileMenu;
	private JMenuItem newWindowItem;
	private JMenuItem serverConnectItem;
	private JMenuItem runItem;
	private JMenuItem zipItem;
	private JMenuItem unzipItem;
    private JMenuItem emailFilesItem;
    private JMenuItem propertiesItem;
	private JMenuItem preferencesItem;
	private JMenuItem checkForUpdatesItem;
	private JMenuItem quitItem;

	// Mark menu
	private JMenu markMenu;
	private JMenuItem markGroupItem;
	private JMenuItem unmarkGroupItem;
	private JMenuItem markAllItem;
	private JMenuItem unmarkAllItem;
	private JMenuItem invertSelectionItem;
	private JMenuItem compareFoldersItem;

	// View menu
	private JMenu viewMenu;
	private JCheckBoxMenuItem sortByNameItem;
	private JCheckBoxMenuItem sortBySizeItem;
	private JCheckBoxMenuItem sortByDateItem;
	private JCheckBoxMenuItem sortByExtensionItem;
	private JMenuItem reverseOrderItem;
	private JMenuItem goBackItem;
	private JMenuItem goForwardItem;
	private JMenuItem swapFoldersItem;
	private JMenuItem setSameFolderItem;
	private JMenuItem refreshItem;

	// Window menu
	private JMenu windowMenu;
	private JMenuItem showToolbarItem;
//	private JMenuItem previousWindowItem;
//	private JMenuItem nextWindowItem;

	// Help menu
	private JMenu helpMenu;
	private JMenuItem keysItem;
	private JMenuItem forumsItem;
	private JMenuItem homepageItem;
	private JMenuItem aboutItem;
	
	private final static String MUCOMMANDER_HOMEPAGE_URL = "http://www.mucommander.com";
	private final static String MUCOMMANDER_FORUMS_URL = "http://www.mucommander.com/forums/";

	
	public MainMenuBar(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		
		// File menu
		fileMenu = addMenu(Translator.get("file_menu"), KeyEvent.VK_F, true);
		newWindowItem = addMenuItem(fileMenu, Translator.get("file_menu.new_window"), KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		fileMenu.add(new JSeparator());
		serverConnectItem = addMenuItem(fileMenu, Translator.get("file_menu.server_connect"), KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
		runItem = addMenuItem(fileMenu, Translator.get("file_menu.run_command"), KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		zipItem = addMenuItem(fileMenu, Translator.get("file_menu.zip"), KeyEvent.VK_Z, KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		unzipItem = addMenuItem(fileMenu, Translator.get("file_menu.unzip"), KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        emailFilesItem = addMenuItem(fileMenu, Translator.get("file_menu.email"), KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		fileMenu.add(new JSeparator());
        propertiesItem = addMenuItem(fileMenu, Translator.get("file_menu.properties"), KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.ALT_MASK));

		fileMenu.add(new JSeparator());
		preferencesItem = addMenuItem(fileMenu, Translator.get("file_menu.preferences"), KeyEvent.VK_F, null);
		checkForUpdatesItem = addMenuItem(fileMenu, Translator.get("file_menu.check_for_updates"), KeyEvent.VK_K, null);
		
		fileMenu.add(new JSeparator());
		quitItem = addMenuItem(fileMenu, Translator.get("file_menu.close_window"), KeyEvent.VK_Q, KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
	
		// Mark menu
		markMenu = addMenu(Translator.get("mark_menu"), KeyEvent.VK_M, false);
		markGroupItem = addMenuItem(markMenu, Translator.get("mark_menu.mark"), KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0));
		unmarkGroupItem = addMenuItem(markMenu, Translator.get("mark_menu.unmark"), KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0));
		markAllItem = addMenuItem(markMenu, Translator.get("mark_menu.mark_all"), KeyEvent.VK_M, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		unmarkAllItem = addMenuItem(markMenu, Translator.get("mark_menu.unmark_all"), KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		invertSelectionItem = addMenuItem(markMenu, Translator.get("mark_menu.invert_selection"), KeyEvent.VK_I, KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0));

		markMenu.add(new JSeparator());
		compareFoldersItem = addMenuItem(markMenu, Translator.get("mark_menu.compare_folders"), KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));

		// View menu
		viewMenu = addMenu(Translator.get("view_menu"), KeyEvent.VK_V, true);
		goBackItem = addMenuItem(viewMenu, Translator.get("view_menu.go_back"), KeyEvent.VK_B, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK));
		goForwardItem = addMenuItem(viewMenu, Translator.get("view_menu.go_forward"), KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK));
		viewMenu.add(new JSeparator());
		sortByNameItem = addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_name"), KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.CTRL_MASK));
		sortByDateItem = addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_date"), KeyEvent.VK_D, KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK));
		sortBySizeItem = addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_size"), KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.CTRL_MASK));
		sortByExtensionItem = addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_extension"), KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_MASK));
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(sortByNameItem);
		buttonGroup.add(sortByDateItem);
		buttonGroup.add(sortBySizeItem);
		buttonGroup.add(sortByExtensionItem);
		sortByNameItem.setState(true);

		reverseOrderItem = addMenuItem(viewMenu, Translator.get("view_menu.reverse_order"), KeyEvent.VK_R, null);
		viewMenu.add(new JSeparator());
		swapFoldersItem = addMenuItem(viewMenu, Translator.get("view_menu.swap_folders"), KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));
		setSameFolderItem = addMenuItem(viewMenu, Translator.get("view_menu.set_same_folder"), KeyEvent.VK_T, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
//		refreshItem = addMenuItem(viewMenu, "Refresh", KeyEvent.VK_R, null);

		viewMenu.add(new JSeparator());
		// Menu item's text will be set later, when menu is selected
		showToolbarItem = addMenuItem(viewMenu, "", KeyEvent.VK_O, null);
		
		// Window menu
		windowMenu = addMenu(Translator.get("window_menu"), KeyEvent.VK_W, false);
		
		// Help menu
		helpMenu = addMenu(Translator.get("help_menu"), KeyEvent.VK_H, false);
		keysItem = addMenuItem(helpMenu, Translator.get("help_menu.shortcuts"), KeyEvent.VK_K, null);
		helpMenu.add(new JSeparator());
		if (PlatformManager.canOpenURL()) {
			homepageItem = addMenuItem(helpMenu, Translator.get("help_menu.homepage"), KeyEvent.VK_H, null);
			forumsItem = addMenuItem(helpMenu, Translator.get("help_menu.forums"), KeyEvent.VK_F, null);
		}
		helpMenu.add(new JSeparator());
		aboutItem = addMenuItem(helpMenu, Translator.get("help_menu.about"), KeyEvent.VK_A, null);		
	}
	
	/**
	 * Returns a new JMenu.
	 */
	private JMenu addMenu(String title, int mnemonic, boolean addMenuListener) {
		JMenu menu = new JMenu(title);
		menu.setMnemonic(mnemonic);
		if(addMenuListener)
			menu.addMenuListener(this);
		add(menu);
		return menu;
	}
	
	/**
	 * Creates a new JMenuItem and adds it to the given JMenu.
	 * @param accelerator can be <code>null</code>
	 */
	private JMenuItem addMenuItem(JMenu menu, String title, int mnemonic, KeyStroke accelerator) {
		return addMenuItem(menu, title, mnemonic, accelerator, false);
	}


	/**
	 * Creates a new JCheckBoxMenuItem initially unselected and adds it to the given JMenu.
	 * @param accelerator can be <code>null</code>
	 */
	private JCheckBoxMenuItem addCheckBoxMenuItem(JMenu menu, String title, int mnemonic, KeyStroke accelerator) {
		return (JCheckBoxMenuItem)addMenuItem(menu, title, mnemonic, accelerator, true);
	}


	/**
	 * Creates a new JMenuItem or JCheckBoxMenuItem and adds it to the given JMenu.
	 * @param accelerator can be <code>null</code>
	 */
	private JMenuItem addMenuItem(JMenu menu, String title, int mnemonic, KeyStroke accelerator, boolean checkBoxMenuItem) {
		JMenuItem menuItem = checkBoxMenuItem?new JCheckBoxMenuItem(title, false):new JMenuItem(title);
		if(mnemonic!=0)
			menuItem.setMnemonic(mnemonic);
		if(accelerator!=null)
			menuItem.setAccelerator(accelerator);
		menuItem.addActionListener(this);
		menu.add(menuItem);
	
		return menuItem;
	}
	
	/**
	 * Called to notify that sort order has changed and that menu items need to reflect
	 * that change.
	 *
	 * @param criteria sort-by criteria.
	 */	
	public void sortOrderChanged(int criteria) {
		switch (criteria) {
			case FileTableModel.NAME:
				sortByNameItem.setState(true);
				break;
			case FileTableModel.DATE:
				sortByDateItem.setState(true);
				break;
			case FileTableModel.SIZE:
				sortBySizeItem.setState(true);
				break;
			case FileTableModel.EXTENSION:
				sortByExtensionItem.setState(true);
				break;
		}
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}
	
	public JMenu getWindowMenu() {
		return windowMenu;	
	}

	public void locationChanged(FolderPanel folderPanel) {
		goBackItem.setEnabled(folderPanel.hasBackFolder());
		goForwardItem.setEnabled(folderPanel.hasForwardFolder());
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		// File menu
		if (source == newWindowItem) {
			WindowManager.getInstance().createNewMainFrame();
		}
		else if (source == propertiesItem) {
			mainFrame.showPropertiesDialog();
		}
		else if (source == serverConnectItem) {
			mainFrame.showServerConnectDialog();
		}
		else if (source == runItem) {
			new RunDialog(mainFrame);
		}
		else if (source == zipItem) {
			new ZipDialog(mainFrame, false);
		}
		else if (source == unzipItem) {
			new CopyDialog(mainFrame, true, false);
		}
        else if  (source == emailFilesItem) {
			new EmailFilesDialog(mainFrame);
		}
		else if (source == preferencesItem) {
			mainFrame.showPreferencesDialog();
		}
		else if (source == checkForUpdatesItem) {
			new CheckVersionDialog(mainFrame, true);
		}
		else if (source == quitItem) {
			WindowManager.getInstance().disposeMainFrame(mainFrame);
		}
		// Mark menu
		else if (source == markGroupItem) {
			mainFrame.getLastActiveTable().markGroup();	
		}
		else if (source == unmarkGroupItem) {
			mainFrame.getLastActiveTable().unmarkGroup();	
		}
		else if (source == markAllItem) {
			mainFrame.getLastActiveTable().markAll();	
		}
		else if (source == unmarkAllItem) {
			mainFrame.getLastActiveTable().unmarkAll();	
		}
		else if (source == invertSelectionItem) {
			mainFrame.getLastActiveTable().invertSelection();	
		}
		else if (source == compareFoldersItem) {
			mainFrame.compareDirectories();	
		}
		// View menu
		else if (source == goBackItem) {
			mainFrame.getLastActiveTable().getBrowser().goBack();	
		}
		else if (source == goForwardItem) {
			mainFrame.getLastActiveTable().getBrowser().goForward();	
		}
		else if (source == sortByNameItem) {
			mainFrame.getLastActiveTable().sortBy(FileTable.NAME);	
		}
		else if (source == sortBySizeItem) {
			mainFrame.getLastActiveTable().sortBy(FileTable.SIZE);	
		}
		else if (source == sortByDateItem) {
			mainFrame.getLastActiveTable().sortBy(FileTable.DATE);	
		}
		else if (source == sortByExtensionItem) {
			mainFrame.getLastActiveTable().sortBy(FileTable.EXTENSION);	
		}
		else if (source == reverseOrderItem) {
			mainFrame.getLastActiveTable().reverseSortOrder();	
		}
		else if (source == swapFoldersItem) {
			mainFrame.swapFolders();	
		}
		else if (source == setSameFolderItem) {
			mainFrame.setSameFolder();	
		}
		else if (source == showToolbarItem) {
			mainFrame.setToolbarVisible(!mainFrame.isToolbarVisible());
		}
		// Help menu
		else if (source == keysItem) {
			new ShortcutsDialog(mainFrame).showDialog();
		}
		else if (source == forumsItem) {
			PlatformManager.open(MUCOMMANDER_FORUMS_URL, mainFrame.getLastActiveTable().getCurrentFolder());
		}
		else if (source == homepageItem) {
			PlatformManager.open(MUCOMMANDER_HOMEPAGE_URL, mainFrame.getLastActiveTable().getCurrentFolder());
		}
		else if (source == aboutItem) {
			new AboutDialog(mainFrame).showDialog();
		}
	}


	/************************
	 * MenuListener methods *
	 ************************/

	public void menuSelected(MenuEvent e) {
	 	Object source = e.getSource();
		if (source==fileMenu) {
			boolean filesSelected = mainFrame.getLastActiveTable().getSelectedFiles().size()!=0;
			
			// disables menu items if no file is selected
			propertiesItem.setEnabled(filesSelected);
			zipItem.setEnabled(filesSelected);
			unzipItem.setEnabled(filesSelected);
			emailFilesItem.setEnabled(filesSelected);
		}
		else if(source==viewMenu) {
			showToolbarItem.setText(mainFrame.isToolbarVisible()?Translator.get("view_menu.hide_toolbar"):Translator.get("view_menu.show_toolbar"));
		}
	}
	
	public void menuDeselected(MenuEvent e) {
	}
	 
	public void menuCanceled(MenuEvent e) {
	}
}