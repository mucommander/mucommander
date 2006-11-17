
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.about.AboutDialog;
import com.mucommander.ui.action.*;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;
import com.mucommander.ui.editor.EditorFrame;
import com.mucommander.ui.help.ShortcutsDialog;
import com.mucommander.ui.viewer.ViewerFrame;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	
    // Bookmark menu
    private JMenu bookmarksMenu;
    private int bookmarksOffset;  // Index of the first bookmark menu item
    private Vector bookmarks;
    private Vector bookmarkMenuItems;

    // Window menu
    private JMenu windowMenu;
    private int windowOffset; // Index of the first window menu item

    /** Maps window menu items onto weakly-referenced frames */
    private WeakHashMap windowMenuFrames;

    // Help menu
//    private JMenu helpMenu;
    private JMenuItem keysItem;
    private JMenuItem homepageItem;
    private JMenuItem forumsItem;
    private JMenuItem donateItem;
    private JMenuItem aboutItem;


    private final static Class RECALL_WINDOW_ACTIONS[] = {
        com.mucommander.ui.action.RecallWindow1Action.class,
        com.mucommander.ui.action.RecallWindow2Action.class,
        com.mucommander.ui.action.RecallWindow3Action.class,
        com.mucommander.ui.action.RecallWindow4Action.class,
        com.mucommander.ui.action.RecallWindow5Action.class,
        com.mucommander.ui.action.RecallWindow6Action.class,
        com.mucommander.ui.action.RecallWindow7Action.class,
        com.mucommander.ui.action.RecallWindow8Action.class,
        com.mucommander.ui.action.RecallWindow9Action.class,
        com.mucommander.ui.action.RecallWindow10Action.class
    };



    /**
     * Creates a new MenuBar for the given MainFrame.
     */
    public MainMenuBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
		
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
		
        // File menu
        JMenu fileMenu = MenuToolkit.addMenu(Translator.get("file_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(NewWindowAction.class, mainFrame), menuItemMnemonicHelper);

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(OpenAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(OpenNativelyAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(RevealInDesktopAction.class, mainFrame), menuItemMnemonicHelper);

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ConnectToServerAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(RunCommandAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(PackAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(UnpackAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(EmailAction.class, mainFrame), menuItemMnemonicHelper);

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ShowFilePropertiesAction.class, mainFrame), menuItemMnemonicHelper);

        // Under Mac OS X, 'Preferences' already appears in the application (muCommander) menu, do not display it again
        if(PlatformManager.OS_FAMILY!=PlatformManager.MAC_OS_X) {
            fileMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ShowPreferencesAction.class, mainFrame), menuItemMnemonicHelper);
        }

        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(CheckForUpdatesAction.class, mainFrame), menuItemMnemonicHelper);
		
        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(CloseWindowAction.class, mainFrame), menuItemMnemonicHelper);
        // Under Mac OS X, 'Quit' already appears in the application (muCommander) menu, do not display it again
		if(PlatformManager.OS_FAMILY!=PlatformManager.MAC_OS_X)
            MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(QuitAction.class, mainFrame), menuItemMnemonicHelper);
        
        add(fileMenu);

        // Mark menu
        menuItemMnemonicHelper.clear();
        JMenu markMenu = MenuToolkit.addMenu(Translator.get("mark_menu"), menuMnemonicHelper, this);
        // Accelerators (keyboard shortcuts) for the following menu items will be set/unset when the menu is selected/deselected
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(MarkGroupAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(UnmarkGroupAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(MarkAllAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(UnmarkAllAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(InvertSelectionAction.class, mainFrame), menuItemMnemonicHelper);

        markMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(CopyFilesToClipboardAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(CopyFileNamesAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(CopyFilePathsAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(PasteClipboardFilesAction.class, mainFrame), menuItemMnemonicHelper);

        markMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(CompareFoldersAction.class, mainFrame), menuItemMnemonicHelper);

        add(markMenu);

        // View menu
        menuItemMnemonicHelper.clear();
        JMenu viewMenu = MenuToolkit.addMenu(Translator.get("view_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(GoBackAction.class, mainFrame), menuItemMnemonicHelper);
        // Accelerators (keyboard shortcuts) for the following menu items will be set/unset when the menu is selected/deselected
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(GoForwardAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(GoToParentAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ChangeLocationAction.class, mainFrame), menuItemMnemonicHelper);
        viewMenu.add(new JSeparator());
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(SortByExtensionAction.class, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(SortByNameAction.class, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(SortBySizeAction.class, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(SortByDateAction.class, mainFrame), menuItemMnemonicHelper));

        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ReverseSortOrderAction.class, mainFrame), menuItemMnemonicHelper);
        viewMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(SwapFoldersAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(SetSameFolderAction.class, mainFrame), menuItemMnemonicHelper);

        viewMenu.add(new JSeparator());
        MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ToggleAutoSizeAction.class, mainFrame), menuItemMnemonicHelper).setSelected(mainFrame.getActiveTable().isAutoSizeColumnsEnabled());

        viewMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ToggleToolBarAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ToggleStatusBarAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ToggleCommandBarAction.class, mainFrame), menuItemMnemonicHelper);
		
        add(viewMenu);

        // Bookmark menu, menu items will be added when the menu gets selected
        menuItemMnemonicHelper.clear();
        bookmarksMenu = MenuToolkit.addMenu(Translator.get("bookmarks_menu"), menuItemMnemonicHelper, this);
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(AddBookmarkAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(EditBookmarksAction.class, mainFrame), menuItemMnemonicHelper);
        bookmarksMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(EditCredentialsAction.class, mainFrame), menuItemMnemonicHelper);
        bookmarksMenu.add(new JSeparator());
        // Save the first bookmark menu item's offset for later
        this.bookmarksOffset = bookmarksMenu.getItemCount();
		
        add(bookmarksMenu);
		
        // Window menu
        menuItemMnemonicHelper.clear();

        windowMenu = MenuToolkit.addMenu(Translator.get("window_menu"), menuItemMnemonicHelper, this);

        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(RecallPreviousWindowAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(RecallNextWindowAction.class, mainFrame), menuItemMnemonicHelper);
        // All other window menu items will be added when the menu gets selected
        windowMenu.add(new JSeparator());

        // Save the first window menu item's offset for later
        this.windowOffset = windowMenu.getItemCount();

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
	

//    /**
//     * This method is called when a menu is pulled down, and called again when menu is deselected or cancelled,
//     * for contextual menu items that need to refresh their state.
//     *
//     * @param menu menu that just got selected/deselected/cancelled
//     */
//    private void toggleContextualMenuItems(JMenu menu) {
//        if(menu == windowMenu && !menu.isSelected()) {
//            // Remove accelerators from all menu items but do not remove or disable menu items,
//            // this would cause the actionPerformed() method not be called when item is clicked.
//            int nbWindowMenuItems = windowMenu.getItemCount();
//            JMenuItem menuItem;
//            for(int i=0; i<nbWindowMenuItems; i++) {
//                menuItem = windowMenu.getItem(i);
//                if(menuItem!=null)
//                    menuItem.setAccelerator(null);
//            }
//        }
//    }


    ///////////////////////////
    // ActionListener method //
    ///////////////////////////

    public void actionPerformed(ActionEvent e) {
        // Discard action events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        Object source = e.getSource();

        // Bookmark menu item
        if (bookmarkMenuItems!=null && bookmarkMenuItems.contains(source)) {
            int index = bookmarkMenuItems.indexOf(source);
//            mainFrame.getActiveTable().getFolderPanel().trySetCurrentFolder(((Bookmark)bookmarks.elementAt(index)).getURL());
            mainFrame.getActiveTable().getFolderPanel().trySetCurrentFolder(((Bookmark)bookmarks.elementAt(index)).getLocation());
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

//        // Enable/disable contextual menu items depending on the current context
//        toggleContextualMenuItems((JMenu)source);

        if(source==bookmarksMenu) {
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
            // Start by removing any window menu item previously added
            // Note: menu item cannot be removed by menuDeselected() as actionPerformed() will be called after
            // menu has been deselected.
            for(int i=windowMenu.getItemCount(); i>windowOffset; i--)
                windowMenu.remove(windowOffset);

            // This WeakHashMap maps menu items to frame instances. It has to be a weakly referenced hash map
            // and not a regular hash map, since it will not (and cannot) be emptied when the menu has been deselected
            // and we really do not want this hash map to prevent the frames to be GCed 
            windowMenuFrames = new WeakHashMap();
            
            // Create a menu item for each of the MainFrame instances, that displays the MainFrame's path
            // and a keyboard accelerator to recall the frame (for the first 10 frames only).
            java.util.Vector mainFrames = WindowManager.getMainFrames();
            MainFrame mainFrame;
            JCheckBoxMenuItem checkBoxMenuItem;
            int nbFrames = mainFrames.size();
            for(int i=0; i<nbFrames; i++) {
                mainFrame = (MainFrame)mainFrames.elementAt(i);
                checkBoxMenuItem = new JCheckBoxMenuItem();

                // If frame number is less than 10, use the corresponding action (accelerator will be displayed in the menu item)
                if(i<10)
                    checkBoxMenuItem.setAction(ActionManager.getActionInstance(RECALL_WINDOW_ACTIONS[i], this.mainFrame));
                // Else catch the action event
                else {
                    checkBoxMenuItem.addActionListener(this);
                    windowMenuFrames.put(checkBoxMenuItem, mainFrame);
                }

                // Display the MainFrame's current folder path
                checkBoxMenuItem.setText((i+1)+" "+mainFrame.getActiveTable().getCurrentFolder().getAbsolutePath());

                // Check current MainFrame (the one this menu bar belongs to)
                checkBoxMenuItem.setSelected(mainFrame==this.mainFrame);

                windowMenu.add(checkBoxMenuItem);
            }

            // Add 'other' (non-MainFrame) windows : viewer and editor frames, no associated accelerator
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
//        // Enable/disable contextual menu items depending on the current context
//        toggleContextualMenuItems((JMenu)e.getSource());
    }
	 
    public void menuCanceled(MenuEvent e) {
//        // Enable/disable contextual menu items depending on the current context
//        toggleContextualMenuItems((JMenu)e.getSource());
    }
}
