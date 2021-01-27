/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.process;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.protocol.local.LocalFile;

import java.io.IOException;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.CompletionStage;

/**
 * Used to run process in as safe a manner as possible.
 * <p>
 * The Java process API, while very simple, contains a lot of pitfalls and requires some work to use properly.
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
     * Note that both <code>currentDirectory</code> and <code>listener</code> can be set to <code>null</code>.<br>
     * If no current directory is specified, the VM's current directory will be used. Moreover, if the current directory
     * is not on residing on local file system, the user's home directory will be used instead. Finally, if the current
     * directory is on a local file system but is actually not a {@link AbstractFile#isDirectory() directory}
     * (an archive entry for instance), the first file's parent that is an actual directory will be used.
     * <br>
     * If <code>listener</code> is set to <code>null</code>, nobody will be notified of the process' state. Its streams
     * will still be emptied to prevent deadlocks.
     * </p>
     * @param  tokens           tokens that compose the command to execute.
     * @param  currentDirectory directory in which to execute the process (user directory if <code>null</code>).
     * @param  listener         object that will be notified of modifications in the process' state (ignored if <code>null</code>).
     * @param  encoding         encoding used to read from the process' stream (system default is used if <code>null</code>).
     * @return                  the generated process.
     * @throws IOException      thrown if any error occurs while creating the process.
     */
    public static AbstractProcess execute(String[] tokens, AbstractFile currentDirectory, ProcessListener listener, String encoding) throws IOException {
        AbstractProcess process;

        // If currentDirectory is null, use the VM's current directory.
        if(currentDirectory == null) {
            currentDirectory = FileFactory.getFile(System.getProperty("user.dir"), true);
        }
        else {
            // If currentDirectory is not on a local filesytem, use the user's home.
            if(!currentDirectory.hasAncestor(LocalFile.class)) {
                currentDirectory = FileFactory.getFile(System.getProperty("user.home"), true);
            }
            // If currentDirectory is not a directory (e.g. an archive entry)
            else {
                while(currentDirectory!=null && !currentDirectory.isDirectory())
                    currentDirectory = currentDirectory.getParent();

                // This shouldn't normally happen
                if(currentDirectory==null)
                    currentDirectory = FileFactory.getFile(System.getProperty("user.dir"), true);
            }
        }

//        // Register a debug process listener.
//        if(listener == null)
//            listener = new DebugProcessListener(tokens);

        // Starts the process.
        process = new LocalProcess(tokens, (java.io.File)currentDirectory.getUnderlyingFileObject());
        process.startMonitoring(listener, encoding);

        return process;
    }



    // - Helper methods ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, currentDirectory, listener, null)</code>.
     * </p>
     * @param  command          command to execute.
     * @param  currentDirectory directory in which to execute the process (user directory if <code>null</code>).
     * @param  listener         object that will be notified of modifications in the process' state (ignored if <code>null</code>).
     * @return                  the generated process.
     * @throws IOException      thrown if any error occurs while creating the process.
     */
    public static AbstractProcess execute(String command, AbstractFile currentDirectory, ProcessListener listener) throws IOException {return execute(command, currentDirectory, listener, null);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, null, null, null)</code>.
     * </p>
     * @param  command     command to execute.
     * @return             the generated process.
     * @see                #execute(String,AbstractFile,ProcessListener,String)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String command) throws IOException {return execute(command, null, null, null);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, null, null, encoding)</code>.
     * </p>
     * @param  command     command to execute.
     * @param  encoding    encoding used to read from the process' stream (system default is used if <code>null</code>).
     * @return             the generated process.
     * @see                #execute(String,AbstractFile,ProcessListener,String)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String command, String encoding) throws IOException {return execute(command, null, null, encoding);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, null, listener, null)</code>.
     * </p>
     * @param  command     command to execute.
     * @param  listener    object that will be notified of any modification in the process' state (ignored if <code>null</code>).
     * @return             the generated process.
     * @see                #execute(String,AbstractFile,ProcessListener,String)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String command, ProcessListener listener) throws IOException {return execute(command, null, listener, null);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, null, listener, encoding)</code>.
     * </p>
     * @param  command     command to execute.
     * @param  listener    object that will be notified of any modification in the process' state.
     * @param  encoding    encoding used to read from the process' stream (system default is used if <code>null</code>).
     * @return             the generated process.
     * @see                #execute(String,AbstractFile,ProcessListener,String)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String command, ProcessListener listener, String encoding) throws IOException {return execute(command, null, listener, encoding);}

    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, currentDirectory, null, null)</code>.
     * </p>
     * @param  command          command to execute.
     * @param  currentDirectory directory in which to run the command.
     * @return                  the generated process.
     * @see                     #execute(String,AbstractFile,ProcessListener,String)
     * @throws IOException      thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String command, AbstractFile currentDirectory) throws IOException {return execute(command, currentDirectory, null, null);}

    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(command, currentDirectory, null, encoding)</code>.
     * </p>
     * @param  command          command to execute.
     * @param  currentDirectory directory in which to run the command (uses the VM's current directory if <code>null</code>).
     * @param  encoding         encoding used to read from the process' stream (system default is used if <code>null</code>).
     * @return                  the generated process.
     * @see                     #execute(String,AbstractFile,ProcessListener,String)
     * @throws IOException      thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String command, AbstractFile currentDirectory, String encoding) throws IOException {return execute(command, currentDirectory, null, encoding);}

    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, currentDirectory, null, encoding)</code> where <code>tokens</code>
     * is an array contains all the tokens found in <code>command</code>.
     * </p>
     * <p>
     * More precisely, the <code>command</code> string is broken into tokens using a <code>StringTokenizer</code> created by the call
     * <code>new StringTokenizer(command)</code> with no further modification of the character categories. The tokens produced by the
     *  tokenizer are then placed in the new string array <code>tokens</code>, in the same order.
     * </p>
     * @param  command          command to execute.
     * @param  currentDirectory directory in which to run the command (uses the VM's current directory if <code>null</code>).
     * @param  encoding         encoding used to read from the process' stream (system default is used if <code>null</code>).
     * @param  listener         object that will be notified of modifications in the process' state (ignored if <code>null</code>).
     * @return                  the generated process.
     * @throws IOException      thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String command, AbstractFile currentDirectory, ProcessListener listener, String encoding) throws IOException {
        StringTokenizer parser; // Used to parse the command.
        String[]        tokens; // Tokens that make up the command.

        // Initialisation.
        parser = new StringTokenizer(command);
        tokens = new String[parser.countTokens()];

        // Breaks command into tokens.
        for(int i = 0; i < tokens.length; i++)
            tokens[i] = parser.nextToken();

        // Starts the process.
        return execute(tokens, currentDirectory, listener, encoding);
    }

    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, currentDirectory, listener, null)</code>.
     * </p>
     * @param  tokens           command to execute.
     * @param  currentDirectory directory in which to run the command (uses the VM's current directory if <code>null</code>).
     * @param  listener         object that will be notified of any modification in the process' state.
     * @return                  the generated process.
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String[] tokens, AbstractFile currentDirectory, ProcessListener listener) throws IOException {return execute(tokens, currentDirectory, listener, null);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, null, null, null)</code>.
     * </p>
     * @param  tokens      command to execute.
     * @return             the generated process.
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String[] tokens) throws IOException {return execute(tokens, null, null, null);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, null, null, encoding)</code>.
     * </p>
     * @param  tokens      command to execute.
     * @param  encoding    encoding used to read from the process' stream (system default is used if <code>null</code>).
     * @return             the generated process.
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String[] tokens, String encoding) throws IOException {return execute(tokens, null, null, encoding);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, null, listener, null)</code>.
     * </p>
     * @param  tokens      command to execute.
     * @param  listener    object that will be notified of any modification in the process' state (ignored if <code>null</code>).
     * @return             the generated process.
     * @see                #execute(String[],AbstractFile,ProcessListener,String)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String[] tokens, ProcessListener listener) throws IOException {return execute(tokens, null, listener, null);}

    /**
     * Executes the specified command in the VM's current directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, null, listener, encoding)</code>.
     * </p>
     * @param  tokens      command to execute.
     * @param  listener    object that will be notified of any modification in the process' state (ignored if <code>null</code>).
     * @param  encoding    encoding used to read from the process' stream (system default is used if <code>null</code>).
     * @return             the generated process.
     * @see                #execute(String[],AbstractFile,ProcessListener,String)
     * @throws IOException thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String[] tokens, ProcessListener listener, String encoding) throws IOException {return execute(tokens, null, listener, encoding);}

    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, currentDirectory, null, null)</code>.
     * </p>
     * @param  tokens           command to execute.
     * @param  currentDirectory directory in which to run the command.
     * @return                  the generated process.
     * @see                     #execute(String[],AbstractFile,ProcessListener,String)
     * @throws IOException      thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String[] tokens, AbstractFile currentDirectory) throws IOException {return execute(tokens, currentDirectory, null, null);}

    /**
     * Executes the specified command in the specified directory asynchronously with the ability to perform an
     * action in case of an error.
     * @param  tokens           command to execute.
     * @param  currentDirectory directory in which to run the command.
     * @return                  the {@link CompletionStage} allowing to receive asynchronously the output messages in case of error if any.
     * @see                     #execute(String[],AbstractFile,ProcessListener,String)
     * @throws IOException      thrown if an error happens while starting the process.
     */
    public static CompletionStage<Optional<String>> executeAsync(String[] tokens, AbstractFile currentDirectory) throws IOException {
        final AsyncProcessListener listener = new AsyncProcessListener();
        execute(tokens, currentDirectory, listener, null);
        return listener.toCompletionStage();
    }

    /**
     * Executes the specified command in the specified directory.
     * <p>
     * This is a convenience method and behaves exactly as a call to <code>execute(tokens, currentDirectory, null, null)</code>.
     * </p>
     * @param  tokens           command to execute.
     * @param  currentDirectory directory in which to run the command.
     * @param  encoding         encoding used to read from the process' stream (system default is used if <code>null</code>).
     * @return                  the generated process.
     * @see                     #execute(String[],AbstractFile,ProcessListener,String)
     * @throws IOException      thrown if an error happens while starting the process.
     */
    public static AbstractProcess execute(String[] tokens, AbstractFile currentDirectory, String encoding) throws IOException {return execute(tokens, currentDirectory, null, encoding);}
}
