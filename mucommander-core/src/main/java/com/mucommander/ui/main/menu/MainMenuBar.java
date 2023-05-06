/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main.menu;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.mucommander.bonjour.BonjourMenu;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionParameters;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.OpenLocationAction;
import com.mucommander.ui.action.impl.RecallWindowAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.pref.theme.ThemeEditorDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.table.Column;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.ui.viewer.FileFrame;


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
    private JCheckBoxMenuItem[] sortByItems = new JCheckBoxMenuItem[Column.values().length];
    private JMenu columnsMenu;
    private JCheckBoxMenuItem[] toggleColumnItems = new JCheckBoxMenuItem[Column.values().length];
    private JCheckBoxMenuItem toggleToggleAutoSizeItem;
    private JCheckBoxMenuItem toggleShowFoldersFirstItem;
    private JCheckBoxMenuItem toggleShowHiddenFilesItem;
    private JCheckBoxMenuItem toggleTreeItem;
    private JCheckBoxMenuItem toggleUseSinglePanel;

    /* TODO branch private JCheckBoxMenuItem toggleBranchView; */


    // Go menu
    private JMenu goMenu;
    private int volumeOffset;

    // Bookmark menu
    private JMenu bookmarksMenu;
    private int bookmarksOffset;  // Index of the first bookmark menu item

    // Window menu
    private JMenu windowMenu;
    private int windowOffset; // Index of the first window menu item
    private JCheckBoxMenuItem splitHorizontallyItem;
    private JCheckBoxMenuItem splitVerticallyItem;

    /** Maps window menu items onto weakly-referenced frames */
    private WeakHashMap<JMenuItem, Frame> windowMenuFrames;


    private final static String RECALL_WINDOW_ACTION_IDS[] = {
            ActionType.RecallWindow1.toString(),
            ActionType.RecallWindow2.toString(),
            ActionType.RecallWindow3.toString(),
            ActionType.RecallWindow4.toString(),
            ActionType.RecallWindow5.toString(),
            ActionType.RecallWindow6.toString(),
            ActionType.RecallWindow7.toString(),
            ActionType.RecallWindow8.toString(),
            ActionType.RecallWindow9.toString(),
            ActionType.RecallWindow10.toString()
    };


    /**
     * Creates a new MenuBar for the given MainFrame.
     */
    public MainMenuBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // Disable menu bar (NOT menu item) mnemonics under Mac OS X because of a bug: when screen menu bar is enabled
        // and a menu is triggered by a mnemonic, the menu pops up where it would appear with a regular menu bar
        // (i.e. with screen menu bar disabled).
        MnemonicHelper menuMnemonicHelper = OsFamily.MAC_OS.isCurrent()?null:new MnemonicHelper();

        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper2 = new MnemonicHelper();

        // File menu
        JMenu fileMenu = MenuToolkit.addMenu(Translator.get("file_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.NewWindow, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.NewTab, mainFrame), menuItemMnemonicHelper);
        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.Open, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.OpenNatively, mainFrame), menuItemMnemonicHelper);
        fileMenu.add(new OpenWithMenu(mainFrame));
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.OpenInNewTab, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.OpenInOtherPanel, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.OpenInBothPanels, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.RevealInDesktop, mainFrame), menuItemMnemonicHelper);

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.Find, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.RunCommand, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.Pack, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.Unpack, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.Email, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.BatchRename, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.SplitFile, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.CombineFiles, mainFrame), menuItemMnemonicHelper);

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.ShowFileProperties, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.CalculateChecksum, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.ChangePermissions, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.ChangeDate, mainFrame), menuItemMnemonicHelper);

        // Under Mac OS X, 'Preferences' already appears in the application (muCommander) menu, do not display it again
        if(!OsFamily.MAC_OS.isCurrent()) {
            fileMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.ShowPreferences, mainFrame), menuItemMnemonicHelper);
        }

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.CloseWindow, mainFrame), menuItemMnemonicHelper);
        // Under Mac OS X, 'Quit' already appears in the application (muCommander) menu, do not display it again
        if(!OsFamily.MAC_OS.isCurrent())
            MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ActionType.Quit, mainFrame), menuItemMnemonicHelper);

        add(fileMenu);

        // Mark menu
        menuItemMnemonicHelper.clear();
        JMenu markMenu = MenuToolkit.addMenu(Translator.get("mark_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.MarkSelectedFile, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.MarkGroup, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.UnmarkGroup, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.MarkAll, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.UnmarkAll, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.MarkExtension, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.InvertSelection, mainFrame), menuItemMnemonicHelper);

        markMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.CopyFilesToClipboard, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.CopyFileNames, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.CopyFileBaseNames, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.CopyFilePaths, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.PasteClipboardFiles, mainFrame), menuItemMnemonicHelper);

        markMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(ActionType.CompareFolders, mainFrame), menuItemMnemonicHelper);

        add(markMenu);

        // View menu
        menuItemMnemonicHelper.clear();
        viewMenu = MenuToolkit.addMenu(Translator.get("view_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.SwapFolders, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.SetSameFolder, mainFrame), menuItemMnemonicHelper);

        viewMenu.add(new JSeparator());
        toggleShowFoldersFirstItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.ToggleShowFoldersFirst, mainFrame), menuItemMnemonicHelper);
        toggleShowHiddenFilesItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.ToggleHiddenFiles, mainFrame), menuItemMnemonicHelper);
        toggleTreeItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.ToggleTree, mainFrame), menuItemMnemonicHelper);
        toggleUseSinglePanel = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.ToggleUseSinglePanel, mainFrame), menuItemMnemonicHelper);
        /* TODO branch toggleBranchView = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ToggleBranchViewAction.class, mainFrame), menuItemMnemonicHelper); */

        viewMenu.add(new JSeparator());
        ButtonGroup buttonGroup = new ButtonGroup();
        for(Column c : Column.values())
            buttonGroup.add(sortByItems[c.ordinal()] = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(c.getSortByColumnActionId(), mainFrame), menuItemMnemonicHelper));

        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.ReverseSortOrder, mainFrame), menuItemMnemonicHelper);

        viewMenu.add(new JSeparator());

        // Toggle columns submenu
        columnsMenu = MenuToolkit.addMenu(Translator.get("view_menu.show_hide_columns"), null, this);
        menuItemMnemonicHelper2.clear();
        for(Column c : Column.values()) {
            if(c==Column.NAME)
                continue;

            toggleColumnItems[c.ordinal()] = MenuToolkit.addCheckBoxMenuItem(columnsMenu, ActionManager.getActionInstance(c.getToggleColumnActionId(), mainFrame), menuItemMnemonicHelper2);
        }
        viewMenu.add(columnsMenu);

        toggleToggleAutoSizeItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.ToggleAutoSize, mainFrame), menuItemMnemonicHelper);

        viewMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.ToggleToolBar, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.ToggleStatusBar, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.ToggleCommandBar, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ActionType.CustomizeCommandBar, mainFrame), menuItemMnemonicHelper);

        add(viewMenu);

        // Go menu
        menuItemMnemonicHelper.clear();
        goMenu = MenuToolkit.addMenu(Translator.get("go_menu"), menuMnemonicHelper, this);

        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ActionType.GoBack, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ActionType.GoForward, mainFrame), menuItemMnemonicHelper);

        goMenu.add(new JSeparator());

        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ActionType.GoToParent, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ActionType.GoToParentInOtherPanel, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ActionType.GoToParentInBothPanels, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ActionType.GoToRoot, mainFrame), menuItemMnemonicHelper);

        goMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ActionType.ChangeLocation, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ActionType.ConnectToServer, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ActionType.ShowServerConnections, mainFrame), menuItemMnemonicHelper);

        // Quick lists
        goMenu.add(new JSeparator());
        JMenu quickListMenu = MenuToolkit.addMenu(Translator.get("quick_lists_menu"), menuMnemonicHelper, this);
        menuItemMnemonicHelper2.clear();
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ActionType.ShowParentFoldersQL, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ActionType.ShowRecentLocationsQL, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ActionType.ShowRecentExecutedFilesQL, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ActionType.ShowBookmarksQL, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ActionType.ShowRootFoldersQL, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ActionType.ShowTabsQL, mainFrame), menuItemMnemonicHelper2);
        goMenu.add(quickListMenu);

        // Add Bonjour services menu
        goMenu.add(new JSeparator());
        BonjourMenu bonjourMenu = new BonjourMenu(MainMenuBar.this.mainFrame);
        char mnemonic = menuItemMnemonicHelper.getMnemonic(bonjourMenu.getName());
        if(mnemonic!=0)
            bonjourMenu.setMnemonic(mnemonic);
        bonjourMenu.setIcon(null);
        goMenu.add(bonjourMenu);

        // Volumes will be added when the menu is selected
        goMenu.add(new JSeparator());
        volumeOffset = goMenu.getItemCount();

        add(goMenu);

        // Bookmark menu, menu items will be added when the menu gets selected
        menuItemMnemonicHelper.clear();
        bookmarksMenu = MenuToolkit.addMenu(Translator.get("bookmarks_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(ActionType.AddBookmark, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(ActionType.EditBookmarks, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(ActionType.ExploreBookmarks, mainFrame), menuItemMnemonicHelper);
        bookmarksMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(ActionType.EditCredentials, mainFrame), menuItemMnemonicHelper);
        bookmarksMenu.add(new JSeparator());

        // Save the first bookmark menu item's offset for later (bookmarks will be added when menu becomes visible)
        this.bookmarksOffset = bookmarksMenu.getItemCount();

        add(bookmarksMenu);
        
        // Window menu
        menuItemMnemonicHelper.clear();

        windowMenu = MenuToolkit.addMenu(Translator.get("window_menu"), menuMnemonicHelper, this);

        // If running Mac OS X, add 'Minimize' and 'Zoom' items
        if(OsFamily.MAC_OS.isCurrent()) {
            MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(ActionType.MinimizeWindow, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(ActionType.MaximizeWindow, mainFrame), menuItemMnemonicHelper);
            windowMenu.add(new JSeparator());
        }

        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(ActionType.SplitEqually, mainFrame), menuItemMnemonicHelper);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(splitVerticallyItem = MenuToolkit.addCheckBoxMenuItem(windowMenu, ActionManager.getActionInstance(ActionType.SplitVertically, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(splitHorizontallyItem = MenuToolkit.addCheckBoxMenuItem(windowMenu, ActionManager.getActionInstance(ActionType.SplitHorizontally, mainFrame), menuItemMnemonicHelper));

        windowMenu.add(new JSeparator());
        themesMenu = MenuToolkit.addMenu(Translator.get("prefs_dialog.themes"), null, this);
        // Theme menu items will be added when the themes menu is selected
        windowMenu.add(themesMenu);

        windowMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(ActionType.RecallPreviousWindow, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(ActionType.RecallNextWindow, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(ActionType.BringAllToFront, mainFrame), menuItemMnemonicHelper);
        // All other window menu items will be added when the menu gets selected
        windowMenu.add(new JSeparator());

        // Save the first window menu item's offset for later
        this.windowOffset = windowMenu.getItemCount();

        add(windowMenu);

        // Help menu
        menuItemMnemonicHelper.clear();
        JMenu helpMenu = MenuToolkit.addMenu(Translator.get("help_menu"), menuMnemonicHelper, null);

        MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ActionType.GoToDocumentation, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ActionType.ShowKeyboardShortcuts, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ActionType.ShowDebugConsole, mainFrame), menuItemMnemonicHelper);

        // Links to website, only shows for OS/Window manager that can launch the default browser to open URLs
        if(DesktopManager.canBrowse()) {
            helpMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ActionType.GoToWebsite, mainFrame), menuItemMnemonicHelper);
             MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ActionType.GoToForums, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ActionType.ReportBug, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ActionType.Donate, mainFrame), menuItemMnemonicHelper);

            helpMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ActionType.CheckForUpdates, mainFrame), menuItemMnemonicHelper);
        }
		
        // Under Mac OS X, 'About' already appears in the application (muCommander) menu, do not display it again
        if(!OsFamily.MAC_OS.isCurrent()) {
            helpMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ActionType.ShowAbout, mainFrame), menuItemMnemonicHelper);
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

        // Bring the frame corresponding to the clicked menu item to the front
        windowMenuFrames.get(e.getSource()).toFront();
    }


    //////////////////////////
    // MenuListener methods //
    //////////////////////////

    public void menuSelected(MenuEvent e) {
        Object source = e.getSource();

        if(source==viewMenu) {
            FileTable activeTable = mainFrame.getActiveTable();

            // Select the 'sort by' criterion currently in use in the active table
            sortByItems[activeTable.getSortInfo().getCriterion().ordinal()].setSelected(true);

            toggleShowFoldersFirstItem.setSelected(activeTable.getSortInfo().getFoldersFirst());
            toggleShowHiddenFilesItem.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_HIDDEN_FILES, MuPreferences.DEFAULT_SHOW_HIDDEN_FILES));
            toggleTreeItem.setSelected(activeTable.getFolderPanel().isTreeVisible());
            toggleToggleAutoSizeItem.setSelected(mainFrame.isAutoSizeColumnsEnabled());
            toggleUseSinglePanel.setSelected(mainFrame.isSinglePanel());

            /* TODO branch toggleBranchView.setSelected(activeTable.getFolderPanel().isBranchView()); */
        }
        else if(source==columnsMenu) {
            // Update the selected and enabled state of each column menu item.
            FileTable activeTable = mainFrame.getActiveTable();
            for(Column c : Column.values()) {
                if(c==Column.NAME)     // Name column doesn't have a menu item as it cannot be disabled
                    continue;

                JCheckBoxMenuItem item = toggleColumnItems[c.ordinal()];
                item.setSelected(activeTable.isColumnEnabled(c));
                item.setEnabled(activeTable.isColumnDisplayable(c));
                // Override the action's label to a shorter one
                item.setText(c.getLabel());
            }
        }
        else if(source==goMenu) {
            // Remove any previous volumes from the Go menu
            // as they might have changed since menu was last selected
            for(int i=goMenu.getItemCount(); i> volumeOffset; i--)
                goMenu.remove(volumeOffset);

            AbstractFile volumes[] = LocalFile.getVolumes();
            int nbFolders = volumes.length;

            for(int i=0; i<nbFolders; i++)
                goMenu.add(new OpenLocationAction(mainFrame, new Hashtable<>(), volumes[i]));
        }
        else if(source==bookmarksMenu) {
            // Remove any previous bookmarks menu items from menu
            // as bookmarks might have changed since menu was last selected
            for(int i=bookmarksMenu.getItemCount(); i>bookmarksOffset; i--)
                bookmarksMenu.remove(bookmarksOffset);

            // Add bookmarks menu items
            var bookmarks = BookmarkManager.getBookmarks();
            if (bookmarks.isEmpty()) {
                // Show 'No bookmark' as a disabled menu item instead showing nothing
                JMenuItem noBookmarkItem = MenuToolkit.addMenuItem(bookmarksMenu, Translator.get("bookmarks_menu.no_bookmark"), null, null, null);
                noBookmarkItem.setEnabled(false);
            }
            else {
                bookmarks.forEach(bookmark -> MenuToolkit.addMenuItem(bookmarksMenu, new OpenLocationAction(mainFrame, new Hashtable<>(), bookmark), null));
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
            windowMenuFrames = new WeakHashMap<>();
            
            // Create a menu item for each of the MainFrame instances, that displays the MainFrame's path
            // and a keyboard accelerator to recall the frame (for the first 10 frames only).
            java.util.List<MainFrame> mainFrames = WindowManager.getMainFrames();
            MainFrame mainFrame;
            JCheckBoxMenuItem checkBoxMenuItem;
            int nbFrames = mainFrames.size();
            for(int i=0; i<nbFrames; i++) {
                mainFrame = mainFrames.get(i);
                checkBoxMenuItem = new JCheckBoxMenuItem();

                // If frame number is less than 10, use the corresponding action class (accelerator will be displayed in the menu item)
                MuAction recallWindowAction;
                if(i<10) {
                    recallWindowAction = ActionManager.getActionInstance(RECALL_WINDOW_ACTION_IDS[i], this.mainFrame);
                }
                // Else use the generic RecallWindowAction
                else {
                    Hashtable<String, Object> actionProps = new Hashtable<>();
                    // Specify the window number using the dedicated property
                    actionProps.put(RecallWindowAction.WINDOW_NUMBER_PROPERTY_KEY, ""+(i+1));
                    recallWindowAction = ActionManager.getActionInstance(new ActionParameters(RecallWindowAction.Descriptor.ACTION_ID, actionProps), this.mainFrame);
                }

                checkBoxMenuItem.setAction(recallWindowAction);

                // Replace the action's label and use the MainFrame's current folder path instead
                checkBoxMenuItem.setText((i+1)+" "+mainFrame.getActiveTable().getFolderPanel().getCurrentFolder().getAbsolutePath());

                // Use the action's label as a tooltip 
                checkBoxMenuItem.setToolTipText(recallWindowAction.getLabel());

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
                if(frame.isShowing() && (frame instanceof FileFrame)) {
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
        else if(source==themesMenu) {
            // Remove all previous theme items, create new ones for each available theme and select the current theme
            themesMenu.removeAll();
            ButtonGroup buttonGroup = new ButtonGroup();
            Iterator<Theme> themes = ThemeManager.availableThemes();
            Theme theme;
            JCheckBoxMenuItem item;
            themesMenu.add(new JMenuItem(new EditCurrentThemeAction()));
            themesMenu.add(new JSeparator());
            while(themes.hasNext()) {
                theme = themes.next();
                item = new JCheckBoxMenuItem(new ChangeCurrentThemeAction(theme));
                buttonGroup.add(item);
                if(ThemeManager.isCurrentTheme(theme))
                    item.setSelected(true);

                themesMenu.add(item);
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
            try {
                ThemeManager.setCurrentTheme(theme);
            }
            catch(IllegalArgumentException e) {
                InformationDialog.showErrorDialog(mainFrame, Translator.get("theme_could_not_be_loaded"));
            }
        }
    }

    /**
     * Actions that edits the current theme.
     */
    private class EditCurrentThemeAction extends AbstractAction {
        public EditCurrentThemeAction() {
            super(Translator.get("prefs_dialog.edit_current_theme"));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            new ThemeEditorDialog(mainFrame, ThemeManager.getCurrentTheme()).editTheme();
        }
    }
}
