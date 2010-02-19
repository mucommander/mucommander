/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.ui.combobox.AutocompletedEditableCombobox;

import java.awt.event.KeyEvent;

/**
 * This <code>AutocompleterTextComponent</code> implements {@link #OnEnterPressed(java.awt.event.KeyEvent)}
 * and {@link #OnEscPressed(java.awt.event.KeyEvent)} as the typical AutocompletedEditableCombobox's ops. 
 * 
 * @author Arik Hadas
 */

public class TypicalAutocompleterEditableCombobox extends AutocompleterTextComponent {
	protected AutocompletedEditableCombobox editableCombobox;
	
	public TypicalAutocompleterEditableCombobox(AutocompletedEditableCombobox editableCombobox) {
		super(editableCombobox);		
		this.editableCombobox = editableCombobox;		
	}

	@Override
    public void OnEnterPressed(KeyEvent keyEvent) {
		editableCombobox.respondToEnterKeyPressing(keyEvent);
	}

	@Override
    public void OnEscPressed(KeyEvent keyEvent) {
		editableCombobox.respondToEscapeKeyPressing(keyEvent);
	}
}
