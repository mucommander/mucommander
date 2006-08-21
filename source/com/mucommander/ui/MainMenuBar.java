
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.about.AboutDialog;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;
import com.mucommander.ui.editor.EditorFrame;
import com.mucommander.ui.help.ShortcutsDialog;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.viewer.ViewerFrame;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MucoAction;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;
import java.util.WeakHashMap;


/**
 * This class is the main menu bar. It takes care of displaying menu and menu items and triggering
 * the proper actions.
 *
 * <p><b>Implementation note</b>: for performance reasons, dynamic menu items are created/enabled/disabled when corresponding menus
 * are selected, instead of monitoring the MainFrame's state and unnecessarily creating/enabling/disabling menu items
 * when they are not visible. However, this prevents keyboard shortcuts from being managed by the menu bar for those
 * dynamic items.
 *
 * @author Maxence Bernard
 */
public class MainMenuBar extends JMenuBar implements ActionListener, MenuListener {

    private MainFrame mainFrame;	
	
    // File menu
    private JMenu fileMenu;
//    private JMenuItem newWindowItem;
//    private JMenuItem copyNamesItem;
//    private JMenuItem copyPathsItem;
//    private JMenuItem serverConnectItem;
//    private JMenuItem runItem;
    private JMenuItem packItem;
    private JMenuItem unpackItem;
    private JMenuItem emailFilesItem;
    private JMenuItem propertiesItem;
//    private JMenuItem preferencesItem;
//    private JMenuItem checkForUpdatesItem;
//    private JMenuItem closeItem;
//    private JMenuItem quitItem;

    // Mark menu
    private JMenu markMenu;
    private JMenuItem markGroupItem;
    private JMenuItem unmarkGroupItem;
    private JMenuItem markAllItem;
    private JMenuItem unmarkAllItem;
    private JMenuItem invertSelectionItem;
//    private JMenuItem compareFoldersItem;
	
    // View menu
    private JMenu viewMenu;
//    private JMenuItem changeFolderItem;
    private JMenuItem goBackItem;
    private JMenuItem goForwardItem;
    private JMenuItem goToParentItem;
//    private JMenuItem swapFoldersItem;
//    private JMenuItem setSameFolderItem;
    private JCheckBoxMenuItem sortByNameItem;
    private JCheckBoxMenuItem sortBySizeItem;
    private JCheckBoxMenuItem sortByDateItem;
    private JCheckBoxMenuItem sortByExtensionItem;
//    private JMenuItem reverseOrderItem;
//    private JCheckBoxMenuItem autoSizeColumnsItem;

    // Bookmark menu
    private JMenu bookmarksMenu;
//    private JMenuItem addBookmarkItem;
//    private JMenuItem editBookmarksItem;
    private int bookmarksOffset;  // Index of first bookmark in menu
    private Vector bookmarks;
    private Vector bookmarkMenuItems;

    // Window menu
    private JMenu windowMenu;
//    private JMenuItem showToolbarItem;
//    private JMenuItem showCommandBarItem;
//    private JMenuItem showStatusBarItem;

    /** Maps window menu items onto weakly-referenced frames */
    private WeakHashMap windowMenuFrames;

    // Help menu
//    private JMenu helpMenu;
    private JMenuItem keysItem;
    private JMenuItem homepageItem;
    private JMenuItem forumsItem;
    private JMenuItem donateItem;
    private JMenuItem aboutItem;


    /**
     * Creates a new MenuBar for the given MainFrame.
     */
    public MainMenuBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
		
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
		
        // File menu
        fileMenu = MenuToolkit.addMenu(Translator.get("file_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.NewWindowAction.class, mainFrame), menuItemMnemonicHelper);

/*
        fileMenu.add(new JSeparator());
        openItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.open"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), this);
        openItem = MenuToolkit.addMenuItem(fileMenu, ActionManager.getAction(com.mucommander.ui.action.OpenAction.class, mainFrame), menuItemMnemonicHelper);
        openNativelyItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.open_natively"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.SHIFT_MASK), this);
        revealInDesktopItem  = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_menu.reveal_in_desktop", PlatformManager.getDefaultDesktopFMName()), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK), this);
*/

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.ConnectToServerAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.RunCommandAction.class, mainFrame), menuItemMnemonicHelper);
        packItem = MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.PackAction.class, mainFrame), menuItemMnemonicHelper);
        unpackItem = MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.UnpackAction.class, mainFrame), menuItemMnemonicHelper);
        emailFilesItem = MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.EmailAction.class, mainFrame), menuItemMnemonicHelper);

        fileMenu.add(new JSeparator());
        propertiesItem = MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.PropertiesAction.class, mainFrame), menuItemMnemonicHelper);

        // Under Mac OS X, 'Preferences' already appears in the application (muCommander) menu, do not display it again
        if(PlatformManager.OS_FAMILY!=PlatformManager.MAC_OS_X) {
            fileMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.PreferencesAction.class, mainFrame), menuItemMnemonicHelper);
        }

        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.CheckForUpdatesAction.class, mainFrame), menuItemMnemonicHelper);
		
        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.CloseWindowAction.class, mainFrame), menuItemMnemonicHelper);
        // Under Mac OS X, 'Quit' already appears in the application (muCommander) menu, do not display it again
		if(PlatformManager.OS_FAMILY!=PlatformManager.MAC_OS_X)
            MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(com.mucommander.ui.action.QuitAction.class, mainFrame), menuItemMnemonicHelper);
        
        add(fileMenu);

        // Mark menu
        menuItemMnemonicHelper.clear();
        markMenu = MenuToolkit.addMenu(Translator.get("mark_menu"), menuMnemonicHelper, this);
        // Accelerators (keyboard shortcuts) for the following menu items will be set/unset when the menu is selected/deselected
        markGroupItem = MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(com.mucommander.ui.action.MarkGroupAction.class, mainFrame), menuItemMnemonicHelper);
        unmarkGroupItem = MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(com.mucommander.ui.action.UnmarkGroupAction.class, mainFrame), menuItemMnemonicHelper);
        markAllItem = MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(com.mucommander.ui.action.MarkAllAction.class, mainFrame), menuItemMnemonicHelper);
        unmarkAllItem = MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(com.mucommander.ui.action.UnmarkAllAction.class, mainFrame), menuItemMnemonicHelper);
        invertSelectionItem = MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(com.mucommander.ui.action.InvertSelectionAction.class, mainFrame), menuItemMnemonicHelper);

        markMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(com.mucommander.ui.action.CompareFoldersAction.class, mainFrame), menuItemMnemonicHelper);

        markMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(com.mucommander.ui.action.CopyFilenamesAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(com.mucommander.ui.action.CopyPathsAction.class, mainFrame), menuItemMnemonicHelper);

        add(markMenu);

        // View menu
        menuItemMnemonicHelper.clear();
        viewMenu = MenuToolkit.addMenu(Translator.get("view_menu"), menuMnemonicHelper, this);
        goBackItem = MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.GoBackAction.class, mainFrame), menuItemMnemonicHelper);
        // Accelerators (keyboard shortcuts) for the following menu items will be set/unset when the menu is selected/deselected
        goForwardItem = MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.GoForwardAction.class, mainFrame), menuItemMnemonicHelper);
        goToParentItem = MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.GoToParentAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.ChangeLocationAction.class, mainFrame), menuItemMnemonicHelper);
        viewMenu.add(new JSeparator());
        sortByExtensionItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.SortByExtensionAction.class, mainFrame), menuItemMnemonicHelper);
        sortByNameItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.SortByNameAction.class, mainFrame), menuItemMnemonicHelper);
        sortBySizeItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.SortBySizeAction.class, mainFrame), menuItemMnemonicHelper);
        sortByDateItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.SortByDateAction.class, mainFrame), menuItemMnemonicHelper);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(sortByNameItem);
        buttonGroup.add(sortByDateItem);
        buttonGroup.add(sortBySizeItem);
        buttonGroup.add(sortByExtensionItem);
        sortByNameItem.setState(true);

        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.ReverseSortOrderAction.class, mainFrame), menuItemMnemonicHelper);
        viewMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.SwapFoldersAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.SetSameFolderAction.class, mainFrame), menuItemMnemonicHelper);

        viewMenu.add(new JSeparator());
        MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.ToggleAutoSizeAction.class, mainFrame), menuItemMnemonicHelper);

        viewMenu.add(new JSeparator());
        // Menu item's text will be set later, when menu is selected
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.ToggleToolBarAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.ToggleStatusBarAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(com.mucommander.ui.action.ToggleCommandBarAction.class, mainFrame), menuItemMnemonicHelper);
		
        add(viewMenu);

        // Bookmark menu, menu items will be added when the menu gets selected
        menuItemMnemonicHelper.clear();
        bookmarksMenu = MenuToolkit.addMenu(Translator.get("bookmarks_menu"), menuItemMnemonicHelper, this);
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(com.mucommander.ui.action.AddBookmarkAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(com.mucommander.ui.action.EditBookmarksAction.class, mainFrame), menuItemMnemonicHelper);
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
        JMenu helpMenu = MenuToolkit.addMenu(Translator.get("help_menu"), menuMnemonicHelper, null);
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
        if(PlatformManager.OS_FAMILY!=PlatformManager.MAC_OS_X) {
            helpMenu.add(new JSeparator());
            aboutItem = MenuToolkit.addMenuItem(helpMenu, Translator.get("help_menu.about"), menuItemMnemonicHelper, null, this);		
        }
		
        add(helpMenu);
    }
	

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
        if(menu == fileMenu) {
            FileTable fileTable = mainFrame.getLastActiveTable();
            boolean filesSelected = enableUnconditional || fileTable.getSelectedFiles().size()>0;
                
            // enable/disable those menu items if no file is selected
            propertiesItem.setEnabled(filesSelected);
            packItem.setEnabled(filesSelected);
            unpackItem.setEnabled(filesSelected);
            emailFilesItem.setEnabled(filesSelected);
        }
        else if(menu == markMenu) {
            // Display accelerators when menu is selected and remove them when it get deselected (keyboard shortcut is caught in FileTable)
            if(menu.isSelected()) {
                restoreAccelerator(markGroupItem);
                restoreAccelerator(unmarkGroupItem);
                restoreAccelerator(markAllItem);
                restoreAccelerator(unmarkAllItem);
                restoreAccelerator(invertSelectionItem);
            }
            else {
                markGroupItem.setAccelerator(null);
                unmarkGroupItem.setAccelerator(null);
                markAllItem.setAccelerator(null);
                unmarkAllItem.setAccelerator(null);
                invertSelectionItem.setAccelerator(null);
            }
        }
        else if(menu == viewMenu) {
            // Display accelerators when menu is selected and remove them when it get deselected (keyboard shortcut is caught in FileTable)
            if(menu.isSelected()) {
                FolderPanel folderPanel = mainFrame.getLastActiveTable().getFolderPanel();

                restoreAccelerator(goBackItem);
                restoreAccelerator(goForwardItem);
                restoreAccelerator(goToParentItem);
            }
            else {
                goBackItem.setAccelerator(null);
                goForwardItem.setAccelerator(null);
                goToParentItem.setAccelerator(null);
            }
        }
        else if(menu == windowMenu && !menu.isSelected()) {
            // Remove accelerators from all menu items but do not remove or disable menu items,
            // this would cause the actionPerformed() method not be called when item is clicked.            
            int nbWindowMenuItems = windowMenu.getItemCount();
            JMenuItem menuItem;
            for(int i=0; i<nbWindowMenuItems; i++) {
                menuItem = windowMenu.getItem(i);
                if(menuItem!=null)
                    menuItem.setAccelerator(null);
            }
        }
    }


    private void restoreAccelerator(JMenuItem menuItem) {
        menuItem.setAccelerator(((MucoAction)menuItem.getAction()).getAccelerator());
    }


    ///////////////////////////
    // ActionListener method //
    ///////////////////////////

    public void actionPerformed(ActionEvent e) {
        // Discard action events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

//        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("mainFrame.getFocusOwner()="+mainFrame.getFocusOwner()+" isSelected()="+isSelected());
//        if(!(mainFrame.getFocusOwner() instanceof FileTable))
//            return;

        Object source = e.getSource();

/*
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
        else if (source==packItem || source==unpackItem || source==emailFilesItem || source==propertiesItem) {
            // This actions need to work on selected files
            FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
            int nbSelectedFiles = files.size();
		
            if(nbSelectedFiles>0) {
                if (source == packItem) {
                    new PackDialog(mainFrame, files, false);
                }
                else if (source == unpackItem) {
                    new UnpackDialog(mainFrame, files, false);
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
        else if (source == quitItem) {
            // Quit after asking user for confirmation
            if(QuitDialog.confirmQuit())
                WindowManager.quit();
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
*/
        // Bookmark menu item
        if (bookmarkMenuItems!=null && bookmarkMenuItems.contains(source)) {
            int index = bookmarkMenuItems.indexOf(source);
            mainFrame.getLastActiveTable().getFolderPanel().trySetCurrentFolder(((Bookmark)bookmarks.elementAt(index)).getURL());
        }
        // Help menu
        else if (source == keysItem) {
            new ShortcutsDialog(mainFrame).showDialog();
        }
        else if (source == homepageItem) {
            PlatformManager.openURLInBrowser(com.mucommander.RuntimeConstants.HOMEPAGE_URL);
        }
        else if (source == forumsItem) {
            PlatformManager.openURLInBrowser(com.mucommander.RuntimeConstants.FORUMS_URL);
        }
        else if (source == donateItem) {
            PlatformManager.openURLInBrowser(com.mucommander.RuntimeConstants.DONATION_URL);
        }
        else if (source == aboutItem) {
            new AboutDialog(mainFrame).showDialog();
        }
        // Window menu item
        else {
            // Bring the frame corresponding to the clicked menu item to the front
            ((JFrame)windowMenuFrames.get(source)).toFront();
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

/*
            showToolbarItem.setText(mainFrame.isToolbarVisible()?Translator.get("view_menu.hide_toolbar"):Translator.get("view_menu.show_toolbar"));
            showCommandBarItem.setText(mainFrame.isCommandBarVisible()?Translator.get("view_menu.hide_command_bar"):Translator.get("view_menu.show_command_bar"));
            showStatusBarItem.setText(mainFrame.isStatusBarVisible()?Translator.get("view_menu.hide_status_bar"):Translator.get("view_menu.show_status_bar"));
            autoSizeColumnsItem.setSelected(activeTable.getAutoSizeColumns());
*/
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
            // Note: menu item cannot be removed by menuDeselected() as actionPerformed() will be called after
            // menu has been deselected.
            windowMenu.removeAll();
            // This WeakHashMap maps menu items to frame instances. It has to be a weakly referenced hash map
            // and not a regular hash map, since it will not (and cannot) be emptied when the menu has been deselected
            // and we really do not want this hash map to prevent the frames to be GCed 
            windowMenuFrames = new WeakHashMap();
            
            // Create a menu item for each of the MainFrame instances, that displays the MainFrame's path
            // and a keyboard shortcut to recall the frame (for the first 10 frames only). 
            java.util.Vector mainFrames = WindowManager.getMainFrames();
            MainFrame mainFrame;
            JCheckBoxMenuItem checkBoxMenuItem;
            int nbFrames = mainFrames.size();
            for(int i=0; i<nbFrames; i++) {
                mainFrame = (MainFrame)mainFrames.elementAt(i);
                checkBoxMenuItem = new JCheckBoxMenuItem((i+1)+" "+mainFrame.getLastActiveTable().getCurrentFolder().getAbsolutePath(), mainFrame==this.mainFrame);
                checkBoxMenuItem.addActionListener(this);
                // The accelator is set just for 'decoration' purposes, i.e. just to indicate what the shortcut is.
                // All accelators are removed after the menu is deselected as window shortcuts are managed
                // by MainFrame directly.
                if(i<10)
                    checkBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(i==9?KeyEvent.VK_0:i+KeyEvent.VK_1, ActionEvent.CTRL_MASK));

                windowMenu.add(checkBoxMenuItem);                
                windowMenuFrames.put(checkBoxMenuItem, mainFrame);
            }
            
            // Add 'other' (non-MainFrame) windows : viewer and editor frames.
            Frame frames[] = Frame.getFrames();
            nbFrames = frames.length;
            Frame frame;
            JMenuItem menuItem;
            boolean firstFrame = true;
            for(int i=0; i<nbFrames; i++) {
                frame = frames[i];
                // Test if Frame is not hidden (disposed), Frame.getFrames() returns both active and disposed frames
                if(frame.isShowing() && (frame instanceof ViewerFrame) || (frame instanceof EditorFrame)) {
                    // Add a separator before the first non-MainFrame frame to mark a separation between MainFrames
                    // and other frames
                    if(firstFrame) {
                        windowMenu.add(new JSeparator());
                        firstFrame = false;
                    }
                    // Use frame's window title
                    menuItem = new JMenuItem(frame.getTitle());
                    menuItem.addActionListener(this);
                    windowMenu.add(menuItem);
                    windowMenuFrames.put(menuItem, frame);
                }
            }
        }
    }
	
    public void menuDeselected(MenuEvent e) {
        // Enable all contextual menu items
        toggleContextualMenuItems((JMenu)e.getSource(), true); 
    }
	 
    public void menuCanceled(MenuEvent e) {
        // Enable all contextual menu items
        toggleContextualMenuItems((JMenu)e.getSource(), true); 
    }
}
