
package com.mucommander.job;


/**
 * FileJobException are exceptions that can be thrown by certain
 * FileJob methods.
 *
 * @author Maxence Bernard
 */
public class FileJobException extends Exception {

	/** Source cannot be opened */
	public final static int CANNOT_OPEN_SOURCE = 1;

	/** Destination cannot be opened */
	public final static int CANNOT_OPEN_DESTINATION = 2;

	/** An error occurred during the file transfer */
	public final static int ERROR_WHILE_TRANSFERRING = 3;


	protected int reason;

	
	public FileJobException(int reason) {
		this.reason = reason;
	}
	
	public int getReason() {
		return reason;
	}
}
