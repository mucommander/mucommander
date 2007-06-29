/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
