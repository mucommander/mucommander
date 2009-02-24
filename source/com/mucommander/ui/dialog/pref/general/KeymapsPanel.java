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

package com.mucommander.ui.dialog.pref.general;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;

/**
 * 'Shortcuts' preferences panel.
 * 
 * @author Arik Hadas, Johann Schmitz
 */
public class KeymapsPanel extends PreferencesPanel {
	
	/* The table with action mappings */
	private KeymapTable keymapsTable;

	public KeymapsPanel(PreferencesDialog parent) {
		//TODO: use translator
		super(parent, "Shortcuts");
		initUI();
		new JTable();
		setPreferredSize(new Dimension(0,0));
		
		keymapsTable.addDialogListener(parent);
	}
	
	// - UI initialisation ------------------------------------------------------
    // --------------------------------------------------------------------------
	private void initUI() {
		setLayout(new GridLayout(1, 0));
		keymapsTable = new KeymapTable();
		keymapsTable.setPreferredColumnWidths(new double[] {0.4, 0.2, 0.2});
		add(new JScrollPane(keymapsTable));
	}	
	
	///////////////////////
    // PrefPanel methods //
    ///////////////////////
	protected void commit() {
		keymapsTable.updateActions();
	}
}
