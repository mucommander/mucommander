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

import com.mucommander.Debug;
import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.dialog.pref.component.PrefTable;
import com.mucommander.ui.dialog.pref.general.ShortcutsPanel.TooltipBar;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.table.CellLabel;
import com.mucommander.ui.table.CenteredTableHeaderRenderer;
import com.mucommander.ui.text.KeyStrokeUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * This class is the table in which the actions and their shortcuts are
 * present in the ShortcutsPanel.
 * 
 * @author Arik Hadas, Johann Schmitz (johann@j-schmitz.net)
 */
public class ShortcutsTable extends PrefTable implements KeyListener, ListSelectionListener, FocusListener {

	/** Row index to action class map */
	private HashMap rowToAction;
	
	/** Row index to accelerator keystroke map */
	private HashMap rowToAccelerator;
	
	/** Row index to alternate accelerator keystroke map */
	private HashMap rowToAlternateAccelerator;
	
	/** Row index to action tooltip map */
	private HashMap rowToactionTooltip;
	
	/** Row index to action icon map */
	private HashMap rowToIcon;
	
	/** Base width and height of icons for a scale factor of 1 */
    private final static int BASE_ICON_DIMENSION = 16;
	
	/** Transparent icon used to align non-locked themes with the others. */
    private static ImageIcon transparentIcon = new ImageIcon(new BufferedImage(BASE_ICON_DIMENSION, BASE_ICON_DIMENSION, BufferedImage.TYPE_INT_ARGB));

    /** Background color of the {@link RecordingKeyStrokeField} */
    private static final Color RECORDING_KEYSTROKE_FIELD_BACKGROUND_COLOR = new Color(60, 126, 231);

    /** Foreground color of the {@link RecordingKeyStrokeField} */
    public static final Color RECORDING_KEYSTROKE_FIELD_FOREGROUND_COLOR = Color.white;

	/** Private object used to indicate that a delete operation was made */
	private final Object DELETE = new Object();

	/** Comparator of actions according to their labels */
	private static final Comparator ACTIONS_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			// TODO: remove actions without a standard label?
			if (MuAction.getStandardLabel((Class) o1) == null)
				return 1;
			if (MuAction.getStandardLabel((Class) o2) == null)
				return -1;
			return MuAction.getStandardLabel((Class) o1).compareTo(MuAction.getStandardLabel((Class) o2));
		}
	};
	
	/** Object that manage the state of the table (whether it was change, and which rows were modified) */
	private State state;
	
	/** Last selected row in the table */
	private int lastSelectedRow = -1;
	
	/** The bar below the table in which messages can be displayed */
	private TooltipBar tooltipBar;
	
	/** Number of mouse clicks required to enter cell's editing state */
	private static final int NUM_OF_CLICKS_TO_ENTER_EDITING_STATE = 2;
	
	/** Column indexes */
	private static final int ACTION_DESCRIPTION_COLUMN_INDEX     = 0;
	private static final int ACCELERATOR_COLUMN_INDEX            = 1;
	private static final int ALTERNATE_ACCELERATOR_COLUMN_INDEX  = 2;

	/** Number of columns in the table */
	private static final int NUM_OF_COLUMNS = 3;
	
	/** After the following time (msec) that cell is being in editing state 
	 *  and no pressing was made, the editing state is canceled */
	private static final int CELL_EDITING_STATE_PERIOD = 4000;
	
	/** Thread that cancel cell's editing state after CELL_EDITING_STATE_PERIOD time */
	private CancelEditingStateThread cancelEditingStateThread;

	private ShortcutsTableCellRenderer cellRenderer;
	
	public ShortcutsTable(TooltipBar tooltipBar) {
		super();

		this.tooltipBar = tooltipBar;
		
		updateModel(new ActionFilter() {
			public boolean accept(MuAction action) {
				return true;
			}
		});
		
		cellRenderer = new ShortcutsTableCellRenderer();
		setShowGrid(false);
		setIntercellSpacing(new Dimension(0,0));
		setRowHeight(Math.max(getRowHeight(), BASE_ICON_DIMENSION + 2 * CellLabel.CELL_BORDER_HEIGHT));
		getTableHeader().setReorderingAllowed(false);
		setRowSelectionAllowed(false);
		setAutoCreateColumnsFromModel(false);
		setCellSelectionEnabled(false);
		setColumnSelectionAllowed(false);
		setDragEnabled(false);		
		
		if (!usesTableHeaderRenderingProperties()) {
			CenteredTableHeaderRenderer renderer = new CenteredTableHeaderRenderer();
			getColumnModel().getColumn(ACTION_DESCRIPTION_COLUMN_INDEX).setHeaderRenderer(renderer);
			getColumnModel().getColumn(ACCELERATOR_COLUMN_INDEX).setHeaderRenderer(renderer);
			getColumnModel().getColumn(ALTERNATE_ACCELERATOR_COLUMN_INDEX).setHeaderRenderer(renderer);
		}

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		addKeyListener(this);
		addFocusListener(this);
		getSelectionModel().addListSelectionListener(this);
		getColumnModel().getSelectionModel().addListSelectionListener(this);
	}
	
	/**
	 * Create thread that will cancel the editing state of the given TableCellEditor
	 * after CELL_EDITING_STATE_PERIOD time in which with no pressing was made.
	 */
	public void createCancelEditingStateThread(TableCellEditor cellEditor) {
		if (cancelEditingStateThread != null)
			cancelEditingStateThread.neutralize();
		(cancelEditingStateThread = new CancelEditingStateThread(cellEditor)).start();
	}
	
	public TableCellRenderer getCellRenderer(int row, int column) {
		return cellRenderer;
	}
	
	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);
		// Selection might be changed, update tooltip
		int selectetRow = getSelectedRow();
		if (selectetRow == -1) // no row is selected
			tooltipBar.showDefaultMessage();
		else
			tooltipBar.showActionTooltip(getTooltipForRow(selectetRow));
	}
	
	public void updateModel(ActionFilter filter) {
		rowToAction = new HashMap();
		rowToAccelerator = new HashMap();
		rowToAlternateAccelerator = new HashMap();
		rowToactionTooltip = new HashMap();
		rowToIcon = new HashMap();

		setModel(new KeymapTableModel(createTableData(filter)));
	}

	/**
	 * Override this method so that calls for SetModel function outside this class
	 * won't get to setModel(KeymapTableModel model) function.
	 */
	public void setModel(TableModel model) {
		super.setModel(model);
	}
	
	public void setModel(KeymapTableModel model) {
		super.setModel(model);
		// the data in the table was changed- update the state object.
		state = new State(model.getData());
	}
	
	private static boolean usesTableHeaderRenderingProperties() {
        return OsFamilies.MAC_OS_X.isCurrent() && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher() && JavaVersions.JAVA_1_5.isCurrentOrHigher();
    }	
	
	public boolean hasChanged() { return state.isTableBeenModified(); }
	
	public TableCellEditor getCellEditor(int row, int column) {
		return new KeyStrokeCellEditor(new RecordingKeyStrokeField((String) getValueAt(row, column)));
	}
	
	private void setActionClass(Class action, int row) {
		rowToAction.put(Integer.valueOf(row), action);
	}

	private void setAccelerator(KeyStroke keyStroke, int row) {
		if (keyStroke != null)
			rowToAccelerator.put(Integer.valueOf(row), keyStroke);
		else
			rowToAccelerator.remove(Integer.valueOf(row));
	}
	
	private void setAlternativeAccelerator(KeyStroke keyStroke, int row) {
		if (keyStroke != null)
			rowToAlternateAccelerator.put(Integer.valueOf(row), keyStroke);
		else
			rowToAlternateAccelerator.remove(Integer.valueOf(row));
	}

	/**
	 * This method updates ActionKeymap with the modified shortcuts.
	 */
	public void updateActions() {
		Iterator iterator = state.getDirtyRowsIterator();
		while(iterator.hasNext()) {
			Integer row = (Integer) iterator.next();
			ActionKeymap.changeActionAccelerators((Class) rowToAction.get(row),
												  (KeyStroke) rowToAccelerator.get(row), 
												  (KeyStroke) rowToAlternateAccelerator.get(row));
		}
		// After the update above, the table contains up-to-date data - clear the dirty rows indexes
		state.resetDirtyRows();
	}
	
	/**
	 * Builds the table data based of all actions with their associated keystrokes
	 */
	private Object[][] createTableData(ActionFilter filter) {
		Enumeration actionClasses = ActionManager.getActionClasses();
		
		// Convert the action-classes to MuAction instances
		List list = Collections.list(actionClasses);
		
		// Sort actions by their labels
		Collections.sort(list, ACTIONS_COMPARATOR);

		// Build the table data
		Object[][] tableData = new Object[list.size()][NUM_OF_COLUMNS];
		for (int row = 0; row < list.size(); ++row) {
			Class actionClass = (Class) list.get(row);

			ImageIcon actionIcon = MuAction.getStandardIcon(actionClass);
			if (actionIcon == null)
				actionIcon = transparentIcon;			
			rowToIcon.put(Integer.valueOf(row), IconManager.getPaddedIcon(actionIcon, new Insets(0, 4, 0, 4)));
			
			String actionLabel = MuAction.getStandardLabel(actionClass);
			setActionClass(actionClass, row);
			tableData[row][ACTION_DESCRIPTION_COLUMN_INDEX] = actionLabel;
			
			KeyStroke accelerator = ActionKeymap.getAccelerator(actionClass);
			setAccelerator(accelerator, row);
			tableData[row][ACCELERATOR_COLUMN_INDEX] = KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(accelerator);
			
			KeyStroke alternativeAccelerator = ActionKeymap.getAlternateAccelerator(actionClass);
			setAlternativeAccelerator(alternativeAccelerator, row);
			tableData[row][ALTERNATE_ACCELERATOR_COLUMN_INDEX] = KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(alternativeAccelerator);
			
			String actionTooltip = MuAction.getStandardTooltip(actionClass);
			if (actionTooltip == null)
				actionTooltip = MuAction.getStandardLabel(actionClass);
			rowToactionTooltip.put(actionClass, actionTooltip);
		
		}

		return tableData;
	}
	
	private String getTooltipForRow(int row) {
		return (String) rowToactionTooltip.get(rowToAction.get(Integer.valueOf(getSelectedRow())));
	}

	///////////////////////////
    // FocusListener methods //
    ///////////////////////////
	
	public void focusGained(FocusEvent e) {
		int currentSelectedRow = getSelectedRow();
		if (lastSelectedRow != currentSelectedRow)
			tooltipBar.showActionTooltip(getTooltipForRow(currentSelectedRow));
		lastSelectedRow = currentSelectedRow;
	}

	public void focusLost(FocusEvent e) { }
	
	/////////////////////////////
	//// KeyListener methods ////
	/////////////////////////////
	
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_ENTER) {
			if (editCellAt(getSelectedRow(), getSelectedColumn()))
				getEditorComponent().requestFocusInWindow();
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
	
	public static abstract class ActionFilter {
		public abstract boolean accept(MuAction action);
	}
	
	/**
	 * Helper Classes
	 */
	private class KeyStrokeCellEditor extends DefaultCellEditor implements TableCellEditor {
		
		RecordingKeyStrokeField rec;
		
		public KeyStrokeCellEditor(RecordingKeyStrokeField rec) {
			super(rec);
			this.rec = rec;
			rec.setSelectionColor(rec.getBackground());
			rec.setSelectedTextColor(rec.getForeground());
			rec.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent e) {
					// quit editing state after text is written to the text field.
					stopCellEditing();
				}
				public void changedUpdate(DocumentEvent e) {}
				public void removeUpdate(DocumentEvent e) {}
			});
			
			setClickCountToStart(NUM_OF_CLICKS_TO_ENTER_EDITING_STATE);
			
			createCancelEditingStateThread(this);
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int col) {
            return rec;
        }
 
        public Object getCellEditorValue() {
        	return rec.getLastKeyStroke();
        }
	}
	
	private class CancelEditingStateThread extends Thread {
		private boolean stopped = false;
		private TableCellEditor cellEditor;

		public CancelEditingStateThread(TableCellEditor cellEditor) {
			this.cellEditor = cellEditor;
		}
		
		public void neutralize() {
			stopped = true;
		}
		
		public void run() {
        	try {
				Thread.sleep(CELL_EDITING_STATE_PERIOD);
			} catch (InterruptedException e) {}
			
			if (!stopped && cellEditor != null)
				cellEditor.stopCellEditing();
        }
	}
	
	private class KeymapTableModel extends DefaultTableModel {	
		private Object[][] tableData = null;

		private KeymapTableModel(Object[][] data) {
			//TODO: use translator
			super(data, new String[] {"Action Description", "Shortcut", "Alternate Shortcut"});
			this.tableData = data;
		}

		public Object[][] getData() { return tableData; }
		
		public boolean isCellEditable(int row, int column) {
			switch(column) {
			case ACTION_DESCRIPTION_COLUMN_INDEX:
				return false;
			case ACCELERATOR_COLUMN_INDEX:
			case ALTERNATE_ACCELERATOR_COLUMN_INDEX:
				return true;
			default:
				return false;
			}
		}
		
		public Object getValueAt(int row, int column) { return tableData[row][column]; }

		public void setValueAt(Object value, int row, int column) {
			// if no keystroke was pressed
			if (value == null)
				return;
			// if the user pressed a keystroke that is used to indicate 
			// a delete operation should be made
			else if (value == DELETE)
				value = null;
				
			KeyStroke typedKeyStroke = (KeyStroke) value;
			switch(column){
			case ACCELERATOR_COLUMN_INDEX:
				setAccelerator(typedKeyStroke, row);
				tableData[row][column] = KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(typedKeyStroke);
				break;
			case ALTERNATE_ACCELERATOR_COLUMN_INDEX:
				setAlternativeAccelerator(typedKeyStroke, row);
				tableData[row][column] = KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(typedKeyStroke);
				break;
			default:
				if (Debug.ON) { Debug.trace("Unexpected column index: " + column); }
			}

			state.rowHasBeenUpdated(row);

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
			setBackground(RECORDING_KEYSTROKE_FIELD_BACKGROUND_COLOR);
			setForeground(RECORDING_KEYSTROKE_FIELD_FOREGROUND_COLOR);
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
	        boolean isAlternativeActionCellSelected = getSelectedColumn() == ALTERNATE_ACCELERATOR_COLUMN_INDEX;
	        boolean isKeyStrokeRegisteredToAlternatuveAction = false;
	        boolean isAlreadyExist = rowToAccelerator.containsValue(pressedKeyStroke) || 
	        						 (isKeyStrokeRegisteredToAlternatuveAction = rowToAlternateAccelerator.containsValue(pressedKeyStroke));

	        if (isAlreadyExist) {
	        	// Will contain the index of the row in which the keystroke is already located
	        	int row;
	        	// Represent the above index as an Integer
	        	Integer rowAsInteger = null;
	        	
	        	int nbRows = getRowCount();
	        	// Search for the row that contains the keystroke
	        	for (row=0; row<nbRows; ++row) {
	        		rowAsInteger = Integer.valueOf(row);
	        		// If accelerator or alternative accelerator at row i is equal to the
	        		// pressed keystroke, stop the search (row field holds index i)
	        		if (pressedKeyStroke.equals(rowToAccelerator.get(rowAsInteger)) ||
	        			pressedKeyStroke.equals(rowToAlternateAccelerator.get(rowAsInteger)))
	        			break;
	        	}
	        	
	        	if (isAlternativeActionCellSelected != isKeyStrokeRegisteredToAlternatuveAction || row != getSelectedRow()) {
	        		String errorMessage = "The shortcut [" + KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(pressedKeyStroke)
	    			+ "] is already assigned to '" + ActionManager.getActionInstance((Class) rowToAction.get(rowAsInteger)).getLabel() + "'";
	        		tooltipBar.showErrorMessage(errorMessage);
	        		createCancelEditingStateThread(getCellEditor());
	        	}
	        	else {
	        		TableCellEditor activeCellEditor = getCellEditor();
	        		if (activeCellEditor!= null)
	        			activeCellEditor.stopCellEditing();
	        	}
	        }
	        else {
	        	lastKeyStroke = pressedKeyStroke;
	        	setText(KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(lastKeyStroke));
	        }
	        keyEvent.consume();
		}
			
		public void keyReleased(KeyEvent e) {e.consume();}

		public void keyTyped(KeyEvent e) {e.consume();}
	}
	
	/**
	 * This class tracks the table's state - whether it was changed and which rows were modified.
	 */
	private class State {
		/** Saved copy of the table data, before any change was made by the user */
		private Object[][] originalTableData;
		/** Set that holds the row indexes the user may have changed */
		private Set dirtyRows;
		
		/**
		 * @param tableData - table's data before any modification was made.
		 */
		public State(Object[][] tableData) {
			dirtyRows = new HashSet();
			int nbRows = tableData.length;
			int nbCol = tableData[0].length;
			originalTableData = new Object[nbRows][nbCol];
			for (int i=0; i<nbRows; ++i)
				for (int j=0; j<nbCol; ++j)
					originalTableData[i][j] = tableData[i][j];
		}
		
		/**
		 * This method is called to inform that a change was made to one of the table's rows.
		 * 
		 * @param row - row's number in the table.
		 */
		public void rowHasBeenUpdated(int row) {
			Object[][] currentTableData = ((KeymapTableModel) getModel()).getData();
			// if the row's data (accelerator + alternative accelerator) is identical to 
			// its original data (=> the row is not modified), then remove it from the
			// set of dirty rows. otherwise, add the row to the set.
			if (equals(originalTableData[row][ACCELERATOR_COLUMN_INDEX], currentTableData[row][ACCELERATOR_COLUMN_INDEX]) &&
				equals(originalTableData[row][ALTERNATE_ACCELERATOR_COLUMN_INDEX], currentTableData[row][ALTERNATE_ACCELERATOR_COLUMN_INDEX]))
				dirtyRows.remove(Integer.valueOf(row));
			else
				dirtyRows.add(Integer.valueOf(row));
		}

		/**
		 * @return true if the table was modified such that its current data is 
		 * 		   different from the saved data, else otherwise.
		 */
		public boolean isTableBeenModified() { return !dirtyRows.isEmpty(); }
		
		/**
		 * @return Iterator that points to the indexes of the rows in the table
		 *			which contain data that is different from their original data.
		 */
		public Iterator getDirtyRowsIterator() { return dirtyRows.iterator(); }
		
		/**
		 * Clear the set of dirty rows indexes.
		 */
		public void resetDirtyRows() { dirtyRows.clear(); }
		
		/**
		 * helper function to compare two Objects for equality, that supports null values.
		 */
		private boolean equals(Object first, Object second) {
			if (first == null)
				return second == null;
			return first.equals(second);
		}
	}
	
	private class ShortcutsTableCellRenderer implements TableCellRenderer {
		/** Custom JLabel that render specific column cells */
	    private CustomCellLabel[] cellLabels = new CustomCellLabel[NUM_OF_COLUMNS];
	    
	    private final Color REGULAR_CELL_BACKGROUND_COLOR = Color.white;
	    private final Color ALTERNATE_CELL_BACKGROUND_COLOR = new Color(238, 238, 238);
	    private final Color OUTLINE_COLOR = Color.gray;
	    
	    public ShortcutsTableCellRenderer() {
	    	for(int i=0; i<NUM_OF_COLUMNS; ++i)
	            cellLabels[i] = new CustomCellLabel();

	    	cellLabels[ACTION_DESCRIPTION_COLUMN_INDEX].setHorizontalAlignment(CellLabel.LEFT);
	    	cellLabels[ACCELERATOR_COLUMN_INDEX].setHorizontalAlignment(CellLabel.CENTER);
	    	cellLabels[ALTERNATE_ACCELERATOR_COLUMN_INDEX].setHorizontalAlignment(CellLabel.CENTER);
	    }
		
		public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
			CustomCellLabel label;
			int       columnId;
			
			columnId = convertColumnIndexToModel(vColIndex);
			label = cellLabels[columnId];
			
			// action's icon column: return ImageIcon instance
			if(columnId == ACTION_DESCRIPTION_COLUMN_INDEX) {
				label.setIcon((ImageIcon) rowToIcon.get(Integer.valueOf(convertRowIndexToModel(rowIndex))));
				label.setText("" + (value!=null ? value : ""));
			}
			// Any other column
			else {
				String text = value == null ? "" : (String) value;
				
				// If component's preferred width is bigger than column width then the component is not entirely
	            // visible so we set a tooltip text that will display the whole text when mouse is over the 
	            // component
	            if (table.getColumnModel().getColumn(vColIndex).getWidth() < label.getPreferredSize().getWidth())
	                label.setToolTipText(text);
	            // Have to set it to null otherwise the defaultRender sets the tooltip text to the last one
	            // specified
	            else
	                label.setToolTipText(null);
	            
	            // Set label's text
				label.setText(text);
			}
			
			label.setOutline(hasFocus ? OUTLINE_COLOR : null);
			
			// set cell's background color
			label.setBackground(rowIndex % 2 == 0 ? REGULAR_CELL_BACKGROUND_COLOR : ALTERNATE_CELL_BACKGROUND_COLOR);
			
			return label;
		}
	}
	
	/**
	 * CellLabel with a different outline than the default outline.
	 */
	private class CustomCellLabel extends CellLabel {
		
		protected void paintOutline(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
        	g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER,
        			2.0f, new float[]{2.0f}, 0));
        	g2.setColor(outlineColor);

        	g2.drawLine(0, 0, getWidth(), 0);
        	g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        	g2.drawLine(0, 0, 0, getHeight() - 1);
        	g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight() - 1);
		}
	}
}
