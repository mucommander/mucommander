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

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.icon.CustomFileIconProvider;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.theme.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


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

    private FileTable table;
    private FileTableModel tableModel;

    /** Custom JLabel that render specific column cells */
    private CellLabel[] cellLabels = new CellLabel[Columns.COLUMN_COUNT];

    // - Color definitions -----------------------------------------------------------
    // -------------------------------------------------------------------------------
    private static Color[][][] foregroundColors;
    private static Color[][]   backgroundColors;
    private static Color       unmatchedForeground;
    private static Color       unmatchedBackground;
    private static Color       activeOutlineColor;
    private static Color       inactiveOutlineColor;
    private static final int NORMAL               = 0;
    private static final int SELECTED             = 1;
    private static final int ALTERNATE            = 2;
    private static final int SECONDARY            = 3;
    private static final int INACTIVE             = 0;
    private static final int ACTIVE               = 1;
    private static final int HIDDEN_FILE          = 0;
    private static final int FOLDER               = 1;
    private static final int ARCHIVE              = 2;
    private static final int SYMLINK              = 3;
    private static final int MARKED               = 4;
    private static final int PLAIN_FILE           = 5;

    // - Font definitions ------------------------------------------------------------
    // -------------------------------------------------------------------------------
    private static Font font;

    // - Initialisation --------------------------------------------------------------
    // -------------------------------------------------------------------------------
    static {
        foregroundColors = new Color[2][2][6];
        backgroundColors = new Color[2][4];

        // Active background colors.
        backgroundColors[ACTIVE][NORMAL]    = ThemeManager.getCurrentColor(Theme.FILE_TABLE_BACKGROUND_COLOR);
        backgroundColors[ACTIVE][SELECTED]  = ThemeManager.getCurrentColor(Theme.FILE_TABLE_SELECTED_BACKGROUND_COLOR);
        backgroundColors[ACTIVE][ALTERNATE] = ThemeManager.getCurrentColor(Theme.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR);
        backgroundColors[ACTIVE][SECONDARY] = ThemeManager.getCurrentColor(Theme.FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR);

        // Inactive background colors.
        backgroundColors[INACTIVE][NORMAL]    = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR);
        backgroundColors[INACTIVE][SELECTED]  = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR);
        backgroundColors[INACTIVE][ALTERNATE] = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR);
        backgroundColors[INACTIVE][SECONDARY] = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR);

        // Normal foreground foregroundColors.
        foregroundColors[ACTIVE][NORMAL][HIDDEN_FILE]     = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][FOLDER]          = ThemeManager.getCurrentColor(Theme.FOLDER_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][ARCHIVE]         = ThemeManager.getCurrentColor(Theme.ARCHIVE_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][SYMLINK]         = ThemeManager.getCurrentColor(Theme.SYMLINK_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][MARKED]          = ThemeManager.getCurrentColor(Theme.MARKED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][PLAIN_FILE]      = ThemeManager.getCurrentColor(Theme.FILE_FOREGROUND_COLOR);

        // Normal unfocused foreground foregroundColors.
        foregroundColors[INACTIVE][NORMAL][HIDDEN_FILE]    = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][FOLDER]         = ThemeManager.getCurrentColor(Theme.FOLDER_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][ARCHIVE]        = ThemeManager.getCurrentColor(Theme.ARCHIVE_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][SYMLINK]        = ThemeManager.getCurrentColor(Theme.SYMLINK_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][MARKED]         = ThemeManager.getCurrentColor(Theme.MARKED_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][PLAIN_FILE]     = ThemeManager.getCurrentColor(Theme.FILE_INACTIVE_FOREGROUND_COLOR);

        // Selected foreground foregroundColors.
        foregroundColors[ACTIVE][SELECTED][HIDDEN_FILE]   = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][FOLDER]        = ThemeManager.getCurrentColor(Theme.FOLDER_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][ARCHIVE]       = ThemeManager.getCurrentColor(Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][SYMLINK]       = ThemeManager.getCurrentColor(Theme.SYMLINK_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][MARKED]        = ThemeManager.getCurrentColor(Theme.MARKED_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][PLAIN_FILE]    = ThemeManager.getCurrentColor(Theme.FILE_SELECTED_FOREGROUND_COLOR);

        // Selected unfocused foreground foregroundColors.
        foregroundColors[INACTIVE][SELECTED][HIDDEN_FILE]  = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][FOLDER]       = ThemeManager.getCurrentColor(Theme.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][ARCHIVE]      = ThemeManager.getCurrentColor(Theme.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][SYMLINK]      = ThemeManager.getCurrentColor(Theme.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][MARKED]       = ThemeManager.getCurrentColor(Theme.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][PLAIN_FILE]   = ThemeManager.getCurrentColor(Theme.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR);

        unmatchedForeground                                = ThemeManager.getCurrentColor(Theme.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR);
        unmatchedBackground                                = ThemeManager.getCurrentColor(Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR);
        font                                               = ThemeManager.getCurrentFont(Theme.FILE_TABLE_FONT);

        activeOutlineColor                                 = ThemeManager.getCurrentColor(Theme.FILE_TABLE_SELECTED_OUTLINE_COLOR);
        inactiveOutlineColor                               = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR);
    }


    public FileTableCellRenderer(FileTable table) {
    	this.table = table;
        this.tableModel = table.getFileTableModel();

        // Create a label for each column
        for(int i=0; i<Columns.COLUMN_COUNT; i++)
            this.cellLabels[i] = new CellLabel();

        // Set labels' font.
        setCellLabelsFont(font);

        // Set labels' text alignment
        cellLabels[Columns.EXTENSION].setHorizontalAlignment(CellLabel.CENTER);
        cellLabels[Columns.NAME].setHorizontalAlignment(CellLabel.LEFT);
        cellLabels[Columns.SIZE].setHorizontalAlignment(CellLabel.RIGHT);
        cellLabels[Columns.DATE].setHorizontalAlignment(CellLabel.RIGHT);
        cellLabels[Columns.PERMISSIONS].setHorizontalAlignment(CellLabel.LEFT);
        cellLabels[Columns.OWNER].setHorizontalAlignment(CellLabel.LEFT);
        cellLabels[Columns.GROUP].setHorizontalAlignment(CellLabel.LEFT);

        // Listens to certain configuration variables
        ThemeManager.addCurrentThemeListener(this);
    }


    /**
     * Returns the font used to render all table cells.
     */
    public static Font getCellFont() {
        return font;
    }

	
    /**
     * Sets CellLabels' font to the current one.
     */
    private void setCellLabelsFont(Font newFont) {
        // Set custom font
        for(int i=0; i<Columns.COLUMN_COUNT; i++) {
            // No need to set extension label's font as this label renders only icons and no text
            if(i==Columns.EXTENSION)
                continue;

            cellLabels[i].setFont(newFont);
        }
    }


    ///////////////////////////////
    // TableCellRenderer methods //
    ///////////////////////////////

    private static int getColorIndex(int row, AbstractFile file, FileTableModel tableModel) {
        // Parent directory.
        if(row==0 && tableModel.hasParentFolder())
            return FOLDER;

        // Marked file.
        if(tableModel.isRowMarked(row))
            return MARKED;

        // Symlink.
        if(file.isSymlink())
            return SYMLINK;

        // Hidden file.
        if(file.isHidden())
            return HIDDEN_FILE;

        // Directory.
        if(file.isDirectory())
            return FOLDER;

        // Archive.
        if(file.isBrowsable())
            return ARCHIVE;

        // Plain file.
        return PLAIN_FILE;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int                   columnId;
        int                   colorIndex;
        int                   focusedIndex;
        int                   selectedIndex;
        CellLabel             label;
        AbstractFile          file;
        boolean               matches;
        FileTable.QuickSearch search;

        // Need to check that row index is not out of bounds because when the folder
        // has just been changed, the JTable may try to repaint the old folder and
        // ask for a row index greater than the length if the old folder contained more files
        if(row < 0 || row >= tableModel.getRowCount())
            return null;

        // Sanity check.
        file = tableModel.getCachedFileAtRow(row);
        if(file==null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("tableModel.getCachedFileAtRow("+row+") RETURNED NULL !"); 
            return null;
        }

        search = this.table.getQuickSearch();
        if(!table.hasFocus())
            matches = true;
        else {
            if(search.isActive())
                matches = search.matches((row == 0 && tableModel.hasParentFolder()) ? ".." : tableModel.getFileAtRow(row).getName());
            else
                matches = true;
        }

        // Retrieves the various indexes of the colors to apply.
        // Selection only applies when the table is the active one
        selectedIndex =  (isSelected && ((FileTable)table).isActiveTable()) ? SELECTED : NORMAL;
        focusedIndex  = table.hasFocus() ? ACTIVE : INACTIVE;
        colorIndex    = getColorIndex(row, file, tableModel);

        columnId = table.convertColumnIndexToModel(column);
        label = cellLabels[columnId];

        // Extension/icon column: return ImageIcon instance
        if(columnId == Columns.EXTENSION) {
            // Set file icon (parent folder icon if '..' file)
            label.setIcon(
                                   row==0 && tableModel.hasParentFolder()?
                                   IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.PARENT_FOLDER_ICON_NAME, FileIcons.getScaleFactor())
                                   :FileIcons.getFileIcon(file)
                                   );
        }
        // Any other column (name, date or size)
        else {
            String text = (String)value;
            if(matches || isSelected)
                label.setForeground(foregroundColors[focusedIndex][selectedIndex][colorIndex]);
            else
                label.setForeground(unmatchedForeground);

            // If component's preferred width is bigger than column width then the component is not entirely
            // visible so we set a tooltip text that will display the whole text when mouse is over the 
            // component
            if (table.getColumnModel().getColumn(column).getWidth() < label.getPreferredSize().getWidth())
                label.setToolTipText(text);
            // Have to set it to null otherwise the defaultRender sets the tooltip text to the last one
            // specified
            else
                label.setToolTipText(null);


            // Set label's text
            label.setText(text); 
        }

        // Set background color depending on whether the row is selected or not, and whether the table has focus or not
        if(selectedIndex == SELECTED)
            label.setBackground(backgroundColors[focusedIndex][SELECTED], backgroundColors[focusedIndex][SECONDARY]);
        else if(matches) {
            if(table.hasFocus() && search.isActive())
                label.setBackground(backgroundColors[focusedIndex][NORMAL]);
            else
                label.setBackground(backgroundColors[focusedIndex][(row % 2 == 0) ? NORMAL : ALTERNATE]);
        }
        else
            label.setBackground(unmatchedBackground);

        if(selectedIndex == SELECTED)
            label.setOutline(table.hasFocus() ? activeOutlineColor : inactiveOutlineColor);
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
        switch(event.getColorId()) {
            // Plain file color.
        case Theme.FILE_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][PLAIN_FILE] = event.getColor();
            break;

            // Selected file color.
        case Theme.FILE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][PLAIN_FILE] = event.getColor();
            break;

            // Hidden files.
        case Theme.HIDDEN_FILE_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][HIDDEN_FILE] = event.getColor();
            break;

            // Selected hidden files.
        case Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][HIDDEN_FILE] = event.getColor();
            break;

            // Folders.
        case Theme.FOLDER_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][FOLDER] = event.getColor();
            break;

            // Selected folders.
        case Theme.FOLDER_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][FOLDER] = event.getColor();
            break;

            // Archives.
        case Theme.ARCHIVE_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][ARCHIVE] = event.getColor();
            break;

            // Selected archives.
        case Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][ARCHIVE] = event.getColor();
            break;

            // Symlinks.
        case Theme.SYMLINK_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][SYMLINK] = event.getColor();
            break;

            // Selected symlinks.
        case Theme.SYMLINK_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][SYMLINK] = event.getColor();
            break;

            // Marked files.
        case Theme.MARKED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][MARKED] = event.getColor();
            break;

            // Selected marked files.
        case Theme.MARKED_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][MARKED] = event.getColor();
            break;

            // Plain file color.
        case Theme.FILE_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][PLAIN_FILE] = event.getColor();
            break;

            // Selected file color.
        case Theme.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][PLAIN_FILE] = event.getColor();
            break;

            // Hidden files.
        case Theme.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][HIDDEN_FILE] = event.getColor();
            break;

            // Selected hidden files.
        case Theme.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][HIDDEN_FILE] = event.getColor();
            break;

            // Folders.
        case Theme.FOLDER_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][FOLDER] = event.getColor();
            break;

            // Selected folders.
        case Theme.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][FOLDER] = event.getColor();
            break;

            // Archives.
        case Theme.ARCHIVE_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][ARCHIVE] = event.getColor();
            break;

            // Selected archives.
        case Theme.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][ARCHIVE] = event.getColor();
            break;

            // Symlinks.
        case Theme.SYMLINK_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][SYMLINK] = event.getColor();
            break;

            // Selected symlinks.
        case Theme.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][SYMLINK] = event.getColor();
            break;

            // Marked files.
        case Theme.MARKED_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][MARKED] = event.getColor();
            break;

            // Selected marked files.
        case Theme.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][MARKED] = event.getColor();
            break;

            // Unmatched foreground
        case Theme.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR:
            unmatchedForeground = event.getColor();
            break;

            // Unmached background
        case Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR:
            unmatchedBackground = event.getColor();
            break;

            // Active normal background.
        case Theme.FILE_TABLE_BACKGROUND_COLOR:
            backgroundColors[ACTIVE][NORMAL] = event.getColor();
            break;

            // Active selected background.
        case Theme.FILE_TABLE_SELECTED_BACKGROUND_COLOR:
            backgroundColors[ACTIVE][SELECTED] = event.getColor();
            break;

            // Active alternate background.
        case Theme.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR:
            backgroundColors[ACTIVE][ALTERNATE] = event.getColor();
            break;

            // Inactive normal background.
        case Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR:
            backgroundColors[INACTIVE][NORMAL] = event.getColor();
            break;

            // Inactive selected background.
        case Theme.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR:
            backgroundColors[INACTIVE][SELECTED] = event.getColor();
            break;

            // Inactive alternate background.
        case Theme.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR:
            backgroundColors[INACTIVE][ALTERNATE] = event.getColor();
            break;

            // Active selection outline.
        case Theme.FILE_TABLE_SELECTED_OUTLINE_COLOR:
            activeOutlineColor = event.getColor();
            break;

            // Inactive selection outline.
        case Theme.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR:
            inactiveOutlineColor = event.getColor();
            break;

            // Secondary background color.
        case Theme.FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR:
            backgroundColors[ACTIVE][SECONDARY] = event.getColor();
            break;

            // Inactive secondary background color.
        case Theme.FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR:
            backgroundColors[INACTIVE][SECONDARY] = event.getColor();
            break;

        default:
            return;
        }
        table.repaint();
    }

    /**
     * Receives theme font changes notifications.
     */
    public void fontChanged(FontChangedEvent event) {
        if(event.getFontId() == Theme.FILE_TABLE_FONT) {
            font = event.getFont();
            setCellLabelsFont(font);
        }
    }
}
