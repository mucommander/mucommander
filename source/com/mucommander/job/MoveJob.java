
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
import java.util.Vector;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;

/**
 * This class is responsible for moving recursively a group of files.
 */
public class MoveJob extends ExtendedFileJob implements Runnable {
    private MainFrame mainFrame;

	private Vector filesToMove;
	private String newName;
	private AbstractFile baseDestFolder;

	/** Current file info (path) */
	private String currentFileInfo = "";

	/** Index of file currently being moved */
	private int currentFileIndex;

    /** Size of current file */
    private long currentFileSize;

    /** Number of bytes of current file that have been processed */
    private long currentFileProcessed;
    
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
    private final static int OVERWRITE_ACTION = 2;
    private final static int APPEND_ACTION = 3;
    private final static int SKIP_ALL_ACTION = 4;
    private final static int OVERWRITE_ALL_ACTION = 5;
    private final static int APPEND_ALL_ACTION = 6;
	private final static int OVERWRITE_ALL_OLDER_ACTION = 7;

    private final static int CANCEL_MNEMONIC = KeyEvent.VK_C;
	private final static int SKIP_MNEMONIC = KeyEvent.VK_S;
    private final static int OVERWRITE_MNEMONIC = KeyEvent.VK_O;
    private final static int APPEND_MNEMONIC = KeyEvent.VK_A;
    private final static int SKIP_ALL_MNEMONIC = KeyEvent.VK_K;
    private final static int OVERWRITE_ALL_MNEMONIC = KeyEvent.VK_V;
    private final static int APPEND_ALL_MNEMONIC = KeyEvent.VK_P;
    private final static int OVERWRITE_ALL_OLDER_MNEMONIC = KeyEvent.VK_E;

    private final static String CANCEL_CAPTION = "Cancel";
    private final static String SKIP_CAPTION = "Skip";
    private final static String OVERWRITE_CAPTION = "Overwrite";
    private final static String APPEND_CAPTION = "Append";
    private final static String SKIP_ALL_CAPTION = "Skip all";
    private final static String OVERWRITE_ALL_CAPTION = "Overwrite all";
    private final static String APPEND_ALL_CAPTION = "Append all";
	private final static String OVERWRITE_ALL_OLDER_CAPTION = "Overwrite older";

	private final static int BLOCK_SIZE = 1024;

    /**
	 * @param newName in case where filesToMove contains a single file, newName can be used to rename the file in the
	 * destination folder. Otherwise, a null is passed.
	 */
	public MoveJob(MainFrame mainFrame, ProgressDialog progressDialog, Vector filesToMove, String newName, AbstractFile destFolder) {
//System.out.println("MOVE JOB: "+ " "+ filesToMove + " "+ destFolder.getAbsolutePath());
		super(progressDialog);

		this.mainFrame = mainFrame;
		this.filesToMove = filesToMove;
        this.nbFiles = filesToMove.size();
		this.newName = newName;
		this.baseDestFolder = destFolder;
	}

	/**
	 * Moves recursively a file to a destination folder.
	 * @return <code>true</code> if the file was completely moved.
	 */
    private boolean moveRecurse(AbstractFile file, AbstractFile destFolder, String newName) {

		if(isInterrupted())
            return false;

//        if(level==0) {
            currentFileProcessed = 0;
            currentFileSize = file.getSize();
//        }

		String originalName = file.getName();
		String destFileName = (newName==null?originalName:newName);
		String destFilePath = destFolder.getAbsolutePath()
        	+destFolder.getSeparator()
        	+destFileName;

		currentFileInfo = "\""+originalName+"\" ("+SizeFormatter.format(currentFileSize, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";

		AbstractFile destFile = AbstractFile.getAbstractFile(destFilePath);

		// Tries to move the file with AbstractFile.moveTo() 
		// skipping the whole recursive process
		try {
			if(file.moveTo(destFile))
				return true;		// if it succeeded return
		}
		catch(IOException e) {
		    // Let's try the other regular way
		}
		
//		if(file.isFolder() && !(file instanceof ArchiveFile)) {
		if(file.isDirectory()) {
            // creates the folder in the destination folder if it doesn't exist
			if (!destFile.exists()) {
				try {
//System.out.println("CREATING FOLDER "+destFolder.getAbsolutePath()+"\\"+(newName==null?file.getName():newName));
					destFolder.mkdir(destFileName);
				}
            	catch(IOException e) {
                	int ret = showErrorDialog("Unable to create folder "+destFile.getAbsolutePath());
                	if(ret==-1 || ret==CANCEL_ACTION) 		// CANCEL_ACTION or close dialog
	                    stop();
        	        return false;		// abort and return failure
            	}
			}
			
			// move each file in this folder recursively
            try {
                AbstractFile subFiles[] = file.ls();
                boolean isFolderEmpty = true;
				for(int i=0; i<subFiles.length && !isInterrupted(); i++)
                    if(!moveRecurse(subFiles[i], destFile, null))
						isFolderEmpty = false;
            	// If one file could returned failure, return failure as well since this
				// folder could not be moved totally
				if(!isFolderEmpty)
					return false;
			}
            catch(IOException e) {
                int ret = showErrorDialog("Unable to read contents of folder "+file.getAbsolutePath());
                if(ret==-1 || ret==CANCEL_ACTION) 		// CANCEL_ACTION or close dialog
                    stop();
                return false;			// abort and return failure
            }
			
			
			// and finally deletes the empty folder
        	try  {
				file.delete();
        		return true;
			}
        	catch(IOException e) {
        	    int ret = showErrorDialog("Unable to delete folder "+file.getAbsolutePath());
        	    if(ret==-1 || ret==CANCEL_ACTION) 		// CANCEL_ACTION or close dialog
        	        stop();
       	        return false;
        	}
			
		}
        else  {
//System.out.println("SOURCE: "+file.getAbsolutePath()+"\nDEST: "+destFilePath);
			boolean append = false;

	        // Tests if the file exists
	        // and resolves a potential conflicting situation
	        if (destFile.exists())  {
	        	if(skipAll)
					return false;
				else if(overwriteAll);
				else if(appendAll)
					append = true;
				else if (overwriteAllOlder) {
					if(file.getDate()<destFile.getDate())
						return false;
				}
				else {	
					int ret = showFileExistsDialog(file, destFile);
				
		        	if (ret==-1 || ret==CANCEL_ACTION) {
		        		stop();                
		        		return false;
		        	}
		        	else if (ret==SKIP_ACTION) {
		        		return false;
		        	}
					else if (ret==APPEND_ACTION) {
		        		append = true;
					}
					else if (ret==SKIP_ALL_ACTION) {
	        			skipAll = true;
						return false;
	        		}
					else if (ret==OVERWRITE_ALL_ACTION) {
						overwriteAll = true;
					}
					else if (ret==APPEND_ALL_ACTION) {
	        			appendAll = true;
						append = true;
					}	
					else if (ret==OVERWRITE_ALL_OLDER_ACTION) {
						overwriteAllOlder = true;
						if(file.getDate()<destFile.getDate())
							return false;
					}
				}
	        }
			
			// if moveTo() returned false or wasn't possible (append)
			byte buf[] = new byte[BLOCK_SIZE];
			
			OutputStream out = null;
			InputStream in = null;
			
			boolean moved = false;
			// true if file was moved successfully
			boolean retValue = false;
			try  {
				in = file.getInputStream();
				out = destFile.getOutputStream(append);

				try  {
					int read;
					while ((read=in.read(buf, 0, buf.length))!=-1 && !isInterrupted()) {
						out.write(buf, 0, read);
                        nbBytesProcessed += read;
						currentFileProcessed += read;
                    }

					moved = !isInterrupted();
				}
				catch(IOException e) {
				    int ret = showErrorDialog("Error while moving file "+file.getAbsolutePath());
				    if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
				        stop();                
				}
			}
			catch(IOException e) {
			    int ret = showErrorDialog("Unable to move file "+file.getAbsolutePath());
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
			
			// If the file was successfully moved, delete the original one
			if (moved) {
				try  {
					file.delete();
					retValue = true;
				}
				catch(IOException e) {
				    int ret = showErrorDialog("Unable to delete file "+file.getAbsolutePath());
				    if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
				        stop();
				}
			}
		
			return retValue;
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
		return "Moving "+currentFileInfo;
    }
 

	private int showFileExistsDialog(AbstractFile sourceFile, AbstractFile destFile) {
		JPanel panel = new JPanel(new GridLayout(0,1));
	//				panel.add(new JLabel("File already exists in destination:"));
	//				panel.add(new JLabel(""));
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
		NumberFormat numberFormat = NumberFormat.getInstance();
		panel.add(new JLabel("Source: "+sourceFile.getAbsolutePath()));
		panel.add(new JLabel("  "+numberFormat.format(sourceFile.getSize())
				+" bytes, "+dateFormat.format(new Date(sourceFile.getDate()))));
		panel.add(new JLabel(""));
		panel.add(new JLabel("Destination: "+destFile.getAbsolutePath()));
		panel.add(new JLabel("  "+numberFormat.format(destFile.getSize())
				+" bytes, "+dateFormat.format(new Date(destFile.getDate()))));
		
		QuestionDialog dialog = new QuestionDialog(progressDialog, "File already exists in destination", 
			panel, mainFrame,
			new String[] {SKIP_CAPTION, OVERWRITE_CAPTION, APPEND_CAPTION, SKIP_ALL_CAPTION, OVERWRITE_ALL_CAPTION, APPEND_ALL_CAPTION, CANCEL_CAPTION, OVERWRITE_ALL_OLDER_CAPTION},
			new int[]  {SKIP_ACTION, OVERWRITE_ACTION, APPEND_ACTION, SKIP_ALL_ACTION, OVERWRITE_ALL_ACTION, APPEND_ALL_ACTION, CANCEL_ACTION, OVERWRITE_ALL_OLDER_ACTION},
			new int[]  {SKIP_MNEMONIC, OVERWRITE_MNEMONIC, APPEND_MNEMONIC, SKIP_ALL_MNEMONIC, OVERWRITE_ALL_MNEMONIC, APPEND_ALL_MNEMONIC, CANCEL_MNEMONIC, OVERWRITE_ALL_OLDER_MNEMONIC},
			3);
	
	    return waitForUserResponse(dialog);
	}
	
    private int showErrorDialog(String message) {
		QuestionDialog dialog = new QuestionDialog(progressDialog, "Move error", message, mainFrame, 
			new String[] {SKIP_CAPTION, CANCEL_CAPTION},
			new int[]  {SKIP_ACTION, CANCEL_ACTION},
			new int[]  {SKIP_MNEMONIC, CANCEL_MNEMONIC},
			0);

	    return waitForUserResponse(dialog);
    }

    public void run() {
        // Important!
        waitForDialog();

		FileTable activeTable = mainFrame.getLastActiveTable();
        AbstractFile currentFile;
		while (!isInterrupted()) {
            currentFile = (AbstractFile)filesToMove.elementAt(currentFileIndex);
			
			// if current file or folder was successfully moved, exclude it from the file table
			if (moveRecurse(currentFile, baseDestFolder, newName))
				activeTable.excludeFile(currentFile);
			// else unmark it
			else
				activeTable.setFileMarked(currentFile, false);
			activeTable.repaint();
			
			// This ensures that currentFileIndex is never out of bounds
			if(currentFileIndex<nbFiles-1)
                currentFileIndex++;
            else break;
        }
    
        stop();

		try {
		    activeTable.refresh();
		}
		catch(IOException e) {
			// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
			// like switching to a root folder        
		}

		// Refreshes only if table's folder is destFolder
		FileTable unactiveTable = mainFrame.getUnactiveTable();
		if (unactiveTable.getCurrentFolder().equals(baseDestFolder))
			try {
				unactiveTable.refresh();
			}
			catch(IOException e) {
				// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
				// like switching to a root folder        
			}

		activeTable.requestFocus();
	}
}	
