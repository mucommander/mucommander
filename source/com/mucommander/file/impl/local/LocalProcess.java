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

package com.mucommander.file.impl.local;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.process.AbstractProcess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Process running on the local computer.
 * @author Nicolas Rinaudo
 */
class LocalProcess extends AbstractProcess {
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
        // Java 1.5 and higher can merge stderr and stdout through ProcessBuilder.
        // This is the preferred way of working with processes.
        if(PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_5) {
            ProcessBuilder pb = new ProcessBuilder(tokens);
            // Set the process' working directory
            pb.directory(dir);
            // Merge the process' stdout and stderr 
            pb.redirectErrorStream(true);

            process = pb.start();
        }

        // Java 1.4 or below, use Runtime.exec() which separates stdout and stderr (harder to manipulate)
        else
            process = Runtime.getRuntime().exec(tokens, null, dir);

        // Safeguard: makes sure that an exception is raised if the process could not be created.
        // This might not be strictly necessary, but the Runtime.exec documentation is not very precise
        // on what happens in case of an error.
        if(process == null)
            throw new IOException();
    }



    // - Implementation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the VM supports merged <code>java.lang.Process</code> streams.
     * @return <code>true</code> if the VM supports merged <code>java.lang.Process</code> streams, <code>false</code> otherwise.
     */
    public boolean usesMergedStreams() {return PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6;}

    /**
     * Waits for the process to die.
     * @return                      the process' exit code.
     * @throws InterruptedException if the thread was interrupted while waiting on the process to die.
     */
    public int waitFor() throws InterruptedException {return process.waitFor();}

    /**
     * Destroys the process.
     */
    protected void destroyProcess() {process.destroy();}

    /**
     * Returns the process' exit value.
     * @return the process' exit value.
     */
    public int exitValue() {return process.exitValue();}

    /**
     * Returns the process' output stream.
     * @return the process' output stream.
     */
    public OutputStream getOutputStream() {return process.getOutputStream();}

    /**
     * Returns the process' error stream.
     * <p>
     * On Java 1.5 or higher, this will throw an <code>java.io.IOException</code>, as we're using
     * merged output streams. Developers should protect themselves against this by checking
     * {@link #usesMergedStreams()} before accessing streams.
     * </p>
     * @return             the process' error stream.
     * @throws IOException if this process is using merged streams.
     */
    public InputStream getErrorStream()  throws IOException {
        if(usesMergedStreams()) {
            if(Debug.ON) Debug.trace("Tried to access the error stream of a merged streams process.");
            throw new IOException();
        }
        return process.getErrorStream();
    }

    /**
     * Returns the process' input stream.
     * @return the process' input stream.
     */
    public InputStream getInputStream() {return process.getInputStream();}
}
