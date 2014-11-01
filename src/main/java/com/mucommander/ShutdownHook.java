/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.auth.CredentialsManager;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.command.CommandManager;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.ui.action.ActionKeymapIO;
import com.mucommander.ui.main.commandbar.CommandBarIO;
import com.mucommander.ui.main.toolbar.ToolBarIO;
import com.mucommander.ui.main.tree.TreeIOThreadManager;
import com.mucommander.ui.theme.ThemeManager;

/**
 * The run method of this thread is called when the program shuts down, either because
 * the user chose to quit the program or because the program was interrupted by a logoff.
 * @author Maxence Bernard
 */
public class ShutdownHook extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);
	
    /** Whether shutdown tasks have been performed already. */
    private static boolean shutdownTasksPerformed;

    /**
     * Creates a new <code>ShutdownHook</code>.
     */
    public ShutdownHook() {
        super(ShutdownHook.class.getName());
    }


    /**
     * Shuts down muCommander.
     */
    public static void initiateShutdown() {
        LOGGER.info("shutting down");

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
    @Override
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
        
        TreeIOThreadManager.getInstance().interrupt();

        // Save snapshot
        try{MuConfigurations.saveSnapshot();}
        catch(Exception e) {LOGGER.warn("Failed to save snapshot", e);}
        
        // Save preferences
        try {MuConfigurations.savePreferences();}
        catch(Exception e) {LOGGER.warn("Failed to save configuration", e);}

        // Save shell history
        try {ShellHistoryManager.writeHistory();}
        catch(Exception e) {LOGGER.warn("Failed to save shell history", e);}

        // Write credentials file to disk, only if changes were made
        try {CredentialsManager.writeCredentials(false);}
        catch(Exception e) {LOGGER.warn("Failed to save credentials", e);}

        // Write bookmarks file to disk, only if changes were made
        try {BookmarkManager.writeBookmarks(false);}
        catch(Exception e) {LOGGER.warn("Failed to save bookmarks", e);}

        // Saves the current theme.
        try {ThemeManager.saveCurrentTheme();}
        catch(Exception e) {LOGGER.warn("Failed to save user theme", e);}

        // Saves the file associations.
        try {CommandManager.writeCommands();}
        catch(Exception e) {LOGGER.warn("Failed to save commands", e);}
        try {CommandManager.writeAssociations();}
        catch(Exception e) {LOGGER.warn("Failed to save associations", e);}
        
        // Saves the action keymap.
        try { ActionKeymapIO.saveActionKeymap(); }
        catch(Exception e) {LOGGER.warn("Failed to save action keymap", e);}
        
        // Saves the command bar.
        try { CommandBarIO.saveCommandBar(); }
        catch(Exception e) {LOGGER.warn("Failed to save command bar", e); }
        
        // Saves the tool bar.
        try { ToolBarIO.saveToolBar(); }
        catch(Exception e) {LOGGER.warn("Failed to save toolbar", e); }
        

        // Shutdown tasks should only be performed once
        shutdownTasksPerformed = true;
    }
}
