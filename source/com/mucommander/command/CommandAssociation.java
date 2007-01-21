package com.mucommander.command;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.RegexpFilenameFilter;
import com.mucommander.file.filter.PermissionsFileFilter;

/**
 * Helper class wrapping a <code>Command</code> and a <code>RegexpFilenameFilter</code> together.
 * @author Nicolas Rinaudo
 */
public class CommandAssociation {
    public static final int YES        = PermissionsFileFilter.YES;
    public static final int NO         = PermissionsFileFilter.NO;
    public static final int UNFILTERED = PermissionsFileFilter.UNFILTERED;

    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Command associated to this file name filter. */
    private Command               command;
    private RegexpFilenameFilter  nameFilter;
    private PermissionsFileFilter permFilter;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    public CommandAssociation(Command command, String regexp, int read, int write, int execute) {
        this.command    = command;
        this.nameFilter = new RegexpFilenameFilter(regexp);
        this.permFilter = new PermissionsFileFilter(read, write, execute);
    }

    public CommandAssociation(Command command, String regexp) {
        this.command = command;
        this.nameFilter = new RegexpFilenameFilter(regexp);
    }

    public String getRegularExpression() {return nameFilter.getRegularExpression();}

    public int getReadFilter() {return permFilter == null ? UNFILTERED : permFilter.getReadFilter();}

    public int getWriteFilter() {return permFilter == null ? UNFILTERED : permFilter.getWriteFilter();}

    public int getExecuteFilter() {return permFilter == null ? UNFILTERED : permFilter.getExecuteFilter();}

    public boolean accept(AbstractFile file) {
        if(!nameFilter.accept(file))
            return false;
        return permFilter == null ? true : permFilter.accept(file);
    }

    // - Command retrieval -----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the command used in the association.
     * @return the command used in the association.
     */
    public Command getCommand() {return command;}
}
