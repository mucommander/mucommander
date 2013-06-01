/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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
import com.mucommander.bonjour.BonjourService;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionParameters;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.AddBookmarkAction;
import com.mucommander.ui.action.impl.BatchRenameAction;
import com.mucommander.ui.action.impl.BringAllToFrontAction;
import com.mucommander.ui.action.impl.CalculateChecksumAction;
import com.mucommander.ui.action.impl.ChangeDateAction;
import com.mucommander.ui.action.impl.ChangeLocationAction;
import com.mucommander.ui.action.impl.ChangePermissionsAction;
import com.mucommander.ui.action.impl.CheckForUpdatesAction;
import com.mucommander.ui.action.impl.CloseWindowAction;
import com.mucommander.ui.action.impl.CombineFilesAction;
import com.mucommander.ui.action.impl.CompareFoldersAction;
import com.mucommander.ui.action.impl.ConnectToServerAction;
import com.mucommander.ui.action.impl.CopyFileBaseNamesAction;
import com.mucommander.ui.action.impl.CopyFileNamesAction;
import com.mucommander.ui.action.impl.CopyFilePathsAction;
import com.mucommander.ui.action.impl.CopyFilesToClipboardAction;
import com.mucommander.ui.action.impl.CustomizeCommandBarAction;
import com.mucommander.ui.action.impl.DonateAction;
import com.mucommander.ui.action.impl.EditBookmarksAction;
import com.mucommander.ui.action.impl.EditCredentialsAction;
import com.mucommander.ui.action.impl.EmailAction;
import com.mucommander.ui.action.impl.ExploreBookmarksAction;
import com.mucommander.ui.action.impl.GoBackAction;
import com.mucommander.ui.action.impl.GoForwardAction;
import com.mucommander.ui.action.impl.GoToDocumentationAction;
import com.mucommander.ui.action.impl.GoToForumsAction;
import com.mucommander.ui.action.impl.GoToParentAction;
import com.mucommander.ui.action.impl.GoToParentInBothPanelsAction;
import com.mucommander.ui.action.impl.GoToParentInOtherPanelAction;
import com.mucommander.ui.action.impl.GoToRootAction;
import com.mucommander.ui.action.impl.GoToWebsiteAction;
import com.mucommander.ui.action.impl.InvertSelectionAction;
import com.mucommander.ui.action.impl.MarkAllAction;
import com.mucommander.ui.action.impl.MarkExtensionAction;
import com.mucommander.ui.action.impl.MarkGroupAction;
import com.mucommander.ui.action.impl.MarkSelectedFileAction;
import com.mucommander.ui.action.impl.MaximizeWindowAction;
import com.mucommander.ui.action.impl.MinimizeWindowAction;
import com.mucommander.ui.action.impl.NewWindowAction;
import com.mucommander.ui.action.impl.OpenAction;
import com.mucommander.ui.action.impl.OpenInBothPanelsAction;
import com.mucommander.ui.action.impl.OpenInNewTabAction;
import com.mucommander.ui.action.impl.OpenInOtherPanelAction;
import com.mucommander.ui.action.impl.OpenLocationAction;
import com.mucommander.ui.action.impl.OpenNativelyAction;
import com.mucommander.ui.action.impl.PackAction;
import com.mucommander.ui.action.impl.PasteClipboardFilesAction;
import com.mucommander.ui.action.impl.QuitAction;
import com.mucommander.ui.action.impl.RecallNextWindowAction;
import com.mucommander.ui.action.impl.RecallPreviousWindowAction;
import com.mucommander.ui.action.impl.RecallWindowAction;
import com.mucommander.ui.action.impl.ReportBugAction;
import com.mucommander.ui.action.impl.RevealInDesktopAction;
import com.mucommander.ui.action.impl.ReverseSortOrderAction;
import com.mucommander.ui.action.impl.RunCommandAction;
import com.mucommander.ui.action.impl.SetSameFolderAction;
import com.mucommander.ui.action.impl.ShowAboutAction;
import com.mucommander.ui.action.impl.ShowBookmarksQLAction;
import com.mucommander.ui.action.impl.ShowDebugConsoleAction;
import com.mucommander.ui.action.impl.ShowFilePropertiesAction;
import com.mucommander.ui.action.impl.ShowKeyboardShortcutsAction;
import com.mucommander.ui.action.impl.ShowParentFoldersQLAction;
import com.mucommander.ui.action.impl.ShowPreferencesAction;
import com.mucommander.ui.action.impl.ShowRecentExecutedFilesQLAction;
import com.mucommander.ui.action.impl.ShowRecentLocationsQLAction;
import com.mucommander.ui.action.impl.ShowRootFoldersQLAction;
import com.mucommander.ui.action.impl.ShowServerConnectionsAction;
import com.mucommander.ui.action.impl.ShowTabsQLAction;
import com.mucommander.ui.action.impl.SplitEquallyAction;
import com.mucommander.ui.action.impl.SplitFileAction;
import com.mucommander.ui.action.impl.SplitHorizontallyAction;
import com.mucommander.ui.action.impl.SplitVerticallyAction;
import com.mucommander.ui.action.impl.SwapFoldersAction;
import com.mucommander.ui.action.impl.ToggleAutoSizeAction;
import com.mucommander.ui.action.impl.ToggleCommandBarAction;
import com.mucommander.ui.action.impl.ToggleHiddenFilesAction;
import com.mucommander.ui.action.impl.ToggleShowFoldersFirstAction;
import com.mucommander.ui.action.impl.ToggleStatusBarAction;
import com.mucommander.ui.action.impl.ToggleToolBarAction;
import com.mucommander.ui.action.impl.ToggleTreeAction;
import com.mucommander.ui.action.impl.UnmarkAllAction;
import com.mucommander.ui.action.impl.UnmarkGroupAction;
import com.mucommander.ui.action.impl.UnpackAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.pref.theme.ThemeEditorDialog;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
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
        com.mucommander.ui.action.impl.RecallWindow1Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow2Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow3Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow4Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow5Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow6Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow7Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow8Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow9Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow10Action.Descriptor.ACTION_ID
    };


    /**
     * Creates a new MenuBar for the given MainFrame.
     */
    public MainMenuBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // Disable menu bar (NOT menu item) mnemonics under Mac OS X because of a bug: when screen menu bar is enabled
        // and a menu is triggered by a mnemonic, the menu pops up where it would appear with a regular menu bar
        // (i.e. with screen menu bar disabled).
        MnemonicHelper menuMnemonicHelper = OsFamily.MAC_OS_X.isCurrent()?null:new MnemonicHelper();

        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper2 = new MnemonicHelper();

        // File menu
        JMenu fileMenu = MenuToolkit.addMenu(Translator.get("file_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(NewWindowAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(OpenAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(OpenNativelyAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        fileMenu.add(new OpenWithMenu(mainFrame));
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(OpenInNewTabAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(OpenInOtherPanelAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(OpenInBothPanelsAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(RevealInDesktopAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(RunCommandAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(PackAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(UnpackAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(EmailAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(BatchRenameAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(SplitFileAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(CombineFilesAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ShowFilePropertiesAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(CalculateChecksumAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ChangePermissionsAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ChangeDateAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        // Under Mac OS X, 'Preferences' already appears in the application (muCommander) menu, do not display it again
        if(!OsFamily.MAC_OS_X.isCurrent()) {
            fileMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(ShowPreferencesAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        }

        fileMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(CloseWindowAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        // Under Mac OS X, 'Quit' already appears in the application (muCommander) menu, do not display it again
        if(!OsFamily.MAC_OS_X.isCurrent())
            MenuToolkit.addMenuItem(fileMenu, ActionManager.getActionInstance(QuitAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        add(fileMenu);

        // Mark menu
        menuItemMnemonicHelper.clear();
        JMenu markMenu = MenuToolkit.addMenu(Translator.get("mark_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(MarkSelectedFileAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(MarkGroupAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(UnmarkGroupAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(MarkAllAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(UnmarkAllAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(MarkExtensionAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(InvertSelectionAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        markMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(CopyFilesToClipboardAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(CopyFileNamesAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(CopyFileBaseNamesAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(CopyFilePathsAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(PasteClipboardFilesAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        markMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(markMenu, ActionManager.getActionInstance(CompareFoldersAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        add(markMenu);

        // View menu
        menuItemMnemonicHelper.clear();
        viewMenu = MenuToolkit.addMenu(Translator.get("view_menu"), menuMnemonicHelper, this);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(SwapFoldersAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(SetSameFolderAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        viewMenu.add(new JSeparator());
        toggleShowFoldersFirstItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ToggleShowFoldersFirstAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        toggleShowHiddenFilesItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ToggleHiddenFilesAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        toggleTreeItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ToggleTreeAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        /* TODO branch toggleBranchView = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ToggleBranchViewAction.class, mainFrame), menuItemMnemonicHelper); */

        viewMenu.add(new JSeparator());
        ButtonGroup buttonGroup = new ButtonGroup();
        for(Column c : Column.values())
            buttonGroup.add(sortByItems[c.ordinal()] = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(c.getSortByColumnActionId(), mainFrame), menuItemMnemonicHelper));

        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ReverseSortOrderAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

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

        toggleToggleAutoSizeItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, ActionManager.getActionInstance(ToggleAutoSizeAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        viewMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ToggleToolBarAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ToggleStatusBarAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(ToggleCommandBarAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(viewMenu, ActionManager.getActionInstance(CustomizeCommandBarAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        add(viewMenu);

        // Go menu
        menuItemMnemonicHelper.clear();
        goMenu = MenuToolkit.addMenu(Translator.get("go_menu"), menuMnemonicHelper, this);

        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(GoBackAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(GoForwardAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        goMenu.add(new JSeparator());

        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(GoToParentAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(GoToParentInOtherPanelAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(GoToParentInBothPanelsAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(GoToRootAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        goMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ChangeLocationAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ConnectToServerAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(goMenu, ActionManager.getActionInstance(ShowServerConnectionsAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        // Quick lists
        goMenu.add(new JSeparator());
        JMenu quickListMenu = MenuToolkit.addMenu(Translator.get("quick_lists_menu"), menuMnemonicHelper, this);
        menuItemMnemonicHelper2.clear();
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ShowParentFoldersQLAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ShowRecentLocationsQLAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ShowRecentExecutedFilesQLAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ShowBookmarksQLAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ShowRootFoldersQLAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper2);
        MenuToolkit.addMenuItem(quickListMenu, ActionManager.getActionInstance(ShowTabsQLAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper2);
        goMenu.add(quickListMenu);

        // Add Bonjour services menu
        goMenu.add(new JSeparator());
        BonjourMenu bonjourMenu = new BonjourMenu() {
            @Override
            public MuAction getMenuItemAction(BonjourService bs) {
                return new OpenLocationAction(MainMenuBar.this.mainFrame, new Hashtable<String, Object>(), bs);
            }
        };
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
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(AddBookmarkAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(EditBookmarksAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(ExploreBookmarksAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        bookmarksMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(bookmarksMenu, ActionManager.getActionInstance(EditCredentialsAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        bookmarksMenu.add(new JSeparator());

        // Save the first bookmark menu item's offset for later (bookmarks will be added when menu becomes visible)
        this.bookmarksOffset = bookmarksMenu.getItemCount();

        add(bookmarksMenu);
        
        // Window menu
        menuItemMnemonicHelper.clear();

        windowMenu = MenuToolkit.addMenu(Translator.get("window_menu"), menuMnemonicHelper, this);

        // If running Mac OS X, add 'Minimize' and 'Zoom' items
        if(OsFamily.MAC_OS_X.isCurrent()) {
            MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(MinimizeWindowAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(MaximizeWindowAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
            windowMenu.add(new JSeparator());
        }

        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(SplitEquallyAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(splitVerticallyItem = MenuToolkit.addCheckBoxMenuItem(windowMenu, ActionManager.getActionInstance(SplitVerticallyAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper));
        buttonGroup.add(splitHorizontallyItem = MenuToolkit.addCheckBoxMenuItem(windowMenu, ActionManager.getActionInstance(SplitHorizontallyAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper));

        windowMenu.add(new JSeparator());
        themesMenu = MenuToolkit.addMenu(Translator.get("prefs_dialog.themes"), null, this);
        // Theme menu items will be added when the themes menu is selected
        windowMenu.add(themesMenu);

        windowMenu.add(new JSeparator());
        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(RecallPreviousWindowAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(RecallNextWindowAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(windowMenu, ActionManager.getActionInstance(BringAllToFrontAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        // All other window menu items will be added when the menu gets selected
        windowMenu.add(new JSeparator());

        // Save the first window menu item's offset for later
        this.windowOffset = windowMenu.getItemCount();

        add(windowMenu);

        // Help menu
        menuItemMnemonicHelper.clear();
        JMenu helpMenu = MenuToolkit.addMenu(Translator.get("help_menu"), menuMnemonicHelper, null);

        MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(GoToDocumentationAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ShowKeyboardShortcutsAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ShowDebugConsoleAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

        // Links to website, only shows for OS/Window manager that can launch the default browser to open URLs
        if(DesktopManager.canBrowse()) {
            helpMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(GoToWebsiteAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(GoToForumsAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ReportBugAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(DonateAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);

            helpMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(CheckForUpdatesAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
        }
		
        // Under Mac OS X, 'About' already appears in the application (muCommander) menu, do not display it again
        if(!OsFamily.MAC_OS_X.isCurrent()) {
            helpMenu.add(new JSeparator());
            MenuToolkit.addMenuItem(helpMenu, ActionManager.getActionInstance(ShowAboutAction.Descriptor.ACTION_ID, mainFrame), menuItemMnemonicHelper);
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
                goMenu.add(new OpenLocationAction(mainFrame, new Hashtable<String, Object>(), volumes[i]));
        }
        else if(source==bookmarksMenu) {
            // Remove any previous bookmarks menu items from menu
            // as bookmarks might have changed since menu was last selected
            for(int i=bookmarksMenu.getItemCount(); i>bookmarksOffset; i--)
                bookmarksMenu.remove(bookmarksOffset);

            // Add bookmarks menu items
            java.util.List<Bookmark> bookmarks = BookmarkManager.getBookmarks();
            int nbBookmarks = bookmarks.size();
            if(nbBookmarks>0) {
                for(int i=0; i<nbBookmarks; i++)
                    MenuToolkit.addMenuItem(bookmarksMenu, new OpenLocationAction(mainFrame, new Hashtable<String, Object>(), bookmarks.get(i)), null);
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
            windowMenuFrames = new WeakHashMap<JMenuItem, Frame>();
            
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
                    Hashtable<String, Object> actionProps = new Hashtable<String, Object>();
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
