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
import com.mucommander.job.TransferFileJob;
import com.mucommander.text.DurationFormat;
import com.mucommander.text.Translator;

/**
 * Contains information about job progress.
 *
 */
public class JobProgress {
	private FileJob job;
	private TransferFileJob transferFileJob;

	private long effectiveJobTime;
	private long lastTime;
	private int totalPercentInt;
	private String totalProgressText;
	private int filePercentInt;
	private String fileProgressText;
	private long currentBps;
	private long bytesTotal;
	private long totalBps;
	private long lastBytesTotal;
	private String jobStatusString;
	private long jobPauseStartDate;

	public JobProgress(FileJob job) {
		this.job = job;
		if (job instanceof TransferFileJob) {
			this.transferFileJob = (TransferFileJob) job;
		}
		lastBytesTotal = 0;
		lastTime = System.currentTimeMillis();
	}

	
	/**
	 * Calculates the job progress status. This method calculates variables used
	 * to show job progress information. It can update information only on a
	 * processed file (when <code>labelOnly</code> is <code>true</code>). If
	 * <code>labelOnly</code> is false it will try to update full information on
	 * a job progress (e.g. percent completed, bytes per second, etc.).
	 * 
	 * @param fullUpdate
	 * 			 <code>true</code> update all information about processed file.<br/>
	 * 			 <code>false</code> update only label of a processed file.<br/>
	 * 		     Note that if a job has just finished this flag is ignored 
	 * 			 and all variables are recalulated.
	 * @return <code>true</code> if full job progress has been updated,
	 *         <code>false</code> if only label has been updated.
	 */
	public boolean calcJobProgress(boolean fullUpdate) {
		int jobState = job.getState();
		jobPauseStartDate = job.getPauseStartDate();
		if (jobState == FileJob.FINISHED || jobState == FileJob.INTERRUPTED) {
			jobStatusString = Translator.get("progress_dialog.job_finished");
			// Job just finished, let's loop one more time to ensure that
			// components (progress bar in particular)
			// reflect job completion
			fullUpdate = true;
		} else {
			jobStatusString = job.getStatusString();
		}
		if (!fullUpdate) {
			return false;
		}
		// Do not refresh progress information is job is paused, simply sleep
		if (jobState == FileJob.PAUSED) {
			return false;
		}
		// Now is updated with current time, or job end date if job has finished
		// already.
		long now = job.getEndDate();
		if (now == 0) { // job hasn't finished yet
			now = System.currentTimeMillis();
		}

		long currentFileRemainingTime = 0;
		long totalRemainingTime;

		effectiveJobTime = job.getEffectiveJobTime();
		if (effectiveJobTime == 0) {
			effectiveJobTime = 1; // To avoid potential zero divisions
		}

		if (transferFileJob != null) {
			bytesTotal = transferFileJob.getTotalByteCounter().getByteCount()
					- transferFileJob.getTotalSkippedByteCounter().getByteCount();
			totalBps = (long) (bytesTotal * 1000d / effectiveJobTime);
			if (now - lastTime > 0) { // To avoid divisions by zero 
				currentBps = (long) ((bytesTotal - lastBytesTotal) * 1000d / (now - lastTime));
			} else {
				currentBps = 0;
			}

			// Update current file progress bar
			float filePercentFloat = transferFileJob.getFilePercentDone();
			filePercentInt = (int) (100 * filePercentFloat);

			fileProgressText = filePercentInt + "%";
			// Append estimated remaining time (ETA) if current file transfer is
			// not already finished (100%)
			if (filePercentFloat < 1) {
				fileProgressText += " - ";

				long currentFileSize = transferFileJob.getCurrentFileSize();
				// If current file size is not available, ETA cannot be
				// calculated
				if (currentFileSize == -1) {
					fileProgressText += "?";
				}
				// Avoid potential divisions by zero
				else if (totalBps == 0) {
					currentFileRemainingTime = -1;
					fileProgressText += DurationFormat.getInfiniteSymbol();
				} else {
					currentFileRemainingTime = (long) ((1000 * (currentFileSize - 
							transferFileJob.getCurrentFileByteCounter().getByteCount())) / 
							(float) totalBps);
					fileProgressText += DurationFormat.format(currentFileRemainingTime);
				}
			}

			lastBytesTotal = bytesTotal;
			lastTime = now;
		}

		// Update total progress bar
		// Total job percent is based on the *number* of files remaining, not
		// their actual size.
		// So this is very approximate.
		float totalPercentFloat = job.getTotalPercentDone();
		totalPercentInt = (int) (100 * totalPercentFloat);

		totalProgressText = totalPercentInt + "%";

		// Add a rough estimate of the total remaining time (ETA):
		// total remaining time is based on the total job percent completed
		// which itself is based on the *number*
		// of files remaining, not their actual size. So this is very
		// approximate.
		// Do not add ETA if job is already finished (100%)
		if (totalPercentFloat < 1) {
			totalProgressText += " - ";

			// Avoid potential divisions by zero
			if (totalPercentFloat == 0) {
				totalProgressText += "?";
			} else {
				// Make sure that total ETA is never smaller than current file
				// ETA
				totalRemainingTime = (long) ((1 - totalPercentFloat) * 
						(effectiveJobTime / totalPercentFloat));
				totalRemainingTime = Math.max(totalRemainingTime,
						currentFileRemainingTime);
				totalProgressText += DurationFormat.format(totalRemainingTime);
			}
		}
		return true;
	}

	public String getJobStatusString() {
		return jobStatusString;
	}

	public boolean isTransferFileJob() {
		return transferFileJob != null;
	}

	public int getFilePercentInt() {
		return filePercentInt;
	}

	public String getFileProgressText() {
		return fileProgressText;
	}

	public long getBytesTotal() {
		return bytesTotal;
	}

	public long getTotalBps() {
		return totalBps;
	}

	public long getLastTime() {
		return lastTime;
	}

	public long getCurrentBps() {
		return currentBps;
	}

	public int getTotalPercentInt() {
		return totalPercentInt;
	}

	public String getTotalProgressText() {
		return totalProgressText;
	}

	public long getEffectiveJobTime() {
		return effectiveJobTime;
	}

	public long getJobPauseStartDate() {
		return jobPauseStartDate;
	}

}
