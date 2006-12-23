package com.mucommander.ui.theme;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;
import com.mucommander.Debug;

import java.io.*;
import java.util.*;
import java.awt.Font;
import java.awt.Color;

/**
 * Loads theme instances from properly formatted XML files.
 * @author Nicolas Rinaudo
 */
class ThemeReader implements ContentHandler, XmlConstants {
    // - XML parser states ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Parsing hasn't started yet. */
    private static final int STATE_UNKNOWN                = 0;
    /** Parsing the root element. */
    private static final int STATE_ROOT                   = 1;
    /** Parsing the table element.*/
    private static final int STATE_TABLE                  = 2;
    /** Parsing the shell element. */
    private static final int STATE_SHELL                  = 3;
    /** Parsing the editor element. */
    private static final int STATE_EDITOR                 = 4;
    /** Parsing the location bar element. */
    private static final int STATE_LOCATION_BAR           = 5;
    /** Parsing the table.normal element. */
    private static final int STATE_TABLE_NORMAL           = 6;
    /** Parsing the shell.normal element. */
    private static final int STATE_SHELL_NORMAL           = 7;
    /** Parsing the table.selected element. */
    private static final int STATE_TABLE_SELECTED         = 8;
    /** Parsing the shell.selected element. */
    private static final int STATE_SHELL_SELECTED         = 9;
    /** Parsing the editor.normal element. */
    private static final int STATE_EDITOR_NORMAL          = 10;
    /** Parsing the location bar.normal element. */
    private static final int STATE_LOCATION_BAR_NORMAL    = 11;
    /** Parsing the editor.selected element. */
    private static final int STATE_EDITOR_SELECTED        = 12;
    /** Parsing the location bar.selected element. */
    private static final int STATE_LOCATION_BAR_SELECTED  = 13;
    /** Parsing the shell_history element. */
    private static final int STATE_SHELL_HISTORY          = 14;
    /** Parsing the shell_history.normal element. */
    private static final int STATE_SHELL_HISTORY_NORMAL   = 15;
    /** Parsing the shell_history.selected element. */
    private static final int STATE_SHELL_HISTORY_SELECTED = 16;



    // - Default values ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Default font size. */
    private static final int    DEFAULT_FONT_SIZE;
    /** Default font family. */
    private static final String DEFAULT_FONT_FAMILY;



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Theme that is currently being built. */
    private ThemeData theme;
    /** Current state of the XML parser. */
    private int       state;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Computes the default font size and family of UI elements.
     */
    static {
        Font defaultFont;

        // Uses the JLabel font as default.
        defaultFont = new javax.swing.JLabel().getFont();

        DEFAULT_FONT_SIZE   = defaultFont.getSize();
        DEFAULT_FONT_FAMILY = defaultFont.getFamily();
    }

    /**
     * Creates a new theme reader.
     */
    private ThemeReader(ThemeData t) {
        theme = t;
        state = STATE_UNKNOWN;
    }

    /**
     * Attempts to read a theme from the specified input stream.
     * @param     in        where to read the theme from.
     * @return              the parsed theme.
     * @exception Exception thrown if an error occured while reading the theme.
     */
    public static ThemeData read(InputStream in) throws Exception {
        ThemeData buffer;

        new Parser().parse(in, new ThemeReader(buffer = new ThemeData()), "UTF-8");
        return buffer;
    }


    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Notifies the reader that a new XML element is starting.
     */
    public void startElement(String uri, String name, Hashtable attributes, Hashtable attURIs) throws Exception {
        // XML root element.
        if(name.equals(ELEMENT_ROOT)) {
            if(state != STATE_UNKNOWN)
                throw createIllegalElementDeclaration(name);
            state = STATE_ROOT;
        }

        // File table declaration.
        else if(name.equals(ELEMENT_TABLE)) {
            if(state != STATE_ROOT)
                throw createIllegalElementDeclaration(name);
            state = STATE_TABLE;
        }

        // Shell declaration.
        else if(name.equals(ELEMENT_SHELL)) {
            if(state != STATE_ROOT)
                throw createIllegalElementDeclaration(name);
            state = STATE_SHELL;
        }

        // Editor declaration.
        else if(name.equals(ELEMENT_EDITOR)) {
            if(state != STATE_ROOT)
                throw createIllegalElementDeclaration(name);
            state = STATE_EDITOR;
        }

        // Location bar declaration.
        else if(name.equals(ELEMENT_LOCATION_BAR)) {
            if(state != STATE_ROOT)
                throw createIllegalElementDeclaration(name);
            state = STATE_LOCATION_BAR;
        }

        // Shell history declaration.
        else if(name.equals(ELEMENT_SHELL_HISTORY)) {
            if(state != STATE_ROOT)
                throw createIllegalElementDeclaration(name);
            state = STATE_SHELL_HISTORY;
        }

        // Normal element declaration.
        else if(name.equals(ELEMENT_NORMAL)) {
            if(state == STATE_SHELL)
                state = STATE_SHELL_NORMAL;
            else if(state == STATE_TABLE)
                state = STATE_TABLE_NORMAL;
            else if(state == STATE_EDITOR)
                state = STATE_EDITOR_NORMAL;
            else if(state == STATE_LOCATION_BAR)
                state = STATE_LOCATION_BAR_NORMAL;
            else if(state == STATE_SHELL_HISTORY)
                state = STATE_SHELL_HISTORY_NORMAL;
            else
                throw createIllegalElementDeclaration(name);
        }

        // Selected element declaration.
        else if(name.equals(ELEMENT_SELECTION)) {
            if(state == STATE_SHELL)
                state = STATE_SHELL_SELECTED;
            else if(state == STATE_TABLE)
                state = STATE_TABLE_SELECTED;
            else if(state == STATE_EDITOR)
                state = STATE_EDITOR_SELECTED;
            else if(state == STATE_LOCATION_BAR)
                state = STATE_LOCATION_BAR_SELECTED;
            else if(state == STATE_SHELL_HISTORY)
                state = STATE_SHELL_HISTORY_SELECTED;
            else
                throw createIllegalElementDeclaration(name);
        }

        // Font creation.
        else if(name.equals(ELEMENT_FONT)) {
            if(state == STATE_SHELL)
                theme.setFont(Theme.SHELL, createFont(attributes));
            else if(state == STATE_TABLE)
                theme.setFont(Theme.FILE_TABLE, createFont(attributes));
            else if(state == STATE_EDITOR)
                theme.setFont(Theme.EDITOR, createFont(attributes));
            else if(state == STATE_LOCATION_BAR)
                theme.setFont(Theme.LOCATION_BAR, createFont(attributes));
            else if(state == STATE_SHELL_HISTORY)
                theme.setFont(Theme.SHELL_HISTORY, createFont(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // Unfocused background color.
        else if(name.equals(ELEMENT_UNFOCUSED_BACKGROUND)) {
            if(state == STATE_TABLE_SELECTED)
                theme.setColor(Theme.FILE_UNFOCUSED_BACKGROUND_SELECTED, createColor(attributes));
            else if(state == STATE_TABLE_NORMAL)
                theme.setColor(Theme.FILE_UNFOCUSED_BACKGROUND, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // File table border color.
        else if(name.equals(ELEMENT_BORDER)) {
            if(state == STATE_TABLE)
                theme.setColor(Theme.FILE_TABLE_BORDER, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // Background color.
        else if(name.equals(ELEMENT_BACKGROUND)) {
            if(state == STATE_TABLE_NORMAL)
                theme.setColor(Theme.FILE_BACKGROUND, createColor(attributes));
            else if(state == STATE_TABLE_SELECTED)
                theme.setColor(Theme.FILE_BACKGROUND_SELECTED, createColor(attributes));

            else if(state == STATE_SHELL_NORMAL)
                theme.setColor(Theme.SHELL_BACKGROUND, createColor(attributes));
            else if(state == STATE_SHELL_SELECTED)
                theme.setColor(Theme.SHELL_BACKGROUND_SELECTED, createColor(attributes));

            else if(state == STATE_EDITOR_NORMAL)
                theme.setColor(Theme.EDITOR_BACKGROUND, createColor(attributes));
            else if(state == STATE_EDITOR_SELECTED)
                theme.setColor(Theme.EDITOR_BACKGROUND_SELECTED, createColor(attributes));

            else if(state == STATE_LOCATION_BAR_NORMAL)
                theme.setColor(Theme.LOCATION_BAR_BACKGROUND, createColor(attributes));
            else if(state == STATE_LOCATION_BAR_SELECTED)
                theme.setColor(Theme.LOCATION_BAR_BACKGROUND_SELECTED, createColor(attributes));

            else if(state == STATE_SHELL_HISTORY_NORMAL)
                theme.setColor(Theme.SHELL_HISTORY_BACKGROUND, createColor(attributes));
            else if(state == STATE_SHELL_HISTORY_SELECTED)
                theme.setColor(Theme.SHELL_HISTORY_BACKGROUND_SELECTED, createColor(attributes));

            else
                throw createIllegalElementDeclaration(name);
        }

        // Hidden files color.
        else if(name.equals(ELEMENT_HIDDEN)) {
            if(state == STATE_TABLE_NORMAL)
                theme.setColor(Theme.HIDDEN_FILE, createColor(attributes));
            else if(state == STATE_TABLE_SELECTED)
                theme.setColor(Theme.HIDDEN_FILE_SELECTED, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // Folders color.
        else if(name.equals(ELEMENT_FOLDER)) {
            if(state == STATE_TABLE_NORMAL)
                theme.setColor(Theme.FOLDER, createColor(attributes));
            else if(state == STATE_TABLE_SELECTED)
                theme.setColor(Theme.FOLDER_SELECTED, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // Archives color.
        else if(name.equals(ELEMENT_ARCHIVE)) {
            if(state == STATE_TABLE_NORMAL)
                theme.setColor(Theme.ARCHIVE, createColor(attributes));
            else if(state == STATE_TABLE_SELECTED)
                theme.setColor(Theme.ARCHIVE_SELECTED, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // Symlinks color.
        else if(name.equals(ELEMENT_SYMLINK)) {
            if(state == STATE_TABLE_NORMAL)
                theme.setColor(Theme.SYMLINK, createColor(attributes));
            else if(state == STATE_TABLE_SELECTED)
                theme.setColor(Theme.SYMLINK_SELECTED, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // Marked elements color.
        else if(name.equals(ELEMENT_MARKED)) {
            if(state == STATE_TABLE_NORMAL)
                theme.setColor(Theme.MARKED, createColor(attributes));
            else if(state == STATE_TABLE_SELECTED)
                theme.setColor(Theme.MARKED_SELECTED, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // Normal files color.
        else if(name.equals(ELEMENT_FILE)) {
            if(state == STATE_TABLE_NORMAL)
                theme.setColor(Theme.FILE, createColor(attributes));
            else if(state == STATE_TABLE_SELECTED)
                theme.setColor(Theme.FILE_SELECTED, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // Progress bar color.
        else if(name.equals(ELEMENT_PROGRESS)) {
            if(state == STATE_LOCATION_BAR)
                theme.setColor(Theme.LOCATION_BAR_PROGRESS, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        // Text color
        else if(name.equals(ELEMENT_TEXT)) {
            if(state == STATE_SHELL_NORMAL)
                theme.setColor(Theme.SHELL_TEXT, createColor(attributes));
            else if(state == STATE_SHELL_SELECTED)
                theme.setColor(Theme.SHELL_TEXT_SELECTED, createColor(attributes));

            else if(state == STATE_SHELL_HISTORY_NORMAL)
                theme.setColor(Theme.SHELL_HISTORY_TEXT, createColor(attributes));
            else if(state == STATE_SHELL_HISTORY_SELECTED)
                theme.setColor(Theme.SHELL_HISTORY_TEXT_SELECTED, createColor(attributes));

            else if(state == STATE_EDITOR_NORMAL)
                theme.setColor(Theme.EDITOR_TEXT, createColor(attributes));
            else if(state == STATE_EDITOR_SELECTED)
                theme.setColor(Theme.EDITOR_TEXT_SELECTED, createColor(attributes));

            else if(state == STATE_LOCATION_BAR_NORMAL)
                theme.setColor(Theme.LOCATION_BAR_TEXT, createColor(attributes));
            else if(state == STATE_LOCATION_BAR_SELECTED)
                theme.setColor(Theme.LOCATION_BAR_TEXT_SELECTED, createColor(attributes));
            else
                throw createIllegalElementDeclaration(name);
        }

        else
            throw createIllegalElementDeclaration(name);
    }

    /**
     * Notifies the reader that the current element declaration is over.
     */
    public void endElement(String uri, String name) throws Exception {
        // XML root element.
        if(name.equals(ELEMENT_ROOT)) {
            if(state != STATE_ROOT)
                throw createIllegalElementClosing(name);
            state = STATE_UNKNOWN;
        }

        // File table declaration.
        else if(name.equals(ELEMENT_TABLE)) {
            if(state != STATE_TABLE)
                throw createIllegalElementClosing(name);
            state = STATE_ROOT;
        }

        // Shell declaration.
        else if(name.equals(ELEMENT_SHELL)) {
            if(state != STATE_SHELL)
                throw createIllegalElementClosing(name);
            state = STATE_ROOT;
        }

        // Shell history declaration.
        else if(name.equals(ELEMENT_SHELL_HISTORY)) {
            if(state != STATE_SHELL_HISTORY)
                throw createIllegalElementClosing(name);
            state = STATE_ROOT;
        }

        // Editor declaration.
        else if(name.equals(ELEMENT_EDITOR)) {
            if(state != STATE_EDITOR)
                throw createIllegalElementClosing(name);
            state = STATE_ROOT;
        }

        // Location bar declaration.
        else if(name.equals(ELEMENT_LOCATION_BAR)) {
            if(state != STATE_LOCATION_BAR)
                throw createIllegalElementClosing(name);
            state = STATE_ROOT;
        }

        // Normal element declaration.
        else if(name.equals(ELEMENT_NORMAL)) {
            if(state == STATE_SHELL_NORMAL)
                state = STATE_SHELL;
            else if(state == STATE_SHELL_HISTORY_NORMAL)
                state = STATE_SHELL_HISTORY;
            else if(state == STATE_TABLE_NORMAL)
                state = STATE_TABLE;
            else if(state == STATE_EDITOR_NORMAL)
                state = STATE_EDITOR;
            else if(state == STATE_LOCATION_BAR_NORMAL)
                state = STATE_LOCATION_BAR;
            else
                throw createIllegalElementClosing(name);
        }

        // Selected element declaration.
        else if(name.equals(ELEMENT_SELECTION)) {
            if(state == STATE_SHELL_SELECTED)
                state = STATE_SHELL;
            else if(state == STATE_SHELL_HISTORY_SELECTED)
                state = STATE_SHELL_HISTORY;
            else if(state == STATE_TABLE_SELECTED)
                state = STATE_TABLE;
            else if(state == STATE_EDITOR_SELECTED)
                state = STATE_EDITOR;
            else if(state == STATE_LOCATION_BAR_SELECTED)
                state = STATE_LOCATION_BAR;
            else
                throw createIllegalElementClosing(name);
        }
    }



    // - Unused XML methods --------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Not used.
     */
    public void startDocument() {}

    /**
     * Not used.
     */
    public void endDocument() {}

    /**
     * Not used.
     */
    public void characters(String s) {}



    // - Helper methods ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a font from the specified XML attributes.
     * <p>
     * Ignored attributes will be set to their default values.
     * </p>
     * @param  attributes XML attributes describing the font to use.
     * @return            the resulting Font instance.
     */
    private static Font createFont(Hashtable attributes) {
        String buffer; // Buffer for attribute values.
        int    size;   // Font size.
        int    style;  // Font style.

        // Computes the font style.
        style = 0;
        if(((buffer = (String)attributes.get(ATTRIBUTE_BOLD)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.BOLD;
        if(((buffer = (String)attributes.get(ATTRIBUTE_ITALIC)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.ITALIC;

        // Computes the font size.
        if((buffer = (String)attributes.get(ATTRIBUTE_SIZE)) == null)
            size = DEFAULT_FONT_SIZE;
        else
            size = Integer.parseInt(buffer);

        // Computes the font family.
        if((buffer = (String)attributes.get(ATTRIBUTE_FAMILY)) == null)
            buffer = DEFAULT_FONT_FAMILY;

        // Generates the font.
        return new Font(buffer, style, size);
    }

    /**
     * Creates a color from the specified XML attributes.
     * @param  attributes XML attributes describing the font to use.
     * @return            the resulting Color instance.
     * @throws Exception  thrown if the {@link #ATTRIBUTE_COLOR} attribute is not set.
     */
    private static Color createColor(Hashtable attributes) throws Exception {
        String buffer;
        int    color;

        // Retrieves the color attribute's value.
        if((buffer = (String)attributes.get(ATTRIBUTE_COLOR)) == null) {
            if(Debug.ON) Debug.trace("Missing color attribute in theme.");
            return null;
        }
        color = Integer.parseInt(buffer, 16);

        // Retrieves the transparency attribute's value..
        if((buffer = (String)attributes.get(ATTRIBUTE_ALPHA)) == null)
            return new Color(color);
        return new Color(color | (Integer.parseInt(buffer, 16) << 24), true);
    }


    // - Error generation methods --------------------------------------------
    // -----------------------------------------------------------------------
    private static Exception createIllegalElementDeclaration(String element) {return new Exception("Illegal element declaration: " + element);}
    private static Exception createIllegalElementClosing(String element) {return new Exception("Illegal element closure: " + element);}
}
