
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

import java.net.URL;
import java.net.URLDecoder;


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

	
	private void downloadFile() {

		// Determines local file name
		int urlLen = fileURL.length();
		while(fileURL.charAt(urlLen-1)=='/')
			fileURL = fileURL.substring(0, --urlLen);

		int lastSlashPos = fileURL.lastIndexOf('/');
		String fileName = URLDecoder.decode(fileURL.substring(lastSlashPos==-1||lastSlashPos<7?7:lastSlashPos, urlLen));
		
		// Create destination file
		AbstractFile destFile = AbstractFile.getAbstractFile(destFolder.getAbsolutePath()+destFolder.getSeparator()+fileName);
		
		// Tests if file exists in destination folder
		boolean append = false;
		if (destFile.exists())  {
			int ret = showFileExistsDialog(fileURL, destFile);
		
			// Cancel
			if (ret==-1 || ret==FileExistsDialog.CANCEL_ACTION) {
				stop();
				return;
			}
			// Append to destination file
			else if (ret==FileExistsDialog.APPEND_ACTION) {
				append = true;
			}
			// Simply continue for overwrite
		}

		// Open inputstream
		InputStream in = null;
		try {
			in = new URL(fileURL).openStream();
		}
		catch(IOException e) {
			showErrorDialog(Translator.get("http_download.cannot_open_url"));
			return;
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
				int ret = showErrorDialog(Translator.get("http_download.cannot_copy_file", fileName));
				if(ret!=SKIP_ACTION)		// CANCEL_ACTION or close dialog
					stop();                
			}
		}
		catch(IOException e) {
			if(com.mucommander.Debug.ON)
				System.out.println(""+e);
			int ret = showErrorDialog(Translator.get("http_download.cannot_create_destination", fileName));
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

	
	/**
	 * Shows a dialog which notifies the user that a file already exists in the destination folder
	 * under the same name and asks for what to do.
	 */
    protected int showFileExistsDialog(String sourceURL, AbstractFile destFile) {
		QuestionDialog dialog = new FileExistsDialog(progressDialog, mainFrame, sourceURL, destFile);
		return waitForUserResponse(dialog);
	}
	
	
    public void run() {

		// Download file
		downloadFile();
		
        stop();
		
        // Refresh only if table's folder is destFolder
        FileTable table1 = mainFrame.getBrowser1().getFileTable();
		if (table1.getCurrentFolder().equals(destFolder))
        	try {
        		table1.refresh();
        	}
        	catch(IOException e) {
        		// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
        		// like switching to a root folder        
        	}

        // Refresh only if table's folder is destFolder
        FileTable table2 = mainFrame.getBrowser2().getFileTable();
        if (table2.getCurrentFolder().equals(destFolder))
        	try {
        		table2.refresh();
        	}
        	catch(IOException e) {
        		// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
        		// like switching to a root folder        
        	}

		cleanUp();
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
