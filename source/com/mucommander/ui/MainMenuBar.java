
package com.mucommander.ui;

import com.mucommander.PlatformManager;

import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.EmailFilesDialog;
import com.mucommander.ui.help.ShortcutsDialog;
import com.mucommander.ui.about.AboutDialog;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;
import com.mucommander.ui.bookmark.AddBookmarkDialog;
import com.mucommander.ui.bookmark.EditBookmarksDialog;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.file.FileSet;

import com.mucommander.text.Translator;

import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.bookmark.Bookmark;

import com.mucommander.event.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.Vector;


/**
 * This class is the main menu bar. It takes care of displaying menu and menu items and triggering
 * the proper actions.
 *
 * @author Maxence Bernard
 */
public class MainMenuBar extends JMenuBar implements ActionListener, MenuListener {
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
	private JMenuItem closeItem;

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
	private JCheckBoxMenuItem autoSizeColumnsItem;
	private JMenuItem goBackItem;
	private JMenuItem goForwardItem;
	private JMenuItem goToParentItem;
	private JMenuItem swapFoldersItem;
	private JMenuItem setSameFolderItem;

	// Bookmark menu
	private JMenu bookmarksMenu;
	private JMenuItem addBookmarkItem;
	private JMenuItem editBookmarksItem;
	private int bookmarksOffset;  // Index of first bookmark in menu
	private Vector bookmarks;
	private Vector bookmarkMenuItems;

	// Window menu
	private JMenu windowMenu;
	private JMenuItem showToolbarItem;
	private JMenuItem showCommandBarItem;
	private JMenuItem showStatusBarItem;

	// Help menu
	private JMenu helpMenu;
	private JMenuItem keysItem;
	private JMenuItem homepageItem;
	private JMenuItem forumsItem;
	private JMenuItem donateItem;
	private JMenuItem aboutItem;
	
	private final static String MUCOMMANDER_HOMEPAGE_URL = "http://www.mucommander.com";
	private final static String MUCOMMANDER_FORUMS_URL = "http://www.mucommander.com/forums/";
	private final static String MUCOMMANDER_DONATE_URL = "http://www.mucommander.com/#donate";

	
	public MainMenuBar(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		
		MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
		MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
		
		// File menu
		fileMenu = MenuToolkit.addMenu(Translator.get("file_menu"), menuMnemonicHelper, this);
		newWindowItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.new_window"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK), this);

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
		closeItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.close_window"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), this);
		
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

		add(markMenu);
		
		// View menu
		menuItemMnemonicHelper.clear();
		viewMenu = MenuToolkit.addMenu(Translator.get("view_menu"), menuMnemonicHelper, this);
		goBackItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.go_back"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK), this);
		goForwardItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.go_forward"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK), this);
		goToParentItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.go_to_parent"), menuItemMnemonicHelper, null, this);
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

		viewMenu.add(new JSeparator());
		// Auto column sizing
		autoSizeColumnsItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.auto_size_columns"), menuItemMnemonicHelper, null, this);

		viewMenu.add(new JSeparator());
		// Menu item's text will be set later, when menu is selected
		showToolbarItem = MenuToolkit.addMenuItem(viewMenu, "", menuItemMnemonicHelper, null, this);
		showStatusBarItem = MenuToolkit.addMenuItem(viewMenu, "", menuItemMnemonicHelper, null, this);
		showCommandBarItem = MenuToolkit.addMenuItem(viewMenu, "", menuItemMnemonicHelper, null, this);
		
		add(viewMenu);
		
		// Bookmark menu, bookmarks will be added to the menu each it gets displayed
		menuItemMnemonicHelper.clear();
		bookmarksMenu = MenuToolkit.addMenu(Translator.get("bookmarks_menu"), menuItemMnemonicHelper, this);
		addBookmarkItem = MenuToolkit.addMenuItem(bookmarksMenu, Translator.get("bookmarks_menu.add_bookmark"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK), this);
		editBookmarksItem = MenuToolkit.addMenuItem(bookmarksMenu, Translator.get("bookmarks_menu.edit_bookmarks"), menuItemMnemonicHelper, null, this);
		bookmarksMenu.add(new JSeparator());
		// Remember bookmarks offset (index of first bookmark in menu) for later
		this.bookmarksOffset = bookmarksMenu.getItemCount();
		
		add(bookmarksMenu);
		
		// Window menu
		menuItemMnemonicHelper.clear();
		windowMenu = MenuToolkit.addMenu(Translator.get("window_menu"), menuItemMnemonicHelper, null);
		
		add(windowMenu);
		
		// Help menu
		menuItemMnemonicHelper.clear();
		helpMenu = MenuToolkit.addMenu(Translator.get("help_menu"), menuMnemonicHelper, null);
		// Keyboard shortuts
		keysItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.shortcuts"), menuItemMnemonicHelper, null, this);
		// Links to website, only shows for OSes that can launch the default browser to open URLs
		if (PlatformManager.canOpenURLInBrowser()) {
			helpMenu.add(new JSeparator());
			homepageItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.homepage"), menuItemMnemonicHelper, null, this);
			forumsItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.forums"), menuItemMnemonicHelper, null, this);
			donateItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.donate"), menuItemMnemonicHelper, null, this);
		}
		
		// 'About' in Mac OS X already appears in the app menu, no need to add it again
		if(PlatformManager.getOSFamily()!=PlatformManager.MAC_OS_X) {
			helpMenu.add(new JSeparator());
			aboutItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.about"), menuItemMnemonicHelper, null, this);		
		}
		
		add(helpMenu);
	}
	
	public MainFrame getMainFrame() {
		return mainFrame;
	}
	
	public JMenu getWindowMenu() {
		return windowMenu;	
	}


	///////////////////////////
	// ActionListener method //
	///////////////////////////

	public void actionPerformed(ActionEvent e) {
		// Discard action events while in 'no events mode'
		if(mainFrame.getNoEventsMode())
			return;

		Object source = e.getSource();

		// Some actions need to work on selected files
		FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
		int nbSelectedFiles = files.size();
		
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
			if(nbSelectedFiles>0)
				new PackDialog(mainFrame, files, false);
		}
		else if (source == unzipItem) {
			if(nbSelectedFiles>0)
				new UnzipDialog(mainFrame, files, false);
		}
        else if  (source == emailFilesItem) {
			if(nbSelectedFiles>0)
				new EmailFilesDialog(mainFrame, files);
		}
		else if (source == preferencesItem) {
			mainFrame.showPreferencesDialog();
		}
		else if (source == checkForUpdatesItem) {
			new CheckVersionDialog(mainFrame, true);
		}
		else if (source == closeItem) {
			WindowManager.getInstance().disposeMainFrame(mainFrame);
		}
		// Mark menu
		else if (source == markGroupItem) {
			mainFrame.showSelectionDialog(true);	
		}
		else if (source == unmarkGroupItem) {
			mainFrame.showSelectionDialog(false);	
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
			mainFrame.getLastActiveTable().getFolderPanel().goBack();	
		}
		else if (source == goForwardItem) {
			mainFrame.getLastActiveTable().getFolderPanel().goForward();	
		}
		else if (source == goToParentItem) {
			mainFrame.getLastActiveTable().getFolderPanel().goToParent();
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
		else if (source == autoSizeColumnsItem) {
			boolean selected = autoSizeColumnsItem.isSelected();
			mainFrame.getLastActiveTable().setAutoSizeColumnsEnabled(selected);
			ConfigurationManager.setVariableBoolean("prefs.file_table.auto_size_columns", selected);		
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
		else if (source == showCommandBarItem) {
			mainFrame.setCommandBarVisible(!mainFrame.isCommandBarVisible());
		}
		else if (source == showStatusBarItem) {
			mainFrame.setStatusBarVisible(!mainFrame.isStatusBarVisible());
		}
		// Bookmark menu
		else if (source == addBookmarkItem) {
			new AddBookmarkDialog(mainFrame);
		}
		else if (source == editBookmarksItem) {
			new EditBookmarksDialog(mainFrame);
		}
		// Bookmark menu item
		else if (bookmarkMenuItems!=null && bookmarkMenuItems.contains(source)) {
			int index = bookmarkMenuItems.indexOf(source);
			mainFrame.getLastActiveTable().getFolderPanel().trySetCurrentFolder(((Bookmark)bookmarks.elementAt(index)).getURL(), true);
		}		
		// Help menu
		else if (source == keysItem) {
			new ShortcutsDialog(mainFrame).showDialog();
		}
		else if (source == homepageItem) {
			PlatformManager.openURLInBrowser(MUCOMMANDER_HOMEPAGE_URL);
		}
		else if (source == forumsItem) {
			PlatformManager.openURLInBrowser(MUCOMMANDER_FORUMS_URL);
		}
		else if (source == donateItem) {
			PlatformManager.openURLInBrowser(MUCOMMANDER_DONATE_URL);
		}
		else if (source == aboutItem) {
			new AboutDialog(mainFrame).showDialog();
		}
	}


	//////////////////////////
	// MenuListener methods //
	//////////////////////////

	public void menuSelected(MenuEvent e) {
	 	Object source = e.getSource();
		if (source==fileMenu) {
			boolean filesSelected = mainFrame.getLastActiveTable().getSelectedFiles().size()!=0;
			
			// disable menu items if no file is selected
			propertiesItem.setEnabled(filesSelected);
			zipItem.setEnabled(filesSelected);
			unzipItem.setEnabled(filesSelected);
			emailFilesItem.setEnabled(filesSelected);
		}
		else if(source==viewMenu) {
			FileTable activeTable = mainFrame.getLastActiveTable();
			FolderPanel folderPanel = activeTable.getFolderPanel();
			goBackItem.setEnabled(folderPanel.hasBackFolder());
			goForwardItem.setEnabled(folderPanel.hasForwardFolder());
			goToParentItem.setEnabled(folderPanel.getCurrentFolder().getParent()!=null);

			// Toggle current sort by menu item
			int criteria = activeTable.getSortByCriteria();
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

			showToolbarItem.setText(mainFrame.isToolbarVisible()?Translator.get("view_menu.hide_toolbar"):Translator.get("view_menu.show_toolbar"));
			showCommandBarItem.setText(mainFrame.isCommandBarVisible()?Translator.get("view_menu.hide_command_bar"):Translator.get("view_menu.show_command_bar"));
			showStatusBarItem.setText(mainFrame.isStatusBarVisible()?Translator.get("view_menu.hide_status_bar"):Translator.get("view_menu.show_status_bar"));
			autoSizeColumnsItem.setSelected(mainFrame.getLastActiveTable().getAutoSizeColumns());
		}
		else if(source==bookmarksMenu) {
			if(this.bookmarkMenuItems != null) {
				// Remove any previous bookmarks menu items from menu
				// as bookmarks might have changed since menu was last selected
				for(int i=bookmarksMenu.getItemCount(); i>bookmarksOffset; i--)
					bookmarksMenu.remove(bookmarksOffset);
			}

			// Add bookmarks menu items
			this.bookmarks = BookmarkManager.getBookmarks();
			this.bookmarkMenuItems = new Vector();
			int nbBookmarks = bookmarks.size();
			MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
			if(nbBookmarks>0) {
				for(int i=0; i<nbBookmarks; i++)
					bookmarkMenuItems.add(MenuToolkit.addMenuItem(bookmarksMenu, ((Bookmark)bookmarks.elementAt(i)).getName(), menuItemMnemonicHelper, null, this));
			}
			else {
				// Show 'No bookmark' as a disabled menu item instead showing nothing
				JMenuItem noBookmarkItem = MenuToolkit.addMenuItem(bookmarksMenu, Translator.get("bookmarks_menu.no_bookmark"), menuItemMnemonicHelper, null, null);
				noBookmarkItem.setEnabled(false);
				bookmarkMenuItems.add(noBookmarkItem);
			}
		}
	}
	
	public void menuDeselected(MenuEvent e) {
	}
	 
	public void menuCanceled(MenuEvent e) {
	}
}