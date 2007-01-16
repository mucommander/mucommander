package com.mucommander.command;

import com.mucommander.PlatformManager;
import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.RegexpFilenameFilter;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.text.Translator;

import java.util.*;
import java.io.*;


/**
 * @author Nicolas Rinaudo
 */
public class CommandManager implements AssociationBuilder {
    // - System command aliases ------------------------------------------------
    // -------------------------------------------------------------------------
    /** Alias for the default system file opener. */
    public static final String DEFAULT_FILE_OPENER_ALIAS = "open";
    /** Alias for the default system URL opener. */
    public static final String DEFAULT_URL_OPENER_ALIAS  = "openURL";



    // - Self-open command -----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Alias for the 'self open' command. */
    public static final String SELF_OPEN_COMMAND_ALIAS   = "execute";
    /** Command used to try and run a file as an executable. */
    public static final Command SELF_OPEN_COMMAND = CommandParser.getCommand(SELF_OPEN_COMMAND_ALIAS, "$f");



    // - Association definitions -----------------------------------------------
    // -------------------------------------------------------------------------
    /** All known file associations. */
    private static Vector associations;
    /** All known commands. */
    private static Vector commands;



    // - Association file ------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Path to a potential custom association file. */
    private static       File    associationFile;
    /** Whether the associations were modified since the last time they were saved. */
    private static       boolean wasModified;
    /** Default name of the association XML file. */
    private static final String  ASSOCIATION_FILE_NAME = "associations.xml";



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
    /**
     * Returns the Command associated with the specified file name.
     * <p>
     * The <code>allowDefault</code> is used to prevent the default command from being returned.
     * In some cases, the fact that no command is associated with a file name is important.
     * <code>allowDefault</code> should be set to <code>false</code> for this cases.
     * </p>
     * @param  file         name of the file whose associated command should be retrieved.
     * @param  allowDefault whether or not a default command can be returned.
     * @return              the command associated with <code>file</code>, <code>null</code> if not found and <code>allowDefault</code> is set to <code>false</code>.
     */
    public static Command getCommandForFile(String file, boolean allowDefault) {
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
            return SELF_OPEN_COMMAND;
        return null;
    }

    /**
     * Returns the Command associated with the specified file name.
     * <p>
     * This is a convenience method, and is equivalent to calling <code>getCommandForFile(file, true);</code>.
     * </p>
     * @param  file name of the file whose associated command should be retrieved.
     * @return      the command associated with <code>file</code>.
     */
    public static Command getCommandForFile(String file) {return getCommandForFile(file, true);}

    /**
     * Returns the Command associated with the specified file.
     * <p>
     * This is a convenience method, and is equivalent to calling <code>getCommandForFile(file.getAbsolutePath(), true);</code>.
     * </p>
     * @param  file file whose associated command should be retrieved.
     * @return      the command associated with <code>file</code>.
     */
    public static Command getCommandForFile(AbstractFile file) {return getCommandForFile(file.getAbsolutePath(), true);}

    /**
     * Returns the Command associated with the specified file.
     * <p>
     * This is a convenience method, and is equivalent to calling <code>getCommandForFile(file.getAbsolutePath(), allowDefaultPath);</code>.
     * </p>
     * @param  file file whose associated command should be retrieved.
     * @param  allowDefault whether or not a default command can be returned.
     * @return              the command associated with <code>file</code>, <code>null</code> if not found and <code>allowDefault</code> is set to <code>false</code>.
     */
    public static Command getCommandForFile(AbstractFile file, boolean allowDefault) {return getCommandForFile(file.getAbsolutePath(), allowDefault);}

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
        wasModified = true;
    }

    /**
     * Registers the specified command at the specified index.
     * @param  i                index at which to register the command.
     * @param  command          command to register.
     * @throws CommandException if a command with same alias has already been registered.
     */
    public static void registerCommand(int i, Command command) throws CommandException {
        // Checks whether a font with the same alias has already been registered.
        if(getCommandForAlias(command.getAlias()) != null)
            throw new CommandException("Duplicated command alias: " + command.getAlias());

        // Registers the command and marks associations as having been modified.
        if(Debug.ON) Debug.trace("Registering '" + command.getCommand() + "' as '" + command.getAlias() + "' at index " + i);
        commands.add(i, command);
        wasModified = true;
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
            wasModified = true;
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
        wasModified = true;
        return true;
    }


    // - Associations handling -------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns an iterator on all known file associations.
     * @return an iterator on all known file associations.
     */
    public static Iterator associations() {return associations.iterator();}

    /**
     * Registers the specified association at the end of the association list.
     * <p>
     * While this method could accept a {@link com.mucommander.command.CommandAssociation} parameter, it would be very
     * unsafe for it to do so: it would be possible to register associations which use an unregistered command.
     * </p>
     * @param  mask             regular expression that a filename must match to be associated with <code>command</code>.
     * @param  command          command to be used on files that match <code>mask</code>.
     * @throws CommandException if <code>command</code> is not a registered command alias.
     */
    public static void registerAssociation(String mask, String command) throws CommandException {
        Command cmd;

        // The specified alias is known, registers the association and marks associations as modified.
        if((cmd = getCommandForAlias(command)) != null) {
            if(Debug.ON) Debug.trace("Registering '" + command + "' to files that match '" + mask + "' at the end of the list.");
            associations.add(new CommandAssociation(cmd, mask));
            wasModified = true;
        }

        // The specified alias is not known.
        else
            throw new CommandException(command + " not found");
    }

    /**
     * Registers the specified association at the specified index of the list.
     * <p>
     * While this method could accept a {@link com.mucommander.command.CommandAssociation} parameter, it would be very
     * unsafe for it to do so: it would be possible to register associations which use an unregistered command.
     * </p>
     * @param  i                index at which to register the association.
     * @param  mask             regular expression that a filename must match to be associated with <code>command</code>.
     * @param  command          command to be used on files that match <code>mask</code>.
     * @throws CommandException if <code>command</code> is not a registered command alias.
     */
    public static void registerAssociation(int i, String mask, String command) throws CommandException {
        Command cmd;

        // The specified alias is known, registers the association and marks associations as modified.
        if((cmd = getCommandForAlias(command)) != null) {
            if(Debug.ON) Debug.trace("Registering '" + command + "' to files that match '" + mask + "' at index " + i);
            associations.add(i, new CommandAssociation(cmd, mask));
            wasModified = true;
        }

        // The specified alias is not known.
        else
            throw new CommandException(command + " not found");
    }

    /**
     * Removes the specified association from the list of registered associations.
     * @param association association to remove.
     */
    public static void removeAssociation(CommandAssociation association) {
        // If the association was found, mark the list as modified.
        if(associations.remove(association))
            wasModified = true;
    }

    /**
     * Removes the association found at the specified index.
     * @param i index of the association to remove.
     */
    public static void removeAssociationAt(int i) {
        associations.remove(i);
        wasModified = true;
    }



    // - Builder code ----------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Not used.
     */
    public void startBuilding() {}

    /**
     * Not used.
     */
    public void endBuilding() {}

    /**
     * Registers the specified command.
     * @param  command          command to register.
     * @throws CommandException if a command with the same alias has already been registered.
     */
    public void addCommand(Command command) throws CommandException {registerCommand(command);}

    /**
     * Registers the specified association.
     * @param  mask             regular-expression that file names should match to be executed by <code>command</code>.
     * @param  command          alias of the command to which <code>mask</code> should be associated.
     * @throws CommandException if no match is found for <code>command</code>.
     */
    public void addAssociation(String mask, String command) throws CommandException {registerAssociation(mask, command);}

    /**
     * Notifies the specified <code>builder</code> of the current registered commands and associations.
     * @param  builder          object to notify of the current registered commands and actions.
     * @throws CommandException if something goes wrong.
     */
    public static void buildAssociations(AssociationBuilder builder) throws CommandException {
        Iterator           iterator; // Used to iterate through commands and associations.
        CommandAssociation current;  // Current command association.

        builder.startBuilding();

        // Goes through all the registered commands.
        iterator = commands();
        while(iterator.hasNext())
            builder.addCommand((Command)iterator.next());

        // Goes through all the registered associations.
        iterator = associations();
        while(iterator.hasNext()) {
            current = (CommandAssociation)iterator.next();
            builder.addAssociation(current.getRegularExpression(), current.getCommand().getAlias());
        }

        builder.endBuilding();
    }


    // - Associations reading/writing ------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the path to the associations XML file.
     * @return the path to the associations XML file.
     */
    private static File getAssociationFile() {
        if(associationFile == null)
            return new File(PlatformManager.getPreferencesFolder(), ASSOCIATION_FILE_NAME);
        return associationFile;
    }

    /**
     * Sets the path to the association XML file.
     * @param file path to the association XML file.
     */
    public static void setAssociationFile(String file) {associationFile = new File(file);}

    /**
     * Creates the default system associations.
     * <p>
     * This method is system dependant, and might, in some cases, not create anything.<br/>
     * It relies on {@link com.mucommander.PlatformManager#getDefaultURLOpener()} and
     * {@link com.mucommander.PlatformManager#getDefaultFileOpener()} to generate its associations.
     * </p>
     */
    private static void createDefaultAssociations() {
        String  command; // Default command.
        Command buffer;  // Buffer for new commands.

        // If it exists, creates a default URL opening command.
        if((command = PlatformManager.getDefaultURLOpener()) != null) {
            try {
                registerCommand(buffer = CommandParser.getCommand(DEFAULT_URL_OPENER_ALIAS, command));
                buffer.setSystem(true);
                registerAssociation("http://.*", DEFAULT_URL_OPENER_ALIAS);
                registerAssociation("https://.*", DEFAULT_URL_OPENER_ALIAS);
            }
            // This should never occur. If it does, something's really messed up in the system.
            catch(Exception e) {if(Debug.ON) Debug.trace("Couldn't create default URL opener: " + e.getMessage());}
        }

        // If it exists, creates a default file opening command.
        if((command = PlatformManager.getDefaultFileOpener()) != null) {
            try {
                registerCommand(buffer = CommandParser.getCommand(DEFAULT_FILE_OPENER_ALIAS, command));
                buffer.setSystem(true);
                registerAssociation(".*", DEFAULT_FILE_OPENER_ALIAS);
            }
            // This should never occur. If it does, something's really messed up in the system.
            catch(Exception e) {if(Debug.ON) Debug.trace("Couldn't create default file opener: " + e.getMessage());}
        }

        // If it exists, creates a default 'desktop' command.
        if((command = PlatformManager.getDefaultDesktopFM()) != null) {
            try {
                registerCommand(buffer = CommandParser.getCommand(PlatformManager.getDefaultDesktopFMName(), command));
                buffer.setSystem(true);
            }
            // This should never occur. If it does, something's really messed up in the system.
            catch(Exception e) {if(Debug.ON) Debug.trace("Couldn't create default file opener: " + e.getMessage());}
        }
    }

    /**
     * Loads and registers commands and associations from the associations file.
     */
    public static void loadAssociations() {
        File file;

        if(Debug.ON) Debug.trace("Loading custom file associations...");

        // Checks whether the associations file exists. If it doesn't, create default associations.
        file = getAssociationFile();
        if(!file.isFile()) {
            if(Debug.ON) Debug.trace("Associations file doesn't exist, using default associations");
            createDefaultAssociations();
        }
        else {
            InputStream in;

            // Tries to load the associations file. If an error occurs, create default associations.
            in = null;
            try {AssociationReader.read(in = new BackupInputStream(file), new CommandManager());}
            catch(Exception e) {
                if(Debug.ON) Debug.trace("Failed to load associations file: " + e.getMessage() + ". Using default associations");

                // The associations file is corrupt, discard anything we might have loaded from it.
                commands     = new Vector();
                associations = new Vector();

                // Creates the default associations.
                createDefaultAssociations();
            }

            // Makes sure the input stream is closed.
            finally {
                if(in != null) {
                    try {in.close();}
                    catch(Exception e) {}
                }
            }
        }
        wasModified = false;
    }

    /**
     * Saves the registered commands and associations.
     */
    public static void writeAssociations() {
        // Do not save the associations if they were not modified.
        if(wasModified) {
            BackupOutputStream out;    // Where to write the associations.
            AssociationWriter  writer; // What to write the associations with.

            if(Debug.ON) Debug.trace("Writing custom file associations...");

            // Writes the associations.
            out = null;
            try {
                writer = new AssociationWriter(out = new BackupOutputStream(getAssociationFile()));
                buildAssociations(writer);
                out.close(true);
            }
            // Prevents overwriting of the association file if there's reason to believe it wasn't
            // saved properly.
            catch(Exception e) {
                if(out != null) {
                    try {out.close(false);}
                    catch(Exception e2) {}
                }
            }
            wasModified = false;
        }
        else if(Debug.ON) Debug.trace("Custom file associations not modified, skip saving.");
    }
}
