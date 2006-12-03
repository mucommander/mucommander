package com.mucommander.ui.theme;

import com.mucommander.Debug;

import java.awt.Color;
import java.awt.Font;
import java.util.*;

/**
 * Describes a set of custom colors and fonts that can be applied to muCommander.
 * <p>
 * There are two different types of themes in muCommander: predefined ones and
 * user theme. A theme's type can be checked through the {@link #isUserTheme()} method.<break/>
 * Predefined themes cannot be modified, and will throw an <code>IllegalStateException</code>
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
 *     field.setFont(theme.getCustomFont(Theme.LOCATION_BAR));
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
    private static final int FONT_COUNT  = 4;
    /** Number of known colors. */
    private static final int COLOR_COUNT = 30;



    // - Listeners -----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Registered theme listeners. */
    private static WeakHashMap listeners      = new WeakHashMap();
    /** Whether theme events should be triggered. */
    private        boolean     triggerEvents;



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
    /** Selected version of {#FILE_BACKGROUND}. */
    public static final int FILE_BACKGROUND_SELECTED           = 15;
    /** Selected version of {#FILE_UNFOCUSED_BACKGROUND}. */
    public static final int FILE_UNFOCUSED_BACKGROUND_SELECTED = 16;
    /** Selected version of {#HIDDEN_FILE}. */
    public static final int HIDDEN_FILE_SELECTED               = 17;
    /** Selected version of {#FOLDER}. */
    public static final int FOLDER_SELECTED                    = 18;
    /** Selected version of {#ARCHIVE}. */
    public static final int ARCHIVE_SELECTED                   = 19;
    /** Selected version of {#SYMLINK}. */
    public static final int SYMLINK_SELECTED                   = 20;
    /** Selected version of {#MARKED}. */
    public static final int MARKED_SELECTED                    = 21;
    /** Selected version of {#FILE}. */
    public static final int FILE_SELECTED                      = 22;
    /** Selected version of {#SHELL_TEXT}. */
    public static final int SHELL_TEXT_SELECTED                = 23;
    /** Selected version of {#SHELL_BACKGROUND}. */
    public static final int SHELL_BACKGROUND_SELECTED          = 24;
    /** Selected version of {#EDITOR_TEXT}. */
    public static final int EDITOR_TEXT_SELECTED               = 25;
    /** Selected version of {#EDITOR_BACKGROUND}. */
    public static final int EDITOR_BACKGROUND_SELECTED         = 26;
    /** Selected version of {#LOCATION_BAR_TEXT}. */
    public static final int LOCATION_BAR_TEXT_SELECTED         = 27;
    /** Selected version of {#LOCATION_BAR_BACKGROUND}. */
    public static final int LOCATION_BAR_BACKGROUND_SELECTED   = 28;
    /** Color for the borders of the file table panels. */
    public static final int FILE_TABLE_BORDER                  = 29;



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Colors known to the theme. */
    private Color[] colors;
    /** Fonts known to the theme. */
    private Font[]  fonts;
    /** Whether or not this is the user theme. */
    private boolean isUserTheme;


    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Only classes from the theme package are allowed to create a new theme.
     */
    Theme() {
        colors = new Color[COLOR_COUNT];
        fonts  = new Font[FONT_COUNT];

        // The default value must be true, otherwise the ThemeReader won't be able
        // to set the theme's values.
        // Once loading is done however, the ThemeManager is expected to set
        // isUserTheme to its proper value.
        isUserTheme = true;
    }



    // - Use theme access ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the <code>isUserTheme</code> flag.
     * <p>
     * This method is only accessible to classes from within the theme package, as we don't
     * want other classes to play silly bugger with predefined themes.
     * </p>
     * @param b new value for the <code>isUserTheme</code> flag.
     */
    void setUserTheme(boolean b) {isUserTheme = b;}

    /**
     * Returns <code>true</code> if this is the user theme, <code>false</code> otherwise.
     * <p>
     * The user theme is the only one that can be modified - predefined themes are read only.
     * Should a class need to modify a predefined theme, it must first copy all of that theme's
     * attributes into the user one, then modify it.
     * </p>
     * @return <code>true</code> if this is the user theme, <code>false</code> otherwise.
     */
    public boolean isUserTheme() {return isUserTheme;}



    // - Theme values modification -------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the specified font.
     * @param  id                       identifier of the font to set.
     * @param  font                     new font for the specified id.
     * @throws IllegalStateException    if this is not the user theme.
     * @throws IllegalArgumentException if <code>id</code> is not a legal font id.
     */
    public synchronized void setFont(int id, Font font) {
        Font buffer; // Buffer for the old font in case this theme needs to trigger events.

        // Makes sure this theme is modifiable.
        if(!isUserTheme) {
            if(Debug.ON) Debug.trace("Tried to modify a non user theme font.");
            throw new IllegalStateException();
        }

        // Makes sure the font id is legal (only in Debug mode).
        if(Debug.ON && (id < 0 || id >= FONT_COUNT)) {
            Debug.trace("Illegal font id: " + id);
            throw new IllegalArgumentException();
        }

        // Triggers theme events if necessary.
        if(triggerEvents) {
            buffer    = fonts[id];
            fonts[id] = font;

            // Compares the old and new fonts to make sure the event must be triggered.
            if(font == null) {
                if(buffer == null)
                    return;
            }
            else if(font.equals(buffer))
                return;

            triggerFontEvent(id, buffer, font);
        }
        else
            fonts[id] = font;
    }

    /**
     * Sets the specified color.
     * @param  id                       identifier of the color to set.
     * @param  color                    new color for the specified id.
     * @throws IllegalStateException    if this is not the user theme.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color id.
     */
    public synchronized void setColor(int id, Color color) {
        Color buffer; // Buffer for the old color in case this theme needs to trigger events.

        // Makes sure this theme is modifiable.
        if(!isUserTheme) {
            if(Debug.ON) Debug.trace("Tried to modify a non user theme color.");
            throw new IllegalStateException();
        }

        // Makes sure the color id is legal (only in Debug mode).
        if(Debug.ON && (id < 0 || id >= COLOR_COUNT)) {
            Debug.trace("Illegal color id: " + id);
            throw new IllegalArgumentException();
        }

        // Triggers theme events if necessary.
        if(triggerEvents) {
            buffer     = colors[id];
            colors[id] = color;

            // Compares the old and new colors to make sure the event must be triggered.
            if(color == null) {
                if(buffer == null)
                    return;
            }
            else if(color.equals(buffer))
                return;

            triggerColorEvent(id, buffer, color);
        }
        else
            colors[id] = color;
    }



    // - Theme values retrieval ----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the requested font has a custom value with the current theme.
     * @param  id                   identifier of the font to check for.
     * @return                      <code>true</code> if the requested font has a custom value with the current theme, <code>false</code> otherwise.
     * @see     #getCustomFont(int)
     */
    public boolean hasCustomFont(int id) {return fonts[id] != null;}

    /**
     * Returns <code>true</code> if the requested color has a custom value with the current theme.
     * @param  id                   identifier of the color to check for.
     * @return                      <code>true</code> if the requested color has a custom value with the current theme, <code>false</code> otherwise.
     * @see     #getCustomColor(int)
     */
    public boolean hasCustomColor(int id) {return colors[id] != null;}

    /**
     * Returns the requested font.
     * <p>
     * Note that this method might return <code>null</code> if the requested font hasn't
     * been set by the current theme. In that case, UI components should be initialised
     * with their default system font.
     * </p>
     * @param  id                       identifier of the requested font.
     * @return                          the requested font.
     * @throws IllegalArgumentException if <code>id</code> is not a legal font id.
     * @see    #hasCustomFont(int)
     */
    public Font getCustomFont(int id) {
        // Makes sure the font id is legal.
        if(id < 0 || id >= FONT_COUNT) {
            if(Debug.ON) Debug.trace("Illegal font id: " + id);
            throw new IllegalArgumentException();
        }

        return fonts[id];
    }

    /**
     * Returns the requested color.
     * <p>
     * Note that this method might return <code>null</code> if the requested color hasn't
     * been set by the current theme. In that case, UI components should be initialised
     * with their default system color.
     * </p>
     * @param  id                       identifier of the requested color.
     * @return                          the requested color.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color id.
     * @see    #hasCustomColor(int)
     */
    public Color getCustomColor(int id) {
        // Makes sure the color id is legal.
        if(id < 0 || id >= COLOR_COUNT) {
            if(Debug.ON) Debug.trace("Illegal color id: " + id);
            throw new IllegalArgumentException();
        }

        return colors[id];
    }



    // - Theme listening -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the theme is the one triggering theme events.
     * @return <code>true</code> if the theme is the one triggering theme events, <code>false</code> otherwise.
     */
    boolean isTriggeringEvents() {return triggerEvents;}

    /**
     * Changes the theme's event triggering status.
     * @param flag whether or not the theme should trigger events.
     */
    void setTriggerEvents(boolean flag) {triggerEvents = flag;}

    /**
     * Registers the specified listener.
     * @param listener object that will receive theme events from now on.
     */
    public static synchronized void addThemeListener(ThemeListener listener) {listeners.put(listener, null);}

    /**
     * Removes the specified listener from the list of registered theme listeners.
     * @param listener theme listener to remove from the list.
     */
    public static synchronized void removeThemeListener(ThemeListener listener) {listeners.remove(listener);}

    /**
     * Notifies all listeners that a font has been modified.
     * @param fontId  identifier of the font that has been changed.
     * @param oldFont font value prior to the change.
     * @param newFont font value after the change.
     */
    private static synchronized void triggerFontEvent(int fontId, Font oldFont, Font newFont) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).fontChanged(fontId, oldFont, newFont);
    }

    /**
     * Notifies all listeners that a color has been modified.
     * @param colorId  identifier of the color that has been changed.
     * @param oldColor color value prior to the change.
     * @param newColor color value after the change.
     */
    private static synchronized void triggerColorEvent(int colorId, Color oldColor, Color newColor) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).colorChanged(colorId, oldColor, newColor);
    }
}
