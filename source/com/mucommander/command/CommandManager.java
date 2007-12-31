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

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.filter.*;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.BackupOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author Nicolas Rinaudo
 */
public class CommandManager implements CommandBuilder {
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



    // - Self-open command -----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Alias of the 'run as executable' command. */
    public static final String  RUN_AS_EXECUTABLE_ALIAS   = "execute";
    /** Command used to run a file as an executable. */
    public static final Command RUN_AS_EXECUTABLE_COMMAND = new Command(RUN_AS_EXECUTABLE_ALIAS, "$f", Command.SYSTEM_COMMAND);



    // - Association definitions -----------------------------------------------
    // -------------------------------------------------------------------------
    /** All known file associations. */
    private static       Vector       associations;
    /** Path to the custom association file, <code>null</code> if the default one should be used. */
    private static       AbstractFile associationFile;
    /** Whether the associations were modified since the last time they were saved. */
    private static       boolean      wereAssociationsModified;
    /** Default name of the association XML file. */
    public  static final String       DEFAULT_ASSOCIATION_FILE_NAME = "associations.xml";



    // - Commands definition ---------------------------------------------------
    // -------------------------------------------------------------------------
    /** All known commands. */
    private static       Vector       commands;
    /** Path to the custom commands XML file, <code>null</code> if the default one should be used. */
    private static       AbstractFile commandsFile;
    /** Whether the custom commands have been modified since the last time they were saved. */
    private static       boolean      wereCommandsModified;
    /** Default name of the custom commands file. */
    public  static final String       DEFAULT_COMMANDS_FILE_NAME    = "commands.xml";
    /** Default command used when no other command is found for a specific file type. */
    private static       Command      defaultCommand;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Initialises the command manager.
     */
    static {
        associations   = new Vector();
        commands       = new Vector();
        defaultCommand = null;
    }

    /**
     * Prevents instances of CommandManager from being created.
     */
    private CommandManager() {}



    // - Command handling ------------------------------------------------------
    // -------------------------------------------------------------------------
    public static String[] getTokensForFile(AbstractFile file) {return getTokensForFile(file, true);}
    public static String[] getTokensForFile(AbstractFile file, boolean allowDefault) {
        Command command;

        if((command = getCommandForFile(file, allowDefault)) == null)
            return null;
        return command.getTokens(file);
    }

    public static Command getCommandForFile(AbstractFile file) {return getCommandForFile(file, true);}

    public static Command getCommandForFile(AbstractFile file, boolean allowDefault) {
        Iterator           iterator;
        CommandAssociation association;

        // Goes through all known associations and checks whether file matches any.
        iterator = associations();
        while(iterator.hasNext())
            if((association = (CommandAssociation)iterator.next()).accept(file))
                return association.getCommand();

        // If we've reached that point, no command has been found. Returns the default command
        // if we're allowed.
        if(allowDefault)
            return (defaultCommand == null) ? RUN_AS_EXECUTABLE_COMMAND : defaultCommand;
        return null;
    }

    /**
     * Returns an iterator on all registered commands.
     * @return an iterator on all registered commands.
     */
    public static Iterator commands() {return commands.iterator();}

    /**
     * Returns the command associated with the specified alias.
     * @param  alias alias whose associated command should be returned.
     * @return       the command associated with the specified alias if found, <code>null</code> otherwise.
     */
    public static Command getCommandForAlias(String alias) {
        Iterator iterator;
        Command  command;

        // Goes through all registered commands and tries to match alias.
        iterator = commands();
        while(iterator.hasNext())
            if((command = (Command)iterator.next()).getAlias().equals(alias))
                return command;

        return null;
    }

    private static void setDefaultCommand(Command command) {
        if(defaultCommand == null && command.getAlias().equals(FILE_OPENER_ALIAS)) {
            if(Debug.ON) Debug.trace("Registering '" + command.getCommand() + "' as default command.");
            defaultCommand = command;
        }
    }

    /**
     * Registers the specified command at the end of the command list.
     * @param  command          command to register.
     * @throws CommandException if a command with same alias has already been registered.
     */
    public static void registerCommand(Command command) throws CommandException {
        // Checks whether a command with the same alias has already been registered.
        if(getCommandForAlias(command.getAlias()) != null)
            throw new CommandException("Duplicated command alias: " + command.getAlias());

        // Registers the command and marks command as having been modified.
        setDefaultCommand(command);
        if(Debug.ON) Debug.trace("Registering '" + command.getCommand() + "' as '" + command.getAlias() + "' at the end of the list.");
        commands.add(command);
        wereCommandsModified = true;
    }

    /**
     * Registers the specified command at the specified index.
     * @param  i                index at which to register the command.
     * @param  command          command to register.
     * @throws CommandException if a command with same alias has already been registered.
     */
    public static void registerCommand(int i, Command command) throws CommandException {
        // Checks whether a font with the same alias has already been registered.
        if(getCommandForAlias(command.getAlias()) != null) {
            if(Debug.ON) Debug.trace("Duplicated command alias: " + command.getAlias());
            throw new CommandException("Duplicated command alias: " + command.getAlias());
        }

        // Registers the command and marks associations as having been modified.
        setDefaultCommand(command);
        if(Debug.ON) Debug.trace("Registering '" + command.getCommand() + "' as '" + command.getAlias() + "' at index " + i);
        commands.add(i, command);
        wereCommandsModified = true;
    }

    /**
     * Checks whether the specified command is associated to any file name filter.
     * @param  command command to check for.
     * @return         <code>true</code> if the command is associated, <code>false</code> otherwise.
     */
    private static boolean isCommandAssociated(Command command) {
        Iterator iterator;

        // Goes through all the command associations looking for command.
        iterator = associations.iterator();
        while(iterator.hasNext())
            if(((CommandAssociation)iterator.next()).getCommand() == command)
                return true;

        return false;
    }

    /**
     * Removes the specified command from the list of registered commands.
     * <p>
     * This method might actually refuse to remove <code>command</code>: if it is associated to any
     * file name filter, a command cannot be removed without, well, messing the system up quite baddly.<br/>
     * If the command is associated to any file name filter, this method will return <code>false</code> and not
     * do anything.
     * </p>
     * @param  command command to remove from the list.
     * @return         <code>false</code> if the command could not be removed, <code>true</code> otherwise.
     */
    public static boolean removeCommand(Command command) {
        // If the command is associated to any file name filter, abort.
        if(isCommandAssociated(command))
            return false;

        // If the operation actually changed the list, mark it as modified.
        if(commands.remove(command))
            wereCommandsModified = true;
        return true;
    }

    /**
     * Removes the command found at the specified index of the command list.
     * <p>
     * This method might actually not remove the command: if it is associated to any
     * file name filter, a command cannot be removed without, well, messing the system up quite baddly.<br/>
     * If the command is associated to any file name filter, this method will return <code>false</code> and not
     * do anything.
     * </p>
     * @param  i index of the command to remove.
     * @return   <code>false</code> if the command could not be removed, <code>true</code> otherwise.
     */
    public static boolean removeCommandAt(int i) {
        Command buffer;

        // If the command is associated to any file name filter, abort.
        buffer = (Command)commands.get(i);
        if(isCommandAssociated(buffer))
            return false;

        // Removes the command and marks the list as modified.
        commands.remove(i);
        wereCommandsModified = true;
        return true;
    }


    // - Associations handling -------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns an iterator on all known file associations.
     * @return an iterator on all known file associations.
     */
    public static Iterator associations() {return associations.iterator();}

    public static void registerAssociation(String command, ChainedFileFilter filter) throws CommandException {
        Command cmd;

        // The specified alias is known, registers the association and marks associations as modified.
        if((cmd = getCommandForAlias(command)) != null) {
            if(Debug.ON) Debug.trace("Registering '" + command + "'.");
            associations.add(new CommandAssociation(cmd, filter));
            wereAssociationsModified = true;
        }

        // The specified alias is not known.
        else {
            if(Debug.ON) Debug.trace("Failed to create association as '" + command + "' is not known.");
            throw new CommandException(command + " not found");
        }
    }

    /**
     * Removes the specified association from the list of registered associations.
     * @param association association to remove.
     */
    public static void removeAssociation(CommandAssociation association) {
        // If the association was found, mark the list as modified.
        if(associations.remove(association))
            wereAssociationsModified = true;
    }

    /**
     * Removes the association found at the specified index.
     * @param i index of the association to remove.
     */
    public static void removeAssociationAt(int i) {
        associations.remove(i);
        wereAssociationsModified = true;
    }



    // - Command builder code --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and must not be called directly.
     */
    public void addCommand(Command command) throws CommandException {registerCommand(command);}

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
        Iterator           iterator; // Used to iterate through commands and associations.

        builder.startBuilding();

        // Goes through all the registered commands.
        iterator = commands();
        try {
            while(iterator.hasNext())
                builder.addCommand((Command)iterator.next());
        }
        finally {builder.endBuilding();}
    }



    // - Associations building -------------------------------------------------
    // -------------------------------------------------------------------------
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
        Iterator           iterator; // Used to iterate through commands and associations.
        Iterator           filters;  // Used to iterate through each association's filters.
        FileFilter         filter;   // Buffer for the current file filter.
        CommandAssociation current;  // Current command association.

        builder.startBuilding();

        // Goes through all the registered associations.
        iterator = associations();
        try {
            while(iterator.hasNext()) {
                current = (CommandAssociation)iterator.next();
                builder.startAssociation(current.getCommand().getAlias());

                filters = current.filters();
                while(filters.hasNext()) {
                    filter = (FileFilter)filters.next();

                    // Filter on the file type.
                    if(filter instanceof AttributeFileFilter) {
                        AttributeFileFilter attributeFilter;

                        attributeFilter = (AttributeFileFilter)filter;
                        switch(attributeFilter.getAttribute()) {
                        case AttributeFileFilter.HIDDEN:
                            builder.setIsHidden(!attributeFilter.isInverted());
                            break;

                        case AttributeFileFilter.SYMLINK:
                            builder.setIsSymlink(!attributeFilter.isInverted());
                            break;
                        }
                    }
                    else if(filter instanceof PermissionsFileFilter) {
                        PermissionsFileFilter permissionFilter;

                        permissionFilter = (PermissionsFileFilter)filter;

                        switch(permissionFilter.getPermission()) {
                        case PermissionsFileFilter.READ_PERMISSION:
                            builder.setIsReadable(permissionFilter.getFilter());
                            break;

                        case PermissionsFileFilter.WRITE_PERMISSION:
                            builder.setIsWritable(permissionFilter.getFilter());
                            break;

                        case PermissionsFileFilter.EXECUTE_PERMISSION:
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
     * The command files will be loaded as a <i>backed-up file</i> (see {@link com.mucommander.io.BackupInputStream}).
     * Its format is described {@link AssociationsXmlConstants here}.
     * </p>
     * @see #writeAssociations()
     * @see #getAssociationFile()
     * @see #setAssociationFile(String)
     */
    public static void loadAssociations() throws IOException {
        AbstractFile file;
        InputStream  in;

        file = getAssociationFile();
        if(Debug.ON)
            Debug.trace("Loading associations from file: " + file.getAbsolutePath());

        // Tries to load the associations file. If an error occurs, create default associations.
        in = null;
        try {AssociationReader.read(in = new BackupInputStream(file), new AssociationFactory());}
        catch(Exception e) {
            // The associations file is corrupt, discard anything we might have loaded from it.
            if(Debug.ON) Debug.trace("Failed to load associations file: " + e.getMessage() + ". Using default associations");
            associations = new Vector();

            throw new IOException(e.getMessage());
        }

        finally {
            // If an 'openEXE' command was registered:
            // - if the system has an association for that command, use it.
            // - If we have a sure way of identifying executable files (Java >= 1.6), use it.
            if(getCommandForAlias(EXE_OPENER_ALIAS) != null) {
                AndFileFilter filter;

                // Uses the 'executable' regexp if it exists.
                if(PlatformManager.getExeAssociation() != null) {
                    try {
                        filter = new AndFileFilter();
                        filter.addFileFilter(new RegexpFilenameFilter(PlatformManager.getExeAssociation(), PlatformManager.getDefaultRegexpCaseSensitivity()));
                        registerAssociation(EXE_OPENER_ALIAS, filter);
                    }
                    catch(Exception e) {if(Debug.ON) Debug.trace("Failed to create default EXE opener association: " + e.getMessage());}
                }

                // Match executables if necessary and if running under java >= 1.6.
                if(PlatformManager.runExecutables() && (PlatformManager.getJavaVersion() >= PlatformManager.JAVA_1_6)) {
                    try {
                        filter = new AndFileFilter();
                        filter.addFileFilter(new PermissionsFileFilter(PermissionsFileFilter.EXECUTE_PERMISSION, true));
                        registerAssociation(EXE_OPENER_ALIAS, filter);
                    }
                    catch(Exception e) {if(Debug.ON) Debug.trace("Failed to create default EXE opener association: " + e.getMessage());}
                }
            }
            wereAssociationsModified = false;

            // Makes sure the input stream is closed.
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
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
     * The association files will be saved as a <i>backed-up file</i> (see {@link com.mucommander.io.BackupOutputStream}).
     * Its format is described {@link AssociationsXmlConstants here}.
     * </p>
     * @see    #loadAssociations()
     * @see    #getAssociationFile()
     * @see    #setAssociationFile(String)
     * @throws IOException if an I/O error occurs.
     */
    public static void writeAssociations() throws CommandException, IOException {
        // Do not save the associations if they were not modified.
        if(wereAssociationsModified) {
            BackupOutputStream out;    // Where to write the associations.

            if(Debug.ON) Debug.trace("Writing associations to file: " + getAssociationFile());

            // Writes the associations.
            out = null;
            try {
                buildAssociations(new AssociationWriter(out = new BackupOutputStream(getAssociationFile())));
                wereAssociationsModified = false;
            }
            finally {
                if(out != null) {
                    try {out.close();}
                    catch(Exception e) {}
                }
            }
        }
        else if(Debug.ON) Debug.trace("Custom file associations not modified, skip saving.");
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
     * The command files will be saved as a <i>backed-up file</i> (see {@link com.mucommander.io.BackupOutputStream}).
     * Its format is described {@link CommandsXmlConstants here}.
     * </p>
     * @see    #loadCommands()
     * @see    #getCommandFile()
     * @see    #setCommandFile(String)
     * @throws IOException if an I/O error occurs.
     */
    public static void writeCommands() throws IOException, CommandException {
        // Only saves the command if they were modified since the last time they were written.
        if(wereCommandsModified) {
            BackupOutputStream out;    // Where to write the associations.

            if(Debug.ON) Debug.trace("Writing custom commands to file: " + getCommandFile());

            // Writes the commands.
            out = null;
            try {
                buildCommands(new CommandWriter(out = new BackupOutputStream(getCommandFile())));
                wereCommandsModified = false;
            }
            finally {
                if(out != null) {
                    try {out.close();}
                    catch(Exception e) {}
                }
            }
        }
        else if(Debug.ON) Debug.trace("Custom commands not modified, skip saving.");
    }

    /**
     * Loads the custom commands XML File.
     * <p>
     * The command files will be loaded as a <i>backed-up file</i> (see {@link com.mucommander.io.BackupInputStream}).
     * Its format is described {@link CommandsXmlConstants here}.
     * </p>
     * @see #writeCommands()
     * @see #getCommandFile()
     * @see #setCommandFile(String)
     */
    public static void loadCommands() throws IOException {
        AbstractFile file;
        InputStream  in;

        file = getCommandFile();
        if(Debug.ON)
            Debug.trace("Loading custom commands from: " + file.getAbsolutePath());

        // Tries to load the associations file. If an error occurs, create default associations.
        in = null;
        try {CommandReader.read(in = new BackupInputStream(file), new CommandManager());}
        catch(Exception e) {
            // Creates the default associations.
            if(Debug.ON) Debug.trace("Failed to load commands file: " + e.getMessage() + ". Using default commands.");
            commands = new Vector();
            throw new IOException(e.getMessage());
        }

        finally {
            // Registers default commands if necessary.
            registerDefaultCommand(FILE_OPENER_ALIAS,  PlatformManager.getDefaultFileOpenerCommand(), null);
            registerDefaultCommand(URL_OPENER_ALIAS,   PlatformManager.getDefaultUrlOpenerCommand(), null);
            registerDefaultCommand(EXE_OPENER_ALIAS,   PlatformManager.getDefaultExeOpenerCommand(), null);
            registerDefaultCommand(FILE_MANAGER_ALIAS, PlatformManager.getDefaultFileManagerCommand(), PlatformManager.getDefaultFileManagerName());
            wereCommandsModified = false;

            // Makes sure the input stream is closed.
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }

    private static void registerDefaultCommand(String alias, String command, String display) {
        if(getCommandForAlias(alias) == null) {
            if(command != null) {
                //                try {registerCommand(CommandParser.getCommand(alias, command, Command.SYSTEM_COMMAND, display));}
                try {registerCommand(new Command(alias, command, Command.SYSTEM_COMMAND, display));}
                catch(Exception e) {if(Debug.ON) Debug.trace("Failed to register " + command + ": " + e.getMessage());}
            }
        }
    }



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
