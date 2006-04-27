
package com.mucommander;


/**
 * This interface provides a way for classes to be notified of the data that's being sent by a process
 * to the standard (stdin) and error (stdout) outputs, and of the process' death.
 *
 * @see com.mucommander.ProcessMonitor
 * @author Maxence Bernard
 */
public interface ProcessListener {
	
	/** 
	 * This method is called when the monitored process dies. No more calls to <code>processOutput</code> and
	 * <code>processError</code> will be made past this call.
	 *
	 * @param process the process monitored by this listener.
	 * @param returnValue the value returned by the process (return code).
	 */
	public void processDied(Process process, int returnValue);

	/**
	 *  This method is called whenever the process has written something to the standard output (stdout).
	 *
	 * @param process the process monitored by this listener.
	 */
	public void processOutput(Process process, byte buffer[], int offset, int length);

	/**
	 * This method is called whenever the process has written something to the error output (stderr).
	 *
	 * @param process the process monitored by this listener.
	 */
	public void processError(Process process, byte buffer[], int offset, int length);

}
