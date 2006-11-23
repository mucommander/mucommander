package com.mucommander.conf;

/**
 * Aggregates all of muCommander's configuration variables in one place.
 * @author Nicolas Rinaudo
 */
public interface ConfigurationVariables {
    // - Misc. variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Global section. */
    public static final String  ROOT_SECTION                      = "prefs";
    /** Whether or not to automaticaly check for updates on startup. */
    public static final String  CHECK_FOR_UPDATE                  = ROOT_SECTION + '.' + "check_for_updates_on_startup";
    /** Default automated update behavior. */
    public static final boolean DEFAULT_CHECK_FOR_UPDATE          = true;
    /** Description of the format dates should be displayed with. */
    public static final String  DATE_FORMAT                       = ROOT_SECTION + '.' + "date_format";
    /** Default date format. */
    public static final String  DEFAULT_DATE_FORMAT               = "MM/dd/yy";
    /** Character used to separate years, months and days in a date. */
    public static final String  DATE_SEPARATOR                    = ROOT_SECTION + '.' + "date_separator";
    /** Default date separator. */
    public static final String  DEFAULT_DATE_SEPARATOR            = "/";
    /** Description of the format timestamps should be displayed with.*/
    public static final String  TIME_FORMAT                       = ROOT_SECTION + '.' + "time_format";
    /** Default time format. */
    public static final String  DEFAULT_TIME_FORMAT               = "hh:mm a";
    /** Language muCommander should use when looking for text.. */
    public static final String  LANGUAGE                          = ROOT_SECTION + '.' + "language";
    /** muCommander's version at time of writing the configuration file. */
    public static final String  VERSION                           = ROOT_SECTION + '.' + "conf_version";
    /** Whether or not to display compact file sizes. */
    public static final String  DISPLAY_COMPACT_FILE_SIZE         = ROOT_SECTION + '.' + "display_compact_file_size";
    /** Default file size display behavior. */
    public static final boolean DEFAULT_DISPLAY_COMPACT_FILE_SIZE = true;
    /** Whether or not to ask the user for confirmation before quitting muCommander. */
    public static final String  CONFIRM_ON_QUIT                   = ROOT_SECTION + '.' + "quit_confirmation";
    /** Default quitting behavior. */
    public static final boolean DEFAULT_CONFIRM_ON_QUIT           = true;
    /** Look and feel used by muCommander. */
    public static final String  LOOK_AND_FEEL                     = ROOT_SECTION + '.' + "lookAndFeel";



    // - Shell variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the shell configuration. */
    public static final String  SHELL_SECTION                     = ROOT_SECTION + '.' + "shell";
    /** Shell invocation command (in case muCommander is not using the default one). */
    public static final String  CUSTOM_SHELL                      = SHELL_SECTION + '.' + "custom_command";
    /** Whether or not to use a custom shell invocation command. */
    public static final String  USE_CUSTOM_SHELL                  = SHELL_SECTION + '.' + "use_custom";
    /** Default custom shell behavior. */
    public static final boolean DEFAULT_USE_CUSTOM_SHELL          = false;
    /** Maximum number of items that should be present in the shell history. */
    public static final String  SHELL_HISTORY_SIZE                = SHELL_SECTION + '.' + "history_size";
    /** Default maximum shell history size. */
    public static final int     DEFAULT_SHELL_HISTORY_SIZE        = 100;



    // - Mail variables ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing mail configuration. */
    public static final String MAIL_SECTION                       = ROOT_SECTION + '.' + "mail";
    /** Address of the SMTP server that should be used when sending mails. */
    public static final String SMTP_SERVER                        = MAIL_SECTION + '.' + "smtp_server";
    /** Name under which mails sent by muCommander should appear. */
    public static final String MAIL_SENDER_NAME                   = MAIL_SECTION + '.' + "sender_name";
    /** Address which mails sent by muCommander should be replied to. */
    public static final String MAIL_SENDER_ADDRESS                = MAIL_SECTION + '.' + "sender_address";



    // - Command bar variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the command bar configuration. */
    public static final String  COMMAND_BAR_SECTION               = ROOT_SECTION + '.' + "command_bar";
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
    public static final String STATUS_BAR_SECTION                 = ROOT_SECTION + '.' + "status_bar";
    /** Whether or not the status bar is visible. */
    public static final String STATUS_BAR_VISIBLE                 = STATUS_BAR_SECTION + '.' + "visible";
    /** Default status bar visibility. */
    public static final boolean DEFAULT_STATUS_BAR_VISIBLE        = true;



    // - Toolbar variables ---------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the toolbar configuration. */
    public static final String TOOLBAR_SECTION                    = ROOT_SECTION + '.' + "toolbar";
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
    public static final String  FILE_TABLE_SECTION                = ROOT_SECTION + '.' + "file_table";
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



    // - Color variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing muCommander's various colors. */
    public static final String COLORS_SECTION                     = ROOT_SECTION + '.' + "colors";
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


    // - Mac OS X variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing muCommander's Mac OS X integration. */
    public static final String  MAC_OSX_SECTION                   = ROOT_SECTION + '.' + "macosx";
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
    public static final String  STARTUP_FOLDER_SECTION            = ROOT_SECTION + '.' + "startup_folder";
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



    // - Last window variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing known information about the last muCommander window. */
    public static final String LAST_WINDOW_SECTION                = ROOT_SECTION + '.' + "last_window";
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



    // - Folder monitoring variables -----------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the automatic folder refresh behavior. */
    public static final String REFRESH_SECTION                    = ROOT_SECTION + '.' + "auto_refresh";
    /** Frequency at which the current folder is checked for updates. */
    public static final String REFRESH_CHECK_PERIOD               = REFRESH_SECTION + '.' + "check_period";
    /** Default folder refresh frequency. */
    public static final long   DEFAULT_REFRESH_CHECK_PERIOD       = 3000;
    /** Minimum amount of time a folder should be checked for updates after it's been refreshed. */
    public static final String WAIT_AFTER_REFRESH                 = REFRESH_SECTION + '.' + "wait_after_refresh";
    /** Default minimum amount of time between two refreshes. */
    public static final long   DEFAULT_WAIT_AFTER_REFRESH         = 10000;



    // - Font variables ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the font used by muCommander. */
    public static final String FONT_SECTION                       = ROOT_SECTION + '.' + "font";
    /** Family of the font used by muCommander. */
    public static final String FONT_FAMILY                        = FONT_SECTION + '.' + "family";
    /** Size of the font used by muCommander. */
    public static final String FONT_SIZE                          = FONT_SECTION + '.' + "size";
    /** Style of the font used by muCommander. */
    public static final String FONT_STYLE                         = FONT_SECTION + '.' + "style";



    // - Progress dialog variables -------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the behavior of the progress dialog. */
    public static final String  PROGRESS_DIALOG_SECTION           = ROOT_SECTION + '.' + "progress_dialog";
    /** Whether the progress dialog is expanded or not. */
    public static final String  PROGRESS_DIALOG_EXPANDED          = PROGRESS_DIALOG_SECTION + '.' + "expanded";
    /** Default progress dialog behavior. */
    public static final boolean DEFAULT_PROGRESS_DIALOG_EXPANDED  = true;
}
