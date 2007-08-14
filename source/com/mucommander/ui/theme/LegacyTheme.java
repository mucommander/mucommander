/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

import com.mucommander.conf.impl.MuConfiguration;

/**
 * Defines older muCommander versions' way of managing themes.
 * <p>
 * This is necessary to ensure that, were this newer version be started on preferences generated
 * by older versions, no data would be lost by the user.
 * </p>
 */
interface LegacyTheme {
    // - Color variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing muCommander's various colors. */
    public static final String COLORS_SECTION                     = "colors";
    /** Color used for the background of the folder views. */
    public static final String BACKGROUND_COLOR                   = COLORS_SECTION + '.' + "background";
    /** Default folder views background colors. */
    public static final String DEFAULT_BACKGROUND_COLOR           = "000084";
    /** Color used to display plain files. */
    public static final String PLAIN_FILE_COLOR                   = COLORS_SECTION + '.' + "plain";
    /** Default plain files color. */
    public static final String DEFAULT_PLAIN_FILE_COLOR           = "00F0F0";
    /** Color used to display hidden files. */
    public static final String HIDDEN_FILE_COLOR                  = COLORS_SECTION + '.' + "hidden";
    /** Default hidden files color. */
    public static final String DEFAULT_HIDDEN_FILE_COLOR          = "C0C0C0";
    /** Color used to display folders. */
    public static final String FOLDER_COLOR                       = COLORS_SECTION + '.' + "folder";
    /** Default folders color. */
    public static final String DEFAULT_FOLDER_COLOR               = "FFFFFF";
    /** Color used to display archive files. */
    public static final String ARCHIVE_FILE_COLOR                 = COLORS_SECTION + '.' + "archive";
    /** Default archive files color. */
    public static final String DEFAULT_ARCHIVE_FILE_COLOR         = "40FF40";
    /** Color used to display symlinks. */
    public static final String SYMLINK_COLOR                      = COLORS_SECTION + '.' + "symlink";
    /** Default symlink color. */
    public static final String DEFAULT_SYMLINK_COLOR              = "CC00CC";
    /** Color used to display marked files. */
    public static final String MARKED_FILE_COLOR                  = COLORS_SECTION + '.' + "marked";
    /** Default marked files color. */
    public static final String DEFAULT_MARKED_FILE_COLOR          = "FFFF00";
    /** Color used to display selected files. */
    public static final String SELECTED_FILE_COLOR                = COLORS_SECTION + '.' + "selected";
    /** Default selected files color. */
    public static final String DEFAULT_SELECTED_FILE_COLOR        = "000080";
    /** Color used to display the background of selected files. */
    public static final String SELECTION_BACKGROUND_COLOR         = COLORS_SECTION + '.' + "selectionBackground";
    /** Default selected files background color. */
    public static final String DEFAULT_SELECTION_BACKGROUND_COLOR = "00FFFF";
    /** Color used to display selected items when muCommander doesn't have the focus. */
    public static final String OUT_OF_FOCUS_COLOR                 = COLORS_SECTION + '.' + "notInFocusSelectionBackground";
    /** Default out of focus color. */
    public static final String DEFAULT_OUT_OF_FOCUS_COLOR         = "CCCCCC";



    // - Shell color variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the shell colors. */
    public static final String SHELL_COLORS_SECTION               = COLORS_SECTION + '.' + "shell";
    /** Color used to display the background of the shell. */
    public static final String SHELL_BACKGROUND_COLOR             = SHELL_COLORS_SECTION + '.' + "background";
    /** Default shell background color. */
    public static final String DEFAULT_SHELL_BACKGROUND_COLOR     = "FFFFFF";
    /** Color used to display the selection background in the shell. */
    public static final String SHELL_SELECTION_COLOR              = SHELL_COLORS_SECTION + '.' + "selectionBackground";
    /** Default shell selection color. */
    public static final String DEFAULT_SHELL_SELECTION_COLOR      = "BBBBBB";
    /** Color used to display the shell text. */
    public static final String SHELL_TEXT_COLOR                   = SHELL_COLORS_SECTION + '.' + "text";
    /** Default shell text color. */
    public static final String DEFAULT_SHELL_TEXT_COLOR           = "000000";



    // - Font variables ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the font used by muCommander. */
    public static final String FONT_SECTION                       = "font";
    /** Family of the font used by muCommander. */
    public static final String FONT_FAMILY                        = FONT_SECTION + '.' + "family";
    /** Size of the font used by muCommander. */
    public static final String FONT_SIZE                          = FONT_SECTION + '.' + "size";
    /** Style of the font used by muCommander. */
    public static final String FONT_STYLE                         = FONT_SECTION + '.' + "style";
}
