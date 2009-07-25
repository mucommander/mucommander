/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.KeyStrokeUtils;


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
    private final static String NAVIGATION_ACTIONS[] =
    {
        com.mucommander.ui.action.impl.PopupLeftDriveButtonAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.PopupRightDriveButtonAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.ChangeLocationAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.SwitchActiveTableAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.GoToParentAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.OpenAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.OpenNativelyAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RevealInDesktopAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.StopAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.SelectFirstRowAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.SelectLastRowAction.Descriptor.ACTION_ID
    };


    ///////////////////////
    // Selection actions //
    ///////////////////////

    private final static String SELECTION_TITLE = "shortcuts_dialog.selection";
    private final static String SELECTION_ACTIONS[] =
    {
    	com.mucommander.ui.action.impl.MarkSelectedFileAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.MarkGroupAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.UnmarkGroupAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.MarkAllAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.UnmarkAllAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.InvertSelectionAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.CopyFilesToClipboardAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.CopyFilePathsAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.CopyFilePathsAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.PasteClipboardFilesAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.CompareFoldersAction.Descriptor.ACTION_ID,
    };


    //////////////////
    // View actions //
    //////////////////

    private final static String VIEW_TITLE = "shortcuts_dialog.view";
    private final static String VIEW_ACTIONS[] =
    {
        com.mucommander.ui.action.impl.GoBackAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.GoForwardAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.SortByNameAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.SortByExtensionAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.SortByDateAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.SortBySizeAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.SwapFoldersAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.SetSameFolderAction.Descriptor.ACTION_ID
    };


    /////////////////////////////
    // File operations actions //
    /////////////////////////////

    private final static String FILE_OPERATIONS_TITLE = "shortcuts_dialog.file_operations";
    private final static String FILE_OPERATIONS_ACTIONS[] =
    {
        com.mucommander.ui.action.impl.ViewAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.EditAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.CopyAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.LocalCopyAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.MoveAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RenameAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.MkdirAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.MkfileAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.DeleteAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RefreshAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.ConnectToServerAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.ShowServerConnectionsAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RunCommandAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.PackAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.UnpackAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.EmailAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.AddBookmarkAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.ShowFilePropertiesAction.Descriptor.ACTION_ID
    };


    /////////////////////
    // Windows actions //
    /////////////////////

    private final static String WINDOWS_TITLE = "shortcuts_dialog.windows";
    private final static String WINDOWS_ACTIONS[] =
    {
        com.mucommander.ui.action.impl.NewWindowAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.CloseWindowAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallPreviousWindowAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallNextWindowAction.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow1Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow2Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.RecallWindow10Action.Descriptor.ACTION_ID,
        com.mucommander.ui.action.impl.QuitAction.Descriptor.ACTION_ID
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
        super(mainFrame, MuAction.getStandardLabel(com.mucommander.ui.action.impl.ShowKeyboardShortcutsAction.class), mainFrame);

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
        if(descriptions instanceof String[][])
        	addShortcutList(compPanel, (String[][])descriptions);
        else
        	addShortcutList(compPanel, (String[])descriptions);

        // Panel needs to be vertically aligned to the top
        northPanel = new JPanel(new BorderLayout());
        northPanel.add(compPanel, BorderLayout.NORTH);

        // Horizontal/vertical scroll bars will be displayed if needed
        scrollPane = new JScrollPane(northPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        tabbedPane.addTab(Translator.get(titleKey), scrollPane);
    }


    private void addShortcutList(XAlignedComponentPanel compPanel, String muActionIds[]) {
        // Add all actions shortcut and label (or tooltip if available)
        int nbActions = muActionIds.length;
        MuAction action;
        KeyStroke shortcut;
        String shortcutsRep;
        String desc;
        for(int i=0; i<nbActions; i++) {
            action = ActionManager.getActionInstance(muActionIds[i], mainFrame);

            shortcut = action.getAccelerator();
            if(shortcut==null)
                continue;

            shortcutsRep = KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(shortcut);

            shortcut = action.getAlternateAccelerator();
            if(shortcut!=null)
                shortcutsRep += " / "+ KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(shortcut);

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
