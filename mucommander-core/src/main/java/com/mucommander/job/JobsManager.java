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

package com.mucommander.job;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import com.mucommander.commons.file.AbstractFile;

/**
 * A class that monitors jobs progress.
 * @author Arik Hadas, Mariusz Jakubowski
 *
 */
public class JobsManager implements FileJobListener {
	
    /** Controls how often should current file label be refreshed (in ms) */
	private final static int CURRENT_FILE_LABEL_REFRESH_RATE = 100;
	
	/** Controls how often should progress information be refreshed */
    private final static int MAIN_REFRESH_RATE = 10;
    
    /** Time after which remove finished job from a monitor */
    private final static int FINISHED_JOB_REMOVE_TIME = 1500;

    /** Timer used to monitor jobs progress */
    private Timer progressTimer;
	
    /** List of listeners */
	private EventListenerList listenerList = new EventListenerList();
	
	/** A list of monitored jobs. */
	private List<FileJob> jobs;

	/** An instance of this class */
	private static final JobsManager instance = new JobsManager();
		
	
	/**
	 * Creates a new JobsManager instance.
	 */
	private JobsManager() {
		JobProgressTimer timerListener = new JobProgressTimer(); 
    	progressTimer = new Timer(CURRENT_FILE_LABEL_REFRESH_RATE, timerListener);
        jobs  = new CopyOnWriteArrayList<>();
	}
	
	/**
	 * Returns the instance of JobsManager.
	 * @return the instance of JobsManager.
	 */
	public static JobsManager getInstance() {
		return instance;
	}
    
	
    /**
     * Adds a listener to the list that's notified each time a job 
     * progress is updated.
     *
     * @param	l		the JobListener
     */
    public void addJobListener(JobListener l) {
    	listenerList.add(JobListener.class, l);
    }

    /**
     * Removes a listener from the list that's notified each time job
     * progress is updated.
     *
     * @param	l		the JobListener
     */
    public void removeJobListener(JobListener l) {
    	listenerList.remove(JobListener.class, l);
    }

    /**
     * Forwards the progress notification event to all
     * <code>JobListeners</code> that registered
     * themselves as listeners.
     * @param source a job for which the progress has been updated
     * @param fullUpdate if false only file label has been updated 
     * 
     * @see #addJobListener
     * @see JobListener#jobProgress
     */
    private void fireJobProgress(FileJob source, boolean fullUpdate) {
    	Object[] listeners = listenerList.getListenerList();
    	for (int i = listeners.length-2; i>=0; i-=2) {
    		((JobListener)listeners[i+1]).jobProgress(source, fullUpdate);
    	}
    }
    
    private void fireJobAdded(FileJob source) {
    	Object[] listeners = listenerList.getListenerList();
    	for (int i = listeners.length-2; i>=0; i-=2) {
    		((JobListener)listeners[i+1]).jobAdded(source);
    	}    	
    }
    
    private void fireJobRemoved(FileJob source) {
    	Object[] listeners = listenerList.getListenerList();
    	for (int i = listeners.length-2; i>=0; i-=2) {
    		((JobListener)listeners[i+1]).jobRemoved(source);
    	}    	
    }

    /**
     * Adds a new job to the list of monitored jobs. 
     * This method is executed in Swing Thread (EDT).
     * After adding a new job a {@link JobListener#jobAdded(FileJob)}
     * event is fired.
     * @param job a job to be added
     */
    public void addJob(final FileJob job) {
    	// ensure that this method is called in EDT
    	if (!SwingUtilities.isEventDispatchThread()) {
    		SwingUtilities.invokeLater(() -> addJob(job));
    		return;
    	}

    	jobs.add(job);
    	if (job.isRunInBackground()) {
    	    fireJobAdded(job);
    	}
    	if (!progressTimer.isRunning()) {
    		progressTimer.start();
    	}
    	job.addFileJobListener(this);
    }
    
    /**
     * Removes a job from a list of monitored jobs.
     * This method is executed in Swing Thread (EDT).
     * After removing a job a {@link JobListener#jobRemoved(FileJob)}
     * event is fired.
     * @param job a job to be removed
     */
    private void removeJob(final FileJob job) {
    	// ensure that this method is called in EDT
    	if (!SwingUtilities.isEventDispatchThread()) {
    		SwingUtilities.invokeLater(() -> removeJob(job));
    		return;
    	}

    	jobs.remove(job);
        fireJobRemoved(job);
		if (jobs.isEmpty()) {
			progressTimer.stop();
		}
		job.removeFileJobListener(this);
    }
    
	/**
	 * Returns number of monitored jobs.
	 * @return number of monitored jobs.
	 */
	public int getJobCount() {
		return jobs.size();
	}

	/**
	 * Checks if the given folder may change by any of the existing running job.
	 * @param folder a folder to check
	 * @return true if the folder may change by existing job, false otherwise
	 */
	public boolean mayFolderChangeByExistingJob(AbstractFile folder) {
	    return jobs.stream()
	            .filter(job -> job.getState() != FileJobState.PAUSED)
	            .anyMatch(job -> job.hasFolderChanged(folder));
	}

	/**
	 * Removes the given job from the jobs collection. Should be called when the job is ended.
	 * @param job a job to remove
	 */
	public void jobEnded(FileJob job) {
	    Timer timer = new Timer(FINISHED_JOB_REMOVE_TIME, event -> removeJob(job));
	    timer.setRepeats(false);
	    timer.start();
	}

	/**
	 * Returns jobs that are running in the background.
	 * @return jobs that are running in the background.
	 */
	public List<FileJob> getBackgroundJobs() {
	    return jobs.stream()
	            .filter(FileJob::isRunInBackground)
	            .collect(Collectors.toList());
	}
	
	/**
	 * Returns a progress of a job with specified index.
	 * @param rowIndex an index of a job
	 * @return a progress information or null if job doesn't exists
	 */
	public JobProgress getJobProgres(int rowIndex) {
		if (rowIndex < jobs.size()) {
			FileJob job = jobs.get(rowIndex);
			return job.getJobProgress();
		}
		return null;
	}

	/**
	 * A {@link FileJobListener} implementation.
	 * Removes a finished job after a small delay.
	 */
	@Override
	public void jobStateChanged(final FileJob source, FileJobState oldState, FileJobState newState) {
	}

	@Override
	public void jobExecutionModeChanged(FileJob source, boolean background) {
	    if (background)
	        fireJobAdded(source);
	    else
	        fireJobRemoved(source);
	}


	/**
     * 
     * This class implements a listener for a job progress timer.
     *
     */
	private class JobProgressTimer implements ActionListener {
		
		/** a loop index indicating if this refresh is partial (label only) or full */
		private int loopCount;

		public void actionPerformed(ActionEvent e) {
			loopCount++;

			boolean fullUpdate;			
			if (loopCount >= MAIN_REFRESH_RATE) {
				fullUpdate = true;
				loopCount = 0;
			} else {
				fullUpdate = false;
			}
			
			// for each job calculate new progress and notify listeners
			for (FileJob job : jobs) {
				JobProgress jobProgress = job.getJobProgress();
				boolean updateFullUI = jobProgress.calcJobProgress(fullUpdate);
				fireJobProgress(job, updateFullUI);
			}
			
		}

	}


}
