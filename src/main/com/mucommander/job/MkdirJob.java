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
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.io.RandomAccessOutputStream;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;


/**
 * This FileJob creates a new file or directory.
 *
 * @author Maxence Bernard
 */
public class MkdirJob extends FileJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(MkdirJob.class);
	
    private AbstractFile destFolder;

    private boolean mkfileMode;
    private long allocateSpace;


    /**
     * Creates a new MkdirJob which operates in 'mkdir' mode.
     */
    public MkdirJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet fileSet) {
        super(progressDialog, mainFrame, fileSet);

        this.destFolder = fileSet.getBaseFolder();
        this.mkfileMode = false;
		
        setAutoUnmark(false);
    }

    /**
     * Creates a new MkdirJob which operates in 'mkfile' mode.
     *
     * @param allocateSpace number of bytes to allocate to the file, -1 for none (use AbstractFile#mkfile())
     */
    public MkdirJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet fileSet, long allocateSpace) {
        super(progressDialog, mainFrame, fileSet);

        this.destFolder = fileSet.getBaseFolder();
        this.mkfileMode = true;
        this.allocateSpace = allocateSpace;

        setAutoUnmark(false);
    }


    ////////////////////////////
    // FileJob implementation //
    ////////////////////////////

    /**
     * Creates the new directory in the destination folder.
     */
    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted (although there is no way to stop the job at this time)
        if(getState()==INTERRUPTED)
            return false;

        do {
            try {
                LOGGER.debug("Creating "+file);

                // Check for file collisions, i.e. if the file already exists in the destination
                int collision = FileCollisionChecker.checkForCollision(null, file);
                if(collision!=FileCollisionChecker.NO_COLLOSION) {
                    // File already exists in destination, ask the user what to do (cancel, overwrite,...) but
                    // do not offer the multiple files mode options such as 'skip' and 'apply to all'.
                    int choice = waitForUserResponse(new FileCollisionDialog(getMainFrame(), getMainFrame(), collision, null, file, false, false));

                    // Overwrite file
                    if (choice==FileCollisionDialog.OVERWRITE_ACTION) {
                        // Delete the file
                        file.delete();
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
                    // Use mkfile
                    if(allocateSpace==-1) {
                        file.mkfile();
                    }
                    // Allocate the requested number of bytes
                    else {
                        OutputStream mkfileOut = null;
                        try {
                            // using RandomAccessOutputStream if we can have one
                            if(file.isFileOperationSupported(FileOperation.RANDOM_WRITE_FILE)) {
                                mkfileOut = file.getRandomAccessOutputStream();
                                ((RandomAccessOutputStream)mkfileOut).setLength(allocateSpace);
                            }
                            // manually otherwise
                            else {
                                mkfileOut = file.getOutputStream();

                                // Use BufferPool to avoid excessive memory allocation and garbage collection
                                byte buffer[] = BufferPool.getByteArray();
                                int bufferSize = buffer.length;

                                try {
                                    long remaining = allocateSpace;
                                    int nbWrite;
                                    while(remaining>0 && getState()!=INTERRUPTED) {
                                        nbWrite = (int)(remaining>bufferSize?bufferSize:remaining);
                                        mkfileOut.write(buffer, 0, nbWrite);
                                        remaining -= nbWrite;
                                    }
                                }
                                finally {
                                    BufferPool.releaseByteArray(buffer);
                                }
                            }
                        }
                        finally {
                            if(mkfileOut!=null)
                                try { mkfileOut.close(); }
                                catch(IOException e) {}
                        }
                    }
                }
                // Create directory
                else {
                    file.mkdir();
                }

                // Resolve new file instance now that it exists: remote files do not update file attributes after
                // creation, we need to get an instance that reflects the newly created file attributes
                file = FileFactory.getFile(file.getURL());

                // Select newly created file when job is finished
                selectFileWhenFinished(file);

                return true;		// Return Success
            }
            catch(IOException e) {
                // In mkfile mode, interrupting the job will close the OutputStream and cause an IOException to be
                // thrown, this is normal behavior
                if(mkfileMode && getState()==INTERRUPTED)
                    return false;

                LOGGER.debug("IOException caught", e);

                int action = showErrorDialog(
                     Translator.get("error"),
                     Translator.get(mkfileMode?"cannot_write_file":"cannot_create_folder", file.getAbsolutePath()),
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
     * Folders only needs to be refreshed if it is the destination folder
     */
    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return destFolder.equalsCanonical(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public String getStatusString() {
        return Translator.get("creating_file", getCurrentFilename());
    }
}
