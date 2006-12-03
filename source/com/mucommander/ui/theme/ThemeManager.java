package com.mucommander.ui.theme;

import com.mucommander.*;
import com.mucommander.io.*;

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



    // - User theme access ---------------------------------------------------------------
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
}
