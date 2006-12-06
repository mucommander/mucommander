package com.mucommander;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.auth.CredentialsManager;


/**
 * The run method of this thread is called when the program shuts down, either because
 * the user chose to quit the program or because the program was interrupted by a logoff.
 *
 * @author Maxence Bernard
 */
public class ShutdownHook extends Thread {

    private static boolean shutdownTasksPerformed;

    public ShutdownHook() {
        super(ShutdownHook.class.getName());
    }


    public static void initiateShutdown() {
        if(Debug.ON) Debug.trace("shutting down");

//            // No need to call System.exit() under Java 1.4, application will naturally exit
//            // when no there is no more window showing and no non-daemon thread still running.
//            // However, natural application death will not trigger ShutdownHook so we need to explicitly
//            // perform shutdown tasks.
//            performShutdownTasks();

        // System.exit() will trigger ShutdownHook and perform shutdown tasks
        System.exit(0);
    }
    

    /**
     * Called by the VM when the program shuts down, this method writes the configuration.
     */
    public void run() {
        performShutdownTasks();
    }


    /**
     * Performs tasks before shut down, such as writing the configuration file. This method can only
     * be called once, any further call will be ignored (no-op).
     */
    private synchronized static void performShutdownTasks() {
        // Return if shutdown tasks have already been performed
        if(shutdownTasksPerformed)
            return;

        if(Debug.ON) Debug.trace("called");
        
        // Save preferences
        ConfigurationManager.writeConfiguration();
        // Save shell history
        ShellHistoryManager.writeHistory();
        // Write credentials file to disk, only if changes were made
        CredentialsManager.writeCredentials(false);

        // Shutdown tasks should only be performed once
        shutdownTasksPerformed = true;
    }
}
