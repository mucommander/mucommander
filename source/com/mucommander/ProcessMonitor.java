
package com.mucommander;

import java.io.*;

/**
 * Monitors a Process state and notifies the ProcessListener when the process output something to
 * standard output or error streams, or when it died.
 *
 * @author Maxence Bernard
 */
public class ProcessMonitor implements Runnable {
	
	private Process process;
	private ProcessListener listener;
	
	private Thread outputMonitorThread;
	
	private boolean stillMonitoring = true;
	
	private int outputCheckPeriod = 100;

	
	/**
	 * Creates a new ProcessMonitor and immediately starts monitoring the given Process
	 */
	public ProcessMonitor(Process process, ProcessListener listener) {
		this.process = process;
		this.listener = listener;
	
		(this.outputMonitorThread=new Thread(this, "com.mucommander.ProcessMonitor's Thread")).start();
	}
	
	/**
	 * Sets the period in milliseconds between 2 checks to see if the process did output something new.
	 * Lower period means faster notification but more CPU overhead.
	 */
	public void setOutputCheckPeriod(int msPeriod) {
		this.outputCheckPeriod = msPeriod;	
	}
	
	
	public void stopMonitoring() {
//System.out.println("ProcessMonitor.stopMonitoring");
		
		this.stillMonitoring = false;
		
		// Stop running threads
		outputMonitorThread = null;
	}

	
    /**
	 * Prints the command output to the dialog's text area as it comes
     */
    public void run() {
//System.out.println("ProcessMonitor.run");
        InputStream pin = process.getInputStream();	
		InputStream perr = process.getErrorStream();
		
        byte b[] = new byte[512];
        int nbRead = 0;

        while(outputMonitorThread!=null && nbRead!=-1) {
//System.out.println("ProcessMonitor.run2 (main loop)");
			try {
				// Try to read stdout
				if((nbRead=pin.read(b, 0, 512))>0) {
					// Notify the listener if it is still interested
					listener.processOutput(process, b, 0, nbRead);
				}
				// If nothing to read, try to read stderr
				else if((nbRead=perr.read(b, 0, 512))>0) {
					// Notify the listener if it is still interested
					listener.processError(process, b, 0, nbRead);
				}
			}
			catch(IOException e) {
//System.out.println("ProcessMonitor.run2b (IOException) "+e);
				break;
			}

//System.out.println("ProcessMonitor.run3 (sleeping) "+outputMonitorThread+" "+nbRead+" "+stillMonitoring);
				
			try {
				outputMonitorThread.sleep(outputCheckPeriod);
			}
			catch(InterruptedException e) {
//System.out.println("ProcessMonitor.run3b (InterruptedException)");
			}
		}
//System.out.println("ProcessMonitor.run4");

		if(stillMonitoring)
			listener.processDied(process, process.exitValue());

		// Close input streams
		try {
			pin.close();
			perr.close();
		}
		catch(IOException e) {}
	}
	
}
