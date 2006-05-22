
package com.mucommander.job;

import com.mucommander.file.*;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.FileExistsDialog;

import com.mucommander.text.Translator;

import java.io.IOException;


/**
 * This job recursively moves a group of files.
 *
 * @author Maxence Bernard
 */
public class MoveJob extends ExtendedFileJob {

    /** Base destination folder */
    protected AbstractFile baseDestFolder;

    /** New filename in destination */
    private String newName;

    /** Default choice when encountering an existing file */
    private int defaultFileExistsAction = -1;

    /** Title used for error dialogs */
    private String errorDialogTitle;
	
	
    /**
     * Creates a new MoveJob without starting it.
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param files files which are going to be moved
     * @param destFolder destination folder where the files will be moved
     * @param newName the new filename in the destination folder, can be <code>null</code> in which case the original filename will be used.
     * @param fileExistsAction default action to be triggered if a file already exists in the destination (action can be to ask the user)
     */
    public MoveJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFolder, String newName, int fileExistsAction) {
        super(progressDialog, mainFrame, files);

        this.baseDestFolder = destFolder;
        this.newName = newName;
        this.defaultFileExistsAction = fileExistsAction;
        this.errorDialogTitle = Translator.get("move_dialog.error_title");
	
        // If this job correponds to a file renaming in the same directory, select the renamed file
        // in the active table after this job has finished (and hasn't been cancelled)
        if(files.size()==1 && newName!=null && destFolder.equals(files.fileAt(0).getParent()))
            selectFileAfter(AbstractFile.getAbstractFile(destFolder.getAbsolutePath(true)+newName));
    }

	
    /**
     * Tries to move the file with AbstractFile.moveTo() 
     * skipping the whole manual recursive process
     */
    private boolean fileMove(AbstractFile file, AbstractFile destFile) {
        try {
            if(file.moveTo(destFile))
                return true;		// return true in case of success
        } catch(IOException e) { 
            if(com.mucommander.Debug.ON) e.printStackTrace();
        }
		
        return false;
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
        if(isInterrupted())
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
		
        // Destination file
        AbstractFile destFile = AbstractFile.getAbstractFile(destFolder.getAbsolutePath(true)+destFileName);


        // Do not follow symlink, simply delete it
        if(file.isSymlink()) {
            do {		// Loop for retry
                try  {
                    file.delete();
                    return true;
                }
                catch(IOException e) {
                    if(com.mucommander.Debug.ON) e.printStackTrace();

                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_delete_file", file.getAbsolutePath()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog returns false
                    return false;
                }
            } while(true);
        }
        // Move directory recursively
        else if(file.isDirectory()) {
            // Let's try the easy way
            if(fileMove(file, destFile))
                return true;
            // That didn't work, let's recurse

            // creates the folder in the destination folder if it doesn't exist
            if(!(destFile.exists() && destFile.isDirectory())) {
                do {		// Loop for retry
                    try {
                        destFolder.mkdir(destFileName);
                    }
                    catch(IOException e) {
                        if(com.mucommander.Debug.ON) e.printStackTrace();

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
                    for(int i=0; i<subFiles.length && !isInterrupted(); i++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(subFiles[i]);
                        if(!processFile(subFiles[i], destFile))
                            isFolderEmpty = false;
                    }
                    // If one file could returned failure, return failure as well since this
                    // folder could not be moved totally
                    if(!isFolderEmpty)
                        return false;
                }
                catch(IOException e) {
                    if(com.mucommander.Debug.ON) e.printStackTrace();

                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_source", file.getAbsolutePath()));
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
                    if(com.mucommander.Debug.ON) e.printStackTrace();
					
                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_delete_folder", file.getAbsolutePath()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog returns false
                    return false;
                }
            } while(true);
        }
        // Move file
        else  {
            boolean append = false;

            // Tests if the file already exists in destination
            // and if it does, ask the user what to do or
            // use a previous user global answer.
            if (destFile.exists())  {
                int choice;
                // No default choice
                if(defaultFileExistsAction==-1) {
                    FileExistsDialog dialog = getFileExistsDialog(file, destFile, true);
                    choice = waitForUserResponse(dialog);
                    // If 'apply to all' was selected, this choice will be used
                    // for any files that already exist  (user will not be asked again)
                    if(dialog.applyToAllSelected())
                        defaultFileExistsAction = choice;
                }
                // Use previous choice
                else
                    choice = defaultFileExistsAction;
				
                // Cancel job
                if (choice==-1 || choice==FileExistsDialog.CANCEL_ACTION) {
                    stop();
                    return false;
                }
                // Skip file
                else if (choice==FileExistsDialog.SKIP_ACTION) {
                    return false;
                }
                // Append to file (resume file copy)
                else if (choice==FileExistsDialog.RESUME_ACTION) {
                    append = true;
                }
                // Overwrite file 
                else if (choice==FileExistsDialog.OVERWRITE_ACTION) {
                    // Do nothing, simply continue
                }
                //  Overwrite file if destination is older
                else if (choice==FileExistsDialog.OVERWRITE_IF_OLDER_ACTION) {
                    // Overwrite if file is newer (stricly)
                    if(file.getDate()<=destFile.getDate())
                        return false;
                }
            }

            // Let's try the easy way			
            if(!append && fileMove(file, destFile))
                return true;

            // if moveTo() returned false it wasn't possible to this method because of 'append',
            // try the hard way by copying the file first, and then deleting the source file
            if(tryCopyFile(file, destFile, append, errorDialogTitle) && !isInterrupted()) {
                // Preserve original file's date
                destFile.changeDate(file.getDate());

                // Delete the source file
                do {		// Loop for retry
                    try  {
                        file.delete();
                        // All OK
                        return true;
                    }
                    catch(IOException e) {
                        if(com.mucommander.Debug.ON) e.printStackTrace();

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
        return baseSourceFolder.isParent(folder) || baseDestFolder.isParent(folder);
    }

}
