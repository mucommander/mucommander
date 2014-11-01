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

package com.mucommander.ui.theme;

import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class used to save themes in XML format.
 * @author Nicolas Rinaudo
 */
class ThemeWriter implements ThemeXmlConstants {
    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Prevents instanciation of the class.
     */
    private ThemeWriter() {}



    // - XML output ----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Saves the specified theme to the specified output stream.
     * @param  theme       theme to save.
     * @param  stream      where to write the theme to.
     * @throws IOException thrown if any IO related error occurs.
     */
    public static void write(ThemeData theme, OutputStream stream) throws IOException {
        XmlWriter out;

        out = new XmlWriter(stream);
        out.startElement(ELEMENT_ROOT);
        out.println();

        // - File table description ------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_TABLE);
        out.println();

        // Global values.
        if(theme.isColorSet(Theme.FILE_TABLE_BORDER_COLOR))
            out.writeStandAloneElement(ELEMENT_BORDER, getColorAttributes(theme.getColor(Theme.FILE_TABLE_BORDER_COLOR)));
        if(theme.isColorSet(Theme.FILE_TABLE_INACTIVE_BORDER_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_BORDER, getColorAttributes(theme.getColor(Theme.FILE_TABLE_INACTIVE_BORDER_COLOR)));
        if(theme.isColorSet(Theme.FILE_TABLE_SELECTED_OUTLINE_COLOR))
            out.writeStandAloneElement(ELEMENT_OUTLINE, getColorAttributes(theme.getColor(Theme.FILE_TABLE_SELECTED_OUTLINE_COLOR)));
        if(theme.isColorSet(Theme.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_OUTLINE, getColorAttributes(theme.getColor(Theme.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR)));
        if(theme.isFontSet(Theme.FILE_TABLE_FONT))
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.FILE_TABLE_FONT)));

        // Normal background colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.FILE_TABLE_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);

        // Selected background colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.FILE_TABLE_SELECTED_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_SELECTED_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_SECONDARY_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_SECONDARY_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR)));

        out.endElement(ELEMENT_SELECTED);

        // Alternate background colors.
        out.startElement(ELEMENT_ALTERNATE);
        out.println();
        if(theme.isColorSet(Theme.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR)));
        out.endElement(ELEMENT_ALTERNATE);

        // Unmatched colors.
        out.startElement(ELEMENT_UNMATCHED);
        out.println();
        if(theme.isColorSet(Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_UNMATCHED);

        // Hidden files.
        out.startElement(ELEMENT_HIDDEN);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.HIDDEN_FILE_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.HIDDEN_FILE_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_HIDDEN);

        // Folders.
        out.startElement(ELEMENT_FOLDER);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.FOLDER_INACTIVE_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.FOLDER_INACTIVE_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.FOLDER_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.FOLDER_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.FOLDER_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.FOLDER_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_FOLDER);

        // Archives.
        out.startElement(ELEMENT_ARCHIVE);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.ARCHIVE_INACTIVE_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.ARCHIVE_INACTIVE_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.ARCHIVE_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.ARCHIVE_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_ARCHIVE);

        // Symlink.
        out.startElement(ELEMENT_SYMLINK);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.SYMLINK_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.SYMLINK_INACTIVE_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.SYMLINK_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.SYMLINK_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.SYMLINK_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.SYMLINK_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_SYMLINK);

        // Marked files.
        out.startElement(ELEMENT_MARKED);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.MARKED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.MARKED_INACTIVE_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.MARKED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.MARKED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.MARKED_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.MARKED_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_MARKED);

        // Plain files.
        out.startElement(ELEMENT_FILE);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.FILE_INACTIVE_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.FILE_INACTIVE_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.FILE_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.FILE_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_INACTIVE_FOREGROUND, getColorAttributes(theme.getColor(Theme.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.FILE_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.FILE_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_FILE);
        out.endElement(ELEMENT_TABLE);



        // - Shell description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_SHELL);
        out.println();
        if(theme.isFontSet(Theme.SHELL_FONT))
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.SHELL_FONT)));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.SHELL_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.SHELL_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.SHELL_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.SHELL_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.SHELL_SELECTED_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.SHELL_SELECTED_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.SHELL_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.SHELL_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_SHELL);



        // - Shell history description ---------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_SHELL_HISTORY);
        out.println();
        if(theme.isFontSet(Theme.SHELL_HISTORY_FONT))
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.SHELL_HISTORY_FONT)));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.SHELL_HISTORY_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.SHELL_HISTORY_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.SHELL_HISTORY_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.SHELL_HISTORY_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_SHELL_HISTORY);



        // - Editor description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_EDITOR);
        out.println();
        if(theme.isFontSet(Theme.EDITOR_FONT))
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.EDITOR_FONT)));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.EDITOR_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.EDITOR_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.EDITOR_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.EDITOR_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.EDITOR_SELECTED_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.EDITOR_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_EDITOR);


        // - Location bar description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_LOCATION_BAR);
        out.println();
        if(theme.isFontSet(Theme.LOCATION_BAR_FONT))
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.LOCATION_BAR_FONT)));
        if(theme.isColorSet(Theme.LOCATION_BAR_PROGRESS_COLOR))
            out.writeStandAloneElement(ELEMENT_PROGRESS, getColorAttributes(theme.getColor(Theme.LOCATION_BAR_PROGRESS_COLOR)));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.LOCATION_BAR_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.LOCATION_BAR_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.LOCATION_BAR_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.LOCATION_BAR_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.LOCATION_BAR_SELECTED_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.LOCATION_BAR_SELECTED_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.LOCATION_BAR_SELECTED_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.LOCATION_BAR_SELECTED_FOREGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_LOCATION_BAR);



        // - Volume label description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_STATUS_BAR);
        out.println();
        // Font.
        if(theme.isFontSet(Theme.STATUS_BAR_FONT))
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.STATUS_BAR_FONT)));

        // Colors.
        if(theme.isColorSet(Theme.STATUS_BAR_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.STATUS_BAR_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.STATUS_BAR_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.STATUS_BAR_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.STATUS_BAR_BORDER_COLOR))
            out.writeStandAloneElement(ELEMENT_BORDER, getColorAttributes(theme.getColor(Theme.STATUS_BAR_BORDER_COLOR)));
        if(theme.isColorSet(Theme.STATUS_BAR_OK_COLOR))
            out.writeStandAloneElement(ELEMENT_OK, getColorAttributes(theme.getColor(Theme.STATUS_BAR_OK_COLOR)));
        if(theme.isColorSet(Theme.STATUS_BAR_WARNING_COLOR))
            out.writeStandAloneElement(ELEMENT_WARNING, getColorAttributes(theme.getColor(Theme.STATUS_BAR_WARNING_COLOR)));
        if(theme.isColorSet(Theme.STATUS_BAR_CRITICAL_COLOR))
            out.writeStandAloneElement(ELEMENT_CRITICAL, getColorAttributes(theme.getColor(Theme.STATUS_BAR_CRITICAL_COLOR)));
        out.endElement(ELEMENT_STATUS_BAR);


        
        // - Quick list label description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_QUICK_LIST);
        out.println();
        
        // Quick list header
        out.startElement(ELEMENT_HEADER);
        out.println();
        // Font.
        if(theme.isFontSet(Theme.QUICK_LIST_HEADER_FONT))
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.QUICK_LIST_HEADER_FONT)));
        // Colors.
        if(theme.isColorSet(Theme.QUICK_LIST_HEADER_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.QUICK_LIST_HEADER_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.QUICK_LIST_HEADER_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.QUICK_LIST_HEADER_BACKGROUND_COLOR)));
        if(theme.isColorSet(Theme.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_SECONDARY_BACKGROUND, getColorAttributes(theme.getColor(Theme.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR)));
        out.endElement(ELEMENT_HEADER);
        
        // Quick list item
        out.startElement(ELEMENT_ITEM);
        out.println();
        // Font.
        if(theme.isFontSet(Theme.QUICK_LIST_ITEM_FONT))
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.QUICK_LIST_ITEM_FONT)));
        // Colors.
        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if(theme.isColorSet(Theme.QUICK_LIST_ITEM_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.QUICK_LIST_ITEM_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.QUICK_LIST_ITEM_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.QUICK_LIST_ITEM_BACKGROUND_COLOR)));
        out.endElement(ELEMENT_NORMAL);
        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if(theme.isColorSet(Theme.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(theme.getColor(Theme.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR)));
        if(theme.isColorSet(Theme.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR))
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(theme.getColor(Theme.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR)));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_ITEM);
        out.endElement(ELEMENT_QUICK_LIST);

        out.endElement(ELEMENT_ROOT);
    }



    // - Helper methods ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the XML attributes describing the specified font.
     * @param  font font to described as XML attributes.
     * @return      the XML attributes describing the specified font.
     */
    private static XmlAttributes getFontAttributes(Font font) {
        XmlAttributes attributes; // Stores the font's description.

        attributes = new XmlAttributes();

        // Font family and size.
        attributes.add(ATTRIBUTE_FAMILY, font.getFamily());
        attributes.add(ATTRIBUTE_SIZE, Integer.toString(font.getSize()));

        // Font style.
        if(font.isBold())
            attributes.add(ATTRIBUTE_BOLD, VALUE_TRUE);
        if(font.isItalic())
            attributes.add(ATTRIBUTE_ITALIC, VALUE_TRUE);

        return attributes;
    }

    /**
     * Returns the XML attributes describing the specified color.
     * @param  color color to described as XML attributes.
     * @return       the XML attributes describing the specified color.
     */
    private static XmlAttributes getColorAttributes(Color color) {
        XmlAttributes attributes; // Stores the color's description.
        StringBuilder buffer;     // Used to build the color's string representation.

        buffer = new StringBuilder();

        // Red component.
        if(color.getRed() < 16)
            buffer.append('0');
        buffer.append(Integer.toString(color.getRed(), 16));

        // Green component.
        if(color.getGreen() < 16)
            buffer.append('0');
        buffer.append(Integer.toString(color.getGreen(), 16));

        // Blue component.
        if(color.getBlue() < 16)
            buffer.append('0');
        buffer.append(Integer.toString(color.getBlue(), 16));

        // Builds the XML attributes.
        attributes = new XmlAttributes();
        attributes.add(ATTRIBUTE_COLOR, buffer.toString());

        if(color.getAlpha() != 255) {
            buffer.setLength(0);
            if(color.getAlpha() < 16)
                buffer.append('0');
            buffer.append(Integer.toString(color.getAlpha(), 16));
            attributes.add(ATTRIBUTE_ALPHA, buffer.toString());
        }

        return attributes;
    }

}
