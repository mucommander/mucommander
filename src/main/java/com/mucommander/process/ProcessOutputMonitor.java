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

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to monitor a process' stdout and stderr streams.
 * <p>
 * This class is used by {@link com.mucommander.process.AbstractProcess} to make sure that
 * processes do not stall because their stdout and stderr streams are not emptied.
 * </p>
 * <p>
 * This implementation is rather hackish, and should not be used directly: it works, but is not
 * meant to support anything but the very specific needs of {@link com.mucommander.process.AbstractProcess}.
 * </p>
 * @author Nicolas Rinaudo
 */
class ProcessOutputMonitor implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessOutputMonitor.class);
	
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Stream to read from. */
    private InputStream     in;
    private String          encoding;
    /** Listener to notify of updates. */
    private ProcessListener listener;
    /** Process to wait on once the stream is closed. */
    private AbstractProcess process;
    /** Whether the process is still being monitored. */
    private boolean         monitor;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a news ProcessOutputMonitor that will read from <code>in</code> and notify <code>listener</code>.
     * @param in       input stream to 'empty'.
     * @param listener where to send the content of the stream.
     */
    public ProcessOutputMonitor(InputStream in, String encoding, ProcessListener listener) {
        this.listener = listener;
        this.in       = in;
        monitor       = true;
        this.encoding = encoding;
    }

    /**
     * Creates a news ProcessOutputMonitor that will read from <code>in</code> and notify <code>listener</code>.
     * <p>
     * A process monitor created that way will also wait on the specified <code>process</code> before quiting,
     * and notify the listener when the process has actually died.
     * </p>
     * @param in       input stream to 'empty'.
     * @param listener where to send the content of the stream.
     * @param process  process to wait on.
     */
    public ProcessOutputMonitor(InputStream in, String encoding, ProcessListener listener, AbstractProcess process) {
        this(in, encoding, listener);
        this.process = process;
    }



    // - Main code -------------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Empties the content of the stream and notifies the listener.
     */
    public void run() {
        byte[] buffer; // Where to store the stream's output.
        int    read;   // Number of bytes read in the last read operation.

        buffer = new byte[512];

        // Reads the content of the stream.
        try {
            while(monitor && ((read = in.read(buffer, 0, buffer.length)) != -1)) {
                if(listener != null) {
                    listener.processOutput(buffer, 0, read);
                    if(encoding == null)
                        listener.processOutput(new String(buffer, 0, read));
                    else
                        listener.processOutput(new String(buffer, 0, read, encoding));
                }
            }
        }
        // Ignore this exception: either there's nothing we can do about it anyway,
        // or it's 'normal' (the process has been killed).
        catch(IOException e) {
            LOGGER.debug("IOException thrown while monitoring process", e);
        }

        LOGGER.debug("Process output stream emptied, closing");

        // Closes the stream.
        try {
	    if(in != null)
		in.close();
	}
        catch(IOException e) {
            LOGGER.debug("IOException thrown while closing process stream", e);
        }

        // If a process was set, perform 'cleanup' tasks.
        if(process != null) {
            // Waits for the process to die.
            try {process.waitFor();}
            catch(Exception e) {
                LOGGER.debug("Caught Exception while waiting for process "+process, e);
            }
            // If this process is still being monitored, notifies its
            // listener that it has exited.
            if(monitor && (listener != null))
                listener.processDied(process.exitValue());
        }
    }

    /**
     * Notifies the monitor that it should stop reading from the stream it's been affected to.
     * <p>
     * Calling this method will cause the process' stream to stop being read. For this reason,
     * it should only be called right before the process is killed, as it will otherwise stall.
     * </p>
     */
    public void stopMonitoring() {
	// Closes the input stream.
	try {in.close();}
	catch(Exception e) {}

	// Notifies the main thread that it should stop monitoring the stream.
	in      = null;
	monitor = false;
    }
}
