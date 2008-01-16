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

package com.mucommander.ui.macosx;

import com.mucommander.Debug;
import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessListener;
import com.mucommander.process.ProcessRunner;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * This class allows to run AppleScript code under Mac OS X, relying on the <code>osacript</code> command available
 * that comes with any install of Mac OS X. This command is used instead of the Cocoa-Java library which has been
 * deprecated by Apple.<br/>
 * Calls to {@link #execute(String, StringBuffer)} on any OS other than Mac OS X will always fail.
 *
 * <p>
 * <b>Important notes about character encoding</b>:
 * <ul>
 *   <li>AppleScript 1.10- (Mac OS X 10.4 or lower) expects <i>MacRoman</i> encoding, not <i>UTF-8</i>. <b>That
 *       means the script should only contain characters that are part of the MacRoman charset</b>; any character
 *       that cannot be expressed in MacRoman will not be propertly interpreted.<br/>
 *       The only way to pass Unicode text to a script is by reading it from a file.
 *       See <a href="http://www.satimage.fr/software/en/unicode_and_applescript.html">http://www.satimage.fr/software/en/unicode_and_applescript.html</a>
 *       for more information on how to do so.
 *   </li>
 *   <li>AppleScript 2.0+ (Mac OS X 10.5 and up) is fully Unicode-aware and will properly interpret any Unicode
 *       character: "AppleScript is now entirely Unicode-based. Comments and text constants in scripts may contain
 *       any Unicode characters, and all text processing is done in Unicode".<br/>
 *       See <a href="http://www.apple.com/applescript/features/unicode.html">http://www.apple.com/applescript/features/unicode.html</a>
 *       for more information.
 *   </li>
 * </ul>
 * </p>
 *
 * @author Maxence Bernard
 */
public class AppleScript {

    /** The UTF-8 encoding */
    public final static String UTF8 = "UTF-8";

    /** The MacRoman encoding */
    public final static String MACROMAN = "MacRoman";


    /**
     * Executes the given AppleScript and returns <code>true</code> if it completed its execution normally, i.e. without
     * any error.
     * The script's output is accumulated in the given <code>StringBuffer</code>. If the script completed its execution
     * normally, the buffer will contain the script's standard output. If the script failed because of an error in it,
     * the buffer will contain details about the error.
     *
     * <p>If the caller is not interested in the script's output, a <code>null</code> value can be passed which will
     * speed the execution up a little.</p>
     *
     * @param appleScript the AppleScript to execute
     * @param outputBuffer the StringBuffer that will hold the script's output, <code>null</code> for no output
     * @return true if the script was succesfully executed, false if the
     */
    public static boolean execute(String appleScript, StringBuffer outputBuffer) {
        // No point in going any futher if the current OS is not Mac OS X
        if(!OsFamilies.MAC_OS_X.isCurrent())
            return false;

        if(Debug.ON) Debug.trace("Executing AppleScript: "+appleScript);

        // Use the 'osascript' command to execute the AppleScript. The '-s o' flag tells osascript to print errors to
        // stdout rather than stderr. The AppleScript is piped to the process instead of passing it as an argument
        // ('-e' flag), for better control over the encoding and to remove any limitations on the maximum script size.
        String tokens[] = new String[] {
            "osascript",
            "-s",
            "o",
        };

        OutputStreamWriter pout = null;
        try {
            // Execute the osascript command.
            AbstractProcess process = ProcessRunner.execute(tokens, outputBuffer==null?null:new ScriptOutputListener(outputBuffer, AppleScript.getScriptEncoding()));

            // Pipe the script to the osascript process.
            pout = new OutputStreamWriter(process.getOutputStream(), getScriptEncoding());
            pout.write(appleScript);
            pout.close();

            // Wait for the process to die
            int returnCode = process.waitFor();

            if(Debug.ON) Debug.trace("osascript returned code="+returnCode+", output="+ outputBuffer);

            if(returnCode!=0) {
                if(Debug.ON) Debug.trace("osascript terminated abnormally");
                return false;
            }

            return true;
        }
        catch(Exception e) {        // IOException, InterruptedException
            // Shouldn't normally happen
            if(Debug.ON) {
                Debug.trace("Unexcepted exception while executing AppleScript: "+e);
                e.printStackTrace();
            }

            try {
                if(pout!=null)
                    pout.close();
            }
            catch(IOException e1) {
                // Can't do much about it
            }

            return false;
        }
    }

    /**
     * Returns the encoding that AppleScript uses on the current runtime environment:
     * <ul>
     *   <li>{@link #UTF8} for AppleScript 2.0+ (Mac OS X 10.5 and up)</li>
     *   <li>{@link #MACROMAN} for AppleScript 1.10- (Mac OS X 10.4 or lower)</li>
     * </ul>
     *
     * If {@link #MACROMAN} is used, the scripts passed to {@link #execute(String, StringBuffer)} should not contain
     * characters that are not part of the <i>MacRoman</i> charset or they will not be properly interpreted.
     *
     * @return the encoding that AppleScript uses on the current runtime environment
     */
    public static String getScriptEncoding() {
        // - AppleScript 2.0+ (Mac OS X 10.5 and up) is fully Unicode-aware and expects a script in UTF-8 encoding.
        // - AppleScript 1.3- (Mac OS X 10.4 or lower) expects MacRoman encoding, not UTF-8.
        String encoding;
        if(OsVersions.MAC_OS_X_10_5.isCurrentOrHigher())
            encoding = UTF8;
        else
            encoding = MACROMAN;

        return encoding;
    }


    /**
     * This ProcessListener accumulates the output of the 'osascript' command and suppresses the trailing '\n' character
     * from the script's output.
     */
    private static class ScriptOutputListener implements ProcessListener {

        private StringBuffer outputBuffer;
        private String outputEncoding;

        private ScriptOutputListener(StringBuffer outputBuffer, String outputEncoding) {
            this.outputBuffer = outputBuffer;
            this.outputEncoding = outputEncoding;
        }

        ////////////////////////////////////
        // ProcessListener implementation //
        ////////////////////////////////////

        public void processOutput(byte[] buffer, int offset, int length) {
            try {
                outputBuffer.append(new String(buffer, offset, length, outputEncoding));
            }
            catch(UnsupportedEncodingException e) {
                // The encoding is necessarily supported
            }
        }

        public void processOutput(String s) {
        }

        public void processDied(int returnValue) {
            // Remove the trailing "\n" character that osascript returns.
            int len = outputBuffer.length();
            if(len>0 && outputBuffer.charAt(len-1)=='\n')
                outputBuffer.setLength(len-1);    
        }
    }


    // The following commented method executes an AppleScript using the deprecated Cocoa-Java library.
    // We're now using the 'osascript' command instead, but this method is kept for the record in case Apple one day
    // decides to un-deprecate the Cocoa-Java library.

//    /**
//     * Executes the given AppleScript and returns the script's output if it was successfully executed, <code>null</code>
//     * if the script couldn't be compiled or if an error occurred while executing it.
//     * An empty string <code>""</code> is returned if the script doesn't output anything.
//     *
//     * @param appleScript the AppleScript to compile and execute
//     * @return the script's output, null if an error occurred while compiling or executing the script
//     */
//    private static String executeAppleScript(String appleScript) {
//        if(Debug.ON) Debug.trace("Executing AppleScript "+appleScript);
//
//        int pool = -1;
//
//        try {
//            // Quote from Apple Cocoa-Java doc:
//            // An autorelease pool is used to manage Foundation’s autorelease mechanism for Objective-C objects.
//            // NSAutoreleasePool provides Java applications access to autorelease pools. Typically it is not
//            // necessary for Java applications to use NSAutoreleasePools since Java manages garbage collection.
//            // However, some situations require an autorelease pool; for instance, if you start off a thread that
//            // calls Cocoa, there won’t be a top-level pool.
//            pool = NSAutoreleasePool.push();
//
//            NSMutableDictionary errorInfo = new NSMutableDictionary();
//            NSAppleEventDescriptor eventDescriptor = new NSAppleScript(appleScript).execute(errorInfo);
//            if(eventDescriptor==null) {
//                if(Debug.ON)
//                    Debug.trace("Caught AppleScript error: "+errorInfo.objectForKey(NSAppleScript.AppleScriptErrorMessage));
//
//                return null;
//            }
//
//            String output = eventDescriptor.stringValue();  // Returns null if the script didn't output anything
//            if(Debug.ON) Debug.trace("AppleScript output="+output);
//
//            return output==null?"":output;
//        }
//        catch(Error e) {
//            // Can happen if Cocoa-java is not in the classpath
//            if(Debug.ON) Debug.trace("Unexcepted error while executing AppleScript (cocoa-java not available?): "+e);
//
//            return null;
//        }
//        catch(Exception e) {
//            // Try block is not supposed to throw any exception, but this is low-level stuff so just to be safe
//            if(Debug.ON) Debug.trace("Unexcepted exception while executing AppleScript: "+e);
//
//            return null;
//        }
//        finally {
//            if(pool!=-1)
//                NSAutoreleasePool.pop(pool);
//        }
//    }
}
