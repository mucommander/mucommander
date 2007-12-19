/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.main.table;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Used to keep track of a file table's columns position and visibility settings.
 * @author Nicolas Rinaudo
 */
public class FileTableColumnModel implements TableColumnModel, Columns, PropertyChangeListener {
    // - Class constants -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** If {@link #widthCache} is set to this, it needs to be recalulated. */
    private static final int                CACHE_OUT_OF_DATE = -1;
    /** Even though we're not using column selection, the table API forces us to return this instance or will crash. */
    private static final ListSelectionModel SELECTION_MODEL   = new DefaultListSelectionModel();


    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** All registered listeners. */
    private WeakHashMap   listeners  = new WeakHashMap();
    /** Cache for the table's total width. */
    private int           widthCache = CACHE_OUT_OF_DATE;
    /** All available columns. */
    private Vector        columns    = new Vector(COLUMN_COUNT);
    /** Visibility status of each column. */
    private boolean[]     visibility = new boolean[COLUMN_COUNT];
    /** Cache for the number of available columns. */
    private int           countCache;
    /** Whether the column sizes were set already. */
    private boolean       columnSizesSet;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table column model.
     */
    public FileTableColumnModel(FileTableConfiguration conf) {
        TableColumn             column;         // Buffer for the current column.

        // The name column is always visible, so we know that the column count is always
        // at least 1.
        countCache = 1;

        // Initialises the columns.
        for(int i = 0; i < visibility.length; i++) {
            columns.add(column = new TableColumn(i));
            column.setCellEditor(null);
            column.setHeaderValue(COLUMN_LABELS[i]);

            FileTableHeaderRenderer headerRenderer = null;
            // Mac OS X 10.5 (Leopard) and up uses JTableHeader properties to render sort indicators on table headers.
            // On other platforms, we use a custom header renderer
            if(!FileTable.usesTableHeaderRenderingProperties()) {
                headerRenderer = new FileTableHeaderRenderer();
                column.setHeaderRenderer(headerRenderer);
            }

            column.addPropertyChangeListener(this);

            // Sets the column's initial width.
            if(conf.getWidth(i) != 0)
                column.setWidth(conf.getWidth(i));

            // Initialises the column's visibility and minimum width.
            if(i == NAME) {
                visibility[i] = true;
                if(headerRenderer!=null)
                    headerRenderer.setCurrent(true);
            }
            else {
                if((visibility[i] = conf.isVisible(i)))
                    countCache++;
            }
            column.setMinWidth(MINIMUM_COLUMN_SIZES[i]);
        }

        // Sorts the columns.
        Collections.sort(columns, new ColumnSorter(conf));
    }



    // - Configuration -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public synchronized FileTableConfiguration getConfiguration() {
        FileTableConfiguration conf;
        TableColumn            column;
        int                    index;

        conf = new FileTableConfiguration();
        for(int i = 0; i < COLUMN_COUNT; i++) {
            column = (TableColumn)columns.get(i);
            index  = column.getModelIndex();

            conf.setVisible(index, visibility[index]);
            conf.setPosition(index, i);
            conf.setWidth(index, column.getWidth());
        }

        return conf;
    }



    // - Columns visibility --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the specified column's visibility status.
     * @param id      identifier of the column as defined in {@link Columns}.
     * @param visible whether the column should be visible or not.
     */
    public synchronized void setColumnVisible(int id, boolean visible) {
        // Ignores calls that won't actually change anything.
        if(visibility[id] != visible) {
            visibility[id] = visible;
            widthCache = CACHE_OUT_OF_DATE;

            // Adds the column.
            if(visible) {
                countCache++;
                triggerColumnAdded(new TableColumnModelEvent(this, id, id));
            }

            // Removes the column.
            else {
                countCache--;
                triggerColumnRemoved(new TableColumnModelEvent(this, id, id));
            }
        }
    }

    /**
     * Returns <code>true</code> if the specified column is visible.
     * @param  id identifier of the column as definied in {@link Columns}.
     * @return    <code>true</code> if the specified column is visible, <code>false</code> otherwise.
     */
    public synchronized boolean isColumnVisible(int id) {return visibility[id];}

    /**
     * Adds the specified column to the model.
     * @param column column to add to the model.
     */
    public void addColumn(TableColumn column) {setColumnVisible(column.getModelIndex(), true);}

    /**
     * Removes the specified column from the model.
     * @param column column to remove from the model.
     */
    public void removeColumn(TableColumn column) {setColumnVisible(column.getModelIndex(), false);}



    // - Column retrieval ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private synchronized int getInternalIndex(int index) {
        int         visibleIndex;
        TableColumn column;

        // Looks for the visible column of index 'index'.
        visibleIndex = -1;
        for(int i = 0; i < visibility.length; i++) {
            column = (TableColumn)columns.get(i);
            if(visibility[column.getModelIndex()])
                if(++visibleIndex == index)
                    return i;
        }
        // Index doesn't exist.
        throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
    }

    /**
     * Returns the specified column.
     * @param  index index of the column in the model.
     * @return       the requested column.
     */
    public synchronized TableColumn getColumn(int index) {return (TableColumn)columns.get(getInternalIndex(index));}

    public synchronized TableColumn getColumnFromId(int id) {return (TableColumn)columns.get(id);}

    public synchronized int getColumnPosition(int id) {
        for(int i = 0; i < visibility.length; i++)
            if(((TableColumn)columns.get(i)).getModelIndex() == id)
                return i;
        return -1;
    }

    /**
     * Returns the number of columns currently displayed.
     * @return the number of columns currently displayed.
     */
    public synchronized int getColumnCount() {return countCache;}

    /**
     * Moves a column.
     * @param from index of the column to move.
     * @param to   where to move the column.
     */
    public synchronized void moveColumn(int from, int to) {
        // We need to trigger these for the file table to display the 'column dragging' animation.
        if(from == to) {
            triggerColumnMoved(new TableColumnModelEvent(this, from, to));
            return;
        }

        TableColumn column; // Buffer for the table to remove.
        int         index;  // Used to store the internal index of 'from' and 'to'.

        // Locates the internal index of the requested column
        // and removes that column
        index  = getInternalIndex(from);
        column = (TableColumn)columns.get(index);
        columns.removeElementAt(index);

        // If the column needs to be moved at the end of the set,
        // no need to locate its correct index.
        if(to == countCache - 1)
            columns.add(column);

        // Otherwise, finds the column's internal index and inserts
        // it there.
        else {
            index  = getInternalIndex(to);
            columns.insertElementAt(column, index);
        }

        // Notifies listeners and stores the new configuration.
        triggerColumnMoved(new TableColumnModelEvent(this, from, to));
    }

    public int getColumnIndex(Object identifier) {return 0;}



    // - Columns width -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the index of the column at the specified position.
     * @param  x position of the column to look for.
     * @return   the index of the column at the specified position, <code>-1</code> if not found.
     */
    public int getColumnIndexAtX(int x) {
        int count;

        count = getColumnCount();
        for(int i = 0; i < count; i++) {
            x = x - getColumn(i).getWidth();
            if(x < 0)
            return i;
            }
        return -1;
    }

    /**
     * Returns the total width of the table column model.
     * @return the total width of the table column model.
     */
    public int getTotalColumnWidth() {
        if(widthCache == CACHE_OUT_OF_DATE)
            computeWidthCache();
        return widthCache;
    }

    /**
     * Computes the model's width.
     */
    private void computeWidthCache() {
        Enumeration elements;

        elements = getColumns();
        widthCache = 0;
        while(elements.hasMoreElements())
            widthCache += ((TableColumn)elements.nextElement()).getWidth();
    }

    /**
     * Invalidates the width cache if a column's width has changed.
     */
    public void propertyChange(PropertyChangeEvent event) {
        String name;

        name = event.getPropertyName();
        if(name.equals("width")) {
                columnSizesSet = true;
            widthCache = CACHE_OUT_OF_DATE;
                // Notifies the table that columns width have changed and that it should repaint itself.
                triggerColumnMarginChanged(new ChangeEvent(this));
        }
    }

    boolean wereColumnSizesSet() {return columnSizesSet;}


    // - Columns margin ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Column margin is fixed to 0.

    /**
     * Returns 0.
     * @return 0.
     */
    public int getColumnMargin() {return 0;}

    /**
     * Ignored.
     */
    public void setColumnMargin(int margin) {}



    // - Listeners -----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Registers the specified column model listener.
     * @param listener listener to register.
     */
    public void addColumnModelListener(TableColumnModelListener listener) {listeners.put(listener, null);}

    /**
     * Removes the specified column model listener.
     * @param listener listener to remove.
     */
    public void removeColumnModelListener(TableColumnModelListener listener) {listeners.remove(listener);}

    /**
     * Calls all registered listeners' <code>columnAdded(event)</code>.
     * @param event event to propagate.
     */
    private void triggerColumnAdded(TableColumnModelEvent event) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((TableColumnModelListener)iterator.next()).columnAdded(event);
    }

    /**
     * Calls all registered listeners' <code>columnMarginChanged(event)</code>.
     * @param event event to propagate.
     */
    private void triggerColumnMarginChanged(ChangeEvent event) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((TableColumnModelListener)iterator.next()).columnMarginChanged(event);
    }

    /**
     * Calls all registered listeners' <code>columnMoved(event)</code>.
     * @param event event to propagate.
     */
    private void triggerColumnMoved(TableColumnModelEvent event) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((TableColumnModelListener)iterator.next()).columnMoved(event);
    }

    /**
     * Calls all registered listeners' <code>columnRemoved(event)</code>.
     * @param event event to propagate.
     */
    private void triggerColumnRemoved(TableColumnModelEvent event) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((TableColumnModelListener)iterator.next()).columnRemoved(event);
    }



    // - Column selection ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Column selection is not allowed, this methods are ignored or return default values.

    /**
     * Returns <code>false</code>.
     * @return <code>false</code>.
     */
    public boolean getColumnSelectionAllowed() {return false;}

    /**
     * Returns <code>0</code>.
     * @return <code>0</code>.
     */
    public int getSelectedColumnCount() {return 0;}

    /**
     * Returns an integer array of size 0.
     * @return an integer array of size 0.
     */
    public int[] getSelectedColumns() {return new int[0];}

    /**
     * Ignored.
     */
    public void setColumnSelectionAllowed(boolean flag) {}

    /**
     * Returns a default list selection model.
     * <p>
     * Ideally, we'd like to return <code>null</code> here, but the table API takes a dim view
     * of this and we're forced to keep a useless reference.
     * </p>
     * @return a default list selection model.
     */
    public ListSelectionModel getSelectionModel() {return SELECTION_MODEL;}

    /**
     * Ignored.
     */
    public void setSelectionModel(ListSelectionModel model) {}



    // - Column enumeration --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns an enumeration on all visible columns.
     * @return an enumeration on all visible columns.
     */
    public Enumeration getColumns() {return new ColumnEnumeration();}

    public Enumeration getAllColumns() {return columns.elements();}

    /**
     * Browses through the model's visible columns
     * <p>
     * This will enumerate all the elements of {@link FileTableColumnModel#columns}, skipping
     * over any that's marked as invisible.
     * </p>
     * @author Nicolas Rinaudo
     */
    private class ColumnEnumeration implements Enumeration {
        // - Instance fields -------------------------------------------------------------
        // -------------------------------------------------------------------------------
        /** Index of the next available element in the enumeration. */
        private int nextIndex;



        // - Initialisation --------------------------------------------------------------
        // -------------------------------------------------------------------------------
        /**
         * Creates a new column enumeration.
         */
        public ColumnEnumeration() {
            nextIndex = -1;
            findNextElement();
        }

        /**
         * Finds the next visible element in the model.
         */
        private void findNextElement() {
            TableColumn column;

            for(nextIndex++; nextIndex < visibility.length; nextIndex++) {
                column = (TableColumn)columns.get(nextIndex);
                if(visibility[column.getModelIndex()])
                    break;
            }
        }



        // - Enumeration methods ---------------------------------------------------------
        // -------------------------------------------------------------------------------
        /**
         * Returns <code>true</code> if there's a next element in the enumeration.
         * @return <code>true</code> if there's a next element in the enumeration, <code>false</code> otherwise.
         */
        public boolean hasMoreElements() {return nextIndex < visibility.length;}

        /**
         * Returns the next element in the enumeration.
         * @return                        the next element in the enumeration.
         * @throws NoSuchElementException if there is no next element in the enumeration.
         */
        public Object nextElement() {
            // Makes sure we have at least one more element to return.
            if(!hasMoreElements())
                throw new NoSuchElementException();

            // Retrieves the next element.
            TableColumn column;
            column = (TableColumn)columns.get(nextIndex);

            // Looks for the next one.
            findNextElement();

            return column;
        }
    }



    // - Column sorting ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Used to sort the model's columns at boot time.
     * <p>
     * The sort is done by first comparing each column's index as defined in the configuration and,
     * if there's a conflict, by comparing each column's identifier.
     * </p>
     * @author Nicolas Rinaudo
     */
    private class ColumnSorter implements Comparator {
        // - Instance fields -------------------------------------------------------------
        // -------------------------------------------------------------------------------
        /** Defines the columns order. */
        private FileTableConfiguration conf;



        // - Initialisation --------------------------------------------------------------
        // -------------------------------------------------------------------------------
        /**
         * Loads the columns order as defined in the configuration.
         */
        public ColumnSorter(FileTableConfiguration conf) {this.conf = conf;}



        // - Comparator code -------------------------------------------------------------
        // -------------------------------------------------------------------------------
        /**
         * Compares <code>o1</code> and <code>o2</code>.
         */
        public int compare(Object o1, Object o2) {
            int id1;    // Identifier of the first column.
            int id2;    // Identifier of the second column.
            int index1; // Index of the first column as defined in the configuration.
            int index2; // Index of the second column as defined in the configuration.

            // Retrieves the two columns' indexes and identifiers.
            index1 = conf.getPosition(id1 = ((TableColumn)o1).getModelIndex());
            index2 = conf.getPosition(id2 = ((TableColumn)o2).getModelIndex());

            // Sort by index, then by identifier.
            if(index1 < index2)
                return -1;
            if(index1 == index2) {
                if(id1 < id2)
                    return -1;
                if (id1 == id2)
                    return 0;
            }
            return 1;
        }
    }
}
