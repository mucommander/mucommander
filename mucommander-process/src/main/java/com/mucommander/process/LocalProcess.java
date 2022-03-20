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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process running on the local computer.
 * @author Nicolas Rinaudo
 */
class LocalProcess extends AbstractProcess {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalProcess.class);
	
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Underlying system process. */
    private Process process;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new local process running the specified command.
     * @param  tokens      command to run and its parameters.
     * @param  dir         directory in which to start the command.
     * @throws IOException if the process could not be created.
     */
    public LocalProcess(String[] tokens, File dir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(tokens);
        // Set the process' working directory
        pb.directory(dir);
        // Merge the process' stdout and stderr
        pb.redirectErrorStream(true);

        process = pb.start();

        // Safeguard: makes sure that an exception is raised if the process could not be created.
        // This might not be strictly necessary, but the Runtime.exec documentation is not very precise
        // on what happens in case of an error.
        if(process == null)
            throw new IOException();
    }



    // - Implementation --------------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Waits for the process to die.
     * @return                      the process' exit code.
     * @throws InterruptedException if the thread was interrupted while waiting on the process to die.
     */
    @Override
    public int waitFor() throws InterruptedException {return process.waitFor();}

    /**
     * Destroys the process.
     */
    @Override
    protected void destroyProcess() {process.destroy();}

    /**
     * Returns the process' exit value.
     * @return the process' exit value.
     */
    @Override
    public int exitValue() {return process.exitValue();}

    /**
     * Returns the process' output stream.
     * @return the process' output stream.
     */
    @Override
    public OutputStream getOutputStream() {return process.getOutputStream();}

    /**
     * Returns the process' input stream.
     * @return the process' input stream.
     */
    @Override
    public InputStream getInputStream() {return process.getInputStream();}
}
