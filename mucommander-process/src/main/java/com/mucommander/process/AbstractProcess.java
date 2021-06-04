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

package com.mucommander.process;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * muCommander specific version of a process, allowing various types of processes to be executed.
 * <p>
 * Unlike normal instances of <code>java.lang.Process</code>, abstract processes
 * will empty their own streams, preventing deadlocks from occurring on some systems.
 * </p>
 * <p>
 * Note that abstract processes should not be created directly. They should be
 * instantiated through {@link com.mucommander.process.ProcessRunner#execute(String[], com.mucommander.commons.file.AbstractFile,ProcessListener)}.
 * </p>
 * @author Nicolas Rinaudo
 */
public abstract class AbstractProcess {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProcess.class);
	
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Stdout monitor. */
    private ProcessOutputMonitor stdoutMonitor;
    /** Stderr monitor. */
    private ProcessOutputMonitor stderrMonitor;



    // - Process monitoring ----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Kills the process.
     */
    public final void destroy() {
        // Process destruction occurs in a separate thread, as in some (rare)
        // cases, deadlocks will occur while trying to kill a native process.
        // An example of that is executing <code>echo blah | ssh localhost ls -l</code>
        // under macOS.
        // Using a separate thread allows muCommander to continue working properly even
        // when that occurs.
        new Thread() {
            @Override
            public void run() {
                // Closes the process' streams.
            	LOGGER.debug("Destroying process...");
                stdoutMonitor.stopMonitoring();
                if(stderrMonitor != null)
                    stderrMonitor.stopMonitoring();

                // Destroys the process.
                try {
                    destroyProcess();
                }
                catch(IOException e) {
                	LOGGER.debug("IOException caught", e);
                }
            }
        }.start();
    }

    /**
     * Starts monitoring the process.
     * @param listener if non <code>null</code>, <code>listener</code> will receive updates about the process' event.
     * @param encoding encoding that should be used by the process' stdout and stderr streams.
     */
    final void startMonitoring(ProcessListener listener, String encoding) throws IOException {
        LOGGER.debug("Starting process merged output monitor...");
        new Thread(stdoutMonitor = new ProcessOutputMonitor(getInputStream(), encoding, listener, this), "Process sdtout/stderr monitor").start();
    }



    // - Abstract methods ------------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Makes the current thread wait for the process to die.
     * @return the process' exit code.
     * @throws InterruptedException thrown if the current thread is interrupted while waiting on the process to die.
     * @throws IOException          thrown if an error occurs while waiting for the process to die.
     */
    public abstract int waitFor() throws InterruptedException, IOException;

    /**
     * Destroys the process.
     * @throws IOException thrown if an error occurs while destroying the process.
     */
    protected abstract void destroyProcess() throws IOException;

    /**
     * Returns this process' exit value.
     * @return this process' exit value.
     */
    public abstract int exitValue();

    /**
     * Returns the stream used to send data to the process.
     * @return             the stream used to send data to the process.
     * @throws IOException thrown if an error occurs while retrieving the process' output stream.
     */
    public abstract OutputStream getOutputStream() throws IOException;

    /**
     * Returns the process' standard output stream.
     * @return             the process' standard output stream.
     * @throws IOException thrown if an error occurs while retrieving the process' input stream.
     */
    public abstract InputStream getInputStream() throws IOException;

}
