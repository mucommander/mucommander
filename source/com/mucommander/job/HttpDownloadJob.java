
package com.mucommander.job;

import com.mucommander.file.*;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FileExistsDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.table.FileTable;

import com.mucommander.text.SizeFormatter;
import com.mucommander.text.Translator;

import java.io.*;

import java.util.Vector;


/**
 * This job downloads a file over HTTP.
 */
public class HttpDownloadJob extends FileJob implements Runnable, FileModifier {

	/** URL of the file to download */
	private String fileURL;
	
	/** Destination folder where the file will be downloaded */
	private AbstractFile destFolder;

	/** Index of file currently being processed */
	private int currentFileIndex;

    /** Number of bytes processed so far */
    private long nbBytesProcessed;

	private final static int CANCEL_ACTION = 0;
	private final static int SKIP_ACTION = 1;

	private final static String CANCEL_TEXT = Translator.get("cancel");
	private final static String SKIP_TEXT = Translator.get("skip");

	
    /**
	 *
	 */
	public HttpDownloadJob(MainFrame mainFrame, ProgressDialog progressDialog, String fileURL, AbstractFile destFolder) {
		super(progressDialog, mainFrame);

		this.fileURL = fileURL;
		this.destFolder = destFolder;

	}

	
	/**
	 * Recursively copies a file or folder.
	 */
    private void copyRecurse(AbstractFile file, AbstractFile destFolder, String newName) {
/*
		if(isInterrupted())
            return;

		currentFileProcessed = 0;
		currentFileSize = file.getSize();        

		String originalName = file.getName();
		String destFileName = (newName==null?originalName:newName);
       	String destFilePath = destFolder.getAbsolutePath(true)
       		+destFileName;

		currentFileInfo = "\""+originalName+ "\" ("+SizeFormatter.format(currentFileSize, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";

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
        	byte buf[] = new byte[READ_BLOCK_SIZE];
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
						currentFileProcessed += read;
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
*/
    }

	
	private void downloadFile() {
		// Determines local file name
		int slashPos = fileURL.indexOf('/', 7);
		String fileName = fileURL.substring(7, slashPos==-1?fileURL.length());
		
		// Create destination file
		AbstractFile destFile = AbstractFile.getAbstractFile(destFolder.getAbsolutePath()+destFolder.getSeparator()+fileName);
		
		// Tests if the file exists
		// and resolves a potential conflicting situation
		boolean append = false;
		if (destFile.exists())  {
			int ret = showFileExistsDialog(file, destFile, false);
		
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
		}

		// Open inputstream
		InputStream in = null;
		try {
			in = new URL(fileURL).openStream();
		}
		catch(IOException e) {
		
		}

		
		OutputStream out = null;
		try  {
			out = destFile.getOutputStream(append);

			try  {
				byte buf[] = new byte[READ_BLOCK_SIZE];
				int read;
				while ((read=in.read(buf, 0, buf.length))!=-1 && !isInterrupted()) {
					out.write(buf, 0, read);
					nbBytesProcessed += read;
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
	
		// Try to close the streams no matter what happened before
		try {
			if(in!=null)
				in.close();
			if(out!=null)
				out.close();
		}
		catch(IOException e) {
		}
	}
	
	
    private int showErrorDialog(String message) {
		QuestionDialog dialog = new QuestionDialog(progressDialog, 
			Translator.get("http_download.error_title"),
			message,
			mainFrame,
			new String[] {SKIP_TEXT, CANCEL_TEXT},
			new int[]  {SKIP_ACTION, CANCEL_ACTION},
			0);

		return waitForUserResponse(dialog);
    }

    public void run() {
/*
		currentFileIndex = 0;

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
*/
	}
	
	
	/***********************************
	 *** FileJob implemented methods ***
	 ***********************************/

    public long getTotalBytesProcessed() {
        return nbBytesProcessed;
    }

    public int getCurrentFileIndex() {
        return 0;
    }

    public int getNbFiles() {
        return 1;
    }
    
    public String getStatusString() {
        return Translator.get("http_download.downloading", fileURL);
    }
 
}	
