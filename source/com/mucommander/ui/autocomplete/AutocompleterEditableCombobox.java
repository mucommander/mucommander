package com.mucommander.ui.autocomplete;

import com.mucommander.ui.combobox.EditableComboBox;

import java.awt.event.KeyListener;

/**
 * AutocompleterEditableCombobox extends AutocompleterTextComponent to provide a way to add auto-completion support for EditableCombobox. 
 * 
 * @author Arik Hadas
 */

public abstract class AutocompleterEditableCombobox extends AutocompleterTextComponent {
	private EditableComboBox editableCombobox;
	
	public AutocompleterEditableCombobox(EditableComboBox combobox) {
		super(combobox.getTextField());		
		editableCombobox = combobox;
		
		// Remove all key listeners which are defined for the EditableCombobox
		removeAllKeyListeners();
	}
	
	private void removeAllKeyListeners() {
		KeyListener[] l = editableCombobox.getTextField().getKeyListeners();
		int nbKeyListeners = l.length;
		for (int i=0 ; i<nbKeyListeners; i++)
			editableCombobox.getTextField().removeKeyListener(l[i]);
	}
	
	/**
	 * 
	 * @return Array which contains the names of the combobox items.
	 */
	public String[] getItemsNames() {		
		int nbItems = editableCombobox.getItemCount();
		String[] result = new String[nbItems];
		for (int i=0; i < nbItems; i++)
			result[i] = editableCombobox.getItemAt(i).toString();
		return result;
	}
}
