
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.FileExistsDialog;

import com.mucommander.file.AbstractFile;


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

	/** Main frame on which the job is to be performed */ 
	protected MainFrame mainFrame;
	
	/** Thread in which the file job is performed */
	private Thread jobThread;

	/** Timestamp in milliseconds when job started */
	private long startTime;

	/** Number of milliseconds this job has been paused (been waiting for some user response).
	 * Used to compute stats like average speed.
	 */
	private long pausedTime;

	/** Is this job paused ? */
	private boolean isPaused;
	
	/** Contains the timestamp when this job has been put in pause (if in pause) */
	private long pauseStartTime;
	
	/** Size that should be allocated to read buffers */
	public final static int READ_BLOCK_SIZE = 8192;

	
	public FileJob(ProgressDialog progressDialog, MainFrame mainFrame) {
		this.progressDialog = progressDialog;
		this.mainFrame = mainFrame;
	}

	
    /**
     * Starts file job in a separate thread.
     */
    public void start() {
		if(com.mucommander.Debug.ON)
			System.out.println("FileJob.start(): "+this+" modifier="+(this instanceof FileModifier));
		
		// Pause auto-refresh during file job if this job potentially modifies folders contents
		// and would potentially cause table to auto-refresh
		if(this instanceof FileModifier) {
			mainFrame.getBrowser1().getFileTable().setAutoRefreshActive(false);
			mainFrame.getBrowser2().getFileTable().setAutoRefreshActive(false);
		}		
		
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
	 * Returns the number of milliseconds this job has been paused
	 * (been waiting for user response).
	 */
	public long getPausedTime() {
		return pausedTime;
	}


// No needed anymore, now ProgressDialog starts the job once it has been activated
    /**
	 * Waits for ProgressDialog to be activated (visible with keyboard focus)
     * This is very important because we want ProgressDialog to be activated BEFORE 
     * any other dialog, otherwise ProgressDialog could request focus after another FocusDialog
     * but would be nested 'under' (weird).
	 */
/*
	 protected void waitForDialog() {
		if (progressDialog!=null) {
//		    while (!progressDialog.isActivated() || !progressDialog.hasFocus()) {
		    while (!progressDialog.isActivated()) {
//if(com.mucommander.Debug.ON)
//System.out.println("FileJob.waitForDialog "+progressDialog.isActivated()+" "+progressDialog.isShowing()+" "+progressDialog.hasFocus());
		    	try { Thread.sleep(10);
		    	} catch(InterruptedException e) {}
			}
		}
	}
*/

    /**
	 * Asks to stop file job's thread.
	 */	
	public void stop() {
//		if(com.mucommander.Debug.ON)
//			System.out.println("FileJob.stop(): modifier="+(this instanceof FileModifier));

		jobThread = null;
		// Resume auto-refresh if auto-refresh has been paused
		if(this instanceof FileModifier) {
			mainFrame.getBrowser1().getFileTable().setAutoRefreshActive(true);
			mainFrame.getBrowser2().getFileTable().setAutoRefreshActive(true);
		}
	}

	
	/**
	 * This method should be called once after the job has been stopped
	 */
	public void cleanUp() {
		// Dispose associated progress dialog
		progressDialog.dispose();
	
		// Request focus on last active table
        FocusRequester.requestFocus(mainFrame.getLastActiveTable());
	}

	
	/**
	 * Returns <code>true</code> if file job has been interrupted.
	 */
	public boolean isInterrupted() {
		return jobThread == null;
	}

	
	/**
	 * Sets or unsets this job in pause mode (waiting for user response).
	 */
	private void setPaused(boolean paused) {
		if(!paused && this.isPaused) {
			this.pausedTime += System.currentTimeMillis() - this.pauseStartTime;
		}
		else if(paused) {
			this.pauseStartTime = System.currentTimeMillis();
		}
		this.isPaused = paused;
	}

	
	/**
	 * Waits for the user's answer to the given question dialog, putting this
	 * job in pause mode while waiting for the user.
	 */
	protected int waitForUserResponse(QuestionDialog dialog) {
		// Put this job in pause mode while waiting for user response
		setPaused(true);
		int retValue = dialog.getActionValue();
		// Back to work
		setPaused(false);
		return retValue;
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
	 * Shows a dialog which notifies the user that a file already exists in the destination folder
	 * under the same name and asks for what to do.
	 */
    protected int showFileExistsDialog(AbstractFile sourceFile, AbstractFile destFile) {
		QuestionDialog dialog = new FileExistsDialog(progressDialog, mainFrame, sourceFile, destFile);
		return waitForUserResponse(dialog);
	}
	

    /**
     * Returns the number of file that this job contains.
     */
    public abstract int getNbFiles();

    /**
     * Returns the index of the file currently being processed (has to be < getNbFile()).
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