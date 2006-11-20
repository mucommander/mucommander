package com.mucommander.ui;

import com.mucommander.shell.ShellHistoryListener;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.ui.comp.combobox.EditableComboBox;
import com.mucommander.ui.comp.combobox.EditableComboBoxListener;

import javax.swing.*;
import java.util.Iterator;

/**
 * Widget used for shell command input.
 * <p>
 * In addition to providing basic shell command input features, this widget interfaces with
 * the {@link com.mucommander.shell.ShellHistoryManager} to offer a history of shell commands
 * for the user to browse through.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class ShellComboBox extends EditableComboBox implements EditableComboBoxListener, ShellHistoryListener {
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
        //        super(new JTextField() {public void setText(String s) {super.setText(s);System.out.println("HEEEEEEEEEEEEERE " + s);}});
        this.parent = parent;

        // Sets the combo box's editor.
        this.input = getTextField();

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

    public void comboBoxSelectionChanged(EditableComboBox source) {}

    public void textFieldValidated(EditableComboBox source) {parent.runCommand(input.getText());}

    public void textFieldCancelled(EditableComboBox source) {parent.dispose();}


    // - Shell listener code --------------------------------------------------------
    // ------------------------------------------------------------------------------

    public void historyChanged(String command) {insertItemAt(command, 0);}
    public void historyCleared() {
        input.setText("");
        removeAllItems();
    }


    // - Command handling -----------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Returns the current shell command.
     * @return the current shell command.
     */
    public String getCommand() {return input.getText();}
}
