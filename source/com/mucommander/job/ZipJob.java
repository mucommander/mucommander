
package com.mucommander.job;

import com.mucommander.file.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.zip.*;
import java.util.Vector;

/**
 * This class is responsible for compressing a group files in the zip format.
 */
public class ZipJob extends FileJob implements Runnable {
    private Vector filesToZip;
    private MainFrame mainFrame;
	private ZipOutputStream zipOut;
	private AbstractFile destFolder;
	private int currentFileIndex;
	private int currentFileProgress;
	private String currentFileInfo = "";
	private String baseFolderPath;

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
        this.mainFrame = mainFrame;
		this.zipOut = new ZipOutputStream(zipOut);
		this.destFolder = destFolder;

		if(zipComment!=null && !zipComment.equals(""))
			this.zipOut.setComment(zipComment);
	
		this.baseFolderPath = ((AbstractFile)filesToZip.elementAt(0)).getParent().getAbsolutePath();
	}

	public int getFilePercentDone() {
		return currentFileProgress;
	}

    public int getTotalPercentDone() {
        // We could refine and update the value for each file deleted within a folder
		return (int)(100*(currentFileIndex/(float)filesToZip.size()));
    }
    
    public String getCurrentInfo() {
		return "Adding "+currentFileInfo;
    }

    private int showErrorDialog(String message) {
		QuestionDialog dialog = new QuestionDialog(progressDialog, "Zip error", message, mainFrame,
			new String[] {SKIP_CAPTION, CANCEL_CAPTION},
			new int[]  {SKIP_ACTION, CANCEL_ACTION},
			new int[]  {SKIP_MNEMONIC, CANCEL_MNEMONIC},
			0);
	
	    return dialog.getActionValue();
    }


	private boolean zipRecurse(AbstractFile file) {
		if(isInterrupted())
			return false;
		
		String filePath = file.getAbsolutePath();
		String zipEntryRelativePath = filePath.substring(baseFolderPath.length()+1, filePath.length());
		currentFileInfo = zipEntryRelativePath;
		
		try {
			if (file.isFolder() && !(file instanceof ArchiveFile)) {
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
				currentFileProgress = 0;
				InputStream in = file.getInputStream();
				long fileSize = file.getSize();
				int nbRead;
				int bytesTotal = 0;

				zipOut.putNextEntry(new ZipEntry(zipEntryRelativePath.replace('\\', '/')));
				while ((nbRead=in.read(buffer, 0, buffer.length))!=-1) {
					zipOut.write(buffer, 0, nbRead);
					bytesTotal += nbRead;
					fileSize = Math.max(bytesTotal, fileSize);
					currentFileProgress = (int) (100 * (bytesTotal/(float)fileSize));
				}
				currentFileProgress = 100;
			
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
