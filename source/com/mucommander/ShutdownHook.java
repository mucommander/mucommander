
package com.mucommander;

import com.mucommander.conf.ConfigurationManager;

/**
 * The run method of this thread is called when the program shuts down, either because
 * the user chooe to quit the program or because the user logged off, shut down...
 *
 * @author Maxence Bernard
 */
public class ShutdownHook extends Thread {

	public ShutdownHook() {
		super("com.mucommander.ShutDownHook's thread");
	}

	/**
	 * Called by the VM when the program shuts down, this method writes the configuration.
	 */
	public void run() {
		// Saves preferences
		ConfigurationManager.writeConfiguration();
	}

}