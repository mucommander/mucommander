
package com.mucommander.job;

import com.mucommander.file.*;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.FileExistsDialog;

import com.mucommander.text.Translator;

import java.util.Vector;

import java.io.IOException;


/**
 * This job recursively moves a group of files.
 *
 * @author Maxence Bernard
 */
public class MoveJob extends ExtendedFileJob implements Runnable {

	/** New filename in destination */
	private String newName;

	/** Default choice when encountering an existing file */
	private int defaultFileExistsChoice = -1;

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
	 */
	public MoveJob(ProgressDialog progressDialog, MainFrame mainFrame, Vector files, AbstractFile destFolder, String newName) {
		super(progressDialog, mainFrame, files, destFolder);

		this.newName = newName;
		this.errorDialogTitle = Translator.get("move_dialog.error_title");
	}

	
	/////////////////////////////////////
	// Abstract methods Implementation //
	/////////////////////////////////////

    protected boolean processFile(AbstractFile file, AbstractFile destFolder, Object recurseParams[]) {
		if(isInterrupted())
            return false;
		
		// Notify job that we're starting to process this file
		nextFile(file);

		// Is current file at the base folder level ?
		boolean isFileInBaseFolder = file.getParent().equals(baseSourceFolder);

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
					int ret = showErrorDialog(errorDialogTitle, "Unable to delete symlink "+file.getAbsolutePath());
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
						int ret = showErrorDialog(errorDialogTitle, "Unable to create folder "+destFile.getAbsolutePath());
						// Retry loops
						if(ret==RETRY_ACTION)
							continue;
						// Cancel, skip or close dialog returns false
						return false;
					}
				} while(true);
			}
			
			// move each file in this folder recursively
			do {		// Loop for retry
				try {
					AbstractFile subFiles[] = file.ls();
					boolean isFolderEmpty = true;
					for(int i=0; i<subFiles.length && !isInterrupted(); i++)
						if(!processFile(subFiles[i], destFile, null))
							isFolderEmpty = false;
					// If one file could returned failure, return failure as well since this
					// folder could not be moved totally
					if(!isFolderEmpty)
						return false;
				}
				catch(IOException e) {
					int ret = showErrorDialog(errorDialogTitle, "Unable to read contents of folder "+file.getAbsolutePath());
					// Retry loops
					if(ret==RETRY_ACTION)
						continue;
					// Cancel, skip or close dialog returns false
					return false;
				}
			} while(true);

			
			// and finally deletes the empty folder
			do {		// Loop for retry
				try  {
					file.delete();
					return true;
				}
				catch(IOException e) {
					int ret = showErrorDialog(errorDialogTitle, "Unable to delete folder "+file.getAbsolutePath());
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
				if(defaultFileExistsChoice==-1) {
					FileExistsDialog dialog = getFileExistsDialog(file, destFile);
					choice = waitForUserResponse(dialog);
					// If 'apply to all' was selected, this choice will be used
					// for any files that already exist  (user will not be asked again)
					if(dialog.applyToAllSelected())
						defaultFileExistsChoice = choice;
				}
				// Use previous choice
				else
					choice = defaultFileExistsChoice;
				
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
				else if (choice==FileExistsDialog.APPEND_ACTION) {
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
			// try the hard way
			if(tryCopyFile(file, destFile, append, errorDialogTitle)) {
				do {		// Loop for retry
					try  {
						file.delete();
						// All OK
						return true;
					}
					catch(IOException e) {
						int ret = showErrorDialog(errorDialogTitle, "Unable to delete file "+file.getAbsolutePath());
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

/*
		// Do nothing if file is a symlink (skip file)
		if(file.isSymlink())
			;
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
						// Retry : loop
						if(ret==RETRY_ACTION)
							continue;
						// Cancel or close dialog : stop job and return
						else if(ret==-1 || ret==CANCEL_ACTION)		
							stop();
						return;		// Return for skip and cancel
					}
					break;
				} while(true);
			}
			
			// and copy each file in this folder recursively
			do {		// Loop for retry
				try {
					// for each file in folder...
					AbstractFile subFiles[] = file.ls();
					for(int i=0; i<subFiles.length && !isInterrupted(); i++)
						processFile(subFiles[i], destFile, null);
				}
				catch(IOException e) {
					// Unable to open source file
					int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_folder", destFile.getName()));
					// Retry : loop
					if(ret==RETRY_ACTION)
						continue;
					// Cancel or close dialog : stop job and return
					else if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
						stop();
					// Do nothing for skip
				}
				break;
			} while(true);
        }
		// Copy file
        else  {
			boolean append = false;
			
			// Tests if the file already exists in destination
			// and if it does, ask the user what to do or
			// use a previous user global answer.
			if (destFile.exists())  {
				int choice;
				// No default choice
				if(defaultFileExistsChoice==-1) {
					FileExistsDialog dialog = getFileExistsDialog(file, destFile);
					choice = waitForUserResponse(dialog);
					// If 'apply to all' was selected, this choice will be used
					// for any files that already exist  (user will not be asked again)
					if(dialog.applyToAllSelected())
						defaultFileExistsChoice = choice;
				}
				// Use previous choice
				else
					choice = defaultFileExistsChoice;
				
				// Cancel job
				if (choice==-1 || choice==FileExistsDialog.CANCEL_ACTION) {
					stop();
					return;
				}
				// Skip file
				else if (choice==FileExistsDialog.SKIP_ACTION) {
					return;
				}
				// Append to file (resume file copy)
				else if (choice==FileExistsDialog.APPEND_ACTION) {
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
						return;
				}
			}
			
			// Copy file to destination
			do {				// Loop for retry
				try {
					copyFile(file, destFile, append);
				}
				catch(FileJobException e) {
					// Copy failed
					if(com.mucommander.Debug.ON)
						System.out.println(""+e);
					
					int reason = e.getReason();
					String errorMsg;
					switch(reason) {
						// Could not open source file for read
						case FileJobException.CANNOT_OPEN_SOURCE:
							errorMsg = Translator.get("cannot_read_source", file.getName());
							break;
						// Could not open destination file for write
						case FileJobException.CANNOT_OPEN_DESTINATION:
							errorMsg = Translator.get("cannot_write_destination", file.getName());
							break;
						// An error occurred during file transfer
						case FileJobException.ERROR_WHILE_TRANSFERRING:
						default:
							errorMsg = Translator.get("error_while_transferring", file.getName());
							break;
					}
					
					// Ask the user what to do
					int ret = showErrorDialog(errorDialogTitle, errorMsg);
					if(ret==RETRY_ACTION) {
						// Resume transfer
						if(reason==FileJobException.ERROR_WHILE_TRANSFERRING)
							append = true;
						continue;
					}
					else if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
						stop();
				}
				break;
			} while(true);
		}
*/
	}

	
	/**
	 * Tries to move the file with AbstractFile.moveTo() 
	 * skipping the whole manual recursive process
	 */
	public boolean fileMove(AbstractFile file, AbstractFile destFile) {
		try {
			if(file.moveTo(destFile))
				return true;		// return true in case of success
		} catch(IOException e) {}
		
		return false;
	}

	
    public String getStatusString() {
        return Translator.get("move.moving_file", getCurrentFileInfo());
    }
}
