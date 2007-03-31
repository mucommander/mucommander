package com.mucommander.ui.theme;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.RuntimeConstants;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.io.BackupInputStream;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.res.ResourceListReader;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;


/**
 * @author Nicolas Rinaudo
 */
public class ThemeManager {
    // - Class variables -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Path to the user defined theme file. */
    private static       String      userThemeFile;
    /** Default user defined theme file name. */
    private static final String      USER_THEME_FILE_NAME = "user_theme.xml";
    /** Path to the custom themes repository. */
    private static final String      CUSTOM_THEME_FOLDER  = "themes";
    /** List of all registered theme change listeners. */
    private static final WeakHashMap listeners            = new WeakHashMap();



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Whether or not the user theme was modified. */
    private static boolean wasUserThemeModified;
    /** Theme that is currently applied to muCommander. */
    private static Theme   currentTheme;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Prevents instanciations of the class.
     */
    private ThemeManager() {}

    /**
     * Loads the current theme.
     * <p>
     * This method goes through the following steps:
     * <ul>
     *  <li>Try to load the theme defined in the configuration.</li>
     *  <li>If that failed, try to load the default theme.</li>
     *  <li>If that failed, try to load the user theme if that hasn't been tried yet.</li>
     *  <li>If that failed, use an empty theme.</li>
     * </ul>
     * </p>
     */
    public static void loadCurrentTheme() {
        int     type;               // Current theme's type.
        String  name;               // Current theme's name.
        boolean wasUserThemeLoaded; // Whether we have tried loading the user theme or not.

        // Import legacy theme information if necessary.
        // This can happen, for example, if running muCommander 0.8 beta 3 or higher on muCommander 0.8 beta 2
        // or lower configuration.
        importLegacyTheme();

        // Loads the current theme type as defined in configuration.
        try {type = getThemeTypeFromLabel(ConfigurationManager.getVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.DEFAULT_THEME_TYPE));}
        // If some error occurs here (unknown theme type), use configuration defaults.
        catch(Exception e) {
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.DEFAULT_THEME_TYPE);
            type = getThemeTypeFromLabel(ConfigurationVariables.DEFAULT_THEME_TYPE);
        }

        // Loads the current theme name as defined in configuration.
        if(type != Theme.USER_THEME) {
            wasUserThemeLoaded = false;
            name               = ConfigurationManager.getVariable(ConfigurationVariables.THEME_NAME, ConfigurationVariables.DEFAULT_THEME_NAME);
	}
        else {
            name               = null;
            wasUserThemeLoaded = true;
        }

        // If the current theme couldn't be loaded, uses the default theme as defined in the configuration.
        currentTheme = null;
        try {currentTheme = getTheme(type, name);}
        catch(Exception e1) {
            type = getThemeTypeFromLabel(ConfigurationVariables.DEFAULT_THEME_TYPE);
            name = ConfigurationVariables.DEFAULT_THEME_NAME;

            if(type == Theme.USER_THEME)
                wasUserThemeLoaded = true;

            // If the default theme can be loaded, tries to load the user theme if we haven't done so yet.
            // If we have, or if it fails, defaults to an empty user theme.
            try {currentTheme = getTheme(type, name);}
            catch(Exception e2) {
                if(!wasUserThemeLoaded) {
                    try {currentTheme = getTheme(Theme.USER_THEME, null);}
                    catch(Exception e3) {}
                }
                if(currentTheme == null) {
                    currentTheme         = new Theme(Theme.USER_THEME, null);
                    wasUserThemeModified = true;
                }
            }
        }
    }



    // - Theme list retrieval ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Loads all predefined themes and stores them in the specified vector.
     * @param themes where to store the predefined themes.
     */
    private static void loadPredefinedThemes(Vector themes) {
        Iterator iterator; // Iterator on the predefined themes list.
        Theme    theme;    // Buffer for the current theme.

        // Loads the predefined theme list.
        try {iterator = new ResourceListReader().read(ResourceLoader.getResourceAsStream(RuntimeConstants.THEMES_FILE)).iterator();}
        catch(Exception e) {
            if(Debug.ON) {
                Debug.trace("Failed to load predefined themes list.");
                Debug.trace(e);
            }
            return;
        }

        // Iterates through the list and loads each theme.
        while(iterator.hasNext()) {
            try {themes.add(getTheme(Theme.PREDEFINED_THEME, getThemeName((String)iterator.next())));}
            catch(Exception e) {if(Debug.ON) Debug.trace("Predefined theme appears to be corrupt");}
        }
    }

    /**
     * Loads all custom themes and stores them in the specified vector.
     * @param themes where to store the custom themes.
     */
    private static void loadCustomThemes(Vector themes) {
        String[] customThemes; // All custom themes.

        // Loads all the custom themes.
        customThemes = getCustomThemesFolder().list(new FilenameFilter() {public boolean accept(File dir, String name) {return name.endsWith(".xml");}});
        for(int i = 0; i < customThemes.length; i++) {
            // If an exception is thrown here, do not consider this theme available.
            try {themes.add(getTheme(Theme.CUSTOM_THEME, getThemeName(customThemes[i])));}
            catch(Exception e) {
                if(Debug.ON) {
                    Debug.trace("Custom theme " + customThemes[i] + " appears to be corrupt.");
                    Debug.trace(e);
                }
            }
        }
    }

    /**
     * Returns an iterator on all available themes.
     * <p>
     * Note that this method guarantees that any theme it returns is indeed available. If one theme's XML file
     * has become unavailable or corrupt, it won't be listed.
     * </p>
     * @return an iterator on all available themes.
     */
    public static synchronized Iterator availableThemes() {
        Vector themes;

        themes = new Vector();

        // Tries to load the user theme. If it's corrupt, uses an empty user theme.
        try {themes.add(getTheme(Theme.USER_THEME, null));}
        catch(Exception e) {themes.add(new Theme(Theme.USER_THEME, null));}

        // Loads custom and predefined themes.
        loadPredefinedThemes(themes);
        loadCustomThemes(themes);

        return themes.iterator();
    }


    // - Theme paths access --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the path to the user's theme file.
     * <p>
     * This method cannot guarantee the file's existence, and it's up to the caller
     * to deal with the fact that the user might not actually have created a user theme.
     * </p>
     * <p>
     * This method's return value can be modified through {@link #setUserThemeFile(String)}.
     * If this wasn't called, the default path will be used: {@link #USER_THEME_FILE_NAME}
     * in the {@link com.mucommander.PlatformManager#getPreferencesFolder() preferences} folder.
     * </p>
     * @return the path to the user's theme file.
     * @see    #setUserThemeFile()
     * @see    #saveUserTheme()
     */
    public static String getUserThemeFile() {
        if(userThemeFile == null)
            return new File(PlatformManager.getPreferencesFolder(), USER_THEME_FILE_NAME).getAbsolutePath();
        return userThemeFile;
    }

    /**
     * Sets the path to the user theme file.
     * <p>
     * The specified file does not have to exist. If it does, however, it must be accessible.
     * </p>
     * @param  file                     path to the user theme file.
     * @throws IllegalArgumentException if <code>file</code> exists but is not accessible.
     * @see    #getUserThemeFile()
     * @see    #saveUserTheme()
     */
    public static void setUserThemeFile(String file) {
        File tempFile;

        // Makes sure the specified either doesn't exist or is accessible.
        tempFile = new File(file);
        if(tempFile.exists() && !(tempFile.isFile() || tempFile.canRead()))
            throw new IllegalArgumentException("Not a valid file: " + file);

        userThemeFile = file;
    }

    /**
     * Saves the user theme if necessary.
     */
    public static boolean saveUserTheme() {
        if(currentTheme.getType() == Theme.USER_THEME)
            return saveTheme(currentTheme);
        return true;
    }

    public static File getCustomThemesFolder() {
        File customFolder;

        customFolder = new File(PlatformManager.getPreferencesFolder(), CUSTOM_THEME_FOLDER);
        customFolder.mkdirs();

        return customFolder;
    }


    // - IO management -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the requested theme.
     * @param  type type of theme to retrieve.
     * @param  name name of the theme to retrieve.
     * @return the requested theme.
     */
    private static final Theme getTheme(int type, String name) throws Exception {
        Theme theme;

        switch(type) {
            // User defined theme.
        case Theme.USER_THEME:
            ThemeReader.read(new BackupInputStream(ThemeManager.getUserThemeFile()), theme = new Theme(Theme.USER_THEME, null));
            break;

            // Predefined themes.
        case Theme.PREDEFINED_THEME:
            ThemeReader.read(ResourceLoader.getResourceAsStream(RuntimeConstants.THEMES_PATH + "/" + name + ".xml"),
                             theme = new Theme(Theme.PREDEFINED_THEME, name));
            break;

            // Custom themes.
        case Theme.CUSTOM_THEME:
            ThemeReader.read(new FileInputStream(new File(ThemeManager.getCustomThemesFolder(), name + ".xml")),
                             theme = new Theme(Theme.CUSTOM_THEME, name));
            break;

            // Error.
        default:
            throw new IllegalArgumentException("Illegal theme type type: " + type);
        }

        return theme;
    }

    private static synchronized boolean saveTheme(Theme theme) {
        OutputStream out;

        out = null;
        switch(theme.getType()) {
        case Theme.PREDEFINED_THEME:
            if(Debug.ON) Debug.trace("Trying to save predefined theme: " + theme.getName());
            return false;

        case Theme.USER_THEME:
            try {
                if(wasUserThemeModified) {
                    ThemeWriter.write(theme, out = new BackupOutputStream(getUserThemeFile()));
                    out.close();
                    wasUserThemeModified = false;
                }
                return true;
            }
            catch(Exception e) {
                if(out != null) {
                    try {((BackupOutputStream)out).close(false);}
                    catch(Exception e2) {}
                }
                return false;
            }

        case Theme.CUSTOM_THEME:
            try {ThemeWriter.write(theme, out = new FileOutputStream(new File(getCustomThemesFolder(), theme.getName() + ".xml")));}
            catch(Exception e) {return false;}
            finally {
                if(out != null) {
                    try {out.close();}
                    catch(Exception e) {}
                }
            }
        }
        return true;
    }



    // - Current theme access ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sends events to all listeners with the current theme values.
     * <p>
     * This is meant to force a refresh of the whole UI, which can be usefull when
     * the current look and feel has changed, for example.
     * </p>
     */
    public static void forceRefresh() {
        for(int i = 0; i < Theme.FONT_COUNT; i++)
            triggerFontEvent(i, getCurrentFont(i));

        for(int i = 0; i < Theme.COLOR_COUNT; i++)
            triggerColorEvent(i, getCurrentColor(i));
    }

    /**
     * Sets the specified theme as the current theme in configuration.
     * @param theme theme to set as current.
     */
    private static void setConfigurationTheme(Theme theme) {
        // Sets configuration depending on the new theme's type.
        switch(theme.getType()) {
            // User defined theme.
        case Theme.USER_THEME:
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.THEME_USER);
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_NAME, null);
            break;

            // Predefined themes.
        case Theme.PREDEFINED_THEME:
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.THEME_PREDEFINED);
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_NAME, theme.getName());
            break;

            // Custom themes.
        case Theme.CUSTOM_THEME:
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.THEME_CUSTOM);
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_NAME, theme.getName());
            break;

            // Error.
        default:
            throw new IllegalStateException("Illegal theme type: " + theme.getType());
        }
    }

    /**
     * Changes the current theme.
     * <p>
     * This method will change the current theme and trigger all the proper events.
     * </p>
     * @param  theme                    theme to use as the current theme.
     * @throws IllegalArgumentException thrown if the specified theme could not be loaded.
     */
    public synchronized static void setCurrentTheme(Theme theme) {
        Theme oldTheme;

        // Makes sure we're not doing something useless.
        if(isCurrentTheme(theme))
            return;

        // Saves the user theme if necessary.
        saveUserTheme();

        // Updates muCommander's configuration.
        oldTheme = currentTheme;
	setConfigurationTheme(currentTheme = theme);

        // Triggers font events.
        for(int i = 0; i < Theme.FONT_COUNT; i++) {
            if(!oldTheme.getFont(i).equals(currentTheme.getFont(i)))
                triggerFontEvent(i, currentTheme.getFont(i));
        }

        // Triggers color events.
        for(int i = 0; i < Theme.COLOR_COUNT; i++) {
            if(!oldTheme.getColor(i).equals(currentTheme.getColor(i)))
               triggerColorEvent(i, currentTheme.getColor(i));
        }
    }

    public synchronized static Font getCurrentFont(int id) {return getFont(id, currentTheme);}

    private static Font getFont(int id, Theme theme) {
        Font font;

        // If the requested font is not defined in the current theme,
        // returns its default value.
        if((theme == null) || (font = theme.getFont(id, false)) == null)
            return getDefaultFont(id, theme);
        return font;
    }

    public synchronized static Color getCurrentColor(int id) {return getColor(id, currentTheme);}

    private static Color getColor(int id, Theme theme) {
        Color color;

        // If the requested color is not defined in the current theme,
        // returns its default value.
        if((theme == null) || (color = theme.getColor(id, false)) == null)
            return getDefaultColor(id, theme);
        return color;
    }

    /**
     * Sets the specified font for the current theme.
     * <p>
     * This is equivalent to calling <code>setCurrentFont(id, font, true)</code>.
     * </p>
     * @see        #setCurrentFont(int,Font,boolean)
     * @param id   identifier of the font to set.
     * @param font new font value.
     */
    public synchronized static boolean setCurrentFont(int id, Font font) {return setCurrentFont(id, font, true);}

    /**
     * Sets the specified color for the current theme.
     * <p>
     * This is equivalent to calling <code>setCurrentColor(id, color, true)</code>.
     * </p>
     * @see         #setCurrentColor(int,Color,boolean)
     * @param id    identifier of the color to set.
     * @param color new color value.
     */
    public synchronized static boolean setCurrentColor(int id, Color color) {return setCurrentColor(id, color, true);}

    /**
     * Copies the current theme over the user theme.
     */
    private static void overwriteUserTheme() {
        currentTheme.setType(Theme.USER_THEME);
        setConfigurationTheme(currentTheme);
        wasUserThemeModified = true;
    }

    /**
     * Checks whether the setting the specified font would actually change the current theme.
     * @param  fontId identifier of the font to set.
     * @param  font   value for the font to set.
     * @return        <code>true</code> if applying the font would change the current theme, <code>false</code> otherwise.
     */
    private static boolean needsUpdate(int fontId, Font font) {
        Font oldFont;

	// Retrieves the old font to check whether its different
	// from the new one.
	oldFont = currentTheme.getFont(fontId, false);

	// Trying to set a default font over a non-default one.
	if(font == null)
	    return oldFont != null;

	// Trying to set a non default over a default one.
	if(oldFont == null)
	    return !getCurrentFont(fontId).equals(font);

	// Checks whether both fonts are different.
	return !oldFont.equals(font);
    }

    /**
     * Checks whether the setting the specified color would actually change the current theme.
     * @param  colorId identifier of the color to set.
     * @param  color   value for the color to set.
     * @return         <code>true</code> if applying the color would change the current theme, <code>false</code> otherwise.
     */
    private static boolean needsUpdate(int colorId, Color color) {
        Color oldColor;

	// Retrieves the old color to check whether its different
	// from the new one.
	oldColor = currentTheme.getColor(colorId, false);

	// Trying to set a default color over a non-default one.
	if(color == null)
	    return oldColor != null;

	// Trying to set a non default color over a default one.
	if(oldColor == null)
	    return !getCurrentColor(colorId).equals(color);

	// Checks whether both colors are different.
	return !oldColor.equals(color);
    }

    /**
     * Checks whether setting the specified font would require overwriting of the user theme.
     * @param  fontId identifier of the font to set.
     * @param  font   value for the specified font.
     * @return        <code>true</code> if applying the specified font will overwrite the user theme,
     *                <code>false</code> otherwise.
     */
    public synchronized static boolean willOverwriteUserTheme(int fontId, Font font) {
        if(needsUpdate(fontId, font))
            return currentTheme.getType() != Theme.USER_THEME;
        return false;
    }

    /**
     * Checks whether setting the specified color would require overwriting of the user theme.
     * @param  colorId identifier of the color to set.
     * @param  color   value for the specified color.
     * @return         <code>true</code> if applying the specified color will overwrite the user theme,
     *                 <code>false</code> otherwise.
     */
    public synchronized static boolean willOverwriteUserTheme(int colorId, Color color) {
        if(needsUpdate(colorId, color))
            return currentTheme.getType() != Theme.USER_THEME;
        return false;
    }

    /**
     * Updates the current theme with the specified font.
     * <p>
     * This method might require to overwrite the user theme: custom and predefined themes are
     * read only. In order to modify them, the ThemeManager must overwrite the user theme with
     * the current theme and then set the font.<br/>
     * Such a behaviour might not be desirable. In this case, setting <code>overwriteUserTheme</code>
     * to <code>false</code> will abort the modification before overwriting the user theme.
     * </p>
     * @param  id                 identifier of the font to set.
     * @param  font               font to set.
     * @param  overwriteUserTheme whether or not to overwrite the user theme if necessary.
     * @return                    <code>true</code> if the current theme was modified, <code>false</code> otherwise.
     */
    public synchronized static boolean setCurrentFont(int id, Font font, boolean overwriteUserTheme) {
        // If this modification doesn't actually change the current theme,
        // do nothing.
        if(!needsUpdate(id, font))
            return false;

        // If we need to change the user theme in order to perform the modification,
        // but we're not allowed, abort.
        if(currentTheme.getType() == Theme.USER_THEME)
            wasUserThemeModified = true;
        else if(overwriteUserTheme)
            overwriteUserTheme();
        else
            return false;

        currentTheme.setFont(id, font);
        triggerFontEvent(id, font);

        return true;
    }

    /**
     * Updates the current theme with the specified color.
     * <p>
     * This method might require to overwrite the user theme: custom and predefined themes are
     * read only. In order to modify them, the ThemeManager must overwrite the user theme with
     * the current theme and then set the color.<br/>
     * Such a behaviour might not be desirable. In this case, setting <code>overwriteUserTheme</code>
     * to <code>false</code> will abort the modification before overwriting the user theme.
     * </p>
     * @param  id                 identifier of the color to set.
     * @param  color              color to set.
     * @param  overwriteUserTheme whether or not to overwrite the user theme if necessary.
     * @return                    <code>true</code> if the current theme was modified, <code>false</code> otherwise.     
     */
    public synchronized static boolean setCurrentColor(int id, Color color, boolean overwriteUserTheme) {
        // If this modification doesn't actually change the current theme,
        // do nothing.
        if(!needsUpdate(id, color))
            return false;
        if(currentTheme.getType() == Theme.USER_THEME)
            wasUserThemeModified = true;
        else if(overwriteUserTheme)
            overwriteUserTheme();
        else
            return false;

        currentTheme.setColor(id, color);
        triggerColorEvent(id, color);

        return true;
    }

    /**
     * Returns <code>true</code> if the specified theme is the current one.
     * @param theme theme to check.
     * @return <code>true</code> if the specified theme is the current one, <code>false</code> otherwise.
     */
    public static boolean isCurrentTheme(Theme theme) {
        if(theme.getType() != currentTheme.getType())
            return false;
        if(currentTheme.getType() == Theme.USER_THEME)
            return true;
        return theme.getName().equals(currentTheme.getName());
    }


    // - Theme listening -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Adds the specified object to the list of registered theme listeners.
     * @param listener new theme listener.
     */
    public static void addThemeListener(ThemeListener listener) {listeners.put(listener, null);}

    /**
     * Removes the specified object from the list of registered theme listeners.
     * @param listener theme listener to remove.
     */
    public static void removeThemeListener(ThemeListener listener) {listeners.remove(listener);}

    /**
     * Notifies all theme listeners of the specified font's new value.
     * @param id   identifier of the font that has changed.
     * @param font font's new value.
     */
    private static void triggerFontEvent(int id, Font font) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).fontChanged(id, font);
    }

    /**
     * Notifies all theme listeners of the specified color's new value.
     * @param id    identifier of the color that has changed.
     * @param color color's new value.
     */
    private static void triggerColorEvent(int id, Color color) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).colorChanged(id, color);
    }



    // - Legacy theme --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private static void importLegacyTheme() {
        // Legacy theme information.
        String backgroundColor, fileColor, hiddenColor, folderColor, archiveColor, symlinkColor,
               markedColor, selectedColor, selectionColor, unfocusedColor, shellBackgroundColor,
               shellSelectionColor, shellTextColor, fontSize, fontFamily, fontStyle;
        Theme legacyTheme; // Data for the new user theme.

        // Gathers legacy theme information.
        backgroundColor      = ConfigurationManager.getVariable(LegacyTheme.BACKGROUND_COLOR);
        fileColor            = ConfigurationManager.getVariable(LegacyTheme.PLAIN_FILE_COLOR);
        hiddenColor          = ConfigurationManager.getVariable(LegacyTheme.HIDDEN_FILE_COLOR);
        folderColor          = ConfigurationManager.getVariable(LegacyTheme.FOLDER_COLOR);
        archiveColor         = ConfigurationManager.getVariable(LegacyTheme.ARCHIVE_FILE_COLOR);
        symlinkColor         = ConfigurationManager.getVariable(LegacyTheme.SYMLINK_COLOR);
        markedColor          = ConfigurationManager.getVariable(LegacyTheme.MARKED_FILE_COLOR);
        selectedColor        = ConfigurationManager.getVariable(LegacyTheme.SELECTED_FILE_COLOR);
        selectionColor       = ConfigurationManager.getVariable(LegacyTheme.SELECTION_BACKGROUND_COLOR);
        unfocusedColor       = ConfigurationManager.getVariable(LegacyTheme.OUT_OF_FOCUS_COLOR);
        shellBackgroundColor = ConfigurationManager.getVariable(LegacyTheme.SHELL_BACKGROUND_COLOR);
        shellSelectionColor  = ConfigurationManager.getVariable(LegacyTheme.SHELL_SELECTION_COLOR);
        shellTextColor       = ConfigurationManager.getVariable(LegacyTheme.SHELL_TEXT_COLOR);
        fontFamily           = ConfigurationManager.getVariable(LegacyTheme.FONT_FAMILY);
        fontSize             = ConfigurationManager.getVariable(LegacyTheme.FONT_SIZE);
        fontStyle            = ConfigurationManager.getVariable(LegacyTheme.FONT_STYLE);

        // Clears the configuration of the legacy theme data.
        // This is not strictly necessary, but why waste perfectly good memory and
        // hard drive space with values that are never going to be used?
        ConfigurationManager.setVariable(LegacyTheme.BACKGROUND_COLOR,           null);
        ConfigurationManager.setVariable(LegacyTheme.PLAIN_FILE_COLOR,           null);
        ConfigurationManager.setVariable(LegacyTheme.HIDDEN_FILE_COLOR,          null);
        ConfigurationManager.setVariable(LegacyTheme.FOLDER_COLOR,               null);
        ConfigurationManager.setVariable(LegacyTheme.ARCHIVE_FILE_COLOR,         null);
        ConfigurationManager.setVariable(LegacyTheme.SYMLINK_COLOR,              null);
        ConfigurationManager.setVariable(LegacyTheme.MARKED_FILE_COLOR,          null);
        ConfigurationManager.setVariable(LegacyTheme.SELECTED_FILE_COLOR,        null);
        ConfigurationManager.setVariable(LegacyTheme.SELECTION_BACKGROUND_COLOR, null);
        ConfigurationManager.setVariable(LegacyTheme.OUT_OF_FOCUS_COLOR,         null);
        ConfigurationManager.setVariable(LegacyTheme.SHELL_BACKGROUND_COLOR,     null);
        ConfigurationManager.setVariable(LegacyTheme.SHELL_SELECTION_COLOR,      null);
        ConfigurationManager.setVariable(LegacyTheme.SHELL_TEXT_COLOR,           null);
        ConfigurationManager.setVariable(LegacyTheme.FONT_FAMILY,                null);
        ConfigurationManager.setVariable(LegacyTheme.FONT_SIZE,                  null);
        ConfigurationManager.setVariable(LegacyTheme.FONT_STYLE,                 null);

        // If no legacy theme information could be found, aborts import.
        if(backgroundColor == null && fileColor == null && hiddenColor == null && folderColor == null && archiveColor == null &&
           symlinkColor == null && markedColor == null && selectedColor == null && selectionColor == null && unfocusedColor == null &&
           shellBackgroundColor == null && shellSelectionColor == null && shellTextColor == null && shellTextColor == null &&
           shellTextColor == null && fontFamily == null && fontSize == null && fontStyle == null)
            return;
    
        // Creates theme data using whatever values were found in the user configuration.
        // Empty values are set to their 'old fashioned' defaults.
        Color color;
        Font  font;

        legacyTheme = new Theme();

        // File background color.
        if(backgroundColor == null)
            backgroundColor = LegacyTheme.DEFAULT_BACKGROUND_COLOR;
        legacyTheme.setColor(Theme.FILE_BACKGROUND_COLOR, color = new Color(Integer.parseInt(backgroundColor, 16)));
        legacyTheme.setColor(Theme.HIDDEN_FILE_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.MARKED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.FOLDER_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SYMLINK_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.ARCHIVE_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.FILE_TABLE_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.FOLDER_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.ARCHIVE_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SYMLINK_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.MARKED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.FILE_UNFOCUSED_BACKGROUND_COLOR, color);

        // Selected file background color.
        if(selectionColor == null)
            selectionColor = LegacyTheme.DEFAULT_SELECTION_BACKGROUND_COLOR;
        legacyTheme.setColor(Theme.FILE_SELECTED_BACKGROUND_COLOR, color = new Color(Integer.parseInt(selectionColor, 16)));
        legacyTheme.setColor(Theme.HIDDEN_FILE_SELECTED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.MARKED_SELECTED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.FOLDER_SELECTED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SYMLINK_SELECTED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.ARCHIVE_SELECTED_BACKGROUND_COLOR, color);

        // Out of focus file background color.
        if(unfocusedColor == null)
            unfocusedColor = LegacyTheme.DEFAULT_OUT_OF_FOCUS_COLOR;
        legacyTheme.setColor(Theme.FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color = new Color(Integer.parseInt(unfocusedColor, 16)));
        legacyTheme.setColor(Theme.FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);

        // Hidden files color.
        if(hiddenColor == null)
            hiddenColor = LegacyTheme.DEFAULT_HIDDEN_FILE_COLOR;
        legacyTheme.setColor(Theme.HIDDEN_FILE_FOREGROUND_COLOR, color = new Color(Integer.parseInt(hiddenColor, 16)));
        legacyTheme.setColor(Theme.HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR, color);

        // Folder color.
        if(folderColor == null)
            folderColor = LegacyTheme.DEFAULT_FOLDER_COLOR;
        legacyTheme.setColor(Theme.FOLDER_FOREGROUND_COLOR, color = new Color(Integer.parseInt(folderColor, 16)));
        legacyTheme.setColor(Theme.FOLDER_UNFOCUSED_FOREGROUND_COLOR, color);

        // Archives color.
        if(archiveColor == null)
            archiveColor = LegacyTheme.DEFAULT_ARCHIVE_FILE_COLOR;
        legacyTheme.setColor(Theme.ARCHIVE_FOREGROUND_COLOR, color = new Color(Integer.parseInt(archiveColor, 16)));
        legacyTheme.setColor(Theme.ARCHIVE_UNFOCUSED_FOREGROUND_COLOR, color);

        // Symbolic links color.
        if(symlinkColor == null)
            symlinkColor = LegacyTheme.DEFAULT_SYMLINK_COLOR;
        legacyTheme.setColor(Theme.SYMLINK_FOREGROUND_COLOR, color = new Color(Integer.parseInt(symlinkColor, 16)));
        legacyTheme.setColor(Theme.SYMLINK_UNFOCUSED_FOREGROUND_COLOR, color);

        // Plain file color.
        if(fileColor == null)
            fileColor = LegacyTheme.DEFAULT_PLAIN_FILE_COLOR;
        legacyTheme.setColor(Theme.FILE_FOREGROUND_COLOR, color = new Color(Integer.parseInt(fileColor, 16)));
        legacyTheme.setColor(Theme.FILE_UNFOCUSED_FOREGROUND_COLOR, color);

        // Marked file color.
        if(markedColor == null)
            markedColor = LegacyTheme.DEFAULT_MARKED_FILE_COLOR;
        legacyTheme.setColor(Theme.MARKED_FOREGROUND_COLOR, color = new Color(Integer.parseInt(markedColor, 16)));
        legacyTheme.setColor(Theme.MARKED_UNFOCUSED_FOREGROUND_COLOR, color);

        // Selected file color.
        if(selectedColor == null)
            selectedColor = LegacyTheme.DEFAULT_SELECTED_FILE_COLOR;
        legacyTheme.setColor(Theme.FILE_SELECTED_FOREGROUND_COLOR, color = new Color(Integer.parseInt(selectedColor, 16)));
        legacyTheme.setColor(Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.FOLDER_SELECTED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SYMLINK_SELECTED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.MARKED_SELECTED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);

        // Shell background color.
        if(shellBackgroundColor == null)
            shellBackgroundColor = LegacyTheme.DEFAULT_SHELL_BACKGROUND_COLOR;
        legacyTheme.setColor(Theme.SHELL_BACKGROUND_COLOR, new Color(Integer.parseInt(shellBackgroundColor, 16)));

        // Shell text color.
        if(shellTextColor == null)
            shellTextColor = LegacyTheme.DEFAULT_SHELL_TEXT_COLOR;
        legacyTheme.setColor(Theme.SHELL_FOREGROUND_COLOR, color = new Color(Integer.parseInt(shellTextColor, 16)));
        legacyTheme.setColor(Theme.SHELL_SELECTED_FOREGROUND_COLOR, color);

        // Shell selection background color.
        if(shellSelectionColor == null)
            shellSelectionColor = LegacyTheme.DEFAULT_SHELL_SELECTION_COLOR;
        legacyTheme.setColor(Theme.SHELL_SELECTED_BACKGROUND_COLOR, new Color(Integer.parseInt(shellSelectionColor, 16)));

        // File table font.
        legacyTheme.setFont(Theme.FILE_TABLE_FONT, font = getLegacyFont(fontFamily, fontStyle, fontSize));

        // Sets colors that were not customisable in older versions of muCommander, using
        // l&f default where necessary.

        // File table border.
        legacyTheme.setColor(Theme.FILE_TABLE_BORDER_COLOR, new Color(64, 64, 64));

        // File editor / viewer colors.
        legacyTheme.setColor(Theme.EDITOR_BACKGROUND_COLOR, Color.WHITE);
        legacyTheme.setColor(Theme.EDITOR_FOREGROUND_COLOR, getTextAreaColor());
        legacyTheme.setColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR, getTextAreaSelectionBackgroundColor());
        legacyTheme.setColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR, getTextAreaSelectionColor());
        legacyTheme.setFont(Theme.EDITOR_FONT, getTextAreaFont());

        // Location bar and shell history (both use text field defaults).
        legacyTheme.setColor(Theme.LOCATION_BAR_PROGRESS_COLOR, new Color(0, 255, 255, 64));
	color = getTextFieldBackgroundColor();
        legacyTheme.setColor(Theme.LOCATION_BAR_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SHELL_HISTORY_BACKGROUND_COLOR, color);
	color = getTextFieldColor();
        legacyTheme.setColor(Theme.LOCATION_BAR_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SHELL_HISTORY_FOREGROUND_COLOR, color);
	color = getTextFieldSelectionBackgroundColor();
        legacyTheme.setColor(Theme.LOCATION_BAR_SELECTED_BACKGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, color);
	color = getTextFieldSelectionColor();
        legacyTheme.setColor(Theme.LOCATION_BAR_SELECTED_FOREGROUND_COLOR, color);
        legacyTheme.setColor(Theme.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, color);

	font = getTextFieldFont();
        legacyTheme.setFont(Theme.LOCATION_BAR_FONT, font);
        legacyTheme.setFont(Theme.SHELL_HISTORY_FONT, font);

        // If the user theme exists, saves the legacy theme as backup.
        if(new File(getUserThemeFile()).exists()) {
            legacyTheme.setType(Theme.CUSTOM_THEME);
            legacyTheme.setName("BackupTheme");
        }
        // Otherwise, creates a new user theme using the legacy data.
        else {
            legacyTheme.setType(Theme.USER_THEME);
            setConfigurationTheme(legacyTheme);
        }

        saveTheme(legacyTheme);
    }

    /**
     * Creates a font using data used by older versions of muCommander.
     * <p>
     * If any of the parameters is <code>null</code>, default values are used.
     * </p>
     * @param  family font family.
     * @param  size   font size.
     * @param  style  font style (can be any of <code>"bold"</code>, <code>"italic</code>,
     *                <code>"bold_italic"</code> or <code>"plain"</code>).
     * @return        a font that fits the specified parameters.
     */
    private static Font getLegacyFont(String family, String style, String size) {
        Font defaultFont;
        int  fontSize;
        int  fontStyle;

        // Retrieves the default font.
        if((defaultFont = UIManager.getDefaults().getFont("Label.font")) == null)
            defaultFont = new JLabel().getFont();

        // Gets the font family.
        if(family == null)
            family = defaultFont.getFamily();

        // Gets the font size.
        if(size == null)
            fontSize = defaultFont.getSize();
        else
            fontSize = Integer.parseInt(size);

        // Gets the font style.
        if(style == null)
            fontStyle = defaultFont.getStyle();
        else {
            if(style.equals("bold"))
                fontStyle = Font.BOLD;
            else if(style.equals("italic"))
                fontStyle = Font.ITALIC;
            else if(style.equals("bold_italic"))
                fontStyle = Font.BOLD | Font.ITALIC;
            else
                fontStyle = Font.PLAIN;
        }

        // Returns the legacy font.
        return new Font(family, fontStyle, fontSize);
    }



    // - Helper methods ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns a valid type identifier from the specified configuration type definition.
     * @param  label label of the theme type as defined in {@link com.mucommander.conf.ConfigurationVariables}.
     * @return       a valid theme type identifier.
     */
    private static int getThemeTypeFromLabel(String label) {
        if(label.equals(ConfigurationVariables.THEME_USER))
            return Theme.USER_THEME;
        else if(label.equals(ConfigurationVariables.THEME_PREDEFINED))
            return Theme.PREDEFINED_THEME;
        else if(label.equals(ConfigurationVariables.THEME_CUSTOM))
            return Theme.CUSTOM_THEME;
        throw new IllegalStateException("Unknown theme type: " + label);
    }

    /**
     * Returns the current look and feel's text area font.
     * @return the current look and feel's text area font.
     */
    private static Font getTextAreaFont() {
	Font font;

        if((font = UIManager.getDefaults().getFont("TextArea.font")) == null)
            return new JTextArea().getFont();
	return font;
    }

    /**
     * Returns the current look and feel's text field font.
     * @return the current look and feel's text field font.
     */
    private static Font getTextFieldFont() {
	Font font;

        if((font = UIManager.getDefaults().getFont("TextField.font")) == null)
            return new JTextField().getFont();
	return font;
    }

    /**
     * Returns the current look and feel's label font.
     * @return the current look and feel's label font.
     */
    private static Font getLabelFont() {
	Font font;

        if((font = UIManager.getDefaults().getFont("Label.font")) == null)
            return new JLabel().getFont();
	return font;
    }

    private static Color getTextAreaColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("TextArea.foreground")) == null)
            return new JTextArea().getForeground();
	return color;
    }

    private static Color getTextAreaBackgroundColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("TextArea.background")) == null)
            return new JTextArea().getBackground();
	return color;
    }

    private static Color getTextAreaSelectionColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("TextArea.selectionForeground")) == null)
            return new JTextArea().getSelectionColor();
	return color;
    }

    private static Color getTextAreaSelectionBackgroundColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("TextArea.selectionBackground")) == null)
            return new JTextArea().getSelectedTextColor();
	return color;
    }

    private static Color getTextFieldColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("TextField.foreground")) == null)
            return new JTextField().getForeground();
	return color;
    }

    private static Color getTextFieldBackgroundColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("TextField.background")) == null)
            return new JTextField().getBackground();
	return color;
    }

    private static Color getTextFieldSelectionColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("TextField.selectionForeground")) == null)
            return new JTextField().getSelectionColor();
	return color;
    }

    private static Color getTextFieldSelectionBackgroundColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("TextField.selectionBackground")) == null)
            return new JTextField().getSelectedTextColor();
	return color;
    }

    private static Color getTableColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("Table.foreground")) == null)
            return new JTable().getForeground();
	return color;
    }

    private static Color getTableBackgroundColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("Table.background")) == null)
            return new JTable().getBackground();
	return color;
    }

    private static Color getTableSelectionColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("Table.selectionForeground")) == null)
            return new JTable().getSelectionForeground();
	return color;
    }

    private static Color getTableSelectionBackgroundColor() {
	Color color;

        if((color = UIManager.getDefaults().getColor("Table.selectionBackground")) == null)
            return new JTable().getSelectionBackground();
	return color;
    }

    private static Font getTableFont() {
	Font font;

        if((font = UIManager.getDefaults().getFont("Table.font")) == null)
            return new JTable().getFont();
	return font;
    }

    static final Color getDefaultColor(int id, Theme theme) {
        switch(id) {
            // File table background colors.
        case Theme.FILE_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.FOLDER_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.ARCHIVE_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.SYMLINK_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.MARKED_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.FILE_BACKGROUND_COLOR:
        case Theme.HIDDEN_FILE_BACKGROUND_COLOR:
        case Theme.FOLDER_BACKGROUND_COLOR:
        case Theme.ARCHIVE_BACKGROUND_COLOR:
        case Theme.SYMLINK_BACKGROUND_COLOR:
        case Theme.MARKED_BACKGROUND_COLOR:
            return getColor(Theme.FILE_TABLE_BACKGROUND_COLOR, theme);

        case Theme.FILE_TABLE_BACKGROUND_COLOR:
	    return getTableBackgroundColor();

            // File table foreground colors (everything except marked
            // defaults to the l&f specific table foreground color).
        case Theme.HIDDEN_FILE_FOREGROUND_COLOR:
        case Theme.FOLDER_FOREGROUND_COLOR:
        case Theme.ARCHIVE_FOREGROUND_COLOR:
        case Theme.SYMLINK_FOREGROUND_COLOR:
        case Theme.FILE_UNFOCUSED_FOREGROUND_COLOR:
        case Theme.HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR:
        case Theme.FOLDER_UNFOCUSED_FOREGROUND_COLOR:
        case Theme.ARCHIVE_UNFOCUSED_FOREGROUND_COLOR:
        case Theme.SYMLINK_UNFOCUSED_FOREGROUND_COLOR:
            return getColor(Theme.FILE_FOREGROUND_COLOR, theme);

        case Theme.FILE_FOREGROUND_COLOR:
	    return getTableColor();

            // Marked files foreground colors (they have to be different
            // of the standard file foreground colors).
        case Theme.MARKED_FOREGROUND_COLOR:
        case Theme.MARKED_UNFOCUSED_FOREGROUND_COLOR:
        case Theme.MARKED_SELECTED_FOREGROUND_COLOR:
        case Theme.MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            return Color.RED;

            // Text areas default foreground colors.
        case Theme.SHELL_FOREGROUND_COLOR:
        case Theme.EDITOR_FOREGROUND_COLOR:
            return getTextAreaColor();

            // Text areas default background colors.
        case Theme.SHELL_BACKGROUND_COLOR:
        case Theme.EDITOR_BACKGROUND_COLOR:
            return getTextAreaBackgroundColor();

            // Text fields default foreground colors.
        case Theme.SHELL_HISTORY_FOREGROUND_COLOR:
        case Theme.LOCATION_BAR_FOREGROUND_COLOR:
        case Theme.STATUS_BAR_FOREGROUND_COLOR:
            return getTextFieldColor();

            // Text fields default background colors.
        case Theme.LOCATION_BAR_BACKGROUND_COLOR:
        case Theme.SHELL_HISTORY_BACKGROUND_COLOR:
            return getTextFieldBackgroundColor();

            // The location bar progress color is a bit of a special case,
            // as it requires alpha transparency.
        case Theme.LOCATION_BAR_PROGRESS_COLOR:
	    Color color;

	    color = getTextFieldSelectionBackgroundColor();
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), 64);

            // Selected table background colors.
        case Theme.HIDDEN_FILE_SELECTED_BACKGROUND_COLOR:
        case Theme.FOLDER_SELECTED_BACKGROUND_COLOR:
        case Theme.ARCHIVE_SELECTED_BACKGROUND_COLOR:
        case Theme.SYMLINK_SELECTED_BACKGROUND_COLOR:
        case Theme.MARKED_SELECTED_BACKGROUND_COLOR:
            return getColor(Theme.FILE_SELECTED_BACKGROUND_COLOR, theme);

        case Theme.FILE_SELECTED_BACKGROUND_COLOR:
	    return getTableSelectionBackgroundColor();

            // Gray colors.
        case Theme.FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case Theme.FILE_TABLE_BORDER_COLOR:
            return Color.GRAY;

            // Foreground color for selected elements in the file table.
        case Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR:
        case Theme.FOLDER_SELECTED_FOREGROUND_COLOR:
        case Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR:
        case Theme.SYMLINK_SELECTED_FOREGROUND_COLOR:
        case Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case Theme.FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case Theme.ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case Theme.SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case Theme.FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            return getColor(Theme.FILE_SELECTED_FOREGROUND_COLOR, theme);

        case Theme.FILE_SELECTED_FOREGROUND_COLOR:
	    return getTableSelectionColor();

            // Foreground color for selected text area elements.
        case Theme.SHELL_SELECTED_FOREGROUND_COLOR:
        case Theme.EDITOR_SELECTED_FOREGROUND_COLOR:
            return getTextAreaSelectionColor();

            // Background color for selected text area elements.
        case Theme.SHELL_SELECTED_BACKGROUND_COLOR:
        case Theme.EDITOR_SELECTED_BACKGROUND_COLOR:
            return getTextAreaSelectionBackgroundColor();

            // Foreground color for selected text fields elements.
        case Theme.LOCATION_BAR_SELECTED_FOREGROUND_COLOR:
        case Theme.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR:
            return getTextFieldSelectionColor();

            // Background color for selected text fields elements.
        case Theme.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR:
        case Theme.LOCATION_BAR_SELECTED_BACKGROUND_COLOR:
            return getTextFieldSelectionBackgroundColor();

            // Status bar defaults.
        case Theme.STATUS_BAR_BACKGROUND_COLOR:
            return new Color(0xD5D5D5);

        case Theme.STATUS_BAR_BORDER_COLOR:
            return new Color(0x7A7A7A);

        case Theme.STATUS_BAR_OK_COLOR:
            return new Color(0x70EC2B);

        case Theme.STATUS_BAR_WARNING_COLOR:
            return new Color(0xFF7F00);

        case Theme.STATUS_BAR_CRITICAL_COLOR:
            return new Color(0xFF0000);
        }
        throw new IllegalArgumentException("Illegal color identifier: " + id);
    }

    /**
     * Returns the default value for the specified font.
     * @param  id identifier of the font whose default value should be retrieved.
     * @return    the default value for the specified font.
     */
    static final Font getDefaultFont(int id, Theme theme) {
	switch(id) {
            // Table font.
        case Theme.FILE_TABLE_FONT:
            return getTableFont();

	    // Text Area font.
        case Theme.EDITOR_FONT:
        case Theme.SHELL_FONT:
	    return getTextAreaFont();

	    // Text Field font.
        case Theme.LOCATION_BAR_FONT:
        case Theme.SHELL_HISTORY_FONT:
        case Theme.STATUS_BAR_FONT:
	    return getTextFieldFont();

        }
        throw new IllegalArgumentException("Illegal font identifier: " + id);
    }

    private static String getThemeName(String path) {return path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));}
}
