package com.mucommander.shell;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.file.*;
import com.mucommander.*;

import java.util.*;
import java.io.*;

/**
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class Shell {
    // - Shell configuration -------------------------------------------------
    // -----------------------------------------------------------------------
    /** Configuration variable used to store the custom shell command */
    private final static String CUSTOM_SHELL_CONF_VAR     = "prefs.shell.custom_command";

    /** Configuration variable used to store the custom shell command */
    private final static String USE_CUSTOM_SHELL_CONF_VAR = "prefs.shell.use_custom";



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Prevents instances of Shell from being created.
     */
    private Shell() {}



    // - Shell interaction ---------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Returns the shell command muCommander uses.
     * <p>
     * This can be either the system's {@link com.mucommander.PlatformManager#getDefaultShellCommand() default} or what the
     * user defined in his preferences.
     * </p>
     * @return the shell command muCommander uses.
     */
    private static String getShellCommand() {
        if(ConfigurationManager.getVariableBoolean(USE_CUSTOM_SHELL_CONF_VAR, false))
            return ConfigurationManager.getVariable(CUSTOM_SHELL_CONF_VAR, PlatformManager.getDefaultShellCommand());
        return PlatformManager.getDefaultShellCommand();
    }

    /**
     * Executes the specified command in the specified folder.
     * <p>
     * The command will be executed within a shell as returned by {@link com.mucommander.PlatformManager#getShellCommand()}.
     * </p>
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
    public static Process execute(String command, AbstractFile currentFolder) throws IOException {
        if(Debug.ON) Debug.trace("Executing " + command);

        // Stores the command as a vector.
        Vector commandTokens = splitCommand(getShellCommand());
        commandTokens.add(command);
        
        // Determine if specified folder can be used as a working directory
        File workingDirectory = new java.io.File((currentFolder instanceof FSFile) ?
            currentFolder.getAbsolutePath() :
            System.getProperty("user.home"));

        // Adds the command to history.
        ShellHistoryManager.add(command);

        // Under Java 1.5 and up, use ProcessBuilder to merge the created process's output and error streams.
        // The benefit of doing so is that error messages will be displayed in the context of the normal process' output
        // (mixed), whereas otherwise error messages are displayed after all normal output has been read and displayed.
        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_5) {
            ProcessBuilder pb = new ProcessBuilder(commandTokens);
            // Set the process' working directory
            pb.directory(workingDirectory);
            // Merge the process' stdout and stderr 
            pb.redirectErrorStream(true);

            return pb.start();
        }
        // Java 1.4 or below, use Runtime.exec() which separates stdout and stderr (harder to manipulate) 
        else {
            // Stores the tokens in an array for Runtime.exec(String[],String[],File).
            String tokens[] = new String[commandTokens.size()];
            commandTokens.toArray(tokens);

            return Runtime.getRuntime().exec(tokens, null, workingDirectory);
        }
    }



    // - Misc. ---------------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Splits the specified command into a vector of tokens.
     * <p>
     * This method tries to be about its parsing, meaning that it will
     * escape what it thinks should be escaped.<br/>
     * Any <code>\</code> character will be understood to mean that the following
     * character will not be parsed, but added to the current token as is.<br/>
     * Any <code>"</code> character will be understood to mean that all following
     * characters until the next <code>"</code> should be added to the current token.<br/>
     * Any un-escaped whitespace character will mark the end of the current token.
     * </p>
     * <p>
     * Note that while this should be sufficient for most cases, this parsing has limitations.
     * Since <code>"</code> has priority over <code>\</code>, it's impossible to encapsulate
     * a <code>"</code> character within an escaped block.
     * </p>
     * @param  command the command to split.
     * @return         the tokens that compose the specified command.
     */
    private static Vector splitCommand(String command) {
        int          length;
        char         c;
        StringBuffer token;
        Vector       tokens;
        String       value;


        length = command.length();
        token  = new StringBuffer();
        tokens = new Vector();

        for(int i = 0; i < length; i++) {
            c = command.charAt(i);
            // Escape the next character.
            if(c == '\\') {
                // Ignores trailing \ characters.
                if(++i >= length)
                    break;
                token.append(command.charAt(i));
            }

            // Ignores escaping until matching " is found.
            else if(c == '\"') {
                while(++i < length) {
                    c = command.charAt(i);
                    if(c == '"') {
                        i++;
                        break;
                    }
                    else
                        token.append(c);
                }
                if(i >= length)
                    break;
            }

            // End of token.
            else if(Character.isWhitespace(c)) {
                value = token.toString().trim();
                if(value.length() != 0)
                    tokens.add(token.toString().trim());
                token.setLength(0);
            }

            // Regular character.
            else
                token.append(c);
        }
        // Makes sure that trailing tokens are not ignored.
        value = token.toString().trim();
        if(value.length() != 0)
            tokens.add(value);

        return tokens;
    }
}
