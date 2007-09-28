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

package com.mucommander.conf.impl;

import com.mucommander.ui.icon.FileIcons;
import com.mucommander.PlatformManager;
import com.mucommander.RuntimeConstants;
import com.mucommander.conf.Configuration;
import com.mucommander.conf.ConfigurationFormatException;
import com.mucommander.conf.ConfigurationException;
import com.mucommander.conf.ConfigurationListener;

import java.io.IOException;

/**
 * @author Nicolas Rinaudo
 */
public class MuConfiguration {
    // - Misc. variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Whether or not to automaticaly check for updates on startup. */
    public static final String  CHECK_FOR_UPDATE                  = "check_for_updates_on_startup";
    /** Default automated update behavior. */
    public static final boolean DEFAULT_CHECK_FOR_UPDATE          = true;
    /** Description of the format dates should be displayed with. */
    public static final String  DATE_FORMAT                       = "date_format";
    /** Default date format. */
    public static final String  DEFAULT_DATE_FORMAT               = "MM/dd/yy";
    /** Character used to separate years, months and days in a date. */
    public static final String  DATE_SEPARATOR                    = "date_separator";
    /** Default date separator. */
    public static final String  DEFAULT_DATE_SEPARATOR            = "/";
    /** Description of the format timestamps should be displayed with.*/
    public static final String  TIME_FORMAT                       = "time_format";
    /** Default time format. */
    public static final String  DEFAULT_TIME_FORMAT               = "hh:mm a";
    /** Language muCommander should use when looking for text.. */
    public static final String  LANGUAGE                          = "language";
    /** muCommander's version at time of writing the configuration file. */
    public static final String  VERSION                           = "conf_version";
    /** Whether or not to display compact file sizes. */
    public static final String  DISPLAY_COMPACT_FILE_SIZE         = "display_compact_file_size";
    /** Default file size display behavior. */
    public static final boolean DEFAULT_DISPLAY_COMPACT_FILE_SIZE = true;
    /** Whether or not to ask the user for confirmation before quitting muCommander. */
    public static final String  CONFIRM_ON_QUIT                   = "quit_confirmation";
    /** Default quitting behavior. */
    public static final boolean DEFAULT_CONFIRM_ON_QUIT           = true;
    /** Look and feel used by muCommander. */
    public static final String  LOOK_AND_FEEL                     = "lookAndFeel";
    /** Controls whether system notifications are enabled. */
    public static final String  ENABLE_SYSTEM_NOTIFICATIONS       = "enable_system_notifications";
    /** Default for enable system notifications. */
    public static final boolean DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS = com.mucommander.ui.notifier.AbstractNotifier.isAvailable()
        && (PlatformManager.getOsFamily()==PlatformManager.MAC_OS_X || PlatformManager.getOsFamily()==PlatformManager.WINDOWS_NT);
    /** Controls whether files should be moved to trash or permanently erased */
    public static final String DELETE_TO_TRASH                    = "delete_to_trash";
    /** Default 'delete to trash' behavior */
    public static final boolean DEFAULT_DELETE_TO_TRASH           = true;



    // - Shell variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the shell configuration. */
    public static final String  SHELL_SECTION                     = "shell";
    /** Shell invocation command (in case muCommander is not using the default one). */
    public static final String  CUSTOM_SHELL                      = SHELL_SECTION + '.' + "custom_command";
    /** Whether or not to use a custom shell invocation command. */
    public static final String  USE_CUSTOM_SHELL                  = SHELL_SECTION + '.' + "use_custom";
    /** Default custom shell behavior. */
    public static final boolean DEFAULT_USE_CUSTOM_SHELL          = false;
    /** Maximum number of items that should be present in the shell history. */
    public static final String  SHELL_HISTORY_SIZE                = SHELL_SECTION + '.' + "history_size";
    /** Encoding used to read the shell output. */
    public static final String  SHELL_ENCODING                    = SHELL_SECTION + '.' + "encoding";
    /** Whether or not shell encoding should be auto-detected. */
    public static final String  AUTODETECT_SHELL_ENCODING         = SHELL_SECTION + '.' + "autodect_encoding";
    /** Default shell encoding auto-detection behaviour. */
    public static final boolean DEFAULT_AUTODETECT_SHELL_ENCODING = true;
    /** Default maximum shell history size. */
    public static final int     DEFAULT_SHELL_HISTORY_SIZE        = 100;



    // - Mail variables ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing mail configuration. */
    public static final String MAIL_SECTION                       = "mail";
    /** Address of the SMTP server that should be used when sending mails. */
    public static final String SMTP_SERVER                        = MAIL_SECTION + '.' + "smtp_server";
    /** Name under which mails sent by muCommander should appear. */
    public static final String MAIL_SENDER_NAME                   = MAIL_SECTION + '.' + "sender_name";
    /** Address which mails sent by muCommander should be replied to. */
    public static final String MAIL_SENDER_ADDRESS                = MAIL_SECTION + '.' + "sender_address";



    // - Command bar variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the command bar configuration. */
    public static final String  COMMAND_BAR_SECTION               = "command_bar";
    /** Whether or not the command bar is visible. */
    public static final String  COMMAND_BAR_VISIBLE               = COMMAND_BAR_SECTION + '.' + "visible";
    /** Default command bar visibility. */
    public static final boolean DEFAULT_COMMAND_BAR_VISIBLE       = true;
    /** Scale factor of commandbar icons. */
    public static final String  COMMAND_BAR_ICON_SCALE            = COMMAND_BAR_SECTION + '.' + "icon_scale";
    /** Default scale factor of commandbar icons. */
    public static final float   DEFAULT_COMMAND_BAR_ICON_SCALE    = 1.0f;



    // - Status bar variables ------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the status bar configuration. */
    public static final String STATUS_BAR_SECTION                 = "status_bar";
    /** Whether or not the status bar is visible. */
    public static final String STATUS_BAR_VISIBLE                 = STATUS_BAR_SECTION + '.' + "visible";
    /** Default status bar visibility. */
    public static final boolean DEFAULT_STATUS_BAR_VISIBLE        = true;



    // - Toolbar variables ---------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the toolbar configuration. */
    public static final String TOOLBAR_SECTION                    = "toolbar";
    /** Whether or not the toolbar is visible. */
    public static final String TOOLBAR_VISIBLE                    = TOOLBAR_SECTION + '.' + "visible";
    /** Default toolbar visibility. */
    public static final boolean DEFAULT_TOOLBAR_VISIBLE           = true;
    /** Scale factor of toolbar icons. */
    public static final String  TOOLBAR_ICON_SCALE                = TOOLBAR_SECTION + '.' + "icon_scale";
    /** Default scale factor of toolbar icons. */
    public static final float   DEFAULT_TOOLBAR_ICON_SCALE        = 1.0f;



    // - Folders variables ---------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the folders view configuration. */
    public static final String  FILE_TABLE_SECTION                = "file_table";
    /** Whether or not to display hidden files. */
    public static final String  SHOW_HIDDEN_FILES                 = FILE_TABLE_SECTION + '.' + "show_hidden_files";
    /** Default hidden files visibility. */
    public static final boolean DEFAULT_SHOW_HIDDEN_FILES         = true;
    /** Whether or not to display OS X .DS_Store files. */
    public static final String  SHOW_DS_STORE_FILES               = FILE_TABLE_SECTION + '.' + "show_ds_store_files";
    /** Default .DS_Store files visibility. */
    public static final boolean DEFAULT_SHOW_DS_STORE_FILES       = true;
    /** Whether or not to display system folders. */
    public static final String  SHOW_SYSTEM_FOLDERS               = FILE_TABLE_SECTION + '.' + "show_system_folders";
    /** Default system folders visibility. */
    public static final boolean DEFAULT_SHOW_SYSTEM_FOLDERS       = true;
    /** Scale factor of file table icons. */
    public static final String  TABLE_ICON_SCALE                  = FILE_TABLE_SECTION + '.' + "icon_scale";
    /** Default scale factor of file table icons. */
    public static final float   DEFAULT_TABLE_ICON_SCALE          = 1.0f;
    /** Whether or not columns should resize themselves automatically. */
    public static final String  AUTO_SIZE_COLUMNS                 = FILE_TABLE_SECTION + '.' + "auto_size_columns";
    /** Default columns auto-resizing behavior. */
    public static final boolean DEFAULT_AUTO_SIZE_COLUMNS         = true;
    /** Controls if and when system file icons should be used instead of custom file icons */
    public static final String  USE_SYSTEM_FILE_ICONS             = FILE_TABLE_SECTION + '.' + "use_system_file_icons";
    /** Default system file icons policy */
    public static final String  DEFAULT_USE_SYSTEM_FILE_ICONS     = FileIcons.USE_SYSTEM_ICONS_APPLICATIONS;
    /** Controls whether folders are displayed first in the FileTable or mixed with regular files */
    public static final String  SHOW_FOLDERS_FIRST                = FILE_TABLE_SECTION + '.' + "show_folders_first";
    /** Default value for 'Show folders first' option */
    public static final boolean DEFAULT_SHOW_FOLDERS_FIRST        = true;
    /** Controls whether symlinks should be followed when changing directory */
    public static final String  CD_FOLLOWS_SYMLINKS               = FILE_TABLE_SECTION + '.' + "cd_follows_symlinks";
    /** Default value for 'Follow symlinks when changing directory' option */
    public static final boolean DEFAULT_CD_FOLLOWS_SYMLINKS       = false;
    /** Section describing the left table's configuration. */
    public static final String  LEFT_FILE_TABLE_SECTION            = FILE_TABLE_SECTION + '.' + "left";
    /** Section describing the right table's configuration. */
    public static final String  RIGHT_FILE_TABLE_SECTION           = FILE_TABLE_SECTION + '.' + "right";
    /** Identifier of the extension column. */
    public static final String  EXTENSION_COLUMN                   = "extension";
    /** Identifier of the name column. */
    public static final String  NAME_COLUMN                        = "name";
    /** Identifier of the date column. */
    public static final String  DATE_COLUMN                        = "date";
    /** Identifier of the size column. */
    public static final String  SIZE_COLUMN                        = "size";
    /** Identifier of the permissions column. */
    public static final String  PERMISSIONS_COLUMN                 = "permissions";
    /** Name of the 'show column' variable. */
    public static final String  SHOW_COLUMN                        = "show";
    /** Name of the 'column position' variable. */
    public static final String  COLUMN_POSITION                    = "position";
    /** Name of the 'column width' variable. */
    public static final String  COLUMN_WIDTH                       = "width";
    /** Section containing the left extension column configuration. */
    public static final String  LEFT_EXTENSION_SECTION             = LEFT_FILE_TABLE_SECTION + '.' + EXTENSION_COLUMN;
    /** Section containing the left name column configuration. */
    public static final String  LEFT_NAME_SECTION                  = LEFT_FILE_TABLE_SECTION + '.' + NAME_COLUMN;
    /** Section containing the left size column configuration. */
    public static final String  LEFT_SIZE_SECTION                  = LEFT_FILE_TABLE_SECTION + '.' + SIZE_COLUMN;
    /** Section containing the left date column configuration. */
    public static final String  LEFT_DATE_SECTION                  = LEFT_FILE_TABLE_SECTION + '.' + DATE_COLUMN;
    /** Section containing the left permissions column configuration. */
    public static final String  LEFT_PERMISSIONS_SECTION           = LEFT_FILE_TABLE_SECTION + '.' + PERMISSIONS_COLUMN;
    /** Section containing the right extension column configuration. */
    public static final String  RIGHT_EXTENSION_SECTION            = RIGHT_FILE_TABLE_SECTION + '.' + EXTENSION_COLUMN;
    /** Section containing the right name column configuration. */
    public static final String  RIGHT_NAME_SECTION                 = RIGHT_FILE_TABLE_SECTION + '.' + NAME_COLUMN;
    /** Section containing the right size column configuration. */
    public static final String  RIGHT_SIZE_SECTION                 = RIGHT_FILE_TABLE_SECTION + '.' + SIZE_COLUMN;
    /** Section containing the right date column configuration. */
    public static final String  RIGHT_DATE_SECTION                 = RIGHT_FILE_TABLE_SECTION + '.' + DATE_COLUMN;
    /** Section containing the right permissions column configuration. */
    public static final String  RIGHT_PERMISSIONS_SECTION          = RIGHT_FILE_TABLE_SECTION + '.' + PERMISSIONS_COLUMN;
    /** Default value for the 'show extensions' variable. */
    public static final boolean DEFAULT_SHOW_EXTENSION             = true;
    /** Default value for the 'show size' variable. */
    public static final boolean DEFAULT_SHOW_SIZE                  = true;
    /** Default value for the 'show date' variable. */
    public static final boolean DEFAULT_SHOW_DATE                  = true;
    /** Default value for the 'show permissions' variable. */
    public static final boolean DEFAULT_SHOW_PERMISSIONS           = true;
    /** Controls the width of the 'extension' column in the left table. */
    public static final String  LEFT_EXTENSION_WIDTH               = LEFT_EXTENSION_SECTION + '.' + COLUMN_WIDTH;
    /** Controls the width of the 'date' column in the left table. */
    public static final String  LEFT_DATE_WIDTH                    = LEFT_DATE_SECTION + '.' + COLUMN_WIDTH;
    /** Controls the width of the 'size' column in the left table. */
    public static final String  LEFT_SIZE_WIDTH                    = LEFT_SIZE_SECTION + '.' + COLUMN_WIDTH;
    /** Controls the width of the 'permissions' column in the left table. */
    public static final String  LEFT_PERMISSIONS_WIDTH             = LEFT_PERMISSIONS_SECTION + '.' + COLUMN_WIDTH;
    /** Controls the width of the 'extension' column in the right table. */
    public static final String  RIGHT_EXTENSION_WIDTH              = RIGHT_EXTENSION_SECTION + '.' + COLUMN_WIDTH;
    /** Controls the width of the 'date' column in the right table. */
    public static final String  RIGHT_DATE_WIDTH                   = RIGHT_DATE_SECTION + '.' + COLUMN_WIDTH;
    /** Controls the width of the 'size' column in the right table. */
    public static final String  RIGHT_SIZE_WIDTH                   = RIGHT_SIZE_SECTION + '.' + COLUMN_WIDTH;
    /** Controls the width of the 'permissions' column in the right table. */
    public static final String  RIGHT_PERMISSIONS_WIDTH            = RIGHT_PERMISSIONS_SECTION + '.' + COLUMN_WIDTH;
    /** Controls the position of the 'extension' column in the left table. */
    public static final String  LEFT_EXTENSION_POSITION            = LEFT_EXTENSION_SECTION + '.' + COLUMN_POSITION;
    /** Controls the position of the 'name' column in the left table. */
    public static final String  LEFT_NAME_POSITION                 = LEFT_NAME_SECTION + '.' + COLUMN_POSITION;
    /** Controls the position of the 'size' column in the left table. */
    public static final String  LEFT_SIZE_POSITION                 = LEFT_SIZE_SECTION + '.' + COLUMN_POSITION;
    /** Controls the position of the 'date' column in the left table. */
    public static final String  LEFT_DATE_POSITION                 = LEFT_DATE_SECTION + '.' + COLUMN_POSITION;
    /** Controls the position of the 'permissions' column in the left table. */
    public static final String  LEFT_PERMISSIONS_POSITION          = LEFT_PERMISSIONS_SECTION + '.' + COLUMN_POSITION;
    /** Controls the position of the 'extension' column in the right table. */
    public static final String  RIGHT_EXTENSION_POSITION           = RIGHT_EXTENSION_SECTION + '.' + COLUMN_POSITION;
    /** Controls the position of the 'name' column in the right table. */
    public static final String  RIGHT_NAME_POSITION                = RIGHT_NAME_SECTION + '.' + COLUMN_POSITION;
    /** Controls the position of the 'size' column in the right table. */
    public static final String  RIGHT_SIZE_POSITION                = RIGHT_SIZE_SECTION + '.' + COLUMN_POSITION;
    /** Controls the position of the 'date' column in the right table. */
    public static final String  RIGHT_DATE_POSITION                = RIGHT_DATE_SECTION + '.' + COLUMN_POSITION;
    /** Controls the position of the 'permissions' column in the right table. */
    public static final String  RIGHT_PERMISSIONS_POSITION         = RIGHT_PERMISSIONS_SECTION + '.' + COLUMN_POSITION;
    /** Controls whether the extension column is visible in the left table. */
    public static final String  SHOW_LEFT_EXTENSION                = LEFT_EXTENSION_SECTION + '.' + SHOW_COLUMN;
    /** Controls whether the size column is visible in the left table. */
    public static final String  SHOW_LEFT_SIZE                     = LEFT_SIZE_SECTION + '.' + SHOW_COLUMN;
    /** Controls whether the date column is visible in the left table. */
    public static final String  SHOW_LEFT_DATE                     = LEFT_DATE_SECTION + '.' + SHOW_COLUMN;
    /** Controls whether the permissions column is visible in the left table. */
    public static final String  SHOW_LEFT_PERMISSIONS              = LEFT_PERMISSIONS_SECTION + '.' + SHOW_COLUMN;
    /** Controls whether the extension column is visible in the right table. */
    public static final String  SHOW_RIGHT_EXTENSION               = RIGHT_EXTENSION_SECTION + '.' + SHOW_COLUMN;
    /** Controls whether the size column is visible in the right table. */
    public static final String  SHOW_RIGHT_SIZE                    = RIGHT_SIZE_SECTION + '.' + SHOW_COLUMN;
    /** Controls whether the date column is visible in the right table. */
    public static final String  SHOW_RIGHT_DATE                    = RIGHT_DATE_SECTION + '.' + SHOW_COLUMN;
    /** Controls whether the permissions column is visible in the right table. */
    public static final String  SHOW_RIGHT_PERMISSIONS             = RIGHT_PERMISSIONS_SECTION + '.' + SHOW_COLUMN;



    // - Mac OS X variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing muCommander's Mac OS X integration. */
    public static final String  MAC_OSX_SECTION                   = "macosx";
    /** Whether or not to use the brushed metal look. */
    public static final String  USE_BRUSHED_METAL                 = MAC_OSX_SECTION + '.' + "brushed_metal_look";
    /** Default brushed metal look behavior. */
    public static final boolean DEFAULT_USE_BRUSHED_METAL         = true;
    /** Whether or not to use a Mac OS X style menu bar. */
    public static final String  USE_SCREEN_MENU_BAR               = MAC_OSX_SECTION + '.' + "screen_menu_bar";
    /** Default menu bar type. */
    public static final boolean DEFAULT_USE_SCREEN_MENU_BAR       = true;



    // - Startup folder variables --------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing muCommander's Mac OS X integration. */
    public static final String  STARTUP_FOLDER_SECTION            = "startup_folder";
    /** Section describing the right panel's startup folder. */
    public static final String  RIGHT_STARTUP_FOLDER_SECTION      = STARTUP_FOLDER_SECTION + '.' + "right";
    /** Section describing the left panel's startup folder. */
    public static final String  LEFT_STARTUP_FOLDER_SECTION       = STARTUP_FOLDER_SECTION + '.' + "left";
    /** Name for variables that describe the last visited folder of a panel. */
    public static final String  LAST_FOLDER                       = "last_folder";
    /** Last visited folder in the left panel. */
    public static final String  LAST_LEFT_FOLDER                  = LEFT_STARTUP_FOLDER_SECTION + '.' + LAST_FOLDER;
    /** Last visited folder in the right panel. */
    public static final String  LAST_RIGHT_FOLDER                 = RIGHT_STARTUP_FOLDER_SECTION + '.' + LAST_FOLDER;
    /** Path to a custom startup folder. */
    public static final String  CUSTOM_FOLDER                     = "custom_folder";
    /** Path to the left panel custom startup folder. */
    public static final String  LEFT_CUSTOM_FOLDER                = LEFT_STARTUP_FOLDER_SECTION + '.' + CUSTOM_FOLDER;
    /** Path to the right panel custom startup folder. */
    public static final String  RIGHT_CUSTOM_FOLDER               = RIGHT_STARTUP_FOLDER_SECTION + '.' + CUSTOM_FOLDER;
    /** Startup folder type. */
    public static final String  STARTUP_FOLDER                    = "on_startup";
    /** The custom folder should be used on startup. */
    public static final String  STARTUP_FOLDER_CUSTOM             = "customFolder";
    /** The last visited folder should be used on startup. */
    public static final String  STARTUP_FOLDER_LAST               = "lastFolder";
    /** Type of startup folder that should be used in the left panel. */
    public static final String  LEFT_STARTUP_FOLDER               = LEFT_STARTUP_FOLDER_SECTION + '.' + STARTUP_FOLDER;
    /** Type of startup folder that should be used in the right panel. */
    public static final String  RIGHT_STARTUP_FOLDER              = RIGHT_STARTUP_FOLDER_SECTION + '.' + STARTUP_FOLDER;
    /** Default startup folder type. */
    public static final String  DEFAULT_STARTUP_FOLDER            = STARTUP_FOLDER_LAST;



    // - Last window variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing known information about the last muCommander window. */
    public static final String LAST_WINDOW_SECTION                = "last_window";
    /** Last muCommander known x position. */
    public static final String LAST_X                             = LAST_WINDOW_SECTION + '.' + "x";
    /** Last muCommander known y position. */
    public static final String LAST_Y                             = LAST_WINDOW_SECTION + '.' + "y";
    /** Last muCommander known width. */
    public static final String LAST_WIDTH                         = LAST_WINDOW_SECTION + '.' + "width";
    /** Last muCommander known height. */
    public static final String LAST_HEIGHT                        = LAST_WINDOW_SECTION + '.' + "height";
    /** Last known screen width. */
    public static final String SCREEN_WIDTH                       = LAST_WINDOW_SECTION + '.' + "screen_width";
    /** Last known screen height. */
    public static final String SCREEN_HEIGHT                      = LAST_WINDOW_SECTION + '.' + "screen_height";
    /** Last orientation used to split folder panels */
    public static final String SPLIT_ORIENTATION                  = LAST_WINDOW_SECTION + '.' + "split_orientation";
    /** Vertical split pane orientation. */
    public static final String VERTICAL_SPLIT_ORIENTATION         = "vertical";
    /** Horizontal split pane orientation. */
    public static final String HORIZONTAL_SPLIT_ORIENTATION       = "horizontal";
    /** Default split pane orientation. */
    public static final String DEFAULT_SPLIT_ORIENTATION          = VERTICAL_SPLIT_ORIENTATION;



    // - Folder monitoring variables -----------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the automatic folder refresh behavior. */
    public static final String REFRESH_SECTION                    = "auto_refresh";
    /** Frequency at which the current folder is checked for updates. */
    public static final String REFRESH_CHECK_PERIOD               = REFRESH_SECTION + '.' + "check_period";
    /** Default folder refresh frequency. */
    public static final long   DEFAULT_REFRESH_CHECK_PERIOD       = 3000;
    /** Minimum amount of time a folder should be checked for updates after it's been refreshed. */
    public static final String WAIT_AFTER_REFRESH                 = REFRESH_SECTION + '.' + "wait_after_refresh";
    /** Default minimum amount of time between two refreshes. */
    public static final long   DEFAULT_WAIT_AFTER_REFRESH         = 10000;



    // - Progress dialog variables -------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the behavior of the progress dialog. */
    public static final String  PROGRESS_DIALOG_SECTION           = "progress_dialog";
    /** Whether the progress dialog is expanded or not. */
    public static final String  PROGRESS_DIALOG_EXPANDED          = PROGRESS_DIALOG_SECTION + '.' + "expanded";
    /** Default progress dialog expanded state. */
    public static final boolean DEFAULT_PROGRESS_DIALOG_EXPANDED  = true;
    /** Controls whether or not the progress dialog should be closed when the job is finished. */
    public static final String PROGRESS_DIALOG_CLOSE_WHEN_FINISHED = PROGRESS_DIALOG_SECTION + '.' + "close_when_finished";
    /** Default progress dialog behavior when the job is finished. */
    public static final boolean DEFAULT_PROGRESS_DIALOG_CLOSE_WHEN_FINISHED  = true;



    // - Variables used for caches -------------------------------------------
    // -----------------------------------------------------------------------
    /** Section controlling the caching mechanisms used throughout the application */
    public static final String  CACHE_SECTION                     = "cache";
    /** Capacity of the AbstractFile instances cache */
    public static final String  FILE_CACHE_CAPACITY               = CACHE_SECTION + '.' + "file_cache_capacity";
    /** Default capacity of the AbstractFile instances cache */
    public static final int     DEFAULT_FILE_CACHE_CAPACITY       = 1000;
    /** Capacity of the system file icon cache */
    public static final String  SYSTEM_ICON_CACHE_CAPACITY       = CACHE_SECTION + '.' + "system_icon_cache_capacity";
    /** Default capacity of the system file icon cache */
    public static final int     DEFAULT_SYSTEM_ICON_CACHE_CAPACITY = 100;



    // - Variables used for themes -------------------------------------------
    // -----------------------------------------------------------------------
    /** Section controlling which theme should be applied to muCommander. */
    public static final String THEME_SECTION                      = "theme";
    /** Current theme type (custom, predefined or user defined). */
    public static final String THEME_TYPE                         = THEME_SECTION + '.' + "type";
    /** Describes predefined themes. */
    public static final String THEME_PREDEFINED                   = "predefined";
    /** Describes custom themes. */
    public static final String THEME_CUSTOM                       = "custom";
    /** Describes the user theme. */
    public static final String THEME_USER                         = "user";
    /** Default theme type. */
    public static final String DEFAULT_THEME_TYPE                 = THEME_PREDEFINED;
    /** Name of the current theme. */
    public static final String THEME_NAME                         = THEME_SECTION + '.' + "path";
    /** Default current theme name. */
    public static final String DEFAULT_THEME_NAME                 = RuntimeConstants.DEFAULT_THEME;



    // - Variables used by Bonjour/Zeroconf support --------------------------
    // -----------------------------------------------------------------------
    /** Section controlling parameters related to Bonjour/Zeroconf support */
    public static final String  BONJOUR_SECTION                   = "bonjour";
    /** Used do determine whether discovery of Bonjour services should be activated or not */
    public static final String  ENABLE_BONJOUR_DISCOVERY          = BONJOUR_SECTION + '.' + "discovery_enabled";
    /** Default Bonjour discovery activation used on startup */
    public static final boolean DEFAULT_ENABLE_BONJOUR_DISCOVERY  = true;



    // - Variables used for the custom editor --------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the editor configuration. */
    public static final String  EDITOR_SECTION                    = "editor";
    /** Command to use as custom editor. */
    public static final String  CUSTOM_EDITOR                     = EDITOR_SECTION + '.' + "custom_command";
    /** Whether or not to use the custom editor. */
    public static final String  USE_CUSTOM_EDITOR                 = EDITOR_SECTION + '.' + "use_custom";
    /** Default value for {@link #USE_CUSTOM_EDITOR}. */
    public static final boolean DEFAULT_USE_CUSTOM_EDITOR         = false;



    // - Variables used for the custom viewer --------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the viewer configuration. */
    public static final String  VIEWER_SECTION                    = "viewer";
    /** Command to use as custom viewer. */
    public static final String  CUSTOM_VIEWER                     = VIEWER_SECTION + '.' + "custom_command";
    /** Whether or not to use the custom viewer. */
    public static final String  USE_CUSTOM_VIEWER                 = VIEWER_SECTION + '.' + "use_custom";
    /** Default value for {@link #USE_CUSTOM_VIEWER}. */
    public static final boolean DEFAULT_USE_CUSTOM_VIEWER         = false;



    // - Variables used for FTP ----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section containing all FTP variables. */
    public static final String FTP_SECTION                        = "ftp";
    /** Controls whether hidden files should be listed by the client (LIST -al instead of LIST -l) */
    public static final String LIST_HIDDEN_FILES                  = FTP_SECTION + '.' + "list_hidden_files";
    /** Default value for {@link #LIST_HIDDEN_FILES} */
    public static final boolean DEFAULT_LIST_HIDDEN_FILES         = false;



    // - Instance fields -----------------------------------------------------
    // -----------------------------------------------------------------------
    public static Configuration configuration = new Configuration(new MuConfigurationSource());



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    private MuConfiguration() {}



    // - Configuration reading / writing -------------------------------------
    // -----------------------------------------------------------------------
    public static void read() throws IOException, ConfigurationException {
        String configurationVersion;

        try {configuration.read();}
        finally {
            configurationVersion = getVariable(VERSION);
            if(configurationVersion == null || !configurationVersion.equals(RuntimeConstants.VERSION)) {
                renameVariable("show_hidden_files", SHOW_HIDDEN_FILES);
                renameVariable("auto_size_columns", AUTO_SIZE_COLUMNS);
                renameVariable("show_toolbar",      TOOLBAR_VISIBLE);
                renameVariable("show_status_bar",   STATUS_BAR_VISIBLE);
                renameVariable("show_command_bar",  COMMAND_BAR_VISIBLE);
                setVariable(VERSION, RuntimeConstants.VERSION);
            }

            // Initialises mac os x specific values
            if(PlatformManager.getOsFamily() == PlatformManager.MAC_OS_X) {
                if(getVariable(SHELL_ENCODING) == null) {
                    setVariable(SHELL_ENCODING, "UTF-8");
                    setVariable(AUTODETECT_SHELL_ENCODING, false);
                }
            }
        }
    }

    public static void write() throws IOException, ConfigurationException {configuration.write();}


    // - Variable setting ------------------------------------------------------
    // -------------------------------------------------------------------------
    public static void renameVariable(String fromVar, String toVar) {configuration.renameVariable(fromVar, toVar);}

    public static boolean setVariable(String name, String value) {return configuration.setVariable(name, value);}

    public static boolean setVariable(String name, int value) {return configuration.setVariable(name, value);}

    public static boolean setVariable(String name, float value) {return configuration.setVariable(name, value);}

    public static boolean setVariable(String name, boolean value) {return configuration.setVariable(name, value);}

    public static boolean setVariable(String name, long value) {return configuration.setVariable(name, value);}

    public static boolean setVariable(String name, double value) {return configuration.setVariable(name, value);}



    // - Variable retrieval ----------------------------------------------------
    // -------------------------------------------------------------------------
    public static String getVariable(String name) {return configuration.getVariable(name);}

    public static int getIntegerVariable(String name) {return configuration.getIntegerVariable(name);}

    public static long getLongVariable(String name) {return configuration.getLongVariable(name);}

    public static float getFloatVariable(String name) {return configuration.getFloatVariable(name);}

    public static double getDoubleVariable(String name) {return configuration.getDoubleVariable(name);}

    public static boolean getBooleanVariable(String name) {return configuration.getBooleanVariable(name);}

    public static boolean isVariableSet(String name) {return configuration.isVariableSet(name);}



    // - Variable removal ------------------------------------------------------
    // -------------------------------------------------------------------------
    public static String removeVariable(String name) {return configuration.removeVariable(name);}

    public static int removeIntegerVariable(String name) {return configuration.removeIntegerVariable(name);}

    public static long removeLongVariable(String name) {return configuration.removeLongVariable(name);}

    public static float removeFloatVariable(String name) {return configuration.removeFloatVariable(name);}

    public static double removeDoubleVariable(String name) {return configuration.removeDoubleVariable(name);}

    public static boolean removeBooleanVariable(String name) {return configuration.removeBooleanVariable(name);}



    // - Advanced variable retrieval -------------------------------------------
    // -------------------------------------------------------------------------
    public static String getVariable(String name, String defaultValue) {return configuration.getVariable(name, defaultValue);}

    public static int getVariable(String name, int defaultValue) {return configuration.getVariable(name, defaultValue);}

    public static long getVariable(String name, long defaultValue) {return configuration.getVariable(name, defaultValue);}

    public static float getVariable(String name, float defaultValue) {return configuration.getVariable(name, defaultValue);}

    public static boolean getVariable(String name, boolean defaultValue) {return configuration.getVariable(name, defaultValue);}

    public static double getVariable(String name, double defaultValue) {return configuration.getVariable(name, defaultValue);}


    // - Configuration listening -----------------------------------------------
    // -------------------------------------------------------------------------
    public static void addConfigurationListener(ConfigurationListener listener) {configuration.addConfigurationListener(listener);}

    public static void removeConfigurationListener(ConfigurationListener listener) {configuration.removeConfigurationListener(listener);}


    // - Configuration source --------------------------------------------------
    // -------------------------------------------------------------------------
    public static void setConfigurationFile(String file) {configuration.setSource(new MuConfigurationSource(file));}

}
