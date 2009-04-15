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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.pref.component.PrefTable;
import com.mucommander.ui.dialog.pref.general.ShortcutsPanel.TooltipBar;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

/**
 * 
 * @author Johann Schmitz (johann@j-schmitz.net), Arik Hadas
 */
public class ShortcutsTable extends PrefTable implements KeyListener, ListSelectionListener, FocusListener {

	// Column indexes
	private static final int ACTION_DESCRIPTION_COLUMN_INDEX     = 0;
	private static final int ACCELERATOR_COLUMN_INDEX            = 1;
	private static final int ALTERNATE_ACCELERATOR_COLUMN_INDEX  = 2;
	
	// Number of columns in the table
	private static final int NUM_OF_COLUMNS = 3;
	
	// Row number to action class map
	private HashMap rowToAction;
	
	// Row number to accelerator keystroke map
	private HashMap rowToAccelerator;
	
	// Row number to alternate accelerator keystroke map
	private HashMap rowToAlternateAccelerator;
	
	// Row number to action tooltip map
	private HashMap actionToTooltipText;
	
	// Set that holds the row indexes the user may have changed 
	private Set dirtyRows;
	
	// Private object used to indicate that a delete operation was made
	private final Object DELETE = new Object();
	
	// Saved copy of the table data, before any change was made by the user
	private Object[][] data;
	
	private TooltipBar tooltipBar;

	public ShortcutsTable(TooltipBar tooltipBar) {
		super();
		
		this.tooltipBar = tooltipBar;
		
		updateModel(new ActionFilter() {
			public boolean accept(MuAction action) {
				return true;
			}
		});
		
		getTableHeader().setReorderingAllowed(false);
		setRowSelectionAllowed(false);
		setAutoCreateColumnsFromModel(false);
		setCellSelectionEnabled(false);
		setColumnSelectionAllowed(false);
		setDragEnabled(false);		
		
		if (!usesTableHeaderRenderingProperties()) {
			getColumnModel().getColumn(ACTION_DESCRIPTION_COLUMN_INDEX).setHeaderRenderer(new CenteredTableHeaderRenderer());
			getColumnModel().getColumn(ACCELERATOR_COLUMN_INDEX).setHeaderRenderer(new CenteredTableHeaderRenderer());
			getColumnModel().getColumn(ALTERNATE_ACCELERATOR_COLUMN_INDEX).setHeaderRenderer(new CenteredTableHeaderRenderer());
		}

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		addKeyListener(this);
		addFocusListener(this);
		getSelectionModel().addListSelectionListener(this);
		getColumnModel().getSelectionModel().addListSelectionListener(this);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);
		// Selection might be changed so update to tooltip
		tooltipBar.showActionTooltip(getTooltipForRow(getSelectedRow()));
	}
	
	public void updateModel(ActionFilter filter) {
		dirtyRows = new HashSet();
		rowToAction = new HashMap();
		rowToAccelerator = new HashMap();
		rowToAlternateAccelerator = new HashMap();
		actionToTooltipText = new HashMap();

		setModel(new KeymapTableModel(copy(data = buildTableData(filter))));
	}
	
	private static boolean usesTableHeaderRenderingProperties() {
        return OsFamilies.MAC_OS_X.isCurrent() && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher() && JavaVersions.JAVA_1_5.isCurrentOrHigher();
    }	
	
	private boolean compareData() {
		Object[][] model = ((KeymapTableModel) getModel()).getData();
		int nbRows = data.length;
		for (int i=0; i<nbRows; i++)
			if (!Arrays.equals(data[i], model[i]))
				return false;
		return true;
	}

	public boolean hasChanged() { return !compareData(); }
	
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
		int row = e.getFirstRow();
		if (row >= 0)
			dirtyRows.add(Integer.valueOf(row));
	}
	
	private Object[][] copy(Object[][] data) {
		Object[][] c = new Object[data.length][data[0].length];
		for (int i=0; i<data.length; i++) {
			System.arraycopy(data[i], 0, c[i], 0, data[0].length);
		}
		return c;
	}
	
	public TableCellEditor getCellEditor(int row, int column) {
		return new KeyStrokeCellEditor(new RecordingKeyStrokeField((String) getValueAt(row, column)));
	}
	
	private void setActionName(MuAction action, int row, Object[][] model) {
		rowToAction.put(Integer.valueOf(row), action.getClass());
		model[row][ACTION_DESCRIPTION_COLUMN_INDEX] = action.getLabel();
	}

	private void setAccelerator(KeyStroke keyStroke, int row, Object[][] model) {
		if (keyStroke != null) {
			rowToAccelerator.put(Integer.valueOf(row), keyStroke);
			model[row][ACCELERATOR_COLUMN_INDEX] = MuAction.getKeyStrokeRepresentation(keyStroke);
		}
		else {
			rowToAccelerator.remove(Integer.valueOf(row));
			model[row][ACCELERATOR_COLUMN_INDEX] = null;
		}
	}
	
	private void setAlternativeAccelerator(KeyStroke keyStroke, int row, Object[][] model) {
		if (keyStroke != null) {
			rowToAlternateAccelerator.put(Integer.valueOf(row), keyStroke);
			model[row][ALTERNATE_ACCELERATOR_COLUMN_INDEX] = MuAction.getKeyStrokeRepresentation(keyStroke);
		}
		else {
			rowToAlternateAccelerator.remove(Integer.valueOf(row));
			model[row][ALTERNATE_ACCELERATOR_COLUMN_INDEX] = null;
		}
	}

	public void updateActions() {
		data = copy(((KeymapTableModel) getModel()).getData());
		Iterator iterator = dirtyRows.iterator();
		while(iterator.hasNext()) {
			Integer row = (Integer) iterator.next();
			ActionKeymap.changeActionAccelerators((Class) rowToAction.get(row), (KeyStroke) rowToAccelerator.get(row), 
					(KeyStroke) rowToAlternateAccelerator.get(row));
		}
		dirtyRows.clear();
	}
	
	/**
	 * Builds the table data based of all actions with their associated keystrokes
	 */
	private Object[][] buildTableData(ActionFilter filter) {
		// Get all action-classes from the action package
		AbstractFile actionPackageFile = ResourceLoader.getResourceAsFile((MuAction.class.getPackage().getName().replace('.', File.separatorChar)));

		Enumeration actionClasses = null;
		actionClasses = ActionManager.getActionClasses();

		final MainFrame mainFrame = WindowManager.getCurrentMainFrame();
		Vector actions = new Vector();
		// Convert the action-classes to MuAction instances
		while(actionClasses.hasMoreElements()) {
			MuAction action;
			if ((action = ActionManager.getActionInstance((Class) actionClasses.nextElement(), mainFrame)) != null
					&& filter.accept(action))
				actions.add(action);
		}

		// Sort actions by their labels
		Collections.sort(actions, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((MuAction) o1).getLabel().compareTo(((MuAction)o2).getLabel());
			}
		});

		// Build the table data
		Object[][] tableData = new Object[actions.size()][NUM_OF_COLUMNS];
		Iterator it = actions.iterator();
		int row = 0;		
		while (it.hasNext()) {
			MuAction action = (MuAction) it.next();
			if (action != null) {
				setActionName(action, row, tableData);
				setAccelerator(action.getAccelerator(), row, tableData);
				setAlternativeAccelerator(action.getAlternateAccelerator(), row, tableData);
				actionToTooltipText.put(action.getClass(), action.getToolTipText());
				++row;
			}			
		}

		return tableData;
	}
	
	public String getTooltipForRow(int row) {
		return (String) actionToTooltipText.get(rowToAction.get(Integer.valueOf(getSelectedRow())));
	}

	///////////////////////////
    // FocusListener methods //
    ///////////////////////////
	public void focusGained(FocusEvent e) {
		tooltipBar.showActionTooltip(getTooltipForRow(getSelectedRow()));
	}

	public void focusLost(FocusEvent e) { tooltipBar.clear(); }
	
	/////////////////////////////
	//// KeyListener methods ////
	/////////////////////////////
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_ENTER) {
			if (editCellAt(getSelectedRow(), getSelectedColumn()))
				getEditorComponent().requestFocusInWindow();
			else
				tooltipBar.showRegularKeystrokeMustBeAssignedFirstMsg();
			e.consume();
		}
		else if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
			setValueAt(DELETE, getSelectedRow(), getSelectedColumn());
			repaint();
			e.consume();
		}
		else if (keyCode != KeyEvent.VK_LEFT && keyCode != KeyEvent.VK_RIGHT && keyCode != KeyEvent.VK_UP &&
				keyCode != KeyEvent.VK_DOWN && keyCode != KeyEvent.VK_HOME  && keyCode != KeyEvent.VK_END &&
				keyCode != KeyEvent.VK_F2   && keyCode != KeyEvent.VK_ESCAPE)
			e.consume();
	}

	public void keyReleased(KeyEvent e) {}
	
	public void keyTyped(KeyEvent e) {}
	
	
	/**
	 * Helper Classes
	 */
	private class KeyStrokeCellEditor extends DefaultCellEditor implements TableCellEditor {
		
		private final int NUM_OF_CLICK_TO_EDIT = 2;
		
		RecordingKeyStrokeField rec;
		
		public KeyStrokeCellEditor(RecordingKeyStrokeField rec) {
			super(rec);
			this.rec = rec;
			rec.setSelectionColor(rec.getBackground());
			rec.setSelectedTextColor(rec.getForeground());
			rec.getDocument().addDocumentListener(new DocumentListener() {
				/**
				 * 
				 */
				public void insertUpdate(DocumentEvent e) {	stopCellEditing(); }
				public void changedUpdate(DocumentEvent e) {}
				public void removeUpdate(DocumentEvent e) {}
			});
			
			setClickCountToStart(NUM_OF_CLICK_TO_EDIT);
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int col) {
            return rec;
        }
 
        public Object getCellEditorValue() {
        	return rec.getLastKeyStroke();
        }
	}
	
	private class KeymapTableModel extends DefaultTableModel {	
		private Object[][] tableData = null;

		private KeymapTableModel(Object[][] data) {
			//TODO: use translator
			super(data, new String[] { "Action Descriptions", "Keystroke", "Alternative Keystroke" });
			this.tableData = data;
		}

		public Object[][] getData() { return tableData; }
		
		public boolean isCellEditable(int row, int column) {
			switch(column) {
			case 1:
				return true;
			case 2:
				return data[row][1] != null;
			default:
				return false;
			}			
		}
		
		public Object getValueAt(int row, int column) { return tableData[row][column]; }

		public void setValueAt(Object value, int row, int column) {
			// if no keystroke was pressed
			if (value == null)
				return;
			// if the user pressed a keystroke that is used to indicate a delete operation
			// should be made
			else if (value == DELETE)
				value = null;
				
			switch(column){
			case ACTION_DESCRIPTION_COLUMN_INDEX:
				setActionName((MuAction) value, row, tableData);
				break;
			case ACCELERATOR_COLUMN_INDEX:
				setAccelerator((KeyStroke) value, row, tableData);
				break;
			case ALTERNATE_ACCELERATOR_COLUMN_INDEX:
				setAlternativeAccelerator((KeyStroke) value, row, tableData);
				break;
			default:
				if (Debug.ON) { Debug.trace("No such column index: " + column); }
			}
			
			fireTableCellUpdated(row, column);
			
			if (Debug.ON) { Debug.trace("Value: " + value + ", row: " + row + ", col: " + column); }
		}
	}
	
	private class RecordingKeyStrokeField extends JTextField implements KeyListener {
		
		// The last KeyStroke that was entered to the field.
		private KeyStroke lastKeyStroke;
		
		public RecordingKeyStrokeField(String initialText) {
			// TODO translator
			super("Type in a shortcut");		
			
			setBorder(BorderFactory.createEmptyBorder());
			setHorizontalAlignment(JTextField.CENTER);
			setEditable(false);
			setBackground(Color.lightGray);			
			addKeyListener(this);
		}
		
		/**
		 * 
		 * @return the last KeyStroke the user entered to the field.
		 */
		public KeyStroke getLastKeyStroke() { return lastKeyStroke; }

		/////////////////////////////
		//// KeyListener methods ////
		/////////////////////////////		
		public void keyPressed(KeyEvent keyEvent) {
			if(Debug.ON) Debug.trace("keyModifiers="+keyEvent.getModifiers()+" keyCode="+keyEvent.getKeyCode());

	        int keyCode = keyEvent.getKeyCode();
	        if(keyCode==KeyEvent.VK_SHIFT || keyCode==KeyEvent.VK_CONTROL || keyCode==KeyEvent.VK_ALT || keyCode==KeyEvent.VK_META)
	            return;

	        KeyStroke pressedKeyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);
	        Class action = ActionKeymap.getRegisteredActionClassForKeystroke(pressedKeyStroke);
	        if (action == null || action.equals(rowToAction.get(Integer.valueOf(getSelectedRow())))) {
	        	lastKeyStroke = pressedKeyStroke;
	        	setText(MuAction.getKeyStrokeRepresentation(lastKeyStroke));
	        }
	        else {
	        	tooltipBar.showKeystrokeAlreadyInUseMsg(pressedKeyStroke, ActionManager.getActionInstance(action , WindowManager.getCurrentMainFrame()));
	        }
	        keyEvent.consume();
		}
			
		public void keyReleased(KeyEvent e) {e.consume();}

		public void keyTyped(KeyEvent e) {e.consume();}
	}
	
	private class CenteredTableHeaderRenderer extends JLabel implements TableCellRenderer {

		public CenteredTableHeaderRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
	        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		}
				
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {

        	// Configure the component with the specified value
            setText(value.toString());
    
            // Since the renderer is a component, return itself
            return this;
        }
    
        // The following methods override the defaults for performance reasons
        public void validate() {}
        public void revalidate() {}
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    }
	
	private class ComboRenderer extends JComboBox implements TableCellRenderer {

		ComboRenderer(ComboBoxModel model) {
			super(model);

			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setForeground(UIManager.getColor("TableHeader.foreground"));
			setEditable(false);
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, 
				boolean isSelected, boolean hasFocus, int row, int column) {
			setSelectedItem(value);
			return this;
		}
	}
	
	public static abstract class ActionFilter {
		public abstract boolean accept(MuAction action);
	}
}
