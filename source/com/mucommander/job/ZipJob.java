
package com.mucommander.job;

import com.mucommander.file.AbstractFile;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;

import java.io.*;
import java.util.zip.*;
import java.util.Vector;

/**
 * This FileJob is responsible for compressing a group files in the zip format.
 */
public class ZipJob extends ExtendedFileJob implements Runnable, FileModifier {

	/** Files which are going to be zipped */
    private Vector filesToZip;
	
	/** Destination (zip) file */
	private AbstractFile destFile;

	/** Base destination folder */
	private String baseFolderPath;

	/** Zip output stream */
	private ZipOutputStream zipOut;
	
	/** Title used for error dialogs */
	private String errorDialogTitle;
	
	
    public ZipJob(ProgressDialog progressDialog, MainFrame mainFrame, Vector filesToZip, String zipComment, AbstractFile destFile) {
        super(progressDialog, mainFrame);
		
		this.filesToZip = filesToZip;
		this.destFile = destFile;
		this.zipComment = zipComment;

//		this.baseFolderPath = ((AbstractFile)filesToZip.elementAt(0)).getParent().getAbsolutePath();
		this.baseFolderPath = baseSourceFolder.getAbsolutePath();
		this.errorDialogTitle = Translator.get("zip_dialog.error_title");
	}


	private boolean zipRecurse(AbstractFile file) {
		if(isInterrupted())
			return false;

		if(zipOut==null) {
			boolean append = false;
			if (destFile.exists()) {
				// File already exists: cancel, append or overwrite?
				QuestionDialog dialog = new QuestionDialog(mainFrame, Translator.get("warning"), Translator.get("zip_dialog.file_already_exists", destFile.getName()), mainFrame,
					new String[] {Translator.get("cancel"), Translator.get("replace")},
					new int[]  {CANCEL_ACTION, REPLACE_ACTION},
					0);
				int ret = dialog.getActionValue();
				
				// User cancelled
				if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
					return;
			}

			java.io.OutputStream destOut = null;
			// Tries to open zip/destination file
			try {
				destOut = destFile.getOutputStream(false);
			}
			catch(Exception ex) {
				QuestionDialog dialog = new QuestionDialog(mainFrame, Translator.get("zip_dialog.error_title"), Translator.get("zip_dialog.cannot_write"), mainFrame,
					new String[] {Translator.get("ok")},
					new int[]  {0},
					0);
				dialog.getActionValue();
				return;
			}

			zipOut = new ZipOutputStream(destFile.getOutputStream());
			if(zipComment!=null && !zipComment.equals(""))
				zipOut.setComment(zipComment);
		}
		
		currentFileProcessed = 0;
		currentFileSize = file.getSize();
        
		String filePath = file.getAbsolutePath();
		String zipEntryRelativePath = filePath.substring(baseFolderPath.length()+1, filePath.length());
//		currentFileInfo = "\""+file.getName()+"\" ("+SizeFormatter.format(currentFileSize, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";
		
		try {
			if (file.isDirectory() && !file.isSymlink()) {
				// Create directory entry
				zipOut.putNextEntry(new ZipEntry(zipEntryRelativePath.replace('\\', '/')+"/"));
				
				AbstractFile subFiles[] = file.ls();
				boolean folderComplete = true;
				for(int i=0; i<subFiles.length && !isInterrupted(); i++)
					if(!zipRecurse(subFiles[i]))
						folderComplete = false;
				
				return folderComplete;
			}
			else {
				InputStream in = file.getInputStream();
				currentFileSize = file.getSize();
				int nbRead;
				currentFileProcessed = 0;

				zipOut.putNextEntry(new ZipEntry(zipEntryRelativePath.replace('\\', '/')));
				while ((nbRead=in.read(buffer, 0, buffer.length))!=-1) {
					zipOut.write(buffer, 0, nbRead);
                    nbBytesProcessed += nbRead;
					currentFileProcessed += nbRead;
				}
                			
				return true;
			}
		}
		catch(IOException e) {
			int ret = showErrorDialog(errorDialogTitle, "Error while adding "+file.getAbsolutePath());
			if(ret==-1 || ret==CANCEL_ACTION) {		// CANCEL_ACTION or close dialog
			    stop();
			}              
			return false;
		}
	}

	/**
	 * Override stop method to catch zip output stream.
	 */
	public void stop() {
		
		// Try to close ZipOutputStream
		if(zipOut!=null) {
			try { zipOut.close(); }
			catch(IOException e) {}
		}
		
		// and call parent method
		super.stop();
	}
	
	
/*
    public void run() {
        currentFileIndex = 0;
        int numFiles = filesToZip.size();

		AbstractFile currentFile;
        FileTable activeTable = mainFrame.getLastActiveTable();
		int rowCount = activeTable.getRowCount();
		
		while(!isInterrupted()) {
            currentFile = (AbstractFile)filesToZip.elementAt(currentFileIndex);
			zipRecurse(currentFile);
			
			activeTable.setFileMarked(currentFile, false);
			activeTable.repaint();
	            
			if(currentFileIndex<numFiles-1)	// This ensures that currentFileIndex is never out of bounds (cf getCurrentFile)
                currentFileIndex++;
            else break;
        }
    
		stop();
		
		try {
			zipOut.close();
		}
		catch(IOException e) {
		}

		FileTable table1 = mainFrame.getFolderPanel1().getFileTable();
		// Refreshes table1 only if folder is destFolder
		if (table1.getCurrentFolder().equals(destFolder))
			try { table1.refresh();	}
			catch(IOException e) {
				// Probably should do something when a folder becomes unreadable
			}

		FileTable table2 = mainFrame.getFolderPanel2().getFileTable();
		// Refreshes table2 only if folder is destFolder
		if (table2.getCurrentFolder().equals(destFolder))
			try { table2.refresh();	}
			catch(IOException e) {
				// Probably should do something when a folder becomes unreadable
			}

				
//		mainFrame.getLastActiveTable().requestFocus();
		cleanUp();
	}
*/


    public String getStatusString() {
		return "Adding "+currentFileInfo;
    }

}	
