package com.mucommander.process;

import com.mucommander.*;
import com.mucommander.file.*;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Used to run process in as safe a manner as possible.
 * <p>
 * The Java process API, while very simple, contains a lot of pitfalls and requires somem work to use properly.
 * Typical errors are forgetting to monitor a process' output streams, which will make it deadlock more often than not.
 * </p>
 * <p>
 * Using the <code>ProcessRunner</code> will take care of all these tasks, while still allowing most of the flexibility
 * of the standard API.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ProcessRunner {
    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Prevents instances of ProcessRunner from being created.
     */
    private ProcessRunner() {}



    // - Process running -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Executes the specified command in the specified directory.
     * <p>
     * Note that both <code>currentDirectory</code> and <code>listener</code> can be set to <code>null</code>.<br/>
     * If no current directory is specified, the VM's current directory will be used. Moreover, if the current directory
     * is not on a file system that supports process running, the user's home directory will be used instead.<br/>
     * If <code>listener</code> is set to <code>null</code>, nobody will be notified of the process' state. Its streams
     * will still be emptied to prevent deadlocks.
     * </p>
     * @param  tokens           tokens that compose the command to execute.
     * @param  currentDirectory directory in which to execute the process.
     * @param  listener         object that will be notified of modifications in the process' state.
     * @return                  the generated process.
     * @throws IOException      thrown if any error occurs while creating the process.
     */
    public static Process execute(String[] tokens, AbstractFile currentDirectory, ProcessListener listener) throws IOException {
        File workingDirectory; // Directory in which to execute the process.

        // Computes the right 'working directory'.
        if(currentDirectory == null)
            workingDirectory = null;
        else
            workingDirectory = new java.io.File((currentDirectory instanceof FSFile) ?
                                                currentDirectory.getAbsolutePath() :
                                                System.getProperty("user.home"));

        // Java 1.5+ has a convenient method for merging stdout and stderr, use it if available.
        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_5) {
            ProcessBuilder pb = new ProcessBuilder(tokens);
            // Set the process' working directory
            if(workingDirectory != null)
                pb.directory(workingDirectory);
            // Merge the process' stdout and stderr 
            pb.redirectErrorStream(true);

            return new MonitoredProcess(pb.start(), listener);
        }

        // Java 1.4 or below, use Runtime.exec() which separates stdout and stderr (harder to manipulate) 
        if(workingDirectory == null)
            return new MonitoredProcess(Runtime.getRuntime().exec(tokens), listener);
        return new MonitoredProcess(Runtime.getRuntime().exec(tokens, null, workingDirectory), listener);
    }



    // - Helper methods ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, null, null)</code>.
     * </p>
     * @param  command     command to execute.
     * @return             the generated process.
     * @see                #execute(String,AbstractFile,ProcessListener)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static Process execute(String command) throws IOException {return execute(command, null, null);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, null, listener)</code>.
     * </p>
     * @param  command     command to execute.
     * @param  listener    object that will be notified of any modification in the process' state.
     * @return             the generated process.
     * @see                #execute(String,AbstractFile,ProcessListener)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static Process execute(String command, ProcessListener listener) throws IOException {return execute(command, null, listener);}

    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, currentDirectory, null)</code>.
     * </p>
     * @param  command          command to execute.
     * @param  currentDirectory directory in which to run the command.
     * @return                  the generated process.
     * @see                     #execute(String,AbstractFile,ProcessListener)
     * @throws IOException      thrown if an error happens while starting the process.
     */
    public static Process execute(String command, AbstractFile currentDirectory) throws IOException {return execute(command, currentDirectory, null);}

    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, currentDirectory, null)</code> where <code>tokens</code>
     * is an array contains all the tokens found in <code>command</code>.
     * </p>
     * <p>
     * More precisely, the <code>command</code> string is broken into tokens using a <code>StringTokenizer</code> created by the call
     * <code>new StringTokenizer(command)</code> with no further modification of the character categories. The tokens produced by the
     *  tokenizer are then placed in the new string array <code>tokens</code>, in the same order.
     * </p>
     * @param  command          command to execute.
     * @param  currentDirectory directory in which to run the command.
     * @return                  the generated process.
     * @see                     #execute(String,AbstractFile,ProcessListener)
     * @throws IOException      thrown if an error happens while starting the process.
     */
    public static Process execute(String command, AbstractFile currentDirectory, ProcessListener listener) throws IOException {
        StringTokenizer parser; // Used to parse the command.
        String[]        tokens; // Tokens that make up the command.

        // Initialisation.
        parser = new StringTokenizer(command);
        tokens = new String[parser.countTokens()];

        // Breaks command into tokens.
        for(int i = 0; i < tokens.length; i++)
            tokens[i] = parser.nextToken();

        // Starts the process.
        return execute(tokens, currentDirectory, listener);
    }

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, null, null)</code>.
     * </p>
     * @param  tokens      command to execute.
     * @return             the generated process.
     * @see                #execute(String[],AbstractFile,ProcessListener)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static Process execute(String[] tokens) throws IOException {return execute(tokens, null, null);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, null, listener)</code>.
     * </p>
     * @param  tokens      command to execute.
     * @param  listener    object that will be notified of any modification in the process' state.
     * @return             the generated process.
     * @see                #execute(String[],AbstractFile,ProcessListener)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static Process execute(String[] tokens, ProcessListener listener) throws IOException {return execute(tokens, null, listener);}

    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, currentDirectory, null)</code>.
     * </p>
     * @param  tokens           command to execute.
     * @param  currentDirectory directory in which to run the command.
     * @return                  the generated process.
     * @see                     #execute(String[],AbstractFile,ProcessListener)
     * @throws IOException      thrown if an error happens while starting the process.
     */
    public static Process execute(String[] tokens, AbstractFile currentDirectory) throws IOException {return execute(tokens, currentDirectory, null);}
}
