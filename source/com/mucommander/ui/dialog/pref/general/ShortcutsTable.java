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

import com.mucommander.AppLogger;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.text.Translator;
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
import com.mucommander.ui.theme.*;

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
	
	/** Base width and height of icons for a scale factor of 1 */
    private final static int BASE_ICON_DIMENSION = 16;
	
	/** Transparent icon used to align non-locked themes with the others. */
    private static ImageIcon transparentIcon = new ImageIcon(new BufferedImage(BASE_ICON_DIMENSION, BASE_ICON_DIMENSION, BufferedImage.TYPE_INT_ARGB));

	/** Private object used to indicate that a delete operation was made */
	public static final Object DELETE = new Object();
	
	private ShortcutsTableData data;

	/** Comparator of actions according to their labels */
	private static final Comparator<String> ACTIONS_COMPARATOR = new Comparator<String>() {
		public int compare(String id1, String id2) {
			String label1 = ActionProperties.getActionLabel(id1);
			if (label1 == null)
				return 1;
			
			String label2 = ActionProperties.getActionLabel(id2);
			if (label2 == null)
				return -1;
			
			return label1.compareTo(label2);
		}
	};
	
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
		
		setModel(new KeymapTableModel(data = new ShortcutsTableData()));
		
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
        return OsFamilies.MAC_OS_X.isCurrent() && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher();
    }
    
    /** 
     * Assumes table is contained in a JScrollPane. Scrolls the
     * cell (rowIndex, vColIndex) so that it is visible within the viewport.
 	*/
    public void scrollToVisible(int rowIndex, int vColIndex) {
    	if (!(getParent() instanceof JViewport))
            return;

    	JViewport viewport = (JViewport) getParent();
    
        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = getCellRect(rowIndex, vColIndex, true);
    
        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();
    
        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x-pt.x, rect.y-pt.y);
    
        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
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
	
	@Override
    public TableCellRenderer getCellRenderer(int row, int column) {
		return cellRenderer;
	}
	
	@Override
    public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);
		// Selection might be changed, update tooltip
		int selectetRow = getSelectedRow();
		if (selectetRow == -1) // no row is selected
			tooltipBar.showDefaultMessage();
		else
			tooltipBar.showActionTooltip(data.getCurrentTooltip());
	}
	
	public void updateModel(ActionFilter filter) {
		data.filter(filter);
	}

	/**
	 * Override this method so that calls for SetModel function outside this class
	 * won't get to setModel(KeymapTableModel model) function.
	 */
	@Override
    public void setModel(TableModel model) {
		super.setModel(model);
	}
	
	public boolean hasChanged() { return data.hasChanged(); }
	
	@Override
    public TableCellEditor getCellEditor(int row, int column) {
		return new KeyStrokeCellEditor(new RecordingKeyStrokeField((KeyStroke) getValueAt(row, column)));
	}
	
	/**
	 * This method updates ActionKeymap with the modified shortcuts.
	 */
	public void commitChanges() {
		data.submitChanges();
	}
	
	public void restoreDefaults() {
		data.restoreDefaultAccelerators();
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

		@Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int col) {
            return rec;
        }
 
        @Override
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
		
		@Override
        public void run() {
        	try {
				Thread.sleep(CELL_EDITING_STATE_PERIOD);
			} catch (InterruptedException e) {}
			
			if (!stopped && cellEditor != null)
				cellEditor.stopCellEditing();
        }
	}
	
	private class KeymapTableModel extends DefaultTableModel {	
		private ShortcutsTableData tableData = null;

		private KeymapTableModel(ShortcutsTableData data) {
			super(data.getTableData(), new String[] {Translator.get("shortcuts_table.action_description"),
													 Translator.get("shortcuts_table.shortcut"),
													 Translator.get("shortcuts_table.alternate_shortcut")});
			this.tableData = data;
		}

		@Override
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
		
		@Override
        public Object getValueAt(int row, int column) {
			return tableData.getTableData(row, column);
		}

		@Override
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
				tableData.setAccelerator(typedKeyStroke, row);
				break;
			case ALTERNATE_ACCELERATOR_COLUMN_INDEX:
				tableData.setAlternativeAccelerator(typedKeyStroke, row);
				break;
			default:
				AppLogger.fine("Unexpected column index: " + column);
			}

			fireTableCellUpdated(row, column);
			
			AppLogger.finest("Value: " + value + ", row: " + row + ", col: " + column);
		}
	}
	
	private class RecordingKeyStrokeField extends JTextField implements KeyListener {
		
		// The last KeyStroke that was entered to the field.
		// Before any keystroke is entered, it contains the keystroke appearing in the cell before entering the editing state.
		private KeyStroke lastKeyStroke;
		
		public RecordingKeyStrokeField(KeyStroke currentKeyStroke) {
			super(Translator.get("shortcuts_table.type_in_a_shortcut"));
			
			lastKeyStroke = currentKeyStroke;			
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

        @Override
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

	        if (pressedKeyStroke.equals(lastKeyStroke)) {
	        	TableCellEditor activeCellEditor = getCellEditor();
        		if (activeCellEditor!= null)
        			activeCellEditor.stopCellEditing();
	        }
	        else {
	        	String actionId;
	        	if ((actionId = data.contains(pressedKeyStroke)) != null) {
	        		String errorMessage = "The shortcut [" + KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(pressedKeyStroke)
	    			+ "] is already assigned to '" + ActionProperties.getActionDescription(actionId) + "'";
	        		tooltipBar.showErrorMessage(errorMessage);
	        		createCancelEditingStateThread(getCellEditor());
	        	}
	        	else {
	        		lastKeyStroke = pressedKeyStroke;
		        	setText(KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(lastKeyStroke));
	        	}
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
		
		private final Integer description = 0;
		private final Integer accelerator = 1;
		private final Integer alt_accelerator = 2;
		private final Integer tooltips = 3;
		
		private List<String> allActionIds;
		private HashMap<String, HashMap<Integer, Object>> db;
		
		public ShortcutsTableData() {
			allActionIds = Collections.list(ActionManager.getActionIds());
			Collections.sort(allActionIds, ACTIONS_COMPARATOR);
			
			final int nbActions = allActionIds.size();
			db = new HashMap<String, HashMap<Integer, Object>>(nbActions);

			int nbRows = allActionIds.size();
			data = new Object[nbRows][NUM_OF_COLUMNS];

            for(String actionId : allActionIds) {
				ActionDescriptor actionDescriptor = ActionProperties.getActionDescriptor(actionId);
				
				HashMap<Integer, Object> actionProperties = new HashMap<Integer, Object>();
				
				ImageIcon actionIcon = actionDescriptor.getIcon();
				if (actionIcon == null)
					actionIcon = transparentIcon;
				String actionLabel = actionDescriptor.getLabel();
				
				actionProperties.put(description, new ActionDescription(IconManager.getPaddedIcon(actionIcon, new Insets(0, 4, 0, 4)), actionLabel));
				actionProperties.put(accelerator, ActionKeymap.getAccelerator(actionId));
				actionProperties.put(alt_accelerator, ActionKeymap.getAlternateAccelerator(actionId));
				actionProperties.put(tooltips, actionDescriptor.getDescription());
				
				db.put(actionId, actionProperties);
			}
		}
		
		public void filter(ActionFilter filter) {
			List<String> filteredActionIds = filter(allActionIds, filter);

			// Build the table data
			int nbRows = filteredActionIds.size();
			
			actionIds = new String[nbRows];
			descriptions = new String[nbRows];
			data = new Object[nbRows][NUM_OF_COLUMNS];
			
			for (int i = 0; i < nbRows; ++i) {
				String actionId = filteredActionIds.get(i);
				actionIds[i] = actionId;
				ActionDescriptor actionDescriptor = ActionProperties.getActionDescriptor(actionId);
				
				data[i][ACTION_DESCRIPTION_COLUMN_INDEX] = db.get(actionId).get(this.description);
				
				KeyStroke accelerator = (KeyStroke) db.get(actionId).get(this.accelerator);
				setAccelerator(accelerator, i);
				
				KeyStroke alternativeAccelerator = (KeyStroke) db.get(actionId).get(this.alt_accelerator);
				setAlternativeAccelerator(alternativeAccelerator, i);
				
				descriptions[i] = actionDescriptor.getDescription();
			}
			
			ShortcutsTable.this.clearSelection();
			((DefaultTableModel) getModel()).setRowCount(data.length);
			ShortcutsTable.this.repaint();
			ShortcutsTable.this.scrollToVisible(0, 0);
		}
		
		public Object[][] getTableData() { return data; }
		
		public Object getTableData(int row, int col) { return data[row][col]; }
		
		public String getCurrentTooltip() { return descriptions[getSelectedRow()]; }
		
		public String getActionId(int row) { return actionIds[row]; }
		
		public boolean hasChanged() {
            for (String actionId : db.keySet()) {
                HashMap<Integer, Object> actionProperties = db.get(actionId);
                if (!equals(actionProperties.get(this.accelerator), ActionKeymap.getAccelerator(actionId)) ||
                        !equals(actionProperties.get(this.alt_accelerator), ActionKeymap.getAlternateAccelerator(actionId)))
                    return true;
            }
			return false;
		}
		
		public void restoreDefaultAccelerators() {
            for (String actionId : allActionIds) {
                (db.get(actionId)).put(this.accelerator, ActionProperties.getDefaultAccelerator(actionId));
                (db.get(actionId)).put(this.alt_accelerator, ActionProperties.getDefaultAlternativeAccelerator(actionId));
            }
			
			int nbRows = actionIds.length;
			for (int i=0; i<nbRows; ++i) {
				data[i][ACCELERATOR_COLUMN_INDEX] = db.get(actionIds[i]).get(this.accelerator);
				data[i][ALTERNATE_ACCELERATOR_COLUMN_INDEX] = db.get(actionIds[i]).get(this.alt_accelerator);
			}
			
			((DefaultTableModel) getModel()).fireTableDataChanged();
		}
		
		public void submitChanges() {
            for (String actionId : db.keySet()) {
                HashMap<Integer, Object> actionProperties = db.get(actionId);
                KeyStroke accelerator = (KeyStroke) actionProperties.get(this.accelerator);
                KeyStroke alternateAccelerator = (KeyStroke) actionProperties.get(this.alt_accelerator);

                // If action's accelerators differ from its saved accelerators, register them.
                if (!equals(accelerator, ActionKeymap.getAccelerator(actionId)) ||
                        !equals(alternateAccelerator, ActionKeymap.getAlternateAccelerator(actionId)))
                    ActionKeymap.changeActionAccelerators(actionId, accelerator, alternateAccelerator);
            }
		}
		
		public String contains(KeyStroke accelerator) {
			if (accelerator != null) {
                for (String actionId : db.keySet()) {
                    if (accelerator.equals(db.get(actionId).get(this.accelerator)) ||
                            accelerator.equals(db.get(actionId).get(this.alt_accelerator)))
                        return actionId;
                }
			}
			return null;
		}
		
		private void setAccelerator(KeyStroke accelerator, int row) {
			data[row][ACCELERATOR_COLUMN_INDEX] = accelerator;
			db.get(getActionId(row)).put(this.accelerator, accelerator);
		}
		
		private void setAlternativeAccelerator(KeyStroke altAccelerator, int row) {
			data[row][ALTERNATE_ACCELERATOR_COLUMN_INDEX] = altAccelerator;
			db.get(getActionId(row)).put(this.alt_accelerator, altAccelerator);
		}
		
		private List<String> filter(List<String> actionIds, ActionFilter filter) {
			List<String> filteredActionsList = new LinkedList<String>();
            for (String actionId : actionIds) {
                // Discard actions that are parameterized, and those that are rejected by the filter
                if (!ActionProperties.getActionDescriptor(actionId).isParameterized() && filter.accept(actionId))
                    filteredActionsList.add(actionId);
            }
			return filteredActionsList;
		}
		
		private boolean equals(Object obj1, Object obj2) {
			if (obj1 == null)
				return obj2 == null;
			return obj1.equals(obj2);
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

					label.setForeground(ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL][customized ? ThemeCache.PLAIN_FILE : ThemeCache.HIDDEN_FILE]);
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

		@Override
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
