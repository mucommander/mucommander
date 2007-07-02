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

import com.apple.cocoa.foundation.NSAppleEventDescriptor;
import com.apple.cocoa.foundation.NSAppleScript;
import com.apple.cocoa.foundation.NSAutoreleasePool;
import com.apple.cocoa.foundation.NSMutableDictionary;
import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.impl.local.LocalFile;

import java.util.Vector;

/**
 * OSXTrash provides access to the Mac OS X Finder's trash. Only local files (or locally mounted files) can be moved
 * to the trash.
 *
 * <p>
 * <b>Implementation notes:</b><br>
 * <br>
 * AppleScript is used to interact with the Finder. Scripts are executed using the Cocoa-java bridge which is
 * deprecated but still working as of today. Another way would be to use the 'osascript' command.<br>
 * <br>
 * This trash is implemented as a {@link QueuedTrash} for several reasons:
 * <ul>
 *  <li>the Finder plays a sound when it has been told to move a file to the trash and is done with it.
 *   Moving files to the trash repeatedly would play the sound as many times as the Finder has been told to move a file,
 *   which obviously is very ugly.
 *  <li>executing an AppleScript has a cost as it has to be compiled first. When files are moved repeatedly, it is more
 *   efficient to group files and execute only one AppleScript.
 * </ul>
 * </p>
 *
 * @author Maxence Bernard
 */
public class OSXTrash extends QueuedTrash {

    /** AppleScript that empties the trash */
    private final static String EMPTY_TRASH_APPLESCRIPT = "tell application \"Finder\" to empty trash";

    /** AppleScript that reveals the trash in Finder */
    private final static String REVEAL_TRASH_APPLESCRIPT = "tell application \"Finder\" to open trash";

    /** AppleScript that counts and returns the number of items in Trash */
    private final static String COUNT_TRASH_ITEMS_APPLESCRIPT = "tell application \"Finder\" to return count of items in trash";

    private static boolean isAvailable;

    static {
        // Test if the Cocoa-java library is available
        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X) {
            try {
                Class.forName("com.apple.cocoa.foundation.NSAppleScript");
                // Seems OK
                isAvailable = true;
            }
            catch(Exception e) {
                // Not available
            }
        }

        if(Debug.ON) Debug.trace("isAvailable="+isAvailable);
    }

    /**
     * Returns <code>true</code> if this trash can be instanciated, <code>false</code> if the current OS is not
     * Mac OS X or if the Cocoa-java library is not available.
     *
     * @return true if this trash can be instanciated, false if the current OS is not Mac OS X or if the Cocoa-java
     * library is not available
     */
    public static boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Executes the given AppleScript and returns the script's output if it was successfully executed, <code>null</code>
     * if the script couldn't be compiled or if an error occurred while executing it.
     * An empty string <code>""</code> is returned if the script doesn't output anything. 
     *
     * @param appleScript the AppleScript to compile and execute
     * @return the script's output, null if an error occurred while compiling or executing the script  
     */
    private static String executeAppleScript(String appleScript) {
        if(Debug.ON) Debug.trace("Executing AppleScript "+appleScript);

        int pool = -1;

        try {
            // Quote from Apple Cocoa-Java doc:
            // An autorelease pool is used to manage Foundation’s autorelease mechanism for Objective-C objects.
            // NSAutoreleasePool provides Java applications access to autorelease pools. Typically it is not
            // necessary for Java applications to use NSAutoreleasePools since Java manages garbage collection.
            // However, some situations require an autorelease pool; for instance, if you start off a thread that
            // calls Cocoa, there won’t be a top-level pool.
            pool = NSAutoreleasePool.push();

            NSMutableDictionary errorInfo = new NSMutableDictionary();
            NSAppleEventDescriptor eventDescriptor = new NSAppleScript(appleScript).execute(errorInfo);
            if(eventDescriptor==null) {
                if(Debug.ON)
                    Debug.trace("Caught AppleScript error: "+errorInfo.objectForKey(NSAppleScript.AppleScriptErrorMessage));

                return null;
            }

            String output = eventDescriptor.stringValue();  // Returns null if the script didn't output anything
            if(Debug.ON) Debug.trace("AppleScript output="+output);

            return output==null?"":output;
        }
        catch(Error e) {
            // Can happen if Cocoa-java is not in the classpath
            if(Debug.ON) Debug.trace("Unexcepted error while executing AppleScript (cocoa-java not available?): "+e);

            return null;
        }
        catch(Exception e) {
            // Try block is not supposed to throw any exception, but this is low-level stuff so just to be safe
            if(Debug.ON) Debug.trace("Unexcepted exception while executing AppleScript: "+e);

            return null;
        }
        finally {
            if(pool!=-1)
                NSAutoreleasePool.pop(pool);
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
    public boolean canEmptyTrash() {
        return true;
    }

    public boolean emptyTrash() {
        return executeAppleScript(EMPTY_TRASH_APPLESCRIPT)!=null;
    }

    public boolean isTrashFile(AbstractFile file) {
        return (file.getTopAncestor() instanceof LocalFile)
            && file.getAbsolutePath(true).contains("/.Trash/");
    }

    public int getTrashItemCount() {
        String count = executeAppleScript(COUNT_TRASH_ITEMS_APPLESCRIPT);
        if(count==null)
            return -1;

        try {
            return Integer.parseInt(count);
        }
        catch(NumberFormatException e) {
            if(Debug.ON) Debug.trace("Caught an exception: "+e);
            return -1;
        }
    }

    public void revealTrash() {
        executeAppleScript(REVEAL_TRASH_APPLESCRIPT);
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    public boolean canRevealTrash() {
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
     * that period, the thread will wait an additional {@link OSXTrash# QUEUE_PERIOD}, and so on.<p>
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
        int nbFiles = queuedFiles.size();
        String appleScript = "tell application \"Finder\" to move {";
        for(int i=0; i<nbFiles; i++) {
            appleScript += "posix file \""+((AbstractFile)queuedFiles.elementAt(i)).getAbsolutePath()+"\"";
            if(i<nbFiles-1)
                appleScript += ", ";
        }
        appleScript += "} to the trash";

        return executeAppleScript(appleScript)!=null;
    }
}
