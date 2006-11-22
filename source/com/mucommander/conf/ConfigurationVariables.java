package com.mucommander.conf;

/**
 * Aggregates all of muCommander's configuration variables in one place.
 * @author Nicolas Rinaudo
 */
public interface ConfigurationVariables {
    // - Misc. variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Global section. */
    public static final String  ROOT_SECTION                   = "prefs";
    /** Whether or not to automaticaly check for updates on startup. */
    public static final String  CHECK_FOR_UPDATE               = ROOT_SECTION + '.' + "check_for_updates_on_startup";
    /** Default automated update behavior. */
    public static final boolean DEFAULT_CHECK_FOR_UPDATE       = true;
    /** Description of the format dates should be displayed with. */
    public static final String  DATE_FORMAT                    = ROOT_SECTION + '.' + "date_format";
    /** Default date format. */
    public static final String  DEFAULT_DATE_FORMAT            = "MM/dd/yy";
    /** Character used to separate years, months and days in a date. */
    public static final String  DATE_SEPARATOR                 = ROOT_SECTION + '.' + "date_separator";
    /** Default date separator. */
    public static final String  DEFAULT_DATE_SEPARATOR         = "/";
    /** Description of the format timestamps should be displayed with.*/
    public static final String  TIME_FORMAT                    = ROOT_SECTION + '.' + "time_format";
    /** Default time format. */
    public static final String  DEFAULT_TIME_FORMAT            = "hh:mm a";
    /** Language muCommander should use when looking for text.. */
    public static final String  LANGUAGE                       = ROOT_SECTION + '.' + "language";
    /** muCommander's version at time of writing the configuration file. */
    public static final String  VERSION                        = ROOT_SECTION + '.' + "conf_version";



    // - Shell variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the shell configuration. */
    public static final String  SHELL_SECTION                  = ROOT_SECTION + '.' + "shell";
    /** Shell invocation command (in case muCommander is not using the default one). */
    public static final String  CUSTOM_SHELL                   = SHELL_SECTION + '.' + "custom_command";
    /** Whether or not to use a custom shell invocation command. */
    public static final String  USE_CUSTOM_SHELL               = SHELL_SECTION + '.' + "use_custom";
    /** Default custom shell behavior. */
    public static final boolean DEFAULT_USE_CUSTOM_SHELL       = false;
    /** Maximum number of items that should be present in the shell history. */
    public static final String  SHELL_HISTORY_SIZE             = SHELL_SECTION + '.' + "history_size";
    /** Default maximum shell history size. */
    public static final int     DEFAULT_SHELL_HISTORY_SIZE     = 100;



    // - Mail variables ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing mail configuration. */
    public static final String MAIL_SECTION                    = ROOT_SECTION + '.' + "mail";
    /** Address of the SMTP server that should be used when sending mails. */
    public static final String SMTP_SERVER                     = MAIL_SECTION + '.' + "smtp_server";
    /** Name under which mails sent by muCommander should appear. */
    public static final String MAIL_SENDER_NAME                = MAIL_SECTION + '.' + "sender_name";
    /** Address which mails sent by muCommander should be replied to. */
    public static final String MAIL_SENDER_ADDRESS             = MAIL_SECTION + '.' + "sender_address";



    // - Command bar variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the command bar configuration. */
    public static final String  COMMAND_BAR_SECTION            = ROOT_SECTION + '.' + "command_bar";
    /** Whether or not the command bar is visible. */
    public static final String  COMMAND_BAR_VISIBLE            = COMMAND_BAR_SECTION + '.' + "visible";
    /** Default command bar visibility. */
    public static final boolean DEFAULT_COMMAND_BAR_VISIBLE    = true;
    /** Scale factor of commandbar icons. */
    public static final String  COMMAND_BAR_ICON_SCALE         = COMMAND_BAR_SECTION + '.' + "icon_scale";
    /** Default scale factor of commandbar icons. */
    public static final float   DEFAULT_COMMAND_BAR_ICON_SCALE = 1.0f;



    // - Status bar variables ------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the status bar configuration. */
    public static final String STATUS_BAR_SECTION              = ROOT_SECTION + '.' + "status_bar";
    /** Whether or not the status bar is visible. */
    public static final String STATUS_BAR_VISIBLE              = STATUS_BAR_SECTION + '.' + "visible";
    /** Default status bar visibility. */
    public static final boolean DEFAULT_STATUS_BAR_VISIBLE     = true;



    // - Toolbar variables ---------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the toolbar configuration. */
    public static final String TOOLBAR_SECTION                 = ROOT_SECTION + '.' + "toolbar";
    /** Whether or not the toolbar is visible. */
    public static final String TOOLBAR_VISIBLE                 = TOOLBAR_SECTION + '.' + "visible";
    /** Default toolbar visibility. */
    public static final boolean DEFAULT_TOOLBAR_VISIBLE        = true;
    /** Scale factor of toolbar icons. */
    public static final String  TOOLBAR_ICON_SCALE             = TOOLBAR_SECTION + '.' + "icon_scale";
    /** Default scale factor of toolbar icons. */
    public static final float   DEFAULT_TOOLBAR_ICON_SCALE     = 1.0f;




    // - Folders variables ---------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the folders view configuration. */
    public static final String  FILE_TABLE_SECTION             = ROOT_SECTION + '.' + "file_table";
    /** Whether or not to display hidden files. */
    public static final String  SHOW_HIDDEN_FILES              = FILE_TABLE_SECTION + '.' + "show_hidden_files";
    /** Default hidden files visibility. */
    public static final boolean DEFAULT_SHOW_HIDDEN_FILES      = true;
    /** Whether or not to display OS X .DS_Store files. */
    public static final String  SHOW_DS_STORE_FILES            = FILE_TABLE_SECTION + '.' + "show_ds_store_files";
    /** Default .DS_Store files visibility. */
    public static final boolean DEFAULT_SHOW_DS_STORE_FILES    = true;
    /** Whether or not to display system folders. */
    public static final String  SHOW_SYSTEM_FOLDERS            = FILE_TABLE_SECTION + '.' + "show_system_folders";
    /** Default system folders visibility. */
    public static final boolean DEFAULT_SHOW_SYSTEM_FOLDERS    = true;
    /** Scale factor of file table icons. */
    public static final String  TABLE_ICON_SCALE               = FILE_TABLE_SECTION + '.' + "icon_scale";
    /** Default scale factor of file table icons. */
    public static final float   DEFAULT_TABLE_ICON_SCALE       = 1.0f;
    /** Whether or not columns should resize themselves automatically. */
    public static final String  AUTO_SIZE_COLUMNS              = FILE_TABLE_SECTION + '.' + "auto_size_columns";
    /** Default columns auto-resizing behavior. */
    public static final boolean DEFAULT_AUTO_SIZE_COLUMNS      = true;



    // - Color variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing muCommander's various colors. */
    public static final String COLORS_SECTION                  = ROOT_SECTION + '.' + "colors";
    /** Color used for the background of the folder views. */
    public static final String BACKGROUND_COLOR                = COLORS_SECTION + '.' + "background";
    /** Default folder views background colors. */
    public static final String DEFAULT_BACKGROUND_COLOR        = "000084";



    // - Mac OS X variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing muCommander's Mac OS X integration. */
    public static final String  MAC_OSX_SECTION                = ROOT_SECTION + '.' + "macosx";
    /** Whether or not to use the brushed metal look. */
    public static final String  USE_BRUSHED_METAL              = MAC_OSX_SECTION + '.' + "brushed_metal_look";
    /** Default brushed metal look behavior. */
    public static final boolean DEFAULT_USE_BRUSHED_METAL      = true;
    /** Whether or not to use a Mac OS X style menu bar. */
    public static final String  USE_SCREEN_MENU_BAR            = MAC_OSX_SECTION + '.' + "screen_menu_bar";
    /** Default menu bar type. */
    public static final boolean DEFAULT_USE_SCREEN_MENU_BAR    = true;
}
