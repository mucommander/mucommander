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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowKeyboardShortcutsAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
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
    private final static String QUICK_SEARCH_DESC[][] = {
        {"", "shortcuts_dialog.quick_search.start_search"},
        {"UP", "shortcuts_dialog.quick_search.jump_to_previous"},
        {"DOWN", "shortcuts_dialog.quick_search.jump_to_next"},
        {"BACKSPACE", "shortcuts_dialog.quick_search.remove_last_char"},
        {"INSERT", "shortcuts_dialog.quick_search.mark_jump_next"},
        {"ESCAPE", "shortcuts_dialog.quick_search.cancel_search"},
    };
    
    public ShortcutsDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(ShowKeyboardShortcutsAction.Descriptor.ACTION_ID), mainFrame);

        Container contentPane = getContentPane();
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        // Separate the actions according to their categories. 
        Hashtable categoryToItsActionsWithShortcutsIdsMap = createCategoryToItsActionsWithShortcutsMap();
        
        //Create tab and panel for each category
        Enumeration categories = categoryToItsActionsWithShortcutsIdsMap.keys();
        while (categories.hasMoreElements()) {
        	ActionCategory category = (ActionCategory) categories.nextElement();
        	// Get the list of actions from the above category which have shortcuts assigned to them
        	LinkedList categoryActionsWithShortcuts = (LinkedList) categoryToItsActionsWithShortcutsIdsMap.get(category);
        	// If there is at least one action in the category with shortcuts assigned to it, add tab for the category
        	if (!categoryActionsWithShortcuts.isEmpty())
        		addTopic(tabbedPane, ""+category, categoryActionsWithShortcuts.iterator());
        }
        
        // Create tab for quick-search category 
        addTopic(tabbedPane, Translator.get(QUICK_SEARCH_TITLE), QUICK_SEARCH_DESC);
        
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

    private Hashtable createCategoryToItsActionsWithShortcutsMap() {
    	// Get Iterator to all existing action categories
        Iterator actionCategoriesIterator = ActionProperties.getActionCategories().iterator();

        // Hashtable that maps actions-category to LinkedList of actions (Ids) from the category that have shortcuts assigned to them
        Hashtable categoryToItsActionsWithShortcutsIdsMap = new Hashtable();
        
    	// Initialize empty LinkedList for each category
        while (actionCategoriesIterator.hasNext())
        	categoryToItsActionsWithShortcutsIdsMap.put(actionCategoriesIterator.next(), new LinkedList());
        
        // Go over all action ids
    	Enumeration actionIds = ActionManager.getActionIds();
    	while (actionIds.hasMoreElements()) {
    		String actionId = (String) actionIds.nextElement();
    		ActionCategory category = ActionProperties.getActionCategory(actionId);
    		// If the action has category and there is a primary shortcut assigned to it, add its id to the list of the category
    		if (category != null && ActionKeymap.doesActionHaveShortcut(actionId))
    			((LinkedList) categoryToItsActionsWithShortcutsIdsMap.get(category)).add(actionId);
    	}
    	
    	return categoryToItsActionsWithShortcutsIdsMap;
    }

    private void addTopic(JTabbedPane tabbedPane, String titleKey, Iterator descriptionsIterator) {
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
    
    private void addTopic(JTabbedPane tabbedPane, String titleKey, String descriptions[][]) {
        XAlignedComponentPanel compPanel;
        JPanel northPanel;
        JScrollPane scrollPane;

        compPanel = new XAlignedComponentPanel(15);

        // Add all shortcuts and their description
        addShortcutList(compPanel, descriptions);

        // Panel needs to be vertically aligned to the top
        northPanel = new JPanel(new BorderLayout());
        northPanel.add(compPanel, BorderLayout.NORTH);

        // Horizontal/vertical scroll bars will be displayed if needed
        scrollPane = new JScrollPane(northPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        tabbedPane.addTab(titleKey, scrollPane);
    }

    private void addShortcutList(XAlignedComponentPanel compPanel, Iterator muActionIdsIterator) {
        // Add all actions shortcut and label (or tooltip if available)
        String actionId;
        KeyStroke shortcut;
        String shortcutsRep;
        while (muActionIdsIterator.hasNext()) {
        	actionId =(String) muActionIdsIterator.next();

            shortcut = ActionKeymap.getAccelerator(actionId);

            shortcutsRep = KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(shortcut);

            shortcut = ActionKeymap.getAlternateAccelerator(actionId);
            if(shortcut!=null)
                shortcutsRep += " / "+ KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(shortcut);

            compPanel.addRow(shortcutsRep, new JLabel(ActionProperties.getActionDescription(actionId)), 5);
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
