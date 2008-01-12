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

package com.mucommander.shell;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.command.Command;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessListener;
import com.mucommander.process.ProcessListenerList;
import com.mucommander.process.ProcessRunner;

import java.io.IOException;

/**
 * Used to execute shell commands.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class Shell implements ConfigurationListener {
    // - Class variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Encoding used by the shell. */
    private static String   encoding;
    /** Whether encoding should be auto-detected or not. */
    private static boolean  autoDetectEncoding;
    /** Tokens that compose the shell command. */
    private static String[] tokens;
    /** Tokens that compose remote shell commands. */
    private static String[] remoteTokens;
    /** Instance of configuration listener. */
    private static Shell    confListener;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Initialises the shell.
     */
    static {
        MuConfiguration.addConfigurationListener(confListener = new Shell());

        // This could in theory also be written without the confListener reference.
        // It turns out, however, that proGuard is a bit too keen when removing fields
        // he thinks are not used. This code is written that way to make sure
        // confListener is not taken out, and the ConfigurationListener instance removed
        // instantly as there is only a WeakReference on it.
        // The things we have to do...
        confListener.setShellCommand();

        remoteTokens = new String[1];
    }

    /**
     * Prevents instances of Shell from being created.
     */
    private Shell() {}



    // - Shell interaction ---------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Executes the specified command in the specified folder.
     * <p>
     * The <code>currentFolder</code> folder parameter will only be used if it's neither a
     * remote directory nor an archive. Otherwise, the command will run from the user's
     * home directory.
     * </p>
     * @param     command       command to run.
     * @param     currentFolder where to run the command from.
     * @return                  the resulting process.
     * @exception IOException   thrown if any error occurs while trying to run the command.
     */
    public static AbstractProcess execute(String command, AbstractFile currentFolder) throws IOException {return execute(command, currentFolder, null);}

    /**
     * Executes the specified command in the specified folder.
     * <p>
     * The <code>currentFolder</code> folder parameter will only be used if it's neither a
     * remote directory nor an archive. Otherwise, the command will run from the user's
     * home directory.
     * </p>
     * <p>
     * Information about the resulting process will be sent to the specified <code>listener</code>.
     * </p>
     * @param     command       command to run.
     * @param     currentFolder where to run the command from.
     * @param     listener      where to send information about the resulting process.
     * @return                  the resulting process.
     * @exception IOException   thrown if any error occurs while trying to run the command.
     */
    public static synchronized AbstractProcess execute(String command, AbstractFile currentFolder, ProcessListener listener) throws IOException {
        String[] commandTokens;

        if(Debug.ON) Debug.trace("Executing " + command);

        // Adds the command to history.
        ShellHistoryManager.add(command);

        // Builds the shell command.
        // Local files use the configuration defined shell. Remote files
        // will execute the command as-is.
        if(currentFolder instanceof LocalFile || !currentFolder.canRunProcess()) {
            tokens[tokens.length - 1] = command;
            commandTokens             = tokens;
        }
        else {
            remoteTokens[0] = command;
            commandTokens   = remoteTokens;
        }

        // Starts the process.
        if(autoDetectEncoding) {
            if(listener == null)
                listener = new ShellEncodingListener();
            else {
                ProcessListenerList listeners;

                listeners = new ProcessListenerList();
                listeners.add(listener);
                listeners.add(new ShellEncodingListener());
                listener = listeners;
            }
        }
        return (encoding == null) ? ProcessRunner.execute(commandTokens, currentFolder, listener) : ProcessRunner.execute(commandTokens, currentFolder, listener, encoding);
    }



    // - Configuration management --------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Extracts the shell command from configuration.
     */
    private static synchronized void setShellCommand() {
        String   shellCommand;
        String[] buffer;

        // Retrieves the configuration defined shell command.
        if(MuConfiguration.getVariable(MuConfiguration.USE_CUSTOM_SHELL, MuConfiguration.DEFAULT_USE_CUSTOM_SHELL))
            shellCommand = MuConfiguration.getVariable(MuConfiguration.CUSTOM_SHELL, PlatformManager.getDefaultShellCommand());
        else
            shellCommand = PlatformManager.getDefaultShellCommand();

        // Splits the command into tokens, leaving room for the argument.
        buffer = Command.getTokens(shellCommand);
        tokens = new String[buffer.length + 1];
        System.arraycopy(buffer, 0, tokens, 0, buffer.length);

        // Retrieves encoding configuration.
        encoding           = MuConfiguration.getVariable(MuConfiguration.SHELL_ENCODING);
        autoDetectEncoding = MuConfiguration.getVariable(MuConfiguration.AUTODETECT_SHELL_ENCODING, MuConfiguration.DEFAULT_AUTODETECT_SHELL_ENCODING);
    }

    /**
     * Reacts to configuration changes.
     */
    public void configurationChanged(ConfigurationEvent event) {
        if(event.getVariable().startsWith(MuConfiguration.SHELL_SECTION))
            setShellCommand();
    }
}
