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


package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;
import java.io.OutputStream;


/**
 * This FileJob creates a new directory (or file) in a given folder.
 *
 * @author Maxence Bernard
 */
public class MkdirJob extends FileJob {
	
    private AbstractFile destFolder;
    private String filename;
	private boolean mkfileMode;


    /**
     * Creates a new Mkdir/Mkfile job.
     *
     * @param mkfileMode if true, this job will operate in 'mkfile' mode, if false in 'mkdir' mode
     */
    public MkdirJob(MainFrame mainFrame, FileSet fileSet, String filename, boolean mkfileMode) {
        super(mainFrame, fileSet);

        this.destFolder = fileSet.getBaseFolder();
        this.filename = filename;
        this.mkfileMode = mkfileMode;
		
        setAutoUnmark(false);
    }


    /////////////////////////////////////
    // Abstract methods Implementation //
    /////////////////////////////////////

    /**
     * Creates the new directory in the destination folder.
     */
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted (although there is no way to stop the job at this time)
        if(getState()==INTERRUPTED)
            return false;

        do {
            try {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating "+destFolder+" "+ filename);

                AbstractFile newFile = FileFactory.getFile(destFolder.getAbsolutePath(true)+filename);

                // Check for file collisions, i.e. if the file already exists in the destination
                int collision = FileCollisionChecker.checkForCollision(null, newFile);
                if(collision!=FileCollisionChecker.NO_COLLOSION) {
                    // File already exists in destination, ask the user what to do (cancel, overwrite,...) but
                    // do not offer the multiple files mode options such as 'skip' and 'apply to all'.
                    int choice = waitForUserResponse(new FileCollisionDialog(mainFrame, mainFrame, collision, null, newFile, false));

                    // Overwrite file
                    if (choice==FileCollisionDialog.OVERWRITE_ACTION) {
                        // Do nothing, simply continue and file will be overwritten
                    }
                    // Cancel or dialog close (return)
//                    else if (choice==-1 || choice==FileCollisionDialog.CANCEL_ACTION) {
                    else {
                        interrupt();
                        return false;
                    }
                }

                // Create file
                if(mkfileMode) {
                    OutputStream out = newFile.getOutputStream(false);
                    out.close();
                }
                // Create directory
                else {
                    destFolder.mkdir(filename);
                }

                // Resolve new file instance now that it exists: remote files do not update file attributes after
                // creation, we need to get an instance that reflects the newly created file attributes
                newFile = FileFactory.getFile(newFile.getURL());

                // Select newly created file when job is finished
                selectFileWhenFinished(newFile);

                return true;		// Return Success
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);

                int action = showErrorDialog(
                     Translator.get("error"),
                     Translator.get(mkfileMode?"cannot_write_file":"cannot_create_folder", destFolder.getAbsolutePath(true)+ filename),
                     new String[]{RETRY_TEXT, CANCEL_TEXT},
                     new int[]{RETRY_ACTION, CANCEL_ACTION}
                );
                // Retry (loop)
                if(action==RETRY_ACTION)
                    continue;
				
                // Cancel action
                return false;		// Return Failure
            }    
        }
        while(true);
    }

    /**
     * Not used.
     */
    public String getStatusString() {
        return null;
    }

    /**
     * Folders only needs to be refreshed if it is the destination folder
     */
    protected boolean hasFolderChanged(AbstractFile folder) {
        return destFolder.equals(folder);
    }
}
