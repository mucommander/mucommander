/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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


package com.mucommander.ui.dialog.help;

import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Dialog that displays shortcuts used in the application, sorted by topics.
 *
 * @author Maxence Bernard
 */
public class ShortcutsDialog extends FocusDialog implements ActionListener {

    private MainFrame mainFrame;

    ////////////////////////
    // Navigation actions //
    ////////////////////////

    private final static String NAVIGATION_TITLE = "shortcuts_dialog.navigation";
    private final static Class NAVIGATION_ACTIONS[] =
    {
        com.mucommander.ui.action.PopupLeftDriveButtonAction.class,
        com.mucommander.ui.action.PopupRightDriveButtonAction.class,
        com.mucommander.ui.action.ChangeLocationAction.class,
        com.mucommander.ui.action.SwitchActiveTableAction.class,
        com.mucommander.ui.action.GoToParentAction.class,
        com.mucommander.ui.action.OpenAction.class,
        com.mucommander.ui.action.OpenNativelyAction.class,
        com.mucommander.ui.action.RevealInDesktopAction.class,
        com.mucommander.ui.action.StopAction.class,
        com.mucommander.ui.action.SelectFirstRowAction.class,
        com.mucommander.ui.action.SelectLastRowAction.class
    };


    ///////////////////////
    // Selection actions //
    ///////////////////////

    private final static String SELECTION_TITLE = "shortcuts_dialog.selection";
    private final static Class SELECTION_ACTIONS[] =
    {	com.mucommander.ui.action.MarkSelectedFileAction.class,
        com.mucommander.ui.action.MarkGroupAction.class,
        com.mucommander.ui.action.UnmarkGroupAction.class,
        com.mucommander.ui.action.MarkAllAction.class,
        com.mucommander.ui.action.UnmarkAllAction.class,
        com.mucommander.ui.action.InvertSelectionAction.class,
        com.mucommander.ui.action.CopyFilesToClipboardAction.class,
        com.mucommander.ui.action.CopyFilePathsAction.class,
        com.mucommander.ui.action.CopyFilePathsAction.class,
        com.mucommander.ui.action.PasteClipboardFilesAction.class,
        com.mucommander.ui.action.CompareFoldersAction.class,
    };


    //////////////////
    // View actions //
    //////////////////

    private final static String VIEW_TITLE = "shortcuts_dialog.view";
    private final static Class VIEW_ACTIONS[] =
    {
        com.mucommander.ui.action.GoBackAction.class,
        com.mucommander.ui.action.GoForwardAction.class,
        com.mucommander.ui.action.SortByNameAction.class,
        com.mucommander.ui.action.SortByExtensionAction.class,
        com.mucommander.ui.action.SortByDateAction.class,
        com.mucommander.ui.action.SortBySizeAction.class,
        com.mucommander.ui.action.SwapFoldersAction.class,
        com.mucommander.ui.action.SetSameFolderAction.class
    };


    /////////////////////////////
    // File operations actions //
    /////////////////////////////

    private final static String FILE_OPERATIONS_TITLE = "shortcuts_dialog.file_operations";
    private final static Class FILE_OPERATIONS_ACTIONS[] =
    {
        com.mucommander.ui.action.ViewAction.class,
        com.mucommander.ui.action.EditAction.class,
        com.mucommander.ui.action.CopyAction.class,
        com.mucommander.ui.action.LocalCopyAction.class,
        com.mucommander.ui.action.MoveAction.class,
        com.mucommander.ui.action.RenameAction.class,
        com.mucommander.ui.action.MkdirAction.class,
        com.mucommander.ui.action.MkfileAction.class,
        com.mucommander.ui.action.DeleteAction.class,
        com.mucommander.ui.action.RefreshAction.class,
        com.mucommander.ui.action.ConnectToServerAction.class,
        com.mucommander.ui.action.ShowServerConnectionsAction.class,
        com.mucommander.ui.action.RunCommandAction.class,
        com.mucommander.ui.action.PackAction.class,
        com.mucommander.ui.action.UnpackAction.class,
        com.mucommander.ui.action.EmailAction.class,
        com.mucommander.ui.action.AddBookmarkAction.class,
        com.mucommander.ui.action.ShowFilePropertiesAction.class
    };


    /////////////////////
    // Windows actions //
    /////////////////////

    private final static String WINDOWS_TITLE = "shortcuts_dialog.windows";
    private final static Class WINDOWS_ACTIONS[] =
    {
        com.mucommander.ui.action.NewWindowAction.class,
        com.mucommander.ui.action.CloseWindowAction.class,
        com.mucommander.ui.action.RecallPreviousWindowAction.class,
        com.mucommander.ui.action.RecallNextWindowAction.class,
        com.mucommander.ui.action.RecallWindow1Action.class,
        com.mucommander.ui.action.RecallWindow2Action.class,
        com.mucommander.ui.action.RecallWindow10Action.class,
        com.mucommander.ui.action.QuitAction.class
    };


    //////////////////////////
    // Quick search actions //
    //////////////////////////
    
    private final static String QUICK_SEARCH_TITLE = "shortcuts_dialog.quick_search";
    private final static String QUICK_SEARCH_DESC[][] = {
        {"", "shortcuts_dialog.quick_search.start_search"},
        {"UP", "shortcuts_dialog.quick_search.jump_to_previous"},
        {"DOWN", "shortcuts_dialog.quick_search.jump_to_next"},
        {"BACKSPACE", "shortcuts_dialog.quick_search.remove_last_char"},
        {"INSERT", "shortcuts_dialog.quick_search.mark_jump_next"},
        {"ESCAPE", "shortcuts_dialog.quick_search.cancel_search"},
    };


    public ShortcutsDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get(com.mucommander.ui.action.ShowKeyboardShortcutsAction.class.getName()+".label"), mainFrame);

        this.mainFrame = mainFrame;

        Container contentPane = getContentPane();
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        // Create a tab and panel for each topic
        addTopic(tabbedPane, NAVIGATION_TITLE, NAVIGATION_ACTIONS);
        addTopic(tabbedPane, SELECTION_TITLE, SELECTION_ACTIONS);
        addTopic(tabbedPane, QUICK_SEARCH_TITLE, QUICK_SEARCH_DESC);
        addTopic(tabbedPane, VIEW_TITLE, VIEW_ACTIONS);
        addTopic(tabbedPane, FILE_OPERATIONS_TITLE, FILE_OPERATIONS_ACTIONS);
        addTopic(tabbedPane, WINDOWS_TITLE, WINDOWS_ACTIONS);
        
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // Add an OK button
        JButton okButton = new JButton(Translator.get("ok"));
        contentPane.add(DialogToolkit.createOKPanel(okButton, getRootPane(), this), BorderLayout.SOUTH);
        // OK will be selected when enter is pressed
        getRootPane().setDefaultButton(okButton);
        // First tab will receive initial focus
        setInitialFocusComponent(tabbedPane);

        // Set a reasonable maximum size, scroll bars will be displayed if there is not enough space
        setMaximumSize(new Dimension(600, 360));
    }


    private void addTopic(JTabbedPane tabbedPane, String titleKey, Object descriptions[]) {
        XAlignedComponentPanel compPanel;
        JPanel northPanel;
        JScrollPane scrollPane;

        compPanel = new XAlignedComponentPanel(15);

        // Add all shortcuts and their description
        if(descriptions instanceof Class[])
            addShortcutList(compPanel, (Class[])descriptions);
        else
            addShortcutList(compPanel, (String[][])descriptions);

        // Panel needs to be vertically aligned to the top
        northPanel = new JPanel(new BorderLayout());
        northPanel.add(compPanel, BorderLayout.NORTH);

        // Horizontal/vertical scroll bars will be displayed if needed
        scrollPane = new JScrollPane(northPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        tabbedPane.addTab(Translator.get(titleKey), scrollPane);
    }


    private void addShortcutList(XAlignedComponentPanel compPanel, Class muActionClasses[]) {
        // Add all actions shortcut and label (or tooltip if available)
        int nbActions = muActionClasses.length;
        MuAction action;
        KeyStroke shortcut;
        String shortcutsRep;
        String desc;
        for(int i=0; i<nbActions; i++) {
            action = ActionManager.getActionInstance(muActionClasses[i], mainFrame);

            shortcut = action.getAccelerator();
            if(shortcut==null)
                continue;

            shortcutsRep = MuAction.getKeyStrokeRepresentation(shortcut);

            shortcut = action.getAlternateAccelerator();
            if(shortcut!=null)
                shortcutsRep += " / "+ MuAction.getKeyStrokeRepresentation(shortcut);

            desc = action.getToolTipText();
            if(desc==null)
                desc = action.getLabel();
            compPanel.addRow(shortcutsRep, new JLabel(desc), 5);
        }
    }


    private void addShortcutList(XAlignedComponentPanel compPanel, String desc[][]) {
        int nbShortcuts = desc.length;
        for(int i=0; i<nbShortcuts; i++)
            compPanel.addRow(desc[i][0], new JLabel(Translator.get(desc[i][1])), 5);
    }


    public void actionPerformed(ActionEvent e) {
        // OK disposes the dialog
        dispose();
    }
}
