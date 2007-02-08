package com.mucommander.ui.theme;

import com.mucommander.xml.writer.*;

import java.awt.Color;
import java.awt.Font;
import java.io.*;

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
        Font      font;
        Color     color;

        out = new XmlWriter(stream);
        out.startElement(ELEMENT_ROOT);
        out.println();

        // - File table description ------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_TABLE);
        out.println();

        // Global values.
        if((color = theme.getColor(Theme.FILE_TABLE_BORDER_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BORDER, getColorAttributes(color));
        if((color = theme.getColor(Theme.FILE_TABLE_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((font = theme.getFont(Theme.FILE_TABLE_FONT)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Hidden files.
        out.startElement(ELEMENT_HIDDEN);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.HIDDEN_FILE_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.HIDDEN_FILE_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.HIDDEN_FILE_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_HIDDEN);

        // Folders.
        out.startElement(ELEMENT_FOLDER);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.FOLDER_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FOLDER_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FOLDER_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.FOLDER_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FOLDER_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_FOLDER);

        // Archives.
        out.startElement(ELEMENT_ARCHIVE);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.ARCHIVE_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.ARCHIVE_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.ARCHIVE_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.ARCHIVE_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_ARCHIVE);

        // Symlink.
        out.startElement(ELEMENT_SYMLINK);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.SYMLINK_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SYMLINK_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SYMLINK_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.SYMLINK_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SYMLINK_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_SYMLINK);

        // Marked files.
        out.startElement(ELEMENT_MARKED);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.MARKED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.MARKED_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.MARKED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.MARKED_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.MARKED_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_MARKED);

        // Plain files.
        out.startElement(ELEMENT_FILE);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.FILE_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FILE_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FILE_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.FILE_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FILE_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_FILE);
        out.endElement(ELEMENT_TABLE);



        // - Shell description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_SHELL);
        out.println();
        if((font = theme.getFont(Theme.SHELL_FONT)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.SHELL_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SHELL_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.SHELL_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SHELL_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_SHELL);



        // - Shell history description ---------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_SHELL_HISTORY);
        out.println();
        if((font = theme.getFont(Theme.SHELL_HISTORY_FONT)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.SHELL_HISTORY_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SHELL_HISTORY_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_SHELL_HISTORY);



        // - Editor description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_EDITOR);
        out.println();
        if((font = theme.getFont(Theme.EDITOR_FONT)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.EDITOR_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.EDITOR_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_EDITOR);


        // - Location bar description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_LOCATION_BAR);
        out.println();
        if((font = theme.getFont(Theme.LOCATION_BAR_FONT)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.LOCATION_BAR_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.LOCATION_BAR_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        if((color = theme.getColor(Theme.LOCATION_BAR_SELECTED_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.LOCATION_BAR_SELECTED_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_EDITOR);



        // - Volume label description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_STATUS_BAR);
        out.println();
        // Font.
        if((font = theme.getFont(Theme.STATUS_BAR_FONT)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Colors.
        if((color = theme.getColor(Theme.STATUS_BAR_BACKGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.STATUS_BAR_FOREGROUND_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_FOREGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.STATUS_BAR_BORDER_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_BORDER, getColorAttributes(color));
        if((color = theme.getColor(Theme.STATUS_BAR_OK_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_OK, getColorAttributes(color));
        if((color = theme.getColor(Theme.STATUS_BAR_WARNING_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_WARNING, getColorAttributes(color));
        if((color = theme.getColor(Theme.STATUS_BAR_CRITICAL_COLOR)) != null)
            out.writeStandAloneElement(ELEMENT_CRITICAL, getColorAttributes(color));
        out.endElement(ELEMENT_STATUS_BAR);

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
        StringBuffer  buffer;     // Used to build the color's string representation.

        buffer = new StringBuffer();

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
