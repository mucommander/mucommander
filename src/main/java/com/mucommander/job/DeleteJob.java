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
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.desktop.AbstractTrash;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This class is responsible for deleting a set of files. This job can operate in two modes, depending on the boolean
 * value specified in the construtor:
 * <ul>
 *  <li>moveToTrash enabled: files are moved to the trash returned by {@link DesktopManager#getTrash()}.
 *  <li>moveToTrash disabled: files are permanently deleted, i.e deleted files cannot be recovered. In this mode,
 * folders are deleted recursively
 * </ul>
 *
 * @author Maxence Bernard
 */
public class DeleteJob extends FileJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteJob.class);
	
    /** Title used for error dialogs */
    private String errorDialogTitle;

    /** If true, files will be moved to the trash instead of being deleted */
    private boolean moveToTrash;

    /** Trash instance, null if moveToTrash is false */
    private AbstractTrash trash;

    /** The archive that contains the deleted files (may be null) */ 
    private AbstractRWArchiveFile archiveToOptimize;

    /** True when an archive is being optimized */
    private boolean isOptimizingArchive;


    /**
     * Creates a new DeleteJob without starting it.
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param files files which are going to be deleted
     * @param moveToTrash if true, files will be moved to the trash, if false they will be permanently deleted.
     * Should be true only if a trash is available on the current platform.
     */
    public DeleteJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, boolean moveToTrash) {
        super(progressDialog, mainFrame, files);

        this.errorDialogTitle = Translator.get("delete_dialog.error_title");

        this.moveToTrash = moveToTrash;
        if(moveToTrash)
            trash = DesktopManager.getTrash();
    }

    /**
     * Deletes the given file, either by moving it to the trash (if {@link #moveToTrash} is true) or by deleting the
     * file directly.
     *
     * @param file the file to delete
     * @throws IOException if an error occurred while deleting the file
     */
    private void deleteFile(AbstractFile file) throws IOException {
        if(moveToTrash)
            trash.moveToTrash(file);
        else
            file.delete();
    }


    ////////////////////////////
    // FileJob implementation //
    ////////////////////////////

    /**
     * Deletes recursively the given file or folder. 
     *
     * @param file the file or folder to delete
     * @param recurseParams not used
     * 
     * @return <code>true</code> if the file has been completely deleted.
     */
    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        if(getState()==INTERRUPTED)
            return false;

        // Delete files recursively, only if trash is not used.
        int ret;
        if(!moveToTrash && file.isDirectory()) {
            String filePath = file.getAbsolutePath();
            filePath = filePath.substring(getBaseSourceFolder().getAbsolutePath(false).length()+1, filePath.length());

            // Important: symlinks must *not* be followed -- following symlinks could have disastrous effects.
            if(!file.isSymlink()) {
                do {		// Loop for retry
                    // Delete each file in this folder
                    try {
                        AbstractFile subFiles[] = file.ls();
                        for(int i=0; i<subFiles.length && getState()!=INTERRUPTED; i++) {
                            // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                            nextFile(subFiles[i]);
                            processFile(subFiles[i], null);
                        }
                        break;
                    }
                    catch(IOException e) {
                        LOGGER.debug("IOException caught", e);

                        ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_file", filePath));
                        // Retry loops
                        if(ret==RETRY_ACTION)
                            continue;
                        // Cancel, skip or close dialog returns false
                        return false;
                    }
                } while(true);
            }
        }
        // Return now if the job was interrupted, so that we do not attempt to delete this folder
        if(getState()==INTERRUPTED)
            return false;

        do {		// Loop for retry
            try {
                deleteFile(file);

                return true;
            }
            catch(IOException e) {
                LOGGER.debug("IOException caught", e);

                ret = showErrorDialog(errorDialogTitle,
                                      Translator.get(file.isDirectory()?"cannot_delete_folder":"cannot_delete_file", file.getName())
                                      );
                // Retry loops
                if(ret==RETRY_ACTION)
                    continue;
                // Cancel, skip or close dialog returns false
                return false;
            }
        } while(true);
    }

    // This job modifies baseFolder and subfolders
    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return getBaseSourceFolder().isParentOf(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    protected void jobStopped() {
        super.jobStopped();

        if(moveToTrash)
            trash.waitForPendingOperations();
    }

    @Override
    protected void jobCompleted() {
        super.jobCompleted();

        // If the source files are located inside an archive, optimize the archive file
        AbstractArchiveFile archiveFile = getBaseSourceFolder().getParentArchive();

        if(archiveFile!=null && archiveFile.isArchive() && archiveFile.isWritable()) {
            while(true) {
                try {
                    archiveToOptimize = ((AbstractRWArchiveFile)archiveFile);
                    isOptimizingArchive = true;

                    archiveToOptimize.optimizeArchive();

                    break;
                }
                catch(IOException e) {
                    if(showErrorDialog(errorDialogTitle, Translator.get("error_while_optimizing_archive", archiveFile.getName()))==RETRY_ACTION)
                        continue;

                    break;
                }
            }

            isOptimizingArchive = true;
        }
    }

    @Override
    public String getStatusString() {
        if(isOptimizingArchive)
            return Translator.get("optimizing_archive", archiveToOptimize.getName());

        return Translator.get("delete.deleting_file", getCurrentFilename());
    }
}
