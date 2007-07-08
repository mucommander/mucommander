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

package com.mucommander.file.impl.trash;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

/**
 * OSXTrash provides access to the Mac OS X Finder's trash. Only local files (or locally mounted files) can be moved
 * to the trash.
 *
 * <p>
 * <b>Implementation notes:</b><br>
 * <br>
 * This trash is implemented as a {@link QueuedTrash} for several reasons:
 * <ul>
 *  <li>the Finder plays a sound when it has been told to move a file to the trash and is done with it.
 *   Moving files to the trash repeatedly would play the sound as many times as the Finder has been told to move a file,
 *   which obviously is very ugly.
 *  <li>executing an AppleScript has a cost as it has to be compiled first. When files are moved repeatedly, it is more
 *   efficient to group files and execute only one AppleScript.
 * </ul>
 * <br>
 * AppleScript is used to interact with the Finder. Scripts are executed using the 'osascript' command. Another way
 * to run scripts is to use Apple's Cocoa-Java API, but since it is deprecated, it's better to avoid using it when 
 * possible.
 * </p>
 *
 * @author Maxence Bernard
 */
public class OSXTrash extends QueuedTrash {

    /** AppleScript that reveals the trash in Finder */
    private final static String REVEAL_TRASH_APPLESCRIPT = "tell application \"Finder\" to open trash";

    /** AppleScript that counts and returns the number of items in Trash */
    private final static String COUNT_TRASH_ITEMS_APPLESCRIPT = "tell application \"Finder\" to return count of items in trash";

    /** AppleScript that empties the trash */
    private final static String EMPTY_TRASH_APPLESCRIPT = "tell application \"Finder\" to empty trash";

    /**
     * AppleScript that moves files to the trash. Because AppleScript doesn't allow Unicode in the script itself
     * (only Mac OS Roman), this script is a bit more complicated that one would like in order to support files with
     * extended/unicode characters. The only way to deal with UTF-8 strings in AppleScript is to read them from a file.
     * See http://www.satimage.fr/software/en/unicode_and_applescript.html for more info about Unicode and AppleScript.
     */
    private final static String MOVE_TO_TRASH_APPLESCRIPT =
        // Loads the contents of the UTF8-encoded file which path is contained in the 'tmpFilePath' variable.
        // This variable must be set before the beginning of the script. This file contains the list of files to move
        // to the trash, separated by EOL characters. The file must NOT end with a trailing EOL.
        "set tmpFile to (open for access (POSIX file tmpFilePath))\n" +
        "set tmpFileContents to (read tmpFile for (get eof tmpFile) as «class utf8»)\n" +
        "close access tmpFile\n" +
        // Split the file contents into a list of lines, each line representing a POSIX file path to delete
        "set posixFileList to every paragraph of tmpFileContents\n" +
        // Convert the list of POSIX paths into a list of file objects. Note that internally AppleScript uses
        // a Mac-specific colon-separated path notation rather than the POSIX one.
        "set fileCount to the number of items in posixFileList\n" +
        "set fileList to {}\n" +
        "repeat with i from 1 to the fileCount\n" +
            "set posixFile to item i of posixFileList\n" +
            "copy POSIX file posixFile to the end of fileList\n" +
        "end repeat\n" +
        // Tell the Finder to move those files to the trash. Note that the file list must contain file objects and not
        // POSIX paths, hence the previous step. 
        "tell application \"Finder\" to move fileList to the trash";


//    private static boolean isAvailable;

//    static {
//        // Test if the Cocoa-java library is available
//        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X) {
//            try {
//                Class.forName("com.apple.cocoa.foundation.NSAppleScript");
//                // Seems OK
//                isAvailable = true;
//            }
//            catch(Exception e) {
//                // Not available
//            }
//        }
//
//        if(Debug.ON) Debug.trace("isAvailable="+isAvailable);
//    }

//    /**
//     * Returns <code>true</code> if this trash can be instanciated, <code>false</code> if the current OS is not
//     * Mac OS X or if the Cocoa-java library is not available.
//     *
//     * @return true if this trash can be instanciated, false if the current OS is not Mac OS X or if the Cocoa-java
//     * library is not available
//     */
//    public static boolean isAvailable() {
//        return isAvailable;
//    }


    // The following commented method executes an AppleScript using the Cocoa-Java API.
    // We're now using the 'osascript' command instead, but this method is kept for the record.

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


    /**
     * Executes the given AppleScript and returns <code>true</code> if it was successfully executed.
     * The script's output is appended to the given <code>StringBuffer</code>. If the caller is not interested in the
     * script's output, a <code>null</code> value can be passed.
     *
     * @param appleScript the AppleScript to execute
     * @param output the StringBuffer that will hold the script's output, <code>null</code> for no output
     * @return true if the script was succesfully executed
     */
    private static boolean executeAppleScript(String appleScript, StringBuffer output) {
        if(Debug.ON) Debug.trace("Executing AppleScript "+appleScript);

        // Use the 'osascript' command to execute the AppleScript. The '-s o' flag tells osascript to print errors to
        // stdout rather than stderr. The AppleScript is piped to the process instead of passing it as an argument
        // ('-e' flag), for better control over the encoding and to remove any limitations on the maximum script size.
        String tokens[] = new String[] {
            "osascript",
            "-s",
            "o",
        };

        OutputStreamWriter pout = null;
        InputStream pin = null;
        try {
            // Execute the osascript command.
            AbstractProcess process = ProcessRunner.execute(tokens);

            if(output!=null)
                pin = process.getInputStream();

            // Pipe the script to the osascript process.
            // For some strange reason, osascript expects MacRoman encoding, not UTF-8 as one would have thought
            pout = new OutputStreamWriter(process.getOutputStream(), "MacRoman");
            pout.write(appleScript);
            pout.close();

            if(output!=null) {
                // Append the script's output to the given StringBuffer
                byte buffer[] = new byte[128];
                int nbRead;
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                while((nbRead=pin.read(buffer))!=-1)
                    bout.write(buffer, 0, nbRead);

                output.append(bout.toString("utf-8"));
            }

            int returnCode = process.waitFor();

            if(Debug.ON) Debug.trace("osascript returned "+returnCode+" output="+output);

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
            }

            try {
                if(pin!=null)
                    pin.close();
            }
            catch(IOException e2) {
            }

            return false;
        }
    }


    //////////////////////////////////
    // AbstractTrash implementation //
    //////////////////////////////////

    /**
     * Implementation notes: returns <code>true</code> only for local files that are not archive entries.
     */
    public boolean canMoveToTrash(AbstractFile file) {
        return file.getTopAncestor() instanceof LocalFile;
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    public boolean canEmpty() {
        return true;
    }

    public boolean empty() {
        return executeAppleScript(EMPTY_TRASH_APPLESCRIPT, null);
    }

    public boolean isTrashFile(AbstractFile file) {
        return (file.getTopAncestor() instanceof LocalFile)
            && file.getAbsolutePath(true).contains("/.Trash/");
    }

    public int getItemCount() {
        StringBuffer output = new StringBuffer();
        if(!executeAppleScript(COUNT_TRASH_ITEMS_APPLESCRIPT, output))
            return -1;

        try {
            return Integer.parseInt(output.toString().trim());
        }
        catch(NumberFormatException e) {
            if(Debug.ON) Debug.trace("Caught an exception: "+e);
            return -1;
        }
    }

    public void open() {
        executeAppleScript(REVEAL_TRASH_APPLESCRIPT, null);
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    public boolean canOpen() {
        return true;
    }


    ////////////////////////////////
    // QueuedTrash implementation //
    ////////////////////////////////

    /**
     * Performs the actual job of moving files to the trash using AppleScript.
     *
     * <p>The thread starts by waiting {@link OSXTrash#QUEUE_PERIOD} milliseconds before moving them to give additional
     * files a chance to be queued and regrouped as a single AppleScript call. If some files were queued during
     * that period, the thread will wait an additional {@link OSXTrash#QUEUE_PERIOD}, and so on.<p>
     *
     * <p>There are several reasons for doing that instead of executing an AppleScript synchroneously for each file
     * passed to {@link OSXTrash#moveToTrash(com.mucommander.file.AbstractFile)} :
     * <ul>
     *  <li>the Finder plays a sound when it has been told to move a file to the trash and is done with it. Calling
     * moveToTrash repeatedly would play the sound as many times as the method has been called (believe me it's ugly
     * and a show-stopper!)
     *  <li>executing an AppleScript has a cost as it has to be compiled first. If moveToTrash is called repeatedly, it
     * is more efficient to regroup files to be moved and execute only one AppleScript.
     * </ul>
     */
    protected boolean moveToTrash(Vector queuedFiles) {

        // Simple AppleScript that moves the specified file paths to the trash. This script does not support Unicode
        // paths, this is why we don't use it (kept for the record)

//        int nbFiles = queuedFiles.size();
//        String appleScript = "tell application \"Finder\" to move {";
//        for(int i=0; i<nbFiles; i++) {
//            appleScript += "posix file \""+((AbstractFile)queuedFiles.elementAt(i)).getAbsolutePath()+"\"";
//            if(i<nbFiles-1)
//                appleScript += ", ";
//        }
//        appleScript += "} to the trash";

        AbstractFile tmpFile = null;
        OutputStreamWriter tmpOut = null;

        try {
            // Create the temporary file that contains the list of files to move, encoded as UTF-8 and separated by
            // EOL characters. The file must NOT end with a trailing EOL.
            int nbFiles = queuedFiles.size();
            tmpFile = FileFactory.getTemporaryFile("trash_files.muco", false);
            tmpOut = new OutputStreamWriter(tmpFile.getOutputStream(false), "utf-8");

            for(int i=0; i<nbFiles; i++) {
                tmpOut.write(((AbstractFile)queuedFiles.elementAt(i)).getAbsolutePath());
                if(i<nbFiles-1)
                    tmpOut.write("\n");
            }

            tmpOut.close();

            // Set the 'tmpFilePath' variable to the path of the temporary file we just created
            String appleScript = "set tmpFilePath to \""+tmpFile.getAbsolutePath()+"\"\n";
            appleScript += MOVE_TO_TRASH_APPLESCRIPT;

            boolean success = executeAppleScript(appleScript, null);

            // AppleScript has been executed, we can now safely close and delete the temporary file
            tmpFile.delete();

            return success;
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Caught exception: "+e);

            if(tmpOut!=null) {
                try {
                    tmpOut.close();
                }
                catch(IOException e1) {}
            }

            if(tmpFile!=null) {
                try {
                    tmpFile.delete();
                }
                catch(IOException e2) {}
            }

            return false;
        }
    }
}
