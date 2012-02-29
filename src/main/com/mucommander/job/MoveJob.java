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
 * This job recursively moves a group of files.
 *
 * @author Maxence Bernard
 */
public class MoveJob extends AbstractCopyJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(MoveJob.class);
	
    /** True if this job corresponds to a single file renaming */
    protected boolean renameMode = false;

    
    /**
     * Creates a new MoveJob without starting it.
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param files files which are going to be moved
     * @param destFolder destination folder where the files will be moved
     * @param newName the new filename in the destination folder, can be <code>null</code> in which case the original filename will be used.
     * @param fileExistsAction default action to be performed when a file already exists in the destination, see {@link com.mucommander.ui.dialog.file.FileCollisionDialog} for allowed values
     * @param renameMode true if this job corresponds to a single file renaming
     */
    public MoveJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFolder, String newName, int fileExistsAction, boolean renameMode) {
        super(progressDialog, mainFrame, files, destFolder, newName, fileExistsAction);

        this.errorDialogTitle = Translator.get("move_dialog.error_title");
        this.renameMode = renameMode;
    }


    ////////////////////////////////////
    // TransferFileJob implementation //
    ////////////////////////////////////

    /**
     * Moves recursively the given file or folder. 
     *
     * @param file the file or folder to move
     * @param recurseParams destination folder where the given file will be moved (null for top level files)
     * 
     * @return <code>true</code> if the file has been moved completly (copied + deleted).
     */
    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted
        if(getState()==INTERRUPTED)
            return false;
		
        // Destination folder
        AbstractFile destFolder = recurseParams==null?baseDestFolder:(AbstractFile)recurseParams;
		
        // Is current file at the base folder level ?
        boolean isFileInBaseFolder = files.indexOf(file)!=-1;

        // Determine filename in destination
        String originalName = file.getName();
        String destFileName;
        if(isFileInBaseFolder && newName!=null)
            destFileName = newName;
       	else
            destFileName = originalName;
		
        // Create destination AbstractFile instance
        AbstractFile destFile = createDestinationFile(destFolder, destFileName);
        if (destFile == null)
            return false;

        // Do not follow symlink, simply delete it and return
        if(file.isSymlink()) {
            do {		// Loop for retry
                try  {
                    file.delete();
                    return true;
                }
                catch(IOException e) {
                    LOGGER.debug("IOException caught", e);

                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_delete_file", file.getAbsolutePath()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog returns false
                    return false;
                }
            } while(true);
        }

        destFile = checkForCollision(file, destFolder, destFile, renameMode);
        if (destFile == null)
            return false;

        // Let's try to rename the file using AbstractFile#renameTo() whenever possible, as it is more efficient
        // than moving the file manually.
        //
        // Do not attempt to rename the file in the following cases:
        // - destination has to be appended
        // - file schemes do not match (at the time of writing, no filesystem supports mixed scheme renaming)
        // - if the 'rename' operation is not supported
        // Note: we want to avoid calling AbstractFile#renameTo when we know it will fail, as it performs some costly
        // I/O bound checks and ends up throwing an exception which also comes at a cost.
        if(!append && file.getURL().schemeEquals(destFile.getURL()) && file.isFileOperationSupported(FileOperation.RENAME)) {
            try {
                file.renameTo(destFile);
                return true;
            }
            catch(IOException e) {
                // Fail silently: renameTo might fail under normal conditions, for instance for local files which are
                // not located on the same volume.
                LOGGER.debug("Failed to rename "+file+" into "+destFile+" (not necessarily an error)", e);
            }
        }
        // Rename couldn't be used or didn't succeed: move the file manually

        // Move the directory and all its children recursively, by copying files to the destination and then deleting them.
        if(file.isDirectory()) {
            // create the destination folder if it doesn't exist
            if(!(destFile.exists() && destFile.isDirectory())) {
                do {		// Loop for retry
                    try {
                        destFile.mkdir();
                    }
                    catch(IOException e) {
                        int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_create_folder", destFile.getAbsolutePath()));
                        // Retry loops
                        if(ret==RETRY_ACTION)
                            continue;
                        // Cancel, skip or close dialog returns false
                        return false;
                    }
                    break;
                } while(true);
            }
			
            // move each file in this folder recursively
            do {		// Loop for retry
                try {
                    AbstractFile subFiles[] = file.ls();
                    boolean isFolderEmpty = true;
                    for (AbstractFile subFile : subFiles) {
                        // Return now if the job was interrupted, so that we do not attempt to delete this folder
                        if (getState() == INTERRUPTED)
                            return false;

                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(subFile);
                        if (!processFile(subFile, destFile))
                            isFolderEmpty = false;
                    }

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

                    // If one file failed to be moved, return false (failure) since this folder could not be moved totally
                    if(!isFolderEmpty)
                        return false;
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
                break;
            } while(true);

            // Return now if the job was interrupted, so that we do not attempt to delete this folder
            if(getState()==INTERRUPTED)
                return false;

            // finally, delete the empty folder
            do {		// Loop for retry
                try  {
                    file.delete();
                    return true;
                }
                catch(IOException e) {
                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_delete_folder", file.getAbsolutePath()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog returns false
                    return false;
                }
            } while(true);
        }
        // File is a regular file, move it by copying it to the destination and then deleting it
        else  {

            // if renameTo() was not supported or failed, or if it wasn't possible because of 'append',
            // try the hard way by copying the file first, and then deleting the source file.
            if(tryCopyFile(file, destFile, append, errorDialogTitle) && getState()!=INTERRUPTED) {
                // Delete the source file
                do {		// Loop for retry
                    try  {
                        file.delete();
                        // All OK
                        return true;
                    }
                    catch(IOException e) {
                        LOGGER.debug("IOException caught", e);

                        int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_delete_file", file.getAbsolutePath()));
                        // Retry loops
                        if(ret==RETRY_ACTION)
                            continue;
                        // Cancel, skip or close dialog returns false
                        return false;
                    }
                } while(true);
            }

            return false;
        }
    }

    // This job modifies baseDestFolder and its subfolders
    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return (getBaseSourceFolder()!=null && getBaseSourceFolder().isParentOf(folder)) || baseDestFolder.isParentOf(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    protected void jobCompleted() {
        super.jobCompleted();

        // If the source files are located inside an archive, optimize the archive file
        AbstractArchiveFile sourceArchiveFile = getBaseSourceFolder()==null?null:getBaseSourceFolder().getParentArchive();
        if(sourceArchiveFile!=null && sourceArchiveFile.isArchive() && sourceArchiveFile.isWritable())
            optimizeArchive((AbstractRWArchiveFile)sourceArchiveFile);

        // If the destination files are located inside an archive, optimize the archive file, only if the destination
        // archive is different from the source one
        AbstractArchiveFile destArchiveFile = baseDestFolder.getParentArchive();
        if(destArchiveFile!=null && destArchiveFile.isArchive() && destArchiveFile.isWritable()
                && !(sourceArchiveFile!=null && destArchiveFile.equalsCanonical(sourceArchiveFile)))
            optimizeArchive((AbstractRWArchiveFile)destArchiveFile);

        // If this job correponds to a file renaming in the same directory, select the renamed file
        // in the active table after this job has finished (and hasn't been cancelled)
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

        return Translator.get("move_dialog.moving_file", getCurrentFilename());
    }
}
