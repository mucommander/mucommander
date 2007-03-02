package com.mucommander.command;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.ChainedFileFilter;

import java.util.Iterator;

/**
 * @author Nicolas Rinaudo
 */
public class CommandAssociation {

    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Command associated to this file name filter. */
    private Command           command;
    private ChainedFileFilter fileFilter;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    public CommandAssociation(Command command, ChainedFileFilter filter) {
        this.command    = command;
        this.fileFilter = filter;
    }

    public boolean accept(AbstractFile file) {return fileFilter.accept(file);}

    // - Command retrieval -----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the command used in the association.
     * @return the command used in the association.
     */
    public Command getCommand() {return command;}

    public Iterator filters() {return fileFilter.getFileFilterIterator();}
}
