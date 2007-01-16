package com.mucommander.command;

import com.mucommander.file.filter.RegexpFilenameFilter;

/**
 * Helper class wrapping a <code>Command</code> and a <code>RegexpFilenameFilter</code> together.
 * @author Nicolas Rinaudo
 */
public class CommandAssociation extends RegexpFilenameFilter {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Command associated to this file name filter. */
    private Command command;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates an association between the specified command and file name filter.
     * @param command command to associate <code>regexp</code>.
     * @param regexp  regular expression that file names must match to be associated to <code>command</code>.
     */
    public CommandAssociation(Command command, String regexp) {
        super(regexp);
        this.command = command;
    }



    // - Command retrieval -----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the command used in the association.
     * @return the command used in the association.
     */
    public Command getCommand() {return command;}
}
