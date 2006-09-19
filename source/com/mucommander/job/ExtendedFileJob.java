
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.MainFrame;

import com.mucommander.io.CounterInputStream;
import com.mucommander.io.ByteCounter;
import com.mucommander.io.FileTransferException;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;

import com.mucommander.text.Translator;

import java.io.*;


/**
 * ExtendedFileJob is a container for a file task : basically an operation that involves files and bytes.<br>
 *
 * <p>What makes ExtendedFileJob different from FileJob (and explains its very inspired name) is that a class
 * implementing ExtendedFileJob has to be able to give progress information about the file currently being processed.
 * 
 * @author Maxence Bernard
 */
public abstract class ExtendedFileJob extends FileJob {

    private CounterInputStream cin;

    private ByteCounter currentFileCounter = new ByteCounter();


    /**
     * Creates a new ExtendedFileJob.
     */
    public ExtendedFileJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files) {
        super(progressDialog, mainFrame, files);
    }

	
    /**
     * Copies the given source file to the specified destination file, optionally resuming the operation.
     */
    protected void copyFile(AbstractFile sourceFile, AbstractFile destFile, boolean append) throws FileTransferException {
        // Determine whether AbstractFile.copyTo() should be used to copy file or streams should be copied manually.
        // Some file protocols do not provide a getOutputStream() method and require the use of copyTo(). Some other
        // may also offer server to server copy which is more efficient than stream copy.
        int copyToHint = sourceFile.getCopyToHint(destFile);

        // copyTo() should or must be used
        if(copyToHint==AbstractFile.SHOULD_HINT || copyToHint==AbstractFile.MUST_HINT) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling copyTo()");
            sourceFile.copyTo(destFile);
        }

        // Copy source file stream to destination file
        try {
            // Try to open InputStream
            try  {
                long destFileSize = destFile.getSize();
        
                if(append && destFileSize!=-1) {
                    this.cin = new CounterInputStream(sourceFile.getInputStream(destFileSize), currentFileCounter);
                    currentFileCounter.add(destFileSize);
                }
                else {
                    this.cin = new CounterInputStream(sourceFile.getInputStream(), currentFileCounter);
                }
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e+", throwing FileTransferException");
                throw new FileTransferException(FileTransferException.OPENING_SOURCE);
            }
    
            // Copy source stream to destination file
            destFile.copyStream(cin, append);
        }
        finally {
            // This block will always be executed, even if an exception
            // was thrown in the catch block

            // Update total number of bytes processed
            this.nbBytesProcessed += currentFileCounter.getByteCount();

            // Tries to close the streams no matter what happened before
            if(cin!=null) {
                try { cin.close(); }
                catch(IOException e1) {}
            }
        }
    }


    /**
     * Tries to copy the given source file to the specified destination file (see {@link #copyFile(AbstractFile,AbstractFile,boolean)}
     * displaying a generic error dialog {@link #showErrorDialog(String, String) #showErrorDialog()} if something went wrong, 
     * and giving the user the choice to skip the file, retry or cancel.
     *
     * @return true if the file was properly copied, false if the transfer was interrupted / aborted by the user
     *
     */
    protected boolean tryCopyFile(AbstractFile sourceFile, AbstractFile destFile, boolean append, String errorDialogTitle) {
        // Copy file to destination
        do {				// Loop for retry
            try {
                copyFile(sourceFile, destFile, append);
                return true;
            }
            catch(FileTransferException e) {
                // If job was interrupted by the user when the exception occurred, it most likely means that the exception
                // was caused by the job cancellation. In this case, the exception should not be interpreted as an error.
                if(isInterrupted())
                    return false;

                // Copy failed
                if(com.mucommander.Debug.ON) {
                    com.mucommander.Debug.trace("Copy failed: "+e);
                    e.printStackTrace();
                }

                int reason = e.getReason();
                int choice;
                switch(reason) {
                    // Could not open source file for read
                case FileTransferException.OPENING_SOURCE:
                    // Ask the user what to do
                    choice = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_source", sourceFile.getName()));
                    break;
                    // Could not open destination file for write
                case FileTransferException.OPENING_DESTINATION:
                    choice = showErrorDialog(errorDialogTitle, Translator.get("cannot_write_destination", sourceFile.getName()));
                    break;
                    // An error occurred during file transfer
//                case FileTransferException.ERROR_WHILE_TRANSFERRING:
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
//                    if(reason==FileTransferException.ERROR_WHILE_TRANSFERRING) {
                        // Reset processed bytes currentFileCounter
                        currentFileCounter.reset();
                        // Append resumes transfer
                        append = choice==APPEND_ACTION;
//                    }
                    continue;
                }
            }
        } while(true);
    }
	
    
    /**
     * Returns the percentage of the current file which has been processed, or 0 if current file's size is not available
     * (in this case getNbCurrentFileBytesProcessed() returns -1).
     */
    public int getFilePercentDone() {
        long currentFileSize = getCurrentFileSize();
        if(currentFileSize<=0)
            return 0;
        else
            return (int)(100*getCurrentFileBytesProcessed()/(float)currentFileSize);

    }

    
    /**
     * Returns the number of bytes that have been processed in the current file .
     */
    public long getCurrentFileBytesProcessed() {
        return currentFileCounter.getByteCount();
    }


    /**
     * Returns the size of the file currently being processed, -1 if is not available.
     */
    public long getCurrentFileSize() {
        return currentFile==null?-1:currentFile.getSize();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overrides FileJob.stop() to stop any file copy (closes the source file's InputStream).
     */
    public void stop() {
        // Stop job BEFORE closing the stream so that the IOException thrown by copyStream
        // is not interpreted as a failure
        super.stop();

        if(cin!=null) {
            try {
                cin.close();
            }
            catch(IOException e) {}
        }
    }


    /**
     * Advances file index and resets file bytes currentFileCounter. This method should be called by subclasses whenever the job
     * starts processing a new file.
     */
    protected void nextFile(AbstractFile file) {
        super.nextFile(file);
        currentFileCounter.reset();
    }


    /**
     * Method overridden to add the number of bytes processed in the current file.
     */
    public long getTotalBytesProcessed() {
        return getCurrentFileBytesProcessed() + nbBytesProcessed;
    }


    /**
     * Method overridden to return a more accurate percentage of job processed so far by taking
     * into account the current file's processed percentage.
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
