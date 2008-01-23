/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.command;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.ChainedFileFilter;

import java.util.Iterator;

/**
 * Associates a command to a set of file filters.
 * @author Nicolas Rinaudo
 */
class CommandAssociation {

    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Command associated to this file name filter. */
    private Command           command;
    private ChainedFileFilter fileFilter;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new <code>CommandAssociation</code>.
     * @param command command that must be executed if the association is matched.
     * @param filter  filter that files must match in order to be taken into account by the association.
     */
    public CommandAssociation(Command command, ChainedFileFilter filter) {
        this.command    = command;
        this.fileFilter = filter;
    }

    /**
     * Returns <code>true</code> if the specified file matches the association.
     * @param  file file to match against the association.
     * @return      <code>true</code> if the specified file matches the association, <code>false</code> otherwise.
     */
    public boolean accept(AbstractFile file) {return fileFilter.match(file);}

    // - Command retrieval -----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the command used in the association.
     * @return the command used in the association.
     */
    public Command getCommand() {return command;}

    /**
     * Returns an iterator on the various filters that must accept a file for it to match the association.
     * @return an iterator on the various filters that must accept a file for it to match the association.
     */
    public Iterator filters() {return fileFilter.getFileFilterIterator();}
}
