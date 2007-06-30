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
import com.mucommander.file.AbstractTrash;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.layout.YBoxPanel;

import javax.swing.*;
import java.io.IOException;

/**
 * This class is responsible for deleting a set of files. This job can operate in two modes, depending on the boolean
 * value specified in the construtor:
 * <ul>
 *  <li>moveToTrash enabled: files are moved to the trash returned by {@link FileFactory#getTrash()}.
 *  <li>moveToTrash disabled: files are permanently deleted, i.e deleted files cannot be recovered. In this mode,
 * folders are deleted recursively
 * </ul>
 *
 * @author Maxence Bernard
 */
public class DeleteJob extends FileJob {
    
    /** Title used for error dialogs */
    private String errorDialogTitle;

    /** If true, files will be moved to the trash instead of being deleted */
    private boolean moveToTrash;

    /** Trash instance, null if moveToTrash is false */
    private AbstractTrash trash;

    private final static int DELETE_LINK_ACTION = 100;
    private final static int DELETE_FOLDER_ACTION = 101;

    private final static String DELETE_LINK_TEXT = Translator.get("delete.delete_link_only");
    private final static String DELETE_FOLDER_TEXT = Translator.get("delete.delete_linked_folder");

	
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
            trash = FileFactory.getTrash();
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


    private int showSymlinkDialog(String relativePath, String canonicalPath) {
        YBoxPanel panel = new YBoxPanel();

        JTextArea symlinkWarningArea = new JTextArea(Translator.get("delete.symlink_warning", relativePath, canonicalPath));
        symlinkWarningArea.setEditable(false);
        panel.add(symlinkWarningArea);

        QuestionDialog dialog = new QuestionDialog(progressDialog, Translator.get("delete.symlink_warning_title"), panel, mainFrame,
                                                   new String[] {DELETE_LINK_TEXT, DELETE_FOLDER_TEXT, SKIP_TEXT, CANCEL_TEXT},
                                                   new int[]  {DELETE_LINK_ACTION, DELETE_FOLDER_ACTION, SKIP_ACTION, CANCEL_ACTION},
                                                   2);

        return waitForUserResponse(dialog);
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
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        if(getState()==INTERRUPTED)
            return false;

        String filePath = file.getAbsolutePath();
        filePath = filePath.substring(baseSourceFolder.getAbsolutePath(false).length()+1, filePath.length());

        int ret;
        boolean followSymlink = false;
        // Delete files recursively, only if trash is not used.
        if(!moveToTrash && file.isDirectory()) {
            // If folder is a symlink, asks the user what to do
            boolean isSymlink = file.isSymlink();
            if(isSymlink) {
                ret = showSymlinkDialog(filePath, file.getCanonicalPath());
                if(ret==-1 || ret==CANCEL_ACTION) {
                    interrupt();
                    return false;
                }
                else if(ret==SKIP_ACTION) {
                    return false;
                }
                // Delete file only
                else if(ret==DELETE_FOLDER_ACTION) {
                    followSymlink = true;
                }
            }
			
            if(!isSymlink || followSymlink) {
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
                        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);

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
        
        if(getState()==INTERRUPTED)
            return false;

        do {		// Loop for retry
            try {
                // If file is a symlink to a folder and the user asked to follow the symlink,
                // delete the empty folder
                if(followSymlink) {
                    AbstractFile canonicalFile = FileFactory.getFile(file.getCanonicalPath());
                    if(canonicalFile!=null)
                        deleteFile(canonicalFile);
                }
	
                deleteFile(file);

                return true;
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);
	
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


    public String getStatusString() {
        return Translator.get("delete.deleting_file", getCurrentFileInfo());
    }

    // This job modifies baseFolder and subfolders
    protected boolean hasFolderChanged(AbstractFile folder) {
        return baseSourceFolder.isParentOf(folder);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected void jobStopped() {
        super.jobStopped();

        if(moveToTrash)
            trash.waitForPendingOperations();
    }
}
