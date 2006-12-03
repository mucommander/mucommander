package com.mucommander.ui.theme;

import com.mucommander.*;
import com.mucommander.io.*;
import com.mucommander.conf.*;

import java.util.*;
import java.io.*;

/**
 * @author Nicolas Rinaudo
 */
public class ThemeManager {
    // - Class variables -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Current theme used by the user. */
    private static       Theme  currentTheme;
    /** Names of all the available themes. */
    private static       Vector themes;
    /** Path to the user defined theme file. */
    private static       String userThemeFile;
    /** Default user defined theme file name. */
    private static final String USER_THEME_FILE_NAME = "user_theme.xml";


    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Prevents instanciations of the class.
     */
    private ThemeManager() {}



    // - User theme file access ----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the path to the user theme file.
     * @return the path to the user theme file.
     */
    private static String getUserThemeFile() {
        if(userThemeFile == null)
            return new File(PlatformManager.getPreferencesFolder(), USER_THEME_FILE_NAME).getAbsolutePath();
        return userThemeFile;
    }

    /**
     * Sets the path to the user theme file.
     * @param file path to the user theme file.
     */
    public static void setUserThemeFile(String file) {userThemeFile = file;}



    // - Themes IO -----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Loads a theme from the specified file path.
     * @param  path        where to load the theme from
     * @param  isUserTheme whether we're loading the user theme or not.
     * @throws Exception   if an error occurs during the loading process.
     */
    private static Theme loadTheme(String path, boolean isUserTheme) throws Exception {
        InputStream in;    // Where to read the theme from.
        Theme       theme; // Theme that has been loaded.

        in = null;
        try {
            // Opens the proper input stream type.
            if(isUserTheme)
                in = new BackupInputStream(path);
            else
                in = new FileInputStream(path);

            // Loads the theme.
            theme = ThemeReader.read(in);

            // Sets non user theme to read only.
            if(!isUserTheme)
                theme.setUserTheme(false);
            return theme;
        }
        // In Debug mode, we want to trace the source of such errors.
        catch(Exception e) {
            if(Debug.ON) {
                Debug.trace("Error while loading theme " + path + ": ");
                Debug.trace(e);
            }
            throw e;
        }
        // Makes sure streams are properly closed.
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }
}
