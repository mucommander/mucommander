package com.mucommander.command;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.BackupOutputStream;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author Nicolas Rinaudo
 */
public class CommandManager implements AssociationBuilder, CommandBuilder {
    // - Self-open command -----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Alias of the 'run as executable' command. */
    public static final String  RUN_AS_EXECUTABLE_ALIAS   = "execute";
    /** Command used to run a file as an executable. */
    public static final Command RUN_AS_EXECUTABLE_COMMAND = CommandParser.getCommand(RUN_AS_EXECUTABLE_ALIAS, "$f", Command.SYSTEM_COMMAND);



    // - Association definitions -----------------------------------------------
    // -------------------------------------------------------------------------
    /** All known file associations. */
    private static       Vector  associations;
    /** Path to the custom association file, <code>null</code> if the default one should be used. */
    private static       File    associationFile;
    /** Whether the associations were modified since the last time they were saved. */
    private static       boolean wereAssociationsModified;
    /** Default name of the association XML file. */
    public  static final String  DEFAULT_ASSOCIATION_FILE_NAME = "associations.xml";



    // - Commands definition ---------------------------------------------------
    // -------------------------------------------------------------------------
    /** All known commands. */
    private static       Vector  commands;
    /** Path to the custom commands XML file, <code>null</code> if the default one should be used. */
    private static       File    commandsFile;
    /** Whether the custom commands have been modified since the last time they were saved. */
    private static       boolean wereCommandsModified;
    /** Default name of the custom commands file. */
    public  static final String  DEFAULT_COMMANDS_FILE_NAME    = "commands.xml";



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Initialises the command manager.
     */
    static {
        associations = new Vector();
        commands     = new Vector();
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
            return RUN_AS_EXECUTABLE_COMMAND;
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

    /**
     * Registers the specified command at the end of the command list.
     * @param  command          command to register.
     * @throws CommandException if a command with same alias has already been registered.
     */
    public static void registerCommand(Command command) throws CommandException {
        // Checks whether a font with the same alias has already been registered.
        if(getCommandForAlias(command.getAlias()) != null)
            throw new CommandException("Duplicated command alias: " + command.getAlias());

        // Registers the command and marks associations as having been modified.
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

    public static void registerAssociation(String mask, String command) throws CommandException {
        registerAssociation(mask, CommandAssociation.UNFILTERED, CommandAssociation.UNFILTERED, CommandAssociation.UNFILTERED, command);
    }

    public static void registerAssociation(String mask, int read, int write, int execute, String command) throws CommandException {
        Command cmd;

        // The specified alias is known, registers the association and marks associations as modified.
        if((cmd = getCommandForAlias(command)) != null) {
            if(Debug.ON) Debug.trace("Registering '" + command + "' to files that match '" + mask + "' at the end of the list.");
            associations.add(new CommandAssociation(cmd, mask, read, write, execute));
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
        CommandAssociation current;  // Current command association.

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
     * This method is public as an implementation side effect and should not be called.
     */
    public void addAssociation(String mask, int read, int write, int execute, String command) throws CommandException {registerAssociation(mask, read, write, execute, command);}

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
        CommandAssociation current;  // Current command association.

        builder.startBuilding();

        // Goes through all the registered associations.
        iterator = associations();
        try {
            while(iterator.hasNext()) {
                current = (CommandAssociation)iterator.next();
                builder.addAssociation(current.getRegularExpression(),
                                       current.getReadFilter(), current.getWriteFilter(), current.getExecuteFilter(),
                                       current.getCommand().getAlias());
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
     * @see    #getAssociationFile()
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     */
    public static File getAssociationFile() {
        if(associationFile == null)
            return new File(PlatformManager.getPreferencesFolder(), DEFAULT_ASSOCIATION_FILE_NAME);
        return associationFile;
    }

    /**
     * Sets the path to the custom associations file.
     * @param  file                     path to the custom associations file.
     * @throws IllegalArgumentException if <code>file</code> is not accessible.
     * @see    #getAssociationFile()
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     */
    public static void setAssociationFile(String file) {
        File tempFile;

        // If the file exists, it must accessible and readable.
        tempFile = new File(file);
        if(tempFile.exists() && !(tempFile.isFile() || tempFile.canRead()))
            throw new IllegalArgumentException("Not a valid file: " + file);

        associationFile = tempFile;
    }

    /**
     * Loads the custom associations XML File.
     * <p>
     * Data will be loaded from the path returned by {@link #getAssociationFile()}. If the file doesn't exist,
     * or an error occurs while loading it, {@link com.mucommander.PlatformManager#registerDefaultAssociations() default}
     * associations will be used.
     * </p>
     * <p>
     * The command files will be loaded as a <i>backed-up file</i> (see {@link com.mucommander.io.BackupInputStream}).
     * Its format is described {@link AssociationsXmlConstants here}.
     * </p>
     * @see #writeAssociations()
     * @see #getAssociationFile()
     * @see #setAssociationFile(String)
     */
    public static void loadAssociations() {
        File file;

        // Checks whether the associations file exists. If it doesn't, create default associations.
        file = getAssociationFile();
        if(Debug.ON)
            Debug.trace("Loading associations from file: " + file.getAbsolutePath());
        if(!file.isFile()) {
            if(Debug.ON) Debug.trace("Associations file doesn't exist, using default associations");
            PlatformManager.registerDefaultAssociations();
        }
        else {
            InputStream in;

            // Tries to load the associations file. If an error occurs, create default associations.
            in = null;
            try {AssociationReader.read(in = new BackupInputStream(file), new CommandManager());}
            catch(Exception e) {
                if(Debug.ON) Debug.trace("Failed to load associations file: " + e.getMessage() + ". Using default associations");

                // The associations file is corrupt, discard anything we might have loaded from it.
                associations = new Vector();

                // Creates the default associations.
                PlatformManager.registerDefaultAssociations();
            }

            // Makes sure the input stream is closed.
            finally {
                if(in != null) {
                    try {in.close();}
                    catch(Exception e) {}
                }
            }
        }
        wereAssociationsModified = false;
    }

    /**
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
     * @return <code>true</code> if the operation was a succes, <code>false</code> otherwise.
     * @see    #loadAssociations()
     * @see    #getAssociationFile()
     * @see    #setAssociationFile(String)
     */
    public static boolean writeAssociations() {
        // Do not save the associations if they were not modified.
        if(wereAssociationsModified) {
            BackupOutputStream out;    // Where to write the associations.
            AssociationWriter  writer; // What to write the associations with.

            if(Debug.ON) Debug.trace("Writing associations to file: " + getAssociationFile());

            // Writes the associations.
            out = null;
            try {
                writer = new AssociationWriter(out = new BackupOutputStream(getAssociationFile()));
                buildAssociations(writer);
                out.close(true);
                wereAssociationsModified = false;
            }
            // Prevents overwriting of the association file if there's reason to believe it wasn't
            // saved properly.
            catch(Exception e) {
                if(out != null) {
                    try {out.close(false);}
                    catch(Exception e2) {}
                }
                return false;
            }
        }
        else if(Debug.ON) Debug.trace("Custom file associations not modified, skip saving.");
        return true;
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
     */
    public static File getCommandFile() {
        if(commandsFile == null)
            return new File(PlatformManager.getPreferencesFolder(), DEFAULT_COMMANDS_FILE_NAME);
        return commandsFile;
    }

    /**
     * Sets the path to the custom commands file.
     * @param  file                     path to the custom commands file.
     * @throws IllegalArgumentException if <code>file</code> is not accessible.
     * @see    #getCommandFile()
     * @see    #loadCommands()
     * @see    #writeCommands()
     */
    public static void setCommandFile(String file) throws IllegalArgumentException {
        File tempFile;

        // If the file exists, it must accessible and readable.
        tempFile = new File(file);
        if(tempFile.exists() && !(tempFile.isFile() || tempFile.canRead()))
            throw new IllegalArgumentException("Not a valid file: " + file);

        commandsFile = tempFile;
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
     * @return <code>true</code> if the operation was a succes, <code>false</code> otherwise.
     * @see    #loadCommands()
     * @see    #getCommandFile()
     * @see    #setCommandFile(String)
     */
    public static boolean writeCommands() {
        // Only saves the command if they were modified since the last time they were written.
        if(wereCommandsModified) {
            BackupOutputStream out;    // Where to write the associations.
            CommandWriter      writer; // What to write the associations with.

            if(Debug.ON) Debug.trace("Writing custom commands to file: " + getCommandFile());

            // Writes the commands.
            out = null;
            try {
                writer = new CommandWriter(out = new BackupOutputStream(getCommandFile()));
                buildCommands(writer);
                out.close(true);
                wereCommandsModified = false;
            }
            // If an error occured, closes the stream but prevents the original file
            // from being overwritten.
            catch(Exception e) {
                if(out != null) {
                    try {out.close(false);}
                    catch(Exception e2) {}
                }
                return false;
            }

        }
        else if(Debug.ON)
            Debug.trace("Custom commands not modified, skip saving.");
        return true;
    }

    /**
     * Loads the custom commands XML File.
     * <p>
     * Data will be loaded from the path returned by {@link #getCommandFile()}. If the file doesn't exist,
     * or an error occurs while loading it, {@link com.mucommander.PlatformManager#registerDefaultCommands() default}
     * commands will be used.
     * </p>
     * <p>
     * The command files will be loaded as a <i>backed-up file</i> (see {@link com.mucommander.io.BackupInputStream}).
     * Its format is described {@link CommandsXmlConstants here}.
     * </p>
     * @see #writeCommands()
     * @see #getCommandFile()
     * @see #setCommandFile(String)
     */
    public static void loadCommands() {
        File file;

        file = getCommandFile();
        if(Debug.ON)
            Debug.trace("Loading custom commands from: " + file.getAbsolutePath());

        // If the file doesn't exist, registers default commands.
        if(!file.isFile()) {
            if(Debug.ON) Debug.trace("Commands file doesn't exist, using default commands.");
            PlatformManager.registerDefaultCommands();
        }

        // Otherwise, loads custom commands.
        else {
            InputStream in;

            // Tries to load the associations file. If an error occurs, create default associations.
            in = null;
            try {CommandReader.read(in = new BackupInputStream(file), new CommandManager());}
            catch(Exception e) {
                if(Debug.ON) Debug.trace("Failed to load commands file: " + e.getMessage() + ". Using default commands.");

                // Creates the default associations.
                commands = new Vector();
                PlatformManager.registerDefaultCommands();
            }

            // Makes sure the input stream is closed.
            finally {
                if(in != null) {
                    try {in.close();}
                    catch(Exception e) {}
                }
            }
        }
        wereCommandsModified = false;
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
