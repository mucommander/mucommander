package com.mucommander.command;

/**
 * @author Nicolas Rinaudo
 */
public interface AssociationBuilder {
    /**
     * Notifies the builder that association building is about to start.
     * @throws CommandException if an error occurs.
     */
    public void startBuilding() throws CommandException;

    /**
     * Notifies the builder that association building is finished.
     * @throws CommandException if an error occurs.
     */
    public void endBuilding() throws CommandException;

    public void startAssociation(String command) throws CommandException;
    public void endAssociation() throws CommandException;
    public void setMask(String mask, boolean isCaseSensitive) throws CommandException;
    public void setIsSymlink(boolean isSymlink) throws CommandException;
    public void setIsHidden(boolean isHidden) throws CommandException;
    public void setIsReadable(boolean isReadable) throws CommandException;
    public void setIsWritable(boolean isWritable) throws CommandException;
    public void setIsExecutable(boolean isExecutable) throws CommandException;
}
