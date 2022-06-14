/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.job.impl;

import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.CachedFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.job.FileJobAction;
import com.mucommander.job.FileJobListener;
import com.mucommander.job.FileJobState;
import com.mucommander.job.JobProgress;
import com.mucommander.job.JobsManager;
import com.mucommander.job.ui.DialogResult;
import com.mucommander.job.ui.UserInputHelper;
import com.mucommander.os.notifier.NotificationType;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogAction;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.notifier.NotifierProvider;


/**
 * FileJob is a container for a 'file task' : basically an operation that involves files and bytes.
 * The class extending FileJob is required to give some information about the status of the job that
 * will be used to display visual indications of the job's progress.
 * <p>
 * The actual processing is performed in a separate thread. A FileJob needs to be started explicitely using
 * {@link #start()}. The lifecycle of a FileJob is as follows:<br>
 * <br>
 * <pre>
 * {@link #NOT_STARTED} -> {@link #RUNNING} -> {@link #FINISHED}
 *                         ^                |
 *                         |                -> {@link #INTERRUPTED}
 *                         |                |                      
 *                         |                -> {@link #PAUSED} -|
 *                         |                                    |
 *                         -------------------------------------|
 * </pre>
 * </p>
 *
 * @author Maxence Bernard
 */
public abstract class FileJob implements com.mucommander.job.FileJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileJob.class);

    /** Thread in which the file job is performed */
    private Thread jobThread;

    /** Lock used when job is being paused */
    private final Object pauseLock = new Object();

    /** Timestamp in milliseconds when job started */
    private long startDate;

    /** Timestamp in milliseconds when job has finished */
    private long endDate;

    /** Number of milliseconds during which this job has been paused (been waiting for some user response).
     * Used to compute stats like average speed. */
    private long pausedTime;

    /** Contains the timestamp when this job has been put in pause (if in pause) */
    private long pauseStartDate;

    /** Associated dialog showing job progression */
    private ProgressDialog progressDialog;

    /** Main frame on which the job is to be performed */ 
    private MainFrame mainFrame;
    
    /** Base source folder */
    private AbstractFile baseSourceFolder;
    
    /** Files which are going to be processed */
    protected FileSet files;

    /** Number of files that this job contains */
    private int nbFiles;

    /** Index of file currently being processed, see {@link #getCurrentFileIndex()} */
    private int currentFileIndex = -1;

    /** File currently being processed */
    private AbstractFile currentFile;

    /** Name of the file currently being processed */
    private String currentFilename = "";

    /** If set to true, processed files will be unmarked from current table */
    private boolean autoUnmark = true;
    
    /** File to be selected after job has finished (can be null if not set) */
    private AbstractFile fileToSelect;

    /** Current state of this job */
    private FileJobState jobState = FileJobState.NOT_STARTED;

    /** List of registered FileJobListener stored as weak references */
    private WeakHashMap<FileJobListener, ?> listeners = new WeakHashMap<FileJobListener, Object>();
    
    /** Information about this job progress */
    private JobProgress jobProgress;

    /** True if the user asked to automatically skip errors */
    private boolean autoSkipErrors;

    /** Whether or not this job is executed in the background */
    private boolean runInBackground;

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
        this.baseSourceFolder = files.getBaseFolder();

        // Create CachedFile instances around the source files in order to cache the return value of frequently accessed
        // methods. This eliminates some I/O, at the (small) cost of a bit more CPU and memory. Recursion is enabled
        // so that children and parents of the files are also cached.
        // Note: When cached methods are called, they no longer reflect changes in the underlying files. In particular,
        // changes of size or date could potentially not be reflected when files are being processed but this should
        // not really present a risk. 
        AbstractFile tempFile;
        for(int i=0; i<nbFiles; i++) {
            tempFile = files.elementAt(i);
            files.setElementAt((tempFile instanceof CachedFile)?tempFile:new CachedFile(tempFile, true), i);
        }

        if (this.baseSourceFolder!=null)
            this.baseSourceFolder = (getBaseSourceFolder() instanceof CachedFile)?getBaseSourceFolder():new CachedFile(getBaseSourceFolder(), true);

        this.jobProgress = new JobProgress(this);    
    }
    
    
    /**
     * Specifies whether or not files that have been processed should be unmarked from current table (enabled by default).
     *
     * @param autoUnmark <code>true</code> to automatically unmark files after they have been processed.
     */
    public void setAutoUnmark(boolean autoUnmark) {
        this.autoUnmark = autoUnmark;
    }

    /**
     * Sets whether or not this file job should automatically skip errors when encountered (disabled by default).
     *
     * @param autoSkipErrors <code>true</code> to automatically skip errors, <code>false</code> to show an error dialog.
     */
    public void setAutoSkipErrors(boolean autoSkipErrors) {
        this.autoSkipErrors = autoSkipErrors;
    }

    
    /**
     * Sets the given file to be selected in the active table after this job has finished.
     * The file will only be selected if it exists in the active table's folder and if this job hasn't
     * been cancelled. The selection will occur after the tables have been refreshed (if they are refreshed).
     *
     * @param file the file to be selected in the active table after this job has finished
     */
    protected void selectFileWhenFinished(AbstractFile file) {
        this.fileToSelect = file;
    }
    
    @Override
    public void start() {
        // Return if job has already been started
        if (getState() != FileJobState.NOT_STARTED)
            return;

        setState(FileJobState.RUNNING);
        startDate = System.currentTimeMillis();

        jobThread = new Thread(this, getClass().getName());
        jobThread.start();
    }

    @Override
    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }


    /**
     * Returns the main frame.
     * @return the mainFrame
     */
    protected MainFrame getMainFrame() {
        return mainFrame;
    }

    @Override
    public FileJobState getState() {
        return jobState;
    }

    /**
     * Sets a new state for this FileJob and notifies registered FileJobListener instances of the change.
     *
     * @param jobState the new state
     */
    protected void setState(FileJobState jobState) {
        FileJobState oldState = this.jobState;
        this.jobState = jobState;

        for(FileJobListener listener : listeners.keySet())
            listener.jobStateChanged(this, oldState, jobState);
    }

    @Override
    public boolean isRunInBackground() {
        return runInBackground;
    }

    @Override
    public void setRunInBackground(boolean runInBackground) {
        this.runInBackground = runInBackground;

        if (jobState != FileJobState.NOT_STARTED) {
            for (FileJobListener listener : listeners.keySet())
                listener.jobExecutionModeChanged(this,  runInBackground);
        }
    }

    @Override
    public void addFileJobListener(FileJobListener listener) {
        listeners.put(listener, null);
    }

    @Override
    public void removeFileJobListener(FileJobListener listener) {
        listeners.remove(listener);
    }


    /**
     * Returns the timestamp in milliseconds when this job started.
     *
     * @return the timestamp in milliseconds when this job started
     */
    public long getStartDate() {
        return startDate;
    }

    @Override
    public long getEndDate() {
        return endDate;
    }

    @Override
    public long getPauseStartDate() {
        return pauseStartDate;
    }
    
    /**
     * Sets the timestamp in milliseconds when this job is paused.
     */
    private void setPauseStartDate() {
        this.pauseStartDate = System.currentTimeMillis();
    }

    
    /**
     * Returns the number of milliseconds during which this job has been paused (been waiting for some user response).
     * If this job has been paused several times, the total is returned. If this job has not been paused yet,
     * <code>0</code> is returned.
     *
     * @return the number of milliseconds during which this job has been paused
     */
    public long getPausedTime() {
        return pausedTime;
    }

    /**
     * Adds a time of last pause to this job pause time counter. 
     */
    private void calcPausedTime() {
        this.pausedTime += System.currentTimeMillis() - this.getPauseStartDate();
    }

    @Override
    public long getEffectiveJobTime() {
        // If job hasn't start yet, return 0
        if(getStartDate()==0)
            return 0;
        
        return (getEndDate()==0?System.currentTimeMillis():getEndDate())-getStartDate()-getPausedTime();
    }

    @Override
    public void interrupt() {
        FileJobState state = getState();
        if (state == FileJobState.INTERRUPTED || state == FileJobState.FINISHED)
            return;

        if (state == FileJobState.PAUSED)
            setPaused(false);

        // Set state before calling stop() so that state is INTERRUPTED when jobStopped() is called
        // (some FileJob rely on that)
        setState(FileJobState.INTERRUPTED);

        stop();
    }


    /**
     * Release reference to thread and store job's end date.
     */
    private void stop() {
        // Return if job has already been stopped
        if(jobThread==null)
            return;

//        // Start by calling interrupt to have the thread return from any blocking I/O occurring in an interruptible
//        // channel or selector.
//        jobThread.interrupt();

        jobThread = null;
        endDate = System.currentTimeMillis();

        // Notify that the job has been stopped
        jobStopped();
    }

    @Override
    public void setPaused(boolean paused) {
        // Lock the pause lock while updating paused status
        synchronized(pauseLock) {
            // Resume job if it was paused
            if(!paused && getState() == FileJobState.PAUSED) {
                // Calculate pause time
                calcPausedTime();                
                // Call the jobResumed method to notify of the new job's state
                jobResumed();

                // Wake up the job's thread that is potentially waiting for pause to be over 
                pauseLock.notify();

                // Switch to RUNNING state and notify listeners
                setState(FileJobState.RUNNING);
            }
            // Pause job if it not paused already
            else if(paused && getState() != FileJobState.PAUSED && getState() != FileJobState.INTERRUPTED && getState() != FileJobState.FINISHED) {
                // Memorize pause time in order to calculate pause time when the job is resumed
                setPauseStartDate();
                // Call the jobPaused method to notify of the new job's state
                jobPaused();

                // Switch to PAUSED state and notify listeners
                setState(FileJobState.PAUSED);
            }
        }
    }


    /**
     * Changes the current file. This method should be called by subclasses whenever the job
     * starts processing a new file other than a top-level file, i.e. one that was passed
     * as an argument to {@link #processFile(AbstractFile, Object) processFile()}.
     * ({#nextFile(AbstractFile) nextFile()} is automatically called for files in base folder).
     */
    protected void nextFile(AbstractFile file) {
        this.setCurrentFile(file);

//        // Notify ProgressDialog (if any) that a new file is being processed
//        if(progressDialog!=null)
//            progressDialog.notifyCurrentFileChanged();
        
        // Lock the pause lock
        synchronized(pauseLock) {
            // Loop while job is paused, there shouldn't normally be more than one loop
            while (getState() == FileJobState.PAUSED) {
                try {
                    // Wait for a call to notify()
                    pauseLock.wait();
                } catch(InterruptedException e) {
                    // No more problem, loop one more time
                }
            }
        }
    }


    /**
     * Returns the name of the file currently being processed surrounded by simple quotes (e.g. 'test.zip'), or an empty
     * string if no file is currently being processed.
     *
     * @return the name of the file currently being processed surrounded by simple quotes, or an empty string if no file
     * is currently being processed
     */
    protected String getCurrentFilename() {
        return currentFilename;
    }


    /**
     * This method is called when this job starts, before the first call to {@link #processFile(AbstractFile,Object)} is made.
     * This method implementation does nothing but it can be overriden by subclasses to perform some first-time initializations.
     */
    protected void jobStarted() {
        LOGGER.debug("called");
    }
    

    /**
     * This method is called when this job has completed normal execution : all files have been processed without any interruption
     * (without any call to {@link #interrupt()}).
     *
     * <p>The call happens after the last call to {@link #processFile(AbstractFile,Object)} is made.
     * This method implementation does nothing but it can be overriden by subclasses to properly complete the job.</p>
     
     * <p>Note that this method will NOT be called if a call to {@link #interrupt()} was made before all files were processed.</p>
     */
    protected void jobCompleted() {
        LOGGER.debug("called");

        // Send a system notification if a notifier is available and enabled
        if(NotifierProvider.isAvailable() && NotifierProvider.getNotifier().isEnabled())
            NotifierProvider.displayBackgroundNotification(NotificationType.JOB_COMPLETED,
                    getProgressDialog()==null?"":getProgressDialog().getTitle(),
                    Translator.get("progress_dialog.job_finished"));
    }


    /**
     * This method is called when this job has been paused, either by the user, or by the job when asking for user input.
     * 
     * <p>This method implementation does nothing but it can be overridden by subclasses to do whatever is needed
     * when the job has been paused.
     */
    protected void jobPaused() {
        LOGGER.debug("called");
    }


    /**
     * This method is called when this job has been resumed after being paused.
     *
     * <p>This method implementation does nothing but it can be overridden by subclasses to do whatever is needed
     * when the job has returned from pause.
     */
    protected void jobResumed() {
        LOGGER.debug("called");
    }


    /**
     * This method is called when this job has been stopped. The call happens after all calls to {@link #processFile(AbstractFile,Object)} and
     * {@link #jobCompleted()}.
     * This method implementation does nothing but it can be overridden by subclasses to properly terminate the job.
     * This is where you want to close any opened connections.
     *
     * <p>Note that unlike {@link #jobCompleted()} this method is always called, whether the job has been completed (all
     * files were processed) or has been interrupted in the middle.</p>
     */
    protected void jobStopped() {
        LOGGER.debug("called");
    }
    
    
    /**
     * Displays an error dialog with the specified title and message,
     * offers to skip the file, retry or cancel and waits for user choice.
     * The job is stopped if 'cancel' or 'close' was chosen, and the result 
     * is returned.
     */
    protected DialogAction showErrorDialog(String title, String message) {
        List<DialogAction> actions = Arrays.asList(FileJobAction.SKIP, FileJobAction.SKIP_ALL,
                        FileJobAction.RETRY, FileJobAction.CANCEL);
        return showErrorDialog(title, message, actions);
    }


    
    /**
     * Displays an error dialog with the specified title and message and returns the selection action's value.
     */
    protected DialogAction showErrorDialog(String title, String message, List<DialogAction> actionChoices) {
        // Return SKIP_ACTION if 'skip all' has previously been selected and 'skip' is in the list of actions.
        if(autoSkipErrors) {
            for (DialogAction action : actionChoices)
                if (action == FileJobAction.SKIP)
                    return FileJobAction.SKIP;
        }

        // Send a system notification if a notifier is available and enabled
        if(NotifierProvider.isAvailable() && NotifierProvider.getNotifier().isEnabled())
            NotifierProvider.displayBackgroundNotification(NotificationType.JOB_ERROR, title, message);

        QuestionDialog dialog;
        if(getProgressDialog()==null)
            dialog = new QuestionDialog(getMainFrame(), 
                                        title,
                                        message,
                                        getMainFrame(),
                                        actionChoices,
                                        0);
        else
            dialog = new QuestionDialog(getProgressDialog(), 
                                        title,
                                        message,
                                        getMainFrame(),
                                        actionChoices,
                                        0);

        // Cancel or close dialog stops this job
        DialogAction userChoice = waitForUserResponse(dialog);
        if (userChoice == QuestionDialog.DIALOG_DISPOSED_ACTION || userChoice == FileJobAction.CANCEL)
            interrupt();
        // Keep 'skip all' choice for further error and return SKIP
        else if (userChoice == FileJobAction.SKIP_ALL) {
            autoSkipErrors = true;
            return FileJobAction.SKIP;
        }

        return userChoice;
    }
    
    
    /**
     * Waits for the user's answer to the given question dialog, putting this
     * job in pause mode while waiting for the user.
     */
    protected DialogAction waitForUserResponse(DialogResult dialog) {
        Object userInput = waitForUserResponseObject(dialog);
        return (DialogAction) userInput;
    }
    
    protected Object waitForUserResponseObject(DialogResult dialog) {
        // Put this job in pause mode while waiting for user response
        setPaused(true);
        
        UserInputHelper jobUserInput = new UserInputHelper(this, dialog);
        Object userInput = jobUserInput.getUserInput();
        
        // Back to work
        setPaused(false);
        return userInput;
    }
    
    
    
    /**
     * Check and if needed, refreshes both file tables's current folders, based on the job's refresh policy.
     */
    protected void refreshTables() {
        FolderPanel inactivePanel = getMainFrame().getInactivePanel();
        AbstractFile inactiveFolder = inactivePanel.getCurrentFolder();
        if (hasFolderChanged(inactiveFolder))
            inactivePanel.tryRefreshCurrentFolder();

        FolderPanel activePanel = getMainFrame().getActivePanel();
        AbstractFile activeFolder = activePanel.getCurrentFolder();
        if (hasFolderChanged(activeFolder)) {
            // Select file specified by selectFileWhenFinished (if any) only if the file exists in the active table's folder
            if (fileToSelect!=null && activeFolder.equalsCanonical(fileToSelect.getParent()) && fileToSelect.exists())
                activePanel.tryRefreshCurrentFolder(fileToSelect);
            else
                activePanel.tryRefreshCurrentFolder();
        }

        // Repaint the status bar as marked files have changed
        mainFrame.getStatusBar().updateSelectedFilesInfo();
    }
    
    @Override
    public float getTotalPercentDone() {
        return getCurrentFileIndex()/(float)getNbFiles();
    }


    /**
     * Returns the index of the file currently being processed, {@link #getNbFiles()} if all files have been processed.
     *
     * @return the index of the file currently being processed, {@link #getNbFiles()} if all files have been processed
     */
    public int getCurrentFileIndex() {
        return currentFileIndex==-1?0:currentFileIndex;
    }
    
    /**
     * Returns the file currently being processed.
     * @return the file currently being processed.
     */
    public AbstractFile getCurrentFile() {
        return currentFile;
    }
    
    /**
     * Sets the file currently being processed.
     * @param file the file currently being processed.
     */
    private void setCurrentFile(AbstractFile file) {
        this.currentFile = file;
        // Update current file information returned by getCurrentFilename()
        this.currentFilename = "'" + file.getName() + "'";
    }

    /**
     * Returns the number of file that this job contains.
     *
     * @return the number of file that this job contains
     */
    public int getNbFiles() {
        return nbFiles;
    }
    
    /**
     * Sets the number of files that this job contains.
     * 
     * @param nbFiles the number of files that this job contains.
     */
    protected void setNbFiles(int nbFiles) {
        this.nbFiles = nbFiles;
    }

    @Override
    public String getStatusString() {
        return Translator.get("progress_dialog.processing_file", getCurrentFilename());
    }

    @Override
    public JobProgress getJobProgress() {
        return jobProgress;     
    }

    /**
     * Returns the base source folder.
     * @return the baseSourceFolder
     */
    protected AbstractFile getBaseSourceFolder() {
        return baseSourceFolder;
    }
    
    
    /////////////////////////////
    // Runnable implementation //
    /////////////////////////////

    /**
     * This method is public as a side-effect of this class implementing <code>Runnable</code>.
     */
    public final void run() {
        FileTable activeTable = getMainFrame().getActiveTable();

        // Notify that this job has started
        jobStarted();

        // Loop on all source files, checking that job has not been interrupted
        for (currentFileIndex=0; currentFileIndex<nbFiles; currentFileIndex++) {
            AbstractFile currentFile = files.elementAt(currentFileIndex);

            // Change current file and advance file index
            nextFile(currentFile);

            // Process current file
            boolean success = processFile(currentFile, null);

            // Stop if job was interrupted
            if (getState() == FileJobState.INTERRUPTED)
                break;

            // Unmark file in active table if 'auto unmark' is enabled
            // and file was processed successfully
            if (autoUnmark && success) {
                // Do not repaint rows individually as it would be too expensive
                activeTable.setFileMarked(currentFile, false, false);
            }
        }

        // If last file was reached without any user interruption, all files have been processed with or
        // without errors, switch to FINISHED state and notify listeners
        if (currentFileIndex == nbFiles && getState() != FileJobState.INTERRUPTED) {
            stop();
            jobCompleted();
            setState(FileJobState.FINISHED);
        }

        // Refresh tables's current folders, based on the job's refresh policy.
        refreshTables();

        JobsManager.getInstance().jobEnded(this);
    }


    //////////////////////
    // Abstract methods //
    //////////////////////
    
    /**
     * Automatically called by {@link #run()} for each file that needs to be processed.
     *
     * @param file the file or folder to process
     * @param recurseParams array of parameters which can be used when calling this method recursively, contains <code>null</code> when called by {@link #run()}
     *
     * @return <code>true</code> if the operation was successful
     */
    protected abstract boolean processFile(AbstractFile file, Object recurseParams);
}
