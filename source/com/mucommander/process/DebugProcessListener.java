package com.mucommander.process;

import com.mucommander.Debug;

/**
 * Listener used while in debug mode.
 * <p>
 * In debug mode, instances of this listener will automatically be registered to non-monitored processes.
 * Its only goal is to output information about the process' state.
 * </p>
 * @author Nicolas Rinaudo
 */
class DebugProcessListener implements ProcessListener {
    // - Instance fields -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Command that this listener is monitoring. */
    private String command;



    // - Initialisastion -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new process listener monitoring the specified command.
     * @param tokens tokens that compose the command that is being ran.
     */
    public DebugProcessListener(String[] tokens) {
        StringBuffer buffer;

        // Rebuilds the command.
        buffer = new StringBuffer();
        for(int i = 0; i < tokens.length; i++) {
            buffer.append(tokens[i]);
            buffer.append(' ');
        }

        command = buffer.toString();
    }



    // - Process monitoring --------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Prints out information about the way the process died.
     * @param returnValue process' return value.
     */
    public void processDied(int returnValue) {if(Debug.ON) Debug.trace(command + ": died with return code " + returnValue);}

    /**
     * Prints out the process output.
     */
    public void processOutput(char[] buffer, int offset, int length) {if(Debug.ON) Debug.trace(command + ": " + new String(buffer, offset, length));}
}
