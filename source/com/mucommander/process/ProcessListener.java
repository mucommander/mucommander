package com.mucommander.process;


/**
 * Implementations of this interface can listen to a process' state and streams.
 * @see com.mucommander.process.AbstractProcess
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public interface ProcessListener {
	
    /** 
     * This method is called when the process dies. No more calls to <code>processOutput</code> and
     * <code>processError</code> will be made past this call.
     * @param returnValue the value returned by the process (return code).
     */
    public void processDied(int returnValue);

    /**
     * This method is called whenever the process sends data to its output streams (stdout or stderr).
     * @param buffer contains the process' output.
     * @param offset offset in buffer at which the process' output starts.
     * @param length length of the process' output in buffer.
     */
    public void processOutput(byte buffer[], int offset, int length);
}
