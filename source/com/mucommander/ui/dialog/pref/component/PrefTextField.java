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

package com.mucommander.ui.dialog.pref.component;

import com.mucommander.ui.dialog.pref.PreferencesDialog;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Arik Hadas
 */
public abstract class PrefTextField extends JTextField implements PrefComponent {
	
	public PrefTextField(int columns) {
		super(columns);
	}
	
	public PrefTextField(String text) {
		super(text);
	}
	
	public void addDialogListener(final PreferencesDialog dialog) {
		getDocument().addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				dialog.componentChanged(PrefTextField.this);	
			}

			public void insertUpdate(DocumentEvent e) {
				dialog.componentChanged(PrefTextField.this);
			}

			public void removeUpdate(DocumentEvent e) {
				dialog.componentChanged(PrefTextField.this);	
			}
		});
	}
}
