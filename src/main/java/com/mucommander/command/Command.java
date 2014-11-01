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

package com.mucommander.command;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;

import java.io.File;
import java.util.List;
import java.util.Vector;

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
 * </p>
 * <p>
 * Once a <code>Command</code> instance has been retrieved, execution tokens can be retrieved through the
 * {@link #getTokens(AbstractFile)} method. This will return a tokenized version of the command and replace any
 * keyword by the corresponding file value . It's also possible to skip keyword replacement through the {@link #getTokens()} method.
 * </p>
 * <p>
 * A command's executable tokens are typically meant to be used with {@link com.mucommander.process.ProcessRunner#execute(String[],AbstractFile)}
 * in order to generate instances of {@link com.mucommander.process.AbstractProcess}.
 * </p>
 * @author Nicolas Rinaudo
 * @see    CommandManager
 * @see    com.mucommander.process.ProcessRunner
 * @see    com.mucommander.process.AbstractProcess
 */
public class Command implements Comparable<Command> {
    // - Keywords ------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Header of replacement keywords. */
    private static final char KEYWORD_HEADER                      = '$';
    /** Instances of this keyword will be replaced by the file's full path. */
    private static final char KEYWORD_PATH                        = 'f';
    /** Instances of this keyword will be replaced by the file's name. */
    private static final char KEYWORD_NAME                        = 'n';
    /** Instances of this keyword will be replaced by the file's parent directory. */
    private static final char KEYWORD_PARENT                      = 'p';
    /** Instances of this keyword will be replaced by the JVM's current directory. */
    private static final char KEYWORD_VM_PATH                     = 'j';
    /** Instances of this keyword will be replaced by the file's extension. */
    private static final char KEYWORD_EXTENSION                   = 'e';
    /** Instances of this keyword will be replaced by the file's name without its extension. */
    private static final char KEYWORD_NAME_WITHOUT_EXTENSION      = 'b';



    // - Instance variables --------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Command's alias. */
    private final String      alias;
    /** Original command. */
    private final String      command;
    /** Name used to display the command to users. */
    private final String      displayName;
    /** Command type. */
    private final CommandType type;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new command.
     * @param alias       alias of the command.
     * @param command command that will be executed.
     * @param type        type of the command.
     * @param displayName name of the command as seen by users (if <code>null</code>, defaults to <code>alias</code>).
     */
    public Command(String alias, String command, CommandType type, String displayName) {
        this.alias       = alias;
        this.type        = type;
        this.displayName = displayName;
        this.command     = command;
    }

    /**
     * Creates a new command.
     * <p>
     * This is a convenience constructor and is strictly equivalent to calling
     * <code>{@link #Command(String,String,int,String) Command(}alias, command, {@link #NORMAL_COMMAND}, null)</code>.
     * </p>
     * @param alias   alias of the command.
     * @param command command that will be executed.
     */
    public Command(String alias, String command) {
    	this(alias, command, CommandType.NORMAL_COMMAND, null);
    }

    /**
     * Creates a new command.
     * <p>
     * This is a convenience constructor and is strictly equivalent to calling
     * <code>{@link #Command(String,String,int,String) Command(}alias, command, type, null)</code>.
     * </p>
     * @param alias   alias of the command.
     * @param command command that will be executed.
     * @param type    type of the command.
     */
    public Command(String alias, String command, CommandType type) {
    	this(alias, command, type, null);
    }



    // - Token retrieval -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns this command's tokens without performing keyword substitution.
     * @return this command's tokens without performing keyword substitution.
     */
    public synchronized String[] getTokens() {
    	return getTokens(command, (AbstractFile[])null);
    }

    /**
     * Returns this command's tokens, replacing keywords by the corresponding values from the specified file.
     * @param  file file from which to retrieve keyword substitution values.
     * @return      this command's tokens, replacing keywords by the corresponding values from the specified file.
     */
    public synchronized String[] getTokens(AbstractFile file) {
    	return getTokens(command, file);
    }

    /**
     * Returns this command's tokens, replacing keywords by the corresponding values from the specified fileset.
     * @param  files files from which to retrieve keyword substitution values.
     * @return       this command's tokens, replacing keywords by the corresponding values from the specified fileset.
     */
    public synchronized String[] getTokens(FileSet files) {
    	return getTokens(command, files);
    }

    /**
     * Returns this command's tokens, replacing keywords by the corresponding values from the specified files.
     * @param  files files from which to retrieve keyword substitution values.
     * @return       this command's tokens, replacing keywords by the corresponding values from the specified files.
     */
    public synchronized String[] getTokens(AbstractFile[] files) {
    	return getTokens(command, files);
    }

    /**
     * Returns the specified command's tokens without performing keyword substitution.
     * @param  command command to tokenize.
     * @return         the specified command's tokens without performing keyword substitution.
     */
    public static String[] getTokens(String command) {
    	return getTokens(command, (AbstractFile[])null);
    }

    /**
     * Returns the specified command's tokens after replacing keywords by the corresponding values from the specified file.
     * @param  command command to tokenize.
     * @param  file    file from which to retrieve keyword substitution values.
     * @return         the specified command's tokens after replacing keywords by the corresponding values from the specified file.
     */
    public static String[] getTokens(String command, AbstractFile file) {
    	return getTokens(command, new AbstractFile[] {file});
    }

    /**
     * Returns the specified command's tokens after replacing keywords by the corresponding values from the specified fileset.
     * @param  command command to tokenize.
     * @param  files   file from which to retrieve keyword substitution values.
     * @return         the specified command's tokens after replacing keywords by the corresponding values from the specified fileset.
     */
    public static String[] getTokens(String command, FileSet files) {
    	return getTokens(command, files.toArray(new AbstractFile[files.size()]));
    }

    /**
     * Returns the specified command's tokens after replacing keywords by the corresponding values from the specified files.
     * @param  command command to tokenize.
     * @param  files   file from which to retrieve keyword substitution values.
     * @return         the specified command's tokens after replacing keywords by the corresponding values from the specified files.
     */
    public static String[] getTokens(String command, AbstractFile[] files) {
        List<String>  tokens;        // All tokens.
        char[]        buffer;        // All the characters that compose command.
        StringBuilder currentToken;  // Buffer for the current token.
        boolean       isInQuotes;    // Whether we're currently within quotes or not.

        // Initialises parsing.
        tokens       = new Vector<String>();
        command      = command.trim();
        currentToken = new StringBuilder(command.length());
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
                tokens.add(currentToken.toString());
                currentToken.setLength(0);
            }

            // Keyword: perform keyword substitution.
            else if(buffer[i] == KEYWORD_HEADER) {
                // Skips keyword replacement if we're not interested
                // in it.
                if(files == null)
                    currentToken.append(KEYWORD_HEADER);

                // If this is the last character, append it.
                else if(++i == buffer.length)
                    currentToken.append(KEYWORD_HEADER);

                // If we've found a legal keyword, perform keyword replacement
                else if(isLegalKeyword(buffer[i])) {
                    // Deals with the first file.
                    currentToken.append(getKeywordReplacement(buffer[i], files[0]));

                    // $j is a special case, we only ever insert it once.
                    if(buffer[i] != KEYWORD_VM_PATH) {
                        // If we're not between quotes and there's more than one file,
                        // each file will be in its own token.
                        if(!isInQuotes && files.length != 1) {
                            tokens.add(currentToken.toString());
                            currentToken.setLength(0);
                        }

                        // Deals with all subsequent files:
                        // - if we're in quotes, separates each files by a space.
                        // - if we're not in quotes, each new file is its own token.
                        for(int j = 1; j < files.length; j++) {
                            if(isInQuotes) {
                                currentToken.append(' ');
                                currentToken.append(getKeywordReplacement(buffer[i], files[j]));
                            }

                            // When not in quotes, the last file is the beginning of a new token
                            // rather than a single one.
                            else if(j != files.length - 1)
                                tokens.add(getKeywordReplacement(buffer[i], files[j]));
                            else
                                currentToken.append(getKeywordReplacement(buffer[i], files[j]));
                        }
                    }
                }

                // If we've found an illegal keyword, ignore it.
                else {
                    currentToken.append(KEYWORD_HEADER);
                    currentToken.append(buffer[i]);
                }
            }

            // Nothing special about this character.
            else
                currentToken.append(buffer[i]);
        }

        // Adds a possible last token.
        if(currentToken.length() != 0)
            tokens.add(currentToken.toString());

        // Empty commands are returned as an empty token rather than an empty array.
        if(tokens.size() == 0)
            return new String[] {""};

        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * Returns <code>true</code> if the specified character is a legal keyword.
     * @param  keyword character to check.
     * @return         <code>true</code> if the specified character is a legal keyword, <code>false</code> otherwise.
     */
    private static boolean isLegalKeyword(char keyword) {
        return keyword == KEYWORD_PATH || keyword == KEYWORD_NAME || keyword == KEYWORD_PARENT ||
            keyword == KEYWORD_VM_PATH || keyword == KEYWORD_EXTENSION || keyword == KEYWORD_NAME_WITHOUT_EXTENSION;
    }

    /**
     * Gets the value from <code>file</code> that should be used to replace <code>keyword</code>.
     * @param  keyword character to replace.
     * @param  file    file from which to retrieve the replacement value.
     * @return         the requested replacement value.
     */
    private static String getKeywordReplacement(char keyword, AbstractFile file) {
        switch(keyword) {
        case KEYWORD_PATH:
            return file.getAbsolutePath();

        case KEYWORD_NAME:
            return file.getName();

        case KEYWORD_PARENT:
            AbstractFile parentFile = file.getParent();
            return parentFile==null?"":parentFile.getAbsolutePath();

        case KEYWORD_VM_PATH:
            return new File(System.getProperty("user.dir")).getAbsolutePath();

        case KEYWORD_EXTENSION:
            String extension;

            if((extension = file.getExtension()) == null)
                return "";
            return extension;

        case KEYWORD_NAME_WITHOUT_EXTENSION:
            return file.getNameWithoutExtension();
        }
        throw new IllegalArgumentException();
    }



    // - Misc. ---------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    public int hashCode() {
        int hashCode;

        hashCode = alias.hashCode();
        hashCode = hashCode * 31 + command.hashCode();
        hashCode = hashCode * 31 + getDisplayName().hashCode();
        hashCode = hashCode * 31 + type.hashCode();

        return hashCode;
    }

    public boolean equals(Object object) {
        if(object == null || !(object instanceof Command))
            return false;

        Command cmd;
        cmd = (Command)object;
        return command.equals(cmd.command) && alias.equals(cmd.alias) && type == cmd.type &&
               getDisplayName().equals(cmd.getDisplayName());
    }

    public int compareTo(Command command) {
        int buffer;

        if((buffer = getDisplayName().compareTo(command.getDisplayName())) != 0)
            return buffer;
        if((buffer = getAlias().compareTo(command.getAlias())) != 0)
            return buffer;
        return this.command.compareTo(command.command);
    }

    /**
     * Returns the original, un-tokenised command.
     * @return the original, un-tokenised command.
     */
    public synchronized String getCommand() {
    	return command;
    }

    /**
     * Returns this command's alias.
     * @return this command's alias.
     */
    public synchronized String getAlias() {
    	return alias;
    }

    /**
     * Returns the command's type.
     * @return the command's type.
     */
    public synchronized CommandType getType() {
    	return type;
    }

    /**
     * Returns the command's display name.
     * <p>
     * If it hasn't been set, returns this command's alias.
     * </p>
     * @return the command's display name.
     */
    public synchronized String getDisplayName() {
    	return displayName != null ? displayName : alias;
    }

    /**
     * Returns <code>true</code> if the command's display name has been set.
     * @return <code>true</code> if the command's display name has been set, <code>false</code> otherwise.
     */
    synchronized boolean isDisplayNameSet() {
    	return displayName != null;
    }

    @Override
    public String toString() {
    	return alias + (displayName == null ? "" : ":" + displayName) + ":" + command;
    }
}
