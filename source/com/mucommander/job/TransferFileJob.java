
package com.mucommander.job;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.io.ByteCounter;
import com.mucommander.io.CounterInputStream;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.ThroughputLimitInputStream;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;

import java.io.IOException;
import java.io.InputStream;


/**
 * TransferFileJob is a container for a file task : basically an operation that involves files and bytes.<br>
 *
 * <p>What makes TransferFileJob different from FileJob (and explains its very inspired name) is that a class
 * implementing TransferFileJob has to be able to give progress information about the file currently being processed.
 * 
 * @author Maxence Bernard
 */
public abstract class TransferFileJob extends FileJob {

    /** Contains the number of bytes processed in the current file so far, see {@link #getCurrentFileByteCounter()} ()} */
    private ByteCounter currentFileByteCounter;

    /** Contains the number of bytes processed so far, see {@link #getTotalByteCounter()} */
    private ByteCounter totalByteCounter;


    /** InputStream currently being processed, may be null */
    private ThroughputLimitInputStream tlin;

    /** ThroughputLimit in bytes per second, -1 initially (no limit) */
    private long throughputLimit = -1;


    /**
     * Creates a new TransferFileJob.
     */
    public TransferFileJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files) {
        super(progressDialog, mainFrame, files);

        this.currentFileByteCounter = new ByteCounter();

        // Account the current file's byte counter in the total byte counter
        this.totalByteCounter = new ByteCounter(currentFileByteCounter);
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
            return;
        }

        // Throw a specific FileTransferException if source and destination files are identical
        if(sourceFile.equals(destFile))
            throw new FileTransferException(FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL);

        // Copy source file stream to destination file
        try {
            // Try to open InputStream
            try  {
                long destFileSize = destFile.getSize();
        
                if(append && destFileSize!=-1) {
                    setCurrentInputStream(sourceFile.getInputStream(destFileSize));
                    currentFileByteCounter.add(destFileSize);
                }
                else {
                    setCurrentInputStream(sourceFile.getInputStream());
                }
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) {
                    com.mucommander.Debug.trace("IOException caught: "+e+", throwing FileTransferException");
                    e.printStackTrace();
                }
                throw new FileTransferException(FileTransferException.OPENING_SOURCE);
            }
    
            // Copy source stream to destination file
            destFile.copyStream(tlin, append);
        }
        finally {
            // This block will always be executed, even if an exception
            // was thrown in the catch block

            // Tries to close the streams no matter what happened before
            if(tlin !=null) {
                try { tlin.close(); }
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
                // If job was interrupted by the user at the time when the exception occurred,
                // it most likely means that the exception by user cancellation.
                // In this case, the exception should not be interpreted as an error.
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
                        choice = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_file", sourceFile.getName()));
                        break;
                    // Could not open destination file for write
                    case FileTransferException.OPENING_DESTINATION:
                        choice = showErrorDialog(errorDialogTitle, Translator.get("cannot_write_file", sourceFile.getName()));
                        break;
                    // Source and destination files are identical
                    case FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL:
                        choice = showErrorDialog(errorDialogTitle, Translator.get("same_source_destination"));
                        break;
    //                     An error occurred during file transfer
    //                case FileTransferException.ERROR_WHILE_TRANSFERRING:
                    default:
                        choice = showErrorDialog(errorDialogTitle,
                                                 Translator.get("error_while_transferring", sourceFile.getName()),
                                                 new String[]{SKIP_TEXT, APPEND_TEXT, RETRY_TEXT, CANCEL_TEXT},
                                                 new int[]{SKIP_ACTION, APPEND_ACTION, RETRY_ACTION, CANCEL_ACTION}
                                                 );
                    break;
                }

                // Retry action (append or retry)
                if(choice==RETRY_ACTION || choice==APPEND_ACTION) {
                    // Reset processed bytes currentFileByteCounter
                    currentFileByteCounter.reset();
                    // Append resumes transfer
                    append = choice==APPEND_ACTION;
                    continue;
                }

                // Skip or Cancel action (stop() is already called by showErrorDialog)
                return false;

//                // cancel action or close dialog
//                if(choice==-1 || choice==CANCEL_ACTION) {
//                    stop();
//                    return false;
//                }
//                else if(choice==SKIP_ACTION) { 	// skip
//                    return false;
//                }
//                // Retry action (append or retry)
//                else {
//                    // Reset processed bytes currentFileByteCounter
//                    currentFileByteCounter.reset();
//                    // Append resumes transfer
//                    append = choice==APPEND_ACTION;
//                    continue;
//                }
            }
        } while(true);
    }


    /**
     * Returns the percentage of the current file which has been processed, or 0 if current file's size is not available
     * (in this case getNbCurrentFileBytesProcessed() returns -1).
     */
    public float getFilePercentDone() {
        long currentFileSize = getCurrentFileSize();
        if(currentFileSize<=0)
            return 0;
        else
            return getCurrentFileByteCounter().getByteCount()/(float)currentFileSize;
    }


    /**
     * Returns the number of bytes that have been processed in the current file.
     */
    public ByteCounter getCurrentFileByteCounter() {
        return currentFileByteCounter;
    }


    /**
     * Returns the size of the file currently being processed, -1 if is not available.
     */
    public long getCurrentFileSize() {
        return currentFile==null?-1:currentFile.getSize();
    }


    /**
     * Returns a {@link ByteCounter} that holds the total number of bytes that have been processed by this job so far.
     */
    public synchronized ByteCounter getTotalByteCounter() {
        return totalByteCounter;
    }


    /**
     * Registers the given InputStream as currently in use, in order to:
     * <ul>
     * <li>count the number of bytes that have been read from it (see {@link #getCurrentFileByteCounter()})
     * <li>block read methods calls when the job is paused
     * <li>limit the throughput if a limit has been specified (see {@link #setThroughputLimit(long)})
     * <li>close the InputStream when the job is stopped
     * </ul>
     *
     * <p>This method should be called by subclasses when creating a new InputStream, before the InputStream is used.
     *  
     * @param in the InputStream to be used
     * @return the 'augmented' InputStream using the given stream as the underlying InputStream
     */
    public InputStream setCurrentInputStream(InputStream in) {
        if(tlin==null) {
            tlin = new ThroughputLimitInputStream(new CounterInputStream(in, currentFileByteCounter), throughputLimit);
        }
        else {
            tlin.setUnderlyingInputStream(new CounterInputStream(in, currentFileByteCounter));
        }

        return tlin;
    }


    /**
     * Sets a transfer throughput limit in bytes per seconds, replacing any previous limit.
     * This limit corresponds to the number of bytes that can be read from a registered InputStream.
     *
     * <p>Specifying 0 or -1 disables any throughput limit, the transfer will be carried out at full speed.
     *
     * <p>If this job is paused, the new limit will be effective after the job has been resumed.
     * If not, it will be effective immediately.
     *
     * @param bytesPerSecond new throughput limit in bytes per second, 0 or -1 to disable the limit
     */
    public void setThroughputLimit(long bytesPerSecond) {
        // Note: ThroughputInputStream interprets 0 as a complete pause (blocks reads) which is different
        // from what a user would expect when specifying 0 as a limit
        this.throughputLimit = bytesPerSecond<=0?-1:bytesPerSecond;

        if(!isPaused() && tlin !=null)
            tlin.setThroughputLimit(throughputLimit);
    }


    /**
     * Returns the current transfer throughput limit, in bytes per second.
     * 0 or -1 means that no there currently is no limit to the attainable transfer speed (full speed).
     */
    public long getThroughputLimit() {
        return throughputLimit;
    }
    

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overrides {@link FileJob#jobStopped()} to stop any file processing by closing the source InputStream.
     */
    protected void jobStopped() {
        super.jobStopped();

        if(tlin !=null) {
            if(Debug.ON) Debug.trace("closing current InputStream "+ tlin);

            try { tlin.close(); }
            catch(IOException e) {}
        }
    }


    /**
     * Overrides {@link FileJob#jobPaused()} to pause any file processing
     * by having the source InputStream's read methods lock.
     */
    protected void jobPaused() {
        super.jobPaused();

        if(tlin !=null)
            tlin.setThroughputLimit(0);
    }


    /**
     * Overrides {@link FileJob#jobResumed()} to resume any file processing by releasing
     * the lock on the source InputStream's read methods.
     */
    protected void jobResumed() {
        super.jobResumed();

        if(tlin !=null)
            tlin.setThroughputLimit(-1);
    }


    /**
     * Advances file index and resets file bytes currentFileByteCounter. This method should be called by subclasses whenever the job
     * starts processing a new file.
     */
    protected void nextFile(AbstractFile file) {
        totalByteCounter.add(currentFileByteCounter, true);

        super.nextFile(file);
    }



//    /**
//     * Method overridden to return a more accurate percentage of job processed so far by taking
//     * into account the current file's processed percentage.
//     */
//    public float getTotalPercentDone() {
//        float nbFilesProcessed = getNbFilesProcessed();
//
//        // If file is in base folder and is not a directory
//        if(currentFile!=null && files.indexOf(currentFile)!=-1 && !currentFile.isDirectory()) {
//            // Take into account current file's progress
//            long currentFileSize = currentFile.getSize();
//            if(currentFileSize>0)
//                nbFilesProcessed += getCurrentFileByteCounter().getByteCount()/(float)currentFileSize;
//        }
//
////if(Debug.ON) Debug.trace("nbFilesProcessed="+(int)nbFilesProcessed+" nbFilesDiscovered="+getNbFilesDiscovered()+" %="+((int)100*nbFilesProcessed/getNbFilesDiscovered()));
//
//        return nbFilesProcessed/getNbFilesDiscovered();
//    }

    /**
     * Method overridden to return a more accurate percentage of job processed so far by taking
     * into account the current file's processed percentage.
     */
    public float getTotalPercentDone() {
        float nbFilesProcessed = getCurrentFileIndex();

        // If file is in base folder and is not a directory
        if(currentFile!=null && files.indexOf(currentFile)!=-1 && !currentFile.isDirectory()) {
            // Take into account current file's progress
            long currentFileSize = currentFile.getSize();
            if(currentFileSize>0)
                nbFilesProcessed += getCurrentFileByteCounter().getByteCount()/(float)currentFileSize;
        }

        return nbFilesProcessed/(float)getNbFiles();
    }
    
}
