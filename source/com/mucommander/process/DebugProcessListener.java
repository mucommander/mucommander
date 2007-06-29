/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.process;

import com.mucommander.Debug;

/**
 * Listener used while in debug mode.
 * <p>
 * In debug mode, instances of this listener will automatically be registered to non-monitored processes.
 * Its only goal is to output information about the process' state.
 * </p>
 * @author Nicolas Rinaudo
 */
class DebugProcessListener implements ProcessListener {
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
        StringBuffer buffer;

        // Rebuilds the command.
        buffer = new StringBuffer();
        for(int i = 0; i < tokens.length; i++) {
            buffer.append(tokens[i]);
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
    public void processDied(int returnValue) {if(Debug.ON) Debug.trace(command + ": died with return code " + returnValue);}

    /**
     * Prints out the process output.
     */
    public void processOutput(char[] buffer, int offset, int length) {if(Debug.ON) Debug.trace(command + ": " + new String(buffer, offset, length));}
}
