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
    private static boolean       wasUserThemeModified;
    /** Theme that is currently applied to muCommander. */
    private static Theme         currentTheme;
    private static ThemeListener listener = new CurrentThemeListener();


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
        catch(Exception e) {type = getThemeTypeFromLabel(ConfigurationVariables.DEFAULT_THEME_TYPE);}

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
                    currentTheme         = new Theme(listener);
                    wasUserThemeModified = true;
                }
            }
            setConfigurationTheme(currentTheme);
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
     * <p>
     * An important pitfall of this method is that it returns the themes as they are know at a specific point of time.
     * If instances of themes were kept from a previous call, they might very well be inconsistant with the new ones.
     * This will typically happen when the user theme was modified but not saved: the 'old' user theme and the 'new' one
     * will hold different values.<br/>
     * While the current theme is guaranteed to stay up-to-date, the user theme can easily changed using the
     * {@link #overwriteUserTheme(Theme)} method.
     * </p>
     * @return an iterator on all available themes.
     */
    public static synchronized Iterator availableThemes() {
        Vector themes;

        themes = new Vector();

        // Tries to load the user theme. If it's corrupt, uses an empty user theme.
        try {themes.add(getTheme(Theme.USER_THEME, null));}
        catch(Exception e) {themes.add(new Theme(listener));}

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
     * @see    #setUserThemeFile(String)
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
        // Makes sure no NullPointerException is raised if this method is called
        // before themes have been initialised.
        if(currentTheme == null)
            return true;

        // Saves the user theme if it's the current one.
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
    public static final Theme getTheme(int type, String name) throws Exception {
        Theme theme;
        ThemeData template;

        // Do not reload the current theme, both for optimisation purposes and because
        // it might cause user theme modifications to be lost.
        if(currentTheme != null && isCurrentTheme(type, name))
            return currentTheme;

        switch(type) {
            // User defined theme.
        case Theme.USER_THEME:
            ThemeReader.read(new BackupInputStream(ThemeManager.getUserThemeFile()), template = new ThemeData());
            theme = new Theme(listener, template);
            break;

            // Predefined themes.
        case Theme.PREDEFINED_THEME:
            ThemeReader.read(ResourceLoader.getResourceAsStream(RuntimeConstants.THEMES_PATH + "/" + name + ".xml"),
                             template = new ThemeData());
            theme = new Theme(listener, template, Theme.PREDEFINED_THEME, name);
            break;

            // Custom themes.
        case Theme.CUSTOM_THEME:
            ThemeReader.read(new FileInputStream(new File(ThemeManager.getCustomThemesFolder(), name + ".xml")),
                             template = new ThemeData());
            theme = new Theme(listener, template, Theme.CUSTOM_THEME, name);
            break;

            // Error.
        default:
            throw new IllegalArgumentException("Illegal theme type type: " + type);
        }

        return theme;
    }

    public static synchronized boolean saveTheme(Theme theme) {
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
    public static Theme getCurrentTheme() {return currentTheme;}

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

        // Triggers the events generated by the theme change.
        triggerThemeChange(oldTheme, currentTheme);
    }

    private static void triggerThemeChange(Theme oldTheme, Theme newTheme) {
        // Triggers font events.
        for(int i = 0; i < Theme.FONT_COUNT; i++) {
            if(!oldTheme.getFont(i).equals(newTheme.getFont(i)))
                triggerFontEvent(new FontChangedEvent(currentTheme, i, newTheme.getFont(i)));
        }

        // Triggers color events.
        for(int i = 0; i < Theme.COLOR_COUNT; i++) {
            if(!oldTheme.getColor(i).equals(newTheme.getColor(i)))
                triggerColorEvent(new ColorChangedEvent(currentTheme, i, newTheme.getColor(i)));
        }
    }

    public synchronized static Font getCurrentFont(int id) {return currentTheme.getFont(id);}

    public synchronized static Color getCurrentColor(int id) {return currentTheme.getColor(id);}

    /**
     * Copies the current theme over the user theme.
     */
    public synchronized static void overwriteUserTheme(Theme theme) {
        boolean updateCurrentTheme;

        updateCurrentTheme = currentTheme.getType() == Theme.USER_THEME;

        // Marks the current theme as the user one and saves it.
        theme.setType(Theme.USER_THEME);
        if(theme == currentTheme)
            setConfigurationTheme(theme);
        wasUserThemeModified = true;

        // If the user theme was the current one, notifies listeners of any change.
        if(updateCurrentTheme) {
            Theme oldTheme;

            oldTheme     = currentTheme;
            currentTheme = theme;
            triggerThemeChange(oldTheme, currentTheme);
        }
    }

    /**
     * Checks whether setting the specified font would require overwriting of the user theme.
     * @param  fontId identifier of the font to set.
     * @param  font   value for the specified font.
     * @return        <code>true</code> if applying the specified font will overwrite the user theme,
     *                <code>false</code> otherwise.
     */
    public synchronized static boolean willOverwriteUserTheme(int fontId, Font font) {
        if(currentTheme.isFontDifferent(fontId, font))
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
        if(currentTheme.isColorDifferent(colorId, color))
            return currentTheme.getType() != Theme.USER_THEME;
        return false;
    }

    /**
     * Updates the current theme with the specified font.
     * <p>
     * This method might require to overwrite the user theme: custom and predefined themes are
     * read only. In order to modify them, the ThemeManager must overwrite the user theme with
     * the current theme and then set the font.<br/>
     * If necessary, this can be checked beforehand by a call to {@link #willOverwriteUserTheme(int,Font)}.
     * </p>
     * @param  id   identifier of the font to set.
     * @param  font font to set.
     */
    public synchronized static boolean setCurrentFont(int id, Font font) {
        // Only updates if necessary.
        if(currentTheme.isFontDifferent(id, font)) {
            // Checks whether we need to overwrite the user theme to perform this action.
            if(currentTheme.getType() != Theme.USER_THEME) {
                overwriteUserTheme(currentTheme);
                setConfigurationTheme(currentTheme);
            }

            currentTheme.setFont(id, font);
            return true;
        }
        return false;
    }

    /**
     * Updates the current theme with the specified color.
     * <p>
     * This method might require to overwrite the user theme: custom and predefined themes are
     * read only. In order to modify them, the ThemeManager must overwrite the user theme with
     * the current theme and then set the color.<br/>
     * If necessary, this can be checked beforehand by a call to {@link #willOverwriteUserTheme(int,Color)}.
     * </p>
     * @param  id   identifier of the color to set.
     * @param  color color to set.
     */
    public synchronized static boolean setCurrentColor(int id, Color color) {
        // Only updates if necessary.
        if(currentTheme.isColorDifferent(id, color)) {
            // Checks whether we need to overwrite the user theme to perform this action.
            if(currentTheme.getType() != Theme.USER_THEME) {
                overwriteUserTheme(currentTheme);
                setConfigurationTheme(currentTheme);
            }

            // Updates the color and notifies listeners.
            currentTheme.setColor(id, color);
            return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the specified theme is the current one.
     * @param theme theme to check.
     * @return <code>true</code> if the specified theme is the current one, <code>false</code> otherwise.
     */
    public static boolean isCurrentTheme(Theme theme) {return theme == currentTheme;}

    private static boolean isCurrentTheme(int type, String name) {
        if(type != currentTheme.getType())
            return false;
        if(type == Theme.USER_THEME)
            return true;
        return name.equals(currentTheme.getName());
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
     */
    private static void triggerFontEvent(FontChangedEvent event) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).fontChanged(event);
    }

    /**
     * Notifies all theme listeners of the specified color's new value.
     */
    private static void triggerColorEvent(ColorChangedEvent event) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).colorChanged(event);
    }



    // - Legacy theme --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private static void importLegacyTheme() {
        // Legacy theme information.
        String backgroundColor, fileColor, hiddenColor, folderColor, archiveColor, symlinkColor,
               markedColor, selectedColor, selectionColor, unfocusedColor, shellBackgroundColor,
               shellSelectionColor, shellTextColor, fontSize, fontFamily, fontStyle;
        ThemeData legacyTemplate; // Data for the new user theme.

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

        legacyTemplate = new ThemeData();

        // File background color.
        if(backgroundColor == null)
            backgroundColor = LegacyTheme.DEFAULT_BACKGROUND_COLOR;
        legacyTemplate.setColor(Theme.FILE_BACKGROUND_COLOR, color = new Color(Integer.parseInt(backgroundColor, 16)));
        legacyTemplate.setColor(Theme.HIDDEN_FILE_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.MARKED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.FOLDER_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.SYMLINK_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.ARCHIVE_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.FILE_TABLE_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.FOLDER_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.ARCHIVE_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.SYMLINK_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.MARKED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.FILE_UNFOCUSED_BACKGROUND_COLOR, color);

        // Selected file background color.
        if(selectionColor == null)
            selectionColor = LegacyTheme.DEFAULT_SELECTION_BACKGROUND_COLOR;
        legacyTemplate.setColor(Theme.FILE_SELECTED_BACKGROUND_COLOR, color = new Color(Integer.parseInt(selectionColor, 16)));
        legacyTemplate.setColor(Theme.HIDDEN_FILE_SELECTED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.MARKED_SELECTED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.FOLDER_SELECTED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.SYMLINK_SELECTED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.ARCHIVE_SELECTED_BACKGROUND_COLOR, color);

        // Out of focus file background color.
        if(unfocusedColor == null)
            unfocusedColor = LegacyTheme.DEFAULT_OUT_OF_FOCUS_COLOR;
        legacyTemplate.setColor(Theme.FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color = new Color(Integer.parseInt(unfocusedColor, 16)));
        legacyTemplate.setColor(Theme.FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR, color);

        // Hidden files color.
        if(hiddenColor == null)
            hiddenColor = LegacyTheme.DEFAULT_HIDDEN_FILE_COLOR;
        legacyTemplate.setColor(Theme.HIDDEN_FILE_FOREGROUND_COLOR, color = new Color(Integer.parseInt(hiddenColor, 16)));
        legacyTemplate.setColor(Theme.HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR, color);

        // Folder color.
        if(folderColor == null)
            folderColor = LegacyTheme.DEFAULT_FOLDER_COLOR;
        legacyTemplate.setColor(Theme.FOLDER_FOREGROUND_COLOR, color = new Color(Integer.parseInt(folderColor, 16)));
        legacyTemplate.setColor(Theme.FOLDER_UNFOCUSED_FOREGROUND_COLOR, color);

        // Archives color.
        if(archiveColor == null)
            archiveColor = LegacyTheme.DEFAULT_ARCHIVE_FILE_COLOR;
        legacyTemplate.setColor(Theme.ARCHIVE_FOREGROUND_COLOR, color = new Color(Integer.parseInt(archiveColor, 16)));
        legacyTemplate.setColor(Theme.ARCHIVE_UNFOCUSED_FOREGROUND_COLOR, color);

        // Symbolic links color.
        if(symlinkColor == null)
            symlinkColor = LegacyTheme.DEFAULT_SYMLINK_COLOR;
        legacyTemplate.setColor(Theme.SYMLINK_FOREGROUND_COLOR, color = new Color(Integer.parseInt(symlinkColor, 16)));
        legacyTemplate.setColor(Theme.SYMLINK_UNFOCUSED_FOREGROUND_COLOR, color);

        // Plain file color.
        if(fileColor == null)
            fileColor = LegacyTheme.DEFAULT_PLAIN_FILE_COLOR;
        legacyTemplate.setColor(Theme.FILE_FOREGROUND_COLOR, color = new Color(Integer.parseInt(fileColor, 16)));
        legacyTemplate.setColor(Theme.FILE_UNFOCUSED_FOREGROUND_COLOR, color);

        // Marked file color.
        if(markedColor == null)
            markedColor = LegacyTheme.DEFAULT_MARKED_FILE_COLOR;
        legacyTemplate.setColor(Theme.MARKED_FOREGROUND_COLOR, color = new Color(Integer.parseInt(markedColor, 16)));
        legacyTemplate.setColor(Theme.MARKED_UNFOCUSED_FOREGROUND_COLOR, color);

        // Selected file color.
        if(selectedColor == null)
            selectedColor = LegacyTheme.DEFAULT_SELECTED_FILE_COLOR;
        legacyTemplate.setColor(Theme.FILE_SELECTED_FOREGROUND_COLOR, color = new Color(Integer.parseInt(selectedColor, 16)));
        legacyTemplate.setColor(Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.FOLDER_SELECTED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.SYMLINK_SELECTED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.MARKED_SELECTED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);
        legacyTemplate.setColor(Theme.MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR, color);

        // Shell background color.
        if(shellBackgroundColor == null)
            shellBackgroundColor = LegacyTheme.DEFAULT_SHELL_BACKGROUND_COLOR;
        legacyTemplate.setColor(Theme.SHELL_BACKGROUND_COLOR, new Color(Integer.parseInt(shellBackgroundColor, 16)));

        // Shell text color.
        if(shellTextColor == null)
            shellTextColor = LegacyTheme.DEFAULT_SHELL_TEXT_COLOR;
        legacyTemplate.setColor(Theme.SHELL_FOREGROUND_COLOR, color = new Color(Integer.parseInt(shellTextColor, 16)));
        legacyTemplate.setColor(Theme.SHELL_SELECTED_FOREGROUND_COLOR, color);

        // Shell selection background color.
        if(shellSelectionColor == null)
            shellSelectionColor = LegacyTheme.DEFAULT_SHELL_SELECTION_COLOR;
        legacyTemplate.setColor(Theme.SHELL_SELECTED_BACKGROUND_COLOR, new Color(Integer.parseInt(shellSelectionColor, 16)));

        // File table font.
        legacyTemplate.setFont(Theme.FILE_TABLE_FONT, font = getLegacyFont(fontFamily, fontStyle, fontSize));

        // Sets colors that were not customisable in older versions of muCommander, using
        // l&f default where necessary.

        // File table border.
        legacyTemplate.setColor(Theme.FILE_TABLE_BORDER_COLOR, new Color(64, 64, 64));

        // Location bar and shell history (both use text field defaults).
        legacyTemplate.setColor(Theme.LOCATION_BAR_PROGRESS_COLOR, new Color(0, 255, 255, 64));

        Theme legacyTheme;
        // If the user theme exists, saves the legacy theme as backup.
        if(new File(getUserThemeFile()).exists())
            legacyTheme = new Theme(listener, legacyTemplate, Theme.CUSTOM_THEME, "BackupTheme");
        // Otherwise, creates a new user theme using the legacy data.
        else {
            legacyTheme = new Theme(listener, legacyTemplate);
            setConfigurationTheme(legacyTheme);
            wasUserThemeModified = true;
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

    private static String getThemeName(String path) {return path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));}



    // - Listener methods ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private static class CurrentThemeListener implements ThemeListener {
        public void fontChanged(FontChangedEvent event) {
            if(event.getSource().getType() == Theme.USER_THEME)
                wasUserThemeModified = true;

            if(event.getSource() == currentTheme)
                triggerFontEvent(event);
        }

        public void colorChanged(ColorChangedEvent event) {
            if(event.getSource().getType() == Theme.USER_THEME)
                wasUserThemeModified = true;

            if(event.getSource() == currentTheme)
                triggerColorEvent(event);
        }
    }
}
