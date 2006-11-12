package com.mucommander.shell;

import com.mucommander.*;

import java.io.*;

/**
 * Process implementation that takes care of emptying its own streams.
 * <p>
 * One of the pitfalls of the process 'API' is that, depending on the system, processes might
 * lock up if their stdout and stderr streams are not emptied. This implementation's goal is to
 * do just that, and comes with the added bonus of allowing other classes to monitor a process'
 * state.
 * </p>
 * @author Nicolas Rinaudo
 */
class MonitoredProcess extends Process {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Stdout monitor. */
    private ProcessOutputMonitor stdoutMonitor;
    /** Stderr monitor. */
    private ProcessOutputMonitor stderrMonitor;
    /** Process to monitor. */
    private Process              process;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Starts a new monitored process.
     * @param process process to monitor.
     */
    public MonitoredProcess(Process process) {this(process, null);}

    /**
     * Starts a new monitored process which will notify the specified listener of events.
     * @param process  process to monitor.
     * @param listener where to send event messages.
     */
    public MonitoredProcess(Process process, ProcessListener listener) {
        this.process  = process;

        // Depending on the Java version, we don't need to monitor the same streams
        // (Java 1.5+ allows us to merge stdout and stderr).
        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_5) {
            if(Debug.ON) Debug.trace("Starting shell merged output monitor...");
            new Thread(stdoutMonitor = new ProcessOutputMonitor(process.getInputStream(), listener, this), "Shell sdtout/stderr monitor").start();
        }
        else {
            if(Debug.ON) Debug.trace("Starting shell stdout and stderr monitors...");
            new Thread(stdoutMonitor = new ProcessOutputMonitor(process.getInputStream(), listener, this), "Shell stdout monitor").start();
            new Thread(stderrMonitor = new ProcessOutputMonitor(process.getErrorStream(), listener), "Shell stderr monitor").start();
        }

    }



    // - Process methods -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Destroys the process.
     * <p>
     * Note that this method disables notification of the 'thread died' event.
     * </p>
     */
    public void destroy() {
        if(Debug.ON) Debug.trace("Destroying process...");
        stdoutMonitor.stopMonitoring();
        if(stderrMonitor != null)
            stderrMonitor.stopMonitoring();
        process.destroy();
    }

    /**
     * Returns the process' exit value.
     * @return the process' exit value.
     */
    public int exitValue() {
        // If debug is on, prints the exit value.
        if(Debug.ON) {
            int exitValue;

            exitValue = process.exitValue();
            Debug.trace("Process terminated with exit value " + exitValue);
            return exitValue;
        }
        return process.exitValue();
    }

    /**
     * Waits for the process to die.
     * @return the process' exit value.
     * @exception InterruptedException thrown if the current thread is interrupted while waiting for the process to die.
     */
    public int waitFor() throws InterruptedException {
        if(Debug.ON) Debug.trace("Waiting for process to terminate...");
        return process.waitFor();
    }

    /**
     * Returns the process' input stream.
     * @return the process' input stream.
     */
    public InputStream getInputStream() {return process.getInputStream();}

    /**
     * Returns the process' error stream.
     * @return the process' error stream.
     */
    public InputStream getErrorStream() {return process.getErrorStream();}

    /**
     * Returns the process' output stream.
     * @return the process' output stream.
     */
    public OutputStream getOutputStream() {return process.getOutputStream();}
}
