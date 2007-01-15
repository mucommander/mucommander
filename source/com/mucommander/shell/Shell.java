package com.mucommander.shell;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.conf.*;
import com.mucommander.file.AbstractFile;
import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessListener;
import com.mucommander.process.ProcessRunner;
import com.mucommander.command.CommandParser;

import java.io.IOException;

/**
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class Shell implements ConfigurationListener {
    // - Class variables -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Tokens that compose the shell command. */
    private static String[]              tokens;
    /** Instance of configuration listener. */
    private static ConfigurationListener confListener;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Initialises the shell.
     */
    static {
        ConfigurationManager.addConfigurationListener(confListener = new Shell());
        setShellCommand();
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
        if(Debug.ON) Debug.trace("Executing " + command);

        // Builds the shell command.
        tokens[tokens.length - 1] = command;

        // Adds the command to history.
        ShellHistoryManager.add(command);

        // Starts the process.
        if(listener == null)
            return ProcessRunner.execute(tokens, currentFolder);
        return ProcessRunner.execute(tokens, currentFolder, listener);
    }



    // - Configuration management --------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Extracts the shell command from configuration.
     */
    private static synchronized void setShellCommand() {
        String command; // Shell command.

        // Retrieves the configuration defined shell command.
        if(ConfigurationManager.getVariableBoolean(ConfigurationVariables.USE_CUSTOM_SHELL, ConfigurationVariables.DEFAULT_USE_CUSTOM_SHELL))
            command = ConfigurationManager.getVariable(ConfigurationVariables.CUSTOM_SHELL, PlatformManager.getDefaultShellCommand());
        else
            command = PlatformManager.getDefaultShellCommand();

        // Splits the command into tokens, leaving room for the argument.
        tokens = CommandParser.getTokensWithParams(command, 1);
    }

    /**
     * Reacts to configuration changes.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
        if(event.getVariable().startsWith(ConfigurationVariables.SHELL_SECTION))
            setShellCommand();
        return true;
    }
}
