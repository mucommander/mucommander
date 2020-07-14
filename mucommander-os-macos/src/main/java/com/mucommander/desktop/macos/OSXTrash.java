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
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
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
        if (queuedFiles.isEmpty())
            return true;

        switch(type(queuedFiles)) {
        case 1:
            // JNA works fine for Apple File Systems
            return moveToTrashJna(queuedFiles);
        case -1:
            // Finder knows how to move files to the Trash of Samba shares, JNA not
            return moveToTrashAppleScript(queuedFiles);
        case 0:
        default:
            // Otherwise, try JNA and fallback to Finder if it fails
            if (moveToTrashJna(queuedFiles))
                return true;
            LOGGER.info("failed to move files to trash using JNA, trying Apple script");
            return moveToTrashAppleScript(queuedFiles);
        }
    }

    private int type(List<AbstractFile> queuedFiles) {
        List<String> fileStoreTypes = queuedFiles.stream()
                .map(file -> (File) file.getUnderlyingFileObject())
                .map(File::toPath)
                .map(path -> {
                    try {
                        return Files.getFileStore(path);
                    } catch (IOException e) {
                        LOGGER.error("failed to retrieve FileStore of {}", path, e);
                        return null;
                    }
                })
                .map(fs -> fs != null ? fs.type() : null)
                .collect(Collectors.toList());
        if (fileStoreTypes.stream().allMatch("apfs"::equals))
            return 1;
        if (fileStoreTypes.stream().anyMatch("smbfs"::equals))
            return -1;
        return 0;
    }

    private boolean moveToTrashJna(List<AbstractFile> queuedFiles) {
        File[] files = queuedFiles.stream().map(AbstractFile::getAbsolutePath).map(File::new).toArray(File[]::new);
        try { macFileUtils.moveToTrash(files); }
        catch (IOException e) {
            LOGGER.error("failed to move files to trash", e);
            return false;
        }
        return true;
    }

    private boolean moveToTrashAppleScript(List<AbstractFile> queuedFiles) {
        String appleScript;

        // Simple script for AppleScript versions with Unicode support, i.e. that allows Unicode characters in the
        // script (AppleScript 2.0 / Mac OS X 10.5 or higher).
        if(AppleScript.getScriptEncoding().equals(AppleScript.UTF8)) {
            appleScript = queuedFiles.stream()
                    .map(AbstractFile::getAbsolutePath)
                    .map(path -> String.format("posix file \"%s\"", path))
                    .collect(Collectors.joining(", ", "tell application \"Finder\" to move {", "} to the trash"));

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
                tmpOut = new OutputStreamWriter(tmpFile.getOutputStream(), "utf-8");

                for(int i=0; i<nbFiles; i++) {
                    tmpOut.write(queuedFiles.get(i).getAbsolutePath());
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
                LOGGER.debug("Caught IOException", e);

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
