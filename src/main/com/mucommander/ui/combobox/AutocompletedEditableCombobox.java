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

package com.mucommander.ui.combobox;

import com.mucommander.commons.runtime.JavaVersion;
import com.mucommander.ui.autocomplete.EditableComboboxCompletion;
import com.mucommander.ui.autocomplete.TypicalAutocompleterEditableCombobox;
import com.mucommander.ui.autocomplete.completers.Completer;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Vector;

/**
 * <code>AutocompletedEditableCombobox</code> is an editable combo-box that provides
 * auto-completion capabilities based on the given <code>Completer</code>.
 * 
 * @author Arik Hadas
 */
public class AutocompletedEditableCombobox extends EditableComboBox {

	/**
     * Creates a new editable combo box and a JTextField to be used as the editor.
     * Has the same effect as calling {@link EditableComboBox#EditableComboBox(javax.swing.JTextField)} with a null value.
     */
    public AutocompletedEditableCombobox(Completer completer) {
        super();
        enableAutoCompletion(completer);
    }

    /**
     * Creates a new editable combo box using the given text field as the editor.
     *
     * @param textField the text field to be used as the combo box's editor. If null, a new JTextField instance
     * will be created and used.
     */
    public AutocompletedEditableCombobox(JTextField textField, Completer completer) {
        super(textField);
        enableAutoCompletion(completer);
    }

    /**
     * Creates a new editable combo box using the given text field as the editor and ComboBoxModel.
     *
     * @param textField the text field to be used as the combo box's editor. If null, a new JTextField instance
     * will be created and used.
     * @param comboBoxModel the ComboBoxModel to use for this combo box
     */
    public AutocompletedEditableCombobox(JTextField textField, ComboBoxModel comboBoxModel, Completer completer) {
        super(textField, comboBoxModel);
        enableAutoCompletion(completer);
    }

    /**
     * Creates a new editable combo box using the given text field as the editor and items to populate the initial items list.
     *
     * @param textField the text field to be used as the combo box's editor. If null, a new JTextField instance
     * will be created and used.
     * @param items items used to populate the initial items list.
     */
    public AutocompletedEditableCombobox(JTextField textField, Object[] items, Completer completer) {
        super(textField, items);
        enableAutoCompletion(completer);
    }

    /**
     * Creates a new editable combo box using the given text field as the editor and items to populate the initial items list.
     *
     * @param textField the text field to be used as the combo box's editor. If null, a new JTextField instance
     * will be created and used.
     * @param items items used to populate the initial items list.
     */
    public AutocompletedEditableCombobox(JTextField textField, Vector<Object> items, Completer completer) {
        super(textField, items);
        enableAutoCompletion(completer);
    }
    
    private void enableAutoCompletion(Completer completer) {
    	new EditableComboboxCompletion(new TypicalAutocompleterEditableCombobox(this), completer);
    }
	
    /**
     * The desired behavior of this editable combo-box when enter key is pressed.
     * 
     * @param keyEvent - the KeyEvent that occurred. 
     */
	public void respondToEnterKeyPressing(KeyEvent keyEvent) {
		// Combo popup menu is visible
		if(isPopupVisible()) {
			// Under Java 1.5 or lower, we need to explicitely hide the popup.
			if(JavaVersion.JAVA_1_5.isCurrentOrLower())
				hidePopup();
			
			// Note that since the event is not consumed, JComboBox will catch it and fire
		}
		// Combo popup menu is not visible, these events really belong to the text field
		else {
			// Notify listeners that the text field has been validated
			fireComboFieldValidated();
            
            // /!\ Consume the event so to prevent JComboBox from firing an ActionEvent (default JComboBox behavior)
            keyEvent.consume();
		}		
	}

	/**
     * The desired behavior of this editable combo-box when escape key is pressed.
     * 
     * @param keyEvent - the KeyEvent that occurred. 
     */
	public void respondToEscapeKeyPressing(KeyEvent keyEvent) {
		// Combo popup menu is visible
		if(isPopupVisible()) {
			 // Explicitely hide popup menu, JComboBox does not seem do it automatically (at least under Mac OS X + Java 1.5 and Java 1.4)
            hidePopup();
            // Consume the event so that it is not propagated, since dialogs catch this event to close the window
            keyEvent.consume();
		}
		// Combo popup menu is not visible, these events really belong to the text field
        else {
        	// Notify listeners that the text field has been cancelled
        	fireComboFieldCancelled();
        }
	}
}
