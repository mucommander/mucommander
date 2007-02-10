package com.mucommander.ui.theme;

/**
 * Defines the format of the XML theme files.
 * @author Nicolas Rinaudo
 */
interface ThemeXmlConstants {
    // - Main elements -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** XML theme file root element. */
    public static final String ELEMENT_ROOT                 = "theme";
    /** File table description element. */
    public static final String ELEMENT_TABLE                = "file_table";
    /** Shell description element. */
    public static final String ELEMENT_SHELL                = "shell";
    /** File editor description element. */
    public static final String ELEMENT_EDITOR               = "editor";
    /** Location bar description element. */
    public static final String ELEMENT_LOCATION_BAR         = "location_bar";
    /** Shell history description element. */
    public static final String ELEMENT_SHELL_HISTORY        = "shell_history";
    /** Volume label description element. */
    public static final String ELEMENT_STATUS_BAR           = "status_bar";



    // - Status element ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Item normal state description element. */
    public static final String ELEMENT_NORMAL               = "normal";
    /** Item selected state description element. */
    public static final String ELEMENT_SELECTED             = "selected";



    // - Font element --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Font description element. */
    public static final String ELEMENT_FONT                 = "font";
    /** Font family attribute. */
    public static final String ATTRIBUTE_FAMILY             = "family";
    /** Font size attribute. */
    public static final String ATTRIBUTE_SIZE               = "size";
    /** Font bold attribute. */
    public static final String ATTRIBUTE_BOLD               = "bold";
    /** Font italic attribute. */
    public static final String ATTRIBUTE_ITALIC             = "italic";
    /** <i>true</i> value. */
    public static final String VALUE_TRUE                   = "true";
    /** <i>false</i> value. */
    public static final String VALUE_FALSE                  = "false";



    // - Color elements ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public static final String ELEMENT_UNFOCUSED_BACKGROUND = "unfocused_background";
    public static final String ELEMENT_UNFOCUSED_FOREGROUND = "unfocused_foreground";
    public static final String ELEMENT_BACKGROUND           = "background";
    public static final String ELEMENT_FOREGROUND           = "foreground";
    public static final String ELEMENT_HIDDEN               = "hidden";
    public static final String ELEMENT_FOLDER               = "folder";
    public static final String ELEMENT_ARCHIVE              = "archive";
    public static final String ELEMENT_SYMLINK              = "symlink";
    public static final String ELEMENT_MARKED               = "marked";
    public static final String ELEMENT_FILE                 = "file";
    public static final String ELEMENT_PROGRESS             = "progress";
    public static final String ELEMENT_BORDER               = "border";
    public static final String ELEMENT_OK                   = "ok";
    public static final String ELEMENT_WARNING              = "warning";
    public static final String ELEMENT_CRITICAL             = "critical";
    public static final String ATTRIBUTE_COLOR              = "color";
    public static final String ATTRIBUTE_ALPHA              = "alpha";
}
