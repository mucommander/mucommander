
package com.mucommander;

import java.io.*;

/**
 * Monitors a Process state and notifies the ProcessListener when the process did utput something or when it died
 *
 * @author Maxence Bernard
 */
public class ProcessMonitor implements Runnable {
	
	private Process process;
	private ProcessListener listener;
	
	private Thread outputMonitorThread;
//	private Thread deathMonitorThread;
	
	private boolean stillMonitoring = true;
	
	private int outputCheckPeriod = 100;
	
//	private int deathOutputTimeout = 5000;

/*	
	class ProcessDeathMonitor implements Runnable {
		
		ProcessDeathMonitor() {
		}
	
		public void run() {
			try {
				// Wait for the process death (exciting!)
				process.waitFor();
System.out.println("ProcessDeathMonitor.run (process died)");

				// Notifies the listener of the process death (if it is still interested)
				if(stillMonitoring) {
					// Gives output thread 10 * outputCheckPeriod to get the remaining of the process output
					try {
//							deathMonitorThread.sleep(10*outputCheckPeriod);
						deathMonitorThread.sleep(deathOutputTimeout);
					}
					catch(InterruptedException e) {
System.out.println("ProcessDeathMonitor.run InterruptedException ");						
					}
System.out.println("ProcessDeathMonitor.run2 (stopMonitoring)");

					// Stops running threads
					stopMonitoring();

System.out.println("ProcessDeathMonitor.run2 (death notification)");
					listener.processDied(process, process.exitValue());
				}
			}
			catch(InterruptedException e) {}
		}
	}
*/
	
	/**
	 * Creates a new ProcessMonitor and immediately starts monitoring the given Process
	 */
	public ProcessMonitor(Process process, ProcessListener listener) {
		this.process = process;
		this.listener = listener;
	
		(this.outputMonitorThread=new Thread(this)).start();
//		(deathMonitorThread=new Thread(this)).start();
	}
	
	/**
	 * Sets the period in milliseconds between 2 checks to see if the process did output something new.
	 * Lower period means faster notification but more CPU overhead.
	 */
	public void setOutputCheckPeriod(int msPeriod) {
		this.outputCheckPeriod = msPeriod;	
	}
	
	
	public void stopMonitoring() {
System.out.println("ProcessMonitor.stopMonitoring");
		
		this.stillMonitoring = false;
		
		// Stop running threads
//		deathMonitorThread = null;
		outputMonitorThread = null;
	}

	
    /**
	 * Prints the command output to the dialog's text area as it comes
     */
    public void run() {
System.out.println("ProcessMonitor.run");
        InputStream pin = process.getInputStream();	
		
        byte b[] = new byte[512];
        int nbRead = 0;

//        while(outputMonitorThread!=null && nbRead!=-1 && stillMonitoring) {
        while(outputMonitorThread!=null && nbRead!=-1) {
System.out.println("ProcessMonitor.run2 (main loop)");
			try {
				if((nbRead=pin.read(b, 0, 512))>0) {
					// Notifies the listener if it is still interested
						listener.processOutput(process, b, 0, nbRead);
				}
			}
			catch(IOException e) {
System.out.println("ProcessMonitor.run2b (IOException) "+e);
				break;
			}

System.out.println("ProcessMonitor.run3 (sleeping) "+outputMonitorThread+" "+nbRead+" "+stillMonitoring);
				
			try {
				outputMonitorThread.sleep(outputCheckPeriod);
			}
			catch(InterruptedException e) {
System.out.println("ProcessMonitor.run3b (InterruptedException)");
			}
		}
System.out.println("ProcessMonitor.run4");

		if(stillMonitoring)
			listener.processDied(process, process.exitValue());

/*
		// Read error stream before exiting (if listener is still interested)
		InputStream perr = process.getErrorStream();
        if(stillMonitoring) {
			try {
				while((nbRead=perr.read(b, 0, 512))!=-1) {
					// Notifies the listener if it is still interested
					listener.processOutput(process, b, 0, nbRead);
				}
			}
			catch(IOException e) {
			}
		}
*/		
		// Close input streams
		try {
			pin.close();
//			perr.close();
		}
		catch(IOException e) {}
	}
	
}
