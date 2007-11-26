/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.action;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.FileFilter;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import java.util.Hashtable;

/**
 * SelectedFileAction is an abstract action that operates on the currently active FileTable,
 * and that is enabled only when a file other than the parent folder file '..' is selected.
 *
 * <p>Optionally, a FileFilter can be specified using {@link #setSelectedFileFilter(com.mucommander.file.filter.FileFilter) setSelectedFileFilter}
 * to further restrict the enabled condition to files that match the filter.
 *
 * @author Maxence Bernard
 */
public abstract class SelectedFileAction extends FileAction {

    public SelectedFileAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    /**
     * Restricts the enabled condition to selected files that match the specified filter.
     *
     * @param filter FileFilter instance
     */
    public void setSelectedFileFilter(FileFilter filter) {
        this.filter = filter;
    }


    protected boolean getFileTableCondition(FileTable fileTable) {
        AbstractFile selectedFile = fileTable.getSelectedFile(false, true);
        boolean enable = selectedFile!=null;

        if(enable && filter!=null)
            enable = filter.match(selectedFile);

        return enable;
    }
}
