
package com.mucommander.job;

import com.mucommander.file.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.comp.dialog.YBoxPanel;
import com.mucommander.text.SizeFormatter;

import java.io.IOException;
import java.util.Vector;

import javax.swing.*;


/**
 * This class is responsible for deleting recursively a group of files.
 *
 * @author Maxence Bernard
 */
public class DeleteJob extends FileJob implements Runnable, FileModifier {
    
	/** Default choice when encountering an existing file */
	private int defaultFileExistsChoice = -1;

	/** Title used for error dialogs */
	private String errorDialogTitle;


	private final static int DELETE_LINK_ACTION = 0;
	private final static int DELETE_FOLDER_ACTION = 1;
	private final static int CANCEL_ACTION = 2;
	private final static int SKIP_ACTION = 3;

	private final static String DELETE_LINK_TEXT = "Delete Link only";
	private final static String DELETE_FOLDER_TEXT = "Delete Folder";
	private final static String CANCEL_TEXT = "Cancel";
	private final static String SKIP_TEXT = "Skip";

	
    /**
	 * Creates a new DeleteJob without starting it.
	 *
	 * @param progressDialog dialog which shows this job's progress
	 * @param mainFrame mainFrame this job has been triggered by
	 * @param files files which are going to be deleted
	 * @param destFolder destination folder where the files will be moved
	 */
    public DeleteJob(ProgressDialog progressDialog, MainFrame mainFrame, Vector files, AbstractFile destFolder) {
		super(progressDialog, mainFrame, files, destFolder);

		this.errorDialogTitle = Translator.get("delete_dialog.error_title");
	}

	
	/**
	 * Deletes recursively the given file or folder. 
	 *
	 * @param file the file or folder to delete
	 * @param recurseParams not used
	 * 
	 * @return <code>true</code> if the file has been completely deleted.
	 */
    private boolean processFile(AbstractFile file, Object recurseParams[]) {
		String filePath = file.getAbsolutePath();
		filePath = filePath.substring(baseFolderPath.length()+1, filePath.length());

		if(isInterrupted())
            return false;

		int ret;
		boolean followSymlink = false;
        if(file.isDirectory()) {
			// If folder is a symlink, asks the user what to do
			boolean isSymlink = file.isSymlink();
			if(isSymlink) {
				ret = showSymlinkDialog(filePath, file.getCanonicalPath());
				if(ret==-1 || ret==CANCEL_ACTION) {
					stop();
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
						for(int i=0; i<subFiles.length && !isInterrupted(); i++)
							deleteRecurse(subFiles[i]);
					}
					catch(IOException e) {
						if(com.mucommander.Debug.ON) e.printStackTrace();

						ret = showErrorDialog(errorDialogTitle, "Unable to read contents of folder "+filePath);
						// Retry loops
						if(ret==RETRY_ACTION)
							continue;
						// Cancel, skip or close dialog returns false
						return false;
					}
				} while(true);
			}
        }
        
        if(isInterrupted())
            return false;

		do {		// Loop for retry
			try {
				// If file is a symlink to a folder and the user asked to follow the symlink,
				// delete the empty folder
				if(followSymlink) {
					AbstractFile canonicalFile = AbstractFile.getAbstractFile(file.getCanonicalPath());
					if(canonicalFile!=null)
						canonicalFile.delete();
				}
	
				file.delete();
				return true;
			}
			catch(IOException e) {
				if(com.mucommander.Debug.ON) e.printStackTrace();
	
				ret = showErrorDialog(errorDialogTitle, "Unable to delete "
					+(file.isDirectory()?"folder ":"file ")
					+file.getName());
				// Retry loops
				if(ret==RETRY_ACTION)
					continue;
				// Cancel, skip or close dialog returns false
				return false;
			}
		} while(true);
    }

	
	private int showSymlinkDialog(String relativePath, String canonicalPath) {
		YBoxPanel panel = new YBoxPanel();
		panel.add(new JLabel("This file looks like a symbolic link."));
		panel.add(new JLabel(" "));
		panel.add(new JLabel("Delete symlink only (safe) or"));
		panel.add(new JLabel("follow symlink and delete folder (caution) ?"));
		panel.add(new JLabel(" "));
		panel.add(new JLabel("  File: "+relativePath));
		panel.add(new JLabel("  Links to: "+canonicalPath));
		
		QuestionDialog dialog = new QuestionDialog(progressDialog, "Symlink found", panel, mainFrame,
			new String[] {DELETE_LINK_TEXT, DELETE_FOLDER_TEXT, SKIP_TEXT, CANCEL_TEXT},
			new int[]  {DELETE_LINK_ACTION, DELETE_FOLDER_ACTION, SKIP_ACTION, CANCEL_ACTION},
			2);
	
	    return waitForUserResponse(dialog);
	}
	

	
    public void run() {
        currentFileIndex = 0;
        int numFiles = filesToDelete.size();

        FileTable activeTable = mainFrame.getLastActiveTable();
        AbstractFile currentFile;
        while(!isInterrupted()) {
            currentFile = (AbstractFile)filesToDelete.elementAt(currentFileIndex);

			// if current file or folder was successfully deleted, remove it from file table
			if (deleteRecurse(currentFile))
            	activeTable.excludeFile(currentFile);
			// else unmark it
			else
				activeTable.setFileMarked(currentFile, false);

			activeTable.repaint();
			
			if(currentFileIndex<numFiles-1)	// This ensures that currentFileIndex is never out of bounds (cf getCurrentFile)
                currentFileIndex++;
            else break;
        }

        stop();

		// Refreshes FileTables if necessary
		try {
        	activeTable.refresh();
        }
        catch(IOException e) {
        	// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
        	// like switching to a root folder        
        }
        
        // Refreshes the other file table if needed, that is if is 'below' the active table
        FileTable unactiveTable = mainFrame.getUnactiveTable();
        if (unactiveTable.getCurrentFolder().getAbsolutePath().startsWith(activeTable.getCurrentFolder().getAbsolutePath()))  {
        	try {			
        		unactiveTable.refresh();
        	}
        	catch(IOException e) {
        		// If folder cannot be read, changes the folder to the one of active table
        		// since the unactive one is 'below' the active one.
        		FolderPanel folderPanel = unactiveTable.getBrowser();
        		folderPanel.setCurrentFolder(activeTable.getCurrentFolder(), true);
        	}
        }
    
		cleanUp();
	}


    public String getStatusString() {
        return Translator.get("delete.deleting_file", getCurrentFileInfo());
    }

}	
