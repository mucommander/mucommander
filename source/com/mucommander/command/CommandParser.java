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

import java.io.File;

/**
 * Class used to split commands into tokens.
 * <p>
 * The basic command syntax is fairly simple:
 * <ul>
 *  <li>Any non-escaped <code>\</code> character will escape the following character and be removed from the tokens.</li>
 *  <li>Any non-escaped <code>"</code> character will escape all characters until the next occurence of <code>"</code>, except for <code>\</code>.</li>
 *  <li>Non-escaped space characters are used as token separators.</li>
 * </ul>
 * It is important to remember that <code>"</code> characters are <b>not</b> removed from the resulting tokens.
 * </p>
 * <p>
 * It's also possible to include keywords in a command:
 * <ul>
 *  <li><code>$f</code> is replaced by a file's full path.</li>
 *  <li><code>$n</code> is replaced by a file's name.</li>
 *  <li><code>$e</code> is replaced by a file's extension.</li>
 *  <li><code>$N</code> is replaced by a file's name without its extension.</li>
 *  <li><code>$p</code> is replaced by a file's parent's path.</li>
 *  <li><code>$j</code> is replaced by the path of the folder in which the JVM was started.</li>
 * </ul>
 * Note that keywords are only meaningful for the {@link Command#getTokens(com.mucommander.file.AbstractFile)} method.
 * </p>
 * @author Nicolas Rinaudo
 * @see    Command
 */
public class CommandParser {
    // - Keywords --------------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Header of replacement keywords. */
    private static final char KEYWORD_HEADER                 = '$';
    /** Instances of this keyword will be replaced by the file's full path. */
    private static final char KEYWORD_PATH                   = 'f';
    /** Instances of this keyword will be replaced by the file's name. */
    private static final char KEYWORD_NAME                   = 'n';
    /** Instances of this keyword will be replaced by the file's parent directory. */
    private static final char KEYWORD_PARENT                 = 'p';
    /** Instances of this keyword will be replaced by the JVM's current directory. */
    private static final char KEYWORD_VM_PATH                = 'j';
    /** Instances of this keyword will be replaced by the file's extension. */
    private static final char KEYWORD_EXTENSION              = 'e';
    /** Instances of this keyword will be replaced by the file's name without its extension. */
    private static final char KEYWORD_NAME_WITHOUT_EXTENSION = 'N';



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Prevents instances of the parser from being created.
     */
    private CommandParser() {}



    // - Token retrieval -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the tokens that compose the specified command.
     * <p>
     * Note that this method doesn't do any keyword replacement. For that to happen,
     * you need to use the following code:
     * <pre>
     * String[] tokens = CommandParser.getCommand("my_command", command).getTokens(file);
     * </pre>
     * </p>
     * @param  command command to tokenize.
     * @return         the tokens that compose the command.
     */
    public static String[] getTokens(String command) {
        String[] tokens; // Stores the tokens that compose the command.
        String[] buffer; // Buffer for the parse method

        // Parses the command and initialises token to the right size.
        tokens = new String[parse(command, buffer = new String[command.length()], null)];

        // Copies the parsing output into tokens.
        System.arraycopy(buffer, 0, tokens, 0, tokens.length);

        return tokens;
    }

    /**
     * Returns the tokens that compose the specified command.
     * <p>
     * In some cases, the command that needs to be tokenized is not complete (such as,
     * for example, with the {@link com.mucommander.shell.Shell shell} command). This
     * method will allocate <code>paramLength</code> free slot at the end of the token
     * array to accomodate for such cases.
     * </p>
     * <p>
     * Note that this method doesn't do any keyword replacement. For that to happen,
     * you need to use the following code:
     * <pre>
     * String[] tokens = CommandParser.getCommand("my_command", command).getTokens(file);
     * </pre>
     * </p>
     * @param  command     command to tokenize.
     * @param  paramLength number of free slot that should be made available in the token array.
     * @return             the tokens that compose the command.
     */
    public static String[] getTokensWithParams(String command, int paramLength) {
        String[] tokens; // Stores the tokens that compose the command.
        String[] buffer; // Buffer for the parse method

        // Parses the command and initialises token to the right size.
        tokens = new String[parse(command, buffer = new String[command.length()], null) + paramLength];

        // Copies the parsing output into tokens.
        System.arraycopy(buffer, 0, tokens, 0, tokens.length - paramLength);

        return tokens;
    }

    /**
     * Returns a command build with the specified arguments.
     * @param  alias   alias of the command to create.
     * @param  command command line to run when the command is being executed.
     * @param  type    type of the command (one of {@link Command#NORMAL_COMMAND}, {@link Command#SYSTEM_COMMAND} or {@link Command#INVISIBLE_COMMAND}.
     * @return         a new command matching the specified description.
     */
    public static Command getCommand(String alias, String command, int type, String displayName) {
        String[]  tokenBuffer; // Buffer for the tokens that compose command.
        boolean[] typeBuffer;  // Buffer for the type of tokens that compose command.
        String[]  tokens;      // Actual tokens.
        boolean[] tokenTypes;  // Actual types.

        // Parses the command and grabs the proper buffer sizes.
        tokens      = new String[parse(command, tokenBuffer = new String[command.length()], typeBuffer = new boolean[command.length()])];
        tokenTypes  = new boolean[tokens.length];

        // Stores the tokens and token types.
        for(int i = 0; i < tokens.length; i++) {
            tokens[i]      = tokenBuffer[i];
            tokenTypes[i]  = typeBuffer[i];
        }

        // Creates and returns a new command.
        return new Command(alias, command, tokens, tokenTypes, type, displayName);
    }

    public static Command getCommand(String alias, String command, int type) {return getCommand(alias, command, type, null);}

    public static Command getCommand(String alias, String command, String displayName) {return getCommand(alias, command, Command.NORMAL_COMMAND, displayName);}

    /**
     * Returns a command built from the specified arguments.
     * <p>
     * This is a convenience method and is equivalent to calling <code>getCommand(alias, command, Command.NORMAL_COMMAND)</code>.
     * </p>
     * @param  alias   alias of the command to create.
     * @param  command command line to execute when this command is called.
     * @return a command built from the specified arguments.
     */
    public static Command getCommand(String alias, String command) {return getCommand(alias, command, Command.NORMAL_COMMAND, null);}



    // - Command parsing -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Parses the specified command and stores the result in <code>tokens</code>.
     * <p>
     * The <code>tokenTypes</code> argument is optional, and can be set to <code>null</code>.
     * If not set to null, it will contain instructions about whether a keyword is declared in
     * the token of the same index.<br/>
     * For example, if <code>tokenTypes[i] == true</code>, then <code>tokens[i]</code> contains a keyword.
     * </p>
     * <p>
     * This method is very lax about syntax. Errors are just ignored, and a best effort will be
     * made to make sense of the rest of the command.
     * </p>
     * <p>
     * When not sure about how many tokens compose the command - which should be pretty much
     * all the time, it is advised to allocate <code>command.length()</code> big arrays.
     * While not very memory efficient, this is the only way to ensure success. This is an implementation
     * design: growing the size of the buffer to fit the actual number of tokens is resource consuming,
     * both in terms of CPU and memory. The 'use the maximum amount of memory that makes sense' is the
     * lesser of two evils.
     * </p>
     * @param  command    command to parse.
     * @param  tokens     where to store the tokens that compose the command.
     * @param  tokenTypes optional array in which to store information about potential keywords.
     * @return            the number of tokens that were found.
     */
    private static int parse(String command, String[] tokens, boolean[] tokenTypes) {
        char[]       buffer;        // All the characters that compose command.
        int          tokenIndex;    // Index of the current token in tokens[];
        StringBuffer currentToken;  // Buffer for the current token.
        boolean      isInQuotes;    // Whether we're currently within quotes or not.

        // Initialises parsing.
        command      = command.trim();
        currentToken = new StringBuffer(command.length());
        tokenIndex   = 0;
        buffer       = command.toCharArray();
        isInQuotes   = false;

        // Parses the command.
        for(int i = 0; i < command.length(); i++) {
            // Quote escaping: toggle isInQuotes.
            if(buffer[i] == '\"') {
                currentToken.append(buffer[i]);
                isInQuotes = !isInQuotes;
            }

            // Backslash escaping: the next character is not analyzed.
            else if(buffer[i] == '\\') {
                if(i + 1 != command.length())
                    currentToken.append(buffer[++i]);
            }

            // Whitespace: end of token if we're not between quotes.
            else if(buffer[i] == ' ' && !isInQuotes) {
                // Skips un-escaped blocks of spaces.
                while(i + 1 < command.length() && buffer[i + 1] == ' ')
                    i++;

                // Stores the current token.
                tokens[tokenIndex] = currentToken.toString();
                tokenIndex++;

                // Resets the token buffer.
                currentToken.setLength(0);
            }

            // Keyword, gather token type information if necessary.
            else if(buffer[i] == KEYWORD_HEADER) {
                currentToken.append(KEYWORD_HEADER);

                // Makes sure $ is not the last character in the command.
                // If it is, politely pretends nothing wrong happened and treat it
                // as a normal character.
                if(++i != command.length()) {

                    // If we're interested in identifying keywords, check whether we're
                    // actually dealing with one and mark it if necessary.
                    if(tokenTypes != null)
                        if(buffer[i] == KEYWORD_PATH || buffer[i] == KEYWORD_NAME ||
                           buffer[i] == KEYWORD_PARENT || buffer[i] == KEYWORD_VM_PATH ||
                           buffer[i] == KEYWORD_EXTENSION || buffer[i] == KEYWORD_NAME_WITHOUT_EXTENSION)
                            tokenTypes[tokenIndex] = true;

                    currentToken.append(buffer[i]);
                }
            }

            // Nothing special about this character.
            else
                currentToken.append(buffer[i]);
        }

        // Adds a possible last token.
        if(currentToken.length() != 0) {
            tokens[tokenIndex] = currentToken.toString();
            return tokenIndex + 1;
        }

        return tokenIndex;
    }

    /**
     * Replaces keywords in the specified token, using the specified file when it needs to compute a value.
     * <p>
     * Parsing here is quite lenient: when a syntax error is found, it is politely ignored and a best effort
     * is made to analyze the rest of the token.
     * </p>
     * @param  token  token in which to replace keywords.
     * @param  target where to take keyword replacement values from.
     * @return        a string in which all instances of keywords have been replaced by values from <code>target</code>.
     */
    static String replaceKeywords(String token, AbstractFile target) {
        StringBuffer buffer; // Buffer for the final token.
        char[]       chars;  // Token as a char array.

        buffer = new StringBuffer();
        chars  = token.toCharArray();

        // Goes through every character in the token.
        for(int i = 0; i < chars.length; i++) {

            // We've found a keyword header.
            if(chars[i] == CommandParser.KEYWORD_HEADER) {
                // Makes sure this is not the last character in the string,
                // then replace the keyword if necessary.
                if(i + 1 < chars.length) {
                    if(chars[i + 1] == CommandParser.KEYWORD_PATH) {
                        buffer.append(target.getAbsolutePath());
                        i++;
                    }
                    else if(chars[i + 1] == CommandParser.KEYWORD_NAME) {
                        buffer.append(target.getName());
                        i++;
                    }
                    else if(chars[i + 1] == CommandParser.KEYWORD_PARENT) {
                        buffer.append(target.getParent());
                        i++;
                    }
                    else if(chars[i + 1] == CommandParser.KEYWORD_VM_PATH) {
                        buffer.append(new File(System.getProperty("user.dir")).getAbsolutePath());
                        i++;
                    }
                    else if(chars[i + 1] == CommandParser.KEYWORD_EXTENSION) {
                        buffer.append(target.getExtension());
                        i++;
                    }
                    else if(chars[i + 1] == CommandParser.KEYWORD_NAME_WITHOUT_EXTENSION) {
                        buffer.append(target.getNameWithoutExtension());
                        i++;
                    }
                    // Not a legal keyword, append $ to the token.
                    else
                        buffer.append(chars[i]);
                }
                // Not a keyword (this is the last character in the token).
                else
                    buffer.append(chars[i]);
            }
            else
                buffer.append(chars[i]);
        }

        return buffer.toString();
    }
}
