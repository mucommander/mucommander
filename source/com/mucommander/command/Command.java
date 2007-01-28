package com.mucommander.command;

import com.mucommander.file.AbstractFile;

import java.io.File;

/**
 * Compiled file opening commands.
 * @author Nicolas Rinaudo
 */
public class Command {
    // - Type definitions ------------------------------------------------------
    // -------------------------------------------------------------------------
    public static final int NORMAL_COMMAND    = 0;
    public static final int SYSTEM_COMMAND    = 1;
    public static final int INVISIBLE_COMMAND = 2;



    // - Instance variables ----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Different tokens that compose the command. */
    private String[]  tokens;
    /** Information about potential keywords contained in the tokens. */
    private boolean[] tokenTypes;
    /** Command's alias. */
    private String    alias;
    /** Original command. */
    private String    command;
    /** Command type. */
    private int       type;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new command.
     * <p>
     * This is not the prefered way of creating instances of <code>Command</code>.
     * Developers should use either {@link CommandManager#getCommandForFile(AbstractFile)}
     * or {@link CommandParser#getCommand(String,String)} for that purpose.
     * </p>
     * @param alias      alias of the command.
     * @param command    original command (before tokenisation).
     * @param tokens     tokens that compose the command.
     * @param tokenTypes description of each token in <code>tokens</code>
     */
    Command(String alias, String command, String[] tokens, boolean[] tokenTypes, int type) {
        this.tokens     = tokens;
        this.tokenTypes = tokenTypes;
        this.alias      = alias;
        this.command    = command;
        this.type       = type;
    }

    public int getType() {return type;}


    // - Token retrieval -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the command's tokens, replacing any keyword by the values from <code>target</code>.
     * @param  target file from which to get the values by which to replace keywords.
     * @return        the command's tokens.
     */
    public String[] getTokens(AbstractFile target) {
        String[] newTokens; // Buffer for the final tokens.

        newTokens = new String[tokens.length];
        // Performs keyword replacement.
        for(int i = 0; i < tokens.length; i++) {
            if(tokenTypes[i])
                newTokens[i] = CommandParser.replaceKeywords(tokens[i], target);
            else
                newTokens[i] = tokens[i];
        }

        return newTokens;
    }



    // - Misc. -----------------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the original, un-tokenised command.
     * @return the original, un-tokenised command.
     */
    public String getCommand() {return command;}

    /**
     * Returns this command's alias.
     * @return this command's alias.
     */
    public String getAlias() {return alias;}
}
