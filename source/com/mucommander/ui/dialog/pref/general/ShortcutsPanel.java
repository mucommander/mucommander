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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.mucommander.ui.action.ActionKeymapIO;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.text.KeyStrokeUtils;

/**
 * 'Shortcuts' preferences panel.
 * 
 * @author Arik Hadas, Johann Schmitz
 */
public class ShortcutsPanel extends PreferencesPanel {
	
	// The table with action mappings
	private ShortcutsTable shortcutsTable;
	
	// Area in which tooltip texts and error messages are shown below the table
	private TooltipBar tooltipBar;
	
	public ShortcutsPanel(PreferencesDialog parent) {
		//TODO: use translator
		super(parent, "Shortcuts");
		initUI();
		setPreferredSize(new Dimension(0,0));
		
		shortcutsTable.addDialogListener(parent);
	}
	
	// - UI initialization ------------------------------------------------------
    // --------------------------------------------------------------------------
	private void initUI() {
		setLayout(new BorderLayout());

		tooltipBar = new TooltipBar();
		shortcutsTable = new ShortcutsTable(tooltipBar);
		
		add(createCategoriesPanel(), BorderLayout.NORTH);
		add(createTablePanel(), BorderLayout.CENTER);
		add(tooltipBar, BorderLayout.SOUTH);
	}
	
	private JPanel createCategoriesPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.add(new JLabel("Show: "));
		
		String[] items = { "", "A", "B", "C" }; //"All actions", "File operations actions" };
		final JComboBox combo = new JComboBox();
	    for (int i = 0; i < items.length; i++) {
	      combo.addItem(items[i]);
	    }
	    combo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				final String st = (String) combo.getSelectedItem();
				shortcutsTable.updateModel(new ShortcutsTable.ActionFilter() {

					public boolean accept(MuAction action) {
						return action.getLabel().startsWith(st);
					}
				});
			}
	    });
		combo.setSelectedIndex(0);
		
		panel.add(combo);
		
		return panel;
	}
	
	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new GridLayout(1,0));
		shortcutsTable.setPreferredColumnWidths(new double[] {0.6, 0.2, 0.2});
		panel.add(new JScrollPane(shortcutsTable));
		return panel;
	}
	
	///////////////////////
    // PrefPanel methods //
    ///////////////////////
	protected void commit() {
		shortcutsTable.updateActions();
		ActionKeymapIO.setModified();
	}
	
	class TooltipBar extends JLabel {
		private String lastActionTooltipShown;
		private static final String DEFAULT_MESSAGE = "Press Enter\\double click on the shortcut you'd like to edit";
		private static final int MESSAGE_SHOWING_TIME = 3000;
		private MessageRemoverThread currentRemoverThread;
		
		public TooltipBar() {
			Font tableFont = UIManager.getFont("TableHeader.font");
			setFont(new Font(tableFont.getName(), Font.BOLD, tableFont.getSize()));
			setHorizontalAlignment(JLabel.LEFT);
			setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
			setText(DEFAULT_MESSAGE);
		}
		
		public void showActionTooltip(String text) {
			setText(lastActionTooltipShown = text == null ? " " : text);
		}
		
		public void showDefaultMessage() {
			setText(DEFAULT_MESSAGE);
		}
		
		public void showErrorMessage(String text) {
			setText(text);
			createMessageRemoverThread();
		}
		
		private void createMessageRemoverThread() {
			if (currentRemoverThread != null)
				currentRemoverThread.neutralize();
			(currentRemoverThread = new MessageRemoverThread()).start();
		}
		
		private class MessageRemoverThread extends Thread {
			private boolean stopped = false;
			
			public void neutralize() {
				stopped = true;
			}
		
			public void run() {
				try {
					Thread.sleep(MESSAGE_SHOWING_TIME);
				} catch (InterruptedException e) {}
				
				if (!stopped)
					showActionTooltip(lastActionTooltipShown);
			}
		}
	}
}
