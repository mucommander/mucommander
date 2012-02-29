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

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.icon.CustomFileIconProvider;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.quicksearch.QuickSearch;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeCache;
import com.mucommander.ui.theme.ThemeListener;


/**
 * The custom <code>TableCellRenderer</code> class used by {@link FileTable} to render all table cells.
 *
 * <p>Quote from Sun's Javadoc : The table class defines a single cell renderer and uses it as a 
 * as a rubber-stamp for rendering all cells in the table;  it renders the first cell,
 * changes the contents of that cell renderer, shifts the origin to the new location, re-draws it, and so on.</p>
 *
 * <p>This <code>TableCellRender</code> is written from scratch instead of overridding <code>DefaultTableCellRender</code>
 * to provide a more efficient (and more specialized) implementation: each column is rendered using a dedicated 
 * {@link com.mucommander.ui.main.table.CellLabel CellLabel} which takes into account the column's specificities.
 * Having a dedicated for each column avoids calling the label's <code>set</code> methods (alignment, border, font...) 
 * each time {@link #getTableCellRendererComponent(javax.swing.JTable, Object, boolean, boolean, int, int)}}
 * is invoked, making cell rendering faster.
 *
 * <p>Contrarily to <code>DefaultTableCellRender</code>, <code>FileTableCellRenderer</code> does not extend JLabel,
 * instead the dedicated {@link CellLabel} class is used to render cells, making the implementation
 * less confusing IMO.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class FileTableCellRenderer implements TableCellRenderer, ThemeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileTableCellRenderer.class);
	
    private FileTable table;
    private FileTableModel tableModel;

    /** Custom JLabel that render specific column cells */
    private CellLabel[] cellLabels = new CellLabel[Column.values().length];


    public FileTableCellRenderer(FileTable table) {
    	this.table = table;
        this.tableModel = table.getFileTableModel();

        // Create a label for each column
        for(Column c : Column.values())
            this.cellLabels[c.ordinal()] = new CellLabel();

        // Set labels' font.
        setCellLabelsFont(ThemeCache.tableFont);

        // Set labels' text alignment
        cellLabels[Column.EXTENSION.ordinal()].setHorizontalAlignment(CellLabel.CENTER);
        cellLabels[Column.NAME.ordinal()].setHorizontalAlignment(CellLabel.LEFT);
        cellLabels[Column.SIZE.ordinal()].setHorizontalAlignment(CellLabel.RIGHT);
        cellLabels[Column.DATE.ordinal()].setHorizontalAlignment(CellLabel.RIGHT);
        cellLabels[Column.PERMISSIONS.ordinal()].setHorizontalAlignment(CellLabel.LEFT);
        cellLabels[Column.OWNER.ordinal()].setHorizontalAlignment(CellLabel.LEFT);
        cellLabels[Column.GROUP.ordinal()].setHorizontalAlignment(CellLabel.LEFT);

        // Listens to certain configuration variables
        ThemeCache.addThemeListener(this);
    }


    /**
     * Returns the font used to render all table cells.
     */
    public static Font getCellFont() {
        return ThemeCache.tableFont;
    }

	
    /**
     * Sets CellLabels' font to the current one.
     */
    private void setCellLabelsFont(Font newFont) {
        // Set custom font
        for(Column c : Column.values()) {
            // No need to set extension label's font as this label renders only icons and no text
            if(c==Column.EXTENSION)
                continue;

            cellLabels[c.ordinal()].setFont(newFont);
        }
    }


    ///////////////////////////////
    // TableCellRenderer methods //
    ///////////////////////////////

    private static int getColorIndex(int row, AbstractFile file, FileTableModel tableModel) {
        // Parent directory.
        if(row==0 && tableModel.hasParentFolder())
            return ThemeCache.FOLDER;

        // Marked file.
        if(tableModel.isRowMarked(row))
            return ThemeCache.MARKED;

        // Symlink.
        if(file.isSymlink())
            return ThemeCache.SYMLINK;

        // Hidden file.
        if(file.isHidden())
            return ThemeCache.HIDDEN_FILE;

        // Directory.
        if(file.isDirectory())
            return ThemeCache.FOLDER;

        // Archive.
        if(file.isBrowsable())
            return ThemeCache.ARCHIVE;

        // Plain file.
        return ThemeCache.PLAIN_FILE;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        Column                column;
        int                   colorIndex;
        int                   focusedIndex;
        int                   selectedIndex;
        CellLabel             label;
        AbstractFile          file;
        boolean               matches;
        QuickSearch 		  search;

        // Need to check that row index is not out of bounds because when the folder
        // has just been changed, the JTable may try to repaint the old folder and
        // ask for a row index greater than the length if the old folder contained more files
        if(rowIndex < 0 || rowIndex >= tableModel.getRowCount())
            return null;

        // Sanity check.
        file = tableModel.getCachedFileAtRow(rowIndex);
        if(file==null) {
            LOGGER.debug("tableModel.getCachedFileAtRow("+ rowIndex +") RETURNED NULL !");
            return null;
        }

        search = this.table.getQuickSearch();
        if(!table.hasFocus())
            matches = true;
        else {
            if(search.isActive())
                matches = search.matches(this.table.getFileNameAtRow(rowIndex));
            else
                matches = true;
        }

        // Retrieves the various indexes of the colors to apply.
        // Selection only applies when the table is the active one
        selectedIndex =  (isSelected && ((FileTable)table).isActiveTable()) ? ThemeCache.SELECTED : ThemeCache.NORMAL;
        focusedIndex  = table.hasFocus() ? ThemeCache.ACTIVE : ThemeCache.INACTIVE;
        colorIndex    = getColorIndex(rowIndex, file, tableModel);

        column = Column.valueOf(table.convertColumnIndexToModel(columnIndex));
        label = cellLabels[column.ordinal()];

        // Extension/icon column: return ImageIcon instance
        if(column == Column.EXTENSION) {
            // Set file icon (parent folder icon if '..' file)
            label.setIcon(rowIndex ==0 && tableModel.hasParentFolder()
                    ?IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.PARENT_FOLDER_ICON_NAME, FileIcons.getScaleFactor())
                    :FileIcons.getFileIcon(file));
        }
        // Any other column (name, date or size)
        else {
            String text = (String)value;
            if(matches || isSelected)
                label.setForeground(ThemeCache.foregroundColors[focusedIndex][selectedIndex][colorIndex]);
            else
                label.setForeground(ThemeCache.unmatchedForeground);

            // Set the label's text, before calculating it width
            label.setText(text);

            // If label's width is larger than the column width:
            // - truncate the text from the center and equally to the left and right sides, adding an ellipsis ('...')
            // where characters have been removed. This allows both the start and end of filename to be visible.
            // - set a tooltip text that will display the whole text when mouse is over the label
            if (table.getColumnModel().getColumn(columnIndex).getWidth() < label.getPreferredSize().getWidth()) {
                String leftText = text.substring(0, text.length()/2);
                String rightText = text.substring(text.length()/2, text.length());

                while(table.getColumnModel().getColumn(columnIndex).getWidth() < label.getPreferredSize().getWidth()
                   && leftText.length()>0 && rightText.length()>0) {    // Prevents against going out of bounds

                    if(leftText.length()>rightText.length())
                        leftText = leftText.substring(0, leftText.length()-1);
                    else
                        rightText = rightText.substring(1, rightText.length());

                    label.setText(leftText+"..."+rightText);
                }

                // Set the toop
                label.setToolTipText(text);
            }
            // Have to set it to null otherwise the defaultRender sets the tooltip text to the last one
            // specified
            else
                label.setToolTipText(null);
        }

        // Set background color depending on whether the row is selected or not, and whether the table has focus or not
        if(selectedIndex == ThemeCache.SELECTED)
            label.setBackground(ThemeCache.backgroundColors[focusedIndex][ThemeCache.SELECTED], ThemeCache.backgroundColors[focusedIndex][ThemeCache.SECONDARY]);
        else if(matches) {
            if(table.hasFocus() && search.isActive())
                label.setBackground(ThemeCache.backgroundColors[focusedIndex][ThemeCache.NORMAL]);
            else
                label.setBackground(ThemeCache.backgroundColors[focusedIndex][(rowIndex % 2 == 0) ? ThemeCache.NORMAL : ThemeCache.ALTERNATE]);
        }
        else
            label.setBackground(ThemeCache.unmatchedBackground);

        if(selectedIndex == ThemeCache.SELECTED)
            label.setOutline(table.hasFocus() ? ThemeCache.activeOutlineColor : ThemeCache.inactiveOutlineColor);
        else
            label.setOutline(null);

        return label;
    }



    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     */
    public void colorChanged(ColorChangedEvent event) {
        table.repaint();
    }

    /**
     * Receives theme font changes notifications.
     */
    public void fontChanged(FontChangedEvent event) {
        if(event.getFontId() == Theme.FILE_TABLE_FONT) {
            setCellLabelsFont(ThemeCache.tableFont);
        }
    }
}
