/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.util.List;

/**
 * This job renames a group of files to new names defined by Batch-Rename Dialog.
 * @author Mariusz Jakubowski
 */
public class BatchRenameJob extends MoveJob {
    private List<String> newNames;

    public BatchRenameJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, List<String> newNames) {
        super(progressDialog, mainFrame, files, files.getBaseFolder(), null, FileCollisionDialog.ASK_ACTION, true);
        this.newNames = newNames;
    }


    ////////////////////////////
    // FileJob implementation //
    ////////////////////////////

    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        this.newName = newNames.get(getCurrentFileIndex());
        return super.processFile(file, recurseParams);
    }

}
