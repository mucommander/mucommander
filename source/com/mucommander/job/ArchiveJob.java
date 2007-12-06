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
import com.mucommander.file.archiver.Archiver;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * This FileJob is responsible for compressing a set of files into an archive file.
 *
 * @author Maxence Bernard
 */
public class ArchiveJob extends TransferFileJob {

    /** Destination archive file */
    private AbstractFile destFile;

    /** Base destination folder's path */
    private String baseFolderPath;

    /** Archiver instance that does the actual archiving */
    private Archiver archiver;

    /** Archive format */
    private int archiveFormat;
	
    /** Optional archive comment */
    private String archiveComment;
	
    /** Lock to avoid Archiver.close() to be called while data is being written */
    private final Object ioLock = new Object();

    /** OutputStream of the file Current file  */
    private OutputStream out;

    
    public ArchiveJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFile, int archiveFormat, String archiveComment) {
        super(progressDialog, mainFrame, files);
		
        this.destFile = destFile;
        this.archiveFormat = archiveFormat;
        this.archiveComment = archiveComment;

        this.baseFolderPath = baseSourceFolder.getAbsolutePath(false);
    }


    ////////////////////////////////////
    // TransferFileJob implementation //
    ////////////////////////////////////

    protected boolean processFile(AbstractFile file, Object recurseParams) {
        if(getState()==INTERRUPTED)
            return false;

        String filePath = file.getAbsolutePath(false);
        String entryRelativePath = filePath.substring(baseFolderPath.length()+1, filePath.length());

        // Process current file
        do {		// Loop for retry
            try {
                if (file.isDirectory() && !file.isSymlink()) {
                    // Create new directory entry in archive file
                    archiver.createEntry(entryRelativePath, file);

                    // Recurse on files
                    AbstractFile subFiles[] = file.ls();
                    boolean folderComplete = true;
                    for(int i=0; i<subFiles.length && getState()!=INTERRUPTED; i++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(subFiles[i]);
                        if(!processFile(subFiles[i], null))
                            folderComplete = false;
                    }
					
                    return folderComplete;
                }
                else {
                    InputStream in = setCurrentInputStream(file.getInputStream());
                    // Synchronize this block to ensure that Archiver.close() is not closed while data is still being
                    // written to the archive OutputStream, this would cause ZipOutputStream to deadlock.
                    synchronized(ioLock) {
                        // Create a new file entry in archive and copy the current file
                        AbstractFile.copyStream(in, archiver.createEntry(entryRelativePath, file));
                        in.close();
                    }
                    return true;
                }
            }
            // Catch Exception rather than IOException as ZipOutputStream has been seen throwing NullPointerException
            catch(Exception e) {
                // If job was interrupted by the user at the time when the exception occurred,
                // it most likely means that the exception was caused by user cancellation.
                // In this case, the exception should not be interpreted as an error.
                if(getState()==INTERRUPTED)
                    return false;

                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught IOException: "+e);
                
                int ret = showErrorDialog(Translator.get("pack_dialog.error_title"), Translator.get("error_while_transferring", file.getAbsolutePath()));
                // Retry loops
                if(ret==RETRY_ACTION) {
                    // Reset processed bytes currentFileByteCounter
                    getCurrentFileByteCounter().reset();

                    continue;
                }
                // Cancel, skip or close dialog return false
                return false;
            }
        } while(true);
    }

    public String getStatusString() {
        return Translator.get("pack_dialog.packing_file", getCurrentFileInfo());
    }

    protected boolean hasFolderChanged(AbstractFile folder) {
        // This job modifies the folder where the archive is
        return folder.equals(destFile.getParentSilently());     // Note: parent may be null
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overriden method to initialize the archiver and handle the case where the destination file already exists.
     */
    protected void jobStarted() {
        super.jobStarted();

        // Check for file collisions, i.e. if the file already exists in the destination
        int collision = FileCollisionChecker.checkForCollision(null, destFile);
        if(collision!=FileCollisionChecker.NO_COLLOSION) {
            // File already exists in destination, ask the user what to do (cancel, overwrite,...) but
            // do not offer the multiple files mode options such as 'skip' and 'apply to all'.
            int choice = waitForUserResponse(new FileCollisionDialog(progressDialog, mainFrame, collision, null, destFile, false));

            // Overwrite file
            if (choice== FileCollisionDialog.OVERWRITE_ACTION) {
                // Do nothing, simply continue and file will be overwritten
            }
            // 'Cancel' or close dialog interrupts the job
            else {
                interrupt();
                return;
            }
        }

        // Loop for retry
        do {
            try {
                // Tries to get an Archiver instance.
                this.archiver = Archiver.getArchiver(destFile, archiveFormat);
                this.archiver.setComment(archiveComment);
                this.out = archiver.getOutputStream();

                break;

            }
            catch(Exception e) {
                int choice = showErrorDialog(Translator.get("pack_dialog.error_title"),
                                             Translator.get("cannot_write_file", destFile.getName()),
                                             new String[] {CANCEL_TEXT, RETRY_TEXT},
                                             new int[]  {CANCEL_ACTION, RETRY_ACTION}
                                             );

                // Retry loops
                if(choice == RETRY_ACTION)
                    continue;

                // 'Cancel' or close dialog interrupts the job
                interrupt();
                return;
            }
        } while(true);
    }


    /**
     * Overriden method to close the archiver.
     */
    public void jobStopped() {

        // TransferFileJob.jobStopped() closes the current InputStream, this will cause copyStream() to return
        super.jobStopped();

        // Synchronize this block to ensure that Archiver.close() is not closed while data is still being
        // written to the archive OutputStream, this would cause ZipOutputStream to deadlock.
        synchronized(ioLock) {
            // Try to close the archiver which in turns closes the archive OutputStream and underlying file OutputStream
            if(archiver!=null) {
                try { archiver.close(); }
                catch(IOException e) {}
            }

            // Todo: the check below was made when using java.util.zip. Since java.util.zip is not used anymore,
            // check if this is still necessary

            // Makes sure the file OutputStream has been properly closed. Archive.close() normally closes the archive
            // OutputStream which in turn should close the underlying file OutputStream, but for some strange reason,
            // if no entry has been added to a Zip archive and the job is interrupted (e.g. the first file could not be read),
            // ZipOutputStream.close() does not close the underlying OutputStream.
            if(out!=null) {
                try { out.close(); }
                catch(IOException e) {}
            }
        }
    }
}
