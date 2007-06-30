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

import com.apple.cocoa.foundation.NSAppleScript;
import com.apple.cocoa.foundation.NSAutoreleasePool;
import com.apple.cocoa.foundation.NSMutableDictionary;
import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractTrash;
import com.mucommander.file.FileURL;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.file.util.PathTokenizer;

import java.util.Vector;

/**
 * This class is a <code>AbstractTrash</code> implementation for the Mac OS X Finder's trash.
 *
 * <p>Implementation notes: this implementation uses AppleScript to interact with the Finder's trash. It is only able
 * to move local files (or locally mounted files) to the trash.
 * For some technical reasons, {@link #moveToTrash(com.mucommander.file.AbstractFile)} works asynchroneously: files are
 * not moved to the trash immediately. {@link #waitForPendingOperations()} can be used to know when the files have
 * effectively been moved.
 *
 * @author Maxence Bernard
 */
public class OSXTrash extends AbstractTrash {

    /** Contains the files that are waiting to be moved to the trash */
    private final static Vector queuedFiles = new Vector();

    /** Use to synchronize access to the trash */
    private final static Object moveToTrashLock = new Object();

    /** Thread that performs the actual job of moving files to the trash */
    private static Thread moveToTrashThread;

    /** Amount of time in millisecondes to wait for additional files before moving them to the trash */
    private final static int REGROUP_PERIOD = 1000;

    /** AppleScript to empty the trash */
    private final static String EMPTY_TRASH_APPLESCRIPT = "tell application \"Finder\" to empty trash";

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
     * Executes the given AppleScript and returns <code>true</code> if it was successfully executed, <code>false</code>
     * if there was an error in the script, or an error while executing it.
     *
     * @param appleScript the AppleScript to compile and execute
     * @return true if the AppleScript was succesfully executed 
     */
    private static boolean executeAppleScript(String appleScript) {
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
            if(new NSAppleScript(appleScript).execute(errorInfo)==null) {
                if(Debug.ON)
                    Debug.trace("Caught AppleScript error: "+errorInfo.objectForKey(NSAppleScript.AppleScriptErrorMessage));

                return false;
            }

            return true;
        }
        catch(Error e) {
            // Can happen if
            if(Debug.ON) Debug.trace("Unexcepted error while executing AppleScript (cocoa-java not available?): "+e);

            return false;
        }
        catch(Exception e) {
            // Try block is not supposed to throw any exception, but this is low-level stuff so just to be safe
            if(Debug.ON) Debug.trace("Unexcepted exception while executing AppleScript: "+e);

            return false;
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
     * Implementation notes: this method moves the given file to the trash from a separate thread and returns
     * immediately. For this reason:
     * <ul>
     *  <li>it may return <code>true</code> even if the file has not successfully been moved to the trash
     *  <li>{@link #waitForPendingOperations()} should be used to know when the file has effectively been moved to the 
     * trash
     * </ul>
     */
    public boolean moveToTrash(AbstractFile file) {
        if(!canMoveToTrash(file))
            return false;

        synchronized(moveToTrashLock) {
            // Queue the given file
            queuedFiles.add(file);

            // Create a new thread and start it if one isn't already running
            if(moveToTrashThread ==null) {
                moveToTrashThread = new MoveToTrashThread();
                moveToTrashThread.start();
            }
        }
        
        return true;
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    public boolean canEmptyTrash() {
        return true;
    }

    public boolean emptyTrash() {
        return executeAppleScript(EMPTY_TRASH_APPLESCRIPT);
    }

    public boolean containsFile(AbstractFile file) {
        if(!(file.getTopAncestor() instanceof LocalFile))
            return false;

        // Look for a '.Trash' filename in the specified file's path
        FileURL fileURL = file.getURL();
        PathTokenizer pt = new PathTokenizer(fileURL.getPath(), fileURL.getPathSeparator(), false);

        while(pt.hasMoreFilenames()) {
            if(pt.nextFilename().equals(".Trash"))
                return true;
        }

        return false;
    }

    public void waitForPendingOperations() {
        synchronized(moveToTrashLock) {
            if(moveToTrashThread!=null) {
                try {
                    // Wait until moveToTrashThread wakes this thread up
                    moveToTrashLock.wait();
                }
                catch(InterruptedException e) {
                }
            }
        }
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Performs the actual job of moving files to the trash using AppleScript.
     *
     * <p>The thread starts by waiting {@link OSXTrash#REGROUP_PERIOD} milliseconds before moving them to give additional
     * files a chance to be queued and regrouped as a single AppleScript call. If some files were queued during
     * that period, the thread will wait an additional {@link OSXTrash#REGROUP_PERIOD}, and so on.<p>
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
    private class MoveToTrashThread extends Thread {

        public void run() {
            int queueSize;
            do {
                queueSize = queuedFiles.size();

                try {
                    Thread.sleep(REGROUP_PERIOD);
                }
                catch(InterruptedException e) {}
            }
            while(queueSize!=queuedFiles.size());


            synchronized(moveToTrashLock) {
                int nbFiles = queuedFiles.size();
                String appleScript = "tell application \"Finder\" to move {";
                for(int i=0; i<nbFiles; i++) {
                    appleScript += "posix file \""+((AbstractFile)queuedFiles.elementAt(i)).getAbsolutePath()+"\"";
                    if(i<nbFiles-1)
                        appleScript += ", ";
                }
                appleScript += "} to the trash";

                executeAppleScript(appleScript);

                queuedFiles.clear();
                // Wake up any thread waiting for this thread to be finished
                moveToTrashLock.notify();
                moveToTrashThread = null;
            }
        }
    }
}
