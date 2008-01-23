/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import com.mucommander.text.Translator;

/**
 * Defines columns related constants.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class Columns {
    /** Identifier of the extension column. */
    public final static int EXTENSION                 = 0;
    /** Identifier of the name column. */
    public final static int NAME                      = 1;
    /** Identifier of the size column. */
    public final static int SIZE                      = 2;
    /** Identifier of the date column. */
    public final static int DATE                      = 3;
    /** Identifier of the permissions column. */
    public final static int PERMISSIONS               = 4;
    /** Identifier of the owner column. */
    public final static int OWNER                     = 5;
    /** Identifier of the group column. */
    public final static int GROUP                     = 6;

    /** Total number of columns */
    public final static int COLUMN_COUNT              = 7;

    /** Raw column names (not localized / not for display) */
    private static final String[] COLUMN_NAMES = {
        "extension",
        "name",
        "size",
        "date",
        "permissions",
        "owner",
        "group"
    };

    /**
     * Returns the specified column's identifier as a string.
     * <p>
     * Note that the returned identifier should never be used as a displayable label, as it's not localised.
     * It's only meant for internal use.
     * </p>
     * @param  column identifier of the column whose name should be retrieved.
     * @return        the specified column's identifier as a string.
     */
    public static String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    /** Localized column labels */
    private static final String[] COLUMN_LABELS = {
        Translator.get(COLUMN_NAMES[EXTENSION]),
        Translator.get(COLUMN_NAMES[NAME]),
        Translator.get(COLUMN_NAMES[SIZE]),
        Translator.get(COLUMN_NAMES[DATE]),
        Translator.get(COLUMN_NAMES[PERMISSIONS]),
        Translator.get(COLUMN_NAMES[OWNER]),
        Translator.get(COLUMN_NAMES[GROUP])
    };

    /**
     * Returns the localised name of the specified column.
     * @param  column column whose name should be returned.
     * @return        the localised name of the specified column.
     */
    public static String getColumnLabel(int column) {
        return COLUMN_LABELS[column];
    }

    /** Standard minimum column width */
    private final static int STANDARD_MINIMUM_WIDTH    = 2 * CellLabel.CELL_BORDER_WIDTH;

    /**
     * Returns the minimum width of the specified column.
     * @param  column column whose minimum width should be returned.
     * @return        the minimum width of the specified column.
     */
    public static int getMinimumColumnWidth(int column) {
        return MINIMUM_COLUMN_WIDTHS[column];
    }

    /** Minimum width of each column */
    private static final int MINIMUM_COLUMN_WIDTHS[] = {
        STANDARD_MINIMUM_WIDTH,         // Extension
        40,                             // Name
        STANDARD_MINIMUM_WIDTH,         // Size
        STANDARD_MINIMUM_WIDTH,         // Date
        STANDARD_MINIMUM_WIDTH,         // Permissions
        STANDARD_MINIMUM_WIDTH,         // Owner
        STANDARD_MINIMUM_WIDTH,         // Group 
    };

    /**
     * Prevents instanciations of the class.
     */
    private Columns() {}
}
