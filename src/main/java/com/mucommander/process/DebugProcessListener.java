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

package com.mucommander.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener used while in debug mode.
 * <p>
 * In debug mode, instances of this listener will automatically be registered to non-monitored processes.
 * Its only goal is to output information about the process' state.
 * </p>
 * @author Nicolas Rinaudo
 */
class DebugProcessListener implements ProcessListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(DebugProcessListener.class);
	
    // - Instance fields -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Command that this listener is monitoring. */
    private String command;



    // - Initialisastion -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new process listener monitoring the specified command.
     * @param tokens tokens that compose the command that is being ran.
     */
    public DebugProcessListener(String[] tokens) {
        StringBuilder buffer;

        // Rebuilds the command.
        buffer = new StringBuilder();
        for (String token : tokens) {
            buffer.append(token);
            buffer.append(' ');
        }

        command = buffer.toString();
    }



    // - Process monitoring --------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Prints out information about the way the process died.
     * @param returnValue process' return value.
     */
    public void processDied(int returnValue) {
        LOGGER.debug(command + ": died with return code " + returnValue);
    }

    /**
     * Ignored.
     */
    public void processOutput(byte[] buffer, int offset, int length) {
        LOGGER.trace(command + ": " + new String(buffer, offset, length));
    }

    /**
     * Prints out the process output.
     */
    public void processOutput(String output) {
        LOGGER.trace(command + ": " + output);
    }
}
