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


package com.mucommander.ui.main.table;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import com.mucommander.ui.icon.IconManager;


/**
 * Custom table header renderer that displays an icon to indicate the current sort criterion and the sort order
 * (ascending or descending).  
 *
 * @author Maxence Bernard
 */
public class FileTableHeaderRenderer extends DefaultTableCellRenderer {

    private final static ImageIcon ASCENDING_ICON = IconManager.getIcon(IconManager.COMMON_ICON_SET, "arrow_up.png");
    private final static ImageIcon DESCENDING_ICON = IconManager.getIcon(IconManager.COMMON_ICON_SET, "arrow_down.png");

    @Override
    protected void paintComponent(Graphics g) {
        // On macOS with the Aqua LaF (com.apple.laf.AquaLookAndFeel) and
        // apple.awt.application.appearance=system in dark mode, the native UI delegate paints
        // the component black regardless of setBackground(). We fill the background ourselves
        // first so the correct color is always visible.
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }


    public FileTableHeaderRenderer() {
        // These properties can be set only once

        // Icon should be on the right
        setHorizontalTextPosition(LEFT);
        // Increase gap size between text and icon (defaut is 4 pixels)
        setIconTextGap(6);
        // Note: the label is left-aligned by default
        setHorizontalAlignment(JLabel.CENTER);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Note: the label is returned by DefaultTableHeaderRenderer#getTableCellRendererComponent() is in fact this
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                // Read colors from UIManager rather than from the header component directly.
                // On macOS with the Aqua LaF + apple.awt.application.appearance=system in dark mode,
                // UIManager returns static light-mode values (white bg, black fg) which are correct
                // for painting — the native Aqua painter would otherwise override them with black.
                java.awt.Color bg = UIManager.getColor("TableHeader.background");
                java.awt.Color fg = UIManager.getColor("TableHeader.foreground");
                if (bg == null) {
                    bg = UIManager.getColor("Table.background");
                }
                if (fg == null) {
                    fg = UIManager.getColor("Table.foreground");
                }
                if (bg == null) {
                    bg = header.getBackground();
                }
                if (fg == null) {
                    fg = header.getForeground();
                }
                label.setForeground(fg);
                label.setBackground(bg);
                label.setFont(header.getFont());
            }

            FileTable fileTable = (FileTable)table;
            if (fileTable.getSortInfo().getCriterion()==Column.valueOf(fileTable.convertColumnIndexToModel(column))) {
                // This header is the currently selected one
                label.setIcon(fileTable.getSortInfo().getAscendingOrder()? ASCENDING_ICON : DESCENDING_ICON);
                Font font = label.getFont();
                label.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
            }  else {
                // The renderer component acts as a rubber-stamp, therefore the icon value needs to be set to null explicitly
                // as it might still hold a previous value
                label.setIcon(null);
                Font font = label.getFont();
                label.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD));
            }
        }

        // Use borders made specifically for table headers
        Border border = UIManager.getBorder("TableHeader.cellBorder");
        label.setBorder(border);

        // Add a tooltip as headers are sometimes too small for the text to fit entirely
        label.setToolTipText((String)value);

        return label;
    }
}
