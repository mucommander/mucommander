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

package com.mucommander.command;

import com.mucommander.file.AbstractFile;

/**
 * Compiled shell commands.
 * <p>
 * A command is composed of three elements:
 * <ul>
 *   <li>An {@link #getAlias() alias}, used to identify the command through the application.</li>
 *   <li>A {@link #getCommand() command}, which is what will be executed by the instance of <code>Command</code>.</li>
 *   <li>
 *     A {@link #getType() type}, which can be any of {@link #SYSTEM_COMMAND system} (invisible and inmutable),
 *     {@link #INVISIBLE_COMMAND invisible} (invisible and mutable) or {@link #NORMAL_COMMAND} (visible and mutable).
 *   </li>
 * </ul>
 * </p>
 * <p>
 * Retrieving an instance of <code>Command</code> can be done in two main fashions: either through the {@link CommandParser},
 * which is used to create new commands, or through the {@link CommandManager}, which is used to retrieve commands that have already
 * been loaded.<br/>
 * Creating new instances through {@link CommandManager} is fairly simple:
 * <pre>
 * Command myCommand;
 *
 * myCommand = CommandParser.getCommand("Safari", "open -a Safari $f", Command.NORMAL_COMMAND);
 * </pre>
 * The {@link CommandManager} offers different ways of retrieving <code>Command</code> instances:
 * <pre>
 * Command myCommand;
 *
 * // Retrieves the command associated to URLs.
 * myCommand = CommandManager.getCommandForFile(FileFactory.getFile("http://mucommander.com"));
 *
 * // Retrieves the command associated to the specified alias.
 * myCommand = CommandManager.getCommandForAlias("Safari");
 *
 * // Runs through all commands known to the CommandManager.
 * Iterator commands;
 *
 * commands = CommandManager.commands();
 * while(commands.hasNext())
 *     System.out.println(((Command)commands.next()).getAlias());
 * </pre>
 * </p>
 * <p>
 * Once a <code>Command</code> instance has been retrieved, execution tokens can be retrieved through the
 * {@link #getTokens(AbstractFile)} method. This will return a tokenized version of the command and replace any
 * keyword by the corresponding file value (see {@link CommandParser} for syntax information). It's also possible
 * to skip keyword replacement through the {@link #getTokens()} method.
 * </p>
 * <p>
 * A command's executable tokens are typically meant to be used with {@link com.mucommander.process.ProcessRunner#execute(String[],AbstractFile)}
 * in order to generate instances of {@link com.mucommander.process.AbstractProcess}.
 * </p>
 * @author Nicolas Rinaudo
 * @see    CommandManager
 * @see    CommandParser
 * @see    com.mucommander.process.ProcessRunner
 * @see    com.mucommander.process.AbstractProcess
 */
public class Command {
    // - Type definitions ------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Describes <i>normal</i> commands. */
    public static final int NORMAL_COMMAND    = 0;
    /** Describes <i>system</i> commands. */
    public static final int SYSTEM_COMMAND    = 1;
    /** Describres <i>invisible</i> commands. */
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
    /** Name used to display the command to users. */
    private String    displayName;
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
    Command(String alias, String command, String[] tokens, boolean[] tokenTypes, int type, String displayName) {
        this.tokens      = tokens;
        this.tokenTypes  = tokenTypes;
        this.alias       = alias;
        this.command     = command;
        this.type        = type;
        this.displayName = displayName;
    }



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

    /**
     * Returns the command's tokens without performing keyword replacement.
     * @return the command's tokens.
     */
    public String[] getTokens() {
        String[] newTokens;

        // Copies the token to prevent modification of the command.
        newTokens = new String[tokens.length];
        System.arraycopy(tokens, 0, newTokens, 0, tokens.length);

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

    /**
     * Returns the command's type.
     * @return the command's type.
     */
    public int getType() {return type;}

    public String getDisplayName() {
        if(displayName == null)
            return alias;
        return displayName;
    }

    boolean isDisplayNameSet() {return displayName != null;}
}
