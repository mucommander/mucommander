
package com.mucommander.file;


/**
 * Interface denoting a remote file. This interface should be implemented whenever the file is a remote file,
 * i.e. when the file is located on a remote filesystem and access to this file can bring some latency.
 *
 * @author Maxence Bernard
 */
public interface RemoteFile {

	/**
	 * Returns a string describing the protocol used to access the remote file (e.g. SMB).
	 */
	public String getProtocol();
}
