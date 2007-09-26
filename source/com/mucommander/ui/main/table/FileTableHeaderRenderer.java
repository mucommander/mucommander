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

import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;


/**
 * Custom table header renderer that displays an icon to indicate the current sort criterion and the sort order
 * (ascending or descending).  
 *
 * @author Maxence Bernard
 */
public class FileTableHeaderRenderer extends DefaultTableCellRenderer implements Columns {

    private boolean ascendingOrder = false;
    private boolean isCurrent = false;

    private final static ImageIcon ASCENDING_ICON = IconManager.getIcon(IconManager.COMMON_ICON_SET, "arrow_up.png");
    private final static ImageIcon DESCENDING_ICON = IconManager.getIcon(IconManager.COMMON_ICON_SET, "arrow_down.png");


    public FileTableHeaderRenderer() {}


    /**
     * Returns <code>true</code> if this header is the one currently selected.
     */
    public void setCurrent(boolean isCurrent) {this.isCurrent = isCurrent;}

    /**
     * Sets the direction of arrow symbolizing the sort order.
     */
    public void setOrder(boolean isAscending) {this.ascendingOrder = isAscending;}


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = new JLabel((String)value);

        if(isCurrent) {
            // Icon should be on the right
            label.setHorizontalTextPosition(LEFT);
            // Increase gap size between text and icon (defaut is 4 pixels)
            label.setIconTextGap(8);
            label.setIcon(ascendingOrder? ASCENDING_ICON : DESCENDING_ICON);
        }

        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                label.setForeground(header.getForeground());
                label.setBackground(header.getBackground());
                label.setFont(header.getFont());
            }
        }

        int col = table.convertColumnIndexToModel(column);
        // Extension and permissions columns are too small for label to fit (ugly otherwise)
//        label.setText(col==EXTENSION||col==PERMISSIONS?"":(String)value);
        label.setText((String)value);
        label.setToolTipText((String)value);
        label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }
}
