/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

import java.util.EventListener;

/**
 * Interface to be implemented by classes that wish to be notified of progress changes on a particular
 * {@link FileJob}. Those classes need to be registered to receive those events, this can be done by calling
 * {@link JobsManager#addJobListener(JobListener)}.
 *
 * @author Mariusz Jakubowski, Arik Hadas
 */
public interface JobListener extends EventListener {

    /**
     * Called when a job starts to execute in the background (non-blocking mode).
     *
     * @param source a job added
     */
    default void jobAdded(FileJob source) {
    }

    /**
     * Called when a job stops to execute in the background (finished/interrupted/switches
     * to blocking mode).
     *
     * @param source a job removed
     */
    default void jobRemoved(FileJob source) {
    }

    /**
     * Called when the progress of the specified FileJob has been updated.
     *
     * @param source     the FileJob which progress has been updated
     * @param fullUpdate if false indicates that only file label has been updated
     * @see JobProgress#calcJobProgress
     */
    default void jobProgress(FileJob source, boolean fullUpdate) {
    }

}
