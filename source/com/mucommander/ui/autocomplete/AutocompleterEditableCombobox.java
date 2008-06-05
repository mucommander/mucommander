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
