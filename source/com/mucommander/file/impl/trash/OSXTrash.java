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
import com.mucommander.ui.macosx.AppleScript;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

/**
 * OSXTrash provides access to the Mac OS X Finder's trash. Only local files (or locally mounted files) can be moved
 * to the trash.
 *
 * <p>
 *   <b>Implementation notes:</b><br/>
 *   <br/>
 *   This trash is implemented as a {@link QueuedTrash} for several reasons:
 *   <ul>
 *    <li>the Finder plays a sound when it has been told to move a file to the trash and is done with it.
 *        Moving files to the trash repeatedly would play the sound as many times as the Finder has been told to move a
 *        file, which is obviously ugly.</li>
 *    <li>executing an AppleScript has a cost as it has to be compiled first. When files are moved repeatedly, it is
 *        more efficient to group files and execute only one AppleScript.</li>
 *   </ul>
 *   <br/>
 *   This class uses {@link com.mucommander.ui.macosx.AppleScript} to interact with the trash.
 * </p>
 *
 * @author Maxence Bernard
 */
public class OSXTrash extends QueuedTrash {

    /** AppleScript that reveals the trash in Finder */
    private final static String REVEAL_TRASH_APPLESCRIPT =
        "tell application \"Finder\" to open trash\n" +
        "activate application \"Finder\"\n";

    /** AppleScript that counts and returns the number of items in Trash */
    private final static String COUNT_TRASH_ITEMS_APPLESCRIPT =
            "tell application \"Finder\" to return count of items in trash";

    /** AppleScript that empties the trash */
    private final static String EMPTY_TRASH_APPLESCRIPT = "tell application \"Finder\" to empty trash";

    /**
     * AppleScript that moves files to the trash, for versions of AppleScript (1.10 or lower )that do not allow Unicode
     * in the script itself (only MacRoman). As a result, this script is more complicated as the only way to deal with
     * Unicode text is to read them from a file. See http://www.satimage.fr/software/en/unicode_and_applescript.html
     * for more info about this workaround.
     */
    private final static String MOVE_TO_TRASH_APPLESCRIPT_NO_UNICODE =
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
        return AppleScript.execute(EMPTY_TRASH_APPLESCRIPT, null);
    }

    public boolean isTrashFile(AbstractFile file) {
        return (file.getTopAncestor() instanceof LocalFile)
            && (file.getAbsolutePath(true).indexOf("/.Trash/") != -1);
    }

    public int getItemCount() {
        StringBuffer output = new StringBuffer();
        if(!AppleScript.execute(COUNT_TRASH_ITEMS_APPLESCRIPT, output))
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
        AppleScript.execute(REVEAL_TRASH_APPLESCRIPT, null);
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
        String appleScript;

        // Simple script for AppleScript versions with Unicode support, i.e. that allows Unicode characters in the
        // script (AppleScript 2.0 / Mac OS X 10.5 or higher).
        if(AppleScript.getScriptEncoding().equals(AppleScript.UTF8)) {
            int nbFiles = queuedFiles.size();
            appleScript = "tell application \"Finder\" to move {";
            for(int i=0; i<nbFiles; i++) {
                appleScript += "posix file \""+((AbstractFile)queuedFiles.elementAt(i)).getAbsolutePath()+"\"";
                if(i<nbFiles-1)
                    appleScript += ", ";
            }
            appleScript += "} to the trash";

            return AppleScript.execute(appleScript, null);
        }
        // Script for AppleScript versions without Unicode support (AppleScript 1.10 / Mac OS X 10.4 or lower)
        else {
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
                appleScript = "set tmpFilePath to \""+tmpFile.getAbsolutePath()+"\"\n";
                appleScript += MOVE_TO_TRASH_APPLESCRIPT_NO_UNICODE;

                boolean success = AppleScript.execute(appleScript, null);

                // AppleScript has been executed, we can now safely close and delete the temporary file
                tmpFile.delete();

                return success;
            }
            catch(IOException e) {
                if(Debug.ON) Debug.trace("Caught IOException: "+e);

                if(tmpOut!=null) {
                    try { tmpOut.close(); }
                    catch(IOException e1) {
                        // There's not much we can do about it
                    }
                }

                if(tmpFile!=null) {
                    try { tmpFile.delete(); }
                    catch(IOException e2) {
                        // There's not much we can do about it
                    }
                }

                return false;
            }
        }

    }
}
