 package com.mucommander.ui.theme;

import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.WeakHashMap;


/**
 * @author Nicolas Rinaudo
 */
public class ThemeData {
    // - Dirty hack ----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // This is an effort to make the ThemeData class a bit easier to maintain, but I'm the first
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



    // - Default fonts -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // The following fields are look&feel dependant values for the fonts that are used by
    // themes. We need to monitor them, as they are prone to change through UIManager.

    /** Default font for text area components. */
    private static Font  DEFAULT_TEXT_AREA_FONT;
    /** Default font for text field components. */
    private static Font  DEFAULT_TEXT_FIELD_FONT;
    /** Default font for label components. */
    private static Font  DEFAULT_LABEL_FONT;
    /** Default font for table components. */
    private static Font  DEFAULT_TABLE_FONT;



    // - Default colors ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Default foreground color for text area components. */
    private static Color DEFAULT_TEXT_AREA_COLOR;
    /** Default background color for text area components. */
    private static Color DEFAULT_TEXT_AREA_BACKGROUND_COLOR;
    /** Default selection foreground color for text area components. */
    private static Color DEFAULT_TEXT_AREA_SELECTION_COLOR;
    /** Default selection background color for text area components. */
    private static Color DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR;
    /** Default foreground color for text field components. */
    private static Color DEFAULT_TEXT_FIELD_COLOR;
    /** Default progress color for {@link com.mucommander.ui.comp.progress.ProgressTextField} components. */
    private static Color DEFAULT_TEXT_FIELD_PROGRESS_COLOR;
    /** Default background color for text field components. */
    private static Color DEFAULT_TEXT_FIELD_BACKGROUND_COLOR;
    /** Default selection foreground color for text field components. */
    private static Color DEFAULT_TEXT_FIELD_SELECTION_COLOR;
    /** Default selection background color for text field components. */
    private static Color DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR;
    /** Default foreground color for table components. */
    private static Color DEFAULT_TABLE_COLOR;
    /** Default background color for table components. */
    private static Color DEFAULT_TABLE_BACKGROUND_COLOR;
    /** Default selection foreground color for table components. */
    private static Color DEFAULT_TABLE_SELECTION_COLOR;
    /** Default selection background color for table components. */
    private static Color DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR;



    // - Listeners -----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Theme data will trigger font and color events when default values are changed.
    // This mechanism is purely internal to the package, and should never be exposed to the
    // rest of the world.

    /** Listeners on the default font and colors. */
    private static WeakHashMap listeners = new WeakHashMap();



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** All the colors contained by the theme. */
    private Color[] colors;
    /** All the fonts contained by the theme. */
    private Font[]  fonts;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Listens on default colors and fonts.
     */
    static {UIManager.addPropertyChangeListener(new DefaultValuesListener());}


    /**
     * Creates an empty set of theme data.
     */
    public ThemeData() {
        colors = new Color[COLOR_COUNT];
        fonts  = new Font[FONT_COUNT];
    }

    /**
     * Creates a new set of theme data.
     * <p>
     * The content of <code>from</code> will be copied in the new theme data. Note that
     * since we're copying the arrays themselves, rather than creating new ones and copying
     * each color and font individually, <code>from</code> will be unreliable at the end of this
     * call.
     * </p>
     * <p>
     * This constructor is only meant for optimisation purposes. When transforming
     * theme data in a proper theme, using this constructor allows us to not duplicate
     * all the fonts and color.
     * </p>
     * @param from theme data from which to import values.
     */
    ThemeData(ThemeData from) {
        this();
        fonts  = from.fonts;
        colors = from.colors;
    }



    // - Data import / export ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Clones the theme data.
     * @return a clone of the current theme data.
     */
    public ThemeData cloneData() {
        ThemeData data;
        int       i;

        data = new ThemeData();

        // Clones the theme's colors.
        for(i = 0; i < COLOR_COUNT; i++)
            data.colors[i] = colors[i];

        // Clones the theme's fonts.
        for(i = 0; i < FONT_COUNT; i++)
            data.fonts[i] = fonts[i];

        return data;
    }

    /**
     *
     */
    public void importData(ThemeData data) {
        int i;

        // Imports the theme's colors.
        for(i = 0; i < COLOR_COUNT; i++)
            setColor(i, data.colors[i]);

        // Imports the theme's fonts.
        for(i = 0; i < FONT_COUNT; i++)
            setFont(i, data.fonts[i]);
    }



    // - Items setting --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the specified color to the specified value.
     * @param id    identifier of the color to set.
     * @param color value to which the color should be set. <code>null</code> will reset the color to its default.
     */
    public synchronized boolean setColor(int id, Color color) {
        boolean buffer;

        checkColorIdentifier(id);

        buffer = isColorDifferent(id, color);
        colors[id] = color;

        return buffer;
    }

    /**
     * Sets the specified font to the specified value.
     * @param id   identifier of the font to set.
     * @param font value to which the font should be set. <code>null</code> will reset the font to its default.
     */
    public synchronized boolean setFont(int id, Font font) {
        boolean buffer;

        checkFontIdentifier(id);

        buffer = isFontDifferent(id, font);
        fonts[id] = font;

        return buffer;
    }


    // - Items retrieval -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public synchronized Color getColor(int id) {
        checkColorIdentifier(id);
        return (colors[id] == null) ? getDefaultColor(id) : colors[id];
    }

    public synchronized Font getFont(int id) {
        checkFontIdentifier(id);
        return (fonts[id] == null) ? getDefaultFont(id) : fonts[id];
    }



    // - Items status --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the specified data and the current one are identical.
     * @param data data against which to compare.
     * @return <code>true</code> if the specified data and the current one are identical, <code>false</code> otherwise.
     */
    public boolean isIdentical(ThemeData data) {
        int i;

        // Compares the colors.
        for(i = 0; i < COLOR_COUNT; i++)
            if(isColorDifferent(i, data.colors[i]))
                return false;

        // Compares the fonts.
        for(i = 0; i < FONT_COUNT; i++)
            if(isFontDifferent(i, data.fonts[i]))
                return false;

        return true;
    }

    /**
     * Checks whether the specified font is different from the one defined in the data.
     * @param  id   identifier of the font to check.
     * @param  font font to check.
     * @return      <code>true</code> if <code>font</code> is different from the one defined in the data.
     */
    public synchronized boolean isFontDifferent(int id, Font font) {
        if(font == null)
            return fonts[id] != null;
        else if(fonts[id] == null)
            return !getDefaultFont(id).equals(font);
        return !font.equals(fonts[id]);
    }

    /**
     * Checks whether the specified color is different from the one defined in the data.
     * @param  id    identifier of the color to check.
     * @param  color color to check.
     * @return       <code>true</code> if <code>color</code> is different from the one defined in the data.
     */
    public synchronized boolean isColorDifferent(int id, Color color) {
        if(color == null)
            return colors[id] != null;
        else if(colors[id] == null)
            return !getDefaultColor(id).equals(color);
        return !color.equals(colors[id]);
    }


    /**
     * Returns <code>true</code> if the specified color is set.
     * @param  id identifier of the color to check for.
     * @return <code>true</code> if the specified color is set, <code>false</code> otherwise.
     */
    public boolean isColorSet(int id) {return colors[id] != null;}

    /**
     * Returns <code>true</code> if the specified font is set.
     * @param  id identifier of the font to check for.
     * @return <code>true</code> if the specified font is set, <code>false</code> otherwise.
     */
    public boolean isFontSet(int id) {return fonts[id] != null;}



    // - Default values ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private static Color getDefaultColor(int id) {
        checkColorIdentifier(id);
        switch(id) {
            // File table background colors.
        case FILE_UNFOCUSED_BACKGROUND_COLOR:
        case FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR:
        case FOLDER_UNFOCUSED_BACKGROUND_COLOR:
        case ARCHIVE_UNFOCUSED_BACKGROUND_COLOR:
        case SYMLINK_UNFOCUSED_BACKGROUND_COLOR:
        case HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR:
        case MARKED_UNFOCUSED_BACKGROUND_COLOR:
        case FILE_TABLE_BACKGROUND_COLOR:
        case HIDDEN_FILE_BACKGROUND_COLOR:
        case FOLDER_BACKGROUND_COLOR:
        case ARCHIVE_BACKGROUND_COLOR:
        case SYMLINK_BACKGROUND_COLOR:
        case MARKED_BACKGROUND_COLOR:
        case FILE_BACKGROUND_COLOR:
	    return getTableBackgroundColor();

            // File table foreground colors (everything except marked
            // defaults to the l&f specific table foreground color).
        case HIDDEN_FILE_FOREGROUND_COLOR:
        case FOLDER_FOREGROUND_COLOR:
        case ARCHIVE_FOREGROUND_COLOR:
        case SYMLINK_FOREGROUND_COLOR:
        case FILE_UNFOCUSED_FOREGROUND_COLOR:
        case HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR:
        case FOLDER_UNFOCUSED_FOREGROUND_COLOR:
        case ARCHIVE_UNFOCUSED_FOREGROUND_COLOR:
        case SYMLINK_UNFOCUSED_FOREGROUND_COLOR:
        case FILE_FOREGROUND_COLOR:
	    return getTableColor();

            // Marked files foreground colors (they have to be different
            // of the standard file foreground colors).
        case MARKED_FOREGROUND_COLOR:
        case MARKED_UNFOCUSED_FOREGROUND_COLOR:
        case MARKED_SELECTED_FOREGROUND_COLOR:
        case MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            return Color.RED;

            // Text areas default foreground colors.
        case SHELL_FOREGROUND_COLOR:
        case EDITOR_FOREGROUND_COLOR:
            return getTextAreaColor();

            // Text areas default background colors.
        case SHELL_BACKGROUND_COLOR:
        case EDITOR_BACKGROUND_COLOR:
            return getTextAreaBackgroundColor();

            // Text fields default foreground colors.
        case SHELL_HISTORY_FOREGROUND_COLOR:
        case LOCATION_BAR_FOREGROUND_COLOR:
        case STATUS_BAR_FOREGROUND_COLOR:
            return getTextFieldColor();

            // Text fields default background colors.
        case LOCATION_BAR_BACKGROUND_COLOR:
        case SHELL_HISTORY_BACKGROUND_COLOR:
            return getTextFieldBackgroundColor();

            // The location bar progress color is a bit of a special case,
            // as it requires alpha transparency.
        case LOCATION_BAR_PROGRESS_COLOR:
	    Color color;

	    color = getTextFieldSelectionBackgroundColor();
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), 64);

            // Selected table background colors.
        case HIDDEN_FILE_SELECTED_BACKGROUND_COLOR:
        case FOLDER_SELECTED_BACKGROUND_COLOR:
        case ARCHIVE_SELECTED_BACKGROUND_COLOR:
        case SYMLINK_SELECTED_BACKGROUND_COLOR:
        case MARKED_SELECTED_BACKGROUND_COLOR:
        case FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case FILE_SELECTED_BACKGROUND_COLOR:
	    return getTableSelectionBackgroundColor();

        case FILE_TABLE_BORDER_COLOR:
            return Color.GRAY;

            // Foreground color for selected elements in the file table.
        case HIDDEN_FILE_SELECTED_FOREGROUND_COLOR:
        case FOLDER_SELECTED_FOREGROUND_COLOR:
        case ARCHIVE_SELECTED_FOREGROUND_COLOR:
        case SYMLINK_SELECTED_FOREGROUND_COLOR:
        case HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case FILE_SELECTED_FOREGROUND_COLOR:
	    return getTableSelectionColor();

            // Foreground color for selected text area elements.
        case SHELL_SELECTED_FOREGROUND_COLOR:
        case EDITOR_SELECTED_FOREGROUND_COLOR:
            return getTextAreaSelectionColor();

            // Background color for selected text area elements.
        case SHELL_SELECTED_BACKGROUND_COLOR:
        case EDITOR_SELECTED_BACKGROUND_COLOR:
            return getTextAreaSelectionBackgroundColor();

            // Foreground color for selected text fields elements.
        case LOCATION_BAR_SELECTED_FOREGROUND_COLOR:
        case SHELL_HISTORY_SELECTED_FOREGROUND_COLOR:
            return getTextFieldSelectionColor();

            // Background color for selected text fields elements.
        case SHELL_HISTORY_SELECTED_BACKGROUND_COLOR:
        case LOCATION_BAR_SELECTED_BACKGROUND_COLOR:
            return getTextFieldSelectionBackgroundColor();

            // Status bar defaults.
        case STATUS_BAR_BACKGROUND_COLOR:
            return new Color(0xD5D5D5);

        case STATUS_BAR_BORDER_COLOR:
            return new Color(0x7A7A7A);

        case STATUS_BAR_OK_COLOR:
            return new Color(0x70EC2B);

        case STATUS_BAR_WARNING_COLOR:
            return new Color(0xFF7F00);

        case STATUS_BAR_CRITICAL_COLOR:
            return new Color(0xFF0000);
        }

        // This should never happen.
        return null;
    }

    /**
     * Returns the default value for the specified font.
     * @return    the default value for the specified font.
     */
    private static final Font getDefaultFont(int id) {
        checkFontIdentifier(id);

	switch(id) {
            // Table font.
        case FILE_TABLE_FONT:
            return getTableFont();

	    // Text Area font.
        case EDITOR_FONT:
        case SHELL_FONT:
	    return getTextAreaFont();

	    // Text Field font.
        case LOCATION_BAR_FONT:
        case SHELL_HISTORY_FONT:
	    return getTextFieldFont();

        case STATUS_BAR_FONT:
	    return getLabelFont();
        }

        // This should never happen.
        return null;
    }



    // - L&F dependant defaults ----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the current look and feel's text area font.
     * @return the current look and feel's text area font.
     */
    private static Font getTextAreaFont() {
        if(DEFAULT_TEXT_AREA_FONT == null) {
            if((DEFAULT_TEXT_AREA_FONT = UIManager.getDefaults().getFont("TextArea.font")) == null)
                DEFAULT_TEXT_AREA_FONT = new JTable().getFont();
        }
	return DEFAULT_TEXT_AREA_FONT;
    }

    /**
     * Returns the current look and feel's text field font.
     * @return the current look and feel's text field font.
     */
    private static Font getTextFieldFont() {
        if(DEFAULT_TEXT_FIELD_FONT == null) {
            if((DEFAULT_TEXT_FIELD_FONT = UIManager.getDefaults().getFont("TextField.font")) == null)
                DEFAULT_TEXT_FIELD_FONT = new JTable().getFont();
        }
	return DEFAULT_TEXT_FIELD_FONT;
    }

    /**
     * Returns the current look and feel's label font.
     * @return the current look and feel's label font.
     */
    private static Font getLabelFont() {
        if(DEFAULT_LABEL_FONT == null) {
            if((DEFAULT_LABEL_FONT = UIManager.getDefaults().getFont("Label.font")) == null)
                DEFAULT_LABEL_FONT = new JTable().getFont();
        }
	return DEFAULT_LABEL_FONT;
    }

    private static Color escapeColor(Color color) {return new Color(color.getRGB(), (color.getRGB() & 0xFF000000) != 0xFF000000);}

    private static synchronized Color getTextFieldProgressColor() {
        if(DEFAULT_TEXT_FIELD_PROGRESS_COLOR == null) {
            Color buffer;

            buffer = getTextFieldSelectionBackgroundColor();
            DEFAULT_TEXT_FIELD_PROGRESS_COLOR = escapeColor(new Color(buffer.getRed(), buffer.getGreen(), buffer.getBlue(), 64));
        }
	return DEFAULT_TEXT_AREA_COLOR;
    }

    private static synchronized Color getTextAreaColor() {
        if(DEFAULT_TEXT_AREA_COLOR == null) {
            if((DEFAULT_TEXT_AREA_COLOR = UIManager.getDefaults().getColor("TextArea.foreground")) == null)
                DEFAULT_TEXT_AREA_COLOR = new JTextArea().getForeground();
            DEFAULT_TEXT_AREA_COLOR = escapeColor(DEFAULT_TEXT_AREA_COLOR);
        }
	return DEFAULT_TEXT_AREA_COLOR;
    }

    private static synchronized Color getTextAreaBackgroundColor() {
        if(DEFAULT_TEXT_AREA_BACKGROUND_COLOR == null) {
            if((DEFAULT_TEXT_AREA_BACKGROUND_COLOR = UIManager.getDefaults().getColor("TextArea.background")) == null)
                DEFAULT_TEXT_AREA_BACKGROUND_COLOR = new JTextArea().getBackground();
            DEFAULT_TEXT_AREA_BACKGROUND_COLOR = escapeColor(DEFAULT_TEXT_AREA_BACKGROUND_COLOR);
        }
	return DEFAULT_TEXT_AREA_BACKGROUND_COLOR;
    }

    private static synchronized Color getTextAreaSelectionColor() {
        if(DEFAULT_TEXT_AREA_SELECTION_COLOR == null) {
            if((DEFAULT_TEXT_AREA_SELECTION_COLOR = UIManager.getDefaults().getColor("TextArea.selectionForeground")) == null)
                DEFAULT_TEXT_AREA_SELECTION_COLOR = new JTextArea().getSelectionColor();
            DEFAULT_TEXT_AREA_SELECTION_COLOR = escapeColor(DEFAULT_TEXT_AREA_SELECTION_COLOR);
        }
	return DEFAULT_TEXT_AREA_SELECTION_COLOR;
    }

    private static synchronized Color getTextAreaSelectionBackgroundColor() {
        if(DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR == null) {
            if((DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR = UIManager.getDefaults().getColor("TextArea.selectionBackground")) == null)
            DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR = new JTextArea().getSelectedTextColor();
            DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR = escapeColor(DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR);
        }
	return DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR;
    }

    private static synchronized Color getTextFieldColor() {
        if(DEFAULT_TEXT_FIELD_COLOR == null) {
            if((DEFAULT_TEXT_FIELD_COLOR = UIManager.getDefaults().getColor("TextField.foreground")) == null)
                DEFAULT_TEXT_FIELD_COLOR = new JTextField().getForeground();
            DEFAULT_TEXT_FIELD_COLOR = escapeColor(DEFAULT_TEXT_FIELD_COLOR);
        }
	return DEFAULT_TEXT_FIELD_COLOR;
    }

    private static synchronized Color getTextFieldBackgroundColor() {
        if(DEFAULT_TEXT_FIELD_BACKGROUND_COLOR == null) {
            if((DEFAULT_TEXT_FIELD_BACKGROUND_COLOR = UIManager.getDefaults().getColor("TextField.background")) == null)
                DEFAULT_TEXT_FIELD_BACKGROUND_COLOR = new JTextField().getBackground();
            DEFAULT_TEXT_FIELD_BACKGROUND_COLOR = escapeColor(DEFAULT_TEXT_FIELD_BACKGROUND_COLOR);
        }
	return DEFAULT_TEXT_FIELD_BACKGROUND_COLOR;
    }

    private static synchronized Color getTextFieldSelectionColor() {
        if(DEFAULT_TEXT_FIELD_SELECTION_COLOR == null) {
            if((DEFAULT_TEXT_FIELD_SELECTION_COLOR = UIManager.getDefaults().getColor("TextField.selectionForeground")) == null)
                DEFAULT_TEXT_FIELD_SELECTION_COLOR = new JTextField().getSelectionColor();
            DEFAULT_TEXT_FIELD_SELECTION_COLOR = escapeColor(DEFAULT_TEXT_FIELD_SELECTION_COLOR);
        }
	return DEFAULT_TEXT_FIELD_SELECTION_COLOR;
    }

    private static synchronized Color getTextFieldSelectionBackgroundColor() {
        if(DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR == null) {
            if((DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR = UIManager.getDefaults().getColor("TextField.selectionBackground")) == null)
                DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR = new JTextField().getSelectedTextColor();
            DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR = escapeColor(DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR);
        }
	return DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR;
    }

    private static synchronized Color getTableColor() {
        if(DEFAULT_TABLE_COLOR == null) {
            if((DEFAULT_TABLE_COLOR = UIManager.getDefaults().getColor("Table.foreground")) == null)
                DEFAULT_TABLE_COLOR = new JTable().getForeground();
            DEFAULT_TABLE_COLOR = escapeColor(DEFAULT_TABLE_COLOR);
        }
	return DEFAULT_TABLE_COLOR;
    }

    private static synchronized Color getTableBackgroundColor() {
        if(DEFAULT_TABLE_BACKGROUND_COLOR == null) {
            if((DEFAULT_TABLE_BACKGROUND_COLOR = UIManager.getDefaults().getColor("Table.background")) == null)
                DEFAULT_TABLE_BACKGROUND_COLOR = new JTable().getBackground();
            DEFAULT_TABLE_BACKGROUND_COLOR = escapeColor(DEFAULT_TABLE_BACKGROUND_COLOR);
        }
        return DEFAULT_TABLE_BACKGROUND_COLOR;
    }

    private static synchronized Color getTableSelectionColor() {
        if(DEFAULT_TABLE_SELECTION_COLOR == null) {
            if((DEFAULT_TABLE_SELECTION_COLOR = UIManager.getDefaults().getColor("Table.selectionForeground")) == null)
                DEFAULT_TABLE_SELECTION_COLOR = new JTable().getSelectionForeground();
            DEFAULT_TABLE_SELECTION_COLOR = escapeColor(DEFAULT_TABLE_SELECTION_COLOR);
        }
	return DEFAULT_TABLE_SELECTION_COLOR;
    }

    private static synchronized Color getTableSelectionBackgroundColor() {
        if(DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR == null) {
            if((DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR = UIManager.getDefaults().getColor("Table.selectionBackground")) == null)
                DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR = new JTable().getSelectionBackground();
            DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR = escapeColor(DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR);
        }
	return DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR;
    }

    private static Font getTableFont() {
        if(DEFAULT_TABLE_FONT == null) {
            if((DEFAULT_TABLE_FONT = UIManager.getDefaults().getFont("Table.font")) == null)
                DEFAULT_TABLE_FONT = new JTable().getFont();
        }
	return DEFAULT_TABLE_FONT;
    }



    // - Theme events --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    static void addDefaultValuesListener(ThemeListener listener) {listeners.put(listener, null);}
    static void removeDefaultValuesListener(ThemeListener listener) {listeners.remove(listener);}
    private static void triggerFontEvent(int id, Font font) {
        Iterator         iterator;
        FontChangedEvent event;

        event = new FontChangedEvent(null, id, font);

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).fontChanged(event);
    }

    static void triggerColorEvent(int id, Color color) {
        Iterator          iterator;
        ColorChangedEvent event;

        event = new ColorChangedEvent(null, id, color);
        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).colorChanged(event);
    }



    // - Helper methods ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Checks whether the specified color identifier is legal.
     * @param  id                       identifier to check against.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color identifier.
     */
    private static void checkColorIdentifier(int id) {
        if(id < 0 || id >= COLOR_COUNT)
            throw new IllegalArgumentException("Illegal color identifier: " + id);
    }

    /**
     * Checks whether the specified font identifier is legal.
     * @param  id                       identifier to check against.
     * @throws IllegalArgumentException if <code>id</code> is not a legal font identifier.
     */
    private static void checkFontIdentifier(int id) {
        if(id < 0 || id >= FONT_COUNT)
            throw new IllegalArgumentException("Illegal font identifier: " + id);
    }


    private static synchronized void resetTextAreaColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_AREA_COLOR;
        DEFAULT_TEXT_AREA_COLOR = null;

        if(!getTextAreaColor().equals(buffer)) {
            triggerColorEvent(SHELL_FOREGROUND_COLOR, getTextAreaColor());
            triggerColorEvent(EDITOR_FOREGROUND_COLOR, getTextAreaColor());
        }
    }

    private static synchronized void resetTextAreaBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_AREA_BACKGROUND_COLOR;
        DEFAULT_TEXT_AREA_BACKGROUND_COLOR = null;

        if(!getTextAreaBackgroundColor().equals(buffer)) {
            triggerColorEvent(SHELL_BACKGROUND_COLOR, getTextAreaBackgroundColor());
            triggerColorEvent(EDITOR_BACKGROUND_COLOR, getTextAreaBackgroundColor());
        }
    }

    private static synchronized void resetTextAreaSelectionColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_AREA_SELECTION_COLOR;
        DEFAULT_TEXT_AREA_SELECTION_COLOR = null;

        if(!getTextAreaSelectionColor().equals(buffer)) {
            triggerColorEvent(SHELL_SELECTED_FOREGROUND_COLOR, getTextAreaSelectionColor());
            triggerColorEvent(EDITOR_SELECTED_FOREGROUND_COLOR, getTextAreaSelectionColor());
        }
    }

    private static synchronized void resetTextAreaSelectionBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR;
        DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR = null;

        if(!getTextAreaSelectionBackgroundColor().equals(buffer)) {
            triggerColorEvent(SHELL_SELECTED_BACKGROUND_COLOR, getTextAreaSelectionBackgroundColor());
            triggerColorEvent(EDITOR_SELECTED_BACKGROUND_COLOR, getTextAreaSelectionBackgroundColor());
        }
    }

    private static synchronized void resetTextFieldColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_FIELD_COLOR;
        DEFAULT_TEXT_FIELD_COLOR = null;

        if(!getTextFieldColor().equals(buffer)) {
            triggerColorEvent(SHELL_HISTORY_FOREGROUND_COLOR, getTextFieldColor());
            triggerColorEvent(LOCATION_BAR_FOREGROUND_COLOR, getTextFieldColor());
            triggerColorEvent(STATUS_BAR_FOREGROUND_COLOR, getTextFieldColor());
        }
    }

    private static synchronized void resetTextFieldBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_FIELD_BACKGROUND_COLOR;
        DEFAULT_TEXT_FIELD_BACKGROUND_COLOR = null;

        if(!getTextFieldBackgroundColor().equals(buffer)) {
            triggerColorEvent(SHELL_HISTORY_BACKGROUND_COLOR, getTextFieldBackgroundColor());
            triggerColorEvent(LOCATION_BAR_BACKGROUND_COLOR, getTextFieldBackgroundColor());
            triggerColorEvent(STATUS_BAR_BACKGROUND_COLOR, getTextFieldBackgroundColor());
        }
    }

    private static synchronized void resetTextFieldSelectionColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_FIELD_SELECTION_COLOR;
        DEFAULT_TEXT_FIELD_SELECTION_COLOR = null;
        DEFAULT_TEXT_FIELD_PROGRESS_COLOR  = null;

        if(!getTextFieldSelectionColor().equals(buffer)) {
            triggerColorEvent(SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, getTextFieldSelectionColor());
            triggerColorEvent(LOCATION_BAR_SELECTED_FOREGROUND_COLOR, getTextFieldSelectionColor());
            triggerColorEvent(LOCATION_BAR_PROGRESS_COLOR, getTextFieldProgressColor());
        }
    }

    private static synchronized void resetTextFieldSelectionBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR;
        DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR = null;

        if(!getTextFieldSelectionBackgroundColor().equals(buffer)) {
            triggerColorEvent(SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, getTextFieldSelectionBackgroundColor());
            triggerColorEvent(LOCATION_BAR_SELECTED_BACKGROUND_COLOR, getTextFieldSelectionBackgroundColor());
        }
    }

    private static synchronized void resetTableColor() {
        Color buffer;

        buffer = DEFAULT_TABLE_COLOR;
        DEFAULT_TABLE_COLOR = null;

        if(!getTableColor().equals(buffer)) {
            triggerColorEvent(HIDDEN_FILE_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(FOLDER_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(ARCHIVE_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(SYMLINK_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(FILE_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(FOLDER_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(ARCHIVE_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(SYMLINK_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(FILE_FOREGROUND_COLOR, getTableColor());
        }
    }

    private static synchronized void resetTableBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TABLE_BACKGROUND_COLOR;
        DEFAULT_TABLE_BACKGROUND_COLOR = null;

        if(!getTableBackgroundColor().equals(buffer)) {
            triggerColorEvent(FILE_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FOLDER_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(ARCHIVE_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(SYMLINK_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(MARKED_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FILE_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(HIDDEN_FILE_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FOLDER_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(ARCHIVE_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(SYMLINK_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(MARKED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FILE_TABLE_BACKGROUND_COLOR, getTableBackgroundColor());
        }
    }

    private static synchronized void resetTableSelectionColor() {
        Color buffer;

        buffer = DEFAULT_TABLE_SELECTION_COLOR;
        DEFAULT_TABLE_SELECTION_COLOR = null;

        if(!getTableSelectionColor().equals(buffer)) {
            triggerColorEvent(HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(FOLDER_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(ARCHIVE_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(SYMLINK_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(FILE_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
        }
    }

    private static synchronized void resetTableSelectionBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR;
        DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR = null;

        if(!getTableSelectionBackgroundColor().equals(buffer)) {
            triggerColorEvent(HIDDEN_FILE_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(FOLDER_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(ARCHIVE_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(SYMLINK_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(MARKED_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(FILE_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
        }
    }

    private static synchronized void resetTableFont() {
        Font buffer;

        buffer = DEFAULT_TABLE_FONT;
        DEFAULT_TABLE_FONT = null;

        if(!getTableFont().equals(buffer))
            triggerFontEvent(FILE_TABLE_FONT, getTableFont());
    }

    private static synchronized void resetTextAreaFont() {
        Font buffer;

        buffer = DEFAULT_TEXT_AREA_FONT;
        DEFAULT_TEXT_AREA_FONT = null;

        if(!getTextAreaFont().equals(buffer)) {
            triggerFontEvent(EDITOR_FONT, getTextAreaFont());
            triggerFontEvent(SHELL_FONT, getTextAreaFont());
        }
    }

    private static synchronized void resetTextFieldFont() {
        Font buffer;

        buffer = DEFAULT_TEXT_FIELD_FONT;
        DEFAULT_TEXT_FIELD_FONT = null;

        if(!getTextFieldFont().equals(buffer)) {
            triggerFontEvent(LOCATION_BAR_FONT, getTextFieldFont());
            triggerFontEvent(SHELL_HISTORY_FONT, getTextFieldFont());
        }
    }

    private static synchronized void resetLabelFont() {
        Font buffer;

        buffer = DEFAULT_LABEL_FONT;
        DEFAULT_LABEL_FONT = null;

        if(!getLabelFont().equals(buffer))
            triggerFontEvent(STATUS_BAR_FONT, getLabelFont());
    }



    // - Default values listener ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private static class DefaultValuesListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            String property;
            property = event.getPropertyName();
            if(property.equals("lookAndFeel")) {
                resetTextAreaColor();
                resetTextAreaBackgroundColor();
                resetTextAreaSelectionColor();
                resetTextAreaSelectionBackgroundColor();
                resetTextFieldColor();
                resetTextFieldBackgroundColor();
                resetTextFieldSelectionColor();
                resetTextFieldSelectionBackgroundColor();
                resetTableColor();
                resetTableBackgroundColor();
                resetTableSelectionColor();
                resetTableSelectionBackgroundColor();
                resetTableFont();
                resetTextAreaFont();
                resetTextFieldFont();
                resetLabelFont();
            }
            else if(property.equals("TextArea.foreground"))
                resetTextAreaColor();
            else if(property.equals("TextArea.background"))
                resetTextAreaBackgroundColor();
            else if(property.equals("TextArea.selectionForeground"))
                resetTextAreaSelectionColor();
            else if(property.equals("TextArea.selectionBackground"))
                resetTextAreaSelectionBackgroundColor();
            else if(property.equals("TextField.foreground"))
                resetTextFieldColor();
            else if(property.equals("TextField.background"))
                resetTextFieldBackgroundColor();
            else if(property.equals("TextField.selectionForeground"))
                resetTextFieldSelectionColor();
            else if(property.equals("TextField.selectionBackground"))
                resetTextFieldSelectionBackgroundColor();
            else if(property.equals("Table.foreground"))
                resetTableColor();
            else if(property.equals("Table.background"))
                resetTableBackgroundColor();
            else if(property.equals("Table.selectionForeground"))
                resetTableSelectionColor();
            else if(property.equals("Table.selectionBackground"))
                resetTableSelectionBackgroundColor();
            else if(property.equals("Table.font"))
                resetTableFont();
            else if(property.equals("TextArea.font"))
                resetTextAreaFont();
            else if(property.equals("TextField.font"))
                resetTextFieldFont();
            else if(property.equals("Label.font"))
                resetLabelFont();
        }
    }
}
