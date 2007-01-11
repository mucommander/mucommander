package com.mucommander.process;


import com.mucommander.Debug;
import com.mucommander.PlatformManager;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * muCommander specific version of a process.
 * <p>
 * Implementations of this class are used to execute various types of processes.
 * It can be a {@link com.mucommander.process.LocalProcess}, but some types of
 * {@link com.mucommander.file.AbstractFile abstract files}, such as SFTP files,
 * allow for commands to be executed.
 * </p>
 * <p>
 * Unlike normal instances of <code>java.lang.Process</code>, abstract processes
 * will empty their own streams, preventing deadlocks from occuring on some systems.
 * </p>
 * <p>
 * Note that abstract processes should not be created directly. They should be
 * instanciated through {@link com.mucommander.process.ProcessRuner#execute(String[],AbstractFile,ProcessListener}.
 * </p>
 * @author Nicolas Rinaudo
 */
public abstract class AbstractProcess {
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
        // An example of that is executing <code>echo blah | ssh localhost ls -l</cope>
        // under MAC OS X.
        // Using a separate thread allows muCommander to continue working properly even
        // when that occurs.
        new Thread() {
            public void run() {
                // Closes the process' streams.
		if(Debug.ON) Debug.trace("Destroying process...");
		stdoutMonitor.stopMonitoring();
		if(stderrMonitor != null)
		    stderrMonitor.stopMonitoring();

                // Destroys the process.
                destroyProcess();
            }
        }.start();
    }

    /**
     * Starts monitoring the process.
     * @param listener if non <code>null</code>, <code>listener</code> will receive updates about the process' event.
     */
    void startMonitoring(ProcessListener listener) {
        // Only monitors stdout if the process uses merged streams.
        if(usesMergedStreams()) {
            if(Debug.ON) Debug.trace("Starting process merged output monitor...");
            new Thread(stdoutMonitor = new ProcessOutputMonitor(getInputStream(), listener, this), "Shell sdtout/stderr monitor").start();
        }
        // Monitors both stdout and stderr.
        else {
            if(Debug.ON) Debug.trace("Starting process stdout and stderr monitors...");
            new Thread(stdoutMonitor = new ProcessOutputMonitor(getInputStream(), listener, this), "Shell stdout monitor").start();
            new Thread(stderrMonitor = new ProcessOutputMonitor(getErrorStream(), listener), "Shell stderr monitor").start();
        }
    }



    // - Abstract methods ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if this process merges his output streams.
     * <p>
     * Some processes will use a single stream for their standard error and standard output streams. Such
     * processes should return <code>true</code> here to prevent both streams from being monitored.<br/>
     * Note that if a process uses merged streams, {@link #getInputStream()} will be monitored.
     * </p>
     * @return <code>true</code> if this process merges his output streams, <code>false</code> otherwise.
     */
    public abstract boolean usesMergedStreams();

    /**
     * Makes the current thread wait for the process to die.
     * @return the process' exit code.
     * @throws InterruptedException thrown if the current thread is interrupted while wainting on the process to die.
     */
    public abstract int waitFor() throws InterruptedException;

    /**
     * Destroys the process.
     */
    protected abstract void destroyProcess();

    /**
     * Returns this process' exit value.
     * @return this process' exit value.
     */
    public abstract int exitValue();

    /**
     * Returns the stream used to send data to the process.
     * @return the stream used to send data to the process.
     */
    public abstract OutputStream getOutputStream();

    /**
     * Returns the process' standard output stream.
     * @return the process' standard output stream.
     */
    public abstract InputStream getInputStream();

    /**
     * Returns the process' standard error stream.
     * @return the process' standard error stream.
     */
    public abstract InputStream getErrorStream();

}
