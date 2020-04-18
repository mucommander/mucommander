/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.desktop.macos;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.desktop.QueuedTrash;
import com.mucommander.ui.macos.AppleScript;
import com.sun.jna.platform.mac.MacFileUtils;

/**
 * OSXTrash provides access to the Mac OS X Finder's trash. Only local files (or locally mounted files) can be moved
 * to the trash.
 *
 * <p>
 *   <b>Implementation notes:</b><br/>
 *   <br/>
 *   This trash is implemented as a {@link com.mucommander.desktop.QueuedTrash} for several reasons:
 *   <ul>
 *    <li>the Finder plays a sound when it has been told to move a file to the trash and is done with it.
 *        Moving files to the trash repeatedly would play the sound as many times as the Finder has been told to move a
 *        file, which is obviously ugly.</li>
 *    <li>executing an AppleScript has a cost as it has to be compiled first. When files are moved repeatedly, it is
 *        more efficient to group files and execute only one AppleScript.</li>
 *   </ul>
 *   <br/>
 *   This class uses {@link com.mucommander.ui.macos.AppleScript} to interact with the trash.
 * </p>
 *
 * @see OSXTrashProvider
 * @author Maxence Bernard
 */
public class OSXTrash extends QueuedTrash {
	private static final Logger LOGGER = LoggerFactory.getLogger(OSXTrash.class);
	
    /** AppleScript that reveals the trash in Finder */
    private final static String REVEAL_TRASH_APPLESCRIPT =
        "tell application \"Finder\" to open trash\n" +
        "activate application \"Finder\"\n";

    /** AppleScript that counts and returns the number of items in Trash */
    private final static String COUNT_TRASH_ITEMS_APPLESCRIPT =
            "tell application \"Finder\" to return count of items in trash";

    /** AppleScript that empties the trash */
    private final static String EMPTY_TRASH_APPLESCRIPT = "tell application \"Finder\" to empty trash";

    private final static MacFileUtils macFileUtils = new MacFileUtils();

    //////////////////////////////////
    // AbstractTrash implementation //
    //////////////////////////////////

    /**
     * Implementation notes: returns <code>true</code> only for local files that are not archive entries.
     */
    @Override
    public boolean canMoveToTrash(AbstractFile file) {
        return file.getTopAncestor() instanceof LocalFile;
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    @Override
    public boolean canEmpty() {
        return true;
    }

    @Override
    public boolean empty() {
        return AppleScript.execute(EMPTY_TRASH_APPLESCRIPT, null);
    }

    @Override
    public boolean isTrashFile(AbstractFile file) {
        return (file.getTopAncestor() instanceof LocalFile)
            && (file.getAbsolutePath(true).indexOf("/.Trash/") != -1);
    }

    /**
     * Implementation notes: this method is implemented and returns <code>-1</code> only if an error ocurred while
     * retrieving the trash item count.
     */
    @Override
    public int getItemCount() {
        StringBuilder output = new StringBuilder();
        if(!AppleScript.execute(COUNT_TRASH_ITEMS_APPLESCRIPT, output))
            return -1;

        try {
            return Integer.parseInt(output.toString().trim());
        }
        catch(NumberFormatException e) {
            LOGGER.debug("Caught an exception", e);
            return -1;
        }
    }

    @Override
    public void open() {
        AppleScript.execute(REVEAL_TRASH_APPLESCRIPT, null);
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    @Override
    public boolean canOpen() {
        return true;
    }


    ////////////////////////////////
    // QueuedTrash implementation //
    ////////////////////////////////

    /**
     * Performs the actual job of moving files to the trash using JNA.
     */
    @Override
    protected boolean moveToTrash(List<AbstractFile> queuedFiles) {
        File[] files = queuedFiles.stream().map(AbstractFile::getAbsolutePath).map(File::new).toArray(File[]::new);
        try { macFileUtils.moveToTrash(files); }
        catch (IOException e) {
            LOGGER.error("failed to move files to trash", e);
            return false;
        }
        return true;
    }
}
