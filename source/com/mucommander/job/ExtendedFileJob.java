
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.MainFrame;

import com.mucommander.io.CounterInputStream;
import com.mucommander.io.ByteCounter;

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

//    /** Number of bytes of current file that have been processed, see {@link #getCurrentFileBytesProcessed() getCurrentFileBytesProcessed} */
//    private long currentFileProcessed;

    private CounterInputStream cin;

    private ByteCounter counter = new ByteCounter();

//    /** Read buffer */
//    protected byte buffer[];
	
//    /** Size allocated to read buffer */
//    protected final static int READ_BLOCK_SIZE = 8192;

//    /** Default buffer size for BufferedOutputStream */
//    protected final static int OUTPUT_BUFFER_SIZE = 8192;
	
	
    /**
     * Creates a new ExtendedFileJob.
     */
    public ExtendedFileJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files) {
        super(progressDialog, mainFrame, files);
    }

	
    /**
     * Returns a BufferedOutputStream using the given OutputStream, and initialized with a large enough buffer.
     */
/*
    protected BufferedOutputStream getBufferedOutputStream(OutputStream out) {
        return new BufferedOutputStream(out, OUTPUT_BUFFER_SIZE);
    }
*/

    /**
     * Copies the given InputStream's content to the given OutputStream.
     *
     * @return true if the stream was completely copied (i.e. job was not interrupted during the transfer)
     */
/*
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
*/

    /**
     * Copies the given source file to the specified destination file, optionally resuming the operation.
     *
     * @return true if the file was completely copied (i.e. job was not interrupted during the transfer)
     */
    protected boolean copyFile(AbstractFile sourceFile, AbstractFile destFile, boolean append) throws FileJobException {
        // Determine whether AbstractFile.copyTo() should be used to copy file or streams should be copied manually.
        // Some file protocols do not provide a getOutputStream() method and require the use of copyTo(). Some other
        // may also offer server to server copy which is more efficient than stream copy.
        int copyToHint = sourceFile.getCopyToHint(destFile);

        // copyTo() should or must be used
        if(copyToHint==AbstractFile.SHOULD_HINT || copyToHint==AbstractFile.MUST_HINT) {
            try {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling copyTo()");
                sourceFile.copyTo(destFile);
                return true;
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e+", throwing FileJobException");
                throw new FileJobException(FileJobException.ERROR_WHILE_TRANSFERRING);
            }
        }

        // Copy streams manually
        OutputStream out = null;
        try {
            // Try to open InputStream
            try  {
                long destFileSize = destFile.getSize();
        
                if(append && destFileSize!=-1) {
                    this.cin = new CounterInputStream(sourceFile.getInputStream(destFileSize), counter); 
//                    currentFileProcessed += destFileSize;
                    counter.add(destFileSize);
                }
                else {
                    this.cin = new CounterInputStream(sourceFile.getInputStream(), counter);
                }
            }
            catch(IOException e1) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e1+", throwing FileJobException");
                throw new FileJobException(FileJobException.CANNOT_OPEN_SOURCE);
            }
    
            try  {
                // Copy stream to destination file
                destFile.copyStream(cin, append);
                return true;
            }
            catch(IOException e2) {
                if(isInterrupted())
                    return false;
                    
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException caught: "+e2+", throwing FileJobException");
                throw new FileJobException(FileJobException.ERROR_WHILE_TRANSFERRING);
            }
        }
        finally {
            // This block will always be executed, even if an exception
            // was thrown in the catch block

            // Update total number of bytes processed
            this.nbBytesProcessed += counter.getByteCount();

            // Tries to close the streams no matter what happened before
            if(cin!=null) {
                try { cin.close(); }
                catch(IOException e1) {}
            }

            if(out!=null)
                try { out.close(); }
                catch(IOException e2) {}
        }
    }


    /**
     * Tries to copy the given source file to the specified destination file (see {@link #copyFile(AbstractFile,AbstractFile,boolean)}
     * displaying a generic error dialog {@link #showErrorDialog(String, String) #showErrorDialog()} if something went wrong, 
     * and giving the user the choice to skip the file, retry or cancel.
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
//                        currentFileProcessed = 0;
                        counter.reset();
                        // Append resumes transfer
                        append = choice==APPEND_ACTION;
                    }
                    continue;
                }
            }
        } while(true);
    }
	
    
    /**
     * Overrides FileJob.stop() to stop any file copy (closes the InputStream).
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
//        return currentFileProcessed;
        return counter.getByteCount();
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
//        currentFileProcessed = 0;
        counter.reset();
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
