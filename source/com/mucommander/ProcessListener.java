
package com.mucommander;


/**
 * This interface provides a way for classes to be notified of a process's activity.
 *
 * @author Maxence Bernard
 */
public interface ProcessListener {
	
	/** This method is called when the process dies */
	public void processDied(Process process, int returnValue);

	/** This method is called whenever the process has written something to stdout */
	public void processOutput(Process process, byte buffer[], int offset, int length);

	/** This method is called whenever the process has written something to stderr */
	public void processError(Process process, byte buffer[], int offset, int length);

}
