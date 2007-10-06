/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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


package com.mucommander.ui.dialog.pref;

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.layout.XBoxPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/**
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class PreferencesDialog extends FocusDialog implements ActionListener, ChangeListener {
    // - Instance fields --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Displays the different panels. */
    private JTabbedPane tabbedPane;
    /** Stores the different panels. */
    private Vector      prefPanels;
    /** Apply button. */
    private JButton     applyButton;
    /** OK button. */
    private JButton     okButton;
    /** Cancel button. */
    private JButton     cancelButton;	



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    public PreferencesDialog(Frame parent, String title) {
        super(parent, title, parent);
	initUI();
    }

    public PreferencesDialog(Dialog parent, String title) {
        super(parent, title, parent);
	initUI();
    }



    // - UI code ----------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Initialises the tabbed panel's UI.
     */
    private void initUI() {
        Container contentPane;
        XBoxPanel buttonsPanel;
        JPanel    tempPanel;

        // Initialises the tabbed pane.
        prefPanels = new Vector();
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.addChangeListener(this);

        // Adds the tabbed pane.
        contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // Buttons panel.
        buttonsPanel = new XBoxPanel();
        buttonsPanel.add(applyButton = new JButton(Translator.get("apply")));
        buttonsPanel.addSpace(20);
        buttonsPanel.add(okButton     = new JButton(Translator.get("ok")));
        buttonsPanel.add(cancelButton = new JButton(Translator.get("cancel")));

        // Buttons listening.
        applyButton.addActionListener(this);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);

        // Aligns the button panel to the right.
        tempPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tempPanel.add(buttonsPanel);
        contentPane.add(tempPanel, BorderLayout.SOUTH);

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);
    }


    private Component getTabbedPanel(PreferencesPanel prefPanel, boolean scroll) {
        if(scroll) {
            JScrollPane scrollPane = new JScrollPane(prefPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);

            return scrollPane;
        }
        return prefPanel;
    }

    public void addPreferencesPanel(PreferencesPanel prefPanel, String iconName, boolean scroll) {
        tabbedPane.addTab(prefPanel.getTitle(), IconManager.getIcon(IconManager.PREFERENCES_ICON_SET, iconName), getTabbedPanel(prefPanel, scroll));
        prefPanels.add(prefPanel);
    }

    /**
     * Adds a new prefences panel and creates a new tab with an icon.
     */
    public void addPreferencesPanel(PreferencesPanel prefPanel, String iconName) {addPreferencesPanel(prefPanel, iconName, true);}

    public void addPreferencesPanel(PreferencesPanel prefPanel, boolean scroll) {
        tabbedPane.addTab(prefPanel.getTitle(), getTabbedPanel(prefPanel, scroll));
        prefPanels.add(prefPanel);
    }

    public void addPreferencesPanel(PreferencesPanel prefPanel) {addPreferencesPanel(prefPanel, true);}

    /**
     * Calls {@link PreferencesPanel#commit()} on all registered preference panels.
     */
    public void commit() {
        // Ask pref panels to commit changes
        int nbPanels = prefPanels.size();
        for(int i = 0; i < nbPanels; i++)
            ((PreferencesPanel)prefPanels.elementAt(i)).commit();
    }

    public boolean checkCommit() {
        // Ask pref panels to commit changes
        int nbPanels = prefPanels.size();
        for(int i = 0; i < nbPanels; i++)
            if(!((PreferencesPanel)prefPanels.elementAt(i)).checkCommit())
                return false;
        return true;
    }

    /**
     * Sets the currently active tab.
     */
    public void setActiveTab(int index) {tabbedPane.setSelectedIndex(index);}



    // - Listener code ----------------------------------------------------------
    // --------------------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Commit changes
        if (source == okButton || source == applyButton) {
            if(!checkCommit())
                return;
            commit();
        }

        // Dispose dialog
        if (source == okButton || source == cancelButton)
            dispose();
    }

    public void tabSelectionChanged(int newIndex) {}

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == tabbedPane)
            tabSelectionChanged(tabbedPane.getSelectedIndex());
    }
}
