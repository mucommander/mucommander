/*
 * This file is part of muCommander, http://www.mucommander.com
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.mucommander.ui.action.ActionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.Pair;
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
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeCache;
import com.mucommander.ui.theme.ThemeListener;

/**
 * This class is the table in which the actions and their shortcuts are presented in the ShortcutsPanel.
 *
 * @author Arik Hadas, Johann Schmitz (johann@j-schmitz.net)
 */
public class ShortcutsTable extends PrefTable implements KeyListener, ListSelectionListener, FocusListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShortcutsTable.class);

    /**
     * Base width and height of icons for a scale factor of 1
     */
    private final static int BASE_ICON_DIMENSION = 16;

    /**
     * Transparent icon used to align non-locked themes with the others.
     */
    private static final ImageIcon transparentIcon =
            new ImageIcon(new BufferedImage(BASE_ICON_DIMENSION, BASE_ICON_DIMENSION, BufferedImage.TYPE_INT_ARGB));

    /**
     * Private object used to indicate that a delete operation was made
     */
    public static final Object DELETE = new Object();

    private final ShortcutsTableData shortcutsTableData;

    /**
     * Comparator of actions according to their labels
     */
    private static final Comparator<ActionId> ACTIONS_COMPARATOR = new Comparator<>() {
        public int compare(ActionId id1, ActionId id2) {
            String label1 = ActionProperties.getActionLabel(id1);
            if (label1 == null) {
                return 1;
            }

            String label2 = ActionProperties.getActionLabel(id2);
            if (label2 == null) {
                return -1;
            }

            return label1.compareTo(label2);
        }
    };

    /**
     * Last selected row in the table
     */
    private int lastSelectedRow = -1;

    /**
     * The bar below the table in which messages can be displayed
     */
    private final TooltipBar tooltipBar;

    /**
     * Number of mouse clicks required to enter cell's editing state
     */
    private static final int NUM_OF_CLICKS_TO_ENTER_EDITING_STATE = 2;

    /**
     * Number of columns in the table
     */
    private static final int NUM_OF_COLUMNS = 3;

    /**
     * After the following time (msec) that cell is being in editing state and no pressing was made, the editing state
     * is canceled
     */
    private static final int CELL_EDITING_STATE_PERIOD = 3000;

    /**
     * Thread that cancel cell's editing state after CELL_EDITING_STATE_PERIOD time
     */
    private CancelEditingStateThread cancelEditingStateThread;

    private final ShortcutsTableCellRenderer cellRenderer;

    public ShortcutsTable(TooltipBar tooltipBar) {
        super();
        this.tooltipBar = tooltipBar;

        setModel(new KeymapTableModel(shortcutsTableData = new ShortcutsTableData()));

        cellRenderer = new ShortcutsTableCellRenderer();
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setRowHeight(Math.max(getRowHeight(), BASE_ICON_DIMENSION + 2 * CellLabel.CELL_BORDER_HEIGHT));
        getTableHeader().setReorderingAllowed(false);
        setRowSelectionAllowed(false);
        setAutoCreateColumnsFromModel(false);
        setCellSelectionEnabled(false);
        setColumnSelectionAllowed(false);
        setDragEnabled(false);

        if (!usesTableHeaderRenderingProperties()) {
            CenteredTableHeaderRenderer renderer = new CenteredTableHeaderRenderer();
            getColumnModel().getColumn(TableDataColumnEnum.DESCRIPTION.columnIndex).setHeaderRenderer(renderer);
            getColumnModel().getColumn(TableDataColumnEnum.ACCELERATOR.columnIndex).setHeaderRenderer(renderer);
            getColumnModel().getColumn(TableDataColumnEnum.ALT_ACCELERATOR.columnIndex).setHeaderRenderer(renderer);
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
     * @param g
     *            Graphics object to use for painting
     * @param width
     *            border width
     * @param height
     *            border height
     * @param color
     *            border color
     */
    private static void paintDottedBorder(Graphics g, int width, int height, Color color) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1.0f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_MITER,
                2.0f,
                new float[] { 2.0f },
                0));
        g2.setColor(color);

        g2.drawLine(0, 0, width, 0);
        g2.drawLine(0, height - 1, width, height - 1);
        g2.drawLine(0, 0, 0, height - 1);
        g2.drawLine(width - 1, 0, width - 1, height - 1);
    }

    private static boolean usesTableHeaderRenderingProperties() {
        return OsFamily.MAC_OS.isCurrent();
    }

    /**
     * Assumes table is contained in a JScrollPane. Scrolls the cell (rowIndex, vColIndex) so that it is visible within
     * the viewport.
     */
    public void scrollToVisible(int rowIndex, int vColIndex) {
        if (!(getParent() instanceof JViewport)) {
            return;
        }

        JViewport viewport = (JViewport) getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = getCellRect(rowIndex, vColIndex, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);

        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
    }

    /**
     * Create thread that will cancel the editing state of the given TableCellEditor after CELL_EDITING_STATE_PERIOD
     * time in which with no pressing was made.
     */
    public void createCancelEditingStateThread(TableCellEditor cellEditor) {
        if (cancelEditingStateThread != null) {
            cancelEditingStateThread.neutralize();
        }
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
        int selectedRow = getSelectedRow();
        if (selectedRow == -1) { // no row is selected
            tooltipBar.showDefaultMessage();
        } else {
            tooltipBar.showActionTooltip(shortcutsTableData.getCurrentTooltip());
        }
    }

    public void updateModel(ActionFilter filter) {
        shortcutsTableData.filter(filter);
    }

    /**
     * Override this method so that calls for SetModel function outside this class won't get to
     * setModel(KeymapTableModel model) function.
     */
    @Override
    public void setModel(TableModel model) {
        super.setModel(model);
    }

    @Override
    public boolean hasChanged() {
        return shortcutsTableData.hasChanged();
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        return new KeyStrokeCellEditor(new RecordingKeyStrokeField((KeyStroke) getValueAt(row, column)));
    }

    /**
     * This method updates ActionKeymap with the modified shortcuts.
     */
    public void commitChanges() {
        shortcutsTableData.submitChanges();
    }

    public void restoreDefaults() {
        shortcutsTableData.restoreDefaultAccelerators();
    }

    ///////////////////////////
    // FocusListener methods //
    ///////////////////////////

    public void focusGained(FocusEvent e) {
        int currentSelectedRow = getSelectedRow();
        if (lastSelectedRow != currentSelectedRow) {
            tooltipBar.showActionTooltip(shortcutsTableData.getCurrentTooltip());
        }
        lastSelectedRow = currentSelectedRow;
    }

    public void focusLost(FocusEvent e) {
    }

    /////////////////////////////
    //// KeyListener methods ////
    /////////////////////////////

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER) {
            if (editCellAt(getSelectedRow(), getSelectedColumn())) {
                getEditorComponent().requestFocusInWindow();
            }
            e.consume();
        } else if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
            setValueAt(DELETE, getSelectedRow(), getSelectedColumn());
            repaint();
            e.consume();
        } else if (!Set.of(KeyEvent.VK_LEFT,
                KeyEvent.VK_RIGHT,
                KeyEvent.VK_UP,
                KeyEvent.VK_DOWN,
                KeyEvent.VK_HOME,
                KeyEvent.VK_END,
                KeyEvent.VK_F2,
                KeyEvent.VK_ESCAPE).contains(keyCode)) {
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public interface ActionFilter {
        boolean accept(ActionId actionId, String rowAsText);
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

                public void changedUpdate(DocumentEvent e) {
                }

                public void removeUpdate(DocumentEvent e) {
                }
            });

            setClickCountToStart(NUM_OF_CLICKS_TO_ENTER_EDITING_STATE);

            createCancelEditingStateThread(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int col) {
            return rec;
        }

        @Override
        public Object getCellEditorValue() {
            return rec.getLastKeyStroke();
        }
    }

    private class CancelEditingStateThread extends Thread {
        private boolean stopped = false;
        private final TableCellEditor cellEditor;

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
            } catch (InterruptedException e) {
            }

            if (!stopped && cellEditor != null) {
                cellEditor.stopCellEditing();
            }
        }
    }

    private class KeymapTableModel extends DefaultTableModel {
        private ShortcutsTableData tableData = null;

        private KeymapTableModel(ShortcutsTableData tableData) {
            super(new String[] { Translator.get("shortcuts_table.action_description"),
                    Translator.get("shortcuts_table.shortcut"),
                    Translator.get("shortcuts_table.alternate_shortcut") },
                    tableData.getTableSize());
            this.tableData = tableData;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            TableDataColumnEnum colEnum = TableDataColumnEnum.fromInt(column);
            switch (colEnum) {
            case ACCELERATOR:
            case ALT_ACCELERATOR:
                return true;
            default:
                return false;
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            TableDataColumnEnum colEnum = TableDataColumnEnum.fromInt(column);
            return tableData.getTableData(row, colEnum);
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            // if no keystroke was pressed
            if (value == null) {
                return;
            }
            // if the user pressed a keystroke that is used to indicate a delete operation should be made
            if (value == DELETE) {
                value = null;
            }

            TableDataColumnEnum colEnum = TableDataColumnEnum.fromInt(column);
            KeyStroke typedKeyStroke = (KeyStroke) value;
            switch (colEnum) {
            case ACCELERATOR:
                tableData.setAccelerator(typedKeyStroke, row);
                break;
            case ALT_ACCELERATOR:
                tableData.setAlternativeAccelerator(typedKeyStroke, row);
                break;
            default:
                LOGGER.debug("Unexpected column: {} -> {}", column, colEnum);
            }

            fireTableCellUpdated(row, column);

            LOGGER.trace("Value: {}, row: {}, col: {}", value, row, colEnum);
        }
    }

    private class RecordingKeyStrokeField extends JTextField implements KeyListener {

        // The last KeyStroke that was entered to the field.
        // Before any keystroke is entered, it contains the keystroke appearing in the cell before entering the editing
        // state.
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
            // It is required to disable the traversal keys in order to support keys combination that include the TAB
            // key
            setFocusTraversalKeysEnabled(false);
        }

        /**
         * @return the last KeyStroke the user entered to the field.
         */
        public KeyStroke getLastKeyStroke() {
            return lastKeyStroke;
        }

        ////////////////////////
        // Overridden methods //
        ////////////////////////

        @Override
        protected void paintBorder(Graphics g) {
            paintDottedBorder(g,
                    getWidth(),
                    getHeight(),
                    ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL]);
        }

        /////////////////////////////
        //// KeyListener methods ////
        /////////////////////////////

        public void keyPressed(KeyEvent keyEvent) {
            LOGGER.trace("keyModifiers={} keyCode={}", keyEvent.getModifiers(), keyEvent.getKeyCode());

            int keyCode = keyEvent.getKeyCode();
            if (Set.of(KeyEvent.VK_SHIFT,
                    KeyEvent.VK_CONTROL,
                    KeyEvent.VK_ALT,
                    KeyEvent.VK_META).contains(keyCode)) {
                return;
            }

            KeyStroke pressedKeyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);

            if (pressedKeyStroke.equals(lastKeyStroke)) {
                TableCellEditor activeCellEditor = getCellEditor();
                if (activeCellEditor != null) {
                    activeCellEditor.stopCellEditing();
                }
            } else {
                ActionId actionId;
                if ((actionId = shortcutsTableData.contains(pressedKeyStroke)) != null) {
                    tooltipBar.showErrorMessage(Translator.get("shortcuts_panel.already_assigned",
                            KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(pressedKeyStroke),
                            ActionProperties.getActionLabel(actionId)));
                    tooltipBar.setToolTipText(Translator.get("shortcuts_panel.already_assigned",
                            KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(pressedKeyStroke),
                            ActionProperties.getActionDescription(actionId)));
                    createCancelEditingStateThread(getCellEditor());
                } else {
                    lastKeyStroke = pressedKeyStroke;
                    setText(KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(lastKeyStroke));
                }
            }

            keyEvent.consume();
        }

        public void keyReleased(KeyEvent e) {
            e.consume();
        }

        public void keyTyped(KeyEvent e) {
            e.consume();
        }
    }

    public enum TableDataColumnEnum {
        DESCRIPTION(0),
        ACCELERATOR(1),
        ALT_ACCELERATOR(2),
        TOOLTIPS(3),
        UNKNOWN(-1),
        ;

        private int columnIndex;

        TableDataColumnEnum(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        static TableDataColumnEnum fromInt(int idx) {
            TableDataColumnEnum result = UNKNOWN;
            for (TableDataColumnEnum e : TableDataColumnEnum.values()) {
                if (e.columnIndex == idx) {
                    result = e;
                    break;
                }
            }
            return result;
        }
    }

    private class ShortcutsTableData {

        /**
         * Original actions (so we can revert to defaults or compare what has changed)
         */
        private final Map<ActionId, Map<TableDataColumnEnum, Object>> originalActionMap;

        /**
         * Edited actions (decoupled from originalActionMap).
         */
        private final Map<ActionId, Map<TableDataColumnEnum, Object>> editedActionMap;

        /**
         * Filtered aka visible and edited actions (mapped thru references to editedActionMap).
         */
        private List<ActionId> filteredEditedActionIds;
        private final LinkedHashMap<ActionId, Map<TableDataColumnEnum, Object>> filteredEditedActionMap;

        public ShortcutsTableData() {
            List<ActionId> allActionIds = ActionManager.getActionIds();
            Collections.sort(allActionIds, ACTIONS_COMPARATOR);

            final int nbActions = allActionIds.size();
            originalActionMap = new LinkedHashMap<>(nbActions);
            editedActionMap = new LinkedHashMap<>(nbActions);
            filteredEditedActionIds = new LinkedList<>();
            filteredEditedActionMap = new LinkedHashMap<>(nbActions);

            for (ActionId actionId : allActionIds) {
                ActionDescriptor actionDescriptor = ActionProperties.getActionDescriptor(actionId);

                HashMap<TableDataColumnEnum, Object> actionProperties = new HashMap<>();

                ImageIcon actionIcon = actionDescriptor.getIcon();
                if (actionIcon == null) {
                    actionIcon = transparentIcon;
                }
                String actionLabel = actionDescriptor.getLabel();

                /* icon & name pair */
                actionProperties.put(TableDataColumnEnum.DESCRIPTION,
                        new Pair<ImageIcon, String>(IconManager.getPaddedIcon(actionIcon, new Insets(0, 4, 0, 4)),
                                actionLabel));
                /* action's accelerator */
                actionProperties.put(TableDataColumnEnum.ACCELERATOR, ActionKeymap.getAccelerator(actionId));
                /* action's alternate accelerator */
                actionProperties.put(TableDataColumnEnum.ALT_ACCELERATOR,
                        ActionKeymap.getAlternateAccelerator(actionId));
                /* action's description */
                actionProperties.put(TableDataColumnEnum.TOOLTIPS, actionDescriptor.getDescription());

                filteredEditedActionIds.add(actionId);
                originalActionMap.put(actionId, actionProperties);
                // and deep copy so original mapping stays untouched
                editedActionMap.put(actionId, (Map<TableDataColumnEnum, Object>) actionProperties.clone());
            }
            filteredEditedActionMap.putAll(editedActionMap);
        }

        public void filter(ActionFilter filter) {
            filteredEditedActionIds = filter(originalActionMap.keySet(), filter);
            filteredEditedActionMap.clear();

            for (ActionId actionId : filteredEditedActionIds) {
                filteredEditedActionMap.put(actionId, originalActionMap.get(actionId));
            }

            ShortcutsTable.this.clearSelection();
            ((DefaultTableModel) getModel()).setRowCount(filteredEditedActionMap.size());
            ShortcutsTable.this.repaint();
            ShortcutsTable.this.scrollToVisible(0, 0);
        }

        public int getTableSize() {
            return filteredEditedActionMap.size();
        }

        public Object getTableData(int row, TableDataColumnEnum col) {
            return filteredEditedActionMap.get(filteredEditedActionIds.get(row)).get(col);
        }

        public String getCurrentTooltip() {
            return (String) getTableData(getSelectedRow(), TableDataColumnEnum.TOOLTIPS);
        }

        public ActionId getActionId(int row) {
            return filteredEditedActionIds.get(row);
        }

        public boolean hasChanged() {
            for (ActionId actionId : editedActionMap.keySet()) {
                Map<TableDataColumnEnum, Object> actionProperties = editedActionMap.get(actionId);
                if (!Objects.equals(actionProperties.get(TableDataColumnEnum.ACCELERATOR),
                        originalActionMap.get(actionId).get(TableDataColumnEnum.ACCELERATOR)) ||
                        !Objects.equals(actionProperties.get(TableDataColumnEnum.ALT_ACCELERATOR),
                                originalActionMap.get(actionId).get(TableDataColumnEnum.ALT_ACCELERATOR))) {
                    return true;
                }
            }
            return false;
        }

        public void restoreDefaultAccelerators() {
            for (Map.Entry<ActionId, Map<TableDataColumnEnum, Object>> entry : editedActionMap.entrySet()) {
                entry.getValue()
                        .put(TableDataColumnEnum.ACCELERATOR, ActionProperties.getDefaultAccelerator(entry.getKey()));
                entry.getValue()
                        .put(TableDataColumnEnum.ALT_ACCELERATOR,
                                ActionProperties.getDefaultAlternativeAccelerator(entry.getKey()));
            }
            for (Map.Entry<ActionId, Map<TableDataColumnEnum, Object>> entry : filteredEditedActionMap.entrySet()) {
                entry.setValue(editedActionMap.get(entry.getKey()));
            }
            ((DefaultTableModel) getModel()).fireTableDataChanged();
        }

        public void submitChanges() {
            for (Map.Entry<ActionId, Map<TableDataColumnEnum, Object>> entry : originalActionMap.entrySet()) {
                ActionId actionId = entry.getKey();
                Map<TableDataColumnEnum, Object> actionProperties = editedActionMap.get(actionId);
                KeyStroke accelerator = (KeyStroke) actionProperties.get(TableDataColumnEnum.ACCELERATOR);
                KeyStroke altAccelerator = (KeyStroke) actionProperties.get(TableDataColumnEnum.ALT_ACCELERATOR);

                // If action's accelerators differ from its saved accelerators, register them.
                if (!Objects.equals(accelerator, entry.getValue().get(TableDataColumnEnum.ACCELERATOR)) ||
                        !Objects.equals(altAccelerator, entry.getValue().get(TableDataColumnEnum.ALT_ACCELERATOR))) {
                    ActionKeymap.changeActionAccelerators(actionId, accelerator, altAccelerator);
                }
            }
        }

        public ActionId contains(KeyStroke accelerator) {
            if (accelerator != null) {
                for (Map.Entry<ActionId, Map<TableDataColumnEnum, Object>> entry : editedActionMap.entrySet()) {
                    if (accelerator.equals(entry.getValue().get(TableDataColumnEnum.ACCELERATOR)) ||
                            accelerator.equals(entry.getValue().get(TableDataColumnEnum.ALT_ACCELERATOR))) {
                        return entry.getKey();
                    }
                }
            }
            return null;
        }

        private void setAccelerator(KeyStroke accelerator, int row) {
            filteredEditedActionMap.get(getActionId(row)).put(TableDataColumnEnum.ACCELERATOR, accelerator);
        }

        private void setAlternativeAccelerator(KeyStroke altAccelerator, int row) {
            filteredEditedActionMap.get(getActionId(row)).put(TableDataColumnEnum.ALT_ACCELERATOR, altAccelerator);
        }

        private List<ActionId> filter(Set<ActionId> actionIds, ActionFilter filter) {
            List<ActionId> filteredActionsList = new LinkedList<>();
            for (ActionId actionId : actionIds) {
                // Discard actions that are parameterized, and those that are rejected by the filter
                if (!ActionProperties.getActionDescriptor(actionId).isParameterized()
                        && filter.accept(actionId, getRowAsTextForFilter(actionId))) {
                    filteredActionsList.add(actionId);
                }
            }
            return filteredActionsList;
        }

        private String getRowAsTextForFilter(ActionId actionId) {
            StringBuilder rowText = new StringBuilder();
            Map<TableDataColumnEnum, Object> rowData = originalActionMap.get(actionId);

            for (Map.Entry<TableDataColumnEnum, Object> entry : rowData.entrySet()) {
                String rowTextValue;
                Object colValue = entry.getValue();
                switch (entry.getKey()) {
                case DESCRIPTION:
                    Pair<ImageIcon, String> description = (Pair<ImageIcon, String>) colValue;
                    rowTextValue = description.second;
                    break;
                case ACCELERATOR:
                case ALT_ACCELERATOR:
                    final KeyStroke key = (KeyStroke) colValue;
                    rowTextValue = key == null ? "" : KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(key);
                    break;
                case TOOLTIPS:
                    rowTextValue = (String) colValue;
                    break;
                default:
                    rowTextValue = "";
                }
                rowText.append(" ");
                rowText.append(rowTextValue.trim().toLowerCase());
            }

            return rowText.toString().trim();
        }
    }

    private class ShortcutsTableCellRenderer implements TableCellRenderer, ThemeListener {
        /**
         * Custom JLabel that render specific column cells
         */
        private final Map<TableDataColumnEnum, DotBorderedCellLabel> cellLabels =
                Set.of(TableDataColumnEnum.DESCRIPTION,
                        TableDataColumnEnum.ACCELERATOR,
                        TableDataColumnEnum.ALT_ACCELERATOR)
                        .stream()
                        .collect(Collectors.toMap(
                                e -> e,
                                e -> new DotBorderedCellLabel()));

        public ShortcutsTableCellRenderer() {
            // Set labels' font.
            setCellLabelsFont(ThemeCache.tableFont);

            cellLabels.get(TableDataColumnEnum.DESCRIPTION).setHorizontalAlignment(CellLabel.LEFT);
            cellLabels.get(TableDataColumnEnum.ACCELERATOR).setHorizontalAlignment(CellLabel.CENTER);
            cellLabels.get(TableDataColumnEnum.ALT_ACCELERATOR).setHorizontalAlignment(CellLabel.CENTER);

            // Listens to certain configuration variables
            ThemeCache.addThemeListener(this);
        }

        /**
         * Sets CellLabels' font to the current one.
         */
        private void setCellLabelsFont(Font newFont) {
            // Set custom font
            for (DotBorderedCellLabel label : cellLabels.values()) {
                label.setFont(newFont);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int rowIndex,
                int vColIndex) {

            TableDataColumnEnum colEnum = TableDataColumnEnum.fromInt(convertColumnIndexToModel(vColIndex));
            DotBorderedCellLabel label = cellLabels.get(colEnum);

            // action's icon column: return ImageIcon instance
            if (colEnum == TableDataColumnEnum.DESCRIPTION) {
                Pair<ImageIcon, String> description = (Pair<ImageIcon, String>) value;
                label.setIcon(description.first);
                label.setText(description.second);

                // set cell's foreground color
                label.setForeground(
                        ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL][ThemeCache.PLAIN_FILE]);
            }
            // Any other column
            else {
                final KeyStroke key = (KeyStroke) value;
                String text = key == null ? "" : KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(key);

                // If component's preferred width is bigger than column width then the component is not entirely
                // visible, so we set a tooltip text that will display the whole text when mouse is over the component
                if (table.getColumnModel().getColumn(vColIndex).getWidth() < label.getPreferredSize().getWidth()) {
                    label.setToolTipText(text);
                } else {
                    // Have to set it to null otherwise the defaultRender sets the tooltip text to the last one
                    // specified
                    label.setToolTipText(null);
                }

                // Set label's text
                label.setText(text);
                // set cell's foreground color
                if (key != null) {
                    boolean customized;
                    switch (colEnum) {
                    case ACCELERATOR:
                        customized = !key.equals(
                                ActionProperties.getDefaultAccelerator(shortcutsTableData.getActionId(rowIndex)));
                        break;
                    case ALT_ACCELERATOR:
                        customized = !key.equals(ActionProperties
                                .getDefaultAlternativeAccelerator(shortcutsTableData.getActionId(rowIndex)));
                        break;
                    default:
                        customized = false;
                    }

                    label.setForeground(ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL][customized
                            ? ThemeCache.PLAIN_FILE
                            : ThemeCache.HIDDEN_FILE]);
                }
            }

            // set outline for the focused cell
            label.setOutline(hasFocus ? ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED] : null);
            // set cell's background color
            label.setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][rowIndex % 2 == 0 ? ThemeCache.NORMAL
                    : ThemeCache.ALTERNATE]);

            return label;
        }

        // - Theme listening -------------------------------------------------------------
        // -------------------------------------------------------------------------------

        /**
         * Receives theme color changes notifications.
         */
        @Override
        public void colorChanged(ColorChangedEvent event) {
        }

        /**
         * Receives theme font changes notifications.
         */
        @Override
        public void fontChanged(FontChangedEvent event) {
            if (event.getFontId() == Theme.FILE_TABLE_FONT) {
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
}
