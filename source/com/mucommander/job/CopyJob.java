
package com.mucommander.job;

import com.mucommander.file.*;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.FileExistsDialog;

import com.mucommander.text.SizeFormatter;
import com.mucommander.text.Translator;

import java.util.Vector;

import java.io.IOException;


/**
 * This job copies recursively a group of files.
 */
public class CopyJob extends ExtendedFileJob implements Runnable, FileModifier {

	private Vector filesToCopy;
	private String newName;
	private AbstractFile baseDestFolder;
	private boolean unzip;

	/** Current file info (path) */
	private String currentFileInfo = "";

//    /** Size of current file */
//    private long currentFileSize;

    /** Number of files that this job contains */
    private int nbFiles;
    
	private boolean skipAll;
	private boolean overwriteAll;
	private boolean appendAll;
	private boolean overwriteAllOlder;

	private String errorDialogTitle;
	
	
    /**
	 * @param indicates if this CopyJob corresponds to an 'unzip' operation.
	 */
	public CopyJob(MainFrame mainFrame, ProgressDialog progressDialog, AbstractFile baseSourceFolder, Vector filesToCopy, String newName, AbstractFile destFolder, boolean unzip) {
		super(progressDialog, mainFrame, baseSourceFolder);

	    this.filesToCopy = filesToCopy;
        this.nbFiles = filesToCopy.size();
		this.baseDestFolder = destFolder;
		this.newName = newName;
		this.unzip = unzip;
		this.errorDialogTitle = Translator.get(unzip?"unzip_dialog.error_title":"copy_dialog.error_title");
	}

	
	/**
	 * Recursively copies a file or folder.
	 */
    private void copyRecurse(AbstractFile file, AbstractFile destFolder, String newName) {
		if(isInterrupted())
            return;

//		currentFileSize = file.getSize();        

		String originalName = file.getName();
		String destFileName = (newName==null?originalName:newName);
       	String destFilePath = destFolder.getAbsolutePath(true)
       		+destFileName;

//		currentFileInfo = "\""+originalName+ "\" ("+SizeFormatter.format(currentFileSize, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";
		currentFileInfo = "\""+originalName+ "\" ("+SizeFormatter.format(file.getSize(), SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";

		AbstractFile destFile = AbstractFile.getAbstractFile(destFilePath);

		// Do nothing when encountering symlinks (skip file)
		if(file.isSymlink())
			;
		// Copy directory recursively
        else if(file.isDirectory()) {
            // creates the folder in the destination folder if it doesn't exist
			
			if(!(destFile.exists() && destFile.isDirectory())) {
				try {
					destFolder.mkdir(destFileName);
				}
            	catch(IOException e) {
                	int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_create_folder", destFileName));
                	if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
	                    stop();
            		return;		// abort in all cases
				}
			}
			
			// and copy each file in this folder recursively
            try {
                AbstractFile subFiles[] = file.ls();
				for(int i=0; i<subFiles.length && !isInterrupted(); i++) {
					copyRecurse(subFiles[i], destFile, null);
                }
			}
            catch(IOException e) {
                int ret = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_folder", destFile.getAbsolutePath()));
                if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
                    stop();
			}
        }
		// Copy file
        else  {
//System.out.println("SOURCE: "+file.getAbsolutePath()+"\nDEST: "+destFilePath);
			boolean append = false;
			
			// Tests if the file exists
			// and resolves a potential conflicting situation
			if (destFile.exists())  {
				if(skipAll)
					return;
				else if(overwriteAll);
				else if(appendAll)
					append = true;
				else if (overwriteAllOlder) {
					if(file.getDate()<destFile.getDate())
						return;
				}
				else {
					int ret = showFileExistsDialog(file, destFile);
				
			    	if (ret==-1 || ret==FileExistsDialog.CANCEL_ACTION) {
			    		stop();                
			    		return;
			    	}
			    	else if (ret==FileExistsDialog.SKIP_ACTION) {
			    		return;
			    	}
					else if (ret==FileExistsDialog.APPEND_ACTION) {
			    		append = true;
					}
					else if (ret==FileExistsDialog.SKIP_ALL_ACTION) {
						skipAll = true;
						return;
					}
					else if (ret==FileExistsDialog.OVERWRITE_ALL_ACTION) {
						overwriteAll = true;
					}
					else if (ret==FileExistsDialog.APPEND_ALL_ACTION) {
						appendAll = true;
						append = true;
					}	
					else if (ret==FileExistsDialog.OVERWRITE_ALL_OLDER_ACTION) {
						overwriteAllOlder = true;
						if(file.getDate()<destFile.getDate())
							return;
					}
				}
			}
			
			// Copy file to destination
			try {
				copyFile(file, destFile, append);
			}
			catch(FileJobException e) {
				if(com.mucommander.Debug.ON)
					System.out.println(""+e);
				
				int reason = e.getReason();
//				String errorMsg = Translator.get(unzip?"unzip.cannot_unzip_file":"copy.cannot_copy_file", file.getName());
				String errorMsg;
				switch(reason) {
					case FileJobException.CANNOT_OPEN_SOURCE:
						errorMsg = Translator.get("cannot_open_source_file", file.getName());
						break;
					case FileJobException.CANNOT_OPEN_DESTINATION:
						errorMsg = Translator.get("cannot_open_destination_file", file.getName());
						break;
					
					case FileJobException.ERROR_WHILE_TRANSFERRING:
					default:
						errorMsg = Translator.get("error_while_transferring", file.getName());
						break;
				}
				
				int ret = showErrorDialog(errorDialogTitle, errorMsg);
			    if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
			        stop();
			}
		}
    }


    public void run() {
//		currentFileIndex = 0;

		FileTable activeTable = mainFrame.getLastActiveTable();
		AbstractFile currentFile;
        AbstractFile zipSubFiles[];
		for(int i=0; i<nbFiles; i++) {
//		while(true) {
//			currentFile = (AbstractFile)filesToCopy.elementAt(currentFileIndex);
			currentFile = (AbstractFile)filesToCopy.elementAt(i);
			nextFile(currentFile);
			
			// Unzip files		
			if (unzip) {
				if (currentFile instanceof ZipArchiveFile) {
					try {
						zipSubFiles = currentFile.ls();
						for(int j=0; j<zipSubFiles.length; j++) {
                            copyRecurse(zipSubFiles[j], baseDestFolder, null);
						}
					}
					catch(IOException e) {
						int ret = showErrorDialog(errorDialogTitle, Translator.get("unzip.unable_to_open_zip", currentFile.getName()));
						if (ret==-1 || ret==CANCEL_ACTION)	 {		// CANCEL_ACTION or close dialog
						    stop();
							break;
						}
					}
				}
			}
			else {
				copyRecurse(currentFile, baseDestFolder, newName);
			}
			
			if(isInterrupted())
				break;
			
			activeTable.setFileMarked(currentFile, false);
			activeTable.repaint();

//            if(currentFileIndex<nbFiles-1)	// This ensures that currentFileIndex is never out of bounds (cf getCurrentFile)
//                currentFileIndex++;
//            else break;
        }

        stop();
		
        // Refresh tables only if folder is destFolder
        refreshTableIfFolderEquals(mainFrame.getBrowser1().getFileTable(), baseDestFolder);
        refreshTableIfFolderEquals(mainFrame.getBrowser2().getFileTable(), baseDestFolder);

		cleanUp();
	}


	/*******************************************
	 *** ExtendedFileJob implemented methods ***
	 *******************************************/

    public int getNbFiles() {
        return nbFiles;
    }
    
//    public long getCurrentFileSize() {
//        return currentFileSize;
//    }

    public String getStatusString() {
        return Translator.get(unzip?"unzip.unzipping_file":"copy.copying_file", currentFileInfo);
    }
 

}	
