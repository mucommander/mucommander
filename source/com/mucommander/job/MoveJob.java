/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.io.FileTransferException;
import com.mucommander.text.Translator;
import com.mucommander.ui.FileCollisionDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;

import java.io.IOException;


/**
 * This job recursively moves a group of files.
 *
 * @author Maxence Bernard
 */
public class MoveJob extends TransferFileJob {

    /** Base destination folder */
    protected AbstractFile baseDestFolder;

    /** New filename in destination */
    private String newName;

    /** Default choice when encountering an existing file */
    private int defaultFileExistsAction = -1;

    /** Title used for error dialogs */
    private String errorDialogTitle;

    /** True if this job corresponds to a single file renaming */
    private boolean renameMode;

	
    /**
     * Creates a new MoveJob without starting it.
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param files files which are going to be moved
     * @param destFolder destination folder where the files will be moved
     * @param newName the new filename in the destination folder, can be <code>null</code> in which case the original filename will be used.
     * @param fileExistsAction default action to be triggered if a file already exists in the destination (action can be to ask the user)
     * @param renameMode true if this job corresponds to a single file renaming
     */
    public MoveJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFolder, String newName, int fileExistsAction, boolean renameMode) {
        super(progressDialog, mainFrame, files);

        this.baseDestFolder = destFolder;
        this.newName = newName;
        this.defaultFileExistsAction = fileExistsAction;
        this.errorDialogTitle = Translator.get("move_dialog.error_title");
        this.renameMode = renameMode;
    }

	
    /////////////////////////////////////
    // Abstract methods Implementation //
    /////////////////////////////////////

    /**
     * Moves recursively the given file or folder. 
     *
     * @param file the file or folder to move
     * @param recurseParams destination folder where the given file will be moved (null for top level files)
     * 
     * @return <code>true</code> if the file has been moved completly (copied + deleted).
     */
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
        AbstractFile destFile = FileFactory.getFile(destFolder.getAbsolutePath(true)+destFileName);
        if(destFile==null) {
            // Destination file couldn't be created

            // Loop for retry
            do {
                int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_write_file", destFileName));
                // Retry loops
                if(ret==RETRY_ACTION)
                    continue;
                // Cancel or close dialog return false
                return false;
                // Skip continues
            } while(true);
        }

        // Do not follow symlink, simply delete it and return
        if(file.isSymlink()) {
            do {		// Loop for retry
                try  {
                    file.delete();
                    return true;
                }
                catch(IOException e) {
                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);

                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_delete_file", file.getAbsolutePath()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog returns false
                    return false;
                }
            } while(true);
        }


        // Check for file collisions (file exists in the destination, destination subfolder of source, ...)
        // if a default action hasn't been specified
        int collision = FileCollisionChecker.checkForCollision(file, destFile);
        boolean append = false;
//        boolean overwrite = false;

        boolean caseRenaming = false;
        // Tests if destination filename is a variation of the original filename with a different case.
        // In this case, do not show warn about the source and destination being the same and try to rename the file.
        // Note: renaming will only work if AbstractFile#moveTo() succeeds.
        if(renameMode && collision==FileCollisionChecker.SAME_SOURCE_AND_DESTINATION) {
            String sourceFileName = file.getName();
            if(sourceFileName.equalsIgnoreCase(destFileName) && !sourceFileName.equals(destFileName))
                caseRenaming = true;
        }

        // Handle collision, asking the user what to do or using a default action to resolve it
        if(!caseRenaming && collision!=FileCollisionChecker.NO_COLLOSION) {

            int choice;
            // Use default action if one has been set, if not show up a dialog
            if(defaultFileExistsAction==FileCollisionDialog.ASK_ACTION) {
                FileCollisionDialog dialog = new FileCollisionDialog(progressDialog, mainFrame, collision, file, destFile, true);
                choice = waitForUserResponse(dialog);
                // If 'apply to all' was selected, this choice will be used for any other files (user will not be asked again)
                if(dialog.applyToAllSelected())
                    defaultFileExistsAction = choice;
            }
            else
                choice = defaultFileExistsAction;

            // Cancel, skip or close dialog
            if (choice==-1 || choice== FileCollisionDialog.CANCEL_ACTION) {
                interrupt();
                return false;
            }
            // Skip file
            else if (choice== FileCollisionDialog.SKIP_ACTION) {
                return false;
            }
            // Append to file (resume file copy)
            else if (choice== FileCollisionDialog.RESUME_ACTION) {
                append = true;
            }
            // Overwrite file
            else if (choice== FileCollisionDialog.OVERWRITE_ACTION) {
                // Do nothing, simply continue
//                overwrite = true;
            }
            //  Overwrite file if destination is older
            else if (choice== FileCollisionDialog.OVERWRITE_IF_OLDER_ACTION) {
                // Overwrite if file is newer (stricly)
                if(file.getDate()<=destFile.getDate())
                    return false;
//                overwrite = true;
            }
        }

        // First, let's try to move/rename the file using AbstractFile#moveTo() if it is more efficient than moving
        // the file manually. Do not attempt to rename the file if the destination must be appended.
        if(!append) {
            int moveToHint = file.getMoveToHint(destFile);
            if(moveToHint==AbstractFile.SHOULD_HINT || moveToHint==AbstractFile.MUST_HINT) {
                do {
                    try {
                        if(file.moveTo(destFile))
                            return true;
                        break;
                    }
                    catch(FileTransferException e) {
                        int ret = showErrorDialog(errorDialogTitle, Translator.get("error_while_transferring", file.getAbsolutePath()));
                        // Retry loops
                        if(ret==RETRY_ACTION)
                            continue;
                        // Cancel, skip or close dialog returns false
                        return false;
                    }
                }
                while(true);
            }
        }
        // That didn't work, let's copy the file to the destination and then delete the source file

        // Move directory recursively
        if(file.isDirectory()) {

            // creates the folder in the destination folder if it doesn't exist
            if(!(destFile.exists() && destFile.isDirectory())) {
                do {		// Loop for retry
                    try {
                        destFolder.mkdir(destFileName);
                    }
                    catch(IOException e) {
                        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);

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
                    for(int i=0; i<subFiles.length && getState()!=INTERRUPTED; i++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(subFiles[i]);
                        if(!processFile(subFiles[i], destFile))
                            isFolderEmpty = false;
                    }

                    // Only when finished with folder, set destination folder's date to match the original folder one
                    destFile.changeDate(file.getDate());

                    // If one file could returned failure, return failure as well since this
                    // folder could not be moved totally
                    if(!isFolderEmpty)
                        return false;
                }
                catch(IOException e) {
                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);

                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_file", file.getAbsolutePath()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog returns false
                    return false;
                }
                break;
            } while(true);

			
            // and finally deletes the empty folder
            do {		// Loop for retry
                try  {
                    file.delete();
                    return true;
                }
                catch(IOException e) {
                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);

                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_delete_folder", file.getAbsolutePath()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog returns false
                    return false;
                }
            } while(true);
        }
        // File is a regular file, move it
        else  {

            // if moveTo() returned false or if it wasn't possible to this method because of 'append',
            // try the hard way by copying the file first, and then deleting the source file
            if(tryCopyFile(file, destFile, append, errorDialogTitle) && getState()!=INTERRUPTED) {
                // Delete the source file
                do {		// Loop for retry
                    try  {
                        file.delete();
                        // All OK
                        return true;
                    }
                    catch(IOException e) {
                        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);

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

	
    public String getStatusString() {
        return Translator.get("move_dialog.moving_file", getCurrentFileInfo());
    }


    // This job modifies baseDestFolder and its subfolders
	
    protected boolean hasFolderChanged(AbstractFile folder) {
        return (baseSourceFolder!=null && baseSourceFolder.isParentOf(folder)) || baseDestFolder.isParentOf(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected void jobCompleted() {
        super.jobCompleted();

    // If this job correponds to a file renaming in the same directory, select the renamed file
    // in the active table after this job has finished (and hasn't been cancelled)
        if(files.size()==1 && newName!=null && baseDestFolder.equals(files.fileAt(0).getParent())) {
            // Resolve new file instance now that it exists: remote files do not update file attributes after
            // creation, we need to get an instance that reflects the newly created file attributes
            selectFileWhenFinished(FileFactory.getFile(baseDestFolder.getAbsolutePath(true)+newName));
        }
    }
}
