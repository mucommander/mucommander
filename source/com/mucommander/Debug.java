

package com.mucommander;

import java.io.*;


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
	public final static boolean ON = false;


	/**
	 * Returns the names of the class and method and source code line which
	 * triggered the method which called this method.
	 */
	public static String getCallerSignature() {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(bout, true);
			new Throwable().printStackTrace(ps);

			byte[] stackTrace = bout.toByteArray();
			ps.close();

			// Parse stack trace to find out the method that triggered the first call to LogManager, that way
			// we don't have to worry about how many method calls were made within LogManager.
			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stackTrace)));
			br.readLine();

			String line;

			while (((line = br.readLine())!=null) && (line.indexOf("Debug.java")!=-1));

			// One more time to remove caller
			line = br.readLine();
			br.close();

			String sig = null;
			if (line!=null) {
				// Retrieve class name + method name, not fully qualified (without package name)
				String methodLocation;
				int pos;
				int pos2 = 0;
				int pos3 = 0;
				int lastPos = line.lastIndexOf('.');

				while ((pos = line.indexOf('.', pos2+1))<lastPos) {
					pos3 = pos2;
					pos2 = pos;
				}

				// In order to remove ' at ' at line start
				if (pos3==0)
					pos3 = 4;
				else
					pos3 += 1;

				methodLocation = line.substring(pos3, line.indexOf('(', pos2));

				// Retrieves line number
				String lineNumber = line.substring(line.lastIndexOf(':')+1, line.length()-1);

				sig = methodLocation+","+lineNumber;
			}

			return sig;
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

}
