
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.MainFrame;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;

import com.mucommander.text.Translator;

import java.io.*;


/**
 * ExtendedFileJob is a container for a file task : basically an operation that involves files and bytes.<br>
 * <p>What makes it different from FileJob is that the class implementing ExtendedFileJob has to be able to give
 * information about the file currently being processed.</p>
 * 
 * @author Maxence Bernard
 */
public abstract class ExtendedFileJob extends FileJob {

    /** Number of bytes of current file that have been processed, see {@link #getCurrentFileBytesProcessed() getCurrentFileBytesProcessed} */
    protected long currentFileProcessed;

	/** Read buffer */
	protected byte buffer[];
	
	/** Size that should be allocated to read buffer */
	protected final static int READ_BLOCK_SIZE = 8192;

	
	/**
	 * Creates a new ExtendedFileJob.
	 */
    public ExtendedFileJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files) {
        super(progressDialog, mainFrame, files);
    }


	/**
	 * Copies the given InputStream's content to the given OutputStream.
	 *
	 * @return true if the stream was completely copied (i.e. job was not interrupted during the transfer)
	 */
	protected boolean copyStream(InputStream in, OutputStream out) throws IOException {
		// Init read buffer the first time
		if(buffer==null)
			buffer = new byte[READ_BLOCK_SIZE];

		// Copies the InputStream's content to the OutputStream
		int read;
		while ((read=in.read(buffer, 0, buffer.length))!=-1) {
			if(isInterrupted())
				return false;
			
			out.write(buffer, 0, read);
			nbBytesProcessed += read;
			currentFileProcessed += read;
		}
		
		return true;
	}


	/**
	 * Copies the given source file to the specified destination file, optionally resuming the operation.
	 *
	 * @return true if the file was completely copied (i.e. job was not interrupted during the transfer)
	 */
	protected boolean copyFile(AbstractFile sourceFile, AbstractFile destFile, boolean append) throws FileJobException {
		OutputStream out = null;
		InputStream in = null;

		try {
			// Try to open InputStream
			try  {
				long destFileSize = destFile.getSize();
		
				if(append && destFileSize!=-1) {
					in = sourceFile.getInputStream(destFileSize); 
					currentFileProcessed += destFileSize;
				}
				else {
					in = sourceFile.getInputStream();
				}
			}
			catch(IOException e1) {
if(com.mucommander.Debug.ON) e1.printStackTrace();
				throw new FileJobException(FileJobException.CANNOT_OPEN_SOURCE);
			}
	
			// Try to open OutputStream
			try  { out = destFile.getOutputStream(append); }
			catch(IOException e2) {
if(com.mucommander.Debug.ON) e2.printStackTrace();
				throw new FileJobException(FileJobException.CANNOT_OPEN_DESTINATION);
			}

			// Try to copy InputStream to OutputStream
			try  { return copyStream(in, out); }
			catch(IOException e3) {
if(com.mucommander.Debug.ON) e3.printStackTrace();
				throw new FileJobException(FileJobException.ERROR_WHILE_TRANSFERRING);
			}
		}
		catch(FileJobException e) {
			// Rethrow exception 
			throw e;
		}
		finally {
			// Tries to close the streams no matter what happened before
			// This block will always be executed, even if an exception
			// was thrown by the catch block
			// Finally found a use for the finally block!
			if(in!=null)
				try { in.close(); }
				catch(IOException e1) {}
			if(out!=null)
				try { out.close(); }
				catch(IOException e2) {}
		}
		
	}


	/**
	 * Tries to copy the given source file to the specified destination file (see {@link #copyFile(AbstractFile, AbstractFile, boolean} copyFile()}
	 * displaying a generic error dialog {@link #showErrorDialog(String, String) #showErrorDialog()} if something went wrong, 
	 * and giving the user to skip the file, retry or cancel.
	 */
	protected boolean tryCopyFile(AbstractFile sourceFile, AbstractFile destFile, boolean append, String errorDialogTitle) {
		// Copy file to destination
		do {				// Loop for retry
			try {
				return copyFile(sourceFile, destFile, append);
			}
			catch(FileJobException e) {
				// Copy failed
				if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Copy failed: "+e);
				
				int reason = e.getReason();
				int choice;
				switch(reason) {
					// Could not open source file for read
					case FileJobException.CANNOT_OPEN_SOURCE:
						// Ask the user what to do
						choice = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_source", sourceFile.getName()));
						break;
					// Could not open destination file for write
					case FileJobException.CANNOT_OPEN_DESTINATION:
						choice = showErrorDialog(errorDialogTitle, Translator.get("cannot_write_destination", sourceFile.getName()));
						break;
					// An error occurred during file transfer
					case FileJobException.ERROR_WHILE_TRANSFERRING:
					default:
						choice = showErrorDialog(errorDialogTitle, 
							Translator.get("error_while_transferring", sourceFile.getName()),
							new String[]{SKIP_TEXT, APPEND_TEXT, RETRY_TEXT, CANCEL_TEXT},
							new int[]{SKIP_ACTION, APPEND_ACTION, RETRY_ACTION, CANCEL_ACTION}
						);
						break;
				}
				
				// cancel action or close dialog
				if(choice==-1 || choice==CANCEL_ACTION) {
					stop();
					return false;
				}
				else if(choice==SKIP_ACTION) { 	// skip
					return false;
				}
				// Retry action (append or retry)
				else {
					if(reason==FileJobException.ERROR_WHILE_TRANSFERRING) {
						// Reset processed bytes counter
						currentFileProcessed = 0;
						// Append resumes transfer
						append = choice==APPEND_ACTION;
					}
					continue;
				}
			}
		} while(true);
	}
	
	
    /**
     * Computes and returns the percent done of current file. Returns 0 if current file's size is not available
     * (getNbCurrentFileBytesProcessed() returns -1).
     */
    public int getFilePercentDone() {
        long currentFileSize = getCurrentFileSize();
        if(currentFileSize<=0)
            return 0;
        else
            return (int)(100*getCurrentFileBytesProcessed()/(float)currentFileSize);

    }

    
    /**
     * Returns the number of bytes of the current file that have been processed.
     */
    public long getCurrentFileBytesProcessed() {
		return currentFileProcessed;
	}


    /**
     * Returns current file's size, -1 if is not available.
     */
    public long getCurrentFileSize() {
		return currentFile==null?-1:currentFile.getSize();
	}

	
	/**
	 * Advances file index and resets file bytes counter. This method should be called by subclasses whenever the job
	 * starts processing a new file.
	 */
	protected void nextFile(AbstractFile file) {
		super.nextFile(file);
		currentFileProcessed = 0;
	}


    /**
     * Overrides this method to returns a more accurate percent value of the job processed so far, taking
     * into account current file's percent.
     */
    public int getTotalPercentDone() {
        float nbFilesProcessed = getCurrentFileIndex();
		
		// If file is in base folder and is not a directory
		if(currentFile!=null && files.indexOf(currentFile)!=-1 && !currentFile.isDirectory()) {
			// Take into account current file's progress
			long currentFileSize = currentFile.getSize();
			if(currentFileSize>0)
				nbFilesProcessed += getCurrentFileBytesProcessed()/(float)currentFileSize;
		}
			
        return (int)(100*(nbFilesProcessed/(float)getNbFiles()));
    }
    
}