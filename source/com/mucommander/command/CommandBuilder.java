package com.mucommander.command;

/**
 * @author Nicolas Rinaudo
 */
public interface CommandBuilder {
    public void startBuilding();
    public void endBuilding();
    public void addCommand(Command command) throws CommandException;
}
