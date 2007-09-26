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
 * Defines columns related constants.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public interface Columns {
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
    /** Minimum width of the name column. */
    public final static int MINIMUM_NAME_WIDTH        = 40;
    /** Minimum width of the extension column. */
    public final static int MINIMUM_EXTENSION_WIDTH   = 2 * CellLabel.CELL_BORDER_WIDTH;
    /** Minimum width of the date column. */
    public final static int MINIMUM_DATE_WIDTH        = 2 * CellLabel.CELL_BORDER_WIDTH;
    /** Minimum width of the size column. */
    public final static int MINIMUM_SIZE_WIDTH        = 2 * CellLabel.CELL_BORDER_WIDTH;
    /** Minimumn width of the permissions column. */
    public final static int MINIMUM_PERMISSIONS_WIDTH = 2 * CellLabel.CELL_BORDER_WIDTH;
}
