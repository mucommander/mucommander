
package com.mucommander;

import java.io.*;

/**
 * Monitors a Process and notifies the registered {@link com.mucommander.ProcessListener ProcessListener} of the
 * data that's being sent by the process to the standard (stdin) and error (stdout) outputs.
 * 
 * <p>The <code>ProcessListener</code> is also notifed of the process' death whenever it has died a natural death
 * or has been destroyed.
 *
 * @see com.mucommander.ProcessListener
 * @author Maxence Bernard
 */
public class ProcessMonitor implements Runnable {
	
	/** The Process being monitored */ 
	private Process process;
	/** The ProcessListener that is receiving events */
	private ProcessListener listener;
	
	/** Monitoring thread */
	private Thread outputMonitorThread;

	
	/**
	 * Creates a new ProcessMonitor and immediately starts monitoring the given Process
	 * and notifying the listener.
	 *
	 * @param process the Process to monitor
	 * @param listener the ProcessListener instance which will receive events triggered by the process watch
	 */
	public ProcessMonitor(Process process, ProcessListener listener) {
		this.process = process;
		this.listener = listener;
	
		(this.outputMonitorThread=new Thread(this, "com.mucommander.ProcessMonitor's Thread")).start();
	}

	
	/**
	 * Stops monitoring the process and notifying the listener. There might be an extra event sent to the
	 * ProcessListener if data is being read from the process when this method is called.
	 */
	public void stopMonitoring() {
		// Stop monitoring thread
		outputMonitorThread = null;
	}

	
    /**
	 * Fully reads the process standard output and error output, one after another, sending events
	 * to the ProcessListener for each chunk read. Stops reading whenever stopMonitoring() is called.
     */
    public void run() {
        byte b[] = new byte[512];	// Read buffer
        int nbRead = 0;				// Number of bytes read and available in the buffer 

		// Fully read the process' standard output
		// and feed it to the listener chunk by chunk
		InputStream in = process.getInputStream();
		while(outputMonitorThread!=null && nbRead!=-1) {
			try {
				if((nbRead=in.read(b, 0, 512))>0) {
					// Notify the listener
					listener.processOutput(process, b, 0, nbRead);
				}
			}
			catch(IOException e) {
				break;
			}
		}
		// Close input stream
		try { in.close(); }
		catch(IOException e) {}
		
		// Fully read the process' error output
		// and feed it to the listener chunk by chunk
		in = process.getErrorStream();
		nbRead = 0;		// Reset read counter so that it is != -1
		while(outputMonitorThread!=null && nbRead!=-1) {
			try {
				if((nbRead=in.read(b, 0, 512))>0) {
					// Notify the listener
					listener.processError(process, b, 0, nbRead);
				}
			}
			catch(IOException e) {
				break;
			}
		}
		// Close input stream
		try { in.close(); }
		catch(IOException e) {}

		// Notify the listener of the process' death, if it is still interested
		if(outputMonitorThread!=null) {
			// Wait for the process' death
			try { process.waitFor(); }
			catch(InterruptedException e) {}
			
			listener.processDied(process, process.exitValue());
		}
	}
	
}
