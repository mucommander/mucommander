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

/**
 * Internal states of a {@code FileJob}
 *
 * @author Arik Hadas
 */
public enum FileJobState {
	/** Indicates that this job has not started yet, this is a temporary state */
	NOT_STARTED, 
	/** Indicates that this job is currently processing files, this is a temporary state */
	RUNNING,
	/** Indicates that this job is currently paused, waiting for user response, this is a temporary state */
	PAUSED,
	/** Indicates that this job has been interrupted by the end user, this is a permanent state */
	INTERRUPTED,
	/** Indicates that this job has naturally finished (i.e. without being interrupted), this is a permanent state */
	FINISHED
}
