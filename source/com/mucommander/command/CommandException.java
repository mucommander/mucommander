package com.mucommander.command;

/**
 * Exception thrown when errors occur while building custom commands.
 * @author Nicolas Rinaudo
 */
public class CommandException extends Exception {
    /**
     * Builds a new exception.
     */
    public CommandException() {super();}

    /**
     * Builds a new exception with the specified message.
     * @param message exception's message.
     */
    public CommandException(String message) {super(message);}

    /**
     * Builds a new exception with the specified cause.
     * @param cause exception's cause.
     */
    public CommandException(Throwable cause) {super(cause);}

    /**
     * Builds a new exception with the specified message and cause.
     * @param message exception's message.
     * @param cause   exception's cause.
     */
    public CommandException(String message, Throwable cause) {super(message, cause);}
}
