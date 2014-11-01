/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.dialog.file;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.main.MainFrame;

/**
 * Dialog invoked when the user wants to copy a single file to the same directory under a different name.
 * The destination field is pre-filled with the file's name.
 *
 * @author Maxence Bernard
 */
public class LocalCopyDialog extends CopyDialog {

    /**
     * Creates a new <code>LocalCopyDialog</code>.
     *
     * @param mainFrame the main frame that spawned this dialog.
     * @param files files to be copied
     */
    public LocalCopyDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    protected PathFieldContent computeInitialPath(FileSet files) {
        AbstractFile file = files.elementAt(0);
        return selectDestinationFilename(file, file.getName(), 0);

    }
}
