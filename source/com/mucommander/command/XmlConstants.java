package com.mucommander.command;

/**
 * Defines the structure of a custom associations XML file.
 * @author Nicolas Rinaudo
 */
interface XmlConstants {
    // - XML elements ----------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Root element. */
    public static final String ELEMENT_ROOT                 = "commands";
    /** Custom command definition element. */
    public static final String ELEMENT_COMMAND              = "command";
    /** Custom association definition element. */
    public static final String ELEMENT_ASSOCIATION          = "association";



    // - Custom command structure ----------------------------------------------
    // -------------------------------------------------------------------------
    /** Custom command alias. */
    public static final String ARGUMENT_COMMAND_ALIAS       = "alias";
    /** Custom command value. */
    public static final String ARGUMENT_COMMAND_VALUE       = "value";
    /** Custom command 'is system' flag. */
    public static final String ARGUMENT_COMMAND_SYSTEM      = "system";
    /** Custom command 'is visible' flag. */
    public static final String ARGUMENT_COMMAND_VISIBLE     = "visible";



    // - Custom association structure ------------------------------------------
    // -------------------------------------------------------------------------
    /** Custom association command. */
    public static final String ARGUMENT_ASSOCIATION_COMMAND = "command";
    /** Custom association file mask. */
    public static final String ARGUMENT_ASSOCIATION_MASK    = "mask";
}
