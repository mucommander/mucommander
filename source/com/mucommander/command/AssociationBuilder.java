package com.mucommander.command;

/**
 * Used to browse a set of command and associations.
 * <p>
 * Implementations of this interface can be passed to {@link com.mucommander.command.ConfigurationManager#buildAssociation(AssociationBuilder)}
 * in order to be passed all the registered commands and associations.
 * </p>
 * <p>
 * A typical use of this interface is reading from / writing to the custom associations file.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface AssociationBuilder {
    /**
     * Notifies the builder that association building is about to start.
     * @throws AssociationException thrown if anything wrong happens.
     */
    public void startBuilding() throws CommandException;

    /**
     * Notifies the builder that association building is finished.
     * @throws CommandException thrown if anything wrong happens.
     */
    public void endBuilding() throws CommandException;

    /**
     * Notifies the builder that a command has been found.
     * @param  command                command that has been found.
     * @throws CommandException thrown if anything wrong happens.
     */
    public void addCommand(Command command) throws CommandException;

    /**
     * Notifies the builder that an association has been found.
     * @param  mask                   file name mask to which the specified command should be associated.
     * @param  command                alias of the command to which the specified mask should be associated.
     * @throws CommandException thrown if anything wrong happens.
     */
    public void addAssociation(String mask, String command) throws CommandException;
}
