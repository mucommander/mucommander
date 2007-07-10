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

package com.mucommander.ui.dialog.shell;

import com.mucommander.shell.ShellHistoryListener;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.ui.combobox.EditableComboBox;
import com.mucommander.ui.combobox.EditableComboBoxListener;
import com.mucommander.ui.combobox.SaneComboBox;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.Iterator;

/**
 * Widget used for shell command input.
 * <p>
 * In addition to providing basic shell command input features, this widget interfaces with
 * the {@link com.mucommander.shell.ShellHistoryManager} to offer a history of shell commands
 * for the user to browse through.
 * </p>
 * <p>
 * Note that even though this component is affected by themes, it's impossible to edit the current theme while it's being displayed.
 * For this reason, the RunDialog doesn't listen to theme modifications.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class ShellComboBox extends EditableComboBox implements EditableComboBoxListener, ShellHistoryListener, PopupMenuListener {
    // - Instance fields -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Input field used to type in commands. */
    private JTextField input;
    /** Where to run commands. */
    private RunDialog  parent;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new shell combo box.
     * @param parent where to execute commands.
     */
    public ShellComboBox(RunDialog parent) {
        this.parent = parent;

        // Sets the combo box's editor.
        this.input = getTextField();

        addPopupMenuListener(this);

        // Sets colors and font according to the current theme.
        setForeground(ThemeManager.getCurrentColor(Theme.SHELL_HISTORY_FOREGROUND_COLOR));
        setBackground(ThemeManager.getCurrentColor(Theme.SHELL_HISTORY_BACKGROUND_COLOR));
        setSelectionForeground(ThemeManager.getCurrentColor(Theme.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR));
        setSelectionBackground(ThemeManager.getCurrentColor(Theme.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR));
        setFont(ThemeManager.getCurrentFont(Theme.SHELL_HISTORY_FONT));

        // Fills the combo box with the current history.
        populateHistory();
        ShellHistoryManager.addListener(this);

        // Select first item in the combo box (if any)
        if(getItemCount()>0)
            setSelectedIndex(0);

        // Automatically update the text field's contents when an item is selected in this combo box
        setComboSelectionUpdatesTextField(true);
        
        // Listener to actions fired by this EditableComboBox
        addEditableComboBoxListener(this);
    }

    /**
     * Fills the combo box with the current shell history.
     */
    private void populateHistory() {
        Iterator iterator;
        String   command;

        // Empties the content of the combo box
        removeAllItems();

        // Iterates through all shell history elements.
        iterator = ShellHistoryManager.getHistoryIterator();
        command  = null;
        while(iterator.hasNext())
            insertItemAt((command = iterator.next().toString()), 0);

        // If the list is not empty, initialises the input field on the last command.
        if(command != null) {
            input.setText(command);
            input.setSelectionStart(0);
            input.setSelectionEnd(command.length());
        }
    }



    // - Misc. ----------------------------------------------------------------------
    // ------------------------------------------------------------------------------

    /**
     * Overrides this method to ignore events received when this component is disabled.
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if(enabled) {
            input.setSelectionStart(0);
            input.setSelectionEnd(input.getText().length());
        }
    }



    // - EditableComboBoxListener implementation ------------------------------------
    // ------------------------------------------------------------------------------

    public void comboBoxSelectionChanged(SaneComboBox source) {}

    public void textFieldValidated(EditableComboBox source) {parent.runCommand(input.getText());}

    public void textFieldCancelled(EditableComboBox source) {parent.dispose();}


    // - Shell listener code --------------------------------------------------------
    // ------------------------------------------------------------------------------

    public void historyChanged(String command) {insertItemAt(command, 0);}
    public void historyCleared() {
        removeAllItems();
        input.setText("");
    }



    // - Popup menu listening -------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Ignored.
     */
    public void popupMenuCanceled(PopupMenuEvent e) {}

    /**
     * Makes sure the selection is always the first element in the list.
     */
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        setComboSelectionUpdatesTextField(false);
        setSelectedIndex(0);
        setComboSelectionUpdatesTextField(true);
    }

    /**
     * Ignored.
     */
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}


    // - Command handling -----------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Returns the current shell command.
     * @return the current shell command.
     */
    public String getCommand() {return input.getText();}
}
