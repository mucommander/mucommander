/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.job.progress;

import com.mucommander.job.FileJob;
import com.mucommander.job.FileJobListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that monitors jobs progress.
 * @author Mariusz Jakubowski
 *
 */
public class JobProgressMonitor implements FileJobListener {
	
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
	private List<FileJob> jobs = new ArrayList<FileJob>();

	/** An instance of this class */
	private static final JobProgressMonitor instance = new JobProgressMonitor();
		
	
	/**
	 * Creates a new JobProgressMonitor instance.
	 */
	private JobProgressMonitor() {
		JobProgressTimer timerListener = new JobProgressTimer(); 
    	progressTimer = new Timer(CURRENT_FILE_LABEL_REFRESH_RATE, timerListener);
	}
	
	/**
	 * Returns the instance of JobProgressMonitor.
	 * @return the instance of JobProgressMonitor.
	 */
	public static JobProgressMonitor getInstance() {
		return instance;
	}
    
	
    /**
     * Adds a listener to the list that's notified each time a job 
     * progress is updated.
     *
     * @param	l		the JobProgressListener
     */
    public void addJobProgressListener(JobProgressListener l) {
    	listenerList.add(JobProgressListener.class, l);
    }

    /**
     * Removes a listener from the list that's notified each time job
     * progress is updated.
     *
     * @param	l		the JobProgressListener
     */
    public void removeJobProgressListener(JobProgressListener l) {
    	listenerList.remove(JobProgressListener.class, l);
    }

    /**
     * Forwards the progress notification event to all
     * <code>JobProgressListeners</code> that registered
     * themselves as listeners.
     * @param source a job for which the progress has been updated
     * @param fullUpdate if false only file label has been updated 
     * 
     * @see #addJobProgressListener
     * @see JobProgressListener#jobProgress
     */
    private void fireJobProgress(FileJob source, boolean fullUpdate) {
		int idx = jobs.indexOf(source);
    	Object[] listeners = listenerList.getListenerList();
    	for (int i = listeners.length-2; i>=0; i-=2) {
    		((JobProgressListener)listeners[i+1]).jobProgress(source, idx, fullUpdate);
    	}
    }
    
    /**
     * Forwards the job added notification event to all
     * <code>JobProgressListeners</code> that registered
     * themselves as listeners.
     * @param source an added job 
     * @param idx index of a job in a list 
     * 
     * @see #addJobProgressListener
     * @see JobProgressListener#jobAdded(FileJob, int)
     */
    private void fireJobAdded(FileJob source, int idx) {
    	Object[] listeners = listenerList.getListenerList();
    	for (int i = listeners.length-2; i>=0; i-=2) {
    		((JobProgressListener)listeners[i+1]).jobAdded(source, idx);
    	}    	
    }
    
    /**
     * Forwards the job removed notification event to all
     * <code>JobProgressListeners</code> that registered
     * themselves as listeners.
     * @param source a removed job
     * @param idx index of a job in a list 
     * 
     * @see #addJobProgressListener
     * @see JobProgressListener#jobRemoved(FileJob, int)
     */
    private void fireJobRemoved(FileJob source, int idx) {
    	Object[] listeners = listenerList.getListenerList();
    	for (int i = listeners.length-2; i>=0; i-=2) {
    		((JobProgressListener)listeners[i+1]).jobRemoved(source, idx);
    	}    	
    }

    /**
     * Adds a new job to the list of monitored jobs. 
     * This method is executed in Swing Thread (EDT).
     * After adding a new job a {@link JobProgressListener#jobAdded(FileJob, int)} 
     * event is fired.
     * @param job a job to be added
     */
    public void addJob(final FileJob job) {
    	// ensure that this method is called in EDT
    	if (!SwingUtilities.isEventDispatchThread()) {
    		SwingUtilities.invokeLater(new Runnable() {
    			public void run() {
    				addJob(job);
    			}
    		});
    	}

    	jobs.add(job);
    	int idx = jobs.size() - 1;
		fireJobAdded(job, idx);    			
    	if (!progressTimer.isRunning()) {
    		progressTimer.start();
    	}
    	job.addFileJobListener(this);
    }
    
    /**
     * Removes a job from a list of monitored jobs.
     * This method is executed in Swing Thread (EDT).
     * After adding a new job a {@link JobProgressListener#jobRemoved(FileJob, int)} 
     * event is fired.
     * @param job a job to be removed
     */
    public void removeJob(final FileJob job) {
    	// ensure that this method is called in EDT
    	if (!SwingUtilities.isEventDispatchThread()) {
    		SwingUtilities.invokeLater(new Runnable() {
    			public void run() {
    				removeJob(job);
    			}
    		});
    	}

    	int idx = jobs.indexOf(job);
		if (idx != -1) {
			jobs.remove(idx);
		}
		if (jobs.isEmpty()) {
			progressTimer.stop();
		}
		fireJobRemoved(job, idx);
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
	public void jobStateChanged(final FileJob source, int oldState, int newState) {
		if (newState==FileJob.FINISHED || newState==FileJob.INTERRUPTED) {
			ActionListener jobToRemove = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeJob(source);					
				}
			}; 
			Timer timer = new Timer(FINISHED_JOB_REMOVE_TIME, jobToRemove);
			timer.setRepeats(false);
			timer.start();
		}		
	}
	
	
	
	/**
     * 
     * This class implements a listener for a job progress timer.
     *
     */
	private class JobProgressTimer implements ActionListener {
		
		/** a loop index indicating if this refresh is partial (label only) or full */
		private int loopCount = 0;

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
			for(FileJob job : jobs) {
				boolean updateFullUI;
				JobProgress jobProgress = job.getJobProgress();
				updateFullUI = jobProgress.calcJobProgress(fullUpdate);
				fireJobProgress(job, updateFullUI);
			}
			
		}

	}


}
