
package com.mucommander.job;

import com.mucommander.file.AbstractFile;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.text.Translator;

import java.io.*;
import java.util.zip.*;
import java.util.Vector;


/**
 * This FileJob is responsible for compressing a set of files into a zip file.
 *
 * @author Maxence Bernard
 */
public class ZipJob extends ExtendedFileJob {

	/** Files which are going to be zipped */
    private Vector filesToZip;
	
	/** Destination (zip) file */
	private AbstractFile destFile;

	/** Base destination folder */
	private String baseFolderPath;

	/** Zip output stream */
	private ZipOutputStream zipOut;
	
	/** Optional zip comment */
	private String zipComment;
	
	/** Title used for error dialogs */
	private String errorDialogTitle;
	
	/** Replace action's text */
	private String REPLACE_TEXT = Translator.get("replace");
	/** Replace action's value */
	private int REPLACE_ACTION = 100;
	

    public ZipJob(ProgressDialog progressDialog, MainFrame mainFrame, Vector filesToZip, String zipComment, AbstractFile destFile) {
        super(progressDialog, mainFrame, filesToZip);
		
		this.filesToZip = filesToZip;
		this.destFile = destFile;
		this.zipComment = zipComment;

//		this.baseFolderPath = ((AbstractFile)filesToZip.elementAt(0)).getParent().getAbsolutePath();
		this.baseFolderPath = baseSourceFolder.getAbsolutePath();
		this.errorDialogTitle = Translator.get("zip_dialog.error_title");
	}

	
	/**
	 * Overriden method to initialize zip output stream.
	 */
	protected void jobStarted() {
		if (destFile.exists()) {
			// File already exists: cancel, replace?
			int choice = showErrorDialog(Translator.get("warning"),
				Translator.get("zip_dialog.file_already_exists", destFile.getName()),
				new String[] {CANCEL_TEXT, REPLACE_TEXT},
				new int[]  {CANCEL_ACTION, REPLACE_ACTION}
			);
			
			// Cancel or close returns false
			if(choice==-1 || choice==CANCEL_ACTION)
				return;
			// Replace simply continues
		}

		// Tries to open zip/destination file
		java.io.OutputStream destOut = null;
		// Loop for retry
		do {
			try {
				zipOut = new ZipOutputStream(destFile.getOutputStream(false));
				if(zipComment!=null && !zipComment.equals(""))
					zipOut.setComment(zipComment);
				break;
			}
			catch(Exception ex) {
				int choice = showErrorDialog(Translator.get("warning"),
					Translator.get("zip_dialog.cannot_write", destFile.getName()),
					new String[] {CANCEL_TEXT, RETRY_TEXT},
					new int[]  {CANCEL_ACTION, RETRY_ACTION}
				);
			
				// Retry loops
				if(choice == RETRY_ACTION)
					continue;
				// Cancel or close dialog returns false
				return;
			}
		} while(true);
	}
	
	
	protected boolean processFile(AbstractFile file, Object recurseParams) {
		if(isInterrupted())
			return false;

		String filePath = file.getAbsolutePath();
		String zipEntryRelativePath = filePath.substring(baseFolderPath.length()+1, filePath.length());

		// Process current file
		do {		// Loop for retry
			try {
				if (file.isDirectory() && !file.isSymlink()) {
					// Create new directory entry in zip file
					zipOut.putNextEntry(new ZipEntry(zipEntryRelativePath.replace('\\', '/')+"/"));
					
					// Recurse on files
					AbstractFile subFiles[] = file.ls();
					boolean folderComplete = true;
					for(int i=0; i<subFiles.length && !isInterrupted(); i++) {
						// Notify job that we're starting to process this file (needed for recursive calls to processFile)
						nextFile(subFiles[i]);
						if(!processFile(subFiles[i], null))
							folderComplete = false;
					}
					
					return folderComplete;
				}
				else {
					InputStream in = file.getInputStream();

					// Create new file entry in zip file
					zipOut.putNextEntry(new ZipEntry(zipEntryRelativePath.replace('\\', '/')));
					copyStream(in, zipOut);
								
					return true;
				}
			}
			catch(IOException e) {
				int ret = showErrorDialog(errorDialogTitle, "Error while adding "+file.getAbsolutePath());
				// Retry loops
				if(ret==RETRY_ACTION)
					continue;
				// Cancel, skip or close dialog return false
				return false;
			}
		} while(true);
	}

	
	/**
	 * Overriden method to properly close zip output stream.
	 */
	protected void jobStopped() {
		// Try to close ZipOutputStream
		if(zipOut!=null) {
			try { zipOut.close(); }
			catch(IOException e) {}
		}
	}


    public String getStatusString() {
		return "Adding "+getCurrentFileInfo();
    }

}	
