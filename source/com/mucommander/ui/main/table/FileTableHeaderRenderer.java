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

import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;


/**
 * Custom table header renderer that displays an icon to indicate the current sort criterion and the sort order
 * (ascending or descending).  
 *
 * @author Maxence Bernard
 */
public class FileTableHeaderRenderer extends DefaultTableCellRenderer {

    private boolean ascendingOrder = false;
    private boolean isCurrent = false;

    private final static ImageIcon ASCENDING_ICON = IconManager.getIcon(IconManager.COMMON_ICON_SET, "arrow_up.png");
    private final static ImageIcon DESCENDING_ICON = IconManager.getIcon(IconManager.COMMON_ICON_SET, "arrow_down.png");


    public FileTableHeaderRenderer() {
        // These properties can be set only once

        // Icon should be on the right
        setHorizontalTextPosition(LEFT);
        // Increase gap size between text and icon (defaut is 4 pixels)
        setIconTextGap(6);
        // Note: the label is left-aligned by default
        setHorizontalAlignment(JLabel.CENTER);
    }


    /**
     * Specifies whether the column corresponding to this renderer is the currently selected one, i.e. the one the table
     * is currently sorted by.
     *
     * @param isCurrent true if the column corresponding to this renderer is the currently selected one.
     */
    public void setCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    /**
     * Specifies whether if the sort order of the column corresponding to this renderer is ascending or descending.
     *
     * @param isAscending true if the order is ascending, false for descending
     */
    public void setSortOrder(boolean isAscending) {
        this.ascendingOrder = isAscending;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Note: the label is returned by DefaultTableHeaderRenderer#getTableCellRendererComponent() is in fact this
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                label.setForeground(header.getForeground());
                label.setBackground(header.getBackground());
                label.setFont(header.getFont());
            }
        }

        // Use borders made specifically for table headers
        Border border = UIManager.getBorder("TableHeader.cellBorder");
        label.setBorder(border);

        // Add a tooltip as headers are sometimes too small for the text to fit entirely
        label.setToolTipText((String)value);

        if(isCurrent) {
            // This header is the currently selected one
            label.setIcon(ascendingOrder? ASCENDING_ICON : DESCENDING_ICON);
        }
        else {
            // The renderer component acts as a rubber-stamp, therefore the icon value needs to be set to null explicitely
            // as it might still hold a previous value
            label.setIcon(null);
        }

        return label;
    }
}
