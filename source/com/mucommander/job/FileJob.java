
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.FileExistsDialog;
import com.mucommander.ui.table.FileTable;

import com.mucommander.text.Translator;

import com.mucommander.file.AbstractFile;

import java.io.IOException;


/**
 * FileJob is a container for a 'file task' : basically an operation that involves files and bytes.
 * The class extending FileJob is required to give some information about the status of the job that
 * will be used to display visual indications of the job's progress.
 *
 * <p>The actual file operations are performed in a separate thread.</p>
 *
 * @author Maxence Bernard
 */
public abstract class FileJob implements Runnable {

	/** Thread in which the file job is performed */
	private Thread jobThread;

	/** Serves to differenciate between the 'stopped' and 'not started yet' states */
	private boolean hasStarted;
	
	/** Is this job paused ? */
	private boolean isPaused;

	/** Timestamp in milliseconds when job started */
	private long startTime;

	/** Number of milliseconds this job has been paused (been waiting for some user response).
	 * Used to compute stats like average speed.
	 */
	private long pausedTime;

	/** Contains the timestamp when this job has been put in pause (if in pause) */
	private long pauseStartTime;


	/** Associated dialog showing job progression */
	protected ProgressDialog progressDialog;

	/** Main frame on which the job is to be performed */ 
	protected MainFrame mainFrame;
	
	/** Base source folder */
	protected AbstractFile baseSourceFolder;
	
	
    /** Number of bytes processed so far, see {@link #getTotalBytesProcessed() getTotalBytesProcessed} */
    protected long nbBytesProcessed;

    /** Number of bytes skipped so far, see {@link #getTotalBytesSkipped() getTotalBytesSkipped} */
    protected long nbBytesSkipped;

	/** Index of file currently being processed, see {@link #getCurrentFileIndex() getCurrentFileIndex} */
	protected int currentFileIndex = -1;

	/** File currently being processed */
	protected AbstractFile currentFile;

	
	protected final static int CANCEL_ACTION = 0;
	protected final static int SKIP_ACTION = 1;

	protected final static String CANCEL_TEXT = Translator.get("cancel");
	protected final static String SKIP_TEXT = Translator.get("skip");
	
	
	public FileJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile baseSourceFolder) {
		this.progressDialog = progressDialog;
		this.mainFrame = mainFrame;
		this.baseSourceFolder = baseSourceFolder;
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


// Not needed anymore, now ProgressDialog starts the job once it has been activated
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
	 * Displays an error dialog with the specified title and message,
	 * wait for user choice and returns the result.
	 */
    protected int showErrorDialog(String title, String message) {
		QuestionDialog dialog = new QuestionDialog(progressDialog, 
			title,
			message,
			mainFrame,
			new String[] {SKIP_TEXT, CANCEL_TEXT},
			new int[]  {SKIP_ACTION, CANCEL_ACTION},
			0);

		return waitForUserResponse(dialog);
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
	 * Refreshes the folder's content of the given file table.
	 */
	protected void refreshTable(FileTable table) {
		try { table.refresh(); }
		catch(IOException e) {
			// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
			// like switching to a root folder
		}
	}
	

	/**
	 * Refreshes the folder's content of the given file table only
	 * if the table's current folder equals the specified folder.
	 */
	protected void refreshTableIfFolderEquals(FileTable table, AbstractFile folder) {
		if (table.getCurrentFolder().equals(folder))
			refreshTable(table);
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
//System.out.println("getTotalPercentDone: "+((int)(100*(getCurrentFileIndex()/(float)getNbFiles()))));
//System.out.println("getTotalPercentDone(2): "+getCurrentFileIndex()+" "+getNbFiles());
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
     * Returns the number of bytes that have been processed by this job so far.
     */
    public long getTotalBytesProcessed() {
		return nbBytesProcessed;
	}

    /**
	 * Returns the number of bytes reported by {@link #getTotalBytesProcessed() getTotalBytesProcessed}
	 * which have been skipped, for example when resuming a file transfer. This information must be
	 * taken into account when calculating transfer speed.
     */
    public long getTotalBytesSkipped() {
		return nbBytesSkipped;
	}
	
	
    /**
	* Returns the index of the file currently being processed (has to be < {@link #getNbFiles() getNbFiles}).
     */
    public int getCurrentFileIndex() {
        return currentFileIndex==-1?0:currentFileIndex;
    }

	/**
	 * Advances file index. This method should be called by subclasses whenever the job
	 * starts processing a new file.
	 */
	protected void nextFile(AbstractFile file) {
		this.currentFile = file;
		if(file.getFolder().equals(baseSourceFolder)
			currentFileIndex++;
	}
	

	/**
	 * Returns a String describing what's currently being done (e.g. "Deleting file test.zip")
	 */
	public abstract String getStatusString();

    /**
     * Returns the number of file that this job contains.
     */
    public abstract int getNbFiles();
	
}