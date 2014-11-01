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

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractRWArchiveFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;


/**
 * This job recursively copies a set of files. Directories are copied recursively.
 *
 * @author Maxence Bernard
 */
public class CopyJob extends AbstractCopyJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(CopyJob.class);

    /** Destination file that is being copied, this value is updated every time #processFile() is called.
     * The value can be used by subclasses that override processFile should they need to work on the destination file. */
    protected AbstractFile currentDestFile;

    /** Operating mode : COPY_MODE or DOWNLOAD_MODE */
    private int mode;

    public final static int COPY_MODE = 0;
    public final static int DOWNLOAD_MODE = 1;

	
	
    /**
     * Creates a new CopyJob without starting it.
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param files files which are going to be copied
     * @param destFolder destination folder where the files will be copied
     * @param newName the new filename in the destination folder, can be <code>null</code> in which case the original filename will be used.
     * @param mode mode in which CopyJob is to operate: {@link #COPY_MODE} or {@link #DOWNLOAD_MODE}.
     * @param fileExistsAction default action to be performed when a file already exists in the destination, see {@link com.mucommander.ui.dialog.file.FileCollisionDialog} for allowed values
     */
    public CopyJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFolder, String newName, int mode, int fileExistsAction) {
        super(progressDialog, mainFrame, files, destFolder, newName, fileExistsAction);

        this.mode = mode;
        this.errorDialogTitle = Translator.get(mode==DOWNLOAD_MODE?"download_dialog.error_title":"copy_dialog.error_title");
    }



    ////////////////////////////////////
    // TransferFileJob implementation //
    ////////////////////////////////////

    /**
     * Copies recursively the given file or folder. 
     *
     * @param file the file or folder to move
     * @param recurseParams destination folder where the given file will be copied (null for top level files)
     * 
     * @return <code>true</code> if the file has been copied.
     */
    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted
        if(getState()==INTERRUPTED)
            return false;
		
        // Destination folder
        AbstractFile destFolder = recurseParams==null?baseDestFolder:(AbstractFile)recurseParams;
		
        // Is current file in base folder ?
        boolean isFileInBaseFolder = files.indexOf(file)!=-1;

        // Determine filename in destination
        String destFileName;
        if(isFileInBaseFolder && newName!=null)
            destFileName = newName;
       	else
            destFileName = file.getName();

        // Create destination AbstractFile instance
        AbstractFile destFile = createDestinationFile(destFolder, destFileName);
        if (destFile == null)
            return false;
        currentDestFile = destFile;

        // Do nothing if file is a symlink (skip file and return)
        if(file.isSymlink())
            return true;

        destFile = checkForCollision(file, destFolder, destFile, false);
        if (destFile == null)
            return false;

        // Copy directory recursively
        if(file.isDirectory()) {
            // Create the folder in the destination folder if it doesn't exist
            if(!(destFile.exists() && destFile.isDirectory())) {
                // Loop for retry
                do {
                    try {
                        destFile.mkdir();
                    }
                    catch(IOException e) {
                        // Unable to create folder
                        int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_create_folder", destFileName));
                        // Retry loops
                        if(ret==RETRY_ACTION)
                            continue;
                        // Cancel or close dialog return false
                        return false;
                        // Skip continues
                    }
                    break;
                } while(true);
            }
			
            // and copy each file in this folder recursively
            do {		// Loop for retry
                try {
                    // for each file in folder...
                    AbstractFile subFiles[] = file.ls();
//filesDiscovered(subFiles);
                    for(int i=0; i<subFiles.length && getState()!=INTERRUPTED; i++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(subFiles[i]);
                        processFile(subFiles[i], destFile);
                    }

                    // Set currentDestFile back to the enclosing folder in case an overridden processFile method
                    // needs to work with the folder after calling super.processFile.
                    currentDestFile = destFile;

                    // Only when finished with folder, set destination folder's date to match the original folder one
                    if(destFile.isFileOperationSupported(FileOperation.CHANGE_DATE)) {
                        try {
                            destFile.changeDate(file.getDate());
                        }
                        catch (IOException e) {
                            LOGGER.debug("failed to change the date of "+destFile, e);
                            // Fail silently
                        }
                    }

                    return true;
                }
                catch(IOException e) {
                    // file.ls() failed
                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_folder", file.getName()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog returns false
                    return false;
                }
            } while(true);
        }
        // File is a regular file, copy it
        else  {
            // Copy the file
            return tryCopyFile(file, destFile, append, errorDialogTitle);
        }
    }



    // This job modifies baseDestFolder and its subfolders
    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return baseDestFolder.isParentOf(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    protected void jobCompleted() {
        super.jobCompleted();

        // If the destination files are located inside an archive, optimize the archive file
        AbstractArchiveFile archiveFile = baseDestFolder.getParentArchive();
        if(archiveFile!=null && archiveFile.isArchive() && archiveFile.isWritable())
            optimizeArchive((AbstractRWArchiveFile)archiveFile);

        // If this job correponds to a 'local copy' of a single file and in the same directory,
        // select the copied file in the active table after this job has finished (and hasn't been cancelled)
        if(files.size()==1 && newName!=null && baseDestFolder.equalsCanonical(files.elementAt(0).getParent())) {
            // Resolve new file instance now that it exists: some remote files do not immediately update file attributes
            // after creation, we need to get an instance that reflects the newly created file attributes
            selectFileWhenFinished(FileFactory.getFile(baseDestFolder.getAbsolutePath(true)+newName));
        }
    }

    @Override
    public String getStatusString() {
        if(isCheckingIntegrity())
            return super.getStatusString();
        
        if(isOptimizingArchive)
            return Translator.get("optimizing_archive", archiveToOptimize.getName());

        return Translator.get(mode==DOWNLOAD_MODE?"download_dialog.downloading_file":"copy_dialog.copying_file", getCurrentFilename());
    }
}
