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
    public static void write(Theme theme, OutputStream stream) throws IOException {
        XmlWriter out;

        out = new XmlWriter(stream);
        out.startElement(ELEMENT_ROOT);
        out.println();

        // - File table description ------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_TABLE);
        out.println();
        out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.FILE_TABLE)));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        out.writeStandAloneElement(ELEMENT_BACKGROUND,           getColorAttributes(theme.getColor(Theme.FILE_BACKGROUND)));
        out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_UNFOCUSED_BACKGROUND)));
        out.writeStandAloneElement(ELEMENT_HIDDEN,               getColorAttributes(theme.getColor(Theme.HIDDEN_FILE)));
        out.writeStandAloneElement(ELEMENT_FOLDER,               getColorAttributes(theme.getColor(Theme.FOLDER)));
        out.writeStandAloneElement(ELEMENT_ARCHIVE,              getColorAttributes(theme.getColor(Theme.ARCHIVE)));
        out.writeStandAloneElement(ELEMENT_SYMLINK,              getColorAttributes(theme.getColor(Theme.SYMLINK)));
        out.writeStandAloneElement(ELEMENT_MARKED,               getColorAttributes(theme.getColor(Theme.MARKED)));
        out.writeStandAloneElement(ELEMENT_FILE,                 getColorAttributes(theme.getColor(Theme.FILE)));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTION);
        out.println();
        out.writeStandAloneElement(ELEMENT_BACKGROUND,           getColorAttributes(theme.getColor(Theme.FILE_BACKGROUND_SELECTED)));
        out.writeStandAloneElement(ELEMENT_UNFOCUSED_BACKGROUND, getColorAttributes(theme.getColor(Theme.FILE_UNFOCUSED_BACKGROUND_SELECTED)));
        out.writeStandAloneElement(ELEMENT_HIDDEN,               getColorAttributes(theme.getColor(Theme.HIDDEN_FILE_SELECTED)));
        out.writeStandAloneElement(ELEMENT_FOLDER,               getColorAttributes(theme.getColor(Theme.FOLDER_SELECTED)));
        out.writeStandAloneElement(ELEMENT_ARCHIVE,              getColorAttributes(theme.getColor(Theme.ARCHIVE_SELECTED)));
        out.writeStandAloneElement(ELEMENT_SYMLINK,              getColorAttributes(theme.getColor(Theme.SYMLINK_SELECTED)));
        out.writeStandAloneElement(ELEMENT_MARKED,               getColorAttributes(theme.getColor(Theme.MARKED_SELECTED)));
        out.writeStandAloneElement(ELEMENT_FILE,                 getColorAttributes(theme.getColor(Theme.FILE_SELECTED)));
        out.endElement(ELEMENT_SELECTION);
        out.endElement(ELEMENT_TABLE);


        // - Shell description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_SHELL);
        out.println();
        out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.SHELL)));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        out.writeStandAloneElement(ELEMENT_BACKGROUND,           getColorAttributes(theme.getColor(Theme.SHELL_BACKGROUND)));
        out.writeStandAloneElement(ELEMENT_TEXT,                 getColorAttributes(theme.getColor(Theme.SHELL_TEXT)));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTION);
        out.println();
        out.writeStandAloneElement(ELEMENT_BACKGROUND,           getColorAttributes(theme.getColor(Theme.SHELL_BACKGROUND_SELECTED)));
        out.writeStandAloneElement(ELEMENT_TEXT,                 getColorAttributes(theme.getColor(Theme.SHELL_TEXT_SELECTED)));
        out.endElement(ELEMENT_SELECTION);
        out.endElement(ELEMENT_SHELL);


        // - Editor description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_EDITOR);
        out.println();
        out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.EDITOR)));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        out.writeStandAloneElement(ELEMENT_BACKGROUND,           getColorAttributes(theme.getColor(Theme.EDITOR_BACKGROUND)));
        out.writeStandAloneElement(ELEMENT_TEXT,                 getColorAttributes(theme.getColor(Theme.EDITOR_TEXT)));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTION);
        out.println();
        out.writeStandAloneElement(ELEMENT_BACKGROUND,           getColorAttributes(theme.getColor(Theme.EDITOR_BACKGROUND_SELECTED)));
        out.writeStandAloneElement(ELEMENT_TEXT,                 getColorAttributes(theme.getColor(Theme.EDITOR_TEXT_SELECTED)));
        out.endElement(ELEMENT_SELECTION);
        out.endElement(ELEMENT_EDITOR);


        // - Location bar description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_LOCATION_BAR);
        out.println();
        out.writeStandAloneElement(ELEMENT_FONT, getFontAttributes(theme.getFont(Theme.LOCATION_BAR)));

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        out.writeStandAloneElement(ELEMENT_BACKGROUND,           getColorAttributes(theme.getColor(Theme.LOCATION_BAR_BACKGROUND)));
        out.writeStandAloneElement(ELEMENT_TEXT,                 getColorAttributes(theme.getColor(Theme.LOCATION_BAR_TEXT)));
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTION);
        out.println();
        out.writeStandAloneElement(ELEMENT_BACKGROUND,           getColorAttributes(theme.getColor(Theme.LOCATION_BAR_BACKGROUND_SELECTED)));
        out.writeStandAloneElement(ELEMENT_TEXT,                 getColorAttributes(theme.getColor(Theme.LOCATION_BAR_TEXT_SELECTED)));
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
        if(color.getRed() < 10)
            buffer.append('0');
        buffer.append(Integer.toString(color.getRed(), 16));

        // Green component.
        if(color.getGreen() < 10)
            buffer.append('0');
        buffer.append(Integer.toString(color.getGreen(), 16));

        // Blue component.
        if(color.getBlue() < 10)
            buffer.append('0');
        buffer.append(Integer.toString(color.getBlue(), 16));

        // Builds the XML attributes.
        attributes = new XmlAttributes();
        attributes.add(ATTRIBUTE_COLOR, buffer.toString());

        return attributes;
    }

}
