
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;

public abstract class FileJob implements Runnable {

	/** Serves to differenciate between the 'stopped' and 'not started yet' states */
	private boolean hasStarted;
	
	/** Associated dialog showing job progression */
	protected ProgressDialog progressDialog;
	
	/** Thread in which the file job is performed */
	private Thread jobThread;


	protected FileJob(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

    /**
     * Starts file job in a separate thread.
     */
    public void start() {
        // Serves to differenciate between the 'stopped' and 'not started yet' states
        hasStarted = true;
        jobThread = new Thread(this);
        jobThread.start();
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
	 * Returns the percent done for current file, may not be available
	 * for all jobs, if not -1 is returned.
	 */
	public abstract int getFilePercentDone();

	/**
	 * Returns the percent of job done so far.
	 */
	public abstract int getTotalPercentDone();
	
	/**
	 * Returns a String describing what's currently being done (e.g. "Deleting file test.zip")
	 */
	public abstract String getCurrentInfo();
}