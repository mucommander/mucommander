/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.dialog.debug;

import com.mucommander.MuLogging.LogLevel;

/**
 * Logging events that presented in the debug console should implement this interface
 *
 * @author Arik Hadas
 */
public interface LoggingEvent {

	/**
	 * Return true if the logging event's level is equal or higher than the given level, false otherwise.
	 * 
	 * @param level logging event level
	 * @return true if the logging event's level is equal or higher than the given level, false otherwise
	 */
    public boolean isLevelEqualOrHigherThan(LogLevel level);
    
    /**
     * Return the logging event's level.
     * 
     * @return the logging event's level
     */
    public LogLevel getLevel();
}
