
package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.file.archiver.Archiver;

import com.mucommander.ui.*;

import com.mucommander.text.Translator;

import java.io.*;


/**
 * This FileJob is responsible for compressing a set of files into an archive file.
 *
 * @author Maxence Bernard
 */
public class ArchiveJob extends ExtendedFileJob {

	/** Files to be archived */
    private FileSet files;
	
	/** Destination archive file */
	private AbstractFile destFile;

	/** Base destination folder's path */
	private String baseFolderPath;

	/** Archiver instance that does the actual archiving */
	private Archiver archiver;

	/** Archive format */
	private int archiveFormat;
	
	/** Optional archive comment */
	private String archiveComment;
	
//	/** Replace action's text */
//	private String REPLACE_TEXT = Translator.get("replace");
//	/** Replace action's value */
//	private int REPLACE_ACTION = 100;
	

    public ArchiveJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, AbstractFile destFile, int archiveFormat, String archiveComment) {
        super(progressDialog, mainFrame, files);
		
		this.files = files;
		this.destFile = destFile;
		this.archiveFormat = archiveFormat;
		this.archiveComment = archiveComment;

		this.baseFolderPath = baseSourceFolder.getAbsolutePath(false);
	}

	
	/**
	 * Overriden method to initialize the archiver and handle the case where the destination file already exists.
	 */
	protected void jobStarted() {
		if (destFile.exists()) {
			// File already exists in destination: ask the user what to do (cancel, overwrite,...) but
			// don't show the 'resume' option nor the multiple files mode options such as 'skip'.
			FileExistsDialog dialog = getFileExistsDialog(null, destFile, false);
			int choice = waitForUserResponse(dialog);

			// Cancel or dialog close (return)
			if (choice==-1 || choice==FileExistsDialog.CANCEL_ACTION) {
				stop();
				return;
			}
			// Overwrite file
			else if (choice==FileExistsDialog.OVERWRITE_ACTION) {
				// Do nothing, simply continue
			}
		}

		// Loop for retry
		do {
			try {
				// Tries to open destination file and create Archiver
				this.archiver = Archiver.getArchiver(destFile.getOutputStream(false), archiveFormat);
				this.archiver.setComment(archiveComment);
				break;
			}
			catch(Exception e) {
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

		String filePath = file.getAbsolutePath(false);
		String entryRelativePath = filePath.substring(baseFolderPath.length()+1, filePath.length());

		// Process current file
		do {		// Loop for retry
			try {
				if (file.isDirectory() && !file.isSymlink()) {
					// Create new directory entry in archive file
					archiver.createEntry(entryRelativePath, file);
					
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

					// Create new file entry in archive and copy file
					return copyStream(in, archiver.createEntry(entryRelativePath, file));
				}
			}
			catch(IOException e) {
				if(com.mucommander.Debug.ON) {
					com.mucommander.Debug.trace("IOException caught:");
					e.printStackTrace();
				}
				
				int ret = showErrorDialog(Translator.get("zip_dialog.error_title"), Translator.get("zip.error_on_file", file.getAbsolutePath()));
				// Retry loops
				if(ret==RETRY_ACTION)
					continue;
				// Cancel, skip or close dialog return false
				return false;
			}
		} while(true);
	}

	
	/**
	 * Overriden method to close the archiver.
	 */
	protected void jobStopped() {
		// Try to close archiver
		if(archiver!=null) {
			try { archiver.close(); }
			catch(IOException e) {}
		}
	}


    public String getStatusString() {
		return Translator.get("zip.compressing_file", getCurrentFileInfo());
    }

	protected boolean hasFolderChanged(AbstractFile folder) {
		// This job modifies baseFolder
		return destFile.getParent().equals(folder);
	}
}	
