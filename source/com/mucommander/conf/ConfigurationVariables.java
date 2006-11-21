package com.mucommander.conf;

/**
 * Aggregates all of muCommander's configuration variables in one place.
 * @author Nicolas Rinaudo
 */
public interface ConfigurationVariables {
    // - Misc. variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Global section. */
    public static final String  ROOT_SECTION             = "prefs";
    /** <i>Check for update on startup</i> configuration variable. */
    public static final String  CHECK_FOR_UPDATE         = ROOT_SECTION + '.' + "check_for_updates_on_startup";
    /** Default value of the <i>check for update on startup</i> configuration variable. */
    public static final boolean DEFAULT_CHECK_FOR_UPDATE = true;
    /** <i>Date format</i> configuration variable. */
    public static final String  DATE_FORMAT              = ROOT_SECTION + '.' + "date_format";
    /** Default value of the <i>date format</i> configuration variable. */
    public static final String  DEFAULT_DATE_FORMAT      = "MM/dd/yy";
    /** <i>Date separator</i> configuration variable. */
    public static final String  DATE_SEPARATOR           = ROOT_SECTION + '.' + "date_separator";
    /** Default value of the <i>date separator</i> configuration variable. */
    public static final String  DEFAULT_DATE_SEPARATOR   = "/";
    /** <i>Time format</i> configuration variable .*/
    public static final String  TIME_FORMAT              = ROOT_SECTION + '.' + "time_format";
    /** Default value for the <i>time format</i> configuration variable. */
    public static final String  DEFAULT_TIME_FORMAT      = "hh:mm a";
    /** <i>Language</i> configuration variable. */
    public static final String  LANGUAGE                 = ROOT_SECTION + '.' + "language";



    // - Shell variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the shell configuration. */
    public static final String  SHELL_SECTION              = ROOT_SECTION + '.' + "shell";
    /** <i>Custom shell</i> configuration variable. */
    public static final String  CUSTOM_SHELL               = SHELL_SECTION + '.' + "custom_command";
    /** <i>Use custom shell</i> configuration variable. */
    public static final String  USE_CUSTOM_SHELL           = SHELL_SECTION + '.' + "use_custom";
    /** Default value of the <i>use custom shell</i> configuration variable. */
    public static final boolean DEFAULT_USE_CUSTOM_SHELL   = false;
    /** <i>Shell history size</i> configuration variable. */
    public static final String  SHELL_HISTORY_SIZE         = SHELL_SECTION + '.' + "history_size";
    /** Default value for the <i>shell history size</i> configuration variable. */
    public static final int     DEFAULT_SHELL_HISTORY_SIZE = 100;



    // - Mail variables ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing mail configuration. */
    public static final String MAIL_SECTION        = ROOT_SECTION + '.' + "mail";
    /** <i>SMTP server</i> configuration variable. */
    public static final String SMTP_SERVER         = MAIL_SECTION + '.' + "smtp_server";
    /** <i>Sender name</i> configuration variable. */
    public static final String MAIL_SENDER_NAME    = MAIL_SECTION + '.' + "sender_name";
    /** <i>Sender address</i> configuration variable. */
    public static final String MAIL_SENDER_ADDRESS = MAIL_SECTION + '.' + "sender_address";



    // - Command bar variables -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the command bar configuration. */
    public static final String  COMMAND_BAR_SECTION            = ROOT_SECTION + '.' + "command_bar";
    /** <i>Command bar visible</i> configuration variable. */
    public static final String  COMMAND_BAR_VISIBLE            = COMMAND_BAR_SECTION + '.' + "visible";
    /** Default value for the <i>command bar visible</i> configuration variable. */
    public static final boolean DEFAULT_COMMAND_BAR_VISIBLE    = true;
    /** <i>Command bar icon scale</i> configuration variable. */
    public static final String  COMMAND_BAR_ICON_SCALE         = COMMAND_BAR_SECTION + '.' + "icon_scale";
    /** Default value for the <i>command bar icon scale</i> configuration variable. */
    public static final float   DEFAULT_COMMAND_BAR_ICON_SCALE = 1.0f;



    // - Status bar variables ------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the status bar configuration. */
    public static final String STATUS_BAR_SECTION          = ROOT_SECTION + '.' + "status_bar";
    /** <i>Status bar visible</i> configuration variable. */
    public static final String STATUS_BAR_VISIBLE          = STATUS_BAR_SECTION + '.' + "visible";
    /** Default value for the <i>status bar visible</i> configuration variable. */
    public static final boolean DEFAULT_STATUS_BAR_VISIBLE = true;



    // - Toolbar variables ---------------------------------------------------
    // -----------------------------------------------------------------------
    /** Section describing the toolbar configuration. */
    public static final String TOOLBAR_SECTION          = ROOT_SECTION + '.' + "toolbar";
    /** <i>Toolbar visible</i> configuration variable. */
    public static final String TOOLBAR_VISIBLE          = TOOLBAR_SECTION + '.' + "visible";
    /** Default value for the <i>toolbar visible</i> configuration variable. */
    public static final boolean DEFAULT_TOOLBAR_VISIBLE = true;
}
