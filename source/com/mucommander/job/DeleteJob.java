
package com.mucommander.job;

import com.mucommander.file.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.comp.dialog.YBoxPanel;
import com.mucommander.text.SizeFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.IOException;
import java.util.Vector;

/**
 * This class is responsible for deleting recursively a group of files.
 */
public class DeleteJob extends FileJob implements Runnable, FileModifier {
    
    /** Files to be deleted */
    private Vector filesToDelete;
    
    /** Number of files to be deleted */
    private int nbFiles;
    
    /** Index of file currently being deleted */ 
	private int currentFileIndex;
	private String currentFileInfo = "";
    
    private MainFrame mainFrame;
	private String baseFolderPath;

	private final static int DELETE_LINK_ACTION = 0;
	private final static int DELETE_FOLDER_ACTION = 1;
	private final static int CANCEL_ACTION = 2;
	private final static int SKIP_ACTION = 3;

	private final static String DELETE_LINK_TEXT = "Delete Link only";
	private final static String DELETE_FOLDER_TEXT = "Delete Folder";
	private final static String CANCEL_TEXT = "Cancel";
	private final static String SKIP_TEXT = "Skip";

	
    public DeleteJob(MainFrame mainFrame, ProgressDialog progressDialog, Vector filesToDelete) {
		super(progressDialog, mainFrame);

		this.filesToDelete = filesToDelete;
		this.nbFiles = filesToDelete.size();
        this.mainFrame = mainFrame;
	
		this.baseFolderPath = ((AbstractFile)filesToDelete.elementAt(0)).getParent().getAbsolutePath();
	}

	/**
	 * Deletes recursively the given file or folder. 
	 *
	 * @return <code>true</code> if the file has completely been deleted.
	 */
    private boolean deleteRecurse(AbstractFile file) {
		String filePath = file.getAbsolutePath();
		filePath = filePath.substring(baseFolderPath.length()+1, filePath.length());

        currentFileInfo = "\""+file.getName()+"\" ("+SizeFormatter.format(file.getSize(), SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";
				
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
				// Delete each file in this folder
				try {
					AbstractFile subFiles[] = file.ls();
					for(int i=0; i<subFiles.length && !isInterrupted(); i++)
						deleteRecurse(subFiles[i]);
				}
				catch(IOException e) {
					ret = showErrorDialog("Unable to read contents of folder "+filePath);
					if(ret==-1 || ret==CANCEL_ACTION)	// CANCEL_ACTION or close dialog
						stop();
					return false;
				}
			}
        }
        
        if(isInterrupted())
            return false;

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
			if(com.mucommander.Debug.ON)
				e.printStackTrace();
			
            ret = showErrorDialog("Unable to delete "
				+(file.isDirectory()?"folder ":"file ")
				+file.getName());
            if(ret==-1 || ret==CANCEL_ACTION) // CANCEL_ACTION or close dialog
                stop();                
			return false;
        }
    }


    public int getNbFiles() {
        return nbFiles;
    }

    public int getCurrentFileIndex() {
        return currentFileIndex;
    }

    public long getTotalBytesProcessed() {
        return -1;
    }

    public String getStatusString() {
		return "Deleting "+currentFileInfo;
    }
	
	
    private int showErrorDialog(String message) {
		QuestionDialog dialog = new QuestionDialog(progressDialog, "Delete error", message, mainFrame,
			new String[] {SKIP_TEXT, CANCEL_TEXT},
			new int[]  {SKIP_ACTION, CANCEL_ACTION},
			0);
	
	    return waitForUserResponse(dialog);
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

        // Important!
        waitForDialog();

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
    
        activeTable.requestFocus();
	}
}	
