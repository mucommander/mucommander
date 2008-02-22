package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractRWArchiveFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.dialog.file.RenameDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;

/**
 * This class is the parent class of {@link com.mucommander.job.CopyJob} and {@link com.mucommander.job.MoveJob} and
 * allows them to share methods and fields.
 *
 * @author Maxence Bernard, Mariusz Jakubowski
 * @see com.mucommander.job.CopyJob
 * @see com.mucommander.job.MoveJob
 */
public abstract class AbstractCopyJob extends TransferFileJob {
    
    /** Base destination folder */
    protected AbstractFile baseDestFolder;
    
    /** New filename in destination */
    protected String newName;

    /** Default choice when encountering an existing file */
    protected int defaultFileExistsAction = FileCollisionDialog.ASK_ACTION;
    
    /** Title used for error dialogs */
    protected String errorDialogTitle;
    
    protected boolean append;
    
    /** The archive that contains the destination files (may be null) */
    protected AbstractRWArchiveFile archiveToOptimize;

    /** True when an archive is being optimized */
    protected boolean isOptimizingArchive;

    /**
     * Creates a new <code>AbstractCopyJob</code>.
     *
     * @param progressDialog dialog which shows this job's progress
     * @param mainFrame mainFrame this job has been triggered by
     * @param files files which are going to be copied
     * @param destFolder destination folder where the files will be copied
     * @param newName the new filename in the destination folder, can be <code>null</code> in which case the original filename will be used.
     * @param fileExistsAction default action to be performed when a file already exists in the destination, see {@link com.mucommander.ui.dialog.file.FileCollisionDialog} for allowed values
     */
    public AbstractCopyJob(ProgressDialog progressDialog, MainFrame mainFrame,
            FileSet files, AbstractFile destFolder, String newName, int fileExistsAction) {
        super(progressDialog, mainFrame, files);

        this.baseDestFolder = destFolder;
        this.newName = newName;        
        this.defaultFileExistsAction = fileExistsAction;
    }

    /**
     * Creates a destination file given a destination folder and a new file name.
     * @param destFolder a destination folder
     * @param destFileName a destination file name
     * @return the destination file or null if it cannot be created
     */
    protected AbstractFile createDestinationFile(AbstractFile destFolder,
            String destFileName) {
        AbstractFile destFile;
        do {    // Loop for retry
            try {
                destFile = destFolder.getDirectChild(destFileName);
                break;
            }
            catch(IOException e) {
                // Destination file couldn't be instanciated

                int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_write_file", destFileName));
                // Retry loops
                if(ret==RETRY_ACTION)
                    continue;
                // Cancel or close dialog return false
                return null;
                // Skip continues
            }
        } while(true);
        return destFile;
    }
    
    /**
     * Checks if there is a file collision (file exists in the destination).
     * If there is no collision this method returns destFile.
     * If there is a collision this method returns: <ul>
     *  <li>null if a user cancelled the transfer 
     *  <li>null if a user skipped the file
     *  <li>destFile if a user resumed the transfer (and sets append flag)
     *  <li>destFile if a user has chosen to overwrite the file
     *  <li>new file if a user renamed the file
     *  </ul>
     * @param file a source file
     * @param destFolder a destination folder
     * @param destFile a destination file
     * @param allowCaseVariation if true,
     * @return destFile the new destination file
     */
    protected AbstractFile checkForCollision(AbstractFile file, AbstractFile destFolder, AbstractFile destFile, boolean allowCaseVariation) {
        append = false;
        while (true) {
            // Check for file collisions (file exists in the destination, destination subfolder of source, ...)
            // if a default action hasn't been specified
            int collision = FileCollisionChecker.checkForCollision(file, destFile);
            
            // If allowCaseVariation is true and both files are equal, test if the destination filename is a variation
            // of the original filename with a different case. If that is the case, do not warn about the source and
            // destination being the same.
            if(allowCaseVariation && collision==FileCollisionChecker.SAME_SOURCE_AND_DESTINATION) {
                String sourceFileName = file.getName();
                String destFileName = destFile.getName();
                if(sourceFileName.equalsIgnoreCase(destFileName) && !sourceFileName.equals(destFileName))
                    break;
            }
            
            // Handle collision, asking the user what to do or using a default action to resolve the collision 
            if(collision != FileCollisionChecker.NO_COLLOSION) {
                int choice;
                // Use default action if one has been set, if not show up a dialog
                if(defaultFileExistsAction==FileCollisionDialog.ASK_ACTION) {
                    FileCollisionDialog dialog = new FileCollisionDialog(progressDialog, mainFrame, collision, file, destFile, true, true);
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
                    return null;
                }
                // Skip file
                else if (choice== FileCollisionDialog.SKIP_ACTION) {
                    return null;
                }
                // Append to file (resume file copy)
                else if (choice== FileCollisionDialog.RESUME_ACTION) {
                    append = true;
                    break;
                }
                // Overwrite file
                else if (choice== FileCollisionDialog.OVERWRITE_ACTION) {
                    // Do nothing, simply continue
                    break;
                }
                //  Overwrite file if destination is older
                else if (choice== FileCollisionDialog.OVERWRITE_IF_OLDER_ACTION) {
                    // Overwrite if file is newer (stricly)
                    if(file.getDate()<=destFile.getDate())
                        return null;
                    break;
                } else if (choice == FileCollisionDialog.RENAME_ACTION) {
                    setPaused(true);
                    RenameDialog dlg = new RenameDialog(mainFrame, destFile);
                    setPaused(false);
                    String destFileName = dlg.getNewName();
                    if (destFileName != null) {
                        destFile = createDestinationFile(destFolder, destFileName);
                    } else {
                        // turn on FileCollisionDialog, so we don't loop indefinitely
                        defaultFileExistsAction = FileCollisionDialog.ASK_ACTION;
                    }
                    // continue with collision checking
                    continue;
                }
            }
            break;    // no collision
        }
        return destFile;
    }
    
    /**
     * Optimizes the given writable archive file and notifies the user in case of an error.
     *
     * @param rwArchiveFile the writable archive file to optimize
     */
    protected void optimizeArchive(AbstractRWArchiveFile rwArchiveFile) {
        isOptimizingArchive = true;

        while(true) {
            try {
                archiveToOptimize = rwArchiveFile;
                archiveToOptimize.optimizeArchive();

                break;
            }
            catch(IOException e) {
                if(showErrorDialog(errorDialogTitle, Translator.get("error_while_optimizing_archive", rwArchiveFile.getName()))==RETRY_ACTION)
                    continue;

                break;
            }
        }

        isOptimizingArchive = false;
    }

}
