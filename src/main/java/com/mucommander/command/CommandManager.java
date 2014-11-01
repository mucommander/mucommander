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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.PlatformManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.PermissionTypes;
import com.mucommander.commons.file.filter.AttributeFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter.FileAttribute;
import com.mucommander.commons.file.filter.ChainedFileFilter;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.RegexpFilenameFilter;
import com.mucommander.io.backup.BackupInputStream;
import com.mucommander.io.backup.BackupOutputStream;

/**
 * Manages custom commands and associations.
 * @author Nicolas Rinaudo
 */
public class CommandManager implements CommandBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);
	
    // - Built-in commands -----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Alias for the system file opener. */
    public static final String FILE_OPENER_ALIAS           = "open";
    /** Alias for the system URL opener. */
    public static final String URL_OPENER_ALIAS            = "openURL";
    /** Alias for the system file manager. */
    public static final String FILE_MANAGER_ALIAS          = "openFM";
    /** Alias for the system executable file opener. */
    public static final String EXE_OPENER_ALIAS            = "openEXE";
    /** Alias for the default text viewer. */
    public static final String VIEWER_ALIAS                = "view";
    /** Alias for the default text editor. */ 
    public static final String EDITOR_ALIAS                = "edit";



    // - Self-open command -----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Alias of the 'run as executable' command. */
    public static final String  RUN_AS_EXECUTABLE_ALIAS   = "execute";
    /** Command used to run a file as an executable. */
    public static final Command RUN_AS_EXECUTABLE_COMMAND = new Command(RUN_AS_EXECUTABLE_ALIAS, "$f", CommandType.SYSTEM_COMMAND);



    // - Association definitions -----------------------------------------------
    // -------------------------------------------------------------------------
    /** System dependent file associations. */
    private static final List<CommandAssociation> systemAssociations;
    /** All known file associations. */
    private static final List<CommandAssociation> associations;
    /** Path to the custom association file, <code>null</code> if the default one should be used. */
    private static       AbstractFile             associationFile;
    /** Whether the associations were modified since the last time they were saved. */
    private static       boolean                  wereAssociationsModified;
    /** Default name of the association XML file. */
    public  static final String                   DEFAULT_ASSOCIATION_FILE_NAME = "associations.xml";



    // - Commands definition ---------------------------------------------------
    // -------------------------------------------------------------------------
    /** All known commands. */
    private static       Map<String, Command> commands;
    /** Path to the custom commands XML file, <code>null</code> if the default one should be used. */
    private static       AbstractFile         commandsFile;
    /** Whether the custom commands have been modified since the last time they were saved. */
    private static       boolean              wereCommandsModified;
    /** Default name of the custom commands file. */
    public  static final String               DEFAULT_COMMANDS_FILE_NAME    = "commands.xml";
    /** Default command used when no other command is found for a specific file type. */
    private static       Command              defaultCommand;



    // - Initialization --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Initializes the command manager.
     */
    static {
        systemAssociations = new Vector<CommandAssociation>();
        associations       = new Vector<CommandAssociation>();
        commands           = new Hashtable<String, Command>();
        defaultCommand     = null;
    }

    /**
     * Prevents instances of CommandManager from being created.
     */
    private CommandManager() {}



    // - Command handling ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the tokens that compose the command that must be executed to open the specified file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling
     * <code>{@link #getTokensForFile(AbstractFile,boolean) getTokensForFile(}file, true)</code>.
     * </p>
     * @param file file for which the opening command's tokens must be returned.
     * @return the tokens that compose the command that must be executed to open the specified file.
     */
    public static String[] getTokensForFile(AbstractFile file) {return getTokensForFile(file, true);}

    /**
     * Returns the tokens that compose the command that must be executed to open the specified file.
     * @param  file         file for which the opening command's tokens must be returned.
     * @param  allowDefault whether to use the default command if none was found to match the specified file.
     * @return              the tokens that compose the command that must be executed to open the specified file, <code>null</code> if not found.
     */
    public static String[] getTokensForFile(AbstractFile file, boolean allowDefault) {
        Command command;

        if((command = getCommandForFile(file, allowDefault)) == null)
            return null;
        return command.getTokens(file);
    }

    /**
     * Returns the command that must be executed to open the specified file.
     * <p>
     * This is a convenience method and is stricly equivalent to calling
     * <code>{@link #getCommandForFile(AbstractFile,boolean) getCommandForFile(}file, true)</code>.
     * </p>
     * @param  file file for which the opening command must be returned.
     * @return      the command that must be executed to open the specified file.
     */
    public static Command getCommandForFile(AbstractFile file) {return getCommandForFile(file, true);}

    private static Command getCommandForFile(AbstractFile file, Iterator<CommandAssociation> iterator) {
        CommandAssociation association;

        while(iterator.hasNext())
            if((association = iterator.next()).accept(file))
                return association.getCommand();
        return null;
    }

    /**
     * Returns the command that must be executed to open the specified file.
     * @param  file         file for which the opening command must be returned.
     * @param  allowDefault whether to use the default command if none was found to match the specified file.
     * @return              the command that must be executed to open the specified file, <code>null</code> if not found.
     */
    public static Command getCommandForFile(AbstractFile file, boolean allowDefault) {
        Command command;

        // Goes through all known associations and checks whether file matches any.
        if((command = getCommandForFile(file, associations.iterator())) != null)
            return command;

        // Goes through all system associations and checks whether file matches any.
        if((command = getCommandForFile(file, systemAssociations.iterator())) != null)
            return command;

        // We haven't found a command explicitely associated with 'file',
        // but we might have a generic file opener.
        if(defaultCommand != null)
            return defaultCommand;

        // We don't have a generic file opener, return the 'self execute'
        // command if we're allowed.
        if(allowDefault)
            return RUN_AS_EXECUTABLE_COMMAND;
        return null;
    }

    /**
     * Returns a sorted collection of all registered commands.
     * @return a sorted collection of all registered commands.
     */
    public static Collection<Command> commands() {
        // Copy the registered commands to a new list
    	List<Command> list = new Vector<Command>(commands.values());
    	// Sorts the list.
        Collections.sort(list);
        
        return list;
    }

    /**
     * Returns the command associated with the specified alias.
     * @param  alias alias whose associated command should be returned.
     * @return       the command associated with the specified alias if found, <code>null</code> otherwise.
     */
    public static Command getCommandForAlias(String alias) {
        return commands.get(alias);
    }

    private static void setDefaultCommand(Command command) {
        if(defaultCommand == null && command.getAlias().equals(FILE_OPENER_ALIAS)) {
        	LOGGER.debug("Registering '" + command.getCommand() + "' as default command.");
            defaultCommand = command;
        }
    }

    private static void registerCommand(Command command, boolean mark) throws CommandException {
        Command oldCommand;

        // Registers the command and marks command as having been modified.
        setDefaultCommand(command);

        LOGGER.debug("Registering '" + command.getCommand() + "' as '" + command.getAlias() + "'");

        oldCommand = commands.put(command.getAlias(), command);
        if(mark && !command.equals(oldCommand))
            wereCommandsModified = true;
    }

    public static void registerDefaultCommand(Command command) throws CommandException {registerCommand(command, false);}

    /**
     * Registers the specified command at the end of the command list.
     * @param  command          command to register.
     * @throws CommandException if a command with same alias has already been registered.
     */
    public static void registerCommand(Command command) throws CommandException {registerCommand(command, true);}



    // - Associations handling -------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns an iterator on all known file associations.
     * @return an iterator on all known file associations.
     */
    private static Iterator<CommandAssociation> associations() {return associations.iterator();}

    /**
     * Registers the specified association.
     * @param  command          command to execute when the association is matched.
     * @param  filter           file filters that a file must match to be accepted by the association.
     * @throws CommandException if an error occurs.
     */
    public static void registerAssociation(String command, FileFilter filter) throws CommandException {
        associations.add(createAssociation(command, filter));
    }
    
    private static CommandAssociation createAssociation(String cmd, FileFilter filter) throws CommandException {
        Command command;

        if((command = getCommandForAlias(cmd)) == null) {
        	LOGGER.debug("Failed to create association as '" + command + "' is not known.");
            throw new CommandException(command + " not found");
        }

        return new CommandAssociation(command, filter);
    }

    public static void registerDefaultAssociation(String command, FileFilter filter) throws CommandException {
        systemAssociations.add(createAssociation(command, filter));
    }



    // - Command builder code --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and must not be called directly.
     */
    public void addCommand(Command command) throws CommandException {registerCommand(command, false);}

    /**
     * Passes all known custom commands to the specified builder.
     * <p>
     * This method guarantees that the builder's {@link CommandBuilder#startBuilding() startBuilding()} and
     * {@link CommandBuilder#endBuilding() endBuilding()} methods will both be called even if an error occurs.
     * If that happens however, it is entirely possible that not all commands will be passed to
     * the builder.
     * </p>
     * @param  builder          object that will receive commands list building messages.
     * @throws CommandException if anything goes wrong.
     */
    public static void buildCommands(CommandBuilder builder) throws CommandException {
        builder.startBuilding();

        try {
        	// Goes through all the registered commands.
        	for (Command command : commands())
                builder.addCommand(command);
        }
        finally {builder.endBuilding();}
    }



    // - Associations building -------------------------------------------------
    // -------------------------------------------------------------------------
    private static void buildFilter(FileFilter filter, AssociationBuilder builder) throws CommandException {
        // Filter on the file type.
        if(filter instanceof AttributeFileFilter) {
            AttributeFileFilter attributeFilter;

            attributeFilter = (AttributeFileFilter)filter;
            switch(attributeFilter.getAttribute()) {
            case HIDDEN:
                builder.setIsHidden(!attributeFilter.isInverted());
                break;

            case SYMLINK:
                builder.setIsSymlink(!attributeFilter.isInverted());
                break;
            }
        }
        else if(filter instanceof PermissionsFileFilter) {
            PermissionsFileFilter permissionFilter;

            permissionFilter = (PermissionsFileFilter)filter;

            switch(permissionFilter.getPermission()) {
            case PermissionTypes.READ_PERMISSION:
                builder.setIsReadable(permissionFilter.getFilter());
                break;

            case PermissionTypes.WRITE_PERMISSION:
                builder.setIsWritable(permissionFilter.getFilter());
                break;

            case PermissionTypes.EXECUTE_PERMISSION:
                builder.setIsExecutable(permissionFilter.getFilter());
                break;
            }
        }
        else if(filter instanceof RegexpFilenameFilter) {
            RegexpFilenameFilter regexpFilter;

            regexpFilter = (RegexpFilenameFilter)filter;
            builder.setMask(regexpFilter.getRegularExpression(), regexpFilter.isCaseSensitive());
        }
    }

    /**
     * Passes all known file associations to the specified builder.
     * <p>
     * This method guarantees that the builder's {@link AssociationBuilder#startBuilding() startBuilding()} and
     * {@link AssociationBuilder#endBuilding() endBuilding()} methods will both be called even if an error occurs.
     * If that happens however, it is entirely possible that not all associations will be passed to
     * the builder.
     * </p>
     * @param  builder          object that will receive association list building messages.
     * @throws CommandException if anything goes wrong.
     */
    public static void buildAssociations(AssociationBuilder builder) throws CommandException {
        Iterator<CommandAssociation> iterator; // Used to iterate through commands and associations.
        Iterator<FileFilter>         filters;  // Used to iterate through each association's filters.
        FileFilter                   filter;   // Buffer for the current file filter.
        CommandAssociation           current;  // Current command association.

        builder.startBuilding();

        // Goes through all the registered associations.
        iterator = associations();
        try {
            while(iterator.hasNext()) {
                current = iterator.next();
                builder.startAssociation(current.getCommand().getAlias());

                filter = current.getFilter();
                if(filter instanceof ChainedFileFilter) {
                    filters = ((ChainedFileFilter)filter).getFileFilterIterator();
                    while(filters.hasNext())
                        buildFilter(filters.next(), builder);
                }
                else
                    buildFilter(filter, builder);

                builder.endAssociation();
            }
        }
        finally {builder.endBuilding();}
    }



    // - Associations reading/writing ------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the path to the custom associations XML file.
     * <p>
     * This method cannot guarantee the file's existence, and it's up to the caller
     * to deal with the fact that the user might not actually have created custom
     * associations.
     * </p>
     * <p>
     * This method's return value can be modified through {@link #setAssociationFile(String)}.
     * If this wasn't called, the default path will be used: {@link #DEFAULT_ASSOCIATION_FILE_NAME}
     * in the {@link com.mucommander.PlatformManager#getPreferencesFolder() preferences} folder.
     * </p>
     * @return the path to the custom associations XML file.
     * @see    #setAssociationFile(String)
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     * @throws IOException if there was an error locating the default commands file.
     */
    public static AbstractFile getAssociationFile() throws IOException {
        if(associationFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_ASSOCIATION_FILE_NAME);
        return associationFile;
    }

    /**
     * Sets the path to the custom associations file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setAssociationFile(FileFactory.getFile(file))</code>.
     * </p>
     * @param  path                  path to the custom associations file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getAssociationFile()
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     */
    public static void setAssociationFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setAssociationFile(new File(path));
        else
            setAssociationFile(file);
    }

    /**
     * Sets the path to the custom associations file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setAssociationFile(FileFactory.getFile(file.getAbsolutePath()))</code>.
     * </p>
     * @param  file                  path to the custom associations file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getAssociationFile()
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     */
    public static void setAssociationFile(File file) throws FileNotFoundException {setAssociationFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the custom associations file.
     * @param  file                  path to the custom associations file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getAssociationFile()
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     */
    public static void setAssociationFile(AbstractFile file) throws FileNotFoundException {
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());

        associationFile = file;
    }

    /**
     * Loads the custom associations XML File.
     * <p>
     * The command files will be loaded as a <i>backed-up file</i> (see {@link BackupInputStream}).
     * Its format is described {@link AssociationsXmlConstants here}.
     * </p>
     * @throws IOException if an IO error occurs.
     * @see                #writeAssociations()
     * @see                #getAssociationFile()
     * @see                #setAssociationFile(String)
     */
    public static void loadAssociations() throws IOException, CommandException {
        AbstractFile file;
        InputStream  in;

        file = getAssociationFile();
        LOGGER.debug("Loading associations from file: " + file.getAbsolutePath());

        // Tries to load the associations file.
        // Associations are not considered to be modified by this. 
        in = null;
        try {AssociationReader.read(in = new BackupInputStream(file), new AssociationFactory());}
        finally {
            wereAssociationsModified = false;
            // Makes sure the input stream is closed.
            if(in != null) {
                try {in.close();}
                catch(Exception e) {
                    // Ignores this.
                }
            }
        }
    }

    /**,
     * Writes all registered associations to the custom associations file.
     * <p>
     * Data will be written to the path returned by {@link #getAssociationFile()}. Note, however,
     * that this method will not actually do anything if the association list hasn't been modified
     * since the last time it was saved.
     * </p>
     * <p>
     * The association files will be saved as a <i>backed-up file</i> (see {@link BackupOutputStream}).
     * Its format is described {@link AssociationsXmlConstants here}.
     * </p>
     * @throws IOException      if an I/O error occurs.
     * @throws CommandException if an error occurs.
     * @see                     #loadAssociations()
     * @see                     #getAssociationFile()
     * @see                     #setAssociationFile(String)
     */
    public static void writeAssociations() throws CommandException, IOException {
        // Do not save the associations if they were not modified.
        if(wereAssociationsModified) {
            BackupOutputStream out;    // Where to write the associations.

            LOGGER.debug("Writing associations to file: " + getAssociationFile());

            // Writes the associations.
            out = null;
            try {
                buildAssociations(new AssociationWriter(out = new BackupOutputStream(getAssociationFile())));
                wereAssociationsModified = false;
            }
            finally {
                if(out != null) {
                    try {out.close();}
                    catch(Exception e) {
                        // Ignores this.
                    }
                }
            }
        }
        else
        	LOGGER.debug("Custom file associations not modified, skip saving.");
    }



    // - Commands reading/writing ----------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the path to the custom commands XML file.
     * <p>
     * This method cannot guarantee the file's existence, and it's up to the caller
     * to deal with the fact that the user might not actually have created custom
     * commands.
     * </p>
     * <p>
     * This method's return value can be modified through {@link #setCommandFile(String)}.
     * If this wasn't called, the default path will be used: {@link #DEFAULT_COMMANDS_FILE_NAME}
     * in the {@link com.mucommander.PlatformManager#getPreferencesFolder() preferences} folder.
     * </p>
     * @return the path to the custom commands XML file.
     * @see    #setCommandFile(String)
     * @see    #loadCommands()
     * @see    #writeCommands()
     * @throws IOException if there was some error locating the default commands file.
     */
    public static AbstractFile getCommandFile() throws IOException {
        if(commandsFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_COMMANDS_FILE_NAME);
        return commandsFile;
    }

    /**
     * Sets the path to the custom commands file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setCommandFile(FileFactory.getFile(file));</code>.
     * </p>
     * @param  path                  path to the custom commands file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getCommandFile()
     * @see    #loadCommands()
     * @see    #writeCommands()
     */
    public static void setCommandFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setCommandFile(new File(path));
        else
            setCommandFile(file);
    }
        

    /**
     * Sets the path to the custom commands file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setCommandFile(FileFactory.getFile(file.getAbsolutePath()));</code>.
     * </p>
     * @param  file                  path to the custom commands file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getCommandFile()
     * @see    #loadCommands()
     * @see    #writeCommands()
     */
    public static void setCommandFile(File file) throws FileNotFoundException {setCommandFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the custom commands file.
     * @param  file                  path to the custom commands file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getCommandFile()
     * @see    #loadCommands()
     * @see    #writeCommands()
     */
    public static void setCommandFile(AbstractFile file) throws FileNotFoundException {
        // Makes sure file can be used as a command file.
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());

        commandsFile = file;
    }

    /**
     * Writes all registered commands to the custom commands file.
     * <p>
     * Data will be written to the path returned by {@link #getCommandFile()}. Note, however,
     * that this method will not actually do anything if the command list hasn't been modified
     * since the last time it was saved.
     * </p>
     * <p>
     * The command files will be saved as a <i>backed-up file</i> (see {@link BackupOutputStream}).
     * Its format is described {@link CommandsXmlConstants here}.
     * </p>
     * @throws IOException      if an I/O error occurs.
     * @throws CommandException if an error occurs.
     * @see                     #loadCommands()
     * @see                     #getCommandFile()
     * @see                     #setCommandFile(String)
     */
    public static void writeCommands() throws IOException, CommandException {
        // Only saves the command if they were modified since the last time they were written.
        if(wereCommandsModified) {
            BackupOutputStream out;    // Where to write the associations.

            LOGGER.debug("Writing custom commands to file: " + getCommandFile());

            // Writes the commands.
            out = null;
            try {
                buildCommands(new CommandWriter(out = new BackupOutputStream(getCommandFile())));
                wereCommandsModified = false;
            }
            finally {
                if(out != null) {
                    try {out.close();}
                    catch(Exception e) {
                        // Ignores this.
                    }
                }
            }
        }
        else
        	LOGGER.debug("Custom commands not modified, skip saving.");
    }

    /**
     * Loads the custom commands XML File.
     * <p>
     * The command files will be loaded as a <i>backed-up file</i> (see {@link BackupInputStream}).
     * Its format is described {@link CommandsXmlConstants here}.
     * </p>
     * @throws IOException if an I/O error occurs.
     * @see                #writeCommands()
     * @see                #getCommandFile()
     * @see                #setCommandFile(String)
     */
    public static void loadCommands() throws IOException, CommandException {
        AbstractFile file;
        InputStream  in;

        file = getCommandFile();
        LOGGER.debug("Loading custom commands from: " + file.getAbsolutePath());

        // Tries to load the commands file.
        // Commands are not considered to be modified by this.
        in = null;
        try {CommandReader.read(in = new BackupInputStream(file), new CommandManager());}
        finally {
            wereCommandsModified = false;

            // Makes sure the input stream is closed.
            if(in != null) {
                try {in.close();}
                catch(Exception e) {
                    // Ignores this.
                }
            }
        }
    }

    /*
    private static void registerDefaultCommand(String alias, String command, String display) {
        if(getCommandForAlias(alias) == null) {
            if(command != null) {
                //                try {registerCommand(CommandParser.getCommand(alias, command, Command.SYSTEM_COMMAND, display));}
                try {registerCommand(new Command(alias, command, Command.SYSTEM_COMMAND, display));}
                catch(Exception e) {AppLogger.fine("Failed to register " + command + ": " + e.getMessage());}
            }
        }
    }
    */


    // - Unused methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and must not be called directly.
     */
    public void startBuilding() {}

    /**
     * This method is public as an implementation side effect and must not be called directly.
     */
    public void endBuilding() {}
}
