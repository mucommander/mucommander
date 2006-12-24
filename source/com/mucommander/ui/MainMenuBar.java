
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.bonjour.BonjourMenu;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.*;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;
import com.mucommander.ui.editor.EditorFrame;
import com.mucommander.ui.viewer.ViewerFrame;
import com.mucommander.ui.table.SortCriteria;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.ui.theme.Theme;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.Iterator;


/**
 * This class is the main menu bar. It takes care of displaying menu and menu items and triggering
 * the proper actions.
 *
 * <p><b>Implementation note</b>: for performance reasons, some menu items are created/enabled/disabled when corresponding menus
 * are selected, instead of monitoring the MainFrame's state and unnecessarily creating/enabling/disabling menu items
 * when they are not visible. However, this prevents keyboard shortcuts from being managed by the menu bar for those
 * dynamic items.
 *
 * @author Maxence Bernard
 */
public class MainMenuBar extends JMenuBar implements ActionListener, MenuListener {

    private MainFrame mainFrame;	

    // View menu
    private JMenu viewMenu;
    private JMenu themesMenu;
    private JCheckBoxMenuItem sortByExtensionItem;
    private JCheckBoxMenuItem sortByNameItem;
    private JCheckBoxMenuItem sortBySizeItem;
    private JCheckBoxMenuItem sortByDateItem;
    private JCheckBoxMenuItem sortByPermissionsItem;

    // Bookmark menu
    private JMenu bookmarksMenu;
    private int bookmarksOffset;  // Index of the first bookmark menu item

    // Window menu
    private JMenu windowMenu;
    private int windowOffset; // Index of the first window menu item
    private JCheckBoxMenuItem splitHorizontallyItem;
    private JCheckBoxMenuItem splitVerticallyItem;

    /** Maps window menu items onto weakly-referenced frames */
    private WeakHashMap windowMenuFrames;


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
        viewMenu = MenuToolkit.addMenu(Translator.get("view_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(GoBackAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(GoForwardAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(GoToParentAction.class, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ChangeLocationAction.class, mainFrame), menuItemMnemonicHelper);

        viewMenu.add(new JSeparator());
        themesMenu = MenuToolkit.addMenu(Translator.get("prefs_dialog.themes"), null, this);
        // Theme menu items will be added when the themes menu is selected
        viewMenu.add(themesMenu);

        viewMenu.add(new JSeparator());
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(sortByExtensionItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(SortByExtensionAction.class, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(sortByNameItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(SortByNameAction.class, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(sortBySizeItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(SortBySizeAction.class, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(sortByDateItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(SortByDateAction.class, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(sortByPermissionsItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(SortByPermissionsAction.class, mainFrame), menuItemMnemonicHelper));

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

        // Add Bonjour services menu
        BonjourMenu bonjourMenu = new BonjourMenu(mainFrame);
        char mnemonic = menuItemMnemonicHelper.getMnemonic(bonjourMenu.getName());
        if(mnemonic!=0)
            bonjourMenu.setMnemonic(mnemonic);
        bookmarksMenu.add(bonjourMenu);
        bookmarksMenu.add(new JSeparator());

        // Save the first bookmark menu item's offset for later (bookmarks will be added when menu becomes visible)
        this.bookmarksOffset = bookmarksMenu.getItemCount();
		
        add(bookmarksMenu);
		
        // Window menu
        menuItemMnemonicHelper.clear();

        windowMenu = MenuToolkit.addMenu(Translator.get("window_menu"), menuItemMnemonicHelper, this);

        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(SplitEquallyAction.class, mainFrame), menuItemMnemonicHelper);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(splitVerticallyItem = MenuToolkit.addCheckBoxMenuItem(windowMenu, ActionManager.getActionInstance(SplitVerticallyAction.class, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(splitHorizontallyItem = MenuToolkit.addCheckBoxMenuItem(windowMenu, ActionManager.getActionInstance(SplitHorizontallyAction.class, mainFrame), menuItemMnemonicHelper));

        windowMenu.add(new JSeparator());

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
        MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ShowKeyboardShortcutsAction.class, mainFrame), menuItemMnemonicHelper);

        // Links to website, only shows for OS/Window manager that can launch the default browser to open URLs
        if (PlatformManager.canOpenURLInBrowser()) {
            helpMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(GoToWebsiteAction.class, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(GoToForumsAction.class, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ReportBugAction.class, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(DonateAction.class, mainFrame), menuItemMnemonicHelper);
        }
		
        // Under Mac OS X, 'About' already appears in the application (muCommander) menu, do not display it again
        if(PlatformManager.OS_FAMILY!=PlatformManager.MAC_OS_X) {
            helpMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ShowAboutAction.class, mainFrame), menuItemMnemonicHelper);
        }
		
        add(helpMenu);
    }
	

    ///////////////////////////
    // ActionListener method //
    ///////////////////////////

    public void actionPerformed(ActionEvent e) {
        // Discard action events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        Object source = e.getSource();

        if(source==windowMenu) {
            // Bring the frame corresponding to the clicked menu item to the front
            ((JFrame)windowMenuFrames.get(source)).toFront();
        }
    }


    //////////////////////////
    // MenuListener methods //
    //////////////////////////

    public void menuSelected(MenuEvent e) {
        Object source = e.getSource();

        if(source==viewMenu) {
            // Select the 'sort by' criterion currently in use in the active table
            switch(mainFrame.getActiveTable().getSortByCriteria()) {
                case SortCriteria.EXTENSION:
                    sortByExtensionItem.setSelected(true);
                    break;
                case SortCriteria.NAME:
                    sortByNameItem.setSelected(true);
                    break;
                case SortCriteria.SIZE:
                    sortBySizeItem.setSelected(true);
                    break;
                case SortCriteria.DATE:
                    sortByDateItem.setSelected(true);
                    break;
            }
        }
        else if(source==themesMenu) {
            // Remove all previous theme items, create new ones for each available theme and select the current theme
            themesMenu.removeAll();
            ButtonGroup buttonGroup = new ButtonGroup();
            Iterator themes = ThemeManager.availableThemes();
            Theme theme;
            JCheckBoxMenuItem item;
            while(themes.hasNext()) {
                theme = (Theme)themes.next();
                item = new JCheckBoxMenuItem(new ChangeCurrentThemeAction(theme));
                buttonGroup.add(item);
                if(ThemeManager.isCurrentTheme(theme))
                    item.setSelected(true);

                themesMenu.add(item);
            }
        }
        else if(source==bookmarksMenu) {
            // Remove any previous bookmarks menu items from menu
            // as bookmarks might have changed since menu was last selected
            for(int i=bookmarksMenu.getItemCount(); i>bookmarksOffset; i--)
                bookmarksMenu.remove(bookmarksOffset);

            // Add bookmarks menu items
            Vector bookmarks = BookmarkManager.getBookmarks();
            int nbBookmarks = bookmarks.size();
            if(nbBookmarks>0) {
                Bookmark b;
                for(int i=0; i<nbBookmarks; i++)
                    MenuToolkit.addMenuItem(bookmarksMenu, new OpenLocationAction(mainFrame, (Bookmark)bookmarks.elementAt(i)), null);
            }
            else {
                // Show 'No bookmark' as a disabled menu item instead showing nothing
                JMenuItem noBookmarkItem = MenuToolkit.addMenuItem(bookmarksMenu, Translator.get("bookmarks_menu.no_bookmark"), null, null, null);
                noBookmarkItem.setEnabled(false);
            }
        }
        else if(source==windowMenu) {
            // Select the split orientation currently in use
            if(mainFrame.getSplitPaneOrientation())
                splitVerticallyItem.setSelected(true);
            else
                splitHorizontallyItem.setSelected(true);

            // Removing any window menu item previously added
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
    }
	 
    public void menuCanceled(MenuEvent e) {
    }


    /**
     * Action that changes the current theme to the specified in the constructor.
     */
    private class ChangeCurrentThemeAction extends AbstractAction {

        private Theme theme;

        public ChangeCurrentThemeAction(Theme theme) {
            super(theme.getName());
            this.theme = theme;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            ThemeManager.setCurrentTheme(theme);
        }
    }
}
