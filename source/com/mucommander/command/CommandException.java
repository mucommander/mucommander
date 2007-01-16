package com.mucommander.command;

/**
 * Exception thrown when errors occur while building custom commands.
 * @author Nicolas Rinaudo
 */
public class CommandException extends Exception {
    public CommandException() {super();}
    public CommandException(String message) {super(message);}
    public CommandException(Throwable cause) {super(cause);}
    public CommandException(String message, Throwable cause) {super(message, cause);}
}
