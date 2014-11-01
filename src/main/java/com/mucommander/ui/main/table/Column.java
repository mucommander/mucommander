/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.commons.file.util.FileComparator;
import com.mucommander.text.Translator;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates and describes the different columns used in the {@link FileTable}.
 *
 * @author Maxence Bernard
 */
public enum Column {

    EXTENSION(true, true, FileComparator.EXTENSION_CRITERION, "ToggleExtensionColumn", "SortByExtension"),
    NAME(false, true, FileComparator.NAME_CRITERION, null, "SortByName"),
    SIZE(true, true, FileComparator.SIZE_CRITERION, "ToggleSizeColumn", "SortBySize"),
    DATE(true, true, FileComparator.DATE_CRITERION, "ToggleDateColumn", "SortByDate"),
    PERMISSIONS(true, true, FileComparator.PERMISSIONS_CRITERION, "TogglePermissionsColumn", "SortByPermissions"),
    OWNER(true, false, FileComparator.OWNER_CRITERION, "ToggleOwnerColumn", "SortByOwner"),
    GROUP(true, false, FileComparator.GROUP_CRITERION, "ToggleGroupColumn", "SortByGroup");

    private static final Map<Integer, Column> ORDINAL_TO_ENUM_MAPPING = new HashMap<Integer,Column>(){{
      for (Column column : Column.values()) {
        put(column.ordinal(), column);
      }
    }};

    /** Standard minimum column width */
    private final static int STANDARD_MINIMUM_WIDTH = 2 * CellLabel.CELL_BORDER_WIDTH;

    private String label;
    private int minimumWidth;
    private boolean showByDefault;
    private int fileComparatorCriterion;
    private String toggleActionId;
    private String sortByActionId;

    private Column(boolean hasMinimumWidth, boolean showByDefault, int fileComparatorCriterion, String toggleActionId, String sortByActionId) {
        this.label = Translator.get(toString());
        this.minimumWidth = hasMinimumWidth?STANDARD_MINIMUM_WIDTH:0;
        this.showByDefault = showByDefault;
        this.fileComparatorCriterion = fileComparatorCriterion;
        this.toggleActionId = toggleActionId;
        this.sortByActionId = sortByActionId;
    }

    /**
     * Returns this column's localized label.
     *
     * @return this column's localized label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns this column's minimum width.
     *
     * @return this column's minimum width.
     */
    public int getMinimumColumnWidth() {
        return minimumWidth;
    }

    /**
     * Returns <code>true</code> if this column should be displayed, unless configured otherwise.
     *
     * @return <code>true</code> if this column should be displayed, unless configured otherwise.
     */
    public boolean showByDefault() {
        return showByDefault;
    }

    /**
     * Returns the {@link FileComparator} criterion used for sorting column values.
     *
     * @return the {@link FileComparator} criterion used for sorting column values.
     */
    public int getFileComparatorCriterion() {
        return fileComparatorCriterion;
    }

    /**
     * Returns the column instance that has the specified {@link #ordinal()} value.
     *
     * @param ordinal the column's ordinal value
     * @return the column instance that has the specified {@link #ordinal()} value.
     */
    public static Column valueOf(int ordinal) {
      return ORDINAL_TO_ENUM_MAPPING.get(ordinal);
    }

    /**
     * Returns the ID of the action that allows this column to be shown/hidden.
     * Caution: the {@link #NAME} column cannot be toggled, therefore the returned action ID is <code>null</code>.
     *
     * @return the ID of the action that allows this column to be shown/hidden.
     */
    public String getToggleColumnActionId() {
        return toggleActionId;
    }

    /**
     * Returns the ID of the action that allows to sort the table by this column.
     *
     * @return the ID of the action that allows to sort the table by this column.
     */
    public String getSortByColumnActionId() {
        return sortByActionId;
    }
}
