
package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileSet;
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
            selectFileWhenFinished(FileFactory.getFile(destFolder.getAbsolutePath(true)+newName));
    }

	
    /**
     * Moves the file with AbstractFile.moveTo() if it is more efficient than copying the streams,
     * skipping the whole manual recursive process.
     *
     * @return <code>true</code> if the file has been moved using AbstractFile.moveTo()
     */
    private boolean fileMove(AbstractFile file, AbstractFile destFile) {
        int moveToHint = file.getMoveToHint(destFile);
        
        if(!(moveToHint==AbstractFile.SHOULD_HINT || moveToHint==AbstractFile.MUST_HINT))
            return false;
        
        try {
            file.moveTo(destFile);
            return true; 
        } catch(IOException e) { 
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e);
            return false;
        }
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
        boolean overwrite = false;

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
                stop();
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
                overwrite = true;
            }
            //  Overwrite file if destination is older
            else if (choice== FileCollisionDialog.OVERWRITE_IF_OLDER_ACTION) {
                // Overwrite if file is newer (stricly)
                if(file.getDate()<=destFile.getDate())
                    return false;
                overwrite = true;
            }
        }


        // Move directory recursively
        if(file.isDirectory()) {
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

            // FTP overwrite bug workaround: if the destination file is not deleted, the existing destination
            // file is renamed to <filename>.1
            // TODO: fix this in the commons-net library
            if(overwrite && destFile.getURL().getProtocol().equals("ftp")) {
                try { destFile.delete(); }
                catch(IOException e) {};
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

}
