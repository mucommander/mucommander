package com.mucommander.ui.theme;

/**
 * Defines the format of the XML theme files.
 * @author Nicolas Rinaudo
 */
interface XmlConstants {
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



    // - Status element ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Item normal state description element. */
    public static final String ELEMENT_NORMAL               = "normal";
    /** Item selected state description element. */
    public static final String ELEMENT_SELECTION            = "selection";



    // - Font element --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Font description element. */
    public static final String ELEMENT_FONT                 = "font";
    /** Font family attribute. */
    public static final String ATTRIBUTE_FAMILY             = "family";
    /** Font size attribute. */
    public static final String ATTRIBUTE_SIZE                = "size";
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
    /** Background color when an item doesn't have focus. */
    public static final String ELEMENT_UNFOCUSED_BACKGROUND = "unfocused_background";
    /** Background color when an item has the focus. */
    public static final String ELEMENT_BACKGROUND           = "background";
    /** Text color for hidden files. */
    public static final String ELEMENT_HIDDEN               = "hidden";
    /** Text color for folders. */
    public static final String ELEMENT_FOLDER               = "folder";
    /** Text color for archive files. */
    public static final String ELEMENT_ARCHIVE              = "archive";
    /** Text color for symlinks. */
    public static final String ELEMENT_SYMLINK              = "symlink";
    /** Text color for marked files. */
    public static final String ELEMENT_MARKED               = "marked";
    /** Generic color. */
    public static final String ELEMENT_TEXT                 = "text";
    /** Text color for standard files. */
    public static final String ELEMENT_FILE                 = "file";
    /** Progress bar color element. */
    public static final String ELEMENT_PROGRESS             = "progress";
    /** Border color element. */
    public static final String ELEMENT_BORDER               = "border";
    /** Color description attribute. */
    public static final String ATTRIBUTE_COLOR              = "color";
    /** Color transparency description attribute. */
    public static final String ATTRIBUTE_ALPHA              = "alpha";
}
