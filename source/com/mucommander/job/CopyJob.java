
package com.mucommander.job;

import com.mucommander.file.*;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FileExistsDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.table.FileTable;

import com.mucommander.text.SizeFormatter;
import com.mucommander.text.Translator;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.*;

import java.util.Vector;


/**
 * This job copies recursively a group of files.
 */
public class CopyJob extends ExtendedFileJob implements Runnable, FileModifier {
	private MainFrame mainFrame;

	private Vector filesToCopy;
	private String newName;
	private AbstractFile baseDestFolder;
	private boolean unzip;

	/** Current file info (path) */
	private String currentFileInfo = "";

    /** Size of current file */
    private long currentFileSize;

    /** Number of bytes of current file that have been processed */
    private long currentFileProcessed;
    
	/** Index of file currently being processed */
	private int currentFileIndex;

    /** Number of bytes processed so far */
    private long nbBytesProcessed;

    /** Number of files that this job contains */
    private int nbFiles;
    
	private boolean skipAll;
	private boolean overwriteAll;
	private boolean appendAll;
	private boolean overwriteAllOlder;

	private final static int CANCEL_ACTION = 0;
	private final static int SKIP_ACTION = 1;

	private final static String CANCEL_TEXT = Translator.get("cancel");
	private final static String SKIP_TEXT = Translator.get("skip");

	private final static int BLOCK_SIZE = 1024;

	
    /**
	 * @param indicates if this CopyJob corresponds to an 'unzip' operation.
	 */
	public CopyJob(MainFrame mainFrame, ProgressDialog progressDialog, Vector filesToCopy, String newName, AbstractFile destFolder, boolean unzip) {
		super(progressDialog, mainFrame);

	    this.filesToCopy = filesToCopy;
        this.nbFiles = filesToCopy.size();
		this.baseDestFolder = destFolder;
		this.newName = newName;
		this.mainFrame = mainFrame;
		this.unzip = unzip;
	}

	
	/**
	 * Recursively copies a file or folder.
	 */
    private void copyRecurse(AbstractFile file, AbstractFile destFolder, String newName) {
		if(isInterrupted())
            return;

//        if(level==0) {
            currentFileProcessed = 0;
            currentFileSize = file.getSize();        
//        }

//System.out.println("DEST FOLDER "+destFolder.getAbsolutePath());
		String originalName = file.getName();
		String destFileName = (newName==null?originalName:newName);
       	String destFilePath = destFolder.getAbsolutePath(true)
       		+destFileName;

//System.out.println("SOURCE "+file.getAbsolutePath()+" DEST "+destFilePath);

		currentFileInfo = "\""+originalName+ "\" ("+SizeFormatter.format(currentFileSize, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";

		AbstractFile destFile = AbstractFile.getAbstractFile(destFilePath);

		// Do nothing when encountering symlinks (skip file)
		if(file.isSymlink())
			;
		// Copy directory recursively
//        else if(file.isDirectory() || unzip) {
        else if(file.isDirectory()) {
            // creates the folder in the destination folder if it doesn't exist
			
			if(!(destFile.exists() && destFile.isDirectory())) {
				try {
					destFolder.mkdir(destFileName);
				}
            	catch(IOException e) {
                	int ret = showErrorDialog(Translator.get("copy.cannot_create_folder", destFileName));
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
                int ret = showErrorDialog(Translator.get("copy.cannot_read_folder", destFile.getAbsolutePath()));
                if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
                    stop();
			}
        }
		// Copy file
        else  {
        	byte buf[] = new byte[BLOCK_SIZE];
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
			
			OutputStream out = null;
			InputStream in = null;
			try  {
				in = file.getInputStream();
				out = destFile.getOutputStream(append);

				try  {
					int read;
                    while ((read=in.read(buf, 0, buf.length))!=-1 && !isInterrupted()) {
						out.write(buf, 0, read);
                        nbBytesProcessed += read;
//                        if(level==0)
						currentFileProcessed += read;

						// currentFileSize can be wrong (for example with HTMLFiles)
						// currentFileSize = Math.max(currentFileProcessed, currentFileSize);
//                        if(currentFileSize!=-1)
//                            currentFilePercent = (int)(100*currentFileProcessed/(float)currentFileSize);
					}
				}
				catch(IOException e) {
					if(com.mucommander.Debug.ON)
						System.out.println(""+e);
					int ret = showErrorDialog(Translator.get(unzip?"unzip.error_on_file":"copy.error_on_file", file.getName()));
				    if(ret!=SKIP_ACTION)		// CANCEL_ACTION or close dialog
				        stop();                
				}
			}
			catch(IOException e) {
				if(com.mucommander.Debug.ON)
					System.out.println(""+e);
				int ret = showErrorDialog(Translator.get(unzip?"unzip.cannot_unzip_file":"copy.cannot_copy_file", file.getName()));
			    if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
			        stop();
			}
		
			// Tries to close the streams no matter what happened before
			try {
				if(in!=null)
					in.close();
				if(out!=null)
					out.close();
			}
        	catch(IOException e) {
        	}
		}
    }

    public long getTotalBytesProcessed() {
        return nbBytesProcessed;
    }

    public int getCurrentFileIndex() {
        return currentFileIndex;
    }

    public int getNbFiles() {
        return nbFiles;
    }
    
    public long getCurrentFileBytesProcessed() {
        return currentFileProcessed;
    }

    public long getCurrentFileSize() {
        return currentFileSize;
    }

    public String getStatusString() {
        return Translator.get(unzip?"unzip.unzipping_file":"copy.copying_file", currentFileInfo);
    }
 

    private int showFileExistsDialog(AbstractFile sourceFile, AbstractFile destFile) {
		QuestionDialog dialog = new FileExistsDialog(progressDialog, mainFrame, sourceFile, destFile);
		
		return waitForUserResponse(dialog);
	}

	
    private int showErrorDialog(String message) {
		QuestionDialog dialog = new QuestionDialog(progressDialog, 
			Translator.get(unzip?"unzip_dialog.error_title":"copy_dialog.error_title"),
			message,
			mainFrame,
			new String[] {SKIP_TEXT, CANCEL_TEXT},
			new int[]  {SKIP_ACTION, CANCEL_ACTION},
			0);

		return waitForUserResponse(dialog);
    }

    public void run() {
		currentFileIndex = 0;

		// Important!
		waitForDialog();

		FileTable activeTable = mainFrame.getLastActiveTable();
		AbstractFile currentFile;
        AbstractFile zipSubFiles[];
		while(true) {
			currentFile = (AbstractFile)filesToCopy.elementAt(currentFileIndex);
			
			// Unzip files		
			if (unzip) {
				if (currentFile instanceof ZipArchiveFile) {
					try {
						zipSubFiles = currentFile.ls();
						for(int i=0; i<zipSubFiles.length; i++) {
//                            copyRecurse(zipSubFiles[i], baseDestFolder, null, 0);
                            copyRecurse(zipSubFiles[i], baseDestFolder, null);
						}
					}
					catch(IOException e) {
						int ret = showErrorDialog(Translator.get("unzip.unable_to_open_zip", currentFile.getName()));
						if (ret==-1 || ret==CANCEL_ACTION)	 {		// CANCEL_ACTION or close dialog
						    stop();
							break;
						}
					}
				}
			}
			else {
//				copyRecurse(currentFile, baseDestFolder, newName, 0);
				copyRecurse(currentFile, baseDestFolder, newName);
			}

			if(isInterrupted())
				break;
			
			activeTable.setFileMarked(currentFile, false);
			activeTable.repaint();

            if(currentFileIndex<nbFiles-1)	// This ensures that currentFileIndex is never out of bounds (cf getCurrentFile)
                currentFileIndex++;
            else break;
        }

        stop();
		
        // Refreshes only if table's folder is destFolder
        FileTable table1 = mainFrame.getBrowser1().getFileTable();
		if (table1.getCurrentFolder().equals(baseDestFolder))
        	try {
        		table1.refresh();
        	}
        	catch(IOException e) {
        		// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
        		// like switching to a root folder        
        	}

        // Refreshes only if table's folder is destFolder
        FileTable table2 = mainFrame.getBrowser2().getFileTable();
        if (table2.getCurrentFolder().equals(baseDestFolder))
        	try {
        		table2.refresh();
        	}
        	catch(IOException e) {
        		// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
        		// like switching to a root folder        
        	}

		cleanUp();
	}
}	
