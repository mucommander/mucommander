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

/**
 * Describes a file table's initial configuration.
 * @author Nicolas Rinaudo
 */
public class FileTableConfiguration {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Each column's visibility status. */
    private boolean[] visibility;
    /** Initial width of each column. */
    private int[]     width;
    /** Columns initial order. */
    private int[]     order;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table configuration.
     */
    public FileTableConfiguration() {
        visibility = new boolean[Columns.COLUMN_COUNT];
        width      = new int[Columns.COLUMN_COUNT];
        order      = new int[Columns.COLUMN_COUNT];
    }



    // - Visibility access ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the specified column is visible.
     * @param  column column whose visibility status should be returned.
     * @return        <code>true</code> if the specified column is visible, <code>false</code> otherwise.
     */
    public boolean isVisible(int column) {return visibility[column];}

    /**
     * Sets the visibility status of the specified column.
     * <p>
     * Note that the {@link #NAME} column's visibility is ignored, it will always be visible.
     * </p>
     * @param column column whose visibility status should be set.
     * @param flag   whether the column should be visible.
     */
    public void setVisible(int column, boolean flag) {visibility[column] = flag;}



    // - Width access --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the initial width of the specified column.
     * @param  column column whose width should be retrieved.
     * @return        the requested column's width.
     */
    public int getWidth(int column) {return width[column];}

    /**
     * Sets the specified column's width.
     * <p>
     * Note that the {@link #NAME} column's width will be ignored, as it depends on the frame's
     * initial dimensions.
     * </p>
     * @param column column whose width should be set.
     * @param value  column's initial width.
     */
    public void setWidth(int column, int value) {width[column] = value;}



    // - Order access --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the desired initial position of the specified column.
     * <p>
     * Note that the returned value isn't necessarily a legal column position. It's used
     * as a comparison value rather than an index.
     * </p>
     * @param  column column whose initial position will be returned.
     * @return        the desired initial position of the specified column.
     */
    public int getPosition(int column) {return order[column];}

    /**
     * Sets the specified column's initial position.
     * @param column   identifier of the column whose position will be set.
     * @param position desired position for the specified column.
     */
    public void setPosition(int column, int position) {order[column] = position;}
}
