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

package com.mucommander.job;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * @author Maxence Bernard
 */
public class ChangeFileAttributesJob extends FileJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChangeFileAttributesJob.class);
	
    private boolean recurseOnDirectories;

    private int permissions = -1;
    private long date = -1;


    public ChangeFileAttributesJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, int permissions, boolean recurseOnDirectories) {
        super(progressDialog, mainFrame, files);

        this.permissions = permissions;
        this.recurseOnDirectories = recurseOnDirectories;
    }


    public ChangeFileAttributesJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, long date, boolean recurseOnDirectories) {
        super(progressDialog, mainFrame, files);

        this.date = date;
        this.recurseOnDirectories = recurseOnDirectories;
    }


    ////////////////////////////
    // FileJob implementation //
    ////////////////////////////

    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted
        if(getState()==INTERRUPTED)
            return false;

        if(recurseOnDirectories && file.isDirectory()) {
            do {		// Loop for retries
                try {
                    AbstractFile children[] = file.ls();
                    int nbChildren = children.length;

                    for(int i=0; i<nbChildren && getState()!=INTERRUPTED; i++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(children[i]);
                        processFile(children[i], null);
                    }

                    break;
                }
                catch(IOException e) {
                    // Unable to open source file
                    int ret = showErrorDialog("", Translator.get("cannot_read_folder", file.getName()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog return false
                    return false;
                }
            }
            while(true);
        }

        if(permissions!=-1) {
            if(!file.isFileOperationSupported(FileOperation.CHANGE_PERMISSION))
                return false;

            try {
                file.changePermissions(permissions);
                return true;
            }
            catch(IOException e) {
                return false;
            }
        }

//        if(date!=-1)
        if(!file.isFileOperationSupported(FileOperation.CHANGE_DATE))
            return false;

        try {
            file.changeDate(date);
            return true;
        }
        catch (IOException e) {
            LOGGER.debug("failed to change the date of "+file, e);
            return false;
        }
    }

    // This job modifies the FileSet's base folder and potentially its subfolders
    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return getBaseSourceFolder().isParentOf(folder);
    }
}
