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


package com.mucommander.ui.dialog.pref;

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.dialog.pref.component.PrefComponent;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.layout.XBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/**
 * Dialog meant to let users edit software preferences.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public abstract class PreferencesDialog extends FocusDialog implements ActionListener {
    // - Instance fields --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Displays the different panels. */
    private JTabbedPane                      tabbedPane;
    /** Stores the different panels. */
    private java.util.List<PreferencesPanel> prefPanels;
    /** Apply button. */
    private JButton                          applyButton;
    /** OK button. */
    private JButton                          okButton;
    /** Cancel button. */
    private JButton                          cancelButton;



    // - Initialization ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a new preferences dialog.
     * @param parent parent of the dialog.
     * @param title  title of the dialg.
     */
    public PreferencesDialog(Frame parent, String title) {
        super(parent, title, parent);
        initUI();
    }

    /**
     * Creates a new preferences dialog.
     * @param parent parent of the dialog.
     * @param title  title of the dialg.
     */
    public PreferencesDialog(Dialog parent, String title) {
        super(parent, title, parent);
        initUI();
    }



    // - UI code ----------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Initializes the tabbed panel's UI.
     */
    private void initUI() {
        Container contentPane;
        XBoxPanel buttonsPanel;
        JPanel    tempPanel;

        // Initializes the tabbed pane.
        prefPanels = new Vector<PreferencesPanel>();
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);

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
        
        // Disable "commit buttons".
        okButton.setEnabled(false);
    	applyButton.setEnabled(false);

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

    /**
     * Adds the specified preferences panel to this dialog.
     * @param prefPanel panel to add.
     * @param iconName  name of the icon that represents this dialog.
     * @param scroll    whether this panel should be wrapped in a scroll panel.
     */
    public void addPreferencesPanel(PreferencesPanel prefPanel, String iconName, boolean scroll) {
        tabbedPane.addTab(prefPanel.getTitle(), IconManager.getIcon(IconManager.PREFERENCES_ICON_SET, iconName), getTabbedPanel(prefPanel, scroll));
        prefPanels.add(prefPanel);
    }

    /**
     * Adds a new preferences panel and creates a new tab with an icon.
     * @param prefPanel panel to add.
     * @param iconName  name of the icon that represents this dialog.
     */
    public void addPreferencesPanel(PreferencesPanel prefPanel, String iconName) {addPreferencesPanel(prefPanel, iconName, true);}

    /**
     * Adds the specified preferences panel to this dialog.
     * @param prefPanel panel to add.
     * @param scroll    whether this panel should be wrapped in a scroll panel.
     */
    public void addPreferencesPanel(PreferencesPanel prefPanel, boolean scroll) {
        tabbedPane.addTab(prefPanel.getTitle(), getTabbedPanel(prefPanel, scroll));
        prefPanels.add(prefPanel);
    }

    /**
     * Adds the specified preferences panel to this dialog.
     * @param prefPanel panel to add.
     */
    public void addPreferencesPanel(PreferencesPanel prefPanel) {addPreferencesPanel(prefPanel, true);}

    /**
     * Calls {@link PreferencesPanel#commit()} on all registered preference panels.
     */
    public void commit() {
        // Ask pref panels to commit changes
        int nbPanels = prefPanels.size();
        for(int i = 0; i < nbPanels; i++)
            prefPanels.get(i).commit();
        setCommitButtonsEnabled(false);
    }

    /**
     * Notifies all panels that changes are about to be commited.
     * <p>
     * This gives preference panels a chance to display warning or errors before changes are
     * commited.
     * </p>
     * @return <code>true</code> if all preference panels are ok with commiting the changes, <code>false</code> otherwise.
     */
    public boolean checkCommit() {
        // Ask pref panels to commit changes
        int nbPanels = prefPanels.size();
        for(int i = 0; i < nbPanels; i++)
            if(!prefPanels.get(i).checkCommit())
                return false;
        return true;
    }

    /**
     * Sets the currently active tab.
     * @param index index of the tab to select.
     */
    public void setActiveTab(int index) {tabbedPane.setSelectedIndex(index);}



    // - Listener code ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Reacts to buttons being pushed.
     */
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

    /**
     * Returns the index of the currently selected configuration panel.
     * @return the index of the currently selected configuration panel.
     */
    public int getSelectedPanelIndex() {return tabbedPane.getSelectedIndex();}
    
    /**
     * This function set the "commit buttons", i.e apply & ok buttons, enabled\disabled
     * according to the given parameter.
     * 
     * @param enable - parameter that indicated if the commit button will turn to be
     *  enabled (true) or disabled (false).
     */
    protected void setCommitButtonsEnabled(boolean enable) {
    	okButton.setEnabled(enable);
    	applyButton.setEnabled(enable);
    }
    
    /**
     * Function that will be called when the user change a value in a PrefComponent in this dialog.
     * 
     * @param component - the PrefComponent that its value was changed.
     */
    public abstract void componentChanged(PrefComponent component);
}
