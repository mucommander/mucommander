
package com.mucommander.job;

import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
// import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.FileExistsDialog;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;

import com.mucommander.text.Translator;
import com.mucommander.text.SizeFormatter;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;

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
	
	/** Files which are going to be processed */
	protected FileSet files;

    /** Number of files that this job contains */
    protected int nbFiles;
    
	
    /** Number of bytes processed so far, see {@link #getTotalBytesProcessed() getTotalBytesProcessed} */
    protected long nbBytesProcessed;

	/** Index of file currently being processed, see {@link #getCurrentFileIndex() getCurrentFileIndex} */
	protected int currentFileIndex = -1;

	/** File currently being processed */
	protected AbstractFile currentFile;

	/** If set to true, processed files will be unmarked from current table */
	private boolean autoUnmark = true;
	
	
	protected final static int SKIP_ACTION = 0;
	protected final static int RETRY_ACTION = 1;
	protected final static int CANCEL_ACTION = 2;
	protected final static int APPEND_ACTION = 3;

	protected final static String SKIP_TEXT = Translator.get("skip");
	protected final static String RETRY_TEXT = Translator.get("retry");
	protected final static String CANCEL_TEXT = Translator.get("cancel");
	protected final static String APPEND_TEXT = Translator.get("append");
	
	
    /**
	 * Creates a new FileJob without starting it.
	 *
	 * @param progressDialog dialog which shows this job's progress
	 * @param mainFrame mainFrame this job has been triggered by
	 * @param files files which are going to be processed
	 */
	public FileJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files) {
		this(mainFrame, files);
		this.progressDialog = progressDialog;
	}

	
    /**
	 * Creates a new FileJob without starting it, and with no associated ProgressDialog.
	 *
	 * @param mainFrame mainFrame this job has been triggered by
	 * @param files files which are going to be processed
	 */
	public FileJob(MainFrame mainFrame, FileSet files) {
		this.mainFrame = mainFrame;
	    this.files = files;
		
        this.nbFiles = files.size();
//		this.baseSourceFolder = ((AbstractFile)files.elementAt(0)).getParent();
		this.baseSourceFolder = files.getBaseFolder();
	}
	
	
	/**
	 * Specifies whether or not files that have been processed should be unmarked from current table (enabled by default).
	 */
	public void setAutoUnmark(boolean autoUnmark) {
		this.autoUnmark = autoUnmark;
	}
	
	
    /**
     * Starts file job in a separate thread.
     */
    public void start() {
		// Pause auto-refresh during file job if this job potentially modifies folders contents
		// and would potentially cause table to auto-refresh
		mainFrame.getFolderPanel1().getFileTable().setAutoRefreshActive(false);
		mainFrame.getFolderPanel2().getFileTable().setAutoRefreshActive(false);
		
        // Serves to differenciate between the 'stopped' and 'not started yet' states
        hasStarted = true;
		startTime = System.currentTimeMillis();
        jobThread = new Thread(this, "com.mucommander.job.FileJob's Thread");
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


    /**
	 * Asks to stop file job's thread.
	 */	
	public void stop() {
		// Return if job has already been stopped
		if(jobThread==null)
			return;

		jobThread = null;
	
		// Notify that the job has been stopped
		jobStopped();
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
	 * Changes current file. This method should be called by subclasses whenever the job
	 * starts processing a new file other than a top-level file, i.e. one that was passed
	 * as an argument to {@link #processFile(AbstractFile, Object) processFile()}.
	 * ({#nextFile(AbstractFile) nextFile()} is automatically called for files in base folder).
	 */
	protected void nextFile(AbstractFile file) {
		this.currentFile = file;
	}

	
	/**
	 * Returns some info about the file currently being processed, for example : "test.zip" (14KB)
	 */
	protected String getCurrentFileInfo() {
		// Update current file information used by status string
		if(currentFile==null)
			return "";
		return "\""+currentFile.getName()+"\" ("+SizeFormatter.format(currentFile.getSize(), SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";
	}
	
	
	/**
	 * This method is called when this job starts, before the first call to {@link #processFile(AbstractFile,Object) processFile()} is made.
	 * The method implementation here does nothing but it can be overriden by subclasses to perform some first-time initializations.
	 */
	protected void jobStarted() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
	}
	

	/**
	 * This method is called when this job has completed normal execution : all files have been processed without any interuption
	 * (without any call to {@link #stop() stop()}).
	 *
	 * <p>The call happens after the last call to {@link #processFile(AbstractFile,Object) processFile()} is made.
	 * The method implementation here does nothing but it can be overriden by subclasses to properly complete the job.</p>
	 
	 * <p>Note that this method will NOT be called if a call to {@link #stop() stop()} was made before all files were processed.</p>
	 */
	protected void jobCompleted() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
	}
	
	
	/**
	 * This method is called when this job has been stopped. The call after any call to {@link #processFile(AbstractFile,Object) processFile()} and
	 * {@link jobComplete() jobComplete()} is made.
	 * The method implementation here does nothing but it can be overriden by subclasses to properly terminate the job. This is where you want to close
	 * any opened connections.
	 *
	 * <p>Note that unlike {@link jobComplete() jobComplete()} this method is always called, whether the job has been completed (all
	 * files were processed) or has been interrupted in the middle.</p>
	 */
	protected void jobStopped() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
	}
	
	
	/**
	 * Actual job is performed in a separate thread.
	 */
    public void run() {
		FileTable activeTable = mainFrame.getLastActiveTable();
		FileTableModel activeTableModel = (FileTableModel)activeTable.getModel();
		AbstractFile currentFile;

		// Notifies that this job starts
		jobStarted();

		// Loop on all source files, checking that job has not been interrupted
		for(int i=0; i<nbFiles && !isInterrupted(); i++) {
			currentFile = files.fileAt(i);
	
			// Change current file and advance file index
			currentFileIndex = i;
			nextFile(currentFile);
			
			// Process current file
			boolean success = processFile(currentFile, null);
			
			// Unmark file in active table
			if(autoUnmark && success && !isInterrupted()) {
				activeTableModel.setFileMarked(currentFile, false);
				activeTable.repaint();
			}

			if(i==nbFiles-1) {
				// Notifies that job has been completed (all files have been processed).
				jobCompleted();
			}
        }

		// If this job hasn't already been stopped, call stop()
		if(!isInterrupted()) {
			// Stop job
			stop();
		}
		
		// Dispose progress dialog (if any) 
		if(progressDialog!=null)
			progressDialog.dispose();

        // Refresh tables's current folders, based on the job's refresh policy.
		refreshTables();
	}

	
	/**
	 * Displays an error dialog with the specified title and message,
	 * offers to skip the file, retry or cancel and waits for user choice.
	 * The job is stopped if 'cancel' or 'close' was chosen, and the result 
	 * is returned.
	 */
    protected int showErrorDialog(String title, String message) {
		String actionTexts[] = new String[]{SKIP_TEXT, RETRY_TEXT, CANCEL_TEXT};
		int actionValues[] = new int[]{SKIP_ACTION, RETRY_ACTION, CANCEL_ACTION};
		
/*
		int userChoice = showErrorDialog(title, message, actionTexts, actionValues);
		if(userChoice==-1 || userChoice==CANCEL_ACTION)
			stop();
		
		return userChoice;
*/
		return showErrorDialog(title, message, actionTexts, actionValues);
	}


	
	/**
	 * Displays an error dialog with the specified title and message and returns the selection action's value.
	 */
    protected int showErrorDialog(String title, String message, String actionTexts[], int actionValues[]) {
		QuestionDialog dialog;
		
		if(progressDialog==null)
			dialog = new QuestionDialog(mainFrame, 
				title,
				message,
				mainFrame,
				actionTexts,
				actionValues,
				0);
		else
			dialog = new QuestionDialog(progressDialog, 
				title,
				message,
				mainFrame,
				actionTexts,
				actionValues,
				0);

		// Cancel or close dialog stops this job
		int userChoice = waitForUserResponse(dialog);
		if(userChoice==-1 || userChoice==CANCEL_ACTION)
			stop();
		
		return userChoice;
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
	 * Creates and returns a dialog which notifies the user that a file already exists in the destination folder
	 * under the same name and asks for what to do.
	 */
    protected FileExistsDialog getFileExistsDialog(AbstractFile sourceFile, AbstractFile destFile, boolean applyToAllOption) {
		if(progressDialog==null)
			return new FileExistsDialog(mainFrame, mainFrame, sourceFile, destFile, applyToAllOption);
		else
			return new FileExistsDialog(progressDialog, mainFrame, sourceFile, destFile, applyToAllOption);
	}


	/**
	 * Check and if needed, refreshes both file tables's current folders, based on the job's refresh policy.
	 */
	protected void refreshTables() {
		FileTable table1 = mainFrame.getFolderPanel1().getFileTable();
		FileTable table2 = mainFrame.getFolderPanel2().getFileTable();

		for(FileTable table=table1; ; table=table2) {
			if(hasFolderChanged(table.getCurrentFolder())) {
				try {
					// Refresh folder in the same thread
					table.getFolderPanel().refreshCurrentFolder();
				}
				catch(IOException e) {
					if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Folder couldn't be refreshed: "+e);
				}
			}
		
			if(table==table2)
				break;
		}

		// Resume auto-refresh if auto-refresh has been paused
		table1.setAutoRefreshActive(true);
		table2.setAutoRefreshActive(true);
	}
	

	////////////////////////////////////////////
	// Control methods used by ProgressDialog //
	////////////////////////////////////////////
	
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
     * Returns the number of bytes that have been processed by this job so far.
     */
    public long getTotalBytesProcessed() {
		return nbBytesProcessed;
	}


    /**
	* Returns the index of the file currently being processed (has to be < {@link #getNbFiles() getNbFiles}).
     */
    public int getCurrentFileIndex() {
        return currentFileIndex==-1?0:currentFileIndex;
    }

    /**
     * Returns the number of file that this job contains.
     */
    public int getNbFiles() {
        return nbFiles;
	}

	
	//////////////////////
	// Abstract methods //
	//////////////////////

	
	/**
	 * This method should return <code>true</code> if the given folder has or may have been modified. This method is
	 * used to determine if current table folders should be refreshed after this job.
	 */
	protected abstract boolean hasFolderChanged(AbstractFile folder);
	
	
	/**
	 * Automatically called by {@link #run() run()} for each file that needs to be processed.
	 *
	 * @param file the file or folder to process
	 * @param recurseParams array of parameters which can be used when calling this method recursively, contains <code>null</code> when called by {@link #run() run()}
	 *
	 * @return <code>true</code> if the operation was sucessful
	 */
    protected abstract boolean processFile(AbstractFile file, Object recurseParams);

	
	/**
	 * Returns a String describing the file what is currently being done.
	 */
	public abstract String getStatusString();
	
}