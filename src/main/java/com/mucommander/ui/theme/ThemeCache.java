/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.theme;

import java.awt.*;
import java.util.WeakHashMap;


/**
 * Contains cached colors and fonts for current theme.
 *
 * @author Mariusz Jakubowski
 */
public class ThemeCache implements ThemeListener {
    
    // - Color definitions -----------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static Color[][][] foregroundColors;
    public static Color[][]   backgroundColors;
    public static Color       unmatchedForeground;
    public static Color       unmatchedBackground;
    public static Color       activeOutlineColor;
    public static Color       inactiveOutlineColor;
    public static final int NORMAL               = 0;
    public static final int SELECTED             = 1;
    public static final int ALTERNATE            = 2;
    public static final int SECONDARY            = 3;
    public static final int INACTIVE             = 0;
    public static final int ACTIVE               = 1;
    public static final int HIDDEN_FILE          = 0;
    public static final int FOLDER               = 1;
    public static final int ARCHIVE              = 2;
    public static final int SYMLINK              = 3;
    public static final int MARKED               = 4;
    public static final int PLAIN_FILE           = 5;

    // - Font definitions ------------------------------------------------------------
    // -------------------------------------------------------------------------------
    public static Font tableFont;
    
    /** Theme cache instance */
    public static final ThemeCache instance = new ThemeCache();
  
    // - Initialisation --------------------------------------------------------------
    // -------------------------------------------------------------------------------
    static {
        foregroundColors = new Color[2][2][6];
        backgroundColors = new Color[2][4];

        // Active background colors.
        backgroundColors[ACTIVE][NORMAL]    = ThemeManager.getCurrentColor(Theme.FILE_TABLE_BACKGROUND_COLOR);
        backgroundColors[ACTIVE][SELECTED]  = ThemeManager.getCurrentColor(Theme.FILE_TABLE_SELECTED_BACKGROUND_COLOR);
        backgroundColors[ACTIVE][ALTERNATE] = ThemeManager.getCurrentColor(Theme.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR);
        backgroundColors[ACTIVE][SECONDARY] = ThemeManager.getCurrentColor(Theme.FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR);

        // Inactive background colors.
        backgroundColors[INACTIVE][NORMAL]    = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR);
        backgroundColors[INACTIVE][SELECTED]  = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR);
        backgroundColors[INACTIVE][ALTERNATE] = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR);
        backgroundColors[INACTIVE][SECONDARY] = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR);

        // Normal foreground foregroundColors.
        foregroundColors[ACTIVE][NORMAL][HIDDEN_FILE]     = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][FOLDER]          = ThemeManager.getCurrentColor(Theme.FOLDER_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][ARCHIVE]         = ThemeManager.getCurrentColor(Theme.ARCHIVE_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][SYMLINK]         = ThemeManager.getCurrentColor(Theme.SYMLINK_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][MARKED]          = ThemeManager.getCurrentColor(Theme.MARKED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][NORMAL][PLAIN_FILE]      = ThemeManager.getCurrentColor(Theme.FILE_FOREGROUND_COLOR);

        // Normal unfocused foreground foregroundColors.
        foregroundColors[INACTIVE][NORMAL][HIDDEN_FILE]    = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][FOLDER]         = ThemeManager.getCurrentColor(Theme.FOLDER_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][ARCHIVE]        = ThemeManager.getCurrentColor(Theme.ARCHIVE_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][SYMLINK]        = ThemeManager.getCurrentColor(Theme.SYMLINK_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][MARKED]         = ThemeManager.getCurrentColor(Theme.MARKED_INACTIVE_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][NORMAL][PLAIN_FILE]     = ThemeManager.getCurrentColor(Theme.FILE_INACTIVE_FOREGROUND_COLOR);

        // Selected foreground foregroundColors.
        foregroundColors[ACTIVE][SELECTED][HIDDEN_FILE]   = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][FOLDER]        = ThemeManager.getCurrentColor(Theme.FOLDER_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][ARCHIVE]       = ThemeManager.getCurrentColor(Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][SYMLINK]       = ThemeManager.getCurrentColor(Theme.SYMLINK_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][MARKED]        = ThemeManager.getCurrentColor(Theme.MARKED_SELECTED_FOREGROUND_COLOR);
        foregroundColors[ACTIVE][SELECTED][PLAIN_FILE]    = ThemeManager.getCurrentColor(Theme.FILE_SELECTED_FOREGROUND_COLOR);

        // Selected unfocused foreground foregroundColors.
        foregroundColors[INACTIVE][SELECTED][HIDDEN_FILE]  = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][FOLDER]       = ThemeManager.getCurrentColor(Theme.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][ARCHIVE]      = ThemeManager.getCurrentColor(Theme.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][SYMLINK]      = ThemeManager.getCurrentColor(Theme.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][MARKED]       = ThemeManager.getCurrentColor(Theme.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR);
        foregroundColors[INACTIVE][SELECTED][PLAIN_FILE]   = ThemeManager.getCurrentColor(Theme.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR);

        unmatchedForeground                                = ThemeManager.getCurrentColor(Theme.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR);
        unmatchedBackground                                = ThemeManager.getCurrentColor(Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR);
        tableFont                                          = ThemeManager.getCurrentFont(Theme.FILE_TABLE_FONT);

        activeOutlineColor                                 = ThemeManager.getCurrentColor(Theme.FILE_TABLE_SELECTED_OUTLINE_COLOR);
        inactiveOutlineColor                               = ThemeManager.getCurrentColor(Theme.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR);

        ThemeManager.addCurrentThemeListener(instance);
    }

   
    /** Listeners. */
    private static WeakHashMap<ThemeListener, ?> listeners = new WeakHashMap<ThemeListener, Object>();
    

    private ThemeCache() {
	}
    
    public static void addThemeListener(ThemeListener listener) {
        listeners.put(listener, null);
    }

    public static void removeThemeListener(ThemeListener listener) {
        listeners.remove(listener);
    }
    
    private static void fireColorChanged(ColorChangedEvent event) {
        for(ThemeListener listener : listeners.keySet())
            listener.colorChanged(event);
    }
    
    private static void fireFontChanged(FontChangedEvent event) {
        for(ThemeListener listener : listeners.keySet())
            listener.fontChanged(event);
    }
    

    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     */
    public void colorChanged(ColorChangedEvent event) {
        switch(event.getColorId()) {
            // Plain file color.
        case Theme.FILE_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][PLAIN_FILE] = event.getColor();
            break;

            // Selected file color.
        case Theme.FILE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][PLAIN_FILE] = event.getColor();
            break;

            // Hidden files.
        case Theme.HIDDEN_FILE_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][HIDDEN_FILE] = event.getColor();
            break;

            // Selected hidden files.
        case Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][HIDDEN_FILE] = event.getColor();
            break;

            // Folders.
        case Theme.FOLDER_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][FOLDER] = event.getColor();
            break;

            // Selected folders.
        case Theme.FOLDER_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][FOLDER] = event.getColor();
            break;

            // Archives.
        case Theme.ARCHIVE_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][ARCHIVE] = event.getColor();
            break;

            // Selected archives.
        case Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][ARCHIVE] = event.getColor();
            break;

            // Symlinks.
        case Theme.SYMLINK_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][SYMLINK] = event.getColor();
            break;

            // Selected symlinks.
        case Theme.SYMLINK_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][SYMLINK] = event.getColor();
            break;

            // Marked files.
        case Theme.MARKED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][NORMAL][MARKED] = event.getColor();
            break;

            // Selected marked files.
        case Theme.MARKED_SELECTED_FOREGROUND_COLOR:
            foregroundColors[ACTIVE][SELECTED][MARKED] = event.getColor();
            break;

            // Plain file color.
        case Theme.FILE_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][PLAIN_FILE] = event.getColor();
            break;

            // Selected file color.
        case Theme.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][PLAIN_FILE] = event.getColor();
            break;

            // Hidden files.
        case Theme.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][HIDDEN_FILE] = event.getColor();
            break;

            // Selected hidden files.
        case Theme.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][HIDDEN_FILE] = event.getColor();
            break;

            // Folders.
        case Theme.FOLDER_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][FOLDER] = event.getColor();
            break;

            // Selected folders.
        case Theme.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][FOLDER] = event.getColor();
            break;

            // Archives.
        case Theme.ARCHIVE_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][ARCHIVE] = event.getColor();
            break;

            // Selected archives.
        case Theme.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][ARCHIVE] = event.getColor();
            break;

            // Symlinks.
        case Theme.SYMLINK_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][SYMLINK] = event.getColor();
            break;

            // Selected symlinks.
        case Theme.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][SYMLINK] = event.getColor();
            break;

            // Marked files.
        case Theme.MARKED_INACTIVE_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][NORMAL][MARKED] = event.getColor();
            break;

            // Selected marked files.
        case Theme.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR:
            foregroundColors[INACTIVE][SELECTED][MARKED] = event.getColor();
            break;

            // Unmatched foreground
        case Theme.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR:
            unmatchedForeground = event.getColor();
            break;

            // Unmached background
        case Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR:
            unmatchedBackground = event.getColor();
            break;

            // Active normal background.
        case Theme.FILE_TABLE_BACKGROUND_COLOR:
            backgroundColors[ACTIVE][NORMAL] = event.getColor();
            break;

            // Active selected background.
        case Theme.FILE_TABLE_SELECTED_BACKGROUND_COLOR:
            backgroundColors[ACTIVE][SELECTED] = event.getColor();
            break;

            // Active alternate background.
        case Theme.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR:
            backgroundColors[ACTIVE][ALTERNATE] = event.getColor();
            break;

            // Inactive normal background.
        case Theme.FILE_TABLE_INACTIVE_BACKGROUND_COLOR:
            backgroundColors[INACTIVE][NORMAL] = event.getColor();
            break;

            // Inactive selected background.
        case Theme.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR:
            backgroundColors[INACTIVE][SELECTED] = event.getColor();
            break;

            // Inactive alternate background.
        case Theme.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR:
            backgroundColors[INACTIVE][ALTERNATE] = event.getColor();
            break;

            // Active selection outline.
        case Theme.FILE_TABLE_SELECTED_OUTLINE_COLOR:
            activeOutlineColor = event.getColor();
            break;

            // Inactive selection outline.
        case Theme.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR:
            inactiveOutlineColor = event.getColor();
            break;

            // Secondary background color.
        case Theme.FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR:
            backgroundColors[ACTIVE][SECONDARY] = event.getColor();
            break;

            // Inactive secondary background color.
        case Theme.FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR:
            backgroundColors[INACTIVE][SECONDARY] = event.getColor();
            break;

        default:
            return;
        }
        fireColorChanged(event);
    }

    /**
     * Receives theme font changes notifications.
     */
    public void fontChanged(FontChangedEvent event) {
    	switch (event.getFontId()) {
    	case Theme.FILE_TABLE_FONT:
    		tableFont = event.getFont();
    		break;
   		default:
   		    return;
     	}
    	fireFontChanged(event);
    }
	
}
