package com.mucommander.ui.theme;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.res.ResourceListReader;
import com.mucommander.text.Translator;
import com.mucommander.RuntimeConstants;

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
    // - Theme types ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Describes the user defined theme. */
    public static final int USER_THEME                         = 0;
    /** Describes predefined muCommander themes. */
    public static final int PREDEFINED_THEME                   = 1;
    /** Describes custom muCommander themes. */
    public static final int CUSTOM_THEME                       = 2;



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
    /** Data of the current theme. */
    private static ThemeData currentData;
    /** Name of the current theme. */
    private static String    currentName;
    /** Type of the current theme. */
    private static int       currentType;
    /** Whether or not the user theme was modified. */
    private static boolean   wasUserThemeModified;
    /** Contains a list of all the available themes. */
    private static Vector    themes;
    /** User defined theme. */
    private static Theme     userTheme;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Prevents instanciations of the class.
     */
    private ThemeManager() {}

    /**
     * Loads the predefined themes.
     * <p>
     * This method will gather all the themes defined in the JAR file and try to load them.
     * If any theme fails to be loaded, it will not be added to the list of available themes.
     * </p>
     */
    private static void loadPredefinedThemes() {
        Iterator iterator; // Iterator on the predefined themes list.
        String   path;     // Path of the current predefined theme.
        Theme    theme;    // Buffer for the current theme.

        // Loads the predefined theme list.
        try {iterator = new ResourceListReader().read(ThemeManager.class.getResourceAsStream(RuntimeConstants.THEMES_FILE)).iterator();}
        catch(Exception e) {
            if(Debug.ON) {
                Debug.trace("Failed to load predefined themes list.");
                Debug.trace(e);
            }
            return;
        }

        // Iterates through the list and loads each theme.
        while(iterator.hasNext()) {
            path = null;
            try {
                themes.add(theme = new Theme(PREDEFINED_THEME, getThemeName(path = (String)iterator.next()), path));

                // If we've just loaded the current theme, grab its data.
                if(currentType == PREDEFINED_THEME && theme.getName().equals(currentName))
                    currentData = theme.getThemeData();
            }
            catch(Exception e) {if(Debug.ON) Debug.trace("Predefined theme " + path + " appears to be corrupt");}
        }
    }

    /**
     * Loads the custom themes.
     * <p>
     * This method will load all the theme files that can be found in the user's custom theme folder.
     * If any fails to load, it won't be added to the list of available themes.
     * </p>
     */
    private static void loadCustomThemes() {
        String[] customThemes; // All custom themes.
        Theme    theme;        // Buffer for the current theme.

        // Loads all the custom themes.
        customThemes = getCustomThemesFolder().list(new FilenameFilter() {public boolean accept(File dir, String name) {return name.endsWith(".xml");}});
        for(int i = 0; i < customThemes.length; i++) {
            // If an exception is thrown here, do not consider this theme available.
            try {
                themes.add(theme = new Theme(CUSTOM_THEME, getThemeName(customThemes[i]), customThemes[i]));

                // If we've just loaded the current theme, grab its data.
                if(currentType == CUSTOM_THEME && theme.getName().equals(currentName))
                    currentData = theme.getThemeData();
            }
            catch(Exception e) {if(Debug.ON) Debug.trace("Custom theme " + customThemes[i] + " appears to be corrupt.");}
        }
    }

    /**
     * Loads the user theme.
     * <p>
     * If no user theme is defined, this method will create one. If any legacy theme data is found, it will be used.
     * Otherwise, creates an empty used theme.
     * </p>
     */
    private static void loadUserTheme() {
        ThemeData legacyData;

        // Retrieves eventual legacy theme data.
        legacyData = getLegacyThemeData();

        // Loads the user theme. If the file exists and legacy theme data was found,
        // we need to back it up.
        if(new File(getUserThemeFile()).exists()) {

            // Loads the user theme.
            try {userTheme = new Theme(USER_THEME, Translator.get("user_theme"), null);}
            catch(Exception e) {if(Debug.ON) Debug.trace("Impossible error: user theme loading threw an exception.");}

            // If we have some legacy data, save it as a backup custom theme.
            if(legacyData != null)
                saveCustomTheme(legacyData, "BackupTheme.xml");
        }

        // There is no user theme. Create one before carrying on.
        else {
            // If there is no legacy data, creates an empty user theme.
            if(legacyData == null)
                legacyData = new ThemeData();
            // Makes sure that muCommander boots with the user's previous preferences.
	    else
		currentType = USER_THEME;

            // Creates and saves the user theme.
            userTheme            = new Theme(Translator.get("user_theme"), legacyData);
            wasUserThemeModified = true;
            saveUserTheme();
        }

        // Adds the user theme to the list of available themes.
        themes.add(userTheme);

        // If the current theme is the user theme, stores its data.
        if(currentType == USER_THEME) {
            currentData = userTheme.getThemeData();
            currentName = null;
        }
    }

    /**
     * Loads all the available theme.
     * <p>
     * This method will try to load the user theme, predefined themes and custom themes. Since it will
     * also try to set the current theme, it must never be called before the configuration has been loaded.
     * </p>
     * <p>
     * Any theme that fails to load will not be added to the available theme list. If the theme specified in
     * the configuration file fails to load, the current theme will be the one defined in {@link com.mucommander.conf.ConfigurationVariables}.
     * If <i>that</i> theme fails to load, the user defined one will be used instead.
     * </p>
     */
    public static synchronized void loadThemes() {
        themes = new Vector();

        // Loads the current theme type as defined in configuration.
        // If some error occurs here (unknown theme type), uses configuration defaults.
        try {
	    String buffer;

	    if((buffer = ConfigurationManager.getVariable(ConfigurationVariables.THEME_TYPE)) == null)
		buffer = ConfigurationVariables.DEFAULT_THEME_TYPE;
	    currentType = getThemeTypeFromLabel(buffer);
	}
        catch(Exception e) {
            currentType = getThemeTypeFromLabel(ConfigurationVariables.DEFAULT_THEME_TYPE);
            if(Debug.ON)
                Debug.trace("Illegal theme type found in configuration: " +
                            ConfigurationManager.getVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.DEFAULT_THEME_TYPE));
        }

        // Loads the current theme name as defined in configuration.
        if(currentType != USER_THEME) {
            if((currentName = ConfigurationManager.getVariable(ConfigurationVariables.THEME_NAME)) == null)
		currentName = ConfigurationVariables.DEFAULT_THEME_NAME;
	}

        // Loads user, predefined and custom themes.
        loadUserTheme();
        loadPredefinedThemes();
        loadCustomThemes();

        // If the current theme couldn't be identified, use configuration defaults.
        if(currentData == null) {
            Theme buffer;

            currentType = getThemeTypeFromLabel(ConfigurationVariables.DEFAULT_THEME_TYPE);
            currentName = ConfigurationVariables.DEFAULT_THEME_NAME;
            buffer      = getTheme(currentType, currentName);

            // If we get to this point, things are fairly bad: not only is the configuration wrong,
            // but the default theme couldn't be found or loaded. In this case, we'll default to the
            // user theme, as it's the only one that can be relied on.
            if(buffer == null) {
                currentType = USER_THEME;
                currentName = null;
                currentData = userTheme.getThemeData();
            }
            else
                currentData = buffer.getThemeData();
        }
    }


    // - User theme file access ----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the path to the user theme file.
     * @return the path to the user theme file.
     */
    public static String getUserThemeFile() {
        if(userThemeFile == null)
            return new File(PlatformManager.getPreferencesFolder(), USER_THEME_FILE_NAME).getAbsolutePath();
        return userThemeFile;
    }

    /**
     * Sets the path to the user theme file.
     * @param file path to the user theme file.
     */
    public static void setUserThemeFile(String file) {userThemeFile = file;}

    private static File getCustomThemesFolder() {
        File customFolder;

        customFolder = new File(PlatformManager.getPreferencesFolder(), CUSTOM_THEME_FOLDER);
        customFolder.mkdirs();

        return customFolder;
    }


    // - IO management -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens an input stream on the requested theme file.
     * @param  type      where to find the theme file.
     * @param  path        path of the theme to load.
     * @throws IOException thrown if any IO related error occurs.
     */
    static InputStream openInputStream(int type, String path) throws IOException {
        switch(type) {

            // User defined theme.
        case USER_THEME:
            return new BackupInputStream(getUserThemeFile());

            // Predefined themes.
        case PREDEFINED_THEME:
            return ThemeManager.class.getResourceAsStream(path);

            // Custom themes.
        case CUSTOM_THEME:
            return new FileInputStream(new File(getCustomThemesFolder(), path));

            // Error.
        default:
            throw new IllegalArgumentException("Illegal theme type type: " + type);
        }
    }

    /**
     * Saves the user theme file.
     * <p>
     * This method does its own error handling, which is why it doesn't throw any exception. However,
     * it will return <code>false</code> if an error occured.<br/>
     * The user theme file is saved using a {@link com.mucommander.io.BackupOutputStream} in an effort
     * to minimize the possibilities of corruption were muCommander to crash.
     * </p>
     * @return <code>true</code> if the operation was a success, <code>false</code> otherwise.
     */
    public static synchronized boolean saveUserTheme() {
        // Only saves the user theme if it was modified.
        if(wasUserThemeModified) {
            BackupOutputStream out;      // Where to write the user theme.

            out = null;
            try {
                // Saves the theme.
                ThemeWriter.write(userTheme.getThemeData(), out = new BackupOutputStream(getUserThemeFile()));
                out.close(true);

                // Marks the user theme as not modified.
                wasUserThemeModified = false;
                return true;
            }
            catch(Exception e) {
                // Closes the stream without deleting the backup file.
                if(out != null) {
                    try {out.close(false);}
                    catch(Exception e2) {}
                }
                return false;
            }
        }
        return true;
    }

    private static synchronized boolean saveCustomTheme(ThemeData data, String path) {
        OutputStream out;

        out = null;
        try {
            ThemeWriter.write(data, new FileOutputStream(new File(getCustomThemesFolder(), path)));
            return true;
        }
        catch(Exception e) {
            if(out != null) {
                try {out.close();}
                catch(Exception e2) {}
            }
        }
        return false;
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
        switch(currentType = theme.getType()) {
            // User defined theme.
        case USER_THEME:
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.THEME_USER);
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_NAME, currentName = null);
            break;

            // Predefined themes.
        case PREDEFINED_THEME:
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.THEME_PREDEFINED);
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_NAME, currentName = theme.getName());
            break;

            // Custom themes.
        case CUSTOM_THEME:
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.THEME_CUSTOM);
            ConfigurationManager.setVariable(ConfigurationVariables.THEME_NAME, currentName = theme.getName());
            break;

            // Error.
        default:
            throw new IllegalStateException("Illegal theme type: " + currentType);
        }
    }

    /**
     * Changes the current theme.
     * <p>
     * This method will change the current theme and trigger all the proper events.
     * </p>
     * @param theme theme to use as the current theme.
     */
    public synchronized static void setCurrentTheme(Theme theme) {
        ThemeData oldData; // Buffer for the old current data.

        // Makes sure we're not doing something useless.
        if(isCurrentTheme(theme)) {
	    setConfigurationTheme(theme);
            return;
	}

        // Sets the new data.
        oldData = currentData;
        if((currentData = theme.getThemeData()) == null)
            throw new IllegalStateException("Couldn't load data for theme: " + theme.getName());

	setConfigurationTheme(theme);

        // Triggers font events.
        for(int i = 0; i < Theme.FONT_COUNT; i++) {
            if(oldData.getFont(i) == null) {
                if(currentData.getFont(i) == null)
                    continue;
            }
            else if(currentData.getFont(i) != null) {
                if(oldData.getFont(i).equals(currentData.getFont(i)))
                    continue;
            }
            triggerFontEvent(i, getCurrentFont(i));
        }

        // Triggers color events.
        for(int i = 0; i < Theme.COLOR_COUNT; i++) {
            if(oldData.getColor(i) == null) {
                if(currentData.getColor(i) == null)
                    continue;
            }
            else if(currentData.getColor(i) != null) {
                if(oldData.getColor(i).equals(currentData.getColor(i)))
                    continue;
            }
            triggerColorEvent(i, getCurrentColor(i));
        }
    }

    /**
     * Returns the current theme's requested font.
     * @param  id identifier of the requested font.
     * @return    the current theme's requested font.
     */
    public synchronized static Font getCurrentFont(int id) {
        Font font;

        // If the requested font is not defined in the current theme,
        // returns its default value.
        if((font = currentData.getFont(id)) == null)
            return getDefaultFont(id);
        return font;
    }

    /**
     * Returns the current theme's requested color.
     * @param  id identifier of the requested color.
     * @return    the current theme's requested color.
     */
    public synchronized static Color getCurrentColor(int id) {
        Color color;

        // If the requested color is not defined in the current theme,
        // returns its default value.
        if((color = currentData.getColor(id)) == null)
            return getDefaultColor(id);
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
        // Overwrites the user theme.
        userTheme.importData(currentData);
        currentData          = userTheme.getThemeData();
        currentType          = USER_THEME;
        currentName          = null;
        wasUserThemeModified = true;
        ConfigurationManager.setVariable(ConfigurationVariables.THEME_TYPE, ConfigurationVariables.THEME_USER);
        ConfigurationManager.setVariable(ConfigurationVariables.THEME_NAME, null);
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
	oldFont = currentData.getFont(fontId);

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
	oldColor = currentData.getColor(colorId);

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
           return currentType != USER_THEME;
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
           return currentType != USER_THEME;
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
        if((currentType != USER_THEME) && !overwriteUserTheme)
            return false;

        // Overwrites the user theme.
        else
            overwriteUserTheme();

        // Sets the new font.
        currentData.setFont(id, font);
        triggerFontEvent(id, font);
        wasUserThemeModified = true;
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

        // If we need to change the user theme in order to perform the modification,
        // but we're not allowed, abort.
        if((currentType != USER_THEME) && !overwriteUserTheme)
            return false;

        // Overwrites the user theme.
        else
            overwriteUserTheme();

        currentData.setColor(id, color);
        triggerColorEvent(id, color);
        wasUserThemeModified = true;
        return true;
    }

    /**
     * Returns <code>true</code> if the specified theme is the current one.
     * @param theme theme to check.
     * @return <code>true</code> if the specified theme is the current one, <code>false</code> otherwise.
     */
    public static boolean isCurrentTheme(Theme theme) {
        if(theme.getType() != currentType)
            return false;
        if(currentType == USER_THEME)
            return true;
        return theme.getName().equals(currentName);
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



    // - Themes access -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the requested theme.
     * @param  type type of theme to retrieve.
     * @param  name name of the theme to retrieve.
     * @return the requested theme if found, <code>null</code> otherwise.
     */
    public static final Theme getTheme(int type, String name) {
        Iterator iterator; // Iterator on all the themes.
        Theme    theme;    // Buffer for each theme.

        // Goes through each theme in the list.
        iterator = themes.iterator();
        while(iterator.hasNext()) {
            theme = (Theme)iterator.next();

            // We've found the requested theme.
            if(theme.getType() == type && theme.getName().equals(name))
                return theme;
        }

        // The requested theme doesn't exist.
        return null;
    }

    /**
     * Returns an iterator on all available themes.
     * @return an iterator on all available themes.
     */
    public static Iterator availableThemes() {return themes.iterator();}



    // - Legacy theme --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns any eventual legacy theme data.
     * @return any eventual legacy theme data, <code>null</code> if none.
     */
    private static ThemeData getLegacyThemeData() {
        // Legacy theme information.
        String backgroundColor, fileColor, hiddenColor, folderColor, archiveColor, symlinkColor,
               markedColor, selectedColor, selectionColor, unfocusedColor, shellBackgroundColor,
               shellSelectionColor, shellTextColor, fontSize, fontFamily, fontStyle;
        ThemeData data; // Data for the new user theme.

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

        // If no legacy theme information could be found, use an empty user theme data.
        if(backgroundColor == null && fileColor == null && hiddenColor == null && folderColor == null && archiveColor == null &&
           symlinkColor == null && markedColor == null && selectedColor == null && selectionColor == null && unfocusedColor == null &&
           shellBackgroundColor == null && shellSelectionColor == null && shellTextColor == null && shellTextColor == null &&
           shellTextColor == null && fontFamily == null && fontSize == null && fontStyle == null)
            return null;
    
        // Creates theme data using whatever values were found in the user configuration.
        // Empty values are set to their 'old fashioned' defaults.
        Color color;
        Font  font;

        data = new ThemeData();

        // File background color.
        if(backgroundColor == null)
            backgroundColor = LegacyTheme.DEFAULT_BACKGROUND_COLOR;
        data.setColor(Theme.FILE_BACKGROUND, color = new Color(Integer.parseInt(backgroundColor, 16)));
        data.setColor(Theme.FILE_UNFOCUSED_BACKGROUND, color);

        // Selected file background color.
        if(selectionColor == null)
            selectionColor = LegacyTheme.DEFAULT_SELECTION_BACKGROUND_COLOR;
        data.setColor(Theme.FILE_BACKGROUND_SELECTED, new Color(Integer.parseInt(selectionColor, 16)));

        // Out of focus file background color.
        if(unfocusedColor == null)
            unfocusedColor = LegacyTheme.DEFAULT_OUT_OF_FOCUS_COLOR;
        data.setColor(Theme.FILE_UNFOCUSED_BACKGROUND_SELECTED, new Color(Integer.parseInt(unfocusedColor, 16)));

        // Hidden files color.
        if(hiddenColor == null)
            hiddenColor = LegacyTheme.DEFAULT_HIDDEN_FILE_COLOR;
        data.setColor(Theme.HIDDEN_FILE, new Color(Integer.parseInt(hiddenColor, 16)));

        // Folder color.
        if(folderColor == null)
            folderColor = LegacyTheme.DEFAULT_FOLDER_COLOR;
        data.setColor(Theme.FOLDER, new Color(Integer.parseInt(folderColor, 16)));

        // Archives color.
        if(archiveColor == null)
            archiveColor = LegacyTheme.DEFAULT_ARCHIVE_FILE_COLOR;
        data.setColor(Theme.ARCHIVE, new Color(Integer.parseInt(archiveColor, 16)));

        // Symbolic links color.
        if(symlinkColor == null)
            symlinkColor = LegacyTheme.DEFAULT_SYMLINK_COLOR;
        data.setColor(Theme.SYMLINK, new Color(Integer.parseInt(symlinkColor, 16)));

        // Plain file color.
        if(fileColor == null)
            fileColor = LegacyTheme.DEFAULT_PLAIN_FILE_COLOR;
        data.setColor(Theme.FILE, new Color(Integer.parseInt(fileColor, 16)));

        // Marked file color.
        if(markedColor == null)
            markedColor = LegacyTheme.DEFAULT_MARKED_FILE_COLOR;
        data.setColor(Theme.MARKED, new Color(Integer.parseInt(markedColor, 16)));

        // Selected file color.
        if(selectedColor == null)
            selectedColor = LegacyTheme.DEFAULT_SELECTED_FILE_COLOR;
        data.setColor(Theme.FILE_SELECTED, color = new Color(Integer.parseInt(selectedColor, 16)));
        data.setColor(Theme.HIDDEN_FILE_SELECTED, color);
        data.setColor(Theme.FOLDER_SELECTED, color);
        data.setColor(Theme.ARCHIVE_SELECTED, color);
        data.setColor(Theme.SYMLINK_SELECTED, color);
        data.setColor(Theme.MARKED_SELECTED, color);

        // Shell background color.
        if(shellBackgroundColor == null)
            shellBackgroundColor = LegacyTheme.DEFAULT_SHELL_BACKGROUND_COLOR;
        data.setColor(Theme.SHELL_BACKGROUND, new Color(Integer.parseInt(shellBackgroundColor, 16)));

        // Shell text color.
        if(shellTextColor == null)
            shellTextColor = LegacyTheme.DEFAULT_SHELL_TEXT_COLOR;
        data.setColor(Theme.SHELL_TEXT, color = new Color(Integer.parseInt(shellTextColor, 16)));
        data.setColor(Theme.SHELL_TEXT_SELECTED, color);

        // Shell selection background color.
        if(shellSelectionColor == null)
            shellSelectionColor = LegacyTheme.DEFAULT_SHELL_SELECTION_COLOR;
        data.setColor(Theme.SHELL_BACKGROUND_SELECTED, new Color(Integer.parseInt(shellSelectionColor, 16)));

        // File table font.
        data.setFont(Theme.FILE_TABLE, getLegacyFont(fontFamily, fontStyle, fontSize));

        // Sets colors that were not customisable in older versions of muCommander, using
        // l&f default where necessary.

        // File table border.
        data.setColor(Theme.FILE_TABLE_BORDER, new Color(64, 64, 64));

        // File editor / viewer colors.
        data.setColor(Theme.EDITOR_BACKGROUND, Color.WHITE);
        data.setColor(Theme.EDITOR_TEXT, getTextAreaColor());
        data.setColor(Theme.EDITOR_BACKGROUND_SELECTED, getTextAreaSelectionBackgroundColor());
        data.setColor(Theme.EDITOR_TEXT_SELECTED, getTextAreaSelectionColor());
        data.setFont(Theme.EDITOR, getTextAreaFont());

        // Location bar and shell history (both use text field defaults).
        data.setColor(Theme.LOCATION_BAR_PROGRESS, new Color(0, 255, 255, 64));
	color = getTextFieldBackgroundColor();
        data.setColor(Theme.LOCATION_BAR_BACKGROUND, color);
        data.setColor(Theme.SHELL_HISTORY_BACKGROUND, color);
	color = getTextFieldColor();
        data.setColor(Theme.LOCATION_BAR_TEXT, color);
        data.setColor(Theme.SHELL_HISTORY_TEXT, color);
	color = getTextFieldSelectionBackgroundColor();
        data.setColor(Theme.LOCATION_BAR_BACKGROUND_SELECTED, color);
        data.setColor(Theme.SHELL_HISTORY_BACKGROUND_SELECTED, color);
	color = getTextFieldSelectionColor();
        data.setColor(Theme.LOCATION_BAR_TEXT_SELECTED, color);
        data.setColor(Theme.SHELL_HISTORY_TEXT_SELECTED, color);

	font = getTextFieldFont();
        data.setFont(Theme.LOCATION_BAR, font);
        data.setFont(Theme.SHELL_HISTORY, font);

        return data;
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
     * @return         a font that fits the specified parameters.
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
            return USER_THEME;
        else if(label.equals(ConfigurationVariables.THEME_PREDEFINED))
            return PREDEFINED_THEME;
        else if(label.equals(ConfigurationVariables.THEME_CUSTOM))
            return CUSTOM_THEME;
        throw new IllegalStateException("Unknown theme type: " + label);
    }

    /**
     * Extracts the name of a theme from its path.
     * <p>
     * The algorithm here is a fairly simple one: a theme's name is its file name
     * without path or extension information.
     * </p>
     * @param  path path of the theme whose name should be computed.
     * @return      the proper name of the theme.
     */
    private static final String getThemeName(String path) {return path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));}

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

    static final Color getDefaultColor(int id) {
        switch(id) {
        case Theme.FILE_BACKGROUND:
        case Theme.FILE_UNFOCUSED_BACKGROUND:
	    return getTableBackgroundColor();

        case Theme.HIDDEN_FILE:
        case Theme.FOLDER:
        case Theme.ARCHIVE:
        case Theme.SYMLINK:
        case Theme.FILE:
	    return getTableColor();


        case Theme.MARKED:
        case Theme.MARKED_SELECTED:
            return Color.RED;

        case Theme.SHELL_TEXT:
        case Theme.EDITOR_TEXT:
            return getTextAreaColor();

        case Theme.SHELL_BACKGROUND:
        case Theme.EDITOR_BACKGROUND:
            return getTextAreaBackgroundColor();

        case Theme.SHELL_HISTORY_TEXT:
        case Theme.LOCATION_BAR_TEXT:
            return getTextFieldColor();

        case Theme.LOCATION_BAR_BACKGROUND:
        case Theme.SHELL_HISTORY_BACKGROUND:
            return getTextFieldBackgroundColor();

        case Theme.LOCATION_BAR_PROGRESS:
	    Color color;

	    color = getTextFieldSelectionBackgroundColor();
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), 64);

        case Theme.FILE_BACKGROUND_SELECTED:
	    return getTableSelectionBackgroundColor();

        case Theme.FILE_UNFOCUSED_BACKGROUND_SELECTED:
        case Theme.FILE_TABLE_BORDER:
            return Color.GRAY;

        case Theme.HIDDEN_FILE_SELECTED:
        case Theme.FOLDER_SELECTED:
        case Theme.ARCHIVE_SELECTED:
        case Theme.SYMLINK_SELECTED:
        case Theme.FILE_SELECTED:
	    return getTableSelectionColor();

        case Theme.SHELL_TEXT_SELECTED:
        case Theme.EDITOR_TEXT_SELECTED:
            return getTextAreaSelectionColor();

        case Theme.SHELL_BACKGROUND_SELECTED:
        case Theme.EDITOR_BACKGROUND_SELECTED:
            return getTextAreaSelectionBackgroundColor();

        case Theme.LOCATION_BAR_TEXT_SELECTED:
        case Theme.SHELL_HISTORY_TEXT_SELECTED:
            return getTextFieldSelectionColor();

        case Theme.SHELL_HISTORY_BACKGROUND_SELECTED:
        case Theme.LOCATION_BAR_BACKGROUND_SELECTED:
            return getTextFieldSelectionBackgroundColor();
        }
        throw new IllegalArgumentException("Illegal color identifier: " + id);
    }

    /**
     * Returns the default value for the specified font.
     * @param  id identifier of the font whose default value should be retrieved.
     * @return    the default value for the specified font.
     */
    static final Font getDefaultFont(int id) {
	switch(id) {
            // Table font.
        case Theme.FILE_TABLE:
	    return getTableFont();

	    // Text Area font.
        case Theme.EDITOR:
        case Theme.SHELL:
	    return getTextAreaFont();

	    // Text Field font.
        case Theme.LOCATION_BAR:
        case Theme.SHELL_HISTORY:
	    return getTextFieldFont();
        }
        throw new IllegalArgumentException("Illegal font identifier: " + id);
    }
}
