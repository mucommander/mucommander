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

package com.mucommander.ui.dialog.pref.theme;

import com.mucommander.text.Translator;
import com.mucommander.ui.border.MutableLineBorder;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.icon.CustomFileIconProvider;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.table.CellLabel;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Nicolas Rinaudo
 */
class FilePreviewPanel extends JScrollPane implements PropertyChangeListener {
    // - Row identifiers ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private static final int FOLDER      = 0;
    private static final int PLAIN_FILE  = 1;
    private static final int ARCHIVE     = 2;
    private static final int HIDDEN_FILE = 3;
    private static final int SYMLINK     = 4;
    private static final int MARKED_FILE = 5;



    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private ThemeData    data;
    private boolean      isActive;
    private PreviewTable table;
    private ImageIcon    symlinkIcon;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new preview panel on the specified theme data.
     * @param data     data to preview.
     * @param isActive whether we're previewing the active or inactive state.
     */
    public FilePreviewPanel(ThemeData data, boolean isActive) {
        super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.data     = data;
        this.isActive = isActive;
        symlinkIcon   = IconManager.getCompositeIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.FILE_ICON_NAME),
                                                     IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.SYMLINK_ICON_NAME));

        initUI();
    }

    /**
     * Initialises the previwer's UI.
     */
    private void initUI() {
        table = new PreviewTable();
        setViewportView(table);

        getViewport().setBackground(data.getColor(isActive ? Theme.FILE_TABLE_BACKGROUND_COLOR : Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR));

        setBorder(new MutableLineBorder(data.getColor(isActive ? Theme.FILE_TABLE_BORDER_COLOR : Theme.FILE_TABLE_INACTIVE_BORDER_COLOR)));

        addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME))
            getViewport().setBackground(data.getColor(isActive ? Theme.FILE_TABLE_BACKGROUND_COLOR : Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR));
        else if(event.getPropertyName().equals(PreviewLabel.BORDER_COLOR_PROPERTY_NAME)) {
            // Some (rather evil) look and feels will change borders outside of muCommander's control,
            // this check is necessary to ensure no exception is thrown.
            if(getBorder() instanceof MutableLineBorder)
                ((MutableLineBorder)getBorder()).setLineColor(data.getColor(isActive ? Theme.FILE_TABLE_BORDER_COLOR : Theme.FILE_TABLE_INACTIVE_BORDER_COLOR));
        }
        else if(!event.getPropertyName().equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME))
            return;
        repaint();
    }


    // - Theme listening -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Resets the table's row height with the new font.
     */
    public void setFont(Font font) {
        if(table != null)
            table.setRowHeight(font);
    }



    // - Preview table -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Used to preview the current table theme.
     * @author Nicolas Rinaudo
     */
    private class PreviewTable extends JTable {
        private PreviewCellRenderer cellRenderer;
        private Dimension           preferredSize;

        /**
         * Creates a new preview table.
         */
        public PreviewTable() {
            super(new String[][] {{"", Translator.get("theme_editor.folder")},
                                  {"", Translator.get("theme_editor.plain_file")},
                                  {"", Translator.get("theme_editor.archive_file")},
                                  {"", Translator.get("theme_editor.hidden_file")},
                                  {"", Translator.get("theme_editor.symbolic_link")},
                                  {"", Translator.get("theme_editor.marked_file")}},
                new String[] {"", Translator.get("preview")});

            // Initialises table painting.
            cellRenderer = new PreviewCellRenderer();
            setShowGrid(false);

            // Initialises the table selection.
            getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            changeSelection(0, 0, false, false);

            // Initialises row dimensions.
            setRowHeight(data.getFont(ThemeData.FILE_TABLE_FONT));
            setIntercellSpacing(new Dimension(0,0));

            // Initialises the table header.
            getTableHeader().setResizingAllowed(false);
            getTableHeader().setReorderingAllowed(false);
            ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        }

        /**
         * Resets column widths.
         */
        public void doLayout() {
            int width;

            getColumnModel().getColumn(0).setWidth(width = getIconWidth());
            getColumnModel().getColumn(1).setWidth(Math.max(getWidth() - width, getLabelWidth()));
        }

        /**
         * Returns the width of the icon label.
         */
        private int getIconWidth() {return (int)FileIcons.getIconDimension().getWidth() + 2 * CellLabel.CELL_BORDER_WIDTH;}

        /**
         * Returns the width of the text label.
         */
        private int getLabelWidth() {
            FontMetrics fm;
            int         rowCount;
            int         width;

            fm       = getFontMetrics(data.getFont(ThemeData.FILE_TABLE_FONT));
            rowCount = getModel().getRowCount();
            width    = 0;
            for(int i = 0; i < rowCount; i++)
                width = Math.max(width, fm.stringWidth(((String)(getModel().getValueAt(i, 1)))) + 2 * CellLabel.CELL_BORDER_WIDTH);
            return width;
        }

        /**
         * Returns the table's preferred size.
         */
        public Dimension getPreferredSize() {return new Dimension(getIconWidth() + 2 * getLabelWidth(), getModel().getRowCount()*getRowHeight());}

        /**
         * Returns the table's preferred size.
         */
        public Dimension getPreferredScrollableViewportSize() {
            if(preferredSize == null)
                preferredSize = getPreferredSize();
            return preferredSize;
        }

        /**
         * Initialises the row height depending on the font.
         */
        private void setRowHeight(Font font) {
            setRowHeight(2 * CellLabel.CELL_BORDER_HEIGHT + Math.max(getFontMetrics(font).getHeight(),
                                                                     (int)FileIcons.getIconDimension().getHeight()));
        }

        /**
         * Uses our preview cell renderer rather than the default one.
         */
        public TableCellRenderer getCellRenderer(int row, int column) {return cellRenderer;}

        /**
         * Cell are not editable.
         */
        public boolean isCellEditable(int row, int column) {return false;}
    }



    // - Preview renderer ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Used to render preview cells.
     * @author Nicolas Rinaudo
     */
    private class PreviewCellRenderer implements TableCellRenderer {
        private CellLabel label;
        private CellLabel icon;

        /**
         * Creates a new preview cell renderer.
         */
        public PreviewCellRenderer() {
            label = new CellLabel();
            icon  = new CellLabel();
        }

        /**
         * Returns the foregorund color of the specified cell.
         */
        private Color getForegroundColor(int row, boolean isSelected) {
            switch(row) {
                // Folders.
            case FOLDER:
                if(FilePreviewPanel.this.isActive)
                    return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.FOLDER_SELECTED_FOREGROUND_COLOR :
                                                               ThemeData.FOLDER_FOREGROUND_COLOR);
                return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR :
                                                           ThemeData.FOLDER_INACTIVE_FOREGROUND_COLOR);

                // Plain files.
            case PLAIN_FILE:
                if(FilePreviewPanel.this.isActive)
                    return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.FILE_SELECTED_FOREGROUND_COLOR :
                                                               ThemeData.FILE_FOREGROUND_COLOR);
                return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR :
                                                           ThemeData.FILE_INACTIVE_FOREGROUND_COLOR);

                // Archives.
            case ARCHIVE:
                if(FilePreviewPanel.this.isActive)
                    return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.ARCHIVE_SELECTED_FOREGROUND_COLOR :
                                                               ThemeData.ARCHIVE_FOREGROUND_COLOR);
                return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR :
                                                           ThemeData.ARCHIVE_INACTIVE_FOREGROUND_COLOR);

                // Hidden files.
            case HIDDEN_FILE:
                if(FilePreviewPanel.this.isActive)
                    return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR :
                                                               ThemeData.HIDDEN_FILE_FOREGROUND_COLOR);
                return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR :
                                                           ThemeData.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR);

                // Symlinks.
            case SYMLINK:
                if(FilePreviewPanel.this.isActive)
                    return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.SYMLINK_SELECTED_FOREGROUND_COLOR :
                                                               ThemeData.SYMLINK_FOREGROUND_COLOR);
                return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR :
                                                           ThemeData.SYMLINK_INACTIVE_FOREGROUND_COLOR);

                // Marked files.
            case MARKED_FILE:
                if(FilePreviewPanel.this.isActive)
                    return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.MARKED_SELECTED_FOREGROUND_COLOR :
                                                               ThemeData.MARKED_FOREGROUND_COLOR);
                return FilePreviewPanel.this.data.getColor(isSelected ? ThemeData.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR :
                                                           ThemeData.MARKED_INACTIVE_FOREGROUND_COLOR);
            }

            // Impossible.
            return null;
        }

        /**
         * Returns the object used to render the specified cell.
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            CellLabel currentLabel;

            // Icon label foreground.
            if(column == 0) {
                currentLabel = icon;
                if(row == FOLDER)
                    currentLabel.setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.FOLDER_ICON_NAME));
                else if(row == ARCHIVE)
                    currentLabel.setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.ARCHIVE_ICON_NAME));
                else if(row == SYMLINK)
                    currentLabel.setIcon(symlinkIcon);
                else
                    currentLabel.setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.FILE_ICON_NAME));
            }
            // Text label foreground.
            else {
                currentLabel = label;
                currentLabel.setFont(data.getFont(ThemeData.FILE_TABLE_FONT));
                currentLabel.setText((String)value);
                currentLabel.setForeground(getForegroundColor(row, isSelected));
            }

            // Foreground.
            if(isSelected)
                currentLabel.setOutline(isActive ? data.getColor(ThemeData.FILE_TABLE_SELECTED_OUTLINE_COLOR) :
                                        data.getColor(ThemeData.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR));
            else
                currentLabel.setOutline(null);

            // Background.
            if(FilePreviewPanel.this.isActive) {
                if(isSelected)
                    currentLabel.setBackground(FilePreviewPanel.this.data.getColor(ThemeData.FILE_TABLE_SELECTED_BACKGROUND_COLOR));
                else
                    currentLabel.setBackground(FilePreviewPanel.this.data.getColor((row % 2 == 0) ? ThemeData.FILE_TABLE_BACKGROUND_COLOR :
                                                                                   ThemeData.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR));
            }
            else {
                if(isSelected)
                    currentLabel.setBackground(FilePreviewPanel.this.data.getColor(ThemeData.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR));
                else
                    currentLabel.setBackground(FilePreviewPanel.this.data.getColor((row % 2 == 0) ? ThemeData.FILE_TABLE_INACTIVE_BACKGROUND_COLOR :
                                                                                   ThemeData.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR));
            }

            return currentLabel;
        }
    }
}
