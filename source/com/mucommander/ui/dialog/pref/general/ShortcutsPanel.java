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

package com.mucommander.ui.dialog.pref.general;

import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionKeymapIO;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
		super(parent, Translator.get("shortcuts_panel" + ".title"));
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
		
		add(createNorthPanel(), BorderLayout.NORTH);
		add(createCenterPanel(), BorderLayout.CENTER);
		add(createSouthPanel(), BorderLayout.SOUTH);
	}
	
	/**
	 * Returns a panel that contains combo-box of action categories which is used for filtering 
	 * the actions shown at the shortcuts editor table.
	 */
	private JPanel createNorthPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder());
		
		panel.add(createFilteringPanel(), BorderLayout.WEST);
		
		return panel;
	}

	/**
	 * Returns a panel that contains the shortcuts editor table.
	 */
	private JPanel createCenterPanel() {
		JPanel panel = new JPanel(new GridLayout(1,0));
		shortcutsTable.setPreferredColumnWidths(new double[] {0.6, 0.2, 0.2});
		panel.add(new JScrollPane(shortcutsTable));
		return panel;
	}
	
	/**
	 * Returns a panel that contain the tooltip bar and the shortcuts editor buttons below it.
	 */
	private JPanel createSouthPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel tooltipBarPanel = new JPanel(new BorderLayout());
		tooltipBarPanel.add(tooltipBar, BorderLayout.WEST);
		
		panel.add(tooltipBarPanel);
		panel.add(createButtonsPanel());
		
		return panel;
	}
	
	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		
		RemoveButton removeButton = new RemoveButton();	
		
		final JButton restoreDefaultButton = new JButton();
		restoreDefaultButton.setAction(new AbstractAction(Translator.get("shortcuts_panel" + ".restore_defaults")) {
			
			public void actionPerformed(ActionEvent e) {
				shortcutsTable.restoreDefaults();
			}
		});
		
		panel.add(removeButton);
		panel.add(restoreDefaultButton);
		
		return panel;
	}
	
	private JPanel createFilteringPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.add(new JLabel(Translator.get("shortcuts_panel" + ".show") + ":"));
		
		final JComboBox combo = new JComboBox();
		combo.addItem(ActionCategories.ALL);
	    for(ActionCategory category : ActionProperties.getActionCategories())
	      combo.addItem(category);
	    
	    combo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				final ActionCategory selectedActionCategory = (ActionCategory) combo.getSelectedItem();
				shortcutsTable.updateModel(new ShortcutsTable.ActionFilter() {
					@Override
                    public boolean accept(String actionId) {
						return selectedActionCategory.contains(actionId);
					}
				});
				tooltipBar.showDefaultMessage();
			}
	    });

	    combo.setSelectedIndex(0);
		
		panel.add(combo);
		
		return panel;
	}
	
	///////////////////////
    // PrefPanel methods //
    ///////////////////////
	
	@Override
    protected void commit() {
		shortcutsTable.commitChanges();
		ActionKeymapIO.setModified();
	}
	
	class TooltipBar extends JLabel {
		private String lastActionTooltipShown;
		private String DEFAULT_MESSAGE;
		private static final int MESSAGE_SHOWING_TIME = 3000;
		private MessageRemoverThread currentRemoverThread;
		
		public TooltipBar() {
			DEFAULT_MESSAGE = Translator.get("shortcuts_panel.default_message");
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
		
			@Override
            public void run() {
				try {
					Thread.sleep(MESSAGE_SHOWING_TIME);
				} catch (InterruptedException e) {}
				
				if (!stopped)
					showActionTooltip(lastActionTooltipShown);
			}
		}
	}
	
	private class RemoveButton extends JButton implements ListSelectionListener, TableModelListener {
		
		public RemoveButton() {
			setEnabled(false);
			setAction(new AbstractAction(Translator.get("remove")) {
				
				public void actionPerformed(ActionEvent e) {
					shortcutsTable.setValueAt(ShortcutsTable.DELETE, shortcutsTable.getSelectedRow(), shortcutsTable.getSelectedColumn());
					shortcutsTable.repaint();
					shortcutsTable.requestFocus();
				}
			});
			
			shortcutsTable.getSelectionModel().addListSelectionListener(this);
			shortcutsTable.getColumnModel().getSelectionModel().addListSelectionListener(this);
			shortcutsTable.getModel().addTableModelListener(this);
		}

		public void valueChanged(ListSelectionEvent e) {
			updateButtonState();
		}

		public void tableChanged(TableModelEvent e) {
			updateButtonState();			
		}
		
		private void updateButtonState() {
			int column = shortcutsTable.getSelectedColumn();
			int row = shortcutsTable.getSelectedRow();
			boolean canRemove = (column == ShortcutsTable.ACCELERATOR_COLUMN_INDEX || column == ShortcutsTable.ALTERNATE_ACCELERATOR_COLUMN_INDEX)
								&& row != -1 && shortcutsTable.getValueAt(shortcutsTable.getSelectedRow(), column) != null;
			setEnabled(canRemove);
		}
	}
}
