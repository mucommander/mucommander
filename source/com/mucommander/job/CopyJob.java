/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import com.mucommander.Debug;
import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractRWArchiveFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;


/**
 * This job recursively copies (or unpacks) a group of files.
 *
 * @author Maxence Bernard
 */
public class CopyJob extends AbstractCopyJob {

    /** Destination file that is being copied, this value is updated every time #processFile() is called.
     * The value can be used by subclasses that override processFile should they need to work on the destination file. */
    protected AbstractFile currentDestFile;

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
     * @param fileExistsAction default action to be performed when a file already exists in the destination, see {@link com.mucommander.ui.dialog.file.FileCollisionDialog} for allowed values
     */
    public CopyJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFolder, String newName, int mode, int fileExistsAction) {
        super(progressDialog, mainFrame, files, destFolder, newName, fileExistsAction);

        this.mode = mode;
        this.errorDialogTitle = Translator.get(mode==UNPACK_MODE?"unpack_dialog.error_title":mode==DOWNLOAD_MODE?"download_dialog.error_title":"copy_dialog.error_title");
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

                    // Only when finished with folder, set destination folder's date to match the original folder one
                    destFile.changeDate(file.getDate());

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
    protected boolean hasFolderChanged(AbstractFile folder) {
        if(Debug.ON) Debug.trace("folder="+folder+" returning "+baseDestFolder.isParentOf(folder));

        return baseDestFolder.isParentOf(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected void jobCompleted() {
        super.jobCompleted();

        // If the destination files are located inside an archive, optimize the archive file
        AbstractArchiveFile archiveFile = baseDestFolder.getParentArchive();
        if(archiveFile!=null && archiveFile.isWritableArchive())
            optimizeArchive((AbstractRWArchiveFile)archiveFile);

        // If this job correponds to a 'local copy' of a single file and in the same directory,
        // select the copied file in the active table after this job has finished (and hasn't been cancelled)
        if(files.size()==1 && newName!=null && baseDestFolder.equals(files.fileAt(0).getParentSilently())) {
            // Resolve new file instance now that it exists: some remote files do not immediately update file attributes
            // after creation, we need to get an instance that reflects the newly created file attributes
            selectFileWhenFinished(FileFactory.getFile(baseDestFolder.getAbsolutePath(true)+newName));
        }
    }

    public String getStatusString() {
        if(isCheckingIntegrity())
            return super.getStatusString();
        
        if(isOptimizingArchive)
            return Translator.get("optimizing_archive", archiveToOptimize.getName());

        return Translator.get(mode==UNPACK_MODE?"unpack_dialog.unpacking_file":mode==DOWNLOAD_MODE?"download_dialog.downloading_file":"copy_dialog.copying_file", getCurrentFileInfo());
    }
}
