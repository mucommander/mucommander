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

import java.util.EventListener;

/**
 * Interface to be implemented by classes that wish to be notified of progress changes on a particular
 * {@link FileJob}. Those classes need to be registered to receive those events, this can be done by calling
 * {@link JobProgressMonitor#addJobProgressListener(JobProgressListener)}.
 *
 * @author Mariusz Jakubowski
 */
public interface JobProgressListener extends EventListener {
	
	/**
     * Called when a new job has been initiated.
	 * 
	 * @param source a job added
	 * @param idx index of a job in a job queue
	 */
	public void jobAdded(FileJob source, int idx);
	
	/**
     * Called when a new job has finished and has been removed from the queue.
	 * 
	 * @param source a job removed
	 * @param idx index of a job in a job queue
	 */
	public void jobRemoved(FileJob source, int idx);

	/**
     * Called when the progress of the specified FileJob has been updated.
     *
     * @param source the FileJob which progress has been updated
	 * @param idx index of a job in a job queue
     * @param fullUpdate if false indicates that only file label has been updated
     * @see JobProgress#calcJobProgress
     */
	public void jobProgress(FileJob source, int idx, boolean fullUpdate);
	
}
