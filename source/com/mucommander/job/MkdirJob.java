
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

		// Stop if interrupted (although there is no way to stop the job at this time)
		if(isInterrupted())
            return false;

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating "+destFolder+" "+dirName);

		try {
			// Create directory
	        destFolder.mkdir(dirName);
			
			// Refresh and selects newly created folder in active table
			AbstractFile newFolder = AbstractFile.getAbstractFile(destFolder.getAbsolutePath(true)+dirName);
			FileTable table1 = mainFrame.getFolderPanel1().getFileTable();
			FileTable table2 = mainFrame.getFolderPanel2().getFileTable();
			FileTable lastActiveTable = mainFrame.getLastActiveTable();
			for(FileTable table=table1; table!=null; table=table==table1?table2:null) {
				if(destFolder.equals(table.getCurrentFolder())) {
					table.getFolderPanel().tryRefresh(lastActiveTable==table?newFolder:null);
				}
			}
			
//			.getFolderPanel().requestFocus();
			
			return true;		// Success
		}
	    catch(IOException e) {
	        showErrorDialog(Translator.get("mkdir_dialog.error_title"), Translator.get("cannot_create_folder", destFolder.getAbsolutePath(true)+dirName));
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
		// Refresh is done manually
		return false;
	}
}