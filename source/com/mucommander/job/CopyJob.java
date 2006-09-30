
package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.FileExistsDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;

import java.io.IOException;


/**
 * This job recursively copies (or unpacks) a group of files.
 *
 * @author Maxence Bernard
 */
public class CopyJob extends ExtendedFileJob {

    /** Base destination folder */
    protected AbstractFile baseDestFolder;

    /** New filename in destination */
    private String newName;

    /** Default choice when encountering an existing file */
    private int defaultFileExistsAction = FileExistsDialog.ASK_ACTION;

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

        // If this job correponds to a 'local copy' of a single file and in the same directory, 
        // select the copied file in the active table after this job has finished (and hasn't been cancelled)
        if(files.size()==1 && newName!=null && destFolder.equals(files.fileAt(0).getParent()))
            selectFileAfter(FileFactory.getFile(destFolder.getAbsolutePath(true)+newName));
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
        if(isInterrupted())
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
                    for(int j=0; j<archiveFiles.length && !isInterrupted(); j++) {
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
                    int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_source", currentFile.getName()));
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
                int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_write_destination", destFileName));
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
        // Copy directory recursively
        else if(file.isDirectory()) {
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
                    for(int i=0; i<subFiles.length && !isInterrupted(); i++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(subFiles[i]);
                        processFile(subFiles[i], destFile);
                    }

                    // Only when finished with folder, set folder's date to original folder's
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
        // Copy file
        else  {
            boolean append = false;
            boolean overwrite = false;
            int choice;

            // Tests if the file already exists in destination
            // and if it does, ask the user what to do or
            // use a previous user global answer.
            if (destFile.exists())  {
                // No default choice
                if(defaultFileExistsAction==FileExistsDialog.ASK_ACTION) {
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
				
                // Cancel, skip or close dialog
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
                    overwrite = true;
                }
                //  Overwrite file if destination is older
                else if (choice==FileExistsDialog.OVERWRITE_IF_OLDER_ACTION) {
                    // Overwrite if file is newer (stricly)
                    if(file.getDate()<=destFile.getDate())
                        return true;
                    overwrite = true;
                }
            }

            // FTP overwrite bug workaround: if the destination file is not deleted, the existing destination
            // file is renamed to <filename>.1
            // TODO: fix this in the commons-net library
            if(overwrite && destFile.getURL().getProtocol().equals("ftp")) {
                try { destFile.delete(); }
                catch(IOException e) {}
            }

            // Copy the file
            boolean success = tryCopyFile(file, destFile, append, errorDialogTitle);
			
            // Preserve original file's date
            destFile.changeDate(file.getDate());
			
            return success;
        }
    }

    public String getStatusString() {
        return Translator.get(mode==UNPACK_MODE?"unpack_dialog.unpacking_file":mode==DOWNLOAD_MODE?"download_dialog.downloading_file":"copy_dialog.copying_file", getCurrentFileInfo());
    }
	
    // This job modifies baseDestFolder and its subfolders
	
    protected boolean hasFolderChanged(AbstractFile folder) {
        return baseDestFolder.isParentOf(folder);
    }

}
