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

package com.mucommander.process;


/**
 * Implementations of this interface can listen to a process' state and streams.
 * @see com.mucommander.process.AbstractProcess
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public interface ProcessListener {
	
    /** 
     * This method is called when the process dies. No more calls to <code>processOutput</code> and
     * <code>processError</code> will be made past this call.
     * @param returnValue the value returned by the process (return code).
     */
    public void processDied(int returnValue);

    /**
     * This method is called whenever the process sends data to its output streams (stdout or stderr).
     * @param buffer contains the process' output.
     * @param offset offset in buffer at which the process' output starts.
     * @param length length of the process' output in buffer.
     */
    public void processOutput(char buffer[], int offset, int length);
}
