
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;

/**
 * FileJob is a container for a 'file task' : basically an operation that involves files and bytes.
 * The class implementing FileJob is required to give some information about the status of the job that
 * will be used to display visual indications of the job's progress.
 *
 * <p>The actual file operations are performed in a separate thread.</p>
 *
 * @author Maxence Bernard
 */
public abstract class FileJob implements Runnable {

	/** Serves to differenciate between the 'stopped' and 'not started yet' states */
	private boolean hasStarted;
	
	/** Associated dialog showing job progression */
	protected ProgressDialog progressDialog;
	
	/** Thread in which the file job is performed */
	private Thread jobThread;

	/** Timestamp in milliseconds when job started */
	private long startTime;


	public FileJob(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

    /**
     * Starts file job in a separate thread.
     */
    public void start() {
        // Serves to differenciate between the 'stopped' and 'not started yet' states
        hasStarted = true;
		startTime = System.currentTimeMillis();
        jobThread = new Thread(this);
        jobThread.start();
    }


	/**
	 * Returns the timestamp in milliseconds when the job started.
	 */
	public long getStartTime() {
		return startTime;
	}


    /**
	 * Waits for ProgressDialog to be activated (visible with keyboard focus)
     * This is very important because we want ProgressDialog to be activated BEFORE 
     * any other dialog, otherwise ProgressDialog could request focus after another FocusDialog
     * but would be nested 'under' (weird).
	 */
	protected void waitForDialog() {
		if (progressDialog!=null) {
		    while (!progressDialog.isActivated())
		    	try { Thread.sleep(1);
		    	} catch(InterruptedException e) {}
		}
	}


    /**
	 * Asks to stop file job's thread.
	 */	
	public void stop() {
		jobThread = null;
	}

	/**
	 * Returns <code>true</code> if file job has been interrupted.
	 */
	public boolean isInterrupted() {
		return jobThread == null;
	}

	/**
	 * Returns <code>true</code> if the file job is finished.
	 */
	public boolean hasFinished() {
	    return hasStarted && jobThread == null;
	}

    /**
     * Returns the percent of job processed so far.
     */
    public int getTotalPercentDone() {
        return (int)(100*(getCurrentFileIndex()/(float)getNbFiles()));
    }


    /**
     * Returns the number of file that this job contains.
     */
    public abstract int getNbFiles();

    /**
     * Returns the index of the file currently being processed (has to be <getNbFile()).
     */
    public abstract int getCurrentFileIndex();

    /**
     * Returns the number of bytes that have by been processed by this job so far.
     */
    public abstract long getTotalBytesProcessed();

	/**
	 * Returns a String describing what's currently being done (e.g. "Deleting file test.zip")
	 */
	public abstract String getStatusString();

    
}