
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
//public class MainMenuBar extends JMenuBar implements LocationListener, TableChangeListener, ActionListener, MenuListener {
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
    private JMenuItem changeFolderItem;
    private JMenuItem goBackItem;
    private JMenuItem goForwardItem;
    private JMenuItem goToParentItem;
    private JMenuItem swapFoldersItem;
    private JMenuItem setSameFolderItem;
    private JCheckBoxMenuItem sortByNameItem;
    private JCheckBoxMenuItem sortBySizeItem;
    private JCheckBoxMenuItem sortByDateItem;
    private JCheckBoxMenuItem sortByExtensionItem;
    private JMenuItem reverseOrderItem;
    private JCheckBoxMenuItem autoSizeColumnsItem;

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

        // Under Mac OS X, 'Preferences' already appears in the application (muCommander) menu, do not display it again
        if(PlatformManager.getOSFamily()!=PlatformManager.MAC_OS_X) {
            fileMenu.add(new JSeparator());
            preferencesItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.preferences"), menuItemMnemonicHelper, null, this);
        }

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
        goToParentItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.go_to_parent"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), this);
        changeFolderItem = MenuToolkit.addMenuItem(viewMenu, Translator.get("view_menu.change_current_location"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK), this);
        viewMenu.add(new JSeparator());
        sortByExtensionItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_extension"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.CTRL_MASK), this);
        sortByNameItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_name"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_MASK), this);
        sortBySizeItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_size"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK), this);
        sortByDateItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.sort_by_date"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.CTRL_MASK), this);
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
        autoSizeColumnsItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("view_menu.auto_size_columns"), menuItemMnemonicHelper, null, this);

        viewMenu.add(new JSeparator());
        // Menu item's text will be set later, when menu is selected
        showToolbarItem = MenuToolkit.addMenuItem(viewMenu, "", menuItemMnemonicHelper, null, this);
        showStatusBarItem = MenuToolkit.addMenuItem(viewMenu, "", menuItemMnemonicHelper, null, this);
        showCommandBarItem = MenuToolkit.addMenuItem(viewMenu, "", menuItemMnemonicHelper, null, this);
		
        add(viewMenu);
		
        // Bookmark menu, menu items will be added when the menu gets selected
        menuItemMnemonicHelper.clear();
        bookmarksMenu = MenuToolkit.addMenu(Translator.get("bookmarks_menu"), menuItemMnemonicHelper, this);
        addBookmarkItem = MenuToolkit.addMenuItem(bookmarksMenu, Translator.get("bookmarks_menu.add_bookmark"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK), this);
        editBookmarksItem = MenuToolkit.addMenuItem(bookmarksMenu, Translator.get("bookmarks_menu.edit_bookmarks"), menuItemMnemonicHelper, null, this);
        bookmarksMenu.add(new JSeparator());
        // Remember bookmarks offset (index of first bookmark in menu) for later
        this.bookmarksOffset = bookmarksMenu.getItemCount();
		
        add(bookmarksMenu);
		
        // Window menu, menu items will be added when the menu gets selected
        menuItemMnemonicHelper.clear();

        windowMenu = MenuToolkit.addMenu(Translator.get("window_menu"), menuItemMnemonicHelper, this);
		
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
		
        // Under Mac OS X, 'About' already appears in the application (muCommander) menu, do not display it again
        if(PlatformManager.getOSFamily()!=PlatformManager.MAC_OS_X) {
            helpMenu.add(new JSeparator());
            aboutItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.about"), menuItemMnemonicHelper, null, this);		
        }
		
        add(helpMenu);
		
//        // Set initial enabled state of contextual menu items 
//        toggleContextualMenuItems(mainFrame.getLastActiveTable().getFolderPanel());
		
//        // Listen to location and table change events to change the state of contextual menu items
//        // when current folder or active table has changed
//        mainFrame.getFolderPanel1().addLocationListener(this);
//        mainFrame.getFolderPanel2().addLocationListener(this);
//        mainFrame.addTableChangeListener(this);
    }
	

//    /**
//     * Returns the 'Window' JMenu instance.
//     */
//    public JMenu getWindowMenu() {
//        return windowMenu;
//    }


    /**
     * Enables/disables contextual menu items of the given menu, based on the current context. 
     * This method is called when a menu is pulled down, and called again when menu is deselected or cancelled, 
     * to enable all menu items (unconditional) so that their shortcuts are properly caught.
     *
     * @param menu menu that just got selected/deselected/cancelled
     * @param enableUnconditional if true, all conditional menu items in the given menu have to be enabled for
     * the shortcuts to be active
     */
    private void toggleContextualMenuItems(JMenu menu, boolean enableUnconditional) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");

        if(menu == fileMenu) {
            FileTable fileTable = mainFrame.getLastActiveTable();
            boolean filesSelected = enableUnconditional || fileTable.getSelectedFiles().size()>0;
                
            // enable/disable thos menu items if no file is selected
            propertiesItem.setEnabled(filesSelected);
            zipItem.setEnabled(filesSelected);
            unzipItem.setEnabled(filesSelected);
            emailFilesItem.setEnabled(filesSelected);
        }
        else if(menu == viewMenu) {
            FolderPanel folderPanel = mainFrame.getLastActiveTable().getFolderPanel();

            goBackItem.setEnabled(enableUnconditional || folderPanel.getFolderHistory().hasBackFolder());
            goForwardItem.setEnabled(enableUnconditional || folderPanel.getFolderHistory().hasForwardFolder());
            goToParentItem.setEnabled(enableUnconditional || folderPanel.getCurrentFolder().getParent()!=null);
        }
        else if(menu == windowMenu && !menu.isSelected()) {
            // Disable accelerators on all menu items but do not remove or disable them,
            // this would cause the actionPerformed() method not be called when item is clicked.            
            int nbWindowMenuItems = windowMenu.getItemCount();
            for(int i=0; i<nbWindowMenuItems; i++)
                windowMenu.getItem(i).setAccelerator(null);
        }
    }


/*
    /////////////////////////////////
    // TableChangeListener methods //
    /////////////////////////////////
	
    public void tableChanged(FolderPanel folderPanel) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");

        // Toggle contextual menu items on/off to reflect current table's folder
        toggleContextualMenuItems(folderPanel);
    }
	

    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////
	
    public void locationChanged(LocationEvent e) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");

        // Toggle contextual menu items on/off to reflect new folder's location
        toggleContextualMenuItems(e.getFolderPanel());
    }

    public void locationChanging(LocationEvent e) {
    }
	
    public void locationCancelled(LocationEvent e) {
    }
*/	

    ///////////////////////////
    // ActionListener method //
    ///////////////////////////

    public void actionPerformed(ActionEvent e) {
        // Discard action events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
		
        Object source = e.getSource();

        // File menu
        if (source == newWindowItem) {
            WindowManager.createNewMainFrame();
        }
        else if (source == serverConnectItem) {
            mainFrame.showServerConnectDialog();
        }
        else if (source == runItem) {
            new RunDialog(mainFrame);
        }
        else if (source==zipItem || source==unzipItem || source==emailFilesItem || source==propertiesItem) {
            // This actions need to work on selected files
            FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
            int nbSelectedFiles = files.size();
		
            if(nbSelectedFiles>0) {
                if (source == zipItem) {
                    new PackDialog(mainFrame, files, false);
                }
                else if (source == unzipItem) {
                    new UnzipDialog(mainFrame, files, false);
                }
                else if (source == emailFilesItem) {
                    new EmailFilesDialog(mainFrame, files);
                }
                else {      // propertiesItem
                    mainFrame.showPropertiesDialog();
                }
            }
        }
        else if (source == preferencesItem) {
            mainFrame.showPreferencesDialog();
        }
        else if (source == checkForUpdatesItem) {
            new CheckVersionDialog(mainFrame, true);
        }
        else if (source == closeItem) {
            mainFrame.dispose();
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
        else if (source == changeFolderItem) {
            mainFrame.getLastActiveTable().getFolderPanel().changeCurrentLocation();	
        }
        else if (source == goBackItem) {
            mainFrame.getLastActiveTable().getFolderPanel().getFolderHistory().goBack();	
        }
        else if (source == goForwardItem) {
            mainFrame.getLastActiveTable().getFolderPanel().getFolderHistory().goForward();	
        }
        else if (source == goToParentItem) {
            mainFrame.getLastActiveTable().getFolderPanel().goToParent();
        }
        else if (source == sortByExtensionItem) {
            mainFrame.getLastActiveTable().sortBy(FileTable.EXTENSION);	
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
            mainFrame.getLastActiveTable().getFolderPanel().trySetCurrentFolder(((Bookmark)bookmarks.elementAt(index)).getURL());
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
        // Window menu item
        else {
            int nbWindowMenuItems = windowMenu.getItemCount();
            // Recall MainFrame corresponding to the clicked menu item.
            // Unfortunately there's such method as getItemIndex() in JMenu, so we need
            // to interate thru all menu items to find the clicked menu item's index.
            for(int i=0; i<nbWindowMenuItems; i++) {
                if(source==windowMenu.getItem(i)) {
                    ((MainFrame)WindowManager.getMainFrames().elementAt(i)).toFront();
                    break;
                }
            }
        }
    }


    //////////////////////////
    // MenuListener methods //
    //////////////////////////

    public void menuSelected(MenuEvent e) {
        Object source = e.getSource();

        // Enable/disable contextual menu items depending on the current context
        toggleContextualMenuItems((JMenu)source, false); 

        if(source==viewMenu) {
            FileTable activeTable = mainFrame.getLastActiveTable();

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
            autoSizeColumnsItem.setSelected(activeTable.getAutoSizeColumns());
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
        else if(source==windowMenu) {
            // Start by removing any menu item previously added
            // Note: menu item cannot be removed by menuDeselected() as actionPerformed() can be called after
            // menu has been deselected.
            windowMenu.removeAll();
            // Create a menu item for each of the MainFrame instances, that displays the MainFrame's path
            // and associated shortcut to recall the frame. 
            java.util.Vector mainFrames = WindowManager.getMainFrames();
            MainFrame mainFrame;
            JCheckBoxMenuItem checkBox;
            int nbFrames = mainFrames.size();
            for(int i=0; i<nbFrames; i++) {
                mainFrame = (MainFrame)mainFrames.elementAt(i);
                checkBox = new JCheckBoxMenuItem((i+1)+" "+mainFrame.getLastActiveTable().getCurrentFolder().getAbsolutePath(), mainFrame==this.mainFrame);
                checkBox.addActionListener(this);
                // The accelator is set just for 'decoration' purposes, i.e. just to indicate what the shortcut is.
                // All accelators are removed after the menu is deselected as window shortcuts are managed
                // by MainFrame directly.
                if(i<10)
                    checkBox.setAccelerator(KeyStroke.getKeyStroke(i==9?KeyEvent.VK_0:i+KeyEvent.VK_1, ActionEvent.CTRL_MASK));
                windowMenu.add(checkBox);
            }
        }
    }
	
    public void menuDeselected(MenuEvent e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");    
        // Enable all contextual menu items
        toggleContextualMenuItems((JMenu)e.getSource(), true); 
    }
	 
    public void menuCanceled(MenuEvent e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");    
        // Enable all contextual menu items
        toggleContextualMenuItems((JMenu)e.getSource(), true); 
    }
}
