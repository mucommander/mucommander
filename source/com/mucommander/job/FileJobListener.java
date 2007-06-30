/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

/**
 * Interface to be implemented by classes that wish to be notified of state changes on a particular
 * {@link FileJob}. Those classes need to be registered to receive those events, this can be done by calling
 * {@link FileJob#addFileJobListener(FileJobListener)}.
 *
 * @author Maxence Bernard
 */
public interface FileJobListener {

    /**
     * Called when the state of the specified FileJob has changed.
     *
     * @param source the FileJob which state has changed
     * @param oldState the FileJob's state prior to the change, see FileJob's constant fields for possible values
     * @param newState the new FileJob's state, see FileJob's constant fields for possible values
     */
    public abstract void jobStateChanged(FileJob source, int oldState, int newState);

}
