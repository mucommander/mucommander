package com.mucommander.command;

/**
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

    public void addAssociation(String mask, int read, int write, int execute, String command) throws CommandException;
}
