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

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import sun.swing.table.DefaultTableCellHeaderRenderer;

import com.mucommander.Debug;
import com.mucommander.extension.ClassFilter;
import com.mucommander.extension.ClassFinder;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.pref.component.PrefTable;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

/**
 * 
 * @author Johann Schmitz (johann@j-schmitz.net), Arik Hadas
 */
public class KeymapTable extends PrefTable implements KeyListener  {

	// Cell renderer for the action-name column cells
	private static final DefaultTableCellRenderer ACTION_NAME_RENDERER = new ActionCellRenderer();
	
	// Column indexes
	private static final int ACTION_DESCRIPTION_COLUMN_INDEX     = 0;
	private static final int ACCELERATOR_COLUMN_INDEX            = 1;
	private static final int ALTERNATE_ACCELERATOR_COLUMN_INDEX  = 2;
	
	// Number of columns in the table
	private static final int NUM_OF_COLUMNS = 3;
	
	private HashMap rowToAction;
	private HashMap rowToAccelerator;
	private HashMap rowToAlternateAccelerator;
	
	// Set that holds the row indexes the user may have changed 
	private Set touchedRows;
	
	// Private object used to indicate that a delete operation was made
	private final Object DELETE = new Object();
	
	// Saved copy of the table data, before any change was made by the user
	private Object[][] data;

	public KeymapTable() {
		super();
		
		touchedRows = new HashSet();
		rowToAction = new HashMap();
		rowToAccelerator = new HashMap();
		rowToAlternateAccelerator = new HashMap();
		data = buildTableData();
		
		setModel(new KeymapTableModel(copy(data)));

		getTableHeader().setReorderingAllowed(false);
		setRowSelectionAllowed(false);
		setAutoCreateColumnsFromModel(false);
		setCellSelectionEnabled(false);
		setColumnSelectionAllowed(false);
		setDragEnabled(false);
		getTableHeader().setDefaultRenderer(new DefaultTableCellHeaderRenderer() {
			{
				setHorizontalAlignment(SwingConstants.CENTER);
			}
		});

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		addKeyListener(this);
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
			touchedRows.add(Integer.valueOf(row));
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
	
	public TableCellRenderer getCellRenderer(int row, int col) {
		if (col == ACTION_DESCRIPTION_COLUMN_INDEX)
			return ACTION_NAME_RENDERER;
		return new DefaultTableCellRenderer();
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
	
	public void updateActions() {
		data = copy(((KeymapTableModel) getModel()).getData());
		Iterator iterator = touchedRows.iterator();
		while(iterator.hasNext()) {
			Integer row = (Integer) iterator.next();
			ActionKeymap.changeActionAccelerators((Class) rowToAction.get(row), (KeyStroke) rowToAccelerator.get(row), 
					(KeyStroke) rowToAlternateAccelerator.get(row));
		}
		touchedRows.clear();
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
	
	/**
	 * Builds the table data based of all actions with their associated keystrokes
	 */
	private Object[][] buildTableData() {
		// Get all action-classes from the action package
		AbstractFile actionPackageFile = ResourceLoader.getResourceAsFile((MuAction.class.getPackage().getName().replace('.', '/')));

		Vector actions = null;
		try {
			actions = new ClassFinder().find(actionPackageFile, new ClassFilter() {
				public boolean accept(Class c) {
					return !c.isInterface() && !c.isAnonymousClass() 
						&& !(Modifier.toString(c.getModifiers()).toLowerCase().contains("abstract")) && MuAction.class.isAssignableFrom(c);
				}
			});
		} catch (IOException e) {
			if (Debug.ON) { Debug.trace(e); }
			actions = new Vector();
		}

		final MainFrame mainFrame = WindowManager.getCurrentMainFrame();
		// Convert the action-classes to MuAction instances
		int nbClasses = actions.size();
		for (int i=0; i<nbClasses; ++i) {
			MuAction action;
			try {				
				if ((action = ActionManager.getActionInstance((Class) actions.remove(0), mainFrame)) != null)
					actions.add(action);
			}
			// Ignore exceptions - move on
			catch(Exception e) {  }
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
				++row;
			}			
		}

		return tableData;
	}
	
	
	/////////////////////////////
	//// KeyListener methods ////
	/////////////////////////////
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_ENTER) {				
			editCellAt(getSelectedRow(), getSelectedColumn());				
			getEditorComponent().requestFocusInWindow();
			e.consume();
		}
		else if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE /*|| keyCode == KeyEvent.VK_ESCAPE*/) {
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
		RecordingKeyStrokeField rec;
		
		public KeyStrokeCellEditor(RecordingKeyStrokeField rec) {
			super(rec);
			this.rec = rec;
			rec.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {}
				public void insertUpdate(DocumentEvent e) {	stopCellEditing(); }
				public void removeUpdate(DocumentEvent e) {}
			});
			
			setClickCountToStart(2);
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int col) {
            return rec;
        }
 
        public Object getCellEditorValue() {
        	return rec.getLastKeyStroke();
        }
	}
	
	private static class ActionCellRenderer extends DefaultTableCellRenderer {
		public ActionCellRenderer() {
			super();
			setHorizontalAlignment(SwingConstants.LEFT);
			setOpaque(true);
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setForeground(UIManager.getColor("TableHeader.foreground"));
			setBackground(UIManager.getColor("TableHeader.background"));
			setFont(UIManager.getFont("TableHeader.font"));
		}
	}
	
	private class KeymapTableModel extends DefaultTableModel {	
		private Object[][] tableData = null;

		private KeymapTableModel(Object[][] data) {
			//TODO: use translator
			super(data, new String[] { "Action Description", "Keystroke", "Alternative Keystroke" });
			this.tableData = data;
		}

		public Object[][] getData() { return tableData; }
		
		public boolean isCellEditable(int row, int column) { return column > 0; }
		
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
				if (Debug.ON) { Debug.trace("ERROR1"); }
			}
			
			fireTableCellUpdated(row, column);
			
			if (Debug.ON) { Debug.trace("Value: " + value + ", row: " + row + ", col: " + column); }
		}
	}
	
	private class RecordingKeyStrokeField extends JTextField implements KeyListener {
		
		// The last KeyStroke that was entered to the field.
		private KeyStroke lastKeyStroke;
		
		public RecordingKeyStrokeField(String initialText) {
			super(initialText);		
			
			// Make caret invisible
			setCaretColor(getBackground());
			
			this.addKeyListener(this);
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
	        if (!ActionKeymap.isKeyStrokeRegistered(pressedKeyStroke)) {
	        	lastKeyStroke = pressedKeyStroke;
	        	setText(MuAction.getKeyStrokeRepresentation(lastKeyStroke));
	        }
	        keyEvent.consume();
		}
			
		public void keyReleased(KeyEvent e) {e.consume();}

		public void keyTyped(KeyEvent e) {e.consume();}
	}
}
