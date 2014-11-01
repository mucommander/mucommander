/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.job;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apple.eio.FileManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.io.ByteCounter;
import com.mucommander.commons.io.ChecksumInputStream;
import com.mucommander.commons.io.CounterInputStream;
import com.mucommander.commons.io.FileTransferException;
import com.mucommander.commons.io.ThroughputLimitInputStream;
import com.mucommander.commons.io.security.MuProvider;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;


/**
 * TransferFileJob is a container for a file task : basically an operation that involves files and bytes.<br>
 *
 * <p>What makes TransferFileJob different from FileJob (and explains its very inspired name) is that a class
 * implementing TransferFileJob has to be able to give progress information about the file currently being processed.
 * 
 * @author Maxence Bernard
 */
public abstract class TransferFileJob extends FileJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(TransferFileJob.class);
	
    /** Contains the number of bytes processed in the current file so far, see {@link #getCurrentFileByteCounter()} ()} */
    private ByteCounter currentFileByteCounter;

    /** Contains the number of bytes skipped in the current file so far, see {@link #getCurrentFileSkippedByteCounter()} ()} */
    private ByteCounter currentFileSkippedByteCounter;

    /** Contains the number of bytes processed so far, see {@link #getTotalByteCounter()} */
    private ByteCounter totalByteCounter;

    /** Contains the number of bytes skipped so far (resumed files), see {@link #getTotalSkippedByteCounter()} */
    private ByteCounter totalSkippedByteCounter;

    /** InputStream currently being processed, may be null */
    private ThroughputLimitInputStream tlin;

    /** ThroughputLimit in bytes per second, -1 initially (no limit) */
    private long throughputLimit = -1;

    /** Has the file currently being processed been skipped ? */
    private boolean currentFileSkipped;

    /** If true, all transfers will be checked for integrity: the checksum of the source and destination file will
     *  be calculated and compared to verify they match. */
    private boolean integrityCheckEnabled;

    /** True when the checksum of the source or destination file is being calculated. */
    private boolean isCheckingIntegrity;

    /** The checksum algorithm used for checking the integrity of transferred files. The algorithm has to be the fastest
     * possible (to have the minimum impact on transfer speed) and does not need to have a good resitance to collision. */
    private final static String CHECKSUM_VERIFICATION_ALGORITHM = "Adler32";


    static {
        // Register additional MessageDigest implementations provided by the muCommander API
        MuProvider.registerProvider();
    }

    /**
     * Creates a new TransferFileJob.
     */
    public TransferFileJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files) {
        super(progressDialog, mainFrame, files);

        this.currentFileByteCounter = new ByteCounter();
        this.currentFileSkippedByteCounter = new ByteCounter();

        // Account the current file's byte counter in the total byte counter
        this.totalByteCounter = new ByteCounter(currentFileByteCounter);
        this.totalSkippedByteCounter = new ByteCounter(currentFileSkippedByteCounter);
    }

	
    /**
     * Copies the given source file to the specified destination file, optionally resuming the operation.
     * As much as the source and destination protocols allow, the source file's date and permissions will be preserved.
     */
    protected void copyFile(AbstractFile sourceFile, AbstractFile destFile, boolean append) throws FileTransferException {
        // Reset this field in case it was set to true for the previous file
        isCheckingIntegrity = false;

        // Throw a specific FileTransferException if source and destination files are identical
        if(sourceFile.equalsCanonical(destFile))
            throw new FileTransferException(FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL);

        // Determine whether or not AbstractFile.copyRemotelyTo() should be used to copy the file.
        // Some file protocols do not provide a getOutputStream() method and require the use of copyRemotelyTo(). Some other
        // may also offer server to server copy which is more efficient than stream copy.

        boolean copied = false;
        if(sourceFile.isFileOperationSupported(FileOperation.COPY_REMOTELY)) {
            try {
                sourceFile.copyRemotelyTo(destFile);
                copied = true;
            }
            catch(IOException e) {
                // The file will be copied manually
            }
        }

        // If the file wasn't copied using copyRemotelyTo(), or if copyRemotelyTo() failed
        InputStream in = null;
        if(!copied) {
            // Copy source file stream to destination file
            try {
                long inLength = sourceFile.getSize();

                // Try to open InputStream
                try  {
                    long destFileSize = destFile.getSize();
                    if(append && destFileSize!=-1) {
                        in = sourceFile.getInputStream(destFileSize);
                        // Do not calculate checksum, as it needs to be calculated on the whole file

                        inLength -= destFileSize;
                        // Increase current file ByteCounter by the number of bytes skipped
                        currentFileByteCounter.add(destFileSize);
                        // Increase skipped ByteCounter by the number of bytes skipped
                        currentFileSkippedByteCounter.add(destFileSize);
                    }
                    else {
                        in = sourceFile.getInputStream();
                        if(integrityCheckEnabled)
                            in = new ChecksumInputStream(in, MessageDigest.getInstance(CHECKSUM_VERIFICATION_ALGORITHM));
                    }

                    setCurrentInputStream(in);
                }
                catch(Exception e) {
                    LOGGER.debug("IOException caught, throwing FileTransferException", e);
                    throw new FileTransferException(FileTransferException.OPENING_SOURCE);
                }

                // Copy source stream to destination file
                destFile.copyStream(tlin, append, inLength);
            }
            finally {
                // This block will always be executed, even if an exception
                // was thrown in the catch block

                // Tries to close the streams no matter what happened before
                closeCurrentInputStream();
            }
        }

        // Preserve source file's date
        if(destFile.isFileOperationSupported(FileOperation.CHANGE_DATE)) {
            try {
                destFile.changeDate(sourceFile.getDate());
            }
            catch (IOException e) {
                LOGGER.debug("failed to change the date of "+destFile, e);
                // Fail silently
            }
        }

        // Preserve source file's permissions: preserve only the permissions bits that are supported by the source file
        // and use default permissions for the rest of them.
        if(destFile.isFileOperationSupported(FileOperation.CHANGE_PERMISSION)) {
            try {
                destFile.importPermissions(sourceFile, FilePermissions.DEFAULT_FILE_PERMISSIONS);  // use #importPermissions(AbstractFile, int) to avoid isDirectory test
            }
            catch(IOException e) {
                LOGGER.debug("failed to import "+sourceFile+" permissions into "+destFile, e);
                // Fail silently
            }
        }

        // Under Mac OS X only, preserving the file type and creator
        if(OsFamily.MAC_OS_X.isCurrent()
            && sourceFile.hasAncestor(LocalFile.class)
            && destFile.hasAncestor(LocalFile.class)) {

            String sourcePath = sourceFile.getAbsolutePath();
            try {
                FileManager.setFileTypeAndCreator(destFile.getAbsolutePath(), FileManager.getFileType(sourcePath), FileManager.getFileCreator(sourcePath));
            }
            catch(IOException e) {
                // Swallow the exception and do not interrupt the transfer
                LOGGER.debug("Error while setting Mac OS X file type and creator on destination", e);
            }
        }

        // This block is executed only if integrity check has been enabled (disabled by default)
        if(integrityCheckEnabled) {
            String sourceChecksum;
            String destinationChecksum;

            // Indicate that integrity is being checked, the value is reset when the next file starts
            isCheckingIntegrity = true;

            if(in!=null && (in instanceof ChecksumInputStream)) {
                // The file was copied with a ChecksumInputStream, the checksum is already calculated, simply
                // retrieve it
                sourceChecksum = ((ChecksumInputStream)in).getChecksumString();
            }
            else {
                // The file was copied using AbstractFile#copyRemotelyTo(), or the transfer was resumed:
                // we have to calculate the source file's checksum from scratch.
                try {
                    sourceChecksum = calculateChecksum(sourceFile);
                }
                catch(Exception e) {
                    throw new FileTransferException(FileTransferException.READING_SOURCE);
                }
            }

            LOGGER.debug("Source checksum= "+sourceChecksum);

            // Calculate the destination file's checksum
            try {
                destinationChecksum = calculateChecksum(destFile);
            }
            catch(Exception e) {
                throw new FileTransferException(FileTransferException.READING_DESTINATION);
            }

            LOGGER.debug("Destination checksum= "+destinationChecksum);

            // Compare both checksums and throw an exception if they don't match
            if(!sourceChecksum.equals(destinationChecksum)) {
                throw new FileTransferException(FileTransferException.CHECKSUM_MISMATCH);
            }
        }
    }

    private String calculateChecksum(AbstractFile file) throws IOException, NoSuchAlgorithmException {
        currentFileByteCounter.reset();
        InputStream in = setCurrentInputStream(file.getInputStream());
        try {
            return AbstractFile.calculateChecksum(in, MessageDigest.getInstance(CHECKSUM_VERIFICATION_ALGORITHM));
        }
        finally {
            closeCurrentInputStream();
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
                // If the job was interrupted by the user at the time the exception occurred, it most likely means that
                // the IOException was caused by the stream being closed as a result of the user interruption.
                // If that is the case, the exception should not be interpreted as an error.
                // Same goes if the current file was skipped.
                if(getState()==INTERRUPTED || wasCurrentFileSkipped())
                    return false;

                // Print the exception's stack trace
                LOGGER.debug("Copy failed", e);

                int reason = e.getReason();
                int choice;
                switch(reason) {
                    // Could not open source file for read
                    case FileTransferException.OPENING_SOURCE:
                        choice = showErrorDialog(errorDialogTitle, Translator.get("cannot_read_file", sourceFile.getName()));
                        break;
                    // Could not open destination file for write
                    case FileTransferException.OPENING_DESTINATION:
                        choice = showErrorDialog(errorDialogTitle, Translator.get("cannot_write_file", destFile.getName()));
                        break;
                    // Source and destination files are identical
                    case FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL:
                        choice = showErrorDialog(errorDialogTitle, Translator.get("same_source_destination"));
                        break;
                    // Checksum of source and destination files don't match
                    case FileTransferException.CHECKSUM_MISMATCH:
                        choice = showErrorDialog(errorDialogTitle, Translator.get("integrity_check_error"));
                        break;
                    default:
                        choice = showErrorDialog(errorDialogTitle,
                                                 Translator.get("error_while_transferring", sourceFile.getName()),
                                                 new String[]{SKIP_TEXT, SKIP_ALL_TEXT, APPEND_TEXT, RETRY_TEXT, CANCEL_TEXT},
                                                 new int[]{SKIP_ACTION, SKIP_ALL_ACTION, APPEND_ACTION, RETRY_ACTION, CANCEL_ACTION}
                                                 );
                    break;
                }

                // Retry action (append or retry)
                if(choice==RETRY_ACTION || choice==APPEND_ACTION) {
                    // Reset current file byte counters
                    currentFileByteCounter.reset();
                    currentFileSkippedByteCounter.reset();
                    // Append resumes transfer
                    append = choice==APPEND_ACTION;
                    continue;
                }

                // Skip or Cancel action (stop() is already called by showErrorDialog)
                return false;
            }
        } while(true);
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
    protected synchronized InputStream setCurrentInputStream(InputStream in) {
        if(tlin==null) {
            tlin = new ThroughputLimitInputStream(new CounterInputStream(in, currentFileByteCounter), throughputLimit);
        }
        else {
            tlin.setUnderlyingInputStream(new CounterInputStream(in, currentFileByteCounter));
        }

        return tlin;
    }

    /**
     * Closes the currently registered source InputStream.
     */
    protected synchronized void closeCurrentInputStream() {
        if(tlin !=null) {
            try { tlin.close(); }
            catch(IOException e) {}
        }
    }


    /**
     * Returns <code>true</code> if file transfers need to be checked for data integrity. In this case, the checksum of
     * the source and destination files are both calculated and compared to verify they match.
     *
     * @return true if file transfers need to be checked for data integrity
     */
    public boolean isIntegrityCheckEnabled() {
        return integrityCheckEnabled;
    }

    /**
     * Specifies if file transfers need to be checked for data integrity. If <code>true</code> is specified, the
     * checksum of the source and destination files will both be calculated and compared to verify they match.
     *
     * @param integrityCheckEnabled true if file transfers need to be checked for data integrity
     */
    public void setIntegrityCheckEnabled(boolean integrityCheckEnabled) {
        this.integrityCheckEnabled = integrityCheckEnabled;
    }

    /**
     * Returns <code>true</code> if the integrity of the current file is being verified.
     *
     * @return true if the integrity of the current file is being verified
     */
    protected boolean isCheckingIntegrity() {
        return isCheckingIntegrity;
    }


    /**
     * Interrupts the current file transfer and advance to the next one.
     */
    public synchronized void skipCurrentFile() {
        if(tlin !=null) {
            LOGGER.debug("skipping current file, closing "+ tlin);

            // Prevents an error from being reported when the current InputStream is closed
            currentFileSkipped = true;

            // Close the current input stream to interrupt the transfer
            closeCurrentInputStream();
        }

        // Resume job if currently paused 
        if(getState()==PAUSED)
            setPaused(false);
    }

    /**
     * Return <code>true</code> if the file that is currently being processed has been skipped.
     *
     * @return true if the file that is currently being processed has been skipped
     */
    public synchronized boolean wasCurrentFileSkipped() {
        return currentFileSkipped;
    }

    /**
     * Returns the percentage of the current file that has been processed, <code>0</code> if the current file's size
     * is not available (in this case getNbCurrentFileBytesProcessed() returns <code>-1</code>).
     *
     * @return the percentage of the current file that has been processed
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
     *
     * @return the number of bytes that have been processed in the current file
     */
    public ByteCounter getCurrentFileByteCounter() {
        return currentFileByteCounter;
    }

    /**
     * Returns the number of bytes that have been skipped in the current file. Bytes are skipped when file transfers
     * are resumed.
     *
     * @return the number of bytes that have been skipped in the current file
     */
    public ByteCounter getCurrentFileSkippedByteCounter() {
        return currentFileSkippedByteCounter;
    }

    /**
     * Returns the size of the file currently being processed, <code>-1</code> if this information is not available.
     *
     * @return the size of the file currently being processed, -1 if this information is not available.
     */
    public long getCurrentFileSize() {
        return getCurrentFile()==null?-1:getCurrentFile().getSize();
    }


    /**
     * Returns a {@link ByteCounter} that holds the total number of bytes that have been processed by this job so far.
     *
     * @return a ByteCounter that holds the total number of bytes that have been processed by this job so far
     */
    public ByteCounter getTotalByteCounter() {
        return totalByteCounter;
    }

    /**
     * Returns a {@link ByteCounter} that holds the total number of bytes that have been skipped by this job so far.
     * Bytes are skipped when file transfers are resumed.
     *
     * @return a ByteCounter that holds the total number of bytes that have been skipped by this job so far
     */
    public ByteCounter getTotalSkippedByteCounter() {
        return totalSkippedByteCounter;
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

        synchronized(this) {
            if(getState()!=PAUSED && tlin !=null)
                tlin.setThroughputLimit(throughputLimit);
        }
    }

    /**
     * Returns the current transfer throughput limit, in bytes per second. <code>0</code> or <code>-1</code> means that
     * there currently is no limit to the attainable transfer speed (full speed).
     *
     * @return the current transfer throughput limit, in bytes per second
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
    @Override
    protected void jobStopped() {
        super.jobStopped();

        synchronized(this) {
            if(tlin !=null) {
                LOGGER.debug("closing current InputStream "+ tlin);

                closeCurrentInputStream();
            }
        }
    }


    /**
     * Overrides {@link FileJob#jobPaused()} to pause any file processing
     * by having the source InputStream's read methods lock.
     */
    @Override
    protected void jobPaused() {
        super.jobPaused();

        synchronized(this) {
            if(tlin !=null)
                tlin.setThroughputLimit(0);
        }
    }


    /**
     * Overrides {@link FileJob#jobResumed()} to resume any file processing by releasing
     * the lock on the source InputStream's read methods.
     */
    @Override
    protected void jobResumed() {
        super.jobResumed();

        synchronized(this) {
            // Restore previous throughput limit (if any, -1 by default)
            if(tlin !=null)
                tlin.setThroughputLimit(throughputLimit);
        }
    }


    /**
     * Advances file index and resets current file's byte counters. This method should be called by subclasses
     * whenever the job starts processing a new file.
     */
    @Override
    protected void nextFile(AbstractFile file) {
        totalByteCounter.add(currentFileByteCounter, true);
        totalSkippedByteCounter.add(currentFileSkippedByteCounter, true);

        // Reset some fields that need it
        currentFileSkipped = false;

        super.nextFile(file);
    }

    /**
     * Method overridden to return a more accurate percentage of job processed so far by taking into account the current
     * file's percentage of completion.
     */
    @Override
    public float getTotalPercentDone() {
        float nbFilesProcessed = getCurrentFileIndex();
        int nbFiles = getNbFiles();

        // If file is in base folder and is not a directory...
        if(getCurrentFile()!=null && nbFilesProcessed!=nbFiles && files.indexOf(getCurrentFile())!=-1 && !getCurrentFile().isDirectory()) {
            // Add current file's progress
            long currentFileSize = getCurrentFile().getSize();
            if(currentFileSize>0)
                nbFilesProcessed += getCurrentFileByteCounter().getByteCount()/(float)currentFileSize;
        }

        return nbFilesProcessed/(float)nbFiles;
    }

    /**
     * This method is overridden to return a custom string "Checking integrity of CURRENT_FILE" when the current file
     * is being checked for integrity.
     */
    @Override
    public String getStatusString() {
        if(isCheckingIntegrity())
            return Translator.get("progress_dialog.verifying_file", getCurrentFilename());

        return super.getStatusString();
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
////AppLogger.finest("nbFilesProcessed="+(int)nbFilesProcessed+" nbFilesDiscovered="+getNbFilesDiscovered()+" %="+((int)100*nbFilesProcessed/getNbFilesDiscovered()));
//
//        return nbFilesProcessed/getNbFilesDiscovered();
//    }
}
