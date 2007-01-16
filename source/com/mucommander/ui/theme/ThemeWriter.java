package com.mucommander.ui.theme;

import com.mucommander.xml.writer.*;

import java.awt.Color;
import java.awt.Font;
import java.io.*;

/**
 * Class used to save themes in XML format.
 * @author Nicolas Rinaudo
 */
class ThemeWriter implements XmlConstants {
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
        if((font = theme.getFont(Theme.FILE_TABLE)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));
        if((color = theme.getColor(Theme.FILE_TABLE_BORDER)) != null)
            out.writeStandAloneElement(ELEMENT_BORDER, getColorAttributes(color));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.FILE_BACKGROUND)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FILE_UNFOCUSED_BACKGROUND)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.HIDDEN_FILE)) != null)
            out.writeStandAloneElement(ELEMENT_HIDDEN, getColorAttributes(color));
        if((color = theme.getColor(Theme.FOLDER)) != null)
            out.writeStandAloneElement(ELEMENT_FOLDER,  getColorAttributes(color));
        if((color = theme.getColor(Theme.ARCHIVE)) != null)
            out.writeStandAloneElement(ELEMENT_ARCHIVE, getColorAttributes(color));
        if((color = theme.getColor(Theme.SYMLINK)) != null)
            out.writeStandAloneElement(ELEMENT_SYMLINK, getColorAttributes(color));
        if((color = theme.getColor(Theme.MARKED)) != null)
            out.writeStandAloneElement(ELEMENT_MARKED, getColorAttributes(color));
        if((color = theme.getColor(Theme.FILE)) != null)
            out.writeStandAloneElement(ELEMENT_FILE, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTION);
        out.println();
        if((color = theme.getColor(Theme.FILE_BACKGROUND_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.FILE_UNFOCUSED_BACKGROUND_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.HIDDEN_FILE_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_HIDDEN, getColorAttributes(color));
        if((color = theme.getColor(Theme.FOLDER_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_FOLDER,  getColorAttributes(color));
        if((color = theme.getColor(Theme.ARCHIVE_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_ARCHIVE, getColorAttributes(color));
        if((color = theme.getColor(Theme.SYMLINK_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_SYMLINK, getColorAttributes(color));
        if((color = theme.getColor(Theme.MARKED_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_MARKED, getColorAttributes(color));
        if((color = theme.getColor(Theme.FILE_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_FILE, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTION);
        out.endElement(ELEMENT_TABLE);


        // - Shell description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_SHELL);
        out.println();
        if((font = theme.getFont(Theme.SHELL)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.SHELL_BACKGROUND)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SHELL_TEXT)) != null)
            out.writeStandAloneElement(ELEMENT_TEXT, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTION);
        out.println();
        if((color = theme.getColor(Theme.SHELL_BACKGROUND_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SHELL_TEXT_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_TEXT, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTION);
        out.endElement(ELEMENT_SHELL);



        // - Shell history description ---------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_SHELL_HISTORY);
        out.println();
        if((font = theme.getFont(Theme.SHELL_HISTORY)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.SHELL_HISTORY_BACKGROUND)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SHELL_HISTORY_TEXT)) != null)
            out.writeStandAloneElement(ELEMENT_TEXT, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTION);
        out.println();
        if((color = theme.getColor(Theme.SHELL_HISTORY_BACKGROUND_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.SHELL_HISTORY_TEXT_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_TEXT, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTION);
        out.endElement(ELEMENT_SHELL);



        // - Editor description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_EDITOR);
        out.println();
        if((font = theme.getFont(Theme.EDITOR)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.EDITOR_BACKGROUND)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.EDITOR_TEXT)) != null)
            out.writeStandAloneElement(ELEMENT_TEXT, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTION);
        out.println();
        if((color = theme.getColor(Theme.EDITOR_BACKGROUND_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.EDITOR_TEXT_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_TEXT, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTION);
        out.endElement(ELEMENT_EDITOR);


        // - Location bar description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_LOCATION_BAR);
        out.println();
        if((font = theme.getFont(Theme.LOCATION_BAR)) != null)
            out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(font));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        if((color = theme.getColor(Theme.LOCATION_BAR_BACKGROUND)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.LOCATION_BAR_TEXT)) != null)
            out.writeStandAloneElement(ELEMENT_TEXT, getColorAttributes(color));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTION);
        out.println();
        if((color = theme.getColor(Theme.LOCATION_BAR_BACKGROUND_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_BACKGROUND, getColorAttributes(color));
        if((color = theme.getColor(Theme.LOCATION_BAR_TEXT_SELECTED)) != null)
            out.writeStandAloneElement(ELEMENT_TEXT, getColorAttributes(color));
        out.endElement(ELEMENT_SELECTION);
        out.endElement(ELEMENT_EDITOR);

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
