
package com.mucommander.job;

import com.mucommander.file.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.text.SizeFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.zip.*;
import java.util.Vector;

/**
 * This FileJob is responsible for compressing a group files in the zip format.
 */
public class ZipJob extends ExtendedFileJob implements Runnable {
    private Vector filesToZip;
    private MainFrame mainFrame;
	private ZipOutputStream zipOut;
	private AbstractFile destFolder;
	private String currentFileInfo = "";
	private String baseFolderPath;

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

	private final static int CANCEL_ACTION = 0;
	private final static int SKIP_ACTION = 1;

	private final static int CANCEL_MNEMONIC = KeyEvent.VK_C;
	private final static int SKIP_MNEMONIC = KeyEvent.VK_S;

	private final static String CANCEL_CAPTION = "Cancel";
	private final static String SKIP_CAPTION = "Skip";

	private byte buffer[] = new byte[1024];

    public ZipJob(MainFrame mainFrame, ProgressDialog progressDialog, Vector filesToZip, String zipComment, OutputStream zipOut, AbstractFile destFolder) {
        super(progressDialog);
		
		this.filesToZip = filesToZip;
        this.nbFiles = filesToZip.size();
        this.mainFrame = mainFrame;
		this.zipOut = new ZipOutputStream(zipOut);
		this.destFolder = destFolder;

		if(zipComment!=null && !zipComment.equals(""))
			this.zipOut.setComment(zipComment);
	
		this.baseFolderPath = ((AbstractFile)filesToZip.elementAt(0)).getParent().getAbsolutePath();
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
		return "Adding "+currentFileInfo;
    }

    private int showErrorDialog(String message) {
		QuestionDialog dialog = new QuestionDialog(progressDialog, "Zip error", message, mainFrame,
			new String[] {SKIP_CAPTION, CANCEL_CAPTION},
			new int[]  {SKIP_ACTION, CANCEL_ACTION},
			new int[]  {SKIP_MNEMONIC, CANCEL_MNEMONIC},
			0);
	
	    return waitForUserResponse(dialog);
    }


	private boolean zipRecurse(AbstractFile file) {
		if(isInterrupted())
			return false;

//        if(level==0) {
            currentFileProcessed = 0;
            currentFileSize = file.getSize();
//        }
        
		String filePath = file.getAbsolutePath();
		String zipEntryRelativePath = filePath.substring(baseFolderPath.length()+1, filePath.length());
		currentFileInfo = "\""+file.getName()+"\" ("+SizeFormatter.format(currentFileSize, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";
		
		try {
//			if (file.isFolder() && !(file instanceof ArchiveFile)) {
			if (file.isDirectory() && !file.isSymlink()) {
				// Create directory entry
				zipOut.putNextEntry(new ZipEntry(zipEntryRelativePath.replace('\\', '/')+"/"));
				
				AbstractFile subFiles[] = file.ls();
				boolean folderComplete = true;
				for(int i=0; i<subFiles.length && !isInterrupted(); i++) {
					if(!zipRecurse(subFiles[i]))
						folderComplete = false;
				}
				
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
//					fileSize = Math.max(bytesTotal, fileSize);
				}
                			
				return true;
			}
		}
		catch(IOException e) {
			int ret = showErrorDialog("Error while adding "+file.getAbsolutePath());
			if(ret==-1 || ret==CANCEL_ACTION) {		// CANCEL_ACTION or close dialog
			    stop();
			}              
			return false;
		}
	}


    public void run() {
        currentFileIndex = 0;
        int numFiles = filesToZip.size();

        // Important!
        waitForDialog();

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


		FileTable table1 = mainFrame.getBrowser1().getFileTable();
		// Refreshes table1 only if folder is destFolder
		if (table1.getCurrentFolder().equals(destFolder))
			try { table1.refresh();	}
			catch(IOException e) {
				// Probably should do something when a folder becomes unreadable
			}

		FileTable table2 = mainFrame.getBrowser2().getFileTable();
		// Refreshes table2 only if folder is destFolder
		if (table2.getCurrentFolder().equals(destFolder))
			try { table2.refresh();	}
			catch(IOException e) {
				// Probably should do something when a folder becomes unreadable
			}

				
		mainFrame.getLastActiveTable().requestFocus();
	}
}	
