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

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.FileCollisionDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;

import java.io.IOException;


/**
 * This job recursively copies (or unpacks) a group of files.
 *
 * @author Maxence Bernard
 */
public class CopyJob extends TransferFileJob {

    /** Base destination folder */
    protected AbstractFile baseDestFolder;

    /** New filename in destination */
    private String newName;

    /** Default choice when encountering an existing file */
    private int defaultFileExistsAction = FileCollisionDialog.ASK_ACTION;

    /** Title used for error dialogs */
    private String errorDialogTitle;
	
    /** Operating mode : COPY_MODE, UNPACK_MODE or DOWNLOAD_MODE */
    private int mode;
	
    public final static int COPY_MODE = 0;
    public final static int UNPACK_MODE = 1;
    public final static int DOWNLOAD_MODE = 2;
	
	
    /**
     * Creates a new CopyJob without starting it.
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param files files which are going to be copied
     * @param destFolder destination folder where the files will be copied
     * @param newName the new filename in the destination folder, can be <code>null</code> in which case the original filename will be used.
     * @param mode mode in which CopyJob is to operate: COPY_MODE, UNPACK_MODE or DOWNLOAD_MODE.
     * @param fileExistsAction default action to be triggered if a file already exists in the destination (action can be to ask the user)
     */
    public CopyJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFolder, String newName, int mode, int fileExistsAction) {
        super(progressDialog, mainFrame, files);

        this.baseDestFolder = destFolder;
        this.newName = newName;
        this.mode = mode;
        this.defaultFileExistsAction = fileExistsAction;
        this.errorDialogTitle = Translator.get(mode==UNPACK_MODE?"unpack_dialog.error_title":mode==DOWNLOAD_MODE?"download_dialog.error_title":"copy_dialog.error_title");
    }

	
    /////////////////////////////////////
    // Abstract methods Implementation //
    /////////////////////////////////////

    /**
     * Copies recursively the given file or folder. 
     *
     * @param file the file or folder to move
     * @param recurseParams destination folder where the given file will be copied (null for top level files)
     * 
     * @return <code>true</code> if the file has been copied.
     */
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted
        if(getState()==INTERRUPTED)
            return false;
		
        // Destination folder
        AbstractFile destFolder = recurseParams==null?baseDestFolder:(AbstractFile)recurseParams;
		
        // Is current file in base folder ?
        boolean isFileInBaseFolder = files.indexOf(file)!=-1;

        // If in unpack mode, copy files contained by the archive file
        if(mode==UNPACK_MODE && isFileInBaseFolder) {
            // Recursively unpack files
            do {		// Loop for retries
                try {
                    // List files inside archive file (can throw an IOException)
                    AbstractFile archiveFiles[] = currentFile.ls();
                    // Recurse on zip's contents
                    for(int j=0; j<archiveFiles.length && getState()!=INTERRUPTED; j++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(archiveFiles[j]);
                        // Recurse
                        processFile(archiveFiles[j], destFolder);
                    }
                    // Return true when complete
                    return true;
                }
                catch(IOException e) {
                    // File could not be uncompressed properly
//                    int ret = showErrorDialog(errorDialogTitle, Translator.get("unpack.unable_to_open_zip", currentFile.getName()));
                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_file", currentFile.getName()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // cancel, skip or close dialog will simply return false
                    return false;
                }
            } while(true);
        }
		
		
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

        // Do nothing if file is a symlink (skip file and return)
        if(file.isSymlink())
            return true;

        // Check for file collisions (file exists in the destination, destination subfolder of source, ...)
        // if a default action hasn't been specified
        int collision = FileCollisionChecker.checkForCollision(file, destFile);
        boolean append = false;
//        boolean overwrite = false;

        // Handle collision, asking the user what to do or using a default action to resolve the collision 
        if(collision != FileCollisionChecker.NO_COLLOSION) {
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

        // Copy directory recursively
        if(file.isDirectory()) {
            // Create the folder in the destination folder if it doesn't exist
            if(!(destFile.exists() && destFile.isDirectory())) {
                // Loop for retry
                do {
                    try {
                        destFolder.mkdir(destFileName);
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

                    // Only when finished with folder, set destination folder's date to match the original folder one
                    destFile.changeDate(file.getDate());

                    return true;
                }
                catch(IOException e) {
                    // Unable to open source file
                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_folder", destFile.getName()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog return false
                    return false;
                }
            } while(true);
        }
        // File is a regular file, copy it
        else  {
// The source of this issue was that FtpClient#storeUniqueFileStream() was used instead open FtpClient#storeFileStream()
//            // FTP overwrite bug workaround: if the destination file is not deleted, the existing destination
//            // file is renamed to <filename>.1
//            if(overwrite && destFile.getURL().getProtocol().equals(FileProtocols.FTP)) {
//                try { destFile.delete(); }
//                catch(IOException e) {}
//            }

            // Copy the file
            boolean success = tryCopyFile(file, destFile, append, errorDialogTitle);
			
            return success;
        }
    }

    public String getStatusString() {
        return Translator.get(mode==UNPACK_MODE?"unpack_dialog.unpacking_file":mode==DOWNLOAD_MODE?"download_dialog.downloading_file":"copy_dialog.copying_file", getCurrentFileInfo());
    }
	
    // This job modifies baseDestFolder and its subfolders
	
    protected boolean hasFolderChanged(AbstractFile folder) {
        if(Debug.ON) Debug.trace("folder="+folder+" returning "+baseDestFolder.isParentOf(folder));

        return baseDestFolder.isParentOf(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected void jobCompleted() {
        super.jobCompleted();

        // If this job correponds to a 'local copy' of a single file and in the same directory,
        // select the copied file in the active table after this job has finished (and hasn't been cancelled)
        if(files.size()==1 && newName!=null && baseDestFolder.equals(files.fileAt(0).getParent())) {
            // Resolve new file instance now that it exists: remote files do not update file attributes after
            // creation, we need to get an instance that reflects the newly created file attributes
            selectFileWhenFinished(FileFactory.getFile(baseDestFolder.getAbsolutePath(true)+newName));
        }
    }
}
