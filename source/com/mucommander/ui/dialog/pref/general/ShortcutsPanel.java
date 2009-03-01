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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;

/**
 * 'Shortcuts' preferences panel.
 * 
 * @author Arik Hadas, Johann Schmitz
 */
public class ShortcutsPanel extends PreferencesPanel {
	
	// The table with action mappings
	private ShortcutsTable shortcutsTable;
	
	// Area in which tooltip texts and error messages are shown below the table
	private TooltipBar informationBar;
	
	public ShortcutsPanel(PreferencesDialog parent) {
		//TODO: use translator
		super(parent, "Shortcuts");
		initUI();
		setPreferredSize(new Dimension(0,0));
		
		shortcutsTable.addDialogListener(parent);
	}
	
	// - UI initialisation ------------------------------------------------------
    // --------------------------------------------------------------------------
	private void initUI() {
		setLayout(new BorderLayout());

		informationBar = new TooltipBar();
		shortcutsTable = new ShortcutsTable(informationBar);
		
		add(createTablePanel(), BorderLayout.CENTER);
		add(informationBar, BorderLayout.SOUTH);
	}
	
	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new GridLayout(1,0));
		shortcutsTable.setPreferredColumnWidths(new double[] {0.4, 0.2, 0.2});
		panel.add(new JScrollPane(shortcutsTable));
		return panel;
	}
	
	///////////////////////
    // PrefPanel methods //
    ///////////////////////
	protected void commit() {
		shortcutsTable.updateActions();
	}
	
	
	class TooltipBar extends JLabel implements Runnable {
		private String lastActionTooltipShown;
		private final String EMPTY_MESSAGE = " "; 
		
		public TooltipBar() {
			Font tableFont = UIManager.getFont("TableHeader.font");
			setFont(new Font(tableFont.getName(), Font.BOLD, tableFont.getSize()));
			setHorizontalAlignment(JLabel.LEFT);
			setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
			setText(EMPTY_MESSAGE);
		}
		
		public void showActionTooltip(String text) {
			setText(lastActionTooltipShown = text == null ? EMPTY_MESSAGE : text);
		}
		
		public void clear() {
			setText(EMPTY_MESSAGE);
		}
		
		public void showKeystrokeAlreadyInUseMsg(String text) {
			setText(text);
			new Thread(this).start();
		}

		public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			showActionTooltip(lastActionTooltipShown);
		}
	}
}
