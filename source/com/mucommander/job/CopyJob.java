
package com.mucommander.job;

import com.mucommander.file.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.table.FileTable;
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
 * This class is responsible for copying recursively a group of files.
 */
public class CopyJob extends ExtendedFileJob implements Runnable {
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
	private final static String OVERWRITE_ALL_OLDER_CAPTION = "Overwrite all older";

	private final static int BLOCK_SIZE = 1024;

    /**
	 * @param indicates if this CopyJob corresponds to an 'unzip' operation.
	 */
	public CopyJob(MainFrame mainFrame, ProgressDialog progressDialog, Vector filesToCopy, String newName, AbstractFile destFolder, boolean unzip) {
//System.out.println("COPY JOB: "+ " "+ filesToCopy + " "+ newName + " "+destFolder.getAbsolutePath());
		super(progressDialog);

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
       	String destFilePath = destFolder.getAbsolutePath()
       		+destFolder.getSeparator()
       		+destFileName;

//System.out.println("SOURCE "+file.getAbsolutePath()+" DEST "+destFilePath);

		currentFileInfo = "\""+originalName+ "\" ("+SizeFormatter.format(currentFileSize, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";

		AbstractFile destFile = AbstractFile.getAbstractFile(destFilePath);

//        if(file.isFolder() && (!(file instanceof ArchiveFile) || unzip)) {
        if(file.isDirectory() || unzip) {
            // creates the folder in the destination folder if it doesn't exist
			
			if(!destFile.exists())
				try {
//System.out.println("CREATING FOLDER "+destFolder.getAbsolutePath()+"\\"+(newName==null?file.getName():newName));
					destFolder.mkdir(destFileName);
				}
            	catch(IOException e) {
                	int ret = showErrorDialog("Unable to create folder "+destFileName);
                	if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
	                    stop();
            		return;		// abort in all cases
				}
			
			// and copy each file in this folder recursively
            try {
                AbstractFile subFiles[] = file.ls();
				for(int i=0; i<subFiles.length && !isInterrupted(); i++) {
//                    System.out.println("Copy recurse "+subFiles[i].getAbsolutePath()+" to "+destFile.getAbsolutePath());
//					copyRecurse(subFiles[i], destFile, null, level+1);
					copyRecurse(subFiles[i], destFile, null);
                }
			}
            catch(IOException e) {
                int ret = showErrorDialog("Unable to read contents of folder "+destFile.getAbsolutePath());
                if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
                    stop();
			}
        }
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
				
			    	if (ret==-1 || ret==CANCEL_ACTION) {
			    		stop();                
			    		return;
			    	}
			    	else if (ret==SKIP_ACTION) {
			    		return;
			    	}
					else if (ret==APPEND_ACTION) {
			    		append = true;
					}
					else if (ret==SKIP_ALL_ACTION) {
						skipAll = true;
						return;
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
					if(com.mucommander.Debug.TRACE)
						System.out.println(""+e);
				    int ret = showErrorDialog("Error while "+(unzip?"unzipping":"copying")+" file "+file.getName());
				    if(ret!=SKIP_ACTION)		// CANCEL_ACTION or close dialog
				        stop();                
				}
			}
			catch(IOException e) {
				if(com.mucommander.Debug.TRACE)
					System.out.println(""+e);
			    int ret = showErrorDialog("Unable to "+(unzip?"unzip":"copy")+" file "+file.getName());
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
        return (unzip?"Unzipping ":"Copying ")+currentFileInfo;
    }
 

    private int showFileExistsDialog(AbstractFile sourceFile, AbstractFile destFile) {
    	JPanel panel = new JPanel(new GridLayout(0,1));
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
		QuestionDialog dialog = new QuestionDialog(progressDialog, (unzip?"Unzip":"Copy")+" error", message, mainFrame,
			new String[] {SKIP_CAPTION, CANCEL_CAPTION},
			new int[]  {SKIP_ACTION, CANCEL_ACTION},
			new int[]  {SKIP_MNEMONIC, CANCEL_MNEMONIC},
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
		while(!isInterrupted()) {
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
						int ret = showErrorDialog("Unable to open zip file "+currentFile.getName());
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

        mainFrame.getLastActiveTable().requestFocus();
	}
}	
