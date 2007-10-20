/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander;

import com.mucommander.io.SilenceablePrintStream;

import java.io.*;
import java.util.Vector;


/**
 * Simple class which controls the output of debug messages.
 *
 * <p>Checking against a final static field value before sending debug output
 * (e.g. <code>if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("method called!"); </code>)
 * instead of directly calling a debug method (e.g. com.mucommander.Debug.trace("method called");)
 * is a little heavier to type but allows method calls to be removed at compile time when ON is set to false (makes class files lighter).</p>
 *
 * @author Maxence Bernard
 */
public class Debug {

    /** Sets whether or not debug messages should be output to the standard output */
    public final static boolean ON        = RuntimeConstants.DEBUG;
    /** Used to disable debug messages even though debug instructions have been compiled in. */
    private static      boolean enabled   = true;
    /** Used to time blocks of code in the application. */
    private static      long    lastTime;

    private static SilenceablePrintStream outPs;
    private static SilenceablePrintStream errPs;


    static {
        System.setOut(outPs = new SilenceablePrintStream(System.out, false));
        System.setErr(errPs = new SilenceablePrintStream(System.err, false));
    }


    /**
     * Enables / disables debut output.
     * @param b whether or not debug messages should be enabled.
     */
    public static void setEnabled(boolean b) {enabled = b;}

    /**
     * Resets the timestamp used by {@link #time()} to now.
     */
    public static void resetTimer() {lastTime = System.currentTimeMillis();}

    /**
     * Traces the time difference between now and the last call to this method.
     */
    public static void time() {
        long currentTime = System.currentTimeMillis();
        trace((currentTime - lastTime)+" ms since last call");
        lastTime = currentTime;
    }

    /**
     * Prints the given debug message. The caller class and method names and line number within the calling method
     * will be printed before the message. This method is equivalent to calling {@link #trace(String, int)} with a
     * level of <code>0</code>.
     *
     * @param message the debug message to print
     */
    public static void trace(String message) {
        trace(message, 0);
    }

    /**
     * Prints the given debug message. The caller class and method names and line number within the calling methods
     * will be printed before the message. The level parameter controls how many class/method names will be printed.
     * Use <code>-1</code> for no limit.
     *
     * @param message the debug message to print
     * @param level controls how many class/method names will be printed
     */
    public static void trace(String message, int level) {
        if(enabled)
            System.out.println(getCallerTrace(level)+" : "+message);
    }

    /**
     * Prints the given <code>Throwable</code>'s stack trace.
     *
     * @param e the Throwable for which the stack trace will be printed
     */
    public static void trace(Throwable e) {
        e.printStackTrace();
    }


    /**
     * Returns the names of the class and method and source code line number which triggered the call to the debug
     * method of this class. The level parameter controls how many class/method names will be printed.
     * Use <code>-1</code> for no limit.
     *
     * @param level controls how many class/method names will be printed
     * @return the caller trace
     */
    private static String getCallerTrace(int level) {
        try {
            // Todo: rewrite this method using Thread#getStackTrace() that was introduced in Java 1.4, now that Java 1.3 compatibility is gone

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bout, true);
            new Throwable().printStackTrace(ps);

            byte[] stackTrace = bout.toByteArray();
            ps.close();

            // Parse stack trace to find out the method that triggered the first call to Debug, that way
            // we don't have to worry about how many method calls were made within Debug.
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stackTrace)));
            br.readLine();

            String line;
            Vector subTrace = new Vector();
			
            // Fill up trace vector to the desired level
            while ((line = br.readLine())!=null) {
                // Discard lines that refer to this class' trace
                if(line.indexOf("Debug.java")!=-1)
                    continue;
				
                subTrace.add(line);
                if(level!=-1 && subTrace.size()>level)
                    break;
            }

            //			// One more time to remove caller
            //			line = br.readLine();
            br.close();

            String sig = "";
            for(int i=subTrace.size()-1; i>=0; i--) {
                line = (String)subTrace.elementAt(i);
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

                    sig += (sig.equals("")?"":" -> ")+methodLocation+","+lineNumber;
                }
            }

            return sig;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * Enables/disables the standard system output stream. If disabled, any message printed to <code>System.out</code>
     * will be ignored (i.e. will not be printed) after this method has been called, and until has been enabled again.
     *
     * @param enabled <code>true</code> to enable <code>System.out</code> trace, <code>false</code> to disable it
     */
    public static synchronized void setSystemOutEnabled(boolean enabled) {
        outPs.setSilenced(!enabled);
    }

    /**
     * Enables/disables the standard system err stream . If disabled, any message printed to <code>System.err</code>
     * will be ignored (i.e. will not be printed) after this method has been called, and until has been enabled again.
     *
     * @param enabled <code>true</code> to enable <code>System.err</code> trace, <code>false</code> to disable it
     */
    public static synchronized void setSystemErrEnabled(boolean enabled) {
        errPs.setSilenced(!enabled);
    }
}
