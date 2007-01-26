package com.mucommander.ui.theme;

import com.mucommander.Debug;

import java.awt.Color;
import java.awt.Font;
import java.lang.ref.*;
import java.io.*;

/**
 * Describes a set of custom colors and fonts that can be applied to muCommander.
 * <p>
 * There are two different types of themes in muCommander: predefined ones and
 * current one. A theme's type can be checked through the {@link #getType()} method.<break/>
 * Predefined themes cannot be modified, and will throw an <code>java.lang.IllegalStateException</code>
 * if it is attempted.<br/>
 * </p>
 * <p>
 * A theme does not need to have custom values for every single customisable item. If a value
 * isn't set, the related component is expected to use the default look and feel and/or system
 * value. This means that a typical custom value access will look something like:
 * <pre>
 * JTextField field;
 *
 * field = new JTextField();
 * if(theme.hasCustomFont(Theme.LOCATION_BAR))
 *     field.setFont(theme.getFont(Theme.LOCATION_BAR));
 * </pre>
 * </p>
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
    static final int COLOR_COUNT = 40;



    // - Font definitions ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Font used to display files. */
    public static final int FILE_TABLE                         = 0;
    /** Font used in the shell. */
    public static final int SHELL                              = 1;
    /** Font used in the text editor. */
    public static final int EDITOR                             = 2;
    /** Font used in the location bar. */
    public static final int LOCATION_BAR                       = 3;
    /** Font used in the shell history. */
    public static final int SHELL_HISTORY                      = 4;
    /** Font used in the volume label. */
    public static final int STATUS_BAR                       = 5;



    // - Color definitions ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Background color for files in the file table. */
    public static final int FILE_BACKGROUND                    = 0;
    /** Background color for files in the file table, when it doesn't have focus. */
    public static final int FILE_UNFOCUSED_BACKGROUND          = 1;
    /** Text color for hidden files in the file table. */
    public static final int HIDDEN_FILE                        = 2;
    /** Text color for folders in the file table. */
    public static final int FOLDER                             = 3;
    /** Text color for archives in the file table. */
    public static final int ARCHIVE                            = 4;
    /** Text color for symlinks in the file table. */
    public static final int SYMLINK                            = 5;
    /** Text color for marked files in the file table. */
    public static final int MARKED                             = 6;
    /** Text color for plain files in the file table. */
    public static final int FILE                               = 7;
    /** Text color for the shell. */
    public static final int SHELL_TEXT                         = 8;
    /** Background color for the shell. */
    public static final int SHELL_BACKGROUND                   = 9;
    /** Text color for the editor. */
    public static final int EDITOR_TEXT                        = 10;
    /** Background color for the editor. */
    public static final int EDITOR_BACKGROUND                  = 11;
    /** Text color for the location bar. */
    public static final int LOCATION_BAR_TEXT                  = 12;
    /** Background color for the location bar. */
    public static final int LOCATION_BAR_BACKGROUND            = 13;
    /** Background color for the location har when it's being used as a progress bar. */
    public static final int LOCATION_BAR_PROGRESS              = 14;
    /** Selected version of {@link #FILE_BACKGROUND}. */
    public static final int FILE_BACKGROUND_SELECTED           = 15;
    /** Selected version of {@link #FILE_UNFOCUSED_BACKGROUND}. */
    public static final int FILE_UNFOCUSED_BACKGROUND_SELECTED = 16;
    /** Selected version of {@link #HIDDEN_FILE}. */
    public static final int HIDDEN_FILE_SELECTED               = 17;
    /** Selected version of {@link #FOLDER}. */
    public static final int FOLDER_SELECTED                    = 18;
    /** Selected version of {@link #ARCHIVE}. */
    public static final int ARCHIVE_SELECTED                   = 19;
    /** Selected version of {@link #SYMLINK}. */
    public static final int SYMLINK_SELECTED                   = 20;
    /** Selected version of {@link #MARKED}. */
    public static final int MARKED_SELECTED                    = 21;
    /** Selected version of {@link #FILE}. */
    public static final int FILE_SELECTED                      = 22;
    /** Selected version of {@link #SHELL_TEXT}. */
    public static final int SHELL_TEXT_SELECTED                = 23;
    /** Selected version of {@link #SHELL_BACKGROUND}. */
    public static final int SHELL_BACKGROUND_SELECTED          = 24;
    /** Selected version of {@link #EDITOR_TEXT}. */
    public static final int EDITOR_TEXT_SELECTED               = 25;
    /** Selected version of {@link #EDITOR_BACKGROUND}. */
    public static final int EDITOR_BACKGROUND_SELECTED         = 26;
    /** Selected version of {@link #LOCATION_BAR_TEXT}. */
    public static final int LOCATION_BAR_TEXT_SELECTED         = 27;
    /** Selected version of {@link #LOCATION_BAR_BACKGROUND}. */
    public static final int LOCATION_BAR_BACKGROUND_SELECTED   = 28;
    /** Color for the borders of the file table panels. */
    public static final int FILE_TABLE_BORDER                  = 29;
    /** Color used for the shell history text. */
    public static final int SHELL_HISTORY_TEXT                  = 30;
    /** Color used for the shell history background. */
    public static final int SHELL_HISTORY_BACKGROUND           = 31;
    /** Selected version of the color used for the shell history text. */
    public static final int SHELL_HISTORY_TEXT_SELECTED        = 32;
    /** Selected version of the color used for the shell history background. */
    public static final int SHELL_HISTORY_BACKGROUND_SELECTED  = 33;
    /** Background color for the volume label. */
    public static final int STATUS_BAR_BACKGROUND              = 34;
    /** Border color for the volume label. */
    public static final int STATUS_BAR_BORDER                  = 35;
    /** 'OK' color for the volume label. */
    public static final int STATUS_BAR_OK                      = 36;
    /** 'WARNING' color for the volume label. */
    public static final int STATUS_BAR_WARNING                 = 37;
    /** 'CRITICAL' color for the volume label. */
    public static final int STATUS_BAR_CRITICAL                = 38;
    /** Text color for the volume label. */
    public static final int STATUS_BAR_TEXT                    = 39;



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Theme name. */
    private String        name;
    /** Theme type. */
    private int           type;
    /** Path to the theme in the type. */
    private String        path;
    /** Soft reference to the theme data. */
    private SoftReference data;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new theme with the specified name, located in the specified type.
     * @param  type location of the theme data file.
     * @param  name   name of the theme.
     * @param  path   path to the theme (type dependant).
     * @throws Exception thrown if an error occurs while loading the theme.
     */
    Theme(int type, String name, String path) throws Exception {
        this.name = name;
        this.type = type;
        this.path = path;

        // If the data is not available and we're not dealing with a user theme, throw an exception.
        // User themes are a bit different: they *must* be present, even if only with default values.
        if(getThemeData() == null) {
            if(type != ThemeManager.USER_THEME)
                throw new Exception();
        }
    }

    /**
     * Creates a user theme without loading its data.
     * <p>
     * Developers should be very careful using this constructor: they might end up overwriting
     * a perfectly legal user theme without any hope of recovery.
     * </p>
     */
    Theme(String name, ThemeData data) {
        this.name = name;
        this.type = ThemeManager.USER_THEME;
        this.data = new SoftReference(data);
    }

    void importData(ThemeData newData) {
        int       i;
        ThemeData oldData;

        if((oldData = getThemeData()) == null)
            data = new SoftReference(oldData = new ThemeData());
        for(i = 0; i < FONT_COUNT; i++)
            oldData.setFont(i, newData.getFont(i));
        for(i = 0; i < COLOR_COUNT; i++)
            oldData.setColor(i, newData.getColor(i));
    }


    // - Data retrieval ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the theme is available.
     * <p>
     * Due to the somewhat peculiar way theme data is handled, a theme might need to be reloaded
     * before being accessed. In most cases, if a theme instance exists, it means that its
     * description file exists and is correct. There are, however, two cases where this doesn't
     * hold true:
     * <ul>
     *   <li>The user theme is corrupt, in which case it'll be in memory but unaivalable.</i>
     *   <li>The theme's data file has been modified, or made unaccessible.</li>
     * </ul>
     * </p>
     * <p>
     * This method lets developers check whether a theme's data is available. It might be quite
     * CPU intensive though, as it might result in loading the whole theme again. It is advised
     * to only call it when absolutely necessary, such as when displaying a theme's preview (no
     * error will be raised, but the preview will not actually match the theme if it's corrupt).
     * </p>
     * @return <code>true</code> if the theme is available.
     */
    public boolean isAvailable() {return getThemeData() != null;}

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

    /**
     * Returns the path to the theme.
     * @return the path to the theme.
     */
    public String getPath() {return path;}

    /**
     * Returns this theme's data.
     * <p>
     * If the data has not yet been loaded, or has been garbage collected, this
     * method will load it.
     * </p>
     * @return this theme's data.
     */
    ThemeData getThemeData() {
        if(data == null || data.get() == null) {
            InputStream in;

            in = null;
            try {data = new SoftReference(ThemeReader.read(in = ThemeManager.openInputStream(type, path)));}
            catch(Exception e) {
                // Logs errors in debug mode.
                if(Debug.ON) {
                    Debug.trace("Failed to load theme " + path);
                    Debug.trace(e);
                }
                return null;
            }
            finally {
                if(in != null) {
                    try {in.close();}
                    catch(Exception e) {}
                }
            }
        }
        return (ThemeData)data.get();
    }

    /**
     * Returns the theme's requested font.
     * <p>
     * If the theme doesn't use a custom value for the specified font, this method will return null.
     * </p>
     * @param  id                       identifier of the font to retrieve.
     * @return                          the requested font if it exists, <code>null</code> otherwise.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color id.
     */
    public Font getFont(int id) {
        ThemeData buffer;
        Font      font;

        font = null;
        if((buffer = getThemeData()) != null)
            font = buffer.getFont(id);
        if(font == null)
            return ThemeManager.getDefaultFont(id);
        return font;
    }

    /**
     * Returns the theme's requested color.
     * <p>
     * If the theme doesn't use a custom value for the specified color, this method will return null.
     * </p>
     * @param  id                       identifier of the color to retrieve.
     * @return                          the requested color if it exists, <code>null</code> otherwise.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color id.
     */
    public Color getColor(int id) {
        ThemeData buffer;
        Color     color;

        color = null;
        if((buffer = getThemeData()) != null)
            color = buffer.getColor(id);
        if(color == null)
            return ThemeManager.getDefaultColor(id);
        return color;
    }



    // - Misc. ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the theme's name.
     * @return the theme's name.
     */
    public String toString() {return getName();}
}
