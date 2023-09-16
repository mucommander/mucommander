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


package com.mucommander.ui.dialog.help;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.XAlignedComponentPanel;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionId;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.KeyStrokeUtils;

/**
 * Dialog that displays shortcuts used in the application, sorted by topics.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ShortcutsDialog extends FocusDialog implements ActionListener {

    //////////////////////////
    // Quick search actions //
    //////////////////////////

    private final static String QUICK_SEARCH_TITLE = "shortcuts_dialog.quick_search";
    private final static Map<String, String> QUICK_SEARCH_SHORTCUTS = new Hashtable<String, String>() {
        {
            put("shortcuts_dialog.quick_search.start_search", "");
            put("shortcuts_dialog.quick_search.jump_to_previous", "UP");
            put("shortcuts_dialog.quick_search.jump_to_next", "DOWN");
            put("shortcuts_dialog.quick_search.remove_last_char", "BACKSPACE");
            put("shortcuts_dialog.quick_search.mark_jump_next", "INSERT");
            put("shortcuts_dialog.quick_search.cancel_search", "ESCAPE");
        }
    };

    /** Comparator of actions according to their labels */
    private static final Comparator<ActionId> ACTIONS_COMPARATOR = new Comparator<>() {
        public int compare(ActionId id1, ActionId id2) {
            String label1 = ActionProperties.getActionLabel(id1);
            if (label1 == null)
                return 1;

            String label2 = ActionProperties.getActionLabel(id2);
            if (label2 == null)
                return -1;

            return label1.compareTo(label2);
        }
    };

    public ShortcutsDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(ActionType.ShowKeyboardShortcuts), mainFrame);

        Container contentPane = getContentPane();
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        // Separate the actions according to their categories. 
        Map<ActionCategory, LinkedList<ActionId>> categoryToItsActionsWithShortcutsIdsMap = createCategoryToItsActionsWithShortcutsMap();

        //Create tab and panel for each category
        for(ActionCategory category: categoryToItsActionsWithShortcutsIdsMap.keySet()) {
            // Get the list of actions from the above category which have shortcuts assigned to them
            List<ActionId> categoryActionsWithShortcuts = categoryToItsActionsWithShortcutsIdsMap.get(category);
            Collections.sort(categoryActionsWithShortcuts, ACTIONS_COMPARATOR);

            // If there is at least one action in the category with shortcuts assigned to it, add tab for the category
            if (!categoryActionsWithShortcuts.isEmpty())
                addTopic(tabbedPane, ""+category, categoryActionsWithShortcuts.iterator());
        }

        // Create tab for quick-search category 
        addTopic(tabbedPane, Translator.get(QUICK_SEARCH_TITLE), QUICK_SEARCH_SHORTCUTS);

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

    private Map<ActionCategory, LinkedList<ActionId>> createCategoryToItsActionsWithShortcutsMap() {
        // Hashtable that maps actions-category to LinkedList of actions (Ids) from the category that have shortcuts assigned to them
        Hashtable<ActionCategory, LinkedList<ActionId>> categoryToItsActionsWithShortcutsIdsMap = new Hashtable<>();

        // Initialize empty LinkedList for each category
        for (ActionCategory category : ActionProperties.getNonEmptyActionCategories())
            categoryToItsActionsWithShortcutsIdsMap.put(category, new LinkedList<ActionId>());

        // Go over all action ids
        for (ActionId actionId : ActionManager.getActionIds()) {
            ActionCategory category = ActionProperties.getActionCategory(actionId);
            // If the action has category and there is a primary shortcut assigned to it, add its id to the list of the category
            if (category != null && ActionKeymap.doesActionHaveShortcut(actionId))
                categoryToItsActionsWithShortcutsIdsMap.get(category).add(actionId);
        }

        return categoryToItsActionsWithShortcutsIdsMap;
    }

    private void addTopic(JTabbedPane tabbedPane, String titleKey, Iterator<ActionId> descriptionsIterator) {
        XAlignedComponentPanel compPanel;
        JPanel northPanel;
        JScrollPane scrollPane;

        compPanel = new XAlignedComponentPanel(15);

        // Add all shortcuts and their description
        addShortcutList(compPanel, descriptionsIterator);

        // Panel needs to be vertically aligned to the top
        northPanel = new JPanel(new BorderLayout());
        northPanel.add(compPanel, BorderLayout.NORTH);

        // Horizontal/vertical scroll bars will be displayed if needed
        scrollPane = new JScrollPane(northPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        tabbedPane.addTab(titleKey, scrollPane);
    }

    private void addTopic(JTabbedPane tabbedPane, String titleKey, Map<String, String> actionsToShortcutsMap) {
        XAlignedComponentPanel compPanel;
        JPanel northPanel;
        JScrollPane scrollPane;

        compPanel = new XAlignedComponentPanel(15);

        // Add all shortcuts and their description
        addShortcutList(compPanel, actionsToShortcutsMap);

        // Panel needs to be vertically aligned to the top
        northPanel = new JPanel(new BorderLayout());
        northPanel.add(compPanel, BorderLayout.NORTH);

        // Horizontal/vertical scroll bars will be displayed if needed
        scrollPane = new JScrollPane(northPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        tabbedPane.addTab(titleKey, scrollPane);
    }

    private void addShortcutList(XAlignedComponentPanel compPanel, Iterator<ActionId> muActionIdsIterator) {
        // Add all actions shortcut and label (or tooltip if available)
        ActionId actionId;
        KeyStroke shortcut;
        String shortcutsRep;
        while (muActionIdsIterator.hasNext()) {
            actionId = muActionIdsIterator.next();

            shortcut = ActionKeymap.getAccelerator(actionId);

            shortcutsRep = KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(shortcut);

            shortcut = ActionKeymap.getAlternateAccelerator(actionId);
            if(shortcut!=null)
                shortcutsRep += " / "+ KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(shortcut);

            compPanel.addRow(shortcutsRep, new JLabel(ActionProperties.getActionDescription(actionId)), 5);
        }
    }

    private void addShortcutList(XAlignedComponentPanel compPanel, Map<String, String> actionsToShortcutsMap) {
        List<String> vec = new Vector<String>(actionsToShortcutsMap.keySet());
        Collections.sort(vec);

        for(String action : vec)
            compPanel.addRow(actionsToShortcutsMap.get(action), new JLabel(Translator.get(action)), 5);
    }

    public void actionPerformed(ActionEvent e) {
        // OK disposes the dialog
        dispose();
    }
}
