
package com.mucommander.ui;

import com.mucommander.PlatformManager;

import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.EmailFilesDialog;
import com.mucommander.ui.pref.PreferencesDialog;
import com.mucommander.ui.help.ShortcutsDialog;
import com.mucommander.ui.about.AboutDialog;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;

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
//private JMenuItem renamerItem;
	
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
		
		MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
		MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
		
		// File menu
		fileMenu = MenuToolkit.addMenu(Translator.get("file_menu"), menuMnemonicHelper, this);
		newWindowItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.new_window"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK), this);
//
		fileMenu.add(new JSeparator());
		serverConnectItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.server_connect"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK), this);
		runItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.run_command"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK), this);
		zipItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.zip"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK), this);
		unzipItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.unzip"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK), this);
        emailFilesItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.email"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), this);

		fileMenu.add(new JSeparator());
        propertiesItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.properties"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.ALT_MASK), this);

		fileMenu.add(new JSeparator());
		preferencesItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.preferences"), menuItemMnemonicHelper, null, this);
		checkForUpdatesItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.check_for_updates"), menuItemMnemonicHelper, null, this);
		
		fileMenu.add(new JSeparator());
		quitItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.close_window"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), this);
		
		add(fileMenu);
	
		// Mark menu
		menuItemMnemonicHelper.clear();
		markMenu = MenuToolkit.addMenu(Translator.get("mark_menu"), menuMnemonicHelper, null);
		markGroupItem = MenuToolkit.addMenuItem(markMenu, Translator.get("mark_menu.mark"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), this);
		unmarkGroupItem = MenuToolkit.addMenuItem(markMenu, Translator.get("mark_menu.unmark"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), this);
		markAllItem = MenuToolkit.addMenuItem(markMenu, Translator.get("mark_menu.mark_all"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), this);
		unmarkAllItem = MenuToolkit.addMenuItem(markMenu, Translator.get("mark_menu.unmark_all"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK), this);
		invertSelectionItem = MenuToolkit.addMenuItem(markMenu, Translator.get("mark_menu.invert_selection"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0), this);

		markMenu.add(new JSeparator());
		compareFoldersItem = MenuToolkit.addMenuItem(markMenu, Translator.get("mark_menu.compare_folders"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK), this);
//if(com.mucommander.Debug.ON)
//	renamerItem = MenuToolkit.addMenuItem(markMenu, "[[[RENAMER]]]", menuItemMnemonicHelper, null, this);

		add(markMenu);
		
		// View menu
		menuItemMnemonicHelper.clear();
		viewMenu = MenuToolkit.addMenu(Translator.get("view_menu"), menuMnemonicHelper, this);
		goBackItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.go_back"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK), this);
		goForwardItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.go_forward"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK), this);
		viewMenu.add(new JSeparator());
		sortByNameItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_name"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.CTRL_MASK), this);
		sortByDateItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_date"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK), this);
		sortBySizeItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_size"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.CTRL_MASK), this);
		sortByExtensionItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_extension"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_MASK), this);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(sortByNameItem);
		buttonGroup.add(sortByDateItem);
		buttonGroup.add(sortBySizeItem);
		buttonGroup.add(sortByExtensionItem);
		sortByNameItem.setState(true);

		reverseOrderItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.reverse_order"), menuItemMnemonicHelper, null, this);
		viewMenu.add(new JSeparator());
		swapFoldersItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.swap_folders"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK), this);
		setSameFolderItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.set_same_folder"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK), this);
//		refreshItem = addMenuItem(viewMenu, "Refresh", KeyEvent.VK_R, null);

		viewMenu.add(new JSeparator());
		// Menu item's text will be set later, when menu is selected
		showToolbarItem = MenuToolkit.addMenuItem(viewMenu, "", menuItemMnemonicHelper, null, this);
		
		add(viewMenu);
		
		// Window menu
		menuItemMnemonicHelper.clear();
		windowMenu = MenuToolkit.addMenu(Translator.get("window_menu"), menuItemMnemonicHelper, null);
		
		add(windowMenu);
		
		// Help menu
		menuItemMnemonicHelper.clear();
		helpMenu = MenuToolkit.addMenu(Translator.get("help_menu"), menuMnemonicHelper, null);
		keysItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.shortcuts"), menuItemMnemonicHelper, null, this);
		helpMenu.add(new JSeparator());
		if (PlatformManager.canOpenURL()) {
			homepageItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.homepage"), menuItemMnemonicHelper, null, this);
			forumsItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.forums"), menuItemMnemonicHelper, null, this);
		}
		helpMenu.add(new JSeparator());
		aboutItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.about"), menuItemMnemonicHelper, null, this);		
	
		add(helpMenu);
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
//		else if (source == renamerItem) {
//			(new RenamerDialog(mainFrame)).showDialog();	
//		}
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