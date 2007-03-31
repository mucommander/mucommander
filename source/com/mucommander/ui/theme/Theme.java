package com.mucommander.ui.theme;

import com.mucommander.Debug;
import com.mucommander.text.Translator;

import java.awt.Color;
import java.awt.Font;
import java.lang.ref.*;
import java.io.*;

/**
 * Describes a set of custom colors and fonts that can be applied to muCommander.
 * <p>
 * Instances of <code>Theme</code> cannot be created directly, and developers must go through
 * {@link ThemeManager#availableThemes()}. Most classes do not really need to know informations
 * about a specific theme but only the values of the current one, which should be done through
 * {@link ThemeManager#getCurrentColor(int)} and {@link ThemeManager#getCurrentFont(int)}.
 * </p>
 * @see    ThemeManager
 * @see    ThemeReader
 * @see    ThemeWriter
 * @author Nicolas Rinaudo
 */
public class Theme {
    // - Dirty hack ----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // This is an effort to make the Theme class a bit easier to maintain, but I'm the first
    // to admit it's rather dirty.
    // 
    // For optimisation reasons, we're storing the fonts and colors in arrays, using their
    // identifiers as indexes in the array. This, however, means that lots of bits of code
    // must be updated whenever a font or color is added or removed. The probability of
    // someone forgeting this is, well, 100%.
    //
    // For this reason, we've declared the number of font and colors as constants.
    // People are still going to forget to update these constants, but at least it'll be
    // a lot easier to fix.

    /** Number of known fonts. */
    static final int FONT_COUNT  = 6;
    /** Number of known colors. */
    static final int COLOR_COUNT = 74;



    // - Theme types ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Describes the user defined theme. */
    public static final int USER_THEME                         = 0;
    /** Describes predefined muCommander themes. */
    public static final int PREDEFINED_THEME                   = 1;
    /** Describes custom muCommander themes. */
    public static final int CUSTOM_THEME                       = 2;



    // - Font definitions ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public static final int FILE_TABLE_FONT                                 = 0;
    public static final int SHELL_FONT                                      = 1;
    public static final int EDITOR_FONT                                     = 2;
    public static final int LOCATION_BAR_FONT                               = 3;
    public static final int SHELL_HISTORY_FONT                              = 4;
    public static final int STATUS_BAR_FONT                                 = 5;



    // - Color definitions ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public static final int FILE_TABLE_BORDER_COLOR                         = 0;
    public static final int FILE_TABLE_BACKGROUND_COLOR                     = 1;
    public static final int FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR           = 2;
    public static final int HIDDEN_FILE_FOREGROUND_COLOR                    = 3;
    public static final int HIDDEN_FILE_BACKGROUND_COLOR                    = 4;
    public static final int HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR          = 5;
    public static final int HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR          = 6;
    public static final int HIDDEN_FILE_SELECTED_BACKGROUND_COLOR           = 7;
    public static final int HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR = 8;
    public static final int HIDDEN_FILE_SELECTED_FOREGROUND_COLOR           = 9;
    public static final int HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR = 10;
    public static final int FOLDER_FOREGROUND_COLOR                         = 11;
    public static final int FOLDER_BACKGROUND_COLOR                         = 12;
    public static final int FOLDER_UNFOCUSED_BACKGROUND_COLOR               = 13;
    public static final int FOLDER_UNFOCUSED_FOREGROUND_COLOR               = 14;
    public static final int FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR      = 15;
    public static final int FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR      = 16;
    public static final int FOLDER_SELECTED_BACKGROUND_COLOR                = 17;
    public static final int FOLDER_SELECTED_FOREGROUND_COLOR                = 18;
    public static final int ARCHIVE_FOREGROUND_COLOR                        = 19;
    public static final int ARCHIVE_BACKGROUND_COLOR                        = 20;
    public static final int ARCHIVE_UNFOCUSED_BACKGROUND_COLOR              = 21;
    public static final int ARCHIVE_UNFOCUSED_FOREGROUND_COLOR              = 22;
    public static final int ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR     = 23;
    public static final int ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR     = 24;
    public static final int ARCHIVE_SELECTED_BACKGROUND_COLOR               = 25;
    public static final int ARCHIVE_SELECTED_FOREGROUND_COLOR               = 26;
    public static final int SYMLINK_FOREGROUND_COLOR                        = 27;
    public static final int SYMLINK_BACKGROUND_COLOR                        = 28;
    public static final int SYMLINK_UNFOCUSED_BACKGROUND_COLOR              = 29;
    public static final int SYMLINK_UNFOCUSED_FOREGROUND_COLOR              = 30;
    public static final int SYMLINK_SELECTED_BACKGROUND_COLOR               = 31;
    public static final int SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR     = 32;
    public static final int SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR     = 33;
    public static final int SYMLINK_SELECTED_FOREGROUND_COLOR               = 34;
    public static final int MARKED_FOREGROUND_COLOR                         = 35;
    public static final int MARKED_BACKGROUND_COLOR                         = 36;
    public static final int MARKED_UNFOCUSED_BACKGROUND_COLOR               = 37;
    public static final int MARKED_UNFOCUSED_FOREGROUND_COLOR               = 38;
    public static final int MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR      = 39;
    public static final int MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR      = 40;
    public static final int MARKED_SELECTED_BACKGROUND_COLOR                = 41;
    public static final int MARKED_SELECTED_FOREGROUND_COLOR                = 42;
    public static final int FILE_FOREGROUND_COLOR                           = 43;
    public static final int FILE_BACKGROUND_COLOR                           = 44;
    public static final int FILE_UNFOCUSED_BACKGROUND_COLOR                 = 45;
    public static final int FILE_UNFOCUSED_FOREGROUND_COLOR                 = 46;
    public static final int FILE_SELECTED_BACKGROUND_COLOR                  = 47;
    public static final int FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR        = 48;
    public static final int FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR        = 49;
    public static final int FILE_SELECTED_FOREGROUND_COLOR                  = 50;
    public static final int SHELL_FOREGROUND_COLOR                          = 51;
    public static final int SHELL_BACKGROUND_COLOR                          = 52;
    public static final int SHELL_SELECTED_FOREGROUND_COLOR                 = 53;
    public static final int SHELL_SELECTED_BACKGROUND_COLOR                 = 54;
    public static final int SHELL_HISTORY_FOREGROUND_COLOR                  = 55;
    public static final int SHELL_HISTORY_BACKGROUND_COLOR                  = 56;
    public static final int SHELL_HISTORY_SELECTED_FOREGROUND_COLOR         = 57;
    public static final int SHELL_HISTORY_SELECTED_BACKGROUND_COLOR         = 58;
    public static final int EDITOR_FOREGROUND_COLOR                         = 59;
    public static final int EDITOR_BACKGROUND_COLOR                         = 60;
    public static final int EDITOR_SELECTED_FOREGROUND_COLOR                = 61;
    public static final int EDITOR_SELECTED_BACKGROUND_COLOR                = 62;
    public static final int LOCATION_BAR_FOREGROUND_COLOR                   = 63;
    public static final int LOCATION_BAR_BACKGROUND_COLOR                   = 64;
    public static final int LOCATION_BAR_SELECTED_FOREGROUND_COLOR          = 65;
    public static final int LOCATION_BAR_SELECTED_BACKGROUND_COLOR          = 66;
    public static final int LOCATION_BAR_PROGRESS_COLOR                     = 67;
    public static final int STATUS_BAR_FOREGROUND_COLOR                     = 68;
    public static final int STATUS_BAR_BACKGROUND_COLOR                     = 69;
    public static final int STATUS_BAR_BORDER_COLOR                         = 70;
    public static final int STATUS_BAR_OK_COLOR                             = 71;
    public static final int STATUS_BAR_WARNING_COLOR                        = 72;
    public static final int STATUS_BAR_CRITICAL_COLOR                       = 73;



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Theme name. */
    private String  name;
    /** Theme type. */
    private int     type;
    /** Colors known to the theme. */
    private Color[] colors;
    /** Fonts known to the theme. */
    private Font[]  fonts;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    Theme() {
        colors = new Color[Theme.COLOR_COUNT];
        fonts  = new Font[Theme.FONT_COUNT];
    }

    Theme(int type, String name) {
        this();
        setName(name);
        setType(type);
    }

    void setType(int type) {
        this.type = type;
        if(type == USER_THEME)
            setName(Translator.get("theme.custom_theme"));
    }

    void setName(String name) {this.name = name;}

    // - Data retrieval ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the theme's type.
     * @return the theme's type.
     */
    public int getType() {return type;}

    /**
     * Returns the theme's name.
     * @return the theme's name.
     */
    public String getName() {return name;}

    void setFont(int id, Font font) {fonts[id] = font;}

    void setColor(int id, Color color) {colors[id] = color;}

    Font getFont(int id, boolean allowDefault) {
        if(fonts[id] == null && allowDefault)
            return ThemeManager.getDefaultFont(id, this);
        return fonts[id];
    }

    public Font getFont(int id) {return getFont(id, true);}

    Color getColor(int id, boolean allowDefault) {
        if(colors[id] == null && allowDefault)
            return ThemeManager.getDefaultColor(id, this);
        return colors[id];
    }

    public Color getColor(int id) {return getColor(id, true);}



    // - Misc. ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the theme's name.
     * @return the theme's name.
     */
    public String toString() {return getName();}
}
