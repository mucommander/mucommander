
package com.mucommander;


/**
 * Simple class which controls the output of debug messages.
 *
 * <p>Checking against a final static field value before sending debug output
 * (e.g. <code>if(com.mucommander.Debug.ON) System.out.println("Crashed!"); </code>)
 * instead of directly calling a debug method (e.g. com.mucommander.Debug.output("Crashed!");)
 * is a little heavier to type but allows removing debug-related method calls at compile time.</p>
 *
 * @author Maxence Bernard
 */
public class Debug {
	/** Sets whether or not debug messages should be output to the standard output */
	public final static boolean ON = true;
}
