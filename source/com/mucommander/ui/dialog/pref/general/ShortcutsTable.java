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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.mucommander.AppLogger;
import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.dialog.pref.component.PrefTable;
import com.mucommander.ui.dialog.pref.general.ShortcutsPanel.TooltipBar;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.table.CellLabel;
import com.mucommander.ui.table.CenteredTableHeaderRenderer;
import com.mucommander.ui.text.KeyStrokeUtils;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeCache;
import com.mucommander.ui.theme.ThemeListener;

/**
 * This class is the table in which the actions and their shortcuts are
 * present in the ShortcutsPanel.
 * 
 * @author Arik Hadas, Johann Schmitz (johann@j-schmitz.net)
 */
public class ShortcutsTable extends PrefTable implements KeyListener, ListSelectionListener, FocusListener {
	
	/** Base width and height of icons for a scale factor of 1 */
    private final static int BASE_ICON_DIMENSION = 16;
	
	/** Transparent icon used to align non-locked themes with the others. */
    private static ImageIcon transparentIcon = new ImageIcon(new BufferedImage(BASE_ICON_DIMENSION, BASE_ICON_DIMENSION, BufferedImage.TYPE_INT_ARGB));

	/** Private object used to indicate that a delete operation was made */
	public static final Object DELETE = new Object();
	
	private ShortcutsTableData data;

	/** Comparator of actions according to their labels */
	private static final Comparator ACTIONS_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			// TODO: remove actions without a standard label?
			String label1 = ActionProperties.getActionLabel((String) o1);
			if (label1 == null)
				return 1;
			
			String label2 = ActionProperties.getActionLabel((String) o2);
			if (label2 == null)
				return -1;
			
			return label1.compareTo(label2);
		}
	};
	
	/** Object that manage the state of the table (whether it was change, and which rows were modified) */
	private ShortcutsTableState state;
	
	/** Last selected row in the table */
	private int lastSelectedRow = -1;
	
	/** The bar below the table in which messages can be displayed */
	private TooltipBar tooltipBar;
	
	/** Number of mouse clicks required to enter cell's editing state */
	private static final int NUM_OF_CLICKS_TO_ENTER_EDITING_STATE = 2;
	
	/** Column indexes */
	public static final int ACTION_DESCRIPTION_COLUMN_INDEX     = 0;
	public static final int ACCELERATOR_COLUMN_INDEX            = 1;
	public static final int ALTERNATE_ACCELERATOR_COLUMN_INDEX  = 2;

	/** Number of columns in the table */
	private static final int NUM_OF_COLUMNS = 3;
	
	/** After the following time (msec) that cell is being in editing state 
	 *  and no pressing was made, the editing state is canceled */
	private static final int CELL_EDITING_STATE_PERIOD = 3000;
	
	/** Thread that cancel cell's editing state after CELL_EDITING_STATE_PERIOD time */
	private CancelEditingStateThread cancelEditingStateThread;

	private ShortcutsTableCellRenderer cellRenderer;
	
	public ShortcutsTable(TooltipBar tooltipBar) {
		super();
		this.tooltipBar = tooltipBar;
		
		updateModel();
		
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
     * Paints a dotted border of the specified width, height and {@link Color}, and using the given {@link Graphics}
     * object.
     *
     * @param g Graphics object to use for painting
     * @param width border width
     * @param height border height
     * @param color border color
     */
    private static void paintDottedBorder(Graphics g, int width, int height, Color color) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER,
                2.0f, new float[]{2.0f}, 0));
        g2.setColor(color);

        g2.drawLine(0, 0, width, 0);
        g2.drawLine(0, height - 1, width, height - 1);
        g2.drawLine(0, 0, 0, height - 1);
        g2.drawLine(width-1, 0, width-1, height - 1);
    }

    private static boolean usesTableHeaderRenderingProperties() {
        return OsFamilies.MAC_OS_X.isCurrent() && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher() && JavaVersions.JAVA_1_5.isCurrentOrHigher();
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
			tooltipBar.showActionTooltip(data.getCurrentTooltip());
	}
	
	public void updateModel() {
		updateModel(new ActionFilter() {
			public boolean accept(String actionId) {
				return true;
			}
		});
	}
	
	public void updateModel(ActionFilter filter) {
		data = new ShortcutsTableData(filter);
		setModel(new KeymapTableModel(data));
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
		state = new ShortcutsTableState(model.getData());
	}
	
	public boolean hasChanged() { return state.isTableBeenModified(); }
	
	public TableCellEditor getCellEditor(int row, int column) {
		return new KeyStrokeCellEditor(new RecordingKeyStrokeField(KeyStrokeUtils.getKeyStrokeDisplayableRepresentation((KeyStroke) getValueAt(row, column))));
	}
	
	/**
	 * This method updates ActionKeymap with the modified shortcuts.
	 */
	public void updateActions() {
		Iterator iterator = state.getDirtyRowsIterator();
		while(iterator.hasNext()) {
			int row = ((Integer) iterator.next()).intValue();
			ActionKeymap.changeActionAccelerators(data.getActionId(row),
												  data.getAccelerator(row), 
												  data.getAltAccelerator(row));
		}
		// After the update above, the table contains up-to-date data - clear the dirty rows indexes
		state.resetDirtyRows();
	}
	
	///////////////////////////
    // FocusListener methods //
    ///////////////////////////
	
	public void focusGained(FocusEvent e) {
		int currentSelectedRow = getSelectedRow();
		if (lastSelectedRow != currentSelectedRow)
			tooltipBar.showActionTooltip(data.getCurrentTooltip());
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
		public abstract boolean accept(String actionId);
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
		private ShortcutsTableData data = null;

		private KeymapTableModel(ShortcutsTableData data) {
			//TODO: use translator
			super(data.getTableData(), new String[] {"Action Description", "Shortcut", "Alternate Shortcut"});
			this.data = data;
		}

		public Object[][] getData() { return data.getTableData(); }
		
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
		
		public Object getValueAt(int row, int column) {
			switch(column) {
			case ACTION_DESCRIPTION_COLUMN_INDEX:
				return data.getTableData(row, column);
			case ACCELERATOR_COLUMN_INDEX:
			case ALTERNATE_ACCELERATOR_COLUMN_INDEX:
//				return KeyStrokeUtils.getKeyStrokeDisplayableRepresentation((KeyStroke) data.getTableData(row, column));
				return (KeyStroke) data.getTableData(row, column);
			}
			return data.getTableData(row, column);
		}

		public void setValueAt(Object value, int row, int column) {
			// if no keystroke was pressed
			if (value == null)
				return;
			// if the user pressed a keystroke that is used to indicate a delete operation should be made
			else if (value == DELETE)
				value = null;
				
			KeyStroke typedKeyStroke = (KeyStroke) value;
			switch(column){
			case ACCELERATOR_COLUMN_INDEX:
				data.setAccelerator(typedKeyStroke, row);
				break;
			case ALTERNATE_ACCELERATOR_COLUMN_INDEX:
				data.setAlternativeAccelerator(typedKeyStroke, row);
				break;
			default:
				AppLogger.fine("Unexpected column index: " + column);
			}

			state.rowHasBeenUpdated(row);

			fireTableCellUpdated(row, column);
			
			AppLogger.finest("Value: " + value + ", row: " + row + ", col: " + column);
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
			setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED]);
			setForeground(ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED][ThemeCache.PLAIN_FILE]);
			addKeyListener(this);
		}

		/**
		 * 
		 * @return the last KeyStroke the user entered to the field.
		 */
		public KeyStroke getLastKeyStroke() { return lastKeyStroke; }


        ////////////////////////
        // Overridden methods //
        ////////////////////////

        protected void paintBorder(Graphics g) {
            paintDottedBorder(g, getWidth(), getHeight(), ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL]);
        }

		/////////////////////////////
		//// KeyListener methods ////
		/////////////////////////////

		public void keyPressed(KeyEvent keyEvent) {
			AppLogger.finest("keyModifiers="+keyEvent.getModifiers()+" keyCode="+keyEvent.getKeyCode());

	        int keyCode = keyEvent.getKeyCode();
	        if(keyCode==KeyEvent.VK_SHIFT || keyCode==KeyEvent.VK_CONTROL || keyCode==KeyEvent.VK_ALT || keyCode==KeyEvent.VK_META)
	            return;

	        KeyStroke pressedKeyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);
	        
	        boolean isKeyStrokeRegisteredToAlternatuveAction = false;
	        boolean isAlreadyExist = data.containsAccelerator(pressedKeyStroke) || 
	        						 (isKeyStrokeRegisteredToAlternatuveAction = data.containsAlternativeAccelerator(pressedKeyStroke));

	        if (isAlreadyExist) {
	        	// Will contain the index of the row in which the keystroke is already located
	        	int row;
	        	
	        	int nbRows = getRowCount();
	        	// Search for the row that contains the keystroke
	        	for (row=0; row<nbRows; ++row) {
	        		// If accelerator or alternative accelerator at row i is equal to the
	        		// pressed keystroke, stop the search (row field holds index i)
	        		if (pressedKeyStroke.equals(data.getAccelerator(row)) ||
	        			pressedKeyStroke.equals(data.getAltAccelerator(row)))
	        			break;
	        	}
	        
	        	boolean isAlternativeActionCellSelected = getSelectedColumn() == ALTERNATE_ACCELERATOR_COLUMN_INDEX;
	        	if (isAlternativeActionCellSelected != isKeyStrokeRegisteredToAlternatuveAction || row != getSelectedRow()) {
	        		String errorMessage = "The shortcut [" + KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(pressedKeyStroke)
	    			+ "] is already assigned to '" + data.getActionDescription(row).text + "'";
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
	
	private class ShortcutsTableData {
		private Object[][] data;
		private String[] actionIds;
		private String[] descriptions;
		
		public ShortcutsTableData(ActionFilter filter) {
			List filteredActionIds = filter(ActionManager.getActionIds(), filter);
			// Sort actions by their labels
			Collections.sort(filteredActionIds, ACTIONS_COMPARATOR);

			// Build the table data
			int nbRows = filteredActionIds.size();
			
			actionIds = new String[nbRows];
			descriptions = new String[nbRows];
			data = new Object[nbRows][NUM_OF_COLUMNS];
			
			for (int i = 0; i < nbRows; ++i) {
				String actionId = (String) filteredActionIds.get(i);
				actionIds[i] = actionId;
				ActionDescriptor actionDescriptor = ActionProperties.getActionDescriptor(actionId);
				
				ImageIcon actionIcon = actionDescriptor.getIcon();
				if (actionIcon == null)
					actionIcon = transparentIcon;			
				
				String actionLabel = actionDescriptor.getLabel();
				data[i][ACTION_DESCRIPTION_COLUMN_INDEX] = new ActionDescription(IconManager.getPaddedIcon(actionIcon, new Insets(0, 4, 0, 4)), actionLabel);
				
				KeyStroke accelerator = ActionKeymap.getAccelerator(actionId);
				setAccelerator(accelerator, i);
				
				KeyStroke alternativeAccelerator = ActionKeymap.getAlternateAccelerator(actionId);
				setAlternativeAccelerator(alternativeAccelerator, i);
				
				descriptions[i] = actionDescriptor.getDescription();
			}
		}
		
		public Object[][] getTableData() { return data; }
		
		public Object getTableData(int row, int col) { return data[row][col]; }
		
		public String getCurrentTooltip() { return descriptions[getSelectedRow()]; }
		
		public String getActionId(int row) { return (String) actionIds[row]; }
		
		public KeyStroke getAccelerator(int row) { return (KeyStroke) data[row][ACCELERATOR_COLUMN_INDEX]; }
		
		public KeyStroke getAltAccelerator(int row) { return (KeyStroke) data[row][ALTERNATE_ACCELERATOR_COLUMN_INDEX]; }
		
		public ActionDescription getActionDescription(int row) { return (ActionDescription) data[row][ACTION_DESCRIPTION_COLUMN_INDEX]; }
		
		public boolean containsAccelerator(KeyStroke accelerator) {
			int nbRows = data.length;
			for (int i=0; i<nbRows; ++i)
				if (accelerator.equals(data[i][ACCELERATOR_COLUMN_INDEX]))
					return true;
			return false;
		}
		
		public boolean containsAlternativeAccelerator(KeyStroke altAccelerator) {
			int nbRows = data.length;
			for (int i=0; i<nbRows; ++i)
				if (altAccelerator.equals(data[i][ALTERNATE_ACCELERATOR_COLUMN_INDEX]))
					return true;
			return false;
		}
		
		private void setAccelerator(KeyStroke accelerator, int row) {
			data[row][ACCELERATOR_COLUMN_INDEX] = accelerator;
		}
		
		private void setAlternativeAccelerator(KeyStroke altAccelerator, int row) {
			data[row][ALTERNATE_ACCELERATOR_COLUMN_INDEX] = altAccelerator;
		}
		
		private List filter(Enumeration actionIdsEnum, ActionFilter filter) {
			List list = new LinkedList();
			while(actionIdsEnum.hasMoreElements()) {
				String actionId = (String) actionIdsEnum.nextElement();
                // Discard actions that are parameterized, and those that are rejected by the filter
				if (!ActionProperties.getActionDescriptor(actionId).isParameterized() && filter.accept(actionId))
					list.add(actionId);
			}
			return list;
		}
	}
	
	/**
	 * This class tracks the table's state - whether it was changed and which rows were modified.
	 */
	private class ShortcutsTableState {
		/** Saved copy of the table data, before any change was made by the user */
		private Object[][] originalTableData;
		/** Set that holds the row indexes the user may have changed */
		private Set dirtyRows;
		
		/**
		 * @param tableData - table's data before any modification was made.
		 */
		public ShortcutsTableState(Object[][] tableData) {
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
			Object[][] currentTableData = data.getTableData();
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
	
	private class ShortcutsTableCellRenderer implements TableCellRenderer, ThemeListener {
		/** Custom JLabel that render specific column cells */
	    private DotBorderedCellLabel[] cellLabels = new DotBorderedCellLabel[NUM_OF_COLUMNS];
	    
	    public ShortcutsTableCellRenderer() {
	    	for(int i=0; i<NUM_OF_COLUMNS; ++i)
	            cellLabels[i] = new DotBorderedCellLabel();

	    	// Set labels' font.
	        setCellLabelsFont(ThemeCache.tableFont);
	    	
	    	cellLabels[ACTION_DESCRIPTION_COLUMN_INDEX].setHorizontalAlignment(CellLabel.LEFT);
	    	cellLabels[ACCELERATOR_COLUMN_INDEX].setHorizontalAlignment(CellLabel.CENTER);
	    	cellLabels[ALTERNATE_ACCELERATOR_COLUMN_INDEX].setHorizontalAlignment(CellLabel.CENTER);
	    	
	    	// Listens to certain configuration variables
	        ThemeCache.addThemeListener(this);
	    }
		
	    /**
	     * Sets CellLabels' font to the current one.
	     */
	    private void setCellLabelsFont(Font newFont) {
	        // Set custom font
	        for(int i=0; i<NUM_OF_COLUMNS; ++i)
	        	cellLabels[i].setFont(newFont);
	    }
	    
		public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
			DotBorderedCellLabel label;
			int       columnId;
			
			columnId = convertColumnIndexToModel(vColIndex);
			label = cellLabels[columnId];
			
			// action's icon column: return ImageIcon instance
			if(columnId == ACTION_DESCRIPTION_COLUMN_INDEX) {
				ActionDescription description = (ActionDescription) value;
				label.setIcon(description.icon);
				label.setText(description.text);
				
				// set cell's foreground color
				label.setForeground(ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL][ThemeCache.PLAIN_FILE]);
			}
			// Any other column
			else {
				final KeyStroke key = (KeyStroke) value;
				String text = key == null ? "" : KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(key);
				
				// If component's preferred width is bigger than column width then the component is not entirely
	            // visible so we set a tooltip text that will display the whole text when mouse is over the component
	            if (table.getColumnModel().getColumn(vColIndex).getWidth() < label.getPreferredSize().getWidth())
	                label.setToolTipText(text);
	            // Have to set it to null otherwise the defaultRender sets the tooltip text to the last one specified
	            else
	                label.setToolTipText(null);
	            
	            // Set label's text
				label.setText(text);
				// set cell's foreground color
				if (key != null) {
					boolean customized;
					switch (columnId) {
					case ACCELERATOR_COLUMN_INDEX:
						customized = !key.equals(ActionProperties.getDefaultAccelerator(data.getActionId(rowIndex)));
						break;
					case ALTERNATE_ACCELERATOR_COLUMN_INDEX:
						customized = !key.equals(ActionProperties.getDefaultAlternativeAccelerator(data.getActionId(rowIndex)));
						break;
					default:
						customized = false;
					}

					label.setForeground(customized ?
							ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL][ThemeCache.SYMLINK]
							: ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL][ThemeCache.PLAIN_FILE]);
				}
			}
			
			// set outline for the focused cell
			label.setOutline(hasFocus ? ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED] : null);
			// set cell's background color
			label.setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][rowIndex % 2 == 0 ? ThemeCache.NORMAL : ThemeCache.ALTERNATE]);
			
			return label;
		}
		
		// - Theme listening -------------------------------------------------------------
	    // -------------------------------------------------------------------------------
	    /**
	     * Receives theme color changes notifications.
	     */
	    public void colorChanged(ColorChangedEvent event) { }

	    /**
	     * Receives theme font changes notifications.
	     */
	    public void fontChanged(FontChangedEvent event) {
	        if(event.getFontId() == Theme.FILE_TABLE_FONT) {
	            setCellLabelsFont(ThemeCache.tableFont);
	        }
	    }
	}
	
	/**
	 * CellLabel with a dotted outline.
	 */
	private class DotBorderedCellLabel extends CellLabel {
		
		protected void paintOutline(Graphics g) {
            paintDottedBorder(g, getWidth(), getHeight(), outlineColor);
		}
	}
	
	private class ActionDescription {
		private ImageIcon icon;
		private String text;
		
		public ActionDescription(ImageIcon icon, String text) {
			this.icon = icon;
			this.text = text;
		}
	}
}
