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

package com.mucommander.ui.event;

import com.mucommander.ui.main.table.FileTable;

/**
 * Interface to be implemented by classes that wish to be notified of selection changes on a particular
 * FileTable. Those classes need to be registered to receive those events, this can be done by calling
 * {@link com.mucommander.ui.main.table.FileTable#addTableSelectionListener(TableSelectionListener) FileTable.addTableSelectionListener()}.
 *
 * @see com.mucommander.ui.main.table.FileTable
 * @author Maxence Bernard
 */
public interface TableSelectionListener {

    /**
     * This method is invoked when the selected file has changed on the specified FileTable .
     *
     * @param source the {@link com.mucommander.ui.main.table.FileTable} instance on which the file selection has changed
     */
    public void selectedFileChanged(FileTable source);


    /**
     * This method is invoked when the files marked have changed on the specified FileTable.
     *
     * @param source the {@link com.mucommander.ui.main.table.FileTable} instance on which the files marked have changed
     */
    public void markedFilesChanged(FileTable source);
}
