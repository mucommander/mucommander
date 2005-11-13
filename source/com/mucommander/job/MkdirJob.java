
package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import com.mucommander.text.Translator;

import java.io.IOException;


/**
 * This FileJob creates a new directory in a given folder.
 *
 * @author Maxence Bernard
 */
public class MkdirJob extends FileJob {
	
	private AbstractFile destFolder;
	private String dirName;
	


	public MkdirJob(MainFrame mainFrame, FileSet fileSet, String dirName) {
		super(mainFrame, fileSet);

		this.destFolder = fileSet.getBaseFolder();
		this.dirName = dirName;
		
		setAutoUnmark(false);
	}


	/////////////////////////////////////
	// Abstract methods Implementation //
	/////////////////////////////////////

	/**
	 * Creates the new directory in the destination folder.
	 */
	protected boolean processFile(AbstractFile file, Object recurseParams) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Called");

		// Stop if interrupted
		if(isInterrupted())
            return false;

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating "+destFolder+" "+dirName);

		try {
			// Create directory
	        destFolder.mkdir(dirName);
			
//			// Refresh current folder
//			FileTable lastActiveTable = mainFrame.getLastActiveTable();
//			lastActiveTable.getFolderPanel().tryRefresh();

//			// Selects newly created folder
//			lastActiveTable.selectFile(AbstractFile.getAbstractFile(destFolder.getAbsolutePath(true)+dirName));

//			mainFrame.getLastActiveTable().getFolderPanel().requestFocus();
			
			return true;		// Success
		}
	    catch(IOException e) {
	        showErrorDialog(Translator.get("cannot_create_folder", destFolder.getAbsolutePath()), Translator.get("mkdir_dialog.error_title"));
			return false;		// Failure
		}    
	}


	/**
	 * Not used.
	 */
	public String getStatusString() {
		return null;
	}

	protected boolean hasFolderChanged(AbstractFile folder) {
		return destFolder.equals(folder);
	}
}