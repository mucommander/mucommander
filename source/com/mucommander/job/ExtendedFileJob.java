
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;

/**
 * ExtendedFileJob is a container for a 'file task' : basically an operation that involves files and bytes.<br>
 * <p>What makes it different from FileJob is that the class implementing ExtendedFileJob has to be able to give
 * information about the file currently being processed.</p>
 * 
 * @author Maxence Bernard
 */
public abstract class ExtendedFileJob extends FileJob {

    public ExtendedFileJob(ProgressDialog progressDialog) {
        super(progressDialog);
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
    public abstract long getCurrentFileBytesProcessed();

    /**
     * Returns current file's size, -1 if is not available.
     */
    public abstract long getCurrentFileSize();

    /**
     * Overrides this method to returns a more accurate percent value of the job processed so far, taking
     * into account current file's percent.
     */
/*
    public int getTotalPercentDone() {
        float nbFilesProcessed = getCurrentFileIndex();
        long currentFileSize = getCurrentFileSize();
        if(currentFileSize>0)
            nbFilesProcessed += getCurrentFileBytesProcessed()/(float)currentFileSize;

//System.out.println(nbFilesProcessed+" "+currentFileSize+" "+((int)(100*(nbFilesProcessed/(float)getNbFiles()))));

        return (int)(100*(nbFilesProcessed/(float)getNbFiles()));
    }
*/
    
}