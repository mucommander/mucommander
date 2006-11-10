package com.mucommander.shell;

import java.io.IOException;
import java.io.InputStream;

/**
 * Used to empty a process' stdout and stderr streams.
 * @author Nicolas Rinaudo
 */
class ProcessOutputMonitor implements Runnable {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Stream to read from. */
    private InputStream     in;
    /** Listener to notify of updates. */
    private ProcessListener listener;
    /** Process to wait on once the stream is closed. */
    private Process         process;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a news ProcessOutputMonitor that will read from <code>in</code> and notify <code>listener</code>.
     * @param in       input stream to 'empty'.
     * @param listener where to send the content of the stream.
     */
    public ProcessOutputMonitor(InputStream in, ProcessListener listener) {
        this.listener = listener;
        this.in       = in;
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
    public ProcessOutputMonitor(InputStream in, ProcessListener listener, Process process) {
        this(in, listener);
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
            while((read = in.read(buffer)) != -1)
                listener.processOutput(buffer, 0, read);
        }
        // Ignore this exception: either there's nothing we can do about it anyway,
        // or it's 'normal' (the process has been killed).
        catch(IOException e) {}

        // Closes the stream.
        try {in.close();}
        catch(IOException e) {if(com.mucommander.Debug.ON) e.printStackTrace(System.err);}

        // If a process was set, wait for it to die and notify the listener.
        if(process != null) {
            try {process.waitFor();}
            catch(Exception e) {if(com.mucommander.Debug.ON) e.printStackTrace(System.err);}
            listener.processDied(process.exitValue());
        }
    }
}
