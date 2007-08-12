/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander;

import com.mucommander.auth.CredentialsManager;
import com.mucommander.command.CommandManager;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.ui.theme.ThemeManager;


/**
 * The run method of this thread is called when the program shuts down, either because
 * the user chose to quit the program or because the program was interrupted by a logoff.
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

        // Save preferences
        try {ConfigurationManager.writeConfiguration();}
        catch(Exception e) {if(Debug.ON) Debug.trace("Failed to save configugration: " + e);}

        // Save shell history
        try {ShellHistoryManager.writeHistory();}
        catch(Exception e) {if(Debug.ON) Debug.trace("Failed to save shell history: " + e);}

        // Write credentials file to disk, only if changes were made
        try {CredentialsManager.writeCredentials(false);}
        catch(Exception e) {if(Debug.ON) Debug.trace("Failed to save credentials: " + e);}

        // Saves the current theme.
        try {ThemeManager.saveCurrentTheme();}
        catch(Exception e) {if(Debug.ON) Debug.trace("Failed to save user theme: " + e);}

        // Saves the file associations.
        try {CommandManager.writeCommands();}
        catch(Exception e) {if(Debug.ON) Debug.trace("Failed to save commands: " + e);}
        try {CommandManager.writeAssociations();}
        catch(Exception e) {if(Debug.ON) Debug.trace("Failed to save associations: " + e);}
        

        // Shutdown tasks should only be performed once
        shutdownTasksPerformed = true;
    }
}
