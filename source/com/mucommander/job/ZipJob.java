
package com.mucommander.job;

import com.mucommander.file.AbstractFile;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
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
	private int REPLACE_ACTION = 10;
	

    public ZipJob(ProgressDialog progressDialog, MainFrame mainFrame, Vector filesToZip, String zipComment, AbstractFile destFile) {
        super(progressDialog, mainFrame, filesToZip);
		
		this.filesToZip = filesToZip;
		this.destFile = destFile;
		this.zipComment = zipComment;

//		this.baseFolderPath = ((AbstractFile)filesToZip.elementAt(0)).getParent().getAbsolutePath();
		this.baseFolderPath = baseSourceFolder.getAbsolutePath();
		this.errorDialogTitle = Translator.get("zip_dialog.error_title");
	}


	protected boolean processFile(AbstractFile file, Object recurseParams) {
		if(isInterrupted())
			return false;

		// First call to process file, initialize zip file
		if(zipOut==null) {
			if (destFile.exists()) {
				// File already exists: cancel, append or overwrite?
				QuestionDialog dialog = new QuestionDialog(mainFrame, Translator.get("warning"), Translator.get("zip_dialog.file_already_exists", destFile.getName()), mainFrame,
					new String[] {CANCEL_TEXT, REPLACE_TEXT},
					new int[]  {CANCEL_ACTION, REPLACE_ACTION},
					0);
				int ret = dialog.getActionValue();
				
				// Cancel or close returns false
				if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
					return false;
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
					QuestionDialog dialog = new QuestionDialog(mainFrame, errorDialogTitle, Translator.get("zip_dialog.cannot_write"), mainFrame,
						new String[] {CANCEL_TEXT, RETRY_TEXT},
						new int[]  {CANCEL_ACTION, RETRY_ACTION},
						0);
					int ret = dialog.getActionValue();
					// Retry loops
					if(ret == RETRY_ACTION)
						continue;
					// Cancel or close dialog returns false
					return false;
				}
			} while(true);
		}
		
//		currentFileProcessed = 0;
//		currentFileSize = file.getSize();
		String filePath = file.getAbsolutePath();
		String zipEntryRelativePath = filePath.substring(baseFolderPath.length()+1, filePath.length());
//		currentFileInfo = "\""+file.getName()+"\" ("+SizeFormatter.format(currentFileSize, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";

		// Process current file
		do {		// Loop for retry
			try {
				if (file.isDirectory() && !file.isSymlink()) {
					// Create new directory entry in zip file
					zipOut.putNextEntry(new ZipEntry(zipEntryRelativePath.replace('\\', '/')+"/"));
					
					// Recurse on files
					AbstractFile subFiles[] = file.ls();
					boolean folderComplete = true;
					for(int i=0; i<subFiles.length && !isInterrupted(); i++)
						if(!processFile(subFiles[i], null))
							folderComplete = false;
					
					return folderComplete;
				}
				else {
					InputStream in = file.getInputStream();
/*
					int nbRead;
					zipOut.putNextEntry(new ZipEntry(zipEntryRelativePath.replace('\\', '/')));
					while ((nbRead=in.read(buffer, 0, buffer.length))!=-1) {
						zipOut.write(buffer, 0, nbRead);
					}
*/
					// Create new file entry in zip file
					zipOut.putNextEntry(new ZipEntry(zipEntryRelativePath.replace('\\', '/')));
					copyStream(in, zipOut, 0);
								
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


    public String getStatusString() {
		return "Adding "+getCurrentFileInfo();
    }

}	
