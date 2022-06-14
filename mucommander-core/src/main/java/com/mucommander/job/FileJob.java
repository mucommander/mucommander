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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.dialog.file.ProgressDialog;


/**
 * An interface that exposes the public API of {@link com.mucommander.job.impl.FileJob}
 *
 * @author Arik Hadas
 */
public interface FileJob extends Runnable {
    /**
     * Indicates whether or not this job is executed in a non-blocking mode.
     *
     * @return true if the job is executed in the background, false otherwise.
     */
    boolean isRunInBackground();

    /**
     * Registers a FileJobListener to receive notifications whenever state of this FileJob changes.
     *
     * <p>Listeners are stored as weak references so {@link #removeFileJobListener(FileJobListener)}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     *
     * @param listener the FileJobListener to register
     */
    void addFileJobListener(FileJobListener listener);

    /**
     * Removes the given FileJobListener from the list of listeners that receive notifications when the state of
     * this FileJob has changed.
     *
     * @param listener the FileJobListener to remove
     */
    void removeFileJobListener(FileJobListener listener);

    /**
     * Returns the current state of this FileJob. See constant fields for possible return values.
     *
     * @return the current state of this FileJob. See constant fields for possible return values.
     */
    FileJobState getState();

    /**
     * Returns <code>true</code> if the given folder has or may have been modified by this job.
     * This method is called after this job has finished processing files, to determine if the current MainFrame's
     * file tables need to be refreshed to reveal the modified contents.
     *
     * @param folder the folder to test 
     * @return true if the given folder has or may have been modified by this job
     */
    boolean hasFolderChanged(AbstractFile folder);

    /**
     * Returns information about the job progress.
     * @return the job progress
     */
    JobProgress getJobProgress();

    /**
     * Returns the timestamp in milliseconds when this job was last paused, <code>0</code> if this job has not been
     * paused yet.
     *
     * @return the timestamp in milliseconds when this job was last paused
     */
    long getPauseStartDate();

    /**
     * Returns a String describing what the job is currently doing. This default implementation returns
     * <i>Processing CURRENT_FILE</i> where CURRENT_FILE is the name of the file currently being processed.
     * This method should be overridden to provide a more accurate description.
     *
     * @return a String describing what the job is currently doing
     */
    String getStatusString();

    /**
     * Returns the timestamp in milliseconds when this job ended, <code>0</code> if this job hasn't finished yet.
     *
     * @return the timestamp in milliseconds when this job ended
     */
    long getEndDate();

    /**
     * Returns this job's percentage of completion, as a float comprised between 0 and 1.
     *
     * @return this job's percentage of completion, as a float comprised between 0 and 1
     */
    float getTotalPercentDone();

    /**
     * Returns the number of milliseconds this job effectively spent processing files, excluding any pause time.
     *
     * @return the number of milliseconds this job effectively spent processing files, excluding any pause time
     */
    long getEffectiveJobTime();

    /**
     * Starts file job in a separate thread.
     */
    void start();

    /**
     * Interrupts this job, changes the job state to {@link #INTERRUPTED} and notifies listeners.
     */
    void interrupt();

    /**
     * Sets or unsets this job in paused mode.
     */
    void setPaused(boolean paused);

    /**
     * Specifies whether or not this job needs to be executed in a non-blocking mode.
     *
     * @param runInBackground true if the job needs to be executed in the background, false otherwise.
     */
    void setRunInBackground(boolean runInBackground);

    /**
     * Returns the dialog showing progress of this job.
     * @return the progressDialog
     */
    ProgressDialog getProgressDialog();
}